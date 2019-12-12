package jacamo.rest;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.junit.Before;
import org.junit.Test;

import jacamo.infra.JaCaMoLauncher;
import jason.JasonException;

public class RunCmdTest {
    URI uri = null;

    @Before
    public void launchSystem() {
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
            // wait agents
            while (JaCaMoLauncher.getRunner().getNbAgents() == 0) {
                System.out.println("waiting agents to start...");
                Thread.sleep(200);
            }
            Thread.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(timeout=2000)
    public void testCmd1() {
        Form form = new Form();
        form.param("c", ".print(oi); X = 10;");
        Entity<Form> entity = Entity.form(form);
        
        System.out.println("send");
        Client client = ClientBuilder.newClient();
        Response response = client.target(uri.toString())
                .path("agents/bob/cmd")
                .request()
                .post(entity);

        String bb = response.readEntity(String.class);

        //System.out.println("\n\nResponse: " + response.toString() + "\n" + bb);

        assertEquals("{\"X\":\"10\"}", bb.toString());
    }

}
