/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.makercafe.apps.makerbench.millcrum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

/**
 *
 * @author m999ldp
 */
public class Dxf {

    private List invalidEntities  = new ArrayList();;
    private List layers= new ArrayList();
    private List<Line> lines = new ArrayList<>();
    private List<PolyLine> polylines =  new ArrayList();;
    private Point2D minPoint = new Point2D(0, 0);
    private Point2D maxPoint = new Point2D(0, 0);
    private List<String> alerts = new ArrayList<String>();
    private double width = 0;
    private double height = 0;
    private double avgSize = 0;

    public void Dxf() {
        this.invalidEntities = new ArrayList();
        this.layers = new ArrayList();
        this.lines = new ArrayList<>();
        this.polylines = new ArrayList();
        this.minPoint = new Point2D(0, 0);
        this.maxPoint = new Point2D(0, 0);
        this.width = 0;
        this.height = 0;
        this.avgSize = 0;
        this.alerts = new ArrayList<String>();
    }

    ;

public double terneryDiff(double a, double b) {
        return (a > b) ? a - b : b - a;
    }

    ;

public double addDegrees(double base, double mod) {
        // this function expects a 360 degree number
        // base and mod must be between 0-360
        double v = base + mod;
        if (v > 360) {
            v = 360 - v;
        } else if (v < 0) {
            v = 360 + v;
        }
        return Math.abs(v);
    }

    ;


public double distanceFormula(Point2D p1, Point2D p2) {
        // get the distance between p1 and p2
        double a = (p2.getX() - p1.getX()) * (p2.getX() - p1.getX());
        double b = (p2.getY() - p1.getY()) * (p2.getY() - p1.getY());
        return Math.sqrt(a + b);
    }

    ;

public Point2D newPointFromDistanceAndAngle(Point2D pt, double ang, double distance) {
        // use cos and sin to get a new point with an angle
        // and distance from an existing point
        // pt = [x,y]
        // ang = in degrees
        // distance = N
        return new Point2D(pt.getX() + (distance * Math.cos(ang * Math.PI / 180)), pt.getY() + (distance * Math.sin(ang * Math.PI / 180)));
    }

    ;

public Point3D crossProduct(Point3D v1, Point3D v2) {
        // get the cross product of 2 matrices
        return new Point3D((v1.getY() * v2.getZ()) - (v1.getZ() * v2.getY()), (v1.getZ() * v2.getX()) - (v1.getX() * v2.getZ()), (v1.getX() * v2.getY()) - (v1.getY() * v2.getX()));
    }

    

public Point2D calcBulgeCenter(Point3DAndBulge p1, Point2D p2) {

        Double bulge = p1.getBulge();

        Point3D chord = new Point3D(p2.getX() - p1.getX(), p2.getY() - p1.getY(), -1);
	// need to set the Z dimension for crossProduct, default is -1

        // get the frobenius/euclidean norm of the chord
        // which is the square root of the sum of the absolute squares of the elements
        double chordLength = Math.sqrt((chord.getX() * chord.getX()) + (chord.getY() * chord.getY()));

        // get the sagitta
        double sagitta = (bulge * chordLength) / 2;

        double incAngle = Math.atan(bulge) * 4;
        double radius = (chordLength / 2) / Math.sin(incAngle / 2);

        // now we need the cross product of the chord based on a negative or positive bulge
        Point3D perp;
        if (bulge >= 0) {
            perp = this.crossProduct(chord, new Point3D(0, 0, -1));

        } else {
            perp = this.crossProduct(chord, new Point3D(0, 0, 1));
            radius = -radius;
        }

        // get the mid point which is just halving the chord vector (*.5) then adding the p1 vector to that result
        Point2D chord_mid_pt = new Point2D((chord.getX() * .5) + p1.getX(), (chord.getY() * .5) + p1.getY());

        // get the unit vector which is the perp vector divided by the frobenius/euclidean norm of the perp vector
        Point2D unit_vec = new Point2D(perp.getX() / Math.sqrt((perp.getX() * perp.getX()) + (perp.getY() * perp.getY())), perp.getY() / Math.sqrt((perp.getX() * perp.getX()) + (perp.getY() * perp.getY())));

        // and then the arc_center which is: multiply the (radius-sagitta) by the unit_vec then add that to chord_mid_pt
        return new Point2D(((radius - sagitta) * unit_vec.getX()) + chord_mid_pt.getX(), ((radius - sagitta) * unit_vec.getY()) + chord_mid_pt.getY());

    }

