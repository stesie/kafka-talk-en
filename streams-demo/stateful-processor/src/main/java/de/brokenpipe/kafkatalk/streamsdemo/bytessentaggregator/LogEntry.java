package de.brokenpipe.kafkatalk.streamsdemo.bytessentaggregator;

import lombok.Data;

@Data
public class LogEntry {
    private final String ipAddress;
    private final String dateAndTime;
    private final String url;
    private final int statusCode;
    private final long bytesSent;
}
