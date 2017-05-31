package com.larry.lite.base;

public class LiteStrategy {
    public static final int PERIODICITY_PARAM_HOUR = 3600000;
    public static final int PERIODICITY_PARAM_HALF_DAY = 43200000;
    public static final int PERIODICITY_PARAM_DAY = 86400000;
    public static final int PERIODICITY_PARAM_WEEK = 604800000;
    public static final int KEYEVENT_PARAM_START = 1;
    public static final int KEYEVENT_PARAM_UPGRADE = 2;
    public static final int KEYEVENT_PARAM_BACKGROUND = 3;
    public final LiteLaunch mode;
    public final LiteNetworkType networkLimit;
    public final int modeExtra;
    public final int limit;

    private LiteStrategy(LiteLaunch m, LiteNetworkType nt, int mp, int l) {
        this.mode = m;
        this.networkLimit = nt;
        this.modeExtra = mp;
        this.limit = l;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            LiteStrategy that = (LiteStrategy) o;
            return this.modeExtra != that.modeExtra
                    ? false
                    : (this.limit != that.limit
                            ? false
                            : (this.mode != that.mode ? false : this.networkLimit == that.networkLimit));
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.mode.hashCode();
        result = 31 * result + this.networkLimit.hashCode();
        result = 31 * result + this.modeExtra;
        result = 31 * result + this.limit;
        return result;
    }

    public static LiteStrategy.Builder newBuilder() {
        return new LiteStrategy.Builder();
    }

    public String toString() {
        return "{limit=" + this.limit + ", mode=" + this.mode + ", networkLimit=" + this.networkLimit + ", modeExtra="
                + this.modeExtra + '}';
    }

    public static class Builder {
        public LiteLaunch mode;
        public LiteNetworkType networkLimit;
        public int modeExtra;
        public int limit;

        Builder() {
            this.mode = LiteLaunch.Periodicity;
            this.modeExtra = 604800000;
            this.networkLimit = LiteNetworkType.WIFI;
            this.limit = 0;
        }

        public LiteStrategy.Builder setLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public LiteStrategy.Builder setMode(LiteLaunch mode) {
            this.mode = mode;
            return this;
        }

        public LiteStrategy.Builder setModeExtra(int extra) {
            this.modeExtra = extra;
            return this;
        }

        public LiteStrategy.Builder setNetworkLimit(LiteNetworkType networkLimit) {
            this.networkLimit = networkLimit;
            return this;
        }

        public LiteStrategy build() {
            if (this.mode != null && this.networkLimit != null) {
                return new LiteStrategy(this.mode, this.networkLimit, this.modeExtra, this.limit);
            } else {
                throw new NullPointerException("mode or networkLimit is null");
            }
        }
    }
}
