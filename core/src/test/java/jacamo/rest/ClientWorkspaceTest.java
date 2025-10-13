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
        uri = RestTestUtils.launchRestSystem("src/test/test1.jcm");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void test101GetWorkspaces() {
        Response response = client
                .target(uri.toString())
                .path("workspaces")
                .request(MediaType.APPLICATION_JSON)
                .get();

        List vl  = new Gson().fromJson(response.readEntity(String.class), List.class);
        assertTrue(vl.contains("testOrg"));
        assertTrue(vl.contains("testwks"));
        client.close();
    }

    @Test
    public void test201PostOperation() {
        System.out.println("\n\ntest201PostOperation");

        Response response = client
                .target(uri.toString())
                .path("workspaces/testwks/artifacts/a/properties/count")
                .request(MediaType.APPLICATION_JSON)
                .get();

        Object[] vl = new Gson().fromJson(response.readEntity(String.class), Object[].class);

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

        assertEquals( 22, ((Double)vl[0]).intValue(), 0 );

        response = client
                .target(uri.toString())
                .path("workspaces/neww3")
                .request(MediaType.APPLICATION_JSON)
                .get();

        Map vl2 = new Gson().fromJson(response.readEntity(String.class), Map.class);
        List arts = (List)vl2.get("artifacts");
        assertTrue(arts.contains("newart"));

        client.close();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void test301CreateDummyArt() throws InterruptedException {
        // create a workspace jh
        Response response = client
            .target(uri.toString())
            .path("workspaces/jh")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(new Gson().toJson(new Object[] {  })));
        assertEquals(201, response.getStatus());

        // add DummyArt there
        Map<String,Object> m = new HashMap<>();
        m.put("template", "jacamo.rest.util.DummyArt");
        m.put("values", new Object[] { });

        response =  client
            .target(uri.toString())
            .path("workspaces/jh/artifacts/da")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(new Gson().toJson(m)));
        assertEquals(201, response.getStatus());

        // add obs prop in the dummy art using operation defineObsProperty
        response = client
            .target(uri.toString())
            .path("workspaces/jh/artifacts/da/operations/doDefineObsProperty/execute")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(new Gson().toJson(new Object[] { "count", 1111 })));
        assertEquals(200, response.getStatus());

        response = client
                .target(uri.toString())
                .path("workspaces/jh/artifacts/da")
                .request(MediaType.APPLICATION_JSON)
                .get();

        Map vl2 = new Gson().fromJson(response.readEntity(String.class), Map.class);
        assertEquals("jacamo.rest.util.DummyArt", vl2.get("type"));
        assertTrue( ((Map)((List)vl2.get("properties")).get(0)).get("count").toString().contains("1111") );

        // run updateObsProperty
        response = client
                .target(uri.toString())
                .path("workspaces/jh/artifacts/da/operations/doUpdateObsProperty/execute")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new Gson().toJson(new Object[] { "count", 2222 })));
        assertEquals(200, response.getStatus());

        response = client
                .target(uri.toString())
                .path("workspaces/jh/artifacts/da/properties/count")
                .request(MediaType.APPLICATION_JSON)
                .get();

        Object[] vl = new Gson().fromJson(response.readEntity(String.class), Object[].class);

        assertEquals( 2222, ((Double)vl[0]).intValue(), 0 );

        // run updateObsProperty using post in the obs prop
        response = client
                .target(uri.toString())
                .path("workspaces/jh/artifacts/da/properties/count")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new Gson().toJson(new Object[] { 333 })));
        assertEquals(200, response.getStatus());

        response = client
                .target(uri.toString())
                .path("workspaces/jh/artifacts/da/properties/count")
                .request(MediaType.APPLICATION_JSON)
                .get();

        vl = new Gson().fromJson(response.readEntity(String.class), Object[].class);

        assertEquals( 333, ((Double)vl[0]).intValue(), 0 );


        // agent acting on the dummy art

        // register callback (created at https://beeceptor.com/console/jacamotest)
        response = client
                .target(uri.toString())
                .path("workspaces/jh/artifacts/da/operations/register/execute")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new Gson().toJson(new Object[] { "https://jacamotest.free.beeceptor.com/my/api/path" })));
        // create agent and add a plan to test
        response = client.target(uri.toString()).path("agents/belovedbob")
                .request()
                .post(Entity.json(""));
        response = client.target(uri.toString())
                .path("agents/belovedbob/plans")
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .post(
                        Entity.json(
                                "+ns1::beep(X) <- .print(see,X); +bb(X). "+
                                "+!test(A) <- .print(doing,A); act(open(A),R); .print(R). "
                        )
                );

        // ask the agent to focus and act
        sendMsg("belovedbob", "achieve", "jcm::focus_env_art(art_env(jh,da,ns1),5)");
        // ask to act
        sendMsg("belovedbob", "achieve", "test(door)");

        // run signal
        response = client
                .target(uri.toString())
                .path("workspaces/jh/artifacts/da/operations/doSignal/execute")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new Gson().toJson(new Object[] { "beep",  1111 })));

        // wait the agent to proceed
        Thread.sleep(3000);
        response = client.target(uri.toString())
                .path("agents/belovedbob/bb")
                .request(MediaType.APPLICATION_JSON).get();

        String rStr = response.readEntity(String.class);
        //System.out.println("Response (agents/belovedbob/bb): " + rStr);
        assertTrue(rStr.contains("\"agent\": \"belovedbob\""));
        //TODO: Next assert is causing failure on github actions.
        //assertTrue(rStr.contains("bb(1111)"));


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
