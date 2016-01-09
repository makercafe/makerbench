package be.makercafe.apps.makerbench.editors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;

import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleSpansBuilder;
import org.reactfx.Change;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;

import be.makercafe.apps.makerbench.editors.utils.VFX3DUtil;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.MeshContainer;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

public class EditorJFXScad extends Editor {

    private static final String[] KEYWORDS = new String[]{
        "def", "in", "as", "abstract", "assert", "boolean", "break", "byte",
        "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else",
        "enum", "extends", "final", "finally", "float",
        "for", "goto", "if", "implements", "import",
        "instanceof", "int", "interface", "long", "native",
        "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super",
        "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "void", "volatile", "while"
    };

    private static final Pattern KEYWORD_PATTERN
            = Pattern.compile("\\b(" + String.join("|", KEYWORDS) + ")\\b");

    private final Group viewGroup;

    private final CodeArea codeArea ;


    private boolean autoCompile = true;

    private CSG csgObject;

    private TextArea logView;

    private ScrollPane editorContainer;

    private Pane viewContainer;

    private SubScene subScene;

    private ToolBar toolBar = null;

   	public EditorJFXScad(String tabText, Path path) {
		super(tabText);



		viewGroup = new Group();
		editorContainer = new ScrollPane();
		viewContainer = new Pane();


		codeArea = new CodeArea();
	   	codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
	   	codeArea.setPrefSize(Double.MAX_VALUE,Double.MAX_VALUE);
        codeArea.textProperty().addListener(
                (ov, oldText, newText) -> {
                    Matcher matcher = KEYWORD_PATTERN.matcher(newText);
                    int lastKwEnd = 0;
                    StyleSpansBuilder<Collection<String>> spansBuilder
                    = new StyleSpansBuilder<>();
                    while (matcher.find()) {
                        spansBuilder.add(Collections.emptyList(),
                                matcher.start() - lastKwEnd);
                        spansBuilder.add(Collections.singleton("keyword"),
                                matcher.end() - matcher.start());
                        lastKwEnd = matcher.end();
                    }
                    spansBuilder.add(Collections.emptyList(),
                            newText.length() - lastKwEnd);
                    codeArea.setStyleSpans(0, spansBuilder.create());
                });

        EventStream<Change<String>> textEvents
                = EventStreams.changesOf(codeArea.textProperty());

        textEvents.reduceSuccessions((a, b) -> b, Duration.ofMillis(3000)).
                subscribe(code -> {
                    if (autoCompile) {
                        compile(code.getNewValue());
                    }
                });

        if(path == null){
        codeArea.replaceText(
                "CSG cube = new Cube(2).toCSG()\n"
                + "CSG sphere = new Sphere(1.25).toCSG()\n"
                + "\n"
                + "cube.difference(sphere)");
        }else{
        	try {
        		codeArea.replaceText(
        				"CSG cube = new Cube(2).toCSG()\n"
        		                + "CSG sphere = new Sphere(1.25).toCSG()\n"
        		                + "\n"
        		                + "cube.difference(sphere)"
        		+ "/*\n" + FileUtils.readFileToString(path.toFile())
        		+ "*/\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        }

        editorContainer.setContent(codeArea);

        subScene = new SubScene(viewGroup, 100, 100, true,
                SceneAntialiasing.BALANCED);

        subScene.widthProperty().bind(viewContainer.widthProperty());
        subScene.heightProperty().bind(viewContainer.heightProperty());

        PerspectiveCamera subSceneCamera = new PerspectiveCamera(false);
        subScene.setCamera(subSceneCamera);

        viewContainer.getChildren().add(subScene);

        SplitPane editorPane = new SplitPane(editorContainer,viewContainer);
        editorPane.setOrientation(Orientation.HORIZONTAL);
        BorderPane rootPane = new BorderPane();

        BorderPane pane = (BorderPane) this.getTab().getContent();
        toolBar = createToolBar();
        rootPane.setTop(toolBar);
        rootPane.setCenter(editorPane);
        this.getTab().setContent(rootPane);

	}



	private void setCode(String code) {
        codeArea.replaceText(code);
    }

    private String getCode() {
        return codeArea.getText();
    }

    private void clearLog() {
        logView.setText("");
    }

    private void compile(String code) {

        csgObject = null;

        //clearLog();

        viewGroup.getChildren().clear();

        try {

            CompilerConfiguration cc = new CompilerConfiguration();

            cc.addCompilationCustomizers(
                    new ImportCustomizer().
                    addStarImports("eu.mihosoft.vrl.v3d",
                            "eu.mihosoft.vrl.v3d.samples").
                    addStaticStars("eu.mihosoft.vrl.v3d.Transform"));

            GroovyShell shell = new GroovyShell(getClass().getClassLoader(),
                    new Binding(), cc);

            Script script = shell.parse(code);




            Object obj = script.run();

            if (obj instanceof CSG) {

                CSG csg = (CSG) obj;

                csgObject = csg;

                MeshContainer meshContainer = csg.toJavaFXMesh();

                final MeshView meshView = meshContainer.getAsMeshViews().get(0);

                setMeshScale(meshContainer,
                        viewContainer.getBoundsInLocal(), meshView);

                PhongMaterial m = new PhongMaterial(Color.RED);

                meshView.setCullFace(CullFace.NONE);

                meshView.setMaterial(m);

                viewGroup.layoutXProperty().bind(
                        viewContainer.widthProperty().divide(2));
                viewGroup.layoutYProperty().bind(
                        viewContainer.heightProperty().divide(2));

                viewContainer.boundsInLocalProperty().addListener(
                        (ov, oldV, newV) -> {
                            setMeshScale(meshContainer, newV, meshView);
                        });

                VFX3DUtil.addMouseBehavior(meshView,
                        viewContainer, MouseButton.PRIMARY);

                viewGroup.getChildren().add(meshView);

            } else {
                System.out.println(">> no CSG object returned :(");
            }

        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
        }
    }

    private void setMeshScale(
            MeshContainer meshContainer, Bounds t1, final MeshView meshView) {
        double maxDim
                = Math.max(meshContainer.getWidth(),
                        Math.max(meshContainer.getHeight(),
                                meshContainer.getDepth()));

        double minContDim = Math.min(t1.getWidth(), t1.getHeight());

        double scale = minContDim / (maxDim * 2);

        meshView.setScaleX(scale);
        meshView.setScaleY(scale);
        meshView.setScaleZ(scale);
    }

	/**
	 * Creates the toolBar for the editor.
	 * @return
	 */

	public ToolBar createToolBar() {

		ToolBar toolBar = new ToolBar();
		toolBar.setOrientation(Orientation.HORIZONTAL);

		Button btnSave = GlyphsDude.createIconButton(MaterialDesignIcon.FLOPPY, "Save");
		btnSave.setOnAction(this::handleSaveButton);

		Button btnExportSTL = GlyphsDude.createIconButton(MaterialDesignIcon.EXPORT, "Export STL");
		//btnSave.setOnAction(this::handleSaveButton);

		Button btnRun = GlyphsDude.createIconButton(MaterialDesignIcon.RUN, "Run");

		ToggleButton btn3DNav = GlyphsDude.createIconToggleButton(MaterialDesignIcon.ROTATE_3D, "3D Navigation ", null, ContentDisplay.LEFT);
		btn3DNav.setSelected(false);

		toolBar.getItems().addAll(btnSave, btnExportSTL, new Separator(), btnRun, new Separator(), btn3DNav);


		return toolBar;

	}

	public void handleSaveButton(ActionEvent event){
		System.out.println("Event: " + event.getSource());
		Map<String, String> map = (Map<String,String>) this.getTab().getUserData();
		String path = map.get("path");
		try {
			FileUtils.writeStringToFile(new File(path), codeArea.getText());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ToolBar getToolBar() {
		return toolBar;
	}

	public void setToolbar(ToolBar toolBar) {
		this.toolBar = toolBar;
	}

    public TextArea getLogView() {
        return logView;
    }

}
