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
import java.util.Arrays;
import java.util.List;
import javafx.geometry.Point2D;

/**
 *
 * @author m999ldp
 */
class Bezier{
  private static final float AP = 0.5f;
  
  public static List<Point2D> cubicBezier(List<Point2D> points) {
      return cubicBezier((Point2D[]) points.toArray(new Point2D[points.size()]));
  }
  
  /**
   * Creates a new Bezier curve.
   * @param points
   */
  public static List<Point2D> cubicBezier(Point2D[] points) {
     Point2D[] bPoints;
    int n = points.length;
    if (n < 3) {
      // Cannot create bezier with less than 3 points
      return new ArrayList<>();
    }
    bPoints = new Point2D[2 * (n - 2)];
    double paX, paY;
    double pbX = points[0].getX();
    double pbY = points[0].getY();
    double pcX = points[1].getX();
    double pcY = points[1].getY();
    for (int i = 0; i < n - 2; i++) {
      paX = pbX;
      paY = pbY;
      pbX = pcX;
      pbY = pcY;
      pcX = points[i + 2].getX();
      pcY = points[i + 2].getY();
      double abX = pbX - paX;
      double abY = pbY - paY;
      double acX = pcX - paX;
      double acY = pcY - paY;
      double lac = Math.sqrt(acX * acX + acY * acY);
      acX = acX /lac;
      acY = acY /lac;

      double proj = abX * acX + abY * acY;
      proj = proj < 0 ? -proj : proj;
      double apX = proj * acX;
      double apY = proj * acY;

      double p1X = pbX - AP * apX;
      double p1Y = pbY - AP * apY;
      bPoints[2 * i] = new Point2D(p1X, p1Y);

      acX = -acX;
      acY = -acY;
      double cbX = pbX - pcX;
      double cbY = pbY - pcY;
      proj = cbX * acX + cbY * acY;
      proj = proj < 0 ? -proj : proj;
      apX = proj * acX;
      apY = proj * acY;

      double p2X = pbX - AP * apX;
      double p2Y = pbY - AP * apY;
      bPoints[2 * i + 1] = new Point2D(p2X, p2Y);
    }
    return Arrays.asList(bPoints);
  }
}

