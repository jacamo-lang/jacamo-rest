package jacamo.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import jacamo.infra.JaCaMoLauncher;
import jacamo.rest.util.Message;
import jason.JasonException;

public class ClientTest {
    static URI uri;

    @BeforeClass
    public static void launchSystem() {
        uri = TestUtils.launchSystem();
    }

    @AfterClass
    public static void stopSystem() {
        JaCaMoLauncher.getRunner().finish();
    }    

    @Test(timeout=2000)
    public void testGetAgents() {
        System.out.println("\n\ntestGetAgents");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;
        
        // Testing ok from agents/
        response = client.target(uri.toString()).path("agents/")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/): " + rStr);
        assertTrue(rStr.contains("bob"));
    }
    
    @Test(timeout=2000)
    public void testGetAgent() {
        System.out.println("\n\ntestGetAgent");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        // Testing ok agents/bob
        response = client.target(uri.toString()).path("agents/bob")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/bob): " + rStr);
        assertTrue(rStr.contains("price(banana,45)[source(self)]"));

        // Testing 500 agents/bob2 - (bob2 does not exist)
        response = client.target(uri.toString()).path("agents/bob2")
                .request(MediaType.APPLICATION_JSON).get();
        System.out.println("Response (agents/bob2): should be 500");
        assertEquals(500, response.getStatus());
    }
     
    @Test(timeout=2000)
    public void testGetAgentStatus() {
        System.out.println("\n\ntestGetAgentStatus");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        
        // Testing ok agents/bob/status
        response = client.target(uri.toString()).path("agents/bob/status")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/bob/status): " + rStr);
        assertTrue(rStr.contains("intentions"));

        // Testing 500 agents/bob2/status - (bob2 does not exist)
        response = client.target(uri.toString()).path("agents/bob2/status")
                .request(MediaType.APPLICATION_JSON).get();
        System.out.println("Response (agents/bob2/status): should be 500");
        assertEquals(500, response.getStatus());
    }
    
    @Test(timeout=2000)
    public void testGetAgentLog() {
        System.out.println("\n\ntestGetAjgentLog");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        Form form = new Form();
        form.param("c", "+raining");
        Entity<Form> entity = Entity.form(form);
        
        //Send a command to write something on bob's log
        client = ClientBuilder.newClient();
        response = client.target(uri.toString())
                .path("agents/bob/command")
                .request()
                .post(entity);

        // Testing ok agents/bob/log
        response = client.target(uri.toString()).path("agents/bob/log")
                .request(MediaType.TEXT_PLAIN).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/bob/log): " + rStr);
        assertTrue(rStr.contains("Command +raining"));

        // Testing 500 agents/bob2/log - (bob2 does not exist)
        response = client.target(uri.toString()).path("agents/bob2/log")
                .request(MediaType.TEXT_PLAIN).get();
        System.out.println("Response (agents/bob2/log): should be 500");
        assertEquals(500, response.getStatus());

    }
    
    @Test(timeout=2000)
    public void testGetAgentBeliefs() {
        System.out.println("\n\ntestGetAgentBeliefs");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        client = ClientBuilder.newClient();
        response = client.target(uri.toString()).path("agents/bob")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/bob): " + rStr);
        assertTrue(rStr.contains("price(banana,45)[source(self)]"));
    }

    @Test(timeout=2000)
    public void testPostAgentInbox() {
        System.out.println("\n\ntestPostAgentInbox");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        client = ClientBuilder.newClient();

        Message m = new Message("34", "tell", "jomi", "bob", "vl(10)");
        Gson gson = new Gson();

        response = client.target(uri.toString()).path("agents/bob/inbox")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .post(Entity.json(gson.toJson(m)));
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/bob/inbox): " + rStr);
        assertEquals(200, response.getStatus());

        response = client.target(uri.toString()).path("agents/bob")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/bob): " + rStr);
        assertTrue(rStr.contains("vl(10)[source(jomi)]"));    
    }
    
    @Test(timeout=2000)
    public void testPostAgentPlan() {
        System.out.println("\n\ntestPostAgentjjjPlan");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        // 1. add plan
        client = ClientBuilder.newClient();
        response = client.target(uri.toString())
                .path("agents/bob/plans")
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .post(
                        Entity.json(
                                "+!gg(X) : (X > 10) <- +bb1(X); .print(\"*****\",X). " +
                                "+!gg(X) : (X <= 10) <- +bb2(X); .print(\"!!!!!\",X).")
                );
        System.out.println("Post a new plan to bob");
        assertEquals(200, response.getStatus());
        
        // 2. run plan
        response = client.target(uri.toString()).path("agents/bob/plans")
                .request(MediaType.APPLICATION_JSON).get();
        Map<String,String> m = response.readEntity(Map.class); 
        Iterator<String> i = m.values().iterator();
        String p = "";
        while (i.hasNext()) {
            p = i.next();
            if (p.contains(".print(\"*****\",X).")) {
                System.out.println("A response (agents/bob/plans): " + p);
                break;
            }
        }
        assertTrue(p.contains("+bb1(X); .print(\"*****\",X)."));    

        // 3. run plan
        response = client.target(uri.toString())
                .path("agents/bob/inbox")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .post(Entity.json(new Gson().toJson(
                        new Message("39", "achieve", "jomi", "bob", "gg(13)"))));
        
        // 4. test
        response = client.target(uri.toString())
                .path("agents/bob")
                .request(MediaType.APPLICATION_JSON).get();
        
        rStr = response.readEntity(String.class);
        System.out.println("Response (agents/bob): " + rStr);
        assertTrue(rStr.contains("bb1(13)[source(self)]"));    
    }
    
    @Test(timeout=2000)
    public void testPostAgentService() {
        System.out.println("\n\ntestPostAgentService");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        client = ClientBuilder.newClient();

        // empty body
        response = client.target(uri.toString()).path("agents/bob/services/consulting")
                .request()
                .post(null);
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/bob/services/consulting): " + rStr);
        assertEquals(200, response.getStatus());

        response = client.target(uri.toString()).path("services")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (services): " + rStr);
        assertTrue(rStr.contains("consulting"));
        
        // with body
        response = client.target(uri.toString()).path("agents/bob/services/gardening")
                .request()
                .post(Entity.json("{\"service\":\"gardening(vegetables)\",\"type\":\"hand services\"}"));
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/bob/services/gardening): " + rStr);
        assertEquals(200, response.getStatus());

        response = client.target(uri.toString()).path("services")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (services): " + rStr);
        assertTrue(rStr.contains("gardening"));
    }
}
