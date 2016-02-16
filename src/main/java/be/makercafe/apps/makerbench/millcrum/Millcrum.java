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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Point2D;

/**
 * A port of the javascript program millcrum
 *
 * @author m999ldp
 */
public class Millcrum {

    private String gcode = "";
    private String toSaveGcode = "";
    private boolean debug = false;
    private int global5x = -1;
    private int global5y = -1;
    private Tool tool = new Tool();
    private MillCanvas millCanvas = new MillCanvas();

    public String getGcode() {
        return gcode;
    }

    public void setGcode(String gcode) {
        this.gcode = gcode;
    }

    public String getToSaveGcode() {
        return toSaveGcode;
    }

    public void setToSaveGcode(String toSaveGcode) {
        this.toSaveGcode = toSaveGcode;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public int getGlobal5x() {
        return global5x;
    }

    public void setGlobal5x(int global5x) {
        this.global5x = global5x;
    }

    public int getGlobal5y() {
        return global5y;
    }

    public void setGlobal5y(int global5y) {
        this.global5y = global5y;
    }

    public Tool getTool() {
        return tool;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public MillCanvas getMillCanvas() {
        return millCanvas;
    }

    public void setMillCanvas(MillCanvas millCanvas) {
        this.millCanvas = millCanvas;
    }

    /**
     * This function expects a 360 degree number base and mod must be between
     * 0-360
     *
     * @param base A 360 degree number
     * @param mod A number between 0 and 360
     * @return The sum of the degrees in absolute value
     */
    public double addDegrees(final double base, final double mod) {
        double sum = base + mod;
        if (sum > 360) {
            sum = 360 - sum;
        } else if (sum < 0) {
            sum = 360 + sum;
        }
        return Math.abs(sum);
    }

    public void surface(int x, int y) {
        if (this.global5x < 0) {
            this.global5x = x;
            this.global5y = y;
        }
        //init();
    }

//    public boolean pointInPolygon(int[] point, int[][] points) {
//        // check if a point is inside a polygon
//        // The solution is to compare each side of the polygon to the Y (vertical) coordinate of the test
//        // point, and compile a list of nodes, where each node is a point where one side crosses the Y
//        // threshold of the test point. In this example, eight sides of the polygon cross the Y threshold,
//        // while the other six sides do not. Then, if there are an odd number of nodes on each side of
//        // the test point, then it is inside the polygon; if there are an even number of nodes on each
//        // side of the test point, then it is outside the polygon.
//        int j = points.length - 1;
//        boolean oddNodes = false;
//        for (int c = 0; c < points.length; c++) {
//            // if ((thisY < pointY AND thisjY >= pointY) OR (thisjY < pointY AND thisY >= pointY))
//            if ((points[c][1] < point[1] && points[j][1] >= point[1]) || (points[j][1] < point[1] && points[c][1] >= point[1])) {
//                // if (thisX+(pointY-thisY)/(thisjY-thisY)*(thisjX-thisX) < pointX)
//                if (points[c][0] + (point[1] - points[c][1]) / (points[j][1] - points[c][1]) * (points[j][0] - points[c][0]) < point[0]) {
//                    oddNodes = !oddNodes;
//                }
//            }
//            j = c;
//        }
//        return oddNodes;
//    }
    /**
     * check if a point is inside a polygon The solution is to compare each side
     * of the polygon to the Y (vertical) coordinate of the test point, and
     * compile a list of nodes, where each node is a point where one side
     * crosses the Y threshold of the test point. In this example, eight sides
     * of the polygon cross the Y threshold, while the other six sides do not.
     * Then, if there are an odd number of nodes on each side of the test point,
     * then it is inside the polygon; if there are an even number of nodes on
     * each side of the test point, then it is outside the polygon.
     *
     * @param point
     * @param points
     * @return
     */
    public boolean pointInPolygon(Point2D point, List<Point2D> points) {

        boolean oddNodes = false;

        Point2D lastPoint = points.get(points.size() - 1);
        for (Point2D currentPoint : points) {
            if ((currentPoint.getY() < point.getY() && lastPoint.getY() >= point.getY()) || (lastPoint.getY() < point.getY() && currentPoint.getY() >= point.getY())) {
                if (currentPoint.getX() + (point.getY() - currentPoint.getY()) / (lastPoint.getY() - currentPoint.getY()) * (lastPoint.getX() - currentPoint.getX()) < point.getX()) {
                    oddNodes = !oddNodes;
                }
            }
            lastPoint = currentPoint;
        }
        return oddNodes;
    }

    private void drawPath(List<Point2D> basePath, Tool tool, String cutType, double depth, boolean b, String name) {
        millCanvas.drawPath(basePath, tool, cutType, depth, b, name);
    }

    class LinesIntersectionResult {

        public boolean error = true;
        public Point2D point = new Point2D(0, 0);
        public boolean parallel = false;

    }

    /**
     * check if 2 lines intersect and return the point at which they do
     *
     * @param l1start
     * @param l1end
     * @param l2start
     * @param l2end
     * @return LinesIntersectionResult
     */
    public LinesIntersectionResult linesIntersection(Point2D l1start, Point2D l1end, Point2D l2start, Point2D l2end) {
        // 
        double denom, a, b, num1, num2;
        LinesIntersectionResult result = new LinesIntersectionResult();
        denom = ((l2end.getY() - l2start.getY()) * (l1end.getX() - l1start.getX())) - ((l2end.getX() - l2start.getX()) * (l1end.getY() - l1start.getY()));
        if (denom == 0) {
            // they are parallel
            result.parallel = true;
            return result;
        }
        a = l1start.getY() - l2start.getY();
        b = l1start.getX() - l2start.getX();
        num1 = ((l2end.getX() - l2start.getX()) * a) - ((l2end.getY() - l2start.getY()) * b);
        num2 = ((l1end.getX() - l1start.getX()) * a) - ((l1end.getY() - l1start.getY()) * b);
        a = num1 / denom;
        b = num2 / denom;
        // intersection point
        result.point = new Point2D(l1start.getX() + (a * (l1end.getX() - l1start.getX())), l1start.getY() + (a * (l1end.getY() - l1start.getY())));

        if (a > 0 && a < 1 && b > 0 && b < 1) {
            // we can be positive that they intersect
            result.error = false;
        }
        return result;
    }

    /**
     * Get the distance between p1 and p2
     *
     * @param p1
     * @param p2
     * @return
     */
    public double distanceFormula(Point2D p1, Point2D p2) {
        double a = (p2.getX() - p1.getX()) * (p2.getX() - p1.getX());
        double b = (p2.getY() - p1.getY()) * (p2.getY() - p1.getY());
        return Math.sqrt(a + b);
    }

    /**
     * use cos and sin to get a new point with an angle and distance from an
     * existing point pt = [x,y] ang = in degrees distance = N
     *
     * @param pt Point2D
     * @param ang Angle in degrees
     * @param distance N
     * @return New point
     */
    public Point2D newPointFromDistanceAndAngle(Point2D pt, double ang, double distance) {
        Point2D newPoint = new Point2D(pt.getX() + (distance * Math.cos(ang * Math.PI / 180)), pt.getY() + (distance * Math.sin(ang * Math.PI / 180)));
        return newPoint;
    }

    ;
    
    /**
     * For an arc we have to start from the center
     * then using the fragment count we draw that number of triangles
     * extruding from the center point to r and use the outside edge
     * of those triangles to generate the lines for the arc
     * a higher number of fragments will render a smoother arc
     * @param startDeg
     * @param endDeg
     * @param r
     * @param toolDiameter 
     */
    public List<Point2D> generateArc(double startDeg, double endDeg, double r, double toolDiameter) {
        if (startDeg == 360) {
            startDeg = 0;
        }

        double f = 40;
        // degreeStep is 360/f (total fragments)
        // this is the angle we will move each step to create the fragments
        double degreeStep = 360 / f;
        // create the path array
        List<Point2D> path = new ArrayList<>();
        // to get the first point in the arc path, we need to get a new point from distance and angle
        // which has an angle of startDeg and a distance of r
        path.add(newPointFromDistanceAndAngle(new Point2D(0, 0), startDeg, r));

        double fDist = this.distanceFormula(path.get(0), this.newPointFromDistanceAndAngle(new Point2D(0, 0), this.addDegrees(startDeg, degreeStep), r));
        // normalize mm and inches to mm here just for this
        double desired = toolDiameter / 2;
        if (!this.tool.getUnits().equalsIgnoreCase("mm")) {
            // divide it by 25.4 to get inch value
            desired = desired / 25.4;
        }
        // we can automatically calculate the number of fragments by recursively looping
        // and increasing the number until a sample line segment is less than this.tool.diameter/2
        while (fDist > desired) {
            // increase f
            f = f * 1.5;
            // recalculate the degreeStep
            degreeStep = 360 / f;
            // calculate a fragment distance from the first point
            fDist = this.distanceFormula(path.get(0), this.newPointFromDistanceAndAngle(new Point2D(0, 0), this.addDegrees(startDeg, degreeStep), r));
        }
        //console.log('total number of steps '+f+' at '+degreeStep+' degrees which is 360/'+f+' and a distance of '+fDist);
        // now get the number of fragments to actually create, based on the total degrees
        // which is the absolute value of startDeg-endDeg / 360 then multiplied by the total number of fragments
        long totalFrags = Math.round((Math.abs(startDeg - endDeg) / 360) * f);
        for (long c = 1; c < totalFrags; c++) {
            path.add(this.newPointFromDistanceAndAngle(new Point2D(0, 0), this.addDegrees(startDeg, c * degreeStep), r));
        }
        return path;
    }

    ;
    
    /**
     * 
     * Generates an offset path from basePath
     * type of either inside or outside
     * offsetDistance determines how far away it is
     * @param type
     * @param basePath
     * @param offsetDistance
     * @return 
     */
    public List<Point2D> generateOffsetPath(String type, List<Point2D> basePath, double offsetDistance) {

        //console.log('##GENERATING OFFSET PATH##');
        //console.log('');
        // first create an array of midpoints and angles for the offset path
        List<PointAndAngle> newMidpoints = new ArrayList<>();

        // we also need to find the line with the longest distance
        double longestLine = 0;
        for (long c = 1; c < basePath.size(); c++) {
            // we are looping through each point starting with 1 instead of 0
            // which means using currentPoint and previousPoint we are looping through
            // each line segment starting with the first
            Point2D currentPoint = basePath.get((int) c);
            Point2D previousPoint = basePath.get((int) (c - 1));
            //console.log('##LINE '+c+' from '+previousPoint[0]+','+previousPoint[1]+' to '+currentPoint[0]+','+currentPoint[1]+'##');
            // get the deltas for X and Y to calculate the line angle with atan2
            double deltaX = currentPoint.getX() - previousPoint.getX();
            double deltaY = currentPoint.getY() - previousPoint.getY();
            // get the line angle
            double ang = Math.atan2(deltaY, deltaX);
            //console.log(' ANGLE '+ang+' or '+(ang*180/Math.PI));
            // convert it to degrees for later math with addDegree
            ang = ang * 180 / Math.PI;
            // get the length of the line
            double len = this.distanceFormula(currentPoint, previousPoint);
            //console.log(' LENGTH '+len);
            if (len > longestLine) {
                // update longestLine
                longestLine = len;
            }
            // here we have the angle of the line segment and we need to move it
            // for it to go inside the object or outside the object
            // on a path of ang-90 or the opposite of ang-90 (example ang of 90 would be either 0 or opposite 180)
            double movedLineAng = this.addDegrees(ang, -90);
            if (type.equalsIgnoreCase(INSIDE)) {
                // reverse the angle
                movedLineAng = this.addDegrees(movedLineAng, 180);
            }
            //console.log(' offsetting '+offsetDistance+' '+type);
            // now split the line at the middle length and get that point
            // then get the coords of the midpoint on the new lines calculated
            // from outsideAng, insideAng and this.tool.diameter (offset)
            // from those midpoints and with the known (perpendicular) line angles you can
            // extend the new lines out
            // get the point coordinate at midpoint of this line
            Point2D midpoint = this.newPointFromDistanceAndAngle(previousPoint, ang, len / 2);
            //console.log(' line midpoint');
            //console.log(midpoint);
            // now we need the new midpoint for pathAng
            // from midpoint with the this.tool.diameter/2 for a distance
            Point2D movedLineMidPoint = this.newPointFromDistanceAndAngle(midpoint, movedLineAng, offsetDistance);
            //console.log(' movedLineMidPoint');
            //console.log(movedLineMidPoint);
            //console.log('');
            newMidpoints.add(new PointAndAngle(movedLineMidPoint, ang));
        }
        //console.log('##newMidpoints##');
        //console.log(newMidpoints);
        // we will add (longestLine+offsetDistance)*2 to each new line half that we create
        // so that we can find the point of intersection and be sure that the line is long
        // enough to intersect
        double lenForLine = (longestLine + offsetDistance) * 2;
        // this is the path we will return
        List<Point2D> rPath = new ArrayList<>();
        // now we can loop through the newly offset path midpoints and use the angles
        // to extend lines to the point that they interesect with their adjacent line
        // and that will close the path
        for (int c = 0; c < newMidpoints.size(); c++) {
            PointAndAngle currentMidPoint = newMidpoints.get((int) c);
            PointAndAngle previousMidPoint;
            if (c == 0) {
                previousMidPoint = newMidpoints.get(newMidpoints.size() - 1);
            } else {
                previousMidPoint = newMidpoints.get(c - 1);
            }
            //console.log(' midpoint #'+c);
            // since we have the midpoint, first we have to generate the test lines
            // the current mid point is extended at it's opposite angle
            // and the previous at it's angle
            Point2D currentMidPointEndPoint = this.newPointFromDistanceAndAngle(currentMidPoint.getPoint(), this.addDegrees(currentMidPoint.getAngle(), 180), lenForLine);
            Point2D previousMidPointEndPoint = this.newPointFromDistanceAndAngle(previousMidPoint.getPoint(), previousMidPoint.getAngle(), lenForLine);
            // now using the 2 lines, we need to find the intersection point of them
            // this will give us a single point which is the START point for the current line and
            // the END point for the previous line
            LinesIntersectionResult iPoint = this.linesIntersection(previousMidPoint.getPoint(), previousMidPointEndPoint, currentMidPoint.getPoint(), currentMidPointEndPoint);
            //console.log(' intersection point for current mid point in CW');
            //console.log(iPoint);
            // if iPoint.error == true here and path is inside, then we can somehow shrink the path
            // but if path is outside there's a problem
            if (iPoint.error == true) {
                // we can exempt this line
            } else {
                // then we can enter that point in the path and it will magically be correct
                rPath.add(iPoint.point);
            }
        }
        // then we need to add a point to the end of rPath which goes back to the initial point for rPath
        if(rPath.size() > 0){
            rPath.add(new Point2D(rPath.get(0).getX(), rPath.get(0).getY()));
        }

        // now we need to remove points which are outside the bounds of the basePath
        if (type.equalsIgnoreCase(INSIDE)) {
            for (int c = 0; c < rPath.size() - 1; c++) {
                //TODO: check if this is sone correctly
                if (!this.pointInPolygon(rPath.get(c), basePath)) {
                    // remove this point, it's not within the bound
                    //console.log('removing point from polygon');
                    rPath.remove((int) c);
                }
            }
        }
        if (rPath.size() == 1) {
            // path not needed
            return null; //was false, yeah javascript syntax mix some types ;-)
        } else {
            // return the newly offset toolpath
            return rPath;
        }
    }

    /**
     *
     * @param cutType
     * @param obj
     * @param depth
     * @param startPos
     * @param config
     */
    public void cut(String cutType, MillObject obj, double depth, Point2D startPos, Config config) {
        if (depth == 0) {
// default depth of a cut is the tool defined passDepth
            depth = this.tool.getPassDepth();
        }
        if (startPos == null) {
// default start position is X0 Y0
            startPos = new Point2D(0, 0);
        }
        if (config == null) {
            config = new Config();
            if (config.isUseConventionalCut() == null) {
// default cut direction is climb
                config.setUseConventionalCut(false);
            }
        }
// finish setting config options
        if (config.getTabs() == null) {
// default is to not use tabs
            config.setTabs(false);
        } else if (config.getTabs() == true) {
// need to set defaults for using tabs if they aren't set
// by the user
            if (config.getTabHeight() <= 0) {
// default height is 2, sure hope you are using mm
                config.setTabHeight(2);
            }
            if (config.getTabSpacing() <= 0) {
// default tab spacing is 5 times tool.diameter
                config.setTabSpacing(this.tool.getDiameter() * 5);
            }
            if (config.getTabWidth() <= 0) {
// default tab width is 2 times tool.diameter
                config.setTabWidth(this.tool.getDiameter() * 2);
            }
        }
//console.log('generating cut operation:');
//console.log('##tool##');
//console.log(this.tool);
//console.log('##cutType##');
//console.log(cutType);
//console.log('##obj.type##');
//console.log(obj.type);
//console.log('##depth##');
//console.log(depth);
//console.log('##startPos##');
//console.log(startPos);
        List<Point2D> basePath = new ArrayList<>();
// these all generate a climb cut
// which is CCW from 0,0
// a conventional cut would be CW from 0,0
// you can just reverse the path to get a conv cut
        if (obj.getType().equalsIgnoreCase("rect")) {
// for a rectangle we must generate a path using xLen and yLen
// if there's a obj.cornerRadius set then we need to generate a rect with
// rounded corners
            if (obj.getCornerRadius() != 0) {
// we start with obj.cornerRadius,0 as we create the cut path
                basePath.add(new Point2D(obj.getCornerRadius(), 0));
// next the bottom right
// we need to subtract obj.cornerRadius (a distance) from X on this point
// to make room for the arc
                basePath.add(new Point2D(obj.getxLen() - obj.getCornerRadius(), 0));
// now we need to generate an arc which goes from obj.xLen-obj.cornerRadius,0
// to obj.xLen,obj.cornerRadius (this will be the rounded bottom right corner)
// first we have to generate an arc that goes from 270 to 360 degrees
                List<Point2D> arcPath = this.generateArc(270, 360, obj.getCornerRadius(), this.tool.getDiameter());
// and we need the diffs
                double xDiff = obj.getxLen() - obj.getCornerRadius() - arcPath.get(0).getX();
                double yDiff = 0 - arcPath.get(0).getY();
// now we move the arc to that point while adding it to the basePath
                for (int a = 1; a < arcPath.size(); a++) {
// add each segment of the arc path to the basePath
// we don't need the first as there is already a user defined point there so a=1
                    basePath.add(new Point2D(arcPath.get(a).getX() + xDiff, arcPath.get(a).getY() + yDiff));
                }
// that will have generated a path from the right of the bottom line in the rect
// to the bottom of the right line in the rect
// now just create another point to finish the right side of the rect, to the next corner
                basePath.add(new Point2D(obj.getxLen(), obj.getyLen() - obj.getCornerRadius()));
// now repeat for the other corners
// TR CORNER
                arcPath = this.generateArc(360, 90, obj.getCornerRadius(), this.tool.getDiameter());
                xDiff = obj.getxLen() - arcPath.get(0).getX();
                yDiff = obj.getyLen() - obj.getCornerRadius() - arcPath.get(0).getY();
                for (int a = 1; a < arcPath.size(); a++) {
                    basePath.add(new Point2D(arcPath.get(a).getX() + xDiff, arcPath.get(a).getY() + yDiff));
                }
                basePath.add(new Point2D(obj.getCornerRadius(), obj.getyLen()));
// TL CORNER
// SIDE 3
                arcPath = this.generateArc(90, 180, obj.getCornerRadius(), this.tool.getDiameter());
                xDiff = obj.getCornerRadius() - arcPath.get(0).getX();
                yDiff = obj.getyLen() - arcPath.get(0).getY();
                for (int a = 1; a < arcPath.size(); a++) {
                    basePath.add(new Point2D(arcPath.get(a).getX() + xDiff, arcPath.get(a).getY() + yDiff));
                }
                basePath.add(new Point2D(0, obj.getCornerRadius()));
// BL CORNER
                arcPath = this.generateArc(180, 270, obj.getCornerRadius(), this.tool.getDiameter());
                xDiff = 0 - arcPath.get(0).getX();
                yDiff = obj.getCornerRadius() - arcPath.get(0).getY();
                for (int a = 1; a < arcPath.size(); a++) {
                    basePath.add(new Point2D(arcPath.get(a).getX() + xDiff, arcPath.get(a).getY() + yDiff));
                }
            } else {
// just generate a simple rect
// this will be a total of 4 points
// we start with 0,0 as we create the cut path
                basePath.add(new Point2D(0, 0));
// next the bottom right
                basePath.add(new Point2D(obj.getxLen(), 0));
// then the top right
                basePath.add(new Point2D(obj.getxLen(), obj.getyLen()));
// then the top left
                basePath.add(new Point2D(0, obj.getyLen()));
            }
        } else if (obj.getType().equalsIgnoreCase("polygon")) {
// a polygon is just a set of points which represent the steps of a climb cut
// we just push each point to the basePath
            for (int c = 0; c < obj.getPoints().size(); c++) {
// except in the case where one of the "points" is an arc
                if (obj.getPoints().get(c).getType().equalsIgnoreCase("arc")) {
// this is an arc
//console.log('## ARC IN POLYGON AT '+c+'##');
// the arc must start from the previous point in the object
// we just generate the arc, then move it to start at the previous point
                    List<Point2D> arcPath = this.generateArc(obj.getPoints().get(c).getStartDegree(), obj.getPoints().get(c).getEndDegree(), obj.getPoints().get(c).getRadius(), this.tool.getDiameter());
//console.log(arcPath);
//console.log('first point in the arcPath is:');
//console.log(arcPath[0]);
// now we need to get the offset so we can move the arc to the correct place
// that is done by getting the difference between the previous point
// and arcPath[0] (first point in arc)
//TODO: this won't work if the previous object has no point2D ?
                    double xDiff = obj.getPoints().get(c - 1).getX() - arcPath.get(0).getX();
                    double yDiff = obj.getPoints().get(c - 1).getY() - arcPath.get(0).getY();
//console.log('xDiff = '+xDiff+', yDiff = '+yDiff);
                    for (int a = 1; a < arcPath.size(); a++) {
// add each segment of the arc path to the basePath
// we don't need the first as there is already a user defined point there so a=1
//console.log('adding ',[arcPath[a][0]+xDiff,arcPath[a][1]+yDiff]);
                        basePath.add(new Point2D(arcPath.get(a).getX() + xDiff, arcPath.get(a).getY() + yDiff));
                    }
                } else if (obj.getPoints().get(c).getType().equalsIgnoreCase("cubicBezier")) {
                    basePath.addAll(cubicBezier(obj.getPoints().get(c).getPointsAsPoint2D()));
                } else {
// just add the point to the path
//console.log('inserting point '+obj.points[c][0]+','+obj.points[c][1]+' into polygon');
                    basePath.add(new Point2D(obj.getPoints().get(c).getX(), obj.getPoints().get(c).getY()));
                }
            }
        } else if (obj.getType().equalsIgnoreCase("circle")) {
            if (obj.getRadius() * 2 == this.tool.getDiameter()) {
// this circle is the exact same size as the tool
// tools are circles, so the path is just a single point
                basePath.add(new Point2D(0, 0));
            } else {
// a circle is just an arc with a startDeg of 0 and a totalDeg of 360
// circles are whole objects, so they can be created with a single this.cut() operation
                basePath = this.generateArc(0, 360, obj.getRadius(), this.tool.getDiameter());
            }
        }
// here we need to offset basePath by startPos
// this allows users to create objects and move them around
// JS forces us to cp this to a new array here
        List<Point2D> cp = new ArrayList<>();
// we also collect the min, max and total size of the object here
// which we will need for pocket operations
        double minX = basePath.get(0).getX();
        double minY = basePath.get(0).getY();
        double maxX = basePath.get(0).getX();
        double maxY = basePath.get(0).getY();
        List<Double> total = new ArrayList<>();
// we also need to see if any line segments in the path are 0,90,180 or 270 degrees
// because the offset algorithm needs to know this
        boolean hasTAngle = false;
// per the inside path generation algorithm we need to ensure that the starting point of the polygon is
// on the outer bounds of the path, see bug #4 on Github
        int safeStartingPoint = 0;
        String s = "";
        for (int c = 0; c < basePath.size(); c++) {
            if (basePath.get(c).getX() < minX) {
                minX = basePath.get(c).getX();
// this will result in the point with the lowest X being a safe starting point
                safeStartingPoint = c;
            } else if (basePath.get(c).getX() > maxX) {
                maxX = basePath.get(c).getX();
            }
            if (basePath.get(c).getY() < minY) {
                minY = basePath.get(c).getY();
                if (basePath.get(safeStartingPoint).getX() == basePath.get(c).getX()) {
// at this point the safeStartingPoint will have the lowest X and a low Y
                    safeStartingPoint = c;
                }
            } else if (basePath.get(c).getY() > maxY) {
                maxY = basePath.get(c).getY();
            }
            if (c > 0) {
// get the deltas for X and Y to calculate the line angle with atan2
                double deltaX = basePath.get(c).getX() - basePath.get(c - 1).getX();
                double deltaY = basePath.get(c).getY() - basePath.get(c - 1).getY();
// get the line angle
                double ang = Math.atan2(deltaY, deltaX);
                if (ang == 90 || ang == 180 || ang == 270 || ang == 360 || ang == 0) {
                    hasTAngle = true;
                }
            }

// offset X by startPos
//cp[c].push(basePath[c][0]+startPos[0]);
// offset Y by startPos
//cp[c].push(basePath[c][1]+startPos[1]);
            cp.add(new Point2D(basePath.get(c).getX() + startPos.getX(), basePath.get(c).getY() + startPos.getY()));
        }
// now we can re-order cp to start from the safeStartingPoint if we need to
        if (safeStartingPoint > 0 && obj.getType().equalsIgnoreCase("polygon")) {
//console.log('re-ordering polygon to start from safeStartingPoint #'+safeStartingPoint,cp[safeStartingPoint]);
// move anything before safeStartingPoint to the end of the path

            List<Point2D> newEnd = cp.subList(0, safeStartingPoint);
            List<Point2D> newStart = cp.subList(safeStartingPoint, cp.size());
            basePath.clear();
            basePath.addAll(newStart);
            basePath.addAll(newEnd);
        } else {
            basePath = cp;
        }
        total.add(maxX - minX);
        total.add(maxY - minY);
        double smallestAxis = total.get(0);
        if (total.get(1) < total.get(0)) {
            smallestAxis = total.get(1);
        }
// check if the last point in the basePath is equal to the first, if not add it
        if (basePath.get(0).getX() == basePath.get(basePath.size() - 1).getX() && basePath.get(0).getY() == basePath.get(basePath.size() - 1).getY()) {
// they both are equal
        } else {
// add it to the end, this will close the polygon
            basePath.add(new Point2D(basePath.get(0).getX(), basePath.get(0).getY()));
        }
//console.log('##basePath##');
//console.log(basePath);
// we need to figure out the path direction because the path offset algorithm
// expects a CCW path for paths which hasTAngle is true, however we can always change the path direction to either
// direction before or after the tool path processing
// this is only true of a non centerOnPath cutType
        boolean wasReversed = false;
        if (hasTAngle == true && !cutType.equalsIgnoreCase(CENTER_ON_PATH)) {
// to figure out the path direction we can draw an outside offset path then test if any
// point in the newly created path is inside the original path, if it is this means that
// the path direction is CW not CCW which means we will need to temporarily reverse it to
// generate the correct offset path
            List<Point2D> testOffset = this.generateOffsetPath(OUTSIDE, basePath, this.tool.getDiameter() / 2);
            if (this.pointInPolygon(testOffset.get(0), basePath)) {
// reverse the path
                wasReversed = true;
//console.log('reversing path to generate correct offset, path must be CW and have a T angle line segment');
                Collections.reverse(basePath);
            }
        }
        List<Point2D> toolPath = new ArrayList<>();
        if (cutType.equalsIgnoreCase(CENTER_ON_PATH)) {
// just convert the normal path to gcode
// copy basePath to toolPath
            toolPath = basePath;
        } else if (cutType.equalsIgnoreCase(OUTSIDE)) {
            toolPath = this.generateOffsetPath(cutType, basePath, this.tool.getDiameter() / 2);
        } else if (cutType.equalsIgnoreCase(INSIDE)) {
            if (obj.getType().equalsIgnoreCase(CIRCLE) && obj.getRadius() * 2 == this.tool.getDiameter()) {
// this is a circle which is the size of the tool, no need to create an offset
                toolPath = basePath;
            } else {
                toolPath = this.generateOffsetPath(cutType, basePath, this.tool.getDiameter() / 2);
            }
        } else if (cutType.equalsIgnoreCase(POCKET)) {
// this needs to loop over and over until it meets the center
            toolPath = this.generateOffsetPath(INSIDE, basePath, this.tool.getDiameter() / 2);
//console.log('smallestAxis: '+smallestAxis);
            List<Point2D> previousPath = new ArrayList<>();
            previousPath.addAll(toolPath);
            for (int c = 0; c < (smallestAxis - (this.tool.getDiameter() * 2)) / (this.tool.getDiameter() * this.tool.getStep()); c++) {
// we use the previous path (which was itself an inside offset) as the next path
                previousPath = this.generateOffsetPath(INSIDE, previousPath, this.tool.getDiameter() * this.tool.getStep() / 2);
                if (previousPath != null) {
// this is a real toolpath, add it
                    for (int a = 0; a < previousPath.size(); a++) {
// add path to toolpath
                        toolPath.add(previousPath.get(a));
                    }
                }
            }
        }
        if (wasReversed == true) {
// we need to now set the path and offset path back to their original direction
            Collections.reverse(basePath);
            Collections.reverse(toolPath);
            System.out.println("was reversed");
        }
// for reversing path directions to change between the default CCW (Climb) cut
// to a CW (Conventional) cut
        if (config.isUseConventionalCut()) {
            Collections.reverse(basePath);
            Collections.reverse(toolPath);
        }
//console.log('##toolPath##');
//console.log(toolPath);
// draw the original path on the html canvas
        drawPath(basePath, this.tool, cutType, depth, true, obj.getName());
//console.log('basePath first point inside mc.cut ',basePath[0]);
        if (!cutType.equalsIgnoreCase(CENTER_ON_PATH)) {
// draw the offset path on the html canvas
            drawPath(toolPath, this.tool, cutType, depth, false, obj.getName());
        }
// now put a comment that explains that the next block of GCODE is for this obj
        this.gcode += "\n; PATH FOR \"" + obj.getName() + "\" " + obj.getType() + " WITH " + cutType + " CUT\n";
// calculate the number of Z passes
        double numZ = Math.ceil(depth / this.tool.getPassDepth());
// comment with Z information
        this.gcode += "; total Z cut depth of " + depth + " with passDepth of " + this.tool.getPassDepth() + " yields " + numZ + " total passes\n";
// move to zClearance
        this.gcode += "\n; MOVING TO " + this.tool.getzClearance() + "\n";
        this.gcode += "G0 F" + this.tool.getRapid() + " Z" + this.tool.getzClearance() + "\n";
// move to first point in toolPath
        this.gcode += "; MOVING TO FIRST POINT IN toolPath\n";
        this.gcode += "G0 F" + this.tool.getRapid() + " X" + toolPath.get(0).getX() + " Y" + toolPath.get(0).getY() + "\n";
// now for each Z pass, generate the actual path
        double zPos = 0;
        for (int z = 1; z <= numZ; z++) {
// calc Z for this pass
            if (zPos - this.tool.getPassDepth() < -depth) {
// this is a partial pass which would mean it is the final pass
// set zPos to -depth
                zPos = -depth;
            } else {
// this is a full pass, go down another this.tool.passDepth
                zPos = zPos - this.tool.getPassDepth();
            }
// comment for pass
            this.gcode += "\n; PASS #" + z + " AT " + zPos + " DEPTH\n";
// generate Z movement at this.tool.plunge speed
            this.gcode += "G1 F" + this.tool.getPlunge() + " Z" + zPos + "\n";
// loop through each point in the path
            for (int c = 0; c < toolPath.size(); c++) {
                if (c == toolPath.size() - 1) {
// this is the last point in the toolPath we can just add it
// regardless of the current Z
                    this.gcode += "G1 F" + this.tool.getCut() + " X" + toolPath.get(c).getX() + " Y" + toolPath.get(c).getY() + "\n";
// now we need to check if this Z layer would need to account for tabs
// in the event that the tabHeight is greater than tool.passDepth,
// multiple layers would have to account for tabs
// numZ is the total number of Z layers
                } else if (this.tool.getPassDepth() * (numZ - z) <= config.getTabHeight() && config.getTabs() == true) {
                    System.out.println("creating tabs for Z pass " + z);
// we need to create the tabs for this layer
// tabs are only created on straight line sections
// because it is hard to cut them out of curved sections
// first we get the total distance of the path
                    double d = this.distanceFormula(toolPath.get(c), toolPath.get(c + 1));
                    if (d >= (config.getTabSpacing() + config.getTabWidth())) {
// there is space in this line to create tabs
                        long numTabs = Math.round(d / (config.getTabSpacing() + config.getTabWidth()));
// if we have a line distance of 100
// and 3 tabs (width 10) in that line per numTabs
// then we want to evenly space them
// so we divide the line distance by numTabs
                        double spacePerTab = d / numTabs;
// which in this example would be 33.33~
// then in each space per tab we need to center the tab
// which means dividing the difference of the spacePerTab and tabWidth by 2
                        double tabPaddingPerSpace = (spacePerTab - config.getTabWidth()) / 2;
// now we need to do the point geometry to get the points
// we start at toolPath[c] which represents the starting point
// and we end at toolPath[c+1]
// first we need to get the angle that the whole line is running along
// get the deltas for X and Y to calculate the line angle with atan2
                        double deltaX = toolPath.get(c + 1).getX() - toolPath.get(c).getX();
                        double deltaY = toolPath.get(c + 1).getY() - toolPath.get(c).getY();
// get the line angle
                        double ang = Math.atan2(deltaY, deltaX);
//console.log(' ANGLE '+ang+' or '+(ang*180/Math.PI));
// convert it to degrees for later math with addDegree
                        ang = ang * 180 / Math.PI;
// now that we have the line angle, we can create each of the tabs
// first we need to add the first point to gcode
                        this.gcode += "G1 F" + this.tool.getCut() + " X" + toolPath.get(c).getX() + " Y" + toolPath.get(c).getY() + "\n";
                        this.gcode += "\n; START TABS\n";
                        Point2D npt = toolPath.get(c);
                        for (int r = 0; r < numTabs; r++) {
// then for each tab
// add another point at the current point +tabPaddingPerSpace
                            npt = this.newPointFromDistanceAndAngle(npt, ang, tabPaddingPerSpace);
                            this.gcode += "G1 F" + this.tool.getCut() + " X" + npt.getX() + " Y" + npt.getY() + "\n";
// then we raise the z height by config.tabHeight
                            this.gcode += "G1 Z" + (zPos + config.getTabHeight()) + "\n";
// then add another point at the current point +tabWidth
                            npt = this.newPointFromDistanceAndAngle(npt, ang, config.getTabWidth());
                            this.gcode += "G1 F" + this.tool.getCut() + " X" + npt.getX() + " Y" + npt.getY() + "\n";
// then lower the z height back to zPos at plunge speed
                            this.gcode += "G1 F" + this.tool.getPlunge() + " Z" + zPos + "\n";
// then add another point at the current point +tabPaddingPerSpace
// with the cut speed
                            npt = this.newPointFromDistanceAndAngle(npt, ang, tabPaddingPerSpace);
                            this.gcode += "G1 F" + this.tool.getCut() + " X" + npt.getX() + " Y" + npt.getY() + "\n";
                        }
                        this.gcode += "; END TABS\n\n";
//console.log(numTabs+' for a line of '+d+' units with '+spacePerTab+' space per tab and a tabPaddingPerSpace of '+tabPaddingPerSpace);
//console.log('line angle '+ang);
                    } else {
// line is not long enough, just draw it
                        this.gcode += "G1 F" + this.tool.getCut() + " X" + toolPath.get(c).getX() + " Y" + toolPath.get(c).getY() + "\n";
                    }
                } else {
// no tabs
                    this.gcode += "G1 F" + this.tool.getCut() + " X" + toolPath.get(c).getX() + " Y" + toolPath.get(c).getY() + "\n";
                }
            }
        }
// now move back to zClearance
        this.gcode += "\n; PATH FINISHED FOR \"" + obj.getName() + "\" " + obj.getType() + " WITH " + cutType + " CUT, MOVING BACK TO " + this.tool.getzClearance() + "\n";
        this.gcode += "G0 F" + this.tool.getRapid() + " Z" + this.tool.getzClearance() + "\n";
    }
    public static final String POCKET = "pocket";
    public static final String CIRCLE = "circle";
    public static final String INSIDE = "inside";
    public static final String OUTSIDE = "outside";
    public static final String CENTER_ON_PATH = "centerOnPath";

    public void insert(String g) {
// insert gcode directly
        this.gcode += g + "\n";
    }

    ;
public void get() {
// this function returns the finished gcode
// it is called after the cut operations, so we need to prepend and append some gcode
        String s = "";
// first list the options
        s = "; TOOL OPTIONS\n";

        s += "; cut: " + this.tool.getCut() + '\n';

// set units
        s += "\n; SETTING UNITS TO " + this.tool.getUnits() + "\n";
        if (this.tool.getUnits().equalsIgnoreCase("mm")) {
            s += "G21\n";
        } else {
            s += "G20\n";
        }
// set absolute mode
        s += "\n; SETTING ABSOLUTE POSITIONING\n";
        s += "G90\n";
        this.gcode = s + this.gcode;
// returnHome if set
// this needs to be moved outside of the object and at the end of all objects
        if (this.tool.isReturnHome()) {
            this.gcode += "\n; RETURNING TO 0,0,0 BECAUSE this.tool.returnHome IS SET\n";
            this.gcode += "G0 F" + this.tool.getRapid() + " X0 Y0 Z0\n";
        }
//console.log(this.gcode);
        toSaveGcode = this.gcode;
    }

    List<Point2D> cubicBezier(List<Point2D> b) {
// expects an array with 4 points
// p1 (start), cp1, cp2, p2 (end)
// should return a path of line segments from p1 to p2
// which represent the curve
// doing this is relatively simple, I don't know why it's not explained better on the Internet
// first you
// get distance for p1 -> cp1
// get distance for cp1 -> cp2
// get distance for cp2 -> p2
// then if you wanted to get 100 points on the path
// you would just do a loop with 100 iterations
// and in each loop, do this with increase % steps based on the loop count:
// get point at 1% (100 steps) of distance between p1 -> cp1
// repeat for cp1 -> cp2
// repeat for cp2 -> p2
// then you would be down to 3 points from the original 4
// then get the line distance for the 2 new lines which connect the 3 new points
// and the points at 1% of each of those lines
// which would leave you with 2 points
// then repeat again to get the line distance for the new line which would connect those 2 points
// and get the point at 1% of that line
// which would give you the true point of 1% of the actual bezier curve
//console.log('generating line segments for cubic bezier',b);
//console.log('should generate a curved path from',b[0],'to',b[3]);
// we use a bit of the dxf library here

// get distance to calculate a reasonable number of line segments
        double dist = distanceFormula(b.get(0), b.get(3));
        long numLineSegments;
        if (this.tool.getUnits().equals("in")) {
// for inches, assume a .25in bit with 10 lines segments per diameter
            numLineSegments = Math.round(dist / (.25 / 10));
        } else {
// we are assuming 1px = 1mm for your toolchain, if you are using inches change units to in
// going to base it on 10 line segments for a 6.35mm bit
            numLineSegments = Math.round(dist / (6.35 / 10));
        }
        double[] distances = {distanceFormula(b.get(0), b.get(1)), distanceFormula(b.get(1), b.get(2)), distanceFormula(b.get(2), b.get(3))};
// we need the angle of each of the lines in distances to calculate the new points
// which is 180 times the atan2 of the slope 180*Math.atan2(p1y-p2y,p1x-p2x)/Math.PI
        double[] angles = {addDegrees(180, 180 * Math.atan2(b.get(0).getY() - b.get(1).getY(), b.get(0).getX() - b.get(1).getX()) / Math.PI), addDegrees(180, 180 * Math.atan2(b.get(1).getY() - b.get(2).getY(), b.get(1).getX() - b.get(2).getX()) / Math.PI), addDegrees(180, 180 * Math.atan2(b.get(2).getY() - b.get(3).getY(), b.get(2).getX() - b.get(3).getX()) / Math.PI)};
//console.log('distances for 4 original points',distances);
//console.log('angles for those 3 lines',angles);

        double percentOfLine = 1.0 / numLineSegments;
        List<Point2D> newPoints = new ArrayList<>();
        /*
         // just testing to return a point between p1 and p2
         console.log('distance',dist);
         var ang = Math.atan2(b[0][1]-b[3][1], b[0][0]-b[3][0])*180/Math.PI;
         ang = dxfLib.addDegrees(180,ang);
         console.log('angle',ang);
         //ang = dxfLib.addDegrees(angOffset,ang);
         var pt = dxfLib.newPointFromDistanceAndAngle(b[0],ang,dist/2);
         return [pt];
         */
        
        System.out.println("Bezier LineSegements:" + numLineSegments);
        for (int c = 1; c < numLineSegments; c++) {
// here's the loop for each line segment
// we need the point at percentOfLine for each line distance
            List<Point2D> threePoints = new ArrayList<>();
//double[] twoDistances;
//double[] twoAngles;
            List<Point2D> twoPoints = new ArrayList<>();
            double oneDistance = 0.0;
            double oneAngle = 0.0;
            Point2D point;
            for (int r = 0; r < distances.length; r++) {
// this takes us from 4 points down to 3 points
// get the 3 points for the original 3 distances
                threePoints.add(newPointFromDistanceAndAngle(b.get(r), angles[r], distances[r] * percentOfLine * c));
            }
// get the 2 distances for those 3 points
            double[] twoDistances = {distanceFormula(threePoints.get(0), threePoints.get(1)), distanceFormula(threePoints.get(1), threePoints.get(2))};
// get the 2 angles for those 2 distances/3 points
            double[] twoAngles = {addDegrees(180, 180 * Math.atan2(threePoints.get(0).getY() - threePoints.get(1).getY(), threePoints.get(0).getX() - threePoints.get(1).getX()) / Math.PI), addDegrees(180, 180 * Math.atan2(threePoints.get(1).getY() - threePoints.get(2).getY(), threePoints.get(1).getX() - threePoints.get(2).getX()) / Math.PI)};
//console.log('twoAngles',twoAngles);
// now loop through those 2 distances
// taking us from 3 points to 2 points
            for (int r = 0; r < twoDistances.length; r++) {
                twoPoints.add(newPointFromDistanceAndAngle(threePoints.get(r), twoAngles[r], twoDistances[r] * percentOfLine * c));
            }
// now get the distance for those 2 points
            oneDistance = distanceFormula(twoPoints.get(0), twoPoints.get(1));
// and the angle for that distance/2 points
            oneAngle = addDegrees(180, 180 * Math.atan2(twoPoints.get(0).getY() - twoPoints.get(1).getY(), twoPoints.get(0).getX() - twoPoints.get(1).getX()) / Math.PI);
// then finally get the one point, which actually lies on the curve
            point = newPointFromDistanceAndAngle(twoPoints.get(0), oneAngle, oneDistance * percentOfLine * c);
//console.log('point',point);
            newPoints.add(point);
        }
        return newPoints;
    }

}
