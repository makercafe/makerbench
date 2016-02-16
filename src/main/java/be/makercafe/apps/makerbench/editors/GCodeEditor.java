/*
    Copyright 2015 - 2016, Luc De pauw - Makercafe.be
    This file is part of Makerbench.

    Makerbench is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Makerbench is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Makerbench.  If not, see <http://www.gnu.org/licenses/>.
*/
package be.makercafe.apps.makerbench.editors;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;
import org.reactfx.Change;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;

import be.makercafe.apps.makerbench.editors.utils.VFX3DUtil;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.MeshContainer;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

public class GCodeEditor extends Editor {

	// private static final String[] KEYWORDS = new String[] { "def", "in",
	// "as",
	// "abstract", "assert", "boolean", "break", "byte", "case", "catch",
	// "char", "class", "const", "continue", "default", "do", "double",
	// "else", "enum", "extends", "final", "finally", "float", "for",
	// "goto", "if", "implements", "import", "instanceof", "int",
	// "interface", "long", "native", "new", "package", "private",
	// "protected", "public", "return", "short", "static", "strictfp",
	// "super", "switch", "synchronized", "this", "throw", "throws",
	// "transient", "try", "void", "volatile", "while" };
	//
	// private static final String KEYWORD_PATTERN = "\\b(" + String.join("|",
	// KEYWORDS) + ")\\b";
	// private static final String PAREN_PATTERN = "\\(|\\)";
	// private static final String BRACE_PATTERN = "\\{|\\}";
	// private static final String BRACKET_PATTERN = "\\[|\\]";
	// private static final String SEMICOLON_PATTERN = "\\;";
	// private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
	// private static final String COMMENT_PATTERN = "//[^\n]*" + "|" +
	// "/\\*(.|\\R)*?\\*/";
	//
	// private static final Pattern PATTERN = Pattern.compile(
	// "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
	// + "|(?<PAREN>" + PAREN_PATTERN + ")"
	// + "|(?<BRACE>" + BRACE_PATTERN + ")"
	// + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
	// + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
	// + "|(?<STRING>" + STRING_PATTERN + ")"
	// + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
	// );

	private final Group viewGroup;

	private CodeArea caCodeArea;

	private boolean autoCompile = false;

	private CSG csgObject;

	private BorderPane editorContainer;

	private Pane viewContainer;

	private SubScene subScene;

	private ToolBar toolBar = null;

	private ComboBox cbxSourceExamples = null;

	private static PhongMaterial MATERIAL_GREEN = new PhongMaterial(Color.GREEN);
	private static PhongMaterial MATERIAL_RED = new PhongMaterial(Color.RED);
	private static PhongMaterial MATERIAL_BLUE = new PhongMaterial(Color.BLUE);
	private static PhongMaterial MATERIAL_YELLOW = new PhongMaterial(
			Color.YELLOW);
	private static PhongMaterial MATERIAL_ORANGE = new PhongMaterial(
			Color.ORANGE);
	private static PhongMaterial MATERIAL_WHITE = new PhongMaterial(Color.WHITE);
	private static PhongMaterial MATERIAL_BLACK = new PhongMaterial(Color.BLACK);

	private Map<String, Double> lastLine = new HashMap<>();

	private boolean isRelative = false;

