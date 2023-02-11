package jacamo.rest;

import java.net.InetAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import com.google.gson.Gson;

import jacamo.platform.DefaultPlatformImpl;
import jacamo.rest.config.RestAgArch;
import jacamo.rest.config.RestAppConfig;
import jason.infra.local.RunLocalMAS;
import jason.runtime.RuntimeServicesFactory;


public class JCMRest extends DefaultPlatformImpl {

    private static JCMRest singleton = null;
    public  static JCMRest getJCMRest() {
        return singleton;
    }

    protected transient Logger logger  = Logger.getLogger(JCMRest.class.getName());

    protected HttpServer restHttpServer = null;
    protected URI restServerURI = null;
    protected String mainRest = null;
    protected String registerURL = null;

    protected Map<String, Map<String,Object>> ans = new TreeMap<>();     // agent name service (agent name -> ( prop -> value )* )
    protected Map<String, Map<String,Object>> mdCache = new HashMap<>(); // meta data cache

    public String getRestHost() {
        if (restServerURI == null)
            return null;
        else
            return restServerURI.toString();
    }

    public boolean isMain() {
        return mainRest == null;
    }
    public String getMainRest() {
        return mainRest;
    }

    public String getURLForRegister() {
        if (registerURL == null) {
            return restServerURI.toString();
        } else {
            return registerURL;
        }
    }

    @Override
    public void init(String[] args) throws Exception {

        // change the runtimeservices
        RuntimeServicesFactory.set( new JCMRuntimeServices(this) );

        // adds RestAgArch as default ag arch when using this platform
        RuntimeServicesFactory.get().registerDefaultAgArch(RestAgArch.class.getName());

        int restPort = 3280;

        // Used when deploying on heroku
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            restPort = 8080;
        } else {
            restPort = Integer.parseInt(webPort);
        }

        if (args.length > 0) {
            String la = "";
            for (String a: args[0].split(" ")) {
                if (la.equals("--rest-port"))
                    try {
                        restPort = Integer.parseInt(a);
                    } catch (Exception e) {
                        logger.warning("The argument for restPort is not a number.");
                    }

                if (la.equals("--restPort"))
                    try {
                        logger.warning("********* use parameter --rest-port in place of --restPort");
                        restPort = Integer.parseInt(a);
                    } catch (Exception e) {
                        logger.warning("The argument for restPort is not a number.");
                    }

                if (la.equals("--connect")) {
                    mainRest = a;
                }
                if (la.equals("--registerURL")) {
                    registerURL = a;
                    if (!registerURL.endsWith("/"))
                        registerURL += "/";
                }

                if (la.equals("--hostname")) {
                    logger.warning("********* use parameter --registerURL in place of --hostname");
                    registerURL = a;
                    if (!registerURL.endsWith("/"))
                        registerURL += "/";
                }

                la = a;
            }
        }

        restHttpServer = startRestServer(restPort,0);
        singleton = this;

        new ClearDeadAgents().start();

        logger.info("JaCaMo Rest API is running on "+restServerURI
                + (registerURL == null ? "" : " (as "+registerURL+")")
                + (mainRest == null ? "." : ", connected to "+mainRest+".")  );
    }

    @Override
    public void stop() {
        logger.info("Stopping ...");

        logger.info("Stopping http server...");
        if (restHttpServer != null)
            try {
                restHttpServer.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        restHttpServer = null;
        logger.info("stopped!");
    }

    public HttpServer startRestServer(int port, int tryc) {
        if (tryc > 20) {
            logger.warning("Error starting rest server!");
            return null;
        }
        try {
            restServerURI = UriBuilder.fromUri("http://"+InetAddress.getLocalHost().getHostAddress()+"/").port(port).build();

            RestAppConfig rc = new RestAppConfig();

            // get a server from factory
            HttpServer s = GrizzlyHttpServerFactory.createHttpServer(restServerURI, rc);
            return s;
        } catch (javax.ws.rs.ProcessingException e) {
            logger.info("trying next port for rest server "+(port+1)+". e="+e);
            return startRestServer(port+1,tryc+1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //
    // ANS services
    //

    Client client = ClientBuilder.newClient();

    public void registerAgent(String agentName, Map<String,Object> metadata) {
        if (metadata == null)
            metadata = new HashMap<>();

        ans.put(agentName, metadata);

        if (!isMain()) {
            // register also in main
            synchronized (client) {
                // add new entry
                client
                    .target(mainRest)
                    .path("/agents/"+agentName)
                    .queryParam("only_wp", "true")
                    .queryParam("force", "true")
                    .request(MediaType.APPLICATION_JSON)
                    .accept(MediaType.TEXT_PLAIN)
                    .post(Entity.json(new Gson().toJson( metadata )));
            }
        }
    }

    public boolean deregisterAgent(String agentName) {
        if (!isMain()) {
            synchronized (client) {
                client
                        .target(mainRest)
                        .path("/agents/"+agentName)
                        .request()
                        .delete();
            }
        }
        return ans.remove(agentName) != null;
    }

    public boolean hasMetaDataCache(String agentName) {
        return mdCache.containsKey(agentName);
    }
    public void clearAgentMetaDataCache(String agentName) {
        mdCache.remove(agentName);
    }

    @SuppressWarnings("unchecked")
    public Map<String,Object> getAgentMetaData(String agentName) {
        if (ans.get(agentName) != null)
            return ans.get(agentName);
        if (!isMain()) {
            Map<String,Object> md = mdCache.get(agentName);
            if (md != null)
                return md;
            synchronized (client) {
                Response response = client
                        .target(mainRest)
                        .path("/agents/"+agentName)
                        .request(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_PLAIN)
                        .get();
                if (response.getStatus() == 200) {
                    md = response.readEntity(Map.class);
                    mdCache.put(agentName, md);
                    return md;
                }
            }
        }
        return null;
    }

    public Map<String,Map<String,Object>> getWP() throws Exception {
        Map<String,Map<String,Object>> data = new HashMap<>();
        for (String ag : ans.keySet()) {
            Map<String,Object> md = ans.getOrDefault(ag, new HashMap<>());
            data.put(ag, md);
        }
        return data;
    }

    class ClearDeadAgents extends Thread {
        @Override
        public void run() {
            while (!RuntimeServicesFactory.get().isRunning()) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            Client client = ClientBuilder.newClient();
            while (RuntimeServicesFactory.get().isRunning()) {
                try {
                    sleep(4000);

                    // for all remote agents, test if they are running
                    for (String ag : new HashSet<String>(ans.keySet())) {
                        Map<String,Object> md = ans.get(ag);
                        if (md != null && (boolean)md.getOrDefault("remote", false)) {

                            //System.out.println("** remote "+ag+ " "+md.get("uri").toString());
                            String dead = null;
                            try {
                                //Response response =
                                client
                                        .target(md.get("uri").toString())
                                        .path("/")
                                        .request()
                                        .get();
                                /*if (response.getStatus() != 200) {
                                    dead = "bad status "+response.getStatus();
                                }*/
                            } catch (Exception e) {
                                dead = e.getMessage();
                            }
                            if (dead != null) {
                                logger.info("agent "+ag+" ("+md.get("uri")+") seems not running anymore, removing from ANS! "+dead);

                                ans.remove(ag);
                                RunLocalMAS.getRunner().delAg(ag); // to remove entries in DF
                            }
                        }
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
