package godotmmoserver.packets;

import godotmmoserver.enums.PacketIds;
import godotmmoserver.models.GameCharacter;
import godotmmoserver.models.GameItem;
import java.io.IOException;

/**
 *
 * @author marcb
 */
public class PacketCreator {
    private ByteArrayReader bar;
    private ByteArrayWriter baw;
    
    public PacketCreator(){
        bar = new ByteArrayReader();
        baw = new ByteArrayWriter();
    }
    
    public byte[] getDebugPacket(String message) throws IOException{
        baw.writeLEShort(PacketIds.debug.getPacketId());
        baw.writeNullTerminatedString(message);
        return baw.getPacket();
    }
    
    public byte[] getHandshakePacket(boolean result) throws IOException{
        baw.writeLEShort(PacketIds.handshake.getPacketId());
        baw.writeNullTerminatedString(Long.toString(System.currentTimeMillis())); //current server time
        baw.writeByte((byte)(result ? 1 : 0 )); //boolean for being accepted online
        return baw.getPacket();
    }
    
    public byte[] getRegisterPacket(boolean result) throws IOException{
        baw.writeLEShort(PacketIds.register.getPacketId());
        baw.writeByte((byte)(result ? 1 : 0 ));
        return baw.getPacket();
    }
    
    //Failed login packet doesn't include character details
    public byte[] getFailedLoginPacket() throws IOException{
        baw.writeLEShort(PacketIds.login.getPacketId());
        baw.writeByte((byte)(false ? 1 : 0 ));
        return baw.getPacket();
    }
    
    public byte[] getSuccessfulLoginPacket(GameCharacter gc) throws IOException{
        baw.writeLEShort(PacketIds.login.getPacketId());
        baw.writeByte((byte)(true ? 1 : 0 ));
        baw.writeNullTerminatedString(gc.getName());
        baw.writeNullTerminatedString(gc.getCurrentRoom().getName());
        baw.writeLEInt(gc.getPosX());
        baw.writeLEInt(gc.getPosY());
        baw.writeLEInt(gc.getLevel());
        baw.writeLEInt(gc.getExperience());
        return baw.getPacket();
    }
    
    public byte[] getUpdatePositionPacket(GameCharacter gc) throws IOException{
        baw.writeLEShort(PacketIds.position.getPacketId());
        baw.writeNullTerminatedString(gc.getName());
        baw.writeLEInt(gc.getPosX());
        baw.writeLEInt(gc.getPosY());
        return baw.getPacket();
    }
    
    public byte[] getUpdateCharacterStatePacket(GameCharacter gc, short facingDirection, short newState) throws IOException{
        baw.writeLEShort(PacketIds.characterstate.getPacketId());
        baw.writeNullTerminatedString(gc.getName());
        baw.writeLEShort(facingDirection);
        baw.writeLEShort(newState);
        return baw.getPacket();
    }
    
    public byte[] getEntermapPacket(GameCharacter gc) throws IOException{
        baw.writeLEShort(PacketIds.entermap.getPacketId());
        baw.writeNullTerminatedString(gc.getName());
        baw.writeLEInt(gc.getPosX());
        baw.writeLEInt(gc.getPosY());
        return baw.getPacket();
    }
    
    public byte[] getLeavemapPacket(GameCharacter gc) throws IOException{
        baw.writeLEShort(PacketIds.leavemap.getPacketId());
        baw.writeNullTerminatedString(gc.getName());
        return baw.getPacket();
    }
    
    public byte[] getInventoryChangePacket(short inventoryPosition, short itemId, int quantity) throws IOException{
        baw.writeLEShort(PacketIds.inventorychange.getPacketId());
        baw.writeLEShort(inventoryPosition);
        baw.writeLEShort(itemId);
        baw.writeLEInt(quantity);
        return baw.getPacket();
    }
    
    public byte[] getItemDropPacket(GameItem gi, int posX, int posY) throws IOException{
        baw.writeLEShort(PacketIds.itemdrop.getPacketId());
        baw.writeLEShort(gi.getId());
        baw.writeLEInt(gi.getUniqueId());
        baw.writeLEShort(gi.getQuantity());
        baw.writeLEInt(posX);
        baw.writeLEInt(posY);
        return baw.getPacket();
    }
    
    public byte[] getClearLootPacket(int uniqueItemId) throws IOException{
        baw.writeLEShort(PacketIds.clearloot.getPacketId());
        baw.writeLEInt(uniqueItemId);
        return baw.getPacket();
    }
    
    public byte[] getChatPacket(short chatType, GameCharacter gc, String message) throws IOException{
        baw.writeLEShort(PacketIds.chat.getPacketId());
        baw.writeLEShort(chatType);
        baw.writeNullTerminatedString(gc.getName());
        baw.writeNullTerminatedString(message);
        return baw.getPacket();
    }
    
    public byte[] getNpcOkPacket(String message) throws IOException{
        baw.writeLEShort(PacketIds.npcok.getPacketId());
        baw.writeNullTerminatedString(message);
        return baw.getPacket();
    }
    
    public byte[] getUpdateStatPacket(short stat, int statValue) throws IOException{
        baw.writeLEShort(PacketIds.updatestat.getPacketId());
        baw.writeLEShort(stat);
        baw.writeLEInt(statValue);
        return baw.getPacket();
    }
}
