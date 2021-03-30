package jacamo.rest.mediation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cartago.AgentIdCredential;
import cartago.ArtifactConfig;
import cartago.ArtifactId;
import cartago.ArtifactInfo;
import cartago.ArtifactObsProperty;
import cartago.CartagoEnvironment;
import cartago.CartagoEvent;
import cartago.CartagoException;
import cartago.ICartagoCallback;
import cartago.ICartagoContext;
import cartago.Op;
import cartago.Workspace;
import cartago.WorkspaceId;
import jacamo.platform.EnvironmentWebInspector;

public class TranslEnv {

    /**
     * Get list of workspaces in JSON format.
     *
     * @return HTTP 200 Response (ok status) or 500 Internal Server Error in case of
     *         error (based on https://tools.ietf.org/html/rfc7231#section-6.6.1)
     *         Sample: ["main","testOrg","testwks","wkstest"]
     */
    public Collection<String> getWorkspaces() {
        return new ArrayList<>(); // TODO: CartagoService.getNode().getWorkspaces();
    }

    /**
     * Get details about a workspace, the artifacts that are situated on this
     * including their properties, operations, observers and linked artifacts
     *
     * @param wrksName name of the workspace
     * @return A map with workspace details
     * @throws CartagoException
     */
    public Map<String, Object> getWorkspace(String wrksName) throws CartagoException {

        Map<String, Object> workspace = new HashMap<String, Object>();

        Map<String, Object> artifacts = new HashMap<>();
        for (ArtifactId aid : CartagoEnvironment.getInstance().getController(wrksName).getCurrentArtifacts()) {
            artifacts.put(aid.getName(), getArtifact(wrksName, aid.getName()));
        }

        workspace.put("workspace", wrksName);
        workspace.put("artifacts", artifacts);

        return workspace;
    }

    /**
     * Get details about an artifact
     * including their properties, operations, observers and linked artifacts
     *
     * @param wrksName name of the workspace
     * @return A map with workspace details
     * @throws CartagoException
     */
    public Map<String, Object> getArtifact(String wrksName, String artName) throws CartagoException {

        ArtifactInfo info = CartagoEnvironment.getInstance().getController(wrksName).getArtifactInfo(artName);

        // Get artifact's properties
        List<Object> properties = new ArrayList<>();
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

        return artifact;
    }

    public void createWorkspace(String wrksName) throws CartagoException {
        cartago.CartagoEnvironment cenv = cartago.CartagoEnvironment.getInstance();
        var currentWks = cenv.getRootWSP().getWorkspace().getChildWSP(wrksName).get();

        if (currentWks != null)
            throw new CartagoException("Workspace " + wrksName + " already exists.");

        cenv.getRootWSP().getWorkspace().createWorkspace(wrksName);
        if (EnvironmentWebInspector.get() != null)
            EnvironmentWebInspector.get().registerWorkspace(wrksName);
    }

    public void createArtefact(String wrksName, String artName, String javaClass, Object[] values) throws CartagoException {
        var ctxt = getWorkSpaceJoined(wrksName);
        workspaces.get(wrksName).makeArtifact(ctxt.getAgentId(), artName, javaClass, new ArtifactConfig(values));
    }


    public Object[] getObsPropValue(String wrksName, String artName, String obsPropId) throws CartagoException {
        ArtifactInfo info = CartagoEnvironment.getInstance().getController(wrksName).getArtifactInfo(artName);
        for (ArtifactObsProperty op : info.getObsProperties()) {
            if (op.getName().equals(obsPropId)) {
                return op.getValues();
            }
        }
        return null;
    }


    public void execOp(String wrksName, String artName, String operation, Object[] values) throws CartagoException {
        ICartagoContext ctxt = getWorkSpaceJoined(wrksName);
        ArtifactId aid = workspaces.get(wrksName).lookupArtifact(null, artName);
        if (aid == null) {
            throw new CartagoException("artifact "+artName+" not found");
        }
        ctxt.doAction(1, aid.getName(), new Op(operation, values), null, -1);
    }


    protected WorkspaceId getWId(String wrksName) throws CartagoException {
        return workspaces.get(wrksName).getId();
    }

    Map<String, ICartagoContext> contexts = new HashMap<>(); // cache
    Map<String, Workspace> workspaces = new HashMap<>();
    protected ICartagoContext getWorkSpaceJoined(String wrksName) throws CartagoException {
        var ctxt = contexts.get(wrksName);
        if (ctxt == null) {
            cartago.CartagoEnvironment cenv = cartago.CartagoEnvironment.getInstance();
            Workspace currentWks = cenv.getRootWSP().getWorkspace().getChildWSP(wrksName).get().getWorkspace();
            workspaces.put(wrksName, currentWks);
            ctxt = currentWks.joinWorkspace(new AgentIdCredential("JaCaMoRest"), new ICartagoCallback() {
                public void notifyCartagoEvent(CartagoEvent a) {    }
            });

            contexts.put(wrksName, ctxt);
        }
        return ctxt;
    }

}
