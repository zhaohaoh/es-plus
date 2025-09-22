package com.es.plus.web.controller;

import com.alibaba.excel.EasyExcelFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/es/file")
public class ExportController {
    
    
    @PostMapping("excelExport")
    public void excel(@RequestBody List<Map<String, Object>> export, HttpServletResponse httpServletResponse) throws IOException {
        downloadExcel(export, httpServletResponse);
    }
    
    /**
     * 导出excel
     */
    private void downloadExcel(List<Map<String, Object>> list, HttpServletResponse response) throws IOException {
        
        //response为HttpServletResponse对象
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        response.setHeader("Content-Disposition", "attachment;filename=file.xlsx");
        
        List<List<String>> head = createHead(list);
        EasyExcelFactory.write(response.getOutputStream()).head(head).sheet("Sheet1").doWrite(list);
    }
    // 创建表头（需与Map的Key对应）
    private List<List<String>> createHead(List<Map<String, Object>> list) {
        List<List<String>> head = new ArrayList<>();
        Map<String, Object> map = list.get(0);
        
        for (Map.Entry<String, Object> stringObjectEntry : map.entrySet()) {
            head.add(Collections.singletonList(stringObjectEntry.getKey())); // 对应Map中的Key
            head.add(Collections.singletonList(stringObjectEntry.getKey()));
            head.add(Collections.singletonList(stringObjectEntry.getKey()));
        }
        
        return head;
    }
}
