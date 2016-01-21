/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.makercafe.apps.makerbench.resourceview;

import java.nio.file.Path;

import javafx.scene.control.TreeItem;

/**
 *
 * @author m999ldp
 * @param <T>
 */
public class ResourceTreeItem<T extends Object> extends TreeItem<String> {

    private Path path;

    public ResourceTreeItem(String rootNode) {
        super(rootNode);
    }

    public ResourceTreeItem(String rootNode, Path path) {
        super(rootNode);
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }



}
