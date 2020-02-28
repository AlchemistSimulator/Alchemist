---

title: Integration with the ns3 network simulator

---


Alchemist supports the use of the [ns3](https://www.nsnam.org/) network simulator to obtain a realistic simulation of network communication.
This can be useful, for example, when simulating distributed system where the node must communicate to tackle their collective task. 

The integration between Alchemist and ns3 is currently supported only by the Protelis incarnation. 

### What the integration with ns3 can do

The integration with ns3 provides the ability to configure the following parameters: 

- The physical technology to use (Ethernet and WiFi are supported)
- The transport protocol (TCP and UDP are supported)
- The packet size (optional parameter; if not specified, it is determined automatically)
- How objects should be serialized (and deserialized) before sending them (optional parameter; if not specified, the default Java serialization facility is used)

#### Additional configuration for Ethernet

When using Ethernet, it is possible to configure the following parameters:

- The error rate of the underlying physical layer (determined by [this formula](https://www.nsnam.org/doxygen/error-model_8cc_source.html#l00259))
- The channel's data rate, also used to determine the rate at which the nodes should send multiple packets

#### Additional configuration for WiFi

When using WiFi, it is possible to configure the following parameters:

- The position of the access point (optional; if not specified it is (0, 0))
- The propagation delay model ([these models](https://www.nsnam.org/doxygen/group__propagation.html) are available)
- The propagation loss model ([these models](https://www.nsnam.org/doxygen/group__propagation.html) are available)

### How to setup a simulation using ns3 with the Protelis incarnation

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

To understand how to setup a fully fledged Protelis simulation, please refer to its wiki. 

### Current limitations

- It is not possible to make the nodes move when using ns3
- It's only possibile to use ns3 from a single thread at once (be aware of this when running multiple simulations in batch mode)
- It's only possible to use it on Linux
