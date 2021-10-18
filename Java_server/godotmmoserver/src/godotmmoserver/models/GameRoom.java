package godotmmoserver.models;

import godotmmoserver.client.ClientHandler;
import java.util.ArrayList;

/**
 *
 * @author marcb
 */
public class GameRoom {
    private final int id;
    private final String name;
    private final int startX;
    private final int startY;
    
    private ArrayList<ClientHandler> allClients;
    private ArrayList<GameItem> allItems;
    
    public GameRoom(int id, String name, int startX, int startY){
        this.id = id;
        this.name = name;
        this.startX = startX;
        this.startY = startY;
        allClients = new ArrayList<>();
        allItems = new ArrayList<>();
    }

    public int getId() {
        return id;
    }
    
    public ArrayList<ClientHandler> getAllClients(){
        return allClients;
    }
    
    public void addClient(ClientHandler user){
        allClients.add(user);
    }
    
    public void removeClient(ClientHandler clientHandler){
        allClients.remove(clientHandler);
    }
    
    public String getName(){
        return name;
    }
    
    public int getStartX(){
        return startX;
    }
    
    public int getStartY(){
        return startY;
    }

    public ArrayList<GameItem> getAllItems() {
        return allItems;
    }
    
    public void addItem(GameItem item){
        allItems.add(item);
    }
    
    public void removeItem(GameItem item){
        allItems.remove(item);
    }
    
    public void clearItems(){
        allItems.clear();
    }
    
    public GameItem getItem(int uniqueId){
        for(GameItem gi : allItems){
            if(gi.getUniqueId() == uniqueId) return gi;
        }
        return null;
    }
}
