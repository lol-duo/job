package job.match.info;

import com.amazonaws.auth.AWSCredentials;

public class AWSCredential implements AWSCredentials {
    @Override
    public String getAWSAccessKeyId() {
        return "";
    }

    @Override
    public String getAWSSecretKey() {
        return "";
    }
}
