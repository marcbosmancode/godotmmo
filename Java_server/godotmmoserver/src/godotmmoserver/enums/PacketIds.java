package godotmmoserver.enums;

/**
 *
 * @author marcb
 */

public enum PacketIds {
    debug((short)1),
    handshake((short)2),
    register((short)3),
    login((short)4),
    position((short)5),
    characterstate((short)6),
    chat((short)7),
    entermap((short)8),
    leavemap((short)9),
    npcok((short)10),
    itemdrop((short)11),
    inventorychange((short)12),
    clearloot((short)13),
    updatestat((short)14);
    
    private short packetId;
    
    PacketIds(short packetId) {
        this.packetId = packetId;
    }
    
    public short getPacketId() {
        return packetId;
    }
}
