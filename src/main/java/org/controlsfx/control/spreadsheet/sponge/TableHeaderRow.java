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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import org.controlsfx.control.spreadsheet.control.SpreadsheetView.SpreadsheetViewSelectionModel;

import com.sun.javafx.scene.control.skin.resources.ControlResources;

/**
 * Region responsible for painting the entire row of column headers.
 */
public class TableHeaderRow extends StackPane {

	private static final String MENU_SEPARATOR =
			ControlResources.getString("TableView.nestedColumnControlMenuSeparator");

	private final VirtualFlow flow;
	VirtualFlow getVirtualFlow() { return flow; }
	//    private final TableView<?> table;

	private final SpreadsheetViewSkinBase tableSkin;
	protected SpreadsheetViewSkinBase getTableSkin() {
		return this.tableSkin;
	}

	private Insets tablePadding;
	public void setTablePadding(Insets tablePadding) {
		this.tablePadding = tablePadding;
		updateTableWidth();
	}
	public Insets getTablePadding() {
		return tablePadding == null ? Insets.EMPTY : tablePadding;
	}

	// Vertical line that is shown when columns are being reordered
	private Region columnReorderLine;
	public Region getColumnReorderLine() { return columnReorderLine; }
	public void setColumnReorderLine(Region value) { this.columnReorderLine = value; }

	private double scrollX;

	private double tableWidth;
	public double getTableWidth() { return tableWidth; }
	private void updateTableWidth() {
		// snapping added for RT-19428
		double padding = snapSize(getTablePadding().getLeft()) + snapSize(getTablePadding().getRight());
		/*****************************************************************
		 * 				MODIFIED BY NELLARMONIA
		 *****************************************************************/
		if(tableSkin.spreadsheetView.getRowHeader().get()){
			padding += tableSkin.rowHeaderWidth;
		}

		/*****************************************************************
		 * 				END OF MODIFIED BY NELLARMONIA
		 *****************************************************************/
		final Control c = tableSkin.getSkinnable();
		this.tableWidth = c == null ? 0 : snapSize(c.getWidth()) - padding;

		clip.setWidth(tableWidth);
	}

	private final Rectangle clip;

	private final BooleanProperty reorderingProperty = new BooleanPropertyBase() {
		@Override protected void invalidated() {
			final TableColumnHeader r = getReorderingRegion();
			if (r != null) {
				final double dragHeaderHeight = r.getNestedColumnHeader() != null ?
						r.getNestedColumnHeader().getHeight() :
							getReorderingRegion().getHeight();

						dragHeader.resize(dragHeader.getWidth(), dragHeaderHeight);
						dragHeader.setTranslateY(getHeight() - dragHeaderHeight);
			}
			dragHeader.setVisible(isReordering());
		}

		@Override
		public Object getBean() {
			return TableHeaderRow.this;
		}

		@Override
		public String getName() {
			return "reordering";
		}
	};
	public final void setReordering(boolean value) { reorderingProperty().set(value); }
	public final boolean isReordering() { return reorderingProperty.get(); }
	public final BooleanProperty reorderingProperty() { return reorderingProperty; }

	private TableColumnHeader reorderingRegion;
	public TableColumnHeader getReorderingRegion() { return reorderingRegion; }

	public void setReorderingColumn(TableColumnBase rc) {
		dragHeaderLabel.setText(rc == null ? "" : rc.getText());
	}

	public void setReorderingRegion(TableColumnHeader reorderingRegion) {
		this.reorderingRegion = reorderingRegion;

		if (reorderingRegion != null) {
			dragHeader.resize(reorderingRegion.getWidth(), dragHeader.getHeight());
		}
	}

	public void setDragHeaderX(double dragHeaderX) {
		dragHeader.setTranslateX(dragHeaderX);
	}

	/**
	 * This is the ghosted region representing the table column that is being
	 * dragged. It moves along the x-axis but is fixed in the y-axis.
	 */
	private final StackPane dragHeader;
	private final Label dragHeaderLabel = new Label();

	/*
	 * The header row is actually just one NestedTableColumnHeader that spans
	 * the entire width. Nested within this is the TableColumnHeader's and
	 * NestedTableColumnHeader's, as necessary. This makes it nice and clean
	 * to handle column reordering - we basically enforce the rule that column
	 * reordering only occurs within a single NestedTableColumnHeader, and only
	 * at that level.
	 */
	private final NestedTableColumnHeader header;

	public NestedTableColumnHeader getRootHeader() {
		return header;
	}

	private final Region filler;

	/**
	 * This is the region where the user can interact with to show/hide columns.
	 * It is positioned in the top-right hand corner of the TableHeaderRow, and
	 * when clicked shows a PopupMenu consisting of all leaf columns.
	 */
	private final Pane cornerRegion;

