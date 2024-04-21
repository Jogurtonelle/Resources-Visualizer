import com.sun.management.OperatingSystemMXBean;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Locale;

public class GetData {
    /**In a separate thread, calculates the RAM usage every second and adds it to the queue*/
    static void ramUsage(){
        while (true) {
            try {
                Thread.sleep(1000);
                float freeMemory = (float) ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreeMemorySize() /1024/1024/1024f;
                float usedMemory = maxRam() - freeMemory;
                Main.ramUsage.add(usedMemory);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**Returns the maximum amount of RAM in GB*/
    static float maxRam(){
        return (float) ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize() /1024/1024/1024f;
    }

    /**In a separate thread, calculates the avg CPU usage every second and adds it to the queue*/
    static void CPUUsage(){
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        while (true) {
            double[] cpuUsage = new double[5];
            try {
                for (int i = 0; i < 5; i++) {
                    Thread.sleep(190);
                    cpuUsage[i] = osBean.getCpuLoad();
                }
                double cpuUsageAvg = (cpuUsage[0] + cpuUsage[1] + cpuUsage[2] + cpuUsage[3] + cpuUsage[4]) / 5;
                Main.cpuUsage.add((float) (cpuUsageAvg * 100));

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**Returns the ROM usage in GB in the format [total, free, used, usage%]*/
    static String RomUsage(){
        double totalSpace = (double) new File("/").getTotalSpace() /1024/1024/1024.0;
        double freeSpace = (double) new File("/").getFreeSpace() /1024/1024/1024.0;
        double usedSpace = totalSpace - freeSpace;
        double usage = usedSpace / totalSpace * 100;

        String freeSpaceString = String.format(Locale.US,"%.2f", freeSpace);
        String totalSpaceString = String.format(Locale.US,"%.2f", totalSpace);
        String usedSpaceString = String.format(Locale.US,"%.2f", usedSpace);
        String usageString = String.format(Locale.US,"%.2f", usage);

        return "[" + totalSpaceString + ", " + freeSpaceString + "," + usedSpaceString + "," + usageString + "]";
    }
}
