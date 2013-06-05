package org.controlsfx.property;

public interface Property {
     
     Class<?> getType();
     String getGroup();
    
     String getName();
     
     Object getValue();
    
}
