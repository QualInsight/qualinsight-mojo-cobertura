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
package com.qualinsight.mojo.cobertura.transformation;

import java.io.File;

/**
 * Builder class for {@link CoberturaToSonarQubeCoverageReportConversionProcessor}
 *
 * @author Michel Pawlak
 */
public final class CoberturaToSonarQubeCoverageReportConversionProcessorBuilder {

    private final CoberturaToSonarQubeCoverageReportConverter converter;

    private final File inputFile;

    CoberturaToSonarQubeCoverageReportConversionProcessorBuilder(final CoberturaToSonarQubeCoverageReportConverter converter, final File inputFile) {
        this.converter = converter;
        this.inputFile = inputFile;
    }

    /**
     * Defines the output file to be used by the {@link CoberturaToSonarQubeCoverageReportConversionProcessor}.
     *
     * @param outputFile File to which the converted coverage report in SonarQube generic test coverage format has to be written.
     * @return a correctly configured {@link CoberturaToSonarQubeCoverageReportConversionProcessor}
     */
    public CoberturaToSonarQubeCoverageReportConversionProcessor withOuputFile(final File outputFile) {
        return new CoberturaToSonarQubeCoverageReportConversionProcessor(this.converter, this.inputFile, outputFile);
    }
}
