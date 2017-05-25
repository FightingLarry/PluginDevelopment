package com.larry.lite.base

class LiteStrategy private constructor(val mode: LiteLaunch, val networkLimit: LiteNetworkType, val modeExtra: Int, val limit: Int) {

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        } else if (o != null && this.javaClass == o.javaClass) {
            val that = o as LiteStrategy?
            return if (this.modeExtra != that!!.modeExtra)
                false
            else
                if (this.limit != that.limit)
                    false
                else
                    if (this.mode !== that.mode) false else this.networkLimit === that.networkLimit
        } else {
            return false
        }
    }

    override fun hashCode(): Int {
        var result = this.mode.hashCode()
        result = 31 * result + this.networkLimit.hashCode()
        result = 31 * result + this.modeExtra
        result = 31 * result + this.limit
        return result
    }

    override fun toString(): String {
        return "{limit=" + this.limit + ", mode=" + this.mode + ", networkLimit=" + this.networkLimit + ", modeExtra=" + this.modeExtra + "}"
    }

    class Builder internal constructor() {
        var mode: LiteLaunch? = null
        var networkLimit: LiteNetworkType? = null
        var modeExtra: Int = 0
        var limit: Int = 0

        init {
            this.mode = LiteLaunch.Periodicity
            this.modeExtra = 604800000
            this.networkLimit = LiteNetworkType.WIFI
            this.limit = 0
        }

        fun setLimit(limit: Int): LiteStrategy.Builder {
            this.limit = limit
            return this
        }

        fun setMode(mode: LiteLaunch): LiteStrategy.Builder {
            this.mode = mode
            return this
        }

        fun setModeExtra(extra: Int): LiteStrategy.Builder {
            this.modeExtra = extra
            return this
        }

        fun setNetworkLimit(networkLimit: LiteNetworkType): LiteStrategy.Builder {
            this.networkLimit = networkLimit
            return this
        }

        fun build(): LiteStrategy {
            if (this.mode != null && this.networkLimit != null) {
                return LiteStrategy(this.mode!!, this.networkLimit!!, this.modeExtra, this.limit)
            } else {
                throw NullPointerException("mode or networkLimit is null")
            }
        }
    }

    companion object {
        val PERIODICITY_PARAM_HOUR = 3600000
        val PERIODICITY_PARAM_HALF_DAY = 43200000
        val PERIODICITY_PARAM_DAY = 86400000
        val PERIODICITY_PARAM_WEEK = 604800000
        val KEYEVENT_PARAM_START = 1
        val KEYEVENT_PARAM_UPGRADE = 2
        val KEYEVENT_PARAM_BACKGROUND = 3

        fun newBuilder(): LiteStrategy.Builder {
            return LiteStrategy.Builder()
        }
    }
}
