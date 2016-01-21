package be.makercafe.apps.makerbench.editors;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.fxmisc.richtext.CodeArea;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

/**
 * Abstract class which forms the base for all editors in makerbench.
 *
 * @author luc.de.pauw@makercafe.be
 *
 */

public abstract class Editor {
	private Tab tab = new Tab();

	protected String searchText;

	protected String replaceText;

	/**
	 * Construct a new Editor;
	 *
	 * @param tabText
	 */
	public Editor(String tabText) {

		tab = new Tab();
		tab.setText(tabText);

	}

	public Tab getTab() {
		return this.tab;
	}

	/**
	 * Decorate codeArea contextMenu with basic copy/paste/... actions
	 * 
	 * @param codeArea
	 */
	protected void addContextMenu(CodeArea codeArea) {

		ContextMenu rootContextMenu;
		if (codeArea.getContextMenu() == null) {
			rootContextMenu = new ContextMenu();
		} else {
			rootContextMenu = codeArea.getContextMenu();
		}
		// Copy..
		MenuItem copyItem = new MenuItem("Copy");
		copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C,
				KeyCombination.CONTROL_DOWN));
		copyItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				codeArea.copy();
			}
		});
		// Paste..
		MenuItem pasteItem = new MenuItem("Paste");
		pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V,
				KeyCombination.CONTROL_DOWN));
		pasteItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				codeArea.paste();
			}
		});
		// Cut..
		MenuItem cutItem = new MenuItem("Cut");
		cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X,
				KeyCombination.CONTROL_DOWN));
		cutItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				codeArea.cut();
			}
		});
		// Select All..
		MenuItem selectAllItem = new MenuItem("Select all");
		selectAllItem.setAccelerator(new KeyCodeCombination(KeyCode.A,
				KeyCombination.CONTROL_DOWN));
		selectAllItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				codeArea.selectAll();
			}
		});
		// Find..
		MenuItem findItem = new MenuItem("Find..");
		findItem.setAccelerator(new KeyCodeCombination(KeyCode.F,
				KeyCombination.CONTROL_DOWN));
		findItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				TextInputDialog dialog = new TextInputDialog();
				dialog.setTitle("Text Input Dialog");
				dialog.setHeaderText("Search");
				dialog.setContentText("Find:");

				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					searchText = result.get();
					int caretPos = codeArea.getCaretPosition();
					if (caretPos < codeArea.getText().length() - 1) {
						String text = codeArea.getText(caretPos, codeArea
								.getText().length() - 1);
						int index = text.indexOf(searchText);
						if (index >= 0) {
							codeArea.selectRange(caretPos + index, caretPos
									+ index + searchText.length());
							codeArea.copy();
						}
					}
				}

			}
		});

		// Find next..
		MenuItem findNextItem = new MenuItem("Find next");
		findNextItem.setAccelerator(new KeyCodeCombination(KeyCode.N,
				KeyCombination.CONTROL_DOWN));
		findNextItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				int caretPos = codeArea.getCaretPosition();
				if (caretPos < codeArea.getText().length() - 1) {
					String text = codeArea.getText(caretPos, codeArea.getText()
							.length() - 1);
					int index = text.indexOf(searchText);
					if (index >= 0) {
						codeArea.selectRange(caretPos + index, caretPos + index
								+ searchText.length());
						codeArea.copy();
					}
				}
			}
		});
		// Replace..
		MenuItem replaceItem = new MenuItem("Replace..");
		replaceItem.setAccelerator(new KeyCodeCombination(KeyCode.R,
				KeyCombination.CONTROL_DOWN));
		replaceItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				Dialog<Map<String, String>> dialog = createFindAndReplaceDialog();
				Optional<Map<String, String>> result = dialog.showAndWait();

				result.ifPresent(data -> {
					if (data.get("button").equalsIgnoreCase("replaceBtn")) {
						searchText = data.get("find");
						replaceText = data.get("replace");
						int caretPos = codeArea.getCaretPosition();
						if (caretPos < codeArea.getText().length() - 1) {
							String text = codeArea.getText(caretPos, codeArea
									.getText().length() - 1);
							int index = text.indexOf(searchText);
							if (index >= 0) {
								codeArea.selectRange(caretPos + index, caretPos
										+ index + searchText.length());
								codeArea.cut();
								codeArea.insertText(
										codeArea.getCaretPosition(),
										replaceText);
							}
						}
					} else if (data.get("button").equalsIgnoreCase(
							"replaceAllBtn")) {
						// TODO: Quick and dirty replace all
						searchText = data.get("find");
						replaceText = data.get("replace");
						int caretPos = 0;
						String text = codeArea.getText(caretPos, codeArea
								.getText().length() - 1);
						int index = text.indexOf(searchText);
						while (index >= 0) {
							codeArea.selectRange(caretPos + index, caretPos
									+ index + searchText.length());
							codeArea.cut();
							codeArea.insertText(codeArea.getCaretPosition(),
									replaceText);
							caretPos = codeArea.getCaretPosition();
							text = codeArea.getText(caretPos, codeArea
									.getText().length() - 1);
							index = text.indexOf(searchText);
						}
					}
				});

			}
		});

		// Replace next..
		MenuItem replaceNextItem = new MenuItem("Replace next");
		replaceNextItem.setAccelerator(new KeyCodeCombination(KeyCode.W,
				KeyCombination.CONTROL_DOWN));
		replaceNextItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				int caretPos = codeArea.getCaretPosition();
				if (caretPos < codeArea.getText().length() - 1) {
					String text = codeArea.getText(caretPos, codeArea.getText()
							.length() - 1);
					int index = text.indexOf(searchText);
					if (index >= 0) {
						codeArea.selectRange(caretPos + index, caretPos + index
								+ 6);
						codeArea.cut();
						codeArea.insertText(codeArea.getCaretPosition(),
								replaceText);
					}
				}
			}
		});
		rootContextMenu.getItems().addAll(copyItem, pasteItem, cutItem,
				selectAllItem, findItem, findNextItem, replaceItem,
				replaceNextItem);
		codeArea.setContextMenu(rootContextMenu);

	}

	private Dialog<Map<String, String>> createFindAndReplaceDialog() {
		// Create the custom dialog.
		Dialog<Map<String, String>> dialog = new Dialog<>();
		dialog.setTitle("Find and replace dialog");
		dialog.setHeaderText("Enter search and replace creteria");

		// Set the icon (must be included in the project).
		// dialog.setGraphic(new ImageView(this.getClass()
		// .getResource("login.png").toString()));

		// Set the button types.
		ButtonType replaceButtonType = new ButtonType("Replace",
				ButtonData.OK_DONE);
		ButtonType replaceAllButtonType = new ButtonType("Replace All",
				ButtonData.OTHER);
		dialog.getDialogPane()
				.getButtonTypes()
				.addAll(replaceButtonType, replaceAllButtonType,
						ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField findText = new TextField();
		findText.setPromptText("Find");
		TextField replaceText = new TextField();
		replaceText.setPromptText("Replace");

		grid.add(new Label("Find:"), 0, 0);
		grid.add(findText, 1, 0);
		grid.add(new Label("Replace by:"), 0, 1);
		grid.add(replaceText, 1, 1);

		// Enable/Disable login button depending on whether a username was
		// entered.
		Node replaceButton = dialog.getDialogPane().lookupButton(
				replaceButtonType);
		Node replaceAllButton = dialog.getDialogPane().lookupButton(
				replaceAllButtonType);
		replaceButton.setDisable(true);
		replaceAllButton.setDisable(true);

		// Enable / disable buttons when text input is entered.
		findText.textProperty().addListener(
				(observable, oldValue, newValue) -> {
					replaceButton.setDisable(newValue.trim().isEmpty());
					replaceAllButton.setDisable(newValue.trim().isEmpty());
				});

		dialog.getDialogPane().setContent(grid);

		// Request focus on the findText field by default.
		Platform.runLater(() -> findText.requestFocus());

		dialog.setResultConverter(dialogButton -> {
			Map<String, String> map = null;
			if (dialogButton == replaceButtonType) {
				map = new HashMap<String, String>();
				map.put("find", findText.getText());
				map.put("replace", replaceText.getText());
				map.put("button", "replaceBtn");
				return map;
			} else if (dialogButton == replaceAllButtonType) {
				map = new HashMap<String, String>();
				map.put("find", findText.getText());
				map.put("replace", replaceText.getText());
				map.put("button", "replaceAllBtn");
				return map;
			}
			return map;
		});

		return dialog;
	}

}
