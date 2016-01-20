package be.makercafe.apps.makerbench.editors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleSpansBuilder;
import org.reactfx.Change;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;

import be.makercafe.apps.makerbench.millcrum.Millcrum;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;

public class JFXMillEditor extends Editor {

	private static final String[] KEYWORDS = new String[] { "def", "in", "as", "abstract", "assert", "boolean", "break",
			"byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum",
			"extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
			"interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short",
			"static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try",
			"void", "volatile", "while" };

	private static final Pattern KEYWORD_PATTERN = Pattern.compile("\\b(" + String.join("|", KEYWORDS) + ")\\b");

	private final Group viewGroup;

	private CodeArea caCodeArea;

	private boolean autoCompile = false;

	private Millcrum millObject;

//	private BorderPane editorContainer;

	private Pane viewContainer;

//	private SubScene subScene;

	private ToolBar toolBar = null;

	private ComboBox cbxSourceExamples = null;

	public JFXMillEditor(String tabText, Path path) {
		super(tabText);

		this.viewGroup = new Group();
//		this.editorContainer = new BorderPane();
		this.viewContainer = new Pane();

		this.caCodeArea = new CodeArea("");
		this.caCodeArea.setEditable(true);
		this.caCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(caCodeArea));
		this.caCodeArea.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.caCodeArea.textProperty().addListener((ov, oldText, newText) -> {
			Matcher matcher = KEYWORD_PATTERN.matcher(newText);
			int lastKwEnd = 0;
			StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
			while (matcher.find()) {
				spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
				spansBuilder.add(Collections.singleton("keyword"), matcher.end() - matcher.start());
				lastKwEnd = matcher.end();
			}
			spansBuilder.add(Collections.emptyList(), newText.length() - lastKwEnd);
			caCodeArea.setStyleSpans(0, spansBuilder.create());
		});

		EventStream<Change<String>> textEvents = EventStreams.changesOf(caCodeArea.textProperty());

		textEvents.reduceSuccessions((a, b) -> b, Duration.ofMillis(3000)).subscribe(code -> {
			if (autoCompile) {
				compile(code.getNewValue());
			}
		});

		try {
			this.caCodeArea.replaceText(FileUtils.readFileToString(path.toFile()));
		} catch (IOException ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error reading file.", ex);
		}

		SplitPane editorPane = new SplitPane(caCodeArea, viewContainer);
		editorPane.setOrientation(Orientation.HORIZONTAL);
		BorderPane rootPane = new BorderPane();

		toolBar = createToolBar();
		rootPane.setTop(toolBar);
		rootPane.setCenter(editorPane);
		this.getTab().setContent(rootPane);

	}

	private void setCode(String code) {
		this.caCodeArea.replaceText(code);

	}

	private String getCode() {
		return this.caCodeArea.getText();
	}

	private void compile(String code) {

		millObject = null;

		viewGroup.getChildren().clear();

		try {

			CompilerConfiguration cc = new CompilerConfiguration();

			cc.addCompilationCustomizers(
					new ImportCustomizer().addStarImports("be.makercafe.apps.makerbench.millcrum","javafx"));

			GroovyShell shell = new GroovyShell(getClass().getClassLoader(), new Binding(), cc);

			Script script = shell.parse(code);

			Object obj = script.run();

			if (obj instanceof Millcrum) {

				millObject = (Millcrum) obj;

				viewContainer.getChildren().clear();
				viewContainer.getChildren().add(millObject.getMillCanvas().getCanv());

			} else {
				Logger.getLogger(this.getClass().getName()).log(Level.INFO, "No Millcrum object returned");
			}

		} catch (Throwable ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Unsuspected exception", ex);
		}
	}

