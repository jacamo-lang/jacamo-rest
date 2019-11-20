package jacamo.rest;

import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.tools.ant.filters.StringInputStream;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import jacamo.project.JaCaMoProject;
import jacamo.project.parser.JaCaMoProjectParser;
import jason.infra.centralised.RunCentralisedMAS;
import jason.mas2j.AgentParameters;
import jason.runtime.RuntimeServices;

@Singleton
@Path("/jcm")
public class RestImplJCM extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new RestImplJCM()).to(RestImplJCM.class);
    }

    @POST
    public Response runJCM(@FormParam("script") String script, @FormParam("path") String path) {
        try {
            JaCaMoProjectParser parser = new JaCaMoProjectParser(new StringInputStream(script));
            JaCaMoProject project = new JaCaMoProject();

            project = parser.parse(path);

            System.out.println(project.toString());
            
            // create agents
            RuntimeServices rt = RunCentralisedMAS.getRunner().getRuntimeServices(); 
            for (AgentParameters ap: project.getAgents()) {
                rt.createAgent(ap.getAgName(), ap.asSource.toString(), ap.agClass.getClassName(), ap.getAgArchClasses(), ap.getBBClass(), ap.getAsSetts(false, false), null);
            }
            
            // TODO: create workspaces, orgs, ...
            
            return Response.ok().entity(project.toString()).build(); 
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }
    
    // used to test
    @Path("/form")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getForm() {
        return Response.ok().entity("<html><body>\n" + 
                "	<form action=\"/jcm\" method=\"post\">\n" + 
                "		<p>\n" + 
                "			Directory : <input type=\"text\" name=\"path\" />\n" + 
                "		</p>\n" + 
                "		<p>\n" + 
                "			Script    : <br/><textarea name=\"script\" rows=\"10\" cols=\"40\">" +
                "mas bobandalice {\n" + 
                "\n" + 
                "    agent bob\n" + 
                "    agent alice\n" + 
                "\n" + 
                "}"+
                "</textarea></p>\n" + 
                "		<input type=\"submit\" value=\"Run JCM\" />\n" + 
                "	</form>\n" + 
                "</body></html>").build();
    }
}
