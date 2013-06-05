package org.controlsfx.control.propertysheet;

public interface Property {
     
     Class<?> getType();
     String getGroup();
    
     String getName();
     
     Object getValue();
    
}
