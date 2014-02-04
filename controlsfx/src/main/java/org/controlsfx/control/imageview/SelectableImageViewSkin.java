package org.controlsfx.control.imageview;

import javafx.scene.image.ImageView;

import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class SelectableImageViewSkin extends BehaviorSkinBase<SelectableImageView, SelectableImageViewBehavior> {

    /**
     * The image view which displays the image.
     */
    private final ImageView imageView;

    public SelectableImageViewSkin(SelectableImageView selectableImageView) {
        super(selectableImageView, new SelectableImageViewBehavior(selectableImageView));

        this.imageView = new ImageView();
        bindPropertiesToImageView();

        getChildren().add(imageView);
    }

    /**
     * Binds some of the {@code SelectableImageView}'s properties to those of the {@link #imageView image view}.
     */
    private void bindPropertiesToImageView() {
        SelectableImageView selectableImageView = getSkinnable();
        imageView.fitHeightProperty().bindBidirectional(selectableImageView.fitHeightProperty());
        imageView.fitWidthProperty().bindBidirectional(selectableImageView.fitWidthProperty());
        imageView.imageProperty().bindBidirectional(selectableImageView.imageProperty());
    }

}
