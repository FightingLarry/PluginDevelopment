package com.larry.lite;

/**
 * Created by Larry on 2017/5/8.
 */

public class LaunchStrategy {

    public static final int PERIODICITY_PARAM_HOUR = 3600000;
    public static final int PERIODICITY_PARAM_HALF_DAY = 43200000;
    public static final int PERIODICITY_PARAM_DAY = 86400000;
    public static final int PERIODICITY_PARAM_WEEK = 604800000;
    public static final int KEYEVENT_PARAM_START = 1;
    public static final int KEYEVENT_PARAM_UPGRADE = 2;
    public static final int KEYEVENT_PARAM_BACKGROUND = 3;
    public final LaunchMode mode;
    public final NetworkType networkLimit;
    public final int modeExtra;
    public final int limit;

    private LaunchStrategy(LaunchMode m, NetworkType nt, int mp, int l) {
        this.mode = m;
        this.networkLimit = nt;
        this.modeExtra = mp;
        this.limit = l;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;

        LaunchStrategy that = (LaunchStrategy) o;

        if (this.modeExtra != that.modeExtra) return false;
        if (this.limit != that.limit) return false;
        if (this.mode != that.mode) return false;
        return this.networkLimit == that.networkLimit;
    }

    public int hashCode() {
        int result = this.mode.hashCode();
        result = 31 * result + this.networkLimit.hashCode();
        result = 31 * result + this.modeExtra;
        result = 31 * result + this.limit;
        return result;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String toString() {
        return "{limit=" + this.limit + ", mode=" + this.mode + ", networkLimit=" + this.networkLimit + ", modeExtra="
                + this.modeExtra + '}';
    }

    public static class Builder {
        public LaunchMode mode;
        public NetworkType networkLimit;
        public int modeExtra;
        public int limit;

        Builder() {
            this.mode = LaunchMode.Periodicity;
            this.modeExtra = 604800000;
            this.networkLimit = NetworkType.WIFI;
            this.limit = 0;
        }

        public Builder setLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder setMode(LaunchMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder setModeExtra(int extra) {
            this.modeExtra = extra;
            return this;
        }

        public Builder setNetworkLimit(NetworkType networkLimit) {
            this.networkLimit = networkLimit;
            return this;
        }

        public LaunchStrategy build() {
            if ((this.mode == null) || (this.networkLimit == null))
                throw new NullPointerException("mode or networkLimit is null");
            return new LaunchStrategy(this.mode, this.networkLimit, this.modeExtra, this.limit);
        }
    }
}
