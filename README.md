# Jasmine Gradle Plugin

Support for running Jasmine tests in a Gradle build. And more.

* Your Jasmine specs run against source code in situ, as well as part of the build

* Easy to include JSLint reports with the Jasmine output.

* Supports mulitple dashboard pages that aggregate Jasmine SpecRunners.

* Build fails if Jasmine and/or JSLint fail. Or not, if you prefer.

* jasmineGenerate creates template files and required libraries in src/test/javascript

* Built to play nice with others.

Same licensing as Jasmine and JSLint.

## Useful commands for development:

* gradle publishToMavenLocal (publishes plugin to mvnlocal)
* gradle publish (publishes to defined remote mvn)
* gradle wrapper (to generate the wrapper for using the correct gradle version)
