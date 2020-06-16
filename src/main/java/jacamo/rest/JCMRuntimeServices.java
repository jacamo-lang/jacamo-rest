package jacamo.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.curator.x.async.WatchMode;
import org.apache.zookeeper.CreateMode;

import jacamo.rest.config.RestAgArch;
import jason.architecture.AgArch;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.UnnamedVar;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.runtime.DelegatedRuntimeServices;
import jason.runtime.RuntimeServicesFactory;

class JCMRuntimeServices extends DelegatedRuntimeServices {
    public JCMRuntimeServices() {
        super(RuntimeServicesFactory.get());
    }

    @Override
    public void dfRegister(String agName, String service, String type) {
        try {
            if (type == null) type = "no-type";
            String node = JCMRest.JaCaMoZKDFNodeId+"/"+service+"/"+agName;
            if (JCMRest.getZKClient().checkExists().forPath(node) == null) {
                JCMRest.getZKClient().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(node, type.getBytes());
            } else {
                JCMRest.getZKClient().setData().forPath(node, type.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void dfDeRegister(String agName, String service, String type) {
        try {
            JCMRest.getZKClient().delete().forPath(JCMRest.JaCaMoZKDFNodeId+"/"+service+"/"+agName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public Collection<String> dfSearch(String service, String type) {
        Set<String> ags = new HashSet<>();
        try {
            if (JCMRest.getZKClient().checkExists().forPath(JCMRest.JaCaMoZKDFNodeId+"/"+service) != null) {
                for (String r : JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKDFNodeId+"/"+service)) {
                    ags.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ags;
    }
    
    @Override
    public void dfSubscribe(String agName, String service, String type) {
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
    }
    
    
    @Override
    public Collection<String> getAgentsNames() {
        // use ZK WP
        try {
            List<String> all = new ArrayList<>();
            for (String ag : JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKAgNodeId)) {
                all.add(ag);
            }
            return all;
        } catch (Exception e) {
            e.printStackTrace();
            return super.getAgentsNames();
        }
    }
    
    @Override
    public Map<String, Set<String>> getDF() {
        if (JCMRest.getZKHost() == null) {
            return super.getDF();
        } else {
            try {
                Map<String, Set<String>> commonDF = new HashMap<String, Set<String>>();

                for (String s : JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKDFNodeId)) {
                    for (String a : JCMRest.getZKClient().getChildren().forPath(JCMRest.JaCaMoZKDFNodeId + "/" + s)) {
                        commonDF.computeIfAbsent(a, k -> new HashSet<>()).add(s);
                    }
                }
                return commonDF;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }  
    
    Map<String, RestAgArch> archCache = new HashMap<>();
    
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
    }

}

