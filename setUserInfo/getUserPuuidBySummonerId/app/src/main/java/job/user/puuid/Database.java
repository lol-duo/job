package job.user.puuid;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    public static Connection connection = null;

    AppConfig appConfig = AppConfig.getInstance();
    Log log = new Log();

    public boolean connect() {
        // 데이터베이스 연결 정보
        String jdbcUrl = appConfig.getProperty("jdbcUrl");
        String username = appConfig.getProperty("username");
        String password = appConfig.getProperty("password");

        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
        } catch (Exception e) {
            log.failLog("DB connect error" + e.getMessage());
            return false;
        }
        return true;
    }

    public List<String> getSummonerIds(String summonerId) {
        try {
            String sql = "SELECT summoner_id FROM user_info WHERE puuid IS NULL AND summoner_id > ? ORDER BY summoner_id LIMIT 1000";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, summonerId);

            ResultSet resultSet = preparedStatement.executeQuery();

            List<String> summonerIds = new ArrayList<>();
            while (resultSet.next()) {
                String now = resultSet.getString("summoner_id");
                if (now.equals("null"))
                    break;

                summonerIds.add(now);
            }

            resultSet.close();
            preparedStatement.close();

            return summonerIds;
        } catch (SQLException e) {
            log.failLog("getSummonerIds error" + e.getMessage());
            return null;
        }
    }

    public void upsertPuuidBySummonerIds(SummonerRecord summonerInfo, String summonerId) {
        try {
            long startTime = System.currentTimeMillis();
            String sql = "UPDATE user_info SET puuid = ? WHERE summoner_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, summonerInfo.puuid());
            preparedStatement.setString(2, summonerId);

            int insertSuccess = preparedStatement.executeUpdate();

            preparedStatement.close();

            if (insertSuccess >= 1 || insertSuccess == Statement.SUCCESS_NO_INFO) {
                log.dbLog(System.currentTimeMillis() - startTime);
            } else {
                log.failLog("insert fail summonerId: " + summonerId + " puuid: " + summonerInfo.puuid() + " error code: " + insertSuccess);
            }
        } catch (SQLException e) {
            log.failLog("upsertPuuidBySummonerIds error : " + e.getMessage());
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            log.failLog("DB disconnect error" + e.getMessage());
        }
    }
}
