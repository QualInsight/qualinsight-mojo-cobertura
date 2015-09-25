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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class CoberturaToSonarQubeCoverageReportConverter {

    private final DocumentBuilder builder;

    private final Transformer coberturaToSonarqubeTransformer;

    public CoberturaToSonarQubeCoverageReportConverter() throws TransformerConfigurationException, ParserConfigurationException, IOException {
        InputStream is = null;
        try {
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            is = getClass().getClassLoader()
                .getResourceAsStream("com/qualinsight/mojo/cobertura/transformation/cobertura2sonarqube.xsl");
            this.coberturaToSonarqubeTransformer = transformerFactory.newTransformer(new StreamSource(is));
        } finally {
            if (null != is) {
                is.close();
            }
            is = null;
        }
        final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setValidating(false);
        builderFactory.setNamespaceAware(true);
        builderFactory.setFeature("http://xml.org/sax/features/namespaces", false);
        builderFactory.setFeature("http://xml.org/sax/features/validation", false);
        builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        this.builder = builderFactory.newDocumentBuilder();
    }

    public CoberturaToSonarQubeCoverageReportConverterOutputFileBuilder withInputFile(final File inputFile) {
        return new CoberturaToSonarQubeCoverageReportConverterOutputFileBuilder(this, inputFile);
    }

    protected void process(final File input, final File output) throws SAXException, IOException, TransformerException {
        reset();
        final Document document = this.builder.parse(input);
        final Source source = new DOMSource(document);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(output);
            final Result result = new StreamResult(os);
            this.coberturaToSonarqubeTransformer.transform(source, result);
        } finally {
            if (null != os) {
                os.close();
            }
        }
    }

    private void reset() {
        this.coberturaToSonarqubeTransformer.clearParameters();
        this.coberturaToSonarqubeTransformer.reset();
        this.builder.reset();
    }
}
