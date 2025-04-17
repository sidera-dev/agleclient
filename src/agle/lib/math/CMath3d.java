/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                     AGLE agle.lib

                            FILE Math3d
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.lib.math;

// Java classes
import java.lang.*;
import java.util.*;

  
///////////////////////////////// CMath3d \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	This is the class constructor
/////
///////////////////////////////// CMath3d \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
public abstract class CMath3d
{
	
	public static int BEHIND	 = 0;	// This is returned if the sphere is completely behind the plane
	public static int INTERSECTS = 1;	// This is returned if the sphere intersects the plane
	public static int FRONT		 = 2;	// This is returned if the sphere is completely in front of the plane
	
	///////////////////////////////// CROSS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns a perpendicular vector from 2 given vectors by taking the cross product.
	/////
	///////////////////////////////// CROSS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static CVector3 Cross(CVector3 vVector1, CVector3 vVector2)
	{
		CVector3 vNormal = new CVector3();			// The vector to hold the cross product
		
		// The X value for the vector is:  (V1.y * V2.z) - (V1.z * V2.y)	// Get the X value
		vNormal.x = ((vVector1.y * vVector2.z) - (vVector1.z * vVector2.y));
															
		// The Y value for the vector is:  (V1.z * V2.x) - (V1.x * V2.z)
		vNormal.y = ((vVector1.z * vVector2.x) - (vVector1.x * vVector2.z));
															
		// The Z value for the vector is:  (V1.x * V2.y) - (V1.y * V2.x)
		vNormal.z = ((vVector1.x * vVector2.y) - (vVector1.y * vVector2.x));

		// Return the cross product
		return vNormal;										 
	}
	
	/////////////////////////////////////// VECTOR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns a vector between 2 points
	/////
	/////////////////////////////////////// VECTOR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static CVector3 Vector(CVector3 vPoint1, CVector3 vPoint2)
	{
		CVector3 vVector = new CVector3();					// Initialize our variable to zero

		// In order to get a vector from 2 points (a direction) we need to
		// subtract the second point from the first point.

		vVector.x = vPoint1.x - vPoint2.x;					// Get the X value of our new vector
		vVector.y = vPoint1.y - vPoint2.y;					// Get the Y value of our new vector
		vVector.z = vPoint1.z - vPoint2.z;					// Get the Z value of our new vector

		// Now that we have our new vector between the 2 points, we will return it.

		return vVector;										// Return our new vector
	}

	//////////////////////////////// MAGNITUDE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns the magnitude of a vector
	/////
	//////////////////////////////// MAGNITUDE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static float Magnitude(CVector3 vNormal)
	{
		return (float)Math.sqrt( (vNormal.x * vNormal.x) + 
								 (vNormal.y * vNormal.y) + 
								 (vNormal.z * vNormal.z) );
	}

	/////////////////////////////// NORMALIZE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns a normalize vector (A vector exactly of length 1)
	/////
	/////////////////////////////// NORMALIZE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static CVector3 Normalize(CVector3 vVector)
	{
		// Get the magnitude of our normal
		float magnitude = Magnitude(vVector);				

		vVector.x /= magnitude;		
		vVector.y /= magnitude;		
		vVector.z /= magnitude;		
		
		return vVector;
	}
	
	/////////////////////////////////// ADD \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Varie operazioni elementari fra vettori
	/////
	////////////////////////////////// ADD \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static CVector3 Add(CVector3 v1, CVector3 v2)
	{
		return new CVector3(v1.x+v2.x, v1.y+v2.y, v1.z+v2.z);
	}
	
	////////////////////////////////// SUBTRACT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Varie operazioni elementari fra vettori
	/////
	////////////////////////////////// SUBTRACT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static CVector3 Subtract(CVector3 v1, CVector3 v2)
	{
		return new CVector3(v1.x-v2.x, v1.y-v2.y, v1.z-v2.z);
	}
	
	////////////////////////////////// MULTIPLY \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Varie operazioni elementari fra vettori
	/////
	////////////////////////////////// MULTIPLY \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static CVector3 Multiply(CVector3 v1, float m)
	{
		return new CVector3(v1.x*m, v1.y*m, v1.z*m);
	}
	
	/////////////////////////////////// DIVIDE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Varie operazioni elementari fra vettori
	/////
	/////////////////////////////////// DIVIDE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static CVector3 Divide(CVector3 v1, float m)
	{
		return Multiply(v1, 1.0f/m);
	}

	/////////////////////////////////// NEGATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Varie operazioni elementari fra vettori
	/////
	/////////////////////////////////// NEGATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static CVector3 Negate(CVector3 v)	// operator- is used to set this Vector3D's x, y, and z to the negative of them.
	{
		return new CVector3(-v.x, -v.y, -v.z);
	}
	
	/////////////////////////////////////// ABSOLUTE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns the absolute value of the number passed in
	/////
	/////////////////////////////////////// ABSOLUTE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static float Absolute(float num)
	{
		// If num is less than zero, we want to return the absolute value of num.
		// This is simple, either we times num by -1 or subtract it from 0.
		if(num < 0)
			return (0 - num);

		// Return the original number because it was already positive
		return num;
	}
	
