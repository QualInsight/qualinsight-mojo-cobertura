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
import net.sourceforge.cobertura.dsl.Cobertura;
import net.sourceforge.cobertura.reporting.CoverageThresholdsReport;
import net.sourceforge.cobertura.reporting.ReportName;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractCheckMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}/", required = false, readonly = true)
    private String projectPath;

    @Parameter(defaultValue = "0", required = false)
    private double threshold;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Cobertura cobertura = new Cobertura(buildCheckArguments());
        cobertura.checkThresholds();
        final CoverageThresholdsReport coverageThresholdsReport = (CoverageThresholdsReport) cobertura.report()
            .getByName(ReportName.THRESHOLDS_REPORT);
        for (final CoverageResultEntry coverageResultEntry : coverageThresholdsReport.getCoverageResultEntries()) {
            if (coverageResultEntry.isBelowExpectedCoverage()) {
                throw new MojoFailureException(this, coverageResultEntry.getCoverageType() + " coverage is insufficient", coverageResultEntry.getCoverageType() + " for "
                    + coverageResultEntry.getName() + " is " + coverageResultEntry.getCurrentCoverage() + " which is less than " + coverageResultEntry.getExpectedCoverage());
            }
        }
    }

    public double getThreshold() {
        return this.threshold;
    }

    protected abstract Arguments buildCheckArguments();

    protected abstract String getDataFileLocation();

}