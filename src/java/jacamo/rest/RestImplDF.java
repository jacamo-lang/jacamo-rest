package jacamo.rest;

import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.internal.inject.AbstractBinder;

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

}