	/////////////////////////////////////// NORMAL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns the normal of a polygon (The direction the polygon is facing)
	/////
	/////////////////////////////////////// NORMAL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static CVector3 Normal(CVector3 vTriangle[])		// You might want to error check to make sure it's valid
	{														// Get 2 vectors from the polygon
		CVector3 vVector1 = Vector(vTriangle[2], vTriangle[0]);
		CVector3 vVector2 = Vector(vTriangle[1], vTriangle[0]);

		// We Chose to get the vectors surrounding the first point of the polygon.
		// We could have chosen to get 2 other sides of the triangle, but we chose these 2.
		// Now that we have the 2 side vectors, we will take their cross product.
		// (*NOTE*) It is important that pass in the vector of the bottom side of the triangle
		// first, and then pass in the vector of the left side second.  If we switch them,
		// it will turn the normal the opposite way.  Try it, switch them like this: Cross(vVector2, vVector1);
		// Like I said before, it's important to ALWAYS work in the same direction.  In our case,
		// we chose that we always work counter-clockwise.

		CVector3 vNormal = Cross(vVector1, vVector2);

		// Now that we have the direction of the normal, we want to do one last thing.
		// Right now, it's an unknown length, it is probably pretty long in length.
		// We want to do something which gives the normal a length of 1.  This is called
		// normalizing.  To do this we divide the normal by it's magnitude.  Well how do we
		// find it's magnitude? We use this equation: magnitude = sqrt(x^2 + y^2 + z^2)

		vNormal = Normalize(vNormal);						// Use our function we created to normalize the normal (Makes it a length of one)

		// Now return the normalized normal
		// (*NOTE*) If you want to understand what normalizing our normal does, comment out
		// the above line.  Then you can see how long the normal is before we normalize it.
		// I strongly recommend this.  And remember, it doesn't matter how long the normal is,
		// (of course besides (0, 0, 0)), if we normalize it, it will always be of length 1 afterwards.

		return vNormal;										// Return our normal at our desired length
	}
	
	/////////////////////////////////// PLANE DISTANCE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns the distance between a plane and the origin
	/////
	/////////////////////////////////// PLANE DISTANCE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static float PlaneDistance(CVector3 Normal, CVector3 Point)
	{	
		float distance = 0;									// This variable holds the distance from the plane tot he origin

		// Use the plane equation to find the distance (Ax + By + Cz + D = 0)  We want to find D.
		// For more information about the plane equation, read about it in the function below (IntersectedPlane())
		// Basically, A B C is the X Y Z value of our normal and the x y z is our x y z of our point.
		// D is the distance from the origin.  So, we need to move this equation around to find D.
		// We come up with D = -(Ax + By + Cz)
															// Basically, the negated dot product of the normal of the plane and the point. (More about the dot product in another tutorial)
		distance = - ((Normal.x * Point.x) + (Normal.y * Point.y) + (Normal.z * Point.z));

		return distance;									// Return the distance
	}

	/////////////////////////////////// INTERSECTED PLANE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This checks to see if a line intersects a plane
	/////
	/////////////////////////////////// INTERSECTED PLANE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static boolean IntersectedPlane(CVector3 vTriangle[], CVector3 vLine[])
	{
		float distance1=0;
		float distance2=0;			// The distances from the 2 points of the line from the plane
				
		CVector3 vNormal = Normal(vTriangle);	// We need to get the normal of our plane to go any further

		// Now that we have the normal, we need to calculate the distance our triangle is from the origin.
		// Since we would have the same triangle, but -10 down the z axis, we need to know
		// how far our plane is to the origin.  The origin is (0, 0, 0), so we need to find
		// the shortest distance our plane is from (0, 0, 0).  This way we can test the collision.
		// The direction the plane is facing is important (We know this by the normal), but it's
		// also important WHERE that plane is in our 3D space.  I hope this makes sense.

		// We created a function to calculate the distance for us.  All we need is the normal
		// of the plane, and then ANY point located on that plane.  Well, we have 3 points.  Each
		// point of the triangle is on the plane, so we just pass in one of our points.  It doesn't
		// matter which one, so we will just pass in the first one.  We get a single value back.
		// That is the distance.  Just like our normalized normal is of length 1, our distance
		// is a single value too.  It's like if you were to measure something with a ruler,
		// you don't measure it according to the X Y and Z of our world, you just want ONE number.

		float originDistance = PlaneDistance(vNormal, vTriangle[0]);

		// Now the next step is simple, but hard to understand at first.  What we need to
		// do is get the distance of EACH point from out plane.  Above we got the distance of the
		// plane to the point (0, 0, 0) which happens to be the origin, now we need to get a distance
		// for each point.  If the distance is a negative number, then the point is BEHIND the plane.
		// If the distance is positive, then the point is in front of the plane.  Basically, if the
		// line collides with the plane, there should be a negative and positive distance.  make sense?
		// If the line pierces the plane, it will have a negative distance and a positive distance,
		// meaning that a point will be on one side of the plane, and one point on the other.  But we
		// will do the check after this, first we need to get the distance of each point to the plane.

		// Now, we need to use something called the plane equation to get the distance from each point.
		// Here is the plane Equation:  (Ax + By + Cz + D = The distance from the plane)
		// If "The distance from the plane" is 0, that means that the point is ON the plane, which all the polygon points should be.
		// A, B and C is the Normal's X Y and Z values.  x y and z is the Point's x y and z values.
		// "the Point" meaning one of the points of our line.  D is the distance that the plane
		// is from the origin.  We just calculated that and stored it in "originDistance".
		// Let's fill in the equation with our data:

		// Get the distance from point1 from the plane using: Ax + By + Cz + D = (The distance from the plane)

		distance1 = ((vNormal.x * vLine[0].x)  +					// Ax +
					 (vNormal.y * vLine[0].y)  +					// Bx +
					 (vNormal.z * vLine[0].z)) + originDistance;	// Cz + D

		// We just got the first distance from the first point to the plane, now let's get the second.
		
		// Get the distance from point2 from the plane using Ax + By + Cz + D = (The distance from the plane)
		
		distance2 = ((vNormal.x * vLine[1].x)  +					// Ax +
					 (vNormal.y * vLine[1].y)  +					// Bx +
					 (vNormal.z * vLine[1].z)) + originDistance;	// Cz + D

		// Ok, we should have 2 distances from the plane, from each point of our line.
		// Remember what I said about an intersection?  If one is negative and one is positive,
		// that means that they are both on either side of the plane.  So, all we need to do
		// is multiply the 2 distances together, and if the result is less than 0, we intersection.
		// This works because, any number times a negative number is always negative, IE (-1 * 1 = -1)
		// If they are both positive or negative values then it will be above zero.

		if(distance1 * distance2 >= 0)			// Check to see if both point's distances are both negative or both positive
		   return false;						// Return false if each point has the same sign.  -1 and 1 would mean each point is on either side of the plane.  -1 -2 or 3 4 wouldn't...
						
		return true;							// The line intersected the plane, Return TRUE
	}
	
