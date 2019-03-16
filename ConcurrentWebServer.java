import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class ConcurrentWebServer implements Runnable{
    public static int portNumber = 8080;

    Socket clientSocket;

    private ConcurrentWebServer(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public static void main(String args[]) throws IOException {
        ServerSocket server = new ServerSocket(portNumber);
        System.out.println("Listening for connection on port " + portNumber + " ....");

        while (true) {
            try {
                Socket clientSocket = server.accept();
                System.out.println("Connection Accepted");
                new Thread(new ConcurrentWebServer(clientSocket)).start();
            } catch (Exception e) {

            }
        }
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            PrintStream writer = new PrintStream(new BufferedOutputStream(clientSocket.getOutputStream()));

            String line = reader.readLine();
            while (!line.isEmpty()) {
                System.out.println(line);

                if (line.startsWith("GET")) {
                    int firstSlashLocation = line.indexOf('/');
                    int secondSpaceLocation = line.indexOf(" ", firstSlashLocation);

                    String requestedResourcePath;
                    if (line.substring(firstSlashLocation, secondSpaceLocation).endsWith("/")) {
                        requestedResourcePath = line.substring(firstSlashLocation, secondSpaceLocation);
                    } else {
                        requestedResourcePath = line.substring(firstSlashLocation, secondSpaceLocation) + "/";
                    }

                    List<String> slashSeparatedStrings = Arrays.asList(requestedResourcePath.split("/"));
                    String actualRequestedResourcePath;

                    boolean requestedResourceIsDirectory = true;

                    if (slashSeparatedStrings.size() != 0) {
                        if (slashSeparatedStrings.get(slashSeparatedStrings.size() - 1).contains(".")) {
                            actualRequestedResourcePath = requestedResourcePath;
                            requestedResourceIsDirectory = false;
                        } else {
                            actualRequestedResourcePath = requestedResourcePath + "index.html";
                        }
                    } else {
                        actualRequestedResourcePath = requestedResourcePath + "index.html";
                    }

                    String httpResponse;

                    if (requestedResourceIsDirectory) {
                        if (new File(requestedResourcePath).exists()) {
                            System.out.println("Directory Found");

                            if (new File(actualRequestedResourcePath).exists()) {
                                httpResponse = "HTTP/1.0 200 OK\r\n\r\n";
                                System.out.println("File Found");
                                writer.write(httpResponse.getBytes("UTF-8"));

                                byte[] a = new byte[4096];
                                int n;
                                InputStream inputStream = new FileInputStream(actualRequestedResourcePath);
                                while ((n = inputStream.read(a)) > 0) {
                                    writer.write(a, 0, n);
                                }
                            } else {
                                httpResponse = "HTTP/1.0 200 OK\r\n\r\n" + "Directory has got Files";
                                writer.write(httpResponse.getBytes("UTF-8"));
                            }
                        } else {
                            System.out.println("File not Found");

                            httpResponse = "HTTP/1.0 404 Not Found\r\n" + " No such file";
                            writer.write(httpResponse.getBytes("UTF-8"));
                        }
                    } else {
                        if (new File(requestedResourcePath).exists()) {
                            System.out.println("File Found");
                            httpResponse = "HTTP/1.0 200 OK\r\n\r\n";

                            writer.write(httpResponse.getBytes("UTF-8"));

                            byte[] a = new byte[4096];
                            int n;
                            InputStream inputStream = new FileInputStream(requestedResourcePath);
                            while ((n = inputStream.read(a)) > 0) {
                                writer.write(a, 0, n);
                            }
                        } else {
                            httpResponse = "HTTP/1.0 404 Not Found\r\n" + " No such file";
                            writer.write(httpResponse.getBytes("UTF-8"));
                        }
                    }

                    writer.close();
                }
                line = reader.readLine();
            }
        } catch (Exception e) {

        }

        System.out.println();
    }
}
