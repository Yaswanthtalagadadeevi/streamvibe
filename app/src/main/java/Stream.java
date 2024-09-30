public class Stream {
    private String hostName;
    private String streamTitle;
    private String streamDescription;
    private long timestamp;

    public Stream(String hostName, String streamTitle, String streamDescription, long timestamp) {
        this.hostName = hostName;
        this.streamTitle = streamTitle;
        this.streamDescription = streamDescription;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getHostName() { return hostName; }
    public String getStreamTitle() { return streamTitle; }
    public String getStreamDescription() { return streamDescription; }
    public long getTimestamp() { return timestamp; }
}
