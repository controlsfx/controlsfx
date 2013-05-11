/**
 * A package containing a powerful (yet easy to use) dialogs API for showing
 * modal dialogs in JavaFX-based applications.<br/>
 * <br/>
 * There are two types of API defined in this package: 
 *   <ul>
 *     <li>High level API for common dialogs creation</li>
 *     <li>Low level API for creation of highly customized dialogs</li>
 *   </ul>
 * 
 * High level "fluent" API allows for quick creation of common dialogs such as information, warning, error etc. 
 * It is concentrated in the  {@link Dialogs} class. For example:<br/>
 * 
 * <pre>
 * {@code
 *  Action response = Dialogs.create()
 *      .owner(cbSetOwner.isSelected() ? stage : null)
 *      .title("You do want dialogs right?")
 *      .masthead(isMastheadVisible() ? "Just Checkin'" : null)
 *      .message( "I was a bit worried that you might not want them, so I wanted to double check.")
 *      .showConfirm();
 * }
 * </pre>
 * 
 * The code above will setup and show a confirmation dialog. High-level API is built upon low-level API.<br/><br/> 
 *  
 * Low level API allows for developing highly customized dialogs and concentrated in the {@link Dialog} class.
 * Here is the example of building custom dialog:
 * 
 * <pre>
 * {@code
 *    Dialog dlg = new Dialog( owner, "Dialog Title");
 *    dlg.setResizable(false);
 *    dlg.setGraphic(new ImageView(getImage()));
 *    dlg.setMasthead("Dialog Masthead");
 *    dlg.getActions().addAll(Dialog.Actions.OK, Dialog.Actions.CANCEL);
 *    dlg.setContent("Dialog message");
 *    dlg.setExpandableContent( new Label("Expandable content"));
 *    return dlg.show();
 * }
 * </pre>
 * 
 * The code above will setup and present the non resizable dialog with masthead, message and "OK" and "Cancel" buttons. 
 * Also it will have expandable area, visibility of which is triggered by automatically presented "Details" button 
 * 
 */
package org.controlsfx.dialogs;