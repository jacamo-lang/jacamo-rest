package jacamo.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Scanner;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.internal.inject.AbstractBinder;

@Singleton
@Path("/")
public class RestImpl extends AbstractBinder {

    @Override
    protected void configure() {
        bind(new RestImpl()).to(RestImpl.class);
    }

    // HTML interface
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getRootHtml() {
        StringWriter so = new StringWriter();
        so.append("<!DOCTYPE html>\n"); 
        so.append("<html lang=\"en\">\n"); 
        so.append("	<head>\n");
        so.append("		<title>JaCamo-Rest</title>\n");
        so.append("     <link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\">\n");
        so.append("     <meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        so.append("	</head>\n");
        so.append("	<body>\n"); 
        so.append("		<div id=\"root\">\n"); 
        so.append("			<header class=\"row\">\n");
        // logo JaCaMo
        so.append("				<span class=\"logo col-xp-1 col-sm-2 col-md\">JaCaMo</span>\n"); 
        // top menu - button agents
        so.append("				<a class=\"button col-xp-1 col-sm-2 col-md\" href=\"agents/\" target=\"mainframe\">\n" +
                  "					<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"\n" + 
                  "						fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\"\n" + 
                  "						stroke-linejoin=\"round\" style=\"height: 20px; vertical-align: text-top;\">\n" + 
                  "						<circle cx=\"12\" cy=\"12\" r=\"11\"/>\n" + 
                  "					</svg><span>&nbsp;Agents</span>\n" + 
                  "				</a>\n");
        so.append("				<a class=\"button col-xp-1 col-sm-2 col-md\" href=\"workspaces/\" target=\"mainframe\">\n" + 
                  "					<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"\n" +  
                  "						fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\"\n" + 
                  "						stroke-linejoin=\"round\" style=\"height: 20px; vertical-align: text-top;\">\n" + 
                  "						<polygon points=\"0 1, 24 1, 24 8, 0 8, 0 16, 24 16, 24 23, 0 23, 0 1, 24 1, 24 23, 0 23\"></polygon>\n" +
                  "					</svg><span>&nbsp;Environment</span>\n" +
                  "				</a>\n");
        so.append("				<a class=\"button col-xp-1 col-sm-2 col-md\" href=\"oe/\" target=\"mainframe\">\n" + 
                  "					<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"\n" + 
                  "						fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\"\n" + 
                  "						stroke-linejoin=\"round\" style=\"height: 20px; vertical-align: text-top;\">\n" + 
                  "						<polygon points=\"0 1, 10 1, 10 6, 24 6, 24 23, 0 23, 0 6, 10 6, 0 6, 0 1\"></polygon>\n" + 
                  "					</svg><span>&nbsp;Organisation</span>\n" + 
                  "				</a>\n");
        so.append("				<label for=\"doc-drawer-checkbox\" class=\"button drawer-toggle\"></label>\n");
        so.append("				<input id=\"doc-drawer-checkbox\" class=\"drawer\" value=\"on\" type=\"checkbox\">\n" + 
                  "				<nav class=\"col-xp-1 col-md-2\" id=\"nav-drawer\">\n" + 
                  "					<label for=\"doc-drawer-checkbox\" class=\"button drawer-close\"></label>\n" + 
                  "					<h3>Menu</h3>\n" + 
                  "					<a hef=\"agents/\">Agents</a>\n" + 
                  "					<a hef=\"workspaces/\">Environment</a>\n" + 
                  "					<a hef=\"oe/\">Organisation</a>\n" + 
                  "				</nav>\n");
        so.append("			</header>\n"); 
        so.append("			<div class=\"second-row\" id=\"full-content\">\n");
        so.append("				<iframe id=\"mainframe\" name=\"mainframe\" width=\"100%\" height=\"100%\"\n" + 
                  "					frameborder=0></iframe>\n"); 
        so.append("			</div>\n");
        so.append("		</div>\n"); 
        so.append("	</body>\n"); 
        so.append("   <script>\n");
        so.append("       setInterval(function(){ document.getElementById(\"nav-drawer\").innerHTML=sessionStorage.getItem(\"menucontent\"); }, 500);\n");
        so.append("   </script>\n");

        so.append("</html>\n"); 

        return so.toString();
    }

    @Path("/forms/new_agent")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getNewAgentForm() {
        return  "<html><head><title>new agent form</title></head>"+
                "<input type=\"text\" name=\"name\"  size=\"43\" id=\"inputcmd\" placeholder='enter the name of the agent' onkeydown=\"if (event.keyCode == 13) runCMD()\" />\n" + 
                "<script language=\"JavaScript\">\n" + 
                "    function runCMD() {\n" +
                "        http = new XMLHttpRequest();\n" + 
                "        http.open(\"POST\", '/agents/'+document.getElementById('inputcmd').value, false); \n" +
                "        http.send();\n"+
                "        window.location.href = '/agents/'+document.getElementById('inputcmd').value+'/mind';\n"+
                "    }\n" + 
                "</script>"+
                "</form></html>";
    }

    @Path("/css/style.css")
    @GET
    @Produces("text/css")
    public String getStyleCSS() {
        StringBuilder so = new StringBuilder();
        Locale loc = new Locale("en", "US");
        try (Scanner scanner = new Scanner(new FileInputStream("src/resources/css/style.css"), "UTF-8")) {
            scanner.useLocale(loc);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                so.append(line).append("\n");
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return so.toString();
    }
    
}
