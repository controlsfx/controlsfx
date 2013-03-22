package impl.org.controlsfx.skin;

import org.controlsfx.control.GridCell;

import com.sun.javafx.scene.control.behavior.CellBehaviorBase;
import com.sun.javafx.scene.control.skin.CellSkinBase;

public class GridCellSkin<T> extends CellSkinBase<GridCell<T>, CellBehaviorBase<GridCell<T>>> {

    public GridCellSkin(GridCell<T> control) {
        super(control, new CellBehaviorBase<>(control));
    }

}
