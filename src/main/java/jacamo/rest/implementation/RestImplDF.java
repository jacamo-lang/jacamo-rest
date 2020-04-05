package jacamo.rest.implementation;

import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import com.google.gson.Gson;

import jacamo.rest.mediation.TranslAg;

@Singleton
@Path("/services")
public class RestImplDF extends AbstractBinder {

    TranslAg tAg = new TranslAg();
	
    @Override
    protected void configure() {
        bind(new RestImplDF()).to(RestImplDF.class);
    }

    /**
     * Get MAS Directory Facilitator containing agents and services they provide
     * Following the format suggested in the second example of
     * https://opensource.adobe.com/Spry/samples/data_region/JSONDataSetSample.html
     * We are providing lists of maps
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         when ok JSON of the DF Sample output (jsonifiedDF):
     *         {"marcos":{"agent":"marcos","services":["vender(banana)","iamhere"]}}
     *         Testing platform: http://json.parser.online.fr/
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDFJSON() {
        try {
            Gson gson = new Gson();

            return Response
                    .ok()
                    .entity(gson.toJson(tAg.getJsonifiedDF()))
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Get services provided by a given agent
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         ["vender(banana)","iamhere"]
     */

    @Path("/{agname}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServices(@PathParam("agname") String agName) {
        try {
            return Response
                    .ok()
                    .entity(new Gson().toJson( tAg.getCommonDF().get(agName) ))
                    .header("Access-Control-Allow-Origin", "*")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Add some service for an agent
     * 
     * JSON is a Map like {"service":"help(drunks)" }
     * 
     */
    @Path("/{agname}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addService(@PathParam("agname") String agName, Map<String, Object> values) {
        try {
            tAg.addServiceToAgent(agName, values);
                        
            return Response
                    .ok()
                    .build();
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }
}
