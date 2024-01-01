package job.match.info;

import java.util.List;

public record MatchIdRecord(
        String id,
        List<String> matchIdList
) {
}
