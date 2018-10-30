[![Build Status](https://travis-ci.com/secureCodeBox/scanner-webapplication-zap.svg?branch=develop)](https://travis-ci.com/secureCodeBox/scanner-webapplication-zap)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Known Vulnerabilities](https://snyk.io/test/github/secureCodeBox/scanner-webapplication-zap/badge.svg)](https://snyk.io/test/github/secureCodeBox/scanner-webapplication-zap)
[![GitHub release](https://img.shields.io/github/release/secureCodeBox/scanner-webapplication-zap.svg)](https://github.com/secureCodeBox/scanner-webapplication-zap/releases/latest)

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

# Configuration Options

## Environment Variable Options

To configure this service specify the following environment variables:

| Environment Variable       | Value Example         | Description          |
| -------------------------- | --------------------- |--------------------- |
| ENGINE_ADDRESS             | http://engine         | Configures the Engine API Endpoint to connect with                     |
| ENGINE_BASIC_AUTH_USER     | username              |                      |
| ENGINE_BASIC_AUTH_PASSWORD | 123456                |                      |

## Application Properties

### securecodebox.zap.filterSpiderResults and securecodebox.zap.filterScannerResults
If the ZAP Spider or Scanner returns a list of target URLs with similar query parameter pairs, then a filter function can drop them as duplicated. This filter can  be configured in the application.yml.

`securecodebox.zap.filterSpiderResults=true`

*Example Spider Results (without filtering):*
- https://bodgeit:8080/bodgeit/
- https://bodgeit:8080/bodgeit/about.jsp
- https://bodgeit:8080/bodgeit/product.jsp?typeid=6
- https://bodgeit:8080/bodgeit/product.jsp?typeid=7
- https://bodgeit:8080/bodgeit/product.jsp?typeid=10
- https://bodgeit:8080/bodgeit/contact.jsp
- https://bodgeit:8080/bodgeit/product.jsp?typeid=5
- https://bodgeit:8080/bodgeit/product.jsp?prodid=17
- https://bodgeit:8080/bodgeit/product.jsp?prodid=10
- https://bodgeit:8080/bodgeit/product.jsp?prodid=9

*Example Spider Results (with filtering):*
- https://bodgeit:8080/bodgeit/
- https://bodgeit:8080/bodgeit/about.jsp
- https://bodgeit:8080/bodgeit/product.jsp?typeid=6
- https://bodgeit:8080/bodgeit/contact.jsp
- https://bodgeit:8080/bodgeit/product.jsp?prodid=9


## How to start a ZAP Scan Process
Please have a look at our [User Guide][scb-user-guide] documentation, which describes in general how to start a scan process, based on the UI or REST API. There is also a [ZAP specific example][scb-user-guide-zap-example].

## Build with docker
To build the docker container run: `docker build -t CONTAINER_NAME:LABEL .`

[scb-project]:                  https://github.com/secureCodeBox/secureCodeBox
[scb-developer-guide]:          https://github.com/secureCodeBox/secureCodeBox/blob/master/docs/developer-guide/README.md
[scb-developer-guidelines]:     https://github.com/secureCodeBox/secureCodeBox/blob/master/docs/developer-guide/README.md#guidelines
[scb-user-guide]:               https://github.com/secureCodeBox/secureCodeBox/tree/master/docs/user-guide
[scb-user-guide-zap-example]:   https://github.com/secureCodeBox/secureCodeBox/blob/master/docs/user-guide/usage-examples/zap-bodgeit-example.md
