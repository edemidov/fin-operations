# fin-operations
### Used technologies:
* _Platform_ - JVM
* _Language_ - Kotlin
* _Web Framework_ - Spark Java
* _Dependency Injection_ - Google Guice
* _Database Access_ - JetBrains Exposed
* _Database_ - Embedded Postgres
* _Build tool_ - Gradle
* _Tests_ - JUnit5, Mockk, AssertJ, Fuel

*Embedded Postgres is chosen instead of embedded H2 because the latter couldn't handle with ConcurrentTransactionIntegrationTest.
Unfortunately, it takes some time at the first start to download.

### REST API

* http://localhost:8080/api/v1/accounts
    - **/** - read with cursor (GET), create (POST)
    - **/:id** - get by id (GET), update (PUT), remove (DELETE)
    
    ```json
    {
      "id": 1,
      "name": "John Smith",
      "status": "ACTIVE",
      "amount": 1000
    }
    ```
* http://localhost:8080/api/v1/transactions
    - **/** - read with cursor and optional query parameters - sourceAccountId, targetAccountId to narrow down the search (GET), make transaction (POST)
    - **/:id** - get by id (GET)
    
    ```json
    {
      "sourceAccountId": 1,
      "targetAccountId": 2,
      "amount": 100,
      "id": 1,
      "operationTime": "2020-02-19T21:21:40.352052Z"
    }
    ```
  
**Reading collections with cursor**

Example: http://localhost:8080/api/v1/accounts?size=2 returns result in batches with size = 2.
```json
{
  "content": [
    {
      "name": "John Smith",
      "status": "ACTIVE",
      "amount": 1000,
      "id": 1
    },
    {
      "name": "Tim Walker",
      "status": "ACTIVE",
      "amount": 5000,
      "id": 2
    }
  ],
  "nextCursor": 2
}
```
Next batch can be loaded using "nextCursor" from response: http://localhost:8080/api/v1/accounts?size=2&cursor=2
```json
{
  "content": [
    {
      "name": "Bill Green",
      "status": "BLOCKED",
      "amount": 5000,
      "id": 3
    }
  ],
  "nextCursor": 0
}
```
"nextCursor" = 0 means there are no more batches in the result set.

**API Errors**

Exceptions return 4xx response with description. 
There can be several errors in one response:
```json
{
  "errors": [
    {
      "code": 5,
      "message": "Account with id = 1 has insufficient funds for the operation"
    },
    {
      "code": 4,
      "message": "Account with id = 3 is blocked"
    }
  ]
}
```

**Running the project**

The project can be run:
* on Mac/Linux with `./gradlew :run`, tests with `./gradlew :test`
* on Windows with `gradlew.bat :run`, tests with `gradlew.bat :test`