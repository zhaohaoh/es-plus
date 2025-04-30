/*
 * Copyright (c) 2011-2020, baomidou (jobob@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.es.plus.core.wrapper.core;

import java.io.Serializable;
import java.util.Map;

/**
 * @author miemie
 * @since 2018-12-12
 */
public interface Update<Children, R,T> extends Serializable {


    /**
     * ignore
     */
    default Children set(R column, Object val) {
        return set(true, column, val);
    }

    /**
     * 设置 更新 SQL 的 SET 片段
     *
     * @param condition 是否加入 set
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children set(boolean condition, R column, Object val);
    
    /**
     * 设置 更新 SQL 的 SET 片段
     *
     * @param condition 是否加入 set
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children setEntity(boolean condition, T entity);
    
    /**
     * 设置 更新 实体
     */
    default Children setEntity(T entity) {
        return setEntity(true, entity);
    }
    
    ;

    default Children increment(R column, Long val) {
        return increment(true, column, val);
    }

    Children increment(boolean condition, R column, Long val);

    default Children setScipt(String script, Map<String, Object> sciptParams) {
        return setScipt(true, script,sciptParams);
    }

    Children setScipt(boolean condition, String script,Map<String, Object> sciptParams);
}
