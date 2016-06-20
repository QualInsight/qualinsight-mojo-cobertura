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
package com.qualinsight.mojo.cobertura.core.reporting;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Mojo that generates integration tests coverage report files.
 *
 * @author Michel Pawlak
 */
@Mojo(name = "report-it-coverage", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class ITCoverageReportMojo extends AbstractCleaningReportMojo {

    @Parameter(defaultValue = "${project.build.directory}/cobertura/it/", required = false)
    private String coverageReportPath;

    @Override
    String coverageReportPath() {
        return this.coverageReportPath;
    }

}
