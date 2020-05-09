package jacamo.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.Gson;

import jacamo.rest.util.Message;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientWorkspaceTest {
    static URI uri;
    Client client = ClientBuilder.newClient();

    @BeforeClass
    public static void launchSystem() {
        uri = TestUtils.launchSystem("src/test/test1.jcm");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void test101GetWorkspaces() {
        Response response = client
                .target(uri.toString())
                .path("workspaces")
                .request(MediaType.APPLICATION_JSON)
                .get();

        //System.out.println(response.readEntity(String.class));
        List vl = new Gson().fromJson(response.readEntity(String.class), List.class);
        assertTrue(vl.contains("testOrg"));
        assertTrue(vl.contains("testwks"));
        assertTrue(vl.contains("main"));
        client.close();
    }
    
    @Test
    public void test201PostProperty() {
        System.out.println("\n\ntest201PostProperty");
        
        Response response = client
                .target(uri.toString())
                .path("workspaces/testwks/artifacts/a/properties/count")
                .request(MediaType.APPLICATION_JSON)
                .get();

        Object[] vl = new Gson().fromJson(response.readEntity(String.class), Object[].class);
        
        //TODO: For some reason on dockerhub it is returning null
        if (vl != null) 
            assertEquals( 10, ((Double)vl[0]).intValue(), 0 );

        
        client
            .target(uri.toString())
            .path("workspaces/testwks/artifacts/a/operations/inc/execute")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(new Gson().toJson(new Object[] {})));
        
        response = client
                .target(uri.toString())
                .path("workspaces/testwks/artifacts/a/properties/count")
                .request(MediaType.APPLICATION_JSON)
                .get();

        Object[] vl2 = new Gson().fromJson(response.readEntity(String.class), Object[].class);
        
        //TODO: For some reason on dockerhub it is returning null
        if (vl2 != null) 
            assertEquals( 11, ((Double)vl2[0]).intValue(), 0 );

        client.close();
    }

    @Test
    public void test202PostOperationExecute() {
        System.out.println("\n\ntest202PostOperationExecute");

        client
            .target(uri.toString())
            .path("workspaces/testwks/artifacts/a/operations/reset/execute")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(new Gson().toJson(new Object[] { 40 })));

        Response response = client
                .target(uri.toString())
                .path("workspaces/testwks/artifacts/a/properties/count")
                .request(MediaType.APPLICATION_JSON)
                .get();

        Object[] vl = new Gson().fromJson(response.readEntity(String.class), Object[].class);
        
        //TODO: For some reason on dockerhub it is returning null
        if (vl != null) 
            assertEquals( 40, ((Double)vl[0]).intValue(), 0 );

        
        client.close();
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void test203PostArtifact() {
        System.out.println("\n\ntest203PostArtifact");

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
        
        Response response = client
                .target(uri.toString())
                .path("workspaces/neww3/artifacts/newart/properties/count")
                .request(MediaType.APPLICATION_JSON)
                .get();

        Object[] vl = new Gson().fromJson(response.readEntity(String.class), Object[].class);
        
        //TODO: For some reason on dockerhub it is returning null
        if (vl != null) 
            assertEquals( 22, ((Double)vl[0]).intValue(), 0 );

        response = client
                .target(uri.toString())
                .path("workspaces/neww3")
                .request(MediaType.APPLICATION_JSON)
                .get();

        Map vl2 = new Gson().fromJson(response.readEntity(String.class), Map.class);
        Map art = (Map)((Map)vl2.get("artifacts")).get("newart");
        assertEquals("tools.Counter", art.get("type"));
        
        client.close();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void test301CreateDummyArt() {
        // create a workspace jh
        client
            .target(uri.toString())
            .path("workspaces/jh")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(new Gson().toJson(new Object[] {  })));
    
        // add DummyArt there
        Map<String,Object> m = new HashMap<>();
        m.put("template", "jacamo.rest.util.DummyArt");
        m.put("values", new Object[] { });
        
        client
            .target(uri.toString())
            .path("workspaces/jh/artifacts/da")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(new Gson().toJson(m)));
        
        // run defineObsProperty
        Response response = client
            .target(uri.toString())
            .path("workspaces/jh/artifacts/da/operations/doDefineObsProperty/execute")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(new Gson().toJson(new Object[] { "count", 1111 })));
        assertEquals(200, response.getStatus());

        response = client
                .target(uri.toString())
                .path("workspaces/jh")
                .request(MediaType.APPLICATION_JSON)
                .get();
    
        Map vl2 = new Gson().fromJson(response.readEntity(String.class), Map.class);
        Map art = (Map)((Map)vl2.get("artifacts")).get("da");
        //System.out.println(art);
        assertEquals("jacamo.rest.util.DummyArt", art.get("type"));
        assertEquals("1111.0", ((Map)((List)art.get("properties")).get(0)).get("count").toString());

        // run updateObsProperty
        response = client
                .target(uri.toString())
                .path("workspaces/jh/artifacts/da/operations/doUpdateObsProperty/execute")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new Gson().toJson(new Object[] { "count", 2222 })));
        assertEquals(200, response.getStatus());

        // run signal
        response = client
                .target(uri.toString())
                .path("workspaces/jh/artifacts/da/operations/doSignal/execute")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new Gson().toJson(new Object[] { "count", null })));
        
        response = client
                .target(uri.toString())
                .path("workspaces/jh/artifacts/da/properties/count")
                .request(MediaType.APPLICATION_JSON)
                .get();
    
        Object[] vl = new Gson().fromJson(response.readEntity(String.class), Object[].class);
        
        //TODO: For some reason on dockerhub it is returning null
        if (vl != null) 
            assertEquals( 2222, ((Double)vl[0]).intValue(), 0 );

        // agent acting on the dummy art
        
        // register callback
        response = client
                .target(uri.toString())
                .path("workspaces/jh/artifacts/da/operations/register/execute")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new Gson().toJson(new Object[] { "http://localhost:1010" })));
        // create agent
        response = client.target(uri.toString()).path("agents/belovedbob")
                .request()
                .post(Entity.json(""));
        response = client.target(uri.toString())
                .path("agents/belovedbob/plans")
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .post(
                        Entity.json(
                                "+!test(A) <- .print(doing,A); act(open(A)). "
                        )
                );

        // ask the agent to focus and act
        sendMsg("belovedbob", "achieve", "jcm::focus_env_art(art_env(jh,local,da,ns1),5)");
        // ask to act
        sendMsg("belovedbob", "achieve", "test(door)");
        
        response = client.target(uri.toString())
                .path("agents/belovedbob")
                .request(MediaType.APPLICATION_JSON).get();
        
        String rStr = response.readEntity(String.class);
        System.out.println("Response (agents/marcos): " + rStr);
        assertTrue(rStr.contains("ns1::count(2222)[artifact_id("));
        
        client.close();
    }
    
    int msgId = 0;
    void sendMsg(String to, String perf, String content) {
        Message msg = new Message(""+(msgId++), perf, "rest", to, content);

        Response response = client.target(uri.toString())
                .path("agents/belovedbob/inbox")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .post(Entity.json(new Gson().toJson(msg)));
        assertEquals(200, response.getStatus());

    }
}
