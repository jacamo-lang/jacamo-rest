package jacamo.rest;

import java.util.HashMap;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/")
public class RestAppConfig extends ResourceConfig {
    public RestAppConfig() {
        registerInstances(new RestImplAg());
        
        addProperties(new HashMap<String,Object>() {{ put("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature"); }} );   
    }
}
