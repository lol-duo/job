package job.match.id;

public record MessageRecord(
        String receiptHandle,
        String[] puuids
) {
}
