package jacamo.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientServicesTest {

    static URI uri;
    Client client = ClientBuilder.newClient();

    @BeforeClass
    public static void launchSystem() {
        uri = RestTestUtils.launchRestSystem("src/test/test1.jcm");
    }
    
    @BeforeClass
    public static void createService() {
        Client client = ClientBuilder.newClient();
        client.target(uri.toString()).path("agents/marcos/services/banking")
            .request()
            .post(Entity.json("{\"service\":\"banking(retail)\",\"type\":\"financial services\"}"));
        
        client.close();
    }
    
    @Test
    public void test301GetServices() {
        System.out.println("\n test301GetServices");
        Response response;
        String rStr;
        
        // Testing ok from root URI
        response = client.target(uri.toString()).path("services/")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (services/): " + rStr);
        assertTrue(rStr.contains("banking"));
        
        client.close();
    }

    @Test
    public void test401Subcribe() {
        System.out.println("\n test401Subcribe");
        
        Response response = client.target(uri.toString())
                .path("services/banana/subscriptions/jomi")
                .request()
                .post(null);
        System.out.println(response);
        assertEquals(201,  response.getStatus());
        
        client.close();
    }

}
