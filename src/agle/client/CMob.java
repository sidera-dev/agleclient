/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE CLIENT

                              FILE CMOB
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.client;

// Java classes
import java.lang.*;
import java.util.*;
import java.io.*;

// GL4Java classes
import gl4java.*;
import gl4java.utils.textures.*;

// AGLE classes
import agle.lib.loaders.*;
import agle.lib.math.*;
import agle.lib.utils.*;
import agle.lib.font.CGLFont;

///////////////////////////////// CMOB \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// CMOB \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class CMob extends CLivingform
{
	/* Position vector,velocity', force and Mass extended from CMass
	float			m;
	CVector3		Position;
	CVector3		Velocity;
	CVector3		Force; */
	
	float 			Width;
	
	// rotation
	float			Rotation;
	float[]			SinTable;
	float[]			CosTable;
	// 2 times PI.
	double AR_2PI;
	// PI divided by 180.
	double AR_PI_DIV_180;
	
	// collision sphere
	CBoundingSphere	Sphere;
	
	CClampingLine	GroundLine;
	
	// our mob 3d model
	C3DModelLoader2 Model;
	
	/* Ddimensione extended from CFrom;
	float			dimX;
	float			dimY;
	float			dimZ;  */  
	
	// String		Name   
	String			Race;
	String			Sex;
	// mob level
	byte			Level;
	// Stats
	short			Age; 
	short			Str;
	short			Intl;
	short			Dex;
	short			Car;   
	short			Faith;
	String			Deity;
	short			Glory;
	short			All;
	short			Lif_E;
	short			Men_E;
	short			Fis_E;
	// sword and magic ability
	Vector			Ability;
	Vector			Magic;
	// Body
	Vector			Body;
	// objects carried   
	Vector			Dress;
	Vector			Obj;
	// Flags
	boolean			Visible;
	boolean			Moving;
	boolean			Standing;
	boolean			Walking;
	boolean			Running;
	boolean			Jumping;
	boolean			Climbing;
	boolean			Swimming;
	boolean			RSideWalk;
	boolean			LSideWalk;
	
	String 			said;
	double[] 		win;
	
	// Creates a new instance of Mob 
	public CMob(String mName, String mRace, String mSex, CVector3 mPos, C3DModelLoader2 mModel)
	{
		// Mass
		m =				1;
		// Level      
		Level =			1;
		// Stats
		Faith =			0;
		Glory =			0;
		Str =			10;
		Intl = 			10;
		Dex =			10;
		Car =			10;
		All =			0;
		Lif_E =			10;
		Men_E =			10;
		Fis_E =			10;

		Ability =		new Vector();
		Magic =			new Vector();

		Body =			new Vector();
  
		Dress =			new Vector();
		Obj =			new Vector();     
		//dimensions 1m x 1m x 2m
		dimX =			1f;
		dimY =			1f;
		dimZ =			1f;

		Name =			mName;
		Race =			mRace;
		Sex =			mSex;
		// Flags
		Visible =		false;
		Moving =		false;
		Standing =		true;	// init mob standing
		Walking =		false;
		Running =		false;
		Jumping =		false;
		Climbing =		false;
		Swimming =		false;
		RSideWalk =		false;
		LSideWalk =		false;
		
		said =			"";
		win =			new double[3];
		

		// Initial mob position
		Position =		new CVector3(0.0f,10.0f,0.0f);
		// Initial mob velocity
		Velocity =		new CVector3(0.0f,0.0f,0.0f);
		// Initial force over the mob
		Force	 =		new CVector3(0.0f,0.0f,0.0f);
		
		// mob rotation angle
		Rotation =		AR_DegToRad(0f);
		// 2 times PI.
		AR_2PI 	 = 		6.28318530717958647692;
		// PI divided by 180.
		AR_PI_DIV_180 = 0.017453292519943296;
	
		SinTable = 		new float[361];
		CosTable = 		new float[361];
		for ( int i = 0; i < 361; i++ )
		{
			SinTable[i] = (float)Math.sin( (double)AR_DegToRad( i ) );
			CosTable[i] = (float)Math.cos( (double)AR_DegToRad( i ) );
		}
		
		// for collision checking
		// calculate model height and pass it to collision sphere
		Model = mModel;
		float wHead = Model.m_Head.height;
		float wUpper= Model.m_Upper.height;
		float wLower= Model.m_Lower.height;
		Width = ((wHead+wUpper+wLower)/3.0f)*0.1f;
		
		Sphere = new CBoundingSphere();
		Sphere.SetRadius(Width/2f);
		Sphere.SetPosition(Position);
		
		// init clamping line 
		GroundLine = new CClampingLine();
		GroundLine.GroundLine =		new CVector3[2];
		GroundLine.GroundLine[0] =	new CVector3( 0f, Width , 0f);
		GroundLine.GroundLine[1] =	new CVector3( 0f, -Width, 0f);
		// init clamping line respect mob coordinates
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
	
	public void RotatePlayer(float turnrate, float FrameInterval )
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
	/////	Update the mob
	/////
	///////////////////////////////// UPDATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void Update(float FrameInterval, CZone Zone)
	{	
		// Clamp values above 2 * PI or 360 Deg's.
		if ( Rotation >= AR_2PI )
			Rotation = Rotation - (float)AR_2PI;
	
		// Clamp values below 0.
		if ( Rotation < 0.0f )
			Rotation = (float)AR_2PI + Rotation;
			
		// Imposta a zero le forze agenti sulle masse ed aggiornale, dopodiche aggiorna 
		// la linea del terreno
		UpdateMass(FrameInterval);
		GroundLine.UpdateClampingLine();
		InitMass();
		
		// Processa le collisioni e le forze

		// Se la sfera in cui e' il mob collide con qualcosa calcola l'offset e sposta
		// entrambi indietro, evita pero' l'asse y.
		if( Zone.Octree.IntersectSphereWithOctree( Zone.Octree,  Zone.Landscape, Sphere) ){
			Position.x = Sphere.Position.x + Sphere.vOffset().x;
			Position.z = Sphere.Position.z + Sphere.vOffset().z;
			Sphere.Position.x = Position.x; 
			Sphere.Position.z = Position.z; 
			GroundLine.Position.x = Position.x;
			GroundLine.Position.z = Position.z;
			
			// Se la line del suolo su cui e' il mob collide con il suolo...
			// Calcola l'offset e sposta  entrambi
			// Azzera la velocita' impressa al mob dalla gravita' (e' gia' a terra)
			if( Zone.Octree.IntersectLineWithOctree( Zone.Octree,  Zone.Landscape, GroundLine ) ){
				Position.y = GroundLine.GetCollidingPoint().y + Width/2;
				GroundLine.Position.y = Position.y;
			
				Sphere.Position.y = Position.y;
			
				Velocity = new CVector3(Velocity.x,0.0f,Velocity.z);
			}
		}
		// Se il mob non collide con il suolo...	
		// Calcola la velocita' impressa dalla gravita' e poi applica lo spostamento al giocatore.
		else {
			ApplyForce(CMath3d.Multiply(Zone.Gravity,m) );
		}
		// Testa la collisione del mob con gli altri giocatori,
		// gli oggetti ed i mobs
		if(!Zone.Objs.isEmpty()){
			// Cicla tra i giocatori
			for(int i = 0; i < Zone.Objs.size(); i++ ){
				if( Sphere.CheckSpheretoSphere((Zone.Objs.elementAt(i)).Sphere)){
				}
			}
		}
		if(!Zone.Mobs.isEmpty()){
			// Cicla tra i giocatori
			for(int i = 0; i < Zone.Mobs.size(); i++ ){
				if( Sphere.CheckSpheretoSphere((Zone.Mobs.elementAt(i)).Sphere)){
				}
			}
		}
		if(!Zone.Exits.isEmpty()){
			// Cicla tra i giocatori
			for(int i = 0; i < Zone.Exits.size(); i++ ){
				CExit exit = Zone.Exits.elementAt(i);
				if( Sphere.CheckSpheretoSphere(exit.Sphere)){
				}
			}
		}
		// Se il la velocita' e' diversa zero (c'e' movimento), applica le forze di resistenza
		// al movimento (la velocita' viene moltiplicata per le costanti difrizione dell'aria e della terra)
		if (Velocity != new CVector3(0.0f,0.0f,0.0f)){
			String animName = ((CAnimationInfo)Model.m_Lower.pAnimations.get(Model.m_Lower.currentAnim)).strName;
			if ((Velocity.x > 0.2f) && (Velocity.x < 2.0f) &&
				(Velocity.z > 0.2f) && (Velocity.z < 2.0f)){
				if(Moving == false){
					Moving = true;
				}
				if( !animName.equals("LEGS_WALK")){
					Model.SetLegsAnimation("LEGS_WALK");
				}
			}
			else if ((Velocity.x >= 2.0f) && (Velocity.z >= 2.0f)){
				if(Moving == false){
					Moving = true;
				}
				if( !animName.equals("LEGS_RUN")){
					Model.SetLegsAnimation("LEGS_RUN");
				}
			}
			else if ((Velocity.x < -0.2f) && (Velocity.z < -0.2f)){
				if(Moving == false){
					Moving = true;
				}
				if( !animName.equals("LEGS_BACK")){
					Model.SetLegsAnimation("LEGS_BACK");
				}
			} else {
				if(Moving == true){
					Moving = false;
					Model.SetLegsAnimation("LEGS_IDLE");
				}
			}
			ApplyForce(CMath3d.Multiply((CMath3d.Negate(Velocity)),Zone.AFrictionConst));
			CVector3 vel = new CVector3(Velocity.x,0.0f,Velocity.z);
			ApplyForce(CMath3d.Multiply((CMath3d.Negate(vel)),Zone.GFrictionConst));
		}
	}
	
	///////////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Funzione che disegna il mob a video
	/////
	///////////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void Render(GLFunc gl,GLUFunc glu)
	{
		if (Visible == true){
			gl.glPushMatrix();
				// Trasla il mob 
				gl.glTranslatef(Position.x, Position.y, Position.z);
				gl.glPushMatrix();
					gl.glRotatef(-90f,0.0f,1.0f,0.0f);				
					gl.glRotatef(AR_RadToDeg(Rotation),0.0f,1.0f,0.0f);
					gl.glScalef(0.1f,0.1f,0.1f);
					gl.glEnable(gl.GL_CULL_FACE);
					gl.glCullFace(gl.GL_FRONT);
					// Disegna il mob
					Model.DrawModel();
					gl.glDisable(gl.GL_CULL_FACE); 
				gl.glPopMatrix();
			gl.glPopMatrix();
				
			//Sphere.RenderBoundingSphere(gl,glu);
		}
	}
	
	///////////////////////////////// SAID \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Funzione che disegna affianco al giocatore cio' che dice
	/////
	///////////////////////////////// SAID \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void Speak(GLFunc gl,GLUFunc glu,CGLFont glFont)
	{
		if (Visible){
			if (said != ""){
				double[]	projection = new double[16];
				gl.glGetDoublev( gl.GL_PROJECTION_MATRIX,projection );
				int[]		viewport = new int[4];
				gl.glGetIntegerv( gl.GL_VIEWPORT,viewport );
				double[]	matrix = new double[16];
				gl.glGetDoublev( gl.GL_MODELVIEW_MATRIX,matrix );
				double[] pos = {(double)Position.x,(double)Position.y,(double)Position.z};
				glu.gluProject(pos,matrix,projection,viewport,win);
				gl.glColor3f(1.0f,1.0f,1.0f);
				gl.glBlendFunc(gl.GL_ONE,gl.GL_ONE);
				gl.glEnable(gl.GL_BLEND);
				glFont.glPrint(gl,glu,(int)(win[0]), (int)(win[1]-100), said, 0, (float)(1), (float)(1), (float)(0));
				gl.glDisable(gl.GL_BLEND);
			}
		}
	}
	
	///////////////////////////////// SETPOSITION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Questa funzione imposta la posizione del mob
	/////
	///////////////////////////////// SETPOSITION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void SetPosition(CVector3 pPosition)
	{
		Position = new CVector3(pPosition.x,pPosition.y,pPosition.z);
	}
	
	//////////////////////////////// POSITION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Queste funzioni permettono di accedere ai dati privati del mob
	/////
	/////////7////////////////////// POSITION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public CVector3 Position()
	{
		return new CVector3(Position.x,Position.y,Position.z);
	}
	
}
