---

title: Maps and GPS traces

---

Alchemist is equipped with the ability to load and simulate on real-world maps.
Navigation on maps can be done by using gps traces,
by moving along roads (Alchemist relies on [GraphHopper](https://www.graphhopper.com/) to provide directions),
by interpolating gps traces with on-the-road-movements,
or by ignoring the map information on just move as you would in a continuous space.

## Setting up a map environment

In order to run simulations on real world maps, an appropriate environment must be selected.

Currently, the only environment supporting maps is {{ anchor('OSMEnvironment') }}. 

The only mandatory parameter is the `path` of the file with the map to be loaded. Accepts OSM maps of any format (xml, osm, pbf). 
Is possible define other optional parameter like:

0. `approximation: Int` -> the amount of ciphers of the IEEE 754 encoded position that may be discarded when comparing two positions, allowing a quicker retrieval of the route between two position, since the cache may already contain a similar route which can be considered to be the same route, according to the level of precision determined by this value
0. `onStreets: Boolean` -> if true, the nodes will be placed on the street nearest to the desired position
0. `onlyOnStreets: Boolean` -> if true, the nodes which are too far from a street will be simply discarded. If false, they will be placed anyway, in the original position

The following example shows how to configure a simulation that loads data from an Openstreetmap file 
(OSM, XML and PBF formats are supported) located in the classpath folder `maps`:

```yaml
incarnation: protelis
environment:
  type: OSMEnvironment
  parameters: [maps/foo.pbf]
```

## Using GPS traces to displace nodes

GPS traces can be used to displace nodes on a map.
In order to set the initial position of the nodes with the first position of the GPS traces,
you need to use the displacement {{ anchor('FromGPSTrace') }}.
The displacement `FromGPSTrace` require the following parameters:

0. number of nodes to displace
0. file with the GPS traces to use
0. boolean that indicates if the list of GPS trace is cyclic.
   If `true` and the GPS traces are less than the number of nodes to be displaced,
   then the traces are cyclically re-used to displace nodes.
0. a {{ anchor('GPSTimeAlignment') }} to define how to align the time of all the GPS points of all GPS traces. 
    There are present several strategy in the package {{ anchor('it.unibo.alchemist.boundary.gpsload.api') }} 
    and introduced in the following subsection.
    
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

### Strategy to align time of GPS trace

The strategies available to align time of GPS trace are the following:

0. NoAlignment -> No alignment is performed.
0. AlignToFirstTrace -> Aligns all traces at the start time of the first trace. 
    If you have two traces, the first trace start with time = 2 and second point with time = 5,
    the second trace start with time = 4 and second point with time = 6,
    the result will be: 
     - first trace -> start with time = 0 and second point with time = 3
     - second trace -> start with time = 2 and second point with time = 4
0. AlignToSimulationTime -> Aligns all traces at the initial simulation time.
    If you have two traces, the first trace start with time = 2 and second point with time = 5,
    the second trace start with time = 4 and second point with time = 6,
    the result will be: 
     - first trace -> start with time = 0 and second point with time = 3
     - second trace -> start with time = 0 and second point with time = 2
0. AlignToTime -> Aligns the traces with the given time in seconds from Epoch. 
    All points before such time will be discarded. All points after the provided time will
    be shifted back. 
    If you have two traces, the first trace start with time = 2 and second point with time = 5,
    the second trace start with time = 4 and second point with time = 6, the given time is 3
    the result will be: 
     - first trace -> first point discarded and second point with time = 2
     - second trace -> start with time = 1 and second point with time = 3

## Navigation

As previous say there are several behavior in order to move the nodes in a environment with real-word map:

0. movement ignoring map information. A node move directly from start position to destination position:
    ![No map information]({{ 'assets/media/usage/no_map_information.png'|asset }})
0. movement by using map information. A node move from start position to destination position using
    intermediate position in order to follow streets and avoid obstacle (like building):
    ![Use map information]({{ 'assets/media/usage/use_map_information.png'|asset }})
0. movement by reproducing a GPS trace (action {{ anchor('ReproduceGPSTrace') }}). 
    A node start to the first position of the GPS trace, than move to the next
    position of the trace until arrive to the last. The movement from a position to another is direct with a straight line:
    ![Reproduce GPS trace]({{ 'assets/media/usage/reproduce_gps_trace.png'|asset }})
0. movement by interpolating the GPS trace with street data (action {{ anchor('GPSTraceWalker') }}). 
    A node start to the first position of the GPS trace, 
    than move to the next position of the trace until arrive to the last. The movement from a position to another
    use map information to define intermediate position in order to follow streets and avoid obstacle (like building):
    ![Interpolate GPS trace with street data]({{ 'assets/media/usage/interpolate_gps_trace_with_street_data.png'|asset }})

Both the actions {{ anchor('ReproduceGPSTrace') }} and {{ anchor('GPSTraceWalker') }} require:
 0. the file with the GPS traces
 0. a boolean that indicate if the list of GPS trace is cyclic
 0. the strategy to align the attribute `time` of the GPS points of the GPS traces

The following example show how it is possible define a program to follow GPS traces.

```yaml
programs:
  - time-distribution: 0.1
  type: Event
  actions:
    - type: ReproduceGPSTrace
      parameters: ["/vcmuser.gpx", false, "AlignToTime", 1365922800, false, false]
```
