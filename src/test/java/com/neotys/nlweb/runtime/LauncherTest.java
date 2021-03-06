package com.neotys.nlweb.runtime;

import com.google.common.collect.ImmutableList;
import io.swagger.client.ApiClient;
import io.swagger.client.api.RuntimeApi;
import io.swagger.client.model.ProjectDefinition;
import io.swagger.client.model.RunTestDefinition;
import io.swagger.client.model.ScenarioDefinition;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Launch.class})
public class LauncherTest {

    @Test
    public void getProjectFileNameWithNLPTest() {

    }

    @Before
    public void setUp() {
        PowerMockito.mockStatic(System.class);
    }

    @Test
    public void getNlwebApiURLTest() throws Exception {
        Assertions.assertThat(Launch.getNlwebApiURL()).isEqualTo("https://neoload-api.saas.neotys.com/v1");
        setEnv(Launch.ENV_API_URL, "http://myhost:9050");
        Assertions.assertThat(Launch.getNlwebApiURL()).isEqualTo("http://myhost:9050/v1");
        setEnv(Launch.ENV_API_URL, "http://myhost:9050/");
        Assertions.assertThat(Launch.getNlwebApiURL()).isEqualTo("http://myhost:9050/v1");
        setEnv(Launch.ENV_API_URL, "http://myhost:9050/myPath");
        Assertions.assertThat(Launch.getNlwebApiURL()).isEqualTo("http://myhost:9050/myPath/v1");
        setEnv(Launch.ENV_API_URL, "http://myhost:9050/myPath/");
        Assertions.assertThat(Launch.getNlwebApiURL()).isEqualTo("http://myhost:9050/myPath/v1");
    }

    @Test
    public void getNlwebFilesApiURLTest() throws Exception {
        Assertions.assertThat(Launch.getNlwebFilesApiURL()).isEqualTo("https://neoload-files.saas.neotys.com/v1");
        setEnv(Launch.ENV_FILES_API_URL, "http://myhostfiles:9050");
        Assertions.assertThat(Launch.getNlwebFilesApiURL()).isEqualTo("http://myhostfiles:9050/v1");
        setEnv(Launch.ENV_FILES_API_URL, "http://myhostfiles:9050/");
        Assertions.assertThat(Launch.getNlwebFilesApiURL()).isEqualTo("http://myhostfiles:9050/v1");
        setEnv(Launch.ENV_FILES_API_URL, "http://myhostfiles:9050/mypath");
        Assertions.assertThat(Launch.getNlwebFilesApiURL()).isEqualTo("http://myhostfiles:9050/mypath/v1");
        setEnv(Launch.ENV_FILES_API_URL, "http://myhostfiles:9050/mypath/");
        Assertions.assertThat(Launch.getNlwebFilesApiURL()).isEqualTo("http://myhostfiles:9050/mypath/v1");
    }

    @Test
    public void getTokenTest() throws Exception {
        Assertions.assertThat(Launch.getToken()).isNull();
        setEnv(Launch.ENV_TOKEN, "my-nlweb-token");
        Assertions.assertThat(Launch.getToken()).isEqualTo("my-nlweb-token");
    }

    @Test
    public void getControllerZoneIdTest() throws Exception {
        Assertions.assertThat(Launch.getControllerZoneId()).isNull();
        setEnv(Launch.ENV_CONTROLLER_ZONE, "my-controller-zone");
        Assertions.assertThat(Launch.getControllerZoneId()).isEqualTo("my-controller-zone");
    }

    @Test
    public void getLGZoneIdsTest() throws Exception {
        Assertions.assertThat(Launch.getLgsZoneId()).isNull();
        setEnv(Launch.ENV_LG_ZONES, "my-lg-zones");
        Assertions.assertThat(Launch.getLgsZoneId()).isEqualTo("my-lg-zones");
    }

    @Test
    public void getTestNameTest() throws Exception {
        Assertions.assertThat(Launch.getTestName()).isEqualTo("myTest");
        setEnv(Launch.ENV_TEST_RESULT_NAME, "my-test-name");
        Assertions.assertThat(Launch.getTestName()).isEqualTo("my-test-name");
    }

    @Test
    public void getScenarioNameTest() throws Exception {
        ProjectDefinition projectDefinition = mock(ProjectDefinition.class);
        // Test without ENV var and without Scenario in the project
        when(projectDefinition.getScenarios()).thenReturn(new ArrayList<>());
        Assertions.assertThat(Launch.getScenarioName(projectDefinition)).isNull();
        // Test without ENV var and with one single Scenario in the project
        ScenarioDefinition scenario = new ScenarioDefinition();
        scenario.setScenarioName("myTestScenario");
        when(projectDefinition.getScenarios()).thenReturn(ImmutableList.of(scenario));
        Assertions.assertThat(Launch.getScenarioName(projectDefinition)).isEqualTo("myTestScenario");
        // Test without ENV var and with several Scenarios in the project
        when(projectDefinition.getScenarios()).thenReturn(ImmutableList.of(new ScenarioDefinition(), new ScenarioDefinition()));
        Assertions.assertThat(Launch.getScenarioName(projectDefinition)).isNull();
        // Test with ENV var
        setEnv(Launch.ENV_SCENARIO_NAME, "myScenarioEnvName");
        Assertions.assertThat(Launch.getScenarioName(projectDefinition)).isEqualTo("myScenarioEnvName");

    }

