/**
 * Copyright (c) 2013, ControlsFX
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

import java.time.LocalDate;


/**
 * You can generate some {@link SpreadsheetCell} used by the {@link Grid}
 * with the static method provided. 
 * 
 * Currently you can only create a textCell, listCell, doubleCell or a DateCell.
 * 
 * <h3> Code samples </h3>
 * Here is a method that uses all the static method. 
 * The generation is random here  and you'll probably use a switch instruction in your case.
 * <br/><br/>
 * <pre>
 * private SpreadsheetCell&lt;?&gt; generateCell(int row, int column, int rowSpan, int colSpan) {
 *		SpreadsheetCell&lt;?&gt; cell;
 *		List&lt;String&gt; stringListTextCell = Arrays.asList("Shanghai","Paris","New York City","Bangkok","Singapore","Johannesburg","Berlin","Wellington","London","Montreal");
 *		final double random = Math.random();
 *		if (random &lt; 0.10) {
 *			List&lt;String&gt; stringList = Arrays.asList("China","France","New Zealand","United States","Germany","Canada");
 *			cell = SpreadsheetCells.createListCell(row, column, rowSpan, colSpan, stringList);
 *		} else if (random &gt;= 0.10 && random &lt; 0.25) {
 *			cell = SpreadsheetCells.createTextCell(row, column, rowSpan, colSpan,stringListTextCell.get((int)(Math.random()*10)));
 *		}else if (random &gt;= 0.25 && random &lt; 0.75) {
 *			cell = SpreadsheetCells.createDoubleCell(row, column, rowSpan, colSpan,(double)Math.round((Math.random()*100)*100)/100);
 *		}else{
 *			cell = SpreadsheetCells.createDateCell(row, column, rowSpan, colSpan, LocalDate.now().plusDays((int)(Math.random()*10)));
 *		}
 * 		return cell;
 * }
 * </pre>
 * 
 * @see SpreadsheetCell
 */
public class SpreadsheetCells {

    private SpreadsheetCells() {
        // no-op
    }

    /**
     * Creates a cell that hold a String at the specified position, with 
     * the specified row/column span.
     * @param row row number
     * @param column column number
     * @param rs rowSpan (1 is normal)
     * @param cs ColumnSpan (1 is normal)
     * @param value the String to display
     * @return
     */
    public static SpreadsheetCell<String> createTextCell(final int row, final int column,
            final int rowSpan, final int columnSpan, final String value) {
        return SpreadsheetCellType.STRING.createCell(row, column, rowSpan, columnSpan, value);
    }

    /**
     * Creates a cell that hold a Double at the specified position, with 
     * the specified row/column span.
     * @param r row number
     * @param c column number
     * @param rs rowSpan (1 is normal)
     * @param cs ColumnSpan (1 is normal)
     * @param value the Double to display
     * @return
     */
    public static SpreadsheetCell<Double> createDoubleCell(final int row, final int column,
            final int rowSpan, final int columnSpan, final Double value) {
        return SpreadsheetCellType.DOUBLE.createCell(row, column, rowSpan, columnSpan, value);
    }
	/**
	 * Creates a cell that hold a list of String at the specified position, with 
     * the specified row/column span.
	 * @param r row number
     * @param c column number
     * @param rs rowSpan (1 is normal)
     * @param cs ColumnSpan (1 is normal)
	 * @param _value A list of String to display
	 * @return
	 */
    /*public static SpreadsheetCell<String> createListCell(final int row, final int column,
            final int rowSpan, final int columnSpan, final List<String> items) {
        return new SpreadsheetCell<String>(row, column, rowSpan, columnSpan, CellType.LIST) {

            private static final long serialVersionUID = -1003136076165430609L;

            {
                this.setConverter(new DefaultStringConverter());
                this.getProperties().put("items", items);
                if (items != null && items.size() > 0) {
                    setItem(items.get(0));
                }
            }

            @Override public void match(SpreadsheetCell<?> cell) {
                if (getItem().contains(cell.getText())) {
                    setItem((String)cell.getText());
                }
            }
        };
    }*/

    /**
     * Creates a cell that hold a Date at the specified position, with 
     * the specified row/column span.
     * @param r row number
     * @param c column number
     * @param rs rowSpan (1 is normal)
     * @param cs ColumnSpan (1 is normal)
     * @param value A {@link LocalDate}
     * @return
     */
    public static SpreadsheetCell<LocalDate> createDateCell(final int row, final int column,
            final int rowSpan, final int columnSpan, final LocalDate value) {
        return SpreadsheetCellType.DATE.createCell(row, column, rowSpan, columnSpan, value);
    }
}
