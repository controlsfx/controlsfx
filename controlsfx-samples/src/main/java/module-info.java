/**
 * Copyright (c) 2018, 2020 ControlsFX
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
module org.controlsfx.samples {

    requires java.desktop;
    requires org.controlsfx.controls;
    requires org.controlsfx.fxsampler;

    exports org.controlsfx.samples to javafx.graphics;
    exports org.controlsfx.samples.actions to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.button to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.checked to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.dialogs to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.propertysheet to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.tablefilter to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.tableview to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.tableview2 to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.textfields to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.spreadsheet to org.controlsfx.fxsampler;
    
    opens org.controlsfx.samples;
    opens org.controlsfx.samples.dialogs;
    opens org.controlsfx.samples.actions to org.controlsfx.controls;
    opens org.controlsfx.samples.tableview to javafx.base;
    opens org.controlsfx.samples.spreadsheet to javafx.graphics;
    
    provides fxsampler.FXSamplerProject with org.controlsfx.ControlsFXSampler;
}