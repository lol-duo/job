package job.combi;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Redis {
    AppConfig appConfig = AppConfig.getInstance();
    JedisPool pool = new JedisPool(appConfig.getProperty("redisUrl"),Integer.valueOf(appConfig.getProperty("redisPort")));
    Jedis jedis;
    Log log = new Log();

    String solo_key = "job_solo" + System.currentTimeMillis();
    String duo_key = "job_duo" + System.currentTimeMillis();

    public boolean connect() {
        try {
            jedis = pool.getResource();

            if(jedis.exists(solo_key))
                jedis.del(solo_key);
            if(jedis.exists(duo_key))
                jedis.del(duo_key);
            return true;
        } catch (Exception e) {
            log.failLog("Redis 연결 실패 " + e.getMessage());
            return false;
        }
    }

    public void setSolo(List<Combi> combiList) {
        try {
            long start = System.currentTimeMillis();
            for(Combi combi : combiList) {
                if(combi.win())
                    jedis.hincrBy(solo_key, combi.combiId + "_" + combi.gameVersion + "_win", 1);
                else
                    jedis.hincrBy(solo_key, combi.combiId + "_" + combi.gameVersion + "_lose", 1);
            }
            log.redisLog(System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.failLog("Redis 삽입 실패 " + e.getMessage());
        }
    }

    public void setDuo(List<MatchDuoInfoRecord> matchDuoInfoRecords) {
        try {
            long start = System.currentTimeMillis();
            for (MatchDuoInfoRecord matchDuoInfoRecord : matchDuoInfoRecords) {
                if(matchDuoInfoRecord.win())
                    jedis.hincrBy(duo_key, matchDuoInfoRecord.combiId1() + "_" + matchDuoInfoRecord.combiId2() + "_" + matchDuoInfoRecord.gameVersion() + "_win", 1);
                else
                    jedis.hincrBy(duo_key, matchDuoInfoRecord.combiId1() + "_" + matchDuoInfoRecord.combiId2() + "_" + matchDuoInfoRecord.gameVersion() + "_lose", 1);
            }
            log.redisLog(System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.failLog("Redis 삽입 실패 " + e.getMessage());
        }
    }

    public SoloInfoRecordList getSoloInfoRecordList(String id) {
        ScanParams scanParams = new ScanParams().count(1000);
        ScanResult<Map.Entry<String, String>> scanResult = jedis.hscan(solo_key, id, scanParams);
        List<Map.Entry<String, String>> result = scanResult.getResult();
        List<SoloInfoRecord> soloInfoRecords = new ArrayList<>();
        for (Map.Entry<String, String> entry : result) {
            String[] keys = entry.getKey().split("_");
            if(keys.length != 3)
                continue;
            String combiId = keys[0];
            String gameVersion = keys[1];
            if(keys[2].equals("win"))
                soloInfoRecords.add(new SoloInfoRecord(gameVersion, Integer.parseInt(combiId), Integer.valueOf(entry.getValue()), 0));
            else
                soloInfoRecords.add(new SoloInfoRecord(gameVersion, Integer.parseInt(combiId), 0, Integer.valueOf(entry.getValue())));
        }
        return new SoloInfoRecordList(scanResult.getCursor(), soloInfoRecords);
    }

    public DuoInfoRecordList getDuoInfoRecordList(String id) {
        ScanParams scanParams = new ScanParams().count(1000);
        ScanResult<Map.Entry<String, String>> scanResult = jedis.hscan(duo_key, id, scanParams);
        List<Map.Entry<String, String>> result = scanResult.getResult();
        List<DuoInfoRecord> duoInfoRecords = new ArrayList<>();
        for (Map.Entry<String, String> entry : result) {
            String[] keys = entry.getKey().split("_");
            if(keys.length != 4)
                continue;
            String combiId1 = keys[0];
            String combiId2 = keys[1];
            String gameVersion = keys[2];
            if(keys[3].equals("win"))
                duoInfoRecords.add(new DuoInfoRecord(gameVersion, Integer.parseInt(combiId1), Integer.parseInt(combiId2), Integer.valueOf(entry.getValue()), 0));
            else
                duoInfoRecords.add(new DuoInfoRecord(gameVersion, Integer.parseInt(combiId1), Integer.parseInt(combiId2), 0, Integer.valueOf(entry.getValue())));
        }
        return new DuoInfoRecordList(scanResult.getCursor(), duoInfoRecords);
    }

    public void close() {
        jedis.del(solo_key);
        jedis.del(duo_key);
        jedis.close();
    }

}
