# Devoxx LeadTracker

Lead tracker back-end application

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

Devoxx LeadTracker is written in Scala and uses the Play framework. You need to install several tools.

#### SBT

[SBT](http://www.scala-sbt.org) is the Scala build tool. Check the [documentation](http://www.scala-sbt.org/0.13/docs/).

```
$ brew install sbt  
$ sbt about
[info] This is sbt 0.13.11
```

#### Intellij IDEA

If you use [Intellij IDE](https://www.jetbrains.com/idea/) IDE you will need to install the Scala plugin to be able to develop.

* Scala and SBT Plugin : check the [page](https://www.jetbrains.com/help/idea/2016.3/creating-and-running-your-scala-application.html) and download/install the [plugin](https://plugins.jetbrains.com/plugin/?idea&id=1347) directly from the IDE

Then install the Scala SDK in Intellij IDEA

### Pre requis
 To run the application, you must have local database. You could run one postgresql DB in docker container with the following command :

	docker run  -p 5432:5432 -e POSTGRES_PASSWORD=test -e POSTGRES_USER=fabszn -e POSTGRES_DB=leadtracker  -d postgres:9.6

### local configuration

Into application.conf file, there is information for database connectivity for the clever cloud environment.
To ease the local configuration and avoid the modification for clever cloud env, you have to create a file named :

    local.conf
This file must be located in conf directory.  In this file, you can add your local configuration for database.

sample : 

    
    db_server_url = "jdbc:postgresql://localhost"
    
    db {
        leadTrackerDb = ${db_leadTrackerDb} {
        url = ${db_server_url}"/leadtracker"
        username = "fabszn"
        password = "test"

        }
    }
 Information above are linked with information provided to the docker image. It will overrided information contained in application.conf
 
 /!\ This file mustn't be commited! 

### Installing

Once you have all the code, you can run the following command :

```
sbt run
```

This will compile all the code, and deploy it. Just check you have access to the following URL:

[http://localhost:9000/status]() : status information 


## Running the tests

Explain how to run the automated tests for this system

```
Give an example
```

## Development

Coding styles are described in the `.editorconfig` file.

## Architecture

The Devoxx Lead Tracker is made of several services : 

* Attendee
* Authorization
* Lead Tracker

## Deployment

Add additional notes about how to deploy this on a live system

## Built With

* [Lagom](https://www.lightbend.com/lagom) - The microservice framework
* [SBT](http://www.scala-sbt.org/) - Dependency Management

## Authors

See the list of [contributors](https://github.com/devoxx/devoxx-leadTracker/graphs/contributors) who participated in this project.
