package job.match.info;

import java.util.ArrayList;
import java.util.List;

public class App {
    Log log = new Log();
    Redis redis = new Redis();
    Api api = new Api();
    Aws aws = new Aws();
    AppConfig appConfig = AppConfig.getInstance();
    void insertSQS(){

        log.log("matchIdList 생성 시작");

        // 진행할 게임 matchId 설정.
        redis.setNewMatchIdList();

        log.log("matchIdList 생성 완료 및 SQS 삽입 시작");

        String id = "0";
        do {
            MatchIdRecord matchIdRecord = redis.getMatchIdList(id);
            id = matchIdRecord.id();
            aws.sendMessage(matchIdRecord.matchIdList());
        } while (!id.equals("0"));
    }

    boolean runTask(){
        MatchIdMessageRecord message = aws.receiveMessage();
        if(message == null)
            return false;

        String[] matchIds = message.matchIds();
        List<MatchRecord> matchRecords = new ArrayList<>();
        for(String matchId : matchIds){
            MatchRecord matchInfo = api.get(appConfig.getProperty("riot.api.server")+"/matches/" + matchId, MatchRecord.class);
            if(matchInfo == null)
                continue;
            matchRecords.add(matchInfo);
            if(matchRecords.size() == 20) {
                aws.sendMessage(matchRecords);
                matchRecords.clear();
            }
        }
        if(!matchRecords.isEmpty())
            aws.sendMessage(matchRecords);

        aws.deleteMessage(message.receiptHandle());
        return true;
    }

    public static void main(String[] args) {
        App app = new App();

        app.log.slack("Job start");

        if (!app.redis.connect()) {
            app.log.failLog("redis connect fail");
            return;
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
                app.log.failLog("thread join fail" + e.getMessage());
            }
        }

        app.redis.close();

        app.api.startNextJob();
        app.log.slack("Job end");
    }
}
