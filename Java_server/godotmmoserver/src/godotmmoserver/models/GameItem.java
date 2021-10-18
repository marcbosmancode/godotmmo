package godotmmoserver.models;

/**
 *
 * @author marcb
 */
public class GameItem {
    //id to indentify what item it is
    private final short id;
    private short quantity;
    
    //unique identifier for the item
    private final int uniqueId;
    
    public GameItem(short id, short quantity, int uniqueId){
        this.id = id;
        this.quantity = quantity;
        this.uniqueId = uniqueId;
    }

    public short getQuantity() {
        return quantity;
    }

    public void setQuantity(short quantity) {
        this.quantity = quantity;
    }
    
    public short getId() {
        return id;
    }
    
    public int getUniqueId() {
        return uniqueId;
    }
}
