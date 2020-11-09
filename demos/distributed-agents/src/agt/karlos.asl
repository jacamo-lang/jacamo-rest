+hi(0)[source(S)]
   <- .print("last hi from ",S).
+hi(X)[source(S)]
   <- .print("hi ",X," from ",S);
      //.wait(1000);
      .send(S,signal,hi(X-1));
   .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
