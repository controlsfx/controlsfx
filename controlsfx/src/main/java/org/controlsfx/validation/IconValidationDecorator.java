package org.controlsfx.validation;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class IconValidationDecorator extends GraphicValidationDecorator {
	
	private final Image errorImage;
    private final Image warningImage;
    
    public IconValidationDecorator() {
		this(null,null);
	}
    
    public IconValidationDecorator( Image errorImage, Image warningImage ) {
		this.errorImage   = errorImage != null? 
		   errorImage: new Image("/impl/org/controlsfx/control/validation/decoration-error.png");
		this.warningImage = warningImage != null? 
		   warningImage: new Image("/impl/org/controlsfx/control/validation/decoration-warning.png");
	}
    
    @Override
	protected Node createDecorationNode(ValidationMessage message) {
		ImageView imageView = new ImageView(Severity.ERROR == message.getSeverity()?errorImage:warningImage);
        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
        Label label = new Label();
        label.setGraphic(imageView);
        label.setTooltip( new Tooltip(message.getText()));
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-margin: 0; -fx-padding:0;-fx-border-width:0;-fx-background-color:blue;");
		return label;
	}
    
	@Override
	public String toString() {
		return "Icon Validation Decorator";
	}


}
