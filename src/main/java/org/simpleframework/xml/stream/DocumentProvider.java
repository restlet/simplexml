/*
 * DocumentProvider.java January 2010
 *
 * Copyright (C) 2010, Niall Gallagher <niallg@users.sf.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package org.simpleframework.xml.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The <code>DocumentProvider</code> object is used to provide event
 * reader implementations for DOM. Wrapping the mechanics of the
 * DOM framework within a <code>Provider</code> ensures that it can
 * be plugged in without any dependencies. This allows other parsers
 * to be swapped in should there be such a requirement.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.xml.stream.DocumentProvider
 */
class DocumentProvider implements Provider {
   
   /**
    * This is the factory that is used to create DOM parsers.
    */
   private final DocumentBuilderFactory factory;
   
   /**
    * Constructor for the <code>DocumentProvider</code> object. This
    * is used to instantiate a parser factory that will be used to
    * create parsers when requested. Instantiating the factory up
    * front also checks that the framework is fully supported.
    */
   public DocumentProvider() {
      this.factory = DocumentBuilderFactory.newInstance();
      this.factory.setNamespaceAware(true);

        // Security issue: block entities expansion that can lead to expose local files
        // cf https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#Java
        String FEATURE = null;
        try {
            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
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
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("ParserConfigurationException was thrown. The feature '" + FEATURE
                    + "' is probably not supported by your XML processor.", e);
        }

   }
   
   /**
    * This provides an <code>EventReader</code> that will read from
    * the specified input stream. When reading from an input stream
    * the character encoding should be taken from the XML prolog or
    * it should default to the UTF-8 character encoding.
    * 
    * @param source this is the stream to read the document with
    * 
    * @return this is used to return the event reader implementation
    */
   public EventReader provide(InputStream source) throws Exception {
      return provide(new InputSource(source));
   }
   
   /**
    * This provides an <code>EventReader</code> that will read from
    * the specified reader. When reading from a reader the character
    * encoding should be the same as the source XML document.
    * 
    * @param source this is the reader to read the document with
    * 
    * @return this is used to return the event reader implementation
    */
   public EventReader provide(Reader source) throws Exception {
      return provide(new InputSource(source));
   }   
   
   /**
    * This provides an <code>EventReader</code> that will read from
    * the specified source. When reading from a source the character
    * encoding should be the same as the source XML document.
    * 
    * @param source this is the source to read the document with
    * 
    * @return this is used to return the event reader implementation
    */
   private EventReader provide(InputSource source) throws Exception {
      DocumentBuilder builder = factory.newDocumentBuilder();

        try {
            return new DocumentReader(builder.parse(source));
        } catch (SAXException e) {
            // On Apache, this should be thrown when disallowing DOCTYPE
            throw new RuntimeException("A DOCTYPE was passed into the XML document, and this is blocked on purpose due to security issues", e);
        } catch (IOException e) {
            // XXE that points to a file that doesn't exist
            throw new RuntimeException("IOException occurred, XXE may still possible", e);
        }
    }
}
