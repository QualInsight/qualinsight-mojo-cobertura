/*
 * This file is part of qualinsight-mojo-cobertura-core.
 *
 * qualinsight-mojo-cobertura-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * qualinsight-mojo-cobertura-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with qualinsight-mojo-cobertura-core.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.qualinsight.mojo.cobertura.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.dsl.Cobertura;
import net.sourceforge.cobertura.dsl.ReportFormat;
import net.sourceforge.cobertura.reporting.Report;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.xml.sax.SAXException;
import com.qualinsight.mojo.cobertura.transformation.CoberturaToSonarQubeCoverageReportConverter;

abstract class AbstractReportMojo extends AbstractMojo {

    public static final String BASE_COVERAGE_FILE_NAME = "coverage.xml";

    public static final String CONVERTED_COVERAGE_FILE_NAME = "converted-coverage.xml";

    @Parameter(defaultValue = "${project.basedir}/", required = false, readonly = true)
    private String projectDirectoryPath;

    @Parameter(defaultValue = "${project.basedir}/src/main/", required = false)
    private String baseDirectoryPath;

    @Parameter(defaultValue = "${project.build.directory}/classes/", required = false)
    private String classesDirectoryPath;

    @Parameter(defaultValue = "${project.build.directory}/cobertura/backup-classes/", required = false)
    private String backupClassesDirectoryPath;

    @Parameter(defaultValue = "UTF-8", required = false)
    private String encoding;

    @Parameter(defaultValue = "false", required = false)
    private Boolean calculateMethodComplexity;

    @Parameter(defaultValue = "xml", required = false)
    private String format;

    @Parameter(defaultValue = "true", required = false)
    private Boolean convertToSonarQubeOutput;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final File baseDataFile = new File(this.projectDirectoryPath + AbstractInstrumentationMojo.BASE_DATA_FILE_NAME);
        final File baseDirectory = new File(this.baseDirectoryPath);
        final File destinationDataFile = new File(getDestinationDirectoryPath() + AbstractInstrumentationMojo.BASE_DATA_FILE_NAME);
        final File destinationDirectory = new File(getDestinationDirectoryPath());
        final File classesDirectory = new File(this.classesDirectoryPath);
        final File backupClassesDirectory = new File(this.backupClassesDirectoryPath);
        if (classesDirectory.exists() && classesDirectory.isDirectory()) {
            prepareDirectories(destinationDirectory);
            processReporting(buildReportingArguments(baseDirectory, destinationDirectory, baseDataFile));
            cleanupDirectories(classesDirectory, backupClassesDirectory, baseDataFile, destinationDataFile);
            if (this.convertToSonarQubeOutput) {
                if ("xml".equalsIgnoreCase(this.format)) {
                    convertReport();
                } else {
                    getLog().warn("Conversion to SonarQube generic test coverage format skipped: report format should be 'xml' but was '" + this.format + "'.");
                }
            }
        } else {
            getLog().info("Directory containing instrumented classes does not exist, skipping execution.");
        }
    }

    private void convertReport() throws MojoExecutionException {
        getLog().debug("Converting Cobertura report to SonarQube generic test coverage report format");
        final File conversionInputFile = new File(getDestinationDirectoryPath() + BASE_COVERAGE_FILE_NAME);
        final File conversionOutputFile = new File(getDestinationDirectoryPath() + CONVERTED_COVERAGE_FILE_NAME);
        try {
            new CoberturaToSonarQubeCoverageReportConverter().withInputFile(conversionInputFile)
                .withOuputFile(conversionOutputFile)
                .process();
        } catch (SAXException | TransformerException | ParserConfigurationException | IOException e) {
            final String message = "An error occurred during coverage output conversion: ";
            getLog().error(message, e);
            throw new MojoExecutionException(message, e);
        }
    }

    private void cleanupDirectories(final File classesDirectory, final File backupClassesDirectory, final File baseDataFile, final File destinationDataFile) throws MojoExecutionException {
        getLog().debug("Cleaning up directories after Cobertura report generation");
        try {
            if (classesDirectory.exists()) {
                FileUtils.forceDelete(classesDirectory);
            }
            FileUtils.moveDirectory(backupClassesDirectory, classesDirectory);
            FileUtils.moveFile(baseDataFile, destinationDataFile);
        } catch (final IOException e) {
            final String message = "An error occurred during directories cleanup: ";
            getLog().error(message, e);
            throw new MojoExecutionException(message, e);
        }
    }

    private void processReporting(final Arguments arguments) {
        getLog().debug("Generating Cobertura report");
        final Cobertura cobertura = new Cobertura(arguments);
        final Report report = cobertura.report();
        report.export(ReportFormat.getFromString(this.format));
    }

    private Arguments buildReportingArguments(final File baseDirectory, final File destinationDirectory, final File baseDataFile) {
        getLog().debug("Building Cobertura report generation arguments");
        final ArgumentsBuilder builder = new ArgumentsBuilder();
        return builder.setBaseDirectory(baseDirectory.getAbsolutePath())
            .setDestinationDirectory(destinationDirectory.getAbsolutePath())
            .setDataFile(baseDataFile.getAbsolutePath())
            .setEncoding(this.encoding)
            .calculateMethodComplexity(this.calculateMethodComplexity)
            .build();
    }

    private void prepareDirectories(final File destinationDirectory) throws MojoExecutionException {
        getLog().debug("Preparing Cobertura report generation directories");
        try {
            Files.createDirectories(destinationDirectory.toPath());
        } catch (final IOException e) {
            final String message = "An error occured during directories preparation: ";
            getLog().error(message, e);
            throw new MojoExecutionException(message, e);
        }
    }

    abstract String getDestinationDirectoryPath();
}
