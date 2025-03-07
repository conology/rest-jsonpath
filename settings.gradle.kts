rootProject.name = "jsonpath"

include(
    "core",
    "mongo-spring"
)

project(":core").projectDir = file("src/core")
project(":mongo-spring").projectDir = file("src/mongo-spring")

