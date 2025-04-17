/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE CLIENT

                            FILE CANVAS
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.client;

// Java  classes
import java.io.*;
import java.lang.*;
import java.util.*;
import java.awt.event.*;
import java.awt.*;
import javax.sound.midi.*;

// GL4Java classes

import gl4java.*;
import gl4java.utils.textures.*;
import gl4java.GLContext;
import gl4java.awt.GLAnimCanvas;

// AGLE classes
import agle.lib.interfaces.*;
import agle.lib.camera.*;
import agle.lib.font.*;
import agle.lib.math.*;
import agle.lib.loaders.*;
import agle.lib.physics.*;
import agle.lib.utils.*;


///////////////////////////////// CCAVAS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	This class implement CTimedCanvas, automatically update and calculate 
/////	feamerate and frameinterval and has mause and keyboard listeners
/////
///////////////////////////////// CCANVAS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
class CCanvas extends CTimedCanvas
{   	 
	CScreen 		CurrentClass; 
	CConnection		Connessione;
	CStartScreen 	StartScreen;
	CCreationScreen	CreationScreen;
	CContinueScreen	ContinueScreen;	
	
	CZone Zone;
	
    CMidiLoader music;
	
	CGLFont glFont;
		
	public CCanvas(int w, int h)     
	{
		super(w, h);
		// Canvas registration and add keyboard and mouse listener
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		// keep high framerate
		setAnimateFps(60);
		//  Set this vars with applet defaults values
		width =  (float)w;
		height = (float)h; 
		// Init connection
		Connessione = new CConnection();		
	} 
      

