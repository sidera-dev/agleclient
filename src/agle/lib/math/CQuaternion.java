/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE agle.lib

                            FILE CGameScreen
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.lib.math;

// Java classes
import java.lang.*;
import java.util.*;
import java.io.*;

public class CQuaternion
{
	public float x, y, z, w;

	public CQuaternion()
	{
		x = 0.0f;	y = 0.0f;	z = 0.0f;	w = 1.0f;
	}
	
	// Creates a constructor that will allow us to initialize the quaternion when creating it
	public CQuaternion(float X, float Y, float Z, float W) 
	{ 
		x = X;		y = Y;		z = Z;		w = W;
	}
	
	////////////////////////////// CREATE FROM AXIS ANGLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function converts an axis and angle rotation to a quaternion
	/////
	////////////////////////////// CREATE FROM AXIS ANGLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void CreateFromAxisAngle(float X, float Y, float Z, float degree) 
	{ 
		// This function takes an angle and an axis of rotation, then converts
		// it to a quaternion.  An example of an axis and angle is what we pass into
		// glRotatef().  That is an axis angle rotation.  It is assumed an angle in 
		// degrees is being passed in.  Instead of using glRotatef(), we can now handle
		// the rotations our self.

		// The equations for axis angle to quaternions are such:

		// w = cos( theta / 2 )
		// x = X * sin( theta / 2 )
		// y = Y * sin( theta / 2 )
		// z = Z * sin( theta / 2 )

		// First we want to convert the degrees to radians 
		// since the angle is assumed to be in radians
		float angle = (float)((degree / 180.0f) * Math.PI);

		// Here we calculate the sin( theta / 2) once for optimization
		float result = (float)Math.sin( angle / 2.0f );
			
		// Calcualte the w value by cos( theta / 2 )
		w = (float)Math.cos( angle / 2.0f );

		// Calculate the x, y and z of the quaternion
		x = X * result;
		y = Y * result;
		z = Z * result;
	}

	////////////////////////////// CREATE MATRIX \\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function converts a quaternion to a rotation matrix
	/////
	////////////////////////////// CREATE MATRIX \\\\\\\\\\\\\\\\\\\\\\\\\\\\\*	
	public void CreateMatrix(float[] pMatrix)
	{
		// Make sure the matrix has allocated memory to store the rotation data
		if(pMatrix == null) return;
		
		// Fill in the rows of the 4x4 matrix, according to the quaternion to matrix equations

		// First row
		pMatrix[ 0] = 1.0f - 2.0f * ( y * y + z * z );  
		pMatrix[ 1] = 2.0f * ( x * y - w * z );  
		pMatrix[ 2] = 2.0f * ( x * z + w * y );  
		pMatrix[ 3] = 0.0f;  

		// Second row
		pMatrix[ 4] = 2.0f * ( x * y + w * z );  
		pMatrix[ 5] = 1.0f - 2.0f * ( x * x + z * z );  
		pMatrix[ 6] = 2.0f * ( y * z - w * x );  
		pMatrix[ 7] = 0.0f;  

		// Third row
		pMatrix[ 8] = 2.0f * ( x * z - w * y );  
		pMatrix[ 9] = 2.0f * ( y * z + w * x );  
		pMatrix[10] = 1.0f - 2.0f * ( x * x + y * y );  
		pMatrix[11] = 0.0f;  

		// Fourth row
		pMatrix[12] = 0;  
		pMatrix[13] = 0;  
		pMatrix[14] = 0;  
		pMatrix[15] = 1.0f;

		// Now pMatrix[] is a 4x4 homogeneous matrix that can be applied to an OpenGL Matrix
	}


