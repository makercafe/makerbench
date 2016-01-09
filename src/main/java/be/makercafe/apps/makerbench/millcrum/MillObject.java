/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.makercafe.apps.makerbench.millcrum;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;

/**
 *
 * @author m999ldp
 */
public class MillObject {
    private String name = "noname";
    private String type = "";
    private double cornerRadius;
    private double xLen;
    private double yLen;
    private List<MillPoint> points = new ArrayList<>();
    private double radius;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(double cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    public double getxLen() {
        return xLen;
    }

    public void setxLen(double xLen) {
        this.xLen = xLen;
    }

    public double getyLen() {
        return yLen;
    }

    public void setyLen(double yLen) {
        this.yLen = yLen;
    }

    public List<MillPoint> getPoints() {
        return points;
    }
    
    public List<Point2D> getPointsAsPoint2D() {
        List<Point2D> list = new ArrayList<>();
        for(MillPoint point : points){
            list.add((Point2D) point);
        }
        return list;
    }

    public void setPoints(List<MillPoint> points) {
        this.points = points;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
    
}
