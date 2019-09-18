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

<details>
<summary>Zap output</summary>
<br>
<pre>
{
    "findings": [
  {
    "id": "40252a23-848f-400f-8ecc-1c7b6d6c2afa",
    "name": "Cookie Slack Detector",
    "description": "Repeated GET requests: drop a different cookie each time, followed by normal request with all cookies to stabilize session, compare responses against original baseline GET. This can reveal areas where cookie based authentication/attributes are not actually enforced.",
    "category": "Cookie Slack Detector",
    "osi_layer": "APPLICATION",
    "severity": "INFORMATIONAL",
    "reference": {
      "id": "CVE-200",
      "source": "https://cwe.mitre.org/data/definitions/200.html"
    },
    "attributes": {
      "OTHER": "Dropping this cookie appears to have invalidated the session: [JSESSIONID] A follow-on request with all original cookies still had a different response than the original request. \n",
      "HAR": {
        "log": {
          "version": "1.2",
          "creator": {
            "name": "OWASP ZAP",
            "version": "D-2019-02-05"
          },
          "browser": {},
          "pages": [],
          "entries": [
            {
              "startedDateTime": "2019-09-18T08:22:21.839+0000",
              "time": 2,
              "request": {
                "method": "GET",
                "url": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
                "httpVersion": "HTTP/1.1",
                "cookies": [
                  {
                    "name": "JSESSIONID",
                    "value": "944811ACAE4745805CD489509409AA44"
                  }
                ],
                "headers": [
                  {
                    "name": "User-Agent",
                    "value": "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0"
                  },
                  {
                    "name": "Pragma",
                    "value": "no-cache"
                  },
                  {
                    "name": "Cache-Control",
                    "value": "no-cache"
                  },
                  {
                    "name": "Content-Length",
                    "value": "0"
                  },
                  {
                    "name": "Referer",
                    "value": "http://bodgeit.secure-code-box.svc:8080/bodgeit"
                  },
                  {
                    "name": "Cookie",
                    "value": "JSESSIONID=944811ACAE4745805CD489509409AA44"
                  },
                  {
                    "name": "Host",
                    "value": "bodgeit.secure-code-box.svc:8080"
                  }
                ],
                "queryString": [],
                "postData": {
                  "mimeType": "",
                  "params": [],
                  "text": ""
                },
                "headersSize": 364,
                "bodySize": 0
              },
              "response": {
                "status": 200,
                "statusText": "",
                "httpVersion": "HTTP/1.1",
                "cookies": [],
                "headers": [
                  {
                    "name": "Content-Type",
                    "value": "text/html;charset=ISO-8859-1"
                  },
                  {
                    "name": "Content-Length",
                    "value": "3199"
                  },
                  {
                    "name": "Date",
                    "value": "Wed, 18 Sep 2019 08:22:21 GMT"
                  }
                ],
                "content": {
                  "size": 3199,
                  "compression": 0,
                  "mimeType": "text/html;charset=ISO-8859-1",
                  "text": "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                },
                "redirectURL": "",
                "headersSize": 119,
                "bodySize": 3199
              },
              "cache": {},
              "timings": {
                "blocked": -1,
                "dns": -1,
                "connect": -1,
                "send": 0,
                "wait": 0,
                "receive": 2,
                "ssl": -1
              },
              "_zapMessageId": "48",
              "_zapMessageNote": "",
              "_zapMessageType": "3"
            }
          ]
        }
      },
      "PLUGIN_ID": "90027",
      "OTHER_REFERENCES": [
        "http://projects.webappsec.org/Fingerprinting"
      ],
      "ATTACK": "",
      "WASC_ID": 45,
      "ZAP_BASE_URL": "http://bodgeit.secure-code-box.svc:8080/bodgeit",
      "CONFIDENCE": "Low",
      "EVIDENCE": ""
    },
    "location": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
    "false_positive": false
  },
  {
    "id": "9eeaab44-3d5a-4c59-9a28-44be704b6f70",
    "name": "User Agent Fuzzer",
    "description": "Check for differences in response based on fuzzed User Agent (eg. mobile sites, access as a Search Engine Crawler). Compares the response statuscode and the hashcode of the response body with the original response.",
    "category": "User Agent Fuzzer",
    "osi_layer": "APPLICATION",
    "severity": "INFORMATIONAL",
    "reference": {
      "id": "CVE-0",
      "source": "https://cwe.mitre.org/data/definitions/0.html"
    },
    "attributes": {
      "OTHER": "",
      "HAR": {
        "log": {
          "version": "1.2",
          "creator": {
            "name": "OWASP ZAP",
            "version": "D-2019-02-05"
          },
          "browser": {},
          "pages": [],
          "entries": [
            {
              "startedDateTime": "2019-09-18T08:22:01.367+0000",
              "time": 2,
              "request": {
                "method": "GET",
                "url": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
                "httpVersion": "HTTP/1.1",
                "cookies": [],
                "headers": [
                  {
                    "name": "User-Agent",
                    "value": "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)"
                  },
                  {
                    "name": "Pragma",
                    "value": "no-cache"
                  },
                  {
                    "name": "Cache-Control",
                    "value": "no-cache"
                  },
                  {
                    "name": "Content-Length",
                    "value": "0"
                  },
                  {
                    "name": "Referer",
                    "value": "http://bodgeit.secure-code-box.svc:8080/bodgeit"
                  },
                  {
                    "name": "Host",
                    "value": "bodgeit.secure-code-box.svc:8080"
                  }
                ],
                "queryString": [],
                "postData": {
                  "mimeType": "",
                  "params": [],
                  "text": ""
                },
                "headersSize": 289,
                "bodySize": 0
              },
              "response": {
                "status": 200,
                "statusText": "",
                "httpVersion": "HTTP/1.1",
                "cookies": [
                  {
                    "name": "JSESSIONID",
                    "value": "AC119E624DB88FBEFD591F3466FB9B4B",
                    "path": "/bodgeit",
                    "domain": "bodgeit.secure-code-box.svc",
                    "expires": "2019-09-18T08:22:29.533+0000",
                    "httpOnly": true,
                    "secure": false
                  }
                ],
                "headers": [
                  {
                    "name": "Set-Cookie",
                    "value": "JSESSIONID=AC119E624DB88FBEFD591F3466FB9B4B; Path=/bodgeit; HttpOnly"
                  },
                  {
                    "name": "Content-Type",
                    "value": "text/html;charset=ISO-8859-1"
                  },
                  {
                    "name": "Content-Length",
                    "value": "3205"
                  },
                  {
                    "name": "Date",
                    "value": "Wed, 18 Sep 2019 08:22:00 GMT"
                  }
                ],
                "content": {
                  "size": 3205,
                  "compression": 0,
                  "mimeType": "text/html;charset=ISO-8859-1",
                  "text": "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                },
                "redirectURL": "",
                "headersSize": 201,
                "bodySize": 3205
              },
              "cache": {},
              "timings": {
                "blocked": -1,
                "dns": -1,
                "connect": -1,
                "send": 0,
                "wait": 0,
                "receive": 2,
                "ssl": -1
              },
              "_zapMessageId": "19",
              "_zapMessageNote": "",
              "_zapMessageType": "3"
            }
          ]
        }
      },
      "PLUGIN_ID": "10104",
      "OTHER_REFERENCES": [
        "https://www.owasp.org/index.php/Web_Application_Security_Testing_Cheat_Sheet"
      ],
      "ATTACK": "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)",
      "WASC_ID": 0,
      "ZAP_BASE_URL": "http://bodgeit.secure-code-box.svc:8080/bodgeit",
      "CONFIDENCE": "Medium",
      "EVIDENCE": ""
    },
    "location": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
    "false_positive": false
  },
  {
    "id": "a5ceba78-de90-434c-8de7-fc7db7b64a09",
    "name": "Storable and Cacheable Content",
    "description": "The response contents are storable by caching components such as proxy servers, and may be retrieved directly from the cache, rather than from the origin server by the caching servers, in response to similar requests from other users.  If the response data is sensitive, personal or user-specific, this may result in sensitive information being leaked. In some cases, this may even result in a user gaining complete control of the session of another user, depending on the configuration of the caching components in use in their environment. This is primarily an issue where \"shared\" caching servers such as \"proxy\" caches are configured on the local network. This configuration is typically found in corporate or educational environments, for instance.",
    "category": "Storable and Cacheable Content",
    "osi_layer": "APPLICATION",
    "severity": "INFORMATIONAL",
    "reference": {
      "id": "CVE-524",
      "source": "https://cwe.mitre.org/data/definitions/524.html"
    },
    "hint": "Validate that the response does not contain sensitive, personal or user-specific information.  If it does, consider the use of the following HTTP response headers, to limit, or prevent the content being stored and retrieved from the cache by another user:\nCache-Control: no-cache, no-store, must-revalidate, private\nPragma: no-cache\nExpires: 0\nThis configuration directs both HTTP 1.0 and HTTP 1.1 compliant caching servers to not store the response, and to not retrieve the response (without validation) from the cache, in response to a similar request. ",
    "attributes": {
      "OTHER": "In the absence of an explicitly specified caching lifetime directive in the response, a liberal lifetime heuristic of 1 year was assumed. This is permitted by rfc7234.",
      "HAR": {
        "log": {
          "version": "1.2",
          "creator": {
            "name": "OWASP ZAP",
            "version": "D-2019-02-05"
          },
          "browser": {},
          "pages": [],
          "entries": [
            {
              "startedDateTime": "2019-09-18T08:22:00.473+0000",
              "time": 1,
              "request": {
                "method": "GET",
                "url": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
                "httpVersion": "HTTP/1.1",
                "cookies": [],
                "headers": [
                  {
                    "name": "User-Agent",
                    "value": "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0"
                  },
                  {
                    "name": "Pragma",
                    "value": "no-cache"
                  },
                  {
                    "name": "Cache-Control",
                    "value": "no-cache"
                  },
                  {
                    "name": "Content-Length",
                    "value": "0"
                  },
                  {
                    "name": "Referer",
                    "value": "http://bodgeit.secure-code-box.svc:8080/bodgeit"
                  },
                  {
                    "name": "Host",
                    "value": "bodgeit.secure-code-box.svc:8080"
                  }
                ],
                "queryString": [],
                "postData": {
                  "mimeType": "",
                  "params": [],
                  "text": ""
                },
                "headersSize": 311,
                "bodySize": 0
              },
              "response": {
                "status": 200,
                "statusText": "",
                "httpVersion": "HTTP/1.1",
                "cookies": [
                  {
                    "name": "JSESSIONID",
                    "value": "944811ACAE4745805CD489509409AA44",
                    "path": "/bodgeit",
                    "domain": "bodgeit.secure-code-box.svc",
                    "expires": "2019-09-18T08:22:29.641+0000",
                    "httpOnly": true,
                    "secure": false
                  }
                ],
                "headers": [
                  {
                    "name": "Set-Cookie",
                    "value": "JSESSIONID=944811ACAE4745805CD489509409AA44; Path=/bodgeit; HttpOnly"
                  },
                  {
                    "name": "Content-Type",
                    "value": "text/html;charset=ISO-8859-1"
                  },
                  {
                    "name": "Content-Length",
                    "value": "3209"
                  },
                  {
                    "name": "Date",
                    "value": "Wed, 18 Sep 2019 08:22:00 GMT"
                  }
                ],
                "content": {
                  "size": 3209,
                  "compression": 0,
                  "mimeType": "text/html;charset=ISO-8859-1",
                  "text": "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                },
                "redirectURL": "",
                "headersSize": 201,
                "bodySize": 3209
              },
              "cache": {},
              "timings": {
                "blocked": -1,
                "dns": -1,
                "connect": -1,
                "send": 0,
                "wait": 0,
                "receive": 1,
                "ssl": -1
              },
              "_zapMessageId": "3",
              "_zapMessageNote": "",
              "_zapMessageType": "15"
            }
          ]
        }
      },
      "PLUGIN_ID": "10049",
      "OTHER_REFERENCES": [
        "https://tools.ietf.org/html/rfc7234",
        "https://tools.ietf.org/html/rfc7231",
        "http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html (obsoleted by rfc7234)"
      ],
      "ATTACK": "",
      "WASC_ID": 13,
      "ZAP_BASE_URL": "http://bodgeit.secure-code-box.svc:8080/bodgeit",
      "CONFIDENCE": "Medium",
      "EVIDENCE": ""
    },
    "location": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
    "false_positive": false
  },
  {
    "id": "c37d2d68-d415-4659-a4e2-8087159bd447",
    "name": "X-Content-Type-Options Header Missing",
    "description": "The Anti-MIME-Sniffing header X-Content-Type-Options was not set to 'nosniff'. This allows older versions of Internet Explorer and Chrome to perform MIME-sniffing on the response body, potentially causing the response body to be interpreted and displayed as a content type other than the declared content type. Current (early 2014) and legacy versions of Firefox will use the declared content type (if one is set), rather than performing MIME-sniffing.",
    "category": "X-Content-Type-Options Header Missing",
    "osi_layer": "APPLICATION",
    "severity": "LOW",
    "reference": {
      "id": "CVE-16",
      "source": "https://cwe.mitre.org/data/definitions/16.html"
    },
    "hint": "Ensure that the application/web server sets the Content-Type header appropriately, and that it sets the X-Content-Type-Options header to 'nosniff' for all web pages.\nIf possible, ensure that the end user uses a standards-compliant and modern web browser that does not perform MIME-sniffing at all, or that can be directed by the web application/web server to not perform MIME-sniffing.",
    "attributes": {
      "OTHER": "This issue still applies to error type pages (401, 403, 500, etc) as those pages are often still affected by injection issues, in which case there is still concern for browsers sniffing pages away from their actual content type.\nAt \"High\" threshold this scanner will not alert on client or server error responses.",
      "HAR": {
        "log": {
          "version": "1.2",
          "creator": {
            "name": "OWASP ZAP",
            "version": "D-2019-02-05"
          },
          "browser": {},
          "pages": [],
          "entries": [
            {
              "startedDateTime": "2019-09-18T08:22:00.473+0000",
              "time": 1,
              "request": {
                "method": "GET",
                "url": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
                "httpVersion": "HTTP/1.1",
                "cookies": [],
                "headers": [
                  {
                    "name": "User-Agent",
                    "value": "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0"
                  },
                  {
                    "name": "Pragma",
                    "value": "no-cache"
                  },
                  {
                    "name": "Cache-Control",
                    "value": "no-cache"
                  },
                  {
                    "name": "Content-Length",
                    "value": "0"
                  },
                  {
                    "name": "Referer",
                    "value": "http://bodgeit.secure-code-box.svc:8080/bodgeit"
                  },
                  {
                    "name": "Host",
                    "value": "bodgeit.secure-code-box.svc:8080"
                  }
                ],
                "queryString": [],
                "postData": {
                  "mimeType": "",
                  "params": [],
                  "text": ""
                },
                "headersSize": 311,
                "bodySize": 0
              },
              "response": {
                "status": 200,
                "statusText": "",
                "httpVersion": "HTTP/1.1",
                "cookies": [
                  {
                    "name": "JSESSIONID",
                    "value": "944811ACAE4745805CD489509409AA44",
                    "path": "/bodgeit",
                    "domain": "bodgeit.secure-code-box.svc",
                    "expires": "2019-09-18T08:22:29.623+0000",
                    "httpOnly": true,
                    "secure": false
                  }
                ],
                "headers": [
                  {
                    "name": "Set-Cookie",
                    "value": "JSESSIONID=944811ACAE4745805CD489509409AA44; Path=/bodgeit; HttpOnly"
                  },
                  {
                    "name": "Content-Type",
                    "value": "text/html;charset=ISO-8859-1"
                  },
                  {
                    "name": "Content-Length",
                    "value": "3209"
                  },
                  {
                    "name": "Date",
                    "value": "Wed, 18 Sep 2019 08:22:00 GMT"
                  }
                ],
                "content": {
                  "size": 3209,
                  "compression": 0,
                  "mimeType": "text/html;charset=ISO-8859-1",
                  "text": "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                },
                "redirectURL": "",
                "headersSize": 201,
                "bodySize": 3209
              },
              "cache": {},
              "timings": {
                "blocked": -1,
                "dns": -1,
                "connect": -1,
                "send": 0,
                "wait": 0,
                "receive": 1,
                "ssl": -1
              },
              "_zapMessageId": "3",
              "_zapMessageNote": "",
              "_zapMessageType": "15"
            }
          ]
        }
      },
      "PLUGIN_ID": "10021",
      "OTHER_REFERENCES": [
        "http://msdn.microsoft.com/en-us/library/ie/gg622941%28v=vs.85%29.aspx",
        "https://www.owasp.org/index.php/List_of_useful_HTTP_headers"
      ],
      "ATTACK": "",
      "WASC_ID": 15,
      "ZAP_BASE_URL": "http://bodgeit.secure-code-box.svc:8080/bodgeit",
      "CONFIDENCE": "Medium",
      "EVIDENCE": ""
    },
    "location": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
    "false_positive": false
  },
  {
    "id": "524610a3-4ed3-4cdf-944b-faac543b9fd8",
    "name": "Non-Storable Content",
    "description": "The response contents are not storable by caching components such as proxy servers. If the response does not contain sensitive, personal or user-specific information, it may benefit from being stored and cached, to improve performance.",
    "category": "Non-Storable Content",
    "osi_layer": "APPLICATION",
    "severity": "INFORMATIONAL",
    "reference": {
      "id": "CVE-524",
      "source": "https://cwe.mitre.org/data/definitions/524.html"
    },
    "hint": "The content may be marked as storable by ensuring that the following conditions are satisfied:\nThe request method must be understood by the cache and defined as being cacheable (\"GET\", \"HEAD\", and \"POST\" are currently defined as cacheable)\nThe response status code must be understood by the cache (one of the 1XX, 2XX, 3XX, 4XX, or 5XX response classes are generally understood)\nThe \"no-store\" cache directive must not appear in the request or response header fields\nFor caching by \"shared\" caches such as \"proxy\" caches, the \"private\" response directive must not appear in the response\nFor caching by \"shared\" caches such as \"proxy\" caches, the \"Authorization\" header field must not appear in the request, unless the response explicitly allows it (using one of the \"must-revalidate\", \"public\", or \"s-maxage\" Cache-Control response directives)\nIn addition to the conditions above, at least one of the following conditions must also be satisfied by the response:\nIt must contain an \"Expires\" header field\nIt must contain a \"max-age\" response directive\nFor \"shared\" caches such as \"proxy\" caches, it must contain a \"s-maxage\" response directive\nIt must contain a \"Cache Control Extension\" that allows it to be cached\nIt must have a status code that is defined as cacheable by default (200, 203, 204, 206, 300, 301, 404, 405, 410, 414, 501).   ",
    "attributes": {
      "OTHER": "",
      "HAR": {
        "log": {
          "version": "1.2",
          "creator": {
            "name": "OWASP ZAP",
            "version": "D-2019-02-05"
          },
          "browser": {},
          "pages": [],
          "entries": [
            {
              "startedDateTime": "2019-09-18T08:22:00.466+0000",
              "time": 3,
              "request": {
                "method": "GET",
                "url": "http://bodgeit.secure-code-box.svc:8080/bodgeit",
                "httpVersion": "HTTP/1.1",
                "cookies": [],
                "headers": [
                  {
                    "name": "User-Agent",
                    "value": "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0"
                  },
                  {
                    "name": "Pragma",
                    "value": "no-cache"
                  },
                  {
                    "name": "Cache-Control",
                    "value": "no-cache"
                  },
                  {
                    "name": "Content-Length",
                    "value": "0"
                  },
                  {
                    "name": "Host",
                    "value": "bodgeit.secure-code-box.svc:8080"
                  }
                ],
                "queryString": [],
                "postData": {
                  "mimeType": "",
                  "params": [],
                  "text": ""
                },
                "headersSize": 252,
                "bodySize": 0
              },
              "response": {
                "status": 302,
                "statusText": "",
                "httpVersion": "HTTP/1.1",
                "cookies": [],
                "headers": [
                  {
                    "name": "Location",
                    "value": "/bodgeit/"
                  },
                  {
                    "name": "Date",
                    "value": "Wed, 18 Sep 2019 08:22:00 GMT"
                  }
                ],
                "content": {
                  "size": 0,
                  "compression": 0,
                  "mimeType": ""
                },
                "redirectURL": "/bodgeit/",
                "headersSize": 74,
                "bodySize": 0
              },
              "cache": {},
              "timings": {
                "blocked": -1,
                "dns": -1,
                "connect": -1,
                "send": 0,
                "wait": 0,
                "receive": 3,
                "ssl": -1
              },
              "_zapMessageId": "1",
              "_zapMessageNote": "",
              "_zapMessageType": "15"
            }
          ]
        }
      },
      "PLUGIN_ID": "10049",
      "OTHER_REFERENCES": [
        "https://tools.ietf.org/html/rfc7234",
        "https://tools.ietf.org/html/rfc7231",
        "http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html (obsoleted by rfc7234)"
      ],
      "ATTACK": "",
      "WASC_ID": 13,
      "ZAP_BASE_URL": "http://bodgeit.secure-code-box.svc:8080/bodgeit",
      "CONFIDENCE": "Medium",
      "EVIDENCE": "302"
    },
    "location": "http://bodgeit.secure-code-box.svc:8080/bodgeit",
    "false_positive": false
  },
  {
    "id": "fa1a7deb-864a-4829-8e5e-8b13697f1c39",
    "name": "Information Disclosure - Suspicious Comments",
    "description": "The response appears to contain suspicious comments which may help an attacker.",
    "category": "Information Disclosure - Suspicious Comments",
    "osi_layer": "APPLICATION",
    "severity": "INFORMATIONAL",
    "reference": {
      "id": "CVE-200",
      "source": "https://cwe.mitre.org/data/definitions/200.html"
    },
    "hint": "Remove all comments that return information that may help an attacker and fix any underlying problems they refer to.",
    "attributes": {
      "OTHER": "\n",
      "HAR": {
        "log": {
          "version": "1.2",
          "creator": {
            "name": "OWASP ZAP",
            "version": "D-2019-02-05"
          },
          "browser": {},
          "pages": [],
          "entries": [
            {
              "startedDateTime": "2019-09-18T08:22:00.473+0000",
              "time": 1,
              "request": {
                "method": "GET",
                "url": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
                "httpVersion": "HTTP/1.1",
                "cookies": [],
                "headers": [
                  {
                    "name": "User-Agent",
                    "value": "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0"
                  },
                  {
                    "name": "Pragma",
                    "value": "no-cache"
                  },
                  {
                    "name": "Cache-Control",
                    "value": "no-cache"
                  },
                  {
                    "name": "Content-Length",
                    "value": "0"
                  },
                  {
                    "name": "Referer",
                    "value": "http://bodgeit.secure-code-box.svc:8080/bodgeit"
                  },
                  {
                    "name": "Host",
                    "value": "bodgeit.secure-code-box.svc:8080"
                  }
                ],
                "queryString": [],
                "postData": {
                  "mimeType": "",
                  "params": [],
                  "text": ""
                },
                "headersSize": 311,
                "bodySize": 0
              },
              "response": {
                "status": 200,
                "statusText": "",
                "httpVersion": "HTTP/1.1",
                "cookies": [
                  {
                    "name": "JSESSIONID",
                    "value": "944811ACAE4745805CD489509409AA44",
                    "path": "/bodgeit",
                    "domain": "bodgeit.secure-code-box.svc",
                    "expires": "2019-09-18T08:22:29.635+0000",
                    "httpOnly": true,
                    "secure": false
                  }
                ],
                "headers": [
                  {
                    "name": "Set-Cookie",
                    "value": "JSESSIONID=944811ACAE4745805CD489509409AA44; Path=/bodgeit; HttpOnly"
                  },
                  {
                    "name": "Content-Type",
                    "value": "text/html;charset=ISO-8859-1"
                  },
                  {
                    "name": "Content-Length",
                    "value": "3209"
                  },
                  {
                    "name": "Date",
                    "value": "Wed, 18 Sep 2019 08:22:00 GMT"
                  }
                ],
                "content": {
                  "size": 3209,
                  "compression": 0,
                  "mimeType": "text/html;charset=ISO-8859-1",
                  "text": "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                },
                "redirectURL": "",
                "headersSize": 201,
                "bodySize": 3209
              },
              "cache": {},
              "timings": {
                "blocked": -1,
                "dns": -1,
                "connect": -1,
                "send": 0,
                "wait": 0,
                "receive": 1,
                "ssl": -1
              },
              "_zapMessageId": "3",
              "_zapMessageNote": "",
              "_zapMessageType": "15"
            }
          ]
        }
      },
      "PLUGIN_ID": "10027",
      "OTHER_REFERENCES": [
        ""
      ],
      "ATTACK": "",
      "WASC_ID": 13,
      "ZAP_BASE_URL": "http://bodgeit.secure-code-box.svc:8080/bodgeit",
      "CONFIDENCE": "Medium",
      "EVIDENCE": ""
    },
    "location": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
    "false_positive": false
  },
  {
    "id": "81c0592e-317a-46a8-94db-1930e001e0ec",
    "name": "Content Security Policy (CSP) Header Not Set",
    "description": "Content Security Policy (CSP) is an added layer of security that helps to detect and mitigate certain types of attacks, including Cross Site Scripting (XSS) and data injection attacks. These attacks are used for everything from data theft to site defacement or distribution of malware. CSP provides a set of standard HTTP headers that allow website owners to declare approved sources of content that browsers should be allowed to load on that page — covered types are JavaScript, CSS, HTML frames, fonts, images and embeddable objects such as Java applets, ActiveX, audio and video files.",
    "category": "Content Security Policy (CSP) Header Not Set",
    "osi_layer": "APPLICATION",
    "severity": "LOW",
    "reference": {
      "id": "CVE-16",
      "source": "https://cwe.mitre.org/data/definitions/16.html"
    },
    "hint": "Ensure that your web server, application server, load balancer, etc. is configured to set the Content-Security-Policy header, to achieve optimal browser support: \"Content-Security-Policy\" for Chrome 25+, Firefox 23+ and Safari 7+, \"X-Content-Security-Policy\" for Firefox 4.0+ and Internet Explorer 10+, and \"X-WebKit-CSP\" for Chrome 14+ and Safari 6+.",
    "attributes": {
      "OTHER": "",
      "HAR": {
        "log": {
          "version": "1.2",
          "creator": {
            "name": "OWASP ZAP",
            "version": "D-2019-02-05"
          },
          "browser": {},
          "pages": [],
          "entries": [
            {
              "startedDateTime": "2019-09-18T08:22:00.473+0000",
              "time": 1,
              "request": {
                "method": "GET",
                "url": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
                "httpVersion": "HTTP/1.1",
                "cookies": [],
                "headers": [
                  {
                    "name": "User-Agent",
                    "value": "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0"
                  },
                  {
                    "name": "Pragma",
                    "value": "no-cache"
                  },
                  {
                    "name": "Cache-Control",
                    "value": "no-cache"
                  },
                  {
                    "name": "Content-Length",
                    "value": "0"
                  },
                  {
                    "name": "Referer",
                    "value": "http://bodgeit.secure-code-box.svc:8080/bodgeit"
                  },
                  {
                    "name": "Host",
                    "value": "bodgeit.secure-code-box.svc:8080"
                  }
                ],
                "queryString": [],
                "postData": {
                  "mimeType": "",
                  "params": [],
                  "text": ""
                },
                "headersSize": 311,
                "bodySize": 0
              },
              "response": {
                "status": 200,
                "statusText": "",
                "httpVersion": "HTTP/1.1",
                "cookies": [
                  {
                    "name": "JSESSIONID",
                    "value": "944811ACAE4745805CD489509409AA44",
                    "path": "/bodgeit",
                    "domain": "bodgeit.secure-code-box.svc",
                    "expires": "2019-09-18T08:22:29.647+0000",
                    "httpOnly": true,
                    "secure": false
                  }
                ],
                "headers": [
                  {
                    "name": "Set-Cookie",
                    "value": "JSESSIONID=944811ACAE4745805CD489509409AA44; Path=/bodgeit; HttpOnly"
                  },
                  {
                    "name": "Content-Type",
                    "value": "text/html;charset=ISO-8859-1"
                  },
                  {
                    "name": "Content-Length",
                    "value": "3209"
                  },
                  {
                    "name": "Date",
                    "value": "Wed, 18 Sep 2019 08:22:00 GMT"
                  }
                ],
                "content": {
                  "size": 3209,
                  "compression": 0,
                  "mimeType": "text/html;charset=ISO-8859-1",
                  "text": "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                },
                "redirectURL": "",
                "headersSize": 201,
                "bodySize": 3209
              },
              "cache": {},
              "timings": {
                "blocked": -1,
                "dns": -1,
                "connect": -1,
                "send": 0,
                "wait": 0,
                "receive": 1,
                "ssl": -1
              },
              "_zapMessageId": "3",
              "_zapMessageNote": "",
              "_zapMessageType": "15"
            }
          ]
        }
      },
      "PLUGIN_ID": "10038",
      "OTHER_REFERENCES": [
        "https://developer.mozilla.org/en-US/docs/Web/Security/CSP/Introducing_Content_Security_Policy",
        "https://www.owasp.org/index.php/Content_Security_Policy",
        "http://www.w3.org/TR/CSP/",
        "http://w3c.github.io/webappsec/specs/content-security-policy/csp-specification.dev.html",
        "http://www.html5rocks.com/en/tutorials/security/content-security-policy/",
        "http://caniuse.com/#feat=contentsecuritypolicy",
        "http://content-security-policy.com/"
      ],
      "ATTACK": "",
      "WASC_ID": 15,
      "ZAP_BASE_URL": "http://bodgeit.secure-code-box.svc:8080/bodgeit",
      "CONFIDENCE": "Medium",
      "EVIDENCE": ""
    },
    "location": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
    "false_positive": false
  },
  {
    "id": "45a9a2c3-a23c-44b9-98b6-9aaf45579666",
    "name": "Feature Policy Header Not Set",
    "description": "Feature Policy Header is an added layer of security that helps to restrict from unauthorized access or usage of browser/client features by web resources. This policy ensures the user privacy by limiting or specifying the features of the browsers can be used by the web resources. Feature Policy provides a set of standard HTTP headers that allow website owners to limit which features of browsers can be used by the page such as camera, microphone, location, full screen etc.",
    "category": "Feature Policy Header Not Set",
    "osi_layer": "APPLICATION",
    "severity": "LOW",
    "reference": {
      "id": "CVE-0",
      "source": "https://cwe.mitre.org/data/definitions/0.html"
    },
    "hint": "Ensure that your web server, application server, load balancer, etc. is configured to set the Feature-Policy header.",
    "attributes": {
      "OTHER": "",
      "HAR": {
        "log": {
          "version": "1.2",
          "creator": {
            "name": "OWASP ZAP",
            "version": "D-2019-02-05"
          },
          "browser": {},
          "pages": [],
          "entries": [
            {
              "startedDateTime": "2019-09-18T08:22:00.473+0000",
              "time": 1,
              "request": {
                "method": "GET",
                "url": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
                "httpVersion": "HTTP/1.1",
                "cookies": [],
                "headers": [
                  {
                    "name": "User-Agent",
                    "value": "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0"
                  },
                  {
                    "name": "Pragma",
                    "value": "no-cache"
                  },
                  {
                    "name": "Cache-Control",
                    "value": "no-cache"
                  },
                  {
                    "name": "Content-Length",
                    "value": "0"
                  },
                  {
                    "name": "Referer",
                    "value": "http://bodgeit.secure-code-box.svc:8080/bodgeit"
                  },
                  {
                    "name": "Host",
                    "value": "bodgeit.secure-code-box.svc:8080"
                  }
                ],
                "queryString": [],
                "postData": {
                  "mimeType": "",
                  "params": [],
                  "text": ""
                },
                "headersSize": 311,
                "bodySize": 0
              },
              "response": {
                "status": 200,
                "statusText": "",
                "httpVersion": "HTTP/1.1",
                "cookies": [
                  {
                    "name": "JSESSIONID",
                    "value": "944811ACAE4745805CD489509409AA44",
                    "path": "/bodgeit",
                    "domain": "bodgeit.secure-code-box.svc",
                    "expires": "2019-09-18T08:22:29.657+0000",
                    "httpOnly": true,
                    "secure": false
                  }
                ],
                "headers": [
                  {
                    "name": "Set-Cookie",
                    "value": "JSESSIONID=944811ACAE4745805CD489509409AA44; Path=/bodgeit; HttpOnly"
                  },
                  {
                    "name": "Content-Type",
                    "value": "text/html;charset=ISO-8859-1"
                  },
                  {
                    "name": "Content-Length",
                    "value": "3209"
                  },
                  {
                    "name": "Date",
                    "value": "Wed, 18 Sep 2019 08:22:00 GMT"
                  }
                ],
                "content": {
                  "size": 3209,
                  "compression": 0,
                  "mimeType": "text/html;charset=ISO-8859-1",
                  "text": "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                },
                "redirectURL": "",
                "headersSize": 201,
                "bodySize": 3209
              },
              "cache": {},
              "timings": {
                "blocked": -1,
                "dns": -1,
                "connect": -1,
                "send": 0,
                "wait": 0,
                "receive": 1,
                "ssl": -1
              },
              "_zapMessageId": "3",
              "_zapMessageNote": "",
              "_zapMessageType": "15"
            }
          ]
        }
      },
      "PLUGIN_ID": "10063",
      "OTHER_REFERENCES": [
        "https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Feature-Policy",
        "https://developers.google.com/web/updates/2018/06/feature-policy",
        "https://scotthelme.co.uk/a-new-security-header-feature-policy/",
        "https://w3c.github.io/webappsec-feature-policy/",
        "https://www.smashingmagazine.com/2018/12/feature-policy/"
      ],
      "ATTACK": "",
      "WASC_ID": 0,
      "ZAP_BASE_URL": "http://bodgeit.secure-code-box.svc:8080/bodgeit",
      "CONFIDENCE": "Medium",
      "EVIDENCE": ""
    },
    "location": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
    "false_positive": false
  },
  {
    "id": "82f75225-8848-4b64-b60e-94ecaa2ced09",
    "name": "HTTP Only Site",
    "description": "The site is only served under HTTP and not HTTPS.",
    "category": "HTTP Only Site",
    "osi_layer": "APPLICATION",
    "severity": "MEDIUM",
    "reference": {
      "id": "CVE-311",
      "source": "https://cwe.mitre.org/data/definitions/311.html"
    },
    "hint": "Configure your web or application server to use SSL (https).",
    "attributes": {
      "OTHER": "Failed to connect.\nZAP attempted to connect via: https://bodgeit.secure-code-box.svc:443/bodgeit/",
      "HAR": {
        "log": {
          "version": "1.2",
          "creator": {
            "name": "OWASP ZAP",
            "version": "D-2019-02-05"
          },
          "browser": {},
          "pages": [],
          "entries": [
            {
              "startedDateTime": "2019-09-18T08:22:01.395+0000",
              "time": 20020,
              "request": {
                "method": "GET",
                "url": "https://bodgeit.secure-code-box.svc:443/bodgeit/",
                "httpVersion": "HTTP/1.1",
                "cookies": [],
                "headers": [
                  {
                    "name": "User-Agent",
                    "value": "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0"
                  },
                  {
                    "name": "Pragma",
                    "value": "no-cache"
                  },
                  {
                    "name": "Cache-Control",
                    "value": "no-cache"
                  },
                  {
                    "name": "Content-Length",
                    "value": "0"
                  },
                  {
                    "name": "Referer",
                    "value": "http://bodgeit.secure-code-box.svc:8080/bodgeit"
                  },
                  {
                    "name": "Host",
                    "value": "bodgeit.secure-code-box.svc:8080"
                  }
                ],
                "queryString": [],
                "postData": {
                  "mimeType": "",
                  "params": [],
                  "text": ""
                },
                "headersSize": 311,
                "bodySize": 0
              },
              "response": {
                "status": 0,
                "statusText": "",
                "httpVersion": "HTTP/1.0",
                "cookies": [],
                "headers": [],
                "content": {
                  "size": 0,
                  "compression": 0,
                  "mimeType": ""
                },
                "redirectURL": "",
                "headersSize": 14,
                "bodySize": 0
              },
              "cache": {},
              "timings": {
                "blocked": -1,
                "dns": -1,
                "connect": -1,
                "send": 0,
                "wait": 0,
                "receive": 20020,
                "ssl": -1
              },
              "_zapMessageId": "32",
              "_zapMessageNote": "",
              "_zapMessageType": "3"
            }
          ]
        }
      },
      "PLUGIN_ID": "10106",
      "OTHER_REFERENCES": [
        "https://www.owasp.org/index.php/Transport_Layer_Protection_Cheat_Sheet",
        "https://www.owasp.org/index.php/SSL_Best_Practices",
        "https://letsencrypt.org/"
      ],
      "ATTACK": "",
      "WASC_ID": 4,
      "ZAP_BASE_URL": "http://bodgeit.secure-code-box.svc:8080/bodgeit",
      "CONFIDENCE": "Medium",
      "EVIDENCE": ""
    },
    "location": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
    "false_positive": false
  },
  {
    "id": "0d3e6185-0248-4b84-9075-f484412c3f7d",
    "name": "Web Browser XSS Protection Not Enabled",
    "description": "Web Browser XSS Protection is not enabled, or is disabled by the configuration of the 'X-XSS-Protection' HTTP response header on the web server",
    "category": "Web Browser XSS Protection Not Enabled",
    "osi_layer": "APPLICATION",
    "severity": "LOW",
    "reference": {
      "id": "CVE-933",
      "source": "https://cwe.mitre.org/data/definitions/933.html"
    },
    "hint": "Ensure that the web browser's XSS filter is enabled, by setting the X-XSS-Protection HTTP response header to '1'.",
    "attributes": {
      "OTHER": "The X-XSS-Protection HTTP response header allows the web server to enable or disable the web browser's XSS protection mechanism. The following values would attempt to enable it: \nX-XSS-Protection: 1; mode=block\nX-XSS-Protection: 1; report=http://www.example.com/xss\nThe following values would disable it:\nX-XSS-Protection: 0\nThe X-XSS-Protection HTTP response header is currently supported on Internet Explorer, Chrome and Safari (WebKit).\nNote that this alert is only raised if the response body could potentially contain an XSS payload (with a text-based content type, with a non-zero length).",
      "HAR": {
        "log": {
          "version": "1.2",
          "creator": {
            "name": "OWASP ZAP",
            "version": "D-2019-02-05"
          },
          "browser": {},
          "pages": [],
          "entries": [
            {
              "startedDateTime": "2019-09-18T08:22:00.473+0000",
              "time": 1,
              "request": {
                "method": "GET",
                "url": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
                "httpVersion": "HTTP/1.1",
                "cookies": [],
                "headers": [
                  {
                    "name": "User-Agent",
                    "value": "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0"
                  },
                  {
                    "name": "Pragma",
                    "value": "no-cache"
                  },
                  {
                    "name": "Cache-Control",
                    "value": "no-cache"
                  },
                  {
                    "name": "Content-Length",
                    "value": "0"
                  },
                  {
                    "name": "Referer",
                    "value": "http://bodgeit.secure-code-box.svc:8080/bodgeit"
                  },
                  {
                    "name": "Host",
                    "value": "bodgeit.secure-code-box.svc:8080"
                  }
                ],
                "queryString": [],
                "postData": {
                  "mimeType": "",
                  "params": [],
                  "text": ""
                },
                "headersSize": 311,
                "bodySize": 0
              },
              "response": {
                "status": 200,
                "statusText": "",
                "httpVersion": "HTTP/1.1",
                "cookies": [
                  {
                    "name": "JSESSIONID",
                    "value": "944811ACAE4745805CD489509409AA44",
                    "path": "/bodgeit",
                    "domain": "bodgeit.secure-code-box.svc",
                    "expires": "2019-09-18T08:22:29.616+0000",
                    "httpOnly": true,
                    "secure": false
                  }
                ],
                "headers": [
                  {
                    "name": "Set-Cookie",
                    "value": "JSESSIONID=944811ACAE4745805CD489509409AA44; Path=/bodgeit; HttpOnly"
                  },
                  {
                    "name": "Content-Type",
                    "value": "text/html;charset=ISO-8859-1"
                  },
                  {
                    "name": "Content-Length",
                    "value": "3209"
                  },
                  {
                    "name": "Date",
                    "value": "Wed, 18 Sep 2019 08:22:00 GMT"
                  }
                ],
                "content": {
                  "size": 3209,
                  "compression": 0,
                  "mimeType": "text/html;charset=ISO-8859-1",
                  "text": "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                },
                "redirectURL": "",
                "headersSize": 201,
                "bodySize": 3209
              },
              "cache": {},
              "timings": {
                "blocked": -1,
                "dns": -1,
                "connect": -1,
                "send": 0,
                "wait": 0,
                "receive": 1,
                "ssl": -1
              },
              "_zapMessageId": "3",
              "_zapMessageNote": "",
              "_zapMessageType": "15"
            }
          ]
        }
      },
      "PLUGIN_ID": "10016",
      "OTHER_REFERENCES": [
        "https://www.owasp.org/index.php/XSS_(Cross_Site_Scripting)_Prevention_Cheat_Sheet",
        "https://blog.veracode.com/2014/03/guidelines-for-setting-security-headers/"
      ],
      "ATTACK": "",
      "WASC_ID": 14,
      "ZAP_BASE_URL": "http://bodgeit.secure-code-box.svc:8080/bodgeit",
      "CONFIDENCE": "Medium",
      "EVIDENCE": ""
    },
    "location": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
    "false_positive": false
  },
  {
    "id": "ebe9f8b7-f312-4f9c-ae80-5cd8260290eb",
    "name": "X-Frame-Options Header Not Set",
    "description": "X-Frame-Options header is not included in the HTTP response to protect against 'ClickJacking' attacks.",
    "category": "X-Frame-Options Header Not Set",
    "osi_layer": "APPLICATION",
    "severity": "MEDIUM",
    "reference": {
      "id": "CVE-16",
      "source": "https://cwe.mitre.org/data/definitions/16.html"
    },
    "hint": "Most modern Web browsers support the X-Frame-Options HTTP header. Ensure it's set on all web pages returned by your site (if you expect the page to be framed only by pages on your server (e.g. it's part of a FRAMESET) then you'll want to use SAMEORIGIN, otherwise if you never expect the page to be framed, you should use DENY. ALLOW-FROM allows specific websites to frame the web page in supported web browsers).",
    "attributes": {
      "OTHER": "",
      "HAR": {
        "log": {
          "version": "1.2",
          "creator": {
            "name": "OWASP ZAP",
            "version": "D-2019-02-05"
          },
          "browser": {},
          "pages": [],
          "entries": [
            {
              "startedDateTime": "2019-09-18T08:22:00.473+0000",
              "time": 1,
              "request": {
                "method": "GET",
                "url": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
                "httpVersion": "HTTP/1.1",
                "cookies": [],
                "headers": [
                  {
                    "name": "User-Agent",
                    "value": "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0"
                  },
                  {
                    "name": "Pragma",
                    "value": "no-cache"
                  },
                  {
                    "name": "Cache-Control",
                    "value": "no-cache"
                  },
                  {
                    "name": "Content-Length",
                    "value": "0"
                  },
                  {
                    "name": "Referer",
                    "value": "http://bodgeit.secure-code-box.svc:8080/bodgeit"
                  },
                  {
                    "name": "Host",
                    "value": "bodgeit.secure-code-box.svc:8080"
                  }
                ],
                "queryString": [],
                "postData": {
                  "mimeType": "",
                  "params": [],
                  "text": ""
                },
                "headersSize": 311,
                "bodySize": 0
              },
              "response": {
                "status": 200,
                "statusText": "",
                "httpVersion": "HTTP/1.1",
                "cookies": [
                  {
                    "name": "JSESSIONID",
                    "value": "944811ACAE4745805CD489509409AA44",
                    "path": "/bodgeit",
                    "domain": "bodgeit.secure-code-box.svc",
                    "expires": "2019-09-18T08:22:29.629+0000",
                    "httpOnly": true,
                    "secure": false
                  }
                ],
                "headers": [
                  {
                    "name": "Set-Cookie",
                    "value": "JSESSIONID=944811ACAE4745805CD489509409AA44; Path=/bodgeit; HttpOnly"
                  },
                  {
                    "name": "Content-Type",
                    "value": "text/html;charset=ISO-8859-1"
                  },
                  {
                    "name": "Content-Length",
                    "value": "3209"
                  },
                  {
                    "name": "Date",
                    "value": "Wed, 18 Sep 2019 08:22:00 GMT"
                  }
                ],
                "content": {
                  "size": 3209,
                  "compression": 0,
                  "mimeType": "text/html;charset=ISO-8859-1",
                  "text": "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                },
                "redirectURL": "",
                "headersSize": 201,
                "bodySize": 3209
              },
              "cache": {},
              "timings": {
                "blocked": -1,
                "dns": -1,
                "connect": -1,
                "send": 0,
                "wait": 0,
                "receive": 1,
                "ssl": -1
              },
              "_zapMessageId": "3",
              "_zapMessageNote": "",
              "_zapMessageType": "15"
            }
          ]
        }
      },
      "PLUGIN_ID": "10020",
      "OTHER_REFERENCES": [
        "http://blogs.msdn.com/b/ieinternals/archive/2010/03/30/combating-clickjacking-with-x-frame-options.aspx"
      ],
      "ATTACK": "",
      "WASC_ID": 15,
      "ZAP_BASE_URL": "http://bodgeit.secure-code-box.svc:8080/bodgeit",
      "CONFIDENCE": "Medium",
      "EVIDENCE": ""
    },
    "location": "http://bodgeit.secure-code-box.svc:8080/bodgeit/",
    "false_positive": false
  }
]

  }
</pre>
</details>



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



