package com.es.plus.common.pojo.es.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EpClient<T> {
    
    private T orginalClient;
}