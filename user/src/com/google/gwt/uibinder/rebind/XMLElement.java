/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.uibinder.rebind;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JEnumType;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A wrapper for {@link Element} that limits the way parsers can interact with
 * the XML document, and provides some convenience methods.
 * <p>
 * The main function of this wrapper is to ensure that parsers can only read
 * elements and attributes by 'consuming' them, which removes the given value.
 * This allows for a natural hierarchy among parsers -- more specific parsers
 * will run first, and if they consume a value, less-specific parsers will not
 * see it.
 */
@SuppressWarnings("deprecation")
// BundleAttributeParser not quite ready to die
public class XMLElement {
  /**
   * Callback interface used by {@link #consumeInnerHtml(Interpreter)} and
   * {@link #consumeChildElements(Interpreter)}.
   */
  public interface Interpreter<T> {
    /**
     * Given an XMLElement, return its filtered value.
     * 
     * @throws UnableToCompleteException on error
     */
    T interpretElement(XMLElement elem) throws UnableToCompleteException;
  }

  /**
   * Extends {@link Interpreter} with a method to be called after all elements
   * have been processed.
   */
  public interface PostProcessingInterpreter<T> extends Interpreter<T> {
    String postProcess(String consumedText) throws UnableToCompleteException;
  }

  private static class NoBrainInterpeter<T> implements Interpreter<T> {
    private final T rtn;

    public NoBrainInterpeter(T rtn) {
      this.rtn = rtn;
    }

    public T interpretElement(XMLElement elem) {
      return rtn;
    }
  }

  private static final Set<String> NO_END_TAG = new HashSet<String>();

  private static void clearChildren(Element elem) {
    Node child;
    while ((child = elem.getFirstChild()) != null) {
      elem.removeChild(child);
    }
  }

  private final Element elem;
  final AttributeParsers attributeParsers;
  final BundleAttributeParsers bundleParsers;
  private final MortalLogger logger;

  private final String debugString;
  private final XMLElementProvider provider;

  {
    // from com/google/gxp/compiler/schema/html.xml
    NO_END_TAG.add("area");
    NO_END_TAG.add("base");
    NO_END_TAG.add("basefont");
    NO_END_TAG.add("br");
    NO_END_TAG.add("col");
    NO_END_TAG.add("frame");
    NO_END_TAG.add("hr");
    NO_END_TAG.add("img");
    NO_END_TAG.add("input");
    NO_END_TAG.add("isindex");
    NO_END_TAG.add("link");
    NO_END_TAG.add("meta");
    NO_END_TAG.add("param");
    NO_END_TAG.add("wbr");
  }

  XMLElement(Element elem, AttributeParsers attributeParsers,
      BundleAttributeParsers bundleParsers, MortalLogger logger,
      XMLElementProvider provider) {
    this.elem = elem;
    this.attributeParsers = attributeParsers;
    this.bundleParsers = bundleParsers;
    this.logger = logger;
    this.provider = provider;

    this.debugString = getOpeningTag();
  }

  /**
   * Consumes the named attribute as a boolean expression: a literal, or a field
   * reference. At the moment field references are validated for syntax
   * only--there is no check that there is any such field or methods, nor
   * that they have the correct return type.
   * 
   * @return "true", "false", an expression that will evaluate to a boolean
   *         value in the generated code, or "" if there is no such attribute
   * 
   * @throws UnableToCompleteException on unparseable value
   */
  public String consumeBooleanAttribute(String name)
      throws UnableToCompleteException {
    String value = consumeRawAttribute(name);
    return attributeParsers.getBooleanParser().parse(value, logger);
  }

  /**
   * Consumes the named attribute as a boolean expression. This will not accept
   * {field.reference} expressions. It is intended for values that must be
   * resolved at compile time, such as generated annotation values.
   * 
   * @return {@link Boolean#TRUE}, {@link Boolean#FALSE}, or null if no such
   *         attribute
   * 
   * @throws UnableToCompleteException on unparseable value
   */
  public Boolean consumeBooleanConstantAttribute(String name)
      throws UnableToCompleteException {
    String value = consumeRawAttribute(name);
    if ("".equals(value)) {
      return null;
    }
    if (value.equals("true") || value.equals("false")) {
      return Boolean.valueOf(value);
    }
    logger.die("In %s, %s must be \"true\" or \"false\"", this, name);
    return null; // unreachable
  }

  /**
   * Consumes and returns all child elements, and erases any text nodes.
   */
  public Iterable<XMLElement> consumeChildElements() {
    try {
      Iterable<XMLElement> rtn = consumeChildElements(new NoBrainInterpeter<Boolean>(
          true));
      clearChildren(elem);
      return rtn;
    } catch (UnableToCompleteException e) {
      throw new RuntimeException("Impossible exception", e);
    }
  }

