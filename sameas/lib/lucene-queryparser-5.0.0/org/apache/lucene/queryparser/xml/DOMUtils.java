package org.apache.lucene.queryparser.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Reader;
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
 * Helper methods for parsing XML
 */
public class DOMUtils {

  public static Element getChildByTagOrFail(Element e, String name) throws ParserException {
    Element kid = getChildByTagName(e, name);
    if (null == kid) {
      throw new ParserException(e.getTagName() + " missing \"" + name
          + "\" child element");
    }
    return kid;
  }

  public static Element getFirstChildOrFail(Element e) throws ParserException {
    Element kid = getFirstChildElement(e);
    if (null == kid) {
      throw new ParserException(e.getTagName()
          + " does not contain a child element");
    }
    return kid;
  }

  public static String getAttributeOrFail(Element e, String name) throws ParserException {
    String v = e.getAttribute(name);
    if (null == v) {
      throw new ParserException(e.getTagName() + " missing \"" + name
          + "\" attribute");
    }
    return v;
  }

  public static String getAttributeWithInheritanceOrFail(Element e, String name) throws ParserException {
    String v = getAttributeWithInheritance(e, name);
    if (null == v) {
      throw new ParserException(e.getTagName() + " missing \"" + name
          + "\" attribute");
    }
    return v;
  }

  public static String getNonBlankTextOrFail(Element e) throws ParserException {
    String v = getText(e);
    if (null != v)
      v = v.trim();
    if (null == v || 0 == v.length()) {
      throw new ParserException(e.getTagName() + " has no text");
    }
    return v;
  }

  /* Convenience method where there is only one child Element of a given name */
  public static Element getChildByTagName(Element e, String name) {
    for (Node kid = e.getFirstChild(); kid != null; kid = kid.getNextSibling()) {
      if ((kid.getNodeType() == Node.ELEMENT_NODE) && (name.equals(kid.getNodeName()))) {
        return (Element) kid;
      }
    }
    return null;
  }

  /**
   * Returns an attribute value from this node, or first parent node with this attribute defined
   *
   * @return A non-zero-length value if defined, otherwise null
   */
  public static String getAttributeWithInheritance(Element element, String attributeName) {
    String result = element.getAttribute(attributeName);
    if ((result == null) || ("".equals(result))) {
      Node n = element.getParentNode();
      if ((n == element) || (n == null)) {
        return null;
      }
      if (n instanceof Element) {
        Element parent = (Element) n;
        return getAttributeWithInheritance(parent, attributeName);
      }
      return null; //we reached the top level of the document without finding attribute
    }
    return result;
  }


  /* Convenience method where there is only one child Element of a given name */
  public static String getChildTextByTagName(Element e, String tagName) {
    Element child = getChildByTagName(e, tagName);
    return child != null ? getText(child) : null;
  }

  /* Convenience method to append a new child with text*/
  public static Element insertChild(Element parent, String tagName, String text) {
    Element child = parent.getOwnerDocument().createElement(tagName);
    parent.appendChild(child);
    if (text != null) {
      child.appendChild(child.getOwnerDocument().createTextNode(text));
    }
    return child;
  }

  public static String getAttribute(Element element, String attributeName, String deflt) {
    String result = element.getAttribute(attributeName);
    return (result == null) || ("".equals(result)) ? deflt : result;
  }

  public static float getAttribute(Element element, String attributeName, float deflt) {
    String result = element.getAttribute(attributeName);
    return (result == null) || ("".equals(result)) ? deflt : Float.parseFloat(result);
  }

  public static int getAttribute(Element element, String attributeName, int deflt) {
    String result = element.getAttribute(attributeName);
    return (result == null) || ("".equals(result)) ? deflt : Integer.parseInt(result);
  }

  public static boolean getAttribute(Element element, String attributeName,
                                     boolean deflt) {
    String result = element.getAttribute(attributeName);
    return (result == null) || ("".equals(result)) ? deflt : Boolean.valueOf(result);
  }

  /* Returns text of node and all child nodes - without markup */
  //MH changed to Node from Element 25/11/2005

  public static String getText(Node e) {
    StringBuilder sb = new StringBuilder();
    getTextBuffer(e, sb);
    return sb.toString();
  }

  public static Element getFirstChildElement(Element element) {
    for (Node kid = element.getFirstChild(); kid != null; kid = kid.getNextSibling()) {
      if (kid.getNodeType() == Node.ELEMENT_NODE) {
        return (Element) kid;
      }
    }
    return null;
  }

  private static void getTextBuffer(Node e, StringBuilder sb) {
    for (Node kid = e.getFirstChild(); kid != null; kid = kid.getNextSibling()) {
      switch (kid.getNodeType()) {
        case Node.TEXT_NODE: {
          sb.append(kid.getNodeValue());
          break;
        }
        case Node.ELEMENT_NODE: {
          getTextBuffer(kid, sb);
          break;
        }
        case Node.ENTITY_REFERENCE_NODE: {
          getTextBuffer(kid, sb);
          break;
        }
      }
    }
  }

  /**
   * Helper method to parse an XML file into a DOM tree, given a reader.
   *
   * @param is reader of the XML file to be parsed
   * @return an org.w3c.dom.Document object
   */
  public static Document loadXML(Reader is) {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = null;

    try {
      db = dbf.newDocumentBuilder();
    }
    catch (Exception se) {
      throw new RuntimeException("Parser configuration error", se);
    }

    // Step 3: parse the input file
    org.w3c.dom.Document doc = null;
    try {
      doc = db.parse(new InputSource(is));
      //doc = db.parse(is);
    }
    catch (Exception se) {
      throw new RuntimeException("Error parsing file:" + se, se);
    }

    return doc;
  }
}



