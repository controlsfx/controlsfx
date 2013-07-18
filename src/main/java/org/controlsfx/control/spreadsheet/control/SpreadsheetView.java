package org.controlsfx.control.spreadsheet.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.Duration;

import org.controlsfx.control.spreadsheet.editor.DateEditor;
import org.controlsfx.control.spreadsheet.editor.Editor;
import org.controlsfx.control.spreadsheet.editor.ListEditor;
import org.controlsfx.control.spreadsheet.editor.TextEditor;
import org.controlsfx.control.spreadsheet.model.DataCell;
import org.controlsfx.control.spreadsheet.model.DataRow;
import org.controlsfx.control.spreadsheet.model.Grid;
import org.controlsfx.control.spreadsheet.skin.SpreadsheetViewSkin;
import org.controlsfx.control.spreadsheet.sponge.VirtualScrollBar;

public class SpreadsheetView extends BorderPane{
	public static enum SpanType {

		NORMAL_CELL, // Normal Cell (visible)
		COLUMN_INVISIBLE, //Invisible cell spanned in column
		ROW_INVISIBLE, //Invisible cell spanned in row
		ROW_VISIBLE,//Visible Cell but has invisible cell below
		BOTH_INVISIBLE;   //Invisible cell, span in diagonal
	}

	public static interface RowAccessor<T> {
		T get(int index);

		boolean isEmpty();

		int size();
	}

	private final SpreadsheetViewInternal<DataRow> spreadsheetViewInternal;
	private static final String DEFAULT_STYLE_CLASS = "cell-spreadsheet";
	private final double DEFAULT_CELL_SIZE = 24.0; 	// Height of a cell
	private DataCell<?> lastEdit = null;
	private SpreadsheetCell lastHover = null;
	private Grid grid;
	private final double cellPrefWidth = 100;			// Width of a cell
	private final Map<DataCell.CellType, Editor> editors = FXCollections.observableHashMap();
	private final ObservableList<Integer> fixedRows = FXCollections.observableArrayList();
	private final ObservableList<Integer> fixedColumns = FXCollections.observableArrayList();
	private final BooleanProperty columnHeader = new SimpleBooleanProperty(true);
	private final BooleanProperty rowHeader = new SimpleBooleanProperty(true);

	//Properties needed by the SpreadsheetView and managed by the skin (source is the VirtualFlow)
	private TreeSet<Integer> visibleRows=null;
	private VirtualScrollBar hbar=null;
	private VirtualScrollBar vbar=null;
	private RowAccessor<SpreadsheetRow> cells=null;

	public SpreadsheetRow getNonFixed(int index){
		return cells.get(fixedRows.size()+index);
	}

	public TreeSet<Integer> getVisibleRows() {
		return visibleRows;
	}

	public void setVisibleRows(TreeSet<Integer> visibleRows) {
		this.visibleRows = visibleRows;
	}

	public VirtualScrollBar getHbar() {
		return hbar;
	}

	public void setHbar(VirtualScrollBar hbar) {
		this.hbar = hbar;
	}

	public VirtualScrollBar getVbar() {
		return vbar;
	}

	public void setVbar(VirtualScrollBar vbar) {
		this.vbar = vbar;
	}

	public SpreadsheetRow getRow(int index) {
		return cells.get(index);
	}

	public boolean isEmptyCells() {
		return cells.isEmpty();
	}

	public void setRows(RowAccessor<?> cells) {
		this.cells = (RowAccessor<SpreadsheetRow>) cells;
	}
	public int getVirtualFlowCellSize(){
		return cells.size();
	}
	
	public TablePosition<DataRow, ?> getEditingCell(){
		return spreadsheetViewInternal.getEditingCell();
	}



	public SpreadsheetView(){
		super();
		this.setPadding(new Insets(10, 10, 10, 10));
		spreadsheetViewInternal = new SpreadsheetViewInternal<>();

		buildSpreadsheetView(Grid.GridSpanType.BOTH);

		//Add a listener to the selection model in order to edit the spanned cells when clicked
		setSelectionModel();

		spreadsheetViewInternal.setEditable(true);

		// For keyboard catch
		setFocusModel();

		spreadsheetViewInternal.setContextMenu(getSpreadsheetViewContextMenu());

		//Do nothing basically but give access to the Hover Property.
		spreadsheetViewInternal.setRowFactory(new Callback<TableView<DataRow>, TableRow<DataRow>>() {
			@Override
			public TableRow<DataRow> call(TableView<DataRow> p) {
				return new SpreadsheetRow(SpreadsheetView.this);
			}
		});

		// Set the skin in place and give access to the VirtualFlow
		spreadsheetViewInternal.setSkin(new SpreadsheetViewSkin<>(spreadsheetViewInternal,this));

		spreadsheetViewInternal.setFixedCellSize(getDefaultCellSize());

		spreadsheetViewInternal.getStyleClass().add(DEFAULT_STYLE_CLASS);
		this.setCenter(spreadsheetViewInternal);

		this.setLeft(buildCommonControlGrid());
	}
	
	

