# Basic Demo for JaCaMo REST

Run agent marcos with  `gradle marcos`

```
> Task :examples:basic:marcos
[NetworkListener] Started listener bound to [{0}]
[HttpServer] [{0}] Started.
[JCMRest] JaCaMo Rest API is running on http://192.168.1.201:8080/.
CArtAgO Http Server running on http://192.168.1.201:3273
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Jason Http Server running on http://192.168.1.201:3272
Moise Http Server running on http://192.168.1.201:3271
[Moise] OrgBoard testOrg created.
[Moise] scheme created: scheme1: scheme1 using artifact SchemeBoard
[Moise] group created: group1: group1 using artifact ora4mas.nopl.GroupBoard
[Cartago] Workspace testwks created.
[Cartago] artifact a: tools.Counter(10) at testwks created.
[Cartago] artifact b: tools.Counter(02) at testwks created.
[marcos] join workspace /main/testOrg: done
[marcos] join workspace /main/testwks: done
[marcos] focusing on artifact a (at workspace /main/testwks) using namespace default
[marcos] focus on a: done
[marcos] focusing on artifact b (at workspace /main/testwks) using namespace default
[marcos] focus on b: done
[marcos] focusing on artifact group1 (at workspace /main/testOrg) using namespace default
[marcos] focus on group1: done
[marcos] focusing on artifact testOrg (at workspace /main/testOrg) using namespace default
[marcos] focus on testOrg: done
[marcos] I am obliged to commit to mission1 on scheme1... doing so
[marcos] Ok do goal 2
[marcos] DEBUG: Creating group...
[marcos] something1
[marcos] something2
[marcos] I received hello from alice
[marcos] I received oi from bob
[marcos] I received hello from bob
[marcos] something3
[marcos] something4
```

and then run Bob and Alice with `gradle bob`

```
[JCMRest] JaCaMo Rest API is running on http://192.168.1.201:8081/, connected to http://192.168.1.201:8080.
CArtAgO Http Server running on http://192.168.1.201:3274
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Jason Http Server running on http://192.168.1.201:3275
[alice] hello world.
[bob] Banana sellers [marcos,alice]
[bob] all registered agents: [kk,df,bob,marcos,alice]
[alice] I received hello from bob
[bob] Banana sellers [marcos,alice]
[bob] Price of marcos is 42.093641732341055
[bob] Price of alice is 10
[bob] Price of marcos is 110.96628810011023
[bob] Price of alice is 10
[bob] alice is provider of vender(abacaxi)
[alice] adding abacaxi again....
[alice] removing abacaxi again....
```

in some machines, `localhost` does not work and the IP must be used. In this case, edit `bob.jcm` and replace  `localhost`  by the IP printed by the marcos execution.