	////////////////////////////// INIT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Void init() Called just AFTER the GL-Context is created.
	/////	Sovrascrive quello della CTimedCanvas   
	/////
	////////////////////////////// INIT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\ 
	public void init()
	{			
		
		glFont = new CGLFont(gl,glu,"../data/Font.png");
		glFont.width = width;
		glFont.height = height;  
		
		music = new CMidiLoader();
						
		ContinueScreen = new CContinueScreen();
		
		CreationScreen = new CCreationScreen();
				
		StartScreen = new CStartScreen();
		CurrentClass = (CScreen)StartScreen;   
		
		// Opengl initalization                                  
		gl.glEnable(GL_TEXTURE_2D);    
		gl.glShadeModel(GL_SMOOTH);                            
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);               
		gl.glClearDepth(1.0);                                  
		gl.glEnable(GL_DEPTH_TEST);                           
		gl.glDepthFunc(GL_LEQUAL);                             
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);	
			          
		Zone = new CZone(gl,glu,glFont,(int)width, (int)height,"Enter","Portal");
		Zone.init();	
					                
		// Window redimension
		reshape(getSize().width, getSize().height);
		// Start window animatin
		start();
	}  
	         	      
    //////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Principal function wich draw the screen 
	/////	Function render is called upon currentclass object
	/////	assigned every time by keypress 	
	/////
	//////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\   
	public void Render()     
	{   
		CurrentClass.ProcessKeys();  		
		CurrentClass.Render();    
	}
	
	public void doCleanup()
	{
		CurrentClass.doCleanup();
		
		CurrentClass	= null; 
		Connessione		= null;

		StartScreen		= null;
		CreationScreen	= null;
		ContinueScreen	= null;
		
		//Stop the animation
		stop();
	}
	
	///////////////////////////////// CSCREEN \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	This is the class constructor
	/////
	///////////////////////////////// CSCREEN \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public abstract class CScreen
	{
		public abstract void Render();
		public abstract void doCleanup();		
		public abstract void ProcessKeys(); 
	}
	
	///////////////////////////////// CSTARTSCREEN \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	This is the class constructor
	/////
	///////////////////////////////// CSTARTSCREEN \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public class CStartScreen extends CScreen
	{
		CParticleSystem ParticleSystem;
		int[] texture = new int[2]; 
		
		public CStartScreen()
		{
       		// load a sequence
        	Sequence sequence = music.getSequence("../data/intro.mid");

       		// play the sequence
        	music.play(sequence, true);			
					
			// Load intro texture
			texture[0] = CTextureloader.LoadPng(gl,glu,"../data/intro.png");	

			//Init particle system 
			
			ParticleSystem = new CParticleSystem();
			ParticleSystem.Initialize(1000);
			ParticleSystem.ParticlesCreatedPerSec = 500;
			ParticleSystem.CreationVariance = 0.0f;
			ParticleSystem.RecreateWhenDied = false;
			ParticleSystem.MinDieAge = 2.0f;
			ParticleSystem.MaxDieAge = 2.0f;
			ParticleSystem.SetCreationColor(0.0f,0.0f,1.0f,0.3f,0.3f,1.0f);
			ParticleSystem.SetDieColor(0.4f,0.4f,1.0f, 0.0f,0.0f,1.0f);
			ParticleSystem.SetAlphaValues(1.0f,1.0f,1.0f,1.0f);  
			ParticleSystem.SetEmitter(0.0f,1.0f,0.0f, 3.0f,0.0f,3.0f);
			CVector3 acc = new CVector3(0.0f,0.0f,0.0f);
			ParticleSystem.SetAcceleration(acc,0.0f,0.0f);
			ParticleSystem.SetSizeValues(0.08f,0.08f,0.08f,0.08f);
			ParticleSystem.MaxEmitSpeed = 1.0f;
			ParticleSystem.MinEmitSpeed = 1.0f;
			ParticleSystem.SetEmissionDirection(0.0f,-0.8f,0.0f,0.0f,0.0f,0.0f);
			ParticleSystem.ParticlesLeaveSystem = true;
			ParticleSystem.Billboarding = 2;
			ParticleSystem.LoadTextureFromFile(gl,glu,"../data/flare.png");
			
		}
		                       
		public void DrawBackground()
		{
			gl.glBindTexture(gl.GL_TEXTURE_2D, texture[0]);	// Select Our Font Texture
			gl.glMatrixMode(gl.GL_PROJECTION);		    	// Select The Projection Matrix
			gl.glPushMatrix();								// Store The Projection Matrix
			gl.glLoadIdentity();							// Reset The Projection Matrix
			gl.glOrtho(0,width,height,0,-1,1);				// Set Up An Ortho Screen
			gl.glMatrixMode(gl.GL_MODELVIEW);				// Select The Modelview Matrix
			gl.glPushMatrix();								// Store The Modelview Matrix
			gl.glLoadIdentity();							// Reset The Modelview Matrix
	           
			// background draw
			gl.glBegin(gl.GL_QUADS);
			gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(0.0f, height, -1.0f);
			gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(width, height, -1.0f);
			gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(width, 0.0f, -1.0f);
			gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(0.0f, 0.0f, -1.0f);      
			gl.glEnd();  
			      
			gl.glMatrixMode(gl.GL_PROJECTION);					// Select The Projection Matrix
			gl.glPopMatrix();									// Restore The Old Projection Matrix
			gl.glMatrixMode(gl.GL_MODELVIEW);					// Select The Modelview Matrix
			gl.glPopMatrix();									// Restore The Old Projection Matrix                       
		}
		
		public void Render()   
		{
			// background draw
			DrawBackground();
			
			gl.glBlendFunc(gl.GL_ONE,gl.GL_ONE);
			gl.glEnable(gl.GL_BLEND);  	       
			glFont.glPrint(gl,glu,300,300,  "PRESS 1,2,3 NUMPAD KEYS", 0, (float)(1), (float)(1), (float)(0));
			gl.glDisable(gl.GL_BLEND);								
			
			gl.glPushMatrix();
			gl.glEnable(gl.GL_POINT_SMOOTH);
			gl.glEnable(gl.GL_BLEND);
			gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE);
			gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_FILL);
			gl.glDepthMask(gl.GL_FALSE);
			gl.glEnable(gl.GL_DEPTH_TEST);
		
			ParticleSystem.UpdateSystem(FrameInterval);
			ParticleSystem.Render(gl,glu);
					
			gl.glDepthMask(gl.GL_TRUE);
			gl.glPopMatrix();
			gl.glDisable(gl.GL_BLEND);				

		}	
		
		public void doCleanup()
		{
			gl.glDeleteTextures(texture.length,texture);
		}
		
		public void ProcessKeys()
		{		
			if(keys[KeyEvent.VK_NUMPAD1] ){
				CurrentClass = (CScreen)StartScreen;   
			}
			if(keys[KeyEvent.VK_NUMPAD2] ){
				CurrentClass = (CScreen)CreationScreen;   
			}
			if(keys[KeyEvent.VK_NUMPAD3] ){
				CurrentClass = (CScreen)ContinueScreen;   
			}
		} 

	}
	
	  
	///////////////////////////////// CCREATIONCREEN \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	This is the class constructor
	/////
	///////////////////////////////// CCREATIONCREEN \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public class CCreationScreen extends CScreen
	{
		float Angle;
		int[] texture = new int[2]; 
		
		public CCreationScreen()
		{		
			texture[0] = CTextureloader.LoadPng(gl,glu,"../data/starburst.png");
		} 			      
		 		
		public void DrawStarbust()
		{
			Angle += 0.15f;
			if(Angle == 360.0f){
				Angle=0.0f;
			}
			gl.glPushMatrix();   
			gl.glTranslatef(0.0f,0.5f,-4.5f);
			gl.glBlendFunc(gl.GL_ONE,gl.GL_ONE);
			gl.glEnable(gl.GL_BLEND);         
			gl.glRotatef((Angle/2.0f), 0.0f, 0.0f, 1.0f);
			gl.glBindTexture(gl.GL_TEXTURE_2D, texture[0]);
			gl.glBegin(gl.GL_QUADS);
			gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-3.0f,-3.0f, 0.0f);
			gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f( 3.0f,-3.0f, 0.0f);
			gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( 3.0f, 3.0f, 0.0f);
			gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-3.0f, 3.0f, 0.0f);
			gl.glEnd();
			gl.glRotatef(-(Angle), 0.0f, 0.0f, 1.0f);
			gl.glBindTexture(gl.GL_TEXTURE_2D, texture[0]);
			gl.glBegin(gl.GL_QUADS);
			gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-3.0f,-3.0f, 0.0f);
			gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f( 3.0f,-3.0f, 0.0f);
			gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f( 3.0f, 3.0f, 0.0f);
			gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-3.0f, 3.0f, 0.0f);
			gl.glEnd();
			gl.glDisable(gl.GL_BLEND);			     
			gl.glPopMatrix();      
		} 
	
		public void Render()   
		{
			DrawStarbust();
			gl.glBlendFunc(gl.GL_ONE,gl.GL_ONE);
			gl.glEnable(gl.GL_BLEND);  				
			glFont.glPrint(gl,glu,300,300,  "PRESS 1,2,3 NUMPAD KEYS", 0, (float)(1), (float)(1), (float)(0));
			gl.glDisable(gl.GL_BLEND);									
			
		}
		
		public void doCleanup()
		{
			gl.glDeleteTextures(texture.length,texture);
		}
				
		public void ProcessKeys()
		{		
			if(keys[KeyEvent.VK_NUMPAD1] ){
				CurrentClass = (CScreen)StartScreen;   
			}
			if(keys[KeyEvent.VK_NUMPAD2] ){
				CurrentClass = (CScreen)CreationScreen;   
			}
			if(keys[KeyEvent.VK_NUMPAD3] ){
				CurrentClass = (CScreen)ContinueScreen;   
			}
		} 			
	} 
		
	
	///////////////////////////////// CCONTINUESCREEN \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	This is the class constructor
	/////
	///////////////////////////////// CCONTINUESCREEN \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	public class CContinueScreen extends CScreen
	{		
		public CContinueScreen()
		{			
		}
		
		public void Render()   
		{						
			Zone.Render(FrameInterval);
			gl.glPushMatrix();   
			gl.glBlendFunc(gl.GL_ONE,gl.GL_ONE);
			gl.glEnable(gl.GL_BLEND);
			gl.glColor3f(1.0f,0.0f,0.0f);			// Set The Color To Red			  	
			glFont.glPrint(gl,glu,100,100,  "Movment use arrows keys", 0, (float)(1), (float)(1), (float)(0));
			glFont.glPrint(gl,glu,100,130,  "Camera use 4 and 6 pad keys", 0, (float)(1), (float)(1), (float)(0));
			glFont.glPrint(gl,glu,100,160,  "To use a magic press space", 0, (float)(1), (float)(1), (float)(0));	
			glFont.glPrint(gl,glu,100,190,  "To use sword press enter", 0, (float)(1), (float)(1), (float)(0));					
			gl.glDisable(gl.GL_BLEND);	
			gl.glPopMatrix(); 			
			 				
		}
		public void doCleanup()
		{
			Zone.doCleanup();
		}
		
		public void ProcessKeys()
		{		
			if(keys[KeyEvent.VK_NUMPAD1] ){				
				CurrentClass = (CScreen)StartScreen;   
			}
			if(keys[KeyEvent.VK_NUMPAD2] ){			
				CurrentClass = (CScreen)CreationScreen;   
			}
			if(keys[KeyEvent.VK_NUMPAD3] ){			
				CurrentClass = (CScreen)ContinueScreen;   
			}
			Zone.ProcessKeys(keys,FrameInterval);
		}		
		
	}
	
	///////////////////////////////// KEYBOARD \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Keybiard input menagment
	/////
	///////////////////////////////// KEYBOARD \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void ProcessKeys()
	{		
		if(keys[KeyEvent.VK_NUMPAD1] ){
			CurrentClass = (CScreen)StartScreen;   
		}
		if(keys[KeyEvent.VK_NUMPAD2] ){
			CurrentClass = (CScreen)CreationScreen;   
		}
		if(keys[KeyEvent.VK_NUMPAD3] ){
			CurrentClass = (CScreen)ContinueScreen;   
		}	
	} 

}