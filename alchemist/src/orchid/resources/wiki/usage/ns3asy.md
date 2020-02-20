---

title: Integration with the ns3 network simulator

---


Alchemist supports the use of the [ns3](https://www.nsnam.org/) network simulator to obtain a realistic simulation of network communication.
This can be useful, for example, when simulating distributed system where the node must communicate to tackle their collective task. 

The integration between Alchemist and ns3 is currently supported only by the Protelis incarnation. 

###What the integration with ns3 can do

The integration with ns3 provides the ability to configure the following parameters: 

- The transport protocol (TCP and UDP are supported)
- The packet size
- The error rate of the underlying physical layer (in terms of packet loss)
- The data rate at which the nodes should send multiple packets
- How objects should be serialized (and deserialized) before sending them

###How to setup a simulation using ns3 with the Protelis incarnation

The use of the ns3 simulator can be added to a pre-existing simulation (based on the Protelis incarnation) by adding these configuration parameters to the `.yml` file: 

```yaml
#realistic networking configuration
ns3:
  protocol: "TCP"
  packet-size: 0 #optional
  error-rate: 0.0001
  data-rate: "1Mbps"
  serializer:
    type: it.unibo.alchemist.ns3.utils.DefaultNs3Serializer #optional
```
If such configuration is present, every time the `send` program is executed the corresponding data will be sent using ns3 instead of the built-in system. 

To understand how to setup a fully fledged Protelis simulation, please refer to its wiki. 

###Current limitations

- It is not possible to make the nodes move when using ns3
- It's only possible to use it on Linux
