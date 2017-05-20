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
package com.qualinsight.mojo.cobertura.core.check;

import net.sourceforge.cobertura.check.CoverageResultEntry;
import net.sourceforge.cobertura.dsl.Arguments;
import net.sourceforge.cobertura.dsl.ArgumentsBuilder;
import net.sourceforge.cobertura.dsl.Cobertura;
import net.sourceforge.cobertura.reporting.CoverageThresholdsReport;
import net.sourceforge.cobertura.reporting.ReportName;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Abstract Mojo that checks if coverage is sufficient.
 *
 * @author Michel Pawlak
 * @author pfrank13
 */
public abstract class AbstractCheckMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}/", required = false, readonly = true)
    private String projectPath;

    @Parameter(defaultValue = "0", required = false)
    private double classLineCoverageThreshold;

    @Parameter(defaultValue = "0", required = false)
    private double classBranchCoverageThreshold;

    @Parameter(defaultValue = "0", required = false)
    private double packageLineCoverageThreshold;

    @Parameter(defaultValue = "0", required = false)
    private double packageBranchCoverageThreshold;

    @Parameter(defaultValue = "0", required = false)
    private double projectLineCoverageThreshold;

    @Parameter(defaultValue = "0", required = false)
    private double projectBranchCoverageThreshold;

    @Parameter(defaultValue = "true", required = false)
    private boolean failOnError = true;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Cobertura cobertura = new Cobertura(buildCheckArguments());
        cobertura.checkThresholds();
        final CoverageThresholdsReport coverageThresholdsReport = (CoverageThresholdsReport) cobertura.report()
            .getByName(ReportName.THRESHOLDS_REPORT);
        Boolean foundError = false;
        for (final CoverageResultEntry coverageResultEntry : coverageThresholdsReport.getCoverageResultEntries()) {
            if (coverageResultEntry.isBelowExpectedCoverage()) {
                getLog().warn(buildWarningMessage(coverageResultEntry));
                foundError = true;
            }
        }
        if (foundError && this.failOnError) {
            throw new MojoFailureException(this, "Coverage is insufficient", "One or more coverage types are below expected thresholds.");
        }
    }

    private String buildWarningMessage(final CoverageResultEntry coverageResultEntry) {
        final StringBuilder sb = new StringBuilder();
        return sb.append(coverageResultEntry.getCoverageLevel())
            .append(" ")
            .append(coverageResultEntry.getCoverageType())
            .append(" Coverage is below expected threshold! Mesured ")
            .append(coverageResultEntry.getCurrentCoverage())
            .append("% but should be at least: ")
            .append(coverageResultEntry.getExpectedCoverage())
            .append("%")
            .toString();
    }

    private Arguments buildCheckArguments() {
        return new ArgumentsBuilder().setDataFile(getDataFileLocation())
            .setTotalBranchCoverageThreshold(this.projectBranchCoverageThreshold / 100)
            .setTotalLineCoverageThreshold(this.projectLineCoverageThreshold / 100)
            .setClassBranchCoverageThreshold(this.classBranchCoverageThreshold / 100)
            .setClassLineCoverageThreshold(this.classLineCoverageThreshold / 100)
            .setPackageBranchCoverageThreshold(this.packageBranchCoverageThreshold / 100)
            .setPackageLineCoverageThreshold(this.packageLineCoverageThreshold / 100)
            .build();
    }

    /**
     * Locates Cobertura data file to be used to check coverage.
     *
     * @return Path to the ".ser" Cobertura data file to be used
     */
    protected abstract String getDataFileLocation();

}