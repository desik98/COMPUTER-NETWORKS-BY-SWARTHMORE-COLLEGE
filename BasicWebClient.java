import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class BasicWebClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please Enter the URL:");
        String userInputtedURL = scanner.next();

        String serverURL;
        if (userInputtedURL.endsWith("/")) {
            serverURL = userInputtedURL;
        } else {
            serverURL = userInputtedURL + '/';
        }

        try {
            URL url;
            url = new URL(serverURL);

            Socket socket = new Socket(url.getHost(), url.getDefaultPort());

            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream, true);

            printWriter.print("GET " + url.getFile() + " HTTP/1.0\r\n");
            printWriter.print("\r\n");
            printWriter.flush();

            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;

            boolean responseBodyStarted = false;
            StringBuilder responseBody = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                if (responseBodyStarted) {
                    responseBody.append(line + '\n');
                } else {
                    if (line.isEmpty()) {
                        responseBodyStarted = true;
                    }
                }
            }

            String fileName = url.getFile();
            List<String> slashSeparatedStrings = Arrays.asList(fileName.split("/"));
            String requestedFile;

            if (slashSeparatedStrings.size() != 0) {
                requestedFile = slashSeparatedStrings.get(slashSeparatedStrings.size() - 1);
            } else {
                requestedFile = "";
            }

            String finalFile;
            if (requestedFile.contains(".")) {
                finalFile = requestedFile;
            } else {
                finalFile = "index.html";
            }

            BufferedWriter out = null;
            try {
                FileWriter fstream = new FileWriter(finalFile, false); //true tells to append data.
                out = new BufferedWriter(fstream);
                out.append(responseBody);
            } catch (IOException ioException) {
                System.err.println("Error: " + ioException.getMessage());
            } finally {
                if(out != null) {
                    out.close();
                }
            }
        } catch (MalformedURLException malformedURLException) {
            System.out.println("Wrong URL");
        } catch (UnknownHostException unknownHostException) {
            System.out.println("Problems connecting to the Host");
        } catch (IOException ioException) {
            System.out.println("Problems with the Input");
        }
    }
}
