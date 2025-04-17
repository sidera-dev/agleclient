/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                     AGLE CLIENT

                            FILE CZONES
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.client;

// Java  classes
import java.io.*;
import java.lang.*;
import java.util.*;
import java.awt.event.*;
import java.awt.*;

// GL4Java classes
import gl4java.*;
import gl4java.utils.textures.*;
import gl4java.GLContext;
import gl4java.awt.GLAnimCanvas;

// classes
import agle.lib.interfaces.*;
import agle.lib.camera.*;
import agle.lib.font.*;
import agle.lib.math.*;
import agle.lib.loaders.*;
import agle.lib.physics.*;
import agle.lib.utils.*;

///////////////////////////////// CZONES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This is the class constructor
/////
///////////////////////////////// CZONES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class CZone
{
	public CGLFont glFont; 
	public GLFunc  gl;
	public GLUFunc glu;
	
	boolean	canrun;	// Variabile che blocca l'esecuzione dei update e di render,
					// lascia aggiornare solo il personaggio principale serve quando cambia il canvas
	
	String	LandName;
	String	ZonePath;
	String	ZoneName;
	String 	TerrianFile;
	String 	Weather; 
	
	// Liste dei vari oggetti persenti nella nostra zona
	CPlayer	Player;
	Vector<CMob>		Mobs;
	Vector<CObj>		Objs;
	Vector<CExit>		Exits;
	Vector<CParticleSystem> ParticleSyss;
	Vector<CLight>		Lights;
	
	// Luci
	float[] 	SunAmbience;	// Il colore della luce nel mondo
	float[] 	SunDiffuse;		// Il colore della sorgente lumniosa
	float[] 	SunPosition;	// Posizione della sorgente
	
	// Forze
	CVector3	Gravity;		// Gravita'
	float		AFrictionConst; // Resistenza dell'aria
	float		GFrictionConst; // Resistenza della terra
	
	COctree  	Octree;			// Octree
	C3DModel 	Landscape;		// Sfondo
	CSkyBox  	SkyBox;			// Cielo
	CSkyPlane	SkyPlane;	
	CCamera	 	Camera;			// Telecamera
	
    // Creates a new instance of CZones 
	public CZone(GLFunc zgl,GLUFunc zglu,CGLFont zglFont, int w, int h, String landname, String zonename)
	{		
		canrun = 	false;
		
		LandName =	landname;
		ZoneName =	zonename;
		
		glFont = zglFont;
		gl = zgl;
		glu = zglu;
		
		//init();
	}
	
	////////////////////////////// INIT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Void init() Called just AFTER the GL-Context is created.
	/////	Sovrascrive quello della CTimedCanvas   
	/////
	////////////////////////////// INIT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\ 
	public void init()
	{
		Player   		=	null;
		Mobs   			=	new Vector<CMob>();
		Objs	   		= 	new Vector<CObj>();
		Exits   		= 	new Vector<CExit>();
		ParticleSyss   	= 	new Vector<CParticleSystem>();
		Lights   		= 	new Vector<CLight>();
		
		Gravity 		= 	new CVector3();
		
		SkyPlane 		= 	null;	
		SkyBox			=	null;
				
		// Inizializziamo l'octree ed il frustum e passiamogli i parametri principali
		Octree  =			null;		
		Camera = 			null;		
		
		SunAmbience =	new float[4];
		SunAmbience[0]=0.3f;
		SunAmbience[1]=0.3f;
		SunAmbience[2]=0.3f;
		SunAmbience[3]=1.0f;
		SunDiffuse  =	new float[4];
		SunDiffuse[0]=0.5f;
		SunDiffuse[1]=0.5f;
		SunDiffuse[2]=0.5f;
		SunDiffuse[3]=1.0f;
		SunPosition =	new float[4];
		SunPosition[0]=0.0f;
		SunPosition[1]=800.0f;
		SunPosition[2]=0.0f;
		SunPosition[3]=1.0f;
		
		AddLight("Sun",SunPosition,SunAmbience,SunDiffuse);
		
		LoadZone("../data/world/"+LandName+"/"+ZoneName+"/"+ZoneName+".txt");
		AddPlayer("cloud","cloud","human","male");
		// Mine test example skill	
		
		canrun = true;
		
	}  
	
	///////////////////////////////// UPDATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Aggiorna il mondo
	/////
	///////////////////////////////// UPDATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public void Update(float FrameInterval)
	{		
		if( canrun==true ){						
					
			if( Player != null ) {
				Player.Update(FrameInterval, this);
				if( Camera != null ) {
					Camera.FollowTarget(Player.Position());				
				}			
			}
			if(!ParticleSyss.isEmpty()){
				// Cicla tra le particelle
				for(int i = 0; i < ParticleSyss.size(); i++ ){
					(ParticleSyss.elementAt(i)).UpdateSystem(FrameInterval);
				}
			}
			
			if(!Lights.isEmpty()){
				// Cicla tra le luci
				for(int i = 0; i < Lights.size(); i++ ){
					(Lights.elementAt(i)).Update(gl,glu);
				}
			}
			
			if(!Mobs.isEmpty()){
				// Cicla tra i mob
				for(int i = 0; i < Mobs.size(); i++ ){
					(Mobs.elementAt(i)).Update(FrameInterval, this);
				}
			}

			if(!Objs.isEmpty()){
				// Cicla tra gli oggetti
				for(int i = 0; i < Objs.size(); i++ ){
					(Objs.elementAt(i)).Update(FrameInterval, this);
				}
			}			

			if(!Exits.isEmpty()){
				// Cicla tra le uscite
				for(int i = 0; i < Exits.size(); i++ ){
					(Exits.elementAt(i)).Update(FrameInterval, this);
				}
			}			
			// Aggiorniamo il punto di vista della telecamera
			if( Camera != null ) {						
				Camera.Look(gl,glu);
			}	
			if( Octree != null ) {			
				Octree.TotalNodesDrawn = 0;
				Octree.bDebug = false; //true if you want to see octree lines
				Octree.Frustum.CalculateFrustum(gl,glu);
			}					
		}
	} 
	
	///////////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Questa funizione stampa a schermo tutto
	/////
	///////////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void Render(float FrameInterval)   
	{
		if( canrun==true ){
			
			Update(FrameInterval);
			
			// Disegna il cielo
			if(SkyBox != null) {			
				gl.glPushMatrix();
				SkyBox.Render(gl,glu);
				gl.glPopMatrix();
			}
			// Disegna il cielo
			if(SkyPlane != null) {			
				gl.glPushMatrix();
				SkyPlane.Render(gl,glu,FrameInterval);
				gl.glPopMatrix();			
			}			
			// Disegna i giocatori, i mob e gli oggetti
			if(Player != null) {
				gl.glPushMatrix();
				Player.Render(gl,glu);
				Player.Speak(gl,glu,glFont);
				gl.glPopMatrix();				
			}
			if(!Mobs.isEmpty()){
				// Cicla tra i mob
				for(int i = 0; i < Mobs.size(); i++ ){
					(Mobs.elementAt(i)).Render(gl,glu);
					(Mobs.elementAt(i)).Speak(gl,glu,glFont);
				}
			}
			if(!Objs.isEmpty()){
				// Cicla tra gli oggetti
				for(int i = 0; i < Objs.size(); i++ ){
					(Objs.elementAt(i)).Render(gl,glu);
				}
			}
			if(!Exits.isEmpty()){
				// Cicla tra gli oggetti
				for(int i = 0; i < Exits.size(); i++ ){
					(Exits.elementAt(i)).Render(gl,glu);
				}
			}

			if ( Octree != null) {	
				gl.glPushMatrix();			
				// Abilita il back face culling
				gl.glCullFace(gl.GL_BACK);
				gl.glEnable(gl.GL_CULL_FACE);
				// Turn on a light with defaults set
				gl.glEnable(gl.GL_LIGHT0);
				gl.glEnable(gl.GL_LIGHTING);
				// Allow color
				gl.glEnable(gl.GL_COLOR_MATERIAL);
			
				// Disegna l'octree e lo sfondo
				Octree.DrawOctree(gl,glu, Octree, Landscape);
			
				gl.glDisable(gl.GL_LIGHT0);
				gl.glDisable(gl.GL_LIGHTING);
				gl.glDisable(gl.GL_COLOR_MATERIAL);
				gl.glDisable(gl.GL_CULL_FACE);
				gl.glPopMatrix();
			}
		
			if(!ParticleSyss.isEmpty()){
				// Disegna i nostri sistemi di particelle
				gl.glPushMatrix();
				gl.glEnable(gl.GL_POINT_SMOOTH);
				gl.glEnable(gl.GL_BLEND);
				gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE);
				gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_FILL);
				gl.glDepthMask(gl.GL_FALSE);
				gl.glEnable(gl.GL_DEPTH_TEST);				
				// Cicla tra i sistemi di paricelle
				for(int i = 0; i < ParticleSyss.size(); i++ ){
					(ParticleSyss.elementAt(i)).Render(gl,glu);
				}
				gl.glDepthMask(gl.GL_TRUE);
				gl.glDisable(gl.GL_POINT_SMOOTH);
				gl.glDisable(gl.GL_BLEND);
				gl.glPopMatrix();
				// Fine delle particelle
			}
		}
	} 
	
	///////////////////////////// ADD PLAYER OBJ MOB ETC \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Aggiunge oggetti mondo
	/////
	///////////////////////////// ADD PLAYER OBJ MOB ETC \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	protected void AddPlayer(String pNome,String pPass,String pRazza,String pSesso)
	{
		C3DModelLoader2 Male 	= new C3DModelLoader2(gl,glu);
		C3DModelLoader2 Female	= new C3DModelLoader2(gl,glu);
		try
		{			
			Male.LoadModel("../data/human_male", "human_male");
			Male.LoadWeapon("../data/weapon", "railgun");
			Male.SetTorsoAnimation("TORSO_STAND2");
			Male.SetLegsAnimation("LEGS_IDLE");
		}
		catch (IOException ioe)
		{
			System.err.println("Impossible load player models.");
		}		
		Player = new CPlayer(pNome,pPass,pRazza,pSesso,Male);
		Player.Visible = true;
	}
	
	protected void AddObj(String oNome,CVector3 oPos)
	{
		CObj obj = new CObj(oNome,oPos);
		obj.Visible = true;
		Objs.addElement(obj);
	}
	
	protected void AddMob(String mNome,String mRazza,String mSesso,CVector3 mPos, C3DModelLoader2 model)
	{		
		CMob mob = new CMob(mNome,mRazza,mSesso,mPos,model);
		mob.Visible = true;
		Mobs.addElement(mob);
	}
	
	protected void AddExit(String eName,CVector3 eFrom,CVector3 eTo,String eToLand, String eToZone)
	{
		CExit exit = new CExit(eName,eFrom,eTo,eToLand,eToZone);
		exit.Visible = true;
		Exits.addElement(exit);
	}

	protected void AddParticleSys(CParticleSystem ParticleSys)
	{
		ParticleSyss.addElement(ParticleSys);
	}
	
	protected void AddLight(String lName, float[] lPosition, float[] lDiffuse, float[] lAmbience)
	{
		int Index = Lights.size();
		if( Index > 8){ // Remeber that opengl can support max 8 lights at one time
			return;
		}
		CLight light = new CLight(Index ,lName, lPosition, lDiffuse, lAmbience);
		Lights.addElement(light);
	}
		
	///////////////////////////////// GETLIGHT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Questa funzione torva una luce nel vettore in base al nome
	/////	utile se si vuole cambiare un parametro della luce senza doverla
	/////	eliminare e poi rifare
	/////
	///////////////////////////////// GETLIGHT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public boolean GetLight(String lName)
	{
		CLight light;
		if(!Lights.isEmpty()){
			// Cicla tra i mob
			for(int i = 0; i < Lights.size(); i++ ){
				if( (Lights.elementAt(i)).Name == lName )
					light = (Lights.elementAt(i));
					return true;
			}
		}
		return false;
	}
		
	///////////////////////////////// LOADZONE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Carica una zona da un file
	/////
	///////////////////////////////// LOADZONE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	private boolean LoadZone(String file)
	{
		InputStream is = null;
		int fileSize = 0;
		File f;
		BufferedInputStream in;
		try
		{
			f = new File(file);  
			is = new FileInputStream(f);    
		}
		catch (Exception e)
		{
			System.out.println("Impossible load Zone");
			e.printStackTrace();
			return false;
		
		}
		// wrap a buffer to make reading more efficient (faster)
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		String line = "";	// This stores the current line we read in
		try
		{
			while ((line = reader.readLine()) != null)
			{
				if (line.trim().length() == 0 || line.trim().startsWith("#"))
						continue;
				
				if (line.startsWith("PATH"))
				{			
					ZonePath = line.substring(line.indexOf("PATH") + "PATH".length() + 1);	
				}
				else if(line.startsWith("TERRIANFILE"))
				{
					TerrianFile = line.substring(line.indexOf("TERRIANFILE") + "TERRIANFILE".length() + 1);
					// Carica il landscape
					C3DModelLoader loader = new C3DModelLoader();
					Landscape = loader.Import3DS(ZonePath + TerrianFile);
					Octree  =	new COctree();		
					Octree.Init(gl,glu,Landscape, ZonePath + "Textures/" , 1000, 20);
					Octree.Frustum = new CFrustum();
				}
				else if(line.startsWith("SKYFILES"))
				{
					String skyline  	= line.substring(line.indexOf("SKYFILES") + "SKYFILES".length() + 1);
					StringTokenizer st	= new StringTokenizer(skyline, " ");
					String back		= ParseString(st);	
					String front	= ParseString(st);
					String down		= ParseString(st);	
					String top		= ParseString(st);	
					String left		= ParseString(st);	
					String right	= ParseString(st);
					String[]  skyfiles = { ZonePath+back, ZonePath+front,
										   ZonePath+down, ZonePath+top,
										   ZonePath+left, ZonePath+right};
					SkyBox =			new CSkyBox();				   
					SkyBox.CreateTexture(gl,glu,skyfiles);
					SkyBox.SetDimensions(Octree.maxWidth, Octree.maxHeight, Octree.maxDepth);
				}
				else if(line.startsWith("SKYPLANE"))
				{
					String skyline  	= line.substring(line.indexOf("SKYPLANE") + "SKYPLANE".length() + 1);
					StringTokenizer st	= new StringTokenizer(skyline, " ");
					String domeimg		= ParseString(st);	
					int divisions		= ParseInteger(st);
					float PlanetRadius		= ParseFloat(st);	
					float AtmosphereRadius	= ParseFloat(st);	
					float hTile			= ParseFloat(st);	
					float vTile			= ParseFloat(st);
					SkyPlane = 			new CSkyPlane();	
					SkyPlane.CreateTexture(gl,glu,ZonePath + domeimg);
					SkyPlane.GenerateSkyPlane(divisions,PlanetRadius,AtmosphereRadius,hTile,vTile);
				}				
				else if(line.startsWith("WEATHER"))
				{
					Weather	= line.substring(line.indexOf("WEATHER") + "WEATHER".length() + 1);
				}
				else if(line.startsWith("GRAVITY"))
				{
					String gvline		= line.substring(line.indexOf("GRAVITY") + "GRAVITY".length() + 1);
					StringTokenizer st	= new StringTokenizer(gvline, " ");
					Gravity.x			= ParseFloat(st);
					Gravity.y			= ParseFloat(st);
					Gravity.z			= ParseFloat(st);
				}
				else if(line.startsWith("AFRICTIONCONST"))
				{
					AFrictionConst	= Float.parseFloat(line.substring(line.indexOf("AFRICTIONCONST") + "AFRICTIONCONST".length() + 1));
				}
				else if(line.startsWith("GFRICTIONCONST"))
				{
					GFrictionConst	= Float.parseFloat(line.substring(line.indexOf("GFRICTIONCONST") + "GFRICTIONCONST".length() + 1));
				}
				else if(line.startsWith("CAMERA"))
				{
					String camline		= line.substring(line.indexOf("CAMERA") + "CAMERA".length() + 1);
					StringTokenizer st	= new StringTokenizer(camline, " ");
					CVector3 CameraPos	= new CVector3( ParseFloat(st),ParseFloat(st),ParseFloat(st));
					CVector3 CameraView	= new CVector3( ParseFloat(st),ParseFloat(st),ParseFloat(st));
					CVector3 CameraUpV	= new CVector3( ParseFloat(st),ParseFloat(st),ParseFloat(st));
					Camera = new CCamera();
					Camera.SetPositionv(CameraPos,CameraView,CameraUpV);
				}
				else if(line.startsWith("MOB"))
				{
					String mline = line.substring(line.indexOf("MOB") + "MOB".length() + 1);
					StringTokenizer st = new StringTokenizer(mline, " ");
					int NumMobs   = ParseInteger(st);
					for(int i = 0; i < NumMobs; i++)
					{
						line			= reader.readLine();
						st				= new StringTokenizer(line, " ");
						String MobName	= ParseString(st);
						String MobRace	= ParseString(st);
						String MobSex	= ParseString(st);
						
						String MobModelname = ParseString(st);
						String MobModelpath = ParseString(st);						
						String MobWeaponname = ParseString(st);
  						String MobWeaponpath = ParseString(st);						
												
						CVector3 Pos	= new CVector3(ParseFloat(st),ParseFloat(st),ParseFloat(st));						
  						
						C3DModelLoader2 model	= new C3DModelLoader2(gl,glu);
						try
						{
							// I've putted them here for semplicity
							model.LoadModel(MobModelpath, MobModelname);
							model.LoadWeapon(MobWeaponpath, MobWeaponname);
							model.SetTorsoAnimation("TORSO_STAND2");
							model.SetLegsAnimation("LEGS_IDLE");
							AddMob(MobName,MobRace,MobSex,Pos, model);
						}
						catch (IOException ioe)
						{
							System.err.println("Impossible load mob models");
						}						

					}
				}
				else if(line.startsWith("OBJ"))
				{
					String objline	= line.substring(line.indexOf("OBJ") + "OBJ".length() + 1);
					StringTokenizer st	= new StringTokenizer(objline, " ");
					int NumObjs   = ParseInteger(st);
					for(int i = 0; i < NumObjs; i++)
					{
						line			= reader.readLine();
						st				= new StringTokenizer(line, " ");

						String ObjName	= ParseString(st);
						CVector3 Pos	= new CVector3( ParseFloat(st),ParseFloat(st),ParseFloat(st));
						AddObj(ObjName,Pos);
					}
				}
				else if(line.startsWith("EXIT"))
				{
					String exline	= line.substring(line.indexOf("EXIT") + "EXIT".length() + 1);
					StringTokenizer st	= new StringTokenizer(exline, " ");
					int NumExits   = ParseInteger(st);
					for(int i = 0; i < NumExits; i++)
					{
						line			= reader.readLine();
						st				= new StringTokenizer(line, " ");

						String ExitName	= ParseString(st);
						CVector3 fromPos= new CVector3( ParseFloat(st),ParseFloat(st),ParseFloat(st));
						CVector3 toPos	= new CVector3( ParseFloat(st),ParseFloat(st),ParseFloat(st));
						String toLand	= ParseString(st);
						String toZone	= ParseString(st);
						AddExit(ExitName,fromPos,toPos,toLand,toZone);
					}
				}
				else if(line.startsWith("PARTICLESYSTEM")){
					String partsys	= line.substring(line.indexOf("PARTICLESYSTEM") + "PARTICLESYSTEM".length() + 1);
					StringTokenizer st	= new StringTokenizer(partsys, " ");
					int NumPartSys   = ParseInteger(st);
					for(int i = 0; i < NumPartSys; i++)
					{
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						
						int		NumParts		= ParseInteger(st);
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						int		Part4Sec		= ParseInteger(st);
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						float		Variance	= ParseFloat(st);
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						boolean	Resurrect		= ParseBoolean(st);
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						float	MinDieAge	= ParseFloat(st);
						float	MaxDieAge	= ParseFloat(st);
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						CVector3 CreationColMin	= new CVector3( ParseFloat(st),ParseFloat(st),ParseFloat(st));
						CVector3 CreationColMax= new CVector3( ParseFloat(st),ParseFloat(st),ParseFloat(st));
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						CVector3 DieColorMin	= new CVector3( ParseFloat(st),ParseFloat(st),ParseFloat(st));
						CVector3 DieColorMax	= new CVector3( ParseFloat(st),ParseFloat(st),ParseFloat(st));
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						float   AlphaMinEmit	= ParseFloat(st);
						float   AlphaMaxEmit	= ParseFloat(st);
						float   AlphaMinDie 	= ParseFloat(st);
						float   AlphaMaxDie 	= ParseFloat(st);
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						CVector3 EmitterPos	= new CVector3( ParseFloat(st),ParseFloat(st),ParseFloat(st));
						CVector3 EmitterDev	= new CVector3( ParseFloat(st),ParseFloat(st),ParseFloat(st));
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						CVector3 Acceleration	= new CVector3( ParseFloat(st),ParseFloat(st),ParseFloat(st));
						float  AccellerationMin	= ParseFloat(st);
						float  AccellerationMax	= ParseFloat(st);
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						float  MinEmit			= ParseFloat(st);
						float  MaxEmit			= ParseFloat(st);
						float  MinDie 			= ParseFloat(st);
						float  MaxDie 			= ParseFloat(st);
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						float  MaxEmitSpeed 	= ParseFloat(st);
						float  MinEmitSpeed 	= ParseFloat(st);
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						CVector3 EmisDirection	= new CVector3( ParseFloat(st),ParseFloat(st),ParseFloat(st));
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						CVector3 EmisDeviation	= new CVector3( ParseFloat(st),ParseFloat(st),ParseFloat(st));
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						boolean PartsLeaveSys	= ParseBoolean(st);
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						float  SpinSpeedMin = ParseFloat(st);
						float  SpinSpeedMax	= ParseFloat(st);
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						int	  BillBoarding		= ParseInteger(st);
						line	= reader.readLine();
						st		= new StringTokenizer(line, " ");
						String  TextureFile		= ParseString(st);

						CParticleSystem ParticleSystem = new CParticleSystem();
						ParticleSystem.Initialize(NumParts);
						ParticleSystem.ParticlesCreatedPerSec = Part4Sec;
						ParticleSystem.CreationVariance = Variance;
						ParticleSystem.RecreateWhenDied = Resurrect;
						ParticleSystem.MinDieAge = MinDieAge;
						ParticleSystem.MaxDieAge = MaxDieAge;
						ParticleSystem.SetCreationColor(CreationColMin,CreationColMax);
						ParticleSystem.SetDieColor(DieColorMin,DieColorMax);
						ParticleSystem.SetAlphaValues(AlphaMinEmit,AlphaMaxEmit,AlphaMinDie,AlphaMaxDie);
						ParticleSystem.SetEmitter(EmitterPos,EmitterDev);
						ParticleSystem.SetAcceleration(Acceleration,AccellerationMin,AccellerationMax);
						ParticleSystem.SetSizeValues(MinEmit,MaxEmit,MinDie,MaxDie);
						ParticleSystem.MaxEmitSpeed = MaxEmitSpeed;
						ParticleSystem.MinEmitSpeed = MinEmitSpeed;
						ParticleSystem.SetEmissionDirection(EmisDirection,EmisDeviation);
						ParticleSystem.ParticlesLeaveSystem = PartsLeaveSys;
						ParticleSystem.SetSpinSpeed(SpinSpeedMin,SpinSpeedMax);
						ParticleSystem.Billboarding = BillBoarding;
						ParticleSystem.LoadTextureFromFile(gl,glu,TextureFile);
						AddParticleSys(ParticleSystem);
					}
				}
			}
		} catch (IOException ex) {
			System.err.println("Impossible load file .dat");
		}
		return true;
	}
	
	public String ParseString(StringTokenizer st)
	{
		return(st.nextToken());
	}

	public float ParseFloat(StringTokenizer st)
	{
		return(Float.valueOf(st.nextToken()).floatValue());
	}

	public int ParseInteger(StringTokenizer st)
	{
		return(Integer.valueOf(st.nextToken()).intValue());
	}

	public boolean ParseBoolean(StringTokenizer st)
	{
		return(Boolean.valueOf(st.nextToken()).booleanValue());
	}

	///////////////////////////////// KEYBOARD \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Queste funizioni rilevano l'input dalla tastiera
	/////
	///////////////////////////////// KEYBOARD \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void ProcessKeys(boolean[] keys,float FrameInterval)
	{
		if(Player != null)
		{
			Player.ProcessKeys(keys, Camera, FrameInterval);
		}
	}
	
	public void doCleanup()
	{
		// cancella le texture delle particelle
		if(!ParticleSyss.isEmpty()){
			// Cicla tra i mob
			for(int i = 0; i < ParticleSyss.size(); i++ ){
				(ParticleSyss.elementAt(i)).doCleanup(gl,glu);
			}
		}
		
		Exits = 			null;
		Objs =				null;
		Mobs = 				null;		
		Player = 			null;	
			
		if(!Lights.isEmpty()){
			// Cicla tra i mob
			for(int i = 0; i < Lights.size(); i++ ){
				(Lights.elementAt(i)).doCleanup(gl,glu);
			}
		}		
		Lights =			null;
				
		Camera = 			null;
		
		// cancella le texture dello skybox
		SkyBox.doCleanup(gl,glu);
		SkyBox = 			null;
		
		SkyPlane.doCleanup();
		SkyPlane = 			null;		
		
		Landscape = 		null;
		
		Octree.doCleanup(gl,glu);
		// cancella l'octree
		Octree = 			null;
		
		GFrictionConst =	0f;
		AFrictionConst =	0f;
		Gravity = 			null;
		Weather = 			null;
		TerrianFile = 		null;
		ZoneName = 			null;
		ZonePath = 			null;
		LandName = 			null;
	}
}
