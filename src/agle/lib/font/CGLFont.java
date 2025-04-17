 /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE agle.lib

                            FILE CGLFont
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.lib.font;

// Java classes
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

// GL4Java classes
import gl4java.*;
import gl4java.GLContext.*;
import gl4java.awt.GLAnimCanvas;
import gl4java.utils.textures.*;

// classes
import agle.lib.*;
import agle.lib.loaders.CTextureloader;

///////////////////////////////// CGLFONT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	Classe che costruisce le fonts e stampa a schermo i caratteri
/////
///////////////////////////////// CGLFONT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class CGLFont
{   
	int 			base;					// Base Display List For The Font
	int[]			texture=new int[2]; 	// Storage For Our Font Texturezz
	int 			loop;					// Generic Loop Variable  
	float			cnt1;
	float			cnt2;
	public float	width;
	public float	height;
  
	public CGLFont(GLFunc gl, GLUFunc glu, String FontFile)
	{
		texture[0] = CTextureloader.LoadPng(gl,glu,FontFile);
		BuildFont(gl,glu);
	}
	
	///////////////////////////////// BUILDFONT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	funzione che si occupa di generare la font
	/////
	///////////////////////////////// BUILDFONT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void BuildFont(GLFunc gl, GLUFunc glu)     
	{
		// Holds Our X and Y Character Coord
		float	cx;	
		float	cy;
		// Creating 256 Display Lists
		base = gl.glGenLists(256);
		// Select Our Font Texture
		//gl.glBindTexture(gl.GL_TEXTURE_2D, texture[0]);
		// Loop Through All 256 Lists
		for (loop=0; loop<256; loop++)
			{
				// X and Y Position Of Current Character
				cx=(float)(loop%16)/16.0f;
				cy=(float)(loop/16)/16.0f;
				// Start Building A List
				gl.glNewList(base+loop,gl.GL_COMPILE);
				// Use A Quad For Each Character
				gl.glBegin(gl.GL_QUADS);
				// (Bottom Left)
				gl.glTexCoord2f(cx,1-cy-0.0625f);
				gl.glVertex2i(0,16); 
				// (Bottom Right)
				gl.glTexCoord2f(cx+0.0625f,1-cy-0.0625f);
				gl.glVertex2i(16,16);
				// (Top Right)   
				gl.glTexCoord2f(cx+0.0625f,1-cy);
				gl.glVertex2i(16,0);
				// (Top Left)    
				gl.glTexCoord2f(cx,1-cy);
				gl.glVertex2i(0,0);
				// Done Building Our Quad (Character)       
				gl.glEnd();
				// Move To The Right Of The Character 
				gl.glTranslated(10,0,0);
				// Done Building The Display List
				gl.glEndList();
			}
    }
    
	///////////////////////////////// GLPRINT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	funzione che stampa a schermo i caratteri
	/////
	///////////////////////////////// GLPRINT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ 
    public void glPrint( GLFunc gl, GLUFunc glu, int x, int y, String string, int set, float dimx , float dimy , float dimz )	// Where The Printing Happens      
    {
		if (set>1)
		{
			set=1;
		}
		// Select Our Font Texture
		gl.glBindTexture(gl.GL_TEXTURE_2D, texture[0]);
		// Disables Depth Testing
		gl.glDisable(gl.GL_DEPTH_TEST);
		// Select The Projection Matrix
		gl.glMatrixMode(gl.GL_PROJECTION);
		// Store The Projection Matrix
		gl.glPushMatrix();
		// Reset The Projection Matrix
		gl.glLoadIdentity();
		// Set Up An Ortho Screen
		gl.glOrtho(0,width,height,0,-1,1);
		// Select The Modelview Matrix
		gl.glMatrixMode(gl.GL_MODELVIEW);
		// Store The Modelview Matrix
		gl.glPushMatrix();
		// Reset The Modelview Matrix
		gl.glLoadIdentity();
		// Position The Text (0,0 - Bottom Left)
		gl.glTranslated(x,y,0);
		// Choose The Font Set (0 or 1)
		gl.glListBase(base-32+(128*set));
		// Make The Text nX bigger
		gl.glScalef(dimx,dimy,dimz);
		// Write The Text To The Screen
		gl.glCallLists(string.length(),gl.GL_BYTE,string.getBytes());
		// Select The Projection Matrix
		gl.glMatrixMode(gl.GL_PROJECTION);
		// Restore The Old Projection Matrix
		gl.glPopMatrix();
		// Select The Modelview Matrix
		gl.glMatrixMode(gl.GL_MODELVIEW);
		// Restore The Old Projection Matrix
		gl.glPopMatrix();
		// Enables Depth Testing
		gl.glEnable(gl.GL_DEPTH_TEST);
	}
	
	public void doCleanup(GLFunc gl, GLUFunc glu)
	{
		gl.glDeleteTextures(texture.length,texture);
	}
}