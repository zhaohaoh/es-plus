package com.es.plus.web.compile.core;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Kv {
    private String key;
    private String value;
    
}
