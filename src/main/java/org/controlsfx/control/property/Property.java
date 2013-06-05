package org.controlsfx.control.property;

public interface Property {
     
     Class<?> getType();
     String getGroup();
    
     String getName();
     
     Object getValue();
    
}
