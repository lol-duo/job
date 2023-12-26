package job.user.puuid;

public class Log {
    public void successLog(String message) {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_GREEN = "\u001B[32m";

        System.out.println(ANSI_GREEN + message + ANSI_RESET);
    }

    public void failLog(String message) {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_RED = "\u001B[31m";

        System.out.println(ANSI_RED + message + ANSI_RESET);
    }

    public void warningLog(String message) {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_YELLOW = "\u001B[33m";

        System.out.println(ANSI_YELLOW + message + ANSI_RESET);
    }

    public void apiLog(long time) {
        if(time > 3000)
            failLog("HTTP 요청 성공 time: " + String.format("%7dms ", time));
        else if(time > 1500)
            warningLog("HTTP 요청 성공 time: " + String.format("%7dms ", time));
        else
            successLog("HTTP 요청 성공 time: " + String.format("%7dms ", time));
    }

    public void dbLog(long time) {
        if(time > 100)
            failLog("DB 삽입 성공 time: " + String.format("%7dms ", time));
        else if(time > 50)
            warningLog("DB 삽입 성공 time: " + String.format("%7dms ", time));
        else
            successLog("DB 삽입 성공 time: " + String.format("%7dms ", time));
    }
}
