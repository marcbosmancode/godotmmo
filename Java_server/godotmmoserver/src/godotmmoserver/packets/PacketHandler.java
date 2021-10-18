package godotmmoserver.packets;

import godotmmoserver.client.ClientHandler;
import godotmmoserver.models.GameCharacter;
import godotmmoserver.models.GameItem;
import godotmmoserver.models.ModelController;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 *
 * @author marcb
 */
public class PacketHandler {
    private ModelController modelManager;
    private PacketCreator packetCreator;
    private ByteArrayReader bar;
    private ClientHandler ch;
    
    public PacketHandler(ClientHandler ch){
        modelManager = ModelController.getInstance();
        packetCreator = new PacketCreator();
        bar = new ByteArrayReader();
        this.ch = ch;
    }
    
    //for sending the handshake packet to rejected clients
    public PacketHandler(){
        modelManager = ModelController.getInstance();
    }
    
    //Handshake packet to tell if the connection is successfully made
    public void sendHandshake(DataOutputStream out, boolean result) throws IOException{
        out.write(packetCreator.getHandshakePacket(result));
    }
    
    //Send a packet to everyone in the current room
    public void broadcastRoom(byte[] packet) throws IOException{
        for(ClientHandler clientHandler : ch.getGameCharacter().getCurrentRoom().getAllClients()){
            if(!clientHandler.getSocket().isClosed()){
                clientHandler.getSocket().getOutputStream().write(packet);
            }
        }
    }
    
    //Send a packet to everyone in the current room
    public void broadcastOthersInRoom(byte[] packet) throws IOException{
        for(ClientHandler clientHandler : ch.getGameCharacter().getCurrentRoom().getAllClients()){
            if(!clientHandler.equals(ch)){
                if(!clientHandler.getSocket().isClosed()){
                    clientHandler.getSocket().getOutputStream().write(packet);
                }
            }
        }
    }

    public void handlePacket(int packetLength, DataInputStream in, DataOutputStream out) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException{
        short command = bar.readLEShort(in);
        //String command = bar.readNullTerminatedString(packetLength, in);
        System.out.println("Received a packet with command: " + command);
        
        //handling the received packet based on the command
        switch(command){
            case 1: handleDebug(packetLength, in, out); break;
            case 2: handleRegister(packetLength, in, out); break;
            case 3: handleLogin(packetLength, in, out); break;
            case 4: handleLogout(); break;
            case 5: handlePosition(in, out); break;
            case 6: handleCharacterState(packetLength, in, out); break;
            case 7: handleChat(packetLength, in, out); break;
            case 8: handleChangeMap(packetLength, in, out); break;
            case 9: handleNpcChat(packetLength, in, out); break;
            case 10: handleAlterInventory(packetLength, in, out); break;
            case 11: handleLootItem(packetLength, in, out); break;
            case 12: handleIncreaseStat(packetLength, in, out); break;
            default: handleUnknown(command, packetLength, in);
        }
    }
    
    public void handleDebug(int packetLength, DataInputStream in, DataOutputStream out) throws IOException{
        String message = bar.readNullTerminatedString(packetLength, in);
        System.out.println("got a debug packet with message: " + message);
        out.write(packetCreator.getDebugPacket(message));
    }
    
    public void handleRegister(int packetLength, DataInputStream in, DataOutputStream out) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException{
        String username = bar.readNullTerminatedString(packetLength, in);
        String password = bar.readNullTerminatedString(packetLength, in);
        boolean result = modelManager.registerUser(username, password);
        
        out.write(packetCreator.getRegisterPacket(result));
    }
    
