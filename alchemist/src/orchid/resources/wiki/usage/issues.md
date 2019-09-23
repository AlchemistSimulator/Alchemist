---

title: Troubleshooting and known issues

---

A list of known issues which we are aiming to resolve is tracked [on the Alchemist Github issue tracker](https://github.com/AlchemistSimulator/Alchemist/issues).
Of course, contributions are welcome.

Other issues you may experience are listed below.
If you run into other issues yourself, please report them.

### Graphical glitches on the Swing graphical interface

#### Problem description

Under some combination of hardware and drivers, the Swing interface may render uncorrectly.
This is due to Alchemist using OpenGL as rendering backend for Swing, in order to increase performance.

Examples of graphical glitches are the following:

![Graphical glitch example]({{ 'assets/media/issues/vbox-glitch-1.png'|asset }})
![Graphical glitch example]({{ 'assets/media/issues/vbox-glitch-2.png'|asset }})
![Graphical glitch example]({{ 'assets/media/issues/vbox-glitch-3.png'|asset }})

The issue has been reproduced on:

* Virtualbox-virtualized Ubuntu installation
* Old AMD APU for notebooks with open source radeon driver

#### Solution

In case you are using a virtualized environment, if you can do so, switch to the "bare metal" operating system.
Otherwise, you can try to disable Swing OpenGL support by {{ anchor('writing a custom component', 'Writing custom components') }}
which sets Swing non-accelerated mode, by invoking the following Java code:
```java
System.setProperty("sun.java2d.opengl","false");
```
