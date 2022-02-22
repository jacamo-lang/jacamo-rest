package jacamo.rest.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    protected transient Logger logger  = Logger.getLogger(DummyArt.class.getName());

    protected URL actionTarger = null;
    
    public void init() {
    }

    @OPERATION public void doDefineObsProperty(String obName, Object arg) {
        defineObsProperty(obName, arg);
        logger.log(Level.FINE,"new ob "+obName+"("+arg+")");
    }
    
    @OPERATION public void doUpdateObsProperty(String obName, Object arg) {
        ObsProperty op = getObsProperty(obName);
        if (op == null) {
            defineObsProperty(obName, arg);
        } else {
            op.updateValues(arg);
        }
        logger.log(Level.FINE,"update ob "+obName+"("+arg+")");
    }

    @OPERATION public void doSignal(String signal, Object arg) {
        if (arg == null || arg.toString().equals("null")) {
            signal(signal);
            logger.log(Level.FINE, "signal "+signal);
        } else {
            signal(signal, arg);
            logger.log(Level.FINE, "signal "+signal+"("+arg+")");
        }
    }
    
    @OPERATION public void register(String url) {
        try {
            actionTarger = new URL(url);
            logger.log(Level.FINE, "registered "+actionTarger);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            failed("error creating URL: "+e.getMessage());
        }
    }
    
    @OPERATION public void act(String act, OpFeedbackParam<Term> res) {
        if (actionTarger == null) {
            failed("no URL registered for actions!");           
        } else {
            String nact = act;
            try {
                nact = ASSyntax.parseTerm(act).getAsJsonStr();
            } catch (Exception e) {
                nact = act;
                // ignore parsing error, use string format
                if (!nact.startsWith("\""))
                	nact = "\"" + act+ "\"";
            } 
            logger.log(Level.FINE, getCurrentOpAgentId().getAgentName()+" doing "+act+" at "+actionTarger+" as JSON: "+nact);
            
            try {
            	// send request
                Client client = ClientBuilder.newClient();
                Response response = client.target(actionTarger.toString())
                    .request()
                    .post(Entity.json( nact ));
                
                // process answer
                String ans = response.readEntity(String.class);
                Term   ansj = null;
                if (response.getMediaType().toString().equals("text/plain")) {
                    // try to parse answer as a jason term
                    try {
                    	ansj = ASSyntax.parseTerm(ans);
                        res.set(ansj);
                    } catch (Exception e2) {
                        res.set(ASSyntax.createString(ans));
                    }
                } else {
                    // store answer as string
                	if (ans.startsWith("\"") && ans.endsWith("\"")) 
                		ans = ans.substring(1,ans.length()-1);
                	ansj = ASSyntax.createString(ans);
                    res.set(ansj);
                }
                logger.log(Level.FINE, getCurrentOpAgentId().getAgentName()+" answer "+ans+" "+response.getMediaType()+" as jason "+ansj);
                client.close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "error in act", e); 
                failed("Error to send "+act+" to "+actionTarger);
            }
        }
    }
}

