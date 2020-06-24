package jacamo.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import jason.runtime.DelegatedRuntimeServices;
import jason.runtime.RuntimeServicesFactory;

public class JCMRuntimeServices extends DelegatedRuntimeServices {
    JCMRest restImpl = null;
    Client  client   = ClientBuilder.newClient();
    
    public JCMRuntimeServices(JCMRest impl) {       
        super(RuntimeServicesFactory.get());
        restImpl = impl;
    }

    @Override
    public void dfRegister(String agName, String service, String type) {
        if (restImpl.isMain()) {
            super.dfRegister(agName, service, type);
        } else {
            synchronized (client) {
                client
                        .target( restImpl.getMainRest())
                        .path("agents/"+agName+"/services/"+service)
                        .request()
                        .post(null);                
            }
        }
    }
    
    @Override
    public void dfDeRegister(String agName, String service, String type) {
        //if (restImpl.isMain()) {
            super.dfDeRegister(agName, service, type);
        //} else {
            // TODO: implement in the interface REST
        //}
    }

    @Override
    public Collection<String> dfSearch(String service, String type) {
        if (!restImpl.isMain()) {
            synchronized (client) {
                Response response = client
                        .target( restImpl.getMainRest())
                        .path("/services/"+service)
                        .request(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_PLAIN)
                        .get();
                if (response.getStatus() == 200) {
                    Collection<String> a = new ArrayList<>();
                    for (Object o: response.readEntity(Collection.class))
                        a.add(o.toString());
                    return a;
                }
            }
        }
        return super.dfSearch(service, type);
    }
    
    /*@Override
    public void dfSubscribe(String agName, String service, String type) {
        if (restImpl.isMain()) {
            super.dfSubscribe(agName, service, type);
        } else {
        }*/

/*        
        
        try {
            RestAgArch arch = getRestAgArch(agName); 
            arch.getAsyncCurator()
                .with(WatchMode.successOnly).watched().getChildren().forPath(JCMRest.JaCaMoZKDFNodeId+"/"+service).event().thenAccept(event -> {
                    try {
                        //System.out.println("something changed...."+event.getType()+"/"+event.getState());
                        // stupid implementation: send them all again and
                        dfSubscribe(agName, service, type); // keep watching
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            // update providers
            Term s = new Atom("df");
            Literal l = ASSyntax.createLiteral("provider", new UnnamedVar(), new StringTermImpl(service));
            l.addSource(s);
            arch.getTS().getAg().abolish(l, new Unifier());
            for (String a: dfSearch(service, type)) {
                l = ASSyntax.createLiteral("provider", new Atom(a), new StringTermImpl(service));
                l.addSource(s);
                arch.getTS().getAg().addBel(l);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    
    @Override
    public Collection<String> getAgentsNames() {
        if (restImpl.isMain()) {
            super.getAgentsNames();
        } else {
            synchronized (client) {
                Response response = client
                        .target( restImpl.getMainRest())
                        .path("/agents")
                        .request(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_PLAIN)
                        .get();
                if (response.getStatus() == 200) {
                    Collection<String> a = new ArrayList<>();
                    for (Object o: response.readEntity(Map.class).keySet())
                        a.add(o.toString());
                    return a;
                }
            }
        }
        return super.getAgentsNames();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Map<String, Set<String>> getDF() {
        if (!restImpl.isMain()) {
            synchronized (client) {
                Response response = client
                        .target( restImpl.getMainRest())
                        .path("/services")
                        .request(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_PLAIN)
                        .get();
                if (response.getStatus() == 200) {
                    Map restAns = response.readEntity(Map.class);
                    Map<String, Set<String>> a = new HashMap<>();
                    for (Object ag: restAns.keySet()) {
                        a.put(ag.toString(), new HashSet<String>((Collection<? extends String>) ((Map) restAns.get(ag)).get("services")));
                    }
                    return a;
                }
            }
        }
        return super.getDF();
    }  
    
    
    /*Map<String, RestAgArch> archCache = new HashMap<>();
    
    protected RestAgArch getRestAgArch(String agName) {
        return archCache.computeIfAbsent(agName, k -> {
            AgArch arch = BaseCentralisedMAS.getRunner().getAg(agName).getFirstAgArch();
            while (arch != null) {
                if (arch instanceof RestAgArch) {
                    return (RestAgArch)arch;
                }
                arch = arch.getNextAgArch();
            }
            return null;
        });
    }*/

}

