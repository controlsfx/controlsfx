package impl.org.controlsfx.spreadsheet;

import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Region;

import org.controlsfx.control.spreadsheet.SpreadsheetCell;

import com.sun.javafx.scene.control.skin.TableCellSkin;

public class CellViewSkin extends TableCellSkin<ObservableList<SpreadsheetCell>, SpreadsheetCell>{

	private static final  int TRIANGLE_SIZE = 6;
	private Region commentTriangle = null;
	
	public CellViewSkin(
			TableCell<ObservableList<SpreadsheetCell>, SpreadsheetCell> arg0) {
		super(arg0);
	}
	
	@Override
	protected void layoutChildren(double x, final double y, final double w,
            final double h) {
		super.layoutChildren(x, y, w, h);
		
		if(getSkinnable().getItem() != null){
			if( getSkinnable().getItem().isCommented()){
				if(commentTriangle == null){
					commentTriangle = new Region();
				}
				if(!getChildren().contains(commentTriangle)){
					getChildren().add(commentTriangle);
				}
				commentTriangle.resize(TRIANGLE_SIZE,TRIANGLE_SIZE);
				commentTriangle.getStyleClass().add("comment");
				commentTriangle.relocate(getSkinnable().getWidth()-TRIANGLE_SIZE, snappedTopInset()-1);
			}else if(commentTriangle != null){
				getChildren().remove(commentTriangle);
				commentTriangle = null;
			}
		}
	}
}
