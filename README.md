# farsightwm/testing-core
[![License: MIT](https://img.shields.io/badge/License-MIT-silver.svg)](https://opensource.org/licenses/MIT)

This project is intended to help testing IS services in an automated manner. Therefore it contains mean to mock services and an API for _farsightwm/testing-jbehave_ (or other 3rd party tools) to run test-cases with mocked services.

***This is still work in progress!***

## Requirements

Currently tested only with webMethods IntegrationServer 10.5

## Installation
 1. Copy the IS package from src/is into your Integration Server

 1. Build the (jar) libraries
    1. farsightwm/utils
    1. farsightwm/testing-utils
    1. farsightwm/testing-core

    and place them into the code/jars directory of the package.
    
1. Download Apache Commons Jexl 3.1 jar and place it into code/jars (e.g. from [Maven Central Repository](https://mvnrepository.com/artifact/org.apache.commons/commons-jexl3/3.1))

 1. Activate the package and run the service _farsightwm.testing.verify:checkFrameworkCorrectlyConfigured_ and confirm that the message reports the framework as operational.

 1. (optional) Install _farsightwm/testing-jbehave_ into your Designer (or another Eclipse installation) and use it to create test-stories.
