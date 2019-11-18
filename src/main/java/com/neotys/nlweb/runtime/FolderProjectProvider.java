package com.neotys.nlweb.runtime;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;


/**
 * Return the project file name (zip or YAML) or null if it cannot be found.
 * @param folder the folder containing the NeoLoad project
 * @return the project file name.
 */
public class FolderProjectProvider implements ProjectProvider {

    String folder;

    public FolderProjectProvider(String folder) {
        this.folder = folder;
    }

    public static FolderProjectProvider of(String folder) {
        return new FolderProjectProvider(folder);
    }

    public Path getProjectPath() {
        final File projectFile = new File(this.folder);
        if(!projectFile.isDirectory()) {
            System.err.println("Project folder must be a folder");
            return null;
        }
        String tmpdir = System.getProperty("java.io.tmpdir");
        if(tmpdir==null) tmpdir="/tmp";
        String[] listProjectFiles = projectFile.list();
        if(listProjectFiles==null || listProjectFiles.length==0) throw new IllegalArgumentException("Project folder must exist and must contains a NeoLoad project or YAML project definition.");
        // First look for a .nlp file. If found, zip and and return this zip
        long numberOfNlp = Arrays.stream(listProjectFiles).filter(s -> s.toLowerCase().endsWith(".nlp")).count();
        if(numberOfNlp==1) {
            try {
                ZipParameters parameters = new ZipParameters();
                parameters.setIncludeRootFolder(false);
                new ZipFile(tmpdir+File.separator+"project.zip").addFolder(new File(this.folder), parameters);
            }catch(ZipException e) {
                System.err.println("Error zipping the project");
                e.printStackTrace(System.err);
                return null;
            }
            return Path.of(tmpdir,"project.zip");
        }
        if(numberOfNlp>1) {
            System.err.println("More than one NeoLoad project .zip file found");
            return null;
        }
        // Then look if there is a single Zip in the folder, if yes, return this single zip as to zipped project to use
        long numberOfZip = Arrays.stream(listProjectFiles).filter(s -> s.toLowerCase().endsWith(".zip")).count();
        if(numberOfZip==1) {
            return Path.of(projectFile.getAbsolutePath(), Arrays.stream(listProjectFiles).filter(s -> s.toLowerCase().endsWith(".zip")).findFirst().get());
        }
        // Then look if there is a single yaml file in the folder, if yes, return this single yaml as the as code project to use
        if(numberOfZip>1) {
            System.err.println("More than one NeoLoad project .zip file found");
            return null;
        }

        long numberOfYaml = Arrays.stream(listProjectFiles).filter(s -> s.toLowerCase().endsWith(".yaml") || s.toLowerCase().endsWith(".yml")).count();
        if(numberOfYaml==1) {
            return Path.of(Arrays.stream(listProjectFiles).filter(s -> s.toLowerCase().endsWith(".yaml") || s.toLowerCase().endsWith(".yml")).findFirst().get());
        }
        // Then look if there are several yaml file in the folder, look if we can find a default.yaml
        // TODO we should zip all the YAML and return the zip since the default.yaml file may contain includes
        if(numberOfYaml>1) {
            Optional<String> defaultYaml = Arrays.stream(listProjectFiles).filter(s -> s.equalsIgnoreCase("default.yaml") || s.equalsIgnoreCase("default.yml")).findFirst();
            if(defaultYaml.isPresent()) return Path.of(defaultYaml.get());
        }
        System.err.println("Cannot find a NeoLoad project in the /neoload-project folder");
        return null;
    }
}
