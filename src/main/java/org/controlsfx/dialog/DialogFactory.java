package org.controlsfx.dialog;

import javafx.stage.Window;

class DialogFactory {
    
    static FXDialog createDialog(String title) {
        return createDialog(false, title);
    }
    
    static FXDialog createDialog(boolean useLightweight, String title) {
        return createDialog(false, title, null, false);
    }

    static FXDialog createDialog(boolean useLightweight, String title, Object owner, boolean modal) {
        if (useLightweight) {
            return new LightweightDialog(title, owner);
        } else {
            return new HeavyweightDialog(title, (Window) owner, modal);
        }
    }
}
