package be.makercafe.apps.makerbench.editors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;

import org.apache.commons.io.FileUtils;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;

public class TextEditor extends Editor {

	private CodeArea caCodeArea;

	private ToolBar toolBar = null;

	public TextEditor(String tabText, Path path) {
		super(tabText);

		this.caCodeArea = new CodeArea("");
		this.caCodeArea.setEditable(true);
		this.caCodeArea.setParagraphGraphicFactory(LineNumberFactory
				.get(caCodeArea));
		this.caCodeArea.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		addContextMenu(this.caCodeArea);

		try {
			this.caCodeArea.replaceText(FileUtils.readFileToString(path
					.toFile()));
		} catch (IOException ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
					"Error reading file.", ex);
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

		Button btnSave = GlyphsDude.createIconButton(MaterialDesignIcon.FLOPPY,
				"Save");
		btnSave.setOnAction(this::handleSaveButton);

		toolBar.getItems().add(btnSave);

		return toolBar;

	}

	private void handleSaveButton(ActionEvent event) {
		System.out.println("Event: " + event.getSource());
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) this.getTab()
				.getUserData();
		String path = map.get("path");
		try {
			FileUtils.writeStringToFile(new File(path), caCodeArea.getText());
		} catch (IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
					"Unable to save file.");
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Oeps an error occured");
			alert.setHeaderText("Cannot save file. There went something wrong writing the file.");
			alert.setContentText("Please verify that your file is not read only, is not locked by other user or program, you have enough diskspace.");
			alert.showAndWait();
		}
	}



	public ToolBar getToolBar() {
		return toolBar;
	}

	public void setToolbar(ToolBar toolBar) {
		this.toolBar = toolBar;
	}

}
