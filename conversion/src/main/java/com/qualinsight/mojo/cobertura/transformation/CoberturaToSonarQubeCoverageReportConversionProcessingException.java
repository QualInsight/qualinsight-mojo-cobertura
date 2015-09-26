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

/**
 * Exception raised when a problem occurs while processing report conversion.
 *
 * @author Michel Pawlak
 */
public class CoberturaToSonarQubeCoverageReportConversionProcessingException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor that wraps a cause exception.
     *
     * @param cause Exception cause to be wrapped by the {@link CoberturaToSonarQubeCoverageReportConversionProcessingException}
     */
    public CoberturaToSonarQubeCoverageReportConversionProcessingException(final Exception cause) {
        super(cause);
    }

}
