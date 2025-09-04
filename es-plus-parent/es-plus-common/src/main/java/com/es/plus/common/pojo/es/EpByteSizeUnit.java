//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.es.plus.common.pojo.es;
public enum EpByteSizeUnit   {
    BYTES {
        public long toBytes(long size) {
            return size;
        }

        public long toKB(long size) {
            return size / 1024L;
        }

        public long toMB(long size) {
            return size / 1048576L;
        }

        public long toGB(long size) {
            return size / 1073741824L;
        }

        public long toTB(long size) {
            return size / 1099511627776L;
        }

        public long toPB(long size) {
            return size / 1125899906842624L;
        }

        public String getSuffix() {
            return "b";
        }
    },
    KB {
        public long toBytes(long size) {
            return x(size, 1024L, 9007199254740991L);
        }

        public long toKB(long size) {
            return size;
        }

        public long toMB(long size) {
            return size / 1024L;
        }

        public long toGB(long size) {
            return size / 1048576L;
        }

        public long toTB(long size) {
            return size / 1073741824L;
        }

        public long toPB(long size) {
            return size / 1099511627776L;
        }

        public String getSuffix() {
            return "kb";
        }
    },
    MB {
        public long toBytes(long size) {
            return x(size, 1048576L, 8796093022207L);
        }

        public long toKB(long size) {
            return x(size, 1024L, 9007199254740991L);
        }

        public long toMB(long size) {
            return size;
        }

        public long toGB(long size) {
            return size / 1024L;
        }

        public long toTB(long size) {
            return size / 1048576L;
        }

        public long toPB(long size) {
            return size / 1073741824L;
        }

        public String getSuffix() {
            return "mb";
        }
    },
    GB {
        public long toBytes(long size) {
            return x(size, 1073741824L, 8589934591L);
        }

        public long toKB(long size) {
            return x(size, 1048576L, 8796093022207L);
        }

        public long toMB(long size) {
            return x(size, 1024L, 9007199254740991L);
        }

        public long toGB(long size) {
            return size;
        }

        public long toTB(long size) {
            return size / 1024L;
        }

        public long toPB(long size) {
            return size / 1048576L;
        }

        public String getSuffix() {
            return "gb";
        }
    },
    TB {
        public long toBytes(long size) {
            return x(size, 1099511627776L, 8388607L);
        }

        public long toKB(long size) {
            return x(size, 1073741824L, 8589934591L);
        }

        public long toMB(long size) {
            return x(size, 1048576L, 8796093022207L);
        }

        public long toGB(long size) {
            return x(size, 1024L, 9007199254740991L);
        }

        public long toTB(long size) {
            return size;
        }

        public long toPB(long size) {
            return size / 1024L;
        }

        public String getSuffix() {
            return "tb";
        }
    },
    PB {
        public long toBytes(long size) {
            return x(size, 1125899906842624L, 8191L);
        }

        public long toKB(long size) {
            return x(size, 1099511627776L, 8388607L);
        }

        public long toMB(long size) {
            return x(size, 1073741824L, 8589934591L);
        }

        public long toGB(long size) {
            return x(size, 1048576L, 8796093022207L);
        }

        public long toTB(long size) {
            return x(size, 1024L, 9007199254740991L);
        }

        public long toPB(long size) {
            return size;
        }

        public String getSuffix() {
            return "pb";
        }
    };

    static final long C0 = 1L;
    static final long C1 = 1024L;
    static final long C2 = 1048576L;
    static final long C3 = 1073741824L;
    static final long C4 = 1099511627776L;
    static final long C5 = 1125899906842624L;
    static final long MAX = Long.MAX_VALUE;

    private EpByteSizeUnit() {
    }

    public static EpByteSizeUnit fromId(int id) {
        if (id >= 0 && id < values().length) {
            return values()[id];
        } else {
            throw new IllegalArgumentException("No byte size unit found for id [" + id + "]");
        }
    }

    static long x(long d, long m, long over) {
        if (d > over) {
            return Long.MAX_VALUE;
        } else {
            return d < -over ? Long.MIN_VALUE : d * m;
        }
    }

    public abstract long toBytes(long var1);

    public abstract long toKB(long var1);

    public abstract long toMB(long var1);

    public abstract long toGB(long var1);

    public abstract long toTB(long var1);

    public abstract long toPB(long var1);

    public abstract String getSuffix();

    
}
