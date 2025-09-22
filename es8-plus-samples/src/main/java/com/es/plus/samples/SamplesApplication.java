package com.es.plus.samples;

import com.es.plus.autoconfigure.EsIndexScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EsIndexScan
public class SamplesApplication {
    public static void main(String[] args) {
        SpringApplication.run(SamplesApplication.class, args);
    }
}
