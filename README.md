# nuxeo-hyland-content-intelligence-connector

> [!WARNING]
> This plugin is Work in Progress.

A plugin that connects to Hyland Content Intelligence and leverage some of its API.

This plugin just sends the request and returns its JSON response, it does not add any logic. This is for flexibility: When Hyland Content Intelligence adds new endpoints, and/or add/change endpoint parameters, no need to change this plugin, just modify the caller (in most of our usages, Nuxeo Studio project and JavaScript Automation, see examples below).

There is a single entry point to call the service: An automation operation, `HylandContentIntelligence.Invoke`, that expects the endpoint to call and a JSON payload expected for this endpoint.

The plugin does add some optional optimization though: A cache. If the service is called for the same JSON payload, then a cached value is returned. This is optional and can be turned off on a per call basis

This plugin duplicates several features from [nuxeo-baws-bedrock-connector](https://github.com/nuxeo-sandbox/nuxeo-aws-bedrock-connector), especially all the work around using vector search with openSearch. As a start, it replaces the calls to AWS Bedrock to calls to [Hyland Content Intelligence](https://www.hyland.com/en), to calculate embedding, get the description of an image and tag an image with custom metadata.

## Usage

From Nuxeo Studio, create an automation script that calls the operation, passing it the payload expected for the entry point.

See unit tests at `TestHylandCIService` for examples of payload

> [!WARNING]
> This plugin is Work in Progress and the service itself is Work in Progress. The endpoint, the payload may change.


## How to build
```bash
git clone https://github.com/nuxeo-sandbox/nuxeo-hyland-content-intelligence-connector
cd nuxeo-hyland-content-intelligence-connector
mvn clean install
```

You can add the `-DskipDocker` flag to skip building with Docker.

Also you can use the `DskipTests` flag

### How to UnitTest

You need to set up 3 environement variables that will be used to connect to the service:

* `HYLAND_CONTENT_INTELL_URL`
* `HYLAND_CONTENT_INTELL_HEADER_NAME`
* `HYLAND_CONTENT_INTELL_HEADER_VALUE`

These values are provided by the service. Remember, this is Work In Progress, we are changing the URL of the service, the authentication methods etc., hence why this is temporary until the servioce is final.


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
