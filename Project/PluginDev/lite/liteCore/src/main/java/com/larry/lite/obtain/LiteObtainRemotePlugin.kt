package com.larry.lite.obtain

import android.graphics.Point
import android.os.Build
import android.os.Build.VERSION
import android.text.TextUtils
import android.view.WindowManager
import com.larry.lite.*
import com.larry.lite.base.LiteConnectionFactory
import com.larry.lite.base.LiteLaunch
import com.larry.lite.base.LiteNetworkType
import com.larry.lite.base.LiteStrategy
import com.larry.lite.network.NetworkError
import com.larry.lite.network.NetworkHelper
import com.larry.lite.utils.AndroidUtil
import com.larry.lite.utils.TelephonyManagerUtil
import com.squareup.okhttp.*
import com.squareup.okhttp.Request.Builder
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

class LiteObtainRemotePlugin(internal val mContext: LiteContext, liteConnectionFactory: LiteConnectionFactory) : ILiteObtainPlugin {
    internal val mClient: OkHttpClient
    internal var mCall: Call? = null

    init {
        this.mClient = liteConnectionFactory.okHttpClient
    }

    override fun obtain(callback: ILiteObtainPlugin.Callback): Int {
        if (!NetworkHelper.sharedHelper().isNetworkAvailable) {
            LiteLog.w("network not available!", *arrayOfNulls<Any>(0))
            return -11
        } else {
            val url = this.mContext.pluginConfigUrl
            if (TextUtils.isEmpty(url)) {
                throw RuntimeException("what!!! no plugin config url!!!")
            } else {
                val call = this.requestPlugins(url, this.createRequestBody())
                call.enqueue(LiteObtainRemotePlugin.ConfigurationCallBack(LiteObtainRemotePlugin.WrapCallback(callback)))
                this.mCall = call
                return 0
            }
        }
    }

    override fun cancel() {
        val call = this.mCall
        if (call != null && !call.isCanceled) {
            call.cancel()
        }

    }

    private fun requestPlugins(url: String, body: RequestBody): Call {
        LiteLog.v("requestPlugins url: %s", *arrayOf<Any>(url))
        val requestBuilder = Builder()
        val ua = this.mContext.userAgent
        if (!TextUtils.isEmpty(ua)) {
            requestBuilder.header("User-Agent", ua)
        }

        requestBuilder.url(url).post(body)
        val request = requestBuilder.build()
        return this.mClient.newCall(request)
    }

    private fun createRequestBody(): RequestBody {
        val context = this.mContext.applicationContext
        val vc = AndroidUtil.getVersionCode(context)
        val vn = AndroidUtil.getVersionName(context)
        val model = Build.MODEL
        val old = this.mContext.configuration
        var ts = 0L
        if (old != null) {
            ts = old.ts
        }

        val builder = FormEncodingBuilder()
        builder.add("from", context.packageName)
        builder.add("vc", vc.toString())
        builder.add("vn", vn)
        builder.add("model", model)
        builder.add("ts", ts.toString())
        val channel = this.mContext.channel
        if (!TextUtils.isEmpty(channel)) {
            builder.add("channel", channel)
        }

        val net = AndroidUtil.getNetworkTypeName(context)
        builder.add("net", net)
        builder.add("os_vn", VERSION.RELEASE)
        builder.add("os_vc", VERSION.SDK_INT.toString())
        val wm = context.getSystemService("window") as WindowManager
        if (wm != null) {
            val display = wm.defaultDisplay
            val size = Point()
            display.getSize(size)
            builder.add("sc",
                    String.format(Locale.US, "%d#%d", *arrayOf<Any>(Integer.valueOf(size.x), Integer.valueOf(size.y))))
        }

        val cf = context.resources.configuration
        val locale = cf.locale
        builder.add("lg", locale.toString())
        val si = TelephonyManagerUtil.getSubscriberId(context)
        builder.add("si", si ?: "")
        var ei = TelephonyManagerUtil.getDeviceId(context)
        if (!TelephonyManagerUtil.isIMEI(ei)) {
            ei = ""
        }

        builder.add("ei", ei)
        val params = this.mContext.networkCommonParams
        if (params != null) {
            val var17 = params.entries.iterator()

            while (var17.hasNext()) {
                val entry = var17.next()
                builder.add(entry.key as String, entry.value as String)
            }
        }

        return builder.build()
    }

    private inner class WrapCallback(c: ILiteObtainPlugin.Callback) : ILiteObtainPlugin.Callback {
        private val reference: WeakReference<ILiteObtainPlugin.Callback>

        init {
            this.reference = WeakReference(c)
        }

        override fun onObtainResult(err: Int, litePluginsConfigInfo: LitePluginsConfigInfo) {
            this@LiteObtainRemotePlugin.mCall = null
            val call = this.reference.get() as ILiteObtainPlugin.Callback
            call?.onObtainResult(err, litePluginsConfigInfo)

        }
    }

    private class ConfigurationCallBack internal constructor(private val mCallback: ILiteObtainPlugin.Callback?) : com.squareup.okhttp.Callback {

        override fun onFailure(request: Request, e: IOException) {
            LiteLog.v("plugin onFailure", *arrayOfNulls<Any>(0))
            LiteLog.printStackTrace(e)
            val err = -2
            this.onResultStatus(err, null, 0L)
        }

