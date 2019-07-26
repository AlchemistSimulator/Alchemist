---

title: Maps and GPS traces

---

To set a simulation that uses a map and possibly the GPS tracks,
three parameters of the configuration file must be appropriately defined:
0. Environment
0. Displacement
0. Program to follow the GPS traces

## Setting environment with map

In order to running simulation over real world map, you need to select an environment that support it.

Currently the only environment that support maps is {{ anchor('OSMEnvironment') }}. 

The only mandatory parameter is the path of the file with the map to be loaded.
For the optional parameters, refer to the documentation of the  environment
 
The following example show how to configure a simulation, that loads data from an Openstreetmap file 
(OSM, XML and PBF formats are supported) located in the classpath in the folder
`maps`:
```yaml
incarnation: protelis
environment:
  type: OSMEnvironment
  parameters: [maps/foo.pbf]
```

## Displacing nodes with GPS trace

In order to set the initial position of the nodes with the first position of the GPS traces,
you need to use the displacement {{ anchor('FromGPSTrace') }}. 
The displacement `FromGPSTrace` require the following parameters:

0. number of nodes to displace
0. file with the GPS traces to use
0. boolean that indicate if the list of GPS trace is cyclic.
   If `true` and the GPS traces are less than the number of nodes to displacing,
   then the first position of GPS traces are reused for more nodes
0. a {{ anchor('GPSTimeAlignment') }} to define how to align the time of all the GPS points of all GPS traces. 
    There are present several strategy in the package {{ anchor('it.unibo.alchemist.boundary.gpsload.api') }}

The following example places 1497 nodes with the first position of the GPS traces in the file `vcmuser.gpx`.
The list of GPS traces isn't cyclic.
The strategy to align time of all the GPS points is {{ anchor('AlignTime') }}
in order to consider only the GPS points with attribute `time` greater than the specified one
(in milliseconds from epoch).

```yaml
displacements:
  - in:
      type: FromGPSTrace
      parameters: [1497, "vcmuser.gpx", false, "AlignToTime", 1365922800, false, false]
```

## Follow GPS trace

In order to obtain a behavior that moves the nodes following the GPS traces, the program can use one of the
following types of actions:

0. {{ anchor('GPSTraceWalker') }} -> to follow the GPS trace using the roads
0. {{ anchor('ReproduceGPSTrace') }} -> to follow the GPS trace with straight way

Both the type of actions require:
 0. the file with the GPS traces
 0. a boolean that indicate if the list of GPS trace is cyclic
 0. the strategy to align the attribute `time` of the GPS points of the GPS traces

The following example show how is possible define a program to follow GPS traces.

```yaml
programs:
  - time-distribution: 0.1
  type: Event
  actions:
    - type: ReproduceGPSTrace
      parameters: ["/vcmuser.gpx", false, "AlignToTime", 1365922800, false, false]
```