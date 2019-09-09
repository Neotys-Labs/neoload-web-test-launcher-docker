package com.neotys.nlweb.runtime;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Responsible for downloading the file from the URL and store the project on the local disk.
 *
 */
public class URLProjectProvider implements ProjectProvider {

    private final String url;

    private final HttpClient httpClient;

    public URLProjectProvider(HttpClient httpClient, String url) {
        this.httpClient = httpClient;
        this.url = url;
    }

    public URLProjectProvider(String url) {
        this(HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build(), url);
    }

    public static URLProjectProvider of(String url) {
        return new URLProjectProvider(url);
    }

    /**
     * Download the project file and store it on the local drive.
     * @return the path where the project file was downloaded.
     */
    public Path getProjectPath() {
        System.out.println("Downloading project file from: "+this.url);
        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(this.url))
                    .GET()
                    .build();

            final String extension = getProjectUrlExtension(this.url).orElse("zip");

            Path projectFile = Files.createTempFile("project","."+extension);

            HttpResponse<Path> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofFile(projectFile));

            if(response.statusCode()==200) {
                System.out.println("Project file downloaded and stored in "+projectFile.toString());
                return projectFile;
            } else {
                System.err.println("Error occurs downloading the project");
                System.err.println("Status: "+response.statusCode());
                System.err.println("Body: "+response.body());
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
     * @param url the project URL
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
