/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE lib
                      
                            FILE CParticle
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

// Package
package agle.lib.physics;

// Java  classes
import java.io.*;
import java.lang.*;
import java.util.*;

// GL4Java classes
import gl4java.*;
import gl4java.utils.textures.*;

// classes
import agle.lib.*;
import agle.lib.math.*;
import agle.lib.loaders.CTextureloader;

///////////////////////////////// CPARTICLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// CPARTICLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class CParticle extends CMass
{
	// Eredita da Mass :
	// CVector3 		Position;
	// CVector3 		Velocity;
	// CVector3 		Force;
	private CVector3	Acceleration;
	//Spinning the particle
	private float		SpinAngle;		//radian measure
	//Particle's spin speed:
	private float		SpinSpeed;
	//Particle's spin acceleration:
	private float		SpinAcceleration;
	//Particle's alpha value (=transparency)
	private float		Alpha;
	private float		AlphaChange;	//how much is the alpha value changed per sec?
	//Particle's color:
	private CVector3	Color;			//x=r, y=g, z=b
	private CVector3	ColorChange;	//how to change to color per sec
	//Particle's size:  (the use of this value is dependent of UseTexture in the parent!)
	private float		Size;
	private float		SizeChange;	
	//Handling the lifetime:
	private float		DieAge;			//At what "age" will the particle die?
	private float		Age;			//Age of the particle (is updated 
	public  boolean		IsAlive;		//Is the particle active or not? Must be visible for the System
	
	public CParticleSystem ParentSystem;
	
	///////////////////////////////// RANDOM_FLOAT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione genratrice di numeri casuali (compresi tra 0 ed 1)
	/////
	///////////////////////////////// RANDOM_FLOAT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	private float RANDOM_FLOAT() {
		return ((float)Math.random());
	}
	
	///////////////////////////////// CPARTICLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione costruttore della classe
	/////
	///////////////////////////////// CPARTICLE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public CParticle() 
	{
		Position		= new CVector3(0.0f,0.0f,0.0f);
		Velocity		= new CVector3(0.0f,0.0f,0.0f);
		Force			= new CVector3(0.0f,0.0f,0.0f);
		Acceleration	= new CVector3(0.0f,0.0f,0.0f);
		SpinAngle 		= 0.0f;
		SpinSpeed		= 0.0f;
		SpinAcceleration= 0.0f;
		Alpha			= 0.0f;
		AlphaChange		= 0.0f;
		Color			= new CVector3(0.0f,0.0f,0.0f);
		ColorChange		= new CVector3(0.0f,0.0f,0.0f);
		Size			= 0.0f;
		SizeChange		= 0.0f;
		DieAge			= 0.0f;
		Age				= 0.0f;
		IsAlive			= false;
	}
	
	///////////////////////////////// INITIALIZE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione che inizializza una particella
	/////
	///////////////////////////////// INITIALIZE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void Initialize(CParticleSystem pParentSystem)
	{
		ParentSystem = pParentSystem;
		//Calcola l'eta che la particella vivra':
		DieAge = ParentSystem.MinDieAge + ((ParentSystem.MaxDieAge - ParentSystem.MinDieAge)*RANDOM_FLOAT());
		//assicurati che non vi siano divisioni x zero
		if (DieAge == 0.0f)
			return;
		
		Age = 0.0f;

		//Imposta la posizione:
		if (ParentSystem.ParticlesLeaveSystem == true) {
			//inizia usando le coordinate "globali" (le coordinate correnti dell'emettitore)
			Position = new CVector3(ParentSystem.EmitterPosition.x,
									ParentSystem.EmitterPosition.y,
									ParentSystem.EmitterPosition.z);
		} else {
			Position = new CVector3();
		}
		
		//Aggiunge la deviazione dall'emettitore
		Position.x += ParentSystem.MaxCreationDeviation.x * (RANDOM_FLOAT()*2.0f-1.0f);
		Position.y += ParentSystem.MaxCreationDeviation.y * (RANDOM_FLOAT()*2.0f-1.0f);
		Position.z += ParentSystem.MaxCreationDeviation.z * (RANDOM_FLOAT()*2.0f-1.0f);
		//Imposta la velocita' di emissione
		Velocity.x = ParentSystem.StandardEmitDirection.x + ParentSystem.MaxEmitDirectionDeviation.x * (RANDOM_FLOAT()*2.0f-1.0f);
		Velocity.y = ParentSystem.StandardEmitDirection.y + ParentSystem.MaxEmitDirectionDeviation.y * (RANDOM_FLOAT()*2.0f-1.0f);
		Velocity.z = ParentSystem.StandardEmitDirection.z + ParentSystem.MaxEmitDirectionDeviation.z * (RANDOM_FLOAT()*2.0f-1.0f);
		Velocity   = CMath3d.Multiply(Velocity, 
									 (ParentSystem.MinEmitSpeed +
									 (ParentSystem.MaxEmitSpeed - ParentSystem.MinEmitSpeed)
									 * RANDOM_FLOAT()));

		//Imposta il vettore di accelerazione:
		Acceleration = CMath3d.Multiply(ParentSystem.AccelerationDirection,
									   (ParentSystem.MinAcceleration+ 
									   (ParentSystem.MaxAcceleration - ParentSystem.MinAcceleration)
									   * RANDOM_FLOAT()));

		//imposta i valori alpha / color:
		Color = CMath3d.Add(ParentSystem.MinEmitColor, 
				CMath3d.Multiply((
			    CMath3d.Subtract(ParentSystem.MaxEmitColor, ParentSystem.MinEmitColor )),
				RANDOM_FLOAT() ));
		
		//calcola "il colore finale" (in modo da ottendere il cambio di colore):
		CVector3 EndColor;
		EndColor = CMath3d.Add(ParentSystem.MinDieColor, 
				   CMath3d.Multiply(
				   CMath3d.Subtract(ParentSystem.MaxDieColor, ParentSystem.MinDieColor),
				   RANDOM_FLOAT() ));

		ColorChange = CMath3d.Divide(CMath3d.Subtract(EndColor,Color) ,DieAge);

		Alpha = ParentSystem.MinEmitAlpha + 
			  ((ParentSystem.MaxEmitAlpha - ParentSystem.MinEmitAlpha)
			  * RANDOM_FLOAT() );

		float EndAlpha;
		EndAlpha = ParentSystem.MinDieAlpha  +
				 ((ParentSystem.MaxDieAlpha - ParentSystem.MinDieAlpha)
				 * RANDOM_FLOAT());

		AlphaChange = (EndAlpha - Alpha) / DieAge;

		//set the size values:
		Size = ParentSystem.MinEmitSize + 
			 ((ParentSystem.MaxEmitSize - ParentSystem.MinEmitSize)
			 * RANDOM_FLOAT());

		float EndSize;
		EndSize = ParentSystem.MinDieSize + 
				((ParentSystem.MaxDieSize - ParentSystem.MinDieSize)
				* RANDOM_FLOAT());

		SizeChange = (EndSize - Size) / DieAge;

		//spin values:
		SpinAngle = 0.0f;
		SpinSpeed = ParentSystem.MinEmitSpinSpeed + 
				  ((ParentSystem.MaxEmitSpinSpeed - ParentSystem.MinEmitSpinSpeed)
				  * RANDOM_FLOAT());
  
		SpinAcceleration = ParentSystem.MinSpinAcceleration + 
						 ((ParentSystem.MaxSpinAcceleration - ParentSystem.MinSpinAcceleration)
						 * RANDOM_FLOAT());

		//Ok, we're done:
		IsAlive = true;
	}
	
	///////////////////////////////// UPDATE\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione che aggioran la particella
	/////
	///////////////////////////////// UPDATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void Update(float timePassed)
	{
		//Update all time-dependent values:
		Age += timePassed;
		if (Age >= DieAge) 
		{
			if (ParentSystem.RecreateWhenDied) 
			{
				Initialize(ParentSystem);
				Update(RANDOM_FLOAT() * timePassed);  //see comment in UpdateSystem
			}
			else
			{
				Age = 0.0f;
				IsAlive = false;
				ParentSystem.ParticlesInUse--;
			}
		return;
		}

		Size  += SizeChange *timePassed;
		Alpha += AlphaChange*timePassed;
		Color = CMath3d.Add(Color,CMath3d.Multiply(ColorChange,timePassed));
		Velocity = CMath3d.Add(Velocity,CMath3d.Multiply(Acceleration,timePassed));
		//Note: exact would be: Position = 1/2*Acceleration*timePassed² + VelocityOLD*timePassed;
		//But this approach is ok, I think!
		Position = CMath3d.Add(Position ,CMath3d.Multiply(Velocity,timePassed));

		SpinSpeed += SpinAcceleration*timePassed;
		SpinAngle += SpinSpeed*timePassed;

	//That's all!
	}
	
	///////////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione che renderizza a schermo la particella
	/////
	///////////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void Render(GLFunc gl,GLUFunc glu)
	{
		if (!ParentSystem.UseTexture) 
		{ 
			gl.glPointSize(Size*ParentSystem.CurrentPointSize[0]);
			float color[] = new float[4];
			color[0] = Color.x;
			color[1] = Color.y;
			color[2] = Color.z;
			color[3] = Alpha;
    	
			gl.glColor4f(color[0], color[1], color[2], color[3]);
    	
			gl.glBegin(gl.GL_POINTS);
			gl.glVertex3f(Position.x,Position.y,Position.z);
			gl.glEnd();
		}
		else
		{
			//render using texture: (texture was already set active by the Render method of the particle system)
			float color[] = new float[4];
			color[0] = Color.x;
			color[1] = Color.y;
			color[2] = Color.z;
			color[3] = Alpha;
			gl.glColor4f(color[0], color[1], color[2], color[3]);
    	
			CVector3 RotatedX = ParentSystem.BillboardedX;
			CVector3 RotatedY = ParentSystem.BillboardedY;
    	
		   //If spinning is switched on, rotate the particle now:
			if (SpinAngle > 0.0f)
			{
				RotatedX = CMath3d.Add( CMath3d.Multiply( ParentSystem.BillboardedX , (float)Math.cos( SpinAngle )),
					       CMath3d.Multiply( ParentSystem.BillboardedY , (float)Math.sin(SpinAngle)));
				RotatedY = CMath3d.Subtract( CMath3d.Multiply( ParentSystem.BillboardedY , (float)Math.cos( SpinAngle )),
					       CMath3d.Multiply( ParentSystem.BillboardedX , (float)Math.sin(SpinAngle)));
			}
			
			//Render a quadrangle with the size fSize
			CVector3 coords = CMath3d.Subtract( CMath3d.Subtract ( Position , CMath3d.Multiply(RotatedX,(0.5f*Size))),
																			  CMath3d.Multiply(RotatedY,(0.5f*Size)));
			gl.glBegin(gl.GL_POLYGON);
			  gl.glVertex3f(coords.x,coords.y,coords.z); gl.glTexCoord2f(0.0f,1.0f);
			  coords = CMath3d.Add(coords,(CMath3d.Multiply(RotatedY,Size)));
			  gl.glVertex3f(coords.x,coords.y,coords.z); gl.glTexCoord2f(1.0f,1.0f);
			  coords = CMath3d.Add(coords,(CMath3d.Multiply(RotatedX,Size)));		  
			  gl.glVertex3f(coords.x,coords.y,coords.z); gl.glTexCoord2f(1.0f,0.0f);
			  coords = CMath3d.Subtract(coords,(CMath3d.Multiply(RotatedY,Size)));
			  gl.glVertex3f(coords.x,coords.y,coords.z); gl.glTexCoord2f(0.0f,0.0f);
			gl.glEnd();
		}
	}
}