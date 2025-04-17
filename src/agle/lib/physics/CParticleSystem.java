/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE lib

                            FILE CParticleSystem
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
import gl4java.GLContext;
import gl4java.awt.GLAnimCanvas;

// classes
import agle.lib.*;
import agle.lib.math.*;
import agle.lib.loaders.CTextureloader;

///////////////////////////////// CPARTICLESYSTEM \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// CPARTICLESYSTEM \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class CParticleSystem 
{
	//Position of the emitter:
	public CVector3		EmitterPosition;
	//How far may the particles be created from the emitter?
	public CVector3	 	MaxCreationDeviation;   //3 positive values. Declares the possible distance from the emitter
												// Distance can be between -MaxCreationDeviation.? and +MaxCreationDeviation.?
	//Which direction are the particles emitted to?
	public CVector3		StandardEmitDirection;
	public CVector3		MaxEmitDirectionDeviation; //Works like MaxCreationDeviation

	//Which speed do they have when they are emitted? Somewhere between these speeds:
	public float		MinEmitSpeed;
	public float		MaxEmitSpeed;

	//How fast do they spin when being emitted? Speed here is angle speed (radian measure) per sec
	public float		MinEmitSpinSpeed;
	public float		MaxEmitSpinSpeed;
	//Spinning acceleration:
	public float		MinSpinAcceleration;
	public float		MaxSpinAcceleration;

	//The acceleration vector always has the same direction (normally (0/-1/0) for gravity):
	public CVector3		AccelerationDirection;
	//...but not the same amount:
	public float		MinAcceleration;
	public float		MaxAcceleration;
	
	//How translucent are the particles when they are created?
	public float		MinEmitAlpha;
	public float		MaxEmitAlpha;
	//How translucent are the particles when they have reached their dying age?
	public float		MinDieAlpha;
	public float		MaxDieAlpha;

	//How big are the particles when they are created / when they die
	public float		MinEmitSize;
	public float		MaxEmitSize;
	public float		MinDieSize;
	public float		MaxDieSize;

	//The same with the color:
	public CVector3		MinEmitColor;
	public CVector3		MaxEmitColor;
	public CVector3		MinDieColor;
	public CVector3		MaxDieColor;

	///////////////////////////////
	// OTHER PARTICLE INFORMATION
	///////////////////////////////

	//How long shall the particles live? Somewhere (randomly) between:
	public float		MinDieAge;
	public float		MaxDieAge;

	public boolean		RecreateWhenDied;  //Set it true so a particle will be recreate itsself as soon
								   //as it died

	///////////////////////////////
	// RENDERING PROPERTIES
	///////////////////////////////	

	public int			Billboarding;		//See the constants above
	
	public int[]		Texture;		    //Pointer to the texture (which is only an "alpha texture")	
	public boolean		UseTexture;			//Set it false if you want to use GL_POINTS as particles!

	public boolean		ParticlesLeaveSystem;//Switch it off if the particle's positions 
									 		//shall be relative to the system's position (emitter position)
	///////////////////////////////
	// STORING THE PARTICLES
	///////////////////////////////	
	//Particle array:
	public CParticle[]	Particles;
	//Maximum number of particles (assigned when reserving mem for the particle array)
	public int			MaxParticles;
	//How many particles are currently in use?
	public int			ParticlesInUse;
	//How many particles are created per second?
	//Note that this is an average value and if you set it too high, there won't be
	//dead particles that can be created unless the lifetime is very short and/or 
	//the array of particles (Particles) is big 
	public int			ParticlesCreatedPerSec;  //if bRecreateWhenDied is true, this is the ADDITIONAL number of created particles!
	public float		CreationVariance; //Set it 0 if the number of particles created per sec 
								  //should be the same each second. Otherwise use a positive value:
								  //Example: 1.0 affects that the NumParticlesCreatedPerSec varies 
								  //between ParticlesCreatedPerSec/2 and 1.5*ParticlesCreatedPerSec
	//Do not set these values:
	public float[]		CurrentPointSize;  //required when rendering without particles
	//If Billboarding is set to NONE, the following vectors are (1,0,0) and (0,1,0).
	//If it is switched on, they are modified according to the viewdir/camera position (in Render of the System)
	public CVector3		BillboardedX;
	public CVector3		BillboardedY;
	
	///////////////////////////////// RANDOM_FLOAT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione genratrice di numeri casuali
	/////
	///////////////////////////////// RANDOM_FLOAT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	private float RANDOM_FLOAT() {
		return ((float)Math.random());
	}
	
	///////////////////////////////// CPARTICLESYSTEM \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione genratrice di numeri casuali
	/////
	///////////////////////////////// CPARTICLESYSTEM \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public CParticleSystem ()
	{	
		EmitterPosition			= new CVector3(0.0f,0.0f,0.0f);
		MaxCreationDeviation	= new CVector3(0.0f,0.0f,0.0f);
    	
		StandardEmitDirection	= new CVector3(0.0f,0.0f,0.0f);
		MaxEmitDirectionDeviation= new CVector3(0.0f,0.0f,0.0f);
		MaxEmitSpeed			= 0.0f;
		MinEmitSpeed			= 0.0f;
    	
		AccelerationDirection	= new CVector3(0.0f,0.0f,0.0f);
		MaxAcceleration			= 0.0f;
		MinAcceleration			= 0.0f;
    	
		MinEmitSpinSpeed		= 0.0f;
		MaxEmitSpinSpeed		= 0.0f;
		
		MaxSpinAcceleration		= 0.0f;
		MinSpinAcceleration		= 0.0f;
    	
		//look:
		MaxEmitAlpha			= 0.0f;
		MinEmitAlpha			= 0.0f;
		MaxDieAlpha				= 1.0f;
		MinDieAlpha				= 1.0f;
    	
		MaxEmitColor			= new CVector3(0.0f,0.0f,0.0f);
		MinEmitColor			= new CVector3(0.0f,0.0f,0.0f);
		MaxDieColor				= new CVector3(0.0f,0.0f,0.0f);
		MinDieColor				= new CVector3(0.0f,0.0f,0.0f);
    	
		Texture 				= null;
		UseTexture				= false;
		Billboarding			= 0;
    	
		//size:
		MaxEmitSize				= 0.0f;
		MinEmitSize				= 0.0f;
		MaxDieSize				= 0.0f;
		MinDieSize				= 0.0f;
	
		//behavior:
		RecreateWhenDied		= false;
		
		MaxDieAge				= 1.0f;
		MinDieAge				= 1.0f;
    	
		MaxParticles			= 0;
		ParticlesInUse			= 0;
    	
		ParticlesCreatedPerSec	= 0;
		CreationVariance		= 0.0f;
		ParticlesLeaveSystem	= false;
		Particles				= null;
	}

	///////////////////////////////// SETEMITTER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	///// 
	/////	Imposta un punto di emissione delle particelle (args float)
	/////
	///////////////////////////////// SETEMITTER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void SetEmitter(float x, float y, float z, float EmitterDeviationX,float EmitterDeviationY,float EmitterDeviationZ)
	{
		CVector3 pos = new CVector3(x,y,z);
		CVector3 dev = new CVector3(EmitterDeviationX,EmitterDeviationY,EmitterDeviationZ);
		SetEmitter(pos, dev);
	}

	///////////////////////////////// SETEMITTER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Imposta un punto di emissione delle particelle (args vettori)
	/////
	///////////////////////////////// SETEMITTER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void SetEmitter(CVector3 pos, CVector3 dev)
	{
		EmitterPosition = pos;
		MaxCreationDeviation = dev;
	}
	
	///////////////////////////////// SETEMISSIONDIRECTION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	imposta la direzione in cui le particelle vengono emesse
	/////
	///////////////////////////////// SETEMISSIONDIRECTION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void SetEmissionDirection(float x, float y, float z, float MaxDeviationX, float MaxDeviationY, float MaxDeviationZ)
	{
		CVector3 dir = new CVector3(x,y,z);
		CVector3 dev = new CVector3(MaxDeviationX,MaxDeviationY,MaxDeviationZ);
		SetEmissionDirection(dir,dev);
	}
	
	///////////////////////////////// SETEMISSIONDIRECTION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	imposta la direzione in cui le particelle vengono emesse
	/////
	///////////////////////////////// SETEMISSIONDIRECTION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void SetEmissionDirection(CVector3 direction, CVector3 Deviation)
	{
		StandardEmitDirection = direction;
		MaxEmitDirectionDeviation = Deviation;
	}
	
	///////////////////////////////// SETSPINSPEED \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione che imposta la velocita' di emissione
	/////
	///////////////////////////////// SETSPINSPEED \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void SetSpinSpeed(float min, float max)
	{
		MinEmitSpinSpeed = min;
		MaxEmitSpinSpeed = max;
	}
	
	///////////////////////////////// SETACCELERATION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione che imposta l'accelerazione
	/////
	///////////////////////////////// SETACCELERATION  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void SetAcceleration(float x, float y, float z, float Min, float Max)
	{
		CVector3 acc = new CVector3(x,y,z);
		SetAcceleration(acc,Min,Max);
	}
	
	///////////////////////////////// SETACCELERATION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione che imposta l'accelerazione
	/////
	///////////////////////////////// SETACCELERATION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void SetAcceleration(CVector3 acc, float Min, float Max)
	{
		AccelerationDirection = acc;
		MaxAcceleration = Max;
		MinAcceleration = Min;
	}
	
	///////////////////////////////// SETCREATIONCOLOR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione che impostai l colore delle particelle alla creazione
	/////
	///////////////////////////////// SETCREATIONCOLOR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*	
	public void SetCreationColor(float minr, float ming, float minb,
							   	 float maxr, float maxg, float maxb)
	{
		CVector3 min = new CVector3(minr,ming,minb);
		CVector3 max = new CVector3(maxr,maxg,maxb);
		SetCreationColor(min, max);
	}
	
	///////////////////////////////// SETCREATIONCOLOR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione che impostai l colore delle particelle alla creazione
	/////
	///////////////////////////////// SETCREATIONCOLOR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*	
	public void SetCreationColor(CVector3 min, CVector3 max)
	{
		MinEmitColor = min;
		MaxEmitColor = max;
	}
	
	///////////////////////////////// SETDIECOLOR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione che imposta il colore delle particelle alla morte
	/////
	///////////////////////////////// SETDIECOLOR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void SetDieColor(float minr, float ming, float minb,
							   		    float maxr, float maxg, float maxb)
	{
		CVector3 min = new CVector3(minr,ming,minb);
		CVector3 max = new CVector3(maxr,maxg,maxb);
		SetDieColor(min,max);
	}
	
	///////////////////////////////// SETDIECOLOR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione che imposta il colore delle particelle alla morte
	/////
	///////////////////////////////// SETDIECOLOR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void SetDieColor	(CVector3 min, CVector3 max)
	{
		MinDieColor = min;
		MaxDieColor = max;
	}
	
	///////////////////////////////// SETALPHAVALUES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione che imposta il valore alpha delle particelle
	/////
	///////////////////////////////// SETALPHAVALUES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void SetAlphaValues(float MinEmit, float MaxEmit, float MinDie, float MaxDie)
	{
		MinEmitAlpha = MinEmit;
		MaxEmitAlpha = MaxEmit;
		MinDieAlpha = MinDie;
		MaxDieAlpha = MaxDie;
	}
	
	///////////////////////////////// SETSIZEVALUES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione che imposta le dimensioni del sistema
	/////
	///////////////////////////////// SETSIZEVALUES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void SetSizeValues(float EmitMin, float EmitMax, float DieMin, float DieMax)
	{
		MinEmitSize = EmitMin;
		MaxEmitSize = EmitMax;
		MinDieSize = DieMin;
		MaxDieSize = DieMax;
	}
	
	///////////////////////////////// INITIALIZE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione che inizializza il sistema
	/////
	///////////////////////////////// INITIALIZE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*	
	public boolean Initialize(int NumParticles)
	{
		Particles = new CParticle[NumParticles];
		
		if (Particles == null) 
		{
			MaxParticles = 0;
			ParticlesInUse = 0;
			return false;
		}
	
		MaxParticles = NumParticles;
		ParticlesInUse = 0;
	
		//Istanzia ogni particella (necessario in java)
		for (int i = 0; i < NumParticles; i++)
		{
			Particles[i] = new CParticle();
		}
	
		return true;
	}
	
	///////////////////////////////// UPDATESYSTEM \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione che aggiorna il sistema
	/////
	///////////////////////////////// UPDATESUSYSTEM \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void UpdateSystem(float timePassed)
	{
		//We have to 
		//  -update the particles (= move the particles, change their alpha, color, speed values)
		//  -create new particles, if desired and there are "free" particles
	
		//First get the number of particles we want to create (randomly in a certain dimension (dependent of CreationVariance)
		
		int ParticlesToCreate = (int) ((float)ParticlesCreatedPerSec
											 *timePassed
			                                 *(1.0f+CreationVariance*(RANDOM_FLOAT()-0.5f)));
		
		//loop through the particles and update / create them
		for (int i = 0; i < MaxParticles; i++)
		{
			if (Particles[i].IsAlive)
			{
				Particles[i].Update(timePassed);
			}
	
			//Should we create the particle?
			if (ParticlesToCreate > 0)
			{
				if (Particles[i].IsAlive == false)
				{
					Particles[i].Initialize(this);
					//Update the particle: This has an effect, as if the particle would have
					//been emitted some milliseconds ago. This is very useful on slow PCs:
					//Especially if you simulate something like rain, then you could see that 
					//many particles are emitted at the same time (same "UpdateSystem" call),
					//if you would not call this function:				
					Particles[i].Update(RANDOM_FLOAT()*timePassed);  
					ParticlesToCreate--;
				}
			}
	
		}
		
	}
	
	///////////////////////////////// LOADTEXTURE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione che carica la texture
	/////
	///////////////////////////////// LOADTEXTURE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public boolean LoadTextureFromFile(GLFunc gl,GLUFunc glu,String Filename)
	{
		UseTexture = true;
		//Create the texture pointer:
		Texture = new int[2];
	
		Texture[0] = CTextureloader.LoadPng(gl,glu,Filename);
		return true;
	}
	
	///////////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzione ricorsiva che renderizza il sistema
	/////
	///////////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void Render(GLFunc gl,GLUFunc glu)
	{
		//the calling method must switch on texturing!
		if (UseTexture == true)
		{
			gl.glBindTexture( gl.GL_TEXTURE_2D, Texture[0] );
			//Calculate the "billboarding vectors" (the particles only store their positions, but we need quadrangles!)
			switch (Billboarding)
			{
				//BILLBOARDING_NONE					     = 0;
				//BILLBOARDING_PERPTOVIEWDIR			 = 1; //align particles perpendicular to view direction
				//BILLBOARDING_PERPTOVIEWDIR_BUTVERTICAL = 2; //like PERPToViewDir, but Particles are vertically aligned
				case 0 :
				{
					//independent from camera / view direction
					BillboardedX = new CVector3(1.0f,0.0f,0.0f);
					BillboardedY = new CVector3(0.0f,1.0f,0.0f);
					break;
				}
				case 1 :
				{
					//Retrieve the up and right vector from the modelview matrix:
					float ModelviewMatrix[] = new float[16];
					gl.glGetFloatv(gl.GL_MODELVIEW_MATRIX, ModelviewMatrix);
	
					//Assign the x-Vector for billboarding:
					BillboardedX = new CVector3(ModelviewMatrix[0], ModelviewMatrix[4], ModelviewMatrix[8]);
	
					//Assign the y-Vector for billboarding:
					BillboardedY = new CVector3(ModelviewMatrix[1], ModelviewMatrix[5], ModelviewMatrix[9]);
					break;
				}
				case 2 :
				{
					//Retrieve the right vector from the modelview matrix:
					float ModelviewMatrix[] = new float[16];
					gl.glGetFloatv(gl.GL_MODELVIEW_MATRIX, ModelviewMatrix);
	
					//Assign the x-Vector for billboarding:
					BillboardedX = new CVector3(ModelviewMatrix[0], ModelviewMatrix[4], ModelviewMatrix[8]);
	
					//Assign the y-Vector:
					BillboardedY = new CVector3(0.0f,1.0f,0.0f);				
					break;
				}
			}
		}
		else
		{
			gl.glGetFloatv(gl.GL_POINT_SIZE,CurrentPointSize);
		}
		for (int i = 0; i < MaxParticles; i++)
		{
			if (Particles[i].IsAlive)
				Particles[i].Render(gl,glu);
		}
	}
	
	public void doCleanup(GLFunc gl,GLUFunc glu)
	{
		gl.glDeleteTextures(Texture.length,Texture);
	}
	
}