package godotmmoserver.server;

import godotmmoserver.constants.ServerConstants;
import godotmmoserver.models.ModelController;

/**
 *
 * @author marcb
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting up " + ServerConstants.NAME + ", version: " + ServerConstants.VERSION);
        
        //load all the resources for the server
        ModelController.getInstance().loadMaps();
        
        //start the server thread
        Thread serverThread = new Thread(new Server());
        serverThread.start();
    }
}
