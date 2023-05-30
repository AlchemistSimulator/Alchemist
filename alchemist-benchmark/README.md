# Alchemist Benchmark Module

This is a module created to benchmark alchemist execution
to compare different run configurations and environments.

The benchmarks are based on [JMH](https://github.com/openjdk/jmh) -
Java Microbenchmark Harness which is the de-facto standard for benchmarking JVM.

## Usage

This module uses the jmh gradle [plugin](https://github.com/melix/jmh-gradle-plugin), so
in order to run the benchmarks on your machine, simply launch
*jmh* task.

The benchmark results will be saved by default in *build/results/jmh/results.txt*
