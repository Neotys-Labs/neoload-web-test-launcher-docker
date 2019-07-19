package com.neotys.nlweb.runtime;

import com.google.common.collect.ImmutableList;
import io.swagger.client.ApiClient;
import io.swagger.client.api.RuntimeApi;
import io.swagger.client.model.ProjectDefinition;
import io.swagger.client.model.RunTestDefinition;
import io.swagger.client.model.ScenarioDefinition;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Files;
import org.junit.Test;
import static  org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class LauncherTest {

    @Test
    public void getProjectFileNameWithNLPTest() {

    }

    @Test
    public void getProjectFileNameWithZipTest() throws IOException {
        Path tempDir = java.nio.file.Files.createTempDirectory("nlwebtestlaunchertests");
        tempDir.toFile().deleteOnExit();
        File nlpFile = tempDir.resolve("test.nlp").toFile();
        nlpFile.createNewFile();
        nlpFile.deleteOnExit();
        Assertions.assertThat(Launcher.getProjectFileName(nlpFile.getParentFile().getAbsolutePath())).isEqualTo(tempDir.toFile().getAbsolutePath()+File.separator+"project.zip");
        Assertions.assertThat(tempDir.resolve("project.zip").toFile()).exists();
        File nlpFile2 = tempDir.resolve("test2.nlp").toFile();
        nlpFile2.createNewFile();
        nlpFile2.deleteOnExit();
        Assertions.assertThat(Launcher.getProjectFileName(nlpFile.getParentFile().getAbsolutePath())).isNull();
        nlpFile.delete();
        nlpFile2.delete();
        tempDir.toFile().delete();
    }

    @Test
    public void getProjectFileNameWithYAMLTest() throws IOException {
        File yamlFile = File.createTempFile("test",".yaml");
        yamlFile.deleteOnExit();
        Assertions.assertThat(Launcher.getProjectFileName(yamlFile.getParentFile().getAbsolutePath())).matches("test\\d+\\.yaml");
        File yamlFile2 = File.createTempFile("test2",".yml");
        yamlFile2.deleteOnExit();
        Assertions.assertThat(Launcher.getProjectFileName(yamlFile.getParentFile().getAbsolutePath())).isNull();
        File defaultYamlFile = new File(Files.temporaryFolderPath()+"/default.yaml");
        defaultYamlFile.createNewFile();
        defaultYamlFile.deleteOnExit();
        Assertions.assertThat(Launcher.getProjectFileName(yamlFile.getParentFile().getAbsolutePath())).isEqualTo("default.yaml");

        yamlFile.delete();
        yamlFile2.delete();
        defaultYamlFile.delete();

    }

    @Test
    public void getNlwebApiURLTest() throws Exception {
        Assertions.assertThat(Launcher.getNlwebApiURL()).isEqualTo("https://neoload-api.saas.neotys.com/v1");
        setEnv(Launcher.ENV_API_URL, "http://myhost:9050/v1");
        Assertions.assertThat(Launcher.getNlwebApiURL()).isEqualTo("http://myhost:9050/v1");
    }

    @Test
    public void getNlwebFilesApiURLTest() throws Exception {
        Assertions.assertThat(Launcher.getNlwebFilesApiURL()).isEqualTo("https://neoload-files.saas.neotys.com/v1");
        setEnv(Launcher.ENV_FILES_API_URL, "http://myhostfiles:9050/v1");
        Assertions.assertThat(Launcher.getNlwebFilesApiURL()).isEqualTo("http://myhostfiles:9050/v1");
    }

    @Test
    public void getTokenTest() throws Exception {
        clearEnv();
        Assertions.assertThat(Launcher.getToken()).isNull();
        setEnv(Launcher.ENV_TOKEN, "my-nlweb-token");
        Assertions.assertThat(Launcher.getToken()).isEqualTo("my-nlweb-token");
    }

    @Test
    public void getControllerZoneIdTest() throws Exception {
        Assertions.assertThat(Launcher.getControllerZoneId()).isNull();
        setEnv(Launcher.ENV_CONTROLLER_ZONE, "my-controller-zone");
        Assertions.assertThat(Launcher.getControllerZoneId()).isEqualTo("my-controller-zone");
    }

    @Test
    public void getLGZoneIdsTest() throws Exception {
        clearEnv();
        Assertions.assertThat(Launcher.getLgsZoneId()).isNull();
        setEnv(Launcher.ENV_LG_ZONES, "my-lg-zones");
        Assertions.assertThat(Launcher.getLgsZoneId()).isEqualTo("my-lg-zones");
    }

    @Test
    public void getTestNameTest() throws Exception {
        Assertions.assertThat(Launcher.getTestName()).isEqualTo("myTest");
        setEnv(Launcher.ENV_TEST_NAME, "my-test-name");
        Assertions.assertThat(Launcher.getTestName()).isEqualTo("my-test-name");
    }

    @Test
    public void getScenarioNameTest() throws Exception {
        ProjectDefinition projectDefinition = mock(ProjectDefinition.class);
        // Test without ENV var and without Scenario in the project
        when(projectDefinition.getScenarios()).thenReturn(new ArrayList<>());
        Assertions.assertThat(Launcher.getScenarioName(projectDefinition)).isNull();
        // Test without ENV var and with one single Scenario in the project
        ScenarioDefinition scenario = new ScenarioDefinition();
        scenario.setScenarioName("myTestScenario");
        when(projectDefinition.getScenarios()).thenReturn(ImmutableList.of(scenario));
        Assertions.assertThat(Launcher.getScenarioName(projectDefinition)).isEqualTo("myTestScenario");
        // Test without ENV var and with several Scenarios in the project
        when(projectDefinition.getScenarios()).thenReturn(ImmutableList.of(new ScenarioDefinition(), new ScenarioDefinition()));
        Assertions.assertThat(Launcher.getScenarioName(projectDefinition)).isNull();
        // Test with ENV var
        setEnv(Launcher.ENV_SCENARIO_NAME, "myScenarioEnvName");
        Assertions.assertThat(Launcher.getScenarioName(projectDefinition)).isEqualTo("myScenarioEnvName");

    }

    @Test(expected = IllegalArgumentException.class)
    public void validateEnvParameterTokenFailTest() throws Exception {
        clearEnv();
        setEnv(Launcher.ENV_CONTROLLER_ZONE, "something");
        setEnv(Launcher.ENV_LG_ZONES, "something");
        Launcher.validateEnvParameters();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateEnvParameterControllerZoneFailTest() throws Exception {
        clearEnv();
        setEnv(Launcher.ENV_TOKEN, "something");
        setEnv(Launcher.ENV_LG_ZONES, "something");
        Launcher.validateEnvParameters();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateEnvParameterLgZonesFailTest() throws Exception {
        clearEnv();
        setEnv(Launcher.ENV_CONTROLLER_ZONE, "something");
        setEnv(Launcher.ENV_TOKEN, "something");
        Launcher.validateEnvParameters();
    }

    @Test
    public void validateEnvParameterSuccessTest() throws Exception {
        clearEnv();
        setEnv(Launcher.ENV_TOKEN, "something");
        setEnv(Launcher.ENV_CONTROLLER_ZONE, "something");
        setEnv(Launcher.ENV_LG_ZONES, "something");
        Launcher.validateEnvParameters();
    }

    @Test
    public void getProxyTest() {
        Assertions.assertThat(Launcher.getProxy(null)).isNull();
        Proxy proxy1 = Launcher.getProxy("http://myhost:8080");
        Assertions.assertThat(proxy1).isNotNull();
        Assertions.assertThat(proxy1.type()).isEqualTo(Proxy.Type.HTTP);
        Assertions.assertThat(((InetSocketAddress)proxy1.address()).getHostName()).isEqualTo("myhost");
        Assertions.assertThat(((InetSocketAddress)proxy1.address()).getPort()).isEqualTo(8080);
    }

    @Test
    public void launchTestTest() throws Exception {
        clearEnv();
        setEnv(Launcher.ENV_TOKEN, "myToken");
        setEnv(Launcher.ENV_CONTROLLER_ZONE, "something");
        setEnv(Launcher.ENV_LG_ZONES, "something");
        setEnv(Launcher.ENV_SCENARIO_NAME, "myScenario");

        RunTestDefinition runtTestDefinition = new RunTestDefinition();
        runtTestDefinition.setTestId("mytestid");

        RuntimeApi runtimeApi = mock(RuntimeApi.class);
        ApiClient apiClient = mock(ApiClient.class);
        when(runtimeApi.getApiClient()).thenReturn(apiClient);
        when(runtimeApi.getTestsRun("myTest",
                "myProjectId",
                "myScenario",
                "",
                null,
                null,
                null,
                0,
                0,
                "something",
                "something")).thenReturn(runtTestDefinition);

        ProjectDefinition projectDefinition = new ProjectDefinition();
        projectDefinition.setProjectId("myProjectId");
        when(runtimeApi.postUploadProject(any())).thenReturn(projectDefinition);

        Launcher.launchTest("my-project", runtimeApi);

        verify(apiClient).setApiKey("myToken");
        verify(apiClient).setBasePath("https://neoload-files.saas.neotys.com/v1");
        verify(apiClient).setBasePath("https://neoload-api.saas.neotys.com/v1");

        verify(runtimeApi).postUploadProject(new File(Launcher.PROJECT_FOLDER + File.separator + "my-project"));
        verify(runtimeApi).getTestsRun("myTest",
                "myProjectId",
                "myScenario",
                "",
                null,
                null,
                null,
                0,
                0,
                "something",
                "something");

    }

    @Test
    public void getProjectUrlExtensionTest() {
        Assertions.assertThat(Launcher.getProjectUrlExtension(null)).isEmpty();
        Assertions.assertThat(Launcher.getProjectUrlExtension("http://myserver")).isEmpty();
        Assertions.assertThat(Launcher.getProjectUrlExtension("https://myserver.mydomain")).isEmpty();
        Assertions.assertThat(Launcher.getProjectUrlExtension("https://myserver.mydomain/myproject")).isEmpty();
        Assertions.assertThat(Launcher.getProjectUrlExtension("https://myserver.mydomain/myproject.")).isEmpty();
        Assertions.assertThat(Launcher.getProjectUrlExtension("https://myserver.mydomain/myproject.zip").get()).isEqualTo("zip");
        Assertions.assertThat(Launcher.getProjectUrlExtension("https://myserver.mydomain/myproject.yaml").get()).isEqualTo("yaml");
        Assertions.assertThat(Launcher.getProjectUrlExtension("https://myserver.mydomain/myproject.yml").get()).isEqualTo("yml");
        Assertions.assertThat(Launcher.getProjectUrlExtension("https://myserver.mydomain:8090/path/myproject.yml").get()).isEqualTo("yml");

    }

    private static void setEnv(String key, String value) throws Exception {
        Class[] classes = Collections.class.getDeclaredClasses();
        Map<String, String> env = System.getenv();
        for(Class cl : classes) {
            if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                Field field = cl.getDeclaredField("m");
                field.setAccessible(true);
                Object obj = field.get(env);
                Map<String, String> map = (Map<String, String>) obj;
                map.put(key, value);
            }
        }
    }

    private static void clearEnv() throws Exception {
        Class[] classes = Collections.class.getDeclaredClasses();
        Map<String, String> env = System.getenv();
        for(Class cl : classes) {
            if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                Field field = cl.getDeclaredField("m");
                field.setAccessible(true);
                Object obj = field.get(env);
                Map<String, String> map = (Map<String, String>) obj;
                map.clear();
            }
        }
    }
}
