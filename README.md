# InteropEHRate Terminal Device-to-Device (D2D) HR Exchange Library

## Installation Guide
The process of integrating the `t-d2d-e` library is quite straightforward, as it is provided as a `jar` file, and is hosted in the project's Nexus repository. 

In case a gradle project is created, the following line needs to be inserted in the dependencies section of the build.gradle file:
```
implementation(group:'eu.interoperhate', name:'td2de', version: '0.3.8')
```

If the development team importing the library, is using Maven instead of Gradle, the same dependency must be expressed with the following Maven syntax:
```
<dependency>
	<groupId>eu.interopehrate</groupId>
	<artifactId>td2de</artifactId>
	<version>0.3.8</version>
</dependency>
```
