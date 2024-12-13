package com.es.plus.samples.service;

import com.es.plus.adapter.params.EsAggResponse;
import com.es.plus.adapter.params.EsResponse;
import com.es.plus.core.service.EsServiceImpl;
import com.es.plus.core.statics.Es;
import com.es.plus.core.wrapper.aggregation.EsAggWrapper;
import com.es.plus.core.wrapper.chain.EsChainLambdaQueryWrapper;
import com.es.plus.samples.dto.FastTestDTO;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class FastTestService extends EsServiceImpl<FastTestDTO> {

    public void test() {
        EsResponse<FastTestDTO> test = esChainQueryWrapper().match(FastTestDTO::getText, "苹果").search();
        System.out.println(test);
    }
    
    public void save() {
        //
        for (int i = 800000010; i <800000020; i++) {
            List<FastTestDTO> fastTestDTOs=new ArrayList<>();
            FastTestDTO fastTestDTO = new FastTestDTO();
            fastTestDTO.setId((long) i);
            fastTestDTO.setText("特殊的8");
            fastTestDTO.setAge(18L);
            fastTestDTO.setUsername("特殊的8");
            fastTestDTO.setUsernameTest("特殊的8");
            fastTestDTO.setCreateTime(new Date());
            fastTestDTOs.add(fastTestDTO);
            Es.chainUpdate(FastTestDTO.class).saveBatch(fastTestDTOs);
        }
    }

    public void agg() {
        EsChainLambdaQueryWrapper<FastTestDTO> fastTestDTOEsChainLambdaQueryWrapper = esChainQueryWrapper();
        EsAggWrapper<FastTestDTO> fastTestDTOEsAggWrapper = fastTestDTOEsChainLambdaQueryWrapper.esAggWrapper();
        EsAggWrapper<FastTestDTO> gsdgdsf = fastTestDTOEsAggWrapper.terms("gsdgdsf");
        EsAggResponse<FastTestDTO> aggregations = fastTestDTOEsChainLambdaQueryWrapper.aggregations();
        System.out.println(gsdgdsf);
    }
    
    public void delete() {
        Es.chainUpdate(FastTestDTO.class).removeByIds(Collections.singletonList("800000006"));
    }
    
    public void update() {
        FastTestDTO fastTestDTO = new FastTestDTO();
        fastTestDTO.setId(800000005L);
        fastTestDTO.setText("我该成果了2222");
        Es.chainUpdate(FastTestDTO.class).update(fastTestDTO);
    }
    
    public void updateBy() {
        
        BulkByScrollResponse bulkByScrollResponse = Es.chainUpdate(FastTestDTO.class)
                .terms("id", "800000005", "800000004").set("text", "新结果哦").updateByQuery();
   
        
    }
    
    public void saveAsync() {
        FastTestDTO fastTestDTO = new FastTestDTO();
        fastTestDTO.setId(1L);
        fastTestDTO.setText("asdasdasdsa");
         Es.chainUpdate(FastTestDTO.class).saveBatchAsyncProcessor(Collections.singletonList(fastTestDTO));
    }
    
    public void updateAsync() {
        FastTestDTO fastTestDTO = new FastTestDTO();
        fastTestDTO.setId(1L);
        fastTestDTO.setText("ssssss");
        Es.chainUpdate(FastTestDTO.class).updateBatchAsyncProcessor(Collections.singletonList(fastTestDTO));
    }
    public void saveOrUpdateAsync() {
        FastTestDTO fastTestDTO = new FastTestDTO();
        fastTestDTO.setId(1L);
        fastTestDTO.setText("bvvbdfbfd");
        Es.chainUpdate(FastTestDTO.class).saveOrUpdateBatchAsyncProcessor(Collections.singletonList(fastTestDTO));
    }
}
