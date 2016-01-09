/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.makercafe.apps.makerbench.millcrum;

import javafx.geometry.Point2D;

/**
 *
 * @author m999ldp
 */
public class PointAndAngle {
    
    private Point2D point;
    private double angle;

    public PointAndAngle(Point2D point, double angle) {
        this.point = point;
        this.angle = angle;
    }

    public Point2D getPoint() {
        return point;
    }

    public void setPoint(Point2D point) {
        this.point = point;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
    
    
    
}
