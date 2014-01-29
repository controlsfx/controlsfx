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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import org.controlsfx.control.breadcrumbs.BreadCrumbBar;
import org.controlsfx.control.breadcrumbs.BreadCrumbButton;
import org.controlsfx.control.breadcrumbs.BreadCrumbBar.BreadCrumbActionEvent;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

/**
 * Basic Skin implementation for the {@link BreadCrumbBar}
 *
 * @param <T>
 */
public class BreadCrumbBarSkin<T> extends BehaviorSkinBase<BreadCrumbBar<T>, BehaviorBase<BreadCrumbBar<T>>>{

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
        final Callback<TreeItem<T>, BreadCrumbButton> factory = buttonBar.getCrumbFactory();

        layout.getChildren().clear();

        if(pathTarget != null){
            List<TreeItem<T>> crumbs = constructFlatPath(pathTarget);

            for (int i=0; crumbs.size() > i; i++) {

                BreadCrumbButton crumbView = createCrumb(factory, crumbs.get(i));
                if(i == 0) crumbView.setFirst(true);

                if(crumbView != null){
                    // We have to position the bread crumbs slightly overlapping
                    // thus we have to create negative Insets
                    double ins = crumbView.getArrowWidth() / 2.0;
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

    private BreadCrumbButton createCrumb(
            final Callback<TreeItem<T>, BreadCrumbButton> factory,
            final TreeItem<T> crumbModel) {

        BreadCrumbButton crumb = factory.call(crumbModel);

        // We want all buttons to have the same height
        // so we bind their preferred height to the enclosing container
        crumb.prefHeightProperty().bind(layout.heightProperty());


        // listen to the action event of each bread crumb
        crumb.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ae) {
                onBreadCrumbAction(crumbModel);
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

}
