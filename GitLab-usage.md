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
   - |
     if ! docker info &>/dev/null; then
        if [ -z "$DOCKER_HOST" -a "$KUBERNETES_PORT" ]; then
           export DOCKER_HOST='tcp://localhost:2375'
        fi
     fi
   - >
     docker run --rm 
     -v "$(pwd)/neoload-project/archive":/neoload-project 
     -e SCENARIO_NAME=smoke-test
     -e NEOLOADWEB_TOKEN=<Your Token>
     -e TEST_NAME=CI-smoke 
     -e CONTROLLER_ZONE_ID=<Your ZoneID> 
     -e LG_ZONE_IDS=<ZoneID#1>:<LG count#1>, <ZoneID#2>:<LG count#2> 
     neotys/neoload-web-test-launcher:latest
 except:
   variables:
     - $PERFORMANCE_DISABLED

```
This example uses the NeoLoad project stored in your GitLab repository in the "neoload-project/archive" folder.
If your NeoLoad project is not stored in your GitLab repository, you can use the `NEOLOAD_PROJECT_URL` environment parameter instead of mapping the `"/neoload-project" ` folder.
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTEzNDYzNDg0ODJdfQ==
-->