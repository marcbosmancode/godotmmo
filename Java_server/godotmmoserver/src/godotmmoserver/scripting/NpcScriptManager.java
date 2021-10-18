package godotmmoserver.scripting;

import java.io.IOException;

/**
 *
 * @author marcb
 */
public class NpcScriptManager {
    private PythonScriptManager psm;
    
    public NpcScriptManager(PythonScriptManager psm){
        this.psm = psm;
    }
    public void sendNpcOk(String message) throws IOException{
        psm.getClientHandler().getPacketHandler().sendNpcOk(message);
    }
}
