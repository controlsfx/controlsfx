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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import org.controlsfx.control.spreadsheet.control.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.control.SpreadsheetRow;
import org.controlsfx.control.spreadsheet.control.SpreadsheetView;
import org.controlsfx.control.spreadsheet.model.DataRow;

import com.sun.javafx.scene.control.behavior.TableRowBehavior;

/**
 */
public class TableRowSkin<T extends DataRow> extends TableRowSkinBase<DataRow,TableRow<DataRow>, TableRowBehavior<DataRow>,SpreadsheetCell> {


	private TableViewSkin<SpreadsheetRow> tableViewSkin;

	public TableRowSkin(TableRow<DataRow> tableRow, final SpreadsheetView spreadsheetView) {

		super(tableRow, new TableRowBehavior<DataRow>(tableRow),spreadsheetView);

		updateTableViewSkin();

		super.init(tableRow);

		registerChangeListener(tableRow.tableViewProperty(), "TABLE_VIEW");
	}

	@Override protected void handleControlPropertyChanged(String p) {
		super.handleControlPropertyChanged(p);
		if ("TABLE_VIEW".equals(p)) {
			updateTableViewSkin();

			for (int i = 0, max = cells.size(); i < max; i++) {
				final Node n = cells.get(i);
				if (n instanceof SpreadsheetCell) {
					((SpreadsheetCell)n).updateTableView(getSkinnable().getTableView());
				}
			}

			//			this.spreadsheetView = (SpreadsheetViewInternal<DataRow>) getSkinnable().getTableView();
			//            spanModel = spreadsheetView.getSpanModel();
			//            registerChangeListener(spreadsheetView.spanModelProperty(), "SPAN_MODEL");
		}
	}

	@Override
	protected ObjectProperty<Node> graphicProperty() {
		return null;
	}

	@Override
	protected Control getVirtualFlowOwner() {
		return getSkinnable().getTableView();
	}

	@Override
	protected ObservableList<? extends TableColumnBase> getVisibleLeafColumns() {
		return spreadsheetView.getVisibleLeafColumns();
	}

	@Override
	protected void updateCell(SpreadsheetCell cell,
			TableRow<DataRow> row) {
		cell.updateTableRow(row);
	}

	@Override protected DoubleProperty fixedCellSizeProperty() {
		return spreadsheetView.fixedCellSizeProperty();
	}

	@Override
	protected boolean isColumnPartiallyOrFullyVisible(TableColumnBase tc) {
		//Virtualization of the columns is too complex for now
		return tableViewSkin == null ? false : true;//tableViewSkin.isColumnPartiallyOrFullyVisible(tc);
	}

	@Override
	protected SpreadsheetCell getCell(TableColumnBase tc) {
		final TableColumn tableColumn = (TableColumn<T,?>) tc;
		final SpreadsheetCell cell = (SpreadsheetCell) tableColumn.getCellFactory().call(tableColumn);

		// we set it's TableColumn, TableView and TableRow
		cell.updateTableColumn(tableColumn);
		cell.updateTableView(tableColumn.getTableView());
		cell.updateTableRow(getSkinnable());

		return cell;
	}

	@Override
	protected TableColumnBase<DataRow, ?> getTableColumnBase(
			SpreadsheetCell cell) {
		return cell.getTableColumn();
	}

	private void updateTableViewSkin() {
		final TableView tableView = getSkinnable().getTableView();
		if (tableView.getSkin() instanceof TableViewSkin) {
			tableViewSkin = (TableViewSkin)tableView.getSkin();
		}
	}
}
