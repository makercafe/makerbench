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
