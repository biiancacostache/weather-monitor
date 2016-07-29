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
  - <a href="https://github.com/spray/spray-json">spray-json</a> for marshalling and unmarshalling data
  - <a href="http://doc.akka.io/docs/akka/current/scala/actors.html">akka-actor</a> for scheduling jobs purposes
  - <a href="http://doc.akka.io/docs/akka/2.4.8/scala/http/introduction.html#philosophy">akka-http</a> libraries for dealing with the communication over Http

### Improvements:

