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
