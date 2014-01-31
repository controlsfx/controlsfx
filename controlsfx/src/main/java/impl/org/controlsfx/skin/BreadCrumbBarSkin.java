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
package impl.org.controlsfx.skin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Callback;

import org.controlsfx.control.BreadCrumbBar;
import org.controlsfx.control.BreadCrumbBar.BreadCrumbActionEvent;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

/**
 * Basic Skin implementation for the {@link BreadCrumbBar}
 *
 * @param <T>
 */
public class BreadCrumbBarSkin<T> extends BehaviorSkinBase<BreadCrumbBar<T>, BehaviorBase<BreadCrumbBar<T>>> {
    
    private static final String STYLE_CLASS_FIRST = "first";

    private final HBox layout;

    public BreadCrumbBarSkin(final BreadCrumbBar<T> control) {
        super(control, new BehaviorBase<>(control, Collections.<KeyBinding> emptyList()));

        layout = new HBox();
        getChildren().add(layout);

        control.selectedCrumbProperty().addListener(selectedPathChangeListener);

        updateSelectedPath(getSkinnable().selectedCrumbProperty().get(), null);
    }

    private final ChangeListener<TreeItem<T>> selectedPathChangeListener =
            new ChangeListener<TreeItem<T>>() {
        @Override
        public void changed(
                ObservableValue<? extends TreeItem<T>> obs,
                        TreeItem<T> oldItem,
                        TreeItem<T> newItem) {

            updateSelectedPath(newItem, oldItem);
        }
    };

    private void updateSelectedPath(TreeItem<T> newTarget, TreeItem<T> oldTarget) {

        if(oldTarget != null){
            // remove old listener
            newTarget.removeEventHandler(
                    TreeItem.childrenModificationEvent(), treeChildrenModifiedHandler);
        }
        if(newTarget != null){
            // add new listener
            newTarget.addEventHandler(TreeItem.childrenModificationEvent(), treeChildrenModifiedHandler);
        }
        layoutBreadCrumbs();
    }


    private final EventHandler<TreeModificationEvent<Object>> treeChildrenModifiedHandler =
            new EventHandler<TreeModificationEvent<Object>>(){
        @Override
        public void handle(TreeModificationEvent<Object> args) {
            layoutBreadCrumbs();
        }
    };


    /**
     * Layout the bread crumbs
     */
    private void layoutBreadCrumbs() {
        final BreadCrumbBar<T> buttonBar = getSkinnable();
        final TreeItem<T> pathTarget = buttonBar.getSelectedCrumb();
        final Callback<TreeItem<T>, Button> factory = buttonBar.getCrumbFactory();

        layout.getChildren().clear();

        if(pathTarget != null){
            List<TreeItem<T>> crumbs = constructFlatPath(pathTarget);

            for (int i=0; crumbs.size() > i; i++) {
                Button crumbView = createCrumb(factory, crumbs.get(i));
                
                if (i == 0) {
                    if (crumbView instanceof BreadCrumbButton) {
                        ((BreadCrumbButton)crumbView).setFirst(true);
                    } else {
                        if (! crumbView.getStyleClass().contains(STYLE_CLASS_FIRST)) {
                            crumbView.getStyleClass().add(STYLE_CLASS_FIRST);
                        }
                    }
                } else {
                    crumbView.getStyleClass().remove(STYLE_CLASS_FIRST);
                }

                if(crumbView != null){
                    // We have to position the bread crumbs slightly overlapping
                    // thus we have to create negative Insets
                    double ins = crumbView instanceof BreadCrumbButton ? 
                            ((BreadCrumbButton)crumbView).getArrowWidth() / 2.0 : 0;
                    double right = -ins - 0.1d;
                    double left = !(i==0) ? right : 0; // Omit the first button

                    HBox.setMargin(crumbView, new Insets(0, right, 0, left));
                    layout.getChildren().add(crumbView);
                }
            }
        }
    }

    /**
     * Construct a flat list for the crumbs
     * @param bottomMost The crumb node at the end of the path
     * @return
     */
    private List<TreeItem<T>> constructFlatPath(TreeItem<T> bottomMost){
        List<TreeItem<T>> path = new ArrayList<>();

        TreeItem<T> current = bottomMost;
        do {
            path.add(current);
            current = current.getParent();
        } while (current != null);

        Collections.reverse(path);
        return path;
    }

