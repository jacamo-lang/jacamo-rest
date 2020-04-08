package jacamo.rest;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import jacamo.infra.JaCaMoLauncher;
import jacamo.rest.util.PostArtifact;
import jason.JasonException;

public class RestImplEnvTest {
    static URI uri;

    @BeforeClass
    public static void launchSystem() {
        try {
            // Wait JaCaMo be finished by other tests
            while (JaCaMoLauncher.getRunner() != null) {
                Thread.sleep(400);
            }

            // Launch jacamo and jacamo-rest running marcos.jcm
            new Thread() {
                public void run() {
                    String[] arg = { "src/test/test1.jcm" };
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
            System.out.println("URI="+uri);
            // wait agents
            while (JaCaMoLauncher.getRunner().getNbAgents() == 0) {
                System.out.println("waiting agents to start...");
                Thread.sleep(400);
            }
            Thread.sleep(400);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void stopSystem() {
        JaCaMoLauncher.getRunner().finish();
    }    

    @Test(timeout=2000)
    public void testObsPropAndOperations1() {

        assertEquals( 10, getCount("testwks","a"));

        Client client = ClientBuilder.newClient();
        client
            .target(uri.toString())
            .path("workspaces/testwks/artifacts/a/operations/inc/execute")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(new Gson().toJson(new Object[] {})));
        
        assertEquals( 11, getCount("testwks","a"));

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
    public void testCreateArt1() {
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
    
}