	///////////////////////////////// CREATE FROM MATRIX \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This creates a quaternion from a 3x3 or a 4x4 matrix, depending on rowColumnCount
	/////
	///////////////////////////////// CREATE FROM MATRIX \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void CreateFromMatrix(float[] pTheMatrix, int rowColumnCount)
	{
		// Make sure the matrix has valid memory and it's not expected that we allocate it.
		// Also, we do a check to make sure the matrix is a 3x3 or a 4x4 (must be 3 or 4).
		if((pTheMatrix == null) || ((rowColumnCount != 3) && (rowColumnCount != 4))) return;

		// This function is used to take in a 3x3 or 4x4 matrix and convert the matrix
		// to a quaternion.  If rowColumnCount is a 3, then we need to convert the 3x3
		// matrix passed in to a 4x4 matrix, otherwise we just leave the matrix how it is.
		// Since we want to apply a matrix to an OpenGL matrix, we need it to be 4x4.

		// Point the matrix pointer to the matrix passed in, assuming it's a 4x4 matrix
		float[] pMatrix = pTheMatrix;

		// Create a 4x4 matrix to convert a 3x3 matrix to a 4x4 matrix (If rowColumnCount == 3)
		float[] m4x4 = new float[16];

		// If the matrix is a 3x3 matrix (which it is for Quake3), then convert it to a 4x4
		if(rowColumnCount == 3)
		{
			// Set the 9 top left indices of the 4x4 matrix to the 9 indices in the 3x3 matrix.
			// It would be a good idea to actually draw this out so you can visualize it.
			m4x4[0]  = pTheMatrix[0];	m4x4[1]  = pTheMatrix[1];	m4x4[2]  = pTheMatrix[2];	m4x4[ 3] = 0f;
			m4x4[4]  = pTheMatrix[3];	m4x4[5]  = pTheMatrix[4];	m4x4[6]  = pTheMatrix[5];	m4x4[ 7] = 0f;
			m4x4[8]  = pTheMatrix[6];	m4x4[9]  = pTheMatrix[7];	m4x4[10] = pTheMatrix[8];	m4x4[11] = 0f;

			// Since the bottom and far right indices are zero, set the bottom right corner to 1.
			// This is so that it follows the standard diagonal line of 1's in the identity matrix.
			m4x4[12] = 0f;	m4x4[13] = 0f;	m4x4[14] = 0f;	m4x4[15] = 1;

			// Set the matrix pointer to the first index in the newly converted matrix
			pMatrix = m4x4;
		}

		// The next step, once we made sure we are dealing with a 4x4 matrix, is to check the
		// diagonal of the matrix.  This means that we add up all of the indices that comprise
		// the standard 1's in the identity matrix.  If you draw out the identity matrix of a
		// 4x4 matrix, you will see that they 1's form a diagonal line.  Notice we just assume
		// that the last index (15) is 1 because it is not effected in the 3x3 rotation matrix.

		// Find the diagonal of the matrix by adding up it's diagonal indices.
		// This is also known as the "trace", but I will call the variable diagonal.
		float diagonal = pMatrix[0] + pMatrix[5] + pMatrix[10] + 1;
		float scale = 0.0f;

		// Below we check if the diagonal is greater than zero.  To avoid accidents with
		// floating point numbers, we substitute 0 with 0.00000001.  If the diagonal is
		// great than zero, we can perform an "instant" calculation, otherwise we will need
		// to identify which diagonal element has the greatest value.  Note, that it appears
		// that %99 of the time, the diagonal IS greater than 0 so the rest is rarely used.

		// If the diagonal is greater than zero
		if(diagonal > 0.00000001f)
		{
			// Calculate the scale of the diagonal
			scale = (float)Math.sqrt(diagonal) * 2f;

			// Calculate the x, y, z and w of the quaternion through the respective equation
			x = ( pMatrix[9] - pMatrix[6] ) / scale;
			y = ( pMatrix[2] - pMatrix[8] ) / scale;
			z = ( pMatrix[4] - pMatrix[1] ) / scale;
			w = 0.25f * scale;
		}
		else 
		{
			// If the first element of the diagonal is the greatest value
			if ( pMatrix[0] > pMatrix[5] && pMatrix[0] > pMatrix[10] )  
			{	
				// Find the scale according to the first element, and double that value
				scale  = (float)Math.sqrt( 1.0f + pMatrix[0] - pMatrix[5] - pMatrix[10] ) * 2.0f;

				// Calculate the x, y, z and w of the quaternion through the respective equation
				x = 0.25f * scale;
				y = (pMatrix[4] + pMatrix[1] ) / scale;
				z = (pMatrix[2] + pMatrix[8] ) / scale;
				w = (pMatrix[9] - pMatrix[6] ) / scale;	
			} 
			// Else if the second element of the diagonal is the greatest value
			else if ( pMatrix[5] > pMatrix[10] ) 
			{
				// Find the scale according to the second element, and double that value
				scale  = (float)Math.sqrt( 1.0f + pMatrix[5] - pMatrix[0] - pMatrix[10] ) * 2.0f;
				
				// Calculate the x, y, z and w of the quaternion through the respective equation
				x = (pMatrix[4] + pMatrix[1] ) / scale;
				y = 0.25f * scale;
				z = (pMatrix[9] + pMatrix[6] ) / scale;
				w = (pMatrix[2] - pMatrix[8] ) / scale;
			} 
			// Else the third element of the diagonal is the greatest value
			else 
			{	
				// Find the scale according to the third element, and double that value
				scale  = (float)Math.sqrt( 1.0f + pMatrix[10] - pMatrix[0] - pMatrix[5] ) * 2.0f;

				// Calculate the x, y, z and w of the quaternion through the respective equation
				x = (pMatrix[2] + pMatrix[8] ) / scale;
				y = (pMatrix[9] + pMatrix[6] ) / scale;
				z = 0.25f * scale;
				w = (pMatrix[4] - pMatrix[1] ) / scale;
			}
		}
	}


