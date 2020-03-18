package jacamo.rest;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import jacamo.infra.JaCaMoLauncher;
import jason.JasonException;

public class ClientTestEnv {
    URI uri;

    @Before
    public void launchSystem() {
        if (JaCaMoLauncher.getRunner() != null) {
            uri = UriBuilder.fromUri(JCMRest.getRestHost()).build();
            return;
        }
        try {
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
                    Thread.sleep(200);
            }
            // wait agents
            while (JaCaMoLauncher.getRunner().getNbAgents() == 0) {
                System.out.println("waiting agents to start...");
                Thread.sleep(200);
            }
            Thread.sleep(400);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testObsPropAndOperations1() {

        assertEquals( 10, getCount("a"));

        Client client = ClientBuilder.newClient();
        client
        		.target(uri.toString())
        		.path("workspaces/testwks/a/operations/inc")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .put(Entity.json(new Gson().toJson(new Object[] {})));
        
        assertEquals( 11, getCount("a"));

        client
			.target(uri.toString())
			.path("workspaces/testwks/a/operations/reset")
	        .request(MediaType.APPLICATION_JSON)
	        .accept(MediaType.TEXT_PLAIN)
	        .put(Entity.json(new Gson().toJson(new Object[] { 40 })));

        assertEquals( 40, getCount("a"));
    }

    public int getCount(String art) {
        Client client = ClientBuilder.newClient();
        Response response = client
        		.target(uri.toString())
        		.path("workspaces/testwks/"+art+"/obsprops/count")
                .request(MediaType.APPLICATION_JSON)
                .get();

        Object[] vl = new Gson().fromJson(response.readEntity(String.class), Object[].class);

        //System.out.println("\n\nResponse: " + response.toString() + "\n" + vl[0]);
        
    	return ((Double)vl[0]).intValue();
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testCreateArt1() {
        Client client = ClientBuilder.newClient();
        client
        		.target(uri.toString())
        		.path("workspaces/testwks/newart/tools.Counter")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .post(Entity.json(new Gson().toJson(new Object[] { 22 })));
        
        assertEquals( 22, getCount("newart"));

        Response response = client
        		.target(uri.toString())
        		.path("workspaces/testwks")
                .request(MediaType.APPLICATION_JSON)
                .get();

		Map vl = new Gson().fromJson(response.readEntity(String.class), Map.class);
        Map art = (Map)((Map)vl.get("artifacts")).get("newart");
        //System.out.println("\n\nResponse: " + response.toString() + "\n" + art);
        assertEquals("tools.Counter", art.get("type"));
    }
}
