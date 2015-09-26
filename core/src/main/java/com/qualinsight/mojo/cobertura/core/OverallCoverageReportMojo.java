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
import net.sourceforge.cobertura.dsl.Cobertura;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "report-overall-coverage", defaultPhase = LifecyclePhase.VERIFY)
public class OverallCoverageReportMojo extends AbstractReportMojo {

    @Parameter(defaultValue = "${project.build.directory}/cobertura/ut/", required = false)
    private String baseUtDirectoryPath;

    @Parameter(defaultValue = "${project.build.directory}/cobertura/it/", required = false)
    private String baseItDirectoryPath;

    @Parameter(defaultValue = "${project.build.directory}/cobertura/overall/", readonly = true)
    private String destinationDirectoryPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final File baseDataFile = new File(getProjectDirectoryPath() + AbstractInstrumentationMojo.BASE_DATA_FILE_NAME);
        final File baseDirectory = new File(getBaseDirectoryPath());
        final File baseUtDataFile = new File(this.baseUtDirectoryPath + AbstractInstrumentationMojo.BASE_DATA_FILE_NAME);
        final File baseItDataFile = new File(this.baseItDirectoryPath + AbstractInstrumentationMojo.BASE_DATA_FILE_NAME);
        final File destinationDataFile = new File(this.destinationDirectoryPath + AbstractInstrumentationMojo.BASE_DATA_FILE_NAME);
        final File destinationDirectory = new File(this.destinationDirectoryPath);
        if (baseUtDataFile.exists() && baseUtDataFile.isFile() && baseItDataFile.exists() && baseItDataFile.isFile()) {
            prepareFileSystem(destinationDirectory);
            processMerging(buildMergingArguments(baseUtDataFile, baseItDataFile, destinationDirectory, baseDataFile));
            processReporting(buildReportingArguments(baseDirectory, destinationDirectory, baseDataFile));
            cleanupFileSystem(baseDataFile, destinationDataFile);
            convertToSonarQubeReport();
        } else {
            getLog().info("UT and/or IT coverage data file does not exist, skipping execution.");
        }
    }

    private void processMerging(final Arguments arguments) {
        getLog().debug("Generating overall coverage Cobertura data file");
        final Cobertura cobertura = new Cobertura(arguments);
        cobertura.merge()
            .saveProjectData();
    }

    private Arguments buildMergingArguments(final File baseUtDataFile, final File baseItDataFile, final File destinationDirectory, final File baseDataFile) {
        getLog().debug("Building Cobertura overall coverage data file generation arguments");
        final ArgumentsBuilder builder = new ArgumentsBuilder();
        return builder.setDestinationDirectory(destinationDirectory.getAbsolutePath())
            .addFileToMerge(baseUtDataFile.getAbsolutePath())
            .addFileToMerge(baseItDataFile.getAbsolutePath())
            .setDataFile(baseDataFile.getAbsolutePath())
            .setEncoding(getEncoding())
            .build();
    }

    private void cleanupFileSystem(final File baseDataFile, final File destinationDataFile) throws MojoExecutionException {
        getLog().debug("Cleaning up directories after Cobertura report generation");
        try {
            FileUtils.moveFile(baseDataFile, destinationDataFile);
        } catch (final IOException e) {
            final String message = "An error occurred during directories cleanup: ";
            getLog().error(message, e);
            throw new MojoExecutionException(message, e);
        }
    }

    @Override
    Arguments buildReportingArguments(final File baseDirectory, final File destinationDirectory, final File baseDataFile) {
        getLog().debug("Building Cobertura overall coverage report generation arguments");
        final ArgumentsBuilder builder = new ArgumentsBuilder();
        return builder.setBaseDirectory(baseDirectory.getAbsolutePath())
            .setDestinationDirectory(destinationDirectory.getAbsolutePath())
            .setDataFile(baseDataFile.getAbsolutePath())
            .setEncoding(getEncoding())
            .build();
    }

    @Override
    String getDestinationDirectoryPath() {
        return this.destinationDirectoryPath;
    }
}
