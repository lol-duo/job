package job.match.id;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

public class Redis {

    JedisPool pool = new JedisPool("localhost", 6379);
    Jedis jedis;
    Log log = new Log();

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
            List<String> exists = jedis.mget(matchIds);

            int THREE_DAYS = 60 * 60 * 24 * 3;

            for (int i = 0; i < matchIds.length; i++) {
                if (exists.get(i) == null) {
                    jedis.setex(matchIds[i], THREE_DAYS, "0");
                }
            }

            log.redisLog(System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.failLog("Redis 삽입 실패 " + e.getMessage());
        }
    }

    public void close() {
        jedis.close();
    }

}
