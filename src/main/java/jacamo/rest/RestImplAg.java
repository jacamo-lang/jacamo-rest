package jacamo.rest;

import java.net.URI;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import com.google.gson.Gson;

/**
 * Agent's REST compile class
 * 
 * @author Jomi Fred Hubner
 * @author Cleber Jorge Amaral
 *
 */
@Singleton
@Path("/agents")
public class RestImplAg extends AbstractBinder {

    TranslAg tAg = new TranslAg();
    Gson gson = new Gson();

    @Override
    protected void configure() {
        bind(new RestImplAg()).to(RestImplAg.class);
    }
    
    /**
     * Produces JSON containing the list of existing agents Example: ["ag1","ag2"]
     * 
     * @return HTTP 200 Response (ok status)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAgents() {
        return Response.ok().entity(gson.toJson(tAg.getAgents())).header("Access-Control-Allow-Origin", "*").build();
    }

    /**
     * Create an Agent. Produces PLAIN TEXT with HTTP response for this operation If
     * an ASL file with the given name exists, it will launch an agent with existing
     * code. Otherwise, creates an agent that will start say 'Hi'.
     * 
     * @param agName name of the agent to be created
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response postAgent(@PathParam("agentname") String agName, @Context UriInfo uriInfo) {
        try {
            return Response.created(new URI(uriInfo.getBaseUri() + "agents/" + tAg.createAgent(agName))).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Kill an agent. Produces PLAIN TEXT with response for this operation.
     * 
     * @param agName agent's name to be killed
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteAgent(@PathParam("agentname") String agName) {
        try {
            return Response.ok("Result of kill: " + tAg.deleteAgent(agName)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Produces Agent's intentions statuses in JSON format. Example:
     * {"idle":true,"nbIntentions":1,"intentions":[{"size":1,"finished":false,"id":161,"suspended":false}]}
     * 
     * @param agName agent's name
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/status")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAgentStatus(@PathParam("agentname") String agName) {
        try {
            return Response.ok(gson.toJson(tAg.getAgentStatus(agName))).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Get agent information (namespaces, roles, missions and workspaces) in JSON
     * format
     * 
     * @param agName name of the agent
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     * 
     */
    @Path("/{agentname}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAgent(@PathParam("agentname") String agName) {
        try {
            return Response.ok(gson.toJson(tAg.getAgentDetails(agName))).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Return agent's Belief base in JSON format.
     * 
     * @param agName
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/mind/bb")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAgentBeliefbase(@PathParam("agentname") String agName) {
        try {
            return Response.ok(gson.toJson(tAg.getAgentsBB(agName))).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Upload new plans into an agent.
     * 
     * @param agName              name of the agent
     * @param plans               plans to be uploaded, as an String
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/plans")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response postAgentPlans(@PathParam("agentname") String agName, String plans) {
        try {
            tAg.addAgentPlan(agName, plans);
            return Response.ok("ok, code uploaded for agent '" + agName + "'!").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Send a command to an agent. Produces a TEXT PLAIN output containing a status
     * message
     * 
     * @param cmd    command expression
     * @param agName agent name
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/command")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postAgentCommand(@FormParam("c") String cmd, @PathParam("agentname") String agName) {
        try {
            return Response.ok(gson.toJson(tAg.executeCommand(cmd, agName))).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Get agent full log in a TEXT PLAIN format
     * 
     * @param agName agent name
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/log")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getAgentLog(@PathParam("agentname") String agName) {
        try {
            return Response.ok(tAg.getAgentLog(agName)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    @Path("/{agentname}/inbox")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response postAgentMessage(Message m, @PathParam("agentname") String agName) {
        try {
            tAg.addMessageToAgentMailbox(m, agName); 
            return Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }
}
