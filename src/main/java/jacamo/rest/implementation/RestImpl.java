package jacamo.rest.implementation;

import java.util.*;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.rio.RDFFormat;
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

    @Path("/")
    @GET
    @Produces(RDFProcessing.TURTLE)
    public Response getInitialInfoTurtle(){
        Model m = getInitialInfo();
        String s = RDFProcessing.writeToString(RDFFormat.TURTLE, m);
        return Response
                .ok()
                //.entity(JsonFormater.getAsJsonStr(tEnv.getArtifact(wrksName, artName)))
                .entity(s)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @Path("/")
    @GET
    @Produces(RDFProcessing.JSONLD)
    public Response getInitialInfoJsonLD(){
        Model m = getInitialInfo();
        String s = RDFProcessing.writeToString(RDFFormat.JSONLD, m);
        return Response
                .ok()
                //.entity(JsonFormater.getAsJsonStr(tEnv.getArtifact(wrksName, artName)))
                .entity(s)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    public Model getInitialInfo(){
        ModelBuilder builder = new ModelBuilder();
        Resource mainNode = RDFProcessing.rdf.createIRI(RDFProcessing.baseUrl);
        TranslEnv translEnv = new TranslEnv();
        Collection<String> workspaces = translEnv.getWorkspaces();
        for (String workspace: workspaces){
            Resource workspaceUrl = RDFProcessing.rdf.createIRI(RDFProcessing.baseUrl+"workspaces/"+workspace);
            builder.add(mainNode, RDFProcessing.rdf.createIRI("https://example.org/contains"), workspaceUrl);
        }
        return builder.build();
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
                    agents.add(tAg.getAgentOverview(a));
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