    public void handleLogin(int packetLength, DataInputStream in, DataOutputStream out) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException{
        String username = bar.readNullTerminatedString(packetLength, in);
        String password = bar.readNullTerminatedString(packetLength, in);
        boolean result = modelManager.AuthenticateUser(username, password);
        if(result){
            ch.setGameCharacter(modelManager.getUser(username));
            //if the character fails to load the login should also fail
            if(ch.getGameCharacter() == null){
                result = false;
            //and check if the character isn't already logged in
            } else if(ch.getGameCharacter().isOnline()){
                result = false;
            }
        }
        
        //make a packet with the login result
        
        //when a login fails send the packet telling the client
        if(!result){
            out.write(packetCreator.getFailedLoginPacket());
            return;
        }
        
        //else add the character data
        GameCharacter gc = ch.getGameCharacter();
        //move character to the starting position of the map
        gc.resetPosition();
        
        out.write(packetCreator.getSuccessfulLoginPacket(gc));
        
        //also get the players inventory data
        short currentPosition = 0;
        for(GameItem gi : gc.getInventory().getAllItems()){
            if(gi != null){
                //send the item to the player
                out.write(packetCreator.getInventoryChangePacket(currentPosition, gi.getId(), gi.getQuantity()));
            }
            currentPosition++;
        }
        
        //get all the characters currently in the map already and send them to the player
        for(ClientHandler clientHandler : gc.getCurrentRoom().getAllClients()){
            GameCharacter otherCharacter = clientHandler.getGameCharacter();
            
            out.write(packetCreator.getEntermapPacket(otherCharacter));
        }
        
        //and notify others in the map the player logged in
        broadcastRoom(packetCreator.getEntermapPacket(gc));
        
        //finally add our player to the map and toggle the login status
        gc.getCurrentRoom().addClient(ch);
        gc.setOnline(true);
    }
    
    public void handleLogout() throws IOException{
        //if not logged in on any character there is no need to do anything
        GameCharacter gc = ch.getGameCharacter();
        if(gc == null) return;
        
        //notify all players that we are leaving the map
        broadcastOthersInRoom(packetCreator.getLeavemapPacket(gc));
        
        //remove the client from the current map and logout
        gc.getCurrentRoom().removeClient(ch);
        modelManager.saveUser(ch.getGameCharacter());
        gc.setOnline(false);
        
        //remove reference to the game character from this handler
        ch.setGameCharacter(null);
    }
    
    public void handlePosition(DataInputStream in, DataOutputStream out) throws IOException{
        int pos_x = bar.readLEInt(in);
        int pos_y = bar.readLEInt(in);
        
        //update the position of the character
        GameCharacter gc = ch.getGameCharacter();
        if(gc == null) return;
        gc.setPosX(pos_x);
        gc.setPosY(pos_y);
        
        //send the updated position to others in the same room
        broadcastOthersInRoom(packetCreator.getUpdatePositionPacket(gc));
    }
    
    public void handleCharacterState(int packetLength, DataInputStream in, DataOutputStream out) throws IOException{
        short sprite_direction = bar.readLEShort(in);
        short new_state = bar.readLEShort(in);
        System.out.println("Got a state packet with facing_direciton: " + sprite_direction);
        
        if(ch.getGameCharacter() == null) return;
        //send the new sprite to other players in the same map
        broadcastOthersInRoom(packetCreator.getUpdateCharacterStatePacket(ch.getGameCharacter(), sprite_direction, new_state));
    }
    
    public void handleChat(int packetLength, DataInputStream in, DataOutputStream out) throws IOException{
        short chat_group = bar.readLEShort(in);
        String message = bar.readNullTerminatedString(packetLength, in);
        
        //no need to send the message if the character isn't loaded yet
        if(ch.getGameCharacter() == null) return;
        
        //send the chat message to other players in the same map
        broadcastOthersInRoom(packetCreator.getChatPacket(chat_group, ch.getGameCharacter(), message));
    }
    
    public void handleChangeMap(int packetLength, DataInputStream in, DataOutputStream out) throws IOException{
        String newMap = bar.readNullTerminatedString(packetLength, in);
        short target_x = bar.readLEShort(in);
        short target_y = bar.readLEShort(in);
        GameCharacter gc = ch.getGameCharacter();
        
        if(ch.getGameCharacter() == null) return;
        
        //notify players in our map we're leaving
        broadcastOthersInRoom(packetCreator.getLeavemapPacket(gc));
        
        //get all the characters currently in the map already and send them to the player
        for(ClientHandler clientHandler : modelManager.getMap(newMap).getAllClients()){
            GameCharacter otherCharacter = clientHandler.getGameCharacter();
            
            //create an entermap packet
            out.write(packetCreator.getEntermapPacket(otherCharacter));
        }
        
        //and leave the map
        gc.getCurrentRoom().removeClient(ch);
        
        //join the new map and update position
        gc.setCurrentRoom(modelManager.getMap(newMap));
        gc.setPosX(target_x);
        gc.setPosY(target_y);
        gc.getCurrentRoom().addClient(ch);
        
        //notify players in the new map we've entered
        broadcastOthersInRoom(packetCreator.getEntermapPacket(gc));
        
        System.out.println("Player: " + gc.getName() + " is now in map: " + gc.getCurrentRoom().getName());
    }
    
