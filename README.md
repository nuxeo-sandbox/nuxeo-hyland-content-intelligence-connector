# nuxeo-hyland-content-intelligence-connector

> [!WARNING]
> This plugin is Work in Progress.

> [!IMPORTANT]
> As of May 2025, It is in full rework/rewrite state: Hyland Content Intelligence has evolved, more APIs are available, usage has changed, etc.
> 
> So, please, **do not use it as is**, it will not work. For now, using GitHub more as as backup than a pre-release work :-)

<hr>

A plugin that connects to Hyland Content Intelligence and leverage some of its API.

This plugin just sends the request and returns its JSON response, it does not add any logic. This is for flexibility: When Hyland Content Intelligence adds new endpoints, and/or adds/changes endpoint expected payload, no need to change the code of the plugin, just modify the caller (in most of our usages, Nuxeo Studio project and JavaScript Automation, see examples below).

There is a single entry point to call the service: An automation operation, `HylandContentIntelligence.Invoke`, that expects the endpoint to call and the corresponding JSON payload.

The plugin does add some optional optimization though: A cache. If the service is called for the same JSON payload, then a cached value is returned. This is optional and can be turned off on a per call basis. The default value is `false`.

This plugin duplicates several features from [nuxeo-baws-bedrock-connector](https://github.com/nuxeo-sandbox/nuxeo-aws-bedrock-connector). As a start, it replaces the calls to AWS Bedrock to calls to [Hyland Content Intelligence](https://www.hyland.com/en), to get the description of an image and tag an image with custom metadata. As more endpoints are added to the service, they'll become available from the operation.

## Usage

From Nuxeo Studio, create an automation script that calls the operation, passing it the payload expected for the entry point.

See unit tests at `TestHylandCIService` for examples of payload.

### Nuxeo Configuration Parameters

The plugin expects 3 parameters to be configured in nuxeo.conf:

> [!IMPORTANT]
> Without these values set, the calls to Hyland COntent Intelligence can only fail.

* `nuxeo.hyland.content.intelligence.baseUrl`
* `nuxeo.hyland.content.intelligence.authenticationHeaderName`
* `nuxeo.hyland.content.intelligence.authenticationHeaderValue`

These values are provided by Hyland and are expected to change in a short future (remember, this plugin is Work in Progress). If you are a Hyland customer, please contact us to discuss your use case.


### The `HylandContentIntelligence.Invoke` Operation

* Input: `void`
* Output: `Blob` (see below) 
* Parameters
  * `endpoint`: String required. The endpoint to call. "/description" for example.
  * `jsonPayload`: String, required. The JSON expected by the endpoint.
  * `useCache`: Boolean, optional. Use cached response if `true`. Default is `false`.

The returned Blob holds Hyland Content Intelligence REST API JSON response and information about the call itself. It is a `StringBlob`, use its `getString()` method to get the JSON String (see Automation Scripting example below). The properties of this JSON depend on the endpoint. The plugin always add 2 properties:

* `responseCode`: The HTTP code (200 is OK, etc.)
* `responseMessage`: The HTTP resonse message


> [!WARNING]
> This plugin is Work in Progress and the service itself is Work in Progress. The endpoints, the payloads will change.

### `Base64Helper` Automation Helper

The plugin also provides the `Base64Helper` Automation Helper, that allows for creating the Base64 representation of a blob or a String:

* `Base64Helper.blob2Base64(aBlob)`
* `Base64Helper.string2Base64(aString)`

(See Automation Script example below)

### Example

Getting the description of an image:

```js
function run(input, params) {
  // Get a rendition (don't send a 300MB Photoshop))
  var blob = Picture.GetView(input, {'viewName': 'FullHD'});

  // Encode with the helper
  var base64 = Base64Helper.blob2Base64(blob);

  // Prepare the call to Hyland Content Intelligence, with the
  // payload expected by the "/description" endpoint.
  var payload = {
    "type" : "base64",
    "media_type": blob.getMimeType(),
    "override_request": "",
    "data": base64
  };

  // Call the operation (not using the cache)
  var responseBlob = HylandContentIntelligence.Invoke(null, {
    "endpoint": "/description",
    "jsonPayload": JSON.stringify(payload)
  });

  // Get the result
  var responseJson = JSON.parse(responseBlob.getString());

  // Check the result
  if(responseJson.responseCode !== 200) {
    // . . . handle error . . . throw and error, or just log responseJson.responseCode + ", " + responseJson.responseMessage . . .
  } else {
    // Save the description
    // For "/description", the result is in the "response" property
    input["dc:description"] = responseJson.response;
    input = Document.Save(input, {});
  }

  return input;
}
```


## How to build
```bash
git clone https://github.com/nuxeo-sandbox/nuxeo-hyland-content-intelligence-connector
cd nuxeo-hyland-content-intelligence-connector
mvn clean install
```

You can add the `-DskipDocker` flag to skip building with Docker.

Also you can use the `-DskipTests` flag

### How to UnitTest

You need to set up 3 environment variables that will be used to connect to the service:

* `HYLAND_CONTENT_INTELL_URL`
* `HYLAND_CONTENT_INTELL_HEADER_NAME`
* `HYLAND_CONTENT_INTELL_HEADER_VALUE`

These values are provided by the service. Remember, this is Work In Progress, we are changing the URL of the service, the authentication methods etc., hence why this is temporary until the service is final.


## Support
**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning
resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be
useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.


## License
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)


## About Nuxeo
Nuxeo Platform is an open source Content Services platform, written in Java. Data can be stored in both SQL & NoSQL
databases.

The development of the Nuxeo Platform is mostly done by Nuxeo employees with an open development model.

The source code, documentation, roadmap, issue tracker, testing, benchmarks are all public.

Typically, Nuxeo users build different types of information management solutions
for [document management](https://www.nuxeo.com/solutions/document-management/), [case management](https://www.nuxeo.com/solutions/case-management/),
and [digital asset management](https://www.nuxeo.com/solutions/dam-digital-asset-management/), use cases. It uses
schema-flexible metadata & content models that allows content to be repurposed to fulfill future use cases.

More information is available at [www.nuxeo.com](https://www.nuxeo.com).
