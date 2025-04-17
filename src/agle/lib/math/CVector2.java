/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                 	AGLE agle.lib

                            FILE CVector2
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.lib.math;

import java.io.*;
import java.lang.*;
import java.util.*;
  
///////////////////////////////// CVector2 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// CVector2 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class CVector2 implements Serializable
{
	public float x, y;
	
	public CVector2()
	{
		x = 0f;
		y = 0f;
	}
	
	public CVector2(float x, float y)
	{
		this.x = x;		this.y = y;
	}
};
