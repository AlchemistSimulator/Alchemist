+++
pre = ""
title = "Memory leaks under Linux"
weight = 5
tags = ["linux", "memory leak", "memory pressure", "max_map_count", "batch"]
summary = "Known issue of some Java Virtual Machine implementations when requested to use more than 64GB of RAM"
+++

When Alchemist is used on computers with a large amount of memory and parallelism,
additional configuration might be required.

{{% notice info %}}
These problems have more to do with the way memory allocation on Linux and the JVM works than with Alchemist itself.
They are not memory leaks in Alchemist (hence the reason why they do not happen with smaller memory sizes).
{{% /notice %}}

## Symptoms

* batch execution slowing down (this is due to the *memory pressure*)
* analysis of memory shows that the amount of memory actually in use by the JVM is always growing (similar to a *memory leak*)
* the following (or similar) warning is displayed on the standard error on startup:

```
[warning][gc] ***** WARNING! INCORRECT SYSTEM CONFIGURATION DETECTED! *****
[warning][gc] The system limit on number of memory mappings per process might be too low for the given
[warning][gc] max Java heap size (101754M). Please adjust /proc/sys/vm/max_map_count to allow for at
[warning][gc] least 183157 mappings (current limit is 65530). Continuing execution with the current
[warning][gc] limit could lead to a premature OutOfMemoryError being thrown, due to failure to map memory.
```

## Solution

{{% notice warning %}}
The file `/proc/sys/vm/max_map_count` could be changed manually, but these changes won't persist reboots.
{{% /notice %}}

A persistent solution to the problem is to instruct the system to always load the correct configuration.
One possible way is to instruct `sysctl` through a configuration file
(e.g., `/etc/sysctl.d/99-vm-max-map-count.conf`)
including the following line: ``vm.max_map_count=<custom max_map_count>``.
This can be done on a **root** shell via:

```bash

sudo sh -c 'echo "vm.max_map_count=<custom max_map_count>" > /etc/sysctl.d/99-vm-max-map-count.conf'
sudo sysctl --system
```

{{% notice tip "Computing a \"good\" value for max_map_count" %}}
A reasonable estimation of the value is **four times the amount of system RAM expressed in Megabytes** 
(for instance vm.max_map_count=524288 for a system with 128GB RAM).

When in doubt, select a higher value, [apparently the negative effects are negligible](https://archive.is/xyaA6).
{{% /notice %}}

### Ready-to-use commands

* **128GB** 
```bash
sudo bash -c 'echo "vm.max_map_count=524288" > /etc/sysctl.d/99-vm-max-map-count.conf' && sudo sysctl --system
```

* **256GB**
```bash
sudo bash -c 'echo "vm.max_map_count=1048576" > /etc/sysctl.d/99-vm-max-map-count.conf' && sudo sysctl --system
```

* **512GB**
```bash
sudo bash -c 'echo "vm.max_map_count=2097152" > /etc/sysctl.d/99-vm-max-map-count.conf' && sudo sysctl --system
```

* **1TB**
```bash
sudo bash -c 'echo "vm.max_map_count=4194304" > /etc/sysctl.d/99-vm-max-map-count.conf' && sudo sysctl --system
```
