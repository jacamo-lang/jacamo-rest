package jacamo.rest;

import static org.junit.Assert.assertTrue;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.AfterClass;
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
    
    @AfterClass
    public static void stopSystem() {
        RestTestUtils.stopRestSystem();
    } 
    
    @Test
    public void test301GetServices() {
        System.out.println("\n\test301GetServices");
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
    
}
