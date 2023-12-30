package job.combi;

public record MatchRecord(
        MetadataRecord metadata,
        MatchInfoRecord info
) {
}

record MetadataRecord(
        String matchId
) {

}

record MatchInfoRecord(
        String gameVersion,
        ParticipantRecord[] participants,
        int queueId
) {

}

record ParticipantRecord(
        int championId,
        PerksRecord perks,
        String teamPosition,
        boolean win
) {

}

record PerksRecord(
        StyleRecord[] styles
) {

}

record StyleRecord(
        String description,
        SelectionRecord[] selections
) {

}

record SelectionRecord(
        int perk
) {

}