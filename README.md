# Resources-Visualizer
Simple PC resource visualizer as a web page (default - localhost:8080). Using Java for a local server.

Files breakdown:
- Main.java - contains the main method that starts the server. It creates an instance of ResourcesVisualizer with arguments of the timeInterval (a time period in seconds for data storage) and port number (the port on which the server will run) and then calls the startServer method.
- ResourcesVisualizer.java - contains the ResourcesVisualizer class, which sets up an HTTP server using the HttpServer class from the com.sun.net.httpserver package. It creates endpoints for different data types (RAM, CPU, ROM, etc.) and handles requests for these endpoints.
- GetData.java - contains the GetData class, which collects and stores data on RAM, ROM and CPU usage in separate threads. It uses a custom MyArrayDeque class to store data in a circular buffer-like structure.
- page.html - an HTML file that serves as the main page for the server. It contains placeholders for displaying data and JavaScript code for updating the data in real-time.


![image](https://github.com/Jogurtonelle/Resources-Visualizer/assets/117392254/899fdafd-32d3-4640-82aa-bbbfafc628bf)
