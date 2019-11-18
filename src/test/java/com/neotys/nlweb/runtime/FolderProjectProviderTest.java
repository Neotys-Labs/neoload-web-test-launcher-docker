package com.neotys.nlweb.runtime;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Files;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FolderProjectProviderTest {

    @Test
    public void getProjectFileNameWithFileTest() throws IOException {
        File yamlFile = File.createTempFile("test",".yaml");
        yamlFile.deleteOnExit();
        Assertions.assertThat(FolderProjectProvider.of(yamlFile.getAbsolutePath()).getProjectPath()).isNull();
        yamlFile.delete();
    }

    @Test
    public void getProjectFileNameWithNlpTest() throws IOException {
        Path tempDir = java.nio.file.Files.createTempDirectory("nlwebtestlaunchertests");
        String sysTmpDir = System.getProperty("java.io.tmpdir");
        if(!sysTmpDir.endsWith(File.separator)) sysTmpDir+=File.separator;
        tempDir.toFile().deleteOnExit();
        File nlpFile = tempDir.resolve("test.nlp").toFile();
        nlpFile.createNewFile();
        nlpFile.deleteOnExit();
        Assertions.assertThat(FolderProjectProvider.of(nlpFile.getParentFile().getAbsolutePath()).getProjectPath().toString()).isEqualTo(sysTmpDir+"project.zip");
        Assertions.assertThat(new File(sysTmpDir+"project.zip")).exists();
        File nlpFile2 = tempDir.resolve("test2.nlp").toFile();
        nlpFile2.createNewFile();
        nlpFile2.deleteOnExit();
        Assertions.assertThat(FolderProjectProvider.of(nlpFile.getParentFile().getAbsolutePath()).getProjectPath()).isNull();
        nlpFile.delete();
        nlpFile2.delete();
        tempDir.toFile().delete();
    }


    @Test
    public void getProjectFileNameWithZipTest() throws IOException {
        Path tempDir = java.nio.file.Files.createTempDirectory("nlwebtestlaunchertests");
        String sysTmpDir = System.getProperty("java.io.tmpdir");
        if(!sysTmpDir.endsWith(File.separator)) sysTmpDir+=File.separator;
        tempDir.toFile().deleteOnExit();
        File zipFile = tempDir.resolve("test.zip").toFile();
        zipFile.createNewFile();
        zipFile.deleteOnExit();
        Assertions.assertThat(FolderProjectProvider.of(zipFile.getParentFile().getAbsolutePath()).getProjectPath().toString()).isEqualTo(tempDir+File.separator+"test.zip");
        zipFile.delete();
        tempDir.toFile().delete();
    }

    @Test
    public void getProjectFileNameWithYAMLTest() throws IOException {
        File tempFolder = Files.newTemporaryFolder();
        File yamlFile = new File(tempFolder.getAbsolutePath()+File.separator+"test.yaml");
        yamlFile.createNewFile();
        yamlFile.deleteOnExit();
        Assertions.assertThat(FolderProjectProvider.of(yamlFile.getParentFile().getAbsolutePath()).getProjectPath().toString()).matches("test.yaml");
        File yamlFile2 = new File(tempFolder.getAbsolutePath()+File.separator+"test2.yml");
        yamlFile2.createNewFile();
        yamlFile2.deleteOnExit();
        Assertions.assertThat(FolderProjectProvider.of(yamlFile.getParentFile().getAbsolutePath()).getProjectPath()).isNull();
        tempFolder.delete();
        tempFolder = Files.newTemporaryFolder();
        File defaultYamlFile = new File(tempFolder.getAbsolutePath()+File.separator+"/default.yaml");
        defaultYamlFile.createNewFile();
        defaultYamlFile.deleteOnExit();
        Assertions.assertThat(FolderProjectProvider.of(defaultYamlFile.getParentFile().getAbsolutePath()).getProjectPath().toString()).isEqualTo("default.yaml");

        yamlFile.delete();
        yamlFile2.delete();
        defaultYamlFile.delete();
        tempFolder.delete();

    }

}
