package godotmmoserver.models;

import java.util.ArrayList;

/**
 *
 * @author marcb
 */
public class CharacterInventory {
    private GameCharacter gameCharacter;
    private ArrayList<GameItem> allItems;
    
    private ModelController modelManager;
    
    public CharacterInventory(GameCharacter gameCharacter){
        this.gameCharacter = gameCharacter;
        allItems = new ArrayList<>();
        
        modelManager = ModelController.getInstance();
        allItems = modelManager.getInventory(gameCharacter.getId());
    }
    
    public int addItem(GameItem gi){
        int inventorySlot = 0;
        
        for(GameItem item : allItems){
            if(item == null){
                allItems.set(inventorySlot, gi);
                return inventorySlot;
            }
            inventorySlot++;
        }
        
        return -1;
    }

    public ArrayList<GameItem> getAllItems() {
        return allItems;
    }

    public void setAllItems(ArrayList<GameItem> allItems) {
        this.allItems = allItems;
    }
    
    public GameItem getItem(int inventorySlot) {
        return allItems.get(inventorySlot);
    }
    
    public void updateSlot(int inventorySlot, GameItem gameItem) {
        allItems.set(inventorySlot, gameItem);
    }
}
