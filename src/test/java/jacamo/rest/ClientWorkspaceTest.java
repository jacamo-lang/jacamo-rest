package jacamo.rest;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.junit.Test;

import com.google.gson.Gson;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientWorkspaceTest {
    static URI uri;

    @BeforeClass
    public static void launchSystem() {
        uri = TestUtils.launchSystem("src/test/test1.jcm");
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
        assertEquals("tools.Counter", art.get("type"));
    }
    
}
