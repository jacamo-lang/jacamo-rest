package jacamo.rest.config;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;

import com.google.gson.Gson;

import jacamo.rest.JCMRest;
import jason.ReceiverNotFoundException;
import jason.architecture.AgArch;
import jason.asSemantics.Message;

public class RestAgArch extends AgArch {

    CuratorFramework      zkClient = null;
    AsyncCuratorFramework zkAsync = null;

    Client                restClient = null;

    @Override
    public void init() throws Exception {
        //System.out.println("my ag arch init "+getAgName());
        restClient = ClientBuilder.newClient();

        if (JCMRest.getZKHost() != null) {
            zkClient = CuratorFrameworkFactory.newClient(JCMRest.getZKHost(), new ExponentialBackoffRetry(1000, 3));
            zkClient.start();

            // register the agent in ZK
            Map<String,String> md = new HashMap<>();
            md.put("type", "JaCaMo");
            registerWP(zkClient, getAgName(), md, true);            
        }
    }

    public static boolean registerWP(CuratorFramework zkClient, String agName, Map<String,String> md, boolean addInbox) throws Exception {
        // register the agent in ZK
        if (zkClient.checkExists().forPath(JCMRest.JaCaMoZKAgNodeId+"/"+agName) != null) {
            System.err.println("Agent "+agName+" is already registered in zookeeper!");
            return false;
        } else {
            String agAddr = JCMRest.JaCaMoZKAgNodeId+"/"+agName;
            String agUri  = md.getOrDefault("uri", JCMRest.getRestHost()+"agents/"+agName);
            zkClient.create()//.withMode(CreateMode.EPHEMERAL)
                .forPath(agAddr, agUri.getBytes());
            
            // store meta-data
            try {
                if (addInbox)
                    md.put("inbox", agUri+"/inbox");

                zkClient.create()//.withMode(CreateMode.EPHEMERAL)
                    .forPath(agAddr+"/"+JCMRest.JaCaMoZKMDNodeId, new Gson().toJson(md).getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }
    
    public CuratorFramework      getCurator() {
        return zkClient;
    }
    public AsyncCuratorFramework getAsyncCurator() {
        if (zkAsync == null)
            zkAsync  = AsyncCuratorFramework.wrap(zkClient);
        return zkAsync;
    }
    
    @Override
    public void stop() {
        if (zkClient != null) {
            zkClient.close();
            zkClient = null;
        }
        if (restClient != null) {
            restClient.close();
            restClient = null;
        }
    }

    @SuppressWarnings("unchecked")
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
                    try {
                        // try ZK inbox meta data
                        byte[] lmd = zkClient.getData().forPath(JCMRest.JaCaMoZKAgNodeId+"/"+m.getReceiver()+"/"+JCMRest.JaCaMoZKMDNodeId);
                        Map<String,String> md = new Gson().fromJson(new String(lmd), Map.class);
                        adr = md.get("inbox");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                    if (adr == null) {
                        // try ZK agent data
                        byte[] badr = zkClient.getData().forPath(JCMRest.JaCaMoZKAgNodeId+"/"+m.getReceiver());
                        if (badr != null)
                            adr = new String(badr)+"/inbox";
                    }
                }

                // try to send the message by REST API
                if (adr != null) {
                    // do POST
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
                throw e;
            }
        }
    }
}
