/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE agle.lib

                            FILE CTEXTURELOADER
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.lib.loaders;

// Java classes
import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;      

// GL4Java classes
import gl4java.*;
import gl4java.utils.textures.*;

import agle.lib.*;

///////////////////////////////// CTEXTURELOADER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// CTEXTURELOADER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public abstract class CTextureloader
{
	public static int texture[] = new int[1];
	
	
	///////////////////////////////// LOAD \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Questa funzione carica le texture 
	/////
	///////////////////////////////// LOAD \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\    
	public static int LoadPng(GLFunc gl, GLUFunc glu, String file)
	{
		// First we load the image
		PngTextureLoader texLoader = new PngTextureLoader(gl, glu);   
		texLoader.readTexture(file);

		if (texLoader.isOk())
		{
			gl.glGenTextures(1, texture);
			gl.glBindTexture(gl.GL_TEXTURE_2D, texture[0]);
		
			gl.glTexParameteri(gl.GL_TEXTURE_2D,gl.GL_TEXTURE_MIN_FILTER,gl.GL_LINEAR_MIPMAP_NEAREST);
			gl.glTexParameteri(gl.GL_TEXTURE_2D,gl.GL_TEXTURE_MAG_FILTER,gl.GL_LINEAR_MIPMAP_LINEAR);

			glu.gluBuild2DMipmaps(gl.GL_TEXTURE_2D, 
				                 texLoader.getComponents(),
				                 texLoader.getImageWidth(), 
				                 texLoader.getImageHeight(),
				                 texLoader.getGLFormat(), 
				                 gl.GL_UNSIGNED_BYTE, 
				                 texLoader.getTexture());                  
		}
		return texture[0];
	}	
	
	///////////////////////////////// LOADBMP \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Questa funzione carica le texture 
	/////
	///////////////////////////////// LOADBMP \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\    
	public static int LoadBmp(GLFunc gl, GLUFunc glu, String file)
	{
		// First we load bmp the image
		BmpTextureLoader texLoader = new BmpTextureLoader(gl, glu);   
		texLoader.readTexture(file);

		if (texLoader.isOk())
		{
			gl.glGenTextures(1, texture);
			gl.glBindTexture(gl.GL_TEXTURE_2D, texture[0]);
		
			gl.glTexParameteri(gl.GL_TEXTURE_2D,gl.GL_TEXTURE_MIN_FILTER,gl.GL_LINEAR_MIPMAP_NEAREST);
			gl.glTexParameteri(gl.GL_TEXTURE_2D,gl.GL_TEXTURE_MAG_FILTER,gl.GL_LINEAR_MIPMAP_LINEAR);

			glu.gluBuild2DMipmaps(gl.GL_TEXTURE_2D, 
				                 texLoader.getComponents(),
				                 texLoader.getImageWidth(), 
				                 texLoader.getImageHeight(),
				                 texLoader.getGLFormat(), 
				                 gl.GL_UNSIGNED_BYTE, 
				                 texLoader.getTexture());                  
		}
		return texture[0];
	}	

	///////////////////////////////// LOADJPG \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Questa funzione carica le texture 
	/////
	///////////////////////////////// LOADJPG \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\    
	public static int LoadJpg(GLFunc gl, GLUFunc glu, String file)
	{
		// First we load bmp the image
		JpgTextureLoader texLoader = new JpgTextureLoader(gl, glu);
		texLoader.readTexture(file);

		if (texLoader.isOk())
		{
			gl.glGenTextures(1, texture);
			gl.glBindTexture(gl.GL_TEXTURE_2D, texture[0]);
		
			gl.glTexParameteri(gl.GL_TEXTURE_2D,gl.GL_TEXTURE_MIN_FILTER,gl.GL_LINEAR_MIPMAP_NEAREST);
			gl.glTexParameteri(gl.GL_TEXTURE_2D,gl.GL_TEXTURE_MAG_FILTER,gl.GL_LINEAR_MIPMAP_LINEAR);

			glu.gluBuild2DMipmaps(gl.GL_TEXTURE_2D, 
				                 texLoader.getComponents(),
				                 texLoader.getImageWidth(), 
				                 texLoader.getImageHeight(),
				                 texLoader.getGLFormat(), 
				                 gl.GL_UNSIGNED_BYTE, 
				                 texLoader.getTexture());
		}
		return texture[0];
	}	
}