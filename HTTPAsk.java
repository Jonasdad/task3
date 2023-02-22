import java.net.*;
import java.io.*;
import java.util.*;

public class HTTPAsk {
    public static void main(String[] args) throws IOException {
        // Your code here
        ServerSocket server = new ServerSocket(Integer.parseInt(args[0]));

        while (true) {
            try (Socket serverSocket = server.accept()) {
                InputStream in = serverSocket.getInputStream();
                OutputStream out = serverSocket.getOutputStream();
                ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
                int i;
                while ((i = in.read()) != 10) {
                    BAOS.write(i);
                }
                String request = BAOS.toString();
                String[] requestArray = request.split("[ ?=&]");

                // Server only handles GET requests - All others fail.
                String host = findHost(requestArray, "hostname");
                if (!requestArray[0].equals("GET")){
                    System.out.println("Bad request");
                    String errormessage = "Bad request";
                    out.write("HTTP/1.1 400 Bad Request\r\n".getBytes());
                    out.write(("Content-length: " + errormessage.length() + "\r\n").getBytes());
                    out.write("Content-type: text/plain\r\n\r\n".getBytes());
                    out.write(errormessage.getBytes());
                    out.flush();
                    server.close();
                }
                if (!requestArray[1].equals("/ask") || host == null) {
                    System.out.println("Object not found");
                    String errormessage = "Object not found";
                    out.write(("HTTP/1.1 404 Object Not Found\r\n").getBytes());
                    out.write(("Content-length: " + errormessage.length() + "\r\n").getBytes());
                    out.write("Content-type: text/plain\r\n\r\n".getBytes());
                    out.write(errormessage.getBytes());
                    out.flush();
                    server.close();
                }
                

                byte[] toTCPClient = byteArrayToTCPClient(requestArray, "string");

                Boolean shutdown = findShutdown(requestArray, "shutdown");
                
                Integer port = findInt(requestArray, "port");
                

                TCPClient tcpclient = new TCPClient(shutdown, findInt(requestArray, "timeout"),
                        findInt(requestArray, "limit"));

                // String sent = new String(toTCPClient);
               /*  System.out.print("Host: " + host + "\nPort: " + port + "\nShutdown: " + shutdown + "\nTimeout: "
                        + findInt(requestArray, "timeout") + " ms"+ "\nLimit: " + findInt(requestArray, "limit") + " Bytes" +
                        "\nSent to TCPClient: " + sent + "\n");
                */
                String toBrowser = new String(tcpclient.askServer(host, port, toTCPClient));
                System.out.println("Received from TCPClient: " + toBrowser);
                out.write("HTTP/1.1 200 OK\r\n".getBytes());
                out.write(("Content-length: " + toBrowser.length() + "\r\n").getBytes());
                out.write("Content-type: text/plain\r\n\r\n".getBytes());
                out.write(toBrowser.getBytes());
                out.flush();
            }
        }
    }

    public static byte[] byteArrayToTCPClient(String[] array, String key) {
        byte[] empty = new byte[0];
        for (int i = 0; i < array.length; i++) {
            if ((array[i].toLowerCase()).equals(key)) {
                return array[i + 1].getBytes();
            }
        }
        return empty;
    }

    public static Integer findInt(String[] array, String key) {
        for (int i = 0; i < array.length; i++) {
            if ((array[i].toLowerCase()).equals(key)) {
                return Integer.parseInt(array[i + 1]);
            }
        }
        return null;

    }

    public static String findHost(String[] array, String host) {
        for (int i = 0; i < array.length; i++) {
            if ((array[i].toLowerCase()).equals(host)) {
                return array[i + 1];
            }
        }
        return null;
    }

    public static boolean findShutdown(String[] array, String shutdown) {
        for (int i = 0; i < array.length; i++) {
            if ((array[i].toLowerCase()).equals(shutdown)) {
                if (array[i + 1].toLowerCase().equals("true")) {
                    return true;
                }
            }
        }
        return false;
    }
}
