function authenticate(helper, paramsValues, credentials) {
    var log = org.apache.log4j.Logger.getLogger('myproduct:authenticate');
    log.info("authing user with " + paramsValues.toString());

    var loginUrl = paramsValues.get("LoginURL");
    var csrfTokenName = paramsValues.get("CSRFField");
    var csrfTokenValue = extractInputFieldValue(getPageContent(helper, loginUrl), csrfTokenName);
    var postData = paramsValues.get("POSTData");

    log.info("pre filling post data is : " + postData);

    log.info("username is: " + credentials.getParam("Username"));
    postData = postData.replace('{%username%}', encodeURIComponent(credentials.getParam("Username")));
    postData = postData.replace('{%password%}', encodeURIComponent(credentials.getParam("Password")));
    postData = postData.replace('{%' + csrfTokenName + '%}', encodeURIComponent(csrfTokenValue));

    log.info("post data is : " + postData);

    var msg = sendAndReceive(helper, loginUrl, postData);
    return msg;
}

function getRequiredParamsNames() {
    return [ "LoginURL", "CSRFField", "POSTData" ];
}

function getOptionalParamsNames() {
    return [];
}

function getCredentialsParamsNames() {
    return [ "Username", "Password" ];
}

function getPageContent(helper, url) {
    var msg = sendAndReceive(helper, url);
    return msg.getResponseBody().toString();
}

function sendAndReceive(helper, url, postData) {
    var msg = helper.prepareMessage();

    var method = "GET";
    if (postData) {
        method = "POST";
        msg.setRequestBody(postData);
    }

    var requestUri = new org.apache.commons.httpclient.URI(url, true);
    var requestHeader = new org.parosproxy.paros.network.HttpRequestHeader(method, requestUri, "HTTP/1.0");
    msg.setRequestHeader(requestHeader);

    var log = org.apache.log4j.Logger.getLogger('myproduct:authenticate');
    log.info("msg is : " + msg.toString());

    helper.sendAndReceive(msg);

    return msg;
}

function extractInputFieldValue(page, fieldName) {
    // Rhino:
    //var src = new net.htmlparser.jericho.Source(page);
    // Nashorn:
     var Source = Java.type("net.htmlparser.jericho.Source");
     var src = new Source(page);

    var it = src.getAllElements('input').iterator();

    while (it.hasNext()) {
        var element = it.next();
        if (element.getAttributeValue('name') == fieldName) {
            return element.getAttributeValue('value');
        }
    }
    return '';
}