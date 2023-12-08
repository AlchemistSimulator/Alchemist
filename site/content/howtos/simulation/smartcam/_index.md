+++
title = "Smart cameras and drones"
weight = 5
tags = ["drone", "smart camera"]
summary = "Simulate robots with a field of view."
+++

{{% youtube yuaY_8Vr3oc %}}

## Prerequisites
This guide assumes you already know
[the Alchemist metamodel](/explanation/metamodel)
and that you keep the [YAML reference](/reference/yaml) at your fingertips.

## Smartcam
A smartcam is a camera able to detect objects of interest and to communicate with other smartcameras.
In many cases it is also assumed to be mounted on a drone as to be able to freely move around in the environment.
In Alchemist, smartcams are simulated as
{{% api class=Node %}}s
equipped with specific
{{% api class=Reaction %}}s
defining their capabilities and behaviour.
Single capabilities are expressed as
{{% api class=Action %}}s.

### Vision
The most basic example of a camera is a
{{% api class=Node %}}
containing a
{{% api class=Reaction %}}
with the
{{% api package="model.actions" class=CameraSee %}}
action.

{{% notice warning %}}
Note: the
{{% api package="model.actions" class=CameraSee %}}
action currently
only works in 2D environments supporting euclidean geometry,
for example
{{% api package="model.environments" class=Continuous2DEnvironment %}}.
{{% /notice %}}

{{% api package="model.actions" class=CameraSee %}}
requires the following parameters:
1. the distance of the field of view,
2. its angle in degrees, and
3. the name of the {{% api class=Molecule %}} which will contain the ouput,
   namely, the list of nodes contained in the field of view,
   which is updated every time the action is triggered,
4. optionally, a fourth parameter can be defined in order to filter the output,
   such parameter is expected to be the name of a {{% api class=Molecule %}}
   which has to be contained in a {{% api class=Node %}} for it to be visible,
   e.g., if it is `wanted` then only nodes containing a molecule named `wanted` will be seen.

### Movement
The ability to move can be defined using movement actions such as
{{% api package="model.actions" class=MoveToTarget %}}
or
{{% api package="model.actions" class=FollowAtDistance %}}.

{{% api package="model.actions" class=MoveToTarget %}}
expects 2 parameters:
1. the name of the molecule containing the target's position, and
2. the movement speed.

{{% api package="model.actions" class=FollowAtDistance %}} requires:
1. the name of the molecule containing the target's position,
2. the distance to mantain from the target, and
3. the movement speed.

### Rotation
{{% api package="model.physics.actions" class=HeadTowardTarget %}}
can be used to instruct cameras to always face the specified target.

{{% api package="model.physics.actions" class=Spin %}}
only requires the angular speed and will make the camera spin around itself like a radar.

### Other behavior
Without defining an algorithm the cameras wouldn't do anything interesting.
Algorithms can be definied in a moltitude of different ways, depending on the incarnation.
Usually, camera readings are used as sensors that get read by,
e.g., Protelis or Scafi programs.

### Further references
[Lukas Esterle, Peter R. Lewis\
Online Multi-object k-coverage with Mobile Smart Cameras\
In Proceedings of the International Conference on Distributed Smart Cameras (ICDSC). Nominated for best paper. 2017.](https://doi.org/10.1145/3131885.3131909)