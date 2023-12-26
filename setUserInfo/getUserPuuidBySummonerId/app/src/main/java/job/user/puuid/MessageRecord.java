package job.user.puuid;

public record MessageRecord(
        String receiptHandle,
        String[] summonerIds
) {
}