	/////////////////////////////////// DOT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This computers the dot product of 2 vectors
	/////
	/////////////////////////////////// DOT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static float Dot(CVector3 vVector1, CVector3 vVector2) 
	{
		// The dot product is this equation: V1.V2 = (V1.x * V2.x  +  V1.y * V2.y  +  V1.z * V2.z)
		// In math terms, it looks like this:  V1.V2 = ||V1|| ||V2|| cos(theta)
		// The '.' means DOT.   The || || is magnitude.  So the magnitude of V1 times the magnitude
		// of V2 times the cosine of the angle.  It seems confusing now, but it will become more clear.
		// This function is used for a ton of things, which we will cover in other tutorials.
		// For this tutorial, we use it to compute the angle between 2 vectors.  If the vectors
		// are normalize, the dot product returns the cosine of the angle between the 2 vectors.
		// What does that mean? Well, it doesn't return the actual angle, it returns the value of:
		// cos(angle).	Well, what if we want to get the actual angle?  Then we use the arc cosine.
		// There is more on this in the below function AngleBetweenVectors().  Let's give some
		// applications of using the dot product.  How would you tell if the angle between the
		// 2 vectors is perpendicular (90 degrees)?  Well, if we normalize the vectors we can
		// get rid of the ||V1|| * ||V2|| in front, which just leaves us with:  cos(theta).
		// If a vector is normalize, it's magnitude is 1, so it would be: 1 * 1 * cos(theta) , 
		// which is pointless, so we discard that part of the equation.  So, What is the cosine of 90?
		// If you punch it in your calculator you will find that it's 0.  So that means
		// if the dot product of 2 angles is 0, then they are perpendicular.  What we did in
		// our mind is take the arc cosine of 0, which is 90 (or PI/2 in radians).  More on this below.

				 //    (V1.x * V2.x        +        V1.y * V2.y        +        V1.z * V2.z)
		return ( (vVector1.x * vVector2.x) + (vVector1.y * vVector2.y) + (vVector1.z * vVector2.z) );
	}

	/////////////////////////////////// ANGLE BETWEEN VECTORS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This checks to see if a point is inside the ranges of a polygon
	/////
	/////////////////////////////////// ANGLE BETWEEN VECTORS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static double AngleBetweenVectors(CVector3 Vector1, CVector3 Vector2)
	{							
		// Remember, above we said that the Dot Product of returns the cosine of the angle
		// between 2 vectors?  Well, that is assuming they are unit vectors (normalize vectors).
		// So, if we don't have a unit vector, then instead of just saying  arcCos(DotProduct(A, B))
		// We need to divide the dot product by the magnitude of the 2 vectors multiplied by each other.
		// Here is the equation:   arc cosine of (V . W / || V || * || W || )
		// the || V || means the magnitude of V.  This then cancels out the magnitudes dot product magnitudes.
		// But basically, if you have normalize vectors already, you can forget about the magnitude part.

		// Get the dot product of the vectors
		float dotProduct = Dot(Vector1, Vector2);				

		// Get the product of both of the vectors magnitudes
		float vectorsMagnitude = Magnitude(Vector1) * Magnitude(Vector2) ;

		// Get the arc cosine of the (dotProduct / vectorsMagnitude) which is the angle in RADIANS.
		// (IE.   PI/2 radians = 90 degrees      PI radians = 180 degrees    2*PI radians = 360 degrees)
		// To convert radians to degress use this equation:   radians * (PI / 180)
		// TO convert degrees to radians use this equation:   degrees * (180 / PI)
		double angle = Math.acos( dotProduct / vectorsMagnitude );

		// Here we make sure that the angle is not a -1.#IND0000000 number, which means indefinate.
		// acos() thinks it's funny when it returns -1.#IND0000000.  If we don't do this check,
		// our collision results will sometimes say we are colliding when we aren't.  I found this
		// out the hard way after MANY hours and already wrong written tutorials :)  Usually
		// this value is found when the dot product and the maginitude are the same value.
		// We want to return 0 when this happens.

		if(angle == Double.NaN || angle == Double.NEGATIVE_INFINITY || angle == Double.POSITIVE_INFINITY)
			return 0;
		
		// Return the angle in radians
		return( angle );
	}


