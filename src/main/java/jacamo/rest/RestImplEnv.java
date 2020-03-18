package jacamo.rest;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import com.google.gson.Gson;

@Singleton
@Path("/workspaces")
public class RestImplEnv extends AbstractBinder {

    TranslEnv tEnv = new TranslEnv();

    @Override
    protected void configure() {
        bind(new RestImplEnv()).to(RestImplEnv.class);
    }

    /**
     * Get list of workspaces in JSON format.
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample: ["main","testOrg","testwks","wkstest"]
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkspacesJSON() {
        try {
            return Response
            		.ok()
            		.entity(new Gson().toJson(tEnv.getWorkspaces()))
            		.header("Access-Control-Allow-Origin", "*")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * Get details about a workspace, the artifacts that are situated on this
     * including their properties, operations, observers and linked artifacts
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
    public Response getWorkspaceJSON(@PathParam("wrksname") String wrksName) {
        try {
            return Response
            		.ok()
            		.entity(new Gson().toJson(tEnv.getWorkspace(wrksName)))
            		.header("Access-Control-Allow-Origin", "*")
            		.build();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    @Path("/{wrksname}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response createWorkspace(@PathParam("wrksname") String wrksName) {
        try {
        	tEnv.createWorkspace(wrksName);
            return Response
            		.ok()
            		.build();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    
    /**
     * Get details about an artifact including its properties, operations, observers
     * and linked artifacts
     * 
     * @param wrksName name of the workspace the artifact is situated in
     * @param artName  name of the artifact to be retrieved
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample:
     *         {"artifact":"a","operations":["observeProperty","inc"],"linkedArtifacts":["b"],
     *         "type":"tools.Counter","properties":[{"count":10}],"observers":["marcos"]}
     */
    @Path("/{wrksname}/{artname}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArtifactJSON(@PathParam("wrksname") String wrksName, @PathParam("artname") String artName) {
        try {
            return Response
            		.ok()
            		.entity(new Gson()
            				.toJson(tEnv.getArtifact(wrksName, artName)))
            		.header("Access-Control-Allow-Origin", "*")
            		.build();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * Get value of an observable property
     * 
     * @param wrksName name of the workspace the artifact is situated in
     * @param artName  name of the artifact to be retrieved
     * @param obsProp  name of the observable property
     * @return HTTP 200 Response (ok an array of values) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample:
     *         [10]
     */
    @Path("/{wrksname}/{artname}/obsprops/{obspropid}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getObsPropJSON(@PathParam("wrksname") String wrksName, @PathParam("artname") String artName, @PathParam("obspropid") String obsPropId) {
        try {
            return Response
            		.ok()
            		.entity(new Gson()
            				.toJson(tEnv.getObsPropValue(wrksName, artName, obsPropId)))
            		.header("Access-Control-Allow-Origin", "*")
            		.build();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * Executes an operation in an artifact
     */
    @Path("/{wrksname}/{artname}/operations/{opname}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response execOperation(@PathParam("wrksname") String wrksName, @PathParam("artname") String artName, @PathParam("opname") String operationName, Object[] values) {
        try {
        	tEnv.execOp(wrksName, artName, operationName, values);
            return Response
            		.ok()
            		.build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * creates a new artifact
     */
    @Path("/{wrksname}/{artname}/{javaclass}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response createArt(@PathParam("wrksname") String wrksName, @PathParam("artname") String artName, @PathParam("javaclass") String javaClass, Object[] values) {
        try {
        	tEnv.createArtefact(wrksName, artName, javaClass, values);
            return Response
            		.ok()
            		.build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }
}
