# KillrVideo Java #

A reference application for Java developers looking to learn more about using [Apache Cassandra][cassandra] and
[DataStax Enterprise][dse] in their applications and services. Learn more at [KillrVideo].

The current version of KillrVideo Java includes Cassandra, DSE Search, and DSE Graph with [DSL's (Domain Specific Languages)][gremlindsljava].  DSE Graph powers the recommendation engine and all search capabilites are powered with DSE Search.

NOTE: We are now fully switched to using version 1.4.2 of the [DSE java driver][DSE Java driver]!  API docs are [here][DSE Java driver API docs].

## Running Locally

Use these guides to get started running KillrVideo locally on your development machine:
* [Getting Started with KillrVideo][getting-started]: Follow this to setup common dependencies like Docker.
* [Getting Started with Java][getting-started-java]: Follow this to get this Java code 
running.
  

## Pull Requests, Requests for More Examples
This project will continue to evolve along with Cassandra and you can expect that as Cassandra and the DataStax 
driver add new features. This sample application will try and provide examples of those. 

I'll gladly accept any pull requests for bug fixes, new features, etc.  and if you have a request for an example 
that you don't see in the code currently, send me a message [@SonicDMG][twitter] on Twitter or open an issue 
[here][issues] on GitHub.

## License
Copyright 2018 David Gilardi, derived from original work by Duy Hai Doan

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
[Killrvideo]: https://killrvideo.github.io
[getting-started]: https://killrvideo.github.io/getting-started/
[getting-started-java]: https://killrvideo.github.io/docs/languages/java/
[twitter]: https://twitter.com/SonicDMG
[DSE Java driver API docs]: http://docs.datastax.com/en/drivers/java-dse/1.4/
[DSE Java driver]: https://docs.datastax.com/en/developer/java-driver-dse/1.4/
[issues]: https://github.com/KillrVideo/killrvideo-java/issues
[gremlindsljava]: https://www.datastax.com/dev/blog/gremlin-dsls-in-java-with-dse-graph

