package job.user.puuid;

import com.amazonaws.auth.AWSCredentials;

public class AWSCredential implements AWSCredentials {
    AppConfig appConfig = AppConfig.getInstance();

    @Override
    public String getAWSAccessKeyId() {
        return appConfig.getProperty("aws.accessKey");
    }

    @Override
    public String getAWSSecretKey() {
        return appConfig.getProperty("aws.secretKey");
    }
}
