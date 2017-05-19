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
package com.qualinsight.mojo.cobertura.transformation;

import java.io.File;
import java.io.IOException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * Cobertura coverage report file to SonarQube generic test coverage report file conversion processor class.
 *
 * @author Michel Pawlak
 */
public final class CoberturaToSonarQubeCoverageReportConversionProcessor {

    private final CoberturaToSonarQubeCoverageReportConverter converter;

    private final File inputFile;

    private final File outputFile;

    CoberturaToSonarQubeCoverageReportConversionProcessor(final CoberturaToSonarQubeCoverageReportConverter converter, final File inputFile, final File outputFile) {
        this.converter = converter;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    /**
     * Processes the report conversion.
     *
     * @throws CoberturaToSonarQubeCoverageReportConversionProcessingException if any issue during conversion processing occurs.
     */
    public void process() throws CoberturaToSonarQubeCoverageReportConversionProcessingException {
        try {
            this.converter.process(this.inputFile, this.outputFile);
        } catch (final SAXException e) {
            throw new CoberturaToSonarQubeCoverageReportConversionProcessingException(e);
        } catch (final IOException e) {
            throw new CoberturaToSonarQubeCoverageReportConversionProcessingException(e);
        } catch (final TransformerException e) {
            throw new CoberturaToSonarQubeCoverageReportConversionProcessingException(e);
        }
    }

}
