---

title: Generating navigation meshes

---
Alchemist is capable of generating navigation meshes of bidimensional environments with obstacles. A navigation mesh is a collection of two-dimensional convex polygons representing which areas of an environment are traversable by agents (namely, walkable areas). Since convex polygons are generated, pedestrians can freely walk around within these areas, as it is guaranteed that no obstacle will be found. Here is an image of a navigation mesh (we'll see in a moment why some areas have different colors):

![alt text](https://user-images.githubusercontent.com/52574842/73022311-a4f01480-3e29-11ea-9f04-62d3d9da5faa.png)

There are various algorithms capable of generating navigation meshes, the most common ones make use of triangulation (hence, they divide the walkable area in triangle regions). The one implemented in Alchemist is the DEACCON algorithm (Decomposition of Environments for the Creation of Convex-region Navigation-meshes), which is capable to generate more complex polygons. For a complete description of the algorithm please refer to the [related paper](https://www.aaai.org/Papers/AIIDE/2008/AIIDE08-029.pdf). The algorithm implemented in Alchemist is a slight simplification of the one described there. Here is a brief description of how it works.

### Premises

Before diving into the algorithm, please be aware of the following:
- the deaccon algorithm does not guarantee the coverage of 100% of the walkable area;
- the algorithm is only capable to deal with **convex polygonal obstacles**. Concave ones can be decomposed into convex meshes fairly easily, whereas for curves bounding boxes can be used, eventually arbitrarily oriented minimum bounding boxes;
- the implemented algorithm is designed for rectangular shaped bidimensional environments with euclidean geometry and double precision coordinates.

Let us look at the signature of the only method of the `Deaccon2D` class:
```kotlin
Deaccon2D.generateNavigationMesh(envStart: java.awt.geom.Point2D, envWidth: Double, envHeight: Double, envObstacles: Collection<java.awt.Shape>): Collection<ConvexPolygon>
```
The inputs are:
- `envStart`: the starting point of the rectangular environment. In this context, the starting point is the one point of the rectangle which allows to describe the environment with positive width and height.
- `envWidth`: width of the environment (only positive)
- `envHeight`: height of the environment (only positive)
- `envObstacles`: obstacles of the environment (only convex polygonal obstacles are admitted)

The output, as mentioned before, is a `Collection<ConvexPolygon>`.

### How it works

The algorithm operates through the following phases:

- Seeding: a certain number of seeds is planted in the environment. Each seed is a square-shaped region that will grow maintaining a convex shape.

![alt text](https://user-images.githubusercontent.com/52574842/73022156-4fb40300-3e29-11ea-8846-56dd35484f71.png)

- Growing: planted seeds are extended until possible (i.e. until they are in contact with an obstacle or another seed on each side). The algorithm is capable to deal with non axis-aligned obstacles as well.

![alt text](https://user-images.githubusercontent.com/52574842/73022203-5f334c00-3e29-11ea-9c8e-c4aef07ab168.png)

- Clean-up: adjacent regions are combined if the resulting polygon is still convex.

![alt text](https://user-images.githubusercontent.com/52574842/73022234-6d816800-3e29-11ea-8165-9d0059f73d38.png)

As the pictures show, performing only the above mentioned phases may sometimes result in a poor coverage of the walkable area. In order to avoid this, another operation called active seeding is performed. Basically, other seeds are generated, but much smaller:

![alt text](https://user-images.githubusercontent.com/52574842/73022289-973a8f00-3e29-11ea-99fe-9eec63e31556.png)

Then the three phases described above are repeated for these newly generated seeds. Such seeds are called active because their purpose is to cover the holes resulting from the first three phases. Here's the final result:

![alt text](https://user-images.githubusercontent.com/52574842/73022311-a4f01480-3e29-11ea-9f04-62d3d9da5faa.png)

### Parameters of the algorithm

The only parameter the user needs to specify when instancing a `Deaccon2D` object is the number of seeds to generate in the first phase of the algorithm (defaults to 100). Note that the area that will initially be covered by seeds DO NOT grow with the number of seeds. In fact, the proportion of the environment's area to be covered in the initial seeding phase is fixed. By changing the number of seeds, what changes is their dimension. With their total area fixed, generating a lower number of seeds will result in coarse-grained initial seeds. Whereas generating a lot of seeds will result in fine-grained seeds. Ultimately, altering this parameter affects the grain of the initial seeds. This is highly dependent on the particular environment (if you have a single room coarse-grained seeds are the best, whereas if you have a whole building you'd better go with fine-grained ones or the resulting navigation mesh may be poor). It may be advisable to try different quantity of seeds and pick the best trade-off between coverage of walkable area and time. Generally speaking, the more detailed your environment is, the higher this quantity should be.

### Drawing navigation meshes

A simple effect capable to obtain and draw a navigation mesh of the current environment is available as well. If you wish to draw a navigation mesh of a non limited environment (or a non rectangular shaped environment), you can easily specify a rectangular region inside your environment to consider for the generation of the navigation mesh.