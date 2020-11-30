# interlok-json-web-token

The interlok-json-web-token optional component provides three services
for JWT manipulation: jwt-create, jwt-encode, jwt-decode. It depends on
the interlok-json optional component (and its dependencies).

For more information about JSON Web Tokens [see here](https://github.com/jwtk/jjwt).

## JSON Create

The json-create service will create a JWT string from discreet values of
***issuer***, ***subject***, ***audience***, ***expiration***, ***no
before***. It can also include custom key/value pairs. The output will
be a JWT string.

````xml
    <jwt-creator>
      <unique-id>jwt-create</unique-id>
      <issuer>ashley</issuer>
      <subject>test</subject>
      <audience>everyone</audience>
      <expiration>2040-12-31 00:00:00.0 UTC</expiration>
      <not-before>2020-01-01 00:00:00.0 UTC</not-before>
      <secret class="base64-encoded-secret">
        <secret>c64975ba3cf3f9cd58459710b0a42369f34b0759c9967fb5a47eea488e8bea79</secret>
      </secret>
      <custom-claims>
        <key-value-pair>
          <key>payload</key>
          <value>%message{%payload}</value>
        </key-value-pair>
      </custom-claims>
    </jwt-creator>
````

## JSON Encode

The json-encode service can be used to convert a JSON string into a JWT
string - provided that the necessary JWT keys are present. The output is
a JWT string.

````xml
    <jwt-encode>
      <unique-id>jwt-encode</unique-id>
      <header class="multi-payload-string-input-parameter">
        <payload-id>header</payload-id>
      </header>
      <claims class="multi-payload-string-input-parameter">
        <payload-id>claims</payload-id>
      </claims>
      <secret class="base64-encoded-secret">
        <secret>c64975ba3cf3f9cd58459710b0a42369f34b0759c9967fb5a47eea488e8bea79</secret>
      </secret>
      <jwt-output class="multi-payload-string-output-parameter">
        <payload-id>output</payload-id>
      </jwt-output>
    </jwt-encode>
````

## JSON Decode

The json-decode service will parse a JWT string, validate as necessary,
and output a JSON string.

````xml
    <jwt-decode>
      <unique-id>jwt-decode</unique-id>
      <jwt-string class="string-payload-data-input-parameter"/>
      <secret class="base64-encoded-secret">
        <secret>c64975ba3cf3f9cd58459710b0a42369f34b0759c9967fb5a47eea488e8bea79</secret>
      </secret>
      <header class="multi-payload-string-output-parameter">
        <payload-id>header</payload-id>
      </header>
      <claims class="multi-payload-string-output-parameter">
        <payload-id>claims</payload-id>
      </claims>
    </jwt-decode>
````
