/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE agle.lib

                            FILE CVector3
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.lib.math;

import java.io.*;
import java.lang.*;
import java.util.*; 

///////////////////////////////// CVector3 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// CVector3 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class CVector3 implements Serializable
{
	public float x;
	public float y;
	public float z;
	
	///////////////////////////////// CVECTOR3 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns a perpendicular vector from 2 given vectors by taking the cr
	/////
	///////////////////////////////// CVECTOR3 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public CVector3()
	{
		x = 0.0f;
		y = 0.0f;
		z = 0.0f;
	}
	
	///////////////////////////////// CVECTOR3 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns a perpendicular vector from 2 given vectors by taking the cross product.
	/////
	///////////////////////////////// CVECTOR3 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public CVector3(float vx, float vy, float vz)
	{
		x = vx;
		y = vy;
		z = vz;
	}
	
}