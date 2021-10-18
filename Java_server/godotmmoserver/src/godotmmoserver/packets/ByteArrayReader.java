package godotmmoserver.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 *
 * @author marcb
 */
public class ByteArrayReader {
    public String readNullTerminatedString(int packetLength, DataInputStream in) throws IOException{
        ByteArrayOutputStream byteWriter = new ByteArrayOutputStream();
        
        int string_length = readLEInt(in);
        byte data;
        for (int i = 0; i < string_length; i++){
            data = in.readByte();
            if (data == 0) break;
            byteWriter.write(data);
        }
        return new String(byteWriter.toByteArray());
    }
    
    public String readString(int packetLength, DataInputStream in) throws IOException{
        ByteArrayOutputStream byteWriter = new ByteArrayOutputStream();
        
        int stringLength = readLEInt(in);
        byte data;
        for (int i = 0; i < stringLength; i++){
            data = in.readByte();
            byteWriter.write(data);
        }
        return new String(byteWriter.toByteArray());
    }
    
    //GameMaker Studio 2 provides numbers ordered as little endian
    public int readLEInt(DataInputStream in) throws IOException{
        //reverse order from in.readInt()
        return in.read() + (in.read() << 8) + (in.read() << 16) + (in.read() << 24);
    }
    
    public short readLEShort(DataInputStream in) throws IOException{
        //reverse order from in.readShort()
        return (short)(in.read() + (in.read() << 8));
    }
    
    public byte readByte(DataInputStream in) throws IOException{
        return in.readByte();
    }
}
