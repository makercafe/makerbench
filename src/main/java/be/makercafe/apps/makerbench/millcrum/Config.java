/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
