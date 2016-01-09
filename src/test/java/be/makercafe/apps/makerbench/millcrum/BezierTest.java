/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.makercafe.apps.makerbench.millcrum;

import be.makercafe.apps.makerbench.millcrum.Bezier;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author m999ldp
 */
public class BezierTest {
    
    public BezierTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of cubicBezier method, of class Bezier.
     */
    @Test
    public void testCubicBezier_List() {
        System.out.println("cubicBezier");
        List<Point2D> points = new ArrayList<>();
        points.add(new Point2D(20,20));
        points.add(new Point2D(20,100));
        points.add(new Point2D(200,100));
        points.add(new Point2D(20,20));
        List<Point2D> expResult = null;
        List<Point2D> result = Bezier.cubicBezier(points);
        System.out.println("Size: " + result.size());
        for(Point2D point : result){
             System.out.println("  Point: " + point.getX() +", " + point.getY());
        }
    }


    
}
