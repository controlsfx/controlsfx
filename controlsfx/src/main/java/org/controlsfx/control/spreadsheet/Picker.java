/**
 * Copyright (c) 2014, 2015, ControlsFX
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
package org.controlsfx.control.spreadsheet;

import java.util.Collection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * Pickers can display some Images next to the headers. <br>
 * You can specify the image by providing custom StyleClass :<br>
 * 
 * <pre>
 * .picker-label{
 *   -fx-graphic: url("add.png"); 
 *   -fx-background-color: white;
 *   -fx-padding: 0 0 0 0;
 * }
 * </pre>
 * 
 * The {@link #onClick() } method does nothing by default, so you can override it
 * if you want to execute a custom action when the user will click on your Picker.
 * 
 * <h3>Visual:</h3> <center><img src="pickers.PNG" alt="Screenshot of Picker"></center>
 * 
 */
public abstract class Picker {

    private final ObservableList<String> styleClass = FXCollections.observableArrayList();

    /**
     * Default constructor, the default "picker-label" styleClass is applied.
     */
    public Picker() {
        this("picker-label"); //$NON-NLS-1$
    }

    /**
     * Initialize this Picker with the style classes provided.
     * @param styleClass 
     */
    public Picker(String... styleClass) {
        this.styleClass.addAll(styleClass);
    }
    
    /**
     * Initialize this Picker with the style classes provided.
     * @param styleClass 
     */
    public Picker(Collection<String> styleClass) {
        this.styleClass.addAll(styleClass);
    }


    /**
     * @return the style class of this picker.
     */
    public final ObservableList<String> getStyleClass() {
        return styleClass;
    }

    /**
     * This method will be called whenever the user clicks on this picker.
     */
    public abstract void onClick();
}
