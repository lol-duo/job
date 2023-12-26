
package job.user.puuid;

import java.util.ArrayList;
import java.util.List;

public class App {

    Aws aws = new Aws();
    Database database = new Database();
    Api api = new Api();
    Log log = new Log();

    void insertSQS(){
        System.out.println("SQS 삽입 시작");
        String id = "";
        int count = 0;
        while(true){
            List<String> summonerIds = database.getSummonerIds(id);
            if(summonerIds.isEmpty()){
                break;
            }
            id = summonerIds.get(summonerIds.size() - 1);
            count += summonerIds.size();
            aws.sendMessage(summonerIds);
        }
        System.out.println(count + "개의 데이터 삽입 완료");
    }

    long[] setPuuidBySummonerId(String summonerId){
        long startTime = System.currentTimeMillis();
        SummonerRecord summonerInfo = api.get("http://localhost:8080/summoner/" + summonerId, SummonerRecord.class);
        if(summonerInfo == null){
            System.out.println("summonerInfo is null");
            return null;
        }
        long apiTime = System.currentTimeMillis() - startTime;

        long dbTime = database.upsertPuuidBySummonerIds(summonerInfo, summonerId);
        if(dbTime != -1){
            return new long[]{apiTime, dbTime};
        }
        return null;
    }

    boolean runTask(){
        MessageRecord message = aws.receiveMessage();

        if(message == null)
            return false;

        String[] summonerIds = message.summonerIds();
        for(String summonerId : summonerIds){
            long[] timeInfo = setPuuidBySummonerId(summonerId);
            if(timeInfo != null){
                log.apiLog(timeInfo[0]);
                log.dbLog(timeInfo[1]);
            }
        }
        aws.deleteMessage(message.receiptHandle());
        return true;
    }



    public static void main(String[] args) {
        App app = new App();
        if(!app.database.connect()){
            System.out.println("database connect fail");
            return;
        }

        app.insertSQS();
        List<Thread> threadList = new ArrayList<>();

        for(int i = 0; i < 5; i++){
            Thread thread = new Thread(() -> {
                while(true){
                    if(!app.runTask())
                        break;
                }
            });
            thread.start();
            threadList.add(thread);
        }

        for(Thread thread : threadList){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
            }
        }
    }
}
