package com.es.plus.adapter.config;

import lombok.AllArgsConstructor;

/**
 * @Author: hzh
 * @Date: 2022/11/25 16:34
 *   启动时连接失败处理
 */
@AllArgsConstructor
public enum ConnectFailHandleEnum {
    //忽略失败异常继续启动
    IGNORE
    // 抛出异常
    ,THROW_EXCEPTION
}
