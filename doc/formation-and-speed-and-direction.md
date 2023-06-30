# Formation and Speed and Direction

```text
+--------------------------------------+
|  ========= formation front =======>  | IDK what does this means but this creates front car. front car drives formation.
+------------------1-------------------+ Formation.direction: 1 if opposite
|  <====== formation direction ======  |<==== this is almost fixed
+-----0------+-----1------+------1-----+ FormationEntry.direction: 1 if opposite
| <==ride=== | ===ride==> | ===ride==> | the direction of bogie the player will sit on.
+-----0------+-----1------+------0-----+ TrainState.TrainStateType.Direction: 1 if opposite
| <=entity== | <=entity== | ==entity=> |<==== this is fixed
+-----0------+-----1------+------2-----+ FormationEntry index
```
