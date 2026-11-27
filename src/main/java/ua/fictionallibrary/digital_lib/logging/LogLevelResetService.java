package ua.fictionallibrary.digital_lib.logging;

import java.time.Instant;

public interface LogLevelResetService {
    void check(Instant now);
}
