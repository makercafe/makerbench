package be.makercafe.apps.makerbench.editors;


import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

/**
 * Abstract class which forms the base for all editors in makerbench.
 *
 * @author luc.de.pauw@makercafe.be
 *
 */

public abstract class Editor{
	private Tab tab = new Tab();

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




}
