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

    public static final String BASE_COVERAGE_FILE = "coverage.xml";

    public static final String CONVERTED_COVERAGE_FILE = "converted-coverage.xml";

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
        final File baseDataFile = new File(AbstractInstrumentationMojo.BASE_DATA_FILE);
        final File baseDirectory = new File(this.baseDirectoryPath);
        final File destinationDataFile = new File(getDestinationDirectoryPath() + AbstractInstrumentationMojo.BASE_DATA_FILE);
        final File destinationDirectory = new File(getDestinationDirectoryPath());
        final File classesDirectoryPath = new File(this.classesDirectoryPath);
        final File backupClassesDirectory = new File(this.backupClassesDirectoryPath);
        try {
            Files.createDirectories(destinationDirectory.toPath());
            final ArgumentsBuilder builder = new ArgumentsBuilder();
            final Arguments arguments = builder.setBaseDirectory(baseDirectory.getAbsolutePath())
                .setDestinationDirectory(destinationDirectory.getAbsolutePath())
                .setDataFile(baseDataFile.getAbsolutePath())
                .setEncoding(this.encoding)
                .calculateMethodComplexity(this.calculateMethodComplexity)
                .build();
            final Cobertura cobertura = new Cobertura(arguments);
            final Report report = cobertura.report();
            report.export(ReportFormat.getFromString(this.format));
        } catch (final IOException e) {
            final String message = "An error occured during directories preparation: ";
            getLog().error(message, e);
            throw new MojoExecutionException(message, e);
        }
        try {
            if (classesDirectoryPath.exists()) {
                FileUtils.forceDelete(classesDirectoryPath);
            }
            FileUtils.moveDirectory(backupClassesDirectory, classesDirectoryPath);
            FileUtils.moveFile(baseDataFile, destinationDataFile);
            if (this.convertToSonarQubeOutput) {
                final File conversionInputFile = new File(getDestinationDirectoryPath() + BASE_COVERAGE_FILE);
                final File conversionOutputFile = new File(getDestinationDirectoryPath() + CONVERTED_COVERAGE_FILE);
                try {
                    new CoberturaToSonarQubeCoverageReportConverter().withInputFile(conversionInputFile)
                        .withOuputFile(conversionOutputFile)
                        .process();
                } catch (SAXException | TransformerException | ParserConfigurationException e) {
                    final String message = "An error occurred during coverage output conversion: ";
                    getLog().error(message, e);
                    throw new MojoExecutionException(message, e);
                }
            }
        } catch (final IOException e) {
            final String message = "An error occurred during directories cleanup: ";
            getLog().error(message, e);
            throw new MojoExecutionException(message, e);
        }
    }

    abstract String getDestinationDirectoryPath();
}
