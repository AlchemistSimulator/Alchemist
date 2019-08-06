---

title: "Simulation of indoor environments"

---

In order to load the map of the indoor environment, you can use the {{ anchor('ImageEnvironment') }}.

ImageEnvironment loads the map as raster image from file, interpreting the black pixels as obstacles
(areas that cannot be accessed by nodes like wall). 
The color of the pixel that represent obstacle can be set as constructor's parameter of the environment.

In this example the image with the map is in theclasspath in the folder `images`:
```yaml
incarnation: protelis
environment:
  type: ImageEnvironment
  parameters: [images/foo.png]
```