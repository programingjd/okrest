![jcenter](https://img.shields.io/badge/_jcenter_-_3.5.0-6688ff.png?style=flat) &#x2003; ![jcenter](https://img.shields.io/badge/_Tests_-_10/10-green.png?style=flat)
# okrest
A simple http rest server for the jvm, written in groovy with [okserver](https://github.com/programingjd/okserver).

## Download ##

The maven artifacts are on [Bintray](https://bintray.com/programingjd/maven/info.jdavid.ok.rest/view)
and [jcenter](https://bintray.com/search?query=info.jdavid.ok.rest).

[Download](https://bintray.com/artifact/download/programingjd/maven/info/jdavid/ok/rest/okrest/3.5.0/okrest-3.5.0.jar) the latest jar.

__Maven__

Include [those settings](https://bintray.com/repo/downloadMavenRepoSettingsFile/downloadSettings?repoPath=%2Fbintray%2Fjcenter)
 to be able to resolve jcenter artifacts.
```
<dependency>
  <groupId>info.jdavid.ok.rest</groupId>
  <artifactId>okrest</artifactId>
  <version>3.5.0</version>
</dependency>
```
__Gradle__

Add jcenter to the list of maven repositories.
```
repositories {
  jcenter()
}
```
```
dependencies {
  compile 'info.jdavid.ok.rest:okrest:3.5.0'
}
```

## Usage ##

You specify the endpoints by using the method with the method name,
passing a regex for the path and a closure for the handler.
The order in which you define those can be important if a path matches
more than one of the defined regexes. The first match found is used.

The closure parameters are:
  - the request body `Buffer`
  - the request headers `Headers`
  - the regex captured groups `List&lt;String&gt;`

Example: (using [okjson](https://github.com/programingjd/okjson))

```groovy
List data = Collections.synchronizedList([
  [name: 'a', value: '1'],
  [name: 'b', value: '2'],
  [name: 'c', value: '3']
]);
def server = new RestServer().with {
  get('/data') { ->
    return new Response.Builder().statusLine(StatusLines.OK).
      body(Builder.build(data), MediaTypes.JSON).build()
  }
  get('/data/([a-z]+)') { Buffer b, Headers h, List<String> c ->
    def builder = new Response.Builder()
    def found = data.find { it['name'] == c[0] } as Map
    if (found) {
      builder.statusLine(StatusLines.OK).
        body(Builder.build(found), MediaTypes.JSON)
    }
    else {
      builder.statusLine(StatusLines.NOT_FOUND).noBody()
    }
    return builder.build()
  }
  post('/data') { Buffer b, Headers h, List<String> c, HttpUrl url ->
    def builder = new Response.Builder()
    if (MediaType.parse(h.get('Content-Type')) == MediaTypes.JSON) {
      try {
        def obj = Parser.parse(b) as Map
        if (!(obj['name'] && obj['value'] && obj['name'] ==~ /[a-z]+/)) {
          throw new RuntimeException()
        }
        data.add(obj)
        builder.statusLine(StatusLines.OK).noBody()
      }
      catch (ignore) {
        builder.statusLine(StatusLines.BAD_REQUEST).noBody()
      }
    }
    else {
      builder.statusLine(StatusLines.BAD_REQUEST).noBody()
    }
    return builder.build()
  }
  delete('/data/([a-z]+)') { Buffer b, Headers h, List<String> c ->
      def builder = new Response.Builder()
      def found = data.find { it['name'] == c[0] } as Map
      if (found) {
        data.remove(found)
        builder.statusLine(StatusLines.OK).noBody()
      }
      else {
        builder.statusLine(StatusLines.NO_CONTENT).noBody()
      }
      return builder.build()
  }
}
```