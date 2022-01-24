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
