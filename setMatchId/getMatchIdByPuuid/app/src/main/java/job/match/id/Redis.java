package job.match.id;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

public class Redis {

    JedisPool pool = new JedisPool("localhost", 6379);
    Jedis jedis;
    Log log = new Log();

    String key = "new_" + System.currentTimeMillis();

    public boolean connect() {
        try {
            jedis = pool.getResource();
            return true;
        } catch (Exception e) {
            log.failLog("Redis 연결 실패 " + e.getMessage());
            return false;
        }
    }

    public void set(String[] matchIds) {
        try {
            long start = System.currentTimeMillis();
            jedis.sadd(key, matchIds);
            log.redisLog(System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.failLog("Redis 삽입 실패 " + e.getMessage());
        }
    }

    public void close() {
        String cursor = ScanParams.SCAN_POINTER_START;
        String matchPattern = "new_*";
        ScanParams scanParams = new ScanParams().match(matchPattern);

        do {
            ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
            cursor = scanResult.getCursor();

            scanResult.getResult().forEach(key -> jedis.expire(key, 60 * 60 * 24 * 3));
        } while (!cursor.equals(ScanParams.SCAN_POINTER_START));

        jedis.close();
    }

}
