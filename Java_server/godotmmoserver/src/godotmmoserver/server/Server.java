package godotmmoserver.server;

import godotmmoserver.client.ClientHandler;
import godotmmoserver.constants.ServerConstants;
import godotmmoserver.packets.PacketHandler;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author marcb
 */
public class Server implements Runnable {
    private ServerSocket serverSocket;
    private boolean running = false;
    private Socket client = null;
    
    //packethandler to create handshake packets for rejected connections
    private PacketHandler ph;
    
    //make a thread executor service for the maximum amount of connections
    BlockingQueue<Runnable> queue = new SynchronousQueue<>();
    private final ExecutorService fixedThreadPool = new ThreadPoolExecutor(ServerConstants.MAX_PLAYERS, ServerConstants.MAX_PLAYERS, 0L, TimeUnit.MILLISECONDS, queue);
    
    public void launch() throws IOException {
        serverSocket = new ServerSocket(ServerConstants.PORT);
        ph = new PacketHandler();
        
        //if everything is done loading start listening for clients
        System.out.println("Done loading, server started!");
        running = true;
        
        while(running){
            client = serverSocket.accept();
            try {
                fixedThreadPool.execute(new ClientHandler(client));
                System.out.println("Client connected: " + client.getInetAddress());
            } catch(RejectedExecutionException e){
                System.out.println("Client: " + client.getInetAddress() + " tried to connect but server is full");
                ph.sendHandshake(new DataOutputStream(client.getOutputStream()), false);
                client.close();
            }
        }
    }

    @Override
    public void run() {
        try {
            launch();
        } catch (IOException e) {
            System.out.println("Couldn't start the server, check if port " + ServerConstants.PORT + " is already in use");
        }
    }
}