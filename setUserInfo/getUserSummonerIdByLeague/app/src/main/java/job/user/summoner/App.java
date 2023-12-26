package job.user.summoner;

import java.util.ArrayList;
import java.util.List;

public class App {

    Api api = new Api();
    Database database = new Database();

    void logTotal(String league, int[] successCount) {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_RED = "\u001B[31m";
        String ANSI_GREEN = "\u001B[32m";
        String ANSI_YELLOW = "\u001B[33m";

        String success = ANSI_GREEN + "success: " + successCount[0] + ANSI_RESET;

        // 전체가 success 면 ANSI_GREEN
        String total = ANSI_GREEN;

        // failCount 가 0이면 ANSI_GREEN, 아니면 ANSI_RED
        String fail;
        if(successCount[1] > 0) {
            fail = ANSI_RED + "fail: " + successCount[1] + ANSI_RESET;
            total = ANSI_RED;
        }
        else
            fail = ANSI_GREEN + "fail: " + successCount[1] + ANSI_RESET;

        // dbTime 이 success 개수 보다 크면 ANSI_RED, success 개수의 절반보다 크면 ANSI_YELLOW, 아니면 ANSI_GREEN
        String dbTime;
        if(successCount[2] > successCount[0]) {
            dbTime = ANSI_RED + " time: " + successCount[2] + "ms" + ANSI_RESET;
            total = ANSI_RED;
        }
        else if(successCount[2] > successCount[0] / 2) {
            dbTime = ANSI_YELLOW + " time: " + successCount[2] + "ms" + ANSI_RESET;
            total = ANSI_YELLOW;
        }
        else
            dbTime = ANSI_GREEN + " time: " + successCount[2] + "ms" + ANSI_RESET;

        // 최종 출력
        System.out.printf(total + "%-15s insert %-15s " + ANSI_RESET, league, success);
        System.out.printf("%-15s", fail);
        System.out.printf("%-15s%n", dbTime);
    }

    boolean setChallenger() {
        LeagueRecord leagueRecord = api.get("http://localhost:8080/league/challenger", LeagueRecord.class);
        if(leagueRecord == null){
            System.out.println("leagueRecord is null");
            return false;
        }

        int[] successCount = database.bulkUpsertBySummonerIds(leagueRecord.entries(), "challenger");

        logTotal("challenger", successCount);

        return true;
    }

    boolean setGrandmaster() {
        LeagueRecord leagueRecord = api.get("http://localhost:8080/league/grandmaster", LeagueRecord.class);
        if(leagueRecord == null){
            System.out.println("leagueRecord is null");
            return false;
        }

        int[] successCount = database.bulkUpsertBySummonerIds(leagueRecord.entries(), "grandmaster");

        logTotal("grandmaster", successCount);

        return true;
    }

    boolean setMaster() {
        LeagueRecord leagueRecord = api.get("http://localhost:8080/league/master", LeagueRecord.class);
        if(leagueRecord == null){
            System.out.println("leagueRecord is null");
            return false;
        }

        int[] successCount = database.bulkUpsertBySummonerIds(leagueRecord.entries(), "master");

        logTotal("master", successCount);

        return true;
    }

    boolean setTierDivisionPage(String tier, String division, int page) {
        UserEntryRecord[] detailLeagueRecords = api.get("http://localhost:8080/league/" + tier + "/" + division + "/" + page, UserEntryRecord[].class);
        if(detailLeagueRecords == null){
            System.out.println("detailLeagueRecords is null");
            String ANSI_RESET = "\u001B[0m";
            String ANSI_RED = "\u001B[31m";
            System.out.printf(ANSI_RED + "%-10s %4s %5d page update fail%n" + ANSI_RESET, tier, division, page);
            return false;
        }

        if(detailLeagueRecords.length == 0){
            System.out.println("detailLeagueRecords is empty");
            return false;
        }

        int[] successCount = database.bulkUpsertBySummonerIds(detailLeagueRecords, tier + "_" + division);

        logTotal(tier + "_" + division + "_" + page, successCount);

        return true;
    }

    public static void main(String[] args) {
        App app = new App();
        Log log = new Log();

        String[] tiers = {"DIAMOND"};
        String[] divisions = {"I", "II", "III", "IV"};

        if(app.database.connect()){

            if(app.setChallenger())
                log.successLog("challenger update success");
            else {
                log.failLog("challenger update fail");
                return;
            }

            if(app.setGrandmaster()){
                log.successLog("grandmaster update success");
            } else {
                log.failLog("grandmaster update fail");
                return;
            }

            if(app.setMaster()){
                log.successLog("master update success");
            } else {
                log.failLog("master update fail");
                return;
            }

            List<Thread> threadList = new ArrayList<>();
            for (String tier : tiers) {
                for (String division : divisions) {
                    Thread thread = new Thread(() -> {
                        int page = 1;
                        while(app.setTierDivisionPage(tier, division, page)) {
                            page++;
                        }
                        log.successLog(tier + " " + division + " update success");
                    });
                    thread.start();
                    threadList.add(thread);
                }
                for (Thread thread : threadList) {
                    try {
                        thread.join();
                    } catch (InterruptedException ignored) {
                    }
                }
                threadList.clear();
            }

        } else {
            log.failLog("database connect fail");
        }

    }
}
