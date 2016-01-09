/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.makercafe.apps.makerbench.millcrum;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;

/**
 *
 * @author m999ldp
 */
public class MillPoint extends Point2D {
    private String type = "";
    private double startDegree = 0;
    private double endDegree = 0;
    private double radius = 0;
    private List<MillPoint> points = new ArrayList<>();
    
    public MillPoint(){
        super(0,0);
    }

    public MillPoint(double d, double d1) {
        super(d, d1);
    }
    

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getStartDegree() {
        return startDegree;
    }

    public void setStartDegree(double startDegree) {
        this.startDegree = startDegree;
    }

    public double getEndDegree() {
        return endDegree;
    }

    public void setEndDegree(double endDegree) {
        this.endDegree = endDegree;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

//    public Point2D getPoint() {
//        return point;
//    }
//
//    public void setPoint(Point2D point) {
//        this.point = point;
//    }

    public List<MillPoint> getPoints() {
        return points;
    }

    public void setPoints(List<MillPoint> points) {
        this.points = points;
    }
    
       public List<Point2D> getPointsAsPoint2D() {
        List<Point2D> list = new ArrayList<>();
        for(MillPoint point : points){
            list.add((Point2D) point);
        }
        return list;
    }
    
}
