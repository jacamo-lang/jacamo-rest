package jacamo.rest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.w3c.dom.Document;

import com.google.gson.Gson;

import cartago.ArtifactId;
import cartago.ArtifactInfo;
import cartago.CartagoException;
import cartago.CartagoService;
import cartago.WorkspaceId;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import jaca.CAgentArch;
import jason.ReceiverNotFoundException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.Circumstance;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSemantics.Option;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Trigger;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.infra.centralised.CentralisedAgArch;
import jason.util.Config;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.SchemeBoard;
import ora4mas.nopl.oe.Group;

@Singleton
@Path("/agents")
public class RestImplAg extends AbstractBinder {

    protected Map<String, StringBuilder> agLog = new HashMap<>();
    Gson gson = new Gson();     
    
    @Override
    protected void configure() {
        bind(new RestImplAg()).to(RestImplAg.class);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getAgentsJSON() {
        return gson.toJson(BaseCentralisedMAS.getRunner().getAgs().keySet());
    }

    private Agent getAgent(String agName) {
        CentralisedAgArch cag = BaseCentralisedMAS.getRunner().getAg(agName);
        if (cag != null)
            return cag.getTS().getAg();
        else
            return null;
    }

    /** AGENT **/

    protected Transformer  mindInspectorTransformerHTML = null;
    protected int MAX_LENGTH = 35;
    
    /**
     * Configure to show or hide window items
     * Items: "bels", "annots", "rules", "evt", "mb", "int", "int-details"
     */
    Map<String,Boolean> show = new HashMap<>();
    {
        show.put("annots", Config.get().getBoolean(Config.SHOW_ANNOTS));
    }
    
    @Path("/{agentname}/hide")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String setHide(@PathParam("agentname") String agName,
            @QueryParam("bels") String bels,
            @QueryParam("rules") String rules,
            @QueryParam("int-details") String intd,
            @QueryParam("annots") String annots) {
        if (bels != null) show.put("bels",false);
        if (rules != null) show.put("rules",false);
        if (intd != null) show.put("int-details",false);
        if (annots != null) show.put("annots",false);
        return "<head><meta http-equiv=\"refresh\" content=\"0; URL='/agents/"+agName+"/mind'\" /></head>ok";
    }

    @Path("/{agentname}/show")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String setShow(@PathParam("agentname") String agName,
            @QueryParam("bels") String bels,
            @QueryParam("rules") String rules,
            @QueryParam("int-details") String intd,
            @QueryParam("annots") String annots) {
        if (bels != null) show.put("bels",true);
        if (rules != null) show.put("rules",true);
        if (intd != null) show.put("int-details",true);
        if (annots != null) show.put("annots",true);
        return "<head><meta http-equiv=\"refresh\" content=\"0; URL='/agents/"+agName+"/mind'\" /></head>ok";
    }

    static String helpMsg1 = "Example: +bel; !goal; .send(bob,tell,hello); +{+!goal <- .print(ok) });";


    @Path("/{agentname}")
    @POST
    //@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    //@Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_HTML)
    public Response createNewAgent(@PathParam("agentname") String agName) { //@FormParam("name") String agName) {
        try {
            String name = BaseCentralisedMAS.getRunner().getRuntimeServices().createAgent(agName, null, null, null, null, null, null);
            BaseCentralisedMAS.getRunner().getRuntimeServices().startAgent(name);
            // set some source for the agent
            Agent ag = getAgent(name);
            
            try {
                // TODO: create an agent without plans! use POST plans for that
                File f = new File("src/agt/" + agName + ".asl");
                if (!f.exists()) {
                    f.createNewFile(); 
                    FileOutputStream outputFile = new FileOutputStream(f, false);
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("//Agent created automatically\n\n");
                    stringBuilder.append("!start.\n\n");
                    stringBuilder.append("+!start <- .print(\"Hi\").\n\n");
                    stringBuilder.append("{ include(\"$jacamoJar/templates/common-cartago.asl\") }\n");
                    stringBuilder.append("{ include(\"$jacamoJar/templates/common-moise.asl\") }\n");
                    stringBuilder.append("// uncomment the include below to have an agent compliant with its organisation\n");
                    stringBuilder.append("//{ include(\"$moiseJar/asl/org-obedient.asl\") }");
                    byte[] bytes = stringBuilder.toString().getBytes();
                    outputFile.write(bytes);            
                    outputFile.close();
                } 
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            ag.load(new FileInputStream("src/agt/" + agName + ".asl"), agName + ".asl");
            //ag.setASLSrc("no-inicial.asl");
            createAgLog(agName, ag);
            
            return Response.ok().build(); //"<head><meta http-equiv=\"refresh\" content=\"2; URL='/agents/"+name+"/mind'\" /></head>ok for "+name;
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500, e.getMessage()).build();
        }
    }

    @Path("/{agentname}")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public String killAgent(@PathParam("agentname") String agName) throws ReceiverNotFoundException {
        try {
            boolean r = BaseCentralisedMAS.getRunner().getRuntimeServices().killAgent(agName,"web", 0);
            return "result of kill: "+r;
        } catch (Exception e) {
            return "Agent "+agName+" in unknown."+e.getMessage();
        }
    }

    @Path("/{agentname}/status")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getAgentStatusJSON(@PathParam("agentname") String agName) {
        Agent ag = getAgent(agName);
        Circumstance c = ag.getTS().getC();
        
        Map<String,Object> props = new HashMap<>();
        
        props.put("idle", ag.getTS().canSleep());
                
        props.put("nbIntentions", c.getNbRunningIntentions()+
                c.getPendingIntentions().size());
        
        List<Map<String,Object>> ints = new ArrayList<>();
        Iterator<Intention> ii = c.getAllIntentions();
        while (ii.hasNext()) {
            Intention i = ii.next();
            Map<String,Object> iprops = new HashMap<>();
            iprops.put("id", i.getId());
            iprops.put("finished", i.isFinished());
            iprops.put("suspended", i.isSuspended());
            if (i.isSuspended()) {
                iprops.put("suspendedReason", i.getSuspendedReason());
            }
            iprops.put("size", i.size());
            ints.add(iprops);
        }
        props.put("intentions", ints);
        return gson.toJson(props);
    }


    @Path("/{agentname}/mind")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Document getAgentMindXml(@PathParam("agentname") String agName) {
        try {
            Agent ag = getAgent(agName);
            if (ag != null)
                return ag.getAgState();
            else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    @Path("/{agentname}/mind")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getAgentMindHtml(@PathParam("agentname") String agName) {
        StringWriter mainContent = new StringWriter();

        try {
            if (mindInspectorTransformerHTML == null) {
                mindInspectorTransformerHTML = TransformerFactory.newInstance().newTransformer(
                        new StreamSource(this.getClass().getResource("/xml/agInspection.xsl").openStream()));
            }
            for (String p : show.keySet())
                mindInspectorTransformerHTML.setParameter("show-" + p, show.get(p) + "");
            Agent ag = getAgent(agName);
            if (ag != null) {
                StringWriter so = new StringWriter();
                mindInspectorTransformerHTML.transform(new DOMSource(ag.getAgState()), new StreamResult(so));
                mainContent.append(so.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } // transform to HTML
        return mainContent.toString();
    }

    @Path("/{agentname}/mind/bb")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getAgentBBJSON(@PathParam("agentname") String agName) {
        Agent ag = getAgent(agName);
        List<String> bbs = new ArrayList<>();
        for (Literal l: ag.getBB()) {
            bbs.add(l.toString());
        }
        return gson.toJson(bbs);
    }

    // ****
    //      Plans
    // ****

    
    @Path("/{agentname}/plans")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getAgentPlansTxt(@PathParam("agentname") String agName,
            @DefaultValue("all") @QueryParam("label") String label) {
        StringWriter so = new StringWriter();
        try {
            Agent ag = getAgent(agName);
            if (ag != null) {
                PlanLibrary pl = ag.getPL();
                if (label.equals("all"))
                    so.append(pl.getAsTxt(false));
                else
                    so.append(pl.get(label).toASString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            so.append("Agent "+agName+" does not exist or cannot be observed.");
        }
        return so.toString();
    }
    
    @Path("/{agentname}/aslfile/{aslfilename}")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public String loadASLfileForm(@PathParam("agentname") String agName,
            @PathParam("aslfilename") String aslFileName,
            @FormDataParam("aslfile") InputStream uploadedInputStream
            ) {
        try {
            String r = "nok";
            Agent ag = getAgent(agName);
            if (ag != null) {
                System.out.println("agName: "+agName);
                System.out.println("restAPI://"+aslFileName);
                System.out.println("uis: "+uploadedInputStream);

                StringBuilder stringBuilder = new StringBuilder();
                String line = null;
                
                FileOutputStream outputFile = new FileOutputStream("src/agt/" + aslFileName, false);
                BufferedReader out = new BufferedReader(new InputStreamReader(uploadedInputStream));
                
                while ((line = out.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                
                byte[] bytes=stringBuilder.toString().getBytes();
                outputFile.write(bytes);
                outputFile.close();
                
                ag.getPL().clear();
                ag.parseAS(new FileInputStream("src/agt/" + aslFileName), aslFileName);
                
                r = "<br/><center>Agent reloaded but keeping intentions!<br/>Redirecting...</center>";
            }
            return "<head><meta http-equiv=\"refresh\" content=\"1; URL='/agents/"+agName+
            	   "/mind'\"/><link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\"></head>"+r;
        } catch (Exception e) {
            e.printStackTrace();
            return "error "+e.getMessage();
        }
    }
 
    //TODO: not being used anymore, remove it? 
    @Path("/{agentname}/plans")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public String loadPlans(@PathParam("agentname") String agName,
            @DefaultValue("") @FormDataParam("plans") String plans,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail
            ) {
        try {
            String r = "nok";
            Agent ag = getAgent(agName);
            if (ag != null) {
                ag.parseAS(new StringReader(plans), "RrestAPI");
                
                System.out.println("agName: "+agName);
                System.out.println("plans: "+plans);
                System.out.println("restAPI://"+fileDetail.getFileName());
                System.out.println("uis: "+uploadedInputStream);
                
                ag.load(uploadedInputStream, "restAPI://"+fileDetail.getFileName());
                r = "ok, code uploaded!";
            }
            return "<head><meta http-equiv=\"refresh\" content=\"2; URL='/agents/"+agName+"/mind/'\"/></head>"+r;
        } catch (Exception e) {
            e.printStackTrace();
            return "error "+e.getMessage();
        }
    }
    
    
    // ****
    //      Commands
    // ****

    @Path("/{agentname}/cmd")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public String runCmdPost(@FormParam("c") String cmd, @PathParam("agentname") String agName) {
        String r = execCmd(agName, cmd.trim());
        addAgLog(agName, "Command "+cmd+": "+r);
        return r;
    }

    @Path("/{agentname}/log")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getLogOutput(@PathParam("agentname") String agName) {
        StringBuilder o = agLog.get(agName);
        if (o != null) {
            return o.toString();
        }
        return "";
    }
    
    @Path("/{agentname}/log")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public String delLogOutput(@PathParam("agentname") String agName) {
        agLog.put(agName, new StringBuilder());
        return "ok";
    }

    String execCmd(String agName, String sCmd) {
        try {
            if (sCmd.endsWith("."))
                sCmd = sCmd.substring(0,sCmd.length()-1);
            PlanBody lCmd = ASSyntax.parsePlanBody(sCmd);
            Trigger  te   = ASSyntax.parseTrigger("+!run_repl_expr");
            Intention i   = new Intention();
            i.push(new IntendedMeans(
                       new Option(
                           new Plan(null,te,null,lCmd),
                           new Unifier()),
                       te));

            Agent ag = getAgent(agName);
            if (ag != null) {
                TransitionSystem ts = ag.getTS();
                ts.getC().addRunningIntention(i);
                ts.getUserAgArch().wake();
                createAgLog(agName, ag);
                return "included for execution";
            } else {
                return "not implemented";
            }
        } catch (Exception e) {
            return("Error parsing "+sCmd+"\n"+e);
        }
    }

    protected void createAgLog(String agName, Agent ag) {
        // adds a log for the agent
        if (agLog.get(agName) == null) {
            agLog.put(agName, new StringBuilder());
            ag.getTS().getLogger().addHandler( new StreamHandler() {
                @Override
                public void publish(LogRecord l) {
                    addAgLog(agName, l.getMessage());
                }
            });
        }       
    }
    
    protected void addAgLog(String agName, String msg) {
        StringBuilder o = agLog.get(agName);
        if (o == null) {
            o = new StringBuilder();
            agLog.put(agName, o);
        } else {
            o.append("\n");
        }
        String dt = new SimpleDateFormat("dd-MM-yy HH:mm:ss").format(new Date());
        o.append("["+dt+"] "+msg);
    }
    
    
    // ****
    //      mail box
    // ****

    @Path("/{agentname}/mb")
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_PLAIN)
    public String addAgMsg(Message m, @PathParam("agentname") String agName) {
        CentralisedAgArch a = BaseCentralisedMAS.getRunner().getAg(agName);
        if (a != null) {
            a.receiveMsg(m.getAsJasonMsg());
            return "ok";
        } else {
            return "receiver not found";
        }
    }
    
    // TODO: add JSON version of add msg
    
    
    @Path("/{agentname}/mind/img.svg")
    @GET
    @Produces("image/svg+xml")
    public Response getAgentImg(@PathParam("agentname") String agName) {
        try {
            String dot = getAgAsDot(agName);
            if (dot != null && !dot.isEmpty()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                MutableGraph g = Parser.read(dot);
                Graphviz.fromGraph(g).render(Format.SVG).toOutputStream(out);
                return Response.ok(out.toByteArray()).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.noContent().build(); // TODO: set response properly
    }

    protected String getAgAsDot(String agName) {
        String graph = "digraph G {\n" + "   error -> creating;\n" + "   creating -> GraphImage;\n" + "}";
        
        try {

            StringBuilder sb = new StringBuilder();

            // get workspaces the agent are in (including organisations)
            List<String> workspacesIn = new ArrayList<>();
            Agent ag = getAgent(agName);
            CAgentArch cartagoAgArch = getCartagoArch(ag);
            for (WorkspaceId wid: cartagoAgArch.getSession().getJoinedWorkspaces()) {
                workspacesIn.add(wid.getName());
            }
            
            sb.append("digraph G {\n");
            sb.append("\tgraph [\n");
            sb.append("\t\trankdir=\"LR\"\n");
            sb.append("\t\tbgcolor=\"transparent\"\n");
            sb.append("\t]\n");

            {// beliefs will be placed on the left
                sb.append("\tsubgraph cluster_mind {\n");
                sb.append("\t\tstyle=rounded\n");
                ag.getBB().getNameSpaces().forEach(x -> {
                    sb.append("\t\t\"" + x + "\" [ " + "\n\t\t\tlabel = \"" + x + "\"");
                    sb.append("\n\t\t\tshape=\"cylinder\" style=filled pencolor=black fillcolor=cornsilk\n");
                    sb.append("\t\t];\n");
                });
                sb.append("\t};\n");
                // just to avoid put agent node into the cluster
                ag.getBB().getNameSpaces().forEach(x -> {
                    sb.append("\t\"" + agName + "\"->\"" + x + "\" [arrowhead=none constraint=false style=dotted]\n");
                });
            }

            StringBuilder orglinks = new StringBuilder();

            { // groups and roles are also placed on the left

                for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                    if (workspacesIn.contains(gb.getOEId())) {
                        gb.getGrpState().getPlayers().forEach(p -> {
                            if (p.getAg().equals(agName)) {
                                // group and role (arrow)
                                sb.append("\t\"" + gb.getArtId() + "\" [ " + "\n\t\tlabel = \"" + gb.getArtId() + "\"");
                                sb.append("\n\t\tshape=tab style=filled pencolor=black fillcolor=lightgrey\n");
                                sb.append("\t];\n");
                                // roles (arrows)
                                orglinks.append("\t\"" + gb.getArtId() + "\"->\"" + agName
                                        + "\" [arrowtail=normal dir=back label=\"" + p.getTarget() + "\"]\n");
                            }
                        });
                    }
                }

                for (SchemeBoard schb : SchemeBoard.getSchemeBoards()) {
                    schb.getSchState().getPlayers().forEach(p -> {
                        if (p.getAg().equals(agName)) {
                            // scheme
                            sb.append(
                                    "\t\t\"" + schb.getArtId() + "\" [ " + "\n\t\tlabel = \"" + schb.getArtId() + "\"");
                            sb.append("\n\t\t\tshape=hexagon style=filled pencolor=black fillcolor=linen\n");
                            sb.append("\t\t];\n");
                            for (Group gb : schb.getSchState().getGroupsResponsibleFor()) {
                                orglinks.append("\t\"" + gb.getId() + "\"->\"" + schb.getArtId()
                                        + "\" [arrowtail=normal arrowhead=open label=\"responsible\nfor\"]\n");
                                sb.append("\t\t{rank=same " + gb.getId() + " " + schb.getArtId() + "};\n");
                            }
                            orglinks.append("\t\"" + schb.getArtId() + "\"->\"" + p.getAg()
                                    + "\" [arrowtail=normal dir=back label=\"" + p.getTarget() + "\"]\n");
                        }
                    });
                }

                sb.append(orglinks);
            }
            
            {// agent will be placed on center
                String s1 = (agName.length() <= MAX_LENGTH) ? agName : agName.substring(0, MAX_LENGTH) + " ...";
                sb.append("\t\"" + agName + "\" [ " + "\n\t\tlabel = \"" + s1 + "\"");
                sb.append("\t\tshape = \"ellipse\" style=filled fillcolor=white\n");
                sb.append("\t];\n");
            }

            { // workspances and artifacts the agents is focused on
                workspacesIn.forEach(w -> {
                    String wksName = w.toString();
                    try {
                        for (ArtifactId aid : CartagoService.getController(wksName).getCurrentArtifacts()) {
                            ArtifactInfo info = CartagoService.getController(wksName).getArtifactInfo(aid.getName());
                            info.getObservers().forEach(y -> {
                                if ((info.getId().getArtifactType().equals("cartago.AgentBodyArtifact"))
                                        || (info.getId().getArtifactType().equals("ora4mas.nopl.GroupBoard"))
                                        || (info.getId().getArtifactType().equals("ora4mas.nopl.OrgBoard"))
                                        || (info.getId().getArtifactType().equals("ora4mas.nopl.SchemeBoard"))
                                        || (info.getId().getArtifactType().equals("ora4mas.nopl.NormativeBoard"))) {
                                    ; // do not print system artifacts
                                } else {
                                    if (y.getAgentId().getAgentName().equals(agName)) {
                                        // create a cluster for each artifact even at same wks of other artifacts?
                                        sb.append("\tsubgraph cluster_" + wksName + " {\n");
                                        sb.append("\t\tlabel=\"" + wksName + "\"\n");
                                        sb.append("\t\tlabeljust=\"r\"\n");
                                        sb.append("\t\tgraph[style=dashed]\n");
                                        String str1 = (info.getId().getName().length() <= MAX_LENGTH) ? info.getId().getName()
                                                : info.getId().getName().substring(0, MAX_LENGTH) + " ...";
                                        // It is possible to have same artifact name in different workspaces
                                        sb.append("\t\t\"" + wksName + "_" + info.getId().getName() + "\" [ " + "\n\t\t\tlabel=\"" + str1
                                                + " :\\n");
                                        
                                        str1 = (info.getId().getArtifactType().length() <= MAX_LENGTH)
                                                ? info.getId().getArtifactType()
                                                : info.getId().getArtifactType().substring(0, MAX_LENGTH) + " ...";
                                        sb.append(str1 + "\"\n");

                                        sb.append("\t\t\tshape=record style=filled fillcolor=white;\n");
                                        sb.append("\t\t\tURL=\"/workspaces/" + wksName + "/" + info.getId().getName() + "\";\n");
                                        
                                        sb.append("\t\t\tlabeltooltip=\"teste teste\";\n");
                                        sb.append("\t\t\theadlabel=\"teste2\";\n");
                                        
                                        
                                        sb.append("\t\t\ttarget=\"mainframe\";\n");
                                        sb.append("\t\t];\n");

                                        sb.append("\t};\n");

                                        sb.append("\t\"" + agName + "\"->\"" + wksName + "_" + info.getId().getName()
                                                + "\" [arrowhead=odot]\n");
                                    }
                                }
                            });
                        }
                    } catch (CartagoException e) {
                        e.printStackTrace();
                    }
                });
            }

            sb.append("}\n");
            graph = sb.toString();

        } catch (Exception ex) {
        }
        
        return graph;
    }

    protected CAgentArch getCartagoArch(Agent ag) {
        AgArch arch = ag.getTS().getUserAgArch().getFirstAgArch();
        while (arch != null) {
            if (arch instanceof CAgentArch) {
                return (CAgentArch)arch;
            }
            arch = arch.getNextAgArch();
        }
        return null;
    }
}
