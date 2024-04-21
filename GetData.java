import com.sun.management.OperatingSystemMXBean;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayDeque;
import java.util.Locale;

public class GetData {
    static private class MyArrayDeque extends ArrayDeque<Float> {
        private final int timeInterval;

        /**Constructor for the MyArrayDeque class
         * @param timeInterval the time interval in seconds for the data to be stored
         * */
        MyArrayDeque(int timeInterval){
            this.timeInterval = timeInterval;
            for (int i = 0; i < timeInterval; i++) {
                this.add(0f);
            }
        }

        /**Adds a new element to the queue and removes the oldest one if the queue is full*/
        @Override
        public boolean add(Float aFloat) {
            if (size() == timeInterval) {
                removeFirst();
            }
            return super.add(aFloat);
        }

        /**Returns the queue as a string in the format [a, b, c, ...]*/
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (Float f : this) {
                sb.append(f).append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());
            sb.append("]");
            return sb.toString();
        }
    }

    private final MyArrayDeque ramUsage;
    private final MyArrayDeque cpuUsage;

    GetData(int timeInterval){
        ramUsage = new MyArrayDeque(timeInterval);
        cpuUsage = new MyArrayDeque(timeInterval);

        Thread ramThread = new Thread(this::ramUsageThread);
        Thread cpuThread = new Thread(this::cpuUsageThread);

        ramThread.start();
        cpuThread.start();
    }

    /**In a separate thread, calculates the RAM usage every second and adds it to the queue*/
    private void ramUsageThread(){
        while (true) {
            try {
                Thread.sleep(1000);
                float freeMemory = (float) ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreeMemorySize() /1024/1024/1024f;
                float usedMemory = getMaxRam() - freeMemory;
                ramUsage.add(usedMemory);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    String getRamUsage(){
        return ramUsage.toString();
    }

    /**Returns the maximum amount of RAM in GB*/
    float getMaxRam(){
        return (float) ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize() /1024/1024/1024f;
    }

    String getMaxRamString(){
        return String.format(Locale.US,"%.2f", getMaxRam());
    }

    /**In a separate thread, calculates the avg CPU usage every second and adds it to the queue*/
    private void cpuUsageThread(){
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        while (true) {
            double[] cpuUsageLastSec = new double[5];
            try {
                for (int i = 0; i < 5; i++) {
                    Thread.sleep(190);
                    cpuUsageLastSec[i] = osBean.getCpuLoad();
                }
                double cpuUsageAvg = (cpuUsageLastSec[0] + cpuUsageLastSec[1] + cpuUsageLastSec[2] + cpuUsageLastSec[3] + cpuUsageLastSec[4]) / 5;
                cpuUsage.add((float) (cpuUsageAvg * 100));

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    String getCpuUsage(){
        return cpuUsage.toString();
    }

    /**Returns the ROM usage in GB in the format [total, free, used, usage%]*/
    String getRomUsage(){
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
