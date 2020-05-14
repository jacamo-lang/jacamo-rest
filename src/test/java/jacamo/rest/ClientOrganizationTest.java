package jacamo.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.BeforeClass;
import org.junit.Test;


public class ClientOrganizationTest {
    static URI uri;
    Client client = ClientBuilder.newClient();

    @BeforeClass
    public static void launchSystem() {
        uri = RestTestUtils.launchRestSystem("src/test/test1.jcm");
    }
 
//    @AfterClass
//    public static void stopSystem() {
//        RestTestUtils.stopRestSystem();
//    } 
    

    @Test
    public void test501GetOrganizations() {
        System.out.println("\n\test501GetOrganizations");
        Response response;
        String rStr;
        
        response = client.target(uri.toString()).path("organisations/")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (organisations/): " + rStr);
        assertTrue(rStr.contains("testOrg"));
        
        client.close();
    }
    
    @Test
    public void test502GetOrganization() {
        System.out.println("\n\test502GetOrganization");
        Response response;
        String rStr;
        
        response = client.target(uri.toString()).path("organisations/testOrg")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (organisations/testOrg): " + rStr);
        assertTrue(rStr.contains("scheme1.mission1"));
        
        client.close();
    }
    
    @Test
    public void test503PostRoleUrl() {
        System.out.println("\n\ntest503PostRoleUrl");
        Response response;
        String rStr;
        Form form  = null;
        // POST returns 200 status code
        response = client
            .target(uri.toString())
            .path("organisations/testOrg/groups/group1/roles/role4")
            .request(MediaType.APPLICATION_FORM_URLENCODED)
            .post(Entity.form(form));
        
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (organisations/testOrg/groups/group1/roles/role4: " + rStr);
        assertEquals(200, response.getStatus());
        // GET against organization shows new role
        response = client.target(uri.toString()).path("organisations/testOrg")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (organisations/testOrg): " + rStr);
        assertTrue(rStr.contains("role4"));
        
        client.close();
    }
    
    @Test
    public void test504PostRoleJson() {
        System.out.println("\n\ntest504PostRoleJson");
        Response response;
        String rStr;
        // POST returns 200 status code
        response = client
            .target(uri.toString())
            .path("organisations/testOrg/groups/group1/roles/role5")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(null));
        
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (organisations/testOrg/groups/group1/roles/role5: " + rStr);
        assertEquals(200, response.getStatus());
        // GET against organization shows new role
        response = client.target(uri.toString()).path("organisations/testOrg")
                .request(MediaType.APPLICATION_JSON).get();
        rStr = response.readEntity(String.class).toString(); 
        System.out.println("Response (organisations/testOrg): " + rStr);
        assertTrue(rStr.contains("role5"));
        
        client.close();
    }
    
}
