package org.simpleframework.xml.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Formatter {

    private static final String LS_FEATURE_KEY = "LS";

    private static final String LS_FEATURE_VERSION = "3.0";

    private static final String CORE_FEATURE_KEY = "Core";

    private static final String CORE_FEATURE_VERSION = "2.0";

    private final boolean preserveComments;

    public Formatter() {
        this(false);
    }

    public Formatter(boolean preserveComments) {
        this.preserveComments = preserveComments;
    }

    public String format(String source) {
        StringWriter writer = new StringWriter();
        format(source, writer);
        return writer.toString();
    }

    public void format(String source, Writer writer) {

        String FEATURE = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);

            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are
            // prevented
            // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
            FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
            factory.setFeature(FEATURE, true);

            // If you can't completely disable DTDs, then at least do the following:
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
            // JDK7+ - http://xml.org/sax/features/external-general-entities
            FEATURE = "http://xml.org/sax/features/external-general-entities";
            factory.setFeature(FEATURE, false);

            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
            // JDK7+ - http://xml.org/sax/features/external-parameter-entities
            FEATURE = "http://xml.org/sax/features/external-parameter-entities";
            factory.setFeature(FEATURE, false);

            // Disable external DTDs as well
            FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
            factory.setFeature(FEATURE, false);

            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            // And, per Timothy Morgan: "If for some reason support for inline DOCTYPEs are a requirement, then
            // ensure the entity settings are disabled (as shown above) and beware that SSRF attacks
            // (http://cwe.mitre.org/data/definitions/918.html) and denial
            // of service attacks (such as billion laughs or decompression bombs via "jar:") are a risk."

            DocumentBuilder builder = factory.newDocumentBuilder();
            Reader reader = new StringReader(source);
            InputSource input = new InputSource(reader);
            Document document = builder.parse(input);

            format(document, writer);

        } catch (ParserConfigurationException e) {
            // This should catch a failed setFeature feature
            throw new RuntimeException("ParserConfigurationException was thrown. The feature '" +
                    FEATURE + "' is probably not supported by your XML processor.", e);
        } catch (SAXException e) {
            // On Apache, this should be thrown when disallowing DOCTYPE
            throw new RuntimeException("A DOCTYPE was passed into the XML document", e);
        } catch (IOException e) {
            // XXE that points to a file that doesn't exist
            throw new RuntimeException("IOException occurred, XXE may still possible: " + e.getMessage(), e);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void format(Document document, Writer writer) {
        DOMImplementation implementation = document.getImplementation();

        if (implementation.hasFeature(LS_FEATURE_KEY, LS_FEATURE_VERSION)
                && implementation.hasFeature(CORE_FEATURE_KEY, CORE_FEATURE_VERSION)) {
            DOMImplementationLS implementationLS = (DOMImplementationLS) implementation.getFeature(LS_FEATURE_KEY,
                    LS_FEATURE_VERSION);
            LSSerializer serializer = implementationLS.createLSSerializer();
            DOMConfiguration configuration = serializer.getDomConfig();

            configuration.setParameter("format-pretty-print", Boolean.TRUE);
            configuration.setParameter("comments", preserveComments);

            LSOutput output = implementationLS.createLSOutput();
            output.setEncoding("UTF-8");
            output.setCharacterStream(writer);
            serializer.write(document, output);
        }
    }

    private String read(File file) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InputStream source = new FileInputStream(file);
        byte[] chunk = new byte[1024];
        int count = 0;

        while ((count = source.read(chunk)) != -1) {
            buffer.write(chunk, 0, count);
        }
        return buffer.toString("UTF-8");
    }

    /**
     * Scripts to execute the XML formatter.
     * 
     * #!/bin/bash
     * xml.bat $1 $2 $3
     * 
     * echo off
     * java -jar c:/start/bin/xml.jar %1 %2 %3 %4
     * 
     * @param list
     *            arguments to the formatter
     */
    public static void main(String list[]) throws Exception {
        List<String> values = new ArrayList<String>();

        for (String argument : list) {
            if (argument != null && argument.trim().length() > 0) {
                values.add(argument.trim());
            }
        }
        if (values.size() == 0) {
            throw new FileNotFoundException("File needs to be specified as an argument");
        }
        Formatter formatter = new Formatter();
        File file = new File(values.get(0));
        String source = formatter.read(file); // read before opening for write

        if (values.size() == 1) {
            formatter.format(source, new OutputStreamWriter(System.out));
        }
        else if (values.size() == 2) {
            formatter.format(source, new OutputStreamWriter(new FileOutputStream(new File(values.get(1)))));
        }
        else {
            StringBuilder builder = new StringBuilder();
            for (String value : values) {
                builder.append("'").append(value).append("'");
            }
            throw new IllegalArgumentException("At most two arguments can be specified, you specified " + builder);
        }
    }
}
