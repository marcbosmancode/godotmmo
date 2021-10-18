package godotmmoserver.models;

import godotmmoserver.dbconnection.DatabaseConnection;
import godotmmoserver.security.PasswordEncrypter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

/**
 *
 * @author marcb
 */
public class ModelController {
    private static ModelController instance = null;
    
    private ArrayList<GameCharacter> allUsers;
    private ArrayList<GameRoom> allMaps;
    private DatabaseConnection database;
    private PasswordEncrypter passwordEncrypter;
    
    private int itemId;
    
    private final int inventorySize = 24;
    
    public static ModelController getInstance(){
        if(instance == null){
            instance = new ModelController();
        }
        return instance;
    }
    
    public ModelController(){
        allUsers = new ArrayList<>();
        allMaps = new ArrayList<>();
        database = new DatabaseConnection();
        passwordEncrypter = new PasswordEncrypter();
        
        itemId = 0;
    }
    
    public boolean registerUser(String username, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
        //check if user already exists on the server
        for(GameCharacter gc : allUsers){
            if(gc.getName().equals(username)) return false;
        }
        
        //if user doesn't exist on server check the database
        Connection c = database.getConnection();
        if(c != null){
            //try-with the connection so it will be closed automaticly
            try (c){
                PreparedStatement ps = c.prepareStatement("SELECT * FROM accounts WHERE username=?");
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                
                //if user exists in the database stop the register progress
                if(rs.next()){
                    return false;
                }
                
                //encrypt the users password
                byte[] salt = passwordEncrypter.generateSalt();
                byte[] encryptedPassword = passwordEncrypter.encryptPassword(password, salt);

                //create the account in the database
                ps = c.prepareStatement("INSERT INTO accounts(username, password, salt, banned) VALUES(?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, username);
                ps.setBytes(2, encryptedPassword);
                ps.setBytes(3, salt);
                ps.setBoolean(4, false);
                
                int result = ps.executeUpdate();
                if(result == 1) System.out.println("Added the account(" + username + ") to the database");
                
                //get the generated id from the account
                rs = ps.getGeneratedKeys();
                
                int accountId = -1;
                if(rs.next()){
                    accountId = rs.getInt(1);
                }
                
                //create a template character for the account and save the variables to the database
                GameCharacter gc = new GameCharacter("template", -1, getMap("rmMap1"));
                
                ps = c.prepareStatement("INSERT INTO characters(accountId, name, map, level, experience, hp, maxHp, mp, maxMp, abilityPoints, skillPoints, strength, dexterity, intelligence) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, accountId);
                ps.setString(2, username);
                ps.setInt(3, gc.getCurrentRoom().getId());
                ps.setInt(4, gc.getLevel());
                ps.setInt(5, gc.getExperience());
                ps.setInt(6, gc.getHp());
                ps.setInt(7, gc.getMaxHp());
                ps.setInt(8, gc.getMp());
                ps.setInt(9, gc.getMaxMp());
                ps.setInt(10, gc.getAbilityPoints());
                ps.setInt(11, gc.getSkillPoints());
                ps.setInt(12, gc.getStrength());
                ps.setInt(13, gc.getDexterity());
                ps.setInt(14, gc.getIntelligence());
                
                result = ps.executeUpdate();
                if(result == 1) System.out.println("Added the character(" + username + ") to the database");
                
                //get the generated id from the character
                rs = ps.getGeneratedKeys();
                
                int characterId = -1;
                if(rs.next()){
                    characterId = rs.getInt(1);
                }
                
                //add the character on the server user list
                allUsers.add(new GameCharacter(username, characterId, getMap("rmMap1")));
            } catch (SQLException e) {
                System.out.println("Error creating new user in the database");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    public boolean AuthenticateUser(String username, String password) throws NoSuchAlgorithmException, InvalidKeySpecException{
        Connection c = database.getConnection();
        if(c == null) return false;
        
        boolean result = false;
        try (c){
            PreparedStatement ps = c.prepareStatement("SELECT password, salt FROM accounts WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next()){
                byte[] encryptedPassword = rs.getBytes("password");
                byte[] salt = rs.getBytes("salt");
                result = passwordEncrypter.checkPassword(password, encryptedPassword, salt);
            }
            
            if(result){
                ps = c.prepareStatement("UPDATE accounts SET lastLogin = ? WHERE username = ?");
                ps.setTimestamp(1, Timestamp.from(java.time.Instant.now()));
                ps.setString(2, username);
                int updates = ps.executeUpdate();
                if(updates == 1) System.out.println("Account(" + username + ") successfully authenticated");
            }
            
        } catch (SQLException e) {
            System.out.println("Error loading user from the database");
            e.printStackTrace();
        }
        return result;
    }
    
    public GameCharacter getUser(String username){
        //check if user is already loaded on the server
        for(GameCharacter gc : allUsers){
            if(gc.getName().equals(username)) {
                return gc;
            }
        }
        
        //if user isn't already loaded check the database
        Connection c = database.getConnection();
        if(c == null) return null;
        
        try (c){
            PreparedStatement ps = c.prepareStatement("SELECT * FROM characters WHERE name=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next()){
                GameCharacter gc = new GameCharacter(rs.getString("name"), rs.getInt("id"), getMapById(rs.getInt("map")));
                gc.setLevel(rs.getInt("level"));
                gc.setExperience(rs.getInt("experience"));
                gc.setHp(rs.getInt("hp"));
                gc.setMaxHp(rs.getInt("maxHp"));
                gc.setMp(rs.getInt("mp"));
                gc.setMaxMp(rs.getInt("maxMp"));
                gc.setAbilityPoints(rs.getInt("abilityPoints"));
                gc.setSkillPoints(rs.getInt("skillPoints"));
                gc.setStrength(rs.getInt("strength"));
                gc.setDexterity(rs.getInt("dexterity"));
                gc.setIntelligence(rs.getInt("intelligence"));
                allUsers.add(gc);
                return gc;
            }
        } catch (SQLException e) {
            System.out.println("Error loading user from the database");
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean saveUser(GameCharacter gc){
        //save the user in the database and return if it was successful
        Connection c = database.getConnection();
        if(c == null) return false;
        
        try (c){
            PreparedStatement ps = c.prepareStatement("UPDATE characters SET map=?, level=?, experience=?, hp=?, maxHp=?, mp=?, maxMp=?, abilityPoints=?, skillPoints=?, strength=?, dexterity=?, intelligence=? WHERE id=?");
            ps.setInt(1, gc.getCurrentRoom().getId());
            ps.setInt(2, gc.getLevel());
            ps.setInt(3, gc.getExperience());
            ps.setInt(4, gc.getHp());
            ps.setInt(5, gc.getMaxHp());
            ps.setInt(6, gc.getMp());
            ps.setInt(7, gc.getMaxMp());
            ps.setInt(8, gc.getAbilityPoints());
            ps.setInt(9, gc.getSkillPoints());
            ps.setInt(10, gc.getStrength());
            ps.setInt(11, gc.getDexterity());
            ps.setInt(12, gc.getIntelligence());
            ps.setInt(13, gc.getId());
            int result = ps.executeUpdate();
            if(result != 1) {
                return false;
            }
            
            //save the users inventory to the database
            //delete the existing inventory
            ps = c.prepareStatement("DELETE FROM inventoryitems WHERE characterId=?");
            ps.setInt(1, gc.getId());
            result = ps.executeUpdate();
            
            //and save the current inventory to the database
            //first try to update item ownership to prevent double entries
            int inventoryPosition = 0;
            for(GameItem gi : gc.getInventory().getAllItems()){
                if(gi != null){
                    ps = c.prepareStatement("UPDATE inventoryitems SET characterId=?, position=?, quantity=? WHERE id=?");
                    ps.setInt(1, gc.getId());
                    ps.setInt(2, inventoryPosition);
                    ps.setInt(3, gi.getQuantity());
                    ps.setInt(4, gi.getUniqueId());
                    
                    result = ps.executeUpdate();
                    
                    //if item didn't exist in another players inventory add it to the database
                    if(result == 0) {
                        ps = c.prepareStatement("INSERT INTO inventoryitems(id, characterId, itemId, position, quantity) VALUES(?,?,?,?,?)");
                        ps.setInt(1, gi.getUniqueId());
                        ps.setInt(2, gc.getId());
                        ps.setInt(3, gi.getId());
                        ps.setInt(4, inventoryPosition);
                        ps.setInt(5, gi.getQuantity());

                        result = ps.executeUpdate();
                    }
                }
                inventoryPosition++;
            }
            
            System.out.println("Saved user(" + gc.getName() + ") to the database");
            return true;
        } catch (SQLException e) {
            System.out.println("Error saving user to the database");
            e.printStackTrace();
        }
        //if there was an error or no rows were updated return false
        return false;
    }
    
    public void loadMaps(){
        //get the maps from the database
        Connection c = database.getConnection();
        try (c){
            String query = "SELECT * FROM rooms";
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery(query);
            
            //load the maps from the database and show how many maps are successfully loaded
            int mapCount = 0;
            while(rs.next()){
                GameRoom gr = new GameRoom(rs.getInt("id"), rs.getString("name"), rs.getInt("startX"), rs.getInt("startY"));
                allMaps.add(gr);
                mapCount += 1;
            }
            System.out.println("Loaded " + mapCount + " maps from the database");
        } catch (SQLException e) {
            System.out.println("Error loading maps from the database");
            e.printStackTrace();
        }
    }
    
    public GameRoom getMap(String name){
        for(GameRoom gr : allMaps){
            if(gr.getName().equals(name)){
                return gr;
            }
        }
        return null;
    }
    
    public GameRoom getMapById(int id){
        for(GameRoom gr : allMaps){
            if(gr.getId() == id){
                return gr;
            }
        }
        return null;
    }
    
    public int getUniqueItemId(){
        itemId += 1;
        return itemId;
    }
    
    public ArrayList<GameItem> getInventory(int characterId){
        ArrayList<GameItem> inventoryItems = new ArrayList<>();
        
        for(int i = 0; i < inventorySize; i++){
            inventoryItems.add(null);
        }
        
        Connection c = database.getConnection();
        if(c == null) return inventoryItems;
        
        try (c){
            PreparedStatement ps = c.prepareStatement("SELECT * FROM inventoryitems WHERE characterId=?");
            ps.setInt(1, characterId);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()){
                GameItem gi = new GameItem(rs.getShort("itemId"), rs.getShort("quantity"), rs.getInt("id"));
                inventoryItems.set(rs.getInt("position"), gi);
            }
        } catch (SQLException e) {
            System.out.println("Error loading inventory from the database");
            e.printStackTrace();
        }
        return inventoryItems;
    }
}
