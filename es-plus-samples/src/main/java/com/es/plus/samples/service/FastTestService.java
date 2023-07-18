package com.es.plus.samples.service;

import com.es.plus.adapter.params.EsResponse;
import com.es.plus.core.service.EsServiceImpl;
import com.es.plus.samples.dto.FastTestDTO;
import org.springframework.stereotype.Service;

@Service
public class FastTestService extends EsServiceImpl<FastTestDTO> {

    public void test() {
        EsResponse<FastTestDTO> test = esChainQueryWrapper().match(FastTestDTO::getText, "苹果").list();
        System.out.println(test);
    }
}
