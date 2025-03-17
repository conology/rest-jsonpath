# Jsonpath REST Filters
[![Build Status](https://github.com/conology/rest-jsonpath/actions/workflows/verify.yml/badge.svg)](https://github.com/goatfryed/assert-baseline/actions/workflows/verify.yml)

Enhance your rest collection endpoints with json path compatible filter queries

This projects aims to support execution of a large, but sensible subset of jsonpath filter queries
on rest apis backed by mongoDB collections within a spring project.

## Getting started
Add the dependency
```groovy
implementation("net.conology.spring:rest-jsonpath-mongodb:{version}")
```

````xml
<dependencies>
    <dependency>
        <groupId>net.conology.spring</groupId>
        <artifactId>rest-jsonpath-mongodb</artifactId>
        <version>{version}</version>
    </dependency>
</dependencies>
````

Compile and execute your query

```java
String query = "books[?@.author.firstName == \"Charles\" && @.author.lastName == \"Darwin\"]";
JsonPathCriteriaCompiler compiler = new JsonPathCriteriaCompilerBuilder().build();
Criteria criteria = compiler.compile(query);
List<Store> matches = mongoTemplate.find(query(criteria),Store.class);
```

## Query writing
This library translates [JsonPath](https://datatracker.ietf.org/doc/html/rfc9535#name-filter-selector)
[Filter Selectors](https://datatracker.ietf.org/doc/html/rfc9535#name-filter-selector)
to collection queries.

Important! We don't translate jsonpath, but collection filter
- ❌ `$[?@.name == "Tony"]`
- ☑️ `@.name == "Tony"`

We don't aim to select fields or embedded documents. We only aim to query collections.

### Examples
Assume a collection of stores
```json
[
  {
    "name": "Timeless Stories",
    "books": [
      {
        "title": "A funny tale",
        "author": "Chucky Norelle"
      }
    ]
  }
]
```
Filter by name\
`@.name == "Timeless Stories"`

Filter by stores with a book\
`@.books[?@.title == "A funny tale"]`

You can omit the top level current node identifier `@`\
`books[?@.title == "A funny tale"]`

Multiple or connected queries can be provided comma-separated as well.\
`firstName == "Stark",lastName == "Tony"`


You can check our currently supported query translation 
[in this csv](./src/spring-mongo/src/test/resources/MongoCriteriaCompilerPassTest.csv).

### Limitations
- No nested or-Support



