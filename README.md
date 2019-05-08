Distributed ledger based authentication system

***

Simple demonstration of cordApp with GUI

This example was created for the `Basics of Grid and Cloud Computing` course.

***

### How to launch
* clone this repo
* run `./gradlew installQuasar build -x test deployNodes`
This command will build a project, prepare the executable files of the nodes for launch.
* run `./build/nodes/runnodes`
This command starts terminal windows with corda nodes. Configure the ports that run the nodes can be in the file build.gradle
(task deployNodes)
* run `cd clients/build/libs/` and `java -jar clients-0.1.jar --server.port=10050 --config.rpc.host=localhost --config.rpc.port=10003 --config.rpc.username=user1 --config.rpc.password=test`

A web server will be launched, which will be connected to the node named 'RootContainer'

***

### API

* GET /peers

Returns a list of all nodes in the network at the moment
* GET /containers/register/{type}?name=%name%

Existing types: 
glass - listens for events: StrongShaking, StopStrongShaking
iceCream - listens for events: TurnOffTheFrige, TurnOnTheFrige
gypsumHead - listens for events: StrongShaking, StopStrongShaking, Shaking, StopShaking
name - any unique name
* GET /containers/registered

Returns the list of registered containers
* GET /containers/delete/{name}

Where name is Base64 encoded name of registered container
* GET /containers/deleted

Return a list of all deleted containers
***
* GET /containers/auth/request

Create an auth request and return requestId and all item's certificates.Officer's certificate,
 his signature is generated with each request
* GET /containers/auth/requests

Returns list of all auth requests
* GET /containers/auth/response/{id}?status=%status%

Where status equal OK or Failed, ad id is requestId
Create an auth response
* GET /containers/auth/responses 
Returns list of auth responses
***
* GET /containers/change-carrier?name=%name%

Where name is carrier's name
Changing the carrier of container
* GET /containers/carriers
Returns list of all carriers
***
* GET /event/publish/{name}

Where name is event's name. Can be: StrongShaking, StopStrongShaking, Shaking, StopShaking, TurnOffTheFrige, TurnOnTheFrige
* GET /event/published
Returns history of events 