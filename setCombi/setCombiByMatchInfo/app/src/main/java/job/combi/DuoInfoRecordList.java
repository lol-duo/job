package job.combi;

import java.util.List;

public record DuoInfoRecordList(
        String id,
        List<DuoInfoRecord> duoInfoRecordList
){
}

record DuoInfoRecord(
        String gameVersion,
        int combiId1,
        int combiId2,
        int win,
        int lose
) {
}
