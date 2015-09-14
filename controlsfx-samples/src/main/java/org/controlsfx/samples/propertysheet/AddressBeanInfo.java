/**
 * Copyright (c) 2014, 2015 ControlsFX
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
package org.controlsfx.samples.propertysheet;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class AddressBeanInfo extends SimpleBeanInfo {

    private static final BeanDescriptor beanDescriptor = new BeanDescriptor(AddressBeanInfo.class);
    private static PropertyDescriptor[] propDescriptors;

    static {
        beanDescriptor.setDisplayName("Address Bean");
    }

    @Override
    public BeanDescriptor getBeanDescriptor() {
        return beanDescriptor;
    }

    @Override
    public int getDefaultPropertyIndex() {
        return 0;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        if (propDescriptors == null) {
            propDescriptors = new PropertyDescriptor[4];
            try {
                propDescriptors[0] = new PropertyDescriptor("addressLine", Address.class);
                propDescriptors[0].setDisplayName("Address Line 1");
                propDescriptors[1] = new PropertyDescriptor("suburb", Address.class);
                propDescriptors[1].setDisplayName("Suburb");
                propDescriptors[2] = new PropertyDescriptor("state", Address.class);
                propDescriptors[2].setDisplayName("State");
                propDescriptors[3] = new PropertyDescriptor("postcode", Address.class);
                propDescriptors[3].setDisplayName("Postcode");
            } catch (IntrospectionException ex) {
                ex.printStackTrace();
            }
        }
        return propDescriptors;
    }

}
