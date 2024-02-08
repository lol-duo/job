package job.combi;

import java.util.List;

public record SoloInfoRecordList(
        String id,
        List<SoloInfoRecord> soloInfoRecordList
){
}
record SoloInfoRecord(
        String gameVersion,
        int combiId,
        int win,
        int lose
) {
}
