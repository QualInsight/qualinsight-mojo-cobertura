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
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.dsl.Cobertura;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import com.qualinsight.mojo.cobertura.core.instrumentation.AbstractInstrumentationMojo;

/**
 * Mojo that merges Cobertura unit test and integration test coverage data, then generates an overall test coverage report.
 *
 * @author Michel Pawlak
 */
@Mojo(name = "report-overall-coverage", defaultPhase = LifecyclePhase.VERIFY)
public class OverallCoverageReportMojo extends AbstractReportMojo {

    @Parameter(defaultValue = "${project.build.directory}/cobertura/ut/" + AbstractInstrumentationMojo.DATA_FILE_NAME, required = false)
    private String utCoverageDataFileLocation;

    @Parameter(defaultValue = "${project.build.directory}/cobertura/it/" + AbstractInstrumentationMojo.DATA_FILE_NAME, required = false)
    private String itCoverageDataFileLocation;

    @Parameter(defaultValue = "${project.build.directory}/cobertura/overall/", readonly = true)
    private String overallCoverageReportPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final File sourcesDirectory = new File(sourcesPath());
        final File overallCoverageDirectory = new File(coverageReportPath());
        final File utCoverageDataFile = new File(this.utCoverageDataFileLocation);
        final File itCoverageDataFile = new File(this.itCoverageDataFileLocation);
        final File overallCoverageDataFile = new File(coverageReportPath() + AbstractInstrumentationMojo.DATA_FILE_NAME);
        if (utCoverageDataFile.exists() && utCoverageDataFile.isFile() && itCoverageDataFile.exists() && itCoverageDataFile.isFile()) {
            prepareFileSystem(overallCoverageDirectory);
            processMerging(buildMergingArguments(utCoverageDataFile, itCoverageDataFile, overallCoverageDirectory, overallCoverageDataFile));
            processReporting(buildCoberturaReportArguments(sourcesDirectory, overallCoverageDirectory, overallCoverageDataFile));
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

    private Arguments buildMergingArguments(final File utCoverageDataFile, final File itCoverageDataFile, final File overallCoverageDirectory, final File dataFile) {
        getLog().debug("Building Cobertura overall coverage data file generation arguments");
        final ArgumentsBuilder builder = new ArgumentsBuilder();
        return builder.setDestinationDirectory(overallCoverageDirectory.getAbsolutePath())
            .addFileToMerge(utCoverageDataFile.getAbsolutePath())
            .addFileToMerge(itCoverageDataFile.getAbsolutePath())
            .setDataFile(dataFile.getAbsolutePath())
            .setEncoding(encoding())
            .build();
    }

    @Override
    String coverageReportPath() {
        return this.overallCoverageReportPath;
    }
}
