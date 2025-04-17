/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE LIB

                            FILE CCLAMPINGLINE 
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
package agle.lib.utils;

// Java classes
import java.io.*;
import java.lang.*;
import java.util.*; 

// GL4Java classes
import gl4java.*;
import gl4java.utils.textures.*;

// classes
import agle.lib.*;
import agle.lib.math.*;
import agle.lib.loaders.*;
import agle.lib.physics.*;

///////////////////////////////// CCLAMPINGLINE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// CCLAMPINGLINE  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class CClampingLine
{	
	GLFunc	gl;
	GLUFunc	glu;

	public CVector3 Position;
	
	public CVector3[] GroundLine; 
	public CVector3[] PGroundLine; 
	
	public CVector3 vIntersection;

	///////////////////////////////// CCLAMPINGLINE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	This is the class constructor
	/////
	///////////////////////////////// CCLAMPINGLINE  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public CClampingLine()
	{	
	
		Position = new CVector3();
		
		 GroundLine = new CVector3[2];
		GroundLine[0] = new CVector3();
		GroundLine[1] = new CVector3();
		
		PGroundLine = new CVector3[2];		
		PGroundLine[0] = new CVector3();
		PGroundLine[1] = new CVector3();
		
		vIntersection = new CVector3();
	}
	
	///////////////////////////////// UPDATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Funzione che aggiorna la liena del terreno
	/////
	///////////////////////////////// UPDATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void UpdateClampingLine()
	{
		// Riposiziona la linea  del terreno sotto il giocatore
		PGroundLine[0] = CMath3d.Add(Position, GroundLine[0]);
		PGroundLine[1] = CMath3d.Add(Position, GroundLine[1]);
	}
	
	///////////////////////////////// SETPOSITION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Funzione che aggiorna la posizione della linea
	/////
	///////////////////////////////// SETPOSITION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void SetPosition(CVector3 clPosition)
	{
		Position = clPosition;
	}

	///////////////////////////////// GETCOLLIDINGPOINT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Restituisci il punto di collisione
	/////
	///////////////////////////////// GETCOLLIDINGPOINT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public CVector3 GetCollidingPoint()
	{
		return new CVector3(vIntersection.x,vIntersection.y,vIntersection.z);
	}
		
	///////////////////////////////// CHECK LINE COLLISION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This checks all the polygons in our list and offsets the line if collided
	/////
	///////////////////////////////// CHECK LINE COLLISION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public boolean CheckLineCollision(CVector3[] Vertices, int verticeCount)
	{
		CVector3 vNormal = new CVector3();
		float originDistance = 0;

		// First we check to see if our line intersected the plane.  If this isn't true
		// there is no need to go on, so return false immediately.
		// We pass in address of vNormal and originDistance so we only calculate it once

		if(!CMath3d.IntersectedPlane(Vertices, PGroundLine))
			return false;

		// Java Port Note: Sorry, Java doesn't allow pass backs :( We'll need to calculate these.
		vNormal = CMath3d.Normal(Vertices);
		originDistance = CMath3d.PlaneDistance(vNormal, Vertices[0]);

		// Now that we have our normal and distance passed back from IntersectedPlane(), 
		// we can use it to calculate the intersection point.  The intersection point
		// is the point that actually is ON the plane.  It is between the line.  We need
		// this point test next, if we are inside the polygon.  To get the I-Point, we
		// give our function the normal of the plan, the points of the line, and the originDistance.

		vIntersection = CMath3d.IntersectionPoint(vNormal, PGroundLine, originDistance);

		// Now that we have the intersection point, we need to test if it's inside the polygon.
		// To do this, we pass in :
		// (our intersection point, the polygon, and the number of vertices our polygon has)

		if(CMath3d.InsidePolygon(vIntersection, Vertices, verticeCount))
			return true;							// We collided!	  Return success

		// If we get here, we must have NOT collided

		return false;								// There was no collision, so return false
	}

}