    ;

public void handleHeader(String[] d) {

        //console.log('handleHeader',d);
        // loop through the header and pull out info we want
        for (int c = 0; c < d.length; c++) {
            if (d[c].equalsIgnoreCase("$acadver")) {
                System.out.println("autocad drawing database version " + d[c + 1]);
                // no need to continue parsing the header until we want more data
                // some editors don't even include a header section!
                break;
            }
        }

    }

    ;



public void handleEntities(String[] d) throws Exception {

        //console.log('handleEntities',d);
        // each entity starts with '  0' then the next line is the type of entity
        Entity currentEntity = new Entity();

        String[] entitiesToKeep = {"lwpolyline", "polyline", "line", "circle", "arc"};

        int totalEntities = 0;

        // loop through all of the entities lines
        for (int c = 0; c < d.length; c++) {

            if (d[c].equals("  0")) {

                // if the next line is undefined then there are no more entities
                if ((c + 1) < d.length) { //Not sure if we translated this ok to java

                    boolean isValid = false;

                    // now we can see if this entity is one we want to process
                    for (int i = 0; i < entitiesToKeep.length; i++) {
                        if (entitiesToKeep[i].equals(d[c + 1])) {
                            // this is a keeper
                            //console.log('found keeper entity '+d[c+1]);
                            currentEntity.setType(d[c + 1]);
                            currentEntity.setLines(new ArrayList<String>());

                            c++;

                            // loop through the next lines until the next '  0'
                            while (!d[c].equals("  0")) {

                                // add line to currentEntity.lines
                                currentEntity.getLines().add(d[c]);

                                // increment entities line counter
                                c++;

                            }

                            if (d[c + 1].equals("vertex") && entitiesToKeep[i].equals("polyline")) {
                                // polyline entities have vertex blocks which are terminated by the same
                                // string as the entity blocks so we need to handle that

                                for (int r = c; r < d.length; r++) {
                                    if (d[c].equals("seqend")) {
                                        // exit this for loop, this is another place a while won't work in js
                                        break;
                                    }
                                    // keep adding points
                                    currentEntity.getLines().add(d[c]);
                                    c++;
                                }
                            }

                            // need to decrement the line counter by one so not to skip every other entity
                            c--;

                            // send to entity handler type
                            if (entitiesToKeep[i].equals("line")) {
                                this.handleLine(currentEntity);
                            } else if (entitiesToKeep[i].equals("polyline") || entitiesToKeep[i].equals("lwpolyline")) {
                                this.handlePolyline(currentEntity);
                            } else if (entitiesToKeep[i].equals("circle") || entitiesToKeep[i].equals("arc")) {
                                this.handleArc(currentEntity);
                            }
                            totalEntities++;

                            isValid = true;

                        }
                    }

                    if (!isValid && ((c + 1) != d.length) && !d[c + 1].equals("  0")) {
                        this.invalidEntities.add(d[c + 1]);
                    }

                }

            }
        }

    }

