package com.es.plus.samples.controller;

import com.es.plus.samples.service.SamplesEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("Sample")
public class SamplesController {
    @Autowired
    private SamplesEsService samplesEsService;

    @PostMapping("agg")
    public void agg() {
        samplesEsService.agg();
    }
    @PostMapping("search")
    public void search() {
        samplesEsService.search();
    }

}
