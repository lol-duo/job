package job.user.puuid;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Api {

    Log log = new Log();

    public <T> T get(String uri, Class<T> responseType) {
        ObjectMapper objectMapper = new ObjectMapper();
        T result = null;
        for(int i = 0; i < 5; i++) {
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
                    log.failLog((i + 1) + "회 HTTP 요청 실패 재시도. 응답 코드: " + responseCode);
                    continue;
                }

                long totalTime = System.currentTimeMillis() - startTime;

                // 3초 이상 걸리면 fail, 1.5초 이상 걸리면 warning
                if(totalTime > 3000)
                    log.failLog("HTTP 요청 성공 time: " + String.format("%7dms ", totalTime) + "uri: " + uri);
                else if(totalTime > 1500)
                    log.warningLog("HTTP 요청 성공 time: " + String.format("%7dms ", totalTime) + "uri: " + uri);
                else
                    log.successLog("HTTP 요청 성공 time: " + String.format("%7dms ", totalTime) + "uri: " + uri);

                // HttpURLConnection 닫기
                conn.disconnect();

                return result;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println( (i + 1) + "회 HTTP 요청 실패 재시도");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
            }
        }

        System.out.println("최종 HTTP 요청 실패 uri: " + uri);
        return result;
    }
}
