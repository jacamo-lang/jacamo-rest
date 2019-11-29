package jacamo.rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import com.google.gson.Gson;

import cartago.ArtifactId;
import cartago.ArtifactInfo;
import cartago.ArtifactObsProperty;
import cartago.CartagoException;
import cartago.CartagoService;

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
            Gson gson = new Gson();

            return Response.ok().entity(gson.toJson(tEnv.getWorkspaces())).header("Access-Control-Allow-Origin", "*")
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
            Gson gson = new Gson();

            Map<String, Object> workspace = new HashMap<String, Object>();
            try {
                Map<String, Object> artifacts = new HashMap<>();
                for (ArtifactId aid : CartagoService.getController(wrksName).getCurrentArtifacts()) {
                    ArtifactInfo info = CartagoService.getController(wrksName).getArtifactInfo(aid.getName());

                    // Get artifact's properties
                    Set<Object> properties = new HashSet<>();
                    for (ArtifactObsProperty op : info.getObsProperties()) {
                        for (Object vl : op.getValues()) {
                            Map<String, Object> property = new HashMap<String, Object>();
                            property.put(op.getName(), vl);
                            properties.add(property);
                        }
                    }

                    // Get artifact's operations
                    Set<String> operations = new HashSet<>();
                    info.getOperations().forEach(y -> {
                        operations.add(y.getOp().getName());
                    });

                    // Get agents that are observing the artifact
                    Set<Object> observers = new HashSet<>();
                    info.getObservers().forEach(y -> {
                        // do not print agents_body observation
                        if (!info.getId().getArtifactType().equals("cartago.AgentBodyArtifact")) {
                            observers.add(y.getAgentId().getAgentName());
                        }
                    });

                    // linked artifacts
                    Set<Object> linkedArtifacts = new HashSet<>();
                    info.getLinkedArtifacts().forEach(y -> {
                        // linked artifact node already exists if it belongs to this workspace
                        linkedArtifacts.add(y.getName());
                    });

                    // Build returning object
                    Map<String, Object> artifact = new HashMap<String, Object>();
                    artifact.put("artifact", aid.getName());
                    artifact.put("type", info.getId().getArtifactType());
                    artifact.put("properties", properties);
                    artifact.put("operations", operations);
                    artifact.put("observers", observers);
                    artifact.put("linkedArtifacts", linkedArtifacts);
                    artifacts.put(aid.getName(), artifact);
                }

                workspace.put("workspace", wrksName);
                workspace.put("artifacts", artifacts);
            } catch (CartagoException e) {
                e.printStackTrace();
            }

            return Response.ok().entity(gson.toJson(workspace)).header("Access-Control-Allow-Origin", "*").build();

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
            Gson gson = new Gson();

            ArtifactInfo info = CartagoService.getController(wrksName).getArtifactInfo(artName);

            // Get artifact's properties
            Set<Object> properties = new HashSet<>();
            for (ArtifactObsProperty op : info.getObsProperties()) {
                for (Object vl : op.getValues()) {
                    Map<String, Object> property = new HashMap<String, Object>();
                    property.put(op.getName(), vl);
                    properties.add(property);
                }
            }

            // Get artifact's operations
            Set<String> operations = new HashSet<>();
            info.getOperations().forEach(y -> {
                operations.add(y.getOp().getName());
            });

            // Get agents that are observing the artifact
            Set<Object> observers = new HashSet<>();
            info.getObservers().forEach(y -> {
                // do not print agents_body observation
                if (!info.getId().getArtifactType().equals("cartago.AgentBodyArtifact")) {
                    observers.add(y.getAgentId().getAgentName());
                }
            });

            // linked artifacts
            Set<Object> linkedArtifacts = new HashSet<>();
            info.getLinkedArtifacts().forEach(y -> {
                // linked artifact node already exists if it belongs to this workspace
                linkedArtifacts.add(y.getName());
            });

            // Build returning object
            Map<String, Object> artifact = new HashMap<String, Object>();
            artifact.put("artifact", artName);
            artifact.put("type", info.getId().getArtifactType());
            artifact.put("properties", properties);
            artifact.put("operations", operations);
            artifact.put("observers", observers);
            artifact.put("linkedArtifacts", linkedArtifacts);

            return Response.ok().entity(gson.toJson(artifact)).header("Access-Control-Allow-Origin", "*").build();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

}
