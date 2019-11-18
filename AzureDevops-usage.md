## How to use this image to run NeoLoad test in your Azure DevOps pipeline

### Using As Code pipeline definition
Azure DevOps [pipelines](https://azure.microsoft.com/en-us/services/devops/pipelines/) can be configured using a YAML file called `azure-pipelines.yml` within each project.
Using Azure DevOps pipeline, you can easily run a Docker image. 

Since this image returns a non zero exit code when the test launched is failed, you can integrate a load test directly in your pipeline by running this docker image, bellow is an example of a minimalist `azure-pipelines.yml` file starting a NeoLoad load test:

```
pool:
  name: Azure Pipelines
steps:
- task: DockerInstaller@0
  displayName: 'Install Docker 17.09.0-ce'

- script: |
   docker run --rm \
      -v "$(Build.Repository.LocalPath)/neoload-project/archive":/neoload-project \
      -e SCENARIO_NAME=smoke-test \
      -e NEOLOADWEB_TOKEN=<Your Token> \
      -e TEST_NAME=CI-smoke \
      -e CONTROLLER_ZONE_ID=<Your ZoneID>  \
       -e LG_ZONE_IDS=<ZoneID#1>:<LG count#1>, <ZoneID#2>:<LG count#2>  \
      neotys/neoload-web-test-launcher:latest
   
  displayName: 'Launch NeoLoad test'
```
> **Note:** This example uses the NeoLoad project stored in your Azure DevOps repository in the "neoload-project/archive" folder.
If your NeoLoad project is not stored in your Azure DevOps repository, you can use the `NEOLOAD_PROJECT_URL` environment parameter instead of mapping the `"/neoload-project" ` folder.

### Using UI pipeline definition
Azure DevOps [pipelines](https://azure.microsoft.com/en-us/services/devops/pipelines/) can be configured using the Azure DevOps web portal. In this case, you can add `Tasks` to your pipeline using the web interface.
To launch a NeoLoad project, simply add 2 tasks running on a **Linux agent**:
- Install Docker using `Docker CLI installer` task
- Run this container using `Command line` task. This task must execute the following script:
```
docker run --rm \
      -v "$(Build.Repository.LocalPath)/neoload-project/archive":/neoload-project \
      -e SCENARIO_NAME=smoke-test \
      -e NEOLOADWEB_TOKEN=<Your Token> \
      -e TEST_NAME=CI-smoke \
      -e CONTROLLER_ZONE_ID=<Your ZoneID>  \
       -e LG_ZONE_IDS=<ZoneID#1>:<LG count#1>, <ZoneID#2>:<LG count#2>  \
      neotys/neoload-web-test-launcher:latest
```