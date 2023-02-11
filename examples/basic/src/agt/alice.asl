// Agent sample_agent in project jcmrest

/* Initial beliefs and rules */

price(banana,10).

/* Initial goals */

!start.
!send.
/* Plans */

+!start : true
   <- .print("hello world.");
      .df_register(vender(banana));
      .df_register(vender(banana));
      .wait(1000);
      .df_register(vender(abacaxi));
      .wait(1000);
      .df_deregister(vender(abacaxi));
      .wait(2000);
      .print("adding abacaxi again....");
      .df_register(vender(abacaxi));
      .wait(2000);
      .print("removing abacaxi again....");
      .df_deregister(vender(abacaxi));
   .

+!send
   <- .send(marcos,signal,hello);
      .send(marcos,tell,vl(10,"kk"));
   .

+hello[source(A)] <- .print("I received hello from ",A).

+hello[source(A)] <- .print("I received hello from ",A).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
