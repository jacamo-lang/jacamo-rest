package jacamo.rest.config;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;

import jacamo.rest.JCMRest;
import jason.ReceiverNotFoundException;
import jason.architecture.AgArch;
import jason.asSemantics.Message;

public class RestAgArch extends AgArch {

    private static final long serialVersionUID = 1L;
    
    Client                restClient = null;

    @Override
    public void init() throws Exception {
        //System.out.println("my ag arch init "+getAgName());
        restClient = ClientBuilder.newClient();

        if (JCMRest.getJCMRest().isMain() || !getAgName().equals("df")) {
            if (JCMRest.getJCMRest().getAgentMetaData(getAgName()) != null) {
                System.err.println("Agent "+getAgName()+" is already registered in ANS! Registering again.");
            } 
            Map<String,Object> md = new HashMap<>();
            md.put("type", "JaCaMoAgent");
            Object agUri = md.computeIfAbsent("uri", k -> JCMRest.getJCMRest().getRestHost()+"agents/"+getAgName());
            md.put("inbox", agUri+"/inbox");
            JCMRest.getJCMRest().registerAgent(getAgName(), md);        
        }
    }

    
    @Override
    public void stop() {
        if (restClient != null) {
            restClient.close();
            restClient = null;
        }
        JCMRest.getJCMRest().deregisterAgent(getAgName());
    }

    @Override
    public void sendMsg(Message m) throws Exception {
        try {
            super.sendMsg(m);
            return;
        } catch (ReceiverNotFoundException e) {
            try {
                String adr = null;

                if (m.getReceiver().startsWith("http")) {
                    adr = m.getReceiver();
                } else {
                    Map<String,Object> metadata = JCMRest.getJCMRest().getAgentMetaData(m.getReceiver());
                    if (metadata != null) {
                        adr = metadata.get("inbox").toString();

                        if (adr == null) 
                            adr = metadata.get("uri").toString();
                    }
                }

                // try to send the message by REST API
                if (adr != null) {
                    // do POST
                    //System.out.println("sending msg "+m+" by rest to "+adr);
                    if (adr.startsWith("http")) {
                        restClient
                                  .target(adr)
                                  .request(MediaType.APPLICATION_XML)
                                  .accept(MediaType.TEXT_PLAIN)
                                  .post(
                                        //Entity.xml( new jacamo.rest.Message(m)), String.class);
                                        Entity.json( new Gson().toJson(new jacamo.rest.util.Message(m))));
                    }
                } else {
                    throw e;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw e;
            }
        }
    }
}
