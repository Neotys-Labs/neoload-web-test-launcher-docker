# What is NeoLoad?
-----------------------------
[NeoLoad](http://www.neotys.com/neoload/overview) is a load testing solution developed by [Neotys](http://www.neotys.com). NeoLoad realistically simulates user activity and monitors infrastructure behavior so you can eliminate bottlenecks in all your web and mobile applications.
NeoLoad Web allows you to start you load test and analyse the test results from a shared platform.

This image allows you to run NeoLoad tests from NeoLoad Web. This image will connect to NeoLoad Web REST API and 
start a test using the NeoLoad project stored in the */neoload-project* folder.

Usage example:
`docker run -v /local/path/to/my/neoload/project:/neoload-project neotys/neoload-web-test-launcher`

