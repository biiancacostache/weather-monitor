# weather-monitor
The purpose of this web server is to provide an API for retrieving weather information provided by Accuweather.

### Requirements:

<ul>
  <li> Scala 2.11.8 </li>
  <li> Java 8 </li>
  <li> sbt 0.13.8 </li>
</ul>

To start the service use <b>sbt run</b>.</br>
To run the tests use <b> sbt test </b>.

### API Details:
The API specs can be found in the <a href="https://github.com/Bii03/weather-monitor/blob/master/swagger.yaml">swagger.yaml file</a>. </br>
Please, note that the server will start on <b>localhost:8140</b>.

### Implementation details:
In order to provide the required functionality, the following have been used:
  - <a href="https://github.com/typesafehub/config">typesafe-config</a> for parsing the configuration files
  - <a href="https://github.com/spray/spray-json">spray-json</a> for marshalling and unmarshalling data
  - <a href="http://doc.akka.io/docs/akka/current/scala/actors.html">akka-actor</a> for scheduling jobs purposes
  - <a href="http://doc.akka.io/docs/akka/2.4.8/scala/http/introduction.html#philosophy">akka-http</a> libraries for dealing with the communication over Http
  - <a href="">scala-logging</a> for logging purposes
  - <a href="http://www.scalatest.org/">scalatest</a>, <a href="http://scalamock.org/">scala-mock</a> and <a href="http://blog.madhukaraphatak.com/akka-http-testing/">akka-http-testkit</a> for testing purposes

The entry point of the server is represented by the Bootstrap object, which builds the service configuration, the actor system and materializer, and starts RestService. In addition to this, it schedules the actor system to periodically perform several actions (discussed below).

The RestService is responsible for handling all the incoming Http requests, using the defined routes within WeatherMonitorRoutes.
If the processing of a request fails, the failure is mapped to a corresponding Http Status Code and error, using Fault.Handler.

WeatherMonitorRoutes maps the Http requests paths to a corresponding operation to be executed, defined in WeatherMonitorOps - Future based operations.

AccuWeatherClient handles the communication with the Accuweather service by passing corresponding Http requests and unwraps the responses in a certain manner.

In order to efficiently provide weather information with regards to a specific location, a caching strategy has been used. An active location is defined as a location that has been searched for in the past month. For such locations, the server stores relevant data locally, in State. Whenever a client asks for data related to a location, the service checks whether the location is active or not. If this is the case, the request is completed by providing already registered data instead of fetching it from the Accuweather service. 

To ensure the registered data is still up-to-date, the actor system is scheduled to sample temperature records for all the active locations at a configurable time interval. Also, to only store data with respect to active locations, the system is scheduled to clean up all the data corresponding to inactive locations (not been triggered in the past month) at a configurable interval. Please, see the StateManagerActor for more details.

<b>NOTE</b> that all the configurable parameters can be found in the <a href="https://github.com/Bii03/weather-monitor/blob/master/src/main/resources/application.conf">application.conf</a> file.

### Improvements:

The caching strategy could be improved by using <a href="http://doc.akka.io/docs/akka/current/scala/persistence.html">Akka Persistence</a>.</br></br>
<i>"Akka persistence enables stateful actors to persist their internal state so that it can be recovered when an actor is started, restarted after a JVM crash or by a supervisor, or migrated in a cluster. The key concept behind Akka persistence is that only changes to an actor's internal state are persisted but never its current state directly (except for optional snapshots). These changes are only ever appended to storage, nothing is ever mutated, which allows for very high transaction rates and efficient replication. Stateful actors are recovered by replaying stored changes to these actors from which they can rebuild internal state. This can be either the full history of changes or starting from a snapshot which can dramatically reduce recovery times."</i></br></br>
In this case, it would handle events and commands with regards to the active locations data records. Along the advantage of configuring the journal for keeping the data records, Akka Persistence ensures fault tolerance, ie. the server would not lose the registered data if it fails.



