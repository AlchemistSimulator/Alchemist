---

title: "With Smart Cameras"

---

### Prerequisites
This guide assumes you already know {{anchor('the Alchemist metamodel', 'The Alchemist Simulator metamodel')}} and {{anchor('how to write simulations in YAML', 'Writing Alchemist simulations')}}.

### Smartcam
A smartcam is a camera able to detect objects of interest and to communicate with other smartcameras.
In many cases it is also assumed to be mounted on a drone as to be able to freely move around in the environment.
In Alchemist smartcams are simulated as _Nodes_ equipped with specific _Reactions_ defining their capabilities and behaviour.
Single capabilities are expressed as _Actions_.

#### Vision
The most basic example of a camera is a _Node_ containing a _Reaction_ with the _See_ action. Note: the _See_ action currently
only works in 2D environments supporting euclidean geometry, for example _Continuous2DEnvironment_ and _Rectangular2DEnvironment_.
The _See_ action requires 3 parameters to be defined in this order: the distance of the field of view, its angle in degrees,
and the name of the _Molecule_ which will contain the ouput, namely a list of the nodes contained in the field of view which is updated
each time the action is triggered. Optionally a fourth parameter can be defined in order to filter the output. Such parameter is expected to
be the name of a _Molecule_ which has to be contained in a _Node_ for it to be visible, e.g. if it is "wanted" then only nodes containing
a molecule named "wanted" will be seen.

#### Movement
The ability to move can be defined using movement actions such as _MoveToTarget_ or _FollowAtDistance_.
_MoveToTarget_ expects 2 parameters such as the name of the molecule containing the target's position and the movement speed.
_FollowAtDistance_ requires the name of the molecule containing the target's position, the distance to mantain from the target, 
and the movement speed.

#### Rotation
The action _HeadTowardTarget_ can be used to instruct cameras to always face the specified target, it requires
2 parameters such as the name of the molecule containing the target's position and the angular speed in degrees.
The _Spin_ action only requires the angular speed and will make the camera spin around itself like a radar.

#### Algorithm
Without defining an algorithm the cameras wouldn't do anything interesting. Algorithms can be definied in a moltitude of different
ways. Below there's an example of a basic algorithm defined only with _Reactions_, _Conditions_ and _Actions_.
It is advisable to use real programming languages such as {{anchor('Protelis', 'Alchemist Protelis incarnation')}} to write more elaborate algorithms.
In this regard you'd want to use the protelis incarnation, make cameras be _ProtelisNode_, and to make sure
that the input and output molecules' names of the actions match the ones used by the protelis program.

### Complete example of a simulation
We start by writing a basic yaml configuration to place 20 potential targets and 10 cameras in a 400 x 400 rectangular environment:

```yaml
incarnation: protelis

variables:
  NumHumans: &NumHumans
    formula: 20
  NumCameras: &NumCameras
    formula: 10
  EnvironmentSize: &EnvironmentSize
    formula: 400
  HalfEnvironmentSize: &HalfEnvironmentSize
    formula: EnvironmentSize / 2
  EnvironmentOrigin: &EnvironmentOrigin
    formula: -HalfEnvironmentSize


environment:
  type: Rectangular2DEnvironment
  parameters: [*EnvironmentSize, *EnvironmentSize]

displacements:
  - in: # possible targets
      type: Rectangle
      parameters: [*NumHumans, *EnvironmentOrigin, *EnvironmentOrigin, *EnvironmentSize, *EnvironmentSize]
    nodes:
      type: CircleNode
      parameters: [1]
  - in: # cameras
      type: Rectangle
      parameters: [*NumCameras, *EnvironmentOrigin, *EnvironmentOrigin, *EnvironmentSize, *EnvironmentSize]
    nodes:
      type: ProtelisNode
```

Then we give cameras the ability to see other nodes. The _See_ action requires the distance and angle of the field of view. Each time it is triggered, it writes all the nodes seen in the molecule _vision_.

