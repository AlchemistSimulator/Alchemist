---

title: "Simulation of indoor environments"

---

In order to load map of indoor environment, you can use {{ anchor('ImageEnvironment') }}.

ImageEnvironment loads the map as raster image from file, interpreting the black pixels as obstacles
(wall-like areas not accessible to nodes). Color of pixels that represents obstacles can be set to
every color with relative constructor's parameter, black color is only the default.

In this example the image with the map is in the classpath in the folder `images`. Using a network-model 
like {{ anchor('ObstaclesBreakConnection') }} the connections between nodes are created only if there
aren't obstacles between them. The nodes moves with a brownian move and never they moves across the walls.
```yaml
incarnation: protelis
environment:
  type: ImageEnvironment
  parameters: [images/foo.png]
  
  network-model: { type: ObstaclesBreakConnection, parameters: [100] }
  
  program-pools:
    - move: &move
      - time-distribution: { type: ExponentialTime, parameters: [1] }
        type: Event
        actions:
          - { type: BrownianMove, parameters: [1] }
  displacements:
    - in: { type: Circle, parameters: [100, 700, 700, 300] }
      programs: 
        - *move
```

The following image is a snapshot of the previous simulation. It is possible see how there aren't link
between nodes across walls.

![Simulation result]({{ 'assets/media/usage/indoor_simulation.png'|asset }})