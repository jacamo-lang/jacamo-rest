start.

+start
  <- .print("Locks door")
  .broadcast(tell, locked(door)).


+!~locked(door)[source(claustrophobe)]
  <- .print("Unlocks door")
  .broadcast(tell, ~locked(door)).

+!locked(door)[source(paranoid)]
  <- .print("Locks door")
  .broadcast(tell, locked(door)).
