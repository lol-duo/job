package job.combi;

public record MatchDuoInfoRecord(
        String gameVersion,
        int combiId1,
        int combiId2,
        boolean win
) {
}
