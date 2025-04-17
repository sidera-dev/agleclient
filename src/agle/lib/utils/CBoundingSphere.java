/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE LIB

                            FILE CBOUNDINGSPHERE 
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.lib.utils;

// Java classes
import java.io.*;
import java.lang.*;
import java.util.*; 

// GL4Java classes
import gl4java.*;
import gl4java.utils.textures.*;
import gl4java.GLContext;
import gl4java.awt.GLAnimCanvas;

// classes
import agle.lib.*;
import agle.lib.math.*;
import agle.lib.loaders.*;
import agle.lib.physics.*;

///////////////////////////////// CBOUNDINGSPHERE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// CBOUNDINGSPHERE  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class CBoundingSphere
{	
	public CVector3 Position;
	public CVector3 Velocity;
	public float	Radius;
	public CVector3 vOffset; 

	public CBoundingSphere()
	{				
		Position =		new CVector3(0.0f,0.0f,0.0f);
		Velocity =		new CVector3(0.0f,0.0f,0.0f);
		
		Radius =		0.0f;
		vOffset =		new CVector3(0.0f,0.0f,0.0f);
	}
	
	///////////////////////////////// UPDATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Posiziona la sfera in relazione al mondo
	/////
	///////////////////////////////// UPDATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void Update(CVector3 sPosition)
	{
		Position = sPosition;
		
	}	
	///////////////////////////////// SetPosition \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Posiziona la sfera in relazione al mondo
	/////
	///////////////////////////////// SetPosition \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void SetPosition(CVector3 sPosition)
	{
		Position = sPosition;
	}
	
	///////////////////////////////// SetRadius \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Imposta il raggio
	/////
	///////////////////////////////// SetRadius \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void SetRadius(float sRadius)
	{
		Radius = sRadius;
	}
	
	///////////////////////////////// Radius \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Rileva il raggio
	/////
	///////////////////////////////// SetRadius \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public float Radius()
	{
		return Radius;
	}
	
	///////////////////////////////// SetOffset \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Quanto e' l'offset?
	/////
	///////////////////////////////// SetOffset\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public CVector3 vOffset()
	{
		return vOffset;
	}
	
	///////////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Render the sphere
	/////
	///////////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void RenderBoundingSphere( GLFunc gl, GLUFunc glu)
	{
		gl.glPushMatrix();
		gl.glTranslatef(Position.x, Position.y, Position.z);
		// Allocate a quadric object to use as a sphere
		long pObj = glu.gluNewQuadric();						// Get a Quadric off the stack
		// To make it easier to see, we want the sphere to be in wire frame
		glu.gluQuadricDrawStyle(pObj, glu.GLU_LINE);				// Draw the sphere normally
		// Draw the quadric as a sphere with the radius of .1 and a 15 by 15 detail.
		// To increase the detail of the sphere, just increase the 2 last parameters.
		glu.gluSphere(pObj, Radius, 15, 15);
		glu.gluDeleteQuadric(pObj);								// Free the Quadric
		gl.glPopMatrix();
	}
	
	///////////////////////////////// CHECK SPHERE COLLISION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This checks all the polygons in our list and offsets the sphere if collided
	/////
	///////////////////////////////// CHECK SPHERE COLLISION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public boolean CheckSphereCollision(CVector3[] pVertices, int numOfVerts)
	{	
		// This function is pretty much a direct rip off of SpherePolygonCollision()
		// We needed to tweak it a bit though, to handle the collision detection once 
		// it was found, along with checking every triangle in the list if we collided.  
		// pVertices is the world data. If we have space partitioning, we would pass in 
		// the vertices that were closest to the camera. What happens in this function 
		// is that we go through every triangle in the list and check if the camera's 
		// sphere collided with it.  If it did, we don't stop there.  We can have 
		// multiple collisions so it's important to check them all.  One a collision 
		// is found, we calculate the offset to move the sphere off of the collided plane.

		// Go through all the triangles
		for(int i = 0; i < numOfVerts; i += 3)
		{
			// Store of the current triangle we testing
			CVector3[] vTriangle = { pVertices[i], pVertices[i+1], pVertices[i+2] };

			// 1) STEP ONE - Finding the sphere's classification
		
			// We want the normal to the current polygon being checked
			CVector3 vNormal = CMath3d.Normal(vTriangle);

			// This will store the distance our sphere is from the plane
			float distance = 0.0f;

			// This is where we determine if the sphere is in FRONT, BEHIND, or INTERSECTS the plane
			int classification = CMath3d.ClassifySphere(Position, vNormal, vTriangle[0], Radius, distance);

			// Java Port Note: We must calc the distance here.
			float d = (float)CMath3d.PlaneDistance(vNormal, vTriangle[0]);
			distance = (vNormal.x * Position.x + vNormal.y * Position.y + vNormal.z * Position.z + d);

			// If the sphere intersects the polygon's plane, then we need to check further
			if(classification == CMath3d.INTERSECTS) 
			{
				// 2) STEP TWO - Finding the psuedo intersection point on the plane

				// Now we want to project the sphere's center onto the triangle's plane
				vOffset = CMath3d.Multiply(vNormal , distance);

				// Once we have the offset to the plane, we just subtract it from the center
				// of the sphere.  "vIntersection" is now a point that lies on the plane of the triangle.
				CVector3 vIntersection = CMath3d.Subtract(Position , vOffset);

				// 3) STEP THREE - Check if the intersection point is inside the triangles perimeter

				// We first check if our intersection point is inside the triangle, if not,
				// the algorithm goes to step 4 where we check the sphere again the polygon's edges.

				// We do one thing different in the parameters for EdgeSphereCollision though.
				// Since we have a bulky sphere for our camera, it makes it so that we have to 
				// go an extra distance to pass around a corner. This is because the edges of 
				// the polygons are colliding with our peripheral view (the sides of the sphere).  
				// So it looks likes we should be able to go forward, but we are stuck and considered 
				// to be colliding.  To fix this, we just pass in the radius / 2.  Remember, this
				// is only for the check of the polygon's edges.  It just makes it look a bit more
				// realistic when colliding around corners.  Ideally, if we were using bounding box 
				// collision, cylinder or ellipses, this wouldn't really be a problem.

				if(CMath3d.InsidePolygon(vIntersection, vTriangle, 3) ||
				   CMath3d.EdgeSphereCollision(Position, vTriangle, 3, Radius / 2f))
				{
					// If we get here, we have collided!  To handle the collision detection
					// all it takes is to find how far we need to push the sphere back.
					// GetCollisionOffset() returns us that offset according to the normal,
					// radius, and current distance the center of the sphere is from the plane.
					vOffset = CMath3d.GetCollisionOffset(vNormal, Radius, distance);

					// Now that we have the offset, we want to ADD it to the position and
					// view vector in our camera.  This pushes us back off of the plane.  We
					// don't see this happening because we check collision before we render
					// the scene.
					return true;
				}
			}
		}
	// Niente collisione
	return false;
	}
	
	///////////////////////////////// CHECK SPHERE TO SPHERE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Questa funzione guarda se due sfere collidono
	/////
	///////////////////////////////// CHECK SPHERE TO SPHERE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public boolean CheckSpheretoSphere(CBoundingSphere sph)
	{
		// Relative velocity
		CVector3    dv    = CMath3d.Subtract(Velocity,sph.Velocity);
		// Relative position
		CVector3    dp    = CMath3d.Subtract(Position,sph.Position);
		//Minimal distance squared
		float r = Radius + sph.Radius;
		//dP^2-r^2
		float pp = dp.x * dp.x + dp.y * dp.y + dp.z * dp.z - r*r;
		//(1)Check if the spheres are already intersecting
		if ( pp < 0 ){
			return true;
		}
		//dP*dV
		float pv = dp.x * dv.x + dp.y * dv.y + dp.z * dv.z;
		//(2)Check if the spheres are moving away from each other
		if ( pv >= 0 ){
			return false;
		}
		//dV^2
		float vv = dv.x * dv.x + dv.y * dv.y + dv.z * dv.z;
		//(3)Check if the spheres can intersect within 1 frame
		if ( (pv + vv) <= 0 && (vv + 2 * pv + pp) >= 0 ){
			return false;
		}
		return false;
	}
	
}