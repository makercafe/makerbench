/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.makercafe.apps.makerbench.millcrum;

import be.makercafe.apps.makerbench.millcrum.Dxf;
import be.makercafe.apps.makerbench.millcrum.Point3DAndBulge;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
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
public class DxfTest {

	public DxfTest() {
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
	 * Test of Dxf method, of class Dxf.
	 */
	@Test
	public void testDxf() {
		System.out.println("Dxf");
		Dxf instance = new Dxf();
		instance.Dxf();
		assertNotNull(instance);
	}

	/**
	 * Test of terneryDiff method, of class Dxf.
	 */
	@Test
	public void testTerneryDiff() {
		System.out.println("terneryDiff");
		double a = 0.0;
		double b = 0.0;
		Dxf instance = new Dxf();
		double expResult = 0.0;
		double result = instance.terneryDiff(a, b);
		System.out.println("TernanryDiff: " + result);
		assertEquals(expResult, result, 0.0);
	}

	/**
	 * Test of addDegrees method, of class Dxf.
	 */
	@Test
	public void testAddDegrees() {
		System.out.println("addDegrees");
		double base = 360.0;
		double mod = 5.0;
		Dxf instance = new Dxf();
		double expResult = 5.0;
		double result = instance.addDegrees(base, mod);
		assertEquals(expResult, result, 0.0);
	}

	/**
	 * Test of distanceFormula method, of class Dxf.
	 */
	@Test
	public void testDistanceFormula() {
		System.out.println("distanceFormula");
		Point2D p1 = new Point2D(10, 10);
		Point2D p2 = new Point2D(10, 100);
		Dxf instance = new Dxf();
		double expResult = 90.0;
		double result = instance.distanceFormula(p1, p2);
		System.out.println("Distance: " + result);
		assertEquals(expResult, result, 0.0);
	}

	/**
	 * Test of newPointFromDistanceAndAngle method, of class Dxf.
	 */
	@Test
	public void testNewPointFromDistanceAndAngle() {
		System.out.println("newPointFromDistanceAndAngle");
		Point2D pt = new Point2D(10, 10);
		double ang = 90.0;
		double distance = 10.0;
		Dxf instance = new Dxf();
		Point2D expResult = new Point2D(10, 20);
		Point2D result = instance.newPointFromDistanceAndAngle(pt, ang, distance);
		System.out.println("Distance: " + result.getX() + ", " + result.getY());
		assertEquals(expResult, result);

	}

	/**
	 * Test of crossProduct method, of class Dxf.
	 */
	@Test
	public void testCrossProduct() {
		System.out.println("crossProduct");
		Point3D v1 = new Point3D(10, 10, 10);
		Point3D v2 = new Point3D(100, 100, 100);
		Dxf instance = new Dxf();
		Point3D expResult = new Point3D(0, 0, 0);
		Point3D result = instance.crossProduct(v1, v2);
		System.out.println("Crossproduct: " + result.getX() + ", " + result.getY() + ", " + result.getZ());
		assertEquals(expResult, result);

	}

	/**
	 * Test of calcBulgeCenter method, of class Dxf.
	 */
	@Test
	public void testCalcBulgeCenter() {
		System.out.println("calcBulgeCenter");
		Point3DAndBulge p1 = new Point3DAndBulge(10, 10, 0, 30.0);
		Point2D p2 = new Point2D(100, 100);
		Dxf instance = new Dxf();
		Point2D expResult = new Point2D(729.2500000000015, -619.2500000000015);
		Point2D result = instance.calcBulgeCenter(p1, p2);
		System.out.println("BulgeCenter: " + result.getX() + ", " + result.getY());
		assertEquals(expResult, result);

	}

	/**
	 * Test of handleHeader method, of class Dxf.
	 */
	@Test
	public void testHandleHeader() {
		System.out.println("handleHeader");
		String[] d = { "" };
		Dxf instance = new Dxf();
		instance.handleHeader(d);

	}

	/**
	 * Test of handleEntities method, of class Dxf.
	 */
	@Test
	public void testHandleEntities() throws Exception {
		System.out.println("handleEntities");
		String[] d = { "" };
		Dxf instance = new Dxf();
		instance.handleEntities(d);

	}

	/**
	 * Test of parseDxf method, of class Dxf.
	 */
	@Test
	public void testParseDxf() {
		System.out.println("parseDxf");
		File d;
		try {
			d = new File("C:\\Users\\m999ldp\\workspace-eclipsefx\\makerbench\\src\\test\\java\\be\\makercafe\\apps\\makerbench\\millcrum\\test.dxf");
			Dxf instance = new Dxf();
			instance.parseDxf(d);
			assertNotNull(instance);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
