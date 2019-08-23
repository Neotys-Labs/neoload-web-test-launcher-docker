## How to use this image to run NeoLoad test in your CodeBuild pipeline

AWS CodeBuild is a fully managed continuous integration service that compiles source code, runs tests, and produces software packages that are ready to deploy. More information on  [CodeBuild](https://aws.amazon.com/codebuild/).
CodeBuild are configured using a YAML file called `buildspec.yml` within each project.
Using CodeBuild, you can easily run a Docker image. 
Since this image returns a non zero exit code when the test launched is failed, you can integrate a load test directly in your pipeline by running this docker image, bellow is an example of a minimalist `buildspec.yml` file starting a NeoLoad load test:
```
version: 0.2

phases:
  install:
    runtime-versions:
      docker: 18
  pre_build:
    commands:
      - echo Some intialization steps
      - echo ...
  build:
    commands:
      - echo Build started on `date`
      - echo Building the Application...  
  post_build:
    commands:
      - echo Build completed on `date`
      - echo Load test the application
      - >
        docker run --rm 
        -e NEOLOAD_PROJECT_URL=https://github.com/myProfile/myProject/blob/master/neoload-project/Archive/smokeTest.zip?raw=true
        -e SCENARIO_NAME=smoke-test 
        -e NEOLOADWEB_TOKEN=<myToken> 
        -e TEST_NAME=CI-smoke 
        -e CONTROLLER_ZONE_ID=afRmU 
        -e LG_ZONE_IDS=afRmU:1 
        neotys/neoload-web-test-launcher:latest

```
This example will use the NeoLoad project stored in your GitLab repository in the "neoload-project/archive" folder.
If your NeoLoad project is not stored in your GitLab repository, you can use the `NEOLOAD_PROJECT_URL` environment parameter instead of mapping the `"/neoload-project" ` folder.
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTgzMDA1MTgzMF19
-->