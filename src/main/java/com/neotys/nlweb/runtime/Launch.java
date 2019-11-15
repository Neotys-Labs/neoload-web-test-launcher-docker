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

import java.net.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Launch {


    static final String PROJECT_FOLDER = "//neoload-project";
    static final String RESULT_FOLDER = "/neoload-result";

    static final String ENV_TOKEN = "NEOLOADWEB_TOKEN";
    static final String ENV_PROJECT_URL = "NEOLOAD_PROJECT_URL";
    static final String RESERVATION_ID = "RESERVATION_ID";
    static final String RESERVATION_DURATION = "RESERVATION_DURATION";
    static final String RESERVATION_WEB_VUS = "RESERVATION_WEB_VUS";
    static final String RESERVATION_SAP_VUS = "RESERVATION_SAP_VUS";
    static final String ENV_CONTROLLER_ZONE = "CONTROLLER_ZONE_ID";
    static final String ENV_LG_ZONES = "LG_ZONE_IDS";
    static final String ENV_TEST_RESULT_NAME = "TEST_RESULT_NAME";
    static final String ENV_SCENARIO_NAME = "SCENARIO_NAME";
    static final String ENV_API_URL = "NEOLOADWEB_API_URL";
    static final String ENV_FILES_API_URL = "NEOLOADWEB_FILES_API_URL";
    static final String ENV_PROXY = "NEOLOADWEB_PROXY";

    static final UserMessages userMessages = new UserMessages();

    public static void main(String[] args) {

        try {
            Path projectFile = getProjectPath();
            if (projectFile == null) {
                System.err.println("Cannot get the project to upload");
                throw new IllegalArgumentException("Cannot get the project to upload");
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
        return Optional.ofNullable(System.getenv(ENV_API_URL)).map(s -> s.endsWith("/")?s+"v1":s+"/v1").orElse("https://neoload-api.saas.neotys.com/v1");
    }

    @VisibleForTesting
    static String getNlwebFilesApiURL() {
        return Optional.ofNullable(System.getenv(ENV_FILES_API_URL)).map(s -> s.endsWith("/")?s+"v1":s+"/v1").orElse("https://neoload-files.saas.neotys.com/v1");
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
    static String getReservationId() { return System.getenv(RESERVATION_ID); }

    @VisibleForTesting
    static Long getReservationDuration() {
        return Optional
                .ofNullable(System.getenv(RESERVATION_DURATION))
                .map(duration -> Strings.isNullOrEmpty(duration) ? null : Long.parseLong(duration))
                .orElse(null);
    }

    @VisibleForTesting
    static Integer getReservationWebVus() {
        return Optional
                .ofNullable(System.getenv(RESERVATION_WEB_VUS))
                .map(resaWebVu -> Strings.isNullOrEmpty(resaWebVu) ? 0 : Integer.parseInt(resaWebVu))
                .orElse(0);
    }

    @VisibleForTesting
    static Integer getReservationSapVus() {
        return Optional
                .ofNullable(System.getenv(RESERVATION_SAP_VUS))
                .map(resaSapVu -> Strings.isNullOrEmpty(resaSapVu) ? 0 : Integer.parseInt(resaSapVu))
                .orElse(0);
    }

    @VisibleForTesting
    static String getLgsZoneId() {
        return System.getenv(ENV_LG_ZONES);
    }

    @VisibleForTesting
    static String getTestName() {
        return Optional.ofNullable(System.getenv(ENV_TEST_RESULT_NAME)).orElse("myTest");
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

        if (!Strings.isNullOrEmpty(System.getenv(RESERVATION_DURATION))) {
            try {
                getReservationDuration();
            } catch (NumberFormatException e) {
                System.err.println("Reservation duration is not an integer");
                throw new IllegalArgumentException("Reservation duration should be an integer. please verify the \"" + RESERVATION_DURATION + "\" ENV variable.");
            }
        }

        if (!Strings.isNullOrEmpty(System.getenv(RESERVATION_WEB_VUS))) {
            try {
                getReservationWebVus();
            } catch (NumberFormatException e) {
                System.err.println("Reservation WEB VUs number is not an integer");
                throw new IllegalArgumentException("Reservation WEB VUs number should be an integer. please verify the \"" + RESERVATION_WEB_VUS + "\" ENV variable.");
            }
        }

        if (!Strings.isNullOrEmpty(System.getenv(RESERVATION_SAP_VUS))) {
            try {
                getReservationSapVus();
            } catch (NumberFormatException e) {
                System.err.println("Reservation SAP VUs number is not an integer");
                throw new IllegalArgumentException("Reservation SAP VUs number should be an integer. please verify the \"" + RESERVATION_SAP_VUS + "\" ENV variable.");
            }
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

    private static ProjectProvider getProjectProvider() {
        ProjectProvider projectProvider;
        if(getProjectUrl().isPresent()) {
            projectProvider = URLProjectProvider.of(getProjectUrl().get());
        } else {
            projectProvider = FolderProjectProvider.of(PROJECT_FOLDER);
        }
        return projectProvider;
    }

    private static Path getProjectPath() {
        ProjectProvider projectProvider = getProjectProvider();
        if (projectProvider == null) {
            System.err.println("Cannot find a valid project provider");
            return null;
        }
        return projectProvider.getProjectPath();
    }

    @VisibleForTesting
    static String launchTest(Path projectFile, RuntimeApi runtimeApi) {
        validateEnvParameters();

        setupClientApi(runtimeApi.getApiClient());

        try {

            runtimeApi.getApiClient().setBasePath(getNlwebFilesApiURL());

            System.out.println("Uploading project");
            int readTimeout = runtimeApi.getApiClient().getReadTimeout();
            runtimeApi.getApiClient().setReadTimeout(300000);
            int writeTimeout = runtimeApi.getApiClient().getWriteTimeout();
            runtimeApi.getApiClient().setWriteTimeout(60000);
            ProjectDefinition projectDefinition = runtimeApi.postUploadProject(projectFile.toFile());
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
                    getReservationId(),
                    getReservationDuration(),
                    getReservationWebVus(),
                    getReservationSapVus(),
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
                Thread.sleep(5000);
                testDefinition = resultsApi.getTest(testId);
                if(!knownStatuses.contains(testDefinition.getStatus())) {
                    if(!knownStatuses.isEmpty() && knownStatuses.get(knownStatuses.size()-1).equals(TestDefinition.StatusEnum.RUNNING)) {
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
                final String login = userInfo.substring(0, userInfo.indexOf(':'));
                final String password = userInfo.substring(userInfo.indexOf(':'));
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
}
