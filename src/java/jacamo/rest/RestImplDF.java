package jacamo.rest;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import com.google.gson.Gson;

import jason.infra.centralised.BaseCentralisedMAS;

@Singleton
@Path("/services")
public class RestImplDF extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new RestImplDF()).to(RestImplDF.class);
    }

    /** DF **/

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getDFHtml() {

        StringWriter so = new StringWriter();
        so.append("<!DOCTYPE html>\n");
        so.append("<html lang=\"en\">\n");
        so.append("	<head>\n");
        so.append("		<title>Directory Facilitator</title>\n");
        so.append("	</head>\n");
        so.append("	<body>\n");
        so.append("	<div>\n");
        so.append("		<table>");
        so.append("		<tr><td>Agent</td>");
        so.append("		<td>Service<br/></td>");

        if (JCMRest.getZKHost() == null) {
            // get DF locally
            Map<String, Set<String>> df = BaseCentralisedMAS.getRunner().getDF();
            for (String a : df.keySet()) {
                so.append("				<tr><td>" + a + "</td>");
                for (String s : df.get(a)) {
                    so.append("				<td>" + s + "<br/></td>");
                }
                so.append("				</tr>");

            }
        } else {
            // get DF from ZK
            try {
                for (String s : JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKDFNodeId)) {
                    for (String a : JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKDFNodeId + "/" + s)) {
                        so.append("				<tr><td>" + a + "</td>");
                        so.append("				<td>" + s + "<br/></td>");
                        so.append("				</tr>");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        so.append("		</table>");
        so.append("</div>\n");
        so.append("	</body>\n");
        so.append("</html>\n");

        return so.toString();
    }

    /**
     * Get MAS Directory facilitator containing agents and services they provide
     * Following the format suggested in the second example of
     * https://opensource.adobe.com/Spry/samples/data_region/JSONDataSetSample.html
     * We are providing lists of maps
     * 
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         when ok JSON of the DF Sample output (jsonifiedDF):
     *         {"marcos":{"agent":"marcos","services":["vender(banana)","iamhere"]}}
     *         Testing platform: http://json.parser.online.fr/
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDFJSON() {
        try {
            Gson gson = new Gson();

            // Using format Map<String, Set> as a common representation of ZK and
            // BaseCentralisedMAS
            Map<String, Set<String>> commonDF;
            if (JCMRest.getZKHost() == null) {
                commonDF = BaseCentralisedMAS.getRunner().getDF();
            } else {
                commonDF = new HashMap<String, Set<String>>();

                for (String s : JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKDFNodeId)) {
                    for (String a : JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKDFNodeId + "/" + s)) {
                        if (!commonDF.containsKey(a)) {
                            Set<String> services = new HashSet<String>();
                            services.add(s);
                            commonDF.put(a, services);
                        } else {
                            commonDF.get(a).add(s);
                        }
                    }
                }
            }

            // Json of the DF
            Map<String,Object> jsonifiedDF = new HashMap<>();
            for (String s : commonDF.keySet()) {
                Map<String, Object> agent = new HashMap<String, Object>();
                agent.put("agent", s);
                Set<String> services = new HashSet<>();
                services.addAll(commonDF.get(s));
                agent.put("services", services);
                jsonifiedDF.put(s,agent);
            }

            return Response.ok().entity(gson.toJson(jsonifiedDF)).header("Access-Control-Allow-Origin", "*").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }

    }
}
