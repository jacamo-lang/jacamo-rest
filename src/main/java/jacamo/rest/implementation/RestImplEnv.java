package jacamo.rest.implementation;

import java.net.URI;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import jacamo.rest.mediation.TranslEnv;

@Singleton
@Path("/workspaces")
@Api(value = "/workspaces")
public class RestImplEnv extends AbstractBinder {

    TranslEnv tEnv = new TranslEnv();

    @Override
    protected void configure() {
        bind(new RestImplEnv()).to(RestImplEnv.class);
    }

    /**
     * Get list of workspaces.
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample: ["main","testOrg","testwks","wkstest"]
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get list of workspaces.")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response getWorkspaces() {
        try {
            return Response
                    .ok()
                    .entity(new Gson().toJson(tEnv.getWorkspaces()))
                    .header("Access-Control-Allow-Origin", "*")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Get workspace information (its artifacts including their properties, operations, etc).
     * 
     * @param wrksName name of the workspace
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample:
     *         {"workspace":"testwks","artifacts":{"a":{"artifact":"a","operations":["observeProperty","inc"],
     *         "linkedArtifacts":["b"],"type":"tools.Counter","properties":[{"count":10}],"observers":["marcos"]},
     *         "b":{"artifact":"b","operations":["observeProperty","inc"],"linkedArtifacts":[],"type":"tools.Counter",
     *         "properties":[{"count":2}],"observers":["marcos"]}}}
     */
    @Path("/{wrksname}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get workspace information (its artifacts including their properties, operations, etc).")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response getWorkspace(@PathParam("wrksname") String wrksName) {
        try {
            return Response
                    .ok()
                    .entity(new Gson().toJson(tEnv.getWorkspace(wrksName)))
                    .header("Access-Control-Allow-Origin", "*")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Add a workspace.
     * 
     * @param wrksName
     * @return
     */
    @Path("/{wrksname}")
    @POST
    @ApiOperation(value = "Add a workspace.")
    @ApiResponses(value = { 
            @ApiResponse(code = 201, message = "generated uri"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response postWorkspace(@PathParam("wrksname") String wrksName, @Context UriInfo uriInfo) {
        try {
            tEnv.createWorkspace(wrksName);
            return Response
                    .created(new URI(uriInfo.getBaseUri() + "workspaces/" + wrksName))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }
    
    /**
     * Get artifact information (properties, operations, observers and linked artifacts).
     * 
     * @param wrksName name of the workspace the artifact is situated in
     * @param artName  name of the artifact to be retrieved
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample:
     *         {"artifact":"a","operations":["observeProperty","inc"],"linkedArtifacts":["b"],
     *         "type":"tools.Counter","properties":[{"count":10}],"observers":["marcos"]}
     */
    @Path("/{wrksname}/artifacts/{artname}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get artifact information (properties, operations, observers and linked artifacts).")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response getArtifact(@PathParam("wrksname") String wrksName, @PathParam("artname") String artName) {
        try {
            return Response
                    .ok()
                    .entity(new Gson()
                            .toJson(tEnv.getArtifact(wrksName, artName)))
                    .header("Access-Control-Allow-Origin", "*")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Get value of an observable property.
     * 
     * @param wrksName name of the workspace the artifact is situated in
     * @param artName  name of the artifact to be retrieved
     * @param obsProp  name of the observable property
     * @return HTTP 200 Response (ok an array of values) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample:
     *         [10]
     */
    @Path("/{wrksname}/artifacts/{artname}/properties/{propertyid}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get value of an observable property.")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response getArtifactProperties(
            @PathParam("wrksname") String wrksName, 
            @PathParam("artname") String artName, 
            @PathParam("propertyid") 
            String obsPropId) {
        try {
            return Response
                    .ok()
                    .entity(new Gson()
                            .toJson(tEnv.getObsPropValue(wrksName, artName, obsPropId)))
                    .header("Access-Control-Allow-Origin", "*")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Executes an operation in an artifact.
     */
    @Path("/{wrksname}/artifacts/{artname}/operations/{opname}/execute")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Executes an operation in an artifact.")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response postArtifactOperation(
            @PathParam("wrksname") String wrksName, 
            @PathParam("artname") String artName, 
            @PathParam("opname") String operationName, 
            Object[] values) {
        try {
            tEnv.execOp(wrksName, artName, operationName, values);
            return Response
                    .ok()
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Creates a new artifact from a given template.
     */
    @Path("/{wrksname}/artifacts/{artname}/{javaclass}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new artifact from a given template.")
    @ApiResponses(value = { 
            @ApiResponse(code = 201, message = "generated uri"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response postArtifact(
            @PathParam("wrksname") String wrksName, 
            @PathParam("artname") String artName, 
            @PathParam("javaclass") String javaClass, 
            Object[] values, 
            @Context UriInfo uriInfo) {
        try {
            tEnv.createArtefact(wrksName, artName, javaClass, values);
            return Response
                    .created(new URI(uriInfo.getBaseUri() + "workspaces/" + wrksName + "/" + artName))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }
}
