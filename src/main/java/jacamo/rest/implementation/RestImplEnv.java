package jacamo.rest.implementation;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Singleton;
import javax.json.Json;
import javax.ws.rs.Consumes;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import jacamo.rest.mediation.TranslEnv;
import jacamo.rest.util.JsonFormater;
import jacamo.rest.util.PostArtifact;

@Singleton
@Path("/workspaces")
@Api(value = "/workspaces")
public class RestImplEnv extends AbstractBinder {

    TranslEnv tEnv = new TranslEnv();

    @Override
    protected void configure() {
        bind(new RestImplEnv()).to(RestImplEnv.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get available workspaces",
            notes = "workspaces are identified by a name (a string)",
            response = String.class,
            responseContainer = "List"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response getWorkspaces() {
        try {
            var json = Json.createArrayBuilder();
            for (String w: tEnv.getWorkspaces())
                json.add(w);
            return Response
                    .ok()
                    .entity( json.build().toString())
                    .header("Access-Control-Allow-Origin", "*")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    @Path("/{wrks_name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get workspace information",
        notes = "information is composed of artifacts and their properties, operations, etc"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",
                    examples = @Example( value = { @ExampleProperty( mediaType = "application/json", value =
                            """
            				{\"workspace\":\"testwks\",\"artifacts\":{\"a\":{\"artifact\":\"a\",\"operations\":[\"observeProperty\",\"inc\"],"
                                       \"linkedArtifacts\":[\"b\"],\"type\":\"tools.Counter\",\"properties\":[{\"count\":10}],\"observers\":[\"marcos\"]},\n"
            				            \"b\":{\"artifact\":\"b\",\"operations\":[\"observeProperty\",\"inc\"],\"linkedArtifacts\":[],\"type\":\"tools.Counter\",\n"
                                        \"properties\":[{\"count\":2}],\"observers\":[\"marcos\"]}}
                            }
                            """ )})
                            ),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response getWorkspace(@PathParam("wrks_name") String wrksName) {
        try {
            return Response
                    .ok()
                    .entity(JsonFormater.getAsJsonStr(tEnv.getWorkspace(wrksName)))
                    .header("Access-Control-Allow-Origin", "*")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    @Path("/{wrks_name}")
    @POST
    @ApiOperation(value = "Add a new workspace")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "generated uri",
                    examples = @Example( value = { @ExampleProperty( mediaType = "plain/text", value = "http://host/workspaces/newworkspacename") } )
            ),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response postWorkspace(@PathParam("wrks_name") String wrksName, @Context UriInfo uriInfo) {
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
     *         {"artifact":"a","operations":["observeProperty","inc"],
     *         "type":"tools.Counter","properties":[{"count": [10]}],"observers":["marcos"]}
     */
    @Path("/{wrks_name}/artifacts/{art_name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get artifact information (properties, operations, observers and linked artifacts).")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response getArtifact(@PathParam("wrks_name") String wrksName, @PathParam("art_name") String artName) {
        try {
            return Response
                    .ok()
                    .entity(JsonFormater.getAsJsonStr(tEnv.getArtifact(wrksName, artName)))
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
     * @param obsPropId  name of the observable property
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
            @PathParam("propertyid") String obsPropId) {
        try {
            return Response
                    .ok()
                    .entity(tEnv.getObsPropValue(wrksName, artName, obsPropId).toString())
                    .header("Access-Control-Allow-Origin", "*")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    /**
     * Set the value of an observable property. The artifact have to implement the operation "@OPERATION public void doUpdateObsProperty(String obName, Object arg)"
     *
     * @param wrksName name of the workspace the artifact is situated in
     * @param artName  name of the artifact to be retrieved
     * @param obsPropId  name of the observable property
     */
    @Path("/{wrksname}/artifacts/{artname}/properties/{propertyid}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Set the value of an observable property.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response setArtifactProperties(
            @PathParam("wrksname") String wrksName,
            @PathParam("artname") String artName,
            @PathParam("propertyid") String obsPropId,
            Object[] value) {
        try {
            var values = new ArrayList<Object>();
            values.add(obsPropId);
            values.addAll( Arrays.asList( value ));

            tEnv.execOp(wrksName, artName, "doUpdateObsProperty", values.toArray());
            return Response
                    .ok()
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
     *
     * @return HTTP 200 Response (ok an array of values) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Example of body: \"{\"template\":\"tools.Counter\",\"values\":[22]}\"
     */
    @Path("/{wrksname}/artifacts/{artname}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a new artifact from a given template.",
            notes = "Example of body: \"{\"template\":\"tools.Counter\",\"values\":[22]}\""
            )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "generated uri"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response postArtifact(
            @PathParam("wrksname") String wrksName,
            @PathParam("artname") String artName,
            String content,
            @Context UriInfo uriInfo) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            PostArtifact m = mapper.readValue(content, PostArtifact.class);

            tEnv.createArtefact(wrksName, artName, m.getTemplate(), m.getValues());
            return Response
                    .created(new URI(uriInfo.getBaseUri() + "workspaces/" + wrksName + "/" + artName))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }
}
