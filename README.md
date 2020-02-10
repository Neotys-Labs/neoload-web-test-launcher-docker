What is NeoLoad?
================
[NeoLoad](http://www.neotys.com/neoload/overview) is a load testing solution developed by [Neotys](http://www.neotys.com). NeoLoad realistically simulates user activity and monitors infrastructure behavior so you can eliminate bottlenecks in all your web and mobile applications.
NeoLoad Web allows you to start you load test and analyse the test results from a shared platform.

This image allows you to run NeoLoad tests from NeoLoad Web. This image will connect to NeoLoad Web REST API and 
start a test using the NeoLoad project, stored in the */neoload-project* folder, or downloaded from an URL.
The container will wait until the end of the test and return the test quality status as the return code (0 if the test 
status is PASSED, 1 otherwise).

The */neoload-project* folder can contains:
 * A NeoLoad project folder including <project>.nlp, config.zip ...
 * A single zip file containing the NeoLoad project
 * A single yaml file containing the NeoLoad project as code

Usage
=====
### Using a volume mapping
    docker run --rm \
            -v /localpath/myProject:/neoload-project \
            -e NEOLOADWEB_API_URL={nlweb-onpremise-apiurl:port} \
            -e NEOLOADWEB_FILES_API_URL= {nlweb-onpremise-file-apiurl:port}\
            -e NEOLOADWEB_TOKEN={nlweb-token} \
            -e TEST_RESULT_NAME={test-result-name} \
            -e SCENARIO_NAME={scenario-name} \
            -e CONTROLLER_ZONE_ID={controller-zone} \
            -e LG_ZONE_IDS={lg-zones:lg-number} \
            neotys/neoload-web-test-launcher
            
### Using project URL
    docker run --rm \
            -e NEOLOAD_PROJECT_URL={url-to-project-zip} \
            -e NEOLOADWEB_API_URL={nlweb-onpremise-apiurl:port} \
            -e NEOLOADWEB_FILES_API_URL= {nlweb-onpremise-file-apiurl:port}\
            -e NEOLOADWEB_TOKEN={nlweb-token} \
            -e TEST_RESULT_NAME={test-result-name} \
            -e SCENARIO_NAME={scenario-name} \
            -e CONTROLLER_ZONE_ID={controller-zone} \
            -e LG_ZONE_IDS={lg-zones:lg-number} \
            neotys/neoload-web-test-launcher
            
### Additional parameters to handle reservations

#### Launching in the context of a given reservation

Insert parameter:

    -e RESERVATION_ID={reservation-id} \
    
#### Launching with an auto-reservation

Insert parameters:

    -e RESERVATION_DURATION={reservation-duration} \
    -e RESERVATION_WEB_VUS={reservation-web-vus} \

### Parameters
| Env | Comment | Example |
| ------------------------ | --------------------------------------------- | ---------------- |
| NEOLOAD_PROJECT_URL (Optional) |  A zipped version of he NeoLoad project to launch. Optional, is only if volume containing the project is not mapped | https://github.com/me/myProject/raw/master/neoload-project/Archive/smokeTest.zip
| NEOLOADWEB_API_URL (Optional) |  The NeoLoad Web API URL. Optional, is only required for NeoLoad Web OnPremise deployment. If not present, the Controller will use NeoLoad Web SAAS. | https://neoload.mycompany.com:8080 |
| NEOLOADWEB_FILES_API_URL (Optional) |  The NeoLoad Web Files API URL. Optional, is only required for NeoLoad Web OnPremise deployment. If not present, the Controller will use NeoLoad Web SAAS. | https://neoload.mycompany.com:8080 |
| NEOLOADWEB_TOKEN | The NeoLoad Web API token. | 9be32780c6ec86d92jk0d1d25c |
| NEOLOADWEB_PROXY (Optional) | The proxy URL to access NeoLoad Web | http://login:password@myproxy |
| TEST_RESULT_NAME | The name of the test result. | MyProject non regression test |
| TEST_RESULT_DESCRIPTION (optional) | The description of the test result. | My test description |
| SCENARIO_NAME (Optional) | The scenario name to launch as it appear in the NeoLoad project. This parameter is optional if only one scenario exist in the project. | MyLargeScenario |
| AS_CODE_FILES (Optional) | The comma-separated as-code files to use for the test. Those files must be part of the uploaded project. | path/to/file1.yaml,path/to/file2.yaml |
| RESERVATION_ID (Optional) | The reservation identifier. | 1a73af8d-7222-41ca-b5b4-995e1a4a5175 |
| RESERVATION_DURATION (Optional) | The duration of the reservation for the test (in seconds). | 1200 |
| RESERVATION_WEB_VUS (Optional) | The number of Web Virtual Users to be reserved for the test. | 50 |
| RESERVATION_SAP_VUS (Optional) | The number of SAP Virtual Users to be reserved for the test.| 0 |
| CONTROLLER_ZONE_ID | The controller zone Id. | ZoneId |
| LG_ZONE_IDS | The LG zones with the number of the LGs. | ZoneId1:10,ZoneId2:5 |
| NO_CHECK_CERTIFICATE  (Optional) | Don't check the server certificate against the available certificate authorities. | true |


### Use this image in a CI environment
- [Use this image in **GitLab CI**](https://github.com/Neotys-Labs/neoload-web-test-launcher-docker/blob/master/GitLab-usage.md)
- [Use this image in **AWS CodeBuild**](https://github.com/Neotys-Labs/neoload-web-test-launcher-docker/blob/master/CodeBuild-usage.md)
- [Use this image in **Azure DevOps Pipelines**](https://github.com/Neotys-Labs/neoload-web-test-launcher-docker/blob/master/AzureDevops-usage.md)

License
---------
NeoLoad is licensed under the following [License Agreement](http://www.neotys.com/documents/legal/eula/neoload/eula_en.html). You must agree to this license agreement to download and use the image.

Note: This license does not permit further distribution.


User Feedback
------------------
For general issues relating to NeoLoad you can get help from [Neotys Support](https://www.neotys.com/community/?from=%2Faccountarea%2Fcasecreate.php) or [Neotys Community](http://answers.neotys.com/). 
