## How to use this image to run NeoLoad test in your GitLab pipeline

GitLab CI/CD [pipelines](https://docs.gitlab.com/ee/ci/pipelines.html) are configured using a YAML file called `.gitlab-ci.yml` within each project.
Using GitLab pipeline, you can easily run a Docker image. 

Since this image returns a non zero exit code when the test launched is failed, you can integrate a load test directly in your pipeline by running this docker image, bellow is an example of a minimalist `.gitlab-ci.yml` file starting a NeoLoad load test:
```
stages:
 - test
loadtest:
 stage: test
 image: docker:stable
 allow_failure: false
 services:
   - docker:stable-dind
 script:
   - >
     docker run --rm 
     -v "$(pwd)/neoload-project/archive":/neoload-project 
     -e SCENARIO_NAME=smoke-test
     -e NEOLOADWEB_TOKEN=<Your Token>
     -e TEST_NAME=CI-smoke 
     -e RESERVATION_ID=<ReservationId>
     -e RESERVATION_DURATION=<ReservationDuration>
     -e RESERVATION_WEB_VUS=<ReservationNumberOfWebVUs>
     -e RESERVATION_SAP_VUS=<ReservationNumberOfSapVUs>
     -e CONTROLLER_ZONE_ID=<Your ZoneID> 
     -e LG_ZONE_IDS=<ZoneID#1>:<LG count#1>, <ZoneID#2>:<LG count#2> 
     neotys/neoload-web-test-launcher:latest

```
> **Note:** This example uses the NeoLoad project stored in your GitLab repository in the "neoload-project/archive" folder.
If your NeoLoad project is not stored in your GitLab repository, you can use the `NEOLOAD_PROJECT_URL` environment parameter instead of mapping the `"/neoload-project" ` folder.
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTg2NDQ1OTM1NSw5ODg5MDU0MjZdfQ==
-->