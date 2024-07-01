package com.es.plus.adapter.lock;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;

import java.util.HashMap;
import java.util.Map;

import static com.es.plus.constant.EsConstant.*;

/**
 * @Author: hzh
 * @Date: 2022/9/13 10:01
 */
public class EsWriteLock extends ELock {
    protected EsWriteLock(ELockClient esPlusLockClient, String key) {
        super(esPlusLockClient, key);
    }

    @Override
    public boolean tryLock0() {
        Map<String, Object> data = new HashMap<>();
        data.put("lock_type", "x");
        data.put(GLOBAL_LOCK_EXPIRETIME, System.currentTimeMillis() + GLOBAL_LOCK_TIMEOUT * 1000);
        //自增
        Map<String, Object> params = new HashMap<>();
        //构建scipt语句
        params.put("currentTime", System.currentTimeMillis());
        params.put("expireTime", (System.currentTimeMillis() + GLOBAL_LOCK_TIMEOUT * 1000));
        String lockScript = "if(ctx._source." + GLOBAL_LOCK_EXPIRETIME + "<params.currentTime" + "){\n" +
                " ctx._source." + GLOBAL_LOCK_EXPIRETIME + "=params.expireTime" + ";  \n" +
                " ctx._source.lock_type = 'x';\n" +
                " return;\n" +
                " } \n" +
                " ctx.op= 'noop';";
        Script painless = new Script(ScriptType.INLINE, PAINLESS, lockScript, params);
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
        return true;
    }
    
    @Override
    public void unlock0() {
        Map<String, Object> params = new HashMap<>();
        params.put("param", "delete");
        //构建scipt语句
        String script = "ctx.op = params.param;";
        Script painless = new Script(ScriptType.INLINE, PAINLESS, script, params);
        UpdateResponse update = esPlusLockClient.updateByScript(lockIndexName(), key, painless);
    }

    @Override
    public String lockIndexName() {
        return "ep_read_write_lock";
    }

}
