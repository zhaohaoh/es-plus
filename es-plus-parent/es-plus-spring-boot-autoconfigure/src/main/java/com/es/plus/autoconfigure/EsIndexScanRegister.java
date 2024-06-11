package com.es.plus.autoconfigure;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

public class EsIndexScanRegister implements ImportBeanDefinitionRegistrar {
    
    
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry,
            BeanNameGenerator importBeanNameGenerator) {
        
        AnnotationAttributes scanAttrs = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(EsIndexScan.class.getName()));
        String[] basePackages = scanAttrs.getStringArray("basePackages");
        String className = importingClassMetadata.getClassName();
        String packages = StringUtils.substringBeforeLast(className, ".");
        if (basePackages.length<=0){
            basePackages=new String[]{packages};
        }
      
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(IndexScanProccess.class);
        builder.addPropertyValue("basePackage",basePackages);
        builder.addDependsOn("esPlusClientFacade");
        registry.registerBeanDefinition("indexScanProccess", builder.getBeanDefinition());
    }
    
}
