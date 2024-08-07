Fop Apache Image Library 2.0.2
==============================

What is this
------------
A custom implementation of 'Apache PDFBox' (version 2.0.1) to be compatible with the specific libraries used at Ravago. This was needed due to incompatibility with Apache Fop 1.x.

Build Instructions
------------------
#### Tool Versioning
> Build tool: Maven 3.9.6

#### How-to
Simply build the project with Maven. Be aware that this package was only build and tested with Java 8 at the time of writing.
Compiling this project to Java 9 should be possible, but might cause issues in the applications using this library.

Know Issues
-----------
### Compilation failure
>It might be because you are using a Java version newer than 22 that has dropped support for Java 8.
>For example Java 22 has dropped support for Java 7, so future versions might also drop support for 8.