	/////////////////////////////////// INTERSECTION POINT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns the intersection point of the line that intersects the plane
	/////
	/////////////////////////////////// INTERSECTION POINT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static CVector3 IntersectionPoint(CVector3 vNormal, CVector3 vLine[], double distance)
	{
		CVector3 vPoint = new CVector3(), vLineDir = new CVector3();		// Variables to hold the point and the line's direction
		double Numerator = 0.0, Denominator = 0.0, dist = 0.0;

		// Here comes the confusing part.  We need to find the 3D point that is actually
		// on the plane.  Here are some steps to do that:
		
		// 1)  First we need to get the vector of our line, Then normalize it so it's a length of 1
		vLineDir = Vector(vLine[1], vLine[0]);		// Get the Vector of the line
		vLineDir = Normalize(vLineDir);				// Normalize the lines vector


		// 2) Use the plane equation (distance = Ax + By + Cz + D) to find the distance from one of our points to the plane.
		//    Here I just chose a arbitrary point as the point to find that distance.  You notice we negate that
		//    distance.  We negate the distance because we want to eventually go BACKWARDS from our point to the plane.
		//    By doing this is will basically bring us back to the plane to find our intersection point.
		Numerator = - (vNormal.x * vLine[0].x +		// Use the plane equation with the normal and the line
					   vNormal.y * vLine[0].y +
					   vNormal.z * vLine[0].z + distance);

		// 3) If we take the dot product between our line vector and the normal of the polygon,
		//    this will give us the cosine of the angle between the 2 (since they are both normalized - length 1).
		//    We will then divide our Numerator by this value to find the offset towards the plane from our arbitrary point.
		Denominator = Dot(vNormal, vLineDir);		// Get the dot product of the line's vector and the normal of the plane
					  
		// Since we are using division, we need to make sure we don't get a divide by zero error
		// If we do get a 0, that means that there are INFINATE points because the the line is
		// on the plane (the normal is perpendicular to the line - (Normal.Vector = 0)).  
		// In this case, we should just return any point on the line.

		if( Denominator == 0.0)						// Check so we don't divide by zero
			return vLine[0];						// Return an arbitrary point on the line

		// We divide the (distance from the point to the plane) by (the dot product)
		// to get the distance (dist) that we need to move from our arbitrary point.  We need
		// to then times this distance (dist) by our line's vector (direction).  When you times
		// a scalar (single number) by a vector you move along that vector.  That is what we are
		// doing.  We are moving from our arbitrary point we chose from the line BACK to the plane
		// along the lines vector.  It seems logical to just get the numerator, which is the distance
		// from the point to the line, and then just move back that much along the line's vector.
		// Well, the distance from the plane means the SHORTEST distance.  What about in the case that
		// the line is almost parallel with the polygon, but doesn't actually intersect it until half
		// way down the line's length.  The distance from the plane is short, but the distance from
		// the actual intersection point is pretty long.  If we divide the distance by the dot product
		// of our line vector and the normal of the plane, we get the correct length.  Cool huh?

		dist = Numerator / Denominator;				// Divide to get the multiplying (percentage) factor
		
		// Now, like we said above, we times the dist by the vector, then add our arbitrary point.
		// This essentially moves the point along the vector to a certain distance.  This now gives
		// us the intersection point.  Yay!

		vPoint.x = (float)(vLine[0].x + (vLineDir.x * dist));
		vPoint.y = (float)(vLine[0].y + (vLineDir.y * dist));
		vPoint.z = (float)(vLine[0].z + (vLineDir.z * dist));

		return vPoint;								// Return the intersection point
	}


	/////////////////////////////////// INSIDE POLYGON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This checks to see if a point is inside the ranges of a polygon
	/////
	/////////////////////////////////// INSIDE POLYGON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static boolean InsidePolygon(CVector3 vIntersection, CVector3 Poly[], long verticeCount)
	{
		double MATCH_FACTOR = 0.9999;		// Used to cover up the error in floating point
		double Angle = 0.0;						// Initialize the angle
		CVector3 vA, vB;						// Create temp vectors
		
		// Just because we intersected the plane, doesn't mean we were anywhere near the polygon.
		// This functions checks our intersection point to make sure it is inside of the polygon.
		// This is another tough function to grasp at first, but let me try and explain.
		// It's a brilliant method really, what it does is create triangles within the polygon
		// from the intersection point.  It then adds up the inner angle of each of those triangles.
		// If the angles together add up to 360 degrees (or 2 * PI in radians) then we are inside!
		// If the angle is under that value, we must be outside of polygon.  To further
		// understand why this works, take a pencil and draw a perfect triangle.  Draw a dot in
		// the middle of the triangle.  Now, from that dot, draw a line to each of the vertices.
		// Now, we have 3 triangles within that triangle right?  Now, we know that if we add up
		// all of the angles in a triangle we get 360 right?  Well, that is kinda what we are doing,
		// but the inverse of that.  Say your triangle is an isosceles triangle, so add up the angles
		// and you will get 360 degree angles.  90 + 90 + 90 is 360.

		for (int i = 0; i < verticeCount; i++)		// Go in a circle to each vertex and get the angle between
		{	
			vA = Vector(Poly[i], vIntersection);	// Subtract the intersection point from the current vertex
													// Subtract the point from the next vertex
			vB = Vector(Poly[(int)((i + 1) % verticeCount)], vIntersection);
													
			Angle += AngleBetweenVectors(vA, vB);	// Find the angle between the 2 vectors and add them all up as we go along
		}

		// Now that we have the total angles added up, we need to check if they add up to 360 degrees.
		// Since we are using the dot product, we are working in radians, so we check if the angles
		// equals 2*PI.  We defined PI in 3DMath.h.  You will notice that we use a MATCH_FACTOR
		// in conjunction with our desired degree.  This is because of the inaccuracy when working
		// with floating point numbers.  It usually won't always be perfectly 2 * PI, so we need
		// to use a little twiddling.  I use .9999, but you can change this to fit your own desired accuracy.
													
		if(Angle >= (MATCH_FACTOR * (2.0 * Math.PI)) )	// If the angle is greater than 2 PI, (360 degrees)
			return true;								// The point is inside of the polygon
			
		return false;									// If you get here, it obviously wasn't inside the polygon, so Return FALSE
	}


