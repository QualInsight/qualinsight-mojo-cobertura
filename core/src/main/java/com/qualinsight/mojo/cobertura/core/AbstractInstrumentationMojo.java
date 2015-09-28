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
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

abstract class AbstractInstrumentationMojo extends AbstractMojo {

    /**
     * Default Cobertura base data file name.
     */
    public static final String BASE_DATA_FILE_NAME = "cobertura.ser";

    @Parameter(defaultValue = "${project.basedir}/", required = false, readonly = true)
    private String projectDirectoryPath;

    @Parameter(defaultValue = "${project.build.directory}/classes/", required = false)
    private String classesDirectoryPath;

    @Parameter(defaultValue = "${project.build.directory}/cobertura/backup-classes/", required = false)
    private String backupClassesDirectoryPath;

    @Parameter(defaultValue = "${project.build.directory}/classes/", required = false)
    private String destinationDirectoryPath;

    @Parameter(defaultValue = "true", required = false)
    private boolean ignoreTrivial;

    @Parameter(defaultValue = "false", required = false)
    private boolean failOnError;

    @Parameter(defaultValue = "false", required = false)
    private boolean threadsafeRigorous;

    @Parameter(defaultValue = "UTF-8", required = false)
    private String encoding;

    @Parameter(required = false)
    private String ignoreMethodAnnotation;

    @Parameter(required = false)
    private String ignoreClassAnnotation;

    @Parameter(required = false)
    private String ignoreRegularExpression;

    @Parameter(required = false)
    private String includeClassesRegularExpression;

    @Parameter(required = false)
    private String excludeClassesRegularExpression;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final File classesDirectory = new File(this.classesDirectoryPath);
        final File backupClassesDirectory = new File(this.backupClassesDirectoryPath);
        final File destinationDirectory = new File(this.destinationDirectoryPath);
        final File baseDataFile = new File(this.projectDirectoryPath + BASE_DATA_FILE_NAME);
        if (classesDirectory.exists() && classesDirectory.isDirectory()) {
            prepareDirectories(classesDirectory, backupClassesDirectory, baseDataFile);
            processInstrumentation(buildInstrumentationArguments(classesDirectory, destinationDirectory, baseDataFile));
        } else {
            getLog().info("Directory containing classes to instrument does not exist, skipping execution.");
        }
    }

    private Arguments buildInstrumentationArguments(final File classesDirectory, final File destinationDirectory, final File baseDataFile) {
        getLog().debug("Building Cobertura instrumentation execution arguments");
        ArgumentsBuilder builder = new ArgumentsBuilder();
        // Mandatory arguments
        builder = builder.setBaseDirectory(classesDirectory.getAbsolutePath())
            .setDestinationDirectory(destinationDirectory.getAbsolutePath())
            .setDataFile(baseDataFile.getAbsolutePath())
            .setEncoding(this.encoding)
            .ignoreTrivial(this.ignoreTrivial)
            .failOnError(this.failOnError)
            .threadsafeRigorous(this.threadsafeRigorous)
            .addFileToInstrument(classesDirectory.getAbsolutePath());
        // Optional arguments
        if (!StringUtils.isBlank(this.ignoreRegularExpression)) {
            builder = builder.addIgnoreRegex(this.ignoreRegularExpression);
        }
        if (!StringUtils.isBlank(this.ignoreClassAnnotation)) {
            builder = builder.addIgnoreClassAnnotation(this.ignoreClassAnnotation);
        }
        if (!StringUtils.isBlank(this.ignoreMethodAnnotation)) {
            builder = builder.addIgnoreMethodAnnotation(this.ignoreMethodAnnotation);
        }
        if (!StringUtils.isBlank(this.includeClassesRegularExpression)) {
            builder = builder.addIncludeClassesRegex(this.includeClassesRegularExpression);
        }
        if (!StringUtils.isBlank(this.excludeClassesRegularExpression)) {
            builder = builder.addExcludeClassesRegex(this.excludeClassesRegularExpression);
        }
        return builder.build();
    }

    private void prepareDirectories(final File classesDirectory, final File backupClassesDirectory, final File baseDataFile) throws MojoExecutionException {
        getLog().debug("Preparing Cobertura instrumentation directories");
        try {
            if (backupClassesDirectory.exists()) {
                FileUtils.forceDelete(backupClassesDirectory);
            }
            FileUtils.copyDirectory(classesDirectory, backupClassesDirectory);
            if (baseDataFile.exists() && !FileUtils.deleteQuietly(baseDataFile)) {
                final String message = "Could not delete baseDataFile: " + baseDataFile.getAbsolutePath();
                getLog().error(message);
                throw new MojoExecutionException(message);
            }
        } catch (final IOException e) {
            final String message = "An error occured during directories preparation:";
            getLog().error(message, e);
            throw new MojoExecutionException(message, e);
        }
    }

    private void processInstrumentation(final Arguments arguments) throws MojoExecutionException {
        getLog().debug("Instrumenting code with Cobertura");
        try {
            new Cobertura(arguments).instrumentCode()
                .saveProjectData();
        } catch (final Throwable e) {
            final String message = "An error occured during code instrumentation:";
            getLog().error(message, e);
            throw new MojoExecutionException(message, e);
        }
    }

}
