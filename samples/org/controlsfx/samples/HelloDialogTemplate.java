package org.controlsfx.samples;

import javafx.application.Application;
import javafx.stage.Stage;

import org.controlsfx.dialogs.DialogTemplate2;
import org.controlsfx.dialogs.DialogTemplate2.DialogAction;

public class HelloDialogTemplate extends Application {

    @Override public void start(final Stage stage) {
        new DialogTemplate2(stage, "Hello Dialog Template")
        .resizable(false)
        .masthead( "MASTEHAD" )
        .content("Message Message Message")
        .commands(DialogAction.OK, DialogAction.CANCEL)
        .show();
    }

}
