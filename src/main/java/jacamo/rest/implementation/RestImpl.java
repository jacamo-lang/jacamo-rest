package jacamo.rest.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import com.google.gson.Gson;

import cartago.CartagoException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jacamo.rest.mediation.TranslAg;
import jacamo.rest.mediation.TranslEnv;
import jacamo.rest.mediation.TranslOrg;

@Singleton
@Path("/")
@Api(value = "/")
public class RestImpl extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new RestImpl()).to(RestImpl.class);
    }

    /**
     * Get MAS overview.
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/overview")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get MAS overview.")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response getOverviewJSON() {
        Gson gson = new Gson();
        Map<String, Object> overview = new HashMap<>();

        try {
            TranslOrg tOrg = new TranslOrg();
            TranslAg tAg = new TranslAg();
            TranslEnv tEnv = new TranslEnv();

            List<Object> organisations = new ArrayList<>();
            overview.put("organisations", organisations);
            tOrg.getOrganisations().forEach(o -> {
                organisations.add(tOrg.getSpecification(o));
            });

            List<Object> agents = new ArrayList<>();
            overview.put("agents", agents);
            tAg.getAgents().keySet().forEach(a -> {
                try {
                    agents.add(tAg.getAgentDetails(a));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            List<Object> workspaces = new ArrayList<>();
            overview.put("workspaces", workspaces);
            tEnv.getWorkspaces().forEach(w -> {
                try {
                    workspaces.add(tEnv.getWorkspace(w));
                } catch (CartagoException e) {
                    e.printStackTrace();
                }
            });

            return Response.ok(gson.toJson(overview)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }
}
