package job.user.puuid;

public record SlackTemplateRecord(
        Object[] blocks
) {
    public SlackTemplateRecord(String title, String message) {
        this(
                new Object[]{
                        new SlackHeaderRecord(title),
                        new SlackMessageRecord(message)
                }
        );
    }
}

record SlackHeaderRecord(String type, SlackTextRecord text){
    public SlackHeaderRecord(String text){
        this("header", new SlackTextRecord("plain_text", text));
    }
}

record SlackTextRecord(String type, String text){
}

record SlackMessageRecord(
        String type,
        Object[] elements
){
    public SlackMessageRecord(String text){
        this(
                "rich_text",
                new SlackMessageRecord[]{
                        new SlackMessageRecord(
                                "rich_text_preformatted",
                                new SlackMessageRecordElement[]{
                                        new SlackMessageRecordElement(
                                                "text",
                                                text
                                        )
                                }
                        )
                }
        );
    }
}

record SlackMessageRecordElement(
        String type,
        String text
){
}
