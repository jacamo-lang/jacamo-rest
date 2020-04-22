package jacamo.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.zookeeper.CreateMode;

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
            if (zkClient.checkExists().forPath(JCMRest.JaCaMoZKAgNodeId+"/"+getAgName()) != null) {
                System.err.println("Agent "+getAgName()+" is already registered in zookeeper!");
            } else {
                zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(JCMRest.JaCaMoZKAgNodeId+"/"+getAgName(), (JCMRest.getRestHost()+"agents/"+getAgName()).getBytes());                
            }
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
                    // try ZK
                    byte[] badr = zkClient.getData().forPath(JCMRest.JaCaMoZKAgNodeId+"/"+m.getReceiver());
                    if (badr != null)
                        adr = new String(badr);
                }

                // try to send the message by REST API
                if (adr != null) {
                    // do POST
                    if (adr.startsWith("http")) {
                        restClient
                                  .target(adr)
                                  .path("mb")
                                  .request(MediaType.APPLICATION_XML)
                                  .accept(MediaType.TEXT_PLAIN)
                                  .post(
                                        //Entity.xml( new jacamo.rest.Message(m)), String.class);
                                        Entity.json( new Gson().toJson(new jacamo.rest.Message(m))));
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
