---
title: "Zap"
path: "scanner/Zap"
category: "scanner"
usecase: "Webapplication Vulnerability Scanner"
release: "https://img.shields.io/github/release/secureCodeBox/scanner-webapplication-zap.svg"

---

![zap logo](https://www.owasp.org/images/1/11/Zap128x128.png)

The OWASP Zed Attack Proxy (ZAP) is one of the world’s most popular free security tools and is actively maintained by hundreds of international volunteers*. It can help you automatically find security vulnerabilities in your web applications while you are developing and testing your applications. Its also a great tool for experienced pentesters to use for manual security testing.

<!-- end -->

# About
This repository contains a self contained µService utilizing the OWASP ZAP Proxy scanner for the secureCodeBox Application. To learn more about the Zap scanner itself visit [OWASP_Zap_Project] or [zaproxy.org].

## Zap parameters

To hand over supported parameters through api usage, you can set following attributes:

```json
[
  {
    "name": "zap",
    "context": "some Context",
    "target": {
      "name": "targetName",
      "location": "http://your-target.com/",
      "attributes": {
        "ZAP_BASE_URL": "[String url]",
        "ZAP_AUTHENTICATION": "[true | false]",
        "ZAP_SPIDER_MAX_DEPTH": "[int depth]",
        "ZAP_SPIDER_INCLUDE_REGEX": "[Regex]",
        "ZAP_SPIDER_EXCLUDE_REGEX": "[Regex]",
        "ZAP_LOGIN_SITE": "[String login url]",
        "ZAP_LOGIN_USER": "[String login user]",
        "ZAP_USERNAME_FIELD_ID": "[String username]",
        "ZAP_LOGIN_PW": "[String login pw]",
        "ZAP_SCANNER_INCLUDE_REGEX": "[Regex]",
        "ZAP_SCANNER_EXCLUDE_REGEX": "[Regex]",
        "ZAP_SCANNER_DELAY_IN_MS": "[miliseconds]",
        "ZAP_THREADS_PER_HOST": "[int threads]",
        "ZAP_PW_FIELD_ID": "[String pw id]",
        "ZAP_LOGGED_IN_INDICATOR": "[Regex indicator]",
        "LOGGED_OUT_INDICATOR": "[Regex indicator]",
        "ZAP_SPIDER_API_SPEC_URL": "[String url]",
        "ZAP_CSRF_TOKEN_ID": "[String token id]",
        "ZAP_REPLACER_RULES":  
         [
             { 
               "matchType": "[String matchtype]",
               "description": "[String description]",
               "matchString": "[String matchcontent]",
               "initiators": "[String inititor]",
               "matchRegex": "false",
               "replacement": "[String replacement]",
               "enabled": "[true | false]"
             },
             `...`
        ]
      }
    }
  }
]
```

Like every other scanner in the secureCodeBox, Zap works with the Target and Finding Format. In order to run a Zap Scan you can define multiple targets for the scanner (in most cases you'll only want to define one target). Each of those targets can be configured as follows: 

* `Target Name` defines the name of the target you want to scan
* `Target Hosts` defines the host or IP-address of the target you want to scan (e.g. http://example.org, http://127.0.0.1:8080)
* `Base Url` defines the webapplications base url. This is in most cases the authority of the webapplication (in other words the starting page of your webapplication). Zap uses the base url to create a context based on that value. There are advanced configurations which operate on the context like defining urls in and out of scope of the context. When entering a target for spidering, the base url should always be the same as the Target Hosts parameter. When entering a target for scanning, the base url can be different (This is often the case when targets which were beforehand created by a spider are given to the scanner). 
* `Scan with authentication` checkbox defines, whether the webapplication to scan should be scanned with a user logged in. See [Authentication](#auth)
* `Spider Configuration` defines if you want to configure the spider of Zap manually
* `Scanner Configuration` defines if you want to configure the scanner manually

After configuring each target, you can specify a `business context` under which the scan should be executed. 

## Advanced Configuration

### <a name="auth"></a> Authentication

This configuration lets you define a user configuration which will be used by Zap when scanning the webapplication. The scanner will automatically log in to the webapp with your provided credentials and execute the scan as the user. 
For each target where the checkbox `Scan with authentication` was marked, the following parameters for user authentication need to be specified: 

* `Login Site URL` defines the URL of the login page
* `Login User Name` defines the username that should be logged in
* `Login User Name Field Id` defines the id of the HTML element where the username should be entered
* `Login User Password` defines the password to be used
* `Login Password Field Id` defines the id of the HTML element where the password should be entered
* `Logged In Indicator` defines a regex which specifies an indicator when a user is logged in. This could be for example a present logout button, or a welcome <username> field. 
* `HTML IDs of CSRF Token elements` defines the ids of HTML elements which contain Anti CSRF Tokens

A description of Authentication from Zap itself can be found [here](https://github.com/zaproxy/zap-core-help/wiki/HelpStartConceptsAuthentication). 

### Spider Configuration

For each target where advanced spider configuration was specified the following values can be set: 

* `OpenAPI Specification File` defines a File you can upload if your webapplication supports the OpenAPI Specification
* `Maximum Sitemap Depth` defines the depth, the spider should crawl downwards and take found urls into consideration based on the target url
* `Include RegExe's` defines a Regex Pattern which defines which URLs are in scope of your webapplication. Note that this is based on the specified base url
* `Exclude RegExe's` defines what is out of the webapp scope based on the base url

A description of the spider from Zap can be found [here](https://github.com/zaproxy/zap-core-help/wiki/HelpStartConceptsSpider). 

### Scanner Configuration

For each target where advanced scanner configuration was specified the following values can be set: 

* `Include RegExe's` defines a Regex Pattern which defines which URLs are in scope of your webapplication. Note that this is based on the specified base url
* `Exclude RegExe's` defines what is out of the webapp scope based on the base url
* `Scanner delay in ms` defines the delay between two http requests. This can be used to make the active scan less aggressive
* `Threads per host` defines how many threads the scanner will use per host.


## Automated Execution of Zap

The secureCodeBox Target Format specifies a `name`, `location` and `attributes` field in a target. All of the above mentioned configurations except the target name and url need to be inserted into the attributes field if you want to run the scan automatically. 
The values need to be inserted with keys following the secureCodeBox variable conventions (UPPERCASE and snake_case).
A full example target looks like this: 

```javascript
{
  name: "localhost",
  location: "http://127.0.0.1:8080",
  attributes: [
    ZAP_BASE_URL: "http://127.0.0.1:8080",
    ZAP_AUTHENTICATION: true,
    ZAP_SPIDER_MAX_DEPTH: 5,
    ZAP_SPIDER_INCLUDE_REGEX: "*http://127.0.0.1:8080*",
    ZAP_SPIDER_EXCLUDE_REGEX: "",
    ZAP_LOGIN_SITE: "http://127.0.0.1:8080/login",
    ZAP_LOGIN_USER: "AmazingFranz3",
    ZAP_USERNAME_FIELD_ID: "username",
    ZAP_LOGIN_PW: "i_like_unicorns_and_beer",
    ZAP_SCANNER_INCLUDE_REGEX: "*http://127.0.0.1:8080*",
    ZAP_SCANNER_EXCLUDE_REGEX: "",
    ZAP_SCANNER_DELAY_IN_MS: 10,
    ZAP_THREADS_PER_HOST: 2,
    ZAP_PW_FIELD_ID: "pw",
    ZAP_LOGGED_IN_INDICATOR: "*logout*",
    LOGGED_OUT_INDICATOR: "",
    ZAP_SPIDER_API_SPEC_URL: "",
    ZAP_CSRF_TOKEN_ID: "csrftoken",
    ZAP_REPLACER_RULES:  
     [
         { 
           matchType:"RESP_HEADER",
           description:"Remove CSP",
           matchString:"Content-Security Policy",
           initiators:"",
           matchRegex:"false",
           replacement:"",
           enabled:"true"
         },
         {
            matchType:"REQ_HEADER",
            description:"Add a special Authentication Header",
            matchString:"Authorization",
            initiators:"",
            matchRegex:"false",
            replacement:"Basic QWxhZGRpbjpPcGVuU2VzYW1l",
            enabled:"true"
         }
    ]
  ]
}
```

> **Note**: The attributes in the example are all fields currently supported by the secureCodeBox Zap Scanner. Mandatory is only `ZAP_BASE_URL`. If this field is not present, the target is ignored.

## Zap Addon Replacer

The [Replacer](https://github.com/zaproxy/zap-extensions/wiki/HelpAddonsReplacerReplacer) is used to replace strings in requests and responses and is enabled in the secureCodeBox. It might be useful to to add an authentication header for security testing of APIs (e.g. with an OpenAPI specification).

## Example
Example configuration:

```json
[
  {
    "name": "zap",
    "context": "Example Test",
    "target": {
      "name": "BodgeIT on OpenShift",
      "location": "bodgeit-scb.cloudapps.iterashift.com",
      "attributes": {
        "ZAP_BASE_URL": "bodgeit-scb.cloudapps.iterashift.com"
        }
    }
  }
]
```

Example Output:

```json
{
    "findings": []
  }
```

## Development

### Local Setup

1. Clone the repository
2. In the root folder in a terminal do `gradle clean build` (If you don't have gradle installed use the provided `gradlew` file)
3. Start the Application with `gradle bootRun`

>**Note**: This is a SpringBoot Java Application so of course you can also open the project in any Java IDE like IntelliJ and start the scanner from there.

## Configuration Options

### Environment Variable Options

To configure this service specify the following environment variables:

| Environment Variable       | Value Example         | Description          |
| -------------------------- | --------------------- |--------------------- |
| ENGINE_ADDRESS             | http://engine         | Configures the Engine API Endpoint to connect with                     |
| ENGINE_BASIC_AUTH_USER     | username              |                      |
| ENGINE_BASIC_AUTH_PASSWORD | 123456                |                      |


## Build with docker
To build the docker container run: `docker build -t CONTAINER_NAME:LABEL .`

[![Build Status](https://travis-ci.com/secureCodeBox/scanner-webapplication-zap.svg?branch=master)](https://travis-ci.com/secureCodeBox/scanner-webapplication-zap)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Known Vulnerabilities](https://snyk.io/test/github/secureCodeBox/scanner-webapplication-zap/badge.svg)](https://snyk.io/test/github/secureCodeBox/scanner-webapplication-zap)
[![GitHub release](https://img.shields.io/github/release/secureCodeBox/scanner-webapplication-zap.svg)](https://github.com/secureCodeBox/scanner-webapplication-zap/releases/latest)


[OWASP_ZAP_PROJECT]: https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project
[zaproxy.org]: https://www.zaproxy.org/



