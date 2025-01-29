# nuxeo-hyland-content-intelligence-connector

> [!WARNING]
> This plugin Work in Progress.
> Do not use it now (anyway, it does not build :-))

A plugin that connects to Hyland Content Intelligence and leverage some of its API.

This plugin just sends the request and returns its JSON response, it does not add any logic. This is for flexibility: When Hyland Content Intelligence adds new endpoints, and/or add/change endpoint parameters, no need to change this plugin, just modify the caller (in most of our usages, Nuxeo Studio project and JavaScript Autmation, see examples below).

There is a single entry point to call the service: An automation operation, `HylandContentIntelligence.Invoke`, that expects the endpoint to call and a JSON payload adapted for this endpoint. See details below.

The plugin does add some optional optimization though: A cache. If the service is called for the same JSON payload, then a cached value is returned. This is optional and can be turned off on a per call basis (see below).

This plugin duplicates several features from [nuxeo-baws-bedrock-connector](https://github.com/nuxeo-sandbox/nuxeo-aws-bedrock-connector), especially all the work around using vector search with openSearch. As a start, it replaces the calls to AWS Bedrock to calls to [Hyland Content Intelligence](https://www.hyland.com/en), to calculate embedding, get the description of an image and tag an image with custom metadata.

So as of "today", the work is about getting code from nuxeo-baws-bedrock-connector, update the pom files to change group ID and artifact IDs, etc. etc.