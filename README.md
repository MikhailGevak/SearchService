
# storage

Simple destributed key-value storage. 
To set a count of sorage's nodes on the host it's need to set property *storage.ports*=[...]. 
To set a name of the main node (which routes requests to storage's nodes) ot's need to set property *storage.actor="..."*.
```
storage
{
  actor="StorageProvider"
  =[0,0,0,0,0]
} 
```
You can use storage on your system usinf akka's extension *com.conductor.storage.StorageExtension*
# rest

Simple rest-service which uses  *com.conductor.storage.StorageExtension*. Thereare three end-points:
  - Put request */document/$key* to store document
  - Get request */document/$key* to get document. It returns 404 response if document can't be found.
  - Get request */document/query=[...]* to get document's key which contains all tokens. 
 
 To run service use com.conductor.rest.RestApp
 To set *host* and *port* of the service use properies *http.host* and *http.port*
  
# client
  Contains implementation *com.conductor.client.RestClient* of trait 
  ```
  trait com.conductor.client.Client{
    def putDocument(key: String, document: String): Future[Unit]
    def getDocument(key: String):  Future[Option[String]]
    def searchDocument(tokens: Set[String]): Future[Set[String]]
  }
  ```
  
