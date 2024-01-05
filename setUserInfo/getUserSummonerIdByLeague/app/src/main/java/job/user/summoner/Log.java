package job.user.summoner;

public class Log {
    Slack slack = new Slack();

    public void slack(String message) {
        slack.send(message);
    }

    public void log(String message) {
        System.out.println(message);
    }

    public void successLog(String message) {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_GREEN = "\u001B[32m";

        System.out.println(ANSI_GREEN + message + ANSI_RESET);
    }

    public void failLog(String message) {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_RED = "\u001B[31m";

        System.out.println(ANSI_RED + message + ANSI_RESET);
        slack.send(message);
    }

    public void warningLog(String message) {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_YELLOW = "\u001B[33m";

        System.out.println(ANSI_YELLOW + message + ANSI_RESET);
        slack.send(message);
    }

    public void apiLog(long time, String uri) {
        if (time > 3000)
            failLog("HTTP 요청 성공 time: " + String.format("%7dms ", time) + " uri: " + uri);
        else if (time > 1500)
            warningLog("HTTP 요청 성공 time: " + String.format("%7dms ", time) + " uri: " + uri);
        else
            successLog("HTTP 요청 성공 time: " + String.format("%7dms ", time) + " uri: " + uri);
    }

    public void dbLog(long time) {
        if (time > 4000)
            failLog("DB 쿼리 성공 time: " + String.format("%7dms ", time));
        else if (time > 2500)
            warningLog("DB 쿼리 성공 time: " + String.format("%7dms ", time));
        else
            successLog("DB 쿼리 성공 time: " + String.format("%7dms ", time));
    }
}
