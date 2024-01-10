package job.combi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class App {

    Aws aws = new Aws();
    Database database = new Database();
    Log log = new Log();

    List<String> laneList = List.of("TOP", "JUNGLE", "MIDDLE", "BOTTOM", "UTILITY");

    List<Combi> createSoloInfo(MatchRecord matchRecord) {
        List<Combi> combiInfos = new ArrayList<>();

        String[] gameVersionList = matchRecord.info().gameVersion().split("\\.");
        String gameVersion = gameVersionList[0] + "." + gameVersionList[1];

        for(int i = 0; i < matchRecord.info().participants().length; i++) {
            Combi combi = getMatchSoloInfoRecord(matchRecord.info().participants()[i], i, gameVersion, matchRecord.metadata().matchId());
            combiInfos.add(combi);
        }

        return combiInfos;
    }

    private Combi getMatchSoloInfoRecord(ParticipantRecord participantRecord, int i, String gameVersion, String matchId) {
        int championId = participantRecord.championId();
        String lane;
        if(laneList.contains(participantRecord.teamPosition())) {
            lane = participantRecord.teamPosition();
        } else {
            log.slack("lane error: " + participantRecord.teamPosition() + "\nmatchId : " + matchId + "\nparticipantId : " + i);
            lane = laneList.get(i % 5);
        }

        int main_perk = -1;
        for(int j = 0; j < participantRecord.perks().styles().length; j++) {
            if(Objects.equals(participantRecord.perks().styles()[j].description(), "primaryStyle"))
                main_perk = participantRecord.perks().styles()[j].selections()[0].perk();
        }
        
        boolean win = participantRecord.win();

        return new Combi(championId, gameVersion, lane, main_perk, win);
    }

    List<MatchDuoInfoRecord> createDuoInfo(List<Combi> combiInfos) {
        List<MatchDuoInfoRecord> matchDuoInfoRecords = new ArrayList<>();

        for (int i = 0; i < combiInfos.size(); i++) {
            for (int j = i + 1; j < combiInfos.size(); j++) {
                Combi combi1 = combiInfos.get(i);
                Combi combi2 = combiInfos.get(j);

                if (combi1.win() != combi2.win())
                    continue;

                // 첫 번째 챔피언이 두 번째 챔피언보다 항상 작은 값이 되도록 정렬
                if (combi1.combiId > combi2.combiId){
                    combi1 = combiInfos.get(j);
                    combi2 = combiInfos.get(i);
                }

                MatchDuoInfoRecord matchDuoInfoRecord = new MatchDuoInfoRecord(
                        combi1.gameVersion(),
                        combi1.combiId(),
                        combi2.combiId(),
                        combi1.win()
                );

                matchDuoInfoRecords.add(matchDuoInfoRecord);
            }
        }

        return matchDuoInfoRecords;
    }

    boolean runTask() {
        MessageRecord message = aws.receiveMessage();
        if (message == null)
            return false;

        MatchRecord[] matchRecords = message.matchRecords();

        for(MatchRecord matchRecord : matchRecords) {
            // 솔랭만 추출
            if(matchRecord.info().queueId() != 420)
                continue;

            List<Combi> combiList = createSoloInfo(matchRecord);
            combiList = database.upsertCombi(combiList);

            if(combiList == null){
                log.failLog("combiList is null" + matchRecord.metadata().matchId());
                continue;
            }

            database.insertSoloInfo(combiList);

            List<MatchDuoInfoRecord> matchDuoInfoRecords = createDuoInfo(combiList);
            database.insertDuoInfo(matchDuoInfoRecords);
        }

        aws.deleteMessage(message.receiptHandle());
        return true;
    }

    public static void main(String[] args) {
        App app = new App();

        app.log.slack("Job start");

        if(!app.database.connect()){
            app.log.failLog("DB connect fail");
            return;
        }

        List<Thread> threads = new ArrayList<>();
        for(int i = 0; i < 1; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    if(!app.runTask())
                        break;
                }
            });
            threads.add(thread);
            thread.start();
        }

        for(Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                app.log.failLog("thread join fail" + e.getMessage());
            }
        }

        app.database.close();
        app.log.slack("Job end");

    }
}
