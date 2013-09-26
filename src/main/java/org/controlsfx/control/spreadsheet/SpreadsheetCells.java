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
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.controlsfx.control.spreadsheet.SpreadsheetCell.CellType;

/**
 * You can generate some {@link SpreadsheetCell} used by the {@link Grid}
 * with the static method provided. 
 * 
 * Currently you can only create a textCell, listCell or a DateCell.
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
        return new SpreadsheetCell<String>(row, column, rowSpan, columnSpan, CellType.STRING) {

            /*******************************************************************
             * * Static Fields * *
             ******************************************************************/
            private static final long serialVersionUID = -1711498694430990374L;

            /*******************************************************************
             * * Constructor * *
             ******************************************************************/
            {
                this.setCellValue(value);
            }

            /*******************************************************************
             * * Public Methods * *
             ******************************************************************/
            @Override
            public void setCellValue(String value) {
                this.str = value;
            }

            @Override
            public String getCellValue() {
                return str;
            }

            @Override
            public void match(SpreadsheetCell<?> cell) {
                setStr(cell.getStr());
            }

        };
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
        return new SpreadsheetCell<Double>(row, column, rowSpan, columnSpan, CellType.DOUBLE) {

            /*******************************************************************
             * * Static Fields * *
             ******************************************************************/
            private static final long serialVersionUID = -1711498694430990374L;

            /***************************************************************************
             * * Private Fields * *
             **************************************************************************/
            private Double doubleValue;
            
            /*******************************************************************
             * * Constructor * *
             ******************************************************************/
            {
                this.setCellValue(value);
            }

            /*******************************************************************
             * * Public Methods * *
             ******************************************************************/
            @Override
            public void setCellValue(Double value) {
                if(value != null && !value.isNaN()){
                    this.doubleValue = value;
                    this.str = value.toString();
                }else{
                    this.str="";
                }
            }

            @Override
            public Double getCellValue() {
                return doubleValue;
            }

            @Override
            public void match(SpreadsheetCell<?> cell) {
               try{
            	   Double temp = Double.parseDouble(cell.getStr());
            	   this.setCellValue(temp);
               }catch(Exception e){
            	   
               }
            }

        };
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
    public static SpreadsheetCell<List<String>> createListCell(final int row, final int column,
            final int rowSpan, final int columnSpan, final List<String> _value) {
        return new SpreadsheetCell<List<String>>(row, column, rowSpan, columnSpan, CellType.ENUM) {
            /***************************************************************************
             * * Static Fields * *
             **************************************************************************/
            private static final long serialVersionUID = -1003136076165430609L;

            /***************************************************************************
             * * Private Fields * *
             **************************************************************************/
            private List<String> value;

            /***************************************************************************
             * * Constructor * *
             **************************************************************************/
            {
                this.value = _value;

                str = value.size() > 0 ? this.value
                        .get((int) (Math.random() * value.size())) : "";
            }

            /***************************************************************************
             * * Public Methods * *
             **************************************************************************/
            @Override
            public void setCellValue(List<String> value) {
                this.value = value;
                if (value.size() > 0) {
                    str = value.get(0);
                }
            }

            @Override
            public List<String> getCellValue() {
                return value;
            }

            @Override
            public void match(SpreadsheetCell<?> cell) {
                if (value.contains(cell.getStr())) {
                    setStr(cell.getStr());
                }
            }
        };
    }

    /**
     * Creates a cell that hold a Date at the specified position, with 
     * the specified row/column span.
     * @param r row number
     * @param c column number
     * @param rs rowSpan (1 is normal)
     * @param cs ColumnSpan (1 is normal)
     * @param _value A {@link LocalDate}
     * @return
     */
    public static SpreadsheetCell<LocalDate> createDateCell(final int row, final int column,
            final int rowSpan, final int columnSpan, final LocalDate _value) {
        return new SpreadsheetCell<LocalDate>(row, column, rowSpan, columnSpan, CellType.DATE) {

            /***************************************************************************
             * * Static Fields * *
             **************************************************************************/
            private static final long serialVersionUID = -1711498694430990374L;

            /***************************************************************************
             * * Private Fields * *
             **************************************************************************/
            private LocalDate value;

            /***************************************************************************
             * * Constructor * *
             **************************************************************************/
            {
                this.setCellValue(_value);
            }

            /***************************************************************************
             * * Public Methods * *
             **************************************************************************/

            @Override
            public void setCellValue(LocalDate _value) {
                this.value = _value;
                this.str = value.format(DateTimeFormatter
                        .ofPattern("dd/MM/yyyy"));
            }

            @Override
            public LocalDate getCellValue() {
                return value;
            }

            @Override
            public void match(SpreadsheetCell<?> cell) {
                try {
                    LocalDate temp = LocalDate.parse(
                            cell.getStr()
                                    .subSequence(0, cell.getStr().length()),
                            DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    setCellValue(temp);
                } catch (Exception e) {
                }
            }
        };
    }
}
