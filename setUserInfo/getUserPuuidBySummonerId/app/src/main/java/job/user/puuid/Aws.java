package job.user.puuid;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import java.util.Arrays;
import java.util.List;

public class Aws {


    String queueName = "SummonerId.fifo";
    String queueUrl = "https://sqs.ap-northeast-2.amazonaws.com/809120139230/" + queueName;
    AmazonSQS sqs;

    public Aws () {
        AWSCredentials credentials = new AWSCredential();

        sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion("ap-northeast-2")
                .build();
    }

    public boolean sendMessage(List<String> args) {
        try {
            SendMessageRequest send_msg_request = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(args.toString())
                    .withMessageGroupId(args.get(0))
                    .withMessageDeduplicationId(args.get(0));

            sqs.sendMessage(send_msg_request);
            return true;
        } catch (Exception e) {
            System.out.println(args.toString() + " send fail");
            e.printStackTrace();
            return false;
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

            String summonerIdListMessage = receiveMessageResult.getMessages().get(0).getBody();
            // [abc, def, ghi] -> abc, def, ghi
            String[] summonerIds = summonerIdListMessage.replace("[", "").replace("]", "").split(", ");
            String receiptHandle = receiveMessageResult.getMessages().get(0).getReceiptHandle();

            return new MessageRecord(receiptHandle, summonerIds);
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }
}
