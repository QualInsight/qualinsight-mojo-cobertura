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
package com.qualinsight.mojo.cobertura.core.instrumentation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.dsl.Cobertura;
import net.sourceforge.cobertura.instrument.InstrumentMain;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractInstrumentationMojo extends AbstractMojo {

    /**
     * Default Cobertura base data file name.
     */
    public static final String DATA_FILE_NAME = "cobertura.ser";

    private static final String ERROR_MESSAGE = "An error occured during code instrumentation:";

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.basedir}/", required = false, readonly = true)
    private String projectPath;

    @Parameter(defaultValue = "${project.build.directory}/classes/", required = false)
    private String classesPath;

    @Parameter(defaultValue = "${project.build.directory}/cobertura/backup/", required = false)
    private String backupPath;

    @Parameter(defaultValue = "${project.build.directory}/classes/", required = false)
    private String instrumentationPath;

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
        final File classesDirectory = new File(this.classesPath);
        final File backupClassesDirectory = new File(this.backupPath);
        final File destinationDirectory = new File(this.instrumentationPath);
        final File baseDataFile = new File(this.projectPath + DATA_FILE_NAME);
        if (classesDirectory.exists() && classesDirectory.isDirectory()) {
            prepareDirectories(classesDirectory, backupClassesDirectory, baseDataFile);
            processInstrumentation(buildInstrumentationArguments(classesDirectory, destinationDirectory, baseDataFile));
        } else {
            getLog().info("Directory containing classes to instrument does not exist, skipping execution.");
        }
    }

    private Arguments buildInstrumentationArguments(final File classesDirectory, final File destinationDirectory, final File dataFile) {
        getLog().debug("Building Cobertura instrumentation execution arguments");
        ArgumentsBuilder builder = new ArgumentsBuilder();
        // Mandatory arguments
        builder = builder.setBaseDirectory(classesDirectory.getAbsolutePath())
            .setDestinationDirectory(destinationDirectory.getAbsolutePath())
            .setDataFile(dataFile.getAbsolutePath())
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

    private void prepareDirectories(final File classesDirectory, final File backupClassesDirectory, final File dataFile) throws MojoExecutionException {
        getLog().debug("Preparing Cobertura instrumentation directories");
        try {
            if (backupClassesDirectory.exists()) {
                FileUtils.forceDelete(backupClassesDirectory);
            }
            FileUtils.copyDirectory(classesDirectory, backupClassesDirectory);
            if (dataFile.exists() && !FileUtils.deleteQuietly(dataFile)) {
                final String message = "Could not delete baseDataFile: " + dataFile.getAbsolutePath();
                getLog().error(message);
                throw new MojoExecutionException(message);
            }
        } catch (final IOException e) {
            final String message = "An error occured during directories preparation:";
            getLog().error(message, e);
            throw new MojoExecutionException(message, e);
        }
    }

    /**
     * Cobertura needs some extra classloader handling... Since we are not using ant and have no access to --auxClasspath we need to patch the URL classloader in InstrumentMain. For more details, see:
     * <ul>
     * <li>https://github.com/cobertura/cobertura/wiki/FAQ#classnotfoundexception-during-instrumentation</li>
     * <li>https://github.com/cobertura/cobertura/issues/338</li>
     * <li>https://github.com/cobertura/cobertura/issues/231</li>
     * <li>https://github.com/cobertura/cobertura/issues/74</li>
     * <li>https://github.com/cobertura/cobertura/blob/db3bedf3334d8f35bad7ca3c6f4d777be6a09fc5/cobertura/src/main/java/net/sourceforge/cobertura/instrument/CoberturaClassWriter.java#L32</li>
     * </ul>
     */
    private URLClassLoader prepareAuxClassloader() throws MojoExecutionException {
        final List<URL> urls = new ArrayList<URL>();

        // Add the classes directory
        try {
            final URL classesDirectoryURL = new File(this.classesPath).toURI()
                .toURL();
            urls.add(classesDirectoryURL);
        } catch (final MalformedURLException e) {
            getLog().error("Failed to resolve URL for classes directory " + this.classesPath, e);
            throw new MojoExecutionException(ERROR_MESSAGE, e);
        }

        @SuppressWarnings("unchecked")
        final Set<Artifact> artifacts = this.project.getArtifacts();

        getLog().info("Adding " + artifacts.size() + " artifacts to auxClasspath");

        for (final Artifact artifact : artifacts) {
            final String artifactId = artifact.getArtifactId();
            final File artifactFile = artifact.getFile();
            if (artifactFile != null) {
                getLog().debug("Adding artifact " + artifactId + " - " + artifactFile);
                try {
                    urls.add(artifactFile.toURI()
                        .toURL());
                } catch (final MalformedURLException e) {
                    getLog().error("Failed to resolve URL for artifact " + artifactId, e);
                    throw new MojoExecutionException(ERROR_MESSAGE, e);
                }
            } else {
                getLog().warn("No file found for artifact " + artifactId);
            }
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]));
    }

    private void processInstrumentation(final Arguments arguments) throws MojoExecutionException {
        getLog().debug("Preparing Auxiliary Classloader");
        feedClassLoader(prepareAuxClassloader());
        getLog().debug("Instrumenting code with Cobertura");
        try {
            new Cobertura(arguments).instrumentCode()
                .saveProjectData();
        } catch (final Throwable e) {
            getLog().error(ERROR_MESSAGE, e);
            throw new MojoExecutionException(ERROR_MESSAGE, e);
        }
    }

    private static void feedClassLoader(final URLClassLoader urlClassLoader) {
        InstrumentMain.urlClassLoader = urlClassLoader;
    }

}
