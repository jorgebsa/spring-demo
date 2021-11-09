## Spring demo microservice
[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/jorgebsa/spring-demo/Java%20CI%20with%20Gradle?style=for-the-badge)](https://github.com/jorgebsa/spring-demo/actions/workflows/gradle.yml) 
[![Codecov](https://img.shields.io/codecov/c/github/jorgebsa/spring-demo?style=for-the-badge&token=55F3FYDVUN)](https://codecov.io/gh/jorgebsa/spring-demo) 
[![GitHub last commit](https://img.shields.io/github/last-commit/jorgebsa/spring-demo?style=for-the-badge)](https://github.com/jorgebsa/spring-demo/commits/main) 
[![GitHub commit activity](https://img.shields.io/github/commit-activity/m/jorgebsa/spring-demo?style=for-the-badge)](https://github.com/jorgebsa/spring-demo/commits/main)

[![Java version](https://img.shields.io/badge/Java%20version-17-brightgreen?style=for-the-badge)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot version](https://img.shields.io/badge/Spring%20Boot%20version-2.5.6-brightgreen?style=for-the-badge)](https://docs.spring.io/spring-boot/docs/2.5.6/reference/html/getting-started.html#getting-started)
[![Gradle wrapper version](https://img.shields.io/badge/Gradle%20version-7.2-brightgreen?style=for-the-badge)](https://docs.gradle.org/7.2/release-notes.html)
[![MongoDB container version](https://img.shields.io/badge/MongoDB%20version-5.0.3-brightgreen?style=for-the-badge)](https://docs.mongodb.com/manual/release-notes/5.0-changelog/#std-label-5.0.3-changelog)

## Introduction

This project is about showcasing some of the best practices and technologies that can be used to ensure code quality 
while developing a microservice in Java with Spring Boot. Therefore, the focus won't be to create a complex API but rather 
how to achieve a high code quality standard, using a variety of mature and free libraries and technologies.

The project has been implemented with Java 17, Spring Boot and MongoDB, using Gradle as the build tool.
No frontend application was developed, but a Swagger UI is available in order to explore and consume the API. 
Relevant testing information and libraries will be described in the appropriate section 
of this document.

If you are not familiar with Gradle, the most relevant commands and tasks will be explained in the following sections. 
If you need further help, check its [documentation](https://docs.gradle.org/current/userguide/userguide.html) or 
execute the help task:

```shell
./gradlew help
```

### About the API

The API is rather simple, this microservice allows users to keep track of their "notes", and offers endpoints for the basic
[CRUD](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete) operations.

## How to Build

Gradle was chosen as the build tool for this project, and its wrapper is included in the repository, this way you 
don't have to install Gradle on your machine.

In order to build the project, execute the following command:

```shell
./gradlew build
```

### Gradle's Java Toolchain

We use Gradle's [toolchain](https://docs.gradle.org/current/userguide/toolchains.html) concept in order to define which
java version will be used to build the project. This is declared in the `build.gradle.kts` file:

```
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
```

Since the project was implemented with Java 17, a JDK for Java 17 must be available on your machine in order to build this project. 

Don't worry! If a matching JDK is not found on your machine, gradle will automatically download it before building the project.

## Testing

Testing is crucial to ensure code is reliable, maintainable, reusable, extensible. All of these factors influence the code's 
quality standard.

Many libraries can be used to improve the testing experience. In this project some great libraries and tools are showcased, 
such as:

* [JUnit 5](https://junit.org/junit5/) to create and run our tests
* [AssertJ](https://joel-costigliola.github.io/assertj/) to write amazing assertions in our test cases
* [Testcontainers](https://www.testcontainers.org/) to reproduce a real environment for our tests
* [JaCoCo](https://www.jacoco.org/jacoco/trunk/index.html) to generate the coverage report of our project
* [Pitest](https://pitest.org/) to execute mutation testing
* [Codecov](https://about.codecov.io/) to keep track of our repository's coverage

Integration and Unit tests can be found in the `src/test` directory. Usually, they could've been split into different
source sets, such as `src/test` for unit tests and `src/it` for integration tests, but that was not deemed necessary
for this project's purpose, given its size and complexity. However, this might change in the future.

In order to run the tests in this project, run this command:

```shell
./gradlew test
```

### Why JUnit 5?

JUnit is the most used testing library for Java, 5 being its most recent major version. 
It has many features and advantages over JUnit 4, some of which will be used in our tests, such as:

* `@ParameterizedTest`: this allows the execution of the same test case with any number of varying inputs
* `Assertions.assertAll(...)`: to execute all assertions in a block without failing the test once the first assertion fails
* `Assertions.assertThrows(...)`: which helps asserting exceptions and their properties 

### Why use AssertJ instead of another assertion library?

The purpose of this project is not to compare assertion libraries, such as AssertJ, Truth, Hamcrest and others. 

AssertJ is an amazing library, and is already included when importing the Spring Boot's testing dependency, 
so that's why it was used in this project. 

Writing meaningful and helpful assertions with ease is the goal, and AssertJ does a great job at it.

### Why use Testcontainers instead of an embedded database?

You want your API to be reliable, extensible, maintainable, reusable, and that assumption has many consequences. 

One way to ensure that you don't break anything when developing new features or fixing bugs is to have a very 
thorough test suite, and having a continuous integration pipeline that automatically runs those tests and lets 
you know when things go wrong.

However, what if your tests do not reproduce the real environment? The execution of the test suite can be successful 
and once you deploy the new version to your real world production environment, things that you were lead to assume were fine
still run the risk of breaking because the environment they were deployed to was nothing like what you tested against.

One of the ways to reduce this kind of problem is to use a real database with the same version as the one 
in your production environment. 

This is where Testcontainers comes in handy, allowing you to spin up disposable Docker containers that will
live only during the execution of your test suite. 

In this case, the existing `org.testcontainers:mongodb` dependency was used to import its `MongoDBContainer` class 
which was extended in the `ExtendedMongoDBContainer` class in order to ensure that the appropriate `URL` connection 
value was made available to Spring Data once the container was ready to accept connections.

#### Important:

* Using `testcontainers` means that most of the project's tests REQUIRE docker to be available in the host machine in
order to be run at all.
* The MongoDB Docker image being used is `mongo:5.0.3`. If it is not found in the host machine, `testcontainers` 
will automatically download it
  * If you want to download it manually you can by running `docker pull mongo:5.0.3`

### What is JaCoCo?

JaCoCo is an amazing tool that generates code coverage reports for java. In order to generate these reports, the 
[JaCoCo Gradle plugin](https://docs.gradle.org/current/userguide/jacoco_plugin.html) is used, and is declared 
in the `build.gradle.kts` plugins block. 

The plugin provides two useful tasks: 

* `jacocoTestReport`:  Generates the report and depends on the output of tests. In this project, the gradlew `test` 
task is finalized by this task, so that the coverage report is always generated after test execution
* `jacocoTestCoverageVerification`: verifies if the coverage rules are met. If a project declares min 99% coverage, 
the task will fail if the coverage is below that threshold

In this project, the plugin is configured to generate both HTML and XML reports. 
 * HTML provides the best visual experience, as it allows a developer to navigate through the packages and classes, 
checking all the coverage details.
 * The XML report is created so that is exported to [Codecov](https://codecov.io/) when the GitHub Actions
workflow is executed

These reports can be found in `build/reports/jacoco`, and they include information about:

* Class coverage 
* Method coverage 
* Branch coverage 
* Line coverage
* Instruction coverage 
* Complexity coverage 

More information on what those counters mean can be found [here](https://www.jacoco.org/jacoco/trunk/doc/counters.html).

Another neat plugin used is [jacocolog](https://gitlab.com/barfuin/gradle-jacoco-log), which provides the
`jacocoLogTestCoverage` task which prints all those metrics in the console in case you are only looking for
the consolidated coverage numbers

Having near 100% code coverage does not mean that a software is bug-free or flawless. But usually means that a 
team is invested into making it reliable, and that type of confidence in the code base provides a lot of
comfort when building new features or fixing bugs. 

### Mutation Testing

This is not a very well-known concept, but is a rather powerful one. Below is the explanation of why you should care
about it, from the folks at [Pitest](https://pitest.org):

> Traditional test coverage (i.e line, statement, branch, etc.) measures only which code is executed by your tests. 
> It does not check that your tests are actually able to detect 
> faults in the executed code. It is therefore only able to identify code that is definitely not tested.
>
> The most extreme examples of the problem are tests with no assertions. Fortunately these are uncommon in most code bases.
> Much more common is code that is only partially tested by its suite. A suite that only partially tests code can 
> still execute all its branches (examples).
> 
> As it is actually able to detect whether each statement is meaningfully tested, mutation testing is the gold 
> standard against which all other types of coverage are measured.

In order to run mutation testing, the [Pitest Gradle plugin](https://gradle-pitest-plugin.solidsoft.info/) is used. 
It provides the `pitest` gradle task, which runs the mutated tests, and the generated report can be found in 
`build/reports/pitest/index.html`

#### Important:
* Given many mutations are generated for each test case, the execution time of the `pitest` task is much bigger
than the time from the `test` task 
* When executing the `pitest` task, an extra JVM will be launched, matching the JDK from Gradle. Since the 
`toolchain` approach is being used when building the project, this extra JVM might not use a JDK with version 17,
which will cause the task to fail. In order to avoid this, make sure you set the Gradle JDK to be at version 17+. 
  * One simple way to do this is to use a tool like [SDKMAN](https://sdkman.io/), which manages JDK (and other SDKs) 
on your machine

## Running the microservice

There are a few ways to launch the application: 
* You can run it directly from your IDE, by invoking the main method of `com.github.jorgebsa.spring.demo.Application`
* You can run it using the [Spring Boot Gradle plugin](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#running-your-application)
* You can run it as a Docker container

### MongoDB

Remember that this microservice depends on a MongoDB instance to be available for connection. If you have 
Mongo installed or available on a remote server, you can use such instance, just don't forget to update
`src/main/resources/application.yml` or another profile's properties with the appropriate connection URI.

However, you can also easily spin up a MongoDB docker container. A file called `mongo-docker-compose.yml`
has been added to the project, and you can use [Docker Compose](https://docs.docker.com/compose/) in order 
to take advantage of it.

To start the container with Docker Compose, execute:

```shell
docker-compose -f mongo-docker-compose.yml up -d
```

To stop it, execute:

```shell
docker-compose -f mongo-docker-compose.yml down
```

### Running the application with Gradle

Once your MongoDB is reachable, you can start the application with a simple Gradle [task](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#running-your-application))
from the Spring Boot Plugin

In order to run the database and the service, execute: 

```shell
./gradlew bootRun
```

After the application starts, you can access the Swagger UI available at `http://localhost:8080/swagger-ui.html`
  
### Running the application with Docker

In order to do this, you need to build the project's Docker image. To do so, take advantage of the `bootBuildImage` 
[task](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#build-image) 
from the Spring Boot Gradle Plugin:

```shell
./gradlew bootBuildImage
```

This will generate an image called `docker.io/library/spring-demo:0.0.1-SNAPSHOT`. You can execute it with 
the traditional `docker run` command of take advantage of the `docker-compose.yml` that is already configured 
to launch the container along the MongoDB container. 

To do so, execute:

```shell
docker-compose up -d
```

To stop both the application and the database, execute:

```shell
docker-compose down
```

### TO DO!!

* Add more MongoDB queries
  * Right now there's only findById and findPage, but there are many queries that can be added in order to 
filter the results, such as querying by creation date of last modified date in a range for example
* Integrate with Spring Security 
  * Use Keycloak as the authorization server
  * Remove hardcoded `username` from controller once `Principal` is available to use
  * Allow delete/update of a note only by its owner or an `ADMIN` 
  * Add Keycloak to the project's `docker-compose.yml`
* Improve OpenAPI documentation with annotation usage
