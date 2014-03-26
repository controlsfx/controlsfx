package org.controlsfx.validation;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.control.decoration.GraphicDecoration;

public class IconValidationDecorator implements ValidationDecorator {
	
	private static Image errorIcon   = new Image("/impl/org/controlsfx/control/validation/decoration-error.png");
    private static Image warningIcon = new Image("/impl/org/controlsfx/control/validation/decoration-warning.png");
    
    private Node createImageNode( ValidationMessage msg ) {
        ImageView imageView = new ImageView(Severity.ERROR == msg.getSeverity()?errorIcon:warningIcon);
        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
        Label label = new Label();
        label.setGraphic(imageView);
        label.setTooltip( new Tooltip(msg.getText()));
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-margin: 0; -fx-padding:0;-fx-border-width:0;-fx-background-color:blue;");
        return label;
    }

	@Override
	public Decoration createDecoration(ValidationMessage message) {
		return new GraphicDecoration(createImageNode(message),Pos.BOTTOM_LEFT);
	}

}