	/////////////////////////////////////// SLERP \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Returns a spherical linear interpolated quaternion between q1 and q2, with respect to t
	/////
	/////////////////////////////////////// SLERP \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	CQuaternion Slerp(CQuaternion q1, CQuaternion q2, float t)
	{
		// Create a local quaternion to store the interpolated quaternion
		CQuaternion qInterpolated = new CQuaternion();

		// This function is the milk and honey of our quaternion code, the rest of
		// the functions are an appendage to what is done here.  Everyone understands
		// the terms, "matrix to quaternion", "quaternion to matrix", "create quaternion matrix",
		// "quaternion multiplication", etc.. but "SLERP" is the stumbling block, even a little 
		// bit after hearing what it stands for, "Spherical Linear Interpolation".  What that
		// means is that we have 2 quaternions (or rotations) and we want to interpolate between 
		// them.  The reason what it's called "spherical" is that quaternions deal with a sphere.  
		// Linear interpolation just deals with 2 points primarily, where when dealing with angles
		// and rotations, we need to use sin() and cos() for interpolation.  If we wanted to use
		// quaternions for camera rotations, which have much more instant and jerky changes in 
		// rotations, we would use Spherical-Cubic Interpolation.  The equation for SLERP is this:
		//
		// q = (((b.a)^-1)^t)a
		//
		// Go here for an a detailed explanation and proofs of this equation:
		//
		// http://www.magic-software.com/Documentation/quat.pdf
		//
		// Now, Let's code it

		// Here we do a check to make sure the 2 quaternions aren't the same, return q1 if they are
		if(q1.x == q2.x && q1.y == q2.y && q1.z == q2.z && q1.w == q2.w) 
			return q1;

		// Following the (b.a) part of the equation, we do a dot product between q1 and q2.
		// We can do a dot product because the same math applied for a 3D vector as a 4D vector.
		float result = (q1.x * q2.x) + (q1.y * q2.y) + (q1.z * q2.z) + (q1.w * q2.w);

		// If the dot product is less than 0, the angle is greater than 90 degrees
		if(result < 0.0f)
		{
			// Negate the second quaternion and the result of the dot product
			q2.x = -q2.x;
			q2.y = -q2.y;
			q2.z = -q2.z;
			q2.w = -q2.w;
			result = -result;
		}

		// Set the first and second scale for the interpolation
		float scale0 = 1 - t;
		float scale1 = t;

		// Next, we want to actually calculate the spherical interpolation.  Since this
		// calculation is quite computationally expensive, we want to only perform it
		// if the angle between the 2 quaternions is large enough to warrant it.  If the
		// angle is fairly small, we can actually just do a simpler linear interpolation
		// of the 2 quaternions, and skip all the complex math.  We create a "delta" value
		// of 0.1 to say that if the cosine of the angle (result of the dot product) between
		// the 2 quaternions is smaller than 0.1, then we do NOT want to perform the full on 
		// interpolation using.  This is because you won't really notice the difference.

		// Check if the angle between the 2 quaternions was big enough to warrant such calculations
		if((1 - result) > 0.1f)
		{
			// Get the angle between the 2 quaternions, and then store the sin() of that angle
			float theta = (float)Math.acos(result);
			float sinTheta = (float)Math.sin(theta);

			// Calculate the scale for q1 and q2, according to the angle and it's sine value
			scale0 = (float)Math.sin( ( 1 - t ) * theta) / sinTheta;
			scale1 = (float)Math.sin( ( t * theta) ) / sinTheta;
		}	

		// Calculate the x, y, z and w values for the quaternion by using a special
		// form of linear interpolation for quaternions.
		qInterpolated.x = (scale0 * q1.x) + (scale1 * q2.x);
		qInterpolated.y = (scale0 * q1.y) + (scale1 * q2.y);
		qInterpolated.z = (scale0 * q1.z) + (scale1 * q2.z);
		qInterpolated.w = (scale0 * q1.w) + (scale1 * q2.w);

		// Return the interpolated quaternion
		return qInterpolated;
	}
}