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
package com.google.gwt.uibinder.parsers;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.uibinder.rebind.UiBinderWriter;
import com.google.gwt.uibinder.rebind.XMLElement;
import com.google.gwt.uibinder.rebind.messages.MessageWriter;
import com.google.gwt.uibinder.rebind.messages.MessagesWriter;
import com.google.gwt.uibinder.rebind.messages.PlaceholderInterpreter;

/**
 * Interprets the interior of an html message. Extends the basic
 * PlaceholderInterpreter to handle dom elements with field names and computed
 * attributes--basically any dom attribute that cannot live in a static value
 * and so must be wrapped in a placeholder. (Note that the interior of such an
 * element is processed with a recursive call to
 * {@link XMLElement#consumeInnerHtml}.)
 */
class HtmlPlaceholderInterpreter extends PlaceholderInterpreter {
  private int serial = 0;
  private final XMLElement.Interpreter<String> fieldAndComputed;

  HtmlPlaceholderInterpreter(UiBinderWriter writer,
      MessageWriter message, String ancestorExpression) {
    super(writer, message);
    fieldAndComputed =
        InterpreterPipe.newPipe(new FieldInterpreter(writer,
            ancestorExpression), new ComputedAttributeInterpreter(writer));
  }

  @Override
  public String interpretElement(XMLElement elem)
      throws UnableToCompleteException {
    fieldAndComputed.interpretElement(elem);

    if (isDomPlaceholder(elem)) {
      MessagesWriter mw = uiWriter.getMessages();

      String name = mw.consumeMessageAttribute("ph", elem);
      if ("".equals(name)) {
        name = "htmlElement" + (++serial);
      }

      String openTag = elem.consumeOpeningTag();
      String openPlaceholder =
          nextPlaceholder(name + "Begin", stripTokens(openTag),
              uiWriter.detokenate(openTag));

      String body = elem.consumeInnerHtml(this);

      String closeTag = elem.getClosingTag();
      String closePlaceholder =
          nextPlaceholder(name + "End", closeTag, closeTag);

      return openPlaceholder + body + closePlaceholder;
    }

    return super.interpretElement(elem);
  }

  @Override
  protected String consumePlaceholderInnards(XMLElement elem)
      throws UnableToCompleteException {
    return elem.consumeInnerHtml(fieldAndComputed);
  }

  /**
   * @return true if it has an m:ph attribute, or has a token in any attribute
   */
  private boolean isDomPlaceholder(XMLElement elem) {
    MessagesWriter mw = uiWriter.getMessages();
    if (mw.hasMessageAttribute("ph", elem)) {
      return true;
    }
    for (int i = elem.getAttributeCount() - 1; i >= 0; i--) {
      if (elem.getAttribute(i).hasToken()) {
        return true;
      }
    }
    return false;
  }
}
