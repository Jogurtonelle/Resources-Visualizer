import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;

public class Main {
    /**The maximum number of elements (past seconds)to remember in the queue*/
    public static final int MAX_USAGE_REMEMBER = 21;
    static private class MyArrayDeque extends ArrayDeque<Float> {
        //Initializes the queue with 0s
        {
            for (int i = 0; i < MAX_USAGE_REMEMBER; i++) {
                add(0f);
            }
        }

        /**Adds a new element to the queue and removes the oldest one if the queue is full*/
        @Override
        public boolean add(Float aFloat) {
            if (size() == MAX_USAGE_REMEMBER) {
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

    //Queues for storing data about RAM and CPU usage in the last 20 seconds
    static ArrayDeque<Float> ramUsage = new MyArrayDeque();
    static ArrayDeque<Float> cpuUsage = new MyArrayDeque();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new MainHandler()); //Main page

        //Handlers for the data endpoints
        server.createContext("/ram", new RamDataHandler());
        server.createContext("/maxram", new MaxRamHandler());
        server.createContext("/rom", new RomDataHandler());
        server.createContext("/time", new TimeHandler());
        server.createContext("/cpu", new CpuDataHandler());


        server.setExecutor(null);
        server.start();

        //Threads for getting data from the system
        Thread ramThread = new Thread(GetData::ramUsage);
        Thread cpuThread = new Thread(GetData::CPUUsage);

        ramThread.start();
        cpuThread.start();
    }

    static class MainHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            try {
                String htmlFile = readFile("src/page.html"); //Reading the HTML file
                byte[] htmlBytes = htmlFile.getBytes(); //Converting the string to bytes
                t.sendResponseHeaders(200, htmlBytes.length); //Sending the response header
                OutputStream os = t.getResponseBody(); //Getting the output stream
                try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
                    bos.write(htmlBytes); //Writing the HTML file to the output stream
                }
            } catch (IOException e) {
                t.sendResponseHeaders(404, 0);
            }
        }

        /**Function for reading an HTML file and returning its content as a string
         * @param path path to the file
         * */
        private String readFile(String path) throws IOException{
            StringBuilder requestBuilder = new StringBuilder();
            try (BufferedReader reader = Files.newBufferedReader(Path.of(path))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBuilder.append(line).append("\n");
                }
            }
            return requestBuilder.toString();
        }
    }

    static class RamDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = ramUsage.toString(); //Getting the RAM usage data from the last 20s in format [a, b, c, ...]

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
                bos.write(response.getBytes());
            }
        }
    }

    static class MaxRamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = String.valueOf(GetData.maxRam()); //Getting the maximum RAM available in the system

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
                bos.write(response.getBytes());
            }
        }
    }

    static class CpuDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = cpuUsage.toString(); //Getting the CPU usage data from the last 20s in format [a, b, c, ...]

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
                bos.write(response.getBytes());
            }
        }
    }

    static class RomDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = GetData.RomUsage(); //Getting the ROM usage data in format [total, free, used, usage%]

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
                bos.write(response.getBytes());
            }
        }
    }

    static class TimeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
                bos.write(response.getBytes());
            }
        }
    }
}