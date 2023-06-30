# Formation and Speed and Direction

## Formation direction properties

The meaning of properties around formation

### Original RTM

```text
+--------------------------------------+
|  ========= formation front =======>  | IDK what does this means but this creates front car. front car drives formation.
+------------------1-------------------+ Formation.direction: 1 if opposite
|  <====== formation direction ======  |<==== this is almost fixed
+-----0------+-----1------+------1-----+ FormationEntry.direction: 1 if opposite
| ===ride==> | ===ride==> | ===ride==> | the direction of bogie the player will sit on. This is originally used as direction of train goes.
+-----0------+-----1------+------0-----+ TrainState.TrainStateType.Direction: 1 if opposite
| <=entity== | <=entity== | ==entity=> |<==== this is fixed
+-----0------+-----1------+------2-----+ FormationEntry index
```

### fixRTM changed

```text
+--------------------------------------+
|  ========= formation front =======>  | **This defines the direction the train goes. must be same as ride direction of control car. speed means this direction**
+------------------1-------------------+ Formation.direction: 1 if opposite
|  <====== formation direction ======  |<==== this is almost fixed
+-----0------+-----0------+------1-----+ FormationEntry.direction: 1 if opposite
| ===ride==> | <==ride=== | ===ride==> | the direction of bogie the player will sit on. **This might not be the direction of train goes. if go back, speed is negative.**
+-----0------+-----0------+------0-----+ TrainState.TrainStateType.Direction: 1 if opposite
| <=entity== | <=entity== | ==entity=> |<==== this is fixed
+-----0------+-----1------+------2-----+ FormationEntry index
```

## Things we need to when revere train's direction

- negative speed
- swap door bits

## Things we need to when reverse formation direction

- negative speed
