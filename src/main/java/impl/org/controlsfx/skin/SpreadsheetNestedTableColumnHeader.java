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
package impl.org.controlsfx.skin;

import org.controlsfx.control.SpreadsheetView;

import javafx.scene.control.TableColumnBase;

import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableViewSkinBase;

public class SpreadsheetNestedTableColumnHeader extends NestedTableColumnHeader {

    public SpreadsheetNestedTableColumnHeader(
            TableViewSkinBase<?, ?, ?, ?, ?, ?> skin, TableColumnBase<?, ?> tc) {
        super(skin, tc);
    }

    @Override
    protected TableColumnHeader createTableColumnHeader(TableColumnBase col) {
        return col.getColumns().isEmpty()
                ? new TableColumnHeader(getTableViewSkin(), col)
                : new SpreadsheetNestedTableColumnHeader(getTableViewSkin(),
                        col);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        /*****************************************************************
         * MODIFIED BY NELLARMONIA
         *****************************************************************/
        layoutFixedColumns();
        /*****************************************************************
         * END OF MODIFIED BY NELLARMONIA
         *****************************************************************/
    }

    /**
     * We want ColumnHeader to be fixed when we freeze some columns
     * 
     * @param scrollX
     */
    public void layoutFixedColumns() {
    	final SpreadsheetView spreadsheetView = ((SpreadsheetViewSkin) getTableViewSkin()).spreadsheetView;
        double hbarValue = spreadsheetView.getHbar().getValue();
        
        final int labelHeight = (int) getChildren().get(0).prefHeight(-1);
        double fixedColumnWidth = 0;
        double x = snappedLeftInset();
        
        for (int j = 0, max = getColumnHeaders().size(); j < max; j++) {
        	final TableColumnHeader n = getColumnHeaders().get(j);
        	final double prefWidth = snapSize(n.prefWidth(-1));
        	 
        	//If the column is fixed
        	if(spreadsheetView.getFixedColumns().indexOf(Integer.valueOf(j)) != -1){
                 double tableCellX = 0;
                 //If the column is hidden we have to translate it
                 if(hbarValue + fixedColumnWidth > x){

                 	tableCellX = Math.abs(hbarValue - x + fixedColumnWidth);

                 	n.toFront();
                 	fixedColumnWidth += prefWidth;
                 }
                 n.relocate(x+tableCellX , labelHeight + snappedTopInset());
        	}
        	
           x+= prefWidth;
        }

    }

    public void updateHeader() {
        setHeadersNeedUpdate();
    }

}
