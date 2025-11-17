package github.eliotweber.webrtc_server;

import java.util.Map;
import java.time.Instant;

public record Event (
    EventType type,
    Map<String, Object> data,
    Instant time
) { }
