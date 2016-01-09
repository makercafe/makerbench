/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.makercafe.apps.makerbench.millcrum;

import be.makercafe.apps.makerbench.millcrum.MillObject;
import be.makercafe.apps.makerbench.millcrum.Millcrum;
import be.makercafe.apps.test.JfxTestRunner;
import be.makercafe.apps.makerbench.millcrum.Config;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.stage.Stage;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 *
 * @author m999ldp
 */
@RunWith( JfxTestRunner.class )
public class MillcrumTest {

    public MillcrumTest() {
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
     * Test of addDegrees method, of class Millcrum.
     */
    @Test
    public void testAddDegrees() {
        System.out.println("addDegrees");
        double base = 360.0;
        double mod = 0.5;
        Millcrum instance = new Millcrum();
        double expResult = 0.5;
        double result = instance.addDegrees(base, mod);
        assertEquals(expResult, result, 0.0);

    }

    /**
     * Test of surface method, of class Millcrum.
     */
    @Test
    public void testSurface() {
        System.out.println("surface");
        int x = 100;
        int y = 200;
        Millcrum instance = new Millcrum();
        instance.surface(x, y);
        assertEquals(instance.getGlobal5x(), x);
        assertEquals(instance.getGlobal5y(), y);
    }


    /**
     * Test of pointInPolygon method, of class Millcrum.
     */
    @Test
    public void testPointInPolygon_Point2D_List() {
        System.out.println("pointInPolygon");
        Point2D point = new Point2D(25,25);
        List<Point2D> points = new ArrayList<>();
        points.add(new Point2D(0,0));
        points.add(new Point2D(100,0));
        points.add(new Point2D(100,50));
        points.add(new Point2D(0,50));
        points.add(new Point2D(0,0));

        Millcrum instance = new Millcrum();
        boolean expResult = true;
        boolean result = instance.pointInPolygon(point, points);
        assertEquals(expResult, result);

    }

    /**
     * Test of linesIntersection method, of class Millcrum.
     */
    @Test
    public void testLinesIntersection() {
        System.out.println("linesIntersection");
        Point2D l1start = new Point2D(0,0);
        Point2D l1end = new Point2D(100,100);
        Point2D l2start = new Point2D(0,100);
        Point2D l2end = new Point2D(100,0);
        Millcrum instance = new Millcrum();
        Millcrum.LinesIntersectionResult expResult = null;
        Millcrum.LinesIntersectionResult result = instance.linesIntersection(l1start, l1end, l2start, l2end);
        System.out.println("x " + result.point.getX() + " y " + result.point.getY());
        assertEquals(50.0, result.point.getX(), 0.0);
        l2start = new Point2D(5,0);
        l2end = new Point2D(105,100);
        result = instance.linesIntersection(l1start, l1end, l2start, l2end);
        assertTrue(result.parallel);
    }

    /**
     * Test of distanceFormula method, of class Millcrum.
     */
    @Test
    public void testDistanceFormula() {
        System.out.println("distanceFormula");
        Point2D p1 = new Point2D(0,0);
        Point2D p2 = new Point2D(100,100);
        Millcrum instance = new Millcrum();
        double expResult = 141.4213562373095;
        double result = instance.distanceFormula(p1, p2);
        System.out.println("Distance: " + result);
        assertEquals(expResult, result, 0.0);

    }

    /**
     * Test of newPointFromDistanceAndAngle method, of class Millcrum.
     */
    @Test
    public void testNewPointFromDistanceAndAngle() {
        System.out.println("newPointFromDistanceAndAngle");
        Point2D pt = new Point2D(0,0);;
        double ang = 45.0;
        double distance = 141.4213562373095;
        Millcrum instance = new Millcrum();
        Point2D expResult = null;
        Point2D result = instance.newPointFromDistanceAndAngle(pt, ang, distance);
        assertEquals(100, Math.round(result.getX()));
    }

    /**
     * Test of generateArc method, of class Millcrum.
     */
    @Test
    public void testGenerateArc() {
        System.out.println("generateArc");
        double startDeg = 0.0;
        double endDeg = 360.0;
        double r = 10.0;
        double toolDiameter = 5.0;
        Millcrum instance = new Millcrum();
        List<Point2D> expResult = null;
        List<Point2D> result = instance.generateArc(startDeg, endDeg, r, toolDiameter);
        System.out.println("Size: " + result.size());
        for(Point2D point : result){
            System.out.println("point : " + point.getX() + ", " + point.getY());
        }
        assertEquals(40, result.size());
    }

    /**
     * Test of generateOffsetPath method, of class Millcrum.
     */
    @Test
    public void testGenerateOffsetPath() {
        System.out.println("generateOffsetPath");
        String type = "outside";
        double offsetDistance = 10.0;
        double startDeg = 0.0;
        double endDeg = 360.0;
        double r = 10.0;
        double toolDiameter = 5.0;
        Millcrum instance = new Millcrum();
        List<Point2D> expResult = null;
        List<Point2D> basePath = instance.generateArc(startDeg, endDeg, r, toolDiameter);
        List<Point2D> result = instance.generateOffsetPath(type, basePath, offsetDistance);
                System.out.println("Size: " + result.size());
        for(Point2D point : result){
            System.out.println("point : " + point.getX() + ", " + point.getY());
        }
        assertEquals(40, result.size());
    }

    /**
     * Test of cut method, of class Millcrum.
     */
    @Test
    public void testCut() {
        System.out.println("cut");
        String cutType = "centerOnPath";
        MillObject obj = new MillObject();
        obj.setName("rectangle");
        obj.setType("rect");
        obj.setCornerRadius(5);
        obj.setxLen(100);
        obj.setyLen(200);
        double depth = 5.0;
        Point2D startPos = new Point2D(0,0);
        Config config = null;
        Millcrum instance = new Millcrum();
        instance.cut(cutType, obj, depth, startPos, config);
        System.out.println("Gcode:");
        System.out.println(instance.getGcode());
    }
 /**
     * Test of cut method, of class Millcrum.
     */
    @Test
    public void testCutOutside() {
        System.out.println("cut");
        String cutType = "outside";
        MillObject obj = new MillObject();
        obj.setName("rectangle");
        obj.setType("rect");
        obj.setCornerRadius(5);
        obj.setxLen(100);
        obj.setyLen(200);
        double depth = 5.0;
        Point2D startPos = new Point2D(0,0);
        Config config = null;
        Millcrum instance = new Millcrum();
        instance.cut(cutType, obj, depth, startPos, config);
        System.out.println("Gcode:");
        System.out.println(instance.getGcode());
    }

    /**
     * Test of cut method, of class Millcrum.
     */
    @Test
    public void testCutInside() {
        System.out.println("cut");
        String cutType = "inside";
        MillObject obj = new MillObject();
        obj.setName("rectangle");
        obj.setType("rect");
        obj.setCornerRadius(5);
        obj.setxLen(100);
        obj.setyLen(200);
        double depth = 5.0;
        Point2D startPos = new Point2D(0,0);
        Config config = null;
        Millcrum instance = new Millcrum();
        instance.cut(cutType, obj, depth, startPos, config);
        System.out.println("Gcode:");
        System.out.println(instance.getGcode());
    }
    /**
     * Test of insert method, of class Millcrum.
     */
    @Test
    public void testInsert() {
        System.out.println("insert");
        String g = "test";
        Millcrum instance = new Millcrum();
        instance.insert(g);
        assertEquals("test\n", instance.getGcode());
    }

    /**
     * Test of get method, of class Millcrum.
     */
    @Test
    public void testGet() {
        System.out.println("get");
          String cutType = "centerOnPath";
        MillObject obj = new MillObject();
        obj.setName("rectangle");
        obj.setType("rect");
        obj.setCornerRadius(5);
        obj.setxLen(100);
        obj.setyLen(200);
        double depth = 5.0;
        Point2D startPos = new Point2D(0,0);
        Config config = null;
        Millcrum instance = new Millcrum();
        instance.cut(cutType, obj, depth, startPos, config);
        instance.get();
        System.out.println("Gcode:");
        System.out.println(instance.getToSaveGcode());
    }

}
