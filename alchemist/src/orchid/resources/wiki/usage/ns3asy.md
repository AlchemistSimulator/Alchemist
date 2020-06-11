---

title: Integration with the ns3 network simulator

---


Alchemist can provide a realistic network simulation by leveraging
the [ns3](https://www.nsnam.org/) network simulator.


### Features and Limitations

Alchemist's integration with ns3 provides the following features:

- Pysical and layer-2 simulation of Ethernet and WiFi
- Layer 4 realistic simulation via UDP and TCP protocols
- Packet size can be set manually, or be realistically determined at runtime
- Control over the serialization mechanism, defaulting to Java serialization.

In its current state, the realistic networking has a number of important limitations
the user should be well aware of:

- Realistic networking is supported only if the Protelis incarnation is used.
- Node mobility is *not supported* in conjunction with realistic networking.
- A single simulation at once can be executed with realistic networking.
  In case you intend to run a batch, make sure to set the maximum parallelism (`-t` CLI option) to `1`.
- The simulation seed internally used by ns3 is currently not configurable:
  simulations are reproducible, but there will be a single result per settings configuration.
- Realistic network simulation only works under Linux
- Ethernet and WiFi connection can't get mixed

#### Additional configuration for Ethernet

When using Ethernet, it is possible to configure the following parameters:

- The error rate of the underlying physical layer (determined by [this formula](https://www.nsnam.org/doxygen/error-model_8cc_source.html#l00259))
- The channel's data rate, also used to determine the rate at which the nodes should send multiple packets

#### Additional configuration for WiFi

When using WiFi, it is possible to configure the following parameters:

- The position of the access point (optional; defaults to (0, 0))
- The propagation delay model ([these models](https://www.nsnam.org/doxygen/group__propagation.html) are available)
- The propagation loss model ([these models](https://www.nsnam.org/doxygen/group__propagation.html) are available)

### Configuring a simulation with ns3

The use of the ns3 simulator can be added to a pre-existing simulation (based on the Protelis incarnation) by adding these configuration parameters to the `.yml` file: 

```yaml
#realistic networking configuration with Ethernet
ns3:
  protocol: "TCP"
  packet-size: 0 #optional
  error-rate: 0.0001
  data-rate: "1Mbps"
  serializer:
    type: it.unibo.alchemist.ns3.utils.DefaultNs3Serializer #optional
```

```yaml
#realistic networking configuration with WiFi
ns3:
  protocol: "TCP"
  packet-size: 0 #optional
  ap-position: [0, 0] #optional
  propagation-delay: "ns3::ConstantSpeedPropagationDelayModel" #or any other propagation delay model
  propagation-loss: "ns3::FriisPropagationLossModel" #or any other propagation loss model
  serializer:
    type: it.unibo.alchemist.ns3.utils.DefaultNs3Serializer #optional
```

If such configuration is present, every time the `send` program is executed the corresponding data will be sent using ns3 instead of the built-in system.

To understand how to setup a full fledged Protelis simulation,
please refer to the Protelis documentation of this wiki. 

