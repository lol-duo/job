package job.user.puuid;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    public static Connection connection = null;

    Log log = new Log();

    public boolean connect() {
        // 데이터베이스 연결 정보
        String jdbcUrl = "";
        String username = "";
        String password = "";

        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<String> getSummonerIds(String summonerId){
        try {
            String sql = "SELECT summoner_id FROM user_info WHERE puuid IS NULL AND summoner_id > ? ORDER BY summoner_id LIMIT 1000";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, summonerId);

            ResultSet resultSet = preparedStatement.executeQuery();

            List<String> summonerIds = new ArrayList<>();
            while (resultSet.next()) {
                String now = resultSet.getString("summoner_id");
                if(now.equals("null"))
                    break;

                summonerIds.add(now);
            }

            resultSet.close();
            preparedStatement.close();

            return summonerIds;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("getSummonerIds error");
            return null;
        }
    }

    public long upsertPuuidBySummonerIds(SummonerRecord summonerInfo, String summonerId){
        long startTime = System.currentTimeMillis();
        try {
            String sql = "UPDATE user_info SET puuid = ? WHERE summoner_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, summonerInfo.puuid());
            preparedStatement.setString(2, summonerId);

            int insertSuccess = preparedStatement.executeUpdate();

            preparedStatement.close();

            if(insertSuccess >= 1 || insertSuccess == Statement.SUCCESS_NO_INFO){
                return (System.currentTimeMillis() - startTime);
            } else {
                log.failLog("insert fail summonerId: " + summonerId + " puuid: " + summonerInfo.puuid() + " error code: " + insertSuccess);
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.failLog("upsertPuuidBySummonerIds error");
            return -1;
        }
    }
}
