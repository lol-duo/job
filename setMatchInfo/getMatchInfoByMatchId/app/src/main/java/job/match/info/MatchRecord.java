package job.match.info;

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
        String individualPosition,
        int championId,
        String lane,
        PerksRecord perks,
        String puuid,
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
        SelectionRecord[] selections,
        int style
) {

}

record SelectionRecord(
        int perk
) {

}