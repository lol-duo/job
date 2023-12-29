package job.match.id;

import java.util.ArrayList;
import java.util.List;

public class App {

    Aws aws = new Aws();
    Database database = new Database();
    Redis redis = new Redis();
    Api api = new Api();
    Log log = new Log();
    AppConfig appConfig = AppConfig.getInstance();
    void insertSQS() {
        log.log("SQS 삽입 시작");
        String id = "";
        int count = 0;
        while (true) {
            List<String> puuids = database.getPuuids(id);
            if (puuids.isEmpty()) {
                break;
            }
            id = puuids.get(puuids.size() - 1);
            count += puuids.size();
            aws.sendMessage(puuids);
        }
        log.log(count + "개의 데이터 삽입 완료");
    }

    void setMatchIdByPuuid(String puuid) {
        String[] matchIds = api.get(appConfig.getProperty("riot.api.server")+"/matches/by-puuid/" + puuid, String[].class);
        if (matchIds == null) {
            log.log("matchId is null");
            return;
        }
        if (matchIds.length == 0)
            return;

        redis.set(matchIds);
    }

    boolean runTask() {
        MessageRecord message = aws.receiveMessage();
        if (message == null)
            return false;

        String[] puuids = message.puuids();
        for (String puuid : puuids) {
            setMatchIdByPuuid(puuid);
        }
        aws.deleteMessage(message.receiptHandle());
        return true;
    }


    public static void main(String[] args) {
        App app = new App();
        Log log = new Log();

        //log.slack("Job start");

        if (!app.database.connect()) {
            log.failLog("database connect fail");
            return;
        }

        if (!app.redis.connect()) {
            log.failLog("redis connect fail");
            return;
        }
        else{
             log.successLog("redis connect sucess");
        }
         
        app.insertSQS();
        List<Thread> threadList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    if (!app.runTask())
                        break;
                }
            });
            thread.start();
            threadList.add(thread);
        }

        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                log.failLog("thread join fail" + e.getMessage());
            }
        }

        app.redis.close();
        app.database.close();

        //log.slack("Job end");
        
    }
}
