package jacamo.rest.config;

import java.util.HashMap;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;

import jacamo.rest.implementation.RestImpl;
import jacamo.rest.implementation.RestImplAg;
import jacamo.rest.implementation.RestImplDF;
import jacamo.rest.implementation.RestImplEnv;
import jacamo.rest.implementation.RestImplOrg;
import jacamo.rest.implementation.RestImplWP;

@ApplicationPath("/")
public class RestAppConfig extends ResourceConfig {
    public RestAppConfig() {
        // Registering resource classes
        registerClasses(
                RestImpl.class, 
                RestImplAg.class, 
                RestImplEnv.class, 
                RestImplOrg.class, 
                RestImplDF.class,
                RestImplWP.class);
        
        // gzip compression
        registerClasses(EncodingFilter.class, GZipEncoder.class, DeflateEncoder.class);
        
        addProperties(new HashMap<String,Object>() {
            private static final long serialVersionUID = 1L;

            { put("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature"); }
        } );        
    }
}
