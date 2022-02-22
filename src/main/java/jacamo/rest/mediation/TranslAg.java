package jacamo.rest.mediation;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.tools.ant.filters.StringInputStream;

import cartago.AgentBodyArtifact;
import cartago.AgentSessionArtifact;
import cartago.ArtifactId;
import cartago.ArtifactInfo;
import cartago.CartagoEnvironment;
import cartago.CartagoException;
import cartago.ICartagoController;
import cartago.WorkspaceId;
import jaca.CAgentArch;
import jacamo.rest.JCMRest;
import jacamo.rest.util.Message;
import jason.JasonException;
import jason.ReceiverNotFoundException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.CircumstanceListener;
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
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.TokenMgrError;
import jason.infra.local.BaseLocalMAS;
import jason.infra.local.LocalAgArch;
import jason.runtime.RuntimeServicesFactory;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.SchemeBoard;
import ora4mas.nopl.oe.Group;

public class TranslAg {

    Map<String, StringBuilder> agLog = new HashMap<>();
    Executor executor = Executors.newFixedThreadPool(4);

    /**
     * Get existing agents
     *
     * @return Set of agents;
     */
    public Map<String,Map<String,Object>> getAgents() {
        try {
            return JCMRest.getJCMRest().getWP();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; //RuntimeServicesFactory.get().getAgentsNames();
    }

    /**
     * Create agent and corresponding asl file with the agName if possible, or agName_1, agName_2,...
     *
     * @param agName
     * @return
     * @throws Exception
     * @throws JasonException
     */
    public String createAgent(String agName) throws Exception, JasonException {
        String givenName = RuntimeServicesFactory.get().createAgent(agName, null, null, null, null, null, null);
        RuntimeServicesFactory.get().startAgent(givenName);

        Agent ag = getAgent(givenName);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{ include(\"$jacamoJar/templates/common-cartago.asl\") }\n");
        stringBuilder.append("{ include(\"$jacamoJar/templates/common-moise.asl\") }\n");
        ag.load(new StringInputStream( stringBuilder.toString()), "source-from-rest-api");
        createAgLog(givenName, ag);
        return givenName;
    }


    /**
     * Creates a new entry in the WP
     * @throws Exception
     */
    public boolean createWP(String agName, Map<String,Object> metadata, boolean force) throws Exception {
        if (!force && JCMRest.getJCMRest().getAgentMetaData(agName) != null) {
            System.err.println("Agent "+agName+" is already registered in ANS!");
            return false;
        } else {
            JCMRest.getJCMRest().registerAgent(agName, metadata);
            return true;
        }
    }

    /**
     * ask to agent to run a command
     *
     * @param cmd
     * @param agName
     * @return
     * @throws TokenMgrError
     * @throws Exception
     */
    public Map<String, String> executeCommand(String cmd, String agName) throws TokenMgrError, Exception {
        Agent ag = getAgent(agName);
        if (ag == null) {
            throw new Exception("Receiver '" + agName + "' not found");
        }
        createAgLog(agName, ag);

        cmd = cmd.trim();
        if (cmd.endsWith("."))
            cmd = cmd.substring(0, cmd.length() - 1);

        Unifier u = execCmd(ag, ASSyntax.parsePlanBody(cmd));
        addAgLog(agName, "Command " + cmd + ": " + u);

        Map<String, String> um = new HashMap<>();
        for (VarTerm v : u) {
            um.put(v.toString(), u.get(v).toString());
        }
        return um;
    }

    /**
     * Creates a log area for an agent
     *
     * @param agName agent name
     * @param ag     agent object
     */
    public void createAgLog(String agName, Agent ag) {
        // adds a log for the agent
        if (agLog.get(agName) == null) {
            agLog.put(agName, new StringBuilder());
            ag.getTS().getLogger().addHandler(new StreamHandler() {
                @Override
                public void publish(LogRecord l) {
                    addAgLog(agName, l.getMessage());
                }
            });
        }
    }

    /**
     * Add a message to the agent log.
     *
     * @param agName agent name
     * @param msg    message to be added
     */
    protected void addAgLog(String agName, String msg) {
        StringBuilder o = agLog.get(agName);
        if (o == null) {
            o = new StringBuilder();
            agLog.put(agName, o);
        } else {
            o.append("\n");
        }
        String dt = new SimpleDateFormat("dd-MM-yy HH:mm:ss").format(new Date());
        o.append("[" + dt + "] " + msg);
    }

    /**
     * kill an agent
     * @param agName
     * @return
     * @throws Exception
     */
    public void deleteAgent(String agName) throws Exception {
        if (BaseLocalMAS.getRunner().getAg(agName) != null) {
            if (!RuntimeServicesFactory.get().killAgent(agName, "web", 0))
                throw new Exception("Unable to kill agent: " + agName);
        }
        JCMRest.getJCMRest().deregisterAgent(agName); // should not count on the agent stop method, it could be just a register without running agent
    }

    /**
     * Return status of the agent
     * @param agName
     * @return
     */
    public JsonObject getAgentStatus(String agName) throws JasonException {
        Agent ag = getAgent(agName);
        if (ag == null) {
            throw new ReceiverNotFoundException("agent "+agName+" does not exist in the MAS");
        }
        var json = Json.createObjectBuilder();
        var map = ag.getTS().getAgArch().getStatus();
        for (String k: map.keySet())
            json.add(k, map.get(k).toString());
        return json.build();
    }


    /**
     * List of plans
     *
     * @param agName name of the agent
     * @param label optional filter
     * @return list of string
     */
    public JsonObject getAgentPlans(String agName, String label) {
        var plans = Json.createObjectBuilder();
        Agent ag = getAgent(agName);
        if (ag != null) {
            PlanLibrary pl = ag.getPL();
            if (label.equals("all")) {
                Iterator<Plan> i = pl.getPlans().iterator();
                while (i.hasNext()) {
                    Plan p = i.next();
                    plans.add(p.getLabel().toString(), p.toASString());
                }
            } else {
                plans.add(label, pl.get(label).toASString());
            }
        }
        return plans.build();
    }

    /**
     * Add a piece or the whole agent's program
     *
     * @param agName
     * @param program
     * @throws Exception
     */
    public void addAgentProgram(String agName, String program) throws Exception {
        Agent ag = getAgent(agName);
        if (ag == null) {
            throw new Exception("Receiver '" + agName + "' not found");
        }
        ag.load(new StringInputStream(program), "source-from-rest-api");
    }

    /**
     * Add a plan to the agent's plan library
     *
     * @param agName
     * @param plans
     * @throws Exception
     */
    public void addAgentPlan(String agName, String plans) throws Exception {
        Agent ag = getAgent(agName);
        if (ag == null) {
            throw new Exception("Receiver '" + agName + "' not found");
        }
        ag.parseAS(new StringReader(plans), "RestAPI");
    }

    /**
     * Get agent information (roles, missions and workspaces)
     *
     * @param agName name of the agent
     * @throws CartagoException
     *
     */
    public JsonObject getAgentOverview(String agName) throws Exception {
        var agDetails = Json.createObjectBuilder()
                .add("agent", agName);

        // meta data
        Map<String, Object> agentMD = JCMRest.getJCMRest().getAgentMetaData(agName);
        if (agentMD == null) {
            throw new ReceiverNotFoundException("agent "+agName+" does not exist in the MAS");
        }

        for (String k: agentMD.keySet())
            agDetails.add(k, Json.createValue( agentMD.get(k).toString() ));

        Agent ag = getAgent(agName);
        if (ag != null) {

            // get workspaces the agent are in (including organisations)
            List<String> workspacesIn = new ArrayList<>();
            CAgentArch cartagoAgArch = getCartagoArch(ag);

            for (WorkspaceId wid : cartagoAgArch.getSession().getJoinedWorkspaces()) {
                workspacesIn.add(wid.getName());
            }

            // get groups and roles this agent plays
            var roles = Json.createArrayBuilder();
            for (GroupBoard gb : GroupBoard.getGroupBoards()) {
                if (workspacesIn.contains(gb.getOEId())) {
                    gb.getGrpState().getPlayers().forEach(p -> {
                        if (p.getAg().equals(agName)) {
                            roles.add(Json.createObjectBuilder()
                                    .add("group", gb.getArtId())
                                    .add("role",p.getTarget())
                                    .build());
                        }
                    });
                }
            }
            agDetails.add("roles", roles.build());

            // get schemed this agent belongs
            var missions = Json.createArrayBuilder();
            for (SchemeBoard schb : SchemeBoard.getSchemeBoards()) {
                schb.getSchState().getPlayers().forEach(p -> {
                    if (p.getAg().equals(agName)) {
                        var schemeMission = Json.createObjectBuilder()
                                .add("scheme", schb.getArtId())
                                .add("mission", p.getTarget());

                        var responsibles = Json.createArrayBuilder();
                        for (Group gb : schb.getSchState().getGroupsResponsibleFor())
                            responsibles.add( Json.createValue(gb.getId()));
                        schemeMission.add("responsible-groups", responsibles.build());
                        missions.add(schemeMission);
                    }
                });
            }
            agDetails.add("missions", missions);


            var workspaces = Json.createArrayBuilder();
            cartagoAgArch.getAllJoinedWsps().forEach(wksId -> {
                var workspace = Json.createObjectBuilder()
                        .add("workspace", wksId.getFullName());
                var artifacts = Json.createArrayBuilder();

                // focused arts
                try {
                    ICartagoController ctrl = CartagoEnvironment.getInstance().getController(wksId.getFullName());
                    for (ArtifactId aid : ctrl.getCurrentArtifacts()) {
                        ArtifactInfo info = ctrl.getArtifactInfo(aid.getName());
                        info.getObservers().forEach(y -> {
                            if (y.getAgentId().getAgentName().equals(agName) &&
                                !info.getId().getArtifactType().equals(AgentSessionArtifact.class.getName()) &&
                                !info.getId().getArtifactType().equals(AgentBodyArtifact.class.getName())) {
                                artifacts.add(Json.createObjectBuilder()
                                        .add("artifact", info.getId().getName())
                                        .add("type", info.getId().getArtifactType()));
                            }
                        });
                    }
                    var barts = artifacts.build();
                    if (barts.size() > 0)
                        workspace.add("artifacts", barts);
                    workspaces.add(workspace);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            agDetails.add("workspaces", workspaces);
        }

        return agDetails.build();
    }

    public JsonObject getAgentBB(String agName) throws Exception {
        var agDetails = Json.createObjectBuilder()
                .add("agent", agName);

        Agent ag = getAgent(agName);
        if (ag != null) {

            var nameSpaces = Json.createArrayBuilder();
            ag.getBB().getNameSpaces().forEach(x -> {
                nameSpaces.add(x.toString());
            });

            agDetails.add("namespaces", nameSpaces);

            var bels = Json.createArrayBuilder();
            for (Literal l: ag.getBB())
                bels.add( l.getAsJson());
            agDetails.add("beliefs", bels);
        }

        return agDetails.build();
    }



    /**
     * Send a command to an agent
     *
     * @param agName name of the agent
     * @param sCmd   command to be executed
     * @return Status message
     * @throws ParseException
     */
    public Unifier execCmd(Agent ag, PlanBody lCmd) throws ParseException {
        Trigger te = ASSyntax.parseTrigger("+!run_repl_expr");
        Intention i = new Intention();
        IntendedMeans im = new IntendedMeans(new Option(new Plan(null, te, null, lCmd), new Unifier()), te);
        i.push(im);

        Lock lock = new ReentrantLock();
        Condition goalFinished  = lock.newCondition();
        executor.execute( () -> {
                /*GoalListener gl = new GoalListener() {
                    public void goalSuspended(Trigger goal, String reason) {}
                    public void goalStarted(Event goal) {}
                    public void goalResumed(Trigger goal) {}
                    public void goalFinished(Trigger goal, FinishStates result) {
                        System.out.println("finished!");
                        if (goal.equals(te)) {
                            // finished
                            //if (result.equals(FinishStates.achieved)) {
                            //}
                            try {
                                lock.lock();
                                goalFinished.signalAll();
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                    public void goalFailed(Trigger goal) {
                        if (goal.equals(te)) {
                            try {
                                lock.lock();
                                goalFinished.signalAll();
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                };*/
                CircumstanceListener cl = new CircumstanceListener() {
                    public void intentionDropped(Intention ci) {
                        //System.out.println("*finished!"+ci);
                        if (ci.equals(i)) {
                            try {
                                lock.lock();
                                goalFinished.signalAll();
                            } finally {
                                lock.unlock();
                            }
                        }
                    };

                };
                TransitionSystem ts = ag.getTS();
                try {
                    lock.lock();
                    //ts.addGoalListener(gl);
                    ts.getC().addEventListener(cl);
                    ts.getC().addRunningIntention(i);
                    ts.getAgArch().wake();
                    goalFinished.await();
                    //ts.removeGoalListener(gl);
                    ts.getC().removeEventListener(cl);
                } catch (InterruptedException e) {
                } finally {
                    lock.unlock();
                }
        });
        try {
            lock.lock();
            goalFinished.await();

            return im.getUnif();
        } catch (InterruptedException e) {
        } finally {
            lock.unlock();
        }
        return null;
    }

    /**
     * get agent log
     *
     * @param agName
     * @throws Exception
     */
    public String getAgentLog(String agName) {
        if (agLog.containsKey(agName)) {
            return agLog.get(agName).toString();
        } else {
            return "Log is empty/absent.";
        }
    }

    /**
     * Return agent object by agent's name
     *
     * @param agName name of the agent
     * @return Agent object
     */
    public Agent getAgent(String agName) {
        LocalAgArch cag = BaseLocalMAS.getRunner().getAg(agName);
        if (cag != null)
            return cag.getTS().getAg();
        else
            return null;
    }

    /**
     * Get agent's CArtAgO architecture
     *
     * @param ag Agent object
     * @return agent's CArtAgO architecture
     */
    public CAgentArch getCartagoArch(Agent ag) {
        AgArch arch = ag.getTS().getAgArch().getFirstAgArch();
        while (arch != null) {
            if (arch instanceof CAgentArch) {
                return (CAgentArch) arch;
            }
            arch = arch.getNextAgArch();
        }
        return null;
    }

    /**
     * add a message to the agent's mailbox
     *
     * @param m
     * @param agName
     * @throws Exception
     */
    public void addMessageToAgentMailbox(Message m, String agName) throws Exception {
        LocalAgArch a = BaseLocalMAS.getRunner().getAg(agName);
        if (a != null) {
            a.receiveMsg(m.getAsJasonMsg());
        } else {
            throw new Exception("Internal Server Error! Receiver '" + agName + "' not found");
        }
    }

    /**
     * Returns agents by services
     *
     * @return
     * @throws Exception
     */
    public Map<String, Set<String>> getCommonDF() throws Exception {
        return RuntimeServicesFactory.get().getDF();
    }

    /**
     * Return content of getCommonDF but ready to send to the client (in Json format)
     *
     * @return
     * @throws Exception
     */
    public JsonObject getJsonifiedDF() throws Exception {
        Map<String, Set<String>> commonDF = getCommonDF();

        // Json of the DF
        var jsonifiedDF = Json.createObjectBuilder();
        for (String ag : commonDF.keySet()) {
            var agent = Json.createObjectBuilder()
                    .add("agent", ag);
            var services = Json.createArrayBuilder();
            for (String ags: commonDF.get(ag))
                services.add(ags);
            agent.add("services", services);
            jsonifiedDF.add(ag,agent);
        }
        return jsonifiedDF.build();
    }

    public JsonArray getJsonifiedDF(String service) throws Exception {
        Map<String, Set<String>> commonDF = getCommonDF();

        var ans = Json.createArrayBuilder();
        for (String ag : commonDF.keySet()) {
            if (commonDF.get(ag).contains(service)) {
                ans.add(ag);
            }
        }
        return ans.build();
    }

    public JsonArray getAgJsonifiedDF(String ag) throws Exception {
        var ans = Json.createArrayBuilder();
        if (getCommonDF().get(ag) != null) {
            for (String s : getCommonDF().get(ag)) {
                ans.add(s);
            }
        }
        return ans.build();
    }

    public void subscribe(String agName, String service, String type) {
        RuntimeServicesFactory.get().dfSubscribe(agName, service, type);
    }


    /**
     * Add a service to a given agent
     *
     * @param agName
     * @param values
     * @throws Exception
     */
    public void addServiceToAgent(String agName, String service, Map<String, Object> values) throws Exception {
        String type = "no-type";
        // if a map was provided, use it instead of original given service
        if (values != null) {
            service = values.getOrDefault("service", service).toString();
            type    = values.getOrDefault("type", "no-type").toString();
        }
        RuntimeServicesFactory.get().dfRegister(agName, service, type);
    }

    /**
     * Remove a service from a given agent
     *
     * @param agName
     * @param values
     * @throws Exception
     */
    public void removeServiceToAgent(String agName, String service) throws Exception {
        RuntimeServicesFactory.get().dfDeRegister(agName, service, "no-type");
    }

}
