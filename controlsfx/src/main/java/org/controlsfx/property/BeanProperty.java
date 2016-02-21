/**
 * Copyright (c) 2013, 2015, 2016 ControlsFX
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

import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.PropertyEditor;

import impl.org.controlsfx.i18n.Localization;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;

/**
 * A convenience class for creating a {@link Item} for use in the
 * {@link PropertySheet} control based on a property belonging to a
 * JavaBean - simply provide a {@link PropertyDescriptor} and the rest will be
 * taken care of automatically.
 *
 * @see Item
 * @see PropertySheet
 * @see PropertyDescriptor
 */
public class BeanProperty implements PropertySheet.Item {

    /**
     * Unique identifier to provide a custom category label within
     * {@link PropertySheet.Item#getCategory()}.
     *
     * How to use it: with a PropertyDescriptor, provide the custom category
     * through a a named attribute
     * {@link FeatureDescriptor#setValue(String, Object)}.
     *
     * <pre>
     * final PropertyDescriptor propertyDescriptor = new PropertyDescriptor("yourProperty", YourBean.class);
     * propertyDescriptor.setDisplayName("Your Display Name");
     * propertyDescriptor.setShortDescription("Your explanation about this property.");
     * // then provide a custom category
     * propertyDescriptor.setValue(BeanProperty.CATEGORY_LABEL_KEY, "Your custom category");
     * </pre>
     */
    public static final String CATEGORY_LABEL_KEY = "propertysheet.item.category.label";

    private final Object bean;
    private final PropertyDescriptor beanPropertyDescriptor;
    private final Method readMethod;
    private boolean editable = true;
    private Optional<ObservableValue<? extends Object>> observableValue = Optional.empty();

    public BeanProperty(final Object bean, final PropertyDescriptor propertyDescriptor) {
        this.bean = bean;
        this.beanPropertyDescriptor = propertyDescriptor;
        this.readMethod = propertyDescriptor.getReadMethod();
        if (this.beanPropertyDescriptor.getWriteMethod() == null) {
            this.setEditable(false);
        }

        this.findObservableValue();
    }

    /** {@inheritDoc} */
    @Override public String getName() {
        return this.beanPropertyDescriptor.getDisplayName();
    }

    /** {@inheritDoc} */
    @Override public String getDescription() {
        return this.beanPropertyDescriptor.getShortDescription();
    }

    /** {@inheritDoc} */
    @Override public Class<?> getType() {
        return this.beanPropertyDescriptor.getPropertyType();
    }

    /** {@inheritDoc} */
    @Override public Object getValue() {
        try {
            return this.readMethod.invoke(this.bean);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override public void setValue(final Object value) {
        final Method writeMethod = this.beanPropertyDescriptor.getWriteMethod();
        if ( writeMethod != null ) {
            try {
                writeMethod.invoke(this.bean, value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            } catch (final Throwable e) {
                if (e instanceof PropertyVetoException) {
                    final Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(Localization.localize(Localization.asKey("bean.property.change.error.title")));//$NON-NLS-1$
                    alert.setHeaderText(Localization.localize(Localization.asKey("bean.property.change.error.masthead")));//$NON-NLS-1$
                    alert.setContentText(e.getLocalizedMessage());
                    alert.showAndWait();
                } else {
                    throw e;
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override public String getCategory() {
        String category = (String) this.beanPropertyDescriptor.getValue(BeanProperty.CATEGORY_LABEL_KEY);

        // fall back to default behavior if there is no category provided.
        if (category == null) {
            category = Localization.localize(Localization.asKey(this.beanPropertyDescriptor.isExpert()
                    ? "bean.property.category.expert" : "bean.property.category.basic")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return category;
    }

    /**
     * @return The object passed in to the constructor of the BeanProperty.
     */
    public Object getBean() {
        return this.bean;
    }

    /**
     * @return The {@link PropertyDescriptor} passed in to the constructor of
     * the BeanProperty.
     */
    public PropertyDescriptor getPropertyDescriptor() {
        return this.beanPropertyDescriptor;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked" })
    @Override public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {

        if ((this.beanPropertyDescriptor.getPropertyEditorClass() != null) &&
                PropertyEditor.class.isAssignableFrom(this.beanPropertyDescriptor.getPropertyEditorClass())) {

            return Optional.of((Class<PropertyEditor<?>>)this.beanPropertyDescriptor.getPropertyEditorClass());
        }

        return Item.super.getPropertyEditorClass();
    }

    /** {@inheritDoc} */
    @Override public boolean isEditable() {
        return this.editable;
    }

    /**
     * @param editable Whether this property should be editable in the PropertySheet.
     */
    public void setEditable(final boolean editable) {
        this.editable = editable;
    }

    /** {@inheritDoc} */
    @Override public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return this.observableValue;
    }

    private void findObservableValue() {
        try {
            final String propName = this.beanPropertyDescriptor.getName() + "Property";
            final Method m = this.getBean().getClass().getMethod(propName);
            final Object val = m.invoke(this.getBean());
            if ((val != null) && (val instanceof ObservableValue)) {
                this.observableValue = Optional.of((ObservableValue<?>) val);
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            //Logger.getLogger(BeanProperty.class.getName()).log(Level.SEVERE, null, ex);
            // ignore it...
        }
    }
}
