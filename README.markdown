# KillrVideo Java #

A reference application for Java developers looking to learn more about using [Apache Cassandra][cassandra] and
[DataStax Enterprise][dse] in their applications and services. Learn more at [killrvideo.github.io][killrvideo].

## Running Locally

Use these guides to get started running KillrVideo locally on your development machine:
* [Getting Started with KillrVideo][getting-started]: Follow this to setup common dependencies like Docker.
* [Getting Started with Java](#getting-started-java): Follow this to get this C# code running.

## Getting started with Java <a id="getting-started-java"></a>

The Java implementation of [killrvideo] is using [Achilles Object Mapper]. Please read its documentation to understand
the complete set of features of the framework

To get the Java impl running:

* First clone the project with `git clone https://github.com/doanduyhai/killrvideo-java.git`
* Do a clean compile with `mvn clean test`
* Start your local docker machine
* To run **KillrVideo** with OSS Cassandra, execute `docker-compose -f DockerCompose/docker-compose-OSS-Cassandra.yaml up`
* To run **KillrVideo** with [DataStax Enterprise][dse], execute `docker-compose -f DockerCompose/docker-compose-DSE-Cassandra.yaml up`
* Execute the script _getenvironment.sh_ to set environment variables with `. ./getenvironment.sh` 

> **warning: the first dot (.) is important! It will execute the script in the context of the calling shell**
<br/>

After the script execution, you should be able to see the environment variables:

* _KILLRVIDEO_DOCKER_IP_ (`echo $KILLRVIDEO_DOCKER_IP`) 
* _KILLRVIDEO_HOST_IP_ (`echo $KILLRVIDEO_HOST_IP`)
 
* Start **KillrVideo** server with `mvn spring-boot:run -Dlogback.configurationFile=./src/main/resources/<logback_conf>` 

 where &lt;logback_conf&gt; can be either **logback_dev.xml** or **logback_dev.xml**. The dev configuration file will show you all the
DML logs and DEBUG messages:

```
17:08:50.008 [grpc-default-executor-0] DEBUG k.service.VideoCatalogService:Start getting latest video preview
17:08:50.167 [grpc-default-executor-0] DEBUG killrvideo.entity.LatestVideos:Query ID d06a3a10-cf4e-4424-b03e-f9b6da27a1c7 : [SELECT * FROM killrvideo.latest_videos WHERE yyyymmdd=:yyyymmdd_Eq;] with CONSISTENCY LEVEL [LOCAL_ONE]
17:08:50.172 [grpc-default-executor-0] DEBUG killrvideo.entity.LatestVideos:   	 Java bound values : [20160821]
17:08:50.173 [grpc-default-executor-0] DEBUG killrvideo.entity.LatestVideos:   	 Encoded bound values : [20160821]
17:08:50.182 [grpc-default-executor-0] DEBUG killrvideo.entity.LatestVideos:ResultSet[ exhausted: true, Columns[yyyymmdd(varchar), added_date(timestamp), videoid(uuid), name(varchar), preview_image_location(varchar), userid(uuid)]]
17:08:50.182 [grpc-default-executor-0] DEBUG killrvideo.entity.LatestVideos:Query ID d06a3a10-cf4e-4424-b03e-f9b6da27a1c7 results :

17:08:50.188 [grpc-default-executor-0] DEBUG killrvideo.entity.LatestVideos:Query ID 7737c8c3-f4d3-4f9e-82fe-0c2f49d0c1db : [SELECT * FROM killrvideo.latest_videos WHERE yyyymmdd=:yyyymmdd_Eq;] with CONSISTENCY LEVEL [LOCAL_ONE]
17:08:50.188 [grpc-default-executor-0] DEBUG killrvideo.entity.LatestVideos:   	 Java bound values : [20160820]
17:08:50.188 [grpc-default-executor-0] DEBUG killrvideo.entity.LatestVideos:   	 Encoded bound values : [20160820]
17:08:50.192 [achilles-default-executor-2] DEBUG killrvideo.entity.LatestVideos:ResultSet[ exhausted: true, Columns[yyyymmdd(varchar), added_date(timestamp), videoid(uuid), name(varchar), preview_image_location(varchar), userid(uuid)]]
17:08:50.192 [achilles-default-executor-2] DEBUG killrvideo.entity.LatestVideos:Query ID 7737c8c3-f4d3-4f9e-82fe-0c2f49d0c1db results :
...
```
* Open your web browser at **$KILLRVIDEO_HOST_IP:3000** to start using **KillrVideo**
  

## Pull Requests, Requests for More Examples
This project will continue to evolve along with Cassandra and you can expect that as Cassandra and the DataStax driver add new features. This sample application will try and provide examples of those. 

I'll gladly accept any pull requests for bug fixes, new features, etc.  and if you have a request for an example that you don't see in the code currently, send me a message [@doanduyhai][twitter] on Twitter or open an issue here on GitHub.

## License
Copyright 2016 Duy Hai DOAN

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[cassandra]: http://cassandra.apache.org/
[dse]: http://www.datastax.com/products/datastax-enterprise
[killrvideo]: https://killrvideo.github.io/
[getting-started]: https://killrvideo.github.io/getting-started/
[getting-started-csharp]: https://killrvideo.github.io/docs/languages/c-sharp/
[twitter]: https://twitter.com/doanduyhai
[Achilles Object Mapper]: http://achilles.io