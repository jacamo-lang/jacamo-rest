package jacamo.rest.util;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.ObsProperty;
import cartago.OpFeedbackParam;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Term;

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
    
    @OPERATION public void act(String act, OpFeedbackParam<Term> res) {
        if (actionTarger == null) {
            failed("no URL registered for actions!");           
        } else {
            System.out.println(getCurrentOpAgentId().getAgentName()+" ** doing "+act+" at "+actionTarger);
            try {
                Client client = ClientBuilder.newClient();
                Response response = client.target(actionTarger.toString())
                    .request()
                    .post(Entity.json( ASSyntax.parseTerm(act).getAsJSON("") ));                
                String ans = response.readEntity(String.class);
                System.out.println(getCurrentOpAgentId().getAgentName()+" ** answer "+ans+" "+response.getMediaType());
                if (response.getMediaType().toString().equals("text/plain")) {
                	// try to parse answer as a jason term
	                try {
	                    res.set(ASSyntax.parseTerm(ans));
	                } catch (Exception e2) {
	                    res.set(ASSyntax.createString(ans));
	                }
                } else {
                	// store answer as string
                    res.set(ASSyntax.createString(ans));
                }
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
                failed("Error to send "+act+" to "+actionTarger);
            }
        }
    }
}

