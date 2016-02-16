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
package be.makercafe.apps.makerbench;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import be.makercafe.apps.makerbench.editors.Editor;
import be.makercafe.apps.makerbench.editors.GCodeEditor;
import be.makercafe.apps.makerbench.editors.JFXScadEditor;
import be.makercafe.apps.makerbench.editors.JFXMillEditor;
import be.makercafe.apps.makerbench.editors.TextEditor;
import be.makercafe.apps.makerbench.editors.XMLEditor;
import be.makercafe.apps.makerbench.resourceview.ResourceTreeItem;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


/**
 * Main class
 *
 * @author luc.de.pauw@makercafe.be
 */
public class Main extends Application {

	private TabPane tabFolder;
	private TreeView<String> viewer;
	final FileChooser fileChooser = new FileChooser();
	final DirectoryChooser directoryChooser = new DirectoryChooser();
	private Stage stage;
	private String pathMakerbenchHome = System.getProperty("user.home") + File.separatorChar + "makerbench";
	private ContextMenu rootContextMenu;

	@Override
	public void start(Stage primaryStage) {
		setupWorkspace();
		rootContextMenu = createViewerContextMenu();
		viewer = createViewer();

		this.stage = primaryStage;
		BorderPane p = new BorderPane();

		p.setTop(createMenuBar());

		// p.setLeft(viewer);
		tabFolder = new TabPane();
		BorderPane bodyPane = new BorderPane();
		TextArea taConsole = new TextArea();
		taConsole.setPrefSize(Double.MAX_VALUE, 200.0);
		taConsole.setEditable(false);

		Console console = new Console(taConsole);
		PrintStream ps = new PrintStream(console, true);
		System.setOut(ps);
		System.setErr(ps);

		bodyPane.setBottom(taConsole);
		bodyPane.setCenter(tabFolder);
		SplitPane splitpane = new SplitPane();
		splitpane.getItems().addAll(viewer, bodyPane);
		splitpane.setDividerPositions(0.0f, 1.0f);
		p.setCenter(splitpane);

		Scene scene = new Scene(p, 1024, 800);
		//scene.getStylesheets().add(this.getClass().getResource("/styles/java-keywords.css").toExternalForm());


		primaryStage.setResizable(true);
		primaryStage.setTitle("Makerbench");
		primaryStage.setScene(scene);
		//primaryStage.getIcons().add(new Image("/path/to/stackoverflow.jpg"));
		primaryStage.show();
	}