    ;

private void handleArc(Entity d) {

        //console.log('handleArc',d);
        // x,y,r,startAngle,endAngle
        double[] thisArc = {0, 0, 0, 0, 0};

        // now loop through each of the lines for the arc
        // 10,20,30 start point
        // 40 radius
        // 50,51 start angle, end angle
        for (int c = 0; c < d.getLines().size(); c++) {
            if (d.getLines().get(c).equals(" 10")) {
                c++;
                thisArc[0] = Double.valueOf(d.getLines().get(c));
            } else if (d.getLines().get(c).equals(" 20")) {
                c++;
                thisArc[1] = Double.valueOf(d.getLines().get(c));
            } else if (d.getLines().get(c).equals(" 40")) {
                c++;
                thisArc[2] = Double.valueOf(d.getLines().get(c));
            } else if (d.getLines().get(c).equals(" 50")) {
                c++;
                thisArc[3] = Double.valueOf(d.getLines().get(c));
            } else if (d.getLines().get(c).equals(" 51")) {
                c++;
                thisArc[4] = Double.valueOf(d.getLines().get(c));
            }
        }

        if (d.getType().equals("circle")) {
            thisArc[3] = 0;
            thisArc[4] = 360;
        } else {
            this.alerts.add("Arc detected, arcs are difficult to close paths with.  It may be easier to edit the DXF and generate polylines.");
        }

        // probably need to include 210,220,230 extrusion direction here
        // but it's ok for now
        if (thisArc[4] == 0) {
            thisArc[4] = 360;
        }

        double arcTotalDeg = thisArc[4] - thisArc[3];

        // now we need to create the line segments in the arc
        double numSegments = 40;
        double degreeStep = arcTotalDeg / numSegments;

        // holder for the path
        List<Point2D> newPoints = new ArrayList<>();

        // now loop through each degreeStep
        for (int a = 0; a < numSegments + 1; a++) {
            newPoints.add(this.newPointFromDistanceAndAngle(new Point2D(thisArc[0], thisArc[1]), this.addDegrees(thisArc[3], (degreeStep * a)), thisArc[2]));
            // add the point		
        }

        // check if arc exceeds min and max DXF point
        // if so update
        for (int i = 0; i < newPoints.size() - 1; i++) {
            Point2D p1 = newPoints.get(i);

            // for x
            if (p1.getX() > this.maxPoint.getX()) {
                this.maxPoint = new Point2D(p1.getX(), maxPoint.getY());
            } else if (p1.getX() < this.minPoint.getX()) {
                this.minPoint = new Point2D(p1.getX(), minPoint.getY());
            }

            // for y
            if (p1.getY() > this.maxPoint.getY()) {
                this.maxPoint = new Point2D(maxPoint.getX(), p1.getY());
            } else if (p1.getY() < this.minPoint.getY()) {
                this.minPoint = new Point2D(minPoint.getX(), p1.getY());
            }

        }

        if (d.getType().equals("circle")) {
            this.polylines.add(new PolyLine("circle", newPoints));
        } else {
            // arcs need to be created as lines so the polylines can be followed
            for (int c = 0; c < newPoints.size(); c++) {
                if (c + 1 <= newPoints.size() - 1) {
                    this.lines.add(new Line(new Point3D(newPoints.get(c).getX(), newPoints.get(c).getY(), 0), new Point3D(newPoints.get(c + 1).getX(), newPoints.get(c + 1).getY(), 0)));
                }
            }
        }

    }

    ;





private void handleLine(Entity d) {

        //console.log('handleLine',d);
        double[] thisLine = {0, 0, 0, 0, 0, 0};

        // now loop through each of the lines for the line
        // 10,20,30 = x,y,z start
        // 11,21,31 = x,y,z end
        for (int c = 0; c < d.getLines().size(); c++) {
            if (d.getLines().get(c).equals(" 10")) {
                c++;
                thisLine[0] = Double.valueOf(d.getLines().get(c));
            } else if (d.getLines().get(c).equals(" 20")) {
                c++;
                thisLine[1] = Double.valueOf(d.getLines().get(c));
            } else if (d.getLines().get(c).equals(" 30")) {
                c++;
                thisLine[2] = Double.valueOf(d.getLines().get(c));
            } else if (d.getLines().get(c).equals(" 11")) {
                c++;
                thisLine[3] = Double.valueOf(d.getLines().get(c));
            } else if (d.getLines().get(c).equals(" 21")) {
                c++;
                thisLine[4] = Double.valueOf(d.getLines().get(c));
            } else if (d.getLines().get(c).equals(" 31")) {
                c++;
                thisLine[5] = Double.valueOf(d.getLines().get(c));
            }
        }

        // check if line exceeds min and max DXF point
        // if so update
        // for x start
        if (thisLine[0] > this.maxPoint.getX()) {
            this.maxPoint = new Point2D(thisLine[0], maxPoint.getY());
        } else if (thisLine[0] < this.minPoint.getX()) {
            this.minPoint = new Point2D(thisLine[0], minPoint.getY());
        }

        // for y start
        if (thisLine[1] > this.maxPoint.getY()) {
            this.maxPoint = new Point2D(maxPoint.getX(), thisLine[1]);
        } else if (thisLine[1] < this.minPoint.getY()) {
            this.minPoint = new Point2D(minPoint.getX(), thisLine[1]);
        }

        // for x end
        if (thisLine[3] > this.maxPoint.getX()) {
            this.maxPoint = new Point2D(thisLine[3], maxPoint.getY());
        } else if (thisLine[3] < this.minPoint.getX()) {
            this.minPoint = new Point2D(thisLine[3], minPoint.getY());
        }

        // for y end
        if (thisLine[4] > this.maxPoint.getY()) {
            this.maxPoint = new Point2D(maxPoint.getX(), thisLine[4]);
        } else if (thisLine[4] < this.minPoint.getY()) {
            this.minPoint = new Point2D(minPoint.getX(), thisLine[4]);
        }
        this.lines.add(new Line(new Point3D(thisLine[0], thisLine[1], thisLine[2]), new Point3D(thisLine[3], thisLine[4], thisLine[5])));

    }

