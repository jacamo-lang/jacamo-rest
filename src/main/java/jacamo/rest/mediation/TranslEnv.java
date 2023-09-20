package jacamo.rest.mediation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import cartago.*;
import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import jacamo.platform.EnvironmentWebInspector;
import jacamo.rest.implementation.RDFProcessing;

public class TranslEnv {



    /**
     * Get list of workspaces in JSON format.
     *
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample: ["main","testOrg","testwks","wkstest"]
     */
    public Collection<String> getWorkspaces() {
        var res = new HashSet<String>();
        cartago.CartagoEnvironment cenv = cartago.CartagoEnvironment.getInstance();
        for (WorkspaceDescriptor w: cenv.getRootWSP().getWorkspace().getChildWSPs()) {
            res.add(w.getId().getName());
        }
        return res;
    }

    /**
     * Get details about a workspace, the artifacts that are situated on this
     * including their properties, operations, observers and linked artifacts
     *
     * @param wrksName name of the workspace
     * @return A map with workspace details
     * @throws CartagoException
     */
    public JsonObject getWorkspace(String wrksName) throws CartagoException {

        var workspace = Json.createObjectBuilder()
                .add("workspace", wrksName);

        var artifacts = Json.createArrayBuilder();
        for (ArtifactId aid : resolveWorkspace(wrksName).getArtifactIdList()) {
            artifacts.add( aid.getName() );
        }
        workspace.add("artifacts", artifacts);

        return workspace.build();
    }

    /**
     * Get details about an artifact
     * including their properties, operations, observers and linked artifacts
     *
     * @param wrksName name of the workspace
     * @return A map with workspace details
     * @throws CartagoException
     */
    public JsonObject getArtifact(String wrksName, String artName) throws CartagoException {
        var info = getArtInfo(wrksName, artName);

        var artifact = Json.createObjectBuilder()
                .add("artifact", artName)
                .add("type", info.getId().getArtifactType());


        // Get artifact's properties
        var properties = Json.createArrayBuilder();
        for (ArtifactObsProperty op : info.getObsProperties()) {
            var values = Json.createArrayBuilder();
            for (Object vl : op.getValues()) {
                values.add(
                        Json.createValue(vl.toString())
                );
            }
            properties.add(
                    Json.createObjectBuilder()
                        .add(op.getName(), values)
                    );
        }
        artifact.add("properties", properties);

        // Get artifact's operations
        var operations = Json.createArrayBuilder();
        info.getOperations().forEach(y -> {
            operations.add(y.getOp().getName());
        });
        artifact.add("operations", operations);

        // Get agents that are observing the artifact
        var observers = Json.createArrayBuilder();
        info.getObservers().forEach(y -> {
            // do not print agents_body observation
            if (!info.getId().getArtifactType().equals(AgentBodyArtifact.class.getName())) {
                observers.add(y.getAgentId().getAgentName());
            }
        });
        artifact.add("observers", observers);

        // linked artifacts
        /* not used anymore
        var linkedArtifacts = Json.createArrayBuilder();
        info.getLinkedArtifacts().forEach(y -> {
            // linked artifact node already exists if it belongs to this workspace
            linkedArtifacts.add(y.getName());
        });
        artifact.add("linkedArtifacts", linkedArtifacts);*/

        return artifact.build();
    }