	/////////////////////////////////// INTERSECTED POLYGON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This checks if a line is intersecting a polygon
	/////
	/////////////////////////////////// INTERSECTED POLYGON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static boolean IntersectedPolygon(CVector3 vPoly[], CVector3 vLine[], int verticeCount)
	{
		CVector3 vNormal = new CVector3();
		float originDistance = 0;

		// First we check to see if our line intersected the plane.  If this isn't true
		// there is no need to go on, so return false immediately.
		// We pass in address of vNormal and originDistance so we only calculate it once

		if(!IntersectedPlane(vPoly, vLine))
			return false;

		// Java Port Note: Sorry, Java doesn't allow pass backs :( We'll need to calculate these.
		vNormal = Normal(vPoly);
		originDistance = PlaneDistance(vNormal, vPoly[0]);

		// Now that we have our normal and distance passed back from IntersectedPlane(), 
		// we can use it to calculate the intersection point.  The intersection point
		// is the point that actually is ON the plane.  It is between the line.  We need
		// this point test next, if we are inside the polygon.  To get the I-Point, we
		// give our function the normal of the plan, the points of the line, and the originDistance.

		CVector3 vIntersection = IntersectionPoint(vNormal, vLine, originDistance);

		// Now that we have the intersection point, we need to test if it's inside the polygon.
		// To do this, we pass in :
		// (our intersection point, the polygon, and the number of vertices our polygon has)

		if(InsidePolygon(vIntersection, vPoly, verticeCount))
			return true;							// We collided!	  Return success


		// If we get here, we must have NOT collided

		return false;								// There was no collision, so return false
	}
	
	/////////////////////////////////// DISTANCE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns the distance between 2 3D points
	/////
	/////////////////////////////////// DISTANCE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static float Distance(CVector3 vPoint1, CVector3 vPoint2)
	{
		// This is the classic formula used in beginning algebra to return the
		// distance between 2 points.  Since it's 3D, we just add the z dimension:
		// 
		// Distance = sqrt(  (P2.x - P1.x)^2 + (P2.y - P1.y)^2 + (P2.z - P1.z)^2 )
		//
		double distance = Math.sqrt( (vPoint2.x - vPoint1.x) * (vPoint2.x - vPoint1.x) +
									 (vPoint2.y - vPoint1.y) * (vPoint2.y - vPoint1.y) +
									 (vPoint2.z - vPoint1.z) * (vPoint2.z - vPoint1.z) );

		// Return the distance between the 2 points
		return (float)distance;
	}


	////////////////////////////// CLOSET POINT ON LINE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns the point on the line vA_vB that is closest to the point vPoint
	/////
	////////////////////////////// CLOSET POINT ON LINE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static CVector3 ClosestPointOnLine(CVector3 vA, CVector3 vB, CVector3 vPoint)
	{		
		// Create the vector from end point vA to our point vPoint.
		CVector3 vVector1 = Subtract(vPoint , vA);

		// Create a normalized direction vector from end point vA to end point vB
		CVector3 vVector2 = Normalize(Subtract(vB , vA));

		// Use the distance formula to find the distance of the line segment (or magnitude)
		float d = Distance(vA, vB);

		// Using the dot product, we project the vVector1 onto the vector vVector2.
		// This essentially gives us the distance from our projected vector from vA.
		float t = Dot(vVector2, vVector1);

		// If our projected distance from vA, "t", is less than or equal to 0, it must
		// be closest to the end point vA.  We want to return this end point.
		if (t <= 0) 
			return vA;

		// If our projected distance from vA, "t", is greater than or equal to the magnitude
		// or distance of the line segment, it must be closest to the end point vB.  So, return vB.
		if (t >= d) 
			return vB;
	 
		// Here we create a vector that is of length t and in the direction of vVector2
		CVector3 vVector3 = Multiply(vVector2 , t);

		// To find the closest point on the line segment, we just add vVector3 to the original
		// end point vA.  
		CVector3 vClosestPoint = Add(vA , vVector3);

		// Return the closest point on the line segment
		return vClosestPoint;
	}

