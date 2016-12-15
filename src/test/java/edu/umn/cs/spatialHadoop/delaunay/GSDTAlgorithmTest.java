package edu.umn.cs.spatialHadoop.delaunay;

import edu.umn.cs.spatialHadoop.core.Point;
import edu.umn.cs.spatialHadoop.core.Rectangle;
import edu.umn.cs.spatialHadoop.operations.Head;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Unit test for the utility class {@link Head}.
 */
public class GSDTAlgorithmTest extends TestCase {

  /**
   * Create the test case
   *
   * @param testName
   *          name of the test case
   */
  public GSDTAlgorithmTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(GSDTAlgorithmTest.class);
  }


  static<T> boolean arrayEqualAnyOrder(T[] a1, T[] a2) {
    if (a1.length != a2.length)
      return false;
    for (T x : a1) {
      boolean found = false;
      for (int i = 0; !found && i < a2.length; i++)
        found = x == a2[i];
      // If this element was not found, return false as the arrays are not equal
      if (!found)
        return false;
    }
    // If this point is reached, it indicates that all elements were found
    return true;
  }

  /**
   * Test Delaunay Triangulation for a toy dataset. Visualized in file test_dt1.svg
   */
  public void testTriangulation1() {
    Point[] points = {
        new Point(50, 50),
        new Point(60, 10),
        new Point(70, 80),
        new Point(80, 50),
    };
    Point[][] correctTriangulation = {
        new Point[] {points[0], points[1], points[3]},
        new Point[] {points[0], points[3], points[2]},
    };

    GSDTAlgorithm algo = new GSDTAlgorithm(points, null);
    Triangulation answer = algo.getFinalTriangulation();

    int numOfTrianglesFound = 0;
    for (Point[] unsafeTriangle : answer.iterateTriangles()) {
      boolean found = false;
      for (int i = 0; !found && i < correctTriangulation.length; i++)
        found = arrayEqualAnyOrder(unsafeTriangle, correctTriangulation[i]);
      assertTrue("A triangle not found", found);
      numOfTrianglesFound++;
    }
    assertEquals(correctTriangulation.length, numOfTrianglesFound);
  }

  /**
   * Test Delaunay Triangulation with a small dataset
   */
  public void testTriangulation2() {
    Point[] points = {
        new Point(11, 89), // 0
        new Point(18, 69), // 1
        new Point(22, 25), // 2
        new Point(27, 75), // 3
        new Point(34, 13), // 4
        new Point(49, 95), // 5
        new Point(58, 14), // 6
        new Point(65, 50), // 7
        new Point(75, 25), // 8
        new Point(95, 54), // 9
    };
    List<Point[]> correctTriangulation = new ArrayList<Point[]>();
    correctTriangulation.add(new Point[] {points[7], points[4], points[6]});
    correctTriangulation.add(new Point[] {points[2], points[1], points[0]});
    correctTriangulation.add(new Point[] {points[1], points[3], points[0]});
    correctTriangulation.add(new Point[] {points[3], points[5], points[0]});
    correctTriangulation.add(new Point[] {points[7], points[2], points[4]});
    correctTriangulation.add(new Point[] {points[3], points[7], points[5]});
    correctTriangulation.add(new Point[] {points[5], points[7], points[9]});
    correctTriangulation.add(new Point[] {points[8], points[9], points[7]});
    correctTriangulation.add(new Point[] {points[8], points[7], points[6]});
    correctTriangulation.add(new Point[] {points[3], points[1], points[7]});
    correctTriangulation.add(new Point[] {points[7], points[1], points[2]});

    GSDTAlgorithm algo = new GSDTAlgorithm(points, null);
    Triangulation answer = algo.getFinalTriangulation();

    int iTriangle = 0;
    for (Point[] triangle : answer.iterateTriangles()) {
      boolean found = false;
      int i = 0;
      while (!found && i < correctTriangulation.size()) {
        found = arrayEqualAnyOrder(triangle, correctTriangulation.get(i));
        if (found)
          correctTriangulation.remove(i);
        else
          i++;
      }
      assertTrue(String.format("Triangle #%d (%f, %f), (%f, %f), (%f, %f) not found",
          iTriangle,
          triangle[0].x, triangle[0].y,
          triangle[1].x, triangle[1].y,
          triangle[2].x, triangle[2].y), found);
      iTriangle++;
    }
    for (Point[] triangle : correctTriangulation) {
      System.out.printf("Triangle not found (%f, %f) (%f, %f) (%f, %f)\n",
          triangle[0].x, triangle[0].y,
          triangle[1].x, triangle[1].y,
          triangle[2].x, triangle[2].y
      );
    }
    assertTrue(String.format("%d triangles not found", correctTriangulation.size()),
        correctTriangulation.isEmpty());
  }

  /**
   * Test that partitioning a Delaunay triangulation into final and non-final
   * ones does not change the reported triangles.
   */
  public void testPartitioning() {
    Random random = new Random(0);
    // Generate 100 random points
    Point[] points = new Point[100];
    for (int i = 0; i < points.length; i++)
      points[i] = new Point(random.nextInt(1000), random.nextInt(1000));

    Arrays.sort(points);

    // Compute Delaunay triangulation for the 100 points
    GSDTAlgorithm algo = new GSDTAlgorithm(points, null);
    Triangulation answer = algo.getFinalTriangulation();
    // Retrieve all triangles from the complete answer and use it as a baseline
    List<Point[]> allTriangles = new ArrayList<Point[]>();
    for (Point[] triangle : answer.iterateTriangles()) {
      allTriangles.add(triangle.clone());
    }

    // Split into a final and non-final graphs and check that we get the same
    // set of triangles from the two of them together
    Triangulation safe = new Triangulation();
    Triangulation unsafe = new Triangulation();
    algo.splitIntoSafeAndUnsafeGraphs(new Rectangle(0, 0, 1000, 1000), safe, unsafe);
    for (Point[] safeTriangle : safe.iterateTriangles()) {
      // Check that the triangle is in the list of allTriangles
      boolean found = false;
      int i = 0;
      while (!found && i < allTriangles.size()) {
        found = arrayEqualAnyOrder(safeTriangle, allTriangles.get(i));
        if (found)
          allTriangles.remove(i);
        else
          i++;
      }

      assertTrue(String.format("Safe triangle (%d, %d, %d) not found",
          Arrays.binarySearch(points, safeTriangle[0]),
          Arrays.binarySearch(points, safeTriangle[1]),
          Arrays.binarySearch(points, safeTriangle[2])), found);
    }
    // For unsafe triangles, invert the reportedSites to report all sites that
    // have never been reported before.
    unsafe.sitesToReport = unsafe.reportedSites.invert();
    for (Point[] unsafeTriangle : unsafe.iterateTriangles()) {
      // Check that the triangle is in the list of allTriangles
      boolean found = false;
      int i = 0;
      while (!found && i < allTriangles.size()) {
        found = arrayEqualAnyOrder(unsafeTriangle, allTriangles.get(i));
        if (found)
          allTriangles.remove(i);
        else
          i++;
      }

      assertTrue(String.format("Unsafe triangle (%d, %d, %d) not found",
          Arrays.binarySearch(points, unsafeTriangle[0]),
          Arrays.binarySearch(points, unsafeTriangle[1]),
          Arrays.binarySearch(points, unsafeTriangle[2])), found);
    }
    for (Point[] triangle : allTriangles) {
      System.out.printf("Triangle not found (%d, %d, %d)\n",
          Arrays.binarySearch(points, triangle[0]),
          Arrays.binarySearch(points, triangle[1]),
          Arrays.binarySearch(points, triangle[2])
      );
    }
    assertTrue(String.format("%d triangles not found", allTriangles.size()),
        allTriangles.isEmpty());
  }

  /**
   * Tests the the calculation of DT in a distributed manner reports the correct
   * triangles.
   */
  public void testMerge() {
    Random random = new Random(0);
    // Generate 100 random points
    Point[] points = new Point[100];
    for (int i = 0; i < points.length; i++)
      points[i] = new Point(random.nextInt(1000), random.nextInt(1000));

    Arrays.sort(points);

    // Compute Delaunay triangulation for the 100 points
    GSDTAlgorithm algo = new GSDTAlgorithm(points, null);
    Triangulation answer = algo.getFinalTriangulation();
    // Retrieve all triangles from the complete answer and use it as a baseline
    List<Point[]> allTriangles = new ArrayList<Point[]>();
    for (Point[] triangle : answer.iterateTriangles()) {
      allTriangles.add(triangle.clone());
    }

    // Split the input set of points vertically, compute each DT separately,
    // then merge them and make sure that we get the same answer
    // Points are already sorted on the x-axis
    Point[] leftHalf = new Point[points.length / 2];
    System.arraycopy(points, 0, leftHalf, 0, leftHalf.length);

    GSDTAlgorithm leftAlgo = new GSDTAlgorithm(leftHalf, null);
    Rectangle leftMBR = new Rectangle(0, 0, (points[leftHalf.length - 1].x + points[leftHalf.length].x) / 2, 1000);
    Triangulation leftSafe = new Triangulation();
    Triangulation leftUnsafe = new Triangulation();
    leftAlgo.splitIntoSafeAndUnsafeGraphs(leftMBR, leftSafe, leftUnsafe);

    // Check all final triangles on the left half
    for (Point[] safeTriangle : leftSafe.iterateTriangles()) {
      // Check that the triangle is in the list of allTriangles
      boolean found = false;
      int i = 0;
      while (!found && i < allTriangles.size()) {
        found = arrayEqualAnyOrder(safeTriangle, allTriangles.get(i));
        if (found)
          allTriangles.remove(i);
        else
          i++;
      }

      assertTrue(String.format("Safe triangle (%d, %d, %d) not found",
          Arrays.binarySearch(points, safeTriangle[0]),
          Arrays.binarySearch(points, safeTriangle[1]),
          Arrays.binarySearch(points, safeTriangle[2])), found);
    }

    // Repeat the same thing for the right half.
    Point[] rightHalf = new Point[points.length - leftHalf.length];
    System.arraycopy(points, leftHalf.length, rightHalf, 0, rightHalf.length);

    // Compute DT for the right half
    GSDTAlgorithm rightAlgo = new GSDTAlgorithm(rightHalf, null);
    Rectangle rightMBR = new Rectangle((points[leftHalf.length - 1].x + points[leftHalf.length].x) / 2, 0, 1000, 1000);
    Triangulation rightSafe = new Triangulation();
    Triangulation rightUnsafe = new Triangulation();
    rightAlgo.splitIntoSafeAndUnsafeGraphs(rightMBR, rightSafe, rightUnsafe);

    // Check all final triangles on the right half
    for (Point[] safeTriangle : rightSafe.iterateTriangles()) {
      // Check that the triangle is in the list of allTriangles
      boolean found = false;
      int i = 0;
      while (!found && i < allTriangles.size()) {
        found = arrayEqualAnyOrder(safeTriangle, allTriangles.get(i));
        if (found)
          allTriangles.remove(i);
        else
          i++;
      }

      assertTrue(String.format("Safe triangle (%d, %d, %d) not found",
          Arrays.binarySearch(points, safeTriangle[0]),
          Arrays.binarySearch(points, safeTriangle[1]),
          Arrays.binarySearch(points, safeTriangle[2])), found);
    }

    // Merge the unsafe parts from the letf and right to finalize
    GSDTAlgorithm mergeAlgo = new GSDTAlgorithm(new Triangulation[] {leftUnsafe, rightUnsafe}, null);
    Triangulation finalPart = mergeAlgo.getFinalTriangulation();

    for (Point[] safeTriangle : finalPart.iterateTriangles()) {
      // Check that the triangle is in the list of allTriangles
      boolean found = false;
      int i = 0;
      while (!found && i < allTriangles.size()) {
        found = arrayEqualAnyOrder(safeTriangle, allTriangles.get(i));
        if (found)
          allTriangles.remove(i);
        else
          i++;
      }

      assertTrue(String.format("Safe triangle (%d, %d, %d) not found",
          Arrays.binarySearch(points, safeTriangle[0]),
          Arrays.binarySearch(points, safeTriangle[1]),
          Arrays.binarySearch(points, safeTriangle[2])), found);
    }

    for (Point[] triangle : allTriangles) {
      System.out.printf("Triangle not found (%d, %d, %d)\n",
          Arrays.binarySearch(points, triangle[0]),
          Arrays.binarySearch(points, triangle[1]),
          Arrays.binarySearch(points, triangle[2])
      );
    }
    assertTrue(String.format("%d triangles not found", allTriangles.size()),
        allTriangles.isEmpty());
  }
}