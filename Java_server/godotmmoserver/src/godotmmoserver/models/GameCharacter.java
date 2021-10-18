package godotmmoserver.models;

/**
 *
 * @author marcb
 */
public class GameCharacter {
    private final String name;
    private final int id;
    
    //ingame position
    private GameRoom currentRoom;
    private int posX;
    private int posY;
    
    private boolean online;
    
    //stats
    private int level;
    private int experience;
    private int hp;
    private int maxHp;
    private int mp;
    private int maxMp;
    private int abilityPoints;
    private int skillPoints;
    private int strength;
    private int dexterity;
    private int intelligence;
    
    private CharacterInventory inventory;
    
    public GameCharacter(String name, int id, GameRoom currentRoom){
        this.name = name;
        this.id = id;
        this.currentRoom = currentRoom;
        
        posX = currentRoom.getStartX();
        posY = currentRoom.getStartY();
        
        level = 1;
        experience = 0;
        hp = 10;
        maxHp = 10;
        mp = 10;
        maxMp = 10;
        abilityPoints = 0;
        skillPoints = 0;
        strength = 1;
        dexterity = 1;
        intelligence = 1;
        
        inventory = new CharacterInventory(this);
        
        online = false;
    }
    
    public String getName(){
        return name;
    }

    public int getId() {
        return id;
    }

    public GameRoom getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(GameRoom currentRoom) {
        this.currentRoom = currentRoom;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }
    
    public boolean isOnline(){
        return online;
    }
    
    public void setOnline(boolean online){
        this.online = online;
    }

    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public int getMaxMp() {
        return maxMp;
    }

    public void setMaxMp(int maxMp) {
        this.maxMp = maxMp;
    }

    public int getAbilityPoints() {
        return abilityPoints;
    }

    public void setAbilityPoints(int abilityPoints) {
        this.abilityPoints = abilityPoints;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getDexterity() {
        return dexterity;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }
    
    public void resetPosition() {
        posX = currentRoom.getStartX();
        posY = currentRoom.getStartY();
    }
    
    public CharacterInventory getInventory() {
        return inventory;
    }
}
