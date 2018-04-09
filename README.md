# KillrVideo Java #

[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)

A reference application for Java developers looking to learn more about using [Apache Cassandra][cassandra] and
[DataStax Enterprise][dse] in their applications and services. Learn more at [Killrvideo].

We are now fully switched to use version `1.5.1` of the [DSE java driver][DSE Java driver]!  API docs are [here][DSE Java driver API docs].  
This version is stable and ready for consumption.

## Running Locally

Use these guides to get started running KillrVideo locally on your development machine:
* [Getting Started with KillrVideo][getting-started]: Follow this to setup common dependencies like Docker.
* [Getting Started with Java][getting-started-java]: Follow this to get this Java code 
running.
  

## Pull Requests, Requests for More Examples
This project will continue to evolve along with Cassandra and you can expect that as Cassandra and the DataStax 
driver add new features. This sample application will try and provide examples of those. 

We will gladly accept any pull requests for bug fixes, new features, etc.  and if you have a request for an example 
that you don't see in the code currently, send me a message [@SonicDMG][twitter] or [@clunven][clunTwitter] on Twitter or open an issue 
[here][issues] on GitHub.

## License
Copyright 2018 David Gilardi, Cedrick Lunven, derived from original work by Duy Hai Doan

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
[clunTwitter]: https://twitter.com/clunven
[DSE Java driver API docs]: https://github.com/datastax/java-dse-driver
[DSE Java driver]: https://docs.datastax.com/en/developer/java-driver-dse/1.5/
[issues]: https://github.com/KillrVideo/killrvideo-java/issues
[gremlindsljava]: https://www.datastax.com/dev/blog/gremlin-dsls-in-java-with-dse-graph
