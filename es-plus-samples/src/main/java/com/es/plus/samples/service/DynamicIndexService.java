package com.es.plus.samples.service;

import com.es.plus.core.service.EsServiceImpl;
import com.es.plus.samples.dto.DynamicIndexDTO;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service(value = "dynamicIndexService")
public class DynamicIndexService extends EsServiceImpl<DynamicIndexDTO> {

    public String[] getDynamicIndex(){
        Date date1 = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
        String date = simpleDateFormat.format(date1);
        return new String[]{"dynamic_index_1","dynamic_index_2","dynamic_index_3"};
    }
}
