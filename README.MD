![Build Status](https://travis-ci.com/secureCodeBox/scanner-webapplication-zap.svg?token=2Rsf2E9Bq3FduSxRf6tz&branch=develop)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Known Vulnerabilities](https://snyk.io/test/github/secureCodeBox/scanner-webapplication-zap/badge.svg)](https://snyk.io/test/github/secureCodeBox/scanner-webapplication-zap)
# About
This repository contains a self contained µService utilizing the OWASP ZAP Proxy scanner for the secureCodeBox Application.

Further Documentation:
* [Project Description][scb-project]
* [Developer Guide][scb-developer-guide]
* [User Guide][scb-user-guide]

# How to build and start the Zap Webapplication Scanner

By default the scanprocess gets started automatically with the startup of the secureCodeBox. However, if you want to start the scanner manually, you can do so by following the steps below.

## Local Setup

1. Clone the repository
2. In the root folder in a terminal do `gradle clean build` (If you don't have gradle installed use the provided `gradlew` file)
3. Start the Application with `gradle bootRun`

>**Note**: This is a SpringBoot Java Application so of course you can also open the project in any Java IDE like IntelliJ and start the scanner from there.

## Configuration Options

To configure this service specify the following environment variables:

| Environment Variable       | Value Example         | Description          |
| -------------------------- | --------------------- |--------------------- |
| ENGINE_ADDRESS             | http://engine         |                      |
| ENGINE_BASIC_AUTH_USER     | username              |                      |
| ENGINE_BASIC_AUTH_PASSWORD | 123456                |                      |
| SECURECODEBOX_REDUCE_SPIDER_RESULT_ON_REST_SCHEMAS | true/false | Enables a deduplication feature to filter identical URLs |

### Example for the deduplication feature
If `SECURECODEBOX_REDUCE_SPIDER_RESULT_ON_REST_SCHEMAS` is `true` the following URLs will be filtered:
- http://bodgeit:8080/bodgeit/foo/123/bar -> spider result
  - http://bodgeit:8080/bodgeit/foo/124/bar -> filtered out
  - http://bodgeit:8080/bodgeit/foo/1555/bar -> filtered out
- http://bodgeit:8080/bodgeit/foo12/1555/bar -> spider result
  - http://bodgeit:8080/bodgeit/foo12/1336/bar -> filtered out  
  - http://bodgeit:8080/bodgeit/foo12/1336/bar -> filtered out  
- http://bodgeit:8080/bodgeit/foo12/1336/bar/123 -> spider result
  - http://bodgeit:8080/bodgeit/foo12/1336/bar/12666 -> filtered out


## Build with docker

To build the docker container run: `docker build -t CONTAINER_NAME:LABEL .`


[scb-project]:              https://github.com/secureCodeBox/secureCodeBox
[scb-developer-guide]:      https://github.com/secureCodeBox/secureCodeBox/blob/develop/docs/developer-guide/README.md
[scb-developer-guidelines]: https://github.com/secureCodeBox/secureCodeBox/blob/develop/docs/developer-guide/README.md#guidelines
[scb-user-guide]:           https://github.com/secureCodeBox/secureCodeBox/tree/develop/docs/user-guide
