![Build Status](https://travis-ci.com/secureCodeBox/scanner-webapplication-zap.svg?token=2Rsf2E9Bq3FduSxRf6tz&branch=develop)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Known Vulnerabilities](https://snyk.io/test/github/secureCodeBox/scanner-webapplication-zap/badge.svg)](https://snyk.io/test/github/secureCodeBox/scanner-webapplication-zap)


# How to build and start the Zap Webapplication Scanner

By default the scanprocess gets started automatically with the startup of the secureCodeBox. However, if you want to start the scanner manually, you can do so by following the steps below.

## Local Setup

1. Clone the repository
2. In the root folder in a terminal do `gradle clean build` (If you don't have gradle installed use the provided `gradlew` file)
3. Start the Application with `gradle bootRun`

>**Note**: This is a SpringBoot Java Application so of course you can also open the project in any Java IDE like IntelliJ and start the scanner from there.

## Build with docker

To build the docker container run: docker build -t CONTAINER_NAME:LABEL .