        @Throws(IOException::class)
        override fun onResponse(response: Response) {
            if (!response.isSuccessful) {
                val ret: Byte
                if (response.code() == 404) {
                    ret = -3
                } else {
                    ret = -1
                }

                this.onResultStatus(ret.toInt(), null, 0L)
            }

            val body = response.body()
            val result = body.string()
            LiteLog.i("plugin config onResponse %s", *arrayOf<Any>(result))
            if (!TextUtils.isEmpty(result)) {
                try {
                    this.parse(result)
                } catch (var6: Exception) {
                    this.onResultStatus(NetworkError.FAIL_IO_ERROR, null, 0L)
                }

            } else {
                this.onResultStatus(NetworkError.FAIL_IO_ERROR, null, 0L)
            }

        }

        @Throws(JSONException::class)
        private fun parse(result: String) {
            val jsonObject = JSONObject(result)
            val cr = LiteObtainRemotePlugin.parseResult(jsonObject)
            if (cr.status == 0) {
                this.onResultStatus(NetworkError.SUCCESS, cr.plugins, cr.ts)
            } else if (cr.status == -1) {
                this.onResultStatus(ILiteObtainPlugin.ERR_CONFIG_NO_CHANGED, null, 0L)
            } else {
                this.onResultStatus(NetworkError.FAIL_IO_ERROR, null, 0L)
            }

        }

        private fun onResultStatus(err: Int, plugins: List<LiteStub>?, timestamp: Long) {
            LiteLog.i("callBack result %d", *arrayOf<Any>(Integer.valueOf(err)))
            if (this.mCallback != null) {

                val litePluginsConfigInfo = LitePluginsConfigInfo()
                litePluginsConfigInfo.plugins = plugins
                litePluginsConfigInfo.ts = timestamp
                litePluginsConfigInfo.type = LiteConfigType.Remote

                this.mCallback.onObtainResult(err, litePluginsConfigInfo)
            }

        }
    }

    internal class ConfigurationResult {
        var status: Int = 0
        var cause: String? = null
        var plugins: List<LiteStub>? = null
        var ts: Long = 0
    }

    companion object {

        private fun createLaunchStrategy(limit: Int, launch: String, launchParam: String, network: String): LiteStrategy {
            val liteLaunch = LiteLaunch.valueOf(launch)
            val liteNetworkType = LiteNetworkType.valueOf(network)
            val modeExtra = parseLaunchParam(liteLaunch, launchParam)
            return LiteStrategy.newBuilder().setLimit(limit).setMode(liteLaunch).setNetworkLimit(liteNetworkType)
                    .setModeExtra(modeExtra).build()
        }

        private fun parseLaunchParam(mode: LiteLaunch, launchParam: String): Int {
            var launchParam = launchParam
            var modeExtra: Int
            if (mode == LiteLaunch.Periodicity) {
                modeExtra = Integer.parseInt(launchParam)
                when (modeExtra) {
                    1 -> modeExtra = 3600000
                    12 -> modeExtra = 43200000
                    24 -> modeExtra = 86400000
                    168 -> modeExtra = 604800000
                    else -> throw RuntimeException("launchParam $modeExtra not match for mode $mode")
                }
            } else {
                if (mode != LiteLaunch.KeyEvent) {
                    throw RuntimeException("Unsupported mode " + mode)
                }

                launchParam = launchParam.toLowerCase()
                if (TextUtils.equals("start", launchParam)) {
                    modeExtra = 1
                } else if (TextUtils.equals("background", launchParam)) {
                    modeExtra = 3
                } else {
                    if (!TextUtils.equals("upgrade", launchParam)) {
                        throw RuntimeException("launchParam $launchParam not match for mode $mode")
                    }

                    modeExtra = 2
                }
            }

            return modeExtra
        }

        @Throws(JSONException::class)
        internal fun parseResult(json: JSONObject): LiteObtainRemotePlugin.ConfigurationResult {
            val result = LiteObtainRemotePlugin.ConfigurationResult()
            if (json.has("status") && json.has("msg")) {
                val status = json.getInt("status")
                val msg = json.optString("msg")
                LiteLog.i("status: %d, msg: %s", *arrayOf(Integer.valueOf(status), msg))
                result.status = status
                result.cause = msg
            }

            if (result.status == 0) {
                val data: JSONObject
                if (json.has("data")) {
                    data = json.getJSONObject("data")
                } else {
                    data = json
                }

                result.ts = data.optLong("ts", 0L)
                val plugins = data.getJSONArray("plugins")
                val length = plugins.length()
                val liteStubs = ArrayList(length)

                for (i in 0..length - 1) {
                    val liteStub = LiteStub()
                    val `object` = plugins.getJSONObject(i)
                    liteStub.id = Integer.parseInt(`object`.getString("id"))
                    liteStub.url = `object`.optString("url")
                    liteStub.size = `object`.getLong("size")
                    liteStub.md5 = `object`.getString("md5")
                    liteStub.name = `object`.optString("name")
                    liteStub.desc = `object`.optString("description")
                    liteStub.path = `object`.optString("path")
                    val limit = `object`.optInt("limit", 0)
                    val launch = `object`.getString("launch")
                    val launchParam = `object`.getString("launchParam")
                    val network = `object`.optString("network", "WIFI")
                    liteStub.strategy = createLaunchStrategy(limit, launch, launchParam, network)
                    liteStubs.add(liteStub)
                }

                result.plugins = liteStubs
            }

            return result
        }
    }
}
