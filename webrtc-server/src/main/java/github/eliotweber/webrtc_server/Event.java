package github.eliotweber.webrtc_server;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public record Event(
    Timestamp time,
    List<String> types,
    Map<String, Object> data
) { }
