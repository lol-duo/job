package job.combi;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    public static Connection connection = null;
    Log log = new Log();
    AppConfig appConfig = AppConfig.getInstance();
    private final Map<Integer, PreparedStatement> combiInsertPrepareStatementMap = new HashMap<>();
    private final Map<Integer, PreparedStatement> combiPrepareStatementMap = new HashMap<>();
    private PreparedStatement soloPrepareStatement;
    private PreparedStatement duoPrepareStatement;
    
    public boolean connect() {
        // 데이터베이스 연결 정보
        String jdbcUrl = appConfig.getProperty("jdbcUrl");
        String username = appConfig.getProperty("username");
        String password = appConfig.getProperty("password");

        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            soloPrepareStatement = connection.prepareStatement("INSERT INTO match_solo_info (combi_id, win, lose, win_rate, game_version) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE win = win + VALUES(win), lose = lose + VALUES(lose), win_rate = win / (win + lose) * 10000");
            duoPrepareStatement = connection.prepareStatement("INSERT INTO match_duo_info (combi_id_1, combi_id_2, win, lose, win_rate, game_version) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE win = win + VALUES(win), lose = lose + VALUES(lose), win_rate = win / (win + lose) * 10000");
        } catch (Exception e) {
            log.failLog("DB connect error" + e.getMessage());
            return false;
        }
        return true;
    }

    private PreparedStatement getCombiInsertPrepareStatement(int size) {
        if (combiInsertPrepareStatementMap.containsKey(size)) {
            return combiInsertPrepareStatementMap.get(size);
        }

        String sb = "INSERT IGNORE INTO combi (champion_id, lane, main_perk) VALUES (?, ?, ?)" +
                ", (?, ?, ?)".repeat(Math.max(0, size - 1));

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sb);
            combiInsertPrepareStatementMap.put(size, preparedStatement);
            return preparedStatement;
        } catch (SQLException e) {
            log.failLog("DB getCombiPrepareStatement error" + e.getMessage());
        }
        return null;
    }

    private PreparedStatement getCombiPrepareStatement(int size) {
        if (combiPrepareStatementMap.containsKey(size)) {
            return combiPrepareStatementMap.get(size);
        }

        // combi pk 설정
        StringBuilder queryBuilder = new StringBuilder();

        // 각 Combi 객체에 대한 쿼리를 생성합니다.
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                queryBuilder.append(" UNION ALL ");
            }
            queryBuilder.append("SELECT combi_id, champion_id, lane, main_perk FROM combi WHERE champion_id = ")
                    .append("(?)")
                    .append(" AND lane = ")
                    .append("(?)")
                    .append(" AND main_perk = ")
                    .append("(?)");
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString());
            combiPrepareStatementMap.put(size, preparedStatement);
            return preparedStatement;
        } catch (SQLException e) {
            log.failLog("DB getCombiPrepareStatement error" + e.getMessage());
        }
        return null;
    }

    public List<Combi> upsertCombi(List<Combi> combiInfos) {
        if(combiInfos.isEmpty())
            return null;
        try {
            PreparedStatement preparedStatement = getCombiInsertPrepareStatement(combiInfos.size());
            int i = 1;
            for (Combi combi : combiInfos) {
                preparedStatement.setInt(i++, combi.championId());
                preparedStatement.setString(i++, combi.lane());
                preparedStatement.setInt(i++, combi.main_perk());
            }
            preparedStatement.execute();

            preparedStatement = getCombiPrepareStatement(combiInfos.size());

            // 각 Combi 객체에 대한 쿼리를 생성합니다.
            for (i = 0; i < combiInfos.size(); i++) {
                Combi combi = combiInfos.get(i);
                preparedStatement.setInt(i * 3 + 1, combi.championId());
                preparedStatement.setString(i * 3 + 2, combi.lane());
                preparedStatement.setInt(i * 3 + 3, combi.main_perk());
            }

            // 완성된 쿼리를 실행합니다.
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

            resultSet.close();
            return combiInfos;
        } catch (SQLException e) {
            log.failLog("DB upsertCombi error" + e.getMessage());
        }
        return null;
    }

    public void insertSoloInfo(List<SoloInfoRecord> soloInfoRecords) {
        try {
            long start = System.currentTimeMillis();
            for (SoloInfoRecord soloInfoRecord : soloInfoRecords) {
                soloPrepareStatement.setInt(1, soloInfoRecord.combiId());
                soloPrepareStatement.setInt(2, soloInfoRecord.win());
                soloPrepareStatement.setInt(3, soloInfoRecord.lose());
                soloPrepareStatement.setInt(4, 0);
                soloPrepareStatement.setString(5, soloInfoRecord.gameVersion());
                soloPrepareStatement.addBatch();
            }
            soloPrepareStatement.executeBatch();
            log.dbLog(System.currentTimeMillis() - start);
        } catch (SQLException e) {
            log.failLog("DB insertSoloInfo error" + e.getMessage());
        }
    }

    public void insertDuoInfo(List<DuoInfoRecord> duoInfoRecords){
        try {
            long start = System.currentTimeMillis();
            for (DuoInfoRecord duoInfoRecord : duoInfoRecords) {
                duoPrepareStatement.setInt(1, duoInfoRecord.combiId1());
                duoPrepareStatement.setInt(2, duoInfoRecord.combiId2());
                duoPrepareStatement.setInt(3, duoInfoRecord.win());
                duoPrepareStatement.setInt(4, duoInfoRecord.lose());
                duoPrepareStatement.setInt(5, 0);
                duoPrepareStatement.setString(6, duoInfoRecord.gameVersion());
                duoPrepareStatement.addBatch();
            }
            duoPrepareStatement.executeBatch();
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
