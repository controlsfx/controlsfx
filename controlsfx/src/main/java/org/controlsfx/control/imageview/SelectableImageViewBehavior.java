package org.controlsfx.control.imageview;

import java.util.ArrayList;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;

/**
 * The behavior for the {@link SelectableImageView}.
 */
public class SelectableImageViewBehavior extends BehaviorBase<SelectableImageView> {

    /**
     * Creates a new behavior for the specified {@link SelectableImageView}.
     * 
     * @param selectableImageView
     *            the control which this beavior will control
     */
    public SelectableImageViewBehavior(SelectableImageView selectableImageView) {
        super(selectableImageView, new ArrayList<KeyBinding>());
        // TODO Auto-generated constructor stub
    }

}
