# Concept jsonpath rest filter query

## JSONPath

[\[IETF RFC\]](https://datatracker.ietf.org/doc/rfc9535/)
[\[Wikipedia\]](https://en.wikipedia.org/wiki/JSONPath)
[\[Web Evaluator\]](https://jsonpath.com)

JSONPath is a standardized query expression language to select data in JSON.
The concept isn't new and various implementations exist.
The official standard on the other hand was proposed fairly recently at the beginning of 2024.

## JSONPath, REST and TMF

The TeleManagement Forum (TMForum or TMF) defines powerful REST filter expressions
based on JSONPath in their [TMF630 REST API Design Guideline 4.2.0 Part Six](https://www.tmforum.org/resources/specifications/tmf630-rest-api-design-guidelines-4-2-0/).

This is the major inspiration and driving force behind this project. As the primary use case
we want to enable REST APIs to perform advanced queries based on JSONPath.

The idea is that collection endpoints may support filter expressions to select a subset of entities in a collection.
While this could be used for selection of any datatype on a given endpoint, we still want to provide
a well-designed REST API. So based on the **Uniform Interface** constraint
and more specifically the **Resource Identification** constraint JSONPath query expressions are only defined
for filtering and not for sub-resource selection.

Note that the TMForum Guideline is from 2021 and therefore older than the now proposed standard.
We weigh RFC9535 stronger.

## Mapping REST API to Json model
Let's assume we have a collection of pets at `/pet`.

A pet might look this
```json
{
    "type": "Cat",
    "name": "Lady",
    "tags": [{
        "id": 1,
        "name": "chill"
    }, {
        "id": 2,
        "name": "cuddly"
    }]
}
```

And thus our pet collection looks like this
```json
[{
  "type": "Cat",
  "name": "Lady",
  "tags": [{
    "id": 1,
    "name": "lovely"
  }, {
    "id": 2,
    "name": "cuddly"
  }]
}, {
  "type": "Dog",
  "name": "Spencer",
  "tags": [{
    "id": 3,
    "name": "bashful"
  }, {
    "id": 2,
    "name": "cuddly"
  }]
}]
```

On this JSON collection `$[?@.type=="Cat"]` filters all the cats.
`$[?@.type=="Cat" && @.tags[?@.name=="lovely"]].tags[*]` filters all tags for all lovely cats

As explained in the previous section, we want to satisfy our **Uniform Interface** REST constraint.
The second expression does not conform to this spirit, as it would return something other than cat on a `/cat`-endpoint.

Only expressions that use just [filter selectors](https://www.rfc-editor.org/rfc/rfc9535.html#name-filter-selector)
are reasonable expressions within this constraint. To simply the syntax, we omit the enclosing `$[?(...)]`.