    public ThingDescription getArtifactTD(String wrksName, String artName) throws CartagoException {
        ThingDescription.Builder tdBuilder = new ThingDescription.Builder(artName);
        var info = getArtInfo(wrksName, artName);
        for (ArtifactObsProperty property: info.getObsProperties()){
            Form form = new Form.Builder(RDFProcessing.baseUrl +"workspaces/"+wrksName+"/artifacts/"+artName+"/properties/"+property.getName())
                    .setMethodName("GET")
                    .build();
            PropertyAffordance.Builder propertyAffordanceBuilder = new PropertyAffordance.Builder(property.getName(), form);

            tdBuilder.addProperty(propertyAffordanceBuilder.build());
        }
        for (OpDescriptor operation: info.getOperations()){
            Form form = new Form.Builder(RDFProcessing.baseUrl +"workspaces/"+wrksName+"/artifacts/"+artName+"/operations/"+operation.getKeyId()+"/execute")
                    .setMethodName("GET")
                    .build();
            ActionAffordance.Builder actionAffordanceBuilder = new ActionAffordance.Builder(operation.getKeyId(), form);

            tdBuilder.addAction(actionAffordanceBuilder.build());

        }
        return tdBuilder.build();
    }

    public void createWorkspace(String wrksName) throws CartagoException {
        cartago.CartagoEnvironment cenv = cartago.CartagoEnvironment.getInstance();
        var currentWks = cenv.getRootWSP().getWorkspace().getChildWSP(wrksName);

        if (currentWks.isPresent())
            throw new CartagoException("Workspace " + wrksName + " already exists.");

        cenv.getRootWSP().getWorkspace().createWorkspace(wrksName);
        System.out.println("Workspace "+wrksName+" created!");
        if (EnvironmentWebInspector.get() != null)
            EnvironmentWebInspector.get().registerWorkspace(wrksName);
    }

    public void createArtefact(String wrksName, String artName, String javaClass, Object[] values) throws CartagoException {
        resolveWorkspace(wrksName).makeArtifact(
                getWorkSpaceJoined(wrksName).getAgentId(),
                artName,
                javaClass,
                new ArtifactConfig(values));
    }


    public JsonArray getObsPropValue(String wrksName, String artName, String obsPropId) throws CartagoException {
        var json = Json.createArrayBuilder();
        for (ArtifactObsProperty op : getArtInfo(wrksName, artName).getObsProperties()) {
            if (op.getName().equals(obsPropId)) {
                for (Object o: op.getValues())
                    if (o instanceof Number)
                        json.add( Json.createValue( Double.parseDouble(o.toString())));
                    else
                        json.add( Json.createValue( o.toString()));
                return json.build();
            }
        }
        return null;
    }


    public void execOp(String wrksName, String artName, String operation, Object[] values) throws CartagoException {
        ArtifactId aid = resolveWorkspace(wrksName).lookupArtifact(null, artName);
        if (aid == null) {
            throw new CartagoException("artifact "+artName+" not found");
        }
        getWorkSpaceJoined(wrksName).doAction(1, aid.getName(), new Op(operation, values), null, -1);
    }


    Map<String, ICartagoContext> contexts = new HashMap<>(); // cache
    protected ICartagoContext getWorkSpaceJoined(String wrksName) throws CartagoException {
        var ctxt = contexts.get(wrksName);
        if (ctxt == null) {
            ctxt = resolveWorkspace(wrksName).joinWorkspace(new AgentIdCredential("JaCaMoRest"), new ICartagoCallback() {
                public void notifyCartagoEvent(CartagoEvent a) {    }
            });

            contexts.put(wrksName, ctxt);
        }
        return ctxt;
    }

    protected ArtifactInfo getArtInfo(String wrksName, String artName) throws CartagoException {
        //cartago.CartagoEnvironment cenv = cartago.CartagoEnvironment.getInstance();
        //Workspace wksId = cenv.getRootWSP().getWorkspace().resolveWSP(wrksName).get().getWorkspace();
        ICartagoController ctrl = CartagoEnvironment.getInstance().getController( "/main/"+wrksName); //.getId().getFullName());
        return ctrl.getArtifactInfo(artName);
    }

    protected Workspace resolveWorkspace(String wrksName) throws CartagoException {
        cartago.CartagoEnvironment cenv = cartago.CartagoEnvironment.getInstance();
        return cenv.getRootWSP().getWorkspace().resolveWSP(wrksName).get().getWorkspace();
    }

}