    ;

private void handlePolyline(Entity d) throws Exception {
        boolean isPoints = false;
        //console.log('handlePolyline',d);

        PolyLine singleEntity = new PolyLine();

        // keep track of what coord we are in within this entity
        double currentCoord = -1;

        // keep track of the first layer name
        boolean gotFirstLayerName = false;

        if (d.getType().equals("lwpolyline")) {
            // if the type is lwpolyline then the first coordinate is the first coordinate
            double[] point = {0, 0, 0};
            Point2DAndCurve tempPoint = null;
            for (int c = 0; c < d.getLines().size(); c++) {

                if (d.getLines().get(c).matches(" 8") && gotFirstLayerName == false) {
                    // the first ' 8' means the next line will be the layer name
                    // sometimes there is a '  8' and sometimes a ' 8' so we need to match on ' 8' which will get '  8'
                    // for some reason there are sometimes with some editors multiple of them
                    // the first one is the one that matters
                    c++;
                    singleEntity.setLayer(d.getLines().get(c));
                    gotFirstLayerName = true;
                } else if (d.getLines().get(c).equals(" 10")) {
				// this means the next line will be the X coordinate of a point

                    // inc the currentCoord
                    currentCoord++;
                    c++;
                    point[0] = Double.valueOf(d.getLines().get(c));
                    
                } else if (d.getLines().get(c).equals(" 20")) {
                    // this means the next line will be the Y coordinate of a point
                    c++;
                    point[1] = Double.valueOf(d.getLines().get(c));
                    tempPoint = new Point2DAndCurve(point[0], point[1], 0);
                    singleEntity.getPoints().add(tempPoint);
                } else if (d.getLines().get(c).equals(" 42")) {
                    // this means the next line will be the curve value
                    c++;
                    point[2] = Double.valueOf(d.getLines().get(c));
                    if(tempPoint != null){
                    tempPoint.setCurve(point[2]);
                    } else {
                        throw new Exception("error curve without x and y at line: " + c);
                    }
                }

            }
        } else if (d.getType().equals("polyline")) {
		// if the type is polyline then the first coordinate is the offset
            // followed by VERTEX blocks which contain the points

            // now loop through each of the lines for the polyline
            for (int c = 0; c < d.getLines().size(); c++) {

                if (d.getLines().get(c).matches(" 8") && gotFirstLayerName == false) {
                    // the first ' 8' means the next line will be the layer name
                    // sometimes there is a '  8' and sometimes a ' 8' so we need to match on ' 8' which will get '  8'
                    // for some reason there are sometimes with some editors multiple of them
                    // the first one is the one that matters
                    c++;
                    singleEntity.setLayer(d.getLines().get(c));
                    gotFirstLayerName = true;
                } else if (d.getLines().get(c).equals("vertex")) {
                    // we need to now loop through this vertex to get the coordinates
                    // go to the next line
                    c++;
                    double[] point = {0, 0, 0};
                    for (int r = c; r < d.getLines().size(); r++) {

                        if (d.getLines().get(c).equals("10")) {
                            // inc the currentCoord
                            currentCoord++;
                            c++;
                            point[0] = Double.valueOf(d.getLines().get(c));
                        } else if (d.getLines().get(c).equals("20")) {
                            c++;
                            point[1] = Double.valueOf(d.getLines().get(c));
                        } else if (d.getLines().get(c).equals("42")) {
                            c++;
                            point[2] = Double.valueOf(d.getLines().get(c));
                        } else if (d.getLines().get(c).equals("  0") || d.getLines().get(c).equals("vertex")) {
                            break;
                        }

                        c++;
                    }
                }
            }
        }

        //console.log('polyline singleEntity',singleEntity);
        if (singleEntity.getPoints().isEmpty()) {
            // this has no points, go on to the next
            //console.log('polyline has no points');
            return;
        }

	// now for this polyline we need to process it
        //console.log('processing polyline');
        // loop through each point in polygon to update the min and max
        // values for the whole dxf
        for (int i = 0; i < singleEntity.getPoints().size() - 1; i++) {
            Point2D p1 = singleEntity.getPoints().get(i);

            // for x
            if (p1.getX() > this.maxPoint.getX()) {
                this.maxPoint = new Point2D(p1.getX(), maxPoint.getY());
            } else if (p1.getX() < this.minPoint.getX()) {
                this.minPoint = new Point2D(p1.getX(), minPoint.getY());
            }

            // for y
            if (p1.getY() > this.maxPoint.getY()) {
                this.maxPoint = new Point2D(maxPoint.getX(), p1.getY());
            } else if (p1.getY() < this.minPoint.getY()) {
                this.minPoint = new Point2D(minPoint.getX(), p1.getY());
            }

        }

        // loop through each point in polygon again to calculate the bulges
        List<Point2D> newPoints = new ArrayList<>();

        for (int i = 0; i < singleEntity.getPoints().size() - 1; i++) {

            Point2DAndCurve p1 = singleEntity.getPoints().get(i);
            Point2DAndCurve p2 = singleEntity.getPoints().get(i + 1);

            // temp for displaying points later
            List<Point2D> thisLoopPoints = new ArrayList<>();

            /*
             console.log('\nPOINT LOOP #'+i+' for '+singleEntity.layer);
             console.log('p1',p1);
             console.log('p2',p2);
             console.log('cv',cv);
             */
            if (p1.getX() == p2.getX() && p1.getY() == p2.getY()) {
                // the points are the exact same

            } else {
                // the points are different

                if (p1.getCurve() != null) {
                    // there is a bulge, get the center point
                    Point2D cv = this.calcBulgeCenter(new Point3DAndBulge(p1.getX(), p2.getY(), 0, p1.getCurve()), p2);

                    if (false) {
                        // the bulge point are on the same line, stupid editor
                        // just add the points
                        newPoints.add(p1);
                        newPoints.add(p2);
                    } else {
					// this is a proper curve point, calculate it

                        // radius between p1 and cv
                        double r = this.distanceFormula(p1, cv);
                        double startPointQuad = 0;
                        double endPointQuad = 0;
                        double startAng = 0;
                        double endAng = 0;

                        //
                        //   2   |   1
                        //       |
                        // ------|-------
                        //       |
                        //   3   |   4
                        //
                        // first find start point quadrant relative to cv
                        // and end point quadrant relative to cv
                        // start point
                        if (p1.getX() > cv.getX() && p1.getY() > cv.getY()) {
                            startPointQuad = 1;
                        } else if (p1.getX() < cv.getX() && p1.getY() > cv.getY()) {
                            startPointQuad = 2;
                        } else if (p1.getX() < cv.getX() && p1.getY() < cv.getY()) {
                            startPointQuad = 3;
                        } else if (p1.getX() > cv.getX() && p1.getY() < cv.getY()) {
                            startPointQuad = 4;
                        }

                        // end point
                        if (p2.getX() > cv.getX() && p2.getY() > cv.getY()) {
                            endPointQuad = 1;
                        } else if (p2.getX() < cv.getX() && p2.getY() > cv.getY()) {
                            endPointQuad = 2;
                        } else if (p2.getX() < cv.getX() && p2.getY() < cv.getY()) {
                            endPointQuad = 3;
                        } else if (p2.getX() > cv.getX() && p2.getY() < cv.getY()) {
                            endPointQuad = 4;
                        }

                        // start angle from cv to p1
                        double startSlope = (cv.getY() - p1.getY()) / (cv.getX() - p1.getX());
                        startAng = 180 * Math.atan(startSlope) / Math.PI;
                        if (p1.getCurve() >= 0) {
                            // positive curve
                            if (startPointQuad == 2) {
                                startAng = 180 + startAng;
                            } else if (startPointQuad == 3) {
                                startAng = 180 + startAng;
                            } else if (startPointQuad == 4) {
                                startAng = 360 + startAng;
                            }
                        } else {
                            // negative curve
                            if (startPointQuad == 2) {
                                startAng = 180 + startAng;
                            } else if (startPointQuad == 3) {
                                startAng = 180 + startAng;
                            } else if (startPointQuad == 4) {
                                startAng = 360 + startAng;
                            }
                        }

                        // end angle from cv to p2
                        double endSlope = (cv.getY() - p2.getY()) / (cv.getX() - p2.getX());
                        endAng = 180 * Math.atan(endSlope) / Math.PI;
                        if (p1.getCurve() < 0) {
                            // negative curve
                            if (endPointQuad == 2) {
                                endAng = 180 + endAng;
                            } else if (endPointQuad == 3) {
                                endAng = 180 + endAng;
                            } else if (endPointQuad == 4) {
                                endAng = 360 + endAng;
                            }
                        } else {
                            // positive curve
                            if (endPointQuad == 2) {
                                endAng = 180 + endAng;
                            } else if (endPointQuad == 3) {
                                endAng = 180 + endAng;
                            } else if (endPointQuad == 4) {
                                endAng = 360 + endAng;
                            }
                        }

                        double arcTotalDeg = 0;
                        if (p1.getCurve() < 0) {
                            // this is a negative curve so it will be an arc that goes from p1 to p2 except
                            // it will be bulging toward the cv point and not away from it like normal
                            arcTotalDeg = 360 - this.addDegrees(endAng, -startAng);
                        } else {
                            arcTotalDeg = this.addDegrees(endAng, -startAng);
                        }

                        // now we need to create the line segments in the arc
                        int numSegments = 40;
                        double degreeStep = arcTotalDeg / numSegments;

                        // now loop through each degreeStep
                        for (int a = 1; a < numSegments + 1; a++) {
                            Point2D pt = null;
                            // for a positive curve the start point is always a lower number of degrees
                            if (p1.getCurve() < 0) {
                                // for a negative curve we need to subtract degreeStep
                                pt = this.newPointFromDistanceAndAngle(cv, this.addDegrees(startAng, -(degreeStep * a)), r);
                            } else {
                                // for a positive curve we add degreeStep
                                pt = this.newPointFromDistanceAndAngle(cv, this.addDegrees(startAng, (degreeStep * a)), r);
                            }
                            // add the point
                            newPoints.add(pt);
                            thisLoopPoints.add(pt);
                        }

                    }

                } else {
                    // line segment without curve, add it
                    newPoints.add(p1);
                    newPoints.add(p2);
                    i++;
                }

            }

            /*
             console.log('startAng',startAng);
             console.log('endAng',endAng);
             console.log('startPointQuad',startPointQuad);
             console.log('endPointQuad',endPointQuad);
             console.log('thisLoopPoints ' + thisLoopPoints.length);
             */
            /*
             for (var nn=0; nn<thisLoopPoints.length; nn++) {
             console.log(nn,thisLoopPoints[nn]);
             }
             */
            //console.log('POINT LOOP #'+i+' '+thisLoopPoints.length,p1,p2);
        }

        //console.log('POINTS AFTER PROCESSING',newPoints.length);
        // set newPoints as this polygons points
        this.polylines.add(new PolyLine(singleEntity.getLayer(), newPoints));

    }

