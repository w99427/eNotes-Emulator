# eNotes Emulator

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

## Description

eNotes Emulator provides a service to simulate eNotes. You can use this service to debug eNotes SDK.

**eNotes Home Page**: [https://enotes.io](https://enotes.io)

## Technologies

* Java 8 for the core modules
* [Maven 4.0+](http://maven.apache.org/) - for building the project

## Getting started

### Using Prebuild Jar

Download the jar file from [eNotes Emulator](https://github.com/w99427/eNotes-Emulator/tree/master/out), and run it.

```
java -jar eNotesEmulatorServer-0.1.0.jar
```

### Building from source code

It is best to have the latest JDK and Maven installed. The HEAD of the `master` branch contains the latest development code.

To perform a full build, use:

```
./mvnw package
```

The outputs are under the `target` directory.

### Usage

* config eNotes SDK to enable debug through emulator

  ```
  ENotesSDK.config.debugForEmulatorCard = true;
  ENotesSDK.config.emulatorCardIp = "your server ip";
  ```

* check and reset emulator status

  Access `http://localhost:8083` on the computer that run the service.

For more details, see [eNotes SDK exapmle](https://github.com/w99427/eNotes-Android-SDK/tree/master/examples).

## License

``` 
Copyright 2018 eNotes

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```