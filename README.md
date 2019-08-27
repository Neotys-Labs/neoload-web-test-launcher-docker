What is NeoLoad?
================
[NeoLoad](http://www.neotys.com/neoload/overview) is a load testing solution developed by [Neotys](http://www.neotys.com). NeoLoad realistically simulates user activity and monitors infrastructure behavior so you can eliminate bottlenecks in all your web and mobile applications.
NeoLoad Web allows you to start you load test and analyse the test results from a shared platform.

This image allows you to run NeoLoad tests from NeoLoad Web. This image will connect to NeoLoad Web REST API and 
start a test using the NeoLoad project stored in the */neoload-project* folder.

Usage
=====
### Using a volume mapping
    docker run -d --rm \
            -v /localpath/myProject:/neoload-project
            -e NEOLOADWEB_API_URL={nlweb-onpremise-apiurl:port} \
            -e NEOLOADWEB_FILES_API_URL= {nlweb-onpremise-file-apiurl:port}\
            -e NEOLOADWEB_TOKEN={nlweb-token} \
            -e TEST_NAME={test-name} \
            -e SCENARIO_NAME={scenario-name} \
            -e CONTROLLER_ZONE_ID={controller-zone} \
            -e LG_ZONE_IDS={lg-zones:lg-number} \
            neotys/neoload-web-test-launcher
            
### Using project URL
    docker run -d --rm \
            -e NEOLOAD_PROJECT_URL={url-to-project-zip}
            -e NEOLOADWEB_API_URL={nlweb-onpremise-apiurl:port} \
            -e NEOLOADWEB_FILES_API_URL= {nlweb-onpremise-file-apiurl:port}\
            -e NEOLOADWEB_TOKEN={nlweb-token} \
            -e TEST_NAME={test-name} \
            -e SCENARIO_NAME={scenario-name} \
            -e CONTROLLER_ZONE_ID={controller-zone} \
            -e LG_ZONE_IDS={lg-zones:lg-number} \
            neotys/neoload-web-test-launcher

### Parameters
| Env | Comment | Example |
| ------------------------ | --------------------------------------------- | ---------------- |
| NEOLOAD_PROJECT_URL (Optional) |  A zipped version of he NeoLoad project to launch. Optional, is only if volume containing the project is not mapped | https://github.com/me/myProject/raw/master/neoload-project/Archive/smokeTest.zip
| NEOLOADWEB_API_URL (Optional) |  The NeoLoad Web API URL. Optional, is only required for NeoLoad Web OnPremise deployment. If not present, the Controller will use NeoLoad Web SAAS. | https://neoload.mycompany.com:8080 |
| NEOLOADWEB_FILES_API_URL (Optional) |  The NeoLoad Web Files API URL. Optional, is only required for NeoLoad Web OnPremise deployment. If not present, the Controller will use NeoLoad Web SAAS. | https://neoload.mycompany.com:8080 |
| NEOLOADWEB_TOKEN | The NeoLoad Web API token. | 9be32780c6ec86d92jk0d1d25c |
| NEOLOADWEB_PROXY (Optional) | The proxy URL to access NeoLoad Web | http://login:password@myproxy |
| TEST_NAME | The name of the test result. | MyProject non regression test |
| SCENARIO_NAME (Optional) | The scenario name to launch as it appear in the NeoLoad project. This parameter is optional if only one scenario exist in the project. | MyLargeScenario |
| CONTROLLER_ZONE_ID | The controller zone Id. | ZoneId |
| LG_ZONE_IDS | The LG zones with the number of the LGs. | ZoneId1:10,ZoneId2:5 |

### Use this image in a CI environment
- [Use this image in **GitLab CI**](GitLab-usage.md)
- [Use this image in **AWS CodeBuild**](CodeBuild-usage.md)

License
---------
NeoLoad is licensed under the following [License Agreement](http://www.neotys.com/documents/legal/eula/neoload/eula_en.html). You must agree to this license agreement to download and use the image.

Note: This license does not permit further distribution.


User Feedback
------------------
For general issues relating to NeoLoad you can get help from [Neotys Support](https://www.neotys.com/community/?from=%2Faccountarea%2Fcasecreate.php) or [Neotys Community](http://answers.neotys.com/). 
