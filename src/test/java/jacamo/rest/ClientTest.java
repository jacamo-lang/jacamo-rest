package jacamo.rest;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.junit.Before;
import org.junit.Test;

import jacamo.infra.JaCaMoLauncher;

public class ClientTest {
    URI uri;

    @Before
    public void launchSystem() {
        try {
            // Launch jacamo and jacamo-rest running marcos.jcm
            JaCaMoLauncher jacamo = new JaCaMoLauncher();
            String[] arg = { "src/jcm/marcos.jcm" };
            jacamo.init(arg);
            jacamo.createEnvironment();
            jacamo.createAgs();
            JCMRest jcmrest = new JCMRest();
            String[] arg2 = { "--main 2181 --restPort 8080" };
            jcmrest.init(arg2);
            uri = UriBuilder.fromUri(JCMRest.getRestHost()).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPutMessageBeliefBase() {
        Client client = ClientBuilder.newClient();
        Response response = client.target(uri.toString()).path("agents/marcos/mind/bb")
                .request(MediaType.APPLICATION_JSON).get();

        String bb = response.readEntity(String.class);

        System.out.println("\n\nResponse: " + response.toString() + "\n" + bb.substring(1, 31));
        System.out.println("Expected: \"price(banana,X)[source(self)]");
		    System.out.println("Received: " + bb.substring(1, 31));

		    assertEquals("\"price(banana,X)[source(self)]", bb.substring(1, 31));
    }

    @Test
    public void testPutMessageInMailBox() {
        Client client = ClientBuilder.newClient();

        Message m = new Message("33", "signal", "jomi", "marcos", "oi");
        Response r = client.target(uri.toString()).path("agents/marcos/mb").request(MediaType.APPLICATION_XML)
                .accept(MediaType.TEXT_PLAIN).post(Entity.xml(m));

        System.out.println("Message sent result: " + r);

        assertEquals(200, r.getStatus());
    }
}
