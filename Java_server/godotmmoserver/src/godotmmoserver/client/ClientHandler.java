package godotmmoserver.client;

import godotmmoserver.models.GameCharacter;
import godotmmoserver.packets.ByteArrayReader;
import godotmmoserver.packets.PacketHandler;
import godotmmoserver.scripting.PythonScriptManager;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 *
 * @author marcb
 */
public class ClientHandler implements Runnable {
    private Socket client = null;
    private boolean running = false;
    private DataOutputStream out;
    private DataInputStream in;
    
    private PacketHandler ph = new PacketHandler(this);
    private PythonScriptManager psh = new PythonScriptManager(this);
    
    private ByteArrayReader bar = new ByteArrayReader();
    
    public ClientHandler(Socket socket){
        client = socket;
    }
    
    private GameCharacter gc;

    @Override
    public void run() {
        try {
            running = true;
            out = new DataOutputStream(client.getOutputStream());
            in = new DataInputStream(client.getInputStream());
            
            //upon connecting send out a packet saying we connected successfully
            ph.sendHandshake(out, true);
            
            while(running){
                //each packet we send starts with an int indicating the length
                int packetLength = bar.readLEInt(in);
                String packetString = ("Received packet, length: " + packetLength);
                //for (int i=0; i < packetLength; i++){
                //    int ii = in.read();
                //    packetString += " " + ii;
                //}
                System.out.println(packetString);
                
                //DataInputStream read() will return -1 if the end is reached indicating the client is closed
                //Reading as an int will result in -16843009
                if(packetLength == -16843009){
                    //Throw exception so client to close the client in the catch expression
                    throw new SocketException("Client closed the connection");
                }
                try{
                    //read the data in the packet and handle it
                    ph.handlePacket(packetLength, in, out);
                } catch(Exception e){
                    e.printStackTrace();
                    System.out.println("Error handling incoming packet");
                }
            }
        } catch (IOException e) {
            try {
                System.out.println("Client closed the connection. Logging out and closing the client handler");
                ph.handleLogout();
                client.close();
            } catch (IOException ex) {
                System.out.println("Error while closing client: ");
                ex.printStackTrace();
            }
        }
    }
    
    public Socket getSocket(){
        return client;
    }

    public GameCharacter getGameCharacter() {
        return gc;
    }

    public void setGameCharacter(GameCharacter gc) {
        this.gc = gc;
    }

    public PacketHandler getPacketHandler() {
        return ph;
    }

    public PythonScriptManager getPythonScriptHandler() {
        return psh;
    }
}
