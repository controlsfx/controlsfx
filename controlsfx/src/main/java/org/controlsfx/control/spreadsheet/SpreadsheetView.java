/**
 * Copyright (c) 2013, 2019 ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.control.spreadsheet;

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;
import impl.org.controlsfx.spreadsheet.CellView;
import impl.org.controlsfx.spreadsheet.FocusModelListener;
import impl.org.controlsfx.spreadsheet.GridViewBehavior;
import impl.org.controlsfx.spreadsheet.GridViewSkin;
import impl.org.controlsfx.spreadsheet.RectangleSelection.GridRange;
import impl.org.controlsfx.spreadsheet.RectangleSelection.SelectionRange;
import impl.org.controlsfx.spreadsheet.SpreadsheetGridView;
import impl.org.controlsfx.spreadsheet.SpreadsheetHandle;
import impl.org.controlsfx.spreadsheet.TableViewSpanSelectionModel;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.event.WeakEventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Scale;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import org.controlsfx.tools.Utils;

/**
 * The SpreadsheetView is a control similar to the JavaFX {@link TableView}
 * control but with different functionalities and use cases. The aim is to have
 * a powerful grid where data can be written and retrieved.
 * 
 * <h3>Features</h3>
 * <ul>
 * <li>Cells can span in row and in column.</li>
 * <li>Rows can be frozen to the top of the {@link SpreadsheetView} so that they
 * are always visible on screen.</li>
 * <li>Columns can be frozen to the left of the {@link SpreadsheetView} so that
 * they are always visible on screen.</li>
 * <li>A row header can be switched on in order to display the row number.</li>
 * <li>Rows can be resized just like columns with click &amp; drag.</li>
 * <li>Both row and column header can be visible or invisible.</li>
 * <li>Selection of several cells can be made with a click and drag.</li>
 * <li>A copy/paste context menu is accessible with a right-click. The usual
 * shortcuts are also working.</li>
 * <li>{@link Picker} can be placed above column header or to the side of the
 * row header.</li>
 * <li>Rows and columns can be hidden (like Excel grouping).</li>
 * <li>Zoom in and out in order for the SpreadsheetView to fit on a monitor.</li>
 * <li>Rows can be sorted using a {@link Comparator}.</li>
 * </ul>
 * 
 * <br>
 * 
 * <h3>Freezing Rows and Columns</h3> 
 * <br>
 * You can freeze some rows and some columns by right-clicking on their header. A
 * context menu will appear if it's possible to freeze them. When frozen, the label
 * header will then be in italic and the background will turn to dark grey.
 * <br>
 * You have also the possibility to freeze them manually by adding and removing
 * items from {@link #getFixedRows()} and {@link #getFixedColumns()}. But you
 * are strongly advised to check if it's possible to do so with
 * {@link SpreadsheetColumn#isColumnFixable()} for the frozen columns and with
 * {@link #isRowFixable(int)} for the frozen rows.
 * <br>
 *
 * A set of rows cannot be frozen if any cell inside these rows has a row span
 * superior to the number of frozen rows. Likewise, a set of columns cannot be
 * frozen if any cell inside these columns has a column span superior to the
 * number of frozen columns.
 * 
 * <br><br>
 * If you want to freeze several rows or columns together, and they have a span
 * inside, you can call {@link #areRowsFixable(java.util.List) } or  {@link #areSpreadsheetColumnsFixable(java.util.List)
 * }
 * to verify if you can freeze them. Be sure to add them all in once otherwise the
 * system will detect that a span is going out of bounds and will throw an
 * exception.
 *
 * Calling those methods prior
 * every move will ensure that no exception will be thrown.
 * <br><br>
 * You have also the possibility to deactivate these possibilities. For example,
 * you force some row/column to be frozen and then the user cannot change the 
 * settings. 
 * <br>
 * 
 * <h3>Headers</h3>
 * <br>
 * You can also access and toggle header's visibility by using the methods
 * provided like {@link #setShowRowHeader(boolean) } or {@link #setShowColumnHeader(boolean)
 * }.
 * 
 * <br>
 * Users can double-click on a column header will resize the column to the best
 * size in order to fully see each cell in it. Same rule apply for row header.
 * Also note that double-clicking on the little space between two row or two
 * columns (when resizable) will also work just like Excel.
 * 
 * <h3>Pickers</h3>
 * <br>
 * 
 * You can show some little images next to the headers. They will appear on the
 * left of the VerticalHeader and on top on the HorizontalHeader. They are
 * called "picker" because they were used originally for picking a row or a
 * column to insert in the SpreadsheetView.
 * <br>
 * But you can do anything you want with it. Simply put a row or a column index
 * in {@link #getRowPickers() } and {@link #getColumnPickers() } along with an
 * instance of {@link Picker}. You can override the {@link Picker#onClick() }
 * method in order to react when the user click on the picker.
 * <br>
 * The pickers will appear on the top of the column's header and on the left of
 * the row's header.
 * <br>
 *
 * For example, here is a picker displayed for a row that allow to group rows
 * like Excel:
 * <br>
 * <center><img src="pickerExample.png" alt="A Picker that can hide some rows."></center>
 * <br>
 * Once we clicked on the picker (minus sign), the rows are hidden.
 * <br>
 * <center><img src="pickerExample2.png" alt="A Picker that can sho some rows."></center>
 * <br>
 * Here is the code related to the images :
 * <pre>
 * Picker picker = new Picker() {
 *          &#64;Override
 *          public void onClick() {
 *          //If my details are hidden
 *              if (getHiddenRows().get(3)) {
 *                  showRow(3);
 *                  showRow(4);
 *                  showRow(5);
 *                  showRow(6);
 *              } else {
 *                  hideRow(3);
 *                  hideRow(4);
 *                  hideRow(5);
 *                  hideRow(6);
 *              }
 *          }
 * };
 * getRowPickers().put(2, picker);
 *
 * </pre>
 * <h3>Copy pasting</h3> You can copy any cell you want and paste it elsewhere.
 * Be aware that only the value inside will be pasted, not the style nor the
 * type. Thus the value you're trying to paste must be compatible with the
 * {@link SpreadsheetCellType} of the receiving cell. Pasting a Double into a
 * String will work but the reverse operation will not. 
 * <br>
 * See {@link SpreadsheetCellType} <i>Value Verification</i> documentation for more 
 * information.
 * <br>
 * A unique cell or a selection can be copied and pasted.
 * 
 * <br>
 * <br>
 * <h3>Hiding rows and columns</h3>
 * <p>
 * Rows and columns can be hidden if you need to. Simply call {@link #showRow(int)
 * } or {@link #hideRow(int) } in order to toggle the visibility of a row. Same
 * for the column.
 * <br>
 * Note that the span of the cell (in row or column) will automatically adapt
 * based on the visible rows or columns. You have nothing to do.
 *  <br>
 * Because toggling visibility have an impact on the Grid, if you have a lot of
 * rows/columns to show or hide, you may consider setting them all directly by
 * using {@link #setHiddenRows(java.util.BitSet) }. The {@link BitSet} represent
 * all your rows/columns and the bit associated to it represent its visibility.
 *
 * <h3>Zoom</h3>
 * The SpreadsheetView offers the possibility to zoom in or out. This is useful
 * when you have a second monitor and you want your whole grid to fit in. Or
 * when you want to draw the attention on a particular portion of the grid.
 * <br>
 * You can modify the zoom factor by playing with {@link #setZoomFactor(java.lang.Double)
 * }. We recommend using value between 2 and 0.1.
 * <br>
 * Also note that the SpreadsheetView is configured to react when CTRL + and
 * CTRL - are triggered by, respectively, zooming in and zooming out by 10%.
 * Also CTRL 0 will bring the zoom back to default (1).
 * 
 * <h3>Code Samples</h3> Just like the {@link TableView}, you instantiate the
 * underlying model, a {@link Grid}. You will create some rows filled with {@link SpreadsheetCell}.
 * 
 * <br>
 * <br>
 * 
 * <pre>
 * int rowCount = 15;
 *     int columnCount = 10;
 *     GridBase grid = new GridBase(rowCount, columnCount);
 *     
 *     ObservableList&lt;ObservableList&lt;SpreadsheetCell&gt;&gt; rows = FXCollections.observableArrayList();
 *     for (int row = 0; row &lt; grid.getRowCount(); ++row) {
 *         final ObservableList&lt;SpreadsheetCell&gt; list = FXCollections.observableArrayList();
 *         for (int column = 0; column &lt; grid.getColumnCount(); ++column) {
 *             list.add(SpreadsheetCellType.STRING.createCell(row, column, 1, 1,"value"));
 *         }
 *         rows.add(list);
 *     }
 *     grid.setRows(rows);
 *
 *     SpreadsheetView spv = new SpreadsheetView(grid);
 *     
 * </pre>
 * 
 * At that moment you can span some of the cells with the convenient method
 * provided by the grid. Then you just need to instantiate the SpreadsheetView. <br>
 * <h3>Visual:</h3> <center><img src="spreadsheetView.png" alt="Screenshot of SpreadsheetView"></center>
 * 
 * @see SpreadsheetCell
 * @see SpreadsheetCellBase
 * @see SpreadsheetColumn
 * @see Grid
 * @see GridBase
 * @see Picker
 */
public class SpreadsheetView extends Control{

    /***************************************************************************
     * * Static Fields * *
     **************************************************************************/

    /**
     * The SpanType describes in which state each cell can be. When a spanning
     * is occurring, one cell is becoming larger and the others are becoming
     * invisible. Thus, that particular cell is masking the others. <br>
     * <br>
     * But the SpanType cannot be known in advance because it's evolving for
     * each cell during the lifetime of the {@link SpreadsheetView}. Suppose you
     * have a cell spanning in row, the first one is in a ROW_VISIBLE state, and
     * all the other below are in a ROW_SPAN_INVISIBLE state. But if the user is
     * scrolling down, the first will go out of sight. At that moment, the
     * second cell is switching from ROW_SPAN_INVISIBLE state to ROW_VISIBLE
     * state. <br>
     * <br>
     * 
     * <center><img src="spanType.png" alt="Screenshot of SpreadsheetView.SpanType"></center>
     * Refer to {@link SpreadsheetView} for more information.
     */
    public static enum SpanType {

        /**
         * Visible cell, can be a unique cell (no span) or the first one inside
         * a column spanning cell.
         */
        NORMAL_CELL,

        /**
         * Invisible cell because a cell in a NORMAL_CELL state on the left is
         * covering it.
         */
        COLUMN_SPAN_INVISIBLE,

        /**
         * Invisible cell because a cell in a ROW_VISIBLE state on the top is
         * covering it.
         */
        ROW_SPAN_INVISIBLE,

        /** Visible Cell but has some cells below in a ROW_SPAN_INVISIBLE state. */
        ROW_VISIBLE,

        /**
         * Invisible cell situated in diagonal of a cell in a ROW_VISIBLE state.
         */
        BOTH_INVISIBLE;
    }

    /**
     * Default width of the VerticalHeader.
     */
    private static final double DEFAULT_ROW_HEADER_WIDTH = 30.0;
    /***************************************************************************
     * * Private Fields * *
     **************************************************************************/

