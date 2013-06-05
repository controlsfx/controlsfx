package org.controlsfx.control.propertysheet;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class BeanPropertyUtils {

    private BeanPropertyUtils() {
        // no op
    }

    public static ObservableList<Property> getProperties(final Object bean) {

        ObservableList<Property> list = FXCollections.observableArrayList();

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass(), Object.class);
            for (PropertyDescriptor p : beanInfo.getPropertyDescriptors()) {
                // Add filtering
                list.add(( new BeanProperty(bean.getClass(), p)));
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        return list;
    }
}
