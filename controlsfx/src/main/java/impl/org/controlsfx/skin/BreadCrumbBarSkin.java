package impl.org.controlsfx.skin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.layout.HBox;

import org.controlsfx.control.breadcrumbs.BreadCrumbBar;
import org.controlsfx.control.breadcrumbs.BreadCrumbNodeFactory;
import org.controlsfx.control.breadcrumbs.BreadCrumbButton;
import org.controlsfx.control.breadcrumbs.BreadCrumbBar.BreadCrumbActionEvent;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class BreadCrumbBarSkin<T> extends BehaviorSkinBase<BreadCrumbBar<T>, BehaviorBase<BreadCrumbBar<T>>>{

    private final HBox layout;

    public BreadCrumbBarSkin(final BreadCrumbBar<T> control) {
        super(control, new BehaviorBase<>(control, Collections.<KeyBinding> emptyList()));

        layout = new HBox();
        getChildren().add(layout);

        control.pathTargetProperty().addListener(pathTargetChangeListener);

        updatePathTarget(getSkinnable().pathTargetProperty().get(), null);
    }

    /**
     * Occurs when the pathTarget property has changed
     */
    private final ChangeListener<TreeItem<T>> pathTargetChangeListener =
            new ChangeListener<TreeItem<T>>() {
        @Override
        public void changed(
                ObservableValue<? extends TreeItem<T>> obs,
                        TreeItem<T> oldItem,
                        TreeItem<T> newItem) {

            updatePathTarget(newItem, oldItem);
        }
    };

    private void updatePathTarget(TreeItem<T> newTarget, TreeItem<T> oldTarget) {

        if(oldTarget != null){
            // remove old listener
            newTarget.removeEventHandler(TreeItem.childrenModificationEvent(), treeChildrenModifiedHandler);
        }

        if(newTarget != null){
            // add new listener
            newTarget.addEventHandler(TreeItem.childrenModificationEvent(), treeChildrenModifiedHandler);
        }

        layoutBreadCrumbs();
        //getSkinnable().requestLayout();
    }


    private final EventHandler<TreeModificationEvent<Object>> treeChildrenModifiedHandler = new EventHandler<TreeModificationEvent<Object>>(){

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
        final TreeItem<T> pathTarget = buttonBar.getPathTarget();
        final BreadCrumbNodeFactory<T> factory = buttonBar.getCrumbFactory();

        layout.getChildren().clear();

        if(pathTarget != null){
            List<TreeItem<T>> crumbs = constructFlatPath(pathTarget);

            for (int i=0; crumbs.size() > i; i++) {

                BreadCrumbButton item = createCrumb(factory, crumbs.get(i), i);

                if(item != null){
                    // We have to position the bread crumbs slightly overlapping
                    // thus we have to create negative Insets
                    double ins = item.getArrowWidth() / 2.0;
                    double right = -ins - 0.1d;
                    double left = !(i==0) ? right : 0; // Omit the first button

                    HBox.setMargin(item, new Insets(0, right, 0, left));
                    layout.getChildren().add(item);
                }
            }
        }

    }

    private List<TreeItem<T>> constructFlatPath(TreeItem<T> bottomMost){
        // construct a flat list for the crumbs
        List<TreeItem<T>> path = new ArrayList<>();

        TreeItem<T> current = bottomMost;
        do {
            path.add(current);
            current = current.getParent();
        } while (current != null);

        Collections.reverse(path);
        return path;
    }

    private BreadCrumbButton createCrumb(final BreadCrumbNodeFactory<T> factory, final TreeItem<T> t, final int i) {
        BreadCrumbButton crumb = factory.createBreadCrumbButton(t, i);

        // We want all buttons to have the same height
        // so we bind their preferred height to the enclosing container
        crumb.prefHeightProperty().bind(layout.heightProperty());


        // listen to the action event of each bread crumb

        crumb.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ae) {
                final BreadCrumbBar<T> buttonBar = getSkinnable();
                buttonBar.fireEvent(new BreadCrumbActionEvent<T>(t));
            }
        });

        return crumb;
    }

}
