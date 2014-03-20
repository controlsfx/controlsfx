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
package org.controlsfx.control.cell;

import javafx.scene.control.ContentDisplay;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

/**
 * A {@link GridCell} that can be used to show coloured rectangles inside the 
 * {@link GridView} control.
 *
 * @see GridView
 */
public class ColorGridCell extends GridCell<Color> {
	
	private Rectangle colorRect;
	
	private static final boolean debug = false;

	/**
	 * Creates a default ColorGridCell instance.
	 */
    public ColorGridCell() {
		getStyleClass().add("color-grid-cell"); //$NON-NLS-1$
		
		colorRect = new Rectangle();
		colorRect.setStroke(Color.BLACK);
		colorRect.heightProperty().bind(heightProperty());
		colorRect.widthProperty().bind(widthProperty());   
		setGraphic(colorRect);
		
		if (debug) {
		    setContentDisplay(ContentDisplay.TEXT_ONLY);
		}
	}
	
    /**
     * {@inheritDoc}
     */
	@Override protected void updateItem(Color item, boolean empty) {
	    super.updateItem(item, empty);
	    
	    if (empty) {
	        setGraphic(null);
	    } else {
	        colorRect.setFill(item);
	        setGraphic(colorRect);
	    }
	    
	    if (debug) {
	        setText(getIndex() + ""); //$NON-NLS-1$
	    }
	}
}