```yaml
incarnation: protelis

variables:
  NumHumans: &NumHumans
    formula: 20
  NumCameras: &NumCameras
    formula: 10
  EnvironmentSize: &EnvironmentSize
    formula: 400
  HalfEnvironmentSize: &HalfEnvironmentSize
    formula: EnvironmentSize / 2
  EnvironmentOrigin: &EnvironmentOrigin
    formula: -HalfEnvironmentSize
  CameraFoVAngle: &CameraFoVAngle
    formula: 60
  CameraFoVDistance: &CameraFoVDistance
    formula: 20

environment:
  type: Rectangular2DEnvironment
  parameters: [*EnvironmentSize, *EnvironmentSize]

programs:
  - &Camera
    - time-distribution: 1
      type: ChemicalReaction
      actions:
        - type: See
          parameters: [*CameraFoVDistance, *CameraFoVAngle, vision]

displacements:
  - in: # possible targets
      type: Rectangle
      parameters: [*NumHumans, *EnvironmentOrigin, *EnvironmentOrigin, *EnvironmentSize, *EnvironmentSize]
    nodes:
      type: CircleNode
      parameters: [1]
  - in: # cameras
      type: Rectangle
      parameters: [*NumCameras, *EnvironmentOrigin, *EnvironmentOrigin, *EnvironmentSize, *EnvironmentSize]
    nodes:
      type: ProtelisNode
    programs:
      - *Camera
```

In this example we make humans move randomly. We also want to randomly toggle a _wanted_ molecule in them, as to have a way to mark interesting targets that needs to be followed by the cameras. Every 50 seconds each human has a 1% probability to become _wanted_.

```yaml
incarnation: protelis

variables:
  HumanSpeed: &HumanSpeed
    formula: 1
  NumHumans: &NumHumans
    formula: 20
  NumCameras: &NumCameras
    formula: 10
  EnvironmentSize: &EnvironmentSize
    formula: 400
  HalfEnvironmentSize: &HalfEnvironmentSize
    formula: EnvironmentSize / 2
  EnvironmentOrigin: &EnvironmentOrigin
    formula: -HalfEnvironmentSize
  CameraFoVAngle: &CameraFoVAngle
    formula: 60
  CameraFoVDistance: &CameraFoVDistance
    formula: 20

environment:
  type: Rectangular2DEnvironment
  parameters: [*EnvironmentSize, *EnvironmentSize]

programs:
  - &Human
    - time-distribution: 1
      type: ChemicalReaction
      actions:
        - type: ZigZagMove
          parameters: [*HalfEnvironmentSize, *HumanSpeed]
    - time-distribution: 0.02
      type: ChemicalReaction
      actions:
        - type: RandomlyToggleMolecule
          parameters: [wanted, true, 0.01]
  - &Camera
    - time-distribution: 1
      type: ChemicalReaction
      actions:
        - type: See
          parameters: [*CameraFoVDistance, *CameraFoVAngle, vision]

displacements:
  - in: # possible targets
      type: Rectangle
      parameters: [*NumHumans, *EnvironmentOrigin, *EnvironmentOrigin, *EnvironmentSize, *EnvironmentSize]
    nodes:
      type: CircleNode
      parameters: [1]
    programs:
      - *Human
  - in: # cameras
      type: Rectangle
      parameters: [*NumCameras, *EnvironmentOrigin, *EnvironmentOrigin, *EnvironmentSize, *EnvironmentSize]
    nodes:
      type: ProtelisNode
    programs:
      - *Camera
```

It is time for cameras to identify _wanted_ targets so we add a _ExtractClosestVisibleNodeAtDistance_ action in order to select the closest node to the center of the field of view.
We also specify the _wanted_ molecule in the _See_ action in order to filter only the interesting nodes.
Then, at the condition that a _target_ is found, we want the cameras to start following it, so we add the actions _FollowAtDistance_ and _HeadTowardTarget_.

```yaml
incarnation: protelis

variables:
  HumanSpeed: &HumanSpeed
    formula: 1
  NumHumans: &NumHumans
    formula: 20
  NumCameras: &NumCameras
    formula: 10
  EnvironmentSize: &EnvironmentSize
    formula: 400
  HalfEnvironmentSize: &HalfEnvironmentSize
    formula: EnvironmentSize / 2
  EnvironmentOrigin: &EnvironmentOrigin
    formula: -HalfEnvironmentSize
  CameraFoVAngle: &CameraFoVAngle
    formula: 60
  CameraFoVDistance: &CameraFoVDistance
    formula: 20
  CameraSpeed: &CameraSpeed
    formula: 1
  CameraAngularSpeed: &CameraAngularSpeed
    formula: 3
  CameraDistanceFromTarget: &CameraDistanceFromTarget
    formula: 2 * CameraFoVDistance / 3

environment:
  type: Rectangular2DEnvironment
  parameters: [*EnvironmentSize, *EnvironmentSize]

programs:
  - &Human
    - time-distribution: 1
      type: ChemicalReaction
      actions:
        - type: ZigZagMove
          parameters: [*HalfEnvironmentSize, *HumanSpeed]
    - time-distribution: 0.02
      type: ChemicalReaction
      actions:
        - type: RandomlyToggleMolecule
          parameters: [wanted, true, 0.01]
  - &Camera
    - time-distribution: 1
      type: ChemicalReaction
      actions:
        - type: See
          parameters: [*CameraFoVDistance, *CameraFoVAngle, vision, wanted]
        - type: ExtractClosestVisibleNodeAtDistance
          parameters: [*CameraDistanceFromTarget, vision, target]
    - time-distribution: 1
      type: ChemicalReaction
      conditions:
        - type: ContainsMolecule
          parameters: [target]
      actions:
        - type: HeadTowardTarget
          parameters: [target, *CameraAngularSpeed]
        - type: FollowAtDistance
          parameters: [target, *CameraDistanceFromTarget, *CameraSpeed]

displacements:
  - in: # possible targets
      type: Rectangle
      parameters: [*NumHumans, *EnvironmentOrigin, *EnvironmentOrigin, *EnvironmentSize, *EnvironmentSize]
    nodes:
      type: CircleNode
      parameters: [1]
    programs:
      - *Human
  - in: # cameras
      type: Rectangle
      parameters: [*NumCameras, *EnvironmentOrigin, *EnvironmentOrigin, *EnvironmentSize, *EnvironmentSize]
    nodes:
      type: ProtelisNode
    programs:
      - *Camera
```

