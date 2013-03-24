package impl.org.controlsfx.skin;

import java.lang.ref.WeakReference;
import java.util.Vector;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridRow;

import com.sun.javafx.scene.control.skin.VirtualFlow;

public class GridCellCache<T> {

    private Vector<WeakReference<GridCell<T>>> cells;

    public GridCellCache(final GridViewSkin<T> gridViewSkin, VirtualFlow<GridRow<T>> virtualFlow) {
        cells = new Vector<>();

        // TODO: Currently all Cells are cached. I would be better to cache only
        // the cells above and under the current viewport. To do so the
        // virtualFlow needs properties for this that can be bound. <- Ask
        // Jonathan / compare with JDK8

        final ListChangeListener<T> listListener = new ListChangeListener<T>() {

            @Override public void onChanged(javafx.collections.ListChangeListener.Change<? extends T> arg0) {
                cells.setSize(gridViewSkin.getSkinnable().itemsProperty().get().size());
            }

        };

        gridViewSkin.getSkinnable().itemsProperty().addListener(new ChangeListener<ObservableList<T>>() {

            @Override public void changed(ObservableValue<? extends ObservableList<T>> observableValue,
                    ObservableList<T> oldValue, ObservableList<T> newValue) {
                if (oldValue != null) {
                    oldValue.removeListener(listListener);
                }
                if (newValue != null) {
                    newValue.addListener(listListener);
                }
            }

        });

        if (gridViewSkin.getSkinnable().itemsProperty().get() != null) {
            gridViewSkin.getSkinnable().itemsProperty().get().addListener(listListener);
            cells.setSize(gridViewSkin.getSkinnable().itemsProperty().get().size());
        }
    }

    public GridCell<T> getCellIfCached(int index) {
        if (cells.size() > index) {
            WeakReference<GridCell<T>> weakRef = cells.get(index);
            if (weakRef != null) {
                return weakRef.get();
            }
        }
        return null;
    }

    public void addCellToCache(int index, GridCell<T> cell) {
        cells.set(index, new WeakReference<GridCell<T>>(cell));
    }

    public void clear() {
        int oldSize = cells.size();
        cells.clear();
        cells.setSize(oldSize);
    }
}
