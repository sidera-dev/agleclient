/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                     AGLE LIB

                            FILE CSKYBOX 
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

///////////////////////////////// CSKYBOX \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	Lo SkyBox e' un cubo centrato agli assi che server per riprodurre
/////   scenari molto distanti e dare l'effetto di essere sotto un cielo
/////
///////////////////////////////// CSKYBOX  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class CSkyBox extends CMass
{
	// Larghezza, altezza e lunghezza dello skybox
	float width;
	float height;
	float length;
	// Posizione dello skybox
	CVector3 Position;	
	// Id delle textures del cubo
	int BACK_ID;	// dietro
	int FRONT_ID;	// davanti
	int BOTTOM_ID;	// sotto
	int TOP_ID;		// sopra
	int LEFT_ID;	// sinistra
	int RIGHT_ID;	// destra
	// Spazio per le Textures
	int[] Texture;
	
	///////////////////////////////// CSKYBOX \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Costruttore della classe
	/////
	///////////////////////////////// CSKYBOX  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public CSkyBox()
	{	
		width = 0f;
		height = 0f;
		length = 0f;
		// Posizione dello skybox
		Position = new CVector3(0.0f,0.0f,0.0f);
		
		// Id delle textures del cubo
		BACK_ID		= 0;	// dietro
		FRONT_ID	= 1;	// davanti
		BOTTOM_ID	= 2;	// sotto
		TOP_ID		= 3;	// sopra
		LEFT_ID		= 4;	// sinistra
		RIGHT_ID	= 5;	// destra
		
		// Spazio per 16 Textures
		Texture = new int[6];
	}
	
	///////////////////////////////// SETDIMENSIONS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Imposta / Modifica le dimensioni dello skybox
	/////
	///////////////////////////////// SETDIMENSIONS  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void SetDimensions(float w, float h, float l)
	{
		width  = w;
		height = h*2; // L'altezza normale e' troppo poco
		length = l;
		// Trasla lo skybox di meta'dell' width,height and length
		// in questo modo appraria' come fosse al centro
		Position.x -= w/2;
		Position.y -= h/2;
		Position.z -= l/2;
	}
	
	///////////////////////////////// CREATETEXTURE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Crea le texture per lo skybox
	/////
	///////////////////////////////// CREATETEXTURE  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void CreateTexture(GLFunc gl,GLUFunc glu,String[] textureFiles)
	{	
		// First we load the image
		PngTextureLoader texLoader = new PngTextureLoader(gl, glu); 
		// We need to generate four texture ID's
		gl.glGenTextures(6, Texture);
		  
		for (int i=0; i<6 ;i++ )
		{
			texLoader.readTexture(textureFiles[i]);
			
			if (texLoader.isOk())
			{
				// This sets the alignment requirements for the start of each pixel row in memory.
				gl.glPixelStorei (gl.GL_UNPACK_ALIGNMENT, 1);
				
				gl.glBindTexture(gl.GL_TEXTURE_2D, Texture[i]);
				gl.glTexParameteri(gl.GL_TEXTURE_2D,gl.GL_TEXTURE_MIN_FILTER,gl.GL_LINEAR_MIPMAP_NEAREST);
				gl.glTexParameteri(gl.GL_TEXTURE_2D,gl.GL_TEXTURE_MAG_FILTER,gl.GL_LINEAR_MIPMAP_LINEAR);

				// The default GL_TEXTURE_WRAP_S and ""_WRAP_T property is GL_REPEAT.
				// We need to turn this to GL_CLAMP_TO_EDGE, otherwise it creates ugly seems
				// in our sky box.  GL_CLAMP_TO_EDGE does not repeat when bound to an object.
				gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
				gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);

				glu.gluBuild2DMipmaps(gl.GL_TEXTURE_2D, texLoader.getComponents(), texLoader.getImageWidth(), texLoader.getImageHeight(), texLoader.getGLFormat(), gl.GL_UNSIGNED_BYTE, texLoader.getTexture());	
			}
		}
	}
	
	///////////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Questa funzione crea la scatola centrata negli x,y,z. Invece di
	/////   avere dei colori per i vari vertici ci sono delle textures per 
	/////   ogni lato della cubo in modo da creare l'illusione di un cielo.
	/////
	///////////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void Render(GLFunc gl,GLUFunc glu)
	{
		// Bind the BACK texture of the sky map to the BACK side of the cube
		gl.glBindTexture(gl.GL_TEXTURE_2D, Texture[BACK_ID]);

		// Start drawing the side as a QUAD
		gl.glBegin(gl.GL_QUADS);		
			
			// Assign the texture coordinates and vertices for the BACK Side
			gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(Position.x + width,	Position.y,			Position.z);
			gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(Position.x + width,	Position.y + height,Position.z); 
			gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(Position.x,			Position.y + height,Position.z);
			gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(Position.x,			Position.y,			Position.z);
			
		gl.glEnd();

		// Bind the FRONT texture of the sky map to the FRONT side of the box
		gl.glBindTexture(gl.GL_TEXTURE_2D, Texture[FRONT_ID]);


		// Start drawing the side as a QUAD
		gl.glBegin(gl.GL_QUADS);	
		
			// Assign the texture coordinates and vertices for the FRONT Side
			gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(Position.x,			Position.y,			Position.z + length);
			gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(Position.x,			Position.y + height,Position.z + length);
			gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(Position.x + width,	Position.y + height,Position.z + length); 
			gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(Position.x + width,	Position.y,			Position.z + length);
		gl.glEnd();

		// Bind the BOTTOM texture of the sky map to the BOTTOM side of the box
		gl.glBindTexture(gl.GL_TEXTURE_2D, Texture[BOTTOM_ID]);

		// Start drawing the side as a QUAD
		gl.glBegin(gl.GL_QUADS);		
		
			// Assign the texture coordinates and vertices for the BOTTOM Side
			gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(Position.x,			Position.y,	Position.z);
			gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(Position.x,			Position.y,	Position.z + length);
			gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(Position.x + width,	Position.y,	Position.z + length); 
			gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(Position.x + width,	Position.y,	Position.z);
		gl.glEnd();

		// Bind the TOP texture of the sky map to the TOP side of the box
		gl.glBindTexture(gl.GL_TEXTURE_2D, Texture[TOP_ID]);

		// Start drawing the side as a QUAD
		gl.glBegin(gl.GL_QUADS);		
			
			// Assign the texture coordinates and vertices for the TOP Side
			gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(Position.x + width,	Position.y + height, Position.z);
			gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(Position.x + width,	Position.y + height, Position.z + length); 
			gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(Position.x,			Position.y + height, Position.z + length);
			gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(Position.x,			Position.y + height, Position.z);
			
		gl.glEnd();

		// Bind the LEFT texture of the sky map to the LEFT side of the box
		gl.glBindTexture(gl.GL_TEXTURE_2D, Texture[LEFT_ID]);

		// Start drawing the side as a QUAD
		gl.glBegin(gl.GL_QUADS);		
			
			// Assign the texture coordinates and vertices for the LEFT Side
			gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(Position.x,	Position.y + height,	Position.z);	
			gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(Position.x,	Position.y + height,	Position.z + length); 
			gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(Position.x,	Position.y,				Position.z + length);
			gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(Position.x,	Position.y,				Position.z);		
			
		gl.glEnd();

		// Bind the RIGHT texture of the sky map to the RIGHT side of the box
		gl.glBindTexture(gl.GL_TEXTURE_2D, Texture[RIGHT_ID]);

		// Start drawing the side as a QUAD
		gl.glBegin(gl.GL_QUADS);		

			// Assign the texture coordinates and vertices for the RIGHT Side
			gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(Position.x + width, Position.y,			Position.z);
			gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(Position.x + width, Position.y,			Position.z + length);
			gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(Position.x + width, Position.y + height,	Position.z + length); 
			gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(Position.x + width, Position.y + height,	Position.z);
		gl.glEnd();
	}

	public void doCleanup(GLFunc gl,GLUFunc glu)
	{
		gl.glDeleteTextures(Texture.length, Texture);
	}
	
}