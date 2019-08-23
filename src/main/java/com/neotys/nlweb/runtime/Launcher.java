package com.neotys.nlweb.runtime;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.ResultsApi;
import io.swagger.client.api.RuntimeApi;
import io.swagger.client.model.ProjectDefinition;
import io.swagger.client.model.RunTestDefinition;
import io.swagger.client.model.TestDefinition;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

import java.io.File;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Launcher {


    static final String PROJECT_FOLDER = "//neoload-project";
    static final String RESULT_FOLDER = "/neoload-result";

    static final String ENV_TOKEN = "NEOLOADWEB_TOKEN";
    static final String ENV_PROJECT_URL = "NEOLOAD_PROJECT_URL";
    static final String ENV_CONTROLLER_ZONE = "CONTROLLER_ZONE_ID";
    static final String ENV_LG_ZONES = "LG_ZONE_IDS";
    static final String ENV_TEST_NAME = "TEST_NAME";
    static final String ENV_SCENARIO_NAME = "SCENARIO_NAME";
    static final String ENV_API_URL = "NEOLOADWEB_API_URL";
    static final String ENV_FILES_API_URL = "NEOLOADWEB_FILES_API_URL";
    static final String ENV_PROXY = "NEOLOADWEB_PROXY";

    public static void main(String[] args) {

        try {
            getProjectUrl().ifPresent(url -> downloadFile(url));

            String projectFile = getProjectFileName(PROJECT_FOLDER);
            if (projectFile == null) {
                throw new IllegalArgumentException("Project folder does not contain a NeoLoad project or YAML file");
            }

            RuntimeApi runtimeApi = new RuntimeApi();

            String testId = launchTest(projectFile, runtimeApi);
            if (testId != null) {
                ResultsApi resultsApi = new ResultsApi();
                waitForTestFinished(testId, resultsApi);
            } else {
                // error occurs when launching the test
                System.exit(1);
            }
        }catch (Throwable e) {
            // For all uncaught exception, just fail, an error message should already be logged
            System.exit(1);
        }

    }


    @VisibleForTesting
    static String getNlwebApiURL() {
        return Optional.ofNullable(System.getenv(ENV_API_URL)).orElse("https://neoload-api.saas.neotys.com/v1");
    }

    @VisibleForTesting
    static String getNlwebFilesApiURL() {
        return Optional.ofNullable(System.getenv(ENV_FILES_API_URL)).orElse("https://neoload-files.saas.neotys.com/v1");
    }

    @VisibleForTesting
    static String getToken() {
        return System.getenv(ENV_TOKEN);
    }

    @VisibleForTesting
    static String getControllerZoneId() {
        return System.getenv(ENV_CONTROLLER_ZONE);
    }

    @VisibleForTesting
    static String getLgsZoneId() {
        return System.getenv(ENV_LG_ZONES);
    }

    @VisibleForTesting
    static String getTestName() {
        return Optional.ofNullable(System.getenv(ENV_TEST_NAME)).orElse("myTest");
    }

    @VisibleForTesting
    static String getScenarioName(ProjectDefinition projectDefinition) {
        String scenarioName = System.getenv(ENV_SCENARIO_NAME);
        if(scenarioName==null && projectDefinition.getScenarios().size()==1) {
            scenarioName = projectDefinition.getScenarios().get(0).getScenarioName();
        }
        return scenarioName;
    }

    @VisibleForTesting
    static Optional<String> getProjectUrl() {
        return Optional.ofNullable(System.getenv(ENV_PROJECT_URL));
    }

    @VisibleForTesting
    static void validateEnvParameters() {
        if(Strings.isNullOrEmpty(getToken())) {
            System.err.println("Token not defined");
            throw new IllegalArgumentException("API Token is not defined. please fill the \""+ENV_TOKEN+"\" ENV variable.");
        }

        if(Strings.isNullOrEmpty(getControllerZoneId())) {
            System.err.println("Controller zone id not defined");
            throw new IllegalArgumentException("Controller zone is not defined. Please fill the \""+ENV_CONTROLLER_ZONE+"\" ENV variable");
        }

        if(Strings.isNullOrEmpty(getLgsZoneId())) {
            System.err.println("LG zones ids not defined");
            throw new IllegalArgumentException("Load Generator zone is not defined. Please fill the \""+ENV_LG_ZONES+"\" ENV variable");
        }
    }

    @VisibleForTesting
    static String launchTest(String projectFile, RuntimeApi runtimeApi) {
        validateEnvParameters();

        setupClientApi(runtimeApi.getApiClient());

        try {

            runtimeApi.getApiClient().setBasePath(getNlwebFilesApiURL());

            System.out.println("Uploading project");
            int readTimeout = runtimeApi.getApiClient().getReadTimeout();
            runtimeApi.getApiClient().setReadTimeout(300000);
            int writeTimeout = runtimeApi.getApiClient().getWriteTimeout();
            runtimeApi.getApiClient().setWriteTimeout(60000);
            ProjectDefinition projectDefinition = runtimeApi.postUploadProject(new File(PROJECT_FOLDER + File.separator + projectFile));
            System.out.println("Project uploaded");
            runtimeApi.getApiClient().setReadTimeout(readTimeout);
            runtimeApi.getApiClient().setWriteTimeout(writeTimeout);
            String scenarioName = getScenarioName(projectDefinition);
            if(scenarioName==null) {
                System.err.println("Scenario name is not defined");
                throw new IllegalArgumentException("Scenario name is not defined, and project contains several scenario. Please fill the \""+ENV_SCENARIO_NAME+"\" ENV variable");
            }

            System.out.println("Starting test");
            runtimeApi.getApiClient().setBasePath(getNlwebApiURL());
            RunTestDefinition runTestDefinition = runtimeApi.getTestsRun(getTestName(),
                    projectDefinition.getProjectId(),
                    scenarioName,
                    "",
                    null,
                    null,
                    null,
                    0,
                    0,
                    getControllerZoneId(),
                    getLgsZoneId());
            System.out.println("Test launched with id: "+runTestDefinition.getTestId());
            return runTestDefinition.getTestId();
        }catch(ApiException e) {
            System.err.println("Error code: "+e.getCode());
            System.err.println(e.getResponseBody());
            e.printStackTrace(System.err);
        }
        return null;

    }

    static void waitForTestFinished(String testId, ResultsApi resultsApi) {
        setupClientApi(resultsApi.getApiClient());
        List knownStatuses = new ArrayList<>();
        try {
            TestDefinition testDefinition;
            do {
                Thread.currentThread().sleep(5000);
                testDefinition = resultsApi.getTest(testId);
                if(!knownStatuses.contains(testDefinition.getStatus())) {
                    if(knownStatuses.size()>0 && knownStatuses.get(knownStatuses.size()-1).equals(TestDefinition.StatusEnum.RUNNING)) {
                        System.out.println();
                    }
                    knownStatuses.add(testDefinition.getStatus());
                    System.out.println("Test status: "+testDefinition.getStatus());
                }
                if(testDefinition.getStatus()== TestDefinition.StatusEnum.RUNNING) System.out.print(".");
            } while (testDefinition.getStatus() != TestDefinition.StatusEnum.TERMINATED && testDefinition.getQualityStatus()!= TestDefinition.QualityStatusEnum.COMPUTING);
            System.out.println();
            if(testDefinition.getQualityStatus()!= TestDefinition.QualityStatusEnum.PASSED) {
                System.out.println("Test finished with a failure status:"+testDefinition.getQualityStatus());
                System.exit(1);
            }
            System.out.println("Test finished successfully");
        }catch(InterruptedException | ApiException e) {
            System.err.println("Error during polling test result");
            if(e.getMessage().equalsIgnoreCase("not found")) {
                System.err.println("The testId is not found");
            } else {
                e.printStackTrace(System.err);
            }
        }
    }

    static void generateReport(String testId, ResultsApi resultsApi) {
        setupClientApi(resultsApi.getApiClient());
        try {
            TestDefinition testDefinition = resultsApi.getTest(testId);
            if(testDefinition!=null && testDefinition.getStatus().equals(TestDefinition.StatusEnum.TERMINATED)) {
                System.out.println(testDefinition.toString());
            } else {
                throw new IllegalArgumentException("Cannot generate test result from a non existing or not terminated test");
            }
        }catch(ApiException e) {
            System.err.println("Error when generating test result");
            e.printStackTrace(System.err);
        }
    }

    private static void setupClientApi(ApiClient apiClient) {
        apiClient.setApiKey(getToken());

        apiClient.setBasePath(getNlwebApiURL());

        Proxy proxy = getProxy(System.getenv(ENV_PROXY));
        if (proxy != null) apiClient.getHttpClient().setProxy(proxy);
    }

    static Proxy getProxy(final String proxyUrl) {
        if(proxyUrl==null) return null;

        Proxy proxy;
        try {
            URL address = new URL(proxyUrl);
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(address.getHost(), address.getPort()));
            String userInfo = address.getUserInfo();
            if(userInfo!=null && userInfo.contains(":")) {
                final String login = userInfo.substring(0, userInfo.indexOf(":"));
                final String password = userInfo.substring(userInfo.indexOf(":"));
                Authenticator.setDefault(
                        new Authenticator() {
                            @Override
                            public PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(
                                        login, password.toCharArray());
                            }
                        }
                );
            }
        }catch (Exception e) {
            System.out.println("Invalid proxy url, no proxy will be used");
            e.printStackTrace(System.err);
            return null;
        }
        return proxy;
    }


    /**
     * Return the project file name (zip or YAML) or null if it cannot be found.
     * @param folder the folder containing the NeoLoad project
     * @return the project file name.
     */
    @VisibleForTesting
    static String getProjectFileName(String folder) {
        File projectFile = new File(folder);
        if(!projectFile.isDirectory()) {
            System.err.println("Project folder must be a folder");
            return null;
        }
        String[] listProjectFiles = projectFile.list();
        if(listProjectFiles==null || listProjectFiles.length==0) throw new IllegalArgumentException("Project folder must exist and must contains a NeoLoad project or YAML project definition.");
        // First look for a .nlp file. If found, zip and and return this zip
        long numberOfNlp = Arrays.stream(listProjectFiles).filter(s -> s.toLowerCase().endsWith(".nlp")).count();
        if(numberOfNlp==1) {
            try {
                ZipParameters parameters = new ZipParameters();
                parameters.setIncludeRootFolder(false);
                new ZipFile(folder+File.separator+"project.zip").addFolder(new File(folder), parameters);
            }catch(ZipException e) {
                System.err.println("Error zipping the project");
                e.printStackTrace(System.err);
                return null;
            }
            return folder+File.separator+"project.zip";
        }
        if(numberOfNlp>1) {
            System.err.println("More than one NeoLoad project .zip file found");
            return null;
        }
        // Then look if there is a single Zip in the folder, if yes, return this single zip as to zipped project to use
        long numberOfZip = Arrays.stream(listProjectFiles).filter(s -> s.toLowerCase().endsWith(".zip")).count();
        if(numberOfZip==1) {
            return Arrays.stream(listProjectFiles).filter(s -> s.toLowerCase().endsWith(".zip")).findFirst().get();
        }
        // Then look if there is a single yaml file in the folder, if yes, return this single yaml as the as code project to use
        if(numberOfZip>1) {
            System.err.println("More than one NeoLoad project .zip file found");
            return null;
        }

        long numberOfYaml = Arrays.stream(listProjectFiles).filter(s -> s.toLowerCase().endsWith(".yaml") || s.toLowerCase().endsWith(".yml")).count();
        if(numberOfYaml==1) {
            return Arrays.stream(listProjectFiles).filter(s -> s.toLowerCase().endsWith(".yaml") || s.toLowerCase().endsWith(".yml")).findFirst().get();
        }
        // Then look if there are several yaml file in the folder, look if we can find a default.yaml
        // TODO we should zip all the YAML and return the zip since the default.yaml file may contain includes
        if(numberOfYaml>1) {
            Optional<String> defaultYaml = Arrays.stream(listProjectFiles).filter(s -> s.equalsIgnoreCase("default.yaml") || s.equalsIgnoreCase("default.yml")).findFirst();
            if(defaultYaml.isPresent()) return defaultYaml.get();
        }
        System.err.println("Cannot find a NeoLoad project in the /neoload-project folder");
        return null;

    }

    static Path downloadFile(String url) {
        System.out.println("Downloading project file from: "+url);
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            final String extension = getProjectUrlExtension(url).orElse("zip");

            Files.createDirectory(Paths.get(PROJECT_FOLDER));
            Path projectFile = Files.createFile(Paths.get(PROJECT_FOLDER, "project."+getProjectUrlExtension(url).orElse("zip")));

            HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(projectFile));

            if(response.statusCode()==200) {
                System.out.println("Project file downloaded and stored in "+projectFile.toString());
                return projectFile;
            } else {
                System.err.println(response.statusCode());
                System.err.println(response.body());
            }
        }catch(Exception e) {
            System.err.println("Error while downloading file.");
            e.printStackTrace(System.err);
        }
        return null;
    }

    /**
     * Return the extension of the file ending the URL if it is one of the supported extension.
     * Supported extensions are Zip, Yaml and Yml.
     * @param the url
     * @return the extension or <code>Optional.empty</code> for non compatible extensions.
     */
    static Optional<String> getProjectUrlExtension(String url) {
        if(url!=null && url.contains(".")) {
            final List extensions = List.of("zip", "yaml", "yml");
            String urlExtension = url.substring(url.lastIndexOf(".")+1);
            if(extensions.contains(urlExtension)) {
                return Optional.of(urlExtension);
            }
        }
        return Optional.empty();
    }
}
