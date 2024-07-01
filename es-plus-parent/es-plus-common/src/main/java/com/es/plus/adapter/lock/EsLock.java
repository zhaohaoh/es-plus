package com.es.plus.adapter.lock;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;

import java.util.HashMap;
import java.util.Map;

import static com.es.plus.constant.EsConstant.*;

public class EsLock extends ELock {
    public EsLock(ELockClient esPlusLockClient, String key) {
        super(esPlusLockClient, key);
    }

    @Override
    public String lockIndexName() {
        return GLOBAL_LOCK;
    }

    @Override
    public boolean tryLock0() {
        Map<String, Object> data = new HashMap<>();
        data.put(GLOBAL_LOCK_EXPIRETIME, System.currentTimeMillis() + GLOBAL_LOCK_TIMEOUT * 1000);
        //自增
        Map<String, Object> params = new HashMap<>();
        //构建scipt语句
        params.put("currentTime", System.currentTimeMillis());
        params.put("expireTime", (System.currentTimeMillis() + GLOBAL_LOCK_TIMEOUT * 1000));
        String script = "if(ctx._source." + GLOBAL_LOCK_EXPIRETIME + "<params.currentTime" + "){\n" +
                " ctx._source." + GLOBAL_LOCK_EXPIRETIME + "=params.expireTime" + ";  \n" +
                " return;\n" +
                " } \n" +
                " ctx.op= 'noop';";

        Script painless = new Script(ScriptType.INLINE, PAINLESS, script, params);

        UpdateResponse update = esPlusLockClient.upsertByScript(lockIndexName(), key, data, painless);
        byte op = update.getResult().getOp();
        if (op == DocWriteResponse.Result.NOOP.getOp()) {
            return false;
        } else {
            return true;
        }

    }
    
    @Override
    public boolean tryLock0(String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(GLOBAL_LOCK_EXPIRETIME, System.currentTimeMillis() + GLOBAL_LOCK_TIMEOUT * 1000);
        data.put("value", value);
        //自增
        Map<String, Object> params = new HashMap<>();
        //构建scipt语句
        params.put("currentTime", System.currentTimeMillis());
        params.put("expireTime", (System.currentTimeMillis() + GLOBAL_LOCK_TIMEOUT * 1000));
        String script = "if(ctx._source." + GLOBAL_LOCK_EXPIRETIME + "<params.currentTime" + "){\n" +
                " ctx._source." + GLOBAL_LOCK_EXPIRETIME + "=params.expireTime" + ";  \n" +
                " return;\n" +
                " } \n" +
                " ctx.op= 'noop';";
    
        Script painless = new Script(ScriptType.INLINE, PAINLESS, script, params);
    
        UpdateResponse update = esPlusLockClient.upsertByScript(lockIndexName(), key, data, painless);
        byte op = update.getResult().getOp();
        if (op == DocWriteResponse.Result.NOOP.getOp()) {
            return false;
        } else {
            return true;
        }
    }
    
    
    
    @Override
    public void unlock0() {
        Map<String, Object> params = new HashMap<>();
        //构建scipt语句
        params.put("param", "delete");
        //构建scipt语句
        String script = "ctx.op = params.param;";
        Script painless = new Script(ScriptType.INLINE, PAINLESS, script, params);
        UpdateResponse update = esPlusLockClient.updateByScript(lockIndexName(), key, painless);
    }
    
  
}