	public GCodeEditor(String tabText, Path path) {
		super(tabText);

		this.viewGroup = new Group();
		this.editorContainer = new BorderPane();
		this.viewContainer = new Pane();

		this.caCodeArea = new CodeArea("");
		this.caCodeArea.setEditable(true);
		this.caCodeArea.setParagraphGraphicFactory(LineNumberFactory
				.get(caCodeArea));
		this.caCodeArea.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

		// this.caCodeArea.getStylesheets().add(this.getClass().getResource("java-keywords.css").toExternalForm());
		// this.caCodeArea.richChanges().subscribe(change -> {
		// caCodeArea.setStyleSpans(0,
		// computeHighlighting(caCodeArea.getText()));
		// });

		addContextMenu(this.caCodeArea);
		EventStream<Change<String>> textEvents = EventStreams
				.changesOf(caCodeArea.textProperty());

		textEvents.reduceSuccessions((a, b) -> b, Duration.ofMillis(3000))
				.subscribe(code -> {
					if (autoCompile) {
						compile(code.getNewValue());
					}
				});

		if (path == null) {
			this.caCodeArea.replaceText("#empty");
		} else {
			try {
				this.caCodeArea.replaceText(FileUtils.readFileToString(path
						.toFile()));
			} catch (IOException ex) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
						"Error reading file.", ex);
			}

		}

		// editorContainer.setCenter(this.codeArea);

		subScene = new SubScene(viewGroup, 100, 100, true,
				SceneAntialiasing.BALANCED);

		subScene.widthProperty().bind(viewContainer.widthProperty());
		subScene.heightProperty().bind(viewContainer.heightProperty());

		PerspectiveCamera subSceneCamera = new PerspectiveCamera(false);
		subScene.setCamera(subSceneCamera);

		viewContainer.getChildren().add(subScene);

		SplitPane editorPane = new SplitPane(caCodeArea, viewContainer);
		editorPane.setOrientation(Orientation.HORIZONTAL);
		BorderPane rootPane = new BorderPane();

		BorderPane pane = (BorderPane) this.getTab().getContent();
		toolBar = createToolBar();
		rootPane.setTop(toolBar);
		rootPane.setCenter(editorPane);
		this.getTab().setContent(rootPane);
		
		subScene.setOnScroll(new EventHandler<ScrollEvent>() {
	            @Override
	            public void handle(ScrollEvent event) {
	              System.out.println(String.format("deltaX: %.3f deltaY: %.3f",
	                  event.getDeltaX(), 
	                  event.getDeltaY()
	              ));
	              
	              double z = subSceneCamera.getTranslateZ();
	              double newZ = z + event.getDeltaY();
	              subSceneCamera.setTranslateZ(newZ);
	              
	            }
	          });

	}

	// private static StyleSpans<Collection<String>> computeHighlighting(String
	// text) {
	// Matcher matcher = PATTERN.matcher(text);
	// int lastKwEnd = 0;
	// StyleSpansBuilder<Collection<String>> spansBuilder
	// = new StyleSpansBuilder<>();
	// while(matcher.find()) {
	// String styleClass =
	// matcher.group("KEYWORD") != null ? "keyword" :
	// matcher.group("PAREN") != null ? "paren" :
	// matcher.group("BRACE") != null ? "brace" :
	// matcher.group("BRACKET") != null ? "bracket" :
	// matcher.group("SEMICOLON") != null ? "semicolon" :
	// matcher.group("STRING") != null ? "string" :
	// matcher.group("COMMENT") != null ? "comment" :
	// null; /* never happens */ assert styleClass != null;
	// spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
	// spansBuilder.add(Collections.singleton(styleClass), matcher.end() -
	// matcher.start());
	// lastKwEnd = matcher.end();
	// }
	// spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
	// return spansBuilder.create();
	// }

	private void setCode(String code) {
		// this.codeArea.clear();
		this.caCodeArea.replaceText(code);

	}

	private String getCode() {
		return this.caCodeArea.getText();
	}

	private void compile(String code) {

		csgObject = null;

		// clearLog();

		viewGroup.getChildren().clear();

		try {

			parseGCode(caCodeArea.getText(), viewGroup);

				viewGroup.layoutXProperty().bind(
						viewContainer.widthProperty().divide(2));
				viewGroup.layoutYProperty().bind(
						viewContainer.heightProperty().divide(2));

//				viewContainer.boundsInLocalProperty().addListener(
//						(ov, oldV, newV) -> {
//							setMeshScale(meshContainer, newV, meshView);
//						});
//
				VFX3DUtil.addMouseBehavior(viewGroup, viewContainer,
					MouseButton.PRIMARY);



		} catch (Throwable ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.INFO,
					"Unsuspected xception", ex);
		}
	}

	private void setMeshScale(MeshContainer meshContainer, Bounds t1,
			final MeshView meshView) {
		if (meshContainer != null) {
			double maxDim = Math.max(
					meshContainer.getWidth(),
					Math.max(meshContainer.getHeight(),
							meshContainer.getDepth()));

			double minContDim = Math.min(t1.getWidth(), t1.getHeight());

			double scale = minContDim / (maxDim * 2);

			meshView.setScaleX(scale);
			meshView.setScaleY(scale);
			meshView.setScaleZ(scale);
		}
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

		Button btnExportSTL = GlyphsDude.createIconButton(
				MaterialDesignIcon.EXPORT, "Export STL");

		btnExportSTL.setOnAction(this::handleExportAsStlFile);

		Button btnExportPNG = GlyphsDude.createIconButton(
				MaterialDesignIcon.CAMERA, "Export PNG");
		btnExportPNG.setOnAction(this::handleExportAsPngFile);

		Button btnRun = GlyphsDude.createIconButton(MaterialDesignIcon.RUN,
				"Run");
		btnRun.setOnAction(this::handleCompileAndRun);

		ToggleButton btnAutoCompile = GlyphsDude.createIconToggleButton(
				MaterialDesignIcon.AUTO_FIX, "Automatic run", null,
				ContentDisplay.LEFT);
		btnAutoCompile.setSelected(false);

		ToggleButton btn3DNav = GlyphsDude.createIconToggleButton(
				MaterialDesignIcon.ROTATE_3D, "3D Navigation ", null,
				ContentDisplay.LEFT);
		btn3DNav.setSelected(false);

		ComboBox cbxSourceExamples = new ComboBox();
		cbxSourceExamples.getItems().addAll("BatteryHolder", "BoardMount",
				"BreadBoardConnector", "ServoMount", "Wheel");
		this.cbxSourceExamples = cbxSourceExamples; // TODO: maybe cleaner way
													// to do this ?

		Button btnPasteSource = GlyphsDude.createIconButton(
				MaterialDesignIcon.CONTENT_PASTE, "Paste source");
		btnPasteSource.setOnAction(this::handlePasteSource);

		toolBar.getItems().addAll(btnSave, btnExportSTL, btnExportPNG,
				new Separator(), btnRun, new Separator(), btnAutoCompile,
				new Separator(), cbxSourceExamples, btnPasteSource);

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

	private void handleExportAsStlFile(ActionEvent e) {

		if (csgObject == null) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Oeps an error occured");
			alert.setHeaderText("Cannot export STL. There is no geometry !");
			alert.setContentText("Please verify that your code generates a valid CSG object.");
			alert.showAndWait();
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export STL File");
		fileChooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("STL files (*.stl)", "*.stl"));

		File f = fileChooser.showSaveDialog(null);

		if (f == null) {
			return;
		}

		String fName = f.getAbsolutePath();

		if (!fName.toLowerCase().endsWith(".stl")) {
			fName += ".stl";
		}

		try {
			eu.mihosoft.vrl.v3d.FileUtil.write(Paths.get(fName),
					csgObject.toStlString());
		} catch (IOException ex) {

			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null,
					ex);
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Oeps an error occured");
			alert.setHeaderText("Cannot export STL. There went something wrong writing the file.");
			alert.setContentText("Please verify that your file is not read only, is not locked by other user or program, you have enough diskspace.");
			alert.showAndWait();
		}
	}

	private void handleExportAsPngFile(ActionEvent e) {

		if (csgObject == null) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Oeps an error occured");
			alert.setHeaderText("Cannot export PNG. There is no geometry !");
			alert.setContentText("Please verify that your code generates a valid CSG object.");
			alert.showAndWait();
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export PNG File");
		fileChooser.getExtensionFilters()
				.add(new FileChooser.ExtensionFilter("Image files (*.png)",
						"*.png"));

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

		WritableImage snapshot = new WritableImage(snWidth,
				(int) (realHeight * scale));

		viewGroup.snapshot(snapshotParameters, snapshot);

		try {
			ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png",
					new File(fName));
		} catch (IOException ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null,
					ex);
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Oeps an error occured");
			alert.setHeaderText("Cannot export PNG. There went something wrong writing the file.");
			alert.setContentText("Please verify that your file is not read only, is not locked by other user or program, you have enough diskspace.");
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
			exampleSourceCode = (String) this.cbxSourceExamples
					.getSelectionModel().getSelectedItem();
			String code = IOUtils.toString(
					this.getClass().getResourceAsStream(
							exampleSourceCode + ".jfxscad"), "UTF-8");
			this.caCodeArea.replaceText(code);
		} catch (IOException ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
					"Unable to load example source code: " + exampleSourceCode,
					ex);
		}
	}

	/**
	 * Draws a line in 3D between 2 3D points on the given group.
	 * 
	 * @param origin
	 *            Origin point
	 * @param target
	 *            Target point
	 * @return 3D line (cylinder) between to points
	 */
	private Cylinder drawLine3D(Group group, Point3D origin, Point3D target,
			Material color) {
		if (color == null) {
			color = MATERIAL_BLACK; // default to orange
		}
		Point3D yAxis = new Point3D(0, 1, 0);
		Point3D diff = target.subtract(origin);
		double height = diff.magnitude();

		Point3D mid = target.midpoint(origin);
		Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(),
				mid.getZ());

		Point3D axisOfRotation = diff.crossProduct(yAxis);
		double angle = Math.acos(diff.normalize().dotProduct(yAxis));
		Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle),
				axisOfRotation);

		Cylinder line = new Cylinder(1, height);
		line.setMaterial(color);

		line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);

		if (group != null) {
			group.getChildren().add(line);
		}

		return line;
	}

	/**
	 * Draws the axes
	 * 
	 * @param group
	 * @param distance
	 */
	private void drawAxes(Group group, long distance) {
		// TODO: add naming to axes ?
		drawLine3D(group, new Point3D(0.0, 0.0, 0.0), new Point3D(distance,
				0.0, 0.0), MATERIAL_YELLOW);
		drawLine3D(group, new Point3D(0.0, 0.0, 0.0), new Point3D(0.0,
				distance, 0.0), MATERIAL_ORANGE);
		drawLine3D(group, new Point3D(0.0, 0.0, 0.0), new Point3D(0.0, 0.0,
				distance), MATERIAL_BLUE);
	}

	/**
	 * Return absolute vector
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	private double absolute(double v1, double v2) {
		if (!isRelative) {
			return v2;
		} else {
			return v1 + v2;
		}
	}

	/**
	 * Return relative vector
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	private double delta(double v1, double v2) {
		if (isRelative) {
			return v2;
		} else {
			return v2 - v1;
		}
	}

	/**
	 * Parses a line of gcode
	 * 
	 * @param text
	 * @param lineNumber
	 * @param group
	 */
	private void parseGCodeline(String text, int lineNumber, Group group) {
		String textLine = text.replaceAll(";.*$", "").trim(); // remove comments
																// and
																// withespace
		Map<String, Double> args = new HashMap<String, Double>();
		Map<String, Double> newLine = new HashMap<String, Double>();
		if (!textLine.isEmpty()) {
			String[] tokens = textLine.split(" ");
			String cmd = tokens[0].toLowerCase();
			for (int i = 1; i < tokens.length; i++) {
				args.put(tokens[i].substring(0, 1).toLowerCase(),
						Double.valueOf(tokens[i].substring(1)));
			}
			if (cmd.equals("g0")) {
				if (args.containsKey("x")) {
					newLine.put("x",
							absolute(lastLine.get("x"), args.get("x")));
				} else {
					newLine.put("x", lastLine.get("x"));
				}
				if (args.containsKey("y")) {
					newLine.put("y",
							absolute(lastLine.get("y"), args.get("y")));
				} else {
					newLine.put("y", lastLine.get("y"));
				}
				if (args.containsKey("z")) {
					newLine.put("z",
							absolute(lastLine.get("z"), args.get("z")));
				} else {
					newLine.put("z", lastLine.get("z"));
				}
				if (args.containsKey("e")) {
					newLine.put("e",
							absolute(lastLine.get("e"), args.get("e")));
				} else {
					newLine.put("e", lastLine.get("e"));
				}
				if (args.containsKey("f")) {
					newLine.put("f",
							absolute(lastLine.get("f"), args.get("f")));
				} else {
					newLine.put("f", lastLine.get("f"));
				}
				newLine.put("init", 0.0);
				if (lastLine.containsKey("init")) {
					drawLine3D(group,
							new Point3D(lastLine.get("x"), lastLine.get("y"),
									lastLine.get("z")),
							new Point3D(newLine.get("x"), newLine.get("y"),
									newLine.get("z")), MATERIAL_GREEN);
				}
				lastLine.clear();
				lastLine.putAll(newLine);
			} else if (cmd.equals("g1")) {
				if (args.containsKey("x")) {
					newLine.put("x",
							absolute(lastLine.get("x"), args.get("x")));
				} else {
					newLine.put("x", lastLine.get("x"));
				}
				if (args.containsKey("y")) {
					newLine.put("y",
							absolute(lastLine.get("y"), args.get("y")));
				} else {
					newLine.put("y", lastLine.get("y"));
				}
				if (args.containsKey("z")) {
					newLine.put("z",
							absolute(lastLine.get("z"), args.get("z")));
				} else {
					newLine.put("z", lastLine.get("z"));
				}
				if (args.containsKey("e")) {
					newLine.put("e",
							absolute(lastLine.get("e"), args.get("e")));
				} else {
					newLine.put("e", lastLine.get("e"));
				}
				if (args.containsKey("f")) {
					newLine.put("f",
							absolute(lastLine.get("f"), args.get("f")));
				} else {
					newLine.put("f", lastLine.get("f"));
				}
				newLine.put("init", 0.0);
				if (lastLine.containsKey("init")) {
					drawLine3D(group,
							new Point3D(lastLine.get("x"), lastLine.get("y"),
									lastLine.get("z")),
							new Point3D(newLine.get("x"), newLine.get("y"),
									newLine.get("z")), MATERIAL_RED);
				}
				lastLine.clear();
				lastLine.putAll(newLine);
			} else if (cmd.equals("g99")) {
				this.isRelative = false;
			} else if (cmd.equals("g91")) {
				this.isRelative = true;
			} else if (cmd.equals("g20")) {
				// set unit to inches
			} else if (cmd.equals("g21")) {
				// set unit to mm
			}
		}
	}

	/**
	 * Parses GCode text
	 * @param text
	 * @param group
	 */
	private void parseGCode(String text, Group group) {
		//TODO: this code needs some refactoring
		lastLine.clear();
		lastLine.put("x", 0.0);
		lastLine.put("y", 0.0);
		lastLine.put("z", 0.0);
		lastLine.put("e", 0.0);
		lastLine.put("f", 0.0);

		group.getChildren().clear();
		drawAxes(group, 100L);
		InputStream is = new ByteArrayInputStream(text.getBytes());

		// read it with BufferedReader
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		try {
			while ((line = br.readLine()) != null) {
				//System.out.println(line);
				parseGCodeline(line, 0, group);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(br != null){
					br.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public ToolBar getToolBar() {
		return toolBar;
	}

	public void setToolbar(ToolBar toolBar) {
		this.toolBar = toolBar;
	}

}
