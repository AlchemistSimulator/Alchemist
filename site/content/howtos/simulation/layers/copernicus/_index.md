+++
title = "Layers from gridded geographic data"
weight = 5
tags = ["layer", "layers", "data", "copernicus", "climate", "weather", "netcdf", "grib", "geo-spatial data"]
summary = "Feed real-world, time-varying gridded data (e.g., from Copernicus) into the simulation as layers"
+++

Real-world environmental data, such as river discharge, precipitation, temperature, or air quality,
is commonly distributed as *gridded time series*:
a sequence of time slices, each one holding the values of a variable on a latitude/longitude grid.
The [Copernicus](https://www.copernicus.eu/) data stores
([Climate](https://cds.climate.copernicus.eu/),
[Early Warning](https://ewds.climate.copernicus.eu/), and
[Atmosphere](https://ads.atmosphere.copernicus.eu/))
distribute this kind of data in the [NetCDF](https://www.unidata.ucar.edu/software/netcdf/)
and [GRIB](https://community.wmo.int/en/activity-areas/wis/grib-edition-1-and-2) formats.

The `alchemist-geogrids` module turns these files into [layers](/howtos/simulation/layers/):
{{% api package="model.layers" class="GriddedGeoLayer" %}} exposes, at every position,
the value of the data at the current simulation time,
interpolated in space between the grid points and in time between the slices.
Its specialization {{% api package="model.layers" class="DoubleGriddedGeoLayer" %}}
yields `Double`s with sensible defaults, and it is what most simulations need.
Nodes observing the layer are notified whenever its value at their position changes,
either because they move or because the simulation time advances.

These layers live on geographic coordinates,
so they require an environment whose positions are
{{% api class="GeoPosition" %}}s, such as the
[map environments](/howtos/simulation/environment/maps/).

The data backing a layer is provided as its `data` parameter:
a {{% api package="model.layers" class="GriddedGeoFiles" %}} reads it from local files of any provider,
while a {{% api package="model.layers" class="CopernicusData" %}} downloads it from a Copernicus data store.

## Mapping data time to simulation time

Data slices are timestamped with real-world instants, while simulations advance in simulation time units.
Two parameters, both written in ISO-8601, connect them:

* `timeScale` is the real-world duration of one simulation time unit (by default, `PT1H`: one hour);
* `timeOrigin` is the real-world instant corresponding to simulation time zero
  (by default, the first instant found in the data).

Before the first slice and after the last one, the layer holds the values of the first and of the last slice.
Outside the spatial extent of the grid, instead, no value is extrapolated, and querying the layer fails:
make sure that the data covers the area where nodes move.

## Reading local data files

If the data files are already available,
for instance because they were downloaded from the web interface of a data store,
`GriddedGeoFiles` reads them from the given `directory`,
with no network access and no credentials.
All the files in the directory must hold the same variable on the same grid,
and their time slices get merged in chronological order
(if several files hold the same instant, the slice of the first file is used).
The directory can be a path on the file system or the name of a directory among the classpath resources.

{{< code path="alchemist-geogrids/src/test/resources/copernicus/local-data.yml" >}}

The same scenario, written with the Kotlin DSL:

{{< code path="alchemist-geogrids/src/test/resources/copernicus/local-data.alchemist.kts" >}}

## Downloading data from a Copernicus data store

Given an `endpoint`, a `dataset`, and an `inputsFile`,
`CopernicusData` requests the data directly to a data store:
it submits the request, waits for the data store to prepare the data, downloads it,
and verifies its integrity before using it.
Downloads get cached on the local file system,
so that the data is downloaded only once and is then shared by all the simulations
(even running in parallel) requesting it.

{{< code path="alchemist-geogrids/src/test/resources/copernicus/datastore.yml" >}}

The same scenario, written with the Kotlin DSL:

{{< code path="alchemist-geogrids/src/test/resources/copernicus/datastore.alchemist.kts" >}}

The parameters of `CopernicusData` describing the request are:

* `endpoint`: the base URL of the data store API, e.g., `https://cds.climate.copernicus.eu/api`
  for the Climate Data Store, `https://ewds.climate.copernicus.eu/api` for the Early Warning Data Store,
  or `https://ads.atmosphere.copernicus.eu/api` for the Atmosphere Data Store;
* `dataset`: the identifier of the dataset, as shown in its page on the data store
  (e.g., `cems-glofas-historical`);
* `inputsFile`: a JSON file holding the request, passed as is to the data store,
  either a path on the file system or the name of a file among the classpath resources.

The easiest way to write the request is to compose it in the *Download* tab of the dataset page,
and to copy the `request` dictionary shown in the *API request* section at the bottom of the page,
as in the following example:

{{< code path="alchemist-geogrids/src/test/resources/copernicus/glofas-request.json" >}}

{{% notice note "Why a separate JSON file?" %}}
Requests of some datasets have a `type` field,
which would clash with the `type` keyword that Alchemist uses to
[load classes](/reference/yaml/#arbitrary-class-loading-system),
so the request cannot be written inline in the YAML file.
{{% /notice %}}

### Credentials

Data stores require an account and the acceptance of the dataset license.
`CopernicusData` reads the API token from a `.cdsapirc` file,
the same used by the [official ECMWF client](https://cds.climate.copernicus.eu/how-to-api),
by default in the user's home directory:

```text
url: https://cds.climate.copernicus.eu/api
key: <your-personal-access-token>
```

Only the `key` line is read, as the data store is selected by the `endpoint` parameter:
the same token is valid on all the Copernicus data stores.
A different file can be selected with the `cdsApiRcFile` parameter.
The token is read only when the data must be downloaded:
simulations finding their data in cache need no credentials.

### Caching

Each request is cached in its own directory, named after the dataset and a hash of the whole request,
so that changing any detail of the request (dates, area, variables, ...) produces a different entry.
By default, the cache is in the user's cache directory
(e.g., `~/.cache/alchemist/copernicus` on Linux),
and the `cacheDirectory` parameter selects a different one.
Entries appear only once their data is complete:
an interrupted download leaves no entry behind,
and simulations running in parallel wait for each other instead of downloading the same data twice.

Cached entries are never refreshed:
to download the data again, for instance because the data store published a revised version,
delete the corresponding entry from the cache directory.

### Other parameters

* `providerTimeout`: how long, in ISO-8601, to wait for the data store to prepare the data
  (by default, `PT30M`).
  Large requests may spend a long time in the data store queue.

## Interpolation and conversion

Named parameters are supported in both YAML and the Kotlin DSL;
when using them, all the optional parameters preceding the last one provided must be specified as well.

By default, values are interpolated linearly in space and time with
{{% api package="model.geogrid.strategy.spatiotemporal" class="TrilinearInterpolation" %}}.
Other combinations are available through
{{% api package="model.geogrid.strategy.spatiotemporal" class="SeparableSpatioTemporalInterpolation" %}},
which combines a spatial strategy
({{% api package="model.geogrid.strategy.spatial" class="BilinearInterpolation" %}} or
{{% api package="model.geogrid.strategy.spatial" class="NearestInterpolation" %}})
with a temporal one
({{% api package="model.geogrid.strategy.temporal" class="LinearInterpolation" %}},
{{% api package="model.geogrid.strategy.temporal" class="ClosestInterpolation" %}},
{{% api package="model.geogrid.strategy.temporal" class="LastInterpolation" %}}, or
{{% api package="model.geogrid.strategy.temporal" class="NextInterpolation" %}}),
to be passed as the `interpolation` parameter.

Grid points with no data (e.g., river discharge far from rivers) are read as `NaN`.
The `converter` parameter decides how interpolated values become the values of the layer:
the default {{% api package="model.geogrid.strategy.converter" class="DoubleIdentityWithFallback" %}}
keeps them as they are, optionally replacing missing values with a fallback.
Layers of other types can be created with {{% api package="model.layers" class="GriddedGeoLayer" %}}
and a custom {{% api package="model.geogrid.strategy.converter" class="MeasurementConverter" %}}.
