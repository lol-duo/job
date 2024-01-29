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
            String sb = "INSERT IGNORE INTO combi (champion_id, lane, main_perk) VALUES (?, ?, ?)" +
                    ", (?, ?, ?)".repeat(Math.max(0, combiInfos.size() - 1));

            PreparedStatement preparedStatement = connection.prepareStatement(sb);
            int i = 1;
            for (Combi combi : combiInfos) {
                preparedStatement.setInt(i++, combi.championId());
                preparedStatement.setString(i++, combi.lane());
                preparedStatement.setInt(i++, combi.main_perk());
            }
            System.out.println(preparedStatement.toString());
            preparedStatement.executeBatch();


            // combi pk 설정
            StringBuilder queryBuilder = new StringBuilder();

            // 각 Combi 객체에 대한 쿼리를 생성합니다.
            for (i = 0; i < combiInfos.size(); i++) {
                Combi combi = combiInfos.get(i);
                if (i > 0) {
                    queryBuilder.append(" UNION ALL ");
                }
                queryBuilder.append("SELECT combi_id, champion_id, lane, main_perk FROM combi WHERE champion_id = ")
                        .append(combi.championId())
                        .append(" AND lane = '")
                        .append(combi.lane())
                        .append("' AND main_perk = ")
                        .append(combi.main_perk());
            }

            // 완성된 쿼리를 실행합니다.
            preparedStatement = connection.prepareStatement(queryBuilder.toString());
            ResultSet resultSet = preparedStatement.executeQuery();

            // 결과를 처리합니다.
            while (resultSet.next()) {
                int combiId = resultSet.getInt("combi_id");
                int championId = resultSet.getInt("champion_id");
                String lane = resultSet.getString("lane");
                int mainPerk = resultSet.getInt("main_perk");

                for (Combi combi : combiInfos) {
                    if (combi.championId() == championId && combi.lane().equals(lane) && combi.main_perk() == mainPerk) {
                        combi.setCombiId(combiId);
                        break;
                    }
                }
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
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("INSERT INTO match_solo_info (combi_id, win, lose, win_rate, game_version) VALUES ");

            int count = 0;
            for (Combi combi : combiList) {
                if (count > 0) {
                    sqlBuilder.append(",");
                }
                sqlBuilder.append("(")
                        .append(combi.combiId()).append(", ")
                        .append(combi.win() ? 1 : 0).append(", ")
                        .append(combi.win() ? 0 : 1).append(", ")
                        .append(combi.win() ? 10000 : 0).append(", '")
                        .append(combi.gameVersion()).append("')");
                count++;
            }

            sqlBuilder.append(" ON DUPLICATE KEY UPDATE win = win + VALUES(win), lose = lose + VALUES(lose), win_rate = win / (win + lose) * 10000");

            PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString());
            preparedStatement.executeUpdate();

            log.dbLog(System.currentTimeMillis() - start);
        } catch (SQLException e) {
            log.failLog("DB insertSoloInfo error" + e.getMessage());
        }
    }

    public void insertDuoInfo(List<MatchDuoInfoRecord> duoInfoRecords){
        try {
            long start = System.currentTimeMillis();
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("INSERT INTO match_duo_info (combi_id_1, combi_id_2, win, lose, win_rate, game_version) VALUES ");

            int count = 0;
            for (MatchDuoInfoRecord duoInfoRecord : duoInfoRecords) {
                if (count > 0) {
                    sqlBuilder.append(",");
                }
                sqlBuilder.append("(")
                        .append(duoInfoRecord.combiId1()).append(", ")
                        .append(duoInfoRecord.combiId2()).append(", ")
                        .append(duoInfoRecord.win() ? 1 : 0).append(", ")
                        .append(duoInfoRecord.win() ? 0 : 1).append(", ")
                        .append(duoInfoRecord.win() ? 10000 : 0).append(", '")
                        .append(duoInfoRecord.gameVersion()).append("')");
                count++;
            }

            sqlBuilder.append(" ON DUPLICATE KEY UPDATE win = win + VALUES(win), lose = lose + VALUES(lose), win_rate = win / (win + lose) * 10000");

            PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString());
            preparedStatement.executeUpdate();

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
