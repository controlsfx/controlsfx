package org.controlsfx.samples;

import javafx.application.Application;
import javafx.stage.Stage;

import org.controlsfx.dialogs.DialogTemplate2;
import org.controlsfx.dialogs.DialogTemplate2.DialogAction;

public class HelloDialogTemplate extends Application {

    @Override public void start(final Stage stage) {
        DialogTemplate2 dlg = new DialogTemplate2(stage, "Hello Dialog Template");
        dlg.setMasthead( "MASTEHAD" );
        dlg.setContent("Message Message Message");
        dlg.setResizable(true);
        dlg.getActions().setAll(DialogAction.OK, DialogAction.CANCEL);
        dlg.show();
    }

}
