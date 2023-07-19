package com.es.plus.samples.service;

import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.EsResponse;
import com.es.plus.core.service.EsServiceImpl;
import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.chain.EsChainLambdaQueryWrapper;
import com.es.plus.samples.dto.FastTestDTO;
import org.springframework.stereotype.Service;

@Service
public class FastTestService extends EsServiceImpl<FastTestDTO> {

    public void test() {
        EsResponse<FastTestDTO> test = esChainQueryWrapper().match(FastTestDTO::getText, "苹果").list();
        System.out.println(test);
    }

    public void agg() {
        EsChainLambdaQueryWrapper<FastTestDTO> fastTestDTOEsChainLambdaQueryWrapper = esChainQueryWrapper();
        EsAggWrapper<FastTestDTO> fastTestDTOEsAggWrapper = fastTestDTOEsChainLambdaQueryWrapper.esAggWrapper();
        EsAggWrapper<FastTestDTO> gsdgdsf = fastTestDTOEsAggWrapper.terms("gsdgdsf");
        EsAggResponse<FastTestDTO> aggregations = fastTestDTOEsChainLambdaQueryWrapper.aggregations();
        System.out.println(gsdgdsf);
    }
}
