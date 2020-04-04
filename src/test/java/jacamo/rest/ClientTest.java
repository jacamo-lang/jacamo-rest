package jacamo.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import jacamo.infra.JaCaMoLauncher;
import jason.JasonException;

public class ClientTest {
    static URI uri;

    @BeforeClass
    public static void launchSystem() {
        try {
            // Launch jacamo and jacamo-rest running marcos.jcm
            new Thread() {
                public void run() {
                    String[] arg = { "src/test/test0.jcm" };
                    try {
                        JaCaMoLauncher.main(arg);
                    } catch (JasonException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            
            // wait start of jacamo rest
            while (uri == null) {
                System.out.println("waiting jacamo to start ....");
                if (JCMRest.getRestHost() != null)
                    uri = UriBuilder.fromUri(JCMRest.getRestHost()).build();
                else
                    Thread.sleep(400);
            }
            // wait agents
            while (JaCaMoLauncher.getRunner().getNbAgents() == 0) {
                System.out.println("waiting agents to start...");
                Thread.sleep(400);
            }
            Thread.sleep(600);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void stopSystem() {
        JaCaMoLauncher.getRunner().finish();
    }    

    @Test
    public void testPutMessageBeliefBase() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(uri.toString()).path("agents/bob/mind/bb")
                .request(MediaType.APPLICATION_JSON).get();

        String bb = response.readEntity(String.class);

        System.out.println("\n\nResponse: " + response.toString() + "\n" + bb);

        assertTrue(bb.toString().contains("price(banana,45)[source(self)]"));

    }

    @Test
    public void testPutMessageInMailBoxJson() {
        Client client = ClientBuilder.newClient();

        Message m = new Message("34", "tell", "jomi", "bob", "vl(10)");
        Gson gson = new Gson();

        //System.out.println("sending "+Entity.json(gson.toJson(m)));
        // {"performative":"tell","sender":"jomi","receiver":"bob","content":"vl(10)","msgId":"34"}
        
        Response r = client.target(uri.toString()).path("agents/bob/inbox")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .post(Entity.json(gson.toJson(m)));

        System.out.println("Message sent result: " + r);

        assertEquals(200, r.getStatus());

        r = client.target(uri.toString()).path("agents/bob/mind/bb")
                .request(MediaType.APPLICATION_JSON).get();

        String bb = r.readEntity(String.class);

        System.out.println("\n\nResponse: " + r.toString() + "\n" + bb.substring(1, 31));

        assertTrue(bb.toString().contains("vl(10)[source(jomi)]"));    
    }
    
    @Test
    public void testPutPlan() {
        // 1. add plan
        Client client = ClientBuilder.newClient();
        Response r = client.target(uri.toString())
                .path("agents/bob/plans")
                .request()
                .post(
                        Entity.json(
                                "+!gg(X) : X > 10  <- +bb1(X); .print(\"*****\",X). " +
                                "+!gg(X) : X <= 10 <- +bb2(X); .print(\"!!!!!\",X).")
                );
        String bb = r.readEntity(String.class);
        assertEquals(200, r.getStatus());
        
        // 2. run plan
        r = client.target(uri.toString())
                .path("agents/bob/inbox")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .post(Entity.json(new Gson().toJson(
                        new Message("39", "achieve", "jomi", "bob", "gg(13)"))));
        
        // 3. test
        r = client.target(uri.toString())
                .path("agents/bob/mind/bb")
                .request(MediaType.APPLICATION_JSON).get();

        bb = r.readEntity(String.class);

        //System.out.println("\n\nResponse: " + r.toString() + "\n" + bb);

        assertTrue(bb.toString().contains("bb1(13)[source(self)]"));    
    }
}
