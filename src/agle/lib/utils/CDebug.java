/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE LIB

                            FILE COCTREE
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.lib.utils;

import gl4java.*;
import java.util.ArrayList;

// GL4Java classes
import gl4java.*;
import gl4java.utils.textures.*;
import gl4java.GLContext;
import gl4java.awt.GLAnimCanvas;

// classes
import agle.lib.*;
import agle.lib.math.*;
import agle.lib.loaders.*;

public class CDebug
{
	GLFunc gl;
	GLUFunc glu;
	boolean g_bLighting = true;
	ArrayList m_vLines = new ArrayList();

	///////////////////////////////// RENDER DEBUG LINES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This goes through all of the lines that we stored in our list and draws them
	/////
	///////////////////////////////// RENDER DEBUG LINES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void RenderDebugLines(GLFunc gl,GLUFunc glu)				// This renders all of the lines
	{
		gl.glDisable(gl.GL_LIGHTING);				// Turn OFF lighting so the debug lines are bright yellow

		gl.glBegin(gl.GL_LINES);					// Start rendering lines

			gl.glColor3f(1.0f, 1.0f, 0.0f);			// Turn the lines yellow

			// Go through the whole list of lines stored in the vector m_vLines.
			for(int i = 0; i < m_vLines.size(); i++)
			{
				// Pass in the current point to be rendered as part of a line
				CVector3 temp = (CVector3)m_vLines.get(i);
				gl.glVertex3f(temp.x, temp.y, temp.z);
			}	

		gl.glEnd();									// Stop rendering lines
		
			gl.glColor3f(1.0f, 1.0f, 1.0f);			// Turn the lines yellow

		// If we have lighting turned on, turn the lights back on
		if(g_bLighting) 
			gl.glEnable(gl.GL_LIGHTING);
	}

	///////////////////////////////// ADD DEBUG LINE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This adds a debug LINE to the stack of lines
	/////
	///////////////////////////////// ADD DEBUG LINE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void AddDebugLine(CVector3 vPoint1, CVector3 vPoint2)
	{
		// Add the 2 points that make up the line into our line list.
		m_vLines.add(vPoint1);
		m_vLines.add(vPoint2);
	}

	///////////////////////////////// ADD DEBUG RECTANGLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This adds a debug RECTANGLE to the stack of lines
	/////
	///////////////////////////////// ADD DEBUG RECTANGLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void AddDebugRectangle(CVector3 vCenter, float width, float height, float depth)
	{
		// So we can work with the code better, we divide the dimensions in half.
		// That way we can create the cube from the center outwards.
		width /= 2.0f;	height /= 2.0f;	depth /= 2.0f;

		// Below we create all the 8 points so it will be easier to input the lines
		// of the cube.  With the dimensions we calculate the points.
		CVector3 vTopLeftFront		= new CVector3(vCenter.x - width, vCenter.y + height, vCenter.z + depth);
		CVector3 vTopLeftBack		= new CVector3(vCenter.x - width, vCenter.y + height, vCenter.z - depth);
		CVector3 vTopRightBack		= new CVector3(vCenter.x + width, vCenter.y + height, vCenter.z - depth);
		CVector3 vTopRightFront		= new CVector3(vCenter.x + width, vCenter.y + height, vCenter.z + depth);

		CVector3 vBottom_LeftFront	= new CVector3(vCenter.x - width, vCenter.y - height, vCenter.z + depth);
		CVector3 vBottom_LeftBack	= new CVector3(vCenter.x - width, vCenter.y - height, vCenter.z - depth);
		CVector3 vBottomRightBack	= new CVector3(vCenter.x + width, vCenter.y - height, vCenter.z - depth);
		CVector3 vBottomRightFront	= new CVector3(vCenter.x + width, vCenter.y - height, vCenter.z + depth);

		////////// TOP LINES ////////// 

		// Store the top front line of the box
		m_vLines.add(vTopLeftFront);		m_vLines.add(vTopRightFront);

		// Store the top back line of the box
		m_vLines.add(vTopLeftBack);  		m_vLines.add(vTopRightBack);

		// Store the top left line of the box
		m_vLines.add(vTopLeftFront);		m_vLines.add(vTopLeftBack);

		// Store the top right line of the box
		m_vLines.add(vTopRightFront);		m_vLines.add(vTopRightBack);

		////////// BOTTOM LINES ////////// 

		// Store the bottom front line of the box
		m_vLines.add(vBottom_LeftFront);	m_vLines.add(vBottomRightFront);

		// Store the bottom back line of the box
		m_vLines.add(vBottom_LeftBack);		m_vLines.add(vBottomRightBack);

		// Store the bottom left line of the box
		m_vLines.add(vBottom_LeftFront);	m_vLines.add(vBottom_LeftBack);

		// Store the bottom right line of the box
		m_vLines.add(vBottomRightFront);	m_vLines.add(vBottomRightBack);

		////////// SIDE LINES ////////// 

		// Store the bottom front line of the box
		m_vLines.add(vTopLeftFront);		m_vLines.add(vBottom_LeftFront);

		// Store the back left line of the box
		m_vLines.add(vTopLeftBack);			m_vLines.add(vBottom_LeftBack);

		// Store the front right line of the box
		m_vLines.add(vTopRightBack);		m_vLines.add(vBottomRightBack);

		// Store the front left line of the box
		m_vLines.add(vTopRightFront);		m_vLines.add(vBottomRightFront);
	}

	///////////////////////////////// CLEAR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This clears all of the debug lines
	/////
	///////////////////////////////// CLEAR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

	public void Clear()						
	{
		// Destroy the list using the standard vector clear() function
		m_vLines.clear();
	}
};
