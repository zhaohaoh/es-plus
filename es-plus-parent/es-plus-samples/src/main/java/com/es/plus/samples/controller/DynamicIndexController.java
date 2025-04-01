//package com.es.plus.samples.controller;
//
//
//import com.es.plus.adapter.params.EsResponse;
//import com.es.plus.core.statics.Es;
//import com.es.plus.samples.dto.DynamicIndexDTO;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("dynamicIndex")
//public class DynamicIndexController {
////    @Autowired
////    private DynamicIndexService dynamicIndexService;
//
//    @PostMapping("search")
//    public void search() {
//        EsResponse<DynamicIndexDTO> search = Es.chainQuery(DynamicIndexDTO.class).search();
//        System.out.println(search);
//    }
//
////    @PostMapping("create")
////    public void create() {
////        DynamicIndexDTO dynamicIndexDTO = new DynamicIndexDTO();
////        dynamicIndexDTO.setId(1L);
////        dynamicIndexDTO.setText("dasdasad");
////        dynamicIndexService.updateById(dynamicIndexDTO,"abc");
////    }
//}
