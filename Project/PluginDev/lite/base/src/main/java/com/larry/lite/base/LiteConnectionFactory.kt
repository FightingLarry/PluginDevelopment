package com.larry.lite.base

import com.squareup.okhttp.OkHttpClient
import java.net.HttpURLConnection
import java.net.MalformedURLException

interface LiteConnectionFactory {

    @Throws(MalformedURLException::class)
    fun openConnection(litePlugin: LitePlugin, url: String): HttpURLConnection

    val okHttpClient: OkHttpClient
}
