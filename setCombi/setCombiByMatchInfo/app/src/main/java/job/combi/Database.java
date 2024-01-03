package job.combi;

import java.sql.*;
import java.util.List;

public class Database {

    public static Connection connection = null;
    Log log = new Log();
    AppConfig appConfig = AppConfig.getInstance();
    
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

    public List<Combi> upsertCombi(List<Combi> combiInfos) {
        try {
            // combi 설정
            String sql = "INSERT IGNORE INTO combi (champion_id, lane, main_perk) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (Combi combi : combiInfos) {
                preparedStatement.setInt(1, combi.championId());
                preparedStatement.setString(2, combi.lane());
                preparedStatement.setInt(3, combi.main_perk());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();

            // combi pk 설정
            sql = "SELECT combi_id FROM combi WHERE champion_id = ? AND lane = ? AND main_perk = ?";
            preparedStatement = connection.prepareStatement(sql);
            for (Combi combi : combiInfos) {
                preparedStatement.setInt(1, combi.championId());
                preparedStatement.setString(2, combi.lane());
                preparedStatement.setInt(3, combi.main_perk());

                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                combi.setCombiId(resultSet.getInt("combi_id"));
            }

            return combiInfos;
        } catch (SQLException e) {
            log.failLog("DB upsertCombi error" + e.getMessage());
        }
        return null;
    }

    public void insertSoloInfo(List<Combi> combiList) {
        try {
            long start = System.currentTimeMillis();
            String sql = "INSERT INTO match_solo_info (combi_id, win, lose, win_rate, game_version) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE win = win + ?, lose = lose + ?, win_rate = win / (win + lose) * 10000";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (Combi combi : combiList) {
                preparedStatement.setInt(1, combi.combiId());
                preparedStatement.setInt(2, combi.win() ? 1 : 0);
                preparedStatement.setInt(3, combi.win() ? 0 : 1);
                preparedStatement.setInt(4, combi.win() ? 10000 : 0);
                preparedStatement.setString(5, combi.gameVersion());
                preparedStatement.setInt(6, combi.win() ? 1 : 0);
                preparedStatement.setInt(7, combi.win() ? 0 : 1);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            log.dbLog(System.currentTimeMillis() - start);
        } catch (SQLException e) {
            log.failLog("DB insertSoloInfo error" + e.getMessage());
        }
    }

    public void insertDuoInfo(List<MatchDuoInfoRecord> duoInfoRecords){
        try {
            long start = System.currentTimeMillis();
            String sql = "INSERT INTO match_duo_info (combi_id_1, combi_id_2, win, lose, win_rate, game_version) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE win = win + ?, lose = lose + ?, win_rate = win / (win + lose) * 10000";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            for (MatchDuoInfoRecord duoInfoRecord : duoInfoRecords) {
                preparedStatement.setInt(1, duoInfoRecord.combiId1());
                preparedStatement.setInt(2, duoInfoRecord.combiId2());
                preparedStatement.setInt(3, duoInfoRecord.win() ? 1 : 0);
                preparedStatement.setInt(4, duoInfoRecord.win() ? 0 : 1);
                preparedStatement.setInt(5, duoInfoRecord.win() ? 10000 : 0);
                preparedStatement.setString(6, duoInfoRecord.gameVersion());
                preparedStatement.setInt(7, duoInfoRecord.win() ? 1 : 0);
                preparedStatement.setInt(8, duoInfoRecord.win() ? 0 : 1);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            log.dbLog(System.currentTimeMillis() - start);
        } catch (SQLException e) {
            log.failLog("DB insertDuoInfo error" + e.getMessage());
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
