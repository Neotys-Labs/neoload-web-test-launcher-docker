## How to use this image to run NeoLoad test in your GitLab pipeline

GitLab CI/CD [pipelines](https://docs.gitlab.com/ee/ci/pipelines.html) are configured using a YAML file called `.gitlab-ci.yml` within each project.
Using GitLab pipeline, you can easily run a Docker image. 
Since this image returns a non zero exit code when the test launched is failed, you can integrate a load test directly in your pipeline by running this docker image, for example:
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
 -e SCENARIO_NAME=smoke-test-passed
 -e NEOLOADWEB_TOKEN=d509947c287bb0efe472cf9d4a3461ce0af015636c306e7f 
 -e TEST_NAME=CI-smoke 
 -e CONTROLLER_ZONE_ID=afRmU 
 -e LG_ZONE_IDS=afRmU:1 
 neotys/neoload-web-test-launcher:latest
#    - mv sitespeed-results/data/performance.json performance.json
 artifacts:
 paths:
 - performance.json
 - neoload-results/
 except:
 variables:
 - $PERFORMANCE_DISABLED

```
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTE1NjMzMTIzMTNdfQ==
-->