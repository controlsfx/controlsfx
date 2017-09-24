/**
 * Copyright (c) 2015 ControlsFX
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
package org.controlsfx.property.editor;

import javafx.beans.binding.NumberExpression;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;

import java.math.BigInteger;

/*
 * TODO replace this with proper API when it becomes available:
 * https://javafx-jira.kenai.com/browse/RT-30881
 */
class NumericField extends TextField {

    private final NumericValidator<? extends Number> value ;
    		
    public NumericField( Class<? extends Number> cls ) {
    	
    	if ( cls == byte.class || cls == Byte.class || cls == short.class || cls == Short.class ||
    		 cls ==	int.class  || cls == Integer.class || cls == long.class || cls == Long.class ||
    	     cls == BigInteger.class) {
    		value = new LongValidator(this);
    	} else {
    		value = new DoubleValidator(this);
    	}
    	
        focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				value.setValue(value.toNumber(getText()));
			}
		});
    }
    
    public final ObservableValue<Number> valueProperty() {
        return value;
    }

    @Override public void replaceText(int start, int end, String text) {
        if (replaceValid(start, end, text)) {
            super.replaceText(start, end, text);
        }
    }

    @Override public void replaceSelection(String text) {
        IndexRange range = getSelection();
        if (replaceValid(range.getStart(), range.getEnd(), text)) {
            super.replaceSelection(text);
        }
    }

    private Boolean replaceValid(int start, int end, String fragment) {
        try {
        	String newText = getText().substring(0, start) + fragment + getText().substring(end);
        	if (newText.isEmpty()) return true; 
			value.toNumber(newText);
        	return true;
        } catch( Throwable ex ) {
        	return false;
        }
    }
    
    
    private interface NumericValidator<T extends Number> extends NumberExpression {
    	void setValue(Number num);
    	T toNumber(String s);
    	
    }
    
    static class DoubleValidator extends SimpleDoubleProperty implements NumericValidator<Double>{
    	
    	private NumericField field;
    	
    	public DoubleValidator(NumericField field) {
    		super(field, "value", 0.0); //$NON-NLS-1$
    		this.field = field;
		}
    	
    	@Override protected void invalidated() {
            field.setText(Double.toString(get()));
        }

		@Override
		public Double toNumber(String s) {
			if ( s == null || s.trim().isEmpty() ) return 0d;
	    	String d = s.trim();
	    	if ( d.endsWith("f") || d.endsWith("d") || d.endsWith("F") || d.endsWith("D") ) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    		throw new NumberFormatException("There should be no alpha symbols"); //$NON-NLS-1$
	    	}
	    	return new Double(d);
		};
		
    }
 
    
    static class LongValidator extends SimpleLongProperty implements NumericValidator<Long>{
    	
    	private NumericField field;
    	
    	public LongValidator(NumericField field) {
    		super(field, "value", 0L); //$NON-NLS-1$
    		this.field = field;
		}
    	
    	@Override protected void invalidated() {
            field.setText(Long.toString(get()));
        }

		@Override
		public Long toNumber(String s) {
			if ( s == null || s.trim().isEmpty() ) return 0L;
	    	String d = s.trim();
	    	return new Long(d);
		}
		
    }    
    
    
}