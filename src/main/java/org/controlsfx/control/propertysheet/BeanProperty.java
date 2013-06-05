package org.controlsfx.control.propertysheet;

import java.beans.PropertyDescriptor;

public class BeanProperty implements Property {

    private final Class<?> type;
    private final PropertyDescriptor propertyDescriptor;

    public BeanProperty( Class<?> type, PropertyDescriptor propertyDescriptor ) {
        this.propertyDescriptor = propertyDescriptor;
        this.type = type;
    }
    
    @Override public String getName() {
        return propertyDescriptor.getDisplayName();
    }
    
    @Override public Class<?> getType() {
        return propertyDescriptor.getPropertyType();
    }

    @Override public Object getValue() {
        return propertyDescriptor.getValue(getName());
    }

    @Override public String getGroup() {
        return type.getSimpleName();
    }

}
