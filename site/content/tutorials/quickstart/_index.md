+++
title = "Quickstart"
weight = 1
tags = ["quickstart", "installation", "execution"]
summary = "A super-fast way to get an instance of the simulator up and running."
+++

You need:
* A terminal, bash compatible preferred
* [Java 11+](https://adoptium.net/) installed and working
  * Java can be downloaded from https://adoptium.net/ 
* [Git](https://git-scm.com/) installed and working
  * Git can be downloaded from https://git-scm.com

Open the terminal and follow these steps
1. `git clone https://github.com/AlchemistSimulator/alchemist-primer `
2. `cd alchemist-primer`
3. Launch depending on your terminal:
* Bash compatible (Linux, MacOS X, Git Bash, Cygwin): `./gradlew runAll`
* Windows native (cmd.exe, Powershell): `gradlew.bat runAll`
4. Wait for the simulator components to be downloaded (about five minutes on a reasonable connection)
5. A GUI pops up
6. Press the <kbd>P</kbd> button to start the simulation. Other relevant buttons are described [here](/reference/default-ui)

{{% notice note "Something went wrong along the line?" %}}
[Open an issue](https://github.com/AlchemistSimulator/Alchemist/issues/new/choose) and we'll get back to you.
{{% /notice %}}

You are now  ready to proceed with our tutorials!

{{% notice info %}}
If you want to understand right now what is happening under the hood,
consider learning instead about [the Alchemist meta-model](/explanation/metamodel/)
{{% /notice %}}
