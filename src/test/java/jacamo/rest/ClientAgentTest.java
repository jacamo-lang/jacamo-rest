package jacamo.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.Gson;

import jacamo.rest.util.Message;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientAgentTest {
    static URI uri;
    Client client = ClientBuilder.newClient();

    @BeforeClass
    public static void launchSystem() {
        uri = RestTestUtils.launchRestSystem("src/test/test1.jcm");
    }

    @Test
    public void test001GetAgents() {
        System.out.println("\n\ntest001GetAgents");
        Response response;
        String rStr;

        // Testing ok from agents/
        response = client.target(uri.toString()).path("agents/")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString();
        System.out.println("Response (agents/): " + rStr);
        assertTrue(rStr.contains("marcos"));

        client.close();
    }

    @Test
    public void test002GetAgent() {
        System.out.println("\n\ntest002GetAgent");
        Response response;
        String rStr;

        // Testing ok agents/marcos
        response = client.target(uri.toString()).path("agents/marcos")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString();
        System.out.println("Response (agents/marcos): " + rStr);
        assertTrue(rStr.contains("\"agent\": \"marcos\""));

        // Testing 500 agents/marcos2 - (marcos2 does not exist)
        response = client.target(uri.toString()).path("agents/marcos2")
                .request(MediaType.APPLICATION_JSON).get();
        System.out.println("Response (agents/marcos2): should be 500");
        assertEquals(500, response.getStatus());

        client.close();
    }

    @Test
    public void test003GetAgentStatus() {
        System.out.println("\n\ntest003GetAgentStatus");
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

        client.close();
    }

    @Test
    public void test004GetAgentLog() {
        System.out.println("\n\ntest004GetAgentLog");
        Response response;
        String rStr;

        Form form = new Form();
        form.param("c", "+raining");
        Entity<Form> entity = Entity.form(form);

        //Send a command to write something on marcos's log
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
        rStr = response.readEntity(String.class).toString();
        System.out.println("Response (agents/marcos2/log): " + rStr);
        assertTrue(rStr.contains("Log is empty/absent."));

        client.close();
    }

    @Test
    public void test005GetAgentBeliefs() {
        System.out.println("\n\ntest005GetAgentBeliefs");
        Response response;
        String rStr;

        response = client.target(uri.toString()).path("agents/marcos/bb")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString();
        System.out.println("Response (agents/marcos): " + rStr);
        assertTrue(rStr.contains("price(banana,X)[source(self)]"));

        client.close();
    }

    @Test
    public void test006PostAgentInbox() {
        System.out.println("\n\ntest006PostAgentInbox");
        Response response;
        String rStr;

        Message m = new Message("34", "tell", "jomi", "marcos", "vl(10,\"oi\")");
        Gson gson = new Gson();

        String s1 = gson.toJson( m );
        //System.out.println("*1"+s1);
        //System.out.println("*2"+m.getAsJasonMsg().getAsJSON(""));
        //System.out.println("*3"+gson.fromJson(m.getAsJasonMsg().getAsJSON(""),Message.class));
        String s2 = gson.toJson( gson.fromJson(m.getAsJasonMsg().getAsJsonStr(),Message.class));
        assertEquals(s1, s2);

        response = client.target(uri.toString()).path("agents/marcos/inbox")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .post(Entity.json(gson.toJson(m)));
        rStr = response.readEntity(String.class).toString();
        //System.out.println("Response (agents/marcos/inbox): " + rStr);
        assertEquals(200, response.getStatus());

        response = client.target(uri.toString()).path("agents/marcos/bb")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString();
        System.out.println("Response (agents/marcos): " + rStr);
        assertTrue(rStr.contains("vl(10,\\\"oi\\\")[source(jomi)]"));

        client.close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test007PostAgentPlan() {
        System.out.println("\n\ntest007PostAgentPlan");
        Response response;
        String rStr;

        // 1. add plan
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
                .path("agents/marcos/bb")
                .request(MediaType.APPLICATION_JSON).get();

        rStr = response.readEntity(String.class);
        //System.out.println("Response (agents/marcos): " + rStr);
        assertTrue(rStr.contains("bb1(13)[source(self)]"));

        client.close();
    }

    @Test
    public void test008PostAgentService() {
        System.out.println("\n\ntest008PostAgentService");
        Response response;
        String rStr;

        // empty body
        response = client.target(uri.toString()).path("agents/marcos/services/consulting")
                .request()
                .post(null);
        rStr = response.readEntity(String.class).toString();
        System.out.println("Response (agents/marcos/services/consulting): " + rStr);
        assertEquals(201, response.getStatus());

        client.close();
    }

    @Test
    public void test009GetAgentServices() {
        System.out.println("\n\ntest009GetAgentServices");
        Response response;
        String rStr;

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
        assertEquals(201, response.getStatus());

        response = client.target(uri.toString()).path("services")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString();
        System.out.println("Response (services): " + rStr);
        assertTrue(rStr.contains("gardening"));

        client.close();
    }

    @Test
    public void test010CreateAgent() {
        System.out.println("\n\ntest01XCreateAgent");
        Response response;
        String rStr;

        response = client.target(uri.toString()).path("agents/myalice")
                .request()
                .post(Entity.json(""));

        rStr = response.readEntity(String.class).toString();
        assertEquals(201, response.getStatus());

        response = client.target(uri.toString()).path("agents/myalice/status")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString();
        System.out.println("Response (agents/myalice/status): " + rStr);
        assertEquals(200, response.getStatus());
        assertTrue(rStr.contains("cycle"));

        client.close();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void test010bDeleteAgent() {
        System.out.println("\n\ntest010DeleteAgent");
        Response response;

        // create ag
        response = client.target(uri.toString()).path("agents/mybob")
                .request()
                .post(Entity.json(""));

        // empty body
        response = client.target(uri.toString()).path("agents/mybob/")
                .request()
                .delete();
        assertEquals(200, response.getStatus());

        // test if mybob is not in WP
        response = client
                .target(uri.toString())
                .path("/agents")
                .request(MediaType.APPLICATION_JSON)
                .get();

        Map vl = new Gson().fromJson(response.readEntity(String.class), Map.class);
        assertNotNull(vl);
        System.out.println("\n\nResponse: " + response.toString() + "\n" + vl);
        assertNull(vl.get("mybob"));

        client.close();
    }

    @Test
    public void test011PostAgentCommand() {
        System.out.println("\n\ntest011PostAgentCommand");
        Response response;
        String rStr;

        Form form = new Form();
        form.param("c", ".print(oi); +xyz979898;");
        Entity<Form> entity = Entity.form(form);

        //Send a command to write something on marcos's log
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

        client.close();
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void test401PostWP() throws Exception {
        System.out.println("\n\ntest401PostWP");

        // verufy if marcos is there
        Response response = client
            .target(uri.toString())
            .path("/agents")
            .request(MediaType.APPLICATION_JSON)
            .get();

        Map vl = new Gson().fromJson(response.readEntity(String.class), Map.class);
        System.out.println("\n\nResponse: " + response.toString() + "\n" + vl);
        assertTrue( vl.get("marcos") != null);

        // infos for the new agent, special attention for the "uri"
        Map<String,String> map = new HashMap<>();
        map.put("uri", "http://myhouse");
        map.put("type", "Java Agent");
        map.put("kkk", "http://myhouse/mb");
        //System.out.println("=="+new Gson().toJson(map));

        // add new entry (dummy agent named jomi)
        client
            .target(uri.toString())
            .path("/agents/jomi")
            .queryParam("only_wp", "true")
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_PLAIN)
            .post(Entity.json(new Gson().toJson( map )));

        response = client
                .target(uri.toString())
                .path("/agents")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .get();

        vl = new Gson().fromJson(response.readEntity(String.class), Map.class);
        assertNotNull(vl.get("jomi"));
        System.out.println("\n\nResponse: " + response.toString() + "\n" + vl.get("jomi"));

        assertTrue( ((Map)vl.get("jomi")).get("kkk").equals("http://myhouse/mb"));

        response = client.target(uri.toString())
                .path("agents/jomi")
                .request()
                .delete();

        // wait a bit for delete finish
        Thread.sleep(1000);

        response = client
                .target(uri.toString())
                .path("/agents")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .get();
        vl = new Gson().fromJson(response.readEntity(String.class), Map.class);
        System.out.println("\n\nResponse: " + response.toString() + "\n" + vl);
        assertNull(vl.get("jomi"));

        client.close();
    }

}
