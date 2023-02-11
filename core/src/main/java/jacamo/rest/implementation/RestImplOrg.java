package jacamo.rest.implementation;

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jacamo.rest.mediation.TranslOrg;

@Singleton
@Path("/organisations")
@Api(value = "/organisations")
public class RestImplOrg extends AbstractBinder {

    TranslOrg tOrg = new TranslOrg();
    
    @Override
    protected void configure() {
        bind(new RestImplOrg()).to(RestImplOrg.class);
    }

    /**
     * Get list of running organisations.
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample: ["testOrg","wkstest"]
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get list of running organisations.")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response getOrganisationsJSON() {

        Gson gson = new Gson();
        try {
            return Response
                    .ok(gson.toJson(tOrg.getOrganisations()))
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * Get organisation's information (groups, schemes and norms).
     * 
     * @param oeName name of the organisation
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample:
     *         {"norms":[{"mission":"scheme1.mission1","role":"role1","type":"obligation","norm":"norm2"},
     *         {"mission":"scheme1.mission2","role":"role2","type":"obligation","norm":"norm1"}],
     *         "organisation":"testOrg","groups":[{"roles":[{"role":"role1","cardinality":"1..1","superRoles":["soc"]},
     *         {"role":"soc","cardinality":"0..*","superRoles":[]},{"role":"role2","cardinality":"0..1","superRoles":["soc"]},
     *         {"role":"role3","cardinality":"0..*","superRoles":["role2"]}],"isWellFormed":true,"subGroups":[],"group":"group1"}],
     *         "schemes":[{"scheme":"scheme1","missions":[{"mission":"mission1","missionGoals":["goal2","goal4"]},
     *         {"mission":"mission2","missionGoals":["goal3"]}],"players":["marcos (
	 *         mission1 )"],"isWellFormed":true, "goals":["goal2 \u003c-
	 *         goal1","goal3 \u003c- goal1","goal4 \u003c- goal1"]}]}
     */
    @Path("/{oename}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get organisation's information (groups, schemes and norms).")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response getSpecificationJSON(@PathParam("oename") String oeName) {
        try {
            Gson gson = new Gson();
            return Response
                    .ok(gson.toJson(tOrg.getSpecification(oeName)))
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(500).build();
    }

    /**
     * Add a new role into an organisation/group.
     * 
     * @param oeName name of the organisation
     * @param groupName name of the group
     * @param role name of the new role
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     */
    @Path("/{oename}/groups/{groupname}/roles/{roleid}")
    @POST
    @Consumes({ MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Add a new role into an organisation/group.")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "success"),
            @ApiResponse(code = 500, message = "internal error")
    })
    public Response createNewRole(@PathParam("oename") String oeName, @PathParam("groupname") String groupName,
            @PathParam("roleid") String roleid) {
        try {
            tOrg.createRole(oeName, roleid);

            return Response
                    .ok()
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(500).build();
    }

}