To conclude, we want the cameras to explore randomly and spin when no targets are detected. For this purpose we add another reaction with a _Spin_ and _ZigZagMove_ and with an _Else_ condition which will be triggered when the conditions of the other reactions are not valid.

```yaml
incarnation: protelis

variables:
  HumanSpeed: &HumanSpeed
    formula: 1
  NumHumans: &NumHumans
    formula: 20
  NumCameras: &NumCameras
    formula: 10
  EnvironmentSize: &EnvironmentSize
    formula: 400
  HalfEnvironmentSize: &HalfEnvironmentSize
    formula: EnvironmentSize / 2
  EnvironmentOrigin: &EnvironmentOrigin
    formula: -HalfEnvironmentSize
  CameraFoVAngle: &CameraFoVAngle
    formula: 60
  CameraFoVDistance: &CameraFoVDistance
    formula: 20
  CameraSpeed: &CameraSpeed
    formula: 1
  CameraAngularSpeed: &CameraAngularSpeed
    formula: 3
  CameraDistanceFromTarget: &CameraDistanceFromTarget
    formula: 2 * CameraFoVDistance / 3

environment:
  type: Rectangular2DEnvironment
  parameters: [*EnvironmentSize, *EnvironmentSize]

programs:
  - &Human
    - time-distribution: 1
      type: ChemicalReaction
      actions:
        - type: ZigZagMove
          parameters: [*HalfEnvironmentSize, *HumanSpeed]
    - time-distribution: 0.02
      type: ChemicalReaction
      actions:
        - type: RandomlyToggleMolecule
          parameters: [wanted, true, 0.01]
  - &Camera
    - time-distribution: 1
      type: ChemicalReaction
      actions:
        - type: See
          parameters: [*CameraFoVDistance, *CameraFoVAngle, vision, wanted]
        - type: ExtractClosestVisibleNodeAtDistance
          parameters: [*CameraDistanceFromTarget, vision, target]
    - time-distribution: 1
      type: ChemicalReaction
      conditions:
        - type: ContainsMolecule
          parameters: [target]
      actions:
        - type: HeadTowardTarget
          parameters: [target, *CameraAngularSpeed]
        - type: FollowAtDistance
          parameters: [target, *CameraDistanceFromTarget, *CameraSpeed]
    - time-distribution: 1
      type: ChemicalReaction
      conditions:
        - type: Else
          parameters:
      actions:
        - type: Spin
          parameters: [*CameraAngularSpeed]
        - type: ZigZagMove
          parameters: [*HalfEnvironmentSize, *CameraSpeed]

displacements:
  - in: # possible targets
      type: Rectangle
      parameters: [*NumHumans, *EnvironmentOrigin, *EnvironmentOrigin, *EnvironmentSize, *EnvironmentSize]
    nodes:
      type: CircleNode
      parameters: [1]
    programs:
      - *Human
  - in: # cameras
      type: Rectangle
      parameters: [*NumCameras, *EnvironmentOrigin, *EnvironmentOrigin, *EnvironmentSize, *EnvironmentSize]
    nodes:
      type: ProtelisNode
    programs:
      - *Camera
```

### Further references
[Lukas Esterle, Peter R. Lewis\
Online Multi-object k-coverage with Mobile Smart Cameras\
In Proceedings of the International Conference on Distributed Smart Cameras (ICDSC). Nominated for best paper. 2017.](https://doi.org/10.1145/3131885.3131909)