## How to use this image to run NeoLoad test in your GitLab pipeline

GitLab CI/CD [pipelines](https://docs.gitlab.com/ee/ci/pipelines.html) are configured using a YAML file called `.gitlab-ci.yml` within each project.
Using GitLab pipeline, you can easily run a Docker image. 
Since this image return a non zero exit code when the test launched 
`script`  is the only required keyword that a job needs. It’s a shell script which is executed by the Runner. For example:

```
job:
  script: "bundle exec rspec"

```

This paramet
<!--stackedit_data:
eyJoaXN0b3J5IjpbMTAzMDYzNTM2OV19
-->