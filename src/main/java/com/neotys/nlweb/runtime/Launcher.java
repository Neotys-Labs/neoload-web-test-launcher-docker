package com.neotys.nlweb.runtime;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import io.swagger.client.ApiException;
import io.swagger.client.api.RuntimeApi;
import io.swagger.client.model.ProjectDefinition;
import io.swagger.client.model.RunTestDefinition;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

public class Launcher {

    static final String PROJECT_FOLDER = "/neoload-project";

    static final String ENV_TOKEN = "neoload-token";
    static final String ENV_CONTROLLER_ZONE = "neoload-controller-zoneid";
    static final String ENV_LG_ZONES = "neoload-lg-zonesids";
    static final String ENV_TEST_NAME = "neoload-test-name";
    static final String ENV_SCENARIO_NAME = "neoload-scenario-name";
    static final String ENV_API_URL = "neoload-api-url";
    static final String ENV_FILES_API_URL = "neoload-files-url";


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
    static void launchTest(String projectFile, RuntimeApi runtimeApi) {
        validateEnvParameters();

        runtimeApi.getApiClient().setApiKey(getToken());

        try {
            runtimeApi.getApiClient().setBasePath(getNlwebFilesApiURL());
            ProjectDefinition projectDefinition = runtimeApi.postUploadProject(new File(PROJECT_FOLDER + File.separator + projectFile));
            String scenarioName = getScenarioName(projectDefinition);
            if(scenarioName==null) {
                System.err.println("Scenario name is not defined");
                throw new IllegalArgumentException("Scenario name is not defined, and project contains several scenario. Please fill the \""+ENV_SCENARIO_NAME+"\" ENV variable");
            }

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
            System.out.println("Test launched with id:"+runTestDefinition.getTestId());
        }catch(ApiException e) {
            System.err.println(e.getResponseBody());
        }

    }

    public static void main(String[] args) {

        String projectFile = getProjectFileName(PROJECT_FOLDER);
        if(projectFile==null) {
            throw new IllegalArgumentException("Project folder does not contain a NeoLoad project or YAML file");
        }

        RuntimeApi runtimeApi = new RuntimeApi();

        launchTest(projectFile, runtimeApi);

    }


    /**
     * Return the project file name (nlp or YAML) or null if it cannot be found.
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
        long numberOfZip = Arrays.stream(listProjectFiles).filter(s -> s.toLowerCase().endsWith(".zip")).count();
        if(numberOfZip==1) {
            return Arrays.stream(listProjectFiles).filter(s -> s.toLowerCase().endsWith(".zip")).findFirst().get();
        }
        if(numberOfZip>1) {
            System.err.println("More than one NeoLoad project .zip file found");
            return null;
        }

        long numberOfYaml = Arrays.stream(listProjectFiles).filter(s -> s.toLowerCase().endsWith(".yaml") || s.toLowerCase().endsWith(".yml")).count();
        if(numberOfYaml==1) {
            return Arrays.stream(listProjectFiles).filter(s -> s.toLowerCase().endsWith(".yaml") || s.toLowerCase().endsWith(".yml")).findFirst().get();
        }
        if(numberOfYaml>1) {
            Optional<String> defaultYaml = Arrays.stream(listProjectFiles).filter(s -> s.equalsIgnoreCase("default.yaml") || s.equalsIgnoreCase("default.yml")).findFirst();
            if(defaultYaml.isPresent()) return defaultYaml.get();
        }
        System.err.println("Cannot find a NeoLoad project in the /neoload-project folder");
        return null;

    }
}
