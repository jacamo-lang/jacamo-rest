    package jacamo.rest.config;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import jacamo.rest.JCMRest;
import jason.ReceiverNotFoundException;
import jason.architecture.AgArch;
import jason.asSemantics.Message;

public class RestAgArch extends AgArch {

    private static final long serialVersionUID = 1L;

    Client                restClient = null;

    @Override
    public void init() throws Exception {
        restClient = ClientBuilder.newClient();

        if (JCMRest.getJCMRest().isMain() || !getAgName().equals("df")) {
            if (JCMRest.getJCMRest().getAgentMetaData(getAgName()) != null) {
                System.err.println("Agent "+getAgName()+" is already registered in ANS! Registering again.");
            }
            Map<String,Object> metadata = new HashMap<>();
            metadata.putIfAbsent("uri", JCMRest.getJCMRest().getURLForRegister()+"agents/"+getAgName());
            metadata.putIfAbsent("type", "JaCaMoAgent");
            metadata.putIfAbsent("inbox", JCMRest.getJCMRest().getURLForRegister()+"agents/"+getAgName()+"/inbox");

            JCMRest.getJCMRest().registerAgent(getAgName(), metadata);
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
            // try "default" way to deliver the msg
            super.sendMsg(m);
            return;
        } catch (ReceiverNotFoundException e) {
            try {
                String adr = null;

                boolean fromCache = false;
                if (m.getReceiver().startsWith("http")) {
                    adr = m.getReceiver();
                } else {
                    fromCache = JCMRest.getJCMRest().hasMetaDataCache(m.getReceiver());
                    adr = getAgentAddrFromANS(m, adr);
                }

                // try to send the message by REST API
                if (adr != null) {
                    // do POST
                    //System.out.println("sending msg "+m+" by rest to "+adr);
                    if (adr.startsWith("http")) {
                        try {
                            restClient
                                      .target(adr)
                                      .request(MediaType.APPLICATION_XML)
                                      .accept(MediaType.TEXT_PLAIN)
                                      .post(Entity.json( m.getAsJsonStr())); //new Gson().toJson(new jacamo.rest.util.Message(m))));
                        } catch (Exception ee) {
                            if (fromCache) {
                                // try again refreshing cache
                                JCMRest.getJCMRest().clearAgentMetaDataCache(m.getReceiver());
                                adr = getAgentAddrFromANS(m, adr);
                                restClient
                                    .target(adr)
                                    .request(MediaType.APPLICATION_XML)
                                    .accept(MediaType.TEXT_PLAIN)
                                    .post(Entity.json( m.getAsJsonStr())); //new Gson().toJson(new jacamo.rest.util.Message(m))));

                            } else {
                                throw ee;
                            }
                        }
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

    private String getAgentAddrFromANS(Message m, String adr) {
        Map<String,Object> metadata = JCMRest.getJCMRest().getAgentMetaData(m.getReceiver());
        if (metadata != null) {
            if (metadata.get("inbox") != null)
                adr = metadata.get("inbox").toString();

            if (adr == null && metadata.get("uri") != null)
                adr = metadata.get("uri").toString();
        }
        return adr;
    }
}
