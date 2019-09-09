package com.neotys.nlweb.runtime;

import java.nio.file.Path;

/**
 * return a path where we can find a NeoLoad project to upload to NeoLoad Web.
 */
public interface ProjectProvider {

    Path getProjectPath();
}
