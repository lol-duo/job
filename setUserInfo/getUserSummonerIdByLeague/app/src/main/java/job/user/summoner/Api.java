package job.user.summoner;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Api {

    Log log = new Log();

    public <T> T get(String uri, Class<T> responseType) {
        ObjectMapper objectMapper = new ObjectMapper();
        T result = null;
        List<String> errorLog = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            try {
                // 요청을 보낼 URL 생성
                URL url = new URL(uri);

                // HttpURLConnection 객체 생성 및 설정
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET"); // GET 요청 설정
                conn.setConnectTimeout(5000); // 연결 제한 시간 설정

                long startTime = System.currentTimeMillis();

                // 응답 코드 확인
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 응답 데이터를 읽어오기 위한 BufferedReader 생성
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    // 응답 데이터를 모두 읽어오기
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    // BufferedReader 닫기
                    in.close();

                    result = objectMapper.readValue(response.toString(), responseType);
                } else {
                    log.log((i + 1) + "회 HTTP 요청 실패 재시도. 응답 코드: " + responseCode);
                    errorLog.add("응답 코드: " + responseCode);
                    continue;
                }

                log.apiLog(System.currentTimeMillis() - startTime, uri);

                // HttpURLConnection 닫기
                conn.disconnect();

                return result;
            } catch (Exception e) {
                log.log((i + 1) + "회 HTTP 요청 실패 재시도 : " + e.getMessage());
                errorLog.add(e.getMessage());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
            }
        }

        log.failLog("최종 HTTP 요청 실패 uri: " + uri + "\nerrorLog: " + errorLog);
        return result;
    }
}
