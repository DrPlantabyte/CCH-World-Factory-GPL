CCH-World-Factory-GPL
=====================

# What is it?

CCH World Factory is a Java library for procedural planet generation. It 
produces realistic* planets at any level of detail, complete with biomes, 
altitude, and even rainfall distribution. Planet generation is not limited to 
Earth-like planets either.

*Note: planetary simulations are inspired by reality, but may not be scientifically accurate. 

# License
CCH World Factory - GPL is the GPL fork of the CCH World Factory library. When 
using code from this repository, you are bound to the restrictions placed by the 
GPLv3 license (available at http://www.gnu.org/licenses/gpl.html ).

The original author and copyright holder of the CCH World Factory library, 
C. C. Hall, maintains another branch that is not GPL (nor open-source). If you 
would like to license the private branch of CCH World Factory, contact 
C. C. Hall at explosivegnome@yahoo.com

# Compiling
CCH World Factory - GPL is a Netbeans project. If not using the Netbeans IDE, 
you should still be able to compile using Ant:
```
$ cd Path/To/PureJS
$ ant jar
```
For documentation:
```
$ cd Path/To/PureJS
$ ant javadoc
```


# How to Use
Making planets with CCH World Factory - GPL is fairly easy:
```java
import hall.collin.christopher.worldgeneration.*;
import hall.collin.christopher.worldgeneration.graphics.*;
import java.util.concurrent.atomic.DoubleAdder;
import java.awt.image.BufferedImage;
...
String seed = "Name of planet";
final DoubleAdder progessTracker = new DoubleAdder();
AbstractPlanet planet = TectonicHydrologyPlanet.createPlanet(seed, progessTracker);
PlanetPainter vp = new VegetationPainter();
MercatorMapProjector mapper = new MercatorMapProjector();
int size = 256;
BufferedImage map = mapper.createMapProjection(planet, size, vp, ptracker);
```
Note the use of **java.util.concurrent.atomic.DoubleAdder** to provide 
multi-threaded progress updates, allowing for a progress bar to track generation 
of the planet (see code in 
**hall.collin.christopher.worldgeneration.testapps.FantasyMapMaker1** for an 
example of planet generation with a progress bar).
