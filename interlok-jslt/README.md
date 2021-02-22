# interlok-jslt

Interlok JSON transformation using the JLST language so their canonical documentation supersedes this : [https://github.com/schibsted/jslt/blob/master/tutorial.md](https://github.com/schibsted/jslt/blob/master/tutorial.md)

Rather than having a single service that encapsulates all the possible modes of operation that JSLT supports; we have chosen to be somewhat explicit in the naming so that we can simplify configuration.

- Use `com.adaptris.core.json.jslt.JsltTransformService` if you want to transform a document. This has limited support for caching the underlying `Expression` if that is desired. However, since that class is not Serializable your mileage will vary.
- Use `com.adaptris.core.json.jslt.JsltMetadataService` if you want to extract values and store them as metadata (object or otherwise).

 ## Transforms

 If we take the basic input document (_collapsing repeated objects_) as described in their documentation [https://github.com/schibsted/jslt/blob/master/examples/README.md](https://github.com/schibsted/jslt/blob/master/examples/README.md) then our resulting transform configuration might be as simple as

 - Storing the JSLT expression as a file somewhere on the file system.
 - Configure the jslt-transform service.

```xml
 <jslt-transform-service>
   <url>file:////home/lchan/work/interlok/collapse.jslt</url>
  </jslt-transform-service>
```
### Caching.

It's perfectly possible to cache your transforms directly by configuring a `CacheConnection` as the connection element in your service configuration. This means that the `Expression` is not repeatedly compiled upon each execution of the service and may have some performance benefits depending on your use case. The simplest configuration would be to make use of the built-in _expiring-map-cache_ and configure your connection appropriately. It uses a connection to wrap the cache so that we get the benefit of using a shared-connection and thus a shared cache as required. The key to the cache entry is the URL after resolution.

```xml
 <jslt-transform-service>
   <url>file:////home/lchan/work/interlok/collapse.jslt</url>
   <connection class="cache-connection">
     <cache class="expiring-map-cache"/>
   </connection>
  </jslt-transform-service>
```

### Passing in variables

JSLT supports variables when executing a transform as documented via [https://github.com/schibsted/jslt/blob/master/docs/api.md](https://github.com/schibsted/jslt/blob/master/docs/api.md). It is possible to pass through metadata / object metadata from the existing AdaptrisMessage into the transform.

If we take the _collapsing repeated objects_ example again then we can change our JSLT transform to be something like

```json
{
  "result" : {
    "Open" : .menu.popup.menuitem[0].onclick,
    "Close" : .menu.popup.menuitem[1].onclick,
    "metadata_value_from_metadata" : $text_metadata_key,
  }
}
```

We need to now resolve __text_metadata_key__ as a variable when we execute the transform. This can be done by adding a `jslt-metadata-variable` to the transform configuration resulting in. You can source your variables as metadata, object metadata, or through fixed configuration as you require. In this instance any metadata keys that match the regular expression will be inserted as a variable

```xml
  <jslt-transform-service>
   <url>file:////home/lchan/work/interlok/collapse.jslt</url>
   <variables>
    <jslt-metadata-variable>
     <filter class="regex-metadata-filter">
      <include-pattern>^.*_metadata_key$</include-pattern>
     </filter>
    </jslt-metadata-variable>
   </variables>
  </jslt-transform-service>
```

## JSLT Queries

The `jslt-metadata-service` is broadly analogous to `xpath-metadata-service` in that it allows you to execute a query and store the result as a string (normal metadata) via `jslt-to-metadata` or as a _JsonNode_ (object metadata) via `jslt-to-object-metadata`. If we take the simplest document from the language tutorial : `{"foo" : {"bar" : [1,2,3,4,5]}}`:

- if we wanted to store `[1,2,3,4,5]` as metadata against the key _one2five_
- if we want to store `1` as metadata with the key _one_

then our configuration would be :

```xml
  <jslt-metadata-service>
   <jslt-to-metadata>
    <expression>.foo.bar</expression>
    <key>one2five</key>
   </jslt-to-metadata>
   <jslt-to-metadata>
    <expression>.foo.bar[0]</expression>
    <key>one</key>
   </jslt-to-metadata>
  </jslt-metadata-service>
```
