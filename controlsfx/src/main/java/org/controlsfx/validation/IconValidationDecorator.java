/**
 * Copyright (c) 2014, ControlsFX
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
package org.controlsfx.validation;

import java.util.Arrays;
import java.util.Collection;

import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.control.decoration.GraphicDecoration;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Validation decorator to decorate validation state using images 
 */
public class IconValidationDecorator implements ValidationDecorator {
	
	private final Image errorImage;
    private final Image warningImage;
    
    /**
     * Creates default instance
     */
    public IconValidationDecorator() {
		this(null,null);
	}
    
    /**
     * Creates an instance using custom images to decorate validation states
     * @param errorImage error image, if null default image is used
     * @param warningImage warning image, if null default image is used
     */
    public IconValidationDecorator( Image errorImage, Image warningImage ) {
		this.errorImage   = errorImage != null? 
		   errorImage: new Image("/impl/org/controlsfx/control/validation/decoration-error.png");
		this.warningImage = warningImage != null? 
		   warningImage: new Image("/impl/org/controlsfx/control/validation/decoration-warning.png");
	}
    
  
	protected Node createDecorationNode(ValidationMessage message) {
		ImageView imageView = new ImageView(Severity.ERROR == message.getSeverity()?errorImage:warningImage);
        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
        Label label = new Label();
        label.setGraphic(imageView);
		label.setTooltip( createTooltip(message));
        label.setAlignment(Pos.CENTER);
		return label;
	}
	
	protected Tooltip createTooltip(ValidationMessage message) {
		Tooltip tooltip = new Tooltip(message.getText());
        tooltip.setOpacity(.9);
        tooltip.setAutoFix(true);
        tooltip.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"
        		       + "-fx-background-color: FBEFEF; -fx-text-fill: cc0033;"
        		       + "-fx-font-weight: bold; -fx-padding: 5;" 
        		       + "-fx-border-width:1; -fx-border-color:cc0033;");
        return tooltip;
	}
    
	  /**
	   * {@inheritDoc}
	   */
    @Override
	public final Collection<? extends Decoration> createDecorations(ValidationMessage message) {
		return Arrays.asList(new GraphicDecoration(createDecorationNode(message),Pos.BOTTOM_LEFT));
	}
    
	@Override
	public String toString() {
		return "Icon Validation Decorator";
	}


}
