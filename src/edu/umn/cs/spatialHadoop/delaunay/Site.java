/***********************************************************************
* Copyright (c) 2015 by Regents of the University of Minnesota.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License, Version 2.0 which 
* accompanies this distribution and is available at
* http://www.opensource.org/licenses/apache2.0.php.
*
*************************************************************************/
package edu.umn.cs.spatialHadoop.delaunay;

import java.util.List;
import java.util.Vector;

/**
 * A site in Delaunay triangulation
 * @author Ahmed Eldawy
 *
 */
public class Site {
  /**Coordinates of the point*/
  double x, y;
  
  /**All neighboring sites. A neighbor site has a common edge in the 
   * Delaunay triangulation*/
  List<Site> neighbors = new Vector<Site>();
  
  /**All triangles where this site is involved*/
  List<Triangle> triangles = new Vector<Triangle>();
  
  Site(double x, double y) {
    this.x = x;
    this.y = y;
  }
}