  /**
   * Consumes and returns all child elements selected by the interpreter. Note
   * that text nodes are not elements, and so are not presented for
   * interpretation, and are not consumed.
   * 
   * @param interpreter Should return true for any child that should be consumed
   *          and returned by the consumeChildElements call
   * @throws UnableToCompleteException
   */
  public Collection<XMLElement> consumeChildElements(
      Interpreter<Boolean> interpreter) throws UnableToCompleteException {
    List<XMLElement> elements = new ArrayList<XMLElement>();
    List<Node> doomed = new ArrayList<Node>();

    NodeList childNodes = elem.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); ++i) {
      Node childNode = childNodes.item(i);
      if (childNode.getNodeType() == Node.ELEMENT_NODE) {
        XMLElement childElement = provider.get((Element) childNode);
        if (interpreter.interpretElement(childElement)) {
          elements.add(childElement);
          doomed.add(childNode);
        }
      }
    }

    for (Node n : doomed) {
      elem.removeChild(n);
    }
    return elements;
  }

  /**
   * Consumes the given attribute as a double expression: a literal, or a field
   * reference. At the moment field references are validated for syntax
   * only--there is no check that there is any such field or methods, nor
   * that they have the correct return type.
   * 
   * @return a double literal, an expression that will evaluate to a double
   *         value in the generated code, or "" if there is no such attribute
   * 
   * @throws UnableToCompleteException on unparseable value
   */
  public String consumeDoubleAttribute(String name)
      throws UnableToCompleteException {
    String value = consumeRawAttribute(name);
    return attributeParsers.getDoubleParser().parse(value, logger);
  }

  /**
   * Consumes the given attribute as an enum value.
   * 
   * @param name the attribute's full name (including prefix)
   * @param enumType the enumerated type of which this attribute must be a member
   * @return the attribute's value
   * @throws UnableToCompleteException
   */
  public String consumeEnumAttribute(String name, JEnumType enumType)
      throws UnableToCompleteException {
    String value = consumeRawAttribute(name);
    return attributeParsers.get(enumType).parse(value, logger);
  }

  /**
   * Consumes all child elements, and returns an HTML interpretation of them.
   * Trailing and leading whitespace is trimmed.
   * <p>
   * Each element encountered will be passed to the given Interpreter for
   * possible replacement. Escaping is performed to allow the returned text to
   * serve as a Java string literal used as input to a setInnerHTML call.
   * <p>
   * This call requires an interpreter to make sense of any special children.
   * The odds are you want to use
   * {@link com.google.gwt.templates.parsers.HtmlInterpreter} for an HTML value,
   * or {@link com.google.gwt.templates.parsers.TextInterpreter} for text.
   * 
   * @param interpreter Called for each element, expected to return a string
   *          replacement for it, or null if it should be left as is
   */
  public String consumeInnerHtml(Interpreter<String> interpreter)
      throws UnableToCompleteException {
    if (interpreter == null) {
      throw new NullPointerException("interpreter must not be null");
    }
    StringBuffer buf = new StringBuffer();
    GetInnerHtmlVisitor.getEscapedInnerHtml(elem, buf, interpreter, provider);

    clearChildren(elem);
    return buf.toString().trim();
  }

  /**
   * Refines {@link #consumeInnerHtml(Interpreter)} to handle
   * PostProcessingInterpreter.
   */
  public String consumeInnerHtml(PostProcessingInterpreter<String> interpreter)
      throws UnableToCompleteException {
    String html = consumeInnerHtml((Interpreter<String>) interpreter);
    return interpreter.postProcess(html);
  }

  /**
   * Refines {@link #consumeInnerTextEscapedAsHtmlStringLiteral(Interpreter)} to
   * handle PostProcessingInterpreter.
   */
  public String consumeInnerText(PostProcessingInterpreter<String> interpreter)
      throws UnableToCompleteException {
    String text = consumeInnerTextEscapedAsHtmlStringLiteral(interpreter);
    return interpreter.postProcess(text);
  }

  /**
   * Consumes all child text nodes, and asserts that this element held only
   * text. Trailing and leading whitespace is trimmed, and escaped for use as a
   * string literal. Notice that HTML entities in the text are also escaped--is
   * this a source of errors?
   * <p>
   * This call requires an interpreter to make sense of any special children.
   * The odds are you want to use
   * {@link com.google.gwt.templates.parsers.TextInterpreter}
   * 
   * @throws UnableToCompleteException If any elements present are not consumed
   *           by the interpreter
   */
  public String consumeInnerTextEscapedAsHtmlStringLiteral(
      Interpreter<String> interpreter) throws UnableToCompleteException {
    if (interpreter == null) {
      throw new NullPointerException("interpreter must not be null");
    }
    StringBuffer buf = new StringBuffer();

    GetEscapedInnerTextVisitor.getEscapedInnerText(elem, buf, interpreter,
        provider);

    // Make sure there are no children left but empty husks
    for (XMLElement child : consumeChildElements()) {
      if (child.hasChildNodes() || child.getAttributeCount() > 0) {
        // TODO(rjrjr) This is not robust enough, and consumeInnerHtml needs
        // a similar check
        logger.die("Text value of \"%s\" has illegal child \"%s\"", this, child);
      }
    }

    clearChildren(elem);
    return buf.toString().trim();
  }

  /**
   * Consumes all attributes, and returns a string representing the entire
   * opening tag. E.g., "<div able='baker'>"
   */
  public String consumeOpeningTag() {
    String rtn = getOpeningTag();

    for (int i = getAttributeCount() - 1; i >= 0; i--) {
      getAttribute(i).consumeRawValue();
    }
    return rtn;
  }

  /**
   * Consumes the given attribute and returns its trimmed value, or null if it
   * was unset. The returned string is not escaped.
   * 
   * @param name the attribute's full name (including prefix)
   * @return the attribute's value, or ""
   */
  public String consumeRawAttribute(String name) {
    String value = elem.getAttribute(name);
    elem.removeAttribute(name);
    return value.trim();
  }

  /**
   * Consumes the given attribute and returns its trimmed value, or the given
   * default value if it was unset. The returned string is not escaped.
   * 
   * @param name the attribute's full name (including prefix)
   * @param defaultValue the value to return if the attribute was unset
   * @return the attribute's value, or defaultValue
   */
  public String consumeRawAttribute(String name, String defaultValue) {
    String value = consumeRawAttribute(name);
    if ("".equals(value)) {
      return defaultValue;
    }
    return value;
  }

  /**
   * Consumes the named attribute, or dies if it is missing.
   */
  public String consumeRequiredAttribute(String name)
      throws UnableToCompleteException {
    String value = consumeRawAttribute(name);
    if ("".equals(value)) {
      logger.die("In %s, missing required attribute name \"%s\"", this, name);
    }
    return value;
  }

  /**
   * Consumes a single child element, ignoring any text nodes and throwing an
   * exception if no child is found, or more than one child element is found.
   * 
   * @throws UnableToCompleteException on no children, or too many
   */
  public XMLElement consumeSingleChildElement()
      throws UnableToCompleteException {
    XMLElement ret = null;
    for (XMLElement child : consumeChildElements()) {
      if (ret != null) {
        logger.die("%s may only contain a single child element, but found "
            + "%s and %s.", this, ret, child);
      }

      ret = child;
    }

    if (ret == null) {
      logger.die("%s must have a single child element", this);
    }

    return ret;
  }

  /**
   * Returns the unprocessed, unescaped, raw inner text of the receiver. Dies if
   * the receiver has non-text children.
   * <p>
   * You probably want to use
   * {@link #consumeInnerTextEscapedAsHtmlStringLiteral} instead.
   * 
   * @return the text
   * @throws UnableToCompleteException if it held anything other than text nodes
   */
  public String consumeUnescapedInnerText() throws UnableToCompleteException {
    final NodeList children = elem.getChildNodes();
    if (children.getLength() < 1) {
      return "";
    }
    if (children.getLength() > 1
        || Node.TEXT_NODE != children.item(0).getNodeType()) {
      logger.die("%s must contain only text", this);
    }
    Text t = (Text) children.item(0);
    return t.getTextContent();
  }

  /**
   * Get the attribute at the given index. If you are consuming attributes,
   * remember to traverse them in reverse.
   */
  public XMLAttribute getAttribute(int i) {
    return new XMLAttribute(XMLElement.this,
        (Attr) elem.getAttributes().item(i));
  }

  /**
   * @return The number of attributes this element has
   */
  public int getAttributeCount() {
    return elem.getAttributes().getLength();
  }

  public String getClosingTag() {
    if (NO_END_TAG.contains(elem.getTagName())) {
      return "";
    }
    return String.format("</%s>", elem.getTagName());
  }

  /**
   * Gets this element's local name (sans namespace prefix).
   */
  public String getLocalName() {
    return elem.getLocalName();
  }

  /**
   * Gets this element's namespace URI.
   */
  public String getNamespaceUri() {
    return elem.getNamespaceURI();
  }

  public String getNamespaceUriForAttribute(String fieldName) {
    Attr attr = elem.getAttributeNode(fieldName);
    return attr.getNamespaceURI();
  }

  /**
   * @return the parent element, or null if parent is null or a node type other
   *         than Element
   */
  public XMLElement getParent() {
    Node parent = elem.getParentNode();
    if (parent == null || Node.ELEMENT_NODE != parent.getNodeType()) {
      return null;
    }
    return provider.get((Element) parent);
  }

  public String getPrefix() {
    return elem.getPrefix();
  }

  /**
   * Determines whether the element has a given attribute.
   */
  public boolean hasAttribute(String name) {
    return elem.hasAttribute(name);
  }

  public boolean hasChildNodes() {
    return elem.hasChildNodes();
  }

  public String lookupPrefix(String prefix) {
    return elem.lookupPrefix(prefix);
  }

  public void setAttribute(String name, String value) {
    elem.setAttribute(name, value);
  }

  @Override
  public String toString() {
    return debugString;
  }

  private String getOpeningTag() {
    StringBuilder b = new StringBuilder().append("<").append(elem.getTagName());

    NamedNodeMap attrs = elem.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      Attr attr = (Attr) attrs.item(i);
      b.append(String.format(" %s='%s'", attr.getName(),
          UiBinderWriter.escapeAttributeText(attr.getValue())));
    }
    b.append(">");

    return b.toString();
  }
}
