package com.larry.lite.base

import java.io.IOException

interface LitePlugin {

    fun onCreate(litePluginPeer: LitePluginPeer)

    @Throws(IOException::class)
    fun execute(litePluginPeer: LitePluginPeer, liteConnectionFactory: LiteConnectionFactory): Int

    fun onDestroy(litePluginPeer: LitePluginPeer)

}
