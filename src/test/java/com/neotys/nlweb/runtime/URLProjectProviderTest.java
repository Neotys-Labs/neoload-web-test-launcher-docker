package com.neotys.nlweb.runtime;

import com.pgssoft.httpclient.HttpClientMock;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.IOException;

public class URLProjectProviderTest {

    @Test
    public void getProjectUrlExtensionTest() {
        Assertions.assertThat(URLProjectProvider.getProjectUrlExtension(null)).isEmpty();
        Assertions.assertThat(URLProjectProvider.getProjectUrlExtension("http://myserver")).isEmpty();
        Assertions.assertThat(URLProjectProvider.getProjectUrlExtension("https://myserver.mydomain")).isEmpty();
        Assertions.assertThat(URLProjectProvider.getProjectUrlExtension("https://myserver.mydomain/myproject")).isEmpty();
        Assertions.assertThat(URLProjectProvider.getProjectUrlExtension("https://myserver.mydomain/myproject.")).isEmpty();
        Assertions.assertThat(URLProjectProvider.getProjectUrlExtension("https://myserver.mydomain/myproject.zip").get()).isEqualTo("zip");
        Assertions.assertThat(URLProjectProvider.getProjectUrlExtension("https://myserver.mydomain/myproject.yaml").get()).isEqualTo("yaml");
        Assertions.assertThat(URLProjectProvider.getProjectUrlExtension("https://myserver.mydomain/myproject.yml").get()).isEqualTo("yml");
        Assertions.assertThat(URLProjectProvider.getProjectUrlExtension("https://myserver.mydomain:8090/path/myproject.yml").get()).isEqualTo("yml");

    }

    @Test
    public void getProjectPathWithErrorTest() {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://localhost/myproject.zip").doReturn("Not found").withStatus(404);
        URLProjectProvider urlProjectProvider = new URLProjectProvider(httpClientMock, "http://localhost/myproject.zip");
        Assertions.assertThat(urlProjectProvider.getProjectPath()).isNull();
        httpClientMock.verify().get("http://localhost/myproject.zip").called();

    }

    @Test
    public void getProjectPathWithExceptionTest() {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://localhost/myproject.zip").doThrowException(new IOException("IO error occurs"));
        URLProjectProvider urlProjectProvider = new URLProjectProvider(httpClientMock, "http://localhost/myproject.zip");
        Assertions.assertThat(urlProjectProvider.getProjectPath()).isNull();
        httpClientMock.verify().get("http://localhost/myproject.zip").called();

    }

    @Test
    public void getProjectPatTest() {
        HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet("http://localhost/myproject.zip").doReturn("OK").withStatus(200);
        URLProjectProvider urlProjectProvider = new URLProjectProvider(httpClientMock, "http://localhost/myproject.zip");
        Assertions.assertThat(urlProjectProvider.getProjectPath().toString()).containsPattern("project(\\d)*\\.zip");
        httpClientMock.verify().get("http://localhost/myproject.zip").called();

    }
}
