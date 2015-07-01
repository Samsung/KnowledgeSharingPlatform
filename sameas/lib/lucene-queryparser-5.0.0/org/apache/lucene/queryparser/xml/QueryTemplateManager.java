package org.apache.lucene.queryparser.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

/*
 *
 * Copyright(c) 2015, Samsung Electronics Co., Ltd.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.
    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

/**
 * Provides utilities for turning query form input (such as from a web page or Swing gui) into
 * Lucene XML queries by using XSL templates.  This approach offers a convenient way of externalizing
 * and changing how user input is turned into Lucene queries.
 * Database applications often adopt similar practices by externalizing SQL in template files that can
 * be easily changed/optimized by a DBA.
 * The static methods can be used on their own or by creating an instance of this class you can store and
 * re-use compiled stylesheets for fast use (e.g. in a server environment)
 */
public class QueryTemplateManager {
  static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  static final TransformerFactory tFactory = TransformerFactory.newInstance();

  HashMap<String, Templates> compiledTemplatesCache = new HashMap<>();
  Templates defaultCompiledTemplates = null;


  public QueryTemplateManager() {

  }

  public QueryTemplateManager(InputStream xslIs)
      throws TransformerConfigurationException, ParserConfigurationException, SAXException, IOException {
    addDefaultQueryTemplate(xslIs);
  }

  public void addDefaultQueryTemplate(InputStream xslIs)
      throws TransformerConfigurationException, ParserConfigurationException, SAXException, IOException {
    defaultCompiledTemplates = getTemplates(xslIs);
  }

  public void addQueryTemplate(String name, InputStream xslIs)
      throws TransformerConfigurationException, ParserConfigurationException, SAXException, IOException {
    compiledTemplatesCache.put(name, getTemplates(xslIs));
  }

  public String getQueryAsXmlString(Properties formProperties, String queryTemplateName)
      throws SAXException, IOException, ParserConfigurationException, TransformerException {
    Templates ts = compiledTemplatesCache.get(queryTemplateName);
    return getQueryAsXmlString(formProperties, ts);
  }

  public Document getQueryAsDOM(Properties formProperties, String queryTemplateName)
      throws SAXException, IOException, ParserConfigurationException, TransformerException {
    Templates ts = compiledTemplatesCache.get(queryTemplateName);
    return getQueryAsDOM(formProperties, ts);
  }

  public String getQueryAsXmlString(Properties formProperties)
      throws SAXException, IOException, ParserConfigurationException, TransformerException {
    return getQueryAsXmlString(formProperties, defaultCompiledTemplates);
  }

  public Document getQueryAsDOM(Properties formProperties)
      throws SAXException, IOException, ParserConfigurationException, TransformerException {
    return getQueryAsDOM(formProperties, defaultCompiledTemplates);
  }

  /**
   * Fast means of constructing query using a precompiled stylesheet
   */
  public static String getQueryAsXmlString(Properties formProperties, Templates template)
      throws ParserConfigurationException, TransformerException {
    // TODO: Suppress XML header with encoding (as Strings have no encoding)
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    transformCriteria(formProperties, template, result);
    return writer.toString();
  }

  /**
   * Slow means of constructing query parsing a stylesheet from an input stream
   */
  public static String getQueryAsXmlString(Properties formProperties, InputStream xslIs)
      throws SAXException, IOException, ParserConfigurationException, TransformerException {
    // TODO: Suppress XML header with encoding (as Strings have no encoding)
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    transformCriteria(formProperties, xslIs, result);
    return writer.toString();
  }


  /**
   * Fast means of constructing query using a cached,precompiled stylesheet
   */
  public static Document getQueryAsDOM(Properties formProperties, Templates template)
      throws ParserConfigurationException, TransformerException {
    DOMResult result = new DOMResult();
    transformCriteria(formProperties, template, result);
    return (Document) result.getNode();
  }


  /**
   * Slow means of constructing query - parses stylesheet from input stream
   */
  public static Document getQueryAsDOM(Properties formProperties, InputStream xslIs)
      throws SAXException, IOException, ParserConfigurationException, TransformerException {
    DOMResult result = new DOMResult();
    transformCriteria(formProperties, xslIs, result);
    return (Document) result.getNode();
  }


  /**
   * Slower transformation using an uncompiled stylesheet (suitable for development environment)
   */
  public static void transformCriteria(Properties formProperties, InputStream xslIs, Result result)
      throws SAXException, IOException, ParserConfigurationException, TransformerException {
    dbf.setNamespaceAware(true);
    DocumentBuilder builder = dbf.newDocumentBuilder();
    org.w3c.dom.Document xslDoc = builder.parse(xslIs);
    DOMSource ds = new DOMSource(xslDoc);

    Transformer transformer = null;
    synchronized (tFactory) {
      transformer = tFactory.newTransformer(ds);
    }
    transformCriteria(formProperties, transformer, result);
  }

  /**
   * Fast transformation using a pre-compiled stylesheet (suitable for production environments)
   */
  public static void transformCriteria(Properties formProperties, Templates template, Result result)
      throws ParserConfigurationException, TransformerException {
    transformCriteria(formProperties, template.newTransformer(), result);
  }


  public static void transformCriteria(Properties formProperties, Transformer transformer, Result result)
      throws ParserConfigurationException, TransformerException {
    dbf.setNamespaceAware(true);

    //Create an XML document representing the search index document.
    DocumentBuilder db = dbf.newDocumentBuilder();
    org.w3c.dom.Document doc = db.newDocument();
    Element root = doc.createElement("Document");
    doc.appendChild(root);

    Enumeration keysEnum = formProperties.keys();
    while (keysEnum.hasMoreElements()) {
      String propName = (String) keysEnum.nextElement();
      String value = formProperties.getProperty(propName);
      if ((value != null) && (value.length() > 0)) {
        DOMUtils.insertChild(root, propName, value);
      }
    }
    //Use XSLT to to transform into an XML query string using the  queryTemplate
    DOMSource xml = new DOMSource(doc);
    transformer.transform(xml, result);
  }

  /**
   * Parses a query stylesheet for repeated use
   */
  public static Templates getTemplates(InputStream xslIs)
      throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException {
    dbf.setNamespaceAware(true);
    DocumentBuilder builder = dbf.newDocumentBuilder();
    org.w3c.dom.Document xslDoc = builder.parse(xslIs);
    DOMSource ds = new DOMSource(xslDoc);
    return tFactory.newTemplates(ds);
  }
}
