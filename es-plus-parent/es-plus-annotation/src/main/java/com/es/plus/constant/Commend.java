package com.es.plus.constant;

/**
 * 指令
 *
 * @author hzh
 * @date 2022/09/03
 */
public interface Commend {
    /**
     * 不执行
     */
    String NO_EXECUTE = "no_execute";
    /**
     * 执行映射更新的指令
     */
    String MAPPING_UPDATE = "mapping_update";
    /**
     * 执行重建
     */
    String REINDEX = "reindex";

}
