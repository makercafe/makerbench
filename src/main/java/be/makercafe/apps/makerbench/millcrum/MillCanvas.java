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
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 *
 * @author m999ldp
 */
public class MillCanvas {
    
    private GraphicsContext canvasContext;
    private double scaleFactor = 0;
    private Canvas canv;

    private double canvasToSurfaceMargin = 20;
    private double canvasSurfaceOffsetX = 0;
    private double canvasSurfaceOffsetY = 0;
    private double sX;
    private double sY;

    public GraphicsContext getCanvasContext() {
        return canvasContext;
    }

    public void setCanvasContext(GraphicsContext canvasContext) {
        this.canvasContext = canvasContext;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public Canvas getCanv() {
        return canv;
    }

    public void setCanv(Canvas canv) {
        this.canv = canv;
    }

    public double getCanvasToSurfaceMargin() {
        return canvasToSurfaceMargin;
    }

    public void setCanvasToSurfaceMargin(double canvasToSurfaceMargin) {
        this.canvasToSurfaceMargin = canvasToSurfaceMargin;
    }

    public double getCanvasSurfaceOffsetX() {
        return canvasSurfaceOffsetX;
    }

    public void setCanvasSurfaceOffsetX(double canvasSurfaceOffsetX) {
        this.canvasSurfaceOffsetX = canvasSurfaceOffsetX;
    }

    public double getCanvasSurfaceOffsetY() {
        return canvasSurfaceOffsetY;
    }

    public void setCanvasSurfaceOffsetY(double canvasSurfaceOffsetY) {
        this.canvasSurfaceOffsetY = canvasSurfaceOffsetY;
    }

    public double getsX() {
        return sX;
    }

    public void setsX(double sX) {
        this.sX = sX;
    }

    public double getsY() {
        return sY;
    }

    public void setsY(double sY) {
        this.sY = sY;
    }

    public MillCanvas() {
        this.init(640, 400);
    }
    
    public MillCanvas(double x, double y){
        this.init(x, y);
    }
    
    public void drawPath(List<Point2D> p, Tool tool, String cutType, double depth, boolean isOriginal, String name) {

	if (p.size() == 1) {
		// this is a single point circle
		// which means an inside cut of a circle with a diameter of tool.diameter
		// we just need to draw a circle that big
		//console.log('got single point circle');

		canvasContext.beginPath();
		Point2D tp = getCanvPoint(p.get(0));
		double r = tool.getDiameter()/2*scaleFactor;
		canvasContext.arc(tp.getX(),tp.getY(),r,r,0.0,2*Math.PI);
                canvasContext.setFill(Color.web("#00008b"));
                canvasContext.setStroke(Color.web("#d2691e"));
		canvasContext.fill();
		canvasContext.stroke();

	} else {

		// draw a normal path

		// this is before processing to canvas points
		double minx = p.get(0).getX();
		double  miny = p.get(0).getY();
		double maxx = p.get(0).getX();
		double maxy = p.get(0).getY();
		double firstx = p.get(0).getX();
		double firsty = p.get(0).getY();

		// loop through the points and convert them to canvas points
		// you have to create a new array here or JS gets really off
		List<Point2D> np = new ArrayList<>();
                for (Point2D p1 : p) {
                    if (p1.getX() < minx) {
                        minx = p1.getX();
                    } else if (p1.getX() > maxx) {
                        maxx = p1.getX();
                    }
                    if (p1.getY() < miny) {
                        miny = p1.getY();
                    } else if (p1.getY() > maxy) {
                        maxy = p1.getY();
                    }
                    np.add(getCanvPoint(p1));
                }

		if (isOriginal) {

			// get the path direction
			double  signedArea = 0;
			for (int i=1; i<np.size(); i++) {
				if (!(np.get(i).getX() == np.get(i-1).getX() && np.get(i).getY() == np.get(i-1).getY())) {
					// skip this point it is the same as the last
					signedArea += np.get(i).getX() * np.get(i-1).getY() - np.get(i-1).getX() * np.get(i).getY();
				}
				
			}

			String pathDir = "Climb (CCW)";
			if (signedArea < 0) {
				// clockwise paths have an area below 0
				pathDir = "Conventional (CW)";
			}

			String cDepth;
			if (depth > tool.getPassDepth()) {
				cDepth = depth+tool.getUnits()+" ("+Math.ceil(depth/tool.getPassDepth())+" passes)";
			} else {
				cDepth = depth+tool.getUnits()+" (1 pass)";
			}

			// store the path for mouse clicks
                        // TODO: make paths clickable
			//clickPaths.push({path:np,cutType:cutType,depth:cDepth,pathDir:pathDir,signedArea:Math.round(signedArea/2),startPoint:p[0],name:name});
		}

		if (maxx > sX || maxy > sY) {
			System.out.println("the path is larger than the surface, path has a maximum X of "+maxx+" and a maximum Y of "+maxy);
		}

		// move to first point
		canvasContext.beginPath();
		canvasContext.moveTo(np.get(0).getX(),np.get(0).getY());
		canvasContext.setFont(Font.font("Arial", 12));

		// loop through path starting at 1
		for (int c=1; c<p.size(); c++) {
			if (isOriginal) {
				// draw coordinates on screen
				//canvasContext.fillStyle = '#000';
				//canvasContext.fillText(c+' : '+Math.round(p[c][0])+','+Math.round(p[c][1]), np[c][0], np[c][1]);
			}
			canvasContext.lineTo(np.get(c).getX(),np.get(c).getY());
		}

		canvasContext.setLineWidth(1);
		if (isOriginal) {
			canvasContext.setStroke(Color.web("#d2691e"));
			// write first point at np[0]
			//canvasContext.fillText(Math.round(firstx)+','+Math.round(firsty),np[0][0]-20,np[0][1]+20);
		} else {
			// draw the actual tool path a darker color
			canvasContext.setStroke(Color.web("#00008b"));
		}
		canvasContext.stroke();

	}

}



public Point2D getCanvPoint(Point2D p) {

	// return points modified by scaleFactor
	// and add the pixel margin we set when calculating the scaleFactor
	

	// now flip the y axis
	// in canvas y increases while moving down
	// in cnc y increases while moving up
	

	return new Point2D(canvasToSurfaceMargin+(canvasSurfaceOffsetX/2)+(p.getX()*scaleFactor), canv.getHeight() - (canvasToSurfaceMargin+(canvasSurfaceOffsetY/2)+(p.getY()*scaleFactor)) );

}

// re-init on resize
//window.onresize = function(e) {
//	init();
//}

public void init(double x, double y) {

    
	canv = new Canvas(x,y);

	// update surface size from input boxes
	// this will be the maximum size
	sX = x;
	sY = y;
	System.out.println("surface size: "+sX+","+sY);

	canvasContext = canv.getGraphicsContext2D();

	// clear it
	canvasContext.clearRect(0,0,canv.getWidth(),canv.getHeight());

	// get the scale factor for the X and Y axis
	// with a pixel margin
	double xf = (canv.getWidth()-(canvasToSurfaceMargin*2))/sX;
	double yf = (canv.getHeight()-(canvasToSurfaceMargin*2))/sY;
	//console.log('XY scale factors',xf,yf);

	// now we need to set scaleFactor to the smaller scale factor
	if (xf < yf) {
		scaleFactor = xf;
	} else {
		scaleFactor = yf;
	}

	// calculate the amount of extra space there will be in the case that the surface is
	// has different X and Y scale ratios to the canvas
	canvasSurfaceOffsetX = canv.getWidth()-(canvasToSurfaceMargin*2)-(sX*scaleFactor);
	canvasSurfaceOffsetY = canv.getHeight()-(canvasToSurfaceMargin*2)-(sY*scaleFactor);
	//console.log('canvasSurfaceOffset '+canvasSurfaceOffsetX+','+canvasSurfaceOffsetY);

	// draw the surface
	canvasContext.setLineWidth(1);

	// x axis
	canvasContext.setStroke(Color.BLUE);
	canvasContext.beginPath();
	Point2D p = getCanvPoint(new Point2D(0,0));
	canvasContext.moveTo(p.getX(),p.getY());
	p = getCanvPoint(new Point2D(sX,0));
	canvasContext.lineTo(p.getX(),p.getY());
	canvasContext.stroke();

	// far y axis
	canvasContext.setStroke(Color.web("#aaaaaa"));
	canvasContext.beginPath();
	canvasContext.moveTo(p.getX(),p.getY());
	p = getCanvPoint(new Point2D(sX,sY));
	canvasContext.lineTo(p.getX(),p.getY());

	// far x axis
	p = getCanvPoint(new Point2D(0,sY));
	canvasContext.lineTo(p.getX(),p.getY());
	canvasContext.stroke();

	// y axis
	canvasContext.setStroke(Color.RED);
	canvasContext.beginPath();
	canvasContext.moveTo(p.getX(),p.getY());
	p = getCanvPoint(new Point2D(0,0));
	canvasContext.lineTo(p.getX(),p.getY());
	canvasContext.stroke();

	// generate a grid of nGridLines lines for the larger axis
	// this will calculate a grid spacing number
	double largerAxisSize = 0;
	double nGridLines = 10;
	if (sX > sY) {
		largerAxisSize = sY;
	} else {
		largerAxisSize = sX;

	}

	// grid lines color
	canvasContext.setStroke(Color.web("#eeeeee"));

	// now loop through nGridLines times and draw each line
	for (int c=0; c<nGridLines; c++) {

		// perpendicular to X (growing on Y axis)
		canvasContext.beginPath();
		canvasContext.setFill(Color.RED);
		p = getCanvPoint(new Point2D(0,c*(sY/nGridLines)));
		canvasContext.moveTo(p.getX(),p.getY());
		p = getCanvPoint(new Point2D(sX,c*(sY/nGridLines)));
		canvasContext.lineTo(p.getX(),p.getY());

		// write dimension
		p = getCanvPoint(new Point2D(0,(c*(sY/nGridLines))));
		canvasContext.fillText(String.valueOf(Math.round(c*(sY/nGridLines))),p.getX()-20,p.getY()+5);

		// stroke
		canvasContext.stroke();

		// perpendicular to Y (growing on X axis)
		canvasContext.beginPath();
		canvasContext.setFill(Color.BLUE);
		p = getCanvPoint(new Point2D(c*(sX/nGridLines),0));
		canvasContext.moveTo(p.getX(),p.getY());
		p = getCanvPoint(new Point2D(c*(sX/nGridLines),sY));
		canvasContext.lineTo(p.getX(),p.getY());

		// write dimension
		p = getCanvPoint(new Point2D((c*(sX/nGridLines)),0));
		canvasContext.fillText(String.valueOf(Math.round(c*(sX/nGridLines))),p.getX()-4,p.getY()+17);

		// stroke
		canvasContext.stroke();
	}

}

//addLoadEvent(init);

    
}