//	private void setMeshScale(MeshContainer meshContainer, Bounds t1, final MeshView meshView) {
//		if (meshContainer != null) {
//			double maxDim = Math.max(meshContainer.getWidth(),
//					Math.max(meshContainer.getHeight(), meshContainer.getDepth()));
//
//			double minContDim = Math.min(t1.getWidth(), t1.getHeight());
//
//			double scale = minContDim / (maxDim * 2);
//
//			meshView.setScaleX(scale);
//			meshView.setScaleY(scale);
//			meshView.setScaleZ(scale);
//		}
//	}

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

		Button btnExportSTL = GlyphsDude.createIconButton(MaterialDesignIcon.EXPORT, "Export GCODE");

		btnExportSTL.setOnAction(this::handleExportAsGCodeFile);

		Button btnExportPNG = GlyphsDude.createIconButton(MaterialDesignIcon.CAMERA, "Export PNG");
		btnExportPNG.setOnAction(this::handleExportAsPngFile);

		Button btnRun = GlyphsDude.createIconButton(MaterialDesignIcon.RUN, "Run");
		btnRun.setOnAction(this::handleCompileAndRun);

		ToggleButton btnAutoCompile = GlyphsDude.createIconToggleButton(MaterialDesignIcon.AUTO_FIX, "Automatic run",
				null, ContentDisplay.LEFT);
		btnAutoCompile.setOnAction(this::handleAutoCompile);
		btnAutoCompile.setSelected(false);

		ToggleButton btn3DNav = GlyphsDude.createIconToggleButton(MaterialDesignIcon.ROTATE_3D, "3D Navigation ", null,
				ContentDisplay.LEFT);
		btn3DNav.setSelected(false);

		ComboBox<String> cbxSourceExamples = new ComboBox<String>();
		cbxSourceExamples.getItems().addAll("TestCut");
		this.cbxSourceExamples = cbxSourceExamples; // TODO: maybe cleaner way
													// to do this ?

		Button btnPasteSource = GlyphsDude.createIconButton(MaterialDesignIcon.CONTENT_PASTE, "Paste source");
		btnPasteSource.setOnAction(this::handlePasteSource);

		toolBar.getItems().addAll(btnSave, btnExportSTL, btnExportPNG, new Separator(), btnRun, new Separator(),
				btnAutoCompile, new Separator(), cbxSourceExamples,btnPasteSource );

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

	private void handleExportAsGCodeFile(ActionEvent e) {

		if (millObject == null) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Oeps an error occured");
			alert.setHeaderText("Cannot export gcode. There is no geometry !");
			alert.setContentText("Please verify that your code generates a valid millcrum object.");
			alert.showAndWait();
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export GCODE File");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GCODE files (*.gcode)", "*.tap"));

		File f = fileChooser.showSaveDialog(null);

		if (f == null) {
			return;
		}

		String fName = f.getAbsolutePath();

		if (!fName.toLowerCase().endsWith(".gcode")) {
			fName += ".gcode";
		}

		try {
			millObject.get();
			FileUtils.write(new File(fName), millObject.getToSaveGcode());
		} catch (IOException ex) {

			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Oeps an error occured");
			alert.setHeaderText("Cannot export gcode. There went something wrong writing the file.");
			alert.setContentText(
					"Please verify that your file is not read only, is not locked by other user or program, you have enough diskspace.");
			alert.showAndWait();
		}
	}

	private void handleExportAsPngFile(ActionEvent e) {

		if (millObject == null) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Oeps an error occured");
			alert.setHeaderText("Cannot export PNG. There is no geometry !");
			alert.setContentText("Please verify that your code generates a valid CSG object.");
			alert.showAndWait();
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export PNG File");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files (*.png)", "*.png"));

		File f = fileChooser.showSaveDialog(null);

		if (f == null) {
			return;
		}

		String fName = f.getAbsolutePath();

		if (!fName.toLowerCase().endsWith(".png")) {
			fName += ".png";
		}

		int snWidth = 1024;
		int snHeight = 1024;

		double realWidth = viewGroup.getBoundsInLocal().getWidth();
		double realHeight = viewGroup.getBoundsInLocal().getHeight();

		double scaleX = snWidth / realWidth;
		double scaleY = snHeight / realHeight;

		double scale = Math.min(scaleX, scaleY);

		PerspectiveCamera snCam = new PerspectiveCamera(false);
		snCam.setTranslateZ(-200);

		SnapshotParameters snapshotParameters = new SnapshotParameters();
		snapshotParameters.setTransform(new Scale(scale, scale));
		snapshotParameters.setCamera(snCam);
		snapshotParameters.setDepthBuffer(true);
		snapshotParameters.setFill(Color.TRANSPARENT);

		WritableImage snapshot = new WritableImage(snWidth, (int) (realHeight * scale));

		viewGroup.snapshot(snapshotParameters, snapshot);

		try {
			ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", new File(fName));
		} catch (IOException ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Oeps an error occured");
			alert.setHeaderText("Cannot export PNG. There went something wrong writing the file.");
			alert.setContentText(
					"Please verify that your file is not read only, is not locked by other user or program, you have enough diskspace.");
			alert.showAndWait();
		}
	}

	private void handleCompileAndRun(ActionEvent e) {
		compile(getCode());
	}

	private void handleAutoCompile(ActionEvent e) {
		this.autoCompile = !this.autoCompile;
	}

	private void handlePasteSource(ActionEvent e) {
		String exampleSourceCode = null;
		try {
			exampleSourceCode = (String) this.cbxSourceExamples.getSelectionModel().getSelectedItem();
			String code = IOUtils.toString(this.getClass().getResourceAsStream(exampleSourceCode + ".jfxmill"),
					"UTF-8");
			this.caCodeArea.replaceText(code);
		} catch (IOException ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
					"Unable to load example source code: " + exampleSourceCode, ex);
		}
	}

	public ToolBar getToolBar() {
		return toolBar;
	}

	public void setToolbar(ToolBar toolBar) {
		this.toolBar = toolBar;
	}

}
