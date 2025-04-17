/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE agle.lib

                            FILE CCamera
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.lib.camera;

// Java classes
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.*;
import java.util.*;

// GL4Java classes
import gl4java.*;
import gl4java.utils.textures.*;

// classes
import agle.lib.*;
import agle.lib.math.*;
import agle.lib.font.*;

///////////////////////////////// CCAMERA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// CCAMERA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class CCamera implements KeyListener
{
	boolean[] keys;
	                        	
	CVector3 m_vPosition;		// Init the position to zero
	CVector3 m_vView;			// Init the view to a standard starting view
	CVector3 m_vUpVector;		// Init the UpVector
	CVector3 m_vStrafe;			// Our strafe vector
	
	///////////////////////////////// CAMERA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Funzione chiamata nell'istanziazione della classe
	/////
	///////////////////////////////// CAMERA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public CCamera()	
	{		
		keys=new boolean[256];
	
		m_vPosition	= 	new CVector3(0.0f, 20.0f, -20.0f);
		m_vView		=	new CVector3(0.0f, 1.0f, 1.5f);
		m_vUpVector	=	new CVector3(0.0f, 1.0f, 0.0f);
		m_vStrafe =		new CVector3();
	}
	
	////////////////////// POSITION VIEW UPVECTOR STRAFE \\\\\\\\\\\\\\\\\\\\\
	/////
	/////	This function permit to access to the camera private data
	/////
	////////////////////// POSITION VIEW UPVECTOR STRAFE \\\\\\\\\\\\\\\\\\\\\
	public CVector3 Position(){
			return new CVector3(m_vPosition.x,m_vPosition.y,m_vPosition.z);
	}
	public CVector3 View(){
			return new CVector3(m_vView.x,m_vView.y,m_vView.z);
	}
	public CVector3 UpVector(){
			return new CVector3(m_vUpVector.x,m_vUpVector.y,m_vUpVector.z);	
	}
	public CVector3 Strafe(){
			return new CVector3(m_vStrafe.x,m_vStrafe.y,m_vStrafe.z);
	}
	
	///////////////////////////////// POSITION CAMERA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	This function sets the camera's position and view and up vector.
	/////
	///////////////////////////////// POSITION CAMERA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void SetPosition( float positionX, float positionY, float positionZ,
							 float viewX,     float viewY,     float viewZ,
							 float upVectorX, float upVectorY, float upVectorZ )
	{
		CVector3 vPosition	= new CVector3(positionX, positionY, positionZ);
		CVector3 vView		= new CVector3(viewX, viewY, viewZ);
		CVector3 vUpVector	= new CVector3(upVectorX, upVectorY, upVectorZ);

		m_vPosition = vPosition;						// Assign the position
		m_vView		= vView;							// Assign the view
		m_vUpVector = vUpVector;						// Assign the up vector
	}
	
	///////////////////////////////// POSITION CAMERA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	This function sets the camera's position and view and up vector.
	/////
	///////////////////////////////// POSITION CAMERA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void SetPositionv( CVector3 position, CVector3 view, CVector3 upVector )
	{
		CVector3 vPosition	= new CVector3(position.x, position.y, position.z);
		CVector3 vView		= new CVector3(view.x, view.y, view.z);
		CVector3 vUpVector	= new CVector3(upVector.x, upVector.y, upVector.z);

		m_vPosition = vPosition;						// Assign the position
		m_vView		= vView;							// Assign the view
		m_vUpVector = vUpVector;						// Assign the up vector
	}
	
	///////////////////////////////// ROTATE VIEW \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This rotates the view around the position using an axis-angle rotation
	/////
	///////////////////////////////// ROTATE VIEW \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void RotateView(float angle, float x, float y, float z)
	{
		CVector3 vNewView = new CVector3();
		CVector3 vView = new CVector3();	

		// Get our view vector (The direciton we are facing)
		vView.x = m_vView.x - m_vPosition.x;		// This gets the direction of the X	
		vView.y = m_vView.y - m_vPosition.y;		// This gets the direction of the Y
		vView.z = m_vView.z - m_vPosition.z;		// This gets the direction of the Z

		// Calculate the sine and cosine of the angle once
		float cosTheta = (float)Math.cos(angle);
		float sinTheta = (float)Math.sin(angle);

		// Find the new x position for the new rotated point
		vNewView.x  = (cosTheta + (1 - cosTheta) * x * x)		* vView.x;
		vNewView.x += ((1 - cosTheta) * x * y - z * sinTheta)	* vView.y;
		vNewView.x += ((1 - cosTheta) * x * z + y * sinTheta)	* vView.z;

		// Find the new y position for the new rotated point
		vNewView.y  = ((1 - cosTheta) * x * y + z * sinTheta)	* vView.x;
		vNewView.y += (cosTheta + (1 - cosTheta) * y * y)		* vView.y;
		vNewView.y += ((1 - cosTheta) * y * z - x * sinTheta)	* vView.z;

		// Find the new z position for the new rotated point
		vNewView.z  = ((1 - cosTheta) * x * z - y * sinTheta)	* vView.x;
		vNewView.z += ((1 - cosTheta) * y * z + x * sinTheta)	* vView.y;
		vNewView.z += (cosTheta + (1 - cosTheta) * z * z)		* vView.z;

		// Now we just add the newly rotated vector to our position to set
		// our new rotated view of our camera.
		m_vView.x = m_vPosition.x + vNewView.x;
		m_vView.y = m_vPosition.y + vNewView.y;
		m_vView.z = m_vPosition.z + vNewView.z;
	}

	///////////////////////////////// QROTATE VIEW \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This rotates the view around the position using an axis-angle rotation
	/////	and the auxilius of quaternions
	/////
	///////////////////////////////// QROTATE VIEW \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void QRotateView(float angle, float x, float y, float z)
	{
		CQuaternion tmpQuat = new CQuaternion();
		CQuaternion QuatView = new CQuaternion();
		CQuaternion ResQuat = new CQuaternion();
		CVector3 vView = new CVector3();	

		// Get our view vector (The direciton we are facing)
		vView.x = m_vView.x - m_vPosition.x;		// This gets the direction of the X	
		vView.y = m_vView.y - m_vPosition.y;		// This gets the direction of the Y
		vView.z = m_vView.z - m_vPosition.z;		// This gets the direction of the Z
		
		// Calculate the sine and cosine of the angle once
		float cosTheta = (float)Math.cos(angle/2);
		float sinTheta = (float)Math.sin(angle/2);
		
		tmpQuat.x = x * sinTheta;
		tmpQuat.y = y * sinTheta;
		tmpQuat.z = z * sinTheta;
		tmpQuat.w = cosTheta;
		
		QuatView.x = vView.x;
		QuatView.y = vView.y;
		QuatView.z = vView.z;
		QuatView.w = 0.0f;
		
		ResQuat = CMath3d.QuatMultiply( CMath3d.QuatMultiply(tmpQuat,QuatView),
										CMath3d.Conjugate(tmpQuat));		
		
		m_vView.x = m_vPosition.x + ResQuat.x;
		m_vView.y = m_vPosition.y + ResQuat.y;
		m_vView.z = m_vPosition.z + ResQuat.z;
	}
	
	///////////////////////////////// MOVE CAMERA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	This will move the camera forward or backward depending on the speed
	/////
	///////////////////////////////// MOVE CAMERA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void Move(float speed)
	{
		CVector3 vVector= new CVector3();			// Init a vector for our view

		// Get our view vector (The direciton we are facing)
		vVector.x = m_vView.x - m_vPosition.x;		// This gets the direction of the X	
		vVector.y = m_vView.y - m_vPosition.y;		// This gets the direction of the Y
		vVector.z = m_vView.z - m_vPosition.z;		// This gets the direction of the Z

		vVector = CMath3d.Normalize(vVector);
		
		m_vPosition.x += vVector.x * speed;		// Add our acceleration to our position's X
		m_vPosition.z += vVector.z * speed;		// Add our acceleration to our position's Z
		m_vView.x += vVector.x * speed;			// Add our acceleration to our view's X
		m_vView.z += vVector.z * speed;			// Add our acceleration to our view's Z
	}
		
	///////////////////////////////// ROTATE AROUND POINT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This rotates the position around a given point
	/////
	///////////////////////////////// ROTATE AROUND POINT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void RotateAroundPoint(CVector3 vCenter, float angle, float x, float y, float z)
	{
		CVector3 vNewPosition = new CVector3();			

		// To rotate our position around a point, what we need to do is find
		// a vector from our position to the center point we will be rotating around.
		// Once we get this vector, then we rotate it along the specified axis with
		// the specified degree.  Finally the new vector is added center point that we
		// rotated around (vCenter) to become our new position.  That's all it takes.

		// Get the vVector from our position to the center we are rotating around
		CVector3 vPos = CMath3d.Subtract(m_vPosition , vCenter);

		// Calculate the sine and cosine of the angle once
		float cosTheta = (float)Math.cos(angle);
		float sinTheta = (float)Math.sin(angle);

		// Find the new x position for the new rotated point
		vNewPosition.x  = (cosTheta + (1 - cosTheta) * x * x)		* vPos.x;
		vNewPosition.x += ((1 - cosTheta) * x * y - z * sinTheta)	* vPos.y;
		vNewPosition.x += ((1 - cosTheta) * x * z + y * sinTheta)	* vPos.z;

		// Find the new y position for the new rotated point
		vNewPosition.y  = ((1 - cosTheta) * x * y + z * sinTheta)	* vPos.x;
		vNewPosition.y += (cosTheta + (1 - cosTheta) * y * y)		* vPos.y;
		vNewPosition.y += ((1 - cosTheta) * y * z - x * sinTheta)	* vPos.z;

		// Find the new z position for the new rotated point
		vNewPosition.z  = ((1 - cosTheta) * x * z - y * sinTheta)	* vPos.x;
		vNewPosition.z += ((1 - cosTheta) * y * z + x * sinTheta)	* vPos.y;
		vNewPosition.z += (cosTheta + (1 - cosTheta) * z * z)		* vPos.z;

		// Now we just add the newly rotated vector to our position to set
		// our new rotated position of our camera.
		m_vPosition = CMath3d.Add(vCenter , vNewPosition);
	}
	
	///////////////////////////////// STRAFE CAMERA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This strafes the camera left and right depending on the speed (-/+)
	/////
	///////////////////////////////// STRAFE CAMERA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void Strafe(float speed)
	{
		// Initialize a variable for the cross product result
		CVector3 vCross = CMath3d.Cross(CMath3d.Subtract(m_vView , m_vPosition), m_vUpVector);
		// Normalize the strafe vector
		m_vStrafe = CMath3d.Normalize(vCross);
		// Strafing is quite simple if you understand what the cross product is.
		// If you have 2 vectors (say the up vVector and the view vVector) you can
		// use the cross product formula to get a vVector that is 90 degrees from the 2 vectors.
		// For a better explanation on how this works, check out the OpenGL "Normals" tutorial at our site.
		// In our new Update() function, we set the strafing vector (m_vStrafe).  Due
		// to the fact that we need this vector for many things including the strafing
		// movement and camera rotation (up and down), we just calculate it once.
		//
		// Like our MoveCamera() function, we add the strafing vector to our current position 
		// and view.  It's as simple as that.  It has already been calculated in Update().
		
		// Add the strafe vector to our position
		m_vPosition.x += m_vStrafe.x * speed;
		m_vPosition.z += m_vStrafe.z * speed;

		// Add the strafe vector to our view
		m_vView.x += m_vStrafe.x * speed;
		m_vView.z += m_vStrafe.z * speed;
	}
	
	///////////////////////////////// FOLLOWTARGET \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function permit to the camera to follow a target object
	/////
	///////////////////////////////// FOLLOWTAGET \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void FollowTarget(CVector3 PosTarget)
	{
		CVector3 DeltaView = new CVector3();
		DeltaView = CMath3d.Subtract(m_vView,PosTarget);
		m_vPosition = CMath3d.Subtract(m_vPosition,DeltaView);
		m_vView = PosTarget;		
	}
	
	///////////////////////////////// UPDATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This updates the camera's view and strafe vector
	/////
	///////////////////////////////// UPDATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	void Update() 
	{		
		// In this way keyborad press is getted faster
		ProcessKeys();
	}
	
	///////////////////////////////// LOOK \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This funciotn update camera
	/////
	///////////////////////////////// LOOK \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void Look(GLFunc gl, GLUFunc glu)
	{
		// Give openGL our camera position, then camera view, then camera up vector
		glu.gluLookAt(m_vPosition.x, m_vPosition.y, m_vPosition.z,
					  m_vView.x,	 m_vView.y,     m_vView.z,	
					  m_vUpVector.x, m_vUpVector.y, m_vUpVector.z);
	}
	
	///////////////////////////////// KEYBOARD \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function manage keyboard camera input 
	/////
	///////////////////////////////// KEYBOARD \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void keyTyped(KeyEvent e)
	{
	}
   
	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode()<250)
		{
			keys[e.getKeyCode()]=true;
			// Process Keyboard Results
		}
	}
   
	public void keyReleased(KeyEvent e)
	{
		if(e.getKeyCode()<250)  // only interested in first 250 key codes, are there more?
			 keys[e.getKeyCode()]=false;
	}
	
	public void ProcessKeys(){}

}