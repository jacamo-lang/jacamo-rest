!start.

+!start <- .send(alice,tell,hi(200)).

+hi(X)[source(S)] <- .print("hi ",X," from ",S).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