    ;

public void parseDxf(File d) {

        // parse a dxf file (ASCII) which is passed as d
        List<List<String>> sections = new ArrayList<>();
        List<String> currentSection = new ArrayList<>();

        System.out.println("parsing dxf sections");

        try {
            // first loop through and find all the sections
            BufferedReader br = new BufferedReader(new FileReader(d));
            String line = br.readLine();
            while (line != null) {

                if (line.toLowerCase().matches("section")) {
                    // this starts a section, add a new section object
                    currentSection = new ArrayList<>();
                } else if (line.toLowerCase().matches("endsec")) {
                    // this ends a section, move currentSection into sections
                    sections.add(currentSection);
                } else {
			// this is something to be inserted into currentSection
                    // also remove any newline characters from the end of the string
                    currentSection.add(line.toLowerCase().replace("(\r\n |\n |\r)/gm", ""));
                }
                line = br.readLine();
            }

            // now go through each section and send each to the correct handler
            for (int c = 0; c < sections.size(); c++) {

                //console.log('section #'+c,sections[c]);
                // right now we just get the header and entities, and even the header isn't used that often
                if (sections.get(c).get(1).matches("header")) {
                    this.handleHeader(sections.get(c).toArray(new String[sections.get(c).size()]));
                } else if (sections.get(c).get(1).matches("entities")) {
                    this.handleEntities(sections.get(c).toArray(new String[sections.get(c).size()]));
                }

            }

            // set dxf width and height now that all entities are processed
            this.width = this.maxPoint.getX() - this.minPoint.getX();
            this.height = this.maxPoint.getY() - this.minPoint.getY();
            this.avgSize = (this.width + this.height) / 2;

	// now we can loop through all the lines and make polylines for all
            // lines which share a common end to next start point, procedurally
            // this only converts lines to polylines when points match exactly
            // there is no rounding and if 2 points are off by even .0000000001
            // they will not convert as who knows what scale things are at
            // edit your DXF's correctly
            // this holds the lines we will remove which were turned into polylines
            List<RemoveLine> removeLines = new ArrayList<>();

            for (int c = 0; c < this.lines.size(); c++) {

                List<Point2D> tempPolyline = new ArrayList<>();
                int startLine = 0;

                try {

                    // if this lines end point and the next lines start point are the same
                    if (this.lines.get(c).getEnd().getX() == this.lines.get(c + 1).getStart().getX() && this.lines.get(c).getEnd().getY() == this.lines.get(c + 1).getStart().getY()) {

                        startLine = c;
                        // this line and the next line are connected
                        // add this line start point
                        tempPolyline.add(new Point2D(this.lines.get(c).getStart().getX(), this.lines.get(c).getStart().getY()));
                        // add next line start point
                        tempPolyline.add(new Point2D(this.lines.get(c + 1).getStart().getX(), this.lines.get(c + 1).getStart().getY()));
                        // add next line end point
                        tempPolyline.add(new Point2D(this.lines.get(c + 1).getEnd().getX(), this.lines.get(c + 1).getEnd().getY()));

                        c++;
                        // while this lines end point and the next lines start point remain the same
                        for (int r = c; r < this.lines.size() - 1; r++) {
                            if (this.lines.get(c).getEnd().getX() == this.lines.get(c + 1).getStart().getX() && this.lines.get(c).getEnd().getY() == this.lines.get(c + 1).getStart().getY()) {
                                // add the next lines start point
                                //tempPolyline.push([this.lines[c+1][0],this.lines[c+1][1]]);
                                // add the next lines end point
                                tempPolyline.add(new Point2D(this.lines.get(c + 1).getEnd().getX(), this.lines.get(c + 1).getEnd().getY()));
                                c++;
                            }
                        }

                        // if the previous lines end point equals the first lines start point
                        if (tempPolyline.get(0).getX() == tempPolyline.get(tempPolyline.size() - 1).getX() && tempPolyline.get(0).getY() == tempPolyline.get(tempPolyline.size() - 1).getY()) {
                            // we know it's a valid polygon

                            // remove all those lines from this.lines
                            removeLines.add(new RemoveLine(startLine, c - startLine));

                            // add tempPolyline to this.polylines
                            this.polylines.add(new PolyLine("", tempPolyline));

                        }
                    } else {

			// here we can check if the points were close and inform the user
                        // that these points were close but not exact so we couldn't form a polyline
                        //if (this.terneryDiff(this.lines[c][3],this.lines[c+1][0]) < this.avgSize/20 && this.terneryDiff(this.lines[c][4],this.lines[c+1][1]) < this.avgSize/20) {
                        double pointDiff = this.distanceFormula(new Point2D(this.lines.get(c).getEnd().getX(), this.lines.get(c).getEnd().getY()), new Point2D(this.lines.get(c + 1).getStart().getX(), this.lines.get(c + 1).getStart().getY()));
                        if (pointDiff < this.avgSize / 20) {
                            this.alerts.add("line" + c + " and line " + (c + 1)
                                    + " could be part of a polyline they are " + pointDiff
                                    + " units away from one another.  Make lines start and end at the exact same point and they will form polylines.");
                        }

                    }

                } catch (Exception err) {
                    System.out.println("Error: " + err);
                    System.out.println("Size: " + this.lines.size() + " Counter: " + c);
                    System.out.println("Content: " + this.lines.get(c));
                }

            }

            for (int c = 0; c < removeLines.size(); c++) {
                for (int r = 0; r < removeLines.get(c).size + 1; r++) {
                    this.lines.set(removeLines.get(c).getStart() + r, null);
                }
            }

            //Cleanup the lines
            Iterator it = lines.iterator();
            while (it.hasNext()) {
                if (it.next() == null) {
                    it.remove();
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Dxf.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Dxf.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Dxf.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

//public String toMillcrum(){
//
//List<Point2D> list = {};
//String s = "Tool tool = new Tool(\"mm\",6.35,4,1,2000,100,600,5,true);\n\n";
//s += "// setup a new Millcrum object with that tool\nMillcrum mc = new Millcrum(tool);\n\n";
//s += "// set the surface dimensions for the viewer\nmc.surface("+(this.width*1.1)+","+(this.height*1.1)+");\n\n\n";
//// convert polylines to millcrum
//for (int c=0; c<this.polylines.size(); c++) {
//if (this.polylines.get(c).getLayer().equals("")) {
//// name it polyline+c
//this.polylines.get(c).setLayer("polyline"+c);
//}
//s += "//LAYER "+this.polylines.get(c).getLayer()+"\n";
//s += "List<Point2D> points = ";
//for (int p=0; p<this.polylines.get(c).getPoints().size(); p++) {
//    s += "{'+dxf.polylines[c].points[p][0]+','+dxf.polylines[c].points[p][1]+'],';
//}
//s += ']};\n
//        
//s += "Polyline polyline"+c+" = new Polyline(\"polygon\"," + this.polylines.get(c).getLayer() + ",points);";
//
//        s += "mc.cut(\"centerOnPath\", polyline"+c+"", 4, new Point2D(0,0);\n\n";
//}
//// convert lines to millcrum
//for (var c=0; c<dxf.lines.length; c++) {
//s += 'var line'+c+' = {type:\'polygon\',name:\'line'+c+'\',points:[';
//s += '['+dxf.lines[c][0]+','+dxf.lines[c][1]+'],';
//s += '['+dxf.lines[c][3]+','+dxf.lines[c][4]+'],';
//s += ']};\nmc.cut(\'centerOnPath\', line'+c+', 4, [0,0]);\n\n';
//}
//s += '\nmc.get();\n';
//return String
//}

    class Entity {

        private String type = "";
        private List<String> lines = new ArrayList<>();

        public Entity() {

        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<String> getLines() {
            return lines;
        }

        public void setLines(List<String> lines) {
            this.lines = lines;
        }

    }

    class Line {

        private Point3D start;
        private Point3D end;

        public Line(Point3D start, Point3D end) {
            this.start = start;
            this.end = end;
        }

        public Point3D getStart() {
            return start;
        }

        public void setStart(Point3D start) {
            this.start = start;
        }

        public Point3D getEnd() {
            return end;
        }

        public void setEnd(Point3D end) {
            this.end = end;
        }

    }

    class RemoveLine {

        private int start;

        private int size;

        public RemoveLine(int start, int size) {
            this.start = start;
            this.size = size;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

    }

    class PolyLine {

        private String layer;
        private List<Point2DAndCurve> points = new ArrayList<>();

        public PolyLine() {

        }

        public PolyLine(String layer, List<Point2DAndCurve> points, boolean isCurved) {
            this.layer = layer;
            this.points = points;
        }

        PolyLine(String layer, List<Point2D> newPoints) {
            this.layer = layer;
            for (Point2D pt : newPoints) {
                this.points.add(new Point2DAndCurve(pt.getX(), pt.getY()));
            }
        }

        public String getLayer() {
            return layer;
        }

        public void setLayer(String layer) {
            this.layer = layer;
        }

        public List<Point2DAndCurve> getPoints() {
            return points;
        }

        public void setPoints(List<Point2DAndCurve> points) {
            this.points = points;
        }
    }

    class Point2DAndCurve extends Point2D {

        private Double curve;

        Point2DAndCurve(double x, double y) {
            super(x, y);
            this.curve = null;
        }

        Point2DAndCurve(double x, double y, double curve) {
            super(x, y);
            this.curve = curve;
        }

        public Double getCurve() {
            return curve;
        }

        public void setCurve(Double curve) {
            this.curve = curve;
        }

    }
}