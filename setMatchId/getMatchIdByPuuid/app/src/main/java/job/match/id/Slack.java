package job.match.id;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Slack {
    String cronJobUrl = "";
    String myApp = "2-1번 Job ( Puuid 별 MatchId 수집 )";

    public void send(String message) {
        try {
            String json = template(myApp, message);
            // 요청을 보낼 URL 생성
            URL url = new URL(cronJobUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(5000);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

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
                System.out.println(response);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public String template(String title, String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SlackTemplateRecord slackTemplateRecord = new SlackTemplateRecord(title, message);

            // record -> json
            return mapper.writeValueAsString(slackTemplateRecord);
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
