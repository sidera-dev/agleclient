/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      Rpg Client by Pendragon e Graham

                              FILE CLIGHT
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
package agle.client;

// Java classes
import java.lang.*;
import java.util.*;
import java.io.*;

// GL4Java classes
import gl4java.*;
import gl4java.utils.textures.*;

// Thera classes
import agle.lib.loaders.*;
import agle.lib.math.*;
import agle.lib.utils.*;
import agle.lib.font.CGLFont;

///////////////////////////////// CLIGHT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// CLIGHT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class CLight
{
	int			Index;
	String		Name;
	// Position, diffuse and ambience vectors
	float[]		Position;
	float[]		Diffuse;
	float[]		Ambience;
	
	public CLight(int lIndex, String lName, float[] lPosition, float[] lDiffuse, float[] lAmbience)
	{		
		Index	=		lIndex;
		Name	=		lName;
		Position =		lPosition;
		Diffuse =		lDiffuse;
		Ambience =		lAmbience;
		
	}
	
	///////////////////////////////// UPDATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	update lights
	/////
	///////////////////////////////// UPDATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void Update(GLFunc gl,GLUFunc glu)
	{
		gl.glLightfv( gl.GL_LIGHT0+Index, gl.GL_AMBIENT, Ambience );
		gl.glLightfv( gl.GL_LIGHT0+Index, gl.GL_DIFFUSE, Diffuse );
		gl.glEnable(  gl.GL_LIGHT0+Index  );
		gl.glEnable(  gl.GL_LIGHTING );
		gl.glEnable(gl.GL_COLOR_MATERIAL);	
		gl.glLightfv( gl.GL_LIGHT0+Index, gl.GL_POSITION, Position );
	}	
	
	public void setPosition(float[] lPosition)
	{
		Position = lPosition;
	}
	
	public void setDiffuse(float[] lDiffuse)
	{
		Diffuse = lDiffuse;
	}
		
	public void setAmbience(float[] lAmbience)
	{
		Ambience = lAmbience;
	}
	
	///////////////////////////////// DOCLEANUP \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	clean lights
	/////
	///////////////////////////////// DOCLEANUP \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void doCleanup(GLFunc gl,GLUFunc glu)
	{
		gl.glDisable(  gl.GL_LIGHT0+Index  );
	}
	
}