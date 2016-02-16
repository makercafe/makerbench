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
package be.makercafe.apps.makerbench.millcrum;

/**
 *
 * @author m999ldp
 */
public class Config {
    private Boolean useConventionalCut;
    private Boolean tabs;
    private double tabWidth;
    private double tabHeight;
    private double tabSpacing;

    public Boolean isUseConventionalCut() {
        return useConventionalCut;
    }

    public void setUseConventionalCut(Boolean useConventionalCut) {
        this.useConventionalCut = useConventionalCut;
    }

    public Boolean getTabs() {
        return tabs;
    }

    public void setTabs(Boolean tabs) {
        this.tabs = tabs;
    }

    public double getTabWidth() {
        return tabWidth;
    }

    public void setTabWidth(double tabWidth) {
        this.tabWidth = tabWidth;
    }

    public double getTabSpacing() {
        return tabSpacing;
    }

    public void setTabSpacing(double tabSpacing) {
        this.tabSpacing = tabSpacing;
    }

    public double getTabHeight() {
        return tabHeight;
    }

    public void setTabHeight(double tabHeight) {
        this.tabHeight = tabHeight;
    }
    
    
    
}
