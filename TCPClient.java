//package tcpclient;
import java.net.*;
import java.io.*;
import java.util.*;

public class TCPClient {
    Integer limit;
    Integer timeout;
    boolean shutdown;
    
    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
        this.shutdown = shutdown;
        this.timeout = timeout;
        this.limit = limit;
    }
    
    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {
        Socket socket = new Socket(hostname, port);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //Timeout
        if(this.timeout != null){
            socket.setSoTimeout(this.timeout); 
        }
        //Writes data to server
        socket.getOutputStream().write(toServerBytes);
        if(this.shutdown == true){
            socket.shutdownOutput();
        }
        //Receives data from server
        InputStream in = socket.getInputStream();
        //If shutdown argument is true from function call - Close and exit
        byte[] buffer = new byte[1];
        try{
            
            int i;
            while((i = in.read(buffer)) != -1){ //read data until end of stream (-1)
                //Write data from server to a dynamic buffer and then return it as a byte array
                out.write(buffer);
                //if out buffer is the same size as our limit -> break and return
                if(Objects.equals(out.size(), this.limit)){
                    break;
                }
            }
            socket.close();
            return out.toByteArray();
        }
        catch(SocketTimeoutException e){
            socket.close();
            return out.toByteArray();
        }
    }
}