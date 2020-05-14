package jacamo.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

public class RestTestUtils extends jacamo.util.TestUtils {
    protected static URI uri = null;
    
    public static URI launchRestSystem(String jcm) {
        if (launchSystem(jcm)) {
        	uri = UriBuilder.fromUri(JCMRest.getRestHost()).build();
        }
        return uri;
    }

    public static void stopRestSystem() {
        stopSystem();
    }
}
