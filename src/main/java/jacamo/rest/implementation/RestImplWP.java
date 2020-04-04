package jacamo.rest.implementation;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.zookeeper.CreateMode;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import com.google.gson.Gson;

import jacamo.rest.JCMRest;

/** 
 * Rest interface for White Pages
 * 
 * @author jomi
 *
 */
@Singleton
@Path("/wp")
public class RestImplWP extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new RestImplWP()).to(RestImplWP.class);
    }

    /**
     * Get a Map of all registered agents and their address 
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         when ok JSON :
     *         {"kk":"http://127.0.0.1:8981/agents/kk","marcos":"http://127.0.0.1:8981/agents/marcos"}
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWP() {
        try {
            Gson gson = new Gson();

            Map<String, String> wp = new HashMap<>();

            if (JCMRest.getZKHost() != null) {
                for (String ag : JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKAgNodeId)) {                   
                    byte[] badr = JCMRest.getZKClient().getData().forPath(JCMRest.JaCaMoZKAgNodeId+"/"+ag);
                    if (badr != null)
                        wp.put(ag, new String(badr));
                }
            }

            return Response
                    .ok()
                    .entity(gson.toJson(wp))
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }

    }

    /**
     * add an entry in the WP
     * 
     * value is a Map in JSON like
     * 
     * {"agentid":"jomi","uri":"http://myhouse"}
     * 
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addWP(Map<String,String> values) {
        try {
        	String agName = values.get("agentid");
        	if (agName == null) {
                return Response.status(500, "no agentid informed").build();        		
        	}
        	String uri = values.get("uri");
        	if (uri == null) {
                return Response.status(500, "no uri informed").build();        		        		
        	}
            // register the agent in ZK
            if (JCMRest.getZKClient().checkExists().forPath(JCMRest.JaCaMoZKAgNodeId+"/"+agName) != null) {
                return Response.status(500, "agentid "+agName+" already exists").build();        		        		
            } else {
            	JCMRest.getZKClient().create().withMode(CreateMode.EPHEMERAL).forPath(JCMRest.JaCaMoZKAgNodeId+"/"+agName, uri.getBytes());
            }

            return Response
            		.ok()
            		.build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }
}