    private final SpreadsheetGridView cellsView;// The main cell container.
    private SimpleObjectProperty<Grid> gridProperty = new SimpleObjectProperty<>();
    private DataFormat fmt;
    
    private final ObservableList<Integer> fixedRows = FXCollections.observableArrayList();
    private final ObservableList<SpreadsheetColumn> fixedColumns = FXCollections.observableArrayList();

    private final BooleanProperty fixingRowsAllowedProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty fixingColumnsAllowedProperty = new SimpleBooleanProperty(true);

    private final BooleanProperty showColumnHeader = new SimpleBooleanProperty(true, "showColumnHeader", true); //$NON-NLS-1$
    private final BooleanProperty showRowHeader = new SimpleBooleanProperty(true, "showRowHeader", true); //$NON-NLS-1$

    private BitSet rowFix; // Compute if we can fix the rows or not.

    private final ObservableMap<Integer, Picker> rowPickers = FXCollections.observableHashMap();

    private final ObservableMap<Integer, Picker> columnPickers = FXCollections.observableHashMap();

    // Properties needed by the SpreadsheetView and managed by the skin (source
    // is the VirtualFlow)
    private ObservableList<SpreadsheetColumn> columns = FXCollections.observableArrayList();
    private Map<SpreadsheetCellType<?>, SpreadsheetCellEditor> editors = new IdentityHashMap<>();
    private final SpreadsheetViewSelectionModel selectionModel;
    // KeyCombination used for zooming.
    private final KeyCombination zoomOutChar = new KeyCharacterCombination("-", KeyCombination.SHORTCUT_DOWN);
    private final KeyCombination zoomOutKeypad = new KeyCodeCombination(KeyCode.SUBTRACT, KeyCombination.SHORTCUT_DOWN);
    private final KeyCombination zoomInChar = new KeyCharacterCombination("+", KeyCombination.SHORTCUT_DOWN);
    private final KeyCombination zoomInCharShift = new KeyCharacterCombination("+", KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCombination zoomInKeypadAdd = new KeyCodeCombination(KeyCode.ADD, KeyCombination.SHORTCUT_DOWN);
    private final KeyCombination zoomNormalKeypad = new KeyCodeCombination(KeyCode.NUMPAD0, KeyCombination.SHORTCUT_DOWN);
    private final KeyCombination zoomNormal = new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.SHORTCUT_DOWN);
    private final KeyCombination zoomNormalShift = new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

    /**
     * The vertical header width, just for the Label, not the Pickers.
     */
    private final DoubleProperty rowHeaderWidth = new SimpleDoubleProperty(DEFAULT_ROW_HEADER_WIDTH);
    
    //Zoom for the SpreadsheetView.
    private DoubleProperty zoomFactor = new SimpleDoubleProperty(1);
    private static final double MIN_ZOOM = 0.2;
    private static final double MAX_ZOOM = 2;
    private static final double STEP_ZOOM = 0.10;
    //The visible rows.
    private final ObjectProperty<BitSet> hiddenRowsProperty = new SimpleObjectProperty<>();
    //Used to get a row index directly from the ObservableList in filteredList.
    private IdentityHashMap<ObservableList<SpreadsheetCell>, Integer> identityMap;
    private final ObjectProperty<BitSet> hiddenColumnsProperty = new SimpleObjectProperty<>();
    private HashMap<Integer, Integer> rowMap;
    private HashMap<Integer, Integer> columnMap = new HashMap<>();
    private Integer filteredRow;
    private FilteredList<ObservableList<SpreadsheetCell>> filteredList;
    private SortedList<ObservableList<SpreadsheetCell>> sortedList;
    private CellGraphicFactory cellGraphicFactory;

    /**
     * Since the default with applied to TableColumn is 80. If a user sets a
     * width of 80, the column will be detected as having the default with and
     * therefore will be requested to be autosized. In order to prevent that, we
     * must detect which columns has been specifically set and which not. With
     * that BitSet, we are able to make the difference between a "default" 80
     * width applied by the system, and a 80 width applid by a user.
     */
    private final BitSet columnWidthSet = new BitSet();
    // The handle that bridges with implementation.
    final SpreadsheetHandle handle = new SpreadsheetHandle() {
        
        @Override
        protected SpreadsheetView getView() {
            return SpreadsheetView.this;
        }

        @Override
        protected GridViewSkin getCellsViewSkin() {
            return SpreadsheetView.this.getCellsViewSkin();
        }

        @Override
        protected SpreadsheetGridView getGridView() {
            return SpreadsheetView.this.getCellsView();
        }

        @Override
        protected boolean isColumnWidthSet(int indexColumn) {
            return columnWidthSet.get(indexColumn);
        }
    };

    /**
     * @return the inner table view skin
     */
    final GridViewSkin getCellsViewSkin() {
        return (GridViewSkin) (cellsView.getSkin());
    }

    /**
     * @return the inner table view
     */
    final SpreadsheetGridView getCellsView() {
        return cellsView;
    }
    
    /**
     * Used by {@link SpreadsheetColumn} internally in order to specify if a
     * column width has been set by the user.
     *
     * @param indexColumn
     */
    void columnWidthSet(int indexColumn) {
        columnWidthSet.set(indexColumn);
    }

    /***************************************************************************
     * * Constructor * *
     **************************************************************************/

    /**
     * This constructor will generate sample Grid with 100 rows and 15 columns.
     * All cells are typed as String (see {@link SpreadsheetCellType#STRING}).
     */
    public SpreadsheetView(){
        this(getSampleGrid());
        for(SpreadsheetColumn column: getColumns()){
            column.setPrefWidth(100);
        }
    }
  
    /**
     * Sets the CellGraphicFactory that will provide an implementation for cell
     * that have {@link SpreadsheetCell#isCellGraphic() } set to {@code true}.
     *
     * @param cellGraphicFactory the CellGraphicFactory
     */
    public void setCellGraphicFactory(CellGraphicFactory cellGraphicFactory) {
        this.cellGraphicFactory = cellGraphicFactory;
    }

    /**
     * Returns the CellGraphicFactory if set that provide implementation for
     * browser in {@code SpreadsheetCell}.
     *
     * @return the CellGraphicFactory
     */
    public CellGraphicFactory getCellGraphicFactory() {
        return cellGraphicFactory;
    }
    
    /**
     * Creates a SpreadsheetView control with the {@link Grid} specified.
     *
     * @param grid The Grid that contains the items to be rendered
     */
    public SpreadsheetView(final Grid grid) {
        super();
        //We want to recompute the rectangleHeight when a fixedRow is resized.
        addEventHandler(RowHeightEvent.ROW_HEIGHT_CHANGE, (RowHeightEvent event) -> {
            if(getFixedRows().contains(getModelRow(event.getRow())) && getCellsViewSkin() != null){
                getCellsViewSkin().computeFixedRowHeight();
            }
        });
        hiddenRowsProperty.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                computeRowMap();
                initRowFix(getGrid());
            }
        });
        hiddenColumnsProperty.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                computeColumnMap();
                initRowFix(getGrid());
            }
        });
        getStyleClass().add("SpreadsheetView"); //$NON-NLS-1$
        // anonymous skin
        setSkin(new Skin<SpreadsheetView>() {
            @Override
            public Node getNode() {
                return SpreadsheetView.this.getCellsView();
            }

            @Override
            public SpreadsheetView getSkinnable() {
                return SpreadsheetView.this;
            }

            @Override
            public void dispose() {
                // no-op
            }
            
        });

        this.cellsView = new SpreadsheetGridView(handle);
        getChildren().add(cellsView);
        
        /**
         * Add a listener to the selection model in order to edit the spanned
         * cells when clicked
         */
        TableViewSpanSelectionModel tableViewSpanSelectionModel = new TableViewSpanSelectionModel(this,cellsView);
        cellsView.setSelectionModel(tableViewSpanSelectionModel);
        tableViewSpanSelectionModel.setCellSelectionEnabled(true);
        tableViewSpanSelectionModel.setSelectionMode(SelectionMode.MULTIPLE);
        selectionModel = new SpreadsheetViewSelectionModel(this, tableViewSpanSelectionModel);

        /**
         * Set the focus model to track keyboard change and redirect focus on
         * spanned cells
         */
        // We add a listener on the focus model in order to catch when we are on
        // a hidden cell
        cellsView.getFocusModel().focusedCellProperty()
                .addListener((ChangeListener<TablePosition>) (ChangeListener<?>) new FocusModelListener(this,cellsView));

        /**
         * Keyboard action, maybe use an accelerator
         */
        cellsView.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
