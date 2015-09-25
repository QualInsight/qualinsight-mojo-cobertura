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

public class CoberturaToSonarQubeCoverageReportConversionProcessingException extends Exception {

    private static final long serialVersionUID = 1L;

    public CoberturaToSonarQubeCoverageReportConversionProcessingException(final Exception e) {
        super(e);
    }

}
