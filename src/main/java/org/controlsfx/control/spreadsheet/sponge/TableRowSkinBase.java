/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.controlsfx.control.spreadsheet.sponge;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javafx.animation.FadeTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.util.Duration;

import org.controlsfx.control.spreadsheet.control.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.control.SpreadsheetRow;
import org.controlsfx.control.spreadsheet.control.SpreadsheetView;
import org.controlsfx.control.spreadsheet.control.SpreadsheetView.SpreadsheetViewSelectionModel;
import org.controlsfx.control.spreadsheet.model.DataCell;
import org.controlsfx.control.spreadsheet.model.DataRow;

import com.sun.javafx.scene.control.behavior.CellBehaviorBase;
import com.sun.javafx.scene.control.behavior.TableRowBehavior;
import com.sun.javafx.scene.control.skin.CellSkinBase;
import com.sun.javafx.tk.Toolkit;

public abstract class TableRowSkinBase<T,
C extends IndexedCell/*<T>*/,
B extends CellBehaviorBase<C>,
R extends IndexedCell>
extends CellSkinBase<TableRow<DataRow>,TableRowBehavior<DataRow>> {

	static final double DEFAULT_CELL_SIZE = 24.0;

	/*
	 * This is rather hacky - but it is a quick workaround to resolve the
	 * issue that we don't know maximum width of a disclosure node for a given
	 * TreeView. If we don't know the maximum width, we have no way to ensure
	 * consistent indentation for a given TreeView.
	 *
	 * To work around this, we create a single WeakHashMap to store a max
	 * disclosureNode width per TreeView. We use WeakHashMap to help prevent
	 * any memory leaks.
	 */
	static final Map<Control, Double> maxDisclosureWidthMap = new WeakHashMap<Control, Double>();

	protected SpreadsheetView spreadsheetView;
	private double prefWidth = -1;

	protected int getIndentationLevel(TableRow<DataRow> control) {
		// TreeTableView.getNodeLevel(control.getTreeTable)
		return 0;
	}

	protected double getIndentationPerLevel() {
		return 0;
	}

	/**
	 * Used to represent whether the current virtual flow owner is wanting
	 * indentation to be used in this table row.
	 */
	protected boolean isIndentationRequired() {
		return false;
	}

	/**
	 * Returns the table column that should show the disclosure nodes and / or
	 * a graphic. By default this is the left-most column.
	 */
	protected TableColumnBase getTreeColumn() {
		return null;
	}

	protected Node getDisclosureNode() {
		return null;
	}

	/**
	 * Used to represent whether a disclosure node is visible for _this_
	 * table row. Not to be confused with isIndentationRequired(), which is the
	 * more general API.
	 */
	protected boolean isDisclosureNodeVisible() {
		//		return disclosureNode != null && treeItem != null && ! treeItem.isLeaf();
		return false;
	}

	protected boolean isShowRoot() {
		return true;
	}

	/**
	 * Returns the graphic to draw on the inside of the disclosure node. Null
	 * is acceptable when no graphic should be shown. Commonly this is the
	 * graphic associated with a TreeItem (i.e. treeItem.getGraphic()), rather
	 * than a graphic associated with a cell.
	 */
	//    protected abstract Node getGraphic();
	protected abstract ObjectProperty<Node> graphicProperty();

	protected abstract Control getVirtualFlowOwner(); // return TableView / TreeTableView

	protected abstract ObservableList<? extends TableColumnBase/*<DataRow,?>*/> getVisibleLeafColumns();
	//    protected abstract ObjectProperty<SpanModel<DataRow>> spanModelProperty();

	protected abstract void updateCell(SpreadsheetCell cell, TableRow<DataRow> row);  // cell.updateTableRow(skinnable); (i.e cell.updateTableRow(row))

	protected abstract DoubleProperty fixedCellSizeProperty();

	protected abstract boolean isColumnPartiallyOrFullyVisible(TableColumnBase tc); // tableViewSkin.isColumnPartiallyOrFullyVisible(tc)

	protected abstract SpreadsheetCell getCell(TableColumnBase tc);

	protected abstract TableColumnBase<DataRow,?> getTableColumnBase(SpreadsheetCell cell);

	protected TableColumnBase<DataRow,?> getVisibleLeafColumn(int column) {
		final List<? extends TableColumnBase/*<DataRow,?>*/> visibleLeafColumns = getVisibleLeafColumns();
		if (column < 0 || column >= visibleLeafColumns.size()) {
			return null;
		}
		return visibleLeafColumns.get(column);
	}

	// Specifies the number of times we will call 'recreateCells()' before we blow
	// out the cellsMap structure and rebuild all cells. This helps to prevent
	// against memory leaks in certain extreme circumstances.
	private static final int DEFAULT_FULL_REFRESH_COUNTER = 100;

	/*
	 * A map that maps from TableColumn to TableCell (i.e. model to view).
	 * This is recreated whenever the leaf columns change, however to increase
	 * efficiency we create cells for all columns, even if they aren't visible,
	 * and we only create new cells if we don't already have it cached in this
	 * map.
	 *
	 * Note that this means that it is possible for this map to therefore be
	 * a memory leak if an application uses TableView and is creating and removing
	 * a large number of tableColumns. This is mitigated in the recreateCells()
	 * function below - refer to that to learn more.
	 */
	protected WeakHashMap<TableColumnBase, SpreadsheetCell> cellsMap;

	// This observableArrayList contains the currently visible table cells for this row.
	protected final List<SpreadsheetCell> cells = new ArrayList<SpreadsheetCell>();

	private int fullRefreshCounter = DEFAULT_FULL_REFRESH_COUNTER;

	protected boolean isDirty = false;
	protected boolean updateCells = false;

	private double fixedCellSize;
	private boolean fixedCellSizeEnabled;

	public void requestLayout(){
		getSkinnable().requestLayout();
	}
	private final ListChangeListener<TableColumnBase> visibleLeafColumnsListener = new ListChangeListener<TableColumnBase>() {
		@Override public void onChanged(Change<? extends TableColumnBase> c) {
			isDirty = true;
			getSkinnable().requestLayout();
		}
	};

	private final WeakListChangeListener<TableColumnBase> weakVisibleLeafColumnsListener =
			new WeakListChangeListener<TableColumnBase>(visibleLeafColumnsListener);


	public TableRowSkinBase(TableRow<DataRow> tableRow, TableRowBehavior<DataRow> tableRowBehavior,SpreadsheetView spreadsheetView) {
		super(tableRow, tableRowBehavior);
		this.spreadsheetView = spreadsheetView;
		// init(control) should not be called here - it should be called by the
		// subclass after initialising itself. This is to prevent NPEs (for
		// example, getVisibleLeafColumns() throws a NPE as the control itself
		// is not yet set in subclasses).
	}

	protected void init(TableRow<DataRow> tableRow) {
		getSkinnable().setPickOnBounds(false);

		recreateCells();
		updateCells(true);

		/**
		 * NOT NEEDED BECAUSE THEN RE LAYOUT ON EVERY MOVE OF
		 * THE HORIZONTAL SCROLL BAR BUT MAY SERVE AGAIN
		 */
		// We listen to the Hbar Value change and then re-layout
		/*if(!spreadsheetView.getFlow().getFixedColumns().isEmpty()){
			spreadsheetView.getFlow().getHbar().valueProperty().addListener(new ChangeListener<Number>(){
				@Override
				public void changed(ObservableValue<? extends Number> observable,
						Number oldValue, Number newValue) {
					((SpreadsheetRow)getSkinnable()).setLayoutFixedColumns(true);
					getSkinnable().requestLayout();
				}
			});
		}*/


		// init bindings
		// watches for any change in the leaf columns observableArrayList - this will indicate
		// that the column order has changed and that we should update the row
		// such that the cells are in the new order
		getVisibleLeafColumns().addListener(weakVisibleLeafColumnsListener);
		// --- end init bindings

		//        registerChangeListener(control.textProperty(), "TEXT");
		//        registerChangeListener(control.graphicProperty(), "GRAPHIC");
		//        registerChangeListener(control.editingProperty(), "EDITING");
		registerChangeListener(tableRow.itemProperty(), "ITEM");

		if (fixedCellSizeProperty() != null) {
			registerChangeListener(fixedCellSizeProperty(), "FIXED_CELL_SIZE");
			fixedCellSize = fixedCellSizeProperty().get();
			fixedCellSizeEnabled = fixedCellSize > 0;
		}

		//        // add listener to cell span model
		//        spanModel = spanModelProperty().get();
		//        registerChangeListener(spanModelProperty(), "SPAN_MODEL");
	}

	@Override protected void handleControlPropertyChanged(String p) {
		//        // we run this before the super call because we want to update whether
		//        // we are showing columns or the node (if it isn't null) before the
		//        // parent class updates the content
		//        if ("TEXT".equals(p) || "GRAPHIC".equals(p) || "EDITING".equals(p)) {
		//            updateShowColumns();
		//        }

		super.handleControlPropertyChanged(p);

		if ("ITEM".equals(p)) {
			updateCells = true;
			getSkinnable().requestLayout();
			//        } else if (p == "SPAN_MODEL") {
			//            // TODO update layout based on changes to span model
			//            spanModel = spanModelProperty().get();
			//            getSkinnable().requestLayout();
		} else if ("FIXED_CELL_SIZE".equals(p)) {
			fixedCellSize = fixedCellSizeProperty().get();
			fixedCellSizeEnabled = fixedCellSize > 0;

		}
	}

	@Override protected void layoutChildren(double x, final double y,
			final double w, final double h) {


		checkState(true);
		if (cellsMap.isEmpty()) {
			return;
		}

		final ObservableList<? extends TableColumnBase> visibleLeafColumns = getVisibleLeafColumns();
		if (visibleLeafColumns.isEmpty()) {
			super.layoutChildren(x,y,w,h);
			return;
		}

		final TableRow<DataRow> control = getSkinnable();

		///////////////////////////////////////////
		// indentation code starts here
		///////////////////////////////////////////
		double leftMargin = 0;
		double disclosureWidth = 0;
		double graphicWidth = 0;
		final boolean indentationRequired = isIndentationRequired();
		final boolean disclosureVisible = isDisclosureNodeVisible();
		int indentationColumnIndex = 0;
		Node disclosureNode = null;
		if (indentationRequired) {
			// Determine the column in which we want to put the disclosure node.
			// By default it is null, which means the 0th column should be
			// where the indentation occurs.
			final TableColumnBase<?,?> treeColumn = getTreeColumn();
			indentationColumnIndex = treeColumn == null ? 0 : visibleLeafColumns.indexOf(treeColumn);
			indentationColumnIndex = indentationColumnIndex < 0 ? 0 : indentationColumnIndex;

			int indentationLevel = getIndentationLevel(control);
			if (! isShowRoot()) {
				indentationLevel--;
			}
			final double indentationPerLevel = getIndentationPerLevel();
			leftMargin = indentationLevel * indentationPerLevel;

			// position the disclosure node so that it is at the proper indent
			final Control c = getVirtualFlowOwner();
			final double defaultDisclosureWidth = maxDisclosureWidthMap.containsKey(c) ?
					maxDisclosureWidthMap.get(c) : 0;
					disclosureWidth = defaultDisclosureWidth;

					disclosureNode = getDisclosureNode();
					if (disclosureNode != null) {
						disclosureNode.setVisible(disclosureVisible);

						if (disclosureVisible) {
							disclosureWidth = disclosureNode.prefWidth(h);
							if (disclosureWidth > defaultDisclosureWidth) {
								maxDisclosureWidthMap.put(c, disclosureWidth);
							}
						}
					}
		}
		///////////////////////////////////////////
		// indentation code ends here
		///////////////////////////////////////////

		// layout the individual column cells
		double width;
		double height;

		final double verticalPadding = snappedTopInset() + snappedBottomInset();
		final double horizontalPadding = snappedLeftInset() + snappedRightInset();
		final double controlHeight = control.getHeight();

		/**
		 * RT-26743:TreeTableView: Vertical Line looks unfinished.
		 * We used to not do layout on cells whose row exceeded the number
		 * of items, but now we do so as to ensure we get vertical lines
		 * where expected in cases where the vertical height exceeds the
		 * number of items.
		 */
		final int index = control.getIndex();
		if (index < 0 || index >= spreadsheetView.getItems().size()) {
			return;
		}

		/**
		 * FOR FIXED ROWS
		 */
		double tableCellY = 0;
		int positionY;
		if((positionY = spreadsheetView.getFixedRows().indexOf(index)) != -1 ){// if true, this row is fixed
			if(getSkinnable().getLocalToParentTransform().getTy() <0){ // this rows is a bit hidden on top
				// We translate then for it to be fully visible
				tableCellY = Math.abs(getSkinnable().getLocalToParentTransform().getTy());
			}else{
				//The rows is not hidden but we need to translate it anyways because it will be covered
				// by the previous fixed rows otherwise
				tableCellY = positionY*DEFAULT_CELL_SIZE - getSkinnable().getLocalToParentTransform().getTy() ;
			}
		}


		/**
		 * FOR FIXED COLUMN
		 */
		//If we called layoutChildren just to re-layout the fixed columns
		final int max = ((SpreadsheetRow)getSkinnable()).getLayoutFixedColumns()? spreadsheetView.getFixedColumns().size(): cells.size();

		//In case we were doing layout only of the fixed columns
		((SpreadsheetRow)getSkinnable()).setLayoutFixedColumns(false);

		//		System.out.println("Je layout"+index+"/"+((SpreadsheetRow)getSkinnable()).getIndexVirtualFlow() );
		for (int column = 0; column < max; column++) {
						
			final SpreadsheetCell tableCell = cells.get(column);
			final TableColumnBase<DataRow, ?> tableColumn = getTableColumnBase(tableCell);

			//show(tableCell);

			//In case the node was treated previously
			tableCell.setOpacity(100);

			width = snapSize(tableCell.prefWidth(-1)) - snapSize(horizontalPadding);
			height = Math.max(controlHeight, tableCell.prefHeight(-1));
			height = snapSize(height) - snapSize(verticalPadding);

			/**
			 * FOR FIXED COLUMNS
			 */
			double tableCellX = 0;
			int indexColumn = 0;
			final double hbarValue = spreadsheetView.getHbar().getValue();
			//We translate that column by the Hbar Value if it's fixed
			if((indexColumn = spreadsheetView.getFixedColumns().indexOf(column)) != -1){
				/*if(hbarValue - fixedCellSize*(column-indexColumn) >0){
					tableCellX = Math.abs(hbarValue - tableCell.getWidth()*(column-indexColumn));
				}*/
				tableCellX = Math.abs(hbarValue);
				tableCell.toFront();
			}



			boolean isVisible = true;
			if (fixedCellSizeEnabled) {
				// we determine if the cell is visible, and if not we have the
				// ability to take it out of the scenegraph to help improve
				// performance. However, we only do this when there is a
				// fixed cell length specified in the TableView. This is because
				// when we have a fixed cell length it is possible to know with
				// certainty the height of each TableCell - it is the fixed value
				// provided by the developer, and this means that we do not have
				// to concern ourselves with the possibility that the height
				// may be variable and / or dynamic.
				isVisible = isColumnPartiallyOrFullyVisible(tableColumn);
			}

			if (isVisible) {
				if (fixedCellSizeEnabled && tableCell.getParent() == null) {
					getChildren().add(tableCell);
				}
//				System.out.println("Je layout"+index+"/"+column );



				///////////////////////////////////////////
				// further indentation code starts here
				///////////////////////////////////////////
				if (indentationRequired && column == indentationColumnIndex) {
					if (disclosureVisible) {
						final double ph = disclosureNode.prefHeight(disclosureWidth);

						if (width < disclosureWidth + leftMargin) {
							fadeOut(disclosureNode);
						} else {
							fadeIn(disclosureNode);
							disclosureNode.resize(disclosureWidth, ph);
							positionInArea(disclosureNode, x + leftMargin, y,
									disclosureWidth, h, /*baseline ignored*/0,
									HPos.CENTER, VPos.CENTER);
							disclosureNode.toFront();
						}
					}

					// determine starting point of the graphic or cell node, and the
					// remaining width available to them
					final ObjectProperty<Node> graphicProperty = graphicProperty();
					final Node graphic = graphicProperty == null ? null : graphicProperty.get();

					if (graphic != null) {
						graphicWidth = graphic.prefWidth(-1) + 3;

						if (width < disclosureWidth + leftMargin + graphicWidth) {
							fadeOut(graphic);
						} else {
							fadeIn(graphic);
							positionInArea(graphic, x + leftMargin + disclosureWidth, y,
									graphicWidth, h, /*baseline ignored*/0,
									HPos.CENTER, VPos.CENTER);
							graphic.toFront();
						}
					}
				}
				///////////////////////////////////////////
				// further indentation code ends here
				///////////////////////////////////////////

				final DataCell cellSpan = ((DataRow)spreadsheetView.getItems().get(index)).getCell(column);
				final SpreadsheetView.SpanType spanType = spreadsheetView.getSpanType(index, column);

				switch (spanType) {
				case ROW_INVISIBLE:
				case BOTH_INVISIBLE:
					tableCell.setOpacity(0);
					tableCell.resize(width, height);
					tableCell.relocate(x+tableCellX, snappedTopInset()+ tableCellY);

					x += width;
					continue;       // we don't want to fall through
				case COLUMN_INVISIBLE:
					tableCell.setOpacity(0);
					tableCell.resize(width, height);
					tableCell.relocate(x+tableCellX, snappedTopInset()+ tableCellY);
					continue;          // we don't want to fall through
					// infact, we return to the loop here
				case ROW_VISIBLE:
					// To be sure that the text is the same
					//in case we modified the DataCell after that SpreadsheetCell was created
					final SpreadsheetViewSelectionModel sm = spreadsheetView.getSelectionModel();
					final TableColumn col = spreadsheetView.getColumns().get(column);

					//In case this cell was selected before but we scroll up/down and it's invisible now.
					// It has to pass his "selected property" to the new Cell in charge of spanning
					final TablePosition selectedPosition = sm.isSelectedRange(index, col, column);
					if (selectedPosition != null && selectedPosition.getRow() != index) { // If the selected cell is in the same row, no need to re-select it
						sm.clearSelection(selectedPosition.getRow(), selectedPosition.getTableColumn());
						sm.select(index, col);
					}
				case NORMAL_CELL:  // fall through and carry on
					tableCell.show();
				}


				if (cellSpan != null) {
					if (cellSpan.getColumnSpan() > 1) {
						// we need to span multiple columns, so we sum up
						// the width of the additional columns, adding it
						// to the width variable
						for (int i = 1,
								colSpan = cellSpan.getColumnSpan(),
								max1 = getChildren().size() - column;
								i < colSpan && i < max1; i++) {
							// calculate the width
							final Node adjacentNode = getChildren().get(column + i);
							width += snapSize(adjacentNode.prefWidth(-1));
						}
					}

					if (cellSpan.getRowSpan() > 1) {
						// we need to span multiple rows, so we sum up
						// the height of the additional rows, adding it
						// to the height variable
						for (int i = 1; i < cellSpan.getRowSpan(); i++) {
							// calculate the height
							final double rowHeight = DEFAULT_CELL_SIZE;//getTableRowHeight(index + i, getSkinnable());
							height += snapSize(rowHeight);
						}
					}
				}

				tableCell.resize(width, height);
				// We want to place the layout always at the starting cell.
				final double spaceBetweenTopAndMe = (index - cellSpan.getRow()) * DEFAULT_CELL_SIZE;
				tableCell.relocate(x+tableCellX,snappedTopInset() - spaceBetweenTopAndMe + tableCellY);

				// Request layout is here as (partial) fix for RT-28684
				//tableCell.requestLayout();
			} else {
				if (fixedCellSizeEnabled) {
					// we only add/remove to the scenegraph if the fixed cell
					// length support is enabled - otherwise we keep all
					// TableCells in the scenegraph
					getChildren().remove(tableCell);
				}
			}

			x += width;

		}
	}


	private int columnCount = 0;

	private void recreateCells() {
		// This function is smart in the sense that we don't recreate all
		// TableCell instances every time this function is called. Instead we
		// only create TableCells for TableColumns we haven't already encountered.
		// To avoid a potential memory leak (when the TableColumns in the
		// TableView are created/inserted/removed/deleted, we have a 'refresh
		// counter' that when we reach 0 will delete all cells in this row
		// and recreate all of them.

		//        TableView<DataRow> table = getSkinnable().getTableView();
		//        if (table == null) {
		if (cellsMap != null) {

			//            Set<Entry<TableColumnBase, SpreadsheetCell>> cells = cellsMap.entrySet();
			//            for (Entry<TableColumnBase, SpreadsheetCell> entry : cells) {
			//                SpreadsheetCell cell = entry.getValue();
			//                cell.dispose();
			//            }

			cellsMap.clear();
		}
		//        return;
		//        }

		final ObservableList<? extends TableColumnBase/*<DataRow,?>*/> columns = getVisibleLeafColumns();

		if (columns.size() != columnCount || fullRefreshCounter == 0 || cellsMap == null) {
			if (cellsMap != null) {
				cellsMap.clear();
			}
			cellsMap = new WeakHashMap<TableColumnBase, SpreadsheetCell>(columns.size());
			fullRefreshCounter = DEFAULT_FULL_REFRESH_COUNTER;
			getChildren().clear();
		}
		columnCount = columns.size();
		fullRefreshCounter--;

		for (final TableColumnBase col : columns) {
			if (cellsMap.containsKey(col)) {
				continue;
			}

			// create a TableCell for this column and store it in the cellsMap
			// for future use
			createCell(col);
		}
	}

	protected void updateCells(boolean resetChildren) {
		// if clear isn't called first, we can run into situations where the
		// cells aren't updated properly.
		final boolean cellsEmpty = cells.isEmpty();
		cells.clear();

		prefWidth = 0;

		final TableRow<DataRow> skinnable = getSkinnable();

		// We want to get the REAL index, aka the Datarow behind
		final int skinnableIndex;
		if(skinnable.getItem() == null){
			skinnableIndex = skinnable.getIndex();
		}else{
			skinnableIndex = skinnable.getItem().getRowNumber();
		}
		//final int skinnableIndex = skinnable.getItem().getRowNumber();//skinnable.getIndex();
		final List<? extends TableColumnBase/*<DataRow,?>*/> visibleLeafColumns = getVisibleLeafColumns();

		for (int i = 0, max = visibleLeafColumns.size(); i < max; i++) {
			final TableColumnBase<DataRow,?> col = visibleLeafColumns.get(i);

			prefWidth += col.getWidth();

			SpreadsheetCell cell = cellsMap.get(col);
			if (cell == null) {
				// if the cell is null it means we don't have it in cache and
				// need to create it
				cell = createCell(col);
			}

			updateCell(cell, skinnable);
			cell.updateIndex(skinnableIndex);
			cells.add(cell);
		}

		// update children of each row
		if (!fixedCellSizeEnabled && (resetChildren || cellsEmpty)) {
			getChildren().setAll(cells);
		}
	}

	private SpreadsheetCell createCell(TableColumnBase col) {
		// we must create a TableCell for this table column
		final SpreadsheetCell cell = getCell(col);

		// and store this in our HashMap until needed
		cellsMap.put(col, cell);

		return cell;
	}

	@Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
		return prefWidth;
	}

	@Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		if (fixedCellSizeEnabled) {
			return fixedCellSize;
		}

		// fix for RT-29080
		checkState(false);

		// Support for RT-18467: making it easier to specify a height for
		// cells via CSS, where the desired height is less than the height
		// of the TableCells. Essentially, -fx-cell-size is given higher
		// precedence now
		if (getCellSize() < DEFAULT_CELL_SIZE) {
			return getCellSize();
		}

		// FIXME according to profiling, this method is slow and should
		// be optimised
		double prefHeight = 0.0f;
		final int count = cells.size();
		for (int i=0; i<count; i++) {
			final SpreadsheetCell tableCell = cells.get(i);
			prefHeight = Math.max(prefHeight, tableCell.prefHeight(-1));
		}
		final double ph = Math.max(prefHeight, Math.max(getCellSize(), getSkinnable().minHeight(-1)));

		return ph;
	}

	@Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		if (fixedCellSizeEnabled) {
			return fixedCellSize;
		}

		// fix for RT-29080
		checkState(false);

		// Support for RT-18467: making it easier to specify a height for
		// cells via CSS, where the desired height is less than the height
		// of the TableCells. Essentially, -fx-cell-size is given higher
		// precedence now
		if (getCellSize() < DEFAULT_CELL_SIZE) {
			return getCellSize();
		}

		// FIXME according to profiling, this method is slow and should
		// be optimised
		double minHeight = 0.0f;
		final int count = cells.size();
		for (int i = 0; i < count; i++) {
			final SpreadsheetCell tableCell = cells.get(i);
			minHeight = Math.max(minHeight, tableCell.minHeight(-1));
		}
		return minHeight;
	}

	@Override protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
		if (fixedCellSizeEnabled) {
			return fixedCellSize;
		}
		return super.computeMaxHeight(width, topInset, rightInset, bottomInset, leftInset);
	}

	private void checkState(boolean doRecreateIfNecessary) {
		if (isDirty) {
			// doRecreateIfNecessary was added to resolve RT-29382, which was
			// introduced by the fix for RT-29080 above in computePrefHeight
			if (doRecreateIfNecessary) {
				recreateCells();
			}
			updateCells(true);
			isDirty = false;
		} else if (updateCells) {
			updateCells(false);
			updateCells = false;
		}
	}

	private static final Duration FADE_DURATION = Duration.millis(200);

	// There appears to be a memory leak when using the stub toolkit. Therefore,
	// to prevent tests from failing we disable the animations below when the
	// stub toolkit is being used.
	// Filed as RT-29163.
	private static boolean IS_STUB_TOOLKIT = Toolkit.getToolkit().toString().contains("StubToolkit");

	private void fadeOut(final Node node) {
		if (node.getOpacity() < 1.0) {
			return;
		}

		if (IS_STUB_TOOLKIT) {
			node.setOpacity(0);
			return;
		}

		final FadeTransition fader = new FadeTransition(FADE_DURATION, node);
		fader.setToValue(0.0);
		fader.play();
	}

	private void fadeIn(final Node node) {
		if (node.getOpacity() > 0.0) {
			return;
		}

		if (IS_STUB_TOOLKIT) {
			node.setOpacity(1);
			return;
		}

		final FadeTransition fader = new FadeTransition(FADE_DURATION, node);
		fader.setToValue(1.0);
		fader.play();
	}
}
