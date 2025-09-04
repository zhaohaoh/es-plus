package com.es.plus.common.lock;

import com.es.plus.common.pojo.es.EpScript;

import java.util.HashMap;
import java.util.Map;

import static com.es.plus.constant.EsConstant.*;

public class EsReadLock extends ELock {
    
    public EsReadLock(ELockClient esPlusLockClient, String key) {
        super(esPlusLockClient, key);
    }
    
    @Override
    public boolean tryLock0() {
        Map<String, Object> data = new HashMap<>();
        data.put("read_lock_count", 1);
        data.put("lock_type", "s");
        data.put(GLOBAL_LOCK_EXPIRETIME, System.currentTimeMillis() + GLOBAL_LOCK_TIMEOUT * 1000);
        
        Map<String, Object> params = new HashMap<>();
        params.put("currentTime", System.currentTimeMillis());
        params.put("expireTime", (System.currentTimeMillis() + GLOBAL_LOCK_TIMEOUT * 1000));
        String script = "if(ctx._source." + GLOBAL_LOCK_EXPIRETIME + "<params.currentTime" + "){\n" +
                " ctx._source." + GLOBAL_LOCK_EXPIRETIME + "=params.expireTime" + ";  \n" +
                " ctx._source.read_lock_count = 1;\n" +
                " ctx._source.lock_type = 's';\n" +
                " return;\n" +
                " } \n" +
                "if(ctx._source.lock_type ==  \"s\"){ \n" +
                " ctx._source.read_lock_count += 1; \n" +
                " return; \n" +
                "} \n" +
                " ctx.op = 'noop'; \n" +
                " return; \n";
        
        EpScript painless = new EpScript(EpScript.ScriptType.INLINE, PAINLESS, script, params);
        return esPlusLockClient.upsertByScript(lockIndexName(), key, data, painless);
        
        
    }
    
    @Override
    public boolean tryLock0(String value) {
        return true;
    }
    
    @Override
    public void unlock0() {
        Map<String, Object> params = new HashMap<>();
        params.put("param", 0);
        //构建scipt语句
        String script = "if(ctx._source.lock_type == \"s\" && ctx._source.read_lock_count>=1){\n" +
                "            ctx._source.read_lock_count -= 1;\n" +
                "      }\n" +
                "if(ctx._source.read_lock_count>params.param) {" +
                "return;" +
                " }" +
                "      ctx.op = 'delete';";
        EpScript painless = new EpScript(EpScript.ScriptType.INLINE, PAINLESS, script, params);
        esPlusLockClient.updateByScript(lockIndexName(), key, painless);
    }
    
    @Override
    public String lockIndexName() {
        return "ep_read_write_lock";
    }
}