	/**
	 * Setup workbench files
	 */
	private void setupWorkspace() {
		File home = new File(this.pathMakerbenchHome);
		if (!home.exists()) {
			if (home.mkdir()) { // create makerbench home dir in users home
				File project = new File(this.pathMakerbenchHome + File.separatorChar + "project01");
				if (project.mkdir()) {
					File jfxscadFile = new File(project.getAbsolutePath() + File.separatorChar + "example01.jfxscad");
					String jfxscadSource = "CSG cube = new Cube(2).toCSG()\n"
							+ "CSG sphere = new Sphere(1.25).toCSG()\n" + "\n" + "cube.difference(sphere)";
					try {
						FileUtils.write(jfxscadFile, jfxscadSource);
					} catch (IOException e) {
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
								"Unable to write example source code: " + jfxscadFile.getAbsolutePath(), e);
					}
					File readmeFile = new File(project.getAbsolutePath() + File.separatorChar + "readme.txt");
					String readmeSource = "About makerbench";
					try {
						FileUtils.write(readmeFile, readmeSource);
					} catch (IOException e) {
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
								"Unable to write example source code: " + readmeFile.getAbsolutePath(), e);
					}
				}
			}
		}
	}

	private ContextMenu createViewerContextMenu() {

		ContextMenu rootContextMenu = new ContextMenu();
		// Add Folder..
		MenuItem addFolder = new MenuItem("Add folder..");
		addFolder.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				TextInputDialog dialog = new TextInputDialog("myfolder");
				dialog.setTitle("New folder");
				dialog.setHeaderText("Create a new folder");
				dialog.setContentText("Folder name:");
				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					ResourceTreeItem<String> item = (ResourceTreeItem<String>) viewer.getSelectionModel()
							.getSelectedItem();

					File file = new File(item.getPath().toFile().getAbsolutePath() + File.separatorChar + result.get());
					if (!file.exists()) {
						if (!file.mkdir()) {
							Alert alert = new Alert(AlertType.ERROR);
							alert.setTitle("Error Dialog");
							alert.setHeaderText("Error occured while creating folder");
							alert.setContentText("Folder path: " + file.getAbsolutePath());
							alert.showAndWait();
						} else {
							viewer.setRoot(setRootFolder(new File(pathMakerbenchHome)));
						}
					}

				}
			}
		});

		// Delete folder
		MenuItem deleteFolder = new MenuItem("Delete folder..");
		deleteFolder.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirmation Dialog");
				alert.setHeaderText("Delete folder");
				alert.setContentText("Please confirm deleteion of selected folder and all it's contents ?");

				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK) {
					ResourceTreeItem<String> item = (ResourceTreeItem<String>) viewer.getSelectionModel()
							.getSelectedItem();
					File file = new File(item.getPath().toFile().getAbsolutePath());
					if (file.exists()) {
						try {
							FileUtils.deleteDirectory(file);
							viewer.setRoot(setRootFolder(new File(pathMakerbenchHome)));
						} catch (Exception e) {
							alert = new Alert(AlertType.ERROR);
							alert.setTitle("Error Dialog");
							alert.setHeaderText("Error occured while deleting folder");
							alert.setContentText("Error messsage: " + e.getMessage());
							alert.showAndWait();
						}

					}

				}
			}
		});

		// Add File..
		MenuItem addFile = new MenuItem("Add file..");
		addFile.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				TextInputDialog dialog = new TextInputDialog("myfile.txt");
				dialog.setTitle("New file");
				dialog.setHeaderText("Create a new file (.jfxscad, .jfxmill, .txt, .md, .xml, .gcode");
				dialog.setContentText("File name:");
				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					ResourceTreeItem<String> item = (ResourceTreeItem<String>) viewer.getSelectionModel()
							.getSelectedItem();

					File file = new File(item.getPath().toFile().getAbsolutePath() + File.separatorChar + result.get());
					if (!file.exists()) {
						try {
							file.createNewFile();
							viewer.setRoot(setRootFolder(new File(pathMakerbenchHome)));
						} catch (Exception e) {
							Alert alert = new Alert(AlertType.ERROR);
							alert.setTitle("Error Dialog");
							alert.setHeaderText("Error occured while creating file");
							alert.setContentText(
									"File path: " + file.getAbsolutePath() + "\nError message: " + e.getMessage());
							alert.showAndWait();
						}
					}

				}
			}
		});

		// Delete file
		MenuItem deleteFile = new MenuItem("Delete file..");
		deleteFile.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirmation Dialog");
				alert.setHeaderText("Delete file");
				alert.setContentText("Please confirm deleteion of selected file");

				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK) {
					ResourceTreeItem<String> item = (ResourceTreeItem<String>) viewer.getSelectionModel()
							.getSelectedItem();
					File file = new File(item.getPath().toFile().getAbsolutePath());
					if (file.exists()) {

						if (!file.delete()) {

							alert = new Alert(AlertType.ERROR);
							alert.setTitle("Error Dialog");
							alert.setHeaderText("Error occured while deleting file");
							alert.setContentText("File path: " + file.getAbsolutePath());
							alert.showAndWait();
						} else {
							viewer.setRoot(setRootFolder(new File(pathMakerbenchHome)));
						}

					}

				}
			}
		});
		rootContextMenu.getItems().addAll(addFolder, deleteFolder, addFile, deleteFile);
		return rootContextMenu;
	}

	/**
	 * Creates the viewer control
	 *
	 * @return
	 */
	private TreeView<String> createViewer() {
		TreeView<String> viewer = new TreeView<String>(setRootFolder(new File(this.pathMakerbenchHome)));
		viewer.setMinWidth(200.0);
		viewer.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		viewer.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {

				if (mouseEvent.getClickCount() == 2) {
					ResourceTreeItem<String> item = (ResourceTreeItem<String>) viewer.getSelectionModel()
							.getSelectedItem();
//					System.out.println("Selected Text : " + item.getValue());

					createEditor(item.getValue(), item.getPath(), null);
				} else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
					ResourceTreeItem<String> item = (ResourceTreeItem<String>) viewer.getSelectionModel()
							.getSelectedItem();
					if (item.getPath().toFile().isFile()) {
						viewer.getContextMenu().getItems().get(0).setDisable(true);
						viewer.getContextMenu().getItems().get(1).setDisable(true);
						viewer.getContextMenu().getItems().get(2).setDisable(true);
					} else {
						viewer.getContextMenu().getItems().get(0).setDisable(false);
						viewer.getContextMenu().getItems().get(1).setDisable(false);
						viewer.getContextMenu().getItems().get(2).setDisable(false);
					}
				}

			}
		});
		viewer.setContextMenu(rootContextMenu);
		viewer.setEditable(false);
		viewer.setShowRoot(false);

		return viewer;
	}

	/**
	 * Creates the menubar
	 *
	 * @return
	 */
	private MenuBar createMenuBar() {
		MenuBar bar = new MenuBar();
		Menu projectMenu = new Menu("Project");
		MenuItem openProject = new MenuItem("Open...");
		openProject.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				directoryChooser.setTitle("Please choose a project folder");
				File file = directoryChooser.showDialog(stage);

				viewer.setRoot(setRootFolder(file));

			}
		});
		Menu newProject = new Menu("New");

		MenuItem newFolder = new MenuItem("Folder...");
		newFolder.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				TextInputDialog dialog = new TextInputDialog("my_project_folder");
				dialog.setTitle("New folder");
				dialog.setHeaderText("Create a new folder");
				dialog.setContentText("Folder name:");
				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					String homeDir = System.getProperty("user.home");

					System.out.println("Folder name: " + result.get());
					System.out.println("User home: " + homeDir);
				}
			}
		});
		newProject.getItems().add(newFolder);

		MenuItem importProject = new MenuItem("Import");
		importProject.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
		importProject.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				System.out.println("Import");

			}
		});
		MenuItem deleteProject = new MenuItem("Delete");
		deleteProject.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
		deleteProject.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				System.out.println("Delete");

			}
		});
		projectMenu.getItems().addAll(openProject, newProject, importProject, deleteProject);

		Menu helpMenu = new Menu("Help");

		MenuItem aboutItem = new MenuItem("About");
		aboutItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Information Dialog");
				alert.setHeaderText("About makerbench");
				alert.setContentText(
						"Makerbench is an open source IDE for designing and manufacturing objects and code.\nWritten by Luc De pauw\n\nUses opensource libraries from the next projects:\n-RichtextFX by Tomas Mikula\n-JCSG by Michael Hoffer\n-ControlsFX by FXexperience.com");
				alert.showAndWait();
			}
		});

		helpMenu.getItems().add(aboutItem);
		bar.getMenus().addAll(projectMenu, helpMenu);
		return bar;
	}

	private Tab createEditor(String text, Path path, File item) {
		Tab t = null;
		if (path != null && path.toFile().isFile()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("path", path.toFile().getAbsolutePath());
			// We should lookup the type of editor here
			// TODO: refactor quick and dirty implementation
			Editor editor;
			String absPath = path.toFile().getAbsolutePath();
			if (absPath.endsWith("jfxscad")) {
				editor = new JFXScadEditor(text, path);
			} else if (absPath.endsWith("jfxmill")) {
				editor = new JFXMillEditor(text, path);
			} else if (absPath.endsWith("xml") || absPath.endsWith("xsd")) {
				editor = new XMLEditor(text, path);
			} else if (absPath.endsWith("gcode") || absPath.endsWith("tap")) {
				editor = new GCodeEditor(text, path);
			} else {
				editor = new TextEditor(text, path);
			}
			t = editor.getTab();
			// Hookup the userdata
			t.setUserData(map);
			tabFolder.getTabs().add(t);
		}
		return t;
	}

	private ResourceTreeItem<String> setRootFolder(File file) {
		ResourceTreeItem<String> root = new ResourceTreeItem<String>("root");
		root.setExpanded(true);
		addItem(root, file);
		return root;
	}

	private ResourceTreeItem<String> addItem(ResourceTreeItem<String> parent, File file) {
		if (file.isDirectory()) {
			ResourceTreeItem<String> folder = new ResourceTreeItem<>(file.getName(), file.toPath());
			GlyphsDude.setIcon(folder, MaterialDesignIcon.FOLDER);
			System.out.println("Style:" + folder.getGraphic().getStyle());
			folder.getGraphic().setStyle("-fx-font-family: MaterialDesignIcons; -fx-font-size: 1.2em; -fx-fill: green;");

			parent.getChildren().add(folder);
			try {
				List<File> files = Arrays.asList(file.listFiles());
				for (File subFile : files) {
					addItem(folder, subFile);
				}
			} catch (NullPointerException e) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Unable to open workbench.", e);
			}
			folder.setExpanded(true);

		} else {
			ResourceTreeItem<String> fileItem = new ResourceTreeItem<>(file.getName(), file.toPath());
			GlyphsDude.setIcon(fileItem, MaterialDesignIcon.FILE);
			fileItem.getGraphic().setStyle("-fx-font-family: MaterialDesignIcons; -fx-font-size: 1.2em; -fx-fill: green;");

			parent.getChildren().add(fileItem);
		}
		return parent;
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

	public static class Console extends OutputStream {

		private TextArea output;

		public Console(TextArea ta) {
			this.output = ta;
		}

		@Override
		public void write(int i) throws IOException {
			output.appendText(String.valueOf((char) i));
		}
	}

}
