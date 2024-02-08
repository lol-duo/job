package job.user.summoner;

import java.sql.*;

public class Database {
    AppConfig appConfig = AppConfig.getInstance();
    Log log = new Log();
    public static Connection connection = null;
    private PreparedStatement preparedStatementSummonerId;

    public boolean connect() {
        // 데이터베이스 연결 정보
        String jdbcUrl = appConfig.getProperty("jdbcUrl");
        String username = appConfig.getProperty("username");
        String password = appConfig.getProperty("password");

        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            preparedStatementSummonerId = connection.prepareStatement("INSERT INTO user_info (summoner_id, league) VALUES (?, ?) ON DUPLICATE KEY UPDATE league = ?");
        } catch (Exception e) {
            log.failLog("DB connect error : " + e.getMessage());
            return false;
        }
        return true;
    }

    public int[] bulkUpsertBySummonerIds(UserEntryRecord[] summonerIds, String league) {

        try {
            long startTime = System.currentTimeMillis();

            for (UserEntryRecord summonerId : summonerIds) {
                preparedStatementSummonerId.setString(1, summonerId.summonerId());
                preparedStatementSummonerId.setString(2, league);
                preparedStatementSummonerId.setString(3, league);
                preparedStatementSummonerId.addBatch();
            }

            int[] insertSuccess = preparedStatementSummonerId.executeBatch();

            int successCount = 0, failCount = 0;
            for (int i = 0; i < insertSuccess.length; i++) {
                if (insertSuccess[i] >= 1 || insertSuccess[i] == Statement.SUCCESS_NO_INFO) {
                    successCount++;
                } else {
                    failCount++;
                    log.failLog("insert fail summonerId: " + summonerIds[i].summonerId() + " league: " + league + " error code: " + insertSuccess[i]);
                }
            }

            log.dbLog(System.currentTimeMillis() - startTime);

            return new int[]{successCount, failCount};
        } catch (SQLException e) {
            log.failLog("bulkUpsertBySummonerIds error : " + e.getMessage());
            return new int[]{0, summonerIds.length};
        }
    }

    public void close() {
        try {
            preparedStatementSummonerId.close();
            connection.close();
        } catch (SQLException e) {
            log.failLog("DB disconnect error" + e.getMessage());
        }
    }
}
