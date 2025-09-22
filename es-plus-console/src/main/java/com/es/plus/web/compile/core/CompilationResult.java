package com.es.plus.web.compile.core;

import lombok.Data;

@Data
public class CompilationResult {
    
    /**
     * 编译完成的类
     */
    private Class<?> compiledClass;
    
    
    private byte[] classByte;
    
    public CompilationResult(Class<?> compiledClass) {
        this.compiledClass = compiledClass;
    }
}
