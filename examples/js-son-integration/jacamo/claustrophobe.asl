+locked(door)[source(porter)]
  <- .print("Please, unlock the door.");
  -~locked(door)[source(porter)];
  .send(porter,achieve,~locked(door)).

+~locked(door)[source(porter)]
  <-
  -locked(door)[source(porter)];
  .print("Thanks for unlocking the door!").
