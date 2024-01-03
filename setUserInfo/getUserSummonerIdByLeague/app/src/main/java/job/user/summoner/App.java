package job.user.summoner;

import java.util.ArrayList;
import java.util.List;

public class App {

    Api api = new Api();
    Database database = new Database();
    Log log = new Log();
    AppConfig appConfig = AppConfig.getInstance();
    
    void logTotal(String league, int[] successCount) {
        String Success = String.format("%-15s", "success: " + successCount[0]);
        String Fail = String.format("%-15s", "fail: " + successCount[1]);

        if (successCount[1] > 0) {
            log.failLog(Success + Fail + "league: " + league);
        } else {
            log.successLog(Success + Fail + "league: " + league);
        }
    }

    boolean setChallenger() {
        LeagueRecord leagueRecord = api.get(appConfig.getProperty("riot.api.server")+"/league/challenger", LeagueRecord.class);
        if (leagueRecord == null) {
            log.failLog("leagueRecord is null");
            return false;
        }

        int[] successCount = database.bulkUpsertBySummonerIds(leagueRecord.entries(), "challenger");

        logTotal("challenger", successCount);

        return true;
    }

    boolean setGrandmaster() {
        LeagueRecord leagueRecord = api.get(appConfig.getProperty("riot.api.server")+"/league/grandmaster", LeagueRecord.class);
        if (leagueRecord == null) {
            log.failLog("leagueRecord is null");
            return false;
        }

        int[] successCount = database.bulkUpsertBySummonerIds(leagueRecord.entries(), "grandmaster");

        logTotal("grandmaster", successCount);

        return true;
    }

    boolean setMaster() {
        LeagueRecord leagueRecord = api.get(appConfig.getProperty("riot.api.server")+"/league/master", LeagueRecord.class);
        if (leagueRecord == null) {
            log.failLog("leagueRecord is null");
            return false;
        }

        int[] successCount = database.bulkUpsertBySummonerIds(leagueRecord.entries(), "master");

        logTotal("master", successCount);

        return true;
    }

    boolean setTierDivisionPage(String tier, String division, int page) {
        UserEntryRecord[] detailLeagueRecords = api.get(appConfig.getProperty("riot.api.server")+"/league/" + tier + "/" + division + "/" + page, UserEntryRecord[].class);
        if (detailLeagueRecords == null) {
            log.failLog("detailLeagueRecords is null" + String.format("%-10s %4s %5d page update fail%n", tier, division, page));
            return false;
        }

        if (detailLeagueRecords.length == 0) {
            log.log("detailLeagueRecords is empty");
            return false;
        }

        int[] successCount = database.bulkUpsertBySummonerIds(detailLeagueRecords, tier + "_" + division);

        logTotal(tier + "_" + division + "_" + page, successCount);

        return true;
    }

    public static void main(String[] args) {
        App app = new App();
        Log log = new Log();
        log.slack("Job start");

        String[] tiers = {"DIAMOND"};
        String[] divisions = {"I", "II", "III", "IV"};

        if (app.database.connect()) {

            if (app.setChallenger())
                log.successLog("challenger update success");
            else {
                log.failLog("challenger update fail");
                return;
            }
            
            if (app.setGrandmaster()) {
                log.successLog("grandmaster update success");
            } else {
                log.failLog("grandmaster update fail");
                return;
            }

            if (app.setMaster()) {
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
                        while (app.setTierDivisionPage(tier, division, page)) {
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

        app.database.close();

        log.slack("Job end");
    }
}
