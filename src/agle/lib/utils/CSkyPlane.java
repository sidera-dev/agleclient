/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE LIB

                            FILE SKYPLANE
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.lib.utils;

// Java classes
import java.io.*;
import java.lang.*;
import java.util.*; 
import java.util.Arrays.*; 
// GL4Java classes
import gl4java.*;
import gl4java.utils.textures.*;

// classes
import agle.lib.*;
import agle.lib.math.*;
import agle.lib.loaders.*;
import agle.lib.physics.*;

///////////////////////////////// SKYPLANE\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////
///////////////////////////////// SKYPLANE\\\\\\\\\\\\\\\\\\\\\\\\\
public class CSkyPlane
{
	
	public final float PI = 3.1415926535897f;

	public class CVERTEX
	{
		float x = 0f;
		float y = 0f;
		float z = 0f;
		int color = 0;
		float u = 0f;
		float v= 0f;
	};
	
	
	public CVERTEX PlaneVertices[];
	public int NumPlaneVertices;

	int Indices[];
	int NumIndices;

	float pRadius; // Used for rendering
	
	int[] texture=new int[2]; 	// Storage For Our Texture	

	public void GenerateSkyPlane(int divisions, float PlanetRadius, float AtmosphereRadius, float hTile, float vTile)
	{		
		// Set the number of divisions into a valid range
		int divs = divisions;
		if (divisions < 1) 
			divs = 1;
		
		if (divisions > 256) 
			divs = 256; 
		
		pRadius = PlanetRadius;
		
		// Initialize the Vertex and Indices arrays
		NumPlaneVertices = (divs + 1) * (divs + 1);   // 1 division would give 4 verts
		NumIndices  = divs * divs * 2 * 3;       // 1 division would give 6 indices for 2 tris
		
		PlaneVertices = new CVERTEX[NumPlaneVertices];
		CVERTEX tmpv = new CVERTEX();
		Arrays.fill(PlaneVertices,tmpv);

		Indices = new int[NumIndices];
		Arrays.fill(Indices,0);
		
		// Calculate some values we will need
		
		float plane_size = 2.0f * (float)(Math.sqrt((AtmosphereRadius*AtmosphereRadius)-(PlanetRadius*PlanetRadius)));
		float delta = plane_size/(float)divs;
		float tex_delta = 2.0f/(float)divs;
		
		// Variables we'll use during the dome's generation
		float x_dist   = 0.0f;
		float z_dist   = 0.0f;
		float x_height = 0.0f;
		float z_height = 0.0f;
		float height = 0.0f;
		
		int count = 0;
		
		for (int i=0;i <= divs;i++)
		{
		    for (int j=0; j <= divs; j++)
			{
				CVERTEX SV = new CVERTEX();
				x_dist = (-0.5f * plane_size) + ((float)j*delta);
				z_dist = (-0.5f * plane_size) + ((float)i*delta);
		
			    x_height = (x_dist*x_dist) / AtmosphereRadius;
				z_height = (z_dist*z_dist) / AtmosphereRadius;
				height = x_height + z_height;
		
				SV.x = x_dist;
				SV.y = 0.0f - height;
				SV.z = z_dist;
		
				// Calculate the texture coordinates
				SV.u = hTile*((float)j * tex_delta*0.5f);
				SV.v = vTile*(1.0f - (float)i * tex_delta*0.5f);
		
				PlaneVertices[i*(divs+1)+j] = SV;
			}
		}
		
		// Calculate the indices
		int index = 0;
		for (int i=0; i < divs;i++)
		{
			for (int j=0; j < divs; j++)
			{
				int startvert = (i*(divs+1) + j);
		
			    // tri 1
				Indices[index++] = startvert;
				Indices[index++] = startvert+1;
				Indices[index++] = startvert+divs+1;
		
				// tri 2
				Indices[index++] = startvert+1;
				Indices[index++] = startvert+divs+2;
				Indices[index++] = startvert+divs+1;
			}
		}
	}

	public void Render(GLFunc gl, GLUFunc glu, float FrameInterval)
	{
		gl.glBindTexture(gl.GL_TEXTURE_2D, texture[0]);	
			
		gl.glTranslatef(0.0f,pRadius,0f);
		gl.glRotatef(System.nanoTime()/500000000f,0.0f, 1.0f, 0.0f);

		gl.glBegin(gl.GL_TRIANGLES);
	
		for (int i=0; i < NumIndices; i++)
		{	
	
			gl.glTexCoord2f(PlaneVertices[Indices[i]].u, PlaneVertices[Indices[i]].v);
			gl.glVertex3f(PlaneVertices[Indices[i]].x, PlaneVertices[Indices[i]].y, PlaneVertices[Indices[i]].z);
		}
	
		gl.glEnd();	
	}
	
	public void CreateTexture(GLFunc gl,GLUFunc glu,String domeimg)
	{
		texture[0] = CTextureloader.LoadPng(gl,glu,domeimg);	
	}
	
	public void doCleanup()
	{
		PlaneVertices = null;	
		Indices = null;
	}
}