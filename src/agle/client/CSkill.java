/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE CLIENT

                            FILE CSKILL
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.client;

// Java classes
import java.lang.*;
import java.util.*;
import java.io.*;

// GL4Java classes
import gl4java.*;
import gl4java.utils.textures.*;

import agle.lib.loaders.*;
import agle.lib.math.*;
import agle.lib.utils.*;
import agle.lib.font.CGLFont;

///////////////////////////////// CSKILL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// CSKILL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class CSkill extends CForm
{
	/* Position, velocity and force from CMass
	float			m;
	CVector3		Position;
	CVector3		Velocity;
	CVector3		Force; */
	
	public String Name;
	
	int				delta_m;
	// Livello
	byte			delta_Level;
	// Stats
	short			delta_Age; 
	short			delta_Str;
	short			delta_Intl;
	short			delta_Dex;
	short			delta_Car;   
	short			delta_Faith;
	String			delta_Deity;
	short			delta_Glory;
	short			delta_All;
	short			delta_Lif_E;
	short			delta_Men_E;
	short			delta_Fis_E;

	// Flags
	boolean			delta_Visible;
	boolean			delta_Moving;
	boolean			delta_Standing;
	boolean			delta_Walking;
	boolean			delta_Running;
	boolean			delta_Jumping;
	boolean			delta_Climbing;
	boolean			delta_Swimming;
	boolean			delta_RSideWalk;
	boolean			delta_LSideWalk;	
	
			
	// skill delta position
	CVector3 delta_Position;
	// skill delta velocity
	CVector3 delta_Velocity;
	// skill delta force
	CVector3 delta_Force;
	
	// This is for collisione detection
	public CBoundingSphere	Sphere;
    
	// Creates a new instance of Skill, pass a name a radius and a position
	public CSkill()
	{
		// Mass
		delta_m =			0; 
		m		=			1;
		// Livel       
		delta_Level =		0;
		// Stats
		delta_Faith =		0;
		delta_Glory =		0;       
		delta_Str =			0;
		delta_Intl = 		0;
		delta_Dex =			0;
		delta_Car =			0;
		delta_All =			0;
		delta_Lif_E =		0;
		delta_Men_E =		0;
		delta_Fis_E =		0;
  
		// Flags
		delta_Visible =		false;
		delta_Moving =		false;
		delta_Standing =	true;
		delta_Walking =		false;
		delta_Running =		false;
		delta_Jumping =		false;
		delta_Climbing =	false;
		delta_Swimming =	false;
		delta_RSideWalk =	false;
		delta_LSideWalk =	false;	
		
		// Start skill position
		Position =		new CVector3(0.0f,10.0f,0.0f);
		// Initial skill velocity
		Velocity =		new CVector3(0.0f,0.0f,0.0f);
		// Initial force over skill
		Force	 =		new CVector3(0.0f,0.0f,0.0f);
		
		delta_Position =	new CVector3(0.0f,10.0f,0.0f);		                        	
		delta_Velocity =	new CVector3(0.0f,0.0f,0.0f);		                        	
		delta_Force	 =		new CVector3(0.0f,0.0f,0.0f);		
		
		Sphere = new CBoundingSphere();
		Sphere.SetRadius(1.0f);
		Sphere.SetPosition(new CVector3(0.0f,0.0f,0.0f));
	}
	
	public void UpdateMass(float FrameInterval)
	{	
		// Change in velocity is added to the velocity.
		// The change is proportinal with the acceleration (force / m) and change in time
		Velocity = CMath3d.Add( CMath3d.Multiply( CMath3d.Divide(Force, m), FrameInterval), Velocity);
		// Change in position is added to the position.
		// Change in position is velocity times the change in time
		//Position = CMath3d.Add(Position, (CMath3d.Multiply(Velocity, CGlobals.FrameInterval)));
		Position.x += Velocity.x * FrameInterval;
		Position.y += Velocity.y * FrameInterval;
		Position.z += Velocity.z * FrameInterval;		
	}	
	
	public void Update(float FrameInterval)	
	{
		UpdateMass(FrameInterval);		
		Sphere.Update(Position);	
		InitMass();							
	}	
	
	public void Render(GLFunc gl, GLUFunc glu) 
	{		
		Sphere.RenderBoundingSphere(gl,glu);				
	}	   
}
