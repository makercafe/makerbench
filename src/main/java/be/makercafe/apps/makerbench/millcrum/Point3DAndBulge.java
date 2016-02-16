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
