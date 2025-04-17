/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE CLIENT

                            FILE EXIT
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

///////////////////////////////// EXIT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// EXIT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class CExit extends CUnlivingform
{   
	/* Position vector,velocity', force and Mass extended from CMass
	float			m;
	CVector3		Position;
	CVector3		Velocity;
	CVector3		Force; */
	
	float 			Width;
	
	// rotations
	float			Rotation;
	float[]			SinTable;
	float[]			CosTable;
	// 2 times PI.
	double AR_2PI;
	// PI divided by 180.
	double AR_PI_DIV_180;
	
	// collision check
	public CBoundingSphere	Sphere;

	public  CClampingLine	GroundLine;	
	
	// This holds the 3D Model info that we load in
	C3DModelLoader2 Model;
	
	/*dimensions extended from CFrom;
	float			dimX;
	float			dimY;
	float			dimZ;  */  
	
	// exit propites
	// String		Name   (ereditato da CForm)
	CVector3		ExitTo;
	String 			ToLand;
	String 			ToZone;

	// Flags
	boolean			Visible;
	boolean			Moving;
	boolean			Standing;

    // Creates a new instance of CZones 
	public CExit(String eName,CVector3 eFrom,CVector3 eTo,String eToLand,String eToZone)
	{
		// Mass
		m =				1f;
		
		// exit dimensions 1m x 1m x 2m
		dimX =			1f;
		dimY =			1f;
		dimZ =			1f;
		// exit porp
		Name =			eName;
		ExitTo =		eTo;
		ToLand =		eToLand;
		ToZone =		eToZone;
		// Flags
		Visible =		false;
		Moving =		false;
		Standing =		true;	// Inizilizziamo il personaggio fermo
		 
		// initial exit position 
		Position =		eFrom;
		// initial exit velocity
		Velocity =		new CVector3(0.0f,0.0f,0.0f);
		// initial exit force
		Force	 =		new CVector3(0.0f,0.0f,0.0f);
		
		// rotation
		Rotation =		AR_DegToRad(0f);
		// 2 times PI.
		AR_2PI 	 = 		6.28318530717958647692;
		// PI divided by 180.
		AR_PI_DIV_180 = 0.017453292519943296;
		// sub and cos
		SinTable = 		new float[360];
		CosTable = 		new float[360];
		for ( int i = 0; i < 360; i++ )
		{
			SinTable[i] = (float)Math.sin( (double)AR_DegToRad( i ) );
			CosTable[i] = (float)Math.cos( (double)AR_DegToRad( i ) );
		}
		
		Width = 1.0f;
		
		// collision check
		// init collision spher for collision
		Sphere = new CBoundingSphere();
		Sphere.SetRadius(Width/2f);
		Sphere.SetPosition(Position);
			
		// init clamping lin for ground collision
		GroundLine = new CClampingLine();
		GroundLine.GroundLine =		new CVector3[2];
		GroundLine.GroundLine[0] =	new CVector3( 0f, Width , 0f);
		GroundLine.GroundLine[1] =	new CVector3( 0f, -Width, 0f);
		// init clamping lin for ground collision respect exit coordinates
		GroundLine.PGroundLine =	new CVector3[2];
		GroundLine.PGroundLine[0] = CMath3d.Add(Position, GroundLine.GroundLine[0]);
		GroundLine.PGroundLine[1] = CMath3d.Add(Position, GroundLine.GroundLine[1]);
	}
	
	public float AR_RadToDeg(float x)
	{
		return(float)(x * 57.2957795130823229);
	}
	
	public float AR_DegToRad(float x)
	{
		return(float)(x * 0.017453292519943296);
	}
	
	public void RotatePlayer(float turnrate, float FrameInterval)
	{
		Rotation += AR_DegToRad( turnrate ) * FrameInterval;
	}
	
	public void UpdateMass(float FrameInterval)
	{	
		// Change in velocity is added to the velocity.
		// The change is proportinal with the acceleration (force / m) and change in time
		Velocity = CMath3d.Add( CMath3d.Multiply( CMath3d.Divide(Force, m), FrameInterval), Velocity);
		// Change in position is added to the position.
		// Change in position is velocity times the change in time
		//Position = CMath3d.Add(Position, (CMath3d.Multiply(Velocity, CGlobals.FrameInterval)));		
		Position.x += (SinTable[(int)AR_RadToDeg( Rotation )] * Velocity.x) * FrameInterval;
		Position.y +=  Velocity.y * FrameInterval;
		Position.z += (CosTable[(int)AR_RadToDeg( Rotation )] * Velocity.z) * FrameInterval;
		
		// Update all collision positions
		GroundLine.SetPosition(Position);
		
		Sphere.Update(Position);
	}
    
	///////////////////////////////// UPDATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	function wich update exit
	/////
	///////////////////////////////// UPDATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void Update(float FrameInterval,CZone Zone)
	{	
		// Clamp values above 2 * PI or 360 Deg's.
		if ( Rotation >= AR_2PI )
			Rotation = Rotation - (float)AR_2PI;
	
		// Clamp values below 0.
		if ( Rotation < 0.0f )
			Rotation = (float)AR_2PI + Rotation;
			
		// Set the force on the mass to zero and afeter that  update clampingline
		UpdateMass(FrameInterval);
		GroundLine.UpdateClampingLine();
		InitMass();
		// Calculate bounding sphere in camera frustum and set if the exit is visible 
		if( Zone.Octree.Frustum.SphereInFrustum( Position.x,Position.y,Position.z, Sphere.Radius() ) ){
			if( Visible == false ){
				Visible = true;
			}
		} else if( Visible == true ){
				Visible = false;
		}
		// Process collision and forces
		// If the exit sphere collide calulate offest and set both
		if( Zone.Octree.IntersectSphereWithOctree(Zone.Octree, Zone.Landscape, Sphere) ){
			Position.x = Sphere.Position.x + Sphere.vOffset().x;
			Position.z = Sphere.Position.z + Sphere.vOffset().z;
			Sphere.Position.x = Position.x; 
			Sphere.Position.z = Position.z; 
			GroundLine.Position.x = Position.x;
			GroundLine.Position.z = Position.z;
			
			// Se la line del suolo su cui e' il giocatore collide con il suolo...
			// Calcola l'offset e sposta  entrambi
			// Azzera la velocita' impressa al giocatore dalla gravita' (e' gia' a terra)
			if( Zone.Octree.IntersectLineWithOctree(Zone.Octree, Zone.Landscape, GroundLine ) ){
				Position.y = GroundLine.GetCollidingPoint().y + Width/2;
				GroundLine.Position.y = Position.y;
				
				Sphere.Position.y = Position.y;
				
				Velocity = new CVector3(Velocity.x,0.0f,Velocity.z);
			}			
			// Se il l'uscita non collide con il suolo...	*/
		// Calcola la velocita' impressa dalla gravita' e poi applica lo spostamento all uscita.
		} else {
			ApplyForce(CMath3d.Multiply(Zone.Gravity,m) );
		} 
		// Se il la velocita' e' diversa zero (c'e' movimento), applica le forze di resistenza
		// al movimento (la velocita' viene moltiplicata per le costanti difrizione dell'aria e della terra)
		if (Velocity != new CVector3()){
			if(Moving == false){
				Moving = true;
			}
			ApplyForce(CMath3d.Multiply((CMath3d.Negate(Velocity)),Zone.AFrictionConst));
			CVector3 vel = new CVector3(Velocity.x,0.0f,Velocity.z);
			ApplyForce(CMath3d.Multiply((CMath3d.Negate(vel)),Zone.GFrictionConst));
		} else {
			if(Moving == true){
				Moving = false;
			}
		}
	}
	
	///////////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Draw exits on screen
	/////
	///////////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void Render(GLFunc gl,GLUFunc glu)
	{
		if (Visible == true){
			gl.glPushMatrix();
				// Trasla l'uscita 
				gl.glTranslatef(Position.x, Position.y, Position.z);
				gl.glPushMatrix();
					gl.glRotatef(-90f,0.0f,1.0f,0.0f);				
					gl.glRotatef(AR_RadToDeg(Rotation),0.0f,1.0f,0.0f);
					gl.glScalef(0.1f,0.1f,0.1f);
					gl.glEnable(gl.GL_CULL_FACE);
					gl.glCullFace(gl.GL_FRONT);
					// Disegna l'uscita
					gl.glDisable(gl.GL_CULL_FACE); 
				gl.glPopMatrix();
			gl.glPopMatrix();
				
			Sphere.RenderBoundingSphere(gl,glu);
		}
	}
	
	///////////////////////////////// SETPOSITION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Set the exti position
	/////
	///////////////////////////////// SETPOSITION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void SetPosition(CVector3 pPosition)
	{
		Position = new CVector3(pPosition.x,pPosition.y,pPosition.z);
	}
	
	//////////////////////////////// POSITION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	This function permit to acess to extis pricate data, position
	/////
	/////////7////////////////////// POSITION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public CVector3 Position()
	{
		return new CVector3(Position.x,Position.y,Position.z);
	}
	
}
