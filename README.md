## Spring demo microservice
[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/jorgebsa/spring-demo/Java%20CI%20with%20Gradle?style=for-the-badge)](https://github.com/jorgebsa/spring-demo/actions/workflows/gradle.yml) 
[![Codecov](https://img.shields.io/codecov/c/github/jorgebsa/spring-demo?style=for-the-badge&token=55F3FYDVUN)](https://codecov.io/gh/jorgebsa/spring-demo) 
[![GitHub last commit](https://img.shields.io/github/last-commit/jorgebsa/spring-demo?style=for-the-badge)](https://github.com/jorgebsa/spring-demo/commits/main) 
[![GitHub commit activity](https://img.shields.io/github/commit-activity/m/jorgebsa/spring-demo?style=for-the-badge)](https://github.com/jorgebsa/spring-demo/commits/main)

[![Java version](https://img.shields.io/badge/Java%20version-17-brightgreen?style=for-the-badge)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot version](https://img.shields.io/badge/Spring%20Boot%20version-2.6.0-brightgreen?style=for-the-badge)](https://docs.spring.io/spring-boot/docs/2.6.0/reference/html/getting-started.html#getting-started)
[![Gradle wrapper version](https://img.shields.io/badge/Gradle%20version-7.3.1-brightgreen?style=for-the-badge)](https://docs.gradle.org/7.3.1/release-notes.html)
[![MongoDB container version](https://img.shields.io/badge/MongoDB%20version-5.0.4-brightgreen?style=for-the-badge)](https://docs.mongodb.com/manual/release-notes/5.0-changelog/#std-label-5.0.4-changelog)

## Table of contents

1. [Introduction](#introduction)
2. [About the API](#about-the-api)
3. [Language Version](#language-version)
   1. [JDK 17](#jdk-17)
   2. [Managing JDKs with SDKMAN!](#managing-jdks-with-sdkman!)
   3. [Available Toolchains](#available-toolchains)
4. [How to Build](#how-to-build)
5. [Testing](#testing)
   1. [Why JUnit 5?](#why-junit-5?)
   2. [Why AssertJ?](#why-assertj?)
   3. [Why Testcontainers?](#why-testcontainers?)
   4. [What is JaCoCo?](#what-is-jacoco?)
      1. [JaCoCo logging](#jacoco-logging)
   5. [Mutation Testing](#mutation-testing)
   6. [Future-proofing](#future-proofing)
6. [Required services](#required-services)
   1. [Docker Compose](#docker-compose)
   2. [Keycloak](#keycloak)
7. [Running the microservice](#Running the microservice)
   1. [Running the application with Gradle](#running-the-application-with-gradle)
   2. [Consuming the API](#consuming-the-api)
8. [Contributing](#contributing)

## Introduction

This project is about showcasing some of the best practices and technologies that can be 
used to ensure code quality while developing a microservice in Java with Spring Boot. 
Therefore, the focus isn't on creating a complex API but rather on how to achieve a high 
code quality standard, using a variety of mature and free libraries and technologies.

The project has been implemented with:
* Language: Java 17
* Framework: Spring Boot 
* Database: MongoDB
* Build tool: Gradle
* Security: Spring Security + Keycloak

Relevant testing information and libraries will be described in the appropriate section
of this document.

If you are not familiar with Gradle, the most relevant commands and tasks will be explained
in the following sections. If you need further help, check its [documentation](https://docs.gradle.org/7.3.1/userguide/userguide.html)
or execute the help task:

```shell
./gradlew help
```

## About the API

The API is rather simple: this microservice allows users to keep track of their "notes", 
and offers endpoints for the basic [CRUD](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete) 
operations. 

Endpoints are secured by OAuth2, so each request must include a valid access token to be
processed. These tokens are issued by Keycloak, and the instructions on how to obtain them
will be detailed in a following section of this document.

No frontend application was developed, but a Swagger UI is available in order to explore
and consume the API.

## Language Version

Gradle's [toolchain](https://docs.gradle.org/7.3.1/userguide/toolchains.html) concept is 
used in order to define which java version will be used to build the project. This is 
declared in the [build.gradle.kts](build.gradle.kts) file:

```
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
```

### JDK 17

Since the project is implemented with Java 17, a JDK for Java 17 must be available on your 
machine in order to build this project.

Don't worry! If a matching JDK is not found by Gradle on your machine, it will automatically
download it before building the project. If you want Gradle to download a distribution JDK 
from a specific vendor, you can do so by setting the [vendor property](https://docs.gradle.org/7.3.1/userguide/toolchains.html#sec:vendors)
in the [build.gradle.kts](build.gradle.kts) file:

```
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}
```

### Managing JDKs with SDKMAN!

Most developers want to control which JDKs are installed on their machines. If you would
like to manage JDKs by yourself, you can use tools such as [SDKMAN!](https://sdkman.io/) 
to simplify that task. This is not meant to compare SDKMAN! with any other tool, but rather
explain how you can easily manage SDKs with a mature tool.

The [installation](https://sdkman.io/install) is very straightforward, and you can easily
install any JDK with a single command. For example, in order to install GraalVM 21.3.0 
with Java 17 you can execute the following command:

```shell
sdk install java 21.3.0.r17-grl 
```

After installing it, SDKMAN! will ask if you want to set it as the default JDK on your 
machine. If you chose to not set it as the default JDK, you can later set it as the 
active JDK in your terminal window by running the `use` command:

```shell
sdk use java 21.3.0.r17-grl
```

More info on SDKMAN usage can be found [here](https://sdkman.io/usage)

### Available Toolchains

In order to check which toolchains are available to Gradle, you can execute the 
`javaToolchains` task:

```shell
./gradlew javaToolchains
```

Here's an example of the output you should expect from the task when multiple 
toolchains are available:

```
> Task :javaToolchains

 + GraalVM Community JDK 11.0.13+7-jvmci-21.3-b05
     | Location:           /Users/username/.sdkman/candidates/java/21.3.0.r11-grl
     | Language Version:   11
     | Vendor:             GraalVM Community
     | Is JDK:             true
     | Detected by:        Current JVM

 + GraalVM Community JDK 17.0.1+12-jvmci-21.3-b05
     | Location:           /Users/username/.sdkman/candidates/java/21.3.0.r17-grl
     | Language Version:   17
     | Vendor:             GraalVM Community
     | Is JDK:             true
     | Detected by:        SDKMAN!

 + OpenJDK 18-ea+24-1608
     | Location:           /Users/username/.sdkman/candidates/java/18.ea.24-open
     | Language Version:   18
     | Vendor:             Oracle
     | Is JDK:             true
     | Detected by:        SDKMAN!

 + Oracle JDK 1.8.0_162-b12
     | Location:           /Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home
     | Language Version:   8
     | Vendor:             Oracle
     | Is JDK:             true
     | Detected by:        macOS java_home
```

## How to Build

Gradle was chosen as the build tool for this project and its wrapper has been included 
in the repository. There are at least two very good reasons to advocate for wrapper
inclusion and usage:

* Project can be immediately built after cloning is finished, there's no need for 
the developer to install Gradle beforehand.
* The wrapper will always match the expected Gradle version that is meant to be used
when building the project. 

In order to build the project, execute the following command:

```shell
./gradlew build
```

Please note that the `build` task will also execute the tests. If you simply want to 
compile the source code and resources, you can do so by executing the `assemble` task:

```shell
./gradlew assemble
```

## Testing

Testing is crucial to ensure code is reliable, maintainable, reusable, extensible. All
of these factors influence the code's quality standard.

Many libraries can be used to improve the testing experience. In this project some great
libraries and tools are showcased, such as:

* [JUnit 5](https://junit.org/junit5/) to create and run our tests
* [AssertJ](https://joel-costigliola.github.io/assertj/) to write amazing assertions in 
our test cases
* [Testcontainers](https://www.testcontainers.org/) to reproduce a real environment for
our tests
* [JaCoCo](https://www.jacoco.org/jacoco/trunk/index.html) to generate the coverage 
report of our project
* [Pitest](https://pitest.org/) to execute mutation testing
* [Codecov](https://about.codecov.io/) to keep track of our repository's coverage

Integration and Unit tests can be found in the `src/test` directory. Usually, they
could've been split into different source sets, such as `src/test` for unit tests and 
`src/it` for integration tests, but that was not deemed necessary for this project's 
purpose, given its size, complexity and scope. However, this might change in the future.

In order to run the tests in this project, execute the `test` task:

```shell
./gradlew test
```

When execution ends, reports will be available at:

* `build/reports/tests/test` for JUnit reports
* `build/reports/jacoco/test` for JaCoCo reports

### Why JUnit 5?

JUnit is the most used testing platform for Java, with version 5 being its most 
recent major version. It has many features and advantages over JUnit 4, some
of which will be used in our tests, such as:

* `@ParameterizedTest`: this allows the execution of the same test case with any 
number of varying inputs (example: testing the same endpoint with different payloads)
* `Assertions.assertAll(...)`: executes all assertions in a block without failing
the test as soon as the first assertion fails, allowing for all failed assertions 
to be grouped and displayed together
* `Assertions.assertThrows(...)`: which helps the assertion of an exception and 
its properties 

### Why AssertJ?

The purpose of this project is not to compare assertion libraries, such as AssertJ,
Truth, Hamcrest and others. 

AssertJ is an amazing library, and is already included when importing the Spring 
Boot's testing dependency, so that's why it was used in this project. 

Writing meaningful and helpful assertions with ease is the goal, and AssertJ does a 
great job at it.

### Why Testcontainers?

You want your API to be reliable, extensible, maintainable, reusable, and that 
assumption has many consequences. 

One way to ensure that you don't break anything when developing new features or fixing
bugs is to have a very thorough test suite, and having a continuous integration pipeline
that automatically runs those tests and lets you know when things go wrong.

However, what if your tests do not reproduce the real environment? The execution of the
test suite can be successful and once you deploy the new version to your real world 
production environment, things that you were lead to assume were fine still run the risk
of breaking because the environment they were deployed to was nothing like what you 
tested against.

One of the ways to reduce this kind of problem is to use real instances of the services that
the application relies on, with the same version as the ones in your production environment.
For example, instead of testing an application against an embedded database, have it connect
to a real database server. 

This is where Testcontainers comes in handy, allowing you to spin up disposable Docker 
containers that will live only during the execution of your test suite. 

In this case, the existing `org.testcontainers:mongodb` dependency was used to import its 
`MongoDBContainer` class which was extended in the `ExtendedMongoDBContainer` class in 
order to ensure that the appropriate `URL` connection value was made available to Spring
Data once the container was ready to accept connections.

For the Keycloak authorization server, the `com.github.dasniko:testcontainers-keycloak` 
dependency was used. Similarly to the MongoDB case, the `KeycloakContainer` was extended 
by the `ExtendedKeycloakContainer` class, which is used to configure Keycloak's test realm 
as well as making its auth URL available to Spring Security.

#### IMPORTANT!

Using `testcontainers` means that most of the project's tests REQUIRE docker to be available 
in the host machine in order to run at all.

The Docker images being used in the tests are:

* `mongo:5.0.4`
* `quay.io/keycloak/keycloak:15.0.2`

If they are not found in the host machine, `testcontainers` will automatically download 
them. This can give the impression that the tasks are taking too long to start or finish,
so if you wish to manually download the images before executing the tests, you can by
executing the `pull` command:

```shell
docker pull mongo:5.0.4
docker pull quay.io/keycloak/keycloak:15.0.2
```

### What is JaCoCo?

JaCoCo is an amazing tool that generates code coverage reports for java. In order to 
generate these reports, the [JaCoCo Gradle plugin](https://docs.gradle.org/7.3.1/userguide/jacoco_plugin.html) 
is used, and is declared in the [build.gradle.kts](build.gradle.kts) plugins block. 

The plugin provides two useful tasks: 

* `jacocoTestReport`:  Generates the report and depends on the output of tests. In this
project, the gradlew `test` task is finalized by this task, so that the coverage report
is always generated after test execution
* `jacocoTestCoverageVerification`: verifies if the coverage rules are met. If a project
declares min 99% coverage, the task will fail if the coverage is below that threshold

In this project, the plugin is configured to generate both HTML and XML reports. 
 * HTML provides the best visual experience, as it allows a developer to navigate through
the packages and classes, checking all the coverage details.
 * The XML report is created so that is exported to [Codecov](https://codecov.io/) when 
the GitHub Actions workflow is executed

These reports can be found in `build/reports/jacoco`, and they include information about:

* Class coverage 
* Method coverage 
* Branch coverage 
* Line coverage
* Instruction coverage 
* Complexity coverage 

More information on what those counters mean can be found [here](https://www.jacoco.org/jacoco/trunk/doc/counters.html).

#### JaCoCo logging

Another neat plugin used is [jacocolog](https://gitlab.com/barfuin/gradle-jacoco-log),
which provides the`jacocoLogTestCoverage` task which prints all those metrics 
in the console in case you are only looking for the consolidated coverage numbers

Having near 100% code coverage does not mean that a software is bug-free or flawless.
But it usually means that a team is invested into making it reliable, and that type
of confidence in the code base provides a lot of comfort when planning and building
new features or simply fixing bugs. 

### Mutation Testing

This is not a very widespread concept, but is a rather powerful one. Below is the 
explanation of why you should care about it, from the folks at [Pitest](https://pitest.org):

> Traditional test coverage (i.e line, statement, branch, etc.) measures only which 
> code is executed by your tests. It does not check that your tests are actually able
> to detect faults in the executed code. It is therefore only able to identify code
> that is definitely not tested.
>
> The most extreme examples of the problem are tests with no assertions. Fortunately 
> these are uncommon in most code bases. Much more common is code that is only 
> partially tested by its suite. A suite that only partially tests code can still 
> execute all its branches (examples).
> 
> As it is actually able to detect whether each statement is meaningfully tested, 
> mutation testing is the gold standard against which all other types of coverage
> are measured.

In order to run mutation testing, the [Pitest](https://gradle-pitest-plugin.solidsoft.info/)
Gradle plugin is used. It provides the `pitest` gradle task, which runs the mutated 
tests, and the generated report can be found in`build/reports/pitest/index.html`

#### IMPORTANT!

Given many mutations are generated for each test case, the execution time of the 
`pitest` task is much longer than the time from the `test` task. Since this may take
several minutes, there's no GitHub action configured for it.

The current version of the Pitest Gradle plugin is not compatible with the Gradle 
Toolchain concept. Therefore, if the JVM used by Gradle to run does not match the 
language version declared by the toolchain, the execution of `pitest` task will
fail. In order to avoid this, make sure you set the JDK used by Gradle to be
compatible with the toolchain version. If you are using SDKMAN! this can be easily
done by executing the `sdk use java` command before you invoke the `pitest` task.

### Future-proofing

One way to future-proof your application is to test it against early access builds of
future JDKs. This can save you a lot of time when migrating to newer versions of the
JDK, as it will make it clear if your current code and dependencies are compatible
with the next release.

Since this project is written in Java 17, the only early access builds it can be tested
against are from JDK 18 (at the time of writing this). Therefore, a Gradle task called 
`testsOn18` has been created and declared in the [build.gradle.kts](build.gradle.kts) 
file. This task sets the Java Toolchain to use Java Language Version 18 and executes the
project's tests.

To run the tests on JDK 18, execute the following command:

````shell
./gradlew testsOn18
````

Once the tests finish, you can access the reports at `build/reports/tests/testsOn18`

Please note that since an early access build of the JDK is required to test against a
future version of the JDK, you must install it manually, as Gradle's toolchain isn't 
capable of downloading one. This might change in the future, as Gradle has an open 
[issue](https://github.com/gradle/gradle/issues/14814) for this feature. 

If you are using SDKMAN! as suggested in the previous sections, you can easily
download an early access build of the JDK. At the time of writing, the latest build
was `18.ea.25` which can be installed by executing:

```shell
sdk install java 18.ea.25-open
```

Once installed, Gradle's Toolchain is capable of automatically locating the early
access build of the JDK and use it when required.

#### IMPORTANT

The current version of JaCoCo is `0.8.7` and it isn't compatible with Java 18, 
therefore an exception will be thrown when running the tests on 18. However, this 
doesn't mean that the tests have failed or won't be executed, just that JaCoCo won't
be able to generate its reports from the test output. This should be fixed when 
JaCoCo version `0.8.8` is released.

## Required services

This microservice requires both MongoDB and Keycloak servers to be available in order
to function. If you have both servers installed or available on a remote URL, you can 
use such instances, just don't forget to update corresponding properties on 
`src/main/resources/application.yml` (or another profile's file) with the appropriate
connection URLs.

If you don't want to alter the properties file, you can also provide the URIs as 
JVM properties

```
-Dkeycloak.auth-server-url=http://some.host:8080/auth
-Dspring.data.mongodb.uri=mongodb://user:password@some.host:27017/spring-demo
```

or even as application arguments.

```
--keycloak.auth-server-url=http://some.host:8080/auth
--spring.data.mongodb.uri=mongodb://user:password@some.host:27017/spring-demo
```

### Docker Compose

However, you can also easily spin up both MongoDB and Keycloak as docker containers. 
A file called `docker-compose.yml` has been added to the project, and you can use 
[Docker Compose](https://docs.docker.com/compose/) in order to take advantage of it.

To start the containers with Docker Compose, execute:

```shell
docker-compose up -d
```

To stop them, execute:

```shell
docker-compose down
```

Please note that the services described in the `docker-compose.yml` file do not 
have persistent storage configured, which will cause modifications to the 
databases to be lost once the containers are stopped. This is intentional 
behaviour. 

If you'd like data to be persisted when the containers are stopped, you can use
the `docker-compose-persistent.yml` file instead. To do so, execute the commands
above with the `-f` flag:

```shell
docker-compose -f docker-compose-persistent.yml up -d
docker-compose -f docker-compose-persistent.yml down
```

The main difference in this approach is that PostgreSQL is used as the backing
database for Keycloak, running on its own container, and that both MongoDB and 
PostgreSQL containers are mounting data [volumes](https://docs.docker.com/storage/volumes/),
which are managed by the Docker Engine, in the appropriate container directories 

### Keycloak

Keycloak uses the concept of [realm](https://huongdanjava.com/overview-about-realm-in-keycloak.html) 
to manage Users, Clients, Roles and other security details. There are many ways
to configure a realm:

* You can do it [manually](https://www.keycloak.org/docs/latest/server_admin/#_create-realm)
* You can use Keycloak's [REST API](https://www.keycloak.org/docs-api/15.0/rest-api/index.html) 
(or one if its existing clients)
* You can import an existing `realm` configuration.

In the [realm.json](realm.json) file, you can inspect the basic Keycloak realm 
configuration needed in order for the microservice to work as expected. This file
is automatically imported by Keycloak when it starts if you use the project's 
provided docker compose files.

During tests, instead of importing the realm, it is created programmatically. 
This is done in the `com.github.jorgebsa.spring.demo.util.ExtendedKeycloakContainer` 
class. To achieve this, the `Keycloak` REST client from the 
`org.keycloak:keycloak-admin-client` dependency is used. 

## Running the microservice

Once your MongoDB and Keycloak instances are reachable, there are a few ways
to launch the application: 

* You can run it directly from your IDE, by invoking the main method of
`com.github.jorgebsa.spring.demo.Application`
* You can run it using the [Spring Boot Gradle plugin](https://docs.spring.io/spring-boot/docs/2.6.0/gradle-plugin/reference/htmlsingle/#running-your-application)

### Running the application with Gradle
 
Is as easy as invoking a simple [task](https://docs.spring.io/spring-boot/docs/2.6.0/gradle-plugin/reference/htmlsingle/#running-your-application)
from the Spring Boot Gradle Plugin:

```shell
./gradlew bootRun
```

### Consuming the API

After the application starts, you can send HTTP requests directly to it or 
access the Swagger UI available at `http://localhost:8080/swagger-ui.html`.

Remember that the endpoints are secured, therefore each request must include
an access token in its `Authorization` header. To obtain an access token, you
must request one from Keycloak with the appropriate payload, for example:

```http request
POST http://localhost:8081/auth/realms/tests/protocol/openid-connect/token
Accept: application/json
Cache-Control: no-cache
Content-Type: application/x-www-form-urlencoded

client_id=spring-demo&client_secret=the-secret&username=the-admin&password=123Admin&grant_type=password
```

You are looking for the `access_token` value from the response body, which will
be in the following format:

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ2WENxeHpWT25aNW92a2Y5N0x0UEdZX0JXUkNuSWVRbUYwNU5nUUVzb2hRIn0.eyJleHAiOjE2Mzc5ODE3NjEsImlhdCI6MTYzNzk0NTc2MiwianRpIjoiMmU0ZDgxM2EtM2EwMS00MTg1LWI0NTUtY2EzNzY4ZDljYTNhIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgxL2F1dGgvcmVhbG1zL3Rlc3RzIiwic3ViIjoiNDAxZTVjZGEtNzEzOS00YzljLWFjMjUtYWIxYTE5MjYzZDM5IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic3ByaW5nLWRlbW8iLCJzZXNzaW9uX3N0YXRlIjoiOWZlYzU4MzktMjNiZS00Y2RiLThiOWQtMTQ4OTg5NjMzNmJiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8vbG9jYWxob3N0OjgwODAiXSwicmVzb3VyY2VfYWNjZXNzIjp7InNwcmluZy1kZW1vIjp7InJvbGVzIjpbIm5vdGVzLWFkbWluIiwibm90ZXMtdXNlciJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsInNpZCI6IjlmZWM1ODM5LTIzYmUtNGNkYi04YjlkLTE0ODk4OTYzMzZiYiIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoidGhlLWFkbWluIn0.CII62FsOdCfe9qIyf9BfzBJUbfWjODgP6-BuhqFjQAeqt12yIXGcGXcyy4ZD55uYaBywMovOofMlmhIT_IGx6bRAHLwjYq6sH-TW_5wC_42rrZ4---UWkQn1zn8atdYJnDNHagEPRqMnzvE0R38Nk2otFiVpiYGodP_K-LuoORPKtqpb4LdbiR9kM-uhIuYnqxv_4cJuhH_3-wBFBuhI2uuAi12aHwrgfgWKDkAgc0I4uwNaQ8jIKG7MkeJhPOu81p1kYM99CK2mDYFhjggroR_wdwlvIG33Z_V2tVvHxuNdUCeg7JXTwGMT6t2xRGApW0I0IqeEQ_AdBitxBEeTaA",
  "expires_in": 35999,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJmMGVjMzU3OC1kZTA1LTRkZmItOWFjMC1kZWEwMTYzY2EzYjgifQ.eyJleHAiOjE2Mzc5NDc1NjIsImlhdCI6MTYzNzk0NTc2MiwianRpIjoiZjIzOWM1NzUtYzlhZC00OWNmLThmZGEtMDgwNzc4MDY0Y2Q1IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgxL2F1dGgvcmVhbG1zL3Rlc3RzIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgxL2F1dGgvcmVhbG1zL3Rlc3RzIiwic3ViIjoiNDAxZTVjZGEtNzEzOS00YzljLWFjMjUtYWIxYTE5MjYzZDM5IiwidHlwIjoiUmVmcmVzaCIsImF6cCI6InNwcmluZy1kZW1vIiwic2Vzc2lvbl9zdGF0ZSI6IjlmZWM1ODM5LTIzYmUtNGNkYi04YjlkLTE0ODk4OTYzMzZiYiIsInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsInNpZCI6IjlmZWM1ODM5LTIzYmUtNGNkYi04YjlkLTE0ODk4OTYzMzZiYiJ9.RCiVcFlkK3hkHtFhVJHKze1oBf_upmB6vxVuZyCWYxk",
  "token_type": "Bearer",
  "not-before-policy": 0,
  "session_state": "9fec5839-23be-4cdb-8b9d-1489896336bb",
  "scope": "profile email"
}
```

Once you have the access token, you can use it to Authorize Swagger UI's requests by 
clicking on the `Authorize` button and pasting the value into the field. If you opt
to work with traditional HTTP requests, make sure to set the value of the `Authorization`
header to be of type `Bearer`, here's an example with the access token from above:

```http request
GET http://localhost:8080/notes
Accept: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJmMGVjMzU3OC1kZTA1LTRkZmItOWFjMC1kZWEwMTYzY2EzYjgifQ.eyJleHAiOjE2Mzc5NDc1NjIsImlhdCI6MTYzNzk0NTc2MiwianRpIjoiZjIzOWM1NzUtYzlhZC00OWNmLThmZGEtMDgwNzc4MDY0Y2Q1IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgxL2F1dGgvcmVhbG1zL3Rlc3RzIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgxL2F1dGgvcmVhbG1zL3Rlc3RzIiwic3ViIjoiNDAxZTVjZGEtNzEzOS00YzljLWFjMjUtYWIxYTE5MjYzZDM5IiwidHlwIjoiUmVmcmVzaCIsImF6cCI6InNwcmluZy1kZW1vIiwic2Vzc2lvbl9zdGF0ZSI6IjlmZWM1ODM5LTIzYmUtNGNkYi04YjlkLTE0ODk4OTYzMzZiYiIsInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsInNpZCI6IjlmZWM1ODM5LTIzYmUtNGNkYi04YjlkLTE0ODk4OTYzMzZiYiJ9.RCiVcFlkK3hkHtFhVJHKze1oBf_upmB6vxVuZyCWYxk
```

## Contributing

This project is meant to be a way of showcasing some of the best practices and 
technologies I've come across in the past few years. I'm always interested in
learning more, discussing different points of views and approaches. If you can
contribute to this repository in any meaningful way, be it by showcasing something
new or improving what has been implemented already, you are more than welcome to
open a pull request and start a discussion!