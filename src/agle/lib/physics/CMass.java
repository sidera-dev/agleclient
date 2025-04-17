/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE agle.lib

                            FILE CFACE
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
package agle.lib.physics;

//  classes
import agle.lib.math.*;

///////////////////////////////// CMath3d \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
/////
/////	Un oggetto che rappresenta la massa
/////
///////////////////////////////// CMath3d \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
public class CMass
{
	public float m;				// The mass value
	public CVector3 Position;	// Position in space
	public CVector3 Velocity;	// Velocity
	public CVector3 Force;		// Force applied on this mass at an instance

	public CMass()				// Constructor
	{
		m = 0f;							// The mass value
		Position = new CVector3();		// Position in space
		Velocity = new CVector3();		// Velocity
		Force 	 = new CVector3();		// Force applied on this mass at an instance
	}
	
	public CMass(float m)				// Constructor
	{
		this.m = m;
		Position = new CVector3();		// Position in space
		Velocity = new CVector3();		// Velocity
		Force 	 = new CVector3();		// Force applied on this mass at an instance
	}
	
	public void InitMass()
	{
		Force = new CVector3(0.0f,0.0f,0.0f);
	}
	
	public void ApplyForce(CVector3 force)
	{
		Force = CMath3d.Add(Force, force); // The external force is added to the force of the mass
	}
	
	public void UpdateMass(float FrameInterval)
	{
		// Change in velocity is added to the velocity.
		// The change is proportinal with the acceleration (force / m) and change in time
		Velocity = CMath3d.Add( CMath3d.Multiply( CMath3d.Divide(Force, m), FrameInterval), Velocity);
		// Change in position is added to the position.
		// Change in position is velocity times the change in time
		Position = CMath3d.Add(Position, (CMath3d.Multiply(Velocity, FrameInterval)));
	} 
}