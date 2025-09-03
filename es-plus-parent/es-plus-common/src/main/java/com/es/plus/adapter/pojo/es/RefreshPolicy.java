package com.es.plus.adapter.pojo.es;



public enum RefreshPolicy {
    NONE("false"),
    IMMEDIATE("true"),
    WAIT_UNTIL("wait_for");
    
    private final String value;
    
    private RefreshPolicy(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public static RefreshPolicy parse(String value) {
        for (RefreshPolicy policy : values()) {
            if (policy.getValue().equals(value)) {
                return policy;
            }
        }
        
        if ("".equals(value)) {
            return IMMEDIATE;
        } else {
            throw new IllegalArgumentException("Unknown value for refresh: [" + value + "].");
        }
    }
}