	private void buildSpreadsheetView(Grid.GridSpanType type) {
		setGrid(new Grid(type));
		final ObservableList<DataRow> observableRows = FXCollections.observableArrayList(getGrid().getRows());
		spreadsheetViewInternal.setItems(observableRows);


		for (int i = 0; i < getGrid().getColumncount(); ++i) {
			final int col = i;

			final TableColumn<DataRow, DataCell<?>> column = new TableColumn<>(getEquivColumn(col));

			column.setEditable(true);
			// We don't want to sort the column
			column.setSortable(false);
			
			column.impl_setReorderable(false);
			
			column.setPrefWidth(getCellPrefWidth());


			// We assign a DataCell for each Cell needed (MODEL).
			column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DataRow, DataCell<?>>, ObservableValue<DataCell<?>>>() {
				@Override
				public ObservableValue<DataCell<?>> call(TableColumn.CellDataFeatures<DataRow, DataCell<?>> p) {
					return new ReadOnlyObjectWrapper(p.getValue().getCell(col));
				}
			});
			// We create a SpreadsheetCell for each DataCell in order to specify how to represent the DataCell(VIEW)
			column.setCellFactory(new Callback<TableColumn<DataRow, DataCell<?>>, TableCell<DataRow, DataCell<?>>>() {
				@Override
				public TableCell<DataRow, DataCell<?>> call(TableColumn<DataRow, DataCell<?>> p) {
					return new SpreadsheetCell();
				}
			});
			getColumns().add(column);
		}
	}

	/**
	 * Build a common control Grid with some options on the left to control the
	 * SpreadsheetViewInternal
	 *
	 * @param spreadsheetView
	 * @return
	 */
	public GridPane buildCommonControlGrid() {
		final GridPane grid = new GridPane();
		grid.setHgap(5);
		grid.setVgap(5);
		grid.setPadding(new Insets(5, 5, 5, 5));

		final ChoiceBox<Integer> fixedRows = new ChoiceBox<>(FXCollections.observableArrayList(0, 1, 2));
		fixedRows.getSelectionModel().select(0);
		fixedRows.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0,
					Number arg1, Number arg2) {
				fixRows(arg2.intValue());
			}
		});

		final ChoiceBox<Integer> fixedColumns = new ChoiceBox<>(FXCollections.observableArrayList(0, 1, 2));
		fixedColumns.getSelectionModel().select(0);
		fixedColumns.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				fixColumns(arg2.intValue());
			}
		});

		final CheckBox rowHeader = new CheckBox("Row Header");
		rowHeader.setSelected(true);
		rowHeader.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0,
					Boolean arg1, Boolean arg2) {
				setRowHeader(arg2);
			}
		});

		final CheckBox columnHeader = new CheckBox("Column Header");
		columnHeader.setSelected(true);
		columnHeader.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(
					ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				setColumnHeader(arg2);
			}
		});
		grid.add(new Label("Freeze Rows:"), 1, 1);
		grid.add(fixedRows, 1, 2);
		grid.add(new Label("Freeze Columns:"), 1, 3);
		grid.add(fixedColumns, 1, 4);
		grid.add(rowHeader, 1, 5);
		grid.add(columnHeader, 1, 6);

		return grid;
	}
	public double getCellPrefWidth() {
		return cellPrefWidth;
	}

	/**
	 * Activate and deactivate the Column Header
	 * @param b
	 */
	public void setColumnHeader(final boolean b){

		//TODO Need to do that again
		//flow.recreateCells(); // Because otherwise we have at the bottom
		columnHeader.setValue(b);
		columnHeader.get();//For invalidation Listener to react again
	}

	/**
	 * Activate and desactivate the Column Header
	 * @param b
	 */
	public void setRowHeader(final boolean b){
		rowHeader.setValue(b);
		rowHeader.get();//For invalidation Listener to react again
	}
	public BooleanProperty getRowHeader() {
		return rowHeader;
	}
	public BooleanProperty getColumnHeader() {
		return columnHeader;
	}
	public ObservableList<Integer> getFixedRows() {
		return fixedRows;
	}
	public ObservableList<Integer> getFixedColumns() {
		return fixedColumns;
	}
	/**
	 * Fix the first "numberOfFixedRows" at the top.
	 * @param numberOfFixedRows
	 */
	public void fixRows(int numberOfFixedRows){

		getFixedRows().clear();
		for (int j = 0; j < numberOfFixedRows; j++) {
			getFixedRows().add(j);
		}
	}

	/**
	 * Fix the first "numberOfFixedRows" on the left.
	 * @param numberOfFixedColumns
	 */
	public void fixColumns(int numberOfFixedColumns){

		getFixedColumns().clear();
		for (int j = 0; j < numberOfFixedColumns; j++) {
			getFixedColumns().add(j);
		}
	}

	public boolean addCell(SpreadsheetCell cell){
		SpreadsheetRow temp = getRow(cells.size()-1-fixedRows.size());
		if(temp != null){
			temp.addCell(cell);
			return true;
		}
		return false;
		
	}
	public ObservableList<? extends TableColumnBase> getVisibleLeafColumns() {
		return spreadsheetViewInternal.getVisibleLeafColumns();
	}

	public DoubleProperty fixedCellSizeProperty() {
		return spreadsheetViewInternal.fixedCellSizeProperty();
	}

	public ObservableList getItems() {
		return spreadsheetViewInternal.getItems();
	}

	/**
	 * Return the SpanType of a cell.
	 * @param row
	 * @param column
	 * @return
	 */
	public SpanType getSpanType(final int row, final int column) {

		if (row < 0 || column < 0 || !getVisibleRows().contains(row)) {
			return SpanType.NORMAL_CELL;
		}
		final DataCell<?> cellSpan = ((DataRow)this.getItems().get(row)).getCell(column);
		if (cellSpan.getColumn() == column
				&& cellSpan.getRow() == row
				&& cellSpan.getRowSpan() == 1) {
			return SpanType.NORMAL_CELL;
		} else if (getVisibleRows().contains(row-1)
				&& cellSpan.getColumnSpan() > 1
				&& cellSpan.getColumn() != column
				&& cellSpan.getRowSpan() > 1
				&& cellSpan.getRow() != row) {
			return SpanType.BOTH_INVISIBLE;
		} else if (cellSpan.getRowSpan() > 1
				&& cellSpan.getColumn() == column) {
			if (cellSpan.getRow() == row || !getVisibleRows().contains(row-1)) {
				return SpanType.ROW_VISIBLE;
			} else {
				return SpanType.ROW_INVISIBLE;
			}
		} else if (cellSpan.getColumnSpan() > 1
				&& cellSpan.getColumn() != column
				&& (cellSpan.getRow() == row || !getVisibleRows().contains(row-1))) {
			return SpanType.COLUMN_INVISIBLE;
		} else {
			return SpanType.NORMAL_CELL;
		}
	}

	public SpreadsheetViewSelectionModel getSelectionModel() {
		return (SpreadsheetViewSelectionModel) spreadsheetViewInternal.getSelectionModel();
	}

	public ObservableList<TableColumn<DataRow,?>> getColumns() {
		return spreadsheetViewInternal.getColumns();
	}

	/**
	 * @return the defaultCellSize
	 */
	public double getDefaultCellSize() {
		return DEFAULT_CELL_SIZE;
	}

	DataCell<?> getLastEdit() {
		return lastEdit;
	}

	void setLastEdit( DataCell<?> cell){
		lastEdit = cell;
	}

	/**
	 * A SpreadsheetCell is being hovered and we need to re-route the signal.
	 *
	 * @param cell The DataCell needed to be hovered.
	 * @param hover
	 */
	void hoverGridCell(DataCell<?> cell) {
		//If the top of the spanned cell is visible, then no problem
		SpreadsheetCell gridCell;
		if (!getVisibleRows().isEmpty() && getVisibleRows().first() <= cell.getRow()) {
			// We want to get the top of the spanned cell, so we need
			// to access the fixedRows.size plus the difference between where we want to go and the first visibleRow (header excluded)
			if(getRow(getFixedRows().size()+cell.getRow()-getVisibleRows().first()) != null) {// Sometime when scrolling fast it's null so..
				gridCell = getRow(getFixedRows().size()+cell.getRow()-getVisibleRows().first()).getGridCell(cell.getColumn());
			} else {
				gridCell = getNonFixed(0).getGridCell(cell.getColumn());
			}
		} else { // If it's not, then it's the firstkey
			gridCell = getNonFixed(0).getGridCell(cell.getColumn());
		}
		gridCell.setHoverPublic(true);
		lastHover = gridCell;

	}

	/**
	 * Set Hover to false to the previous Cell we force to be hovered
	 */
	public void unHoverGridCell() {
		//If the top of the spanned cell is visible, then no problem
		if(lastHover != null){
			lastHover.setHoverPublic(false);
		}
	}

	/**
	 * Return an instance of Editor specific to the Cell type
	 * We are not using the build-in editor-Cell because we cannot know in advance
	 * which editor we will need. Furthermore, we want to control the behavior very closely
	 * in regards of the spanned cell (invisible etc).
	 * @param cell The SpreadsheetCell
	 * @param bc The SpreadsheetCell
	 * @return
	 */
	public Editor getEditor(DataCell<?> cell, SpreadsheetCell bc) {
		Editor editor = editors.get(cell.getCellType());
		if (editor == null) {
			switch (cell.getCellType()) {
			case STRING:
				final TextEditor te = new TextEditor();
				editors.put(cell.getCellType(), te);
				editor = te;
				break;
			case ENUM:
				final ListEditor le = new ListEditor();
				editors.put(cell.getCellType(), le);
				editor = le;
				break;
			case DATE:
				final DateEditor de = new DateEditor();
				editors.put(cell.getCellType(), de);
				editor = de;
				break;
			default:
				return null;
			}
		}
		editor.begin(cell, bc, this);
		// We store the lastEditing cell
		setLastEdit(((DataRow)this.getItems().get(cell.getRow())).getCell(cell.getColumn()));
		return editor;
	}

	public Grid getGrid(){
		return grid;
	}

	public void setGrid(Grid grid) {
		this.grid = grid;

	}

	/**
	 * Give the column letter in excel mode with the given number
	 * @param number
	 * @return
	 */
	public String getEquivColumn(int number){
		String converted = "";
		// Repeatedly divide the number by 26 and convert the
		// remainder into the appropriate letter.
		while (number >= 0)
		{
			final int remainder = number % 26;
			converted = (char)(remainder + 'A') + converted;
			number = number / 26 - 1;
		}

		return converted;
	}
	/***************************************************************************
	 * 						COPY PASTE METHODS
	 **************************************************************************/
	DataFormat fmt;

	public void checkFormat(){
		if((fmt = DataFormat.lookupMimeType("shuttle"))== null){
			fmt = new DataFormat("shuttle");
		}
	}
	/***
	 * Create a menu on rightClick with two options: Copy/Paste
	 * @return
	 */
	private ContextMenu getSpreadsheetViewContextMenu(){
		final ContextMenu contextMenu = new ContextMenu();
		final MenuItem item1 = new MenuItem("Copy");
		item1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				putClipboard();
			}
		});
		final MenuItem item2 = new MenuItem("Paste");
		item2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				getClipboard();
			}
		});
		contextMenu.getItems().addAll(item1, item2);
		return contextMenu;
	}

	/**
	 * Put the current selection into the ClipBoard
	 */
	private void putClipboard(){
		checkFormat();

		//		final ArrayList<ArrayList<DataCell>> temp = new ArrayList<>();
		final ArrayList<DataCell<?>> list = new ArrayList<DataCell<?>>();
		final ObservableList<TablePosition> posList = getSelectionModel().getSelectedCells();

		for (final TablePosition<?,?> p : posList) {
			list.add(getGrid().getRows().get(p.getRow()).get(p.getColumn()));
		}

		final ClipboardContent content = new ClipboardContent();
		content.put(fmt,list);
		Clipboard.getSystemClipboard().setContent(content);
	}

	/**
	 * Try to paste the clipBoard to the specified position
	 * Try to paste the current selection into the Grid. If the two contents are
	 * not matchable, then it's not pasted.
	 */
	private void getClipboard(){
		checkFormat();
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		if(clipboard.getContent(fmt) != null){

			final ArrayList<DataCell<?>> list = (ArrayList<DataCell<?>>) clipboard.getContent(fmt);
			//TODO algorithm very bad
			int minRow=grid.getRowCount();
			int minCol=grid.getColumncount();
			int maxRow=0;
			int maxCol=0;
			for (final DataCell<?> p : list) {
				final int tempcol = p.getColumn();
				final int temprow = p.getRow();
				if(tempcol<minCol) {
					minCol = tempcol;
				}
				if(tempcol>maxCol) {
					maxCol = tempcol;
				}
				if(temprow<minRow) {
					minRow = temprow;
				}
				if(temprow>maxRow) {
					maxRow =temprow;
				}
			}

			final TablePosition<?,?> p = spreadsheetViewInternal.getFocusModel().getFocusedCell();

			final int offsetRow = p.getRow()-minRow;
			final int offsetCol = p.getColumn()-minCol;
			int row;
			int column;


			for (final DataCell<?> row1 : list) {
				row = row1.getRow();
				column = row1.getColumn();
				if(row+offsetRow < getGrid().getRowCount() && column+offsetCol < getGrid().getColumncount()
						&& row+offsetRow >= 0 && column+offsetCol >=0 ){
					final SpanType type = getSpanType(row+offsetRow, column+offsetCol);
					if(type == SpanType.NORMAL_CELL || type== SpanType.ROW_VISIBLE) {
						getGrid().getRows().get(row+offsetRow).get(column+offsetCol).match(row1);
					}
				}
			}
			//For layout
			getSelectionModel().clearSelection();
			requestLayout();
		}
	}


	/**************************************************************************
	 * 
	 * 						FOCUS MODEL
	 * 
	 * *************************************************************************/

	/**
	 * Set the focus model to track keyboard change and redirect focus on spanned
	 * cells
	 *
	 * @param spreadsheetView
	 */
	private void setFocusModel() {
		// We add a listener on the focus model in order to catch when we are on a hidden cell
		spreadsheetViewInternal.getFocusModel().focusedCellProperty().addListener((ChangeListener<TablePosition>)(ChangeListener<?>) new FocusModelListener(this));
	}
	class FocusModelListener implements ChangeListener<TablePosition<DataRow,?>> {

		private final SpreadsheetView spreadsheetView;
		private final TableView.TableViewFocusModel<DataRow> tfm;

		public FocusModelListener(SpreadsheetView spreadsheetView) {
			this.spreadsheetView = spreadsheetView;
			tfm = spreadsheetViewInternal.getFocusModel();
		}

		@Override
		public void changed(ObservableValue<? extends TablePosition<DataRow,?>> ov, final TablePosition<DataRow,?> t, final TablePosition<DataRow,?> t1) {
			final SpreadsheetView.SpanType spanType = getSpanType(t1.getRow(), t1.getColumn());
			switch (spanType) {
			case ROW_INVISIBLE:
				// If we notice that the new focused cell is the previous one, then it means that we were
				//already on the cell and we wanted to go below.
				if (!isPressed()
						&& t.getColumn() == t1.getColumn()
						&& t.getRow() == t1.getRow() - 1) {
					final Runnable r = new Runnable() {
						@Override
						public void run() {
							tfm.focus(getTableRowSpan(t), t.getTableColumn());
						}
					};
					Platform.runLater(r);

				} else {
					// If the current focused cell if hidden by row span, we go above
					final Runnable r = new Runnable() {
						@Override
						public void run() {
							tfm.focus(t1.getRow() - 1, t1.getTableColumn());
						}
					};
					Platform.runLater(r);
				}

				break;
			case BOTH_INVISIBLE:
				// If the current focused cell if hidden by a both (row and column) span, we go left-above
				final Runnable r = new Runnable() {
					@Override
					public void run() {
						tfm.focus(t1.getRow() - 1, getColumns().get(t1.getColumn() - 1));
					}
				};
				Platform.runLater(r);
				break;
			case COLUMN_INVISIBLE:
				// If we notice that the new focused cell is the previous one, then it means that we were
				//already on the cell and we wanted to go right.
				if (!isPressed()
						&& t.getColumn() == t1.getColumn() - 1
						&& t.getRow() == t1.getRow()) {

					final Runnable r2 = new Runnable() {
						@Override
						public void run() {
							tfm.focus(t.getRow(), getTableColumnSpan(t));
						}
					};
					Platform.runLater(r2);
				} else {
					// If the current focused cell if hidden by column span, we go left

					final Runnable r2 = new Runnable() {
						@Override
						public void run() {
							tfm.focus(t1.getRow(), getColumns().get(t1.getColumn() - 1));
						}
					};
					Platform.runLater(r2);
				}
			default:
				break;
			}
		}
	}

	/**************************************************************************
	 * 
	 * 						SELECTION MODEL
	 * 
	 * *************************************************************************/

	/**
	 * Set the selection model to give edit when clicking on the spanned Cells
	 *
	 * @param spreadsheetView
	 */
	private void setSelectionModel() {
		spreadsheetViewInternal.setSelectionModel(new SpreadsheetViewSelectionModel<>(this,spreadsheetViewInternal));
		getSelectionModel().setCellSelectionEnabled(true);
		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

	}

	/**
	 * Return the TableColumn right after the current TablePosition (including
	 * the ColumSpan to be on a visible Cell)
	 *
	 * @param t the current TablePosition
	 * @return
	 */
	TableColumn<DataRow, ?> getTableColumnSpan(final TablePosition<?,?> t) {
		return spreadsheetViewInternal.getVisibleLeafColumn(t.getColumn() + spreadsheetViewInternal.getItems().get(t.getRow()).getCell(t.getColumn()).getColumnSpan());
	}

	/**
	 * Return the TableColumn right after the current TablePosition (including
	 * the ColumSpan to be on a visible Cell)
	 *
	 * @param t the current TablePosition
	 * @return
	 */
	int getTableColumnSpanInt(final TablePosition<?,?> t) {
		return t.getColumn() + spreadsheetViewInternal.getItems().get(t.getRow()).getCell(t.getColumn()).getColumnSpan();
	}

	/**
	 * Return the Row number right after the current TablePosition (including
	 * the RowSpan to be on a visible Cell)
	 *
	 * @param t
	 * @param spreadsheetView
	 * @return
	 */
	int getTableRowSpan(final TablePosition<?,?> t) {
		return spreadsheetViewInternal.getItems().get(t.getRow()).getCell(t.getColumn()).getRowSpan()
				+ spreadsheetViewInternal.getItems().get(t.getRow()).getCell(t.getColumn()).getRow();
	}

	/**
	 * For a position, return the Visible Cell associated with
	 * It can be the top of the span cell if it's visible,
	 * or it can be the first row visible if we have scrolled
	 * @param row
	 * @param column
	 * @param col
	 * @return
	 */
	TablePosition<DataRow,?> getVisibleCell(int row, TableColumn<DataRow, ?> column, int col) {
		final SpreadsheetView.SpanType spanType = getSpanType(row, col);
		switch (spanType) {
		case NORMAL_CELL:
		case ROW_VISIBLE:
			return new TablePosition<>(spreadsheetViewInternal, row, column);
		case BOTH_INVISIBLE:
		case COLUMN_INVISIBLE:
		case ROW_INVISIBLE:
		default:
			final DataCell<?> cellSpan = spreadsheetViewInternal.getItems().get(row).getCell(col);
			if (!getVisibleRows().isEmpty() && getVisibleRows().first() <= cellSpan.getRow()) {
				return new TablePosition<>(spreadsheetViewInternal, cellSpan.getRow(), spreadsheetViewInternal.getColumns().get(cellSpan.getColumn()));

			} else { // If it's not, then it's the firstkey
				return new TablePosition<>(spreadsheetViewInternal, getVisibleRows().first(),spreadsheetViewInternal.getColumns().get(cellSpan.getColumn()));
			}
		}
	}




	public static class SpreadsheetViewSelectionModel<S> extends TableView.TableViewSelectionModel<DataRow> {

		private boolean ctrl = false;   // Register state of 'ctrl' key
		private boolean shift = false;  // Register state of 'shift' key
		private boolean key = false;    // Register if we last touch the keyboard or the mouse
		private boolean drag = false;	//register if we are dragging (no edition)
		private int itemCount = 0;
		MouseEvent mouseEvent;
		private final TableView<DataRow> tableView;
		private final SpreadsheetView spreadsheetView;

		/**
		 * Make the SpreadsheetViewInternal move when selection operating outside bounds
		 */
		private final Timeline timer = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (mouseEvent != null && !tableView.contains(mouseEvent.getX(), mouseEvent.getY())) {
					if(mouseEvent.getSceneX() < tableView.getLayoutX()) {
						spreadsheetView.getHbar().decrement();
					}else if(mouseEvent.getSceneX() > tableView.getLayoutX()+tableView.getWidth()){
						spreadsheetView.getHbar().increment();
					}
					else if(mouseEvent.getSceneY() < tableView.getLayoutY()) {
						spreadsheetView.getVbar().decrement();
					}else if(mouseEvent.getSceneY() > tableView.getLayoutY()+tableView.getHeight()) {
						spreadsheetView.getVbar().increment();
					}
				}
			}
		}));

		/**
		 * When the drag is over, we remove the listener and stop the timer
		 */
		private final EventHandler<MouseEvent> dragDoneHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				drag = false;
				timer.stop();
				spreadsheetView.removeEventHandler(MouseEvent.MOUSE_RELEASED, this);
			}
		};

		/**
		 * *********************************************************************
		 *                                                                     *
		 * Constructors * *
		 * ********************************************************************
		 */
		public SpreadsheetViewSelectionModel(SpreadsheetView spreadsheetView, final TableView<DataRow> tableView) {
			super(tableView);
			this.tableView = tableView;
			this.spreadsheetView = spreadsheetView;

			tableView.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent t) {
					key = true;
					ctrl = t.isControlDown();
					shift = t.isShiftDown();
				}
			});

			tableView.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent t) {
					key = false;
					ctrl = t.isControlDown();
					shift = t.isShiftDown();
				}
			});
			tableView.setOnDragDetected(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					tableView.addEventHandler(MouseEvent.MOUSE_RELEASED, dragDoneHandler);
					drag = true;
					timer.setCycleCount(Timeline.INDEFINITE);
					timer.play();
				}
			});

			tableView.setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					mouseEvent = e;
				}
			});

			updateItemCount();

			selectedCells = FXCollections.<TablePosition<DataRow,?>>observableArrayList();

			/*
			 * The following two listeners are used in conjunction with
			 * SelectionModel.select(T obj) to allow for a developer to select
			 * an item that is not actually in the data model. When this occurs,
			 * we actively try to find an index that matches this object, going
			 * so far as to actually watch for all changes to the items list,
			 * rechecking each time.
			 */

			// watching for changes to the items list
			tableView.itemsProperty().addListener(weakItemsPropertyListener);

			// watching for changes to the items list content
			if (tableView.getItems() != null) {
				tableView.getItems().addListener(weakItemsContentListener);
			}
		}

		private final ChangeListener<ObservableList<DataRow>> itemsPropertyListener = new ChangeListener<ObservableList<DataRow>>() {
			@Override
			public void changed(ObservableValue<? extends ObservableList<DataRow>> observable,
					ObservableList<DataRow> oldList, ObservableList<DataRow> newList) {
				updateItemsObserver(oldList, newList);
			}
		};
		private final WeakChangeListener<ObservableList<DataRow>> weakItemsPropertyListener =
				new WeakChangeListener<ObservableList<DataRow>>(itemsPropertyListener);

		final ListChangeListener<DataRow> itemsContentListener = new ListChangeListener<DataRow>() {
			@Override public void onChanged(Change<? extends DataRow> c) {
				updateItemCount();

				while (c.next()) {
					final DataRow selectedItem = getSelectedItem();
					final int selectedIndex = getSelectedIndex();

					if (spreadsheetView.getItems() == null || spreadsheetView.getItems().isEmpty()) {
						clearSelection();
					} else if (getSelectedIndex() == -1 && getSelectedItem() != null) {
						final int newIndex = spreadsheetView.getItems().indexOf(getSelectedItem());
						if (newIndex != -1) {
							setSelectedIndex(newIndex);
						}
					} else if (c.wasRemoved() &&
							c.getRemovedSize() == 1 &&
							! c.wasAdded() &&
							selectedItem != null &&
							selectedItem.equals(c.getRemoved().get(0))) {
						// Bug fix for RT-28637
						if (getSelectedIndex() < getItemCount()) {
							final DataRow newSelectedItem = getModelItem(selectedIndex);
							if (! selectedItem.equals(newSelectedItem)) {
								setSelectedItem(newSelectedItem);
							}
						}
					}
				}

				updateSelection(c);
			}
		};

		final WeakListChangeListener weakItemsContentListener = new WeakListChangeListener(itemsContentListener);

		private void updateItemsObserver(ObservableList<DataRow> oldList, ObservableList<DataRow> newList) {
			// the listview items list has changed, we need to observe
			// the new list, and remove any observer we had from the old list
			if (oldList != null) {
				oldList.removeListener(weakItemsContentListener);
			}
			if (newList != null) {
				newList.addListener(weakItemsContentListener);
			}

			updateItemCount();

			// when the items list totally changes, we should clear out
			// the selection
			setSelectedIndex(-1);
		}
		/**
		 * *********************************************************************
		 *                                                                     *
		 * Observable properties (and getters/setters) * *
		 * ********************************************************************
		 */
		// the only 'proper' internal observableArrayList, selectedItems and selectedIndices
		// are both 'read-only and unbacked'.
		private final ObservableList<TablePosition<DataRow, ?>> selectedCells;

		// NOTE: represents selected ROWS only - use selectedCells for more data
		//        private final ReadOnlyUnbackedObservableList<Integer> selectedIndices;
		@Override
		public ObservableList<Integer> getSelectedIndices() {
			return null;
		}

		// used to represent the _row_ backing data for the selectedCells
		//        private final ReadOnlyUnbackedObservableList<S> selectedItems;
		@Override
		public ObservableList<DataRow> getSelectedItems() {
			return null;//selectedItems;
		}

		//        private final ReadOnlyUnbackedObservableList<TablePosition> selectedCellsSeq;
		@Override
		public ObservableList<TablePosition> getSelectedCells() {
			return (ObservableList<TablePosition>)(Object)selectedCells;
		}
		/**
		 * *********************************************************************
		 *                                                                     *
		 * Internal properties * *
		 * ********************************************************************
		 */
		private int previousModelSize = 0;

		// Listen to changes in the tableview items list, such that when it
		// changes we can update the selected indices list to refer to the
		// new indices.
		private void updateSelection(ListChangeListener.Change<? extends DataRow> c) {
			c.reset();
			while (c.next()) {
				if (c.wasReplaced()) {
					if (c.getList().isEmpty()) {
						// the entire items list was emptied - clear selection
						clearSelection();
					} else {
						final int index = getSelectedIndex();

						if (previousModelSize == c.getRemovedSize()) {
							// all items were removed from the model
							clearSelection();
						} else if (index < getItemCount() && index >= 0) {
							// Fix for RT-18969: the list had setAll called on it
							// Use of makeAtomic is a fix for RT-20945
							//makeAtomic = true;
							clearSelection(index);
							//makeAtomic = false;
							select(index);
						} else {
							// Fix for RT-22079
							clearSelection();
						}
					}
				} else if (c.wasAdded() || c.wasRemoved()) {
					final int position = c.getFrom();
					final int shift = c.wasAdded() ? c.getAddedSize() : -c.getRemovedSize();

					if (position < 0) {
						return;
					}
					if (shift == 0) {
						return;
					}

					final List<TablePosition<DataRow,?>> newIndices = new ArrayList<TablePosition<DataRow,?>>(selectedCells.size());

					for (int i = 0; i < selectedCells.size(); i++) {
						final TablePosition<DataRow,?> old = selectedCells.get(i);
						final int oldRow = old.getRow();
						final int newRow = oldRow < position ? oldRow : oldRow + shift;

						// Special case for RT-28637 (See unit test in TableViewTest).
						// Essentially the selectedItem was correct, but selectedItems
						// was empty.
						if (oldRow == 0 && shift == -1) {
							newIndices.add(new TablePosition<>(getTableView(), 0, old.getTableColumn()));
							continue;
						}

						if (newRow < 0) {
							continue;
						}
						newIndices.add(new TablePosition<>(getTableView(), newRow, old.getTableColumn()));
					}

					quietClearSelection();

					// Fix for RT-22079
					for (int i = 0; i < newIndices.size(); i++) {
						final TablePosition<DataRow,?> tp = newIndices.get(i);
						select(tp.getRow(), tp.getTableColumn());
					}
				} else if (c.wasPermutated()) {
					// General approach:
					//   -- detected a sort has happened
					//   -- Create a permutation lookup map (1)
					//   -- dump all the selected indices into a list (2)
					//   -- clear the selected items / indexes (3)
					//   -- create a list containing the new indices (4)
					//   -- for each previously-selected index (5)
					//     -- if index is in the permutation lookup map
					//       -- add the new index to the new indices list
					//   -- Perform batch selection (6)

					// (1)
					final int length = c.getTo() - c.getFrom();
					final HashMap<Integer, Integer> pMap = new HashMap<Integer, Integer> (length);
					for (int i = c.getFrom(); i < c.getTo(); i++) {
						pMap.put(i, c.getPermutation(i));
					}

					// (2)
					final List<TablePosition<DataRow,?>> selectedIndices = new ArrayList<TablePosition<DataRow,?>>((ObservableList<TablePosition<DataRow,?>>)(Object)getSelectedCells());


					// (3)
					clearSelection();

					// (4)
					final List<TablePosition<DataRow,?>> newIndices = new ArrayList<TablePosition<DataRow,?>>(getSelectedIndices().size());

					// (5)
					for (int i = 0; i < selectedIndices.size(); i++) {
						final TablePosition<DataRow,?> oldIndex = selectedIndices.get(i);

						if (pMap.containsKey(oldIndex.getRow())) {
							final Integer newIndex = pMap.get(oldIndex.getRow());
							newIndices.add(new TablePosition<>(oldIndex.getTableView(), newIndex, oldIndex.getTableColumn()));
						}
					}

					// (6)
					quietClearSelection();
					selectedCells.setAll(newIndices);
					//selectedCellsSeq.callObservers(new NonIterableChange.SimpleAddChange<TablePosition<DataRow,?>>(0, newIndices.size(), selectedCellsSeq));
				}
			}

			previousModelSize = getItemCount();
		}

		/**
		 * *********************************************************************
		 *                                                                     *
		 * Public selection API * *
		 * ********************************************************************
		 */
		@Override
		public void clearAndSelect(int row) {
			clearAndSelect(row, null);
		}

		@Override
		public void clearAndSelect(int row, TableColumn<DataRow, ?> column) {
			quietClearSelection();
			select(row, column);
		}

		@Override
		public void select(int row) {
			select(row, null);
		}
		private TablePosition<DataRow, ?> old = null;

		@Override
		public void select(int row, TableColumn<DataRow, ?> column) {

			if (row < 0 || row >= getItemCount()) {
				return;
			}

			// if I'm in cell selection mode but the column is null, I don't want
			// to select the whole row instead...
			if (isCellSelectionEnabled() && column == null) {
				return;
			}
			//Variable we need for algorithm
			TablePosition<DataRow, ?> posFinal = new TablePosition<>(getTableView(), row, column);

			final SpreadsheetView.SpanType spanType = spreadsheetView.getSpanType(row, posFinal.getColumn());

			/**
			 * We check if we are on covered cell. If so we have the
			 * algorithm of the focus model to give the selection to the right cell.
			 *
			 */
			switch (spanType) {
			case ROW_INVISIBLE:
				// If we notice that the new selected cell is the previous one, then it means that we were
				//already on the cell and we wanted to go below.
				// We make sure that old is not null, and that the move is initiated by keyboard.
				//Because if it's a click, then we just want to go on the clicked cell (not below)
				if (old != null && key && !shift
				&& old.getColumn() == posFinal.getColumn()
				&& old.getRow() == posFinal.getRow() - 1) {
					posFinal = spreadsheetView.getVisibleCell(spreadsheetView.getTableRowSpan(old), old.getTableColumn(), old.getColumn());
				} else {
					// If the current selected cell if hidden by row span, we go above
					posFinal = spreadsheetView.getVisibleCell(row, column, posFinal.getColumn());
				}
				break;
			case BOTH_INVISIBLE:
				// If the current selected cell if hidden by a both (row and column) span, we go left-above
				posFinal = spreadsheetView.getVisibleCell(row, column, posFinal.getColumn());
				break;
			case COLUMN_INVISIBLE:
				// If we notice that the new selected cell is the previous one, then it means that we were
				//already on the cell and we wanted to go right.
				if (old != null && key && !shift
				&& old.getColumn() == posFinal.getColumn() - 1
				&& old.getRow() == posFinal.getRow()) {
					posFinal = spreadsheetView.getVisibleCell(old.getRow(), spreadsheetView.getTableColumnSpan(old), spreadsheetView.getTableColumnSpanInt(old));
				} else {
					// If the current selected cell if hidden by column span, we go left
					posFinal = spreadsheetView.getVisibleCell(row, column, posFinal.getColumn());
				}
			default:
				break;
			}

			//This is to handle edition
			if (posFinal.equals(old) && !ctrl && !shift && !drag) {
				// If we are on an Invisible row or both (in diagonal), we need to force the edition
				if (spanType == SpreadsheetView.SpanType.ROW_INVISIBLE || spanType == SpreadsheetView.SpanType.BOTH_INVISIBLE) {
					final TablePosition<DataRow, ?> FinalPos = new TablePosition<>(tableView, posFinal.getRow(), posFinal.getTableColumn());
					final Runnable r = new Runnable() {
						@Override
						public void run() {
							tableView.edit(FinalPos.getRow(), FinalPos.getTableColumn());
						}
					};
					Platform.runLater(r);
				}
			}
			old = posFinal;

			if (!selectedCells.contains(posFinal)) {
				selectedCells.add(posFinal);
			}

			updateScroll(posFinal);
			addSelectedRowsAndColumns(posFinal);

			updateSelectedIndex(posFinal.getRow());
			focus(posFinal.getRow(), posFinal.getTableColumn());
		}

		private void updateScroll(TablePosition<DataRow, ?> posFinal) {

			//spreadsheetView.scrollTo(posFinal.getRow());
			//		spreadsheetView.scrollToColumnIndex(posFinal.getColumn());

			//We try to make visible the rows that may be hiden by Fixed rows
			// We don't want to do any scroll behavior when dragging
			if(!drag && !spreadsheetView.getVisibleRows().isEmpty() && spreadsheetView.getVisibleRows().first()> posFinal.getRow() && !spreadsheetView.getFixedRows().contains(posFinal.getRow())) {
				tableView.scrollTo(posFinal.getRow());
			}

		}

		@Override
		public void select(DataRow obj) {
			if (obj == null && getSelectionMode() == SelectionMode.SINGLE) {
				clearSelection();
				return;
			}

			// We have no option but to iterate through the model and select the
			// first occurrence of the given object. Once we find the first one, we
			// don't proceed to select any others.
			DataRow rowObj = null;
			for (int i = 0; i < getItemCount(); i++) {
				rowObj = getModelItem(i);
				if (rowObj == null) {
					continue;
				}

				if (rowObj.equals(obj)) {
					if (isSelected(i)) {
						return;
					}

					if (getSelectionMode() == SelectionMode.SINGLE) {
						quietClearSelection();
					}

					select(i);
					return;
				}
			}

			// if we are here, we did not find the item in the entire data model.
			// Even still, we allow for this item to be set to the give object.
			// We expect that in concrete subclasses of this class we observe the
			// data model such that we check to see if the given item exists in it,
			// whilst SelectedIndex == -1 && SelectedItem != null.
			setSelectedItem(obj);
		}

		@Override
		public void selectIndices(int row, int... rows) {
			if (rows == null) {
				select(row);
				return;
			}

			/*
			 * Performance optimisation - if multiple selection is disabled, only
			 * process the end-most row index.
			 */
			final int rowCount = getItemCount();

			if (getSelectionMode() == SelectionMode.SINGLE) {
				quietClearSelection();

				for (int i = rows.length - 1; i >= 0; i--) {
					final int index = rows[i];
					if (index >= 0 && index < rowCount) {
						select(index);
						break;
					}
				}

				if (selectedCells.isEmpty()) {
					if (row > 0 && row < rowCount) {
						select(row);
					}
				}
			} else {
				int lastIndex = -1;
				final Set<TablePosition<DataRow,?>> positions = new LinkedHashSet<TablePosition<DataRow,?>>();

				if (row >= 0 && row < rowCount) {
					final TablePosition<DataRow,Object> tp = new TablePosition<DataRow,Object>(getTableView(), row, null);

					if (! selectedCells.contains(tp)) {
						positions.add(tp);
						lastIndex = row;
					}
				}

				for (int i = 0; i < rows.length; i++) {
					final int index = rows[i];
					if (index < 0 || index >= rowCount) {
						continue;
					}
					lastIndex = index;
					final TablePosition<DataRow,Object> pos = new TablePosition<DataRow,Object>(getTableView(), index, null);
					if (! selectedCells.contains(pos)) {
						positions.add(pos);
					}
				}

				selectedCells.addAll(positions);

				if (lastIndex != -1) {
					select(lastIndex);
				}
			}
		}

		@Override
		public void selectAll() {
			if (getSelectionMode() == SelectionMode.SINGLE) {
				return;
			}

			quietClearSelection();

			if (isCellSelectionEnabled()) {
				final List<TablePosition<DataRow,?>> indices = new ArrayList<TablePosition<DataRow,?>>();
				TableColumn<DataRow, ?> column;
				TablePosition<DataRow,?> tp = null;
				for (int col = 0; col < getTableView().getVisibleLeafColumns().size(); col++) {
					column = getTableView().getVisibleLeafColumns().get(col);
					for (int row = 0; row < getItemCount(); row++) {
						tp = new TablePosition<>(getTableView(), row, column);
						indices.add(tp);
					}
				}
				selectedCells.setAll(indices);

				if (tp != null) {
					select(tp.getRow(), tp.getTableColumn());
					focus(tp.getRow(), tp.getTableColumn());
				}
			} else {
				final List<TablePosition<DataRow,?>> indices = new ArrayList<TablePosition<DataRow,?>>();
				for (int i = 0; i < getItemCount(); i++) {
					indices.add(new TablePosition<>(getTableView(), i, null));
				}
				selectedCells.setAll(indices);

				final int focusedIndex = getFocusedIndex();
				if (focusedIndex == -1) {
					select(getItemCount() - 1);
					focus(indices.get(indices.size() - 1));
				} else {
					select(focusedIndex);
					focus(focusedIndex);
				}
			}
		}

		@Override
		public void clearSelection(int index) {
			clearSelection(index, null);
		}

		@Override
		public void clearSelection(int row, TableColumn<DataRow, ?> column) {

			final TablePosition<DataRow, ?> tp = new TablePosition<>(getTableView(), row, column);
			if (isSelectedRange(row, column, tp.getColumn()) != null) {
				final TablePosition<DataRow, ?> tp1 = isSelectedRange(row, column, tp.getColumn());
				selectedCells.remove(tp1);
				removeSelectedRowsAndColumns(tp1);
				focus(tp1.getRow());
			} else {

				final boolean csMode = isCellSelectionEnabled();

				for (final TablePosition<DataRow, ?> pos : getSelectedCells()) {
					if (!csMode && pos.getRow() == row || csMode && pos.equals(tp)) {
						selectedCells.remove(pos);
						removeSelectedRowsAndColumns(pos);

						// give focus to this cell index
						focus(row);

						return;
					}
				}
			}
		}

		@Override
		public void clearSelection() {
			updateSelectedIndex(-1);
			focus(-1);
			quietClearSelection();
		}

		private void quietClearSelection() {
			selectedCells.clear();
			selectedRows.clear();
			selectedColumns.clear();
		}

		@Override
		public boolean isSelected(int index) {
			return isSelected(index, null);
		}

		@Override
		public boolean isSelected(int row, TableColumn<DataRow, ?> column) {
			// When in cell selection mode, we currently do NOT support selecting
			// entire rows, so a isSelected(row, null)
			// should always return false.

			if (isCellSelectionEnabled() && column == null || row <0) {
				return false;
			}
			final TablePosition<DataRow, ?> tp1 = new TablePosition<>(getTableView(), row, column);
			if (isSelectedRange(row, column, tp1.getColumn()) != null) {
				return true;
			} else {
				return false;
			}

			//    System.oufor (TablePosition tp : getSelectedCells()) {
			//            boolean columnMatch = !isCellSelectionEnabled()
			//                    || (column == null && tp.getTableColumn() == null)
			//                    || (column != null && column.equals(tp.getTableColumn()));
			//
			//            if (tp.getRow() == row && columnMatch) {
			//                return true;
			//            }
			//        }
			//        return false;t.println("Is selected"+row+"/"+tp1.getColumn());
			//
		}

		/**
		 * Return the tablePosition of a selected cell inside a spanned cell if any.
		 *
		 * @param row
		 * @param column
		 * @param col
		 * @return
		 */
		public TablePosition<DataRow, ?> isSelectedRange(int row, TableColumn<DataRow, ?> column, int col) {

			if (isCellSelectionEnabled() && column == null && row >=0) {
				return null;
			}

			final DataCell<?> cellSpan = tableView.getItems().get(row).getCell(col);
			final int infRow = cellSpan.getRow();
			final int supRow = infRow + cellSpan.getRowSpan();

			final int infCol = cellSpan.getColumn();
			final int supCol = infCol + cellSpan.getColumnSpan();

			for (final TablePosition<DataRow, ?> tp : getSelectedCells()) {
				//boolean columnMatch = (column != null && column.equals(tp.getTableColumn()));

				if (tp.getRow() >= infRow && tp.getRow() < supRow && tp.getColumn() >= infCol && tp.getColumn() < supCol) {
					return tp;
				}
			}
			return null;
		}

		@Override
		public boolean isEmpty() {
			return selectedCells.isEmpty();
		}

		@Override
		public void selectPrevious() {
			if (isCellSelectionEnabled()) {
				// in cell selection mode, we have to wrap around, going from
				// right-to-left, and then wrapping to the end of the previous line
				final TablePosition<DataRow, ?> pos = getFocusedCell();
				if (pos.getColumn() - 1 >= 0) {
					// go to previous row
					select(pos.getRow(), getTableColumn(pos.getTableColumn(), -1));
				} else if (pos.getRow() < getItemCount() - 1) {
					// wrap to end of previous row
					select(pos.getRow() - 1, getTableColumn(getTableView().getVisibleLeafColumns().size() - 1));
				}
			} else {
				final int focusIndex = getFocusedIndex();
				if (focusIndex == -1) {
					select(getItemCount() - 1);
				} else if (focusIndex > 0) {
					select(focusIndex - 1);
				}
			}
		}

		@Override
		public void selectNext() {
			if (isCellSelectionEnabled()) {
				// in cell selection mode, we have to wrap around, going from
				// left-to-right, and then wrapping to the start of the next line
				final TablePosition<DataRow, ?> pos = getFocusedCell();
				if (pos.getColumn() + 1 < getTableView().getVisibleLeafColumns().size()) {
					// go to next column
					select(pos.getRow(), getTableColumn(pos.getTableColumn(), 1));
				} else if (pos.getRow() < getItemCount() - 1) {
					// wrap to start of next row
					select(pos.getRow() + 1, getTableColumn(0));
				}
			} else {
				final int focusIndex = getFocusedIndex();
				if (focusIndex == -1) {
					select(0);
				} else if (focusIndex < getItemCount() - 1) {
					select(focusIndex + 1);
				}
			}
		}

		@Override
		public void selectAboveCell() {
			final TablePosition<DataRow, ?> pos = getFocusedCell();
			if (pos.getRow() == -1) {
				select(getItemCount() - 1);
			} else if (pos.getRow() > 0) {
				select(pos.getRow() - 1, pos.getTableColumn());
			}
		}

		@Override
		public void selectBelowCell() {
			final TablePosition<DataRow, ?> pos = getFocusedCell();

			if (pos.getRow() == -1) {
				select(0);
			} else if (pos.getRow() < getItemCount() - 1) {
				select(pos.getRow() + 1, pos.getTableColumn());
			}
		}

		@Override
		public void selectFirst() {
			final TablePosition<DataRow, ?> focusedCell = getFocusedCell();

			if (getSelectionMode() == SelectionMode.SINGLE) {
				quietClearSelection();
			}

			if (getItemCount() > 0) {
				if (isCellSelectionEnabled()) {
					select(0, focusedCell.getTableColumn());
				} else {
					select(0);
				}
			}
		}

		@Override
		public void selectLast() {
			final TablePosition<DataRow, ?> focusedCell = getFocusedCell();

			if (getSelectionMode() == SelectionMode.SINGLE) {
				quietClearSelection();
			}

			final int numItems = getItemCount();
			if (numItems > 0 && getSelectedIndex() < numItems - 1) {
				if (isCellSelectionEnabled()) {
					select(numItems - 1, focusedCell.getTableColumn());
				} else {
					select(numItems - 1);
				}
			}
		}

		@Override
		public void selectLeftCell() {
			if (!isCellSelectionEnabled()) {
				return;
			}

			final TablePosition<DataRow, ?> pos = getFocusedCell();
			if (pos.getColumn() - 1 >= 0) {
				select(pos.getRow(), getTableColumn(pos.getTableColumn(), -1));
			}
		}

		@Override
		public void selectRightCell() {
			if (!isCellSelectionEnabled()) {
				return;
			}

			final TablePosition<DataRow, ?> pos = getFocusedCell();
			if (pos.getColumn() + 1 < getTableView().getVisibleLeafColumns().size()) {
				select(pos.getRow(), getTableColumn(pos.getTableColumn(), 1));
			}
		}

		/**
		 * *********************************************************************
		 *                                                                     *
		 * Support code * *
		 * ********************************************************************
		 */
		private TableColumn<DataRow, ?> getTableColumn(int pos) {
			return getTableView().getVisibleLeafColumn(pos);
		}

		//        private TableColumn<S,?> getTableColumn(TableColumn<S,?> column) {
		//            return getTableColumn(column, 0);
		//        }
		// Gets a table column to the left or right of the current one, given an offset
		private TableColumn<DataRow, ?> getTableColumn(TableColumn<DataRow, ?> column, int offset) {
			final int columnIndex = getTableView().getVisibleLeafIndex(column);
			final int newColumnIndex = columnIndex + offset;
			return getTableView().getVisibleLeafColumn(newColumnIndex);
		}

		private void updateSelectedIndex(int row) {
			setSelectedIndex(row);
			setSelectedItem(getModelItem(row));
		}

		@Override
		public void focus(int row) {
			focus(row, null);
		}

		private void focus(int row, TableColumn<DataRow, ?> column) {
			focus(new TablePosition<>(getTableView(), row, column));
		}

		private void focus(TablePosition<DataRow, ?> pos) {
			if (getTableView().getFocusModel() == null) {
				return;
			}

			getTableView().getFocusModel().focus(pos.getRow(), pos.getTableColumn());
		}

		@Override
		public int getFocusedIndex() {
			return getFocusedCell().getRow();
		}

		private TablePosition<DataRow, ?> getFocusedCell() {
			if (getTableView().getFocusModel() == null) {
				return new TablePosition<>(getTableView(), -1, null);
			}
			return getTableView().getFocusModel().getFocusedCell();
		}

		@Override protected int getItemCount() {
			return itemCount;
			//        List<S> items = spreadsheetView.getItems();
			//        return items == null ? -1 : items.size();
		}

		@Override protected DataRow getModelItem(int index) {
			if (index < 0 || index > getItemCount()) {
				return null;
			}
			return tableView.getItems().get(index);
		}

		private void updateItemCount() {
			if (tableView == null) {
				itemCount = -1;
			} else {
				final List<DataRow> items = tableView.getItems();
				itemCount = items == null ? -1 : items.size();
			}
		}
		/*****************************************************************
		 * 				MODIFIED BY NELLARMONIA
		 *****************************************************************/
		/**
		 * A list of Integer with the current selected Rows. This is useful for columnheader and
		 * RowHeader because they need to highligh when a selection is made.
		 */
		private final ObservableList<Integer> selectedRows = FXCollections.observableArrayList();
		public ObservableList<Integer> getSelectedRows() {
			return selectedRows;
		}

		/**
		 * A list of Integer with the current selected Columns. This is useful for columnheader and
		 * RowHeader because they need to highligh when a selection is made.
		 */
		private final ObservableList<Integer> selectedColumns= FXCollections.observableArrayList();
		public ObservableList<Integer> getSelectedColumns() {
			return selectedColumns;
		}

		private void addSelectedRowsAndColumns(TablePosition<?, ?> t){
			final DataCell<?> cell = tableView.getItems().get(t.getRow()).get(t.getColumn());
			for(int i=cell.getRow();i<cell.getRowSpan()+cell.getRow();++i){
				selectedRows.add(i);
				for(int j=cell.getColumn();j<cell.getColumnSpan()+cell.getColumn();++j){
					selectedColumns.add(j);
				}
			}
		}
		private void removeSelectedRowsAndColumns(TablePosition<?, ?> t){
			final DataCell<?> cell = tableView.getItems().get(t.getRow()).get(t.getColumn());
			for(int i=cell.getRow();i<cell.getRowSpan()+cell.getRow();++i){
				selectedRows.remove(Integer.valueOf(i));
				for(int j=cell.getColumn();j<cell.getColumnSpan()+cell.getColumn();++j){
					selectedColumns.remove(Integer.valueOf(j));
				}
			}
		}

		/*****************************************************************
		 * 				END OF MODIFIED BY NELLARMONIA
		 *****************************************************************/
	}
}




class SpreadsheetViewInternal<T> extends TableView<DataRow> {
	public SpreadsheetViewInternal() {
		super();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override protected String getUserAgentStylesheet() {
		return SpreadsheetViewInternal.class.getResource("spreadsheet.css").toExternalForm();
	}

	public void addCell(SpreadsheetCell cell){
		getChildren().add(cell);
	}


}
