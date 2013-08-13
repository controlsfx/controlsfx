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
package org.controlsfx.control.spreadsheet.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SpreadsheetCells {

    private SpreadsheetCells() {
        // no-op
    }

    public static DataCell<String> createTextCell(final int r, final int c,
            final int rs, final int cs, final String value) {
        return new DataCell<String>(r, c, rs, cs) {

            /*******************************************************************
             * * Static Fields * *
             ******************************************************************/
            private static final long serialVersionUID = -1711498694430990374L;

            /*******************************************************************
             * * Constructor * *
             ******************************************************************/
            {
                this.type = CellType.STRING;
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
            public void match(DataCell<?> cell) {
                setStr(cell.getStr());
            }

        };
    }

    public static DataCell<List<String>> createListCell(final int r,
            final int c, final int rs, final int cs, final List<String> _value) {
        return new DataCell<List<String>>(r, c, rs, cs) {
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
                this.type = CellType.ENUM;
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
            public void match(DataCell<?> cell) {
                if (value.contains(cell.getStr())) {
                    setStr(cell.getStr());
                }
            }
        };
    }

    public static DataCell<LocalDate> createDateCell(final int r, final int c,
            final int rs, final int cs, final LocalDate _value) {
        return new DataCell<LocalDate>(r, c, rs, cs) {

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
                this.type = CellType.DATE;
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
            public void match(DataCell<?> cell) {
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
