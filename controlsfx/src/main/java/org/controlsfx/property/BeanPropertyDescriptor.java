/**
 * Copyright (c) 2016 ControlsFX
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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Mode;

/**
 * A convenience class for creating a full featured {@link PropertyDescriptor}
 * for use in coordination with {@link BeanProperty}. In extension of classic
 * {@link PropertyDescriptor} which provides a display name, a short description
 * and methods to read and optionally write values, it will provide "category"
 * information.
 *
 * @see BeanProperty
 * @see PropertyDescriptor
 */
public class BeanPropertyDescriptor extends PropertyDescriptor {

    /**
     * Label to use in case of category mode has been enabled in
     * {@link PropertySheet}. Null value will be used to fall back in default
     * behavior in {@link PropertySheet.Item#getCategory()}. An empty string
     * will just create a category without a label.
     */
    private String internalCategory = null;

    /**
     * Constructs a PropertyDescriptor for a property that follows the standard
     * Java convention by having getFoo and setFoo accessor methods. Thus if the
     * argument name is "fred", it will assume that the writer method is
     * "setFred" and the reader method is "getFred" (or "isFred" for a boolean
     * property). Note that the property name should start with a lower case
     * character, which will be capitalized in the method names.
     *
     * @param propertyName
     *            The programmatic name of the property.
     * @param beanClass
     *            The Class object for the target bean. For example
     *            sun.beans.OurButton.class.
     * @param category
     *            label to use in case of categorized property. Could be null to
     *            keep default behavior in
     *            {@link PropertySheet.Item#getCategory()}.
     * @exception IntrospectionException
     *                if an exception occurs during introspection.
     */
    public BeanPropertyDescriptor(final String propertyName, final Class<?> beanClass, final String category)
            throws IntrospectionException {
        super(propertyName, beanClass);
        this.setCategory(category);
    }

    /**
     * This constructor takes the name of a simple property, and method names
     * for reading and writing the property.
     *
     * @param propertyName
     *            The programmatic name of the property.
     * @param beanClass
     *            The Class object for the target bean. For example
     *            sun.beans.OurButton.class.
     * @param readMethodName
     *            The name of the method used for reading the property value.
     *            May be null if the property is write-only.
     * @param writeMethodName
     *            The name of the method used for writing the property value.
     *            May be null if the property is read-only.
     * @param category
     *            label to use in case of categorized property. Could be null to
     *            keep default behavior in
     *            {@link PropertySheet.Item#getCategory()}.
     * @exception IntrospectionException
     *                if an exception occurs during introspection.
     */
    public BeanPropertyDescriptor(final String propertyName, final Class<?> beanClass, final String readMethodName,
            final String writeMethodName, final String category) throws IntrospectionException {
        super(propertyName, beanClass, readMethodName, writeMethodName);
        this.setCategory(category);
    }

    /**
     * This constructor takes the name of a simple property, and Method objects
     * for reading and writing the property.
     *
     * @param propertyName
     *            The programmatic name of the property.
     * @param readMethod
     *            The method used for reading the property value. May be null if
     *            the property is write-only.
     * @param writeMethod
     *            The method used for writing the property value. May be null if
     *            the property is read-only.
     * @param category
     *            label to use in case of categorized property. Could be null to
     *            keep default behavior in
     *            {@link PropertySheet.Item#getCategory()}.
     * @exception IntrospectionException
     *                if an exception occurs during introspection.
     */
    public BeanPropertyDescriptor(final String propertyName, final Method readMethod, final Method writeMethod,
            final String category) throws IntrospectionException {
        super(propertyName, readMethod, writeMethod);
        this.setCategory(category);
    }

    /**
     * Returns a String representation of the category of the property. This is
     * relevant when the {@link PropertySheet}
     * {@link PropertySheet#modeProperty() mode property} is set to
     * {@link Mode#CATEGORY} - as then all properties with the same category
     * will be grouped together visually.
     */
    public String getCategory() {
        return this.internalCategory;
    }

    /**
     * Sets the localized category name of this feature.
     *
     * @param categroy
     *            The localized category name for the property.
     */
    public void setCategory(final String category) {
        this.internalCategory = category;
    }
}
