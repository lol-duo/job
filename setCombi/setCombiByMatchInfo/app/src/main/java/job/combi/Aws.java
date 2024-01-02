package job.combi;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Aws {

    Log log = new Log();

    String queueName = "MatchInfo.fifo";
    String queueUrl = "https://sqs.ap-northeast-2.amazonaws.com/809120139230/" + queueName;
    AmazonSQS sqs;

    public Aws() {
        AWSCredentials credentials = new AWSCredential();

        sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion("ap-northeast-2")
                .build();
    }

    public MessageRecord receiveMessage() {
        try {
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMaxNumberOfMessages(1)
                    .withWaitTimeSeconds(20)
                    .withVisibilityTimeout(3000);
            ReceiveMessageResult receiveMessageResult = sqs.receiveMessage(receiveMessageRequest);
            if (receiveMessageResult.getMessages().isEmpty())
                return null;

            ObjectMapper objectMapper = new ObjectMapper();
            String matchRecordListMessage = receiveMessageResult.getMessages().get(0).getBody();
            MatchRecord[] matchRecords = objectMapper.readValue(matchRecordListMessage, MatchRecord[].class);

            String receiptHandle = receiveMessageResult.getMessages().get(0).getReceiptHandle();
            return new MessageRecord(receiptHandle, matchRecords);
        } catch (Exception e) {
            log.failLog("SQS receive fail" + e.getMessage());
            return null;
        }
    }

    public void deleteMessage(String receiptHandle) {
        try {
            DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withReceiptHandle(receiptHandle);
            sqs.deleteMessage(deleteMessageRequest);
        } catch (Exception e) {
            log.failLog("SQS delete fail" + e.getMessage());
        }
    }
}
