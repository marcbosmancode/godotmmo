package godotmmoserver.scripting;

import godotmmoserver.client.ClientHandler;
import org.python.util.PythonInterpreter;

/**
 *
 * @author marcb
 */
public class PythonScriptManager {
    private PythonInterpreter py;
    private NpcScriptManager npcScriptManager;
    private ClientHandler ch;
    
    public PythonScriptManager(ClientHandler ch){
        this.ch = ch;
        
        System.out.println("Loading python script interpreter");
        py = new PythonInterpreter();
        
        //set script manager class so the python interpreter can use it
        npcScriptManager = new NpcScriptManager(this);
        py.set("npc", npcScriptManager);
    }
    
    public void executeNpcScript(String file){
        try {
            py.execfile("src/gmsmmoserver/npcscripts/" + file + ".py");
        } catch (Exception e){
            System.out.println("Couldn't load the requested npc: " + file);
        }
    }
    
    public ClientHandler getClientHandler(){
        return ch;
    }
}
