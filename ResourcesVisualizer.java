import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;

public class ResourcesVisualizer{
    private final int port;
    private final int timeInterval;
    private final GetData dataSource;

    /**Constructor for the ResourcesVisualizer class
     * @param port the port on which the server will run
     * @param timeInterval the time interval in seconds for the data to be stored
     * */
    ResourcesVisualizer(int timeInterval, int port){
        this.port = port;
        this.timeInterval = timeInterval;
        this.dataSource = new GetData(timeInterval);
    }

    void startServer(){
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new MainHandler()); //Main page

            //Handlers for the data endpoints
            server.createContext("/ram", new RamDataHandler());
            server.createContext("/maxram", new MaxRamHandler());
            server.createContext("/cpu", new CpuDataHandler());
            server.createContext("/rom", new RomDataHandler());
            server.createContext("/time", new TimeHandler());

            server.setExecutor(null); // creates a default executor
            server.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MainHandler implements HttpHandler {
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
                    if (line.contains("const xValues =")){
                        requestBuilder.append("const xValues = [");
                        for (int i = timeInterval - 1; i > 0; i--) {
                            requestBuilder.append(-i).append(", ");
                        }
                        requestBuilder.append("0];\n");
                    }
                    else{
                        requestBuilder.append(line).append("\n");
                    }
                }
            }
            return requestBuilder.toString();
        }
    }

    private class RamDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = dataSource.getRamUsage(); //Getting the RAM usage data from the last 20s in format [a, b, c, ...]

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
                bos.write(response.getBytes());
            }
        }
    }

    private class MaxRamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = dataSource.getMaxRamString(); //Getting the maximum RAM available in the system

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
                bos.write(response.getBytes());
            }
        }
    }

    private class CpuDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = dataSource.getCpuUsage(); //Getting the CPU usage data from the last 20s in format [a, b, c, ...]

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
                bos.write(response.getBytes());
            }
        }
    }

    private class RomDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = dataSource.getRomUsage(); //Getting the ROM usage data in format [total, free, used, usage%]

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
                bos.write(response.getBytes());
            }
        }
    }

    private class TimeHandler implements HttpHandler {
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