	/**
	 * PopupMenu shown to users to allow for them to hide/show columns in the
	 * table.
	 */
	private final ContextMenu columnPopupMenu;


	/***************************************************************************
	 *                                                                         *
	 * Constructor                                                             *
	 *                                                                         *
	 **************************************************************************/

	public TableHeaderRow(final SpreadsheetViewSkinBase skin) {
		//        this.table = table;
		this.tableSkin = skin;
		this.flow = skin.flow;

		getStyleClass().setAll("column-header-background");

		clip = new Rectangle();
		clip.setSmooth(false);
		clip.heightProperty().bind(heightProperty());
		setClip(clip);

		updateTableWidth();
		tableSkin.getSkinnable().widthProperty().addListener(weakTableWidthListener);
		/*****************************************************************
		 * 				MODIFIED BY NELLARMONIA
		 *****************************************************************/
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				tableSkin.spreadsheetView.getRowHeader().addListener(rowHeaderListener);
				selectionModel = tableSkin.spreadsheetView.getSelectionModel();
				selectionModel.getSelectedColumns().addListener(selectionListener);
				tableSkin.spreadsheetView.getFixedColumns().addListener(fixedColumnsListener);
			}
		};
		Platform.runLater(r);


		/*****************************************************************
		 * 				END OF MODIFIED BY NELLARMONIA
		 *****************************************************************/
		skin.getVisibleLeafColumns().addListener(weakVisibleLeafColumnsListener);

		// --- popup menu for hiding/showing columns
		columnPopupMenu = new ContextMenu();

		updateTableColumnListeners(tableSkin.getColumns(), Collections.<TableColumnBase<?,?>>emptyList());
		tableSkin.getColumns().addListener(weakTableColumnsListener);
		// --- end of popup menu

		// drag header region. Used to indicate the current column being reordered
		dragHeader = new StackPane();
		dragHeader.setVisible(false);
		dragHeader.getStyleClass().setAll("column-drag-header");
		dragHeader.setManaged(false);
		dragHeader.getChildren().add(dragHeaderLabel);

		// the header lives inside a NestedTableColumnHeader
		header = createRootHeader();
		header.setFocusTraversable(false);
		header.setTableHeaderRow(this);

		// The 'filler' area that extends from the right-most column to the edge
		// of the tableview, or up to the 'column control' button
		filler = new Region();
		filler.getStyleClass().setAll("filler");

		// Give focus to the table when an empty area of the header row is clicked.
		// This ensures the user knows that the table has focus.
		setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent e) {
				skin.getSkinnable().requestFocus();
			}
		});

		final StackPane image = new StackPane();
		image.setSnapToPixel(false);
		image.getStyleClass().setAll("show-hide-column-image");
		cornerRegion = new StackPane() {
			@Override protected void layoutChildren() {
				final double imageWidth = image.snappedLeftInset() + image.snappedRightInset();
				final double imageHeight = image.snappedTopInset() + image.snappedBottomInset();

				image.resize(imageWidth, imageHeight);
				positionInArea(image, 0, 0, getWidth(), getHeight() - 3,
						0, HPos.CENTER, VPos.CENTER);
			}
		};
		cornerRegion.getStyleClass().setAll("show-hide-columns-button");
		cornerRegion.getChildren().addAll(image);
		cornerRegion.setVisible(tableSkin.tableMenuButtonVisibleProperty().get());
		tableSkin.tableMenuButtonVisibleProperty().addListener(new InvalidationListener() {
			@Override public void invalidated(Observable valueModel) {
				cornerRegion.setVisible(tableSkin.tableMenuButtonVisibleProperty().get());
				/*****************************************************************
				 * 				MODIFIED BY NELLARMONIA
				 *****************************************************************/
				if(working) {
					requestLayout();
				}
				/*****************************************************************
				 * 				END OF MODIFIED BY NELLARMONIA
				 *****************************************************************/

			}
		});
		cornerRegion.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent me) {
				// show a popupMenu which lists all columns
				columnPopupMenu.show(cornerRegion, Side.BOTTOM, 0, 0);
				me.consume();
			}
		});

		// the actual header
		// the region that is anchored above the vertical scrollbar
		// a 'ghost' of the header being dragged by the user to force column
		// reordering
		getChildren().addAll(filler, header, cornerRegion, dragHeader);

		/*****************************************************************
		 * 				MODIFIED BY NELLARMONIA
		 *****************************************************************/
		// We listen to the BooleanProperty linked with the CheckBox of the columnHeader
		tableSkin.spreadsheetView.getColumnHeader().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0,
					Boolean arg1, Boolean arg2) {
				working = arg2;
				requestLayout();
				header.layoutFixedColumns();
				updateHighlighSelection();
			}});
		/*****************************************************************
		 * 				END OF MODIFIED BY NELLARMONIA
		 *****************************************************************/
	}

	protected NestedTableColumnHeader createRootHeader() {
		return new NestedTableColumnHeader(tableSkin, null);
	}


	/***************************************************************************
	 *                                                                         *
	 * Listeners                                                               *
	 *                                                                         *
	 **************************************************************************/

	private final InvalidationListener tableWidthListener = new InvalidationListener() {
		@Override public void invalidated(Observable valueModel) {
			updateTableWidth();
		}
	};

	private final ListChangeListener visibleLeafColumnsListener = new ListChangeListener<TableColumn<?,?>>() {
		@Override public void onChanged(ListChangeListener.Change<? extends TableColumn<?,?>> c) {
			// This is necessary for RT-20300 (but was updated for RT-20840)
			header.setHeadersNeedUpdate();
		}
	};

	private final ListChangeListener tableColumnsListener = new ListChangeListener<TableColumn<?,?>>() {
		@Override public void onChanged(Change<? extends TableColumn<?,?>> c) {
			while (c.next()) {
				updateTableColumnListeners(c.getAddedSubList(), c.getRemoved());
			}
		}
	};

	private final InvalidationListener columnTextListener = new InvalidationListener() {
		@Override public void invalidated(Observable observable) {
			final TableColumn<?,?> column = (TableColumn<?,?>) ((StringProperty)observable).getBean();
			final CheckMenuItem menuItem = columnMenuItems.get(column);
			if (menuItem != null) {
				menuItem.setText(getText(column.getText(), column));
			}
		}
	};

	private final WeakInvalidationListener weakTableWidthListener =
			new WeakInvalidationListener(tableWidthListener);

	private final WeakListChangeListener weakVisibleLeafColumnsListener =
			new WeakListChangeListener(visibleLeafColumnsListener);

	private final WeakListChangeListener weakTableColumnsListener =
			new WeakListChangeListener(tableColumnsListener);

	private final WeakInvalidationListener weakColumnTextListener =
			new WeakInvalidationListener(columnTextListener);


	private final Map<TableColumnBase, CheckMenuItem> columnMenuItems = new HashMap<TableColumnBase, CheckMenuItem>();
	private void updateTableColumnListeners(List<? extends TableColumnBase<?,?>> added, List<? extends TableColumnBase<?,?>> removed) {
		// remove binding from all removed items
		for (final TableColumnBase tc : removed) {
			remove(tc);
		}

		// add listeners to all added items
		for (final TableColumnBase tc : added) {
			add(tc);
		}
	}

	private void remove(TableColumnBase<?,?> col) {
		if (col == null) {
			return;
		}

		final CheckMenuItem item = columnMenuItems.remove(col);
		if (item != null) {
			col.textProperty().removeListener(weakColumnTextListener);
			item.selectedProperty().unbindBidirectional(col.visibleProperty());

			columnPopupMenu.getItems().remove(item);
		}

		if (! col.getColumns().isEmpty()) {
			for (final TableColumnBase tc : col.getColumns()) {
				remove(tc);
			}
		}
	}

	private void add(final TableColumnBase<?,?> col) {
		if (col == null) {
			return;
		}

		if (col.getColumns().isEmpty()) {
			CheckMenuItem item = columnMenuItems.get(col);
			if (item == null) {
				item = new CheckMenuItem();
				columnMenuItems.put(col, item);
			}

			// bind column text and isVisible so that the menu item is always correct
			item.setText(getText(col.getText(), col));
			col.textProperty().addListener(weakColumnTextListener);
			item.selectedProperty().bindBidirectional(col.visibleProperty());

			columnPopupMenu.getItems().add(item);
		} else {
			for (final TableColumnBase tc : col.getColumns()) {
				add(tc);
			}
		}
	}

	void updateScrollX() {
		scrollX = flow.getHbar().isVisible() ? -flow.getHbar().getValue() : 0.0F;
		/*****************************************************************
		 * 				MODIFIED BY NELLARMONIA
		 *****************************************************************/
		if(working) {
			requestLayout();
			header.layoutFixedColumns();
		}
		/*****************************************************************
		 * 				END OF MODIFIED BY NELLARMONIA
		 *****************************************************************/
	}

	@Override protected void layoutChildren() {
		final double x = scrollX;
		final double headerWidth = snapSize(header.prefWidth(-1));
		final double prefHeight = getHeight() - snappedTopInset() - snappedBottomInset();
		final double cornerWidth = snapSize(flow.getVbar().prefWidth(-1));

		// position the main nested header
		header.resizeRelocate(x, snappedTopInset(), headerWidth, prefHeight);

		// position the filler region
		final double border = filler.getBoundsInLocal().getWidth() - filler.getLayoutBounds().getWidth();
		double fillerWidth = tableWidth - headerWidth + border;
		fillerWidth -= tableSkin.tableMenuButtonVisibleProperty().get() ? cornerWidth : 0;
		filler.setVisible(fillerWidth > 0);
		if (fillerWidth > 0) {
			filler.resizeRelocate(x + headerWidth, snappedTopInset(), fillerWidth, prefHeight);
		}

		// position the top-right rectangle (which sits above the scrollbar)
		cornerRegion.resizeRelocate(tableWidth - cornerWidth, snappedTopInset(), cornerWidth, prefHeight);
	}

	@Override protected double computePrefWidth(double height) {
		return header.prefWidth(height);
	}

	@Override protected double computeMinHeight(double width) {
		return computePrefHeight(width);
	}

	@Override protected double computePrefHeight(double width) {
		return snappedTopInset() + header.prefHeight(width) + snappedBottomInset();
	}

	//    public function isColumnFullyVisible(col:TableColumn):Number {
	//        if (not col.visible) return 0;
	//
	//        // work out where the header is in 0-based coordinates
	//        var start:Number = scrollX;
	//        for (c in table.visibleLeafColumns) {
	//            if (c == col) break;
	//            start += c.width;
	//        }
	//        var end = start + col.width;
	//
	//        // determine the width of the header (taking into account any scrolling)
	//        var headerWidth = /*scrollX +*/ (clip as Rectangle).width;
	//
	//        return if (start < 0 or end > headerWidth) then start else 0;
	//    }

	/*
	 * Function used for building the strings in the popup menu
	 */
	private String getText(String text, TableColumnBase col) {
		String s = text;
		TableColumnBase parentCol = col.getParentColumn();
		while (parentCol != null) {
			if (isColumnVisibleInHeader(parentCol, tableSkin.getColumns())) {
				s = parentCol.getText() + MENU_SEPARATOR + s;
			}
			parentCol = parentCol.getParentColumn();
		}
		return s;
	}

	// We need to show strings properly. If a column has a parent column which is
	// not inserted into the TableView columns list, it effectively doesn't have
	// a parent column from the users perspective. As such, we shouldn't include
	// the parent column text in the menu. Fixes RT-14482.
	private boolean isColumnVisibleInHeader(TableColumnBase col, List columns) {
		if (col == null) {
			return false;
		}

		for (int i = 0; i < columns.size(); i++) {
			final TableColumnBase column = (TableColumnBase) columns.get(i);
			if (col.equals(column)) {
				return true;
			}

			if (! column.getColumns().isEmpty()) {
				final boolean isVisible = isColumnVisibleInHeader(col, column.getColumns());
				if (isVisible) {
					return true;
				}
			}
		}

		return false;
	}
	/*****************************************************************
	 * 				NELLARMONIA CODE
	 *****************************************************************/
	// Indicate wether the this TableHeaderRow is activated or not
	private   Boolean working = true;

	/**
	 * When the Rowheader is showing (or not anymore) we need to react accordingly
	 */
	private final InvalidationListener rowHeaderListener = new InvalidationListener() {
		@Override public void invalidated(Observable valueModel) {
			updateTableWidth();
		}
	};
	/**
	 * When we fix/unfix some columns, the header must react accordingly
	 * TODO maybe modify and remove that "runLater"
	 * TODO But if if have not that "runLater", we call "layoutFixedColumns" too early..
	 */
	private final InvalidationListener fixedColumnsListener = new InvalidationListener() {
		@Override public void invalidated(Observable valueModel) {
			header.setHeadersNeedUpdate();
			header.layoutFixedColumns();
			final Runnable r = new Runnable() {
				@Override
				public void run() {
					updateHighlighSelection();
				}
			};
			Platform.runLater(r);
		}
	};

	private SpreadsheetViewSelectionModel selectionModel;
	private void updateHighlighSelection(){
		for (final TableColumnHeader i : header.getColumnHeaders()) {
			i.getChildrenUnmodifiable().get(0).getStyleClass().clear();

		}
		final List<Integer> selectedColumns = selectionModel.getSelectedColumns();
		//TODO Ugly hack to get access to the Label
		for (final Object i : selectedColumns) {
			header.getColumnHeaders().get((Integer)i).getChildrenUnmodifiable().get(0).getStyleClass().addAll("selected");
		}

	}
	/**
	 * When we select some cells, we want the header to be highlighted
	 */
	private final InvalidationListener selectionListener = new InvalidationListener() {
		@Override public void invalidated(Observable valueModel) {
			updateHighlighSelection();
		}
	};

}
