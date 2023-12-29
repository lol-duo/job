package job.match.info;

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

    String matchIdQueue = "MatchId.fifo";
    String matchIdQueueUrl = "https://sqs.ap-northeast-2.amazonaws.com/809120139230/" + matchIdQueue;
    String matchInfoQueue = "MatchInfo.fifo";
    String matchInfoQueueUrl = "https://sqs.ap-northeast-2.amazonaws.com/809120139230/" + matchInfoQueue;
    AmazonSQS matchIdSQS;
    AmazonSQS matchInfoSQS;

    public Aws() {
        AWSCredentials credentials = new AWSCredential();

        matchIdSQS = AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion("ap-northeast-2")
                .build();

        matchInfoSQS = AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion("ap-northeast-2")
                .build();
    }

    public void sendMessage(List args) {
        if(args.isEmpty())
            return;

        String queueUrl = matchIdQueueUrl;
        if(args.get(0) instanceof MatchRecord)
            queueUrl = matchInfoQueueUrl;

        try {
            String groupId = String.format("%02d%d", (int) (Math.random() * 100), System.currentTimeMillis());

            SendMessageRequest send_msg_request = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(args.toString())
                    .withMessageGroupId(groupId)
                    .withMessageDeduplicationId(groupId);

            matchIdSQS.sendMessage(send_msg_request);
        } catch (Exception e) {
            log.failLog(args + " send fail : " + e.getMessage());
        }
    }

    public MatchIdMessageRecord receiveMessage() {
        try {
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                    .withQueueUrl(matchIdQueueUrl)
                    .withMaxNumberOfMessages(1)
                    .withWaitTimeSeconds(20)
                    .withVisibilityTimeout(3000);
            ReceiveMessageResult receiveMessageResult = matchIdSQS.receiveMessage(receiveMessageRequest);
            if (receiveMessageResult.getMessages().isEmpty())
                return null;

            String puuidListMessage = receiveMessageResult.getMessages().get(0).getBody();
            // [abc, def, ghi] -> abc, def, ghi
            String[] matchIds = puuidListMessage.replace("[", "").replace("]", "").split(", ");
            String receiptHandle = receiveMessageResult.getMessages().get(0).getReceiptHandle();

            return new MatchIdMessageRecord(receiptHandle, matchIds);
        } catch (Exception e) {
            log.failLog("SQS receive fail" + e.getMessage());
            return null;
        }
    }

    public void deleteMessage(String receiptHandle) {
        try {
            DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest()
                    .withQueueUrl(matchIdQueueUrl)
                    .withReceiptHandle(receiptHandle);
            matchIdSQS.deleteMessage(deleteMessageRequest);
        } catch (Exception e) {
            log.failLog("SQS delete fail" + e.getMessage());
        }
    }
}
