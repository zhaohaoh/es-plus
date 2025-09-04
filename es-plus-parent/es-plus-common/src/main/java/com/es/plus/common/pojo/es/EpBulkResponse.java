package com.es.plus.common.pojo.es;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EpBulkResponse {
    
    private long total;
    
    private long updated;
    
    private long created;
    
    private long deleted;
    
    private int batches;
    
    private long versionConflicts;
    
    private long noops;
    
    private long bulkRetries;
    
    private long searchRetries;
    
    private long took;
    
    private List<String> failIds=new ArrayList<>();
}
