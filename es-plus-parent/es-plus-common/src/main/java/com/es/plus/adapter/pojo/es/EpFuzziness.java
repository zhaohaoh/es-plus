package com.es.plus.adapter.pojo.es;

/**
 * 自定义模糊匹配类
 */
public class EpFuzziness {
    
    public static final EpFuzziness ZERO = new EpFuzziness("0");
    public static final EpFuzziness ONE = new EpFuzziness("1");
    public static final EpFuzziness TWO = new EpFuzziness("2");
    public static final EpFuzziness AUTO = new EpFuzziness("AUTO");
    
    private final String fuzziness;
    
    private EpFuzziness(String fuzziness) {
        this.fuzziness = fuzziness;
    }
    
    public static EpFuzziness build(String fuzziness) {
        return new EpFuzziness(fuzziness);
    }
    
    public static EpFuzziness build(int fuzziness) {
        return new EpFuzziness(String.valueOf(fuzziness));
    }
    
    public String getFuzziness() {
        return fuzziness;
    }
    
    @Override
    public String toString() {
        return fuzziness;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EpFuzziness that = (EpFuzziness) obj;
        return fuzziness.equals(that.fuzziness);
    }
    
    @Override
    public int hashCode() {
        return fuzziness.hashCode();
    }
}