	////////////////////////////// SPHERE POLYGON COLLISION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns true if our sphere collides with the polygon passed in
	/////
	////////////////////////////// SPHERE POLYGON COLLISION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static boolean SpherePolygonCollision(CVector3 vPolygon[], 
								CVector3 vCenter, int vertexCount, float radius)
	{
		// This function is the only function we need to call for testing if a sphere
		// collides with a polygon.  The rest are just helper functions called within here.
		// The theory is actually quite difficult to understand, especially if you are
		// a beginner to collision detection and are following the tutorials in order, but
		// I will try to be as gentle and descriptive as possible.  Let go!
		// Basically, here is the overview:  
		//
		// 1) First you want to check if the sphere collides with the polygon's plane.
		//    Remember, that planes are infinite and you could be 500 units from the
		//    polygon and it's still going to trigger this first test.  We want to 
		//    write a function that classifies the sphere.  Either it's completely 
		//    in front of the plane (the side the normal is on), intersecting the
		//    plane or completely behind the plane.  Got it so far?  We created a 
		//    function called ClassifySphere() that returns BEHIND, FRONT or INTERSECTS.
		//    If ClassifySphere() returns INTERSECTS, then we move on to step 2, otherwise
		//    we did not collide with the polygon.
		// 
		// 2) The second step is to get an intersection point right in front of the sphere.
		//    This one of the tricky parts.  We know that once we have an intersection point
		//    on the plane of the polygon, we just need to use the InsidePolygon() function
		//    to see if that point is inside the dimensions of the polygon, just like we
		//    did with the Ray to Polygon Collision tutorial.  So, how do we get the point
		//    of intersection?  It's not as simple as it might sound.  Since a sphere infinite
		//    points, there would be a million points that it collided at.  You can't just
		//    draw a ray in the direction the sphere was moving because it could have just
		//    nicked the bottom of the polygon and your ray would find an intersection
		//    point that is outside of the polygon.  Well, it turns out that we need to
		//    first try and give it a shot.  We will try the first attempt a different way though.
		//    We know that we can find the normal vector of the polygon, which in essence
		//    tells us the direction that the polygon is facing.  From ClassifyPoly(),
		//	  it also returns the distance the center our sphere is from the plane.  That
		//    means we have a distance our sphere center is from the plane, and the normal
		//    tells us the direction the plane is in.  If we multiply the normal by the
		//    distance from the plane we get an offset.  This offset can then be subtracted
		//    from the center of the sphere.  Believe it or not, but we now have a position
		//    on the plane in the direction of the plane.  Usually, this intersection points
		//    works fine, but if we get around the edges of the polygon, this does not work.
		//    What we just did is also called "projecting the center of the sphere onto the plane".
		//    Another way to do this is to shoot out a ray from the center of the sphere in
		//    the opposite direction of the normal, then we find the intersection of that line
		//    and the plane.  My way just takes 3 multiplies and a subtraction.  You choose.
		//
		// 3) Once we have our psuedo intersection point, we just pass it into InsidePolygon(),
		//    along with the polygon vertices and the vertex count.  This will then return
		//    true if the intersection point was inside of the polygon, otherwise false.
		//    Remember, just because this returns false doesn't mean we stop there!  If
		//    we didn't collide yet, we need to skip to step 4.
		//
		// 4) If we get here, it's assumed that we tried our intersection point and it
		//    wasn't in the polygon's perimeter.  No fear!  There is hope!  If we get to step
		//    4, that's means that our center point is outside of the polygon's perimeter. Since
		//    we are dealing with a sphere, we could still be colliding because of the sphere's radius.
		//	  This last check requires us to find the point on each of the polygon's edges that
		//    is closest to the sphere's center.  We have a tutorial on finding this, so make sure
		//    you have read it or are comfortable with the concept.  If we are dealing with a
		//    triangle, we go through every side and get an edge vector, and calculate the closest
		//    point on those lines to our sphere's center.  After getting each closest point, we
		//    calculate the distance that point is from our sphere center.  If the distance is
		//    less than the radius of the sphere, there was a collision.  This way is pretty fast.  
		//    You don't need to calculate all three sides evey time, since the first closest point's 
		//    distance could be less than the radius and you return "true".
		//
		// That's the overview, *phew!*.  I bet you are reading this just wanting to cry because
		// that seems like so much math and theory to digest, so the code must be atrocious!
		// Well, you are partially right :)  It's not that bad actually, quite straight forward.
		// I will label the steps in the code so you can go back and forth to the overview and code.
		// I might mention that by having our CVector3 class operator overloaded it cuts down the 
		// code tremendously.  If you are confused with this concept of C++, just create functions
		// to add, subtract and multiply vectors or scalars together.
		//

		// 1) STEP ONE - Finding the sphere's classification
		
		// Let's use our Normal() function to return us the normal to this polygon
		CVector3 vNormal = Normal(vPolygon);

		// This will store the distance our sphere is from the plane
		float distance = 0.0f;

		// This is where we determine if the sphere is in FRONT, BEHIND, or INTERSECTS the plane
		// of the polygon.  We pass is our sphere center, the polygon's normal, a point on
		// the plane (vertex), the sphere's radius and an empty float to fill the distance with.
		int classification = ClassifySphere(vCenter, vNormal, vPolygon[0], radius, distance);


		// Java Port Note: We must calc the distance here.
		float d = (float)PlaneDistance(vNormal, vPolygon[0]);
		distance = (vNormal.x * vCenter.x + vNormal.y * vCenter.y + vNormal.z * vCenter.z + d);

		// If the sphere intersects the polygon's plane, then we need to check further,
		// otherwise the sphere did NOT intersect the polygon.  Pretty fast so far huh?
		if(classification == INTERSECTS) 
		{
			// 2) STEP TWO - Finding the psuedo intersection point on the plane

			// Now we want to project the sphere's center onto the polygon's plane,
			// in the direction of the normal.  This is done by multiplying the "normal"
			// by the "distance" the sphere center is from the plane.  We got the distance
			// from the ClassifySphere() function call up above.  2 return values were given
			// through the "distance" variable being passed in as a reference.  If projecting
			// is confusing to you, just think of it as this: "I am starting at the center
			// of the sphere and I am going to just run into the plane.  I will move in the 
			// direction that is reverse from the normal.  When do I know when to stop?  Well,
			// I just go in that direction until my distance from the center is the same as
			// the distance the center of the sphere is from the plane."  By doing this
			// we get an offset to subtract from the center of the sphere.
			CVector3 vOffset = Multiply(vNormal , distance);

			// Once we have the offset to the plane, we just subtract it from the center
			// of the sphere.  "vPosition" now a point that lies on the plane of the polygon.
			// Whether it is inside the polygon's perimeter is another story.  Usually it
			// is though, unless we get near the edges of the polygon.
			CVector3 vPosition = Subtract(vCenter , vOffset);

			// 3) STEP THREE - Check if the intersection point is inside the polygons perimeter

			// This is the same function used in our previous tutorial on Ray to Polygon Collision.
			// If the intersection point is inside the perimeter of the polygon, it returns true.
			// We pass in the intersection point, the list of vertices and vertex count of the poly.
			if(InsidePolygon(vPosition, vPolygon, vertexCount))
				return true;	// We collided!
			else
			{
				// 4) STEP FOUR - Check the sphere to see if it intersects the polygon edges

				// If we get here, we didn't find an intersection point in the perimeter.
				// There is still one more chance to redeem our sphere that it can hit the mark.
				// If any part of the sphere intersects the edges of the polygon, we collided.  
				// This is only checked if the sphere's center point is outside the edges of the
				// polygon. We pass in the center of the sphere, the list of verts, the polygon 
				// vertex count and the sphere's radius.  If this returns true we have a collision.
				if(EdgeSphereCollision(vCenter, vPolygon, vertexCount, radius))
				{
					return true;	// We collided! "And you doubted me..." - Sphere
				}
			}
		}

		// If we get here, there is obviously no collision happening up in this crib
		return false;
	}
	
	///////////////////////////////// CLASSIFY POINT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This tells if a point is BEHIND, in FRONT, or INTERSECTS a plane, also it's distance
	/////
	///////////////////////////////// CLASSIFY POINT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static int ClassifyPoint(  CVector3 vNormal, CVector3 vPoint, float distance)
	{
		// First we need to find the distance our polygon plane is from the origin.
		// We need this for the distance formula below.
		float d = (float)PlaneDistance(vNormal, vPoint);

		// Here we use the famous distance formula to find the distance the center point
		// of the sphere is from the polygon's plane.  
		// Remember that the formula is Ax + By + Cz + d = 0 with ABC = Normal, XYZ = Point
		distance = (vNormal.x * vPoint.x + vNormal.y * vPoint.y + vNormal.z * vPoint.z + d);

		// If the absolute value of the distance we just found is less than the radius, 
		// the sphere intersected the plane.
		if(Absolute(distance) < 0.0f)
			return INTERSECTS;
		// Else, if the distance is greater than or equal to the radius, the sphere is
		// completely in FRONT of the plane.
		else if(distance >= 0.0f)
			return FRONT;
		
		// If the sphere isn't intersecting or in FRONT of the plane, it must be BEHIND
		return BEHIND;
	}
	
	///////////////////////////////// CLASSIFY SPHERE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This tells if a sphere is BEHIND, in FRONT, or INTERSECTS a plane, also it's distance
	/////
	///////////////////////////////// CLASSIFY SPHERE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static int ClassifySphere(CVector3 vCenter, 
					   CVector3 vNormal, CVector3 vPoint, float radius, float distance)
	{
		// First we need to find the distance our polygon plane is from the origin.
		// We need this for the distance formula below.
		float d = (float)PlaneDistance(vNormal, vPoint);

		// Here we use the famous distance formula to find the distance the center point
		// of the sphere is from the polygon's plane.  
		// Remember that the formula is Ax + By + Cz + d = 0 with ABC = Normal, XYZ = Point
		distance = (vNormal.x * vCenter.x + vNormal.y * vCenter.y + vNormal.z * vCenter.z + d);

		// Now we query the information just gathered.  Here is how Sphere Plane Collision works:
		// If the distance the center is from the plane is less than the radius of the sphere,
		// we know that it must be intersecting the plane.  We take the absolute value of the
		// distance when we do this check because once the center of the sphere goes behind
		// the polygon, the distance turns into negative numbers (with 0 being that the center
		// is exactly on the plane).  What do I mean when I say "behind" the polygon?  How do
		// we know which side is the front or back side?  Well, the side with the normal pointing
		// out from it is the front side, the other side is the back side.  This is all dependant
		// on the direction the vertices stored.  I recommend drawing them counter-clockwise.
		// if you go clockwise the normal with then point the opposite way and will screw up
		// everything.
		// So, if we want to find if the sphere is in front of the plane, we just make sure
		// the distance is greater than or equal to the radius.  let's say we have a radius
		// of 5, and the distance the center is from the plane is 6; it's obvious that the
		// sphere is 1 unit away from the plane.
		// If the sphere isn't intersecting or in front of the plane, it HAS to be BEHIND it.

		// If the absolute value of the distance we just found is less than the radius, 
		// the sphere intersected the plane.
		if(Absolute(distance) < radius)
			return INTERSECTS;
		// Else, if the distance is greater than or equal to the radius, the sphere is
		// completely in FRONT of the plane.
		else if(distance >= radius)
			return FRONT;
		
		// If the sphere isn't intersecting or in FRONT of the plane, it must be BEHIND
		return BEHIND;
	}

	///////////////////////////////// EDGE SPHERE COLLSIION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns true if the sphere is intersecting any of the edges of the polygon
	/////
	///////////////////////////////// EDGE SPHERE COLLSIION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static boolean EdgeSphereCollision(CVector3 vCenter, 
							 CVector3 vPolygon[], int vertexCount, float radius)
	{
		CVector3 vPoint;

		// This function takes in the sphere's center, the polygon's vertices, the vertex count
		// and the radius of the sphere.  We will return true from this function if the sphere
		// is intersecting any of the edges of the polygon.  How it works is, every edge line
		// segment finds the closest point on that line to the center of the sphere.  If the
		// distance from that closest point is less than the radius of the sphere, there was
		// a collision.  Otherwise, we are definately out of reach of the polygon.  This works
		// for triangles, quads, and any other convex polygons.

		// Go through all of the vertices in the polygon
		for(int i = 0; i < vertexCount; i++)
		{
			// This returns the closest point on the current edge to the center of the sphere.
			// Notice that we mod the second point of the edge by our vertex count.  This is
			// so that when we get to the last edge of the polygon, the second vertex of the
			// edge is the first vertex that we starting with.  
			vPoint = ClosestPointOnLine(vPolygon[i], vPolygon[(i + 1) % vertexCount], vCenter);
			
			// Now, we want to calculate the distance between the closest point and the center
			float distance = Distance(vPoint, vCenter);
		
			// If the distance is less than the radius, there must be a collision so return true
			if(distance < radius)
				return true;
		}

		// The was no intersection of the sphere and the edges of the polygon
		return false;
	}
	
	///////////////////////////////// GET COLLISION OFFSET \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns the offset to move the center of the sphere off the collided polygon
	/////
	///////////////////////////////// GET COLLISION OFFSET \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static CVector3 GetCollisionOffset(CVector3 vNormal, float radius, float distance)
	{
		CVector3 vOffset = new CVector3(0, 0, 0);

		// Once we find if a collision has taken place, we need make sure the sphere
		// doesn't move into the wall.  In our app, the position will actually move into
		// the wall, but we check our collision detection before we render the scene, which
		// eliminates the bounce back effect it would cause.  The question is, how do we
		// know which direction to move the sphere back?  In our collision detection, we
		// account for collisions on both sides of the polygon.  Usually, you just need
		// to worry about the side with the normal vector and positive distance.  If 
		// you don't want to back face cull and have 2 sided planes, I check for both sides.
		//
		// Let me explain the math that is going on here.  First, we have the normal to
		// the plane, the radius of the sphere, as well as the distance the center of the
		// sphere is from the plane.  In the case of the sphere colliding in the front of
		// the polygon, we can just subtract the distance from the radius, then multiply
		// that new distance by the normal of the plane.  This projects that leftover
		// distance along the normal vector.  For instance, say we have these values:
		//
		//	vNormal = (1, 0, 0)		radius = 5		distance = 3
		//
		// If we subtract the distance from the radius we get: (5 - 3 = 2)
		// The number 2 tells us that our sphere is over the plane by a distance of 2.
		// So basically, we need to move the sphere back 2 units.  How do we know which
		// direction though?  This part is easy, we have a normal vector that tells us the
		// direction of the plane.  
		// If we multiply the normal by the left over distance we get:  (2, 0, 0)
		// This new offset vectors tells us which direction and how much to move back.
		// We then subtract this offset from the sphere's position, giving is the new
		// position that is lying right on top of the plane.  Ba da bing!
		// If we are colliding from behind the polygon (not usual), we do the opposite
		// signs as seen below:
		
		// If our distance is greater than zero, we are in front of the polygon
		if(distance > 0)
		{
			// Find the distance that our sphere is overlapping the plane, then
			// find the direction vector to move our sphere.
			float distanceOver = radius - distance;
			vOffset = Multiply(vNormal , distanceOver);
		}
		else // Else colliding from behind the polygon
		{
			// Find the distance that our sphere is overlapping the plane, then
			// find the direction vector to move our sphere.
			float distanceOver = radius + distance;
			vOffset = Multiply(vNormal , -distanceOver);
		}

		// There is one problem with check for collisions behind the polygon, and that
		// is if you are moving really fast and your center goes past the front of the
		// polygon, it will then assume you were colliding from behind and not let
		// you back in.  Most likely you will take out the if / else check, but I
		// figured I would show both ways in case someone didn't want to back face cull.

		// Return the offset we need to move back to not be intersecting the polygon.
		return vOffset;
	}

	///////////////////////////////// QUATERNION MULTIPLICATION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Moltiplicazion fra quaternioni
	/////
	///////////////////////////////// QUATERNION MULTIPLICATION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static CQuaternion QuatMultiply(CQuaternion A, CQuaternion B)
	{
 		CQuaternion C = new CQuaternion();

  		C.x = A.w*B.x + A.x*B.w + A.y*B.z - A.z*B.y;
  		C.y = A.w*B.y - A.x*B.z + A.y*B.w + A.z*B.x;
  		C.z = A.w*B.z + A.x*B.y - A.y*B.x + A.z*B.w;
  		C.w = A.w*B.w - A.x*B.x - A.y*B.y - A.z*B.z;
  		return C;
	}

	///////////////////////////////// QUATERNION CONJUGATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Cognugazione di un quaternione
	/////
	///////////////////////////////// QUATERNION CONJUGATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static CQuaternion Conjugate(CQuaternion quat)
	{
		quat.x = -quat.x;
		quat.y = -quat.y;
		quat.z = -quat.z;
		return quat;
	}
	
	///////////////////////////////// MATRIX VECTOR MULTIPLICATION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Moltiplicazione fra matrice e vettore
	/////
	///////////////////////////////// MATRIX VECTOR MULTIPLICATION  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public static float[] MatrixVectorMul(float[] M, float[] v)
	{
		float[] res = new float[4];		// Hold Calculated Results
		res[0]=M[ 0]*v[0]+M[ 4]*v[1]+M[ 8]*v[2]+M[12]*v[3];
		res[1]=M[ 1]*v[0]+M[ 5]*v[1]+M[ 9]*v[2]+M[13]*v[3];
		res[2]=M[ 2]*v[0]+M[ 6]*v[1]+M[10]*v[2]+M[14]*v[3];
		res[3]=M[ 3]*v[0]+M[ 7]*v[1]+M[11]*v[2]+M[15]*v[3];
		return res;
 	}
}