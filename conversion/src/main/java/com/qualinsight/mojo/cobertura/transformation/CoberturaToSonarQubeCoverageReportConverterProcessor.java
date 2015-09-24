/*
 * This file is part of qualinsight-mojo-cobertura-conversion.
 *
 * qualinsight-mojo-cobertura-conversion is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * qualinsight-mojo-cobertura-conversion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with qualinsight-mojo-cobertura-conversion.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.qualinsight.mojo.cobertura.transformation;

import java.io.File;
import java.io.IOException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

public class CoberturaToSonarQubeCoverageReportConverterProcessor {

    private final CoberturaToSonarQubeCoverageReportConverter converter;

    private final File inputFile;

    private final File outputFile;

    public CoberturaToSonarQubeCoverageReportConverterProcessor(final CoberturaToSonarQubeCoverageReportConverter converter, final File inputFile, final File outputFile) {
        this.converter = converter;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    public void process() throws CoberturaToSonarQubeCoverageReportConversionProcessingException {
        try {
            this.converter.process(this.inputFile, this.outputFile);
        } catch (SAXException | IOException | TransformerException e) {
            throw new CoberturaToSonarQubeCoverageReportConversionProcessingException(e);
        }
    }

}
