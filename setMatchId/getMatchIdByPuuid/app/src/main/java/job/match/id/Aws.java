package job.match.id;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.util.List;

public class Aws {

    Log log = new Log();

    String queueName = "Puuid.fifo";
    String queueUrl = "https://sqs.ap-northeast-2.amazonaws.com/809120139230/" + queueName;
    AmazonSQS sqs;

    public Aws() {
        AWSCredentials credentials = new AWSCredential();

        sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion("ap-northeast-2")
                .build();
    }

    public void sendMessage(List<String> args) {
        try {
            SendMessageRequest send_msg_request = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(args.toString())
                    .withMessageGroupId(args.get(0))
                    .withMessageDeduplicationId(args.get(0));

            sqs.sendMessage(send_msg_request);
        } catch (Exception e) {
            log.failLog(args.toString() + " send fail : " + e.getMessage());
        }
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

            String puuidListMessage = receiveMessageResult.getMessages().get(0).getBody();
            // [abc, def, ghi] -> abc, def, ghi
            String[] puuids = puuidListMessage.replace("[", "").replace("]", "").split(", ");
            String receiptHandle = receiveMessageResult.getMessages().get(0).getReceiptHandle();
            return new MessageRecord(receiptHandle, puuids);
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
