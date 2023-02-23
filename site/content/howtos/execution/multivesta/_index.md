+++
title = "MultiVesta"
weight = 1
tags = ["multivesta", "execution", "analysis"]
summary = "How to integrate MultiVesta in Alchemist"
+++

Once you have your simulation project configured as you want, you need to:
1. Add dependency on the multivesta jar `implementation(files("libs/multivesta.jar"))`. You can require the jar by going to [MultiVesta's repository](https://github.com/andrea-vandin/MultiVeStA)
2. Include the following dependency in build.gradle.kts `implementation("it.unibo.alchemist:alchemist-multivesta-adapter")`
3. Copy the `AlchemistSimState` class from current repo (you can reach it in following location _alchemist-multivesta-adapter/dist/AlchemistSimState.kt_) and paste it in the package you prefer
4. Set the main class as follows `mainClass.set("it.unibo.alchemist.multivesta.adapter.AlchemistMultiVesta")`, inside the JavaExec Gradle task
5. When you configure Alchemist arguments with Gradle `args` method, add another `args` call after traditional Alchemist arguments and pass all MultiVesta arguments in a single string with key `"-mv"`, e.g. `args("-mv", "-c -sots 1 -bs 30 -a 0.05 -ds [0.1] -l 12 -sd com.example.AlchemistSimState -f myquery.multiquatex")`. **Important**: MultiVesta arguments must include the fully qualified name of the class you have copied on point 3 (in the example it is `-sd com.example.AlchemistSimState`) and the desired multiquatex file (in the example it is `-f myquery.multiquatex`)
6. Add MultiVestaExporter to your Alchemist configuration file

You are now ready to start your simulation as usually with Gradle JavaExec task!