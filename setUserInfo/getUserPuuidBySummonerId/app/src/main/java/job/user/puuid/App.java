package job.user.puuid;

import java.util.ArrayList;
import java.util.List;

public class App {

    Aws aws = new Aws();
    Database database = new Database();
    Api api = new Api();
    Log log = new Log();

    void insertSQS() {
        log.log("SQS 삽입 시작");
        String id = "";
        int count = 0;
        while (true) {
            List<String> summonerIds = database.getSummonerIds(id);
            if (summonerIds.isEmpty()) {
                break;
            }
            id = summonerIds.get(summonerIds.size() - 1);
            count += summonerIds.size();
            aws.sendMessage(summonerIds);
        }
        log.log(count + "개의 데이터 삽입 완료");
    }

    void setPuuidBySummonerId(String summonerId) {
        SummonerRecord summonerInfo = api.get("http://localhost:8080/summoner/" + summonerId, SummonerRecord.class);
        if (summonerInfo == null) {
            log.log("summonerInfo is null");
            return;
        }
        database.upsertPuuidBySummonerIds(summonerInfo, summonerId);
    }

    boolean runTask() {
        MessageRecord message = aws.receiveMessage();

        if (message == null)
            return false;

        String[] summonerIds = message.summonerIds();
        for (String summonerId : summonerIds) {
            setPuuidBySummonerId(summonerId);
        }
        aws.deleteMessage(message.receiptHandle());
        return true;
    }


    public static void main(String[] args) {
        App app = new App();
        Log log = new Log();

        log.slack("Job start");

        if (!app.database.connect()) {
            log.failLog("database connect fail");
            return;
        }

        app.insertSQS();
        List<Thread> threadList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
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
                e.printStackTrace(System.out);
            }
        }

        log.slack("Job end");
    }
}
