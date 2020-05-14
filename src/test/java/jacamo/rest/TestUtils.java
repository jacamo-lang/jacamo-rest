package jacamo.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import cartago.CartagoService;
import jacamo.infra.JaCaMoLauncher;
import jason.JasonException;

public final class TestUtils {
    static Boolean systemRunning = false;
    static URI uri = null;
    
    synchronized public static URI launchSystem(String jcm) {
        if (!systemRunning) {
            try {
                // Launch jacamo and jacamo-rest running test0.jcm
                new Thread() {
                    public void run() {
                        String[] arg = { jcm };
                        try {
                            JaCaMoLauncher.main(arg);
                        } catch (JasonException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
    
                // wait for start of jacamo rest
                while (uri == null) {
                    System.out.println("waiting for rest server to start ....");
                    if (JCMRest.getRestHost() != null)
                        uri = UriBuilder.fromUri(JCMRest.getRestHost()).build();
                    else
                        Thread.sleep(1000);
                }
                // wait for agents (a MAS should have at least one agent)
                while ((JaCaMoLauncher.getRunner() == null) || (JaCaMoLauncher.getRunner().getNbAgents() == 0)) {
                    System.out.println("waiting for jacamo and agents to start...");
                    Thread.sleep(1000);
                }
                // wait for cartago and for workspaces (at least the workspace main should exist)
                while ((CartagoService.getNode() == null) || (CartagoService.getNode().getWorkspaces().size() <= 0)) {
                    System.out.println("waiting for cartago...");
                    Thread.sleep(1000);
                }

                // still give some time for workspaces and artifacts 
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            systemRunning = true;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> stopSystem()));
        }
        return uri;
    }
    
    public static void stopSystem() {
        JaCaMoLauncher.getRunner().finish(0, false); // do not stop the JVM
        while (JaCaMoLauncher.getRunner() != null) {
            System.out.println("waiting for jacamo to STOP ....");
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("JaCaMo stopped");
    }

}
