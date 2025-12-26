package com.example.memlimit.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class JvmInfoService {

    public void printStartupInfo() {
        log.info("========================================");
        log.info("JVM Startup Information");
        log.info("========================================");
        
        // Print JVM startup parameters
        printJvmArguments();
        
        // Print heap memory information
        printMemoryInfo();
        
        // Print GC information
        printGcInfo();
        
        // Print system information
        printSystemInfo();
        
        // Detect container environment
        detectContainerEnvironment();
        
        log.info("========================================");
    }

    private void printJvmArguments() {
        log.info("\n--- JVM Arguments ---");
        List<String> arguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        if (arguments.isEmpty()) {
            log.info("No JVM arguments specified");
        } else {
            for (String arg : arguments) {
                log.info("  {}", arg);
            }
        }
    }

    private void printMemoryInfo() {
        log.info("\n--- Heap Memory Information ---");
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        
        log.info("Heap Memory:");
        log.info("  Initial:   {} MB", heapMemoryUsage.getInit() / (1024 * 1024));
        log.info("  Used:      {} MB", heapMemoryUsage.getUsed() / (1024 * 1024));
        log.info("  Committed: {} MB", heapMemoryUsage.getCommitted() / (1024 * 1024));
        log.info("  Max:       {} MB", heapMemoryUsage.getMax() / (1024 * 1024));
        
        log.info("Non-Heap Memory:");
        log.info("  Initial:   {} MB", nonHeapMemoryUsage.getInit() / (1024 * 1024));
        log.info("  Used:      {} MB", nonHeapMemoryUsage.getUsed() / (1024 * 1024));
        log.info("  Committed: {} MB", nonHeapMemoryUsage.getCommitted() / (1024 * 1024));
        log.info("  Max:       {} MB", nonHeapMemoryUsage.getMax() / (1024 * 1024));
    }

    private void printGcInfo() {
        log.info("\n--- Garbage Collector Information ---");
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        if (gcBeans.isEmpty()) {
            log.info("No Garbage Collectors available");
        } else {
            for (GarbageCollectorMXBean gcBean : gcBeans) {
                log.info("GC Name: {}", gcBean.getName());
                log.info("  Collection Count: {}", gcBean.getCollectionCount());
                log.info("  Collection Time:  {} ms", gcBean.getCollectionTime());
                log.info("  Memory Pool Names: {}", String.join(", ", gcBean.getMemoryPoolNames()));
            }
        }
    }

    private void printSystemInfo() {
        log.info("\n--- System Information ---");
        Runtime runtime = Runtime.getRuntime();
        
        log.info("JVM Information:");
        log.info("  Name:    {}", ManagementFactory.getRuntimeMXBean().getVmName());
        log.info("  Vendor:  {}", ManagementFactory.getRuntimeMXBean().getVmVendor());
        log.info("  Version: {}", ManagementFactory.getRuntimeMXBean().getVmVersion());
        log.info("  Java Version: {}", System.getProperty("java.version"));
        
        log.info("Operating System:");
        log.info("  Name:    {}", System.getProperty("os.name"));
        log.info("  Version: {}", System.getProperty("os.version"));
        log.info("  Architecture: {}", System.getProperty("os.arch"));
        
        log.info("System Resources:");
        log.info("  Available Processors: {}", runtime.availableProcessors());
        log.info("  Total Memory:  {} MB", runtime.totalMemory() / (1024 * 1024));
        log.info("  Free Memory:   {} MB", runtime.freeMemory() / (1024 * 1024));
        log.info("  Max Memory:    {} MB", runtime.maxMemory() / (1024 * 1024));
    }

    private void detectContainerEnvironment() {
        log.info("\n--- Container Environment Detection ---");
        
        boolean isInDocker = false;
        boolean isInKubernetes = false;
        
        // Check for Docker environment
        File dockerEnv = new File("/.dockerenv");
        if (dockerEnv.exists()) {
            isInDocker = true;
            log.info("Running in Docker container (detected via /.dockerenv)");
        }
        
        // Check /proc/1/cgroup for container indicators
        try {
            if (Files.exists(Paths.get("/proc/1/cgroup"))) {
                List<String> lines = Files.readAllLines(Paths.get("/proc/1/cgroup"));
                for (String line : lines) {
                    if (line.contains("docker") || line.contains("containerd")) {
                        isInDocker = true;
                        log.info("Running in Docker container (detected via /proc/1/cgroup)");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not read /proc/1/cgroup: {}", e.getMessage());
        }
        
        // Check for Kubernetes environment
        String k8sServiceHost = System.getenv("KUBERNETES_SERVICE_HOST");
        if (k8sServiceHost != null && !k8sServiceHost.isEmpty()) {
            isInKubernetes = true;
            log.info("Running in Kubernetes (detected via KUBERNETES_SERVICE_HOST: {})", k8sServiceHost);
        }
        
        if (!isInDocker && !isInKubernetes) {
            log.info("Not running in a detected container environment");
        }
    }

    public Map<String, Object> getJvmInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // JVM Arguments
        info.put("jvmArguments", ManagementFactory.getRuntimeMXBean().getInputArguments());
        
        // Memory Info
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        
        Map<String, Object> heapInfo = new HashMap<>();
        heapInfo.put("init", heapMemoryUsage.getInit());
        heapInfo.put("used", heapMemoryUsage.getUsed());
        heapInfo.put("committed", heapMemoryUsage.getCommitted());
        heapInfo.put("max", heapMemoryUsage.getMax());
        info.put("heapMemory", heapInfo);
        
        Map<String, Object> nonHeapInfo = new HashMap<>();
        nonHeapInfo.put("init", nonHeapMemoryUsage.getInit());
        nonHeapInfo.put("used", nonHeapMemoryUsage.getUsed());
        nonHeapInfo.put("committed", nonHeapMemoryUsage.getCommitted());
        nonHeapInfo.put("max", nonHeapMemoryUsage.getMax());
        info.put("nonHeapMemory", nonHeapInfo);
        
        // GC Info
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        Map<String, Object> gcInfo = new HashMap<>();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            Map<String, Object> gcDetails = new HashMap<>();
            gcDetails.put("collectionCount", gcBean.getCollectionCount());
            gcDetails.put("collectionTime", gcBean.getCollectionTime());
            gcDetails.put("memoryPoolNames", gcBean.getMemoryPoolNames());
            gcInfo.put(gcBean.getName(), gcDetails);
        }
        info.put("garbageCollectors", gcInfo);
        
        // System Info
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("jvmName", ManagementFactory.getRuntimeMXBean().getVmName());
        systemInfo.put("jvmVendor", ManagementFactory.getRuntimeMXBean().getVmVendor());
        systemInfo.put("jvmVersion", ManagementFactory.getRuntimeMXBean().getVmVersion());
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        systemInfo.put("osArch", System.getProperty("os.arch"));
        
        Runtime runtime = Runtime.getRuntime();
        systemInfo.put("availableProcessors", runtime.availableProcessors());
        systemInfo.put("totalMemory", runtime.totalMemory());
        systemInfo.put("freeMemory", runtime.freeMemory());
        systemInfo.put("maxMemory", runtime.maxMemory());
        info.put("system", systemInfo);
        
        // Container Environment
        Map<String, Object> containerInfo = new HashMap<>();
        File dockerEnv = new File("/.dockerenv");
        containerInfo.put("isDocker", dockerEnv.exists());
        containerInfo.put("isKubernetes", System.getenv("KUBERNETES_SERVICE_HOST") != null);
        info.put("container", containerInfo);
        
        return info;
    }
}
