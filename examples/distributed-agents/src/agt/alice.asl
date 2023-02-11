// Agent sample_agent in project distributed_agents

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start  <- .send(karlos, signal, hi(10)).

{ include("karlos.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
