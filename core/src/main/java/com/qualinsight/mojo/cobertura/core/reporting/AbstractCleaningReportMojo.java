/*
 * qualinsight-mojo-cobertura
 * Copyright (c) 2015-2017, QualInsight
 * http://www.qualinsight.com/
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, you can retrieve a copy
 * from <http://www.gnu.org/licenses/>.
 */
package com.qualinsight.mojo.cobertura.core.reporting;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import com.qualinsight.mojo.cobertura.core.instrumentation.AbstractInstrumentationMojo;

public abstract class AbstractCleaningReportMojo extends AbstractReportMojo {

    @Parameter(defaultValue = "${project.build.directory}/classes/", required = false)
    private String classesPath;

    @Parameter(defaultValue = "${project.build.directory}/cobertura/backup/", required = false)
    private String backupPath;

    @Parameter(defaultValue = "${project.basedir}/", required = false)
    private String dataFilePath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final File dataFile = new File(dataFilePath() + AbstractInstrumentationMojo.DATA_FILE_NAME);
        final File destinationDataFile = new File(coverageReportPath() + AbstractInstrumentationMojo.DATA_FILE_NAME);
        final File sourcesDirectory = new File(sourcesPath());
        final File destinationDirectory = new File(coverageReportPath());
        final File classesDirectory = new File(this.classesPath);
        final File backupDirectory = new File(this.backupPath);
        if (classesDirectory.exists() && classesDirectory.isDirectory()) {
            prepareFileSystem(destinationDirectory);
            processReporting(buildCoberturaReportArguments(sourcesDirectory, destinationDirectory, dataFile));
            cleanupFileSystem(classesDirectory, backupDirectory, dataFile, destinationDataFile);
            convertToSonarQubeReport();
        } else {
            getLog().info("Directory containing classes does not exist, skipping execution.");
        }
    }

    private void cleanupFileSystem(final File classesDirectory, final File backupClassesDirectory, final File dataFile, final File destinationDataFile) throws MojoExecutionException {
        getLog().debug("Cleaning up file system after Cobertura report generation");
        try {
            FileUtils.forceDelete(classesDirectory);
            FileUtils.moveDirectory(backupClassesDirectory, classesDirectory);
            FileUtils.moveFile(dataFile, destinationDataFile);
        } catch (final IOException e) {
            final String message = "An error occurred during file system cleanup: ";
            getLog().error(message, e);
            throw new MojoExecutionException(message, e);
        }
    }

    protected String dataFilePath() {
        return this.dataFilePath;
    }

    @Override
    abstract String coverageReportPath();

}
