package job.match.id;

import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class Database {

    public static Connection connection = null;
    String leagueList;
    AppConfig appConfig = AppConfig.getInstance();
    Log log = new Log();

    public Database() {
        String[] topLeagues = {"\"CHALLENGER\"", "\"GRANDMASTER\"", "\"MASTER\""};
        String[] leagues = {"\"DIAMOND"};
        String[] divisions = {"I\"", "II\"", "III\"", "IV\""};
        List<String> leagueList = new ArrayList<>(List.of(topLeagues));
        for (String league : leagues) {
            for (String division : divisions) {
                leagueList.add(league + "_" + division);
            }
        }

        this.leagueList = String.join(", ", leagueList);
    }

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

    public List<String> getPuuids(String puuid) {
        try {
            long startTime = System.currentTimeMillis();
            String sql = MessageFormat.format("SELECT puuid FROM user_info WHERE puuid IS NOT NULL AND puuid > ? AND league IN ({0}) ORDER BY puuid LIMIT 1000", leagueList);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, puuid);

            ResultSet resultSet = preparedStatement.executeQuery();

            List<String> puuids = new ArrayList<>();
            while (resultSet.next()) {
                String now = resultSet.getString("puuid");
                if (now.equals("null"))
                    break;
                puuids.add(now);
            }

            resultSet.close();
            preparedStatement.close();

            log.dbLog(System.currentTimeMillis() - startTime);

            return puuids;
        } catch (SQLException e) {
            log.failLog("getPuuids error" + e);
            return null;
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
