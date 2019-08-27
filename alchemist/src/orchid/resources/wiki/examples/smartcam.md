---

title: "Simulation examples - Smart Cameras"

---

### Basis
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