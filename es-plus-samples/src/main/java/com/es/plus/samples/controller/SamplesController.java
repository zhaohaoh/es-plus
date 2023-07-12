package com.es.plus.samples.controller;

import com.es.plus.core.statics.Es;
import com.es.plus.samples.dto.SamplesEsDTO;
import com.es.plus.samples.service.SamplesEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sample")
public class SamplesController {
    @Autowired
    private SamplesEsService samplesEsService;

    @PostMapping("agg")
    public void agg() {
        Es.chainIndex().createIndex(SamplesEsDTO.class).ping();
        samplesEsService.agg();
    }
    @PostMapping("search")
    public void search() {
        samplesEsService.search();
    }

}
