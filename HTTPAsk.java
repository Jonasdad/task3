import java.net.*;
import java.io.*;
import java.util.*;
//http://localhost:8080/ask?hostname=time.nist.gov&limit=1200&port=13d
//httq://localhost:8080/ask?hostname=java.lab.ssvl.kth.se&shutdown=true&limit=1200&port=13&string=hejhejhejhej
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
                while ((i = in.read()) != 13) {
                    BAOS.write(i);
                }
                //String request = "GET /ask?hostname=time.nist.gov&limit=500&port=13 HTTQ/1.1";
                String request = BAOS.toString();
                String[] requestArray = request.split("[ ?=&]");
                String host = findHost(requestArray, "hostname");
                if (!requestArray[0].equals("GET")){
                    out.write("HTTP/1.1 400 Bad Request\r\n\r\n".getBytes());
                    server.close();
                }
                if (!requestArray[1].equals("/ask") || host == null) {
                    out.write(("HTTP/1.1 404 Object Not Found\r\n\r\n").getBytes());
                    server.close();
                }
                String protocol = getProtocol(requestArray);
                if(!protocol.equals("HTTP/1.1")){
                    System.out.println("True");
                    out.write("HTTP/1.1 400 Bad Request\r\n\r\n".getBytes());
                    server.close();
                }
                else{

                    
                byte[] toTCPClient = byteArrayToTCPClient(requestArray, "string");

                Boolean shutdown = findShutdown(requestArray, "shutdown");
                
                Integer port = findInt(requestArray, "port");

                TCPClient tcpclient = new TCPClient(shutdown, findInt(requestArray, "timeout"),
                        findInt(requestArray, "limit"));

                 System.out.print("Host: " + host + "\nPort: " + port + "\nShutdown: " + shutdown + "\nTimeout: "
                        + findInt(requestArray, "timeout") + " ms"+ "\nLimit: " + findInt(requestArray, "limit") + " Bytes" + "\nProtocol: " + protocol);
                
                        String toBrowser = new String(tcpclient.askServer(host, port, toTCPClient));
                out.write("HTTP/1.1 200 OK\r\n".getBytes());
                out.write(("Content-length: " + toBrowser.length() + "\r\n").getBytes());
                out.write("Content-type: text/plain\r\n\r\n".getBytes());
                out.write(toBrowser.getBytes());
                out.flush();
                }   
            }
        }
    }

    public static String getProtocol(String[] array){
        int i = array.length - 1;
        String returnString = array[i];
        return returnString;

    }

    public static byte[] byteArrayToTCPClient(String[] array, String key) {
        byte[] empty = new byte[0];
        for (int i = 0; i < array.length; i++) {
            if ((array[i].toLowerCase()).equals(key)) {
                String send = array[i+1] + "\n";
                return send.getBytes();
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