    private Button createCrumb(
            final Callback<TreeItem<T>, Button> factory,
            final TreeItem<T> selectedCrumb) {

        Button crumb = factory.call(selectedCrumb);

        // We want all buttons to have the same height
        // so we bind their preferred height to the enclosing container
        crumb.prefHeightProperty().bind(layout.heightProperty());

        // listen to the action event of each bread crumb
        crumb.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ae) {
                onBreadCrumbAction(selectedCrumb);
            }
        });

        return crumb;
    }

    /**
     * Occurs when a bread crumb gets the action event
     * 
     * @param crumbModel The crumb which received the action event
     */
    protected void onBreadCrumbAction(final TreeItem<T> crumbModel){
        final BreadCrumbBar<T> breadCrumbBar = getSkinnable();

        // fire the composite event in the breadCrumbBar
        Event.fireEvent(breadCrumbBar, new BreadCrumbActionEvent<T>(crumbModel));

        // navigate to the clicked crumb
        if(breadCrumbBar.isAutoNavigationEnabled()){
            breadCrumbBar.setSelectedCrumb(crumbModel);
        }
    }

    
    
    
    /**
     * Represents a BreadCrumb Button
     * 
     * <pre>
     * ----------
     *  \         \
     *  /         /
     * ----------
     * </pre>
     * 
     * 
     */
    public static class BreadCrumbButton extends Button {

        private final ObjectProperty<Boolean> first = new SimpleObjectProperty<Boolean>(this, "first");

        private final double arrowWidth = 5;
        private final double arrowHeight = 20;

        /**
         * Create a BreadCrumbButton
         * 
         * @param text Buttons text
         * @param first Is this the first / home button?
         */
        public BreadCrumbButton(String text){
            this(text, null);
        }

        /**
         * Create a BreadCrumbButton
         * @param text Buttons text
         * @param gfx Gfx of the Button
         * @param first Is this the first / home button?
         */
        public BreadCrumbButton(String text, Node gfx){
            super(text, gfx);
            first.set(false);

            firstProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> obs, Boolean oldfirst, Boolean newfirst) {
                    updateShape();
                }
            });

            updateShape();
        }

        private void updateShape(){
            this.setShape(createButtonShape(isFirst()));
        }


        /**
         * Gets the crumb arrow with
         * @return
         */
        public double getArrowWidth(){
            return arrowWidth;
        }

        /**
         * Has this button the first flag?
         * @return
         */
        public boolean isFirst() {
            return first.get();
        }

        /**
         * Set this button as the first
         * @param first
         */
        public void setFirst(boolean first) {
            this.first.set(first);
        }

        public ObjectProperty<Boolean> firstProperty(){
            return first;
        }

        /**
         * Create an arrow path
         * 
         * Based upon Uwe / Andy Till code snippet found here:
         * @see http://ustesis.wordpress.com/2013/11/04/implementing-breadcrumbs-in-javafx/
         * @param first
         * @return
         */
        private Path createButtonShape(boolean first){
            // build the following shape (or home without left arrow)

            //   --------
            //  \         \
            //  /         /
            //   --------
            Path path = new Path();

            // begin in the upper left corner
            MoveTo e1 = new MoveTo(0, 0);
            path.getElements().add(e1);

            // draw a horizontal line that defines the width of the shape
            HLineTo e2 = new HLineTo();
            // bind the width of the shape to the width of the button
            e2.xProperty().bind(this.widthProperty().subtract(arrowWidth));
            path.getElements().add(e2);

            // draw upper part of right arrow
            LineTo e3 = new LineTo();
            // the x endpoint of this line depends on the x property of line e2
            e3.xProperty().bind(e2.xProperty().add(arrowWidth));
            e3.setY(arrowHeight / 2.0);
            path.getElements().add(e3);

            // draw lower part of right arrow
            LineTo e4 = new LineTo();
            // the x endpoint of this line depends on the x property of line e2
            e4.xProperty().bind(e2.xProperty());
            e4.setY(arrowHeight);
            path.getElements().add(e4);

            // draw lower horizontal line
            HLineTo e5 = new HLineTo(0);
            path.getElements().add(e5);

            if(!first){
                // draw lower part of left arrow
                // we simply can omit it for the first Button
                LineTo e6 = new LineTo(arrowWidth, arrowHeight / 2.0);
                path.getElements().add(e6);
            }else{
                // draw an arc for the first bread crumb
                ArcTo arcTo = new ArcTo();
                arcTo.setSweepFlag(true);
                arcTo.setX(0);
                arcTo.setY(0);
                arcTo.setRadiusX(15.0f);
                arcTo.setRadiusY(15.0f);
                path.getElements().add(arcTo);
            }

            // close path
            ClosePath e7 = new ClosePath();
            path.getElements().add(e7);
            // this is a dummy color to fill the shape, it won't be visible
            path.setFill(Color.BLACK);
            return path;
        }
    }
}
