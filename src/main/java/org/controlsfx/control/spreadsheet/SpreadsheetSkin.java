package org.controlsfx.control.spreadsheet;

import javafx.scene.Node;
import javafx.scene.control.Skin;

public class SpreadsheetSkin implements Skin<SpreadsheetView> {
	protected SpreadsheetView spreadsheetView;
	
	public SpreadsheetSkin(SpreadsheetView view) {
		spreadsheetView = view;
	}
	
	@Override public Node getNode() {
		return spreadsheetView.getCellsView();
	}

	@Override public SpreadsheetView getSkinnable() {
		return spreadsheetView;
	}

	@Override public void dispose() {
		// no-op
	}
}