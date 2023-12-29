package job.match.info;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.ArrayList;
import java.util.List;

public class Redis {

    String keyFinal = "finalResult";
    JedisPool pool = new JedisPool("localhost", 6379);
    Jedis jedis;
    Log log = new Log();

    public String[] getKeysByPattern(String pattern) {
        String cursor = ScanParams.SCAN_POINTER_START;
        ScanParams scanParams = new ScanParams().match(pattern).count(1000);

        List<String> keys = new ArrayList<>();
        do {
            ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
            cursor = scanResult.getCursor();
            keys.addAll(scanResult.getResult());
        } while (!cursor.equals(ScanParams.SCAN_POINTER_START));

        return keys.toArray(new String[0]);
    }

    public boolean connect() {
        try {
            jedis = pool.getResource();
            return true;
        } catch (Exception e) {
            log.failLog("Redis 연결 실패 " + e.getMessage());
            return false;
        }
    }

    public MatchIdRecord getMatchIdList(String id) {
        ScanParams scanParams = new ScanParams().count(1000);
        ScanResult<String> scanResult = jedis.sscan(keyFinal, id, scanParams);
        return new MatchIdRecord(id, scanResult.getResult());
    }

    public void setNewMatchIdList() {

        if(jedis.exists(keyFinal))
            return;

        String[] newKeyList = getKeysByPattern("new_*");
        String[] oldKeyList = getKeysByPattern("old_*");

        if(newKeyList.length == 0)
            return;

        // 트랜잭션 시작
        Transaction transaction = jedis.multi();

        try {
            // 새로운 키들의 합집합을 구함
            transaction.sunionstore(keyFinal, newKeyList);

            for(String key : oldKeyList)
                transaction.sdiffStore(keyFinal, keyFinal, key);

            transaction.exec();
        } catch (Exception e) {
            // 오류 발생 시 트랜잭션 취소
            transaction.discard();
            log.failLog("Redis 트랜잭션 실패 " + e.getMessage());
        } finally {
            transaction.close();
        }
    }

    public void close() {
        jedis.del(keyFinal);
        jedis.close();
    }

}
