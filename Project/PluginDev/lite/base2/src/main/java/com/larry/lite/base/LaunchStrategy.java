package com.larry.lite.base;

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
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            LaunchStrategy that = (LaunchStrategy) o;
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

    public static LaunchStrategy.Builder newBuilder() {
        return new LaunchStrategy.Builder();
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

        public LaunchStrategy.Builder setLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public LaunchStrategy.Builder setMode(LaunchMode mode) {
            this.mode = mode;
            return this;
        }

        public LaunchStrategy.Builder setModeExtra(int extra) {
            this.modeExtra = extra;
            return this;
        }

        public LaunchStrategy.Builder setNetworkLimit(NetworkType networkLimit) {
            this.networkLimit = networkLimit;
            return this;
        }

        public LaunchStrategy build() {
            if (this.mode != null && this.networkLimit != null) {
                return new LaunchStrategy(this.mode, this.networkLimit, this.modeExtra, this.limit);
            } else {
                throw new NullPointerException("mode or networkLimit is null");
            }
        }
    }
}
