package job.user.summoner;

import java.sql.*;

public class Database {

    Log log = new Log();
    public static Connection connection = null;
    AppConfig appConfig = AppConfig.getInstance();
    
    public boolean connect() {
        // 데이터베이스 연결 정보
        String jdbcUrl = appConfig.getProperty("jdbcUrl");
        String username = appConfig.getProperty("username");
        String password = appConfig.getProperty("password");

        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
        } catch (Exception e) {
            log.failLog("DB connect error : " + e.getMessage());
            e.printStackTrace(System.out);
            return false;
        }
        return true;
    }

    public int[] bulkUpsertBySummonerIds(UserEntryRecord[] summonerIds, String league) {

        try {
            long startTime = System.currentTimeMillis();
            String sql = "INSERT INTO user_info (summoner_id, league) VALUES (?, ?) ON DUPLICATE KEY UPDATE league = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (UserEntryRecord summonerId : summonerIds) {
                preparedStatement.setString(1, summonerId.summonerId());
                preparedStatement.setString(2, league);
                preparedStatement.setString(3, league);
                preparedStatement.addBatch();
            }

            int[] insertSuccess = preparedStatement.executeBatch();
            preparedStatement.close();

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
            e.printStackTrace(System.out);
            return new int[]{0, summonerIds.length};
        }
    }
}
