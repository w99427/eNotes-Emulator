# eNotes-Emulator-Server

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

##### Project website:[https://github.com/w99427/eNotes-Emulator](https://github.com/w99427/eNotes-Emulator)

### Description

The eNotes Emulator Server is a Java implementation of providing you with a set of services to simulate digital currency for your debugging eNotes SDK.

### Technologies

* Java 8 for the core modules
* [Maven 4.0+](http://maven.apache.org/) - for building the project

### Features:
* Search and connect bluetooth device.
* read cert ,verify signature and generate signature by transceive apdu command.
* reset simulate digital currency status.

### Getting started
#### 1.Using  Jar:
Run emulator server,you can find jar under the `out`directory.
 ```
 java -jar eNotesEmulatorServer-0.1.0.jar
 ```

#### 2.Building from the command line:
To get started, it is best to have the latest JDK and Maven installed. The HEAD of the `master` branch contains the latest development code.

To perform a full build use
```
./mvnw package
```

The outputs are under the `target` directory.

#### 3.Usage:
* 1.config eNotes SDK code for able debug 
```
  ENotesSDK.config.debugForEmulatorCard = true;
  ENotesSDK.config.emulatorCardIp = "your server ip";
```
* 2.through computer browsers access to you EmulatorServer and port is 8083 ,such as `http://localhost:8083` , you can reset simulate digital currency status .

For more details ,you can access [eNotes SDK exapmle](https://github.com/w99427/eNotes-Android-SDK/tree/master/examples) project.
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


