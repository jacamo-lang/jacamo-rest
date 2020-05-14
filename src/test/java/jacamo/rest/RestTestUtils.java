package jacamo.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

public class RestTestUtils extends jacamo.util.TestUtils {
    protected static URI uri = null;
    
    public static URI launchRestSystem(String jcm) {
        if (launchSystem(jcm)) {
            try {
                // wait for start of jacamo rest
                while (uri == null) {
                    System.out.println("waiting for jacamo rest to start ....");
                    if (JCMRest.getRestHost() != null)
                        uri = UriBuilder.fromUri(JCMRest.getRestHost()).build();
                    else
                        Thread.sleep(400);
                }
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return uri;
    }

    public static void stopRestSystem() {
        stopSystem();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {      }
    }
}
