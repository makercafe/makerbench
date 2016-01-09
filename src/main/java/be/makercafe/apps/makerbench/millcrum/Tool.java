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
public class Tool {
    private String units = "mm";
    private String name = "noname";
    private double passDepth = 1;
    private double diameter = 5;
    private int step = 1;
    private double zClearance = 10;
    private double rapid = 20;
    private double plunge = 10;
    private double cut = 10;
    private boolean returnHome = true;

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    double getPassDepth() {
        return this.passDepth;
    }
    
    void setPassDepth(double passDepth){
        this.passDepth = passDepth;
    }

    public double getDiameter() {
        return diameter;
    }

    public void setDiameter(double diameter) {
        this.diameter = diameter;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public double getzClearance() {
        return zClearance;
    }

    public void setzClearance(double zClearance) {
        this.zClearance = zClearance;
    }

    public double getRapid() {
        return rapid;
    }

    public void setRapid(double rapid) {
        this.rapid = rapid;
    }

    public double getPlunge() {
        return plunge;
    }

    public void setPlunge(double plunge) {
        this.plunge = plunge;
    }

    public double getCut() {
        return cut;
    }

    public void setCut(double cut) {
        this.cut = cut;
    }

    public boolean isReturnHome() {
        return returnHome;
    }

    public void setReturnHome(boolean returnHome) {
        this.returnHome = returnHome;
    }
    
    
    
}
