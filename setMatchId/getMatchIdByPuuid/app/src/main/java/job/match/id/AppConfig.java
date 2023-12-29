package job.match.id;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static final AppConfig instance = new AppConfig();

    private Properties properties = new Properties();

    public AppConfig(){

        loadCommonProperties();  // 공통 설정 로드
        String activeProfile = properties.getProperty("profile");
        if (activeProfile == null) {
            activeProfile = "dev"; // 기본값
        }
        loadProfileProperties(activeProfile);  // 프로파일에 따른 설정 로드

    }

    public static AppConfig getInstance() {
        return instance;
    }

    //공통 프로퍼티 로드
    private void loadCommonProperties() {
        properties.putAll(loadProperties("application.properties"));
    }
    //profile 별 프로퍼티 로드
    private void loadProfileProperties(String profile) {
        properties.putAll(loadProperties("application-" + profile + ".properties"));
    }
    
    private static Properties loadProperties(String fileName) {
        Properties properties = new Properties();
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input != null) {
                properties.load(input);
            } else {
                System.out.println("Unable to find application.properties");
            }
        }
        catch (IOException e) {
            System.out.println("Unable to find application.properties");
            e.printStackTrace();
        }
        return properties;
    }
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
