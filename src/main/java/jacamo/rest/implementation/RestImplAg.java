package jacamo.rest.implementation;

import java.net.URI;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import jacamo.rest.mediation.TranslAg;
import jacamo.rest.util.Message;
import jason.ReceiverNotFoundException;

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
     * Get list of agent   
     * 
     * Example:
     * {"kk":{ "type":"Jason",
     *         "inbox":"http://192.168.0.19:8080/agents/kk/inbox",
     *         "url":"http://192.168.0.19:8080/agents/kk"},
     *  "marcos":{"type":"Jason",
     *         "inbox":"http://192.168.0.19:8080/agents/marcos/inbox",
     *         "url":"http://192.168.0.19:8080/agents/marcos"}
     * }
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Create an Agent.")
    @ApiResponses(value = { 
            @ApiResponse(code = 201, message = "generated uri"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response postAgent(
            @PathParam("agentname") String agName, 
            @DefaultValue("false") @QueryParam("only_wp") boolean onlyWP, 
            @DefaultValue("false") @QueryParam("force")   boolean force, 
            Map<String,Object> metaData,
            @Context UriInfo uriInfo) {
        try {
            if (onlyWP) {
                metaData.put("remote", true);
                if (tAg.createWP(agName, metaData, force))
                    return Response.created(new URI(uriInfo.getBaseUri() + "agents/" + agName)).build();
                else
                    return Response.status(500, "Agent "+agName+" already exists!").build();
            } else {
                return Response
                        .created(new URI(uriInfo.getBaseUri() + "agents/" + tAg.createAgent(agName)))
                        .build();
            }
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
     * TODO: Update an agent (replace its code and reload).
     * 
     * @param agName agent's name
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    /* API Version 0.6?
    @Path("/{agentname}")
    @PUT
    @ApiOperation(value = "TODO: Update an agent (replace its code and reload).")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response updateAgent(@PathParam("agentname") String agName) {
        try {
            //TODO: to be developed
            return Response
                    .ok()
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }*/
    
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
        } catch (ReceiverNotFoundException e) {
            return Response.status(500, e.getMessage()).build();
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
        } catch (ReceiverNotFoundException e) {
            return Response.status(500, e.getMessage()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Return agent's plans
     * 
     * @param agName name of the agent
     * @param label optional filter
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Example: [\"@l__1[source(self)]\":\"@l__1[source(self)] +!start <- .print(hi).", 
     *         "\"@l__2[source(self)]\": \"@l__2[source(self)] +sayHi[source(A)] <- .print(\"I received hi from \",A)."]
     */
    @Path("/{agentname}/plans")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get agent plans.",
            notes = "Example: [\"@l__1[source(self)]\":\"@l__1[source(self)] +!start <- .print(hi).\", "+ 
                            "\"@l__2[source(self)]\": \"@l__2[source(self)] +sayHi[source(A)] <- .print(\"I received hi from \",A).\"]"
    )
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response getAgentPlansTxt(@PathParam("agentname") String agName,
            @DefaultValue("all") @QueryParam("label") String label) {
        try {
            return Response.ok(gson.toJson(tAg.getAgentPlans(agName, label))).build();
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
     *         Example: curl --request POST 'http://127.0.0.1:8080/agents/marcos/command'
     *                  --header 'Content-Type: application/x-www-form-urlencoded' --data-urlencode 'c=+raining'
     */
    @Path("/{agentname}/command")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Send a command to an agent returning a status message.",
            notes = "Example: curl --request POST 'http://127.0.0.1:8080/agents/marcos/command' "+
                    "--header 'Content-Type: application/x-www-form-urlencoded' --data-urlencode 'c=+raining'"
    )
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
     *         Example: [06-04-20 20:37:03] Command +raining: {}
     */
    @Path("/{agentname}/log")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(
            value = "Get agent full log as text.",
            notes = "Example: [06-04-20 20:37:03] Command +raining: {}"
    )
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
     *         Example: curl --location --request POST 'http://127.0.0.1:8080/agents/marcos/inbox'
     *         --header 'Content-Type: application/json'
     *         --data-raw '{"performative":"tell","sender":"jomi","receiver":"bob","content":"vl(10)","msgId":"34"}'
     */
    @Path("/{agentname}/inbox")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Append a message on agent's inbox.",
            notes = "Example: curl --location --request POST 'http://127.0.0.1:8080/agents/marcos/inbox'" + 
                            " --header 'Content-Type: application/json'" + 
                            " --data-raw '{\"performative\":\"tell\",\"sender\":\"jomi\",\"receiver\":\"bob\",\"content\":\"vl(10)\",\"msgId\":\"34\"}'"
            )
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
    
    /**
     * Get services provided by a given agent.
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Example: ["supply(banana)","consultant"]
     */
    @Path("/{agentname}/services")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get services provided by a given agent.",
            notes = "Example: [\"supply(banana)\",\"consultant\"]"
    )
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response getServices(@PathParam("agentname") String agName) {
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
     * Append a service to the agent.
     * 
     * @param agName agent name
     * @param serviceid service identification
     * @param values a map of services (optional)
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         
     *         Example: curl --request POST 'http://127.0.0.1:8080/agents/marcos/services/gardening' \
     *         --header 'Content-Type: application/json' \
     *         --data-raw '{"service":"gardening(vegetables)","type":"garden services"}'
     */
    @Path("/{agentname}/services/{serviceid}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Append a service to the agent.",
            notes = "Example: curl --request POST 'http://127.0.0.1:8080/agents/marcos/services/gardening'" + 
                            " --header 'Content-Type: application/json'" + 
                            " --data-raw '{\"service\":\"gardening(vegetables)\",\"type\":\"agent\"}"
    )
    @ApiResponses(value = { 
            @ApiResponse(code = 201, message = "generated uri"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response postAgentService(
            @PathParam("agentname") String agName, 
            @PathParam("serviceid") String service, 
            @Context UriInfo uriInfo, 
            Map<String, Object> values) {
        try {
            tAg.addServiceToAgent(agName, service, values);
            
            return Response
                    .created(new URI(uriInfo.getBaseUri() + agName + "/services/" + agName + "/services/" + service))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Remove a service from the agent.
     * 
     * @param agName agent name
     * @param serviceid service identification
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{agentname}/services/{serviceid}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Remove a service from the agent."
    )
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "generated uri"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response deleteAgentService(
            @PathParam("agentname") String agName, 
            @PathParam("serviceid") String service) {
        try {
            tAg.removeServiceToAgent(agName, service);
            return Response
                    .ok()
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

}
