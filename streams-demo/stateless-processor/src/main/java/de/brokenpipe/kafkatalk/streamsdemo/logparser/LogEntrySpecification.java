package de.brokenpipe.kafkatalk.streamsdemo.logparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogEntrySpecification {

    private static final Pattern LOG_ENTRY_PATTERN = Pattern.compile("(?<ipaddress>\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}) - - \\[(?<dateandtime>\\d{2}/\\w{3}/\\d{4}:\\d{2}:\\d{2}:\\d{2} ([+-])\\d{4})] ((\"(GET|POST) )(?<url>.+)(HTTP/1\\.1\")) (?<statuscode>\\d{3}) (?<bytessent>\\d+) ([\"](?<referrer>(-)|(.+))[\"]) ([\"](?<useragent>.+)[\"])");

    public static LogEntry of(String value) {
        Matcher matcher = LOG_ENTRY_PATTERN.matcher(value);

        if (!matcher.find()) {
            return null;
        }

        return new LogEntry(
            matcher.group("ipaddress"),
            matcher.group("dateandtime"),
            matcher.group("url"),
            Integer.parseInt(matcher.group("statuscode")),
            Long.parseLong(matcher.group("bytessent"))
        );
    }
}
