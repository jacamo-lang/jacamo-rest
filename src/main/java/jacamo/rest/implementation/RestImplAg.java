package jacamo.rest.implementation;

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jacamo.rest.util.Message;
import jacamo.rest.mediation.TranslAg;

/**
 * Agent's REST compile class
 * 
 * @author Jomi Fred Hubner
 * @author Cleber Jorge Amaral
 *
 */
@Singleton
@Path("/agents")
@Api(value = "/agents")
public class RestImplAg extends AbstractBinder {

    TranslAg tAg = new TranslAg();
    Gson gson = new Gson();

    @Override
    protected void configure() {
        bind(new RestImplAg()).to(RestImplAg.class);
    }
    
    /**
     * Get list of agent names: ["ag1","ag2"]
     * 
     * @return HTTP 200 Response (ok status)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get list of agent names")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response getAgents() {
        return Response
                .ok()
                .entity(gson.toJson(tAg.getAgents()))
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    /**
     * Create an Agent. Produces PLAIN TEXT with HTTP response for this operation. If
     * an ASL file with the given name exists, it will launch an agent with existing
     * code. Otherwise, creates an agent that will start say 'Hi'.
     * 
     * @param agName name of the agent to be created
     * @return HTTP 201 Response (created) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Create an Agent.")
    @ApiResponses(value = { 
            @ApiResponse(code = 201, message = "generated uri"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response postAgent(@PathParam("agentname") String agName, @Context UriInfo uriInfo) {
        try {
            return Response
                    .created(new URI(uriInfo.getBaseUri() + "agents/" + tAg.createAgent(agName)))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Kill an agent.
     * 
     * @param agName agent's name to be killed
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}")
    @DELETE
    @ApiOperation(value = "Kill an agent.")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response deleteAgent(@PathParam("agentname") String agName) {
        try {
            tAg.deleteAgent(agName);
            return Response
                    .ok()
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Get agent's intentions status. Example:
     * {"idle":true,"nbIntentions":1,"intentions":[{"size":1,"finished":false,"id":161,"suspended":false}]}
     * 
     * @param agName agent's name
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/status")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get agent's intentions status.")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response getAgentStatus(@PathParam("agentname") String agName) {
        try {
            return Response
                    .ok(gson.toJson(tAg.getAgentStatus(agName)))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Get agent information (namespaces, roles, missions and workspaces).
     * 
     * @param agName name of the agent
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     * 
     */
    @Path("/{agentname}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get agent information (namespaces, roles, missions and workspaces).")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response getAgent(@PathParam("agentname") String agName) {
        try {
            return Response
                    .ok(gson.toJson(tAg.getAgentDetails(agName)))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Append new plans into an agent.
     * 
     * @param agName              name of the agent
     * @param plans               plans to be uploaded, as an String
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/plans")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Append new plans into an agent.")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response postAgentPlans(@PathParam("agentname") String agName, String plans) {
        try {
            tAg.addAgentPlan(agName, plans);
            return Response
                    .ok()
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Send a command to an agent returning a status message.
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
    @ApiOperation(value = "Send a command to an agent returning a status message.")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response postAgentCommand(@FormParam("c") String cmd, @PathParam("agentname") String agName) {
        try {
            return Response
                    .ok(gson.toJson(tAg.executeCommand(cmd, agName)))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Get agent full log as text.
     * 
     * @param agName agent name
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/log")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Get agent full log as text.")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response getAgentLog(@PathParam("agentname") String agName) {
        try {
            return Response
                    .ok(tAg.getAgentLog(agName))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Append a message on agent's inbox.
     * 
     * @param m message
     * @param agName agent name
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/inbox")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Append a message on agent's inbox.")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response postAgentMessage(Message m, @PathParam("agentname") String agName) {
        try {
            tAg.addMessageToAgentMailbox(m, agName); 
            return Response
                    .ok()
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }
}
