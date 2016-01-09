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

import org.apache.commons.io.FileUtils;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
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
import be.makercafe.apps.makerbench.editors.EditorJFXScad;
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
		final ContextMenu rootContextMenu = ContextMenuBuilder.create()
				.items(MenuItemBuilder.create().text("Menu Item").onAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						System.out.println("Menu Item Clicked!");
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
					ResourceTreeItem<String> item = (ResourceTreeItem<String>) viewer.getSelectionModel()
							.getSelectedItem();
					System.out.println("Selected Text : " + item.getValue());

					createAndAttachTab(item.getValue(), item.getPath(), null);
				}

			}
		});
		viewer.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {

			@Override
			public TreeCell<String> call(TreeView<String> arg0) {
				// custom tree cell that defines a context menu for the root
				// tree item
				return new ResourceTreeCell();
			}
		});
		viewer.setEditable(true);
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
		scene.getStylesheets().add(this.getClass().getResource("/styles/java-keywords.css").toExternalForm());

		primaryStage.setResizable(true);
		primaryStage.setTitle("Makerbench");
		primaryStage.setScene(scene);
		primaryStage.show();

		// // Custom dialog
		// Dialog<PhoneBook> dialog = new Dialog<>();
		// String titleTxt="title";
		// dialog.setTitle(titleTxt);
		// dialog.setHeaderText("This is a dialog. Enter info and \n" +
		// "press Okay (or click title bar 'X' for cancel).");
		// dialog.setResizable(true);
		//
		// // Widgets
		// Label label1 = new Label("Name: ");
		// Label label2 = new Label("Phone: ");
		// TextField text1 = new TextField();
		// TextField text2 = new TextField();
		//
		// // Create layout and add to dialog
		// GridPane grid = new GridPane();
		// grid.setAlignment(Pos.CENTER);
		// grid.setHgap(10);
		// grid.setVgap(10);
		// grid.setPadding(new Insets(20, 35, 20, 35));
		// grid.add(label1, 1, 1); // col=1, row=1
		// grid.add(text1, 2, 1);
		// grid.add(label2, 1, 2); // col=1, row=2
		// grid.add(text2, 2, 2);
		// dialog.getDialogPane().setContent(grid);
		//
		// // Add button to dialog
		// ButtonType buttonTypeOk = new ButtonType("Okay", ButtonData.OK_DONE);
		// dialog.getDialogPane().getButtonTypes().add(buttonTypeOk );
		//
		// // Result converter for dialog
		// dialog.setResultConverter(new Callback<ButtonType, PhoneBook>() {
		// @Override
		// public PhoneBook call(ButtonType b) {
		//
		// if (b == buttonTypeOk) {
		//
		// return new PhoneBook(text1.getText(), text2.getText());
		// }
		//
		// return null;
		// }
		// });
		//
		// // Show dialog
		// Optional<PhoneBook> result = dialog.showAndWait();
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

		root.getChildren().addAll(new ResourceTreeItem<>("Item 1"), new ResourceTreeItem<>("Item 2"),
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
		MenuItem newProject = new MenuItem("New");
		// newProject.setGraphic((new Glyph("FontAwesome",
		// FontAwesome.Glyph.SAVE)).color(Color.BLACK));
		newProject.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
		newProject.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				System.out.println("New");

			}
		});
		MenuItem importProject = new MenuItem("Import");
		// importProject.setGraphic((new Glyph("FontAwesome",
		// FontAwesome.Glyph.SAVE)).color(Color.BLACK));
		importProject.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
		importProject.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				System.out.println("Import");

			}
		});
		MenuItem deleteProject = new MenuItem("Delete");
		// importProject.setGraphic((new Glyph("FontAwesome",
		// FontAwesome.Glyph.SAVE)).color(Color.BLACK));
		deleteProject.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
		deleteProject.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				System.out.println("Delete");

			}
		});
		projectMenu.getItems().addAll(openProject, newProject, importProject, deleteProject);
		bar.getMenus().add(projectMenu);
		return bar;
	}

	private Tab createAndAttachTab(String text, Path path, File item) {
		Tab t = null;
		Map<String, String> map = new HashMap<String, String>();
		map.put("path", path.toFile().getAbsolutePath());
		// We should lookup the type of editor here.

		EditorJFXScad editor = new EditorJFXScad(text,path);
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

	private ResourceTreeItem<String> addItem(ResourceTreeItem<String> parent, File file) {
		if (file.isDirectory()) {
			ResourceTreeItem<String> folder = new ResourceTreeItem<>(file.getName(), file.toPath());
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
			ResourceTreeItem<String> fileItem = new ResourceTreeItem<>(file.getName(), file.toPath());
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

	// private class PhoneBook {
	//
	// private String name;
	// private String phone;
	//
	// PhoneBook(String s1, String s2) {
	//
	// name = s1;
	// phone = s2;
	// }
	//
	// @Override
	// public String toString() {
	//
	// return (name + ", " + phone);
	// }
	// }

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
