+++
title = "Export data"
weight = 5
tags = ["data", "export", "csv", "mongodb", "data analysis"]
summary = "Select which data the simulator should output, in which format, and where."
+++

The simulator provides tools for exporting data automatically.
An  export section on the simulation file instructs which data is considered interesting,
and should be thus exported with the selected sampling frequency.
Data can be exported separately for each node, or can be aggregated on the fly using any
univariate statistic function (e.g., mean, sum, product, percentile, median...).
The treatment of missing or non-finite values can be specified as well. Results
are exported in comma-separated values files, easily importable in a variety of
data analysis tools.

Data export is realized by
{{% api package="loader.export" class="Exporter" %}}s.
Exporters are defined in the [`export`](/reference/yaml#export) section of the configuration,
by specifying their [`type`](/reference/yaml/#exportertype),
their constructor [`parameters`](/reference/yaml/#parameters),
and the [`data`](/reference/yaml/#data) they should export.
The elements under [`data`](/reference/yaml/#data) must be instanceable implementations of
{{% api package="boundary" class="Extractor" %}}.

## Export data as CSV

Alchemist can export data to a custom comma-separated-values format.
This is the classic way data is exported from the simulator, and relies on
{{% api package="loader.export.exporters" class="CSVExporter" %}}.

### Examples

* Export of the output of a Protelis program every 3 simulated seconds:
  {{< code path="alchemist-loading/src/test/resources/testExportInterval.yml" >}}
* Export data to both a csv file and a MongoDB instance:
  {{< code path="alchemist-loading/src/test/resources/testExporters.yml" >}}
* Export of the {{% api package="loader.export.extractors" class="MeanSquaredError" %}} of some custom properties:
    {{< code path="alchemist-loading/src/test/resources/testCustomExport.yml" >}}
* Export of the output of a Protelis program, values generated from nodes get accumulated into mean, max, min, variance, and median:
  {{< code path="alchemist-loading/src/test/resources/testCSVExporter.yml" >}}

## Export data to a MongoDB instance

Alchemist can send data directly to a pre-existing MongoDB instance through its
{{% api package="boundary.exporters" class="MongoDBExporter" %}}.

### Examples

* Export data to both a csv file and a MongoDB instance:
  {{< code path="alchemist-loading/src/test/resources/testExporters.yml" >}}
* Export of the output of a Protelis program, values generated from nodes get accumulated into mean, max, min, variance, and median:
  {{< code path="alchemist-loading/src/test/resources/testMongoExporter.yml" >}}
