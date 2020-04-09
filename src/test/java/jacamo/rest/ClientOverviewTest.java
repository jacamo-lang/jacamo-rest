package jacamo.rest;

import static org.junit.Assert.assertTrue;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientOverviewTest {

    static URI uri;

    @BeforeClass
    public static void launchSystem() {
        uri = TestUtils.launchSystem("src/test/test1.jcm");
    }

    @AfterClass
    public static void stopSystem() {
        TestUtils.stopSystem();
    } 
    
    @Test(timeout=2000)
    public void test301GetOverview() {
        System.out.println("\n\test301GetOverview");
        Client client = ClientBuilder.newClient();
        Response response;
        String rStr;
        
        // Testing ok from root URI
        response = client.target(uri.toString()).path("overview/")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (overview/): " + rStr);
        assertTrue(rStr.contains("cartago.NodeArtifact"));
    }
    
}
