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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.junit.Test;

import com.google.gson.Gson;

import jacamo.rest.util.Message;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientTest {
    static URI uri;

    @BeforeClass
    public static void launchSystem() {
        uri = TestUtils.launchSystem("src/test/test1.jcm");
    }

    @AfterClass
    public static void stopSystem() {
        TestUtils.stopSystem();
    }   
    
    
    @Test(timeout=2000)
    public void test001GetAgents() {
        System.out.println("\n\ntest001GetAgents");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;
        
        // Testing ok from agents/
        response = client.target(uri.toString()).path("agents/")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/): " + rStr);
        assertTrue(rStr.contains("marcos"));
    }
    
    @Test(timeout=2000)
    public void test002GetAgent() {
        System.out.println("\n\ntest002GetAgent");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        // Testing ok agents/marcos
        response = client.target(uri.toString()).path("agents/marcos")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/marcos): " + rStr);
        assertTrue(rStr.contains("price(banana,X)[source(self)]"));

        // Testing 500 agents/marcos2 - (marcos2 does not exist)
        response = client.target(uri.toString()).path("agents/marcos2")
                .request(MediaType.APPLICATION_JSON).get();
        System.out.println("Response (agents/marcos2): should be 500");
        assertEquals(500, response.getStatus());
    }
     
    @Test(timeout=2000)
    public void test003GetAgentStatus() {
        System.out.println("\n\ntest003GetAgentStatus");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        
        // Testing ok agents/marcos/status
        response = client.target(uri.toString()).path("agents/marcos/status")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/marcos/status): " + rStr);
        assertTrue(rStr.contains("intentions"));

        // Testing 500 agents/marcos2/status - (marcos2 does not exist)
        response = client.target(uri.toString()).path("agents/marcos2/status")
                .request(MediaType.APPLICATION_JSON).get();
        System.out.println("Response (agents/marcos2/status): should be 500");
        assertEquals(500, response.getStatus());
    }
    
    @Test(timeout=2000)
    public void test004GetAgentLog() {
        System.out.println("\n\ntest004GetAgentLog");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        Form form = new Form();
        form.param("c", "+raining");
        Entity<Form> entity = Entity.form(form);
        
        //Send a command to write something on marcos's log
        client = ClientBuilder.newClient();
        response = client.target(uri.toString())
                .path("agents/marcos/command")
                .request()
                .post(entity);

        // Testing ok agents/marcos/log
        response = client.target(uri.toString()).path("agents/marcos/log")
                .request(MediaType.TEXT_PLAIN).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/marcos/log): " + rStr);
        assertTrue(rStr.contains("Command +raining"));

        // Testing 500 agents/marcos2/log - (marcos2 does not exist)
        response = client.target(uri.toString()).path("agents/marcos2/log")
                .request(MediaType.TEXT_PLAIN).get();
        System.out.println("Response (agents/marcos2/log): should be 500");
        assertEquals(500, response.getStatus());

    }
    
    @Test(timeout=2000)
    public void test005GetAgentBeliefs() {
        System.out.println("\n\ntest005GetAgentBeliefs");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        client = ClientBuilder.newClient();
        response = client.target(uri.toString()).path("agents/marcos")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/marcos): " + rStr);
        assertTrue(rStr.contains("price(banana,X)[source(self)]"));
    }

    @Test(timeout=2000)
    public void test006PostAgentInbox() {
        System.out.println("\n\ntest006PostAgentInbox");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        client = ClientBuilder.newClient();

        Message m = new Message("34", "tell", "jomi", "marcos", "vl(10)");
        Gson gson = new Gson();

        response = client.target(uri.toString()).path("agents/marcos/inbox")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .post(Entity.json(gson.toJson(m)));
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/marcos/inbox): " + rStr);
        assertEquals(200, response.getStatus());

        response = client.target(uri.toString()).path("agents/marcos")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/marcos): " + rStr);
        assertTrue(rStr.contains("vl(10)[source(jomi)]"));    
    }
    
    @SuppressWarnings("unchecked")
    @Test(timeout=2000)
    public void test007PostAgentPlan() {
        System.out.println("\n\ntest007PostAgentPlan");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        // 1. add plan
        client = ClientBuilder.newClient();
        response = client.target(uri.toString())
                .path("agents/marcos/plans")
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .post(
                        Entity.json(
                                "+!gg(X) : (X > 10) <- +bb1(X); .print(\"*****\",X). " +
                                "+!gg(X) : (X <= 10) <- +bb2(X); .print(\"!!!!!\",X).")
                );
        System.out.println("Post a new plan to marcos");
        assertEquals(200, response.getStatus());
        
        // 2. run plan
        response = client.target(uri.toString()).path("agents/marcos/plans")
                .request(MediaType.APPLICATION_JSON).get();
        Map<String,String> m = response.readEntity(Map.class); 
        Iterator<String> i = m.values().iterator();
        String p = "";
        while (i.hasNext()) {
            p = i.next();
            if (p.contains(".print(\"*****\",X).")) {
                System.out.println("A response (agents/marcos/plans): " + p);
                break;
            }
        }
        assertTrue(p.contains("+bb1(X); .print(\"*****\",X)."));    

        // 3. run plan
        response = client.target(uri.toString())
                .path("agents/marcos/inbox")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .post(Entity.json(new Gson().toJson(
                        new Message("39", "achieve", "jomi", "marcos", "gg(13)"))));
        
        // 4. test
        response = client.target(uri.toString())
                .path("agents/marcos")
                .request(MediaType.APPLICATION_JSON).get();
        
        rStr = response.readEntity(String.class);
        System.out.println("Response (agents/marcos): " + rStr);
        assertTrue(rStr.contains("bb1(13)[source(self)]"));    
    }
    
    @Test(timeout=2000)
    public void test008PostAgentService() {
        System.out.println("\n\ntest008PostAgentService");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        client = ClientBuilder.newClient();

        // empty body
        response = client.target(uri.toString()).path("agents/marcos/services/consulting")
                .request()
                .post(null);
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/marcos/services/consulting): " + rStr);
        assertEquals(200, response.getStatus());
    }
    
    @Test(timeout=2000)
    public void test009GetAgentServices() {
        System.out.println("\n\ntest009GetAgentServices");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        client = ClientBuilder.newClient();
        response = client.target(uri.toString()).path("services")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (services): " + rStr);
        assertTrue(rStr.contains("consulting"));
        
        // with body
        response = client.target(uri.toString()).path("agents/marcos/services/gardening")
                .request()
                .post(Entity.json("{\"service\":\"gardening(vegetables)\",\"type\":\"hand services\"}"));
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/marcos/services/gardening): " + rStr);
        assertEquals(200, response.getStatus());

        response = client.target(uri.toString()).path("services")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (services): " + rStr);
        assertTrue(rStr.contains("gardening"));
    }
    
    @Test(timeout=2000)
    public void test010DeleteAgent() {
        System.out.println("\n\ntest010DeleteAgent");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        client = ClientBuilder.newClient();

        // empty body
        response = client.target(uri.toString()).path("agents/kk/")
                .request()
                .delete();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/kk/): " + rStr);
        assertEquals(200, response.getStatus());
    }
    
    @Test(timeout=2000)
    public void test011PostAgentCommand() {
        System.out.println("\n\ntest011PostAgentCommand");

        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;

        Form form = new Form();
        form.param("c", ".print(oi); +xyz979898;");
        Entity<Form> entity = Entity.form(form);
        
        //Send a command to write something on marcos's log
        client = ClientBuilder.newClient();
        response = client.target(uri.toString())
                .path("agents/marcos/command")
                .request()
                .post(entity);

        // Testing ok agents/marcos/log
        response = client.target(uri.toString()).path("agents/marcos/log")
                .request(MediaType.TEXT_PLAIN).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (agents/marcos/log): " + rStr);
        assertTrue(rStr.contains("Command .print(oi); +xyz979898;"));
    }
    

    
    @Test(timeout=2000)
    public void test201PostProperty() {
        System.out.println("\n\ntest201PostProperty");

        assertEquals( 10, getCount("testwks","a"));

        Client client = ClientBuilder.newClient();
        client
            .target(uri.toString())
            .path("workspaces/testwks/artifacts/a/operations/inc/execute")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(new Gson().toJson(new Object[] {})));
        
        assertEquals( 11, getCount("testwks","a"));
    }

    @Test(timeout=2000)
    public void test202PostOperationExecute() {
        System.out.println("\n\ntest202PostOperationExecute");

        Client client = ClientBuilder.newClient();
        client
            .target(uri.toString())
            .path("workspaces/testwks/artifacts/a/operations/reset/execute")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(new Gson().toJson(new Object[] { 40 })));

        assertEquals( 40, getCount("testwks","a"));
    }

    public int getCount(String w, String art) {
        Client client = ClientBuilder.newClient();
        Response response = client
                .target(uri.toString())
                .path("workspaces/"+w+"/artifacts/"+art+"/properties/count")
                .request(MediaType.APPLICATION_JSON)
                .get();

        Object[] vl = new Gson().fromJson(response.readEntity(String.class), Object[].class);

        //System.out.println("\n\nResponse: " + response.toString() + "\n" + vl[0]);
        
        return ((Double)vl[0]).intValue();
    }
    
    @Test(timeout=2000)
    @SuppressWarnings("rawtypes")
    public void test203PostArtifact() {
        System.out.println("\n\ntest203PostArtifact");

        Client client = ClientBuilder.newClient();

        client
            .target(uri.toString())
            .path("workspaces/neww3")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(new Gson().toJson(new Object[] {  })));

        Map<String,Object> m = new HashMap<>();
        m.put("template", "tools.Counter");
        m.put("values", new Object[] { 22 });
        
        client
            .target(uri.toString())
            .path("workspaces/neww3/artifacts/newart")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(new Gson().toJson(m)));
        assertEquals( 22, getCount("neww3","newart"));

        Response response = client
                .target(uri.toString())
                .path("workspaces/neww3")
                .request(MediaType.APPLICATION_JSON)
                .get();

        Map vl = new Gson().fromJson(response.readEntity(String.class), Map.class);
        Map art = (Map)((Map)vl.get("artifacts")).get("newart");
        //System.out.println("\n\nResponse: " + response.toString() + "\n" + art);
        assertEquals("tools.Counter", art.get("type"));
    }

    @Test(timeout=2000)
    @SuppressWarnings("rawtypes")
    public void test401PostWP() {
        System.out.println("\n\ntest401PostWP");

        Client client = ClientBuilder.newClient();
        Response response = client
            .target(uri.toString())
            .path("wp")
            .request(MediaType.APPLICATION_JSON)
            .get();
        
        Map vl = new Gson().fromJson(response.readEntity(String.class), Map.class);
        System.out.println("\n\nResponse: " + response.toString() + "\n" + vl);
        assertTrue( vl.get("marcos") != null);
        
        Map<String,String> map = new HashMap<>();
        map.put("agentid", "jomi");
        map.put("uri", "http://myhouse");
        //System.out.println("=="+new Gson().toJson(map));
       
        // add new entry
        client
            .target(uri.toString())
            .path("wp")
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_PLAIN)
            .post(Entity.json(new Gson().toJson( map )));

        response = client
                .target(uri.toString())
                .path("wp")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .get();
            
        vl = new Gson().fromJson(response.readEntity(String.class), Map.class);
        System.out.println("\n\nResponse: " + response.toString() + "\n" + vl);
        System.out.println("\n\nResponse: " + response.toString() + "\n" + vl);
    
        assertTrue( vl.get("jomi").equals("http://myhouse"));       
    }
}
