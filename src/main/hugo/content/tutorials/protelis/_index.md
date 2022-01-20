+++
title = "Protelis Incarnation Tutorial"
weight = 5
tags = ["protelis", "tutorial", "aggregate", "aggregate computing", "aggregate programming"]
summary = "Ready-to-run examples of increasing complexity with the Protelis incarnation"
+++

A tutorial similar to

You need:
* A terminal, bash compatible preferred
* [Java 11+](https://adoptium.net/) installed and working
* [Git](https://git-scm.com/) installed and working

Open the terminal and follow these steps
1. `git clone https://github.com/AlchemistSimulator/alchemist-primer `
2. `cd alchemist-primer`
3. Launch depending on your terminal:
* Bash compatible (Linux, MacOS X, Git Bash, Cygwin): `./gradlew runAll`
* Windows native (cmd.exe, Powershell): `gradlew.bat runAll`
4. Wait for the simulator components to be downloaded (about five minutes on a reasonable connection)
5. A GUI pops up
6. Press the <kbd>P</kbd> button to start the simulation. Other relevant buttons are described [here](/use/swing-gui)

{{% notice note "Something went wrong along the line?" %}}
[Open an issue](https://github.com/AlchemistSimulator/Alchemist/issues/new/choose) and we'll get back to you.
{{% /notice %}}

You are now  ready to proceed with our tutorials!

{{% notice info %}}
If you want to understand right now what is happening under the hood,
consider learning instead about [the Alchemist meta-model](/explanation/metamodel/)
{{% /notice %}}
