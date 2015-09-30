/*
 * qualinsight-mojo-cobertura
 * Copyright (c) 2015, QualInsight
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
package com.qualinsight.mojo.cobertura.core;

import java.io.File;
import java.io.IOException;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

abstract class AbstractCleaningReportMojo extends AbstractReportMojo {

    @Parameter(defaultValue = "${project.build.directory}/classes/", required = false)
    private String classesDirectoryPath;

    @Parameter(defaultValue = "${project.build.directory}/cobertura/backup-classes/", required = false)
    private String backupClassesDirectoryPath;

    @Parameter(defaultValue = "false", required = false)
    private boolean calculateMethodComplexity;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final File baseDataFile = new File(getProjectDirectoryPath() + AbstractInstrumentationMojo.BASE_DATA_FILE_NAME);
        final File baseDirectory = new File(getBaseDirectoryPath());
        final File destinationDataFile = new File(getDestinationDirectoryPath() + AbstractInstrumentationMojo.BASE_DATA_FILE_NAME);
        final File destinationDirectory = new File(getDestinationDirectoryPath());
        final File classesDirectory = new File(this.classesDirectoryPath);
        final File backupClassesDirectory = new File(this.backupClassesDirectoryPath);
        if (classesDirectory.exists() && classesDirectory.isDirectory()) {
            prepareFileSystem(destinationDirectory);
            processReporting(buildReportingArguments(baseDirectory, destinationDirectory, baseDataFile));
            cleanupFileSystem(classesDirectory, backupClassesDirectory, baseDataFile, destinationDataFile);
            convertToSonarQubeReport();
        } else {
            getLog().info("Directory containing classes does not exist, skipping execution.");
        }
    }

    private void cleanupFileSystem(final File classesDirectory, final File backupClassesDirectory, final File baseDataFile, final File destinationDataFile) throws MojoExecutionException {
        getLog().debug("Cleaning up file system after Cobertura report generation");
        try {
            FileUtils.forceDelete(classesDirectory);
            FileUtils.moveDirectory(backupClassesDirectory, classesDirectory);
            FileUtils.moveFile(baseDataFile, destinationDataFile);
        } catch (final IOException e) {
            final String message = "An error occurred during file system cleanup: ";
            getLog().error(message, e);
            throw new MojoExecutionException(message, e);
        }
    }

    @Override
    Arguments buildReportingArguments(final File baseDirectory, final File destinationDirectory, final File baseDataFile) {
        getLog().debug("Building Cobertura report generation arguments");
        final ArgumentsBuilder builder = new ArgumentsBuilder();
        return builder.setBaseDirectory(baseDirectory.getAbsolutePath())
            .setDestinationDirectory(destinationDirectory.getAbsolutePath())
            .setDataFile(baseDataFile.getAbsolutePath())
            .setEncoding(getEncoding())
            .calculateMethodComplexity(this.calculateMethodComplexity)
            .build();
    }
}
