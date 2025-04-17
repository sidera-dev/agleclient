/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE CLIENT

                            FILE CPLAYER
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
package agle.client;

// Java classes
import java.lang.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// GL4Java classes
import gl4java.*;
import gl4java.utils.textures.*;

// classes
import agle.lib.loaders.*;
import agle.lib.math.*;
import agle.lib.utils.*;
import agle.lib.font.CGLFont;
import agle.lib.camera.*;

///////////////////////////////// CPLAYER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	// classe che gestisce un personaggio
/////
///////////////////////////////// CPLAYER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class CPlayer extends CLivingform
{  
	/* Vettori di Posizione Velocita', Forza e Mass ereditati da CMass
	float			m;
	CVector3		Position;
	CVector3		Velocity;
	CVector3		Force; */
	
	float 			Width;
	
	// rotazione
	float			Rotation;
	float[]			SinTable;
	float[]			CosTable;
	// 2 times PI.
	double AR_2PI;
	// PI divided by 180.
	double AR_PI_DIV_180;
	
	// Rilevazine delle collisioni
	CBoundingSphere	Sphere;
	
	CClampingLine	GroundLine;

	// This holds the 3D Model info that we load in
	C3DModelLoader2 Model;
	
	float wHead;
	float wUpper;
	float wLower;
	
	// Proprieta' del giocatore
	// String		Name;
	String			Password;
	String			Race;
	String			Sex;
	String			Background;
	// Livello
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
	// Abilitia'
	Vector<CSkill>	Ability;
	Vector<CSkill>	Magic;

	// Body
	Vector			Body;
	// Oggetti   
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
	
	public CPlayer(String pName, String pPass, String pRace, String pSex, C3DModelLoader2 pModel)
	{
		// Massa
		m =				1; // Verra calcolata in base alla razza ed al sesso per ora 
						   // metto un valore standard
		// Livello       
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
        // Abilita'
		Ability =		new Vector<CSkill>();
		Magic =			new Vector<CSkill>();
        // Body
		Body =			new Vector();
		// Oggetti    
		Dress =			new Vector();
		Obj =			new Vector();    
		// Propieta'
		Name =			pName;
		Password =		pPass;
		Race =			pRace;
		Sex =			pSex;
		// Flags
		Visible =		false;
		Moving =		false;
		Standing =		true;
		Walking =		false;
		Running =		false;
		Jumping =		false;
		Climbing =		false;
		Swimming =		false;
		RSideWalk =		false;
		LSideWalk =		false;
		
		said =			"";
		win =			new double[3];
				
		// Start player position
		Position =		new CVector3(0.0f,10.0f,0.0f);
		// Inital player velocity
		Velocity =		new CVector3(0.0f,0.0f,0.0f);
		// Initizal force over player
		Force	 =		new CVector3(0.0f,0.0f,0.0f);
		
		// Player rotation angle
		Rotation =		AR_DegToRad(0f);
		// 2 times PI.
		AR_2PI 	 = 		6.28318530717958647692;
		// PI divided by 180.
		AR_PI_DIV_180 = 0.017453292519943296;
		// sine and cos tables
		SinTable = 		new float[361];
		CosTable = 		new float[361];
		for ( int i = 0; i < 361; i++ )
		{
			SinTable[i] = (float)Math.sin( (double)AR_DegToRad( i ) );
			CosTable[i] = (float)Math.cos( (double)AR_DegToRad( i ) );
		}
		
		// Collisions check
		// Initialize and begin bounding sphere
		Model = pModel;

		wHead = Model.m_Head.height;
		wUpper= Model.m_Upper.height;
		wLower= Model.m_Lower.height;
		Width = ((wHead+wUpper+wLower)/3.0f)*0.1f;
		
		Sphere = new CBoundingSphere();
		Sphere.SetRadius(Width/2.0f);
		Sphere.SetPosition(Position);
		
		// Init clamping line 
		GroundLine = new CClampingLine();
		GroundLine.GroundLine =		new CVector3[2];
		GroundLine.GroundLine[0] =	new CVector3( 0f, Width , 0f);
		GroundLine.GroundLine[1] =	new CVector3( 0f, -Width, 0f);
		// Init clamping line in player coordinates
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
	/////	Funzione che aggiorna il giocatore
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
		
		if(!Ability.isEmpty()){
			// loop in abilities
			for(int i = 0; i < Ability.size(); i++ ){
				// Update ours ability 
				(Ability.elementAt(i)).Update(FrameInterval);
				// check ability aginst octree
				if( Zone.Octree.IntersectSphereWithOctree( Zone.Octree,  Zone.Landscape, (Ability.elementAt(i)).Sphere)) {
					Ability.remove(i);
				}
			}
		}
		if(!Magic.isEmpty()){
			// loop in magics
			for(int i = 0; i < Magic.size(); i++ ){
				(Magic.elementAt(i)).Update(FrameInterval);
				// check ability aginst octree
				if( Zone.Octree.IntersectSphereWithOctree( Zone.Octree,  Zone.Landscape, (Magic.elementAt(i)).Sphere)) {
					Magic.remove(i);
				}				
			}							
		}		
			
		// Processa le collisioni e le forze

		// Se la sfera in cui e' il giocatore collide con qualcosa calcola l'offset e sposta
		// entrambi indietro, evita pero' l'asse y.
		if( Zone.Octree.IntersectSphereWithOctree( Zone.Octree,  Zone.Landscape, Sphere) ){
			Position.x = Sphere.Position.x + Sphere.vOffset().x;
			Position.z = Sphere.Position.z + Sphere.vOffset().z;
			Sphere.Position.x = Position.x; 
			Sphere.Position.z = Position.z; 
			GroundLine.Position.x = Position.x;
			GroundLine.Position.z = Position.z;
			
			// Se la line del suolo su cui e' il giocatore collide con il suolo...
			// Calcola l'offset e sposta  entrambi
			// Azzera la velocita' impressa al giocatore dalla gravita' (e' gia' a terra)
			if( Zone.Octree.IntersectLineWithOctree( Zone.Octree,  Zone.Landscape, GroundLine ) ){
				Position.y = GroundLine.GetCollidingPoint().y + Width/2;
				GroundLine.Position.y = Position.y;
			
				Sphere.Position.y = Position.y;
			
				Velocity = new CVector3(Velocity.x,0.0f,Velocity.z);
			}
		}
		// Se il giocatore non collide con il suolo...	
		// Calcola la velocita' impressa dalla gravita' e poi applica lo spostamento al giocatore.
		else {
			ApplyForce(CMath3d.Multiply(Zone.Gravity,m) );
		}
		// Testa la collisione del giocatore con gli altri giocatori,
		// gli oggetti ed i mobs
		if(!Zone.Objs.isEmpty()){
			// Cicla tra i giocatori
			for(int i = 0; i < Zone.Objs.size(); i++ ){
				if( Sphere.CheckSpheretoSphere((Zone.Objs.elementAt(i)).Sphere)){
				}
			}
		}
		if(!Zone.Mobs.isEmpty()){
			// loop between mobs
			for(int i = 0; i < Zone.Mobs.size(); i++ ){
				if( Sphere.CheckSpheretoSphere((Zone.Mobs.elementAt(i)).Sphere)){
					// Player and mob collided											  			
				}
				// for every ability used
				for(int j = 0; j < Ability.size(); j++ ){
					//check collisions with every ability bounding sphere and every mob
					if( ((Ability.elementAt(j)).Sphere).CheckSpheretoSphere((Zone.Mobs.elementAt(i)).Sphere)){		
						// Ability sphere and mob collided
						Ability.remove(j);
						(Zone.Mobs.elementAt(i)).Model.SetTorsoAnimation("BOTH_DEATH1");						
					}						
				}
				// for every magic used
				for(int j = 0; j < Magic.size(); j++ ){
					//check collisions with every magic bounding sphere and every mob
					if( ((Magic.elementAt(j)).Sphere).CheckSpheretoSphere((Zone.Mobs.elementAt(i)).Sphere)){		
						// Magic sphere and mob collided				
						Magic.remove(j);
						(Zone.Mobs.elementAt(i)).Model.SetTorsoAnimation("BOTH_DEATH1");					
					}										
				}								
			}
		} /*
		if(!Zone.Exits.isEmpty()){
			// Cicla tra i giocatori
			for(int i = 0; i < Zone.Exits.size(); i++ ){
				CExit exit = (CExit)Zone.Exits.elementAt(i);
				if( Sphere.CheckSpheretoSphere(exit.Sphere)){
					Zone.canrun = false;
					break;
				}
			}
		}*/	
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
	/////	Funzione che disegna il giocatore a video
	/////
	///////////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void Render(GLFunc gl,GLUFunc glu)
	{
		if(!Ability.isEmpty()){
			// loop in abilities
			for(int i = 0; i < Ability.size(); i++ ){
				(Ability.elementAt(i)).Render(gl,glu);
			}
		}
		if(!Magic.isEmpty()){
			// loop in magics
			for(int i = 0; i < Magic.size(); i++ ){
				(Magic.elementAt(i)).Render(gl,glu);
			}
		}		
		if (Visible == true){
			gl.glPushMatrix();
				// Trasla il giocatore 
				gl.glTranslatef(Position.x, Position.y, Position.z);
				gl.glPushMatrix();
					gl.glRotatef(-90f,0.0f,1.0f,0.0f);				
					gl.glRotatef(AR_RadToDeg(Rotation),0.0f,1.0f,0.0f);
					gl.glScalef(0.1f,0.1f,0.1f);
					gl.glEnable(gl.GL_CULL_FACE);
					gl.glCullFace(gl.GL_FRONT);
					// Disegna il giocatore
					Model.DrawModel();
					gl.glDisable(gl.GL_CULL_FACE); 
				gl.glPopMatrix();
			gl.glPopMatrix();
		//Sphere.RenderBoundingSphere(gl,glu);
		}
	}
	
	///////////////////////////////// SPEAK \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Funzione che disegna affianco al giocatore cio' che dice
	/////
	///////////////////////////////// SPEAK \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void Speak(GLFunc gl,GLUFunc glu,CGLFont glFont)
	{
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
	
	///////////////////////////////// PROCESSKEYS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Questa funizione stampa a schermo tutto
	/////
	///////////////////////////////// PROCESSKEYS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void ProcessKeys(boolean[] keys, CCamera Camera, float FrameInterval)
	{
		CVector3 force = new CVector3();
		if (keys[KeyEvent.VK_NUMPAD6]) {
			Camera.RotateAroundPoint(Position(),0.05f,0.0f,1.0f,0.0f);
		}
		if (keys[KeyEvent.VK_NUMPAD4] ){
			Camera.RotateAroundPoint(Position(),-0.05f,0.0f,1.0f,0.0f);
		}
		if(keys[KeyEvent.VK_SPACE] ){
			// This is jumping 
			/*if( Velocity.y == 0.0 )
				force.y += 100f;
				ApplyForce(force);*/
				CSkill fireball = new CSkill();
				fireball.Name = "fireball";
				fireball.Position = new CVector3(Position.x,Position.y+((wLower*0.1f)/2f),Position.z);
				CVector3 fforce = new CVector3((SinTable[(int)AR_RadToDeg( Rotation )] *1000f),Position.y,(CosTable[(int)AR_RadToDeg( Rotation )]*1000f));
				fireball.ApplyForce(fforce);
				Magic.addElement(fireball);	
				Model.SetTorsoAnimation("TORSO_ATTACK");	
		}
		if(keys[KeyEvent.VK_ENTER] ){
				Model.SetTorsoAnimation("TORSO_ATTACK2");	
		}		
		if(keys[KeyEvent.VK_UP] ){
			force.x += 10f;
			force.z += 10f;
			ApplyForce(force);
		}
		if(keys[KeyEvent.VK_DOWN] ){
			force.x -= 10f;
			force.z -= 10f;
			ApplyForce(force);
		}
		if (keys[KeyEvent.VK_LEFT] ){
			RotatePlayer(120f,FrameInterval);
			float PI_DIV_180 = 0.017453292519943296f;
			// If you want camera following player rotation enable this
			//Camera.RotateAroundPoint(((CPlayer)Players.elementAt(0)).Position,
			//							PI_DIV_180 * 120f * CGlobals.FrameInterval,0f,1f,0f);
		}
		if (keys[KeyEvent.VK_RIGHT] ){
			RotatePlayer(-120f,FrameInterval);				
			float PI_DIV_180 = 0.017453292519943296f;
			// If you want camera following player rotation enable this			
			//Camera.RotateAroundPoint(((CPlayer)Players.elementAt(0)).Position,
			//							-PI_DIV_180 * 120f* CGlobals.FrameInterval,0f,1f,0f);
		}
	}
	
	///////////////////////////////// SETPOSITION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Questa funzione imposta la posizione del giocatore
	/////
	///////////////////////////////// SETPOSITION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void SetPosition(CVector3 pPosition)
	{
		Position = new CVector3(pPosition.x,pPosition.y,pPosition.z);
	}
	
	//////////////////////////////// POSITION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Queste funzioni permettono di accedere ai dati privati del giocatore
	/////
	/////////7////////////////////// POSITION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public CVector3 Position()
	{
		return new CVector3(Position.x,Position.y,Position.z);
	}	

}