//        cellsView.setOnKeyPressed(keyPressedHandler);
        /**
         * ContextMenu handling.
         */
        this.contextMenuProperty().addListener(new WeakChangeListener<>(contextMenuChangeListener));
        // The contextMenu creation must be on the JFX thread
        CellView.getValue(() -> {
            setContextMenu(getSpreadsheetViewContextMenu());
        });

        setGrid(grid);
        setEditable(true);
        
        // Listeners & handlers
        fixedRows.addListener(fixedRowsListener);
        fixedColumns.addListener(fixedColumnsListener);
        Scale scale = new Scale(1, 1);
            getTransforms().add(scale);

            zoomFactor.addListener(new ChangeListener<Number>() {
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    scale.setX(newValue.doubleValue());
                    scale.setY(newValue.doubleValue());
                    requestLayout();
                }
            });
        //Zoom
        addEventFilter(ScrollEvent.ANY, (ScrollEvent event) -> {
            if (event.isShortcutDown()) {
                if (event.getTextDeltaY() > 0) {
                    incrementZoom();
                } else {
                    decrementZoom();
                }
                event.consume();
            }
        });
    }
    /***************************************************************************
     * * Public Methods * *
     **************************************************************************/
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        Pos pos = Pos.TOP_LEFT;
        double width = getWidth();
        double height = getHeight();
        double top = getInsets().getTop();
        double right = getInsets().getRight();
        double left = getInsets().getLeft();
        double bottom = getInsets().getBottom();
        double contentWidth = (width - left - right) / zoomFactor.get();
        double contentHeight = (height - top - bottom) / zoomFactor.get();
        layoutInArea(getChildren().get(0), left, top,
                contentWidth, contentHeight,
                0, null,
                pos.getHpos(),
                pos.getVpos());
    }

    /**
     * Return true is this row is hidden.
     *
     * @param row
     * @return true is this row is hidden.
     */
    public boolean isRowHidden(int row) {
        return hiddenRowsProperty.get().get(row);
    }

    /**
     * Return a BitSet of the Hidden rows, where true means the row is hidden.
     *
     * @return a BitSet of the Hidden rows, where true means the row is hidden.
     */
    public BitSet getHiddenRows() {
        return hiddenRowsProperty.get();
    }

    /**
     * Return the Objectproperty wrapping the hidden rows..
     *
     * @return the Objectproperty wrapping the hidden rows..
     */
    public final ObjectProperty<BitSet> hiddenRowsProperty() {
        return hiddenRowsProperty;
    }

    /**
     * Give a complete new BitSet of the hidden rows. The BitSet MUST have the
     * size of {@link Grid#getRowCount() }.
     *
     * @param hiddenRows
     */
    public void setHiddenRows(BitSet hiddenRows) {
        BitSet bitSet = new BitSet(hiddenRows.size());
        bitSet.or(hiddenRows);
        
        this.hiddenRowsProperty.setValue(bitSet);

        requestLayout();
    }

    /**
     * Give a complete new BitSet of the hidden columns. The BitSet MUST have
     * the size of {@link Grid#getColumnCount() () }.
     *
     * @param hiddenColumns
     */
    public void setHiddenColumns(BitSet hiddenColumns) {
        BitSet bitSet = new BitSet(hiddenColumns.size());
        bitSet.or(hiddenColumns);

        this.hiddenColumnsProperty.setValue(bitSet);

        requestLayout();
    }

    /**
     * Return true if this column index (regarding to {@link #getColumns() } is
     * hidden.
     *
     * @param column
     * @return true if this column index (regarding to {@link #getColumns() } is
     * hidden.
     */
    public boolean isColumnHidden(int column) {
        return hiddenColumnsProperty.get().get(column);
    }

    /**
     * Return a BitSet of the Hidden columns, where true means the column is
     * hidden.
     *
     * @return a BitSet of the Hidden columns, where true means the column is
     * hidden.
     */
    public BitSet getHiddenColumns() {
        return hiddenColumnsProperty.get();
    }

    /**
     * Return the Objectproperty wrapping the hidden columns.
     *
     * @return the Objectproperty wrapping the hidden columns.
     */
    public final ObjectProperty<BitSet> hiddenColumnsProperty() {
        return hiddenColumnsProperty;
    }

    /**
     * Return the row where the {@link Filter} will be shown. The row is based
     * on the {@link Grid} indexes.
     *
     * Return -1 if no row is set for the filters.
     * @return the row where the {@link Filter} will be shown.
     */
    public int getFilteredRow() {
        return filteredRow == null ? -1 : filteredRow;
    }

    /**
     * Set the row (based of {@link Grid} indexes) where the {@link Filter} will
     * appear.
     *
     * @param row
     */
    public void setFilteredRow(Integer row) {
        if (row == null || row > getGrid().getRowCount()) {
            filteredRow = null;
        } else {
            filteredRow = row;
        }
    }

    /**
     * Hide the specified row.
     *
     * @param row
     */
    public void hideRow(int row) {
        if (getHiddenRows().get(row)) {
            return;
        }
        getHiddenRows().set(row, true);
        BitSet bitSet = new BitSet(getHiddenRows().size());
        bitSet.or(getHiddenRows());
        setHiddenRows(bitSet);
    }

    /**
     * Hide the specified {@link SpreadsheetColumn}.
     *
     * @param column
     */
    public void hideColumn(SpreadsheetColumn column) {
        int indexColumn = getColumns().indexOf(column);
        if (getHiddenColumns().get(indexColumn)) {
            return;
        }
        getHiddenColumns().set(indexColumn, true);
        BitSet bitSet = new BitSet(getHiddenColumns().size());
        bitSet.or(getHiddenColumns());
        setHiddenColumns(bitSet);
    }

    private void computeRowMap() {
        if (getHiddenRows().isEmpty()) {
            filteredList.setPredicate(null);
        } else {
            filteredList.setPredicate(new Predicate<ObservableList<SpreadsheetCell>>() {
                @Override
                public boolean test(ObservableList<SpreadsheetCell> t) {
                    int index = identityMap.get(t);
                    return !getHiddenRows().get(index) || index == getFilteredRow();
                }
            });
        }
        final int rowCount = getGrid().getRowCount();
        rowMap = new HashMap<>(rowCount);
        int visibleRow = 0;
        for (int i = 0; i < rowCount; ++i) {
            if (!getHiddenRows().get(i)) {
                rowMap.put(i, visibleRow++);
            } else {
                rowMap.put(i, visibleRow);
            }
        }
    }

    private void computeColumnMap() {
        int columnCount = getGrid().getColumnCount();
        columnMap = new HashMap<>(columnCount);

        //Toggling visibility can cause NotOnFxThread Exception.
        CellView.getValue(() -> {
            //Column count can have changed..
            final int columnSize = getColumns().size();
            int totalColumn = getGrid().getColumnCount();
            int visibleColumn = 0;
            for (int i = 0; i < totalColumn; ++i) {
                if (!getHiddenColumns().get(i)) {
                    if (i < columnSize) {
                        getColumns().get(i).column.setVisible(true);
                    }
                    columnMap.put(i, visibleColumn++);
                } else {
                    if (i < columnSize) {
                        getColumns().get(i).column.setVisible(false);
                    }
                    columnMap.put(i, visibleColumn);
                }
            }
        });
    }

    /**
     * Show the specified row.
     *
     * @param row
     */
    public void showRow(int row) {
        if (!getHiddenRows().get(row)) {
            return;
        }
        getHiddenRows().set(row, false);
        BitSet bitSet = new BitSet(getHiddenRows().size());
        bitSet.or(getHiddenRows());
        setHiddenRows(bitSet);
    }

    /**
     * Show the specified {@link SpreadsheetColumn}.
     *
     * @param column
     */
    public void showColumn(SpreadsheetColumn column) {
        int indexColumn = getColumns().indexOf(column);
        if (!getHiddenColumns().get(indexColumn)) {
            return;
        }
        getHiddenColumns().set(indexColumn, false);
        BitSet bitSet = new BitSet(getHiddenColumns().size());
        bitSet.or(getHiddenColumns());
        setHiddenColumns(bitSet);
    }

    /**
     * Given a row index base on the {@link Grid}, return the index used in the
     * SpreadsheetView. Beware ,if the row is hidden, the returned index is not
     * relevant because no row is assigned to it.
     *
     * @param modelRow
     * @return the index used in the SpreadsheetView.
     */
    public int getFilteredRow(int modelRow) {
        try {
            return rowMap.get(modelRow);
        } catch (NullPointerException ex) {
            return modelRow;
        }
    }

    /**
     * Given a column index based on the {@link #getColumns() } list, return an
     * index based on the visible columns in the SpreadsheetView.
     *
     * @param modelColumn
     * @return an index based on the visible columns in the SpreadsheetView.
     */
    public int getViewColumn(int modelColumn) {
        try {
            return columnMap.get(modelColumn);
        } catch (NullPointerException ex) {
            return modelColumn;
        }
    }

    /**
     * Given a column index based on the visible column list, for example when
     * dealing with {@link TablePosition#getColumn() }. It returns an index
     * based on the {@link #getColumns() } list of the SpreadsheetView.
     *
     * @param viewColumn
     * @return an index based on the {@link #getColumns() } list of the
     * SpreadsheetView.
     */
    public int getModelColumn(int viewColumn) {
        try {
            return cellsView.getColumns().indexOf(cellsView.getVisibleLeafColumn(viewColumn));
        } catch (NullPointerException ex) {
            return viewColumn;
        }
    }

    /**
     * Given the row of a {@code SpreadsheetCell}, returns the actual row as displayed
     * in the {@code SpreadsheetView}. Beware as it can be a time-consuming operation.
     * Also, calling this method on a row that it hidden will return incoherent
     * information.
     *
     * @param modelRow the row retrieved in {@link SpreadsheetCell#getRow() }
     * @return the ViewRow if possible, -1 or another row if the row is hidden.
     */
    public int getViewRow(int modelRow) {
        //First translate the modelRow to the filtered row.
        modelRow = getFilteredRow(modelRow);
        //If the grid is sorted, we retrieve the view row.
        if (getComparator() != null) {
            modelRow = getViewIndex(modelRow);
        }
        return modelRow;
    }

    private int getViewIndex(int sourceIndex) {
        //Improved in JDK9 with https://bugs.openjdk.java.net/browse/JDK-8139848
        return sortedList.getViewIndex(sourceIndex);
//        int max = sortedList.size();
//        for (int i = 0; i < max; i++) {
//            if (sortedList.getSourceIndex(i) == sourceIndex) {
//                return i;
//            }
//        }
//        return -1;
    }
    
    /**
     * Given an index on the {@code SpreadsheetView}, return a {@link Grid}
     * index it is related to.
     *
     * @param viewRow a row index based on the {@code SpreadsheetView}
     * @return a {@link Grid} index it is related to.
     */
    public int getModelRow(int viewRow) {
        if (viewRow < 0 || viewRow >= sortedList.size()) {
            return viewRow;
        }
        try {
            return getFilteredSourceIndex(sortedList.getSourceIndex(viewRow));
        } catch (NullPointerException | IndexOutOfBoundsException ex) {
            return viewRow;
        }
    }

    /**
     * Given an index on the SpreadsheetView, it will return the model row by
     * simply considering the hidden rows (and not the actual sort if any).
     *
     * If you hide the row 2, it means the row 2 in the SpreadsheetView will
     * actually display the row 3 in the model {@link Grid}. Thus calling this
     * method with the number 2 will give you the number 3.
     *
     * @param viewRow
     * @return the model row
     */
    public int getFilteredSourceIndex(int viewRow) {
        if (viewRow < 0 || viewRow >= filteredList.size()) {
            return viewRow;
        }
        try {
            return filteredList.getSourceIndex(viewRow);
        } catch (NullPointerException | IndexOutOfBoundsException ex) {
            return viewRow;
        }
    }

    /**
     * Return the current row span for the given cell at the given position in
     * the Table.
     *
     * If a sort is applied to the SpreadsheetView, some spanned cells may be
     * splitted thus explaining why this method can give a different value than {@link SpreadsheetCell#getRowSpan()
     * }.
     *
     * @param cell the considered {@code SpreadsheetCell}
     * @param index the current row position of this cell
     * @return the current row span for the given cell
     */
    public int getRowSpan(SpreadsheetCell cell, int index) {
        /**
         * We return here the exact rowSpan of the considered index. So if a
         * cell span on 4 but we give the second index, the rowspan will be 2.
         */
        int rowSpan = 0;
        do {
            ++rowSpan;
        } while (++index < sortedList.size() && cell.getColumn() < getGrid().getColumnCount()
                && sortedList.get(index).get(cell.getColumn()) == cell);

        return rowSpan;
    }
   
    /**
     * Return the exact opposite of {@link #getRowSpan(org.controlsfx.control.spreadsheet.SpreadsheetCell, int)
     * }. If a cell is spanned on rows, and the index given is the last one of
     * the spanned zone, it's rowSpan will be 1 and its reverse rowspan will be {@link SpreadsheetCell#getRowSpan()
     * }.
     *
     * @param cell the considered {@code SpreadsheetCell}
     * @param index the current row position of this cell
     * @return the current reverse row span for the given cell
     */
    public int getReverseRowSpan(SpreadsheetCell cell, int index) {
        /**
         * We return here the exact rowSpan of the considered index. So if a
         * cell span on 4 but we give the second index, the rowspan will be 2.
         */
        int rowSpan = 0;
        do {
            ++rowSpan;
        } while (--index >= 0 && cell.getColumn() < getGrid().getColumnCount()
                && sortedList.get(index).get(cell.getColumn()) == cell);
        return rowSpan;
    }

    /**
     * Return the row span for the given cell without considering the actual
     * sort. Only the hidden rows are considered.
     *
     * @param cell
     * @return the row span for the given cell.
     */
    public int getRowSpanFilter(SpreadsheetCell cell) {
        int rowSpan = cell.getRowSpan();
        //First remove the filtered
        for (int i = cell.getRow(); i < cell.getRow() + cell.getRowSpan(); ++i) {
            rowSpan -= getHiddenRows().get(i) ? 1 : 0;
        }
        return rowSpan;
    }

    /**
     * Return the current list of rows set in the SpreadsheetView as they appear
     * on the screen.
     *
     * @return the current list of rows.
     */
    public ObservableList<ObservableList<SpreadsheetCell>> getItems() {
        return cellsView.getItems();
    }

    /**
     * Return the current column span of a Cell considering all hidden columns.
     *
     * @param cell
     * @return the current column span of a Cell.
     */
    public int getColumnSpan(SpreadsheetCell cell) {
        int colSpan = cell.getColumnSpan();
        for (int i = cell.getColumn(); i < cell.getColumn() + cell.getColumnSpan(); ++i) {
            colSpan -= getHiddenColumns().get(i) ? 1 : 0;
        }
        return colSpan;
    }

    /**
     * Return the zoomFactor used for the SpreadsheetView.
     *
     * @return the zoomFactor used for the SpreadsheetView.
     */
    public final Double getZoomFactor() {
        return zoomFactor.get();
    }

    /**
     * Set a new zoomFactor for the SpreadsheetView. Advice is not to go beyond
     * 2 and below 0.1.
     *
     * @param zoomFactor
     */
    public final void setZoomFactor(Double zoomFactor) {
        this.zoomFactor.set(zoomFactor);
    }

    /**
     * Return the zoomFactor used for the SpreadsheetView.
     *
     * @return the zoomFactor used for the SpreadsheetView.
     */
    public final DoubleProperty zoomFactorProperty() {
        return zoomFactor;
    }

    /**
     * Increment the level of zoom by 0.10. The base is 1 so we will try to stay
     * of the intervals.
     *
     */
    public void incrementZoom() {
        double newZoom = zoomFactor.getValue() + STEP_ZOOM;
        newZoom *= 10;
        newZoom = Math.floor((float) newZoom);
        newZoom /= 10;
        zoomFactor.setValue(newZoom > MAX_ZOOM ? MAX_ZOOM : newZoom);
    }

    /**
     * Decrement the level of zoom by 0.10. It will block at 0.20. The base is 1
     * so we will try to stay of the intervals.
     */
    public void decrementZoom() {
        double newZoom = zoomFactor.getValue() - STEP_ZOOM;
        newZoom *= 10;
        newZoom = Math.ceil((float) newZoom);
        newZoom /= 10;
        zoomFactor.setValue(newZoom < MIN_ZOOM ? MIN_ZOOM : newZoom);
    }

    /**
     * Causes the cell at the given row/column view indexes to switch into
     * its editing state, if it is not already in it, and assuming that the
     * SpreadsheetView and column are also editable.
     *
     * <p><strong>Note:</strong> This method will cancel editing if the given row
     * value is less than zero and the given column is null.</p>
     * @param row
     * @param column
     */
    public void edit(int row, SpreadsheetColumn column) {
        cellsView.edit(row, column == null ? null : column.column);
    }
    
    /**
     * Return the comparator used in the {@link SortedList} for the
     * SpreadsheetView.
     *
     * @return the comparator used in the {@link SortedList} for the
     * SpreadsheetView.
     */
    public Comparator getComparator() {
        return sortedList == null ? null : sortedList.getComparator();
    }

    /**
     * Return an ObjectProperty wrapping the comparator used in the
     * SpreadsheetView.
     *
     * @return an ObjectProperty wrapping the comparator used in the
     * SpreadsheetView.
     */
    public ObjectProperty<Comparator<? super ObservableList<SpreadsheetCell>>> comparatorProperty() {
        return sortedList.comparatorProperty();
    }

    /**
     * Sets a new Comparator for the SpreadsheetView in order to sort the rows.
     *
     * @param comparator the comparator that will sort the rows.
     */
    public void setComparator(Comparator<ObservableList<SpreadsheetCell>> comparator) {
        sortedList.setComparator(comparator);
        computeRowMap();
        requestLayout();
    }
    /**
     * Set a new Grid for the SpreadsheetView. This will be called by default by
     * {@link #SpreadsheetView(Grid)}. So this is useful when you want to
     * refresh your SpreadsheetView with a new model. This will keep the state
     * of your SpreadsheetView (position of the bar, number of frozen rows etc).
     * 
     * @param grid the new Grid
     */
    public final void setGrid(Grid grid) {
        if(grid == null){
            return;
        }
        // Reactivate that after
//        verifyGrid(grid);
        filteredList = new FilteredList<>(grid.getRows());
        sortedList = new SortedList<>(filteredList);
        gridProperty.set(grid);
        setHiddenRows(new BitSet(filteredList.getSource().size()));
        setHiddenColumns(new BitSet(grid.getColumnCount()));
        initRowFix(grid);

        /**
         * We need to verify that the previous fixedRows are still compatible
         * with our new model
         */

        List<Integer> newFixedRows = new ArrayList<>();
        for (Integer rowFixed : getFixedRows()) {
            if (isRowFixable(rowFixed)) {
                newFixedRows.add(rowFixed);
            }
        }
        getFixedRows().setAll(newFixedRows);

        /**
         * We need to store the index of the fixedColumns and clear then because
         * we will keep reference to SpreadsheetColumn that no longer exist.
         */
        List<Integer> columnsFixed = new ArrayList<>();
        for (SpreadsheetColumn column : getFixedColumns()) {
            columnsFixed.add(getColumns().indexOf(column));
        }
        getFixedColumns().clear();

        /**
         * We try to save the width of the column as we save the height of our rows so that we preserve the state.
         */
        List<Double> widthColumns = new ArrayList<>();
        for (SpreadsheetColumn column : columns) {
            widthColumns.add(column.getWidth());
        }
        //We need to update the focused cell afterwards
        Pair<Integer, Integer> focusedPair = null;
        TablePosition focusedCell = cellsView.getFocusModel().getFocusedCell();
        if (focusedCell != null && focusedCell.getRow() != -1 && focusedCell.getColumn() != -1) {
            focusedPair = new Pair(focusedCell.getRow(), focusedCell.getColumn());
        }

        final Pair<Integer, Integer> finalPair = focusedPair;
        
        if (grid.getRows() != null) {
//            final ObservableList<ObservableList<SpreadsheetCell>> observableRows = FXCollections
//                    .observableArrayList(grid.getRows());
//            cellsView.getItems().clear();
            cellsView.setItems(sortedList);
            computeRowMap();

            final int columnCount = grid.getColumnCount();
            columns.clear();
            for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
                final SpreadsheetColumn spreadsheetColumn = new SpreadsheetColumn(getTableColumn(grid, columnIndex), this, columnIndex, grid);
                if(widthColumns.size() > columnIndex){
                    spreadsheetColumn.setPrefWidth(widthColumns.get(columnIndex));
                }
                columns.add(spreadsheetColumn);
                // We verify if this column was fixed before and try to re-fix
                // it.
                if (columnsFixed.contains((Integer) columnIndex) && spreadsheetColumn.isColumnFixable()) {
                    spreadsheetColumn.setFixed(true);
                }
            }
        }
        
        List<Pair<Integer, Integer>> selectedCells = new ArrayList<>();
        for (TablePosition position : getSelectionModel().getSelectedCells()) {
            selectedCells.add(new Pair<>(position.getRow(), position.getColumn()));
        }
        
        
        /**
         * Since the TableView is added to the sceneGraph, it's not possible to
         * modify the columns in another thread. We normally should call
         * Platform.runLater() and exit. But in this particular case, we need to
         * add the tableColumn right now. So that when we exit this "setGrid"
         * method, we are sure we can manipulate all the elements.
         *
         * We also try to be smart here when we already have some columns in
         * order to re-use them and minimize the time used to add/remove
         * columns.
         */
        Runnable runnable = () -> {
            if (cellsView.getColumns().size() > grid.getColumnCount()) {
                cellsView.getColumns().remove(grid.getColumnCount(), cellsView.getColumns().size());
            } else if (cellsView.getColumns().size() < grid.getColumnCount()) {
                for (int i = cellsView.getColumns().size(); i < grid.getColumnCount(); ++i) {
                    cellsView.getColumns().add(columns.get(i).column);
                }
            }
            ((TableViewSpanSelectionModel) cellsView.getSelectionModel()).verifySelectedCells(selectedCells);
            //Just like the selected cell we update the focused cell.
            if(finalPair != null && finalPair.getKey() < getGrid().getRowCount() && finalPair.getValue() < getGrid().getColumnCount()){
                cellsView.getFocusModel().focus(finalPair.getKey(), cellsView.getColumns().get(finalPair.getValue()));
            }
        };
        
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            try {
                FutureTask future = new FutureTask(runnable, null);
                Platform.runLater(future);
                future.get();
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(SpreadsheetView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Return a {@link TablePosition} of cell being currently edited.
     * 
     * @return a {@link TablePosition} of cell being currently edited.
     */
    public TablePosition<ObservableList<SpreadsheetCell>, ?> getEditingCell() {
        return cellsView.getEditingCell();
    }
    
    /**
     * Represents the current cell being edited, or null if there is no cell
     * being edited.
     *
     * @return the current cell being edited, or null if there is no cell being
     * edited.
     */
    public ReadOnlyObjectProperty<TablePosition<ObservableList<SpreadsheetCell>, ?>> editingCellProperty() {
        return cellsView.editingCellProperty();
    }

    /**
     * Return an ObservableList of the {@link SpreadsheetColumn} used. This list
     * is filled automatically by the SpreadsheetView. Adding and removing
     * columns should be done in the model {@link Grid}.
     *
     * @return An ObservableList of the {@link SpreadsheetColumn}
     */
    public final ObservableList<SpreadsheetColumn> getColumns() {
        return columns;
    }

    /**
     * Return the model Grid used by the SpreadsheetView
     * 
     * @return the model Grid used by the SpreadsheetView
     */
    public final Grid getGrid() {
        return gridProperty.get();
    }

    /**
     * Return a {@link ReadOnlyObjectProperty} containing the current Grid
     * used in the SpreadsheetView.
     * @return a {@link ReadOnlyObjectProperty}.
     */
    public final ReadOnlyObjectProperty<Grid> gridProperty() {
        return gridProperty;
    }

    /**
     * You can freeze or unfreeze a row by modifying this list. Call
     * {@link #isRowFixable(int)} before trying to freeze a row. See
     * {@link SpreadsheetView} description for information.
     *
     * @return an ObservableList of integer representing the frozen rows.
     */
    public ObservableList<Integer> getFixedRows() {
        return fixedRows;
    }

    /**
     * Indicate whether a row can be frozen or not. Call that method before
     * adding an item with {@link #getFixedRows()} .
     *
     * A row cannot be frozen alone if any cell inside the row has a row span
     * superior to one.
     *
     * @param row
     * @return true if the row can be frozen.
     */
    public boolean isRowFixable(int row) {
        return row >= 0 && row < rowFix.size() && isFixingRowsAllowed() ? rowFix.get(row) : false;
    }
    
    /**
     * Indicates whether a List of rows can be frozen or not.
     *
     * A set of rows cannot be frozen if any cell inside these rows has a row
     * span superior to the number of frozen rows.
     *
     * @param list
     * @return true if the List of row can be frozen together.
     */
    public boolean areRowsFixable(List<? extends Integer> list) {
        if(list == null || list.isEmpty() || !isFixingRowsAllowed()){
            return false;
        }
        final Grid grid = getGrid();
        final int rowCount = grid.getRowCount();
        final ObservableList<ObservableList<SpreadsheetCell>> rows = grid.getRows();
        for (Integer row : list) {
            if (row == null || row < 0 || row >= rowCount) {
                return false;
            }
            //If this row is not fixable, we need to identify the maximum span
            if (!isRowFixable(row)) {
                int maxSpan = 1;
                List<SpreadsheetCell> gridRow = rows.get(row);
                for (SpreadsheetCell cell : gridRow) {
                    //If the original row is not within this range, there is not need to look deeper.
                    if (!list.contains(cell.getRow())) {
                        return false;
                    }
                    //We only want to consider the original cell.
                    if (getRowSpan(cell, row) > maxSpan && cell.getRow() == row) {
                        maxSpan = cell.getRowSpan();
                    }
                }
                //Then we need to verify that all rows within that span are fixed.
                int count = row + maxSpan - 1;
                for (int index = row + 1; index <= count; ++index) {
                    if (!list.contains(index)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Return whether change to frozen rows are allowed.
     *
     * @return whether change to frozen rows are allowed.
     */
    public boolean isFixingRowsAllowed() {
        return fixingRowsAllowedProperty.get();
    }

    /**
     * If set to true, user will be allowed to freeze and unfreeze the rows.
     *
     * @param b
     */
    public void setFixingRowsAllowed(boolean b) {
        fixingRowsAllowedProperty.set(b);
    }

    /**
     * Return the Boolean property associated with the allowance of freezing or
     * unfreezing some rows.
     *
     * @return the Boolean property associated with the allowance of freezing or
     * unfreezing some rows.
     */
    public ReadOnlyBooleanProperty fixingRowsAllowedProperty() {
        return fixingRowsAllowedProperty;
    }

    /**
     * You can freeze or unfreeze a column by modifying this list. Call
     * {@link SpreadsheetColumn#isColumnFixable()} on the column before adding
     * an item.
     *
     * @return an ObservableList of the frozen columns.
     */
    public ObservableList<SpreadsheetColumn> getFixedColumns() {
        return fixedColumns;
    }

    /**
     * Indicate whether this column can be frozen or not. If you have a
     * {@link SpreadsheetColumn}, call
     * {@link SpreadsheetColumn#isColumnFixable()} on it directly. Call that
     * method before adding an item with {@link #getFixedColumns()} .
     *
     * @param columnIndex
     * @return true if the column if freezable 
     */
    public boolean isColumnFixable(int columnIndex) {
        return columnIndex >= 0 && columnIndex < getColumns().size() && isFixingColumnsAllowed()
                ? getColumns().get(columnIndex).isColumnFixable() : false;
    }

    /**
     * Indicates whether a List of {@link SpreadsheetColumn} can be fixed or
     * not.
     *
     * A set of columns cannot be frozen if any cell inside these columns has a
     * column span superior to the number of frozen columns.
     *
     * @param list
     * @return true if the List of columns can be frozen together.
     */
    public boolean areSpreadsheetColumnsFixable(List<? extends SpreadsheetColumn> list) {
        List<Integer> newList = new ArrayList<>();
        for (SpreadsheetColumn column : list) {
            if (column != null) {
                newList.add(columns.indexOf(column));
            }
        }
        return areColumnsFixable(newList);
    }

    /**
     * This method is the same as {@link #areSpreadsheetColumnsFixable(java.util.List)
     * } but is using a List of {@link SpreadsheetColumn} indexes.
     *
     * A set of columns cannot be frozen if any cell inside these columns has a
     * column span superior to the number of frozen columns.
     *
     * @param list
     * @return true if the List of columns can be frozen together.
     */
    public boolean areColumnsFixable(List<? extends Integer> list) {
        if (list == null || list.isEmpty() || !isFixingRowsAllowed()) {
            return false;
        }
        final Grid grid = getGrid();
        final int columnCount = grid.getColumnCount();
        final ObservableList<ObservableList<SpreadsheetCell>> rows = grid.getRows();
        for (Integer columnIndex : list) {
            if (columnIndex == null || columnIndex < 0 || columnIndex >= columnCount) {
                return false;
            }
            //If this column is not fixable, we need to identify the maximum span
            if (!isColumnFixable(columnIndex)) {
                int maxSpan = 1;
                SpreadsheetCell cell;
                for (List<SpreadsheetCell> row : rows) {
                    cell = row.get(columnIndex);
                    //If the original column is not within this range, there is not need to look deeper.
                    if (!list.contains(cell.getColumn())) {
                        return false;
                    }
                    //We only want to consider the original cell.
                    if (cell.getColumnSpan() > maxSpan && cell.getColumn() == columnIndex) {
                        maxSpan = cell.getColumnSpan();
                    }
                }
                //Then we need to verify that all columns within that span are fixed.
                int count = columnIndex + maxSpan - 1;
                for (int index = columnIndex + 1; index <= count; ++index) {
                    if (!list.contains(index)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Return whether change to frozen columns are allowed.
     *
     * @return whether change to frozen columns are allowed.
     */
    public boolean isFixingColumnsAllowed() {
        return fixingColumnsAllowedProperty.get();
    }

    /**
     * If set to true, user will be allowed to freeze and unfreeze the columns.
     *
     * @param b
     */
    public void setFixingColumnsAllowed(boolean b) {
        fixingColumnsAllowedProperty.set(b);
    }

    /**
     * Return the Boolean property associated with the allowance of freezing or
     * unfreezing some columns.
     *
     * @return the Boolean property associated with the allowance of freezing or
     * unfreezing some columns.
     */
    public ReadOnlyBooleanProperty fixingColumnsAllowedProperty() {
        return fixingColumnsAllowedProperty;
    }

    /**
     * Activate and deactivate the Column Header
     *
     * @param b
     */
    public final void setShowColumnHeader(final boolean b) {
        showColumnHeader.setValue(b);
    }

    /**
     * Return if the Column Header is showing.
     *
     * @return a boolean telling whether the column Header is shown
     */
    public final boolean isShowColumnHeader() {
        return showColumnHeader.get();
    }

    /**
     * BooleanProperty associated with the column Header.
     *
     * @return the BooleanProperty associated with the column Header.
     */
    public final BooleanProperty showColumnHeaderProperty() {
        return showColumnHeader;
    }

    /**
     * Activate and deactivate the Row Header.
     *
     * @param b
     */
    public final void setShowRowHeader(final boolean b) {
        showRowHeader.setValue(b);
    }

    /**
     * Return if the row Header is showing.
     *
     * @return a boolean telling if the row Header is being shown
     */
    public final boolean isShowRowHeader() {
        return showRowHeader.get();
    }

    /**
     * BooleanProperty associated with the row Header.
     *
     * @return the BooleanProperty associated with the row Header.
     */
    public final BooleanProperty showRowHeaderProperty() {
        return showRowHeader;
    }

    /**
     * This DoubleProperty represents the with of the rowHeader. This is just
     * representing the width of the Labels, not the pickers.
     *
     * @return A DoubleProperty.
     */
    public final DoubleProperty rowHeaderWidthProperty(){
        return rowHeaderWidth;
    }
    
    /**
     * Specify a new width for the row header.
     *
     * @param value
     */
    public final void setRowHeaderWidth(double value){
        rowHeaderWidth.setValue(value);
    }
    
    /**
     *
     * @return the current width of the row header.
     */
    public final double getRowHeaderWidth(){
        return rowHeaderWidth.get();
    }
    
    /**
     * @return An ObservableMap with the row index as key and the Picker as a
     * value.
     */
    public ObservableMap<Integer, Picker> getRowPickers() {
        return rowPickers;
    }

    /**
     * @return An ObservableMap with the column index as key and the Picker as a
     * value.
     */
    public ObservableMap<Integer, Picker> getColumnPickers() {
        return columnPickers;
    }

    /**
     * This method will compute the best height for each line. That is to say
     * a height where each content of each cell could be fully visible.\n
     * Use this method wisely because it can degrade performance on great grid.
     */
    public void resizeRowsToFitContent() {
        if (getCellsViewSkin() != null) {
            getCellsViewSkin().resizeRowsToFitContent();
        }
    }
    
    /**
     * This method will first apply {@link #resizeRowsToFitContent() } and then
     * take the highest height and apply it to every row.\n
     * Just as {@link #resizeRowsToFitContent() }, this method can be degrading
     * your performance on great grid.
     */
    public void resizeRowsToMaximum(){
        if (getCellsViewSkin() != null) {
            getCellsViewSkin().resizeRowsToMaximum();
        }
    }
    
    /**
     * This method will wipe all changes made to the row's height and set all row's
     * height back to their default height defined in the model Grid.
     */
    public void resizeRowsToDefault() {
        if (getCellsViewSkin() != null) {
            getCellsViewSkin().resizeRowsToDefault();
        }
    }
    
    /**
     * @param row
     * @return the height of a particular row of the SpreadsheetView.
     */
    public double getRowHeight(int row) {
        //Sometime, the skin is not initialised yet..
        if (getCellsViewSkin() == null) {
            return getGrid().getRowHeight(row);
        } else {
            return getCellsViewSkin().getRowHeight(row);
        }
    }
    
    /**
     * Return the selectionModel used by the SpreadsheetView. 
     * 
     * @return {@link SpreadsheetViewSelectionModel}
     */
    public SpreadsheetViewSelectionModel getSelectionModel() {
        return selectionModel;
    }
    
    /**
     * Scrolls the {@code SpreadsheetView} so that the given row is visible.
     * Beware, you must call {@link #getViewRow(int) } before if you are using {@link SpreadsheetCell#getRow()
     * } and the grid is sorted/filtered.
     *
     * @param row the row to scroll to
     */
    public void scrollToRow(int row) {
        cellsView.scrollTo(row);
    }
    
    /**
     * Same method as {@link ScrollBar#setValue(double) } on the verticalBar.
     *
     * @param value
     */
    public void setVBarValue(double value) {
        if (getCellsViewSkin() == null) {
            Platform.runLater(() -> {
                setVBarValue(value);
            });
            return;
        }
        getCellsViewSkin().getVBar().setValue(value);
    }

    /**
     * Same method as {@link ScrollBar#setValue(double) } on the verticalBar.
     *
     * @param value
     */
    public void setHBarValue(double value) {
        setHBarValue(value,0);
    }
    
    private void setHBarValue(double value, int attempt) {
        if(attempt > 10){
            return;
        }
        if (getCellsViewSkin() == null) {
            final int newAttempt = ++attempt;
            Platform.runLater(() -> {
                setHBarValue(value, newAttempt);
            });
            return;
        }
        getCellsViewSkin().setHbarValue(value);
    }

    /**
     * Return the value of the vertical scrollbar. See {@link ScrollBar#getValue()
     * }
     *
     * @return the value of the vertical scrollbar.
     */
    public double getVBarValue() {
        if (getCellsViewSkin() != null && getCellsViewSkin().getVBar() != null) {
            return getCellsViewSkin().getVBar().getValue();
        }
        return 0.0;
    }

    /**
     * Return the value of the horizontal scrollbar. See {@link ScrollBar#getValue()
     * }
     *
     * @return the value of the horizontal scrollbar.
     */
    public double getHBarValue() {
        if (getCellsViewSkin() != null && getCellsViewSkin().getHBar() != null) {
            return getCellsViewSkin().getHBar().getValue();
        }
        return 0.0;
    }
    
    /**
     * Scrolls the SpreadsheetView so that the given {@link SpreadsheetColumn} is visible.
     * @param column 
     */
    public void scrollToColumn(SpreadsheetColumn column){
        cellsView.scrollToColumn(column.column);
    }
    
    /**
     *
     * Scrolls the SpreadsheetView so that the given column index is visible.
     *
     * @param modelColumn
     *
     */
    public void scrollToColumnIndex(int modelColumn) {
        cellsView.scrollToColumnIndex(modelColumn);
    }

    /**
     * Return the editor associated with the CellType. (defined in
     * {@link SpreadsheetCellType#createEditor(SpreadsheetView)}. FIXME Maybe
     * keep the editor references inside the SpreadsheetCellType
     * 
     * @param cellType
     * @return the editor associated with the CellType.
     */
    public final Optional<SpreadsheetCellEditor> getEditor(SpreadsheetCellType<?> cellType) {
        if(cellType == null){
            return Optional.empty();
        }
        SpreadsheetCellEditor cellEditor = editors.get(cellType);
        if (cellEditor == null) {
            cellEditor = cellType.createEditor(this);
            if(cellEditor == null){
                return Optional.empty();
            }
            editors.put(cellType, cellEditor);
        }
        return Optional.of(cellEditor);
    }

    /**
     * Sets the value of the property editable.
     * 
     * @param b
     */
    public final void setEditable(final boolean b) {
        cellsView.setEditable(b);
    }

    /**
     * Gets the value of the property editable.
     * 
     * @return a boolean telling if the SpreadsheetView is editable.
     */
    public final boolean isEditable() {
        return cellsView.isEditable();
    }

    /**
     * Specifies whether this SpreadsheetView is editable - only if the
     * SpreadsheetView, and the {@link SpreadsheetCell} within it are both
     * editable will a {@link SpreadsheetCell} be able to go into its editing
     * state.
     * 
     * @return the BooleanProperty associated with the editableProperty.
     */
    public final BooleanProperty editableProperty() {
        return cellsView.editableProperty();
    }

    /**
     * This Node is shown to the user when the SpreadsheetView has no content to show.
     */
    public final ObjectProperty<Node> placeholderProperty() {
        return cellsView.placeholderProperty();
    }

    /**
     * Sets the value of the placeholder property
     *
     * @param placeholder the node to show when the SpreadsheetView has no content to show.
     */
    public final void setPlaceholder(final Node placeholder) {
        cellsView.setPlaceholder(placeholder);
    }

    /**
     * Gets the value of the placeholder property.
     *
     * @return the Node used as a placeholder that is shown when the SpreadsheetView has no content to show.
     */
    public final Node getPlaceholder() {
        return cellsView.getPlaceholder();
    }

    
    /***************************************************************************
     * COPY / PASTE METHODS
     **************************************************************************/
    
    /**
     * Put the current selection into the ClipBoard. This can be overridden by
     * developers for custom behavior.
     */
    public void copyClipboard() {
        checkFormat();

        final ArrayList<ClipboardCell> list = new ArrayList<>();
        final ObservableList<TablePosition> posList = getSelectionModel().getSelectedCells();

        Set<SpreadsheetCell> treatedCells = new HashSet<>();
        for (final TablePosition<?, ?> p : posList) {
            SpreadsheetCell cell = getGrid().getRows().get(getModelRow(p.getRow())).get(getModelColumn(p.getColumn()));
            if (!treatedCells.contains(cell)) {
                treatedCells.add(cell);

                /**
                 * We need to add every cell contained in a span otherwise the
                 * rectangles computed when pasting will be wrong.
                 */
                for (int row = 0; row < getRowSpan(cell, p.getRow()); ++row) {
                    for (int col = 0; col < getColumnSpan(cell); ++col) {
                        list.add(new ClipboardCell(p.getRow() + row, p.getColumn() + col, cell));
                    }
                }
            }
        }
        final ClipboardContent content = new ClipboardContent();
        content.put(fmt, list);
        Clipboard.getSystemClipboard().setContent(content);
    }

    /**
     * Paste one value from the clipboard over the whole selection.
     * @param change 
     */
    private void pasteOneValue(ClipboardCell change) {
        for (TablePosition position : getSelectionModel().getSelectedCells()) {
            tryPasteCell(getModelRow(position.getRow()), getModelColumn(position.getColumn()), change);
        }
    }

    /**
     * Try to paste the given value into the given position.
     * @param row
     * @param column
     * @param value 
     */
    private void tryPasteCell(int row, int column, ClipboardCell change) {
        final SpanType type = getSpanType(row, column);
        if (type == SpanType.NORMAL_CELL || type == SpanType.ROW_VISIBLE) {
            SpreadsheetCell cell = getGrid().getRows().get(row).get(column);
            //Retrieve html in value if exists
            //Value contains the HTML version or the nomrla value.
            Object value = change.getHtmlVersion() == null ? change.getValue() : change.getHtmlVersion();
            value = cell.isCellGraphic() ? value : change.getValue();
            boolean succeed = cell.getCellType().match(value, cell.getOptionsForEditor());
            if (succeed) {
                getGrid().setCellValue(cell.getRow(), cell.getColumn(),
                        cell.getCellType().convertValue(value));
            }
        }
    }

    /**
     * Try to paste the values given into the selection. If both selection are
     * rectangles and the number of rows of the source is equal of the numbers
     * of rows of the target AND number of columns of the target is a multiple
     * of the number of columns of the source, then we can paste.
     *
     * Same goes if we invert the rows and columns.
     * @param list
     */
    private void pasteMixedValues(ArrayList<ClipboardCell> list) {
        SelectionRange sourceSelectionRange = new SelectionRange();
        sourceSelectionRange.fillClipboardRange(list);

        //It means we have a rectangle.
        if (sourceSelectionRange.getRange() != null) {
            SelectionRange targetSelectionRange = new SelectionRange();
            targetSelectionRange.fill(cellsView.getSelectionModel().getSelectedCells());
            if (targetSelectionRange.getRange() != null) {
                //If both selection are rectangle
                GridRange sourceRange = sourceSelectionRange.getRange();
                GridRange targetRange = targetSelectionRange.getRange();
                int sourceRowGap = sourceRange.getBottom() - sourceRange.getTop() + 1;
                int targetRowGap = targetRange.getBottom() - targetRange.getTop() + 1;

                int sourceColumnGap = sourceRange.getRight() - sourceRange.getLeft() + 1;
                int targetColumnGap = targetRange.getRight() - targetRange.getLeft() + 1;

                final int offsetRow = targetRange.getTop() - sourceRange.getTop();
                final int offsetCol = targetRange.getLeft() - sourceRange.getLeft();

                //If the numbers of rows are the same and the targetColumnGap is a multiple of sourceColumnGap
                if ((sourceRowGap == targetRowGap || targetRowGap == 1) && (targetColumnGap % sourceColumnGap) == 0) {
                    for (final ClipboardCell change : list) {
                        int row = getModelRow(change.getRow() + offsetRow);
                        int column = change.getColumn() + offsetCol;
                        do {
                            int modelColumn = getModelColumn(column);
                            if (row < getGrid().getRowCount() && modelColumn < getGrid().getColumnCount()
                                    && row >= 0 && column >= 0) {
                                tryPasteCell(row, modelColumn, change);
                            }
                        } while ((column = column + sourceColumnGap) <= targetRange.getRight());
                    }
                    //If the numbers of columns are the same and the targetRowGap is a multiple of sourceRowGap
                } else if ((sourceColumnGap == targetColumnGap || targetColumnGap == 1) && (targetRowGap % sourceRowGap) == 0) {
                    for (final ClipboardCell change : list) {

                        int row = change.getRow() + offsetRow;
                        int column = getModelColumn(change.getColumn() + offsetCol);
                        do {
                            int modelRow = getModelRow(row);
                            if (modelRow < getGrid().getRowCount() && column < getGrid().getColumnCount()
                                    && row >= 0 && column >= 0) {
                                tryPasteCell(modelRow, column, change);
                            }
                        } while ((row = row + sourceRowGap) <= targetRange.getBottom());
                    }
                }
            }
        }
    }

    /**
     * If we have several source values to paste into one cell, we do it.
     *
     * @param list
     */
    private void pasteSeveralValues(ArrayList<ClipboardCell> list) {
        // TODO algorithm very bad
        int minRow = getGrid().getRowCount();
        int minCol = getGrid().getColumnCount();
        int maxRow = 0;
        int maxCol = 0;
        for (final ClipboardCell p : list) {
            final int tempcol = p.getColumn();
            final int temprow = p.getRow();
            if (tempcol < minCol) {
                minCol = tempcol;
            }
            if (tempcol > maxCol) {
                maxCol = tempcol;
            }
            if (temprow < minRow) {
                minRow = temprow;
            }
            if (temprow > maxRow) {
                maxRow = temprow;
            }
        }

        final TablePosition<?, ?> p = cellsView.getFocusModel().getFocusedCell();

        final int offsetRow = p.getRow() - minRow;
        final int offsetCol = p.getColumn() - minCol;
        final int rowCount = getGrid().getRowCount();
        final int columnCount = getGrid().getColumnCount();
        int row;
        int column;

        for (final ClipboardCell change : list) {
            row = getModelRow(change.getRow() + offsetRow);
            column = getModelColumn(change.getColumn() + offsetCol);
            if (row < rowCount && column < columnCount
                    && row >= 0 && column >= 0) {
                tryPasteCell(row, column, change);
            }
        }
    }
    
    /**
     * Try to paste the clipBoard to the specified position. Try to paste the
     * current selection into the Grid. If the two contents are not matchable,
     * then it's not pasted. This can be overridden by developers for custom
     * behavior.
     */
    public void pasteClipboard() {
        // FIXME Maybe move editableProperty to the model..
        List<TablePosition> selectedCells = cellsView.getSelectionModel().getSelectedCells();
        if (!isEditable() || selectedCells.isEmpty()) {
            return;
        }

        checkFormat();
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.getContent(fmt) != null) {

            @SuppressWarnings("unchecked")
            final ArrayList<ClipboardCell> list = (ArrayList<ClipboardCell>) clipboard.getContent(fmt);
            if (list.size() == 1) {
                pasteOneValue(list.get(0));
            } else if (selectedCells.size() > 1) {
                pasteMixedValues(list);
            } else {
                pasteSeveralValues(list);
            }
            // To be improved
        } else if (clipboard.hasString()) {
            // final TablePosition<?,?> p =
            // cellsView.getFocusModel().getFocusedCell();
            //
            // SpreadsheetCell stringCell =
            // SpreadsheetCellType.STRING.createCell(0, 0, 1, 1,
            // clipboard.getString());
            // getGrid().getRows().get(p.getRow()).get(p.getColumn()).match(stringCell);

        }
    }

    /**
     * Create a menu on rightClick with two options: Copy/Paste This can be
     * overridden by developers for custom behavior.
     * 
     * @return the ContextMenu to use.
     */
    public ContextMenu getSpreadsheetViewContextMenu() {
        final ContextMenu contextMenu = new ContextMenu();

        final MenuItem copyItem = new MenuItem(localize(asKey("spreadsheet.view.menu.copy"))); //$NON-NLS-1$
        copyItem.setGraphic(new ImageView(new Image(SpreadsheetView.class
                .getResourceAsStream("copySpreadsheetView.png")))); //$NON-NLS-1$
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        copyItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                copyClipboard();
            }
        });

        final MenuItem pasteItem = new MenuItem(localize(asKey("spreadsheet.view.menu.paste"))); //$NON-NLS-1$
        pasteItem.setGraphic(new ImageView(new Image(SpreadsheetView.class
                .getResourceAsStream("pasteSpreadsheetView.png")))); //$NON-NLS-1$
        pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN));
        pasteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                pasteClipboard();
            }
        });
        
        final Menu cornerMenu = new Menu(localize(asKey("spreadsheet.view.menu.comment"))); //$NON-NLS-1$
        cornerMenu.setGraphic(new ImageView(new Image(SpreadsheetView.class
                .getResourceAsStream("comment.png")))); //$NON-NLS-1$

        final MenuItem topLeftItem = new MenuItem(localize(asKey("spreadsheet.view.menu.comment.top-left"))); //$NON-NLS-1$
        topLeftItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                TablePosition<ObservableList<SpreadsheetCell>, ?> pos = cellsView.getFocusModel().getFocusedCell();
                SpreadsheetCell cell = getGrid().getRows().get(getModelRow(pos.getRow())).get(getModelColumn(pos.getColumn()));
                cell.activateCorner(SpreadsheetCell.CornerPosition.TOP_LEFT);
                }
        });
        final MenuItem topRightItem = new MenuItem(localize(asKey("spreadsheet.view.menu.comment.top-right"))); //$NON-NLS-1$
        topRightItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                TablePosition<ObservableList<SpreadsheetCell>, ?> pos = cellsView.getFocusModel().getFocusedCell();
                SpreadsheetCell cell = getGrid().getRows().get(getModelRow(pos.getRow())).get(getModelColumn(pos.getColumn()));
                cell.activateCorner(SpreadsheetCell.CornerPosition.TOP_RIGHT);
            }
        });
        final MenuItem bottomRightItem = new MenuItem(localize(asKey("spreadsheet.view.menu.comment.bottom-right"))); //$NON-NLS-1$
        bottomRightItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                TablePosition<ObservableList<SpreadsheetCell>, ?> pos = cellsView.getFocusModel().getFocusedCell();
                SpreadsheetCell cell = getGrid().getRows().get(getModelRow(pos.getRow())).get(getModelColumn(pos.getColumn()));
                cell.activateCorner(SpreadsheetCell.CornerPosition.BOTTOM_RIGHT);
            }
        });
        final MenuItem bottomLeftItem = new MenuItem(localize(asKey("spreadsheet.view.menu.comment.bottom-left"))); //$NON-NLS-1$
        bottomLeftItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                TablePosition<ObservableList<SpreadsheetCell>, ?> pos = cellsView.getFocusModel().getFocusedCell();
                SpreadsheetCell cell = getGrid().getRows().get(getModelRow(pos.getRow())).get(getModelColumn(pos.getColumn()));
                cell.activateCorner(SpreadsheetCell.CornerPosition.BOTTOM_LEFT);
            }
        });

        cornerMenu.getItems().addAll(topLeftItem, topRightItem, bottomRightItem, bottomLeftItem);
        
        contextMenu.getItems().addAll(copyItem, pasteItem, cornerMenu);
        return contextMenu;
    }

    /**
     * This method is called when pressing the "delete" key on the
     * SpreadsheetView. This will erase the values of selected cells. This can
     * be overridden by developers for custom behavior.
     */
    public void deleteSelectedCells() {
        for (TablePosition<ObservableList<SpreadsheetCell>, ?> position : getSelectionModel().getSelectedCells()) {
            getGrid().setCellValue(getModelRow(position.getRow()), getModelColumn(position.getColumn()), null);
        }
    }
    
    /**
     * Return the {@link SpanType} of a cell. This is used internally by the
     * SpreadsheetView but some users may find it useful.
     *
     * @param rowIndex
     * @param modelColumn
     * @return The {@link SpanType} of a cell
     */
    public SpanType getSpanType(final int rowIndex, final int modelColumn) {
        if (getGrid() == null) {
            return SpanType.NORMAL_CELL;
        }

        if (rowIndex < 0 || modelColumn < 0 || rowIndex >= getItems().size() || modelColumn >= getGrid().getColumnCount()) {
            return SpanType.NORMAL_CELL;
        }

        final SpreadsheetCell cell = getCellsView().getItems().get(rowIndex).get(modelColumn);

        final int cellColumn = getHiddenColumns().nextClearBit(cell.getColumn());
//        final int cellRow = spv.getViewRow(cell.getRow());
        int cellRowSpan = getRowSpanFilter(cell);//cell.getRowSpan();

        if (cellColumn == modelColumn /*&& cellRow == rowIndex*/ && cellRowSpan == 1) {
            return SpanType.NORMAL_CELL;
        }
//        cellRowSpan = spv.getRowSpanFilter(cell);
        final int cellColumnSpan = getColumnSpan(cell);
        /**
         * This is a consuming operation so we place it after the normal_cell
         * case since this is the most typical case.
         */
        final GridViewSkin skin = getCellsViewSkin();
        final boolean containsRowMinusOne = skin == null ? true : skin.containsRow(rowIndex - 1);
        //If the cell above is the same.
        final boolean containsSameCellMinusOne = rowIndex > 0
                ? getCellsView().getItems().get(rowIndex - 1).get(modelColumn) == cell
                : false;
        if (containsRowMinusOne && cellColumnSpan > 1 && cellColumn != modelColumn && cellRowSpan > 1
                && containsSameCellMinusOne) {
            return SpanType.BOTH_INVISIBLE;
        } else if (cellRowSpan > 1 && cellColumn == modelColumn) {
            if ((!containsSameCellMinusOne || !containsRowMinusOne)) {
                return SpanType.ROW_VISIBLE;
            } else {
                return SpanType.ROW_SPAN_INVISIBLE;
            }
        } else if (cellColumnSpan > 1 && (!containsSameCellMinusOne || !containsRowMinusOne)) {
            /**
             * If the next visible column from the starting column is my
             * viewColumn, it means all columns before me are hidden and I must
             * show myself.
             */
//            int columnVisible = spv.getHiddenColumns().nextClearBit(cell.getColumn());
            if (cellColumn == modelColumn) {
                return SpanType.NORMAL_CELL;
            } else {
                return SpanType.COLUMN_SPAN_INVISIBLE;
            }
        } else {
            return SpanType.NORMAL_CELL;
        }
    }

    /***************************************************************************
     * * Private/Protected Implementation * *
     **************************************************************************/

    /**
     * This is called when setting a Grid. The main idea is to re-use
     * TableColumn if possible. Because we can have a great amount of time spent
     * in com.sun.javafx.css.StyleManager.forget when removing lots of columns
     * and adding new ones. So if we already have some, we can just re-use them
     * so we avoid doign all the fuss with the TableColumns.
     *
     * @param grid
     * @param columnIndex
     * @return
     */
    private TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> getTableColumn(Grid grid, int columnIndex) {

        TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> column;

        String columnHeader = grid.getColumnHeaders().size() > columnIndex ? grid
                .getColumnHeaders().get(columnIndex) : Utils.getExcelLetterFromNumber(columnIndex);

        if (columnIndex < cellsView.getColumns().size()) {
            column = (TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell>) cellsView.getColumns().get(columnIndex);
            column.setText(columnHeader);
        } else {
            column = new TableColumn<>(columnHeader);

            column.setEditable(true);
            // We don't want to sort the column
            column.setSortable(false);

            column.setReorderable(false);

            // We assign a DataCell for each Cell needed (MODEL).
            column.setCellValueFactory((TableColumn.CellDataFeatures<ObservableList<SpreadsheetCell>, SpreadsheetCell> p) -> {
                if (columnIndex >= p.getValue().size()) {
                    return null;
                }
                return new ReadOnlyObjectWrapper<>(p.getValue().get(columnIndex));
            });
            // We create a SpreadsheetCell for each DataCell in order to
            // specify how to represent the DataCell(VIEW)
            column.setCellFactory((TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> p) -> new CellView(handle));
        }
        return column;
    }
    
    /**
     * This static method creates a sample Grid with 100 rows and 15 columns.
     * All cells are typed as String.
     *
     * @return the sample Grid
     * @see SpreadsheetCellType#STRING
     */
    private static Grid getSampleGrid() {
        GridBase gridBase = new GridBase(100, 15);
        ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();

        for (int row = 0; row < gridBase.getRowCount(); ++row) {
            ObservableList<SpreadsheetCell> currentRow = FXCollections.observableArrayList();
            for (int column = 0; column < gridBase.getColumnCount(); ++column) {
                currentRow.add(SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "toto"));
            }
            rows.add(currentRow);
        }
        gridBase.setRows(rows);
        return gridBase;
    }
    
    private void initRowFix(Grid grid) {
        ObservableList<ObservableList<SpreadsheetCell>> rows = grid.getRows();
        final int rowSize = rows.size();
        rowFix = new BitSet(rowSize);
        identityMap = new IdentityHashMap<>(rowSize);
        rows:
        for (int r = 0; r < rowSize; ++r) {
            ObservableList<SpreadsheetCell> row = rows.get(r);
            identityMap.put(row, r);
            for (SpreadsheetCell cell : row) {
                if (getRowSpanFilter(cell) > 1) {
                    continue rows;
                }
            }
            rowFix.set(r);
        }
    }
    
    /**
     * Verify that the grid is well-formed. Can be quite time-consuming I guess
     * so I would like it not to be compulsory..
     * 
     * @param grid
     */
    private void verifyGrid(Grid grid) {
        verifyColumnSpan(grid);
    }

    private void verifyColumnSpan(Grid grid) {
        for (int i = 0; i < grid.getRows().size(); ++i) {
            ObservableList<SpreadsheetCell> row = grid.getRows().get(i);
            int count = 0;
            for (int j = 0; j < row.size(); ++j) {
                if (row.get(j).getColumnSpan() == 1) {
                    ++count;
                } else if (row.get(j).getColumnSpan() > 1) {
                    ++count;
                    SpreadsheetCell currentCell = row.get(j);
                    for (int k = j + 1; k < currentCell.getColumn() + currentCell.getColumnSpan(); ++k) {
                        if (!row.get(k).equals(currentCell)) {
                            throw new IllegalStateException("\n At row " + i + " and column " + j //$NON-NLS-1$ //$NON-NLS-2$
                                    + ": this cell is in the range of a columnSpan but is different. \n" //$NON-NLS-1$
                                    + "Every cell in a range of a ColumnSpan must be of the same instance."); //$NON-NLS-1$
                        }
                        ++count;
                        ++j;
                    }
                } else {
                    throw new IllegalStateException("\n At row " + i + " and column " + j //$NON-NLS-1$ //$NON-NLS-2$
                            + ": this cell has a negative columnSpan"); //$NON-NLS-1$
                }
            }
            if (count != grid.getColumnCount()) {
                throw new IllegalStateException("The row" + i //$NON-NLS-1$
                        + " has a number of cells different of the columnCount declared in the grid."); //$NON-NLS-1$
            }
        }
    }

    private void checkFormat() {
        if ((fmt = DataFormat.lookupMimeType("SpreadsheetView")) == null) { //$NON-NLS-1$
            fmt = new DataFormat("SpreadsheetView"); //$NON-NLS-1$
        }
    }

    /**
     * ********************************************************************* *
     * private listeners
     * ********************************************************************
     */

    private final ListChangeListener<Integer> fixedRowsListener = new ListChangeListener<Integer>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends Integer> c) {
            while (c.next()) {
                if (c.wasAdded()) {
                    List<? extends Integer> newRows = c.getAddedSubList();
                    if(!areRowsFixable(newRows)){
                        throw new IllegalArgumentException(computeReason(newRows));
                    }
                    FXCollections.sort(fixedRows);
                }
                
                if(c.wasRemoved()){
                    //Handle this case.
                }
            }
        }
    };

        private String computeReason(List<? extends Integer> list) {
        String reason = "\n A row cannot be frozen. \n"; //$NON-NLS-1$

        for (Integer row : list) {
            //If this row is not fixable, we need to identify the maximum span
            if (!isRowFixable(row)) {

                int maxSpan = 1;
                List<SpreadsheetCell> gridRow = getGrid().getRows().get(row);
                for (SpreadsheetCell cell : gridRow) {
                    if(!list.contains(cell.getRow())){
                        reason += "The row " + row + " is inside a row span and the starting row " + cell.getRow() + " is not frozen.\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                    //We only want to consider the original cell.
                    if (cell.getRowSpan() > maxSpan && cell.getRow() == row) {
                        maxSpan = cell.getRowSpan();
                    }
                }
                //Then we need to verify that all rows within that span are fixed.
                int count = row + maxSpan - 1;
                for (int index = row + 1; index < count; ++index) {
                    if (!list.contains(index)) {
                        reason += "One cell on the row " + row + " has a row span of " + maxSpan + ". " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                + "But the row " + index + " contained within that span is not frozen.\n"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
        }
        return reason;
    }

    private final ListChangeListener<SpreadsheetColumn> fixedColumnsListener = new ListChangeListener<SpreadsheetColumn>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends SpreadsheetColumn> c) {
            while (c.next()) {
                if (c.wasAdded()) {
                    List<? extends SpreadsheetColumn> newColumns = c.getAddedSubList();
                    if (!areSpreadsheetColumnsFixable(newColumns)) {
                        List<Integer> newList = new ArrayList<>();
                        for (SpreadsheetColumn column : newColumns) {
                            if (column != null) {
                                newList.add(columns.indexOf(column));
                            }
                        }
                        throw new IllegalArgumentException(computeReason(newList));
                    }
                }
            }
        }

        private String computeReason(List<Integer> list) {

            String reason = "\n This column cannot be frozen."; //$NON-NLS-1$
            final ObservableList<ObservableList<SpreadsheetCell>> rows = getGrid().getRows();
            for (Integer columnIndex : list) {
                //If this row is not fixable, we need to identify the maximum span
                if (!isColumnFixable(columnIndex)) {
                    int maxSpan = 1;
                    SpreadsheetCell cell;
                    for (List<SpreadsheetCell> row : rows) {
                        cell = row.get(columnIndex);
                        //If the original column is not within this range, there is not need to look deeper.
                        if (!list.contains(cell.getColumn())) {
                            reason += "The column " + columnIndex + " is inside a column span and the starting column " + cell.getColumn() + " is not frozen.\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        }
                        //We only want to consider the original cell.
                        if (cell.getColumnSpan() > maxSpan && cell.getColumn() == columnIndex) {
                            maxSpan = cell.getColumnSpan();
                        }
                    }
                    //Then we need to verify that all columns within that span are fixed.
                    int count = columnIndex + maxSpan - 1;
                    for (int index = columnIndex + 1; index < count; ++index) {
                        if (!list.contains(index)) {
                            reason += "One cell on the column " + columnIndex + " has a column span of " + maxSpan + ". " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    + "But the column " + index + " contained within that span is not frozen.\n"; //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                }
            }
            return reason;
        }
    };

    private final ChangeListener<ContextMenu> contextMenuChangeListener = new ChangeListener<ContextMenu>() {
        
        @Override
        public void changed(ObservableValue<? extends ContextMenu> arg0, ContextMenu oldContextMenu, final ContextMenu newContextMenu) {
            if(oldContextMenu !=null){
                oldContextMenu.setOnShowing(null);
            }
            if(newContextMenu != null){
                newContextMenu.setOnShowing(new WeakEventHandler<>(hideContextMenuEventHandler));
            }
        }
    };
    
    private final EventHandler<WindowEvent> hideContextMenuEventHandler = new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent arg0) {
            // We don't want to open a contextMenu when editing
            // because editors
            // have their own contextMenu
            if (getEditingCell() != null) {
                // We're being reactive but we want to be pro-active
                // so we may need a work-around.
                Platform.runLater(()->{
                    getContextMenu().hide();
                });
            }
        }
    };
    
    private final EventHandler<KeyEvent> keyPressedHandler = (KeyEvent keyEvent) -> {
        TablePosition<ObservableList<SpreadsheetCell>, ?> position = getSelectionModel().getFocusedCell();
        // Go to the next row only if we're not editing
        if (getEditingCell() == null && KeyCode.ENTER.equals(keyEvent.getCode())) {
            if (position != null) {
                if(keyEvent.isShiftDown()){
                     ((GridViewBehavior)getCellsViewSkin().getBehavior()).selectCell(-1, 0);
                }else{
                     ((GridViewBehavior)getCellsViewSkin().getBehavior()).selectCell(1, 0);
                }
                //We consume the event because we don't want to go in edition
                keyEvent.consume();
            }
            getCellsViewSkin().scrollHorizontally();
            // Go to next cell
        } else if (getEditingCell() == null 
                && KeyCode.TAB.equals(keyEvent.getCode()) 
                && !keyEvent.isShortcutDown()) {
            if (position != null) {
                if (keyEvent.isShiftDown()) {
                    getSelectionModel().clearAndSelectLeftCell();
                } else {
                    getSelectionModel().clearAndSelectRightCell();
                }
            }
            //We consume the event because we don't want to loose focus
            keyEvent.consume();
            getCellsViewSkin().scrollHorizontally();
            // We want to erase values when delete key is pressed.
        } else if (KeyCode.DELETE.equals(keyEvent.getCode())) {
            deleteSelectedCells();

        } else if (isEditionKey(keyEvent)) {
            getCellsView().edit(position.getRow(), position.getTableColumn());
        } else if (zoomNormalKeypad.match(keyEvent) || zoomNormal.match(keyEvent) || zoomNormalShift.match(keyEvent)) {
            //Reset zoom to zero.
            setZoomFactor(1.0);
        } else if (zoomInChar.match(keyEvent) || zoomInKeypadAdd.match(keyEvent) || zoomInCharShift.match(keyEvent)) {
            incrementZoom();
        } else if (zoomOutChar.match(keyEvent) || zoomOutKeypad.match(keyEvent)) {
            decrementZoom();
        }
    };
    
    /**
     * We want NOT to go in edition if we're pressing SHIFT and if we're using
     * the navigation keys. But we still want the user to go in edition with
     * SHIFT and some letters for example if he wants a capital letter. FIXME
     * Add a test to prevent the Shift fail case.
     *
     * We go in edition if we're typing a letter or a digit simply. Also add the
     * sign because we can directly modify the number by typing "+1" in a cell.
     *
     * @param keyEvent
     * @return
     */
    private boolean isEditionKey(KeyEvent keyEvent) {
        return !keyEvent.isShortcutDown()
                && !keyEvent.getCode().isNavigationKey()
                && !keyEvent.getCode().isFunctionKey()
                && !keyEvent.getCode().isModifierKey()
                && !keyEvent.getCode().isMediaKey()
                && keyEvent.getCode() != KeyCode.ESCAPE;
    }
    
    /**
     * This event is thrown on the SpreadsheetView when the user resize a row
     * with its mouse.
     */
    public static class RowHeightEvent extends Event {

        /**
         * This is the event used by {@link RowHeightEvent}.
         */
        public static final EventType<RowHeightEvent> ROW_HEIGHT_CHANGE 
                = new EventType<>(Event.ANY, "RowHeightChange" + UUID.randomUUID().toString()); //$NON-NLS-1$

        private final int modelRow;
        private final double height;

        public RowHeightEvent(int row, double height) {
            super(ROW_HEIGHT_CHANGE);
            this.modelRow = row;
            this.height = height;
        }

        /**
         * Return the row index that has been resized.
         * @return the row index that has been resized.
         */
        public int getRow() {
            return modelRow;
        }

        /**
         * Return the new height for this row.
         * @return the new height for this row.
         */
        public double getHeight() {
            return height;
        }
    }
    
    /**
     * This event is thrown on the SpreadsheetView when the user resize a column
     * with its mouse.
     */
    public static class ColumnWidthEvent extends Event {

        /**
         * This is the event used by {@link ColumnWidthEvent}.
         */
        public static final EventType<ColumnWidthEvent> COLUMN_WIDTH_CHANGE 
                = new EventType<>(Event.ANY, "ColumnWidthChange" + UUID.randomUUID().toString()); //$NON-NLS-1$

        private final int column;
        private final double width;

        public ColumnWidthEvent(int column, double width) {
            super(COLUMN_WIDTH_CHANGE);
            this.column = column;
            this.width = width;
        }

        /**
         * Return the column index that has been resized.
         * @return the column index that has been resized.
         */
        public int getColumn() {
            return column;
        }

        /**
         * Return the new width for this column.
         * @return the new width for this column.
         */
        public double getWidth() {
            return width;
        }
    }
}
