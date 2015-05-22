/**
 * Copyright (c) 2013, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.property;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.function.Predicate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;

/**
 * Convenience utility class for creating {@link PropertySheet} instances based
 * on a JavaBean.
 */
public final class BeanPropertyUtils {

    private BeanPropertyUtils() {
        // no op
    }

    /**
     * Given a JavaBean, this method will return a list of {@link Item} intances,
     * which may be directly placed inside a {@link PropertySheet} (via its
     * {@link PropertySheet#getItems() items list}.
     * <p>
     * This method will not return read-only properties.
     * 
     * @param bean The JavaBean that should be introspected and be editable via
     *      a {@link PropertySheet}.
     * @return A list of {@link Item} instances representing the properties of the
     *      JavaBean.
     */
    public static ObservableList<Item> getProperties(final Object bean) {
        return getProperties(bean, (p) -> {return true;} );
    }
    
    /**
     * Given a JavaBean, this method will return a list of {@link Item} intances,
     * which may be directly placed inside a {@link PropertySheet} (via its
     * {@link PropertySheet#getItems() items list}.
     * 
     * @param bean The JavaBean that should be introspected and be editable via
     *      a {@link PropertySheet}.
     * @param test Predicate to test whether the property should be included in the 
     *      list of results.
     * @return A list of {@link Item} instances representing the properties of the
     *      JavaBean.
     */
    public static ObservableList<Item> getProperties(final Object bean, Predicate<PropertyDescriptor> test) {
        ObservableList<Item> list = FXCollections.observableArrayList();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass(), Object.class);
            for (PropertyDescriptor p : beanInfo.getPropertyDescriptors()) {
                if (test.test(p)) {
                    list.add(new BeanProperty(bean, p));
                }
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        return list;
    }
    
}
