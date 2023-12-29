package job.match.info;

public record MatchIdMessageRecord(
        String receiptHandle,
        String[] matchIds
) {
}
