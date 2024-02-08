package job.combi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static final AppConfig instance = new AppConfig();
    private final Properties properties = new Properties();
    private static final Log log = new Log();
    public static String activeProfile;
    public AppConfig(){

        loadCommonProperties();  // 공통 설정 로드
        activeProfile = properties.getProperty("profile");
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
                log.failLog("Unable to find " + fileName + " in application.properties, Properties Name : " + fileName);
            }
        }
        catch (IOException e) {
            log.failLog("Unable to find application.properties, errorMessage : " + e.getMessage());
        }
        return properties;
    }
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
