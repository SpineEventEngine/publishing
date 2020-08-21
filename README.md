[![Build Status](https://travis-ci.com/SpineEventEngine/publishing.svg?branch=master)](https://travis-ci.com/SpineEventEngine/publishing)

# Publishing
 
The scripts and tools in this repository automate the publishing of artifacts for 
the Spine Event Engine libraries.

## Version file format

This application expects the libraries to define their `version.gradle.kts` in a consistent fashion.
Version files must declare the version of the library itself and the version of its dependencies
using Kotlin expressions like so:

```kotlin
val coreJava = "5.0.51"     // OK
val base = "5.0.50"         // OK

val time: String = "5.0.51" // Not OK, no need for the type.
var gcloudJava = "5.0.51"   // Not OK, needs to be a constant.

```  

This application also expects consistent naming:

- "coreJava" for the [core-java](https://github.com/SpineEventEngine/core-java) library;
- "base" for the [base](https://github.com/SpineEventEngine/base) library;
- "time" for the [time](https://github.com/SpineEventEngine/time) library.

To extend this, go to `io.spine.publishing.SpineLibrary` and add values to the `SpineLibrary` enum.

