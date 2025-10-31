package com.es.plus.autoconfigure;

import com.es.plus.autoconfigure.auto.EsAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({EsAutoConfiguration.class, EsIndexScanRegister.class})
public @interface EsIndexScan {
    
    /**
     * Base packages to scan for MyBatis interfaces. Note that only interfaces with at least one method will be
     * registered; concrete classes will be ignored.
     *
     * @return base package names for scanning mapper interface
     */
    String[] basePackages() default {};
    
}