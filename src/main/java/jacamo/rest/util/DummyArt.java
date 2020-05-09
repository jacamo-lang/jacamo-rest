package jacamo.rest.util;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.ObsProperty;

public class DummyArt extends Artifact {
    
    private URL actionTarger = null;
    
    public void init() {
    }

    @OPERATION public void doDefineObsProperty(String obName, Object arg) {
        defineObsProperty(obName, arg);
        //System.out.println("** new ob "+obName+"("+arg+")");
    }
    
    @OPERATION public void doUpdateObsProperty(String obName, Object arg) {
        ObsProperty op = getObsProperty(obName);
        if (op == null) {
            defineObsProperty(obName, arg);
        } else {
            op.updateValues(arg);
        }
        //System.out.println("** update ob "+obName+"("+arg+")");
    }

    @OPERATION public void doSignal(String signal, Object arg) {
        if (arg == null || arg.toString().equals("null")) {
            signal(signal);
            //System.out.println("** signal "+signal);
        } else {
            signal(signal, arg);
            //System.out.println("** signal "+signal+"("+arg+")");
        }
    }
    
    @OPERATION public void register(String url) {
        try {
            actionTarger = new URL(url);
            //System.out.println("** registered "+actionTarger);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            failed("error creating URL: "+e.getMessage());
        }
    }
    
    @OPERATION public void act(Object act) {
        if (actionTarger == null) {
            failed("no URL registered for actions!");           
        } else {
            System.out.println(getCurrentOpAgentId().getAgentName()+" ** doing "+act+" at "+actionTarger);
            try {
                Client client = ClientBuilder.newClient();
                client.target(actionTarger.toString())
                    .request()
                    .post(Entity.json(act.toString()));            
                client.close();
            } catch (Exception e) {
                failed("Error to send "+act+" to "+actionTarger);
                e.printStackTrace();
            }
        }
    }
}

