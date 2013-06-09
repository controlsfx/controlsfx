package org.controlsfx.dialog;

import org.controlsfx.dialog.Dialog;

public class DialogsAccessor {

    public static void setWindows(boolean b) {
        Dialog.setWindows(b);
    }
    
    public static void setMacOS(boolean b) {
        Dialog.setMacOS(b);
    }
    
    public static void setLinux(boolean b) {
        Dialog.setLinux(b);
    }
    
//    public static void setUseLightweightDialogs(boolean b) {
//        Dialog.setUseLightweightDialogs(b);
//    }
}
