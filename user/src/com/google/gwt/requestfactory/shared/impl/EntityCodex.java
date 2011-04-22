/*
 * Copyright 2010 Google Inc.
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
package com.google.gwt.requestfactory.shared.impl;

import com.google.gwt.autobean.shared.AutoBean;
import com.google.gwt.autobean.shared.AutoBeanUtils;
import com.google.gwt.autobean.shared.Splittable;
import com.google.gwt.autobean.shared.ValueCodex;
import com.google.gwt.autobean.shared.impl.LazySplittable;
import com.google.gwt.autobean.shared.impl.StringQuoter;
import com.google.gwt.requestfactory.shared.BaseProxy;
import com.google.gwt.requestfactory.shared.EntityProxyId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Analogous to {@link ValueCodex}, but for object types.
 *
 * <p><span style='color:red'>RequestFactory has moved to
 * <code>com.google.web.bindery.requestfactory</code>.  This package will be
 * removed in a future version of GWT.</span></p>
 */
@Deprecated
public class EntityCodex {
  /**
   * Abstracts the process by which EntityProxies are created.
   *
   * <p><span style='color:red'>RequestFactory has moved to
   * <code>com.google.web.bindery.requestfactory</code>.  This package will be
   * removed in a future version of GWT.</span></p>
   */
  @Deprecated
  public interface EntitySource {
    /**
     * Expects an encoded
     * {@link com.google.gwt.requestfactory.shared.messages.IdMessage}.
     */
    <Q extends BaseProxy> AutoBean<Q> getBeanForPayload(
        Splittable serializedIdMessage);

    /**
     * Should return an encoded
     * {@link com.google.gwt.requestfactory.shared.messages.IdMessage}.
     */
    Splittable getSerializedProxyId(SimpleProxyId<?> stableId);

    boolean isEntityType(Class<?> clazz);

    boolean isValueType(Class<?> clazz);
  }

  /**
   * Collection support is limited to value types and resolving ids.
   */
  public static Object decode(EntitySource source, Class<?> type,
      Class<?> elementType, Splittable split) {
    if (split == null || split == LazySplittable.NULL) {
      return null;
    }

    // Collection support
    if (elementType != null) {
      Collection<Object> collection = null;
      if (List.class.equals(type)) {
        collection = new ArrayList<Object>();
      } else if (Set.class.equals(type)) {
        collection = new HashSet<Object>();
      } else {
        throw new UnsupportedOperationException();
      }

      // Decode values
      if (ValueCodex.canDecode(elementType)) {
        for (int i = 0, j = split.size(); i < j; i++) {
          if (split.isNull(i)) {
            collection.add(null);
          } else {
            Object element = ValueCodex.decode(elementType, split.get(i));
            collection.add(element);
          }
        }
      } else {
        for (int i = 0, j = split.size(); i < j; i++) {
          if (split.isNull(i)) {
            collection.add(null);
          } else {
            Object element = decode(source, elementType, null, split.get(i));
            collection.add(element);
          }
        }
      }
      return collection;
    }

    if (source.isEntityType(type) || source.isValueType(type)
        || EntityProxyId.class.equals(type)) {
      return source.getBeanForPayload(split).as();
    }

    // Fall back to values
    return ValueCodex.decode(type, split);
  }

  /**
   * Collection support is limited to value types and resolving ids.
   */
  public static Object decode(EntitySource source, Class<?> type,
      Class<?> elementType, String jsonPayload) {
    Splittable split = StringQuoter.split(jsonPayload);
    return decode(source, type, elementType, split);
  }

  /**
   * Create a wire-format representation of an object.
   */
  public static Splittable encode(EntitySource source, Object value) {
    if (value == null) {
      return LazySplittable.NULL;
    }

    if (value instanceof Poser<?>) {
      value = ((Poser<?>) value).getPosedValue();
    }

    if (value instanceof Iterable<?>) {
      StringBuffer toReturn = new StringBuffer();
      toReturn.append('[');
      boolean first = true;
      for (Object val : ((Iterable<?>) value)) {
        if (!first) {
          toReturn.append(',');
        } else {
          first = false;
        }
        if (val == null) {
          toReturn.append("null");
        } else {
          toReturn.append(encode(source, val).getPayload());
        }
      }
      toReturn.append(']');
      return new LazySplittable(toReturn.toString());
    }

    if (value instanceof BaseProxy) {
      AutoBean<BaseProxy> autoBean = AutoBeanUtils.getAutoBean((BaseProxy) value);
      value = BaseProxyCategory.stableId(autoBean);
    }

    if (value instanceof SimpleProxyId<?>) {
      return source.getSerializedProxyId((SimpleProxyId<?>) value);
    }

    return ValueCodex.encode(value);
  }
}
