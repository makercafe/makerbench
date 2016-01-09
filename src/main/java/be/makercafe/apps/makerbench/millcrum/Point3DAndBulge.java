/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.makercafe.apps.makerbench.millcrum;

import javafx.geometry.Point3D;

/**
 *
 * @author m999ldp
 */
public class Point3DAndBulge extends Point3D{
    private Double bulge;

    public Point3DAndBulge(double x, double y, double z) {
        super(x, y, z);
    }
    
    public Point3DAndBulge(double x, double y, double z, Double bulge) {
        super(x, y, z);
        this.bulge = bulge;
    }

    public Double getBulge() {
        return bulge;
    }

    public void setBulge(Double bulge) {
        this.bulge = bulge;
    }
    
    
}
