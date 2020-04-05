package jacamo.rest;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jacamo.infra.JaCaMoLauncher;
import jason.JasonException;

public class RunCmdTest {
    static URI uri = null;

    @BeforeClass
    public static void launchSystem() {
        try {
            // Launch jacamo and jacamo-rest running marcos.jcm
            new Thread() {
                public void run() {
                    String[] arg = { "src/test/test0.jcm" };
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
            System.out.println("URI="+uri);
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
    
    @AfterClass
    public static void stopSystem() {
        JaCaMoLauncher.getRunner().finish();
    }    

    @Test(timeout=2000)
    public void testCmd1() {
        Form form = new Form();
        form.param("c", ".print(oi); X = 10;");
        Entity<Form> entity = Entity.form(form);
        
        Client client = ClientBuilder.newClient();
        Response response = client.target(uri.toString())
                .path("agents/bob/command")
                .request()
                .post(entity);

        String bb = response.readEntity(String.class);

        //System.out.println("\n\nResponse: " + response.toString() + "\n" + bb);

        assertEquals("{\"X\":\"10\"}", bb.toString());
    }

}