    @Test
    public void getReservationIdTest() throws Exception {
        Assertions.assertThat(Launch.getReservationId()).isNull();
        setEnv(Launch.RESERVATION_ID, "my-reservation-id");
        Assertions.assertThat(Launch.getReservationId()).isEqualTo("my-reservation-id");
    }

    @Test
    public void getReservationDurationTest() throws Exception {
        Assertions.assertThat(Launch.getReservationDuration()).isNull();
        setEnv(Launch.RESERVATION_DURATION, "");
        Assertions.assertThat(Launch.getReservationDuration()).isNull();
        setEnv(Launch.RESERVATION_DURATION, "12345678910");
        Assertions.assertThat(Launch.getReservationDuration()).isEqualTo(12345678910L);
    }

    @Test
    public void getReservationWebVusTest() throws Exception {
        Assertions.assertThat(Launch.getReservationWebVus()).isEqualTo(0);
        setEnv(Launch.RESERVATION_WEB_VUS, "");
        Assertions.assertThat(Launch.getReservationWebVus()).isEqualTo(0);
        setEnv(Launch.RESERVATION_WEB_VUS, "50");
        Assertions.assertThat(Launch.getReservationWebVus()).isEqualTo(50);
    }

    @Test
    public void getReservationSapVusTest() throws Exception {
        Assertions.assertThat(Launch.getReservationSapVus()).isEqualTo(0);
        setEnv(Launch.RESERVATION_SAP_VUS, "");
        Assertions.assertThat(Launch.getReservationSapVus()).isEqualTo(0);
        setEnv(Launch.RESERVATION_SAP_VUS, "50");
        Assertions.assertThat(Launch.getReservationSapVus()).isEqualTo(50);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateEnvParameterTokenFailTest() throws Exception {
        setEnv(Launch.ENV_CONTROLLER_ZONE, "something");
        setEnv(Launch.ENV_LG_ZONES, "something");
        Launch.validateEnvParameters();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateEnvParameterControllerZoneFailTest() throws Exception {
        setEnv(Launch.ENV_TOKEN, "something");
        setEnv(Launch.ENV_LG_ZONES, "something");
        Launch.validateEnvParameters();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateEnvParameterLgZonesFailTest() throws Exception {
        setEnv(Launch.ENV_CONTROLLER_ZONE, "something");
        setEnv(Launch.ENV_TOKEN, "something");
        Launch.validateEnvParameters();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateEnvParameterReservationDurationFailTest() throws Exception {
        setEnv(Launch.ENV_TOKEN, "something");
        setEnv(Launch.RESERVATION_DURATION, "not_a_number");
        Launch.validateEnvParameters();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateEnvParameterReservationWebVuFailTest() throws Exception {
        setEnv(Launch.ENV_TOKEN, "something");
        setEnv(Launch.RESERVATION_WEB_VUS, "not_a_number");
        Launch.validateEnvParameters();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateEnvParameterReservationSapVuFailTest() throws Exception {
        setEnv(Launch.ENV_TOKEN, "something");
        setEnv(Launch.RESERVATION_SAP_VUS, "not_a_number");
        Launch.validateEnvParameters();
    }

    @Test
    public void validateEnvParameterSuccessTest() throws Exception {
        setEnv(Launch.ENV_TOKEN, "something");
        setEnv(Launch.RESERVATION_DURATION, "123");
        setEnv(Launch.RESERVATION_WEB_VUS, "456");
        setEnv(Launch.RESERVATION_SAP_VUS, "789");
        setEnv(Launch.ENV_CONTROLLER_ZONE, "something");
        setEnv(Launch.ENV_LG_ZONES, "something");
        Launch.validateEnvParameters();
    }

    @Test
    public void getProxyTest() {
        Assertions.assertThat(Launch.getProxy(null)).isNull();
        Proxy proxy1 = Launch.getProxy("http://myhost:8080");
        Assertions.assertThat(proxy1).isNotNull();
        Assertions.assertThat(proxy1.type()).isEqualTo(Proxy.Type.HTTP);
        Assertions.assertThat(((InetSocketAddress)proxy1.address()).getHostName()).isEqualTo("myhost");
        Assertions.assertThat(((InetSocketAddress)proxy1.address()).getPort()).isEqualTo(8080);
    }

    @Test
    public void launchTestTest() throws Exception {
        setEnv(Launch.ENV_TOKEN, "myToken");
        setEnv(Launch.ENV_CONTROLLER_ZONE, "something");
        setEnv(Launch.ENV_LG_ZONES, "something");
        setEnv(Launch.ENV_SCENARIO_NAME, "myScenario");

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

        Launch.launchTest(Path.of(Launch.PROJECT_FOLDER,"my-project"), runtimeApi);

        verify(apiClient).setApiKey("myToken");
        verify(apiClient).setBasePath("https://neoload-files.saas.neotys.com/v1");
        verify(apiClient, times(2)).setBasePath("https://neoload-api.saas.neotys.com/v1");

        verify(runtimeApi).postUploadProject(new File(Launch.PROJECT_FOLDER + File.separator + "my-project"));
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


    private static void setEnv(String key, String value) {
        PowerMockito.when(System.getenv(key)).thenReturn(value);
    }
}
