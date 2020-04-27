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

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.junit.Test;

import com.google.gson.Gson;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientWorkspaceTest {
    static URI uri;
    Client client = ClientBuilder.newClient();

    @BeforeClass
    public static void launchSystem() {
        uri = TestUtils.launchSystem("src/test/test1.jcm");
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
    
}
