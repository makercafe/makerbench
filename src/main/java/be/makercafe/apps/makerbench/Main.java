/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

import org.apache.commons.io.FileUtils;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import be.makercafe.apps.makerbench.editors.JFXScadEditor;
import be.makercafe.apps.makerbench.resourceview.ResourceTreeCell;
import be.makercafe.apps.makerbench.resourceview.ResourceTreeItem;

/**
 *
 * @author m999ldp
 */
public class Main extends Application {

	private TabPane tabFolder;
	private TreeView viewer;
	final FileChooser fileChooser = new FileChooser();
	final DirectoryChooser directoryChooser = new DirectoryChooser();
	private Stage stage;

	@Override
	public void start(Stage primaryStage) {
		final ContextMenu rootContextMenu = ContextMenuBuilder
				.create()
				.items(MenuItemBuilder.create().text("Menu Item")
						.onAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent arg0) {
								System.out.println("Menu Item Clicked!");
								ResourceTreeItem<String> item = (ResourceTreeItem<String>) viewer
										.getSelectionModel().getSelectedItem();
								System.out.println("Selected resource item : " + item.getValue());
							}
						}).build()).build();
		
		this.stage = primaryStage;
		BorderPane p = new BorderPane();

		p.setTop(createMenuBar());
		viewer = new TreeView(createTree(null));
		viewer.setMinWidth(200.0);
		viewer.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		viewer.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {

				if (mouseEvent.getClickCount() == 2) {
					@SuppressWarnings("unchecked")
					ResourceTreeItem<String> item = (ResourceTreeItem<String>) viewer
							.getSelectionModel().getSelectedItem();
					System.out.println("Selected Text : " + item.getValue());

					createAndAttachTab(item.getValue(), item.getPath(), null);
				}

			}
		});
		viewer.setContextMenu(rootContextMenu);
//		viewer.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
//
//			@Override
//			public TreeCell<String> call(TreeView<String> arg0) {
//				// custom tree cell that defines a context menu for the root
//				// tree item
//				return new ResourceTreeCell();
//			}
//		});
		viewer.setEditable(false);
		viewer.setShowRoot(false);

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

		Scene scene = new Scene(p, 800, 600);
		scene.getStylesheets().add(
				this.getClass().getResource("/styles/java-keywords.css")
						.toExternalForm());

		primaryStage.setResizable(true);
		primaryStage.setTitle("Makerbench");
		primaryStage.setScene(scene);
		primaryStage.show();


	}

	/**
	 * Dummy tree builder
	 *
	 * @param file
	 * @return
	 */
	private TreeItem<String> createTree(File file) {
		ResourceTreeItem<String> root = new ResourceTreeItem<>("Root Node");

		root.setExpanded(true);

		root.getChildren().addAll(new ResourceTreeItem<>("Item 1"),
				new ResourceTreeItem<>("Item 2"),
				new ResourceTreeItem<>("Item 3"));
		return root;
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

				viewer.setRoot(loadFolder(file));

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
				if (result.isPresent()){
					String homeDir = System.getProperty("user.home");
					
				    System.out.println("Folder name: " + result.get());
				    System.out.println("User home: " + homeDir);
				}
			}
		});
		newProject.getItems().add(newFolder);
		
		MenuItem importProject = new MenuItem("Import");
		importProject.setAccelerator(new KeyCodeCombination(KeyCode.I,
				KeyCombination.CONTROL_DOWN));
		importProject.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				System.out.println("Import");

			}
		});
		MenuItem deleteProject = new MenuItem("Delete");
		deleteProject.setAccelerator(new KeyCodeCombination(KeyCode.D,
				KeyCombination.CONTROL_DOWN));
		deleteProject.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				System.out.println("Delete");

			}
		});
		projectMenu.getItems().addAll(openProject, newProject, importProject,
				deleteProject);
		
		Menu helpMenu = new Menu("Help");
		
		MenuItem aboutItem = new MenuItem("About");
		aboutItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Information Dialog");
				alert.setHeaderText("About makerbench");
				alert.setContentText("Makerbench is an open source IDE for designing and manufacturing objects and code.\nWritten by Luc De pauw\n\nUses opensource libraries from the next projects:\n-RichtextFX by Tomas Mikula\n-JCSG by Michael Hoffer\n-ControlsFX by FXexperience.com");
				alert.showAndWait();
			}
		});

		helpMenu.getItems().add(aboutItem);
		bar.getMenus().addAll(projectMenu,helpMenu);
		return bar;
	}

	private Tab createAndAttachTab(String text, Path path, File item) {
		Tab t = null;
		Map<String, String> map = new HashMap<String, String>();
		if (path != null) {
			map.put("path", path.toFile().getAbsolutePath());
		} else {
			map.put("path", "noname.txt");
		}
		// We should lookup the type of editor here.

		JFXScadEditor editor = new JFXScadEditor(text, path);
		t = editor.getTab();
		// Hookup the userdata
		t.setUserData(map);
		tabFolder.getTabs().add(t);
		return t;
	}

	private ResourceTreeItem<String> loadFolder(File file) {
		ResourceTreeItem<String> root = new ResourceTreeItem<String>("root");
		root.setExpanded(true);
		addItem(root, file);
		return root;
	}

	private ResourceTreeItem<String> addItem(ResourceTreeItem<String> parent,
			File file) {
		if (file.isDirectory()) {
			ResourceTreeItem<String> folder = new ResourceTreeItem<>(
					file.getName(), file.toPath());
			parent.getChildren().add(folder);
			try {
				List<File> files = Arrays.asList(file.listFiles());
				for (File subFile : files) {
					addItem(folder, subFile);
				}
			} catch (NullPointerException e) {
				System.out.print(folder.getValue());
				e.printStackTrace();
			}

		} else {
			ResourceTreeItem<String> fileItem = new ResourceTreeItem<>(
					file.getName(), file.toPath());
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
