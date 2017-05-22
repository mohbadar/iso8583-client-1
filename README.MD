Developer Guide
---------------
Install jpos compat file

```
mvn install:install-file -Dfile=./lib/compat_1_5_2.jar -DgroupId=org.jpos -DartifactId=jpos-compat -Dversion=2.0.10 -Dpackaging=jar
```

Deployment Guide
---------------

Create Native Package (Windows)
---------------------

1. Install MSI Packager (http://www.jrsoftware.org)

```
mvn jfx:jar
```

atau 

```
mvn jfx:native
```