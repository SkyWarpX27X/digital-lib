package ua.fictionallibrary.digital_lib.logging.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import ua.fictionallibrary.digital_lib.logging.ResourceMonitorService;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ResourceMonitorServiceImpl implements ResourceMonitorService {
    private static final Logger log = LoggerFactory.getLogger(ResourceMonitorServiceImpl.class);
    private static final long MB = 1048576;
    private final String hostName;
    private final long diskMinFree = DataSize.ofGigabytes(1).toBytes();
    private final Map<String, Condition> conditions = new HashMap<>();

    public ResourceMonitorServiceImpl() {
        this.hostName = resolveHostName();
    }

    @Override
    public synchronized void check() {
        long heapUsed = heapUsedAfterGC();
        long heapMax = Runtime.getRuntime().maxMemory();
        Map<String, Long> disks = freeDiskBytes();
        log.debug("Resources of {}: heap after GC {}/{} MB, free disk {} bytes", hostName, heapUsed / MB, heapMax / MB, disks);
        long heapPercent = heapMax > 0 ? Math.round(100.0 * heapUsed / heapMax) : 0;
        double heapThreshold = 0.85;
        update("heap", heapMax > 0 && heapUsed > heapThreshold * heapMax,
                "Екземпляр %s: пам'ять після GC %d%% (%d/%d МБ)".formatted(hostName, heapPercent, heapUsed / MB, heapMax / MB),
                "Екземпляр %s: пам'ять після GC повернулась до норми (%d%%)".formatted(hostName, heapPercent));
        disks.forEach((dir, free) -> update("disk:" + dir, free < diskMinFree,
                "Екземпляр %s: на диску '%s' вільно %d МБ".formatted(hostName, dir, free / MB),
                "Екземпляр %s: вільне місце на диску '%s' повернулось до норми (%d МБ)".formatted(hostName, dir, free / MB)));
    }

    private long heapUsedAfterGC() {
        long used = 0;
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            MemoryUsage afterGc = pool.getType() == MemoryType.HEAP ? pool.getCollectionUsage() : null;
            if (afterGc != null)
                used += afterGc.getUsed();
        }
        return used;
    }

    private Map<String, Long> freeDiskBytes() {
        Map<String, Long> free = new LinkedHashMap<>();
        File logs = new File("logs");
        free.put("logs", (logs.exists() ? logs : new File(".")).getAbsoluteFile().getUsableSpace());
        free.put("tmp", new File(System.getProperty("java.io.tmpdir")).getUsableSpace());
        return free;
    }

    private void update(String name, boolean bad, String problem, String recovered) {
        Condition condition = conditions.computeIfAbsent(name, _ -> new Condition());
        if (bad) {
            condition.streak++;
            int consecutiveChecks = 3;
            if (condition.streak >= consecutiveChecks && !condition.alerted) {
                condition.alerted = true;
                log.warn("{} понад {} хв", problem, consecutiveChecks);
            }
        } else {
            if (condition.alerted)
                log.info(recovered);
            condition.streak = 0;
            condition.alerted = false;
        }
    }

    private static String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException _) {
            return "unknown";
        }
    }

    private static final class Condition {
        int streak;
        boolean alerted;
    }
}
