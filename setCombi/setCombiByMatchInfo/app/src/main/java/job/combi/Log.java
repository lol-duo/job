package job.combi;

public class Log {

    Slack slack = new Slack();

    public void slack(String message) {
        slack.send(message);
    }

    public void successLog(String message) {
        if(AppConfig.activeProfile.equals("prod"))
            return;
        String ANSI_RESET = "\u001B[0m";
        String ANSI_GREEN = "\u001B[32m";

        System.out.println(ANSI_GREEN + message + ANSI_RESET);
    }

    public void failLog(String message) {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_RED = "\u001B[31m";

        System.out.println(ANSI_RED + message + ANSI_RESET);
        slack(message);
    }

    public void warningLog(String message) {
        if(AppConfig.activeProfile.equals("prod"))
            return;
        String ANSI_RESET = "\u001B[0m";
        String ANSI_YELLOW = "\u001B[33m";

        System.out.println(ANSI_YELLOW + message + ANSI_RESET);
    }

    public void dbLog(long time) {
        if (time > 3000)
            failLog("DB 쿼리 성공 time: " + String.format("%7dms ", time));
        else if (time > 500)
            warningLog("DB 쿼리 성공 time: " + String.format("%7dms ", time));
        else
            successLog("DB 쿼리 성공 time: " + String.format("%7dms ", time));
    }
    public void redisLog(long time) {
        if (time > 100)
            failLog("Redis 쿼리 성공 time: " + String.format("%7dms ", time));
        else if (time > 50)
            warningLog("Redis 쿼리 성공 time: " + String.format("%7dms ", time));
        else
            successLog("Redis 쿼리 성공 time: " + String.format("%7dms ", time));
    }
}
