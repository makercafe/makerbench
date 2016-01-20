package be.makercafe.apps.makerbench.editors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import be.makercafe.apps.makerbench.resourceview.ResourceTreeItem;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class TextEditor extends Editor {

	private CodeArea caCodeArea;

	private ToolBar toolBar = null;



	public TextEditor(String tabText, Path path) {
		super(tabText);

		this.caCodeArea = new CodeArea("");
		this.caCodeArea.setEditable(true);
		this.caCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(caCodeArea));
		this.caCodeArea.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		addContextMenu(this.caCodeArea);

		try {
			this.caCodeArea.replaceText(FileUtils.readFileToString(path.toFile()));
		} catch (IOException ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error reading file.", ex);
		}

		BorderPane rootPane = new BorderPane();

		toolBar = createToolBar();
		rootPane.setTop(toolBar);
		rootPane.setCenter(caCodeArea);
		this.getTab().setContent(rootPane);

	}

	private void setCode(String code) {
		this.caCodeArea.replaceText(code);

	}

	private String getCode() {
		return this.caCodeArea.getText();
	}


	/**
	 * Creates the toolBar for the editor.
	 *
	 * @return
	 */

	private ToolBar createToolBar() {

		ToolBar toolBar = new ToolBar();
		toolBar.setOrientation(Orientation.HORIZONTAL);

		Button btnSave = GlyphsDude.createIconButton(MaterialDesignIcon.FLOPPY, "Save");
		btnSave.setOnAction(this::handleSaveButton);



		toolBar.getItems().add(btnSave);

		return toolBar;

	}

	private void handleSaveButton(ActionEvent event) {
		System.out.println("Event: " + event.getSource());
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) this.getTab().getUserData();
		String path = map.get("path");
		try {
			FileUtils.writeStringToFile(new File(path), caCodeArea.getText());
		} catch (IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Unable to save file.");
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Oeps an error occured");
			alert.setHeaderText("Cannot save file. There went something wrong writing the file.");
			alert.setContentText(
					"Please verify that your file is not read only, is not locked by other user or program, you have enough diskspace.");
			alert.showAndWait();
		}
	}

	/**
	 * Decorate codeArea contextMenu with basic copy/paste/... actions
	 * @param codeArea
	 */
	private void addContextMenu(CodeArea codeArea) {

		ContextMenu rootContextMenu;
		if(codeArea.getContextMenu() == null){
			rootContextMenu = new ContextMenu();
		}else{
			rootContextMenu = codeArea.getContextMenu();
		}
		// Copy..
		MenuItem copyItem = new MenuItem("Copy");
		copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
		copyItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				codeArea.copy();
			}
		});
		// Paste..
		MenuItem pasteItem = new MenuItem("Paste");
		pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
		pasteItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				codeArea.paste();
			}
		});
		// Cut..
		MenuItem cutItem = new MenuItem("Cut");
		cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
		cutItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				codeArea.cut();
			}
		});
		// Select All..
		MenuItem selectAllItem = new MenuItem("Select all");
		selectAllItem.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
		selectAllItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				codeArea.selectAll();
			}
		});
		// Find..
		MenuItem findItem = new MenuItem("Find..");
		findItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
		findItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				int caretPos = codeArea.getCaretPosition();
				String text = codeArea.getText(caretPos, codeArea.getText().length()-1);
				int index = text.indexOf("testje");
				if(index > 0){
					codeArea.selectRange(caretPos + index, caretPos + index + 6 );
					codeArea.copy();
				}
			}
		});

		// Find next..
		MenuItem findNextItem = new MenuItem("Find next");
		findNextItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
		findNextItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				int caretPos = codeArea.getCaretPosition();
				String text = codeArea.getText(caretPos, codeArea.getText().length()-1);
				int index = text.indexOf("testje");
				if(index > 0){
					codeArea.selectRange(caretPos + index, caretPos + index + 6 );
					codeArea.copy();
				}
			}
		});
		// Replace..
		MenuItem replaceItem = new MenuItem("Find..");
		replaceItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
		replaceItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				int caretPos = codeArea.getCaretPosition();
				String text = codeArea.getText(caretPos, codeArea.getText().length()-1);
				int index = text.indexOf("testje");
				if(index > 0){
					codeArea.selectRange(caretPos + index, caretPos + index + 6 );
					codeArea.cut();
					codeArea.insertText(codeArea.getCaretPosition(), "replace");
				}
			}
		});

		// Replace next..
		MenuItem replaceNextItem = new MenuItem("Find next");
		replaceNextItem.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
		replaceNextItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				int caretPos = codeArea.getCaretPosition();
				String text = codeArea.getText(caretPos, codeArea.getText().length()-1);
				int index = text.indexOf("testje");
				if(index > 0){
					codeArea.selectRange(caretPos + index, caretPos + index + 6 );
					codeArea.copy();
				}
			}
		});
		rootContextMenu.getItems().addAll(copyItem, pasteItem, cutItem, selectAllItem, findItem, findNextItem, replaceItem, replaceNextItem);
		codeArea.setContextMenu(rootContextMenu);

	}

	public ToolBar getToolBar() {
		return toolBar;
	}

	public void setToolbar(ToolBar toolBar) {
		this.toolBar = toolBar;
	}

}
