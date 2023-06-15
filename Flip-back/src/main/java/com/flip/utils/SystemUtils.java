package com.flip.utils;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.Sensors;
import oshi.util.Util;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class SystemUtils {

    private static final HardwareAbstractionLayer hardware;

    static {
        hardware = new SystemInfo().getHardware();
    }

    public static Map<String, String> getMemory() {
        GlobalMemory memory = hardware.getMemory();
        long total = memory.getTotal();
        long available = memory.getAvailable();
        Map<String, String> memoryMap = new HashMap<>();
        memoryMap.put("total", formatByte(total));
        memoryMap.put("available", formatByte(available));
        return memoryMap;
    }

    public static Map<String, Object> getProcessor() {
        Sensors sensors = hardware.getSensors();
        double cpuTemperature = sensors.getCpuTemperature();

        CentralProcessor processor = hardware.getProcessor();
        long[] preTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - preTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - preTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softIrq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - preTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - preTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - preTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - preTicks[CentralProcessor.TickType.USER.getIndex()];
        long ioWait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - preTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - preTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = nice + irq + softIrq + steal + sys + user + ioWait + idle;

        Map<String, Object> cpuMap = new HashMap<>();
        cpuMap.put("coreNum", processor.getLogicalProcessorCount());
        cpuMap.put("idle", new DecimalFormat("#.##%").format(1.0 - (idle * 1.0 / totalCpu)));
        cpuMap.put("temperature", cpuTemperature);
        return cpuMap;
    }

    private static String formatByte(long byteNumber) {
        double FORMAT = 1024.0;
        double kbNumber = byteNumber / FORMAT;
        if (kbNumber < FORMAT) {
            return new DecimalFormat("#.##KB").format(kbNumber);
        }
        double mbNumber = kbNumber / FORMAT;
        if (mbNumber < FORMAT) {
            return new DecimalFormat("#.##MB").format(mbNumber);
        }
        double gbNumber = mbNumber / FORMAT;
        if (gbNumber < FORMAT) {
            return new DecimalFormat("#.##GB").format(gbNumber);
        }
        double tbNumber = gbNumber / FORMAT;
        return new DecimalFormat("#.##TB").format(tbNumber);
    }
}
