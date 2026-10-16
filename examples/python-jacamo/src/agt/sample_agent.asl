// Agent sample_agent in project python_jacamo

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start <- .df_register(sell(bananas)); .print("hello world.").

+vl(X)[source(A)] <- .print("value is ",X," from ",A).
+count(X)         <- .print("count is ",X).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
