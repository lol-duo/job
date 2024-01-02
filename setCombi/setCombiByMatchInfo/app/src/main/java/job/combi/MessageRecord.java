package job.combi;

public record MessageRecord(
        String receiptHandle,
        MatchRecord[] matchRecords
) {
}
