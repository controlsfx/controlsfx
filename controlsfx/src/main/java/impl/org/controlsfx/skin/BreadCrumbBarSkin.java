package impl.org.controlsfx.skin;

import java.util.Collections;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

import org.controlsfx.control.breadcrumbs.BreadCrumbBar;
import org.controlsfx.control.breadcrumbs.BreadCrumbNodeFactory;
import org.controlsfx.control.breadcrumbs.IBreadCrumbModel;
import org.controlsfx.control.breadcrumbs.BreadCrumbButton;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class BreadCrumbBarSkin<T extends IBreadCrumbModel> extends BehaviorSkinBase<BreadCrumbBar<T>, BehaviorBase<BreadCrumbBar<T>>>{

	private final HBox layout;

	public BreadCrumbBarSkin(final BreadCrumbBar<T> control) {
		super(control, new BehaviorBase<>(control, Collections.<KeyBinding> emptyList()));

		layout = new HBox();
		getChildren().add(layout);

		registerChangeListener(control.crumbsProperty(), "CRUMBS");

		updateCrumbItems();
	}

	@Override 
	protected void handleControlPropertyChanged(String p) {
		super.handleControlPropertyChanged(p);
		if (p == "CRUMBS") {
			updateCrumbItems();
		}
	}


	private final ListChangeListener<T> crumbsListener = new ListChangeListener<T>() {
		@Override public void onChanged(ListChangeListener.Change<? extends T> change) {

			layoutBreadCrumbs();
			getSkinnable().requestLayout();
		}
	};   

	private final WeakListChangeListener<T> weakCrumbItemsListener = new WeakListChangeListener<T>(crumbsListener);

	private void updateCrumbItems() {
		if (getSkinnable().getCrumbs() != null) {
			getSkinnable().getCrumbs().removeListener(weakCrumbItemsListener);
		}

		if (getSkinnable().getCrumbs() != null) {
			getSkinnable().getCrumbs().addListener(weakCrumbItemsListener);
		}

		layoutBreadCrumbs();
		getSkinnable().requestLayout();
	}

	/**
	 * Layout the bread crumbs
	 */
	private void layoutBreadCrumbs() {
		final BreadCrumbBar<T> buttonBar = getSkinnable();
		final ObservableList<T> crumbs = buttonBar.getCrumbs();
		final BreadCrumbNodeFactory<T> factory = buttonBar.getCrumbFactory();

		layout.getChildren().clear();

		if(crumbs != null){
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

	private BreadCrumbButton createCrumb(final BreadCrumbNodeFactory<T> factory, final T t, final int i) {
		BreadCrumbButton crumb = factory.createBreadCrumbButton(t, i);

		// We want all buttons to have the same height
		// so we bind their preferred height to the enclosing container
		crumb.prefHeightProperty().bind(layout.heightProperty());


		// listen to the action event of each bread crumb

		crumb.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent ae) {
				final BreadCrumbBar<T> buttonBar = getSkinnable();
				t.activated();
				buttonBar.fireBreadCrumbAction(t);
			}
		});

		return crumb;
	}

}
