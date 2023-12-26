package job.user.summoner;

import java.sql.*;

public class Database {

    public static Connection connection = null;
    public boolean connect() {
        // 데이터베이스 연결 정보
        String jdbcUrl = "";
        String username = "";
        String password = "";

        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            return false;
        }
        return true;
    }

    public int[] bulkUpsertBySummonerIds(UserEntryRecord[] summonerIds, String league){
        long startTime = System.currentTimeMillis();
        try {
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
            for(int i = 0; i < insertSuccess.length; i++){
                if(insertSuccess[i] >= 1 || insertSuccess[i] == Statement.SUCCESS_NO_INFO){
                    successCount++;
                } else {
                    failCount++;
                    System.out.println("insert fail summonerId: " + summonerIds[i].summonerId() + " league: " + league + " error code: " + insertSuccess[i]);
                }
            }

            return new int[]{successCount, failCount, (int)(System.currentTimeMillis() - startTime)};
        } catch (SQLException e) {
            e.printStackTrace(System.out);
            System.out.println("bulkUpsertBySummonerIds error");
            return new int[]{0, summonerIds.length};
        }
    }
}