    public void handleNpcChat(int packetLength, DataInputStream in, DataOutputStream out) throws IOException{
        String npc = bar.readNullTerminatedString(packetLength, in);
        
        ch.getPythonScriptHandler().executeNpcScript(npc);
    }
    
    public void handleAlterInventory(int packetLength, DataInputStream in, DataOutputStream out) throws IOException{
        short inventorySlot = bar.readLEShort(in);
        short inventorySlot2 = bar.readLEShort(in);
        System.out.println(inventorySlot + " moving to " + inventorySlot2);
        
        //the item that is being moved
        GameItem gi = ch.getGameCharacter().getInventory().getItem(inventorySlot);
        if(gi == null) return; //if slot is empty don't do anything
        
        //if inventory slot 2 is -1 the item is being dropped
        if(inventorySlot2 == -1){
            //remove the item from the inventory and add it as drop to the current room
            ch.getGameCharacter().getInventory().updateSlot(inventorySlot, null);
            ch.getGameCharacter().getCurrentRoom().addItem(gi);
            
            //and notify the player the inventory slot is now empty
            out.write(packetCreator.getInventoryChangePacket((short)inventorySlot, (short)-1, 0));
            
            //create the item drop packet and send it to everyone in the map
            broadcastRoom(packetCreator.getItemDropPacket(gi, ch.getGameCharacter().getPosX(), ch.getGameCharacter().getPosY()));
            
            return;
        }
        
        //switch the items from the 2 slots around
        GameItem gi2 = ch.getGameCharacter().getInventory().getItem(inventorySlot2);
        if(gi2 != null){
            ch.getGameCharacter().getInventory().updateSlot(inventorySlot, gi2);
            ch.getGameCharacter().getInventory().updateSlot(inventorySlot2, gi);
            
            //and notify the player with the inventory updates
            out.write(packetCreator.getInventoryChangePacket(inventorySlot, gi2.getId(), gi2.getQuantity()));
            out.write(packetCreator.getInventoryChangePacket(inventorySlot2, gi.getId(), gi.getQuantity()));
        } else {
            ch.getGameCharacter().getInventory().updateSlot(inventorySlot, null);
            ch.getGameCharacter().getInventory().updateSlot(inventorySlot2, gi);
            
            //and notify the player with the inventory updates
            out.write(packetCreator.getInventoryChangePacket(inventorySlot, (short)-1, 0));
            out.write(packetCreator.getInventoryChangePacket(inventorySlot2, gi.getId(), gi.getQuantity()));
        }
    }
    
    public void handleLootItem(int packetLength, DataInputStream in, DataOutputStream out) throws IOException{
        short itemId = bar.readLEShort(in);
        
        GameItem gi = ch.getGameCharacter().getCurrentRoom().getItem(itemId);
        int result = ch.getGameCharacter().getInventory().addItem(gi);
        if(result != -1) {
            //send the item to the player
            out.write(packetCreator.getInventoryChangePacket((short)result, gi.getId(), gi.getQuantity()));
            
            //clear the item from the floor
            broadcastRoom(packetCreator.getClearLootPacket(gi.getUniqueId()));
        }
    }
    
    public void handleIncreaseStat(int packetLength, DataInputStream in, DataOutputStream out) throws IOException{
        short stat = bar.readLEShort(in);
        short amount = bar.readLEShort(in);
        
        //update the stat for the character
        ch.getGameCharacter().setMaxHp(ch.getGameCharacter().getMaxHp() + amount);
        
        //send the stat update
        out.write(packetCreator.getUpdateStatPacket((short)stat, ch.getGameCharacter().getMaxHp()));
        
        //also notify that a stat points has been used
        out.write(packetCreator.getUpdateStatPacket((short)stat, ch.getGameCharacter().getAbilityPoints()));
    }
    
    public void handleUnknown(short command, int packetLength, DataInputStream in) throws IOException{
        //skip the remaining bytes in the packet, packetLength is indicated in first byte, command(short) is 2 bytes
        in.skipBytes(packetLength - 3);
        System.out.println("Unknown request, skipped packet");
    }
    
    public void sendNpcOk(String message) throws IOException{
        //send ok chat popup to the client
        ch.getSocket().getOutputStream().write(packetCreator.getNpcOkPacket(message));
    }
}
