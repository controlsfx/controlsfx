package impl.org.controlsfx.behavior;

import org.controlsfx.control.GridCell;

import com.sun.javafx.scene.control.behavior.CellBehaviorBase;

public class GridCellBehavior<T> extends CellBehaviorBase<GridCell<T>> {
    public GridCellBehavior(GridCell<T> control) {
        super(control);
    }
}
