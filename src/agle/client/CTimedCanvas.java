/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE CLIENT

                            FILE CTIMEDCANVAS
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
import agle.lib.*;
import agle.lib.font.*;

///////////////////////////////// CTIMEDCANVAS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	Questa estensione del canvas aggiunge il calcolo del frame rate e dell
/////	intervallo tra i frames in piu' aggiorna le varibaili globali
/////
///////////////////////////////// CTIMEDCANVAS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public abstract class CTimedCanvas extends GLAnimCanvas implements KeyListener, MouseListener, MouseMotionListener
{	    
	// Varabili 
	public float width;
	public float height;
	public boolean[] keys=new boolean[256];
	public float FrameInterval = 0;
	public float FrameRate = 0;
	public long frameTime = 0;			// This stores the last frame's time
	public long lastTime = 0;			// This will hold the time from the last frame
	public int framesPerSecond = 0;	// This will store our fps
      
   
	public CTimedCanvas(int w, int h)     
	{
		super(w, h);     
		// Registra al canvas e processo gli eventi della tastiera e del mouse
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		// Teniamo un alto framerate
		setAnimateFps(60);
		//  Impostiamo d'apprima queste variabili con il valore di default dell'applet  
		width =  (float)w;
		height = (float)h; 
	}    
   	////////////////////////////// RESHAPE\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	 ridimensiona la finestra (int width, int height)  
	/////
	////////////////////////////// RESHAPE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\ 
	public void reshape(int _width, int _height)
	{
		// Ad ogni resize della finestra aggiorna le le variabili
		width =  (float)_width;
		height = (float)_height; 
		// Reset The Current Viewport And Perspective Transformation
		gl.glViewport(0, 0, _width, _height);
		// Select The Projection Matrix     
		gl.glMatrixMode(GL_PROJECTION); 
		// Reset The Projection Matrix
		gl.glLoadIdentity();
		// Calculate The Aspect Ratio Of The Window
		glu.gluPerspective(45.0f, _width / _height, 1.0f, 1000.0f);
		// Select The Modelview Matrix
		gl.glMatrixMode(GL_MODELVIEW);
		// Reset The ModalView Matrix
		gl.glLoadIdentity();
	}
      
	// void preInit() Called just BEFORE the GL-Context is created.
	public void preInit()
	{
		// buffering but not stereoview  
		doubleBuffer = true; stereoView = false;
 		// request 8 stencil bits
 		stencilBits = 8;
	}

	////////////////////////////// INIT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Void init() Called just AFTER the GL-Context is created.
	/////
	////////////////////////////// INIT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\ 
	public void init()
	{	                  
		// Ridimesione la finestra
		reshape(getSize().width, getSize().height);
		// Avvia l'animazine del canvas 
		start();
	}        
         
	// void display() Draw to the canvas. 
	// Purely a Java thing. Simple calls DrawGLScene once GL is initialized
	public void display()     
	{
		if (glj.gljMakeCurrent() == false) return;  //Ensure GL is initialised correctly
		DrawGLScene();
		glj.gljSwap();             //Swap buffers
		glj.gljCheckGL();		
		glj.gljFree();             // release GL
	}
	      
    //////////////////////////// DRAWGLSCENE \\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Funzione principale che disegna la scena sul monitor
	/////
	//////////////////////////// DRAWGLSCENE \\\\\\\\\\\\\\\\\\\\\\\\\\\   
	public void DrawGLScene()     
	{ 
		// Calcola il frame rate
		CalculateFrameRate();

		// Clear The Screen And The Depth Buffer          
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();       
 
		gl.glColor3f(1.0f,1.0f,1.0f);
		
		Render();
	} 
	
	//////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\
	/////
	/////	Funzione da implementare nelle sottocalssi, usata per 
	/////	la stampa a schermo
	/////
	//////////////////////////// RENDER \\\\\\\\\\\\\\\\\\\\\\\\\\\   
	public void Render() { }     

	///////////////////////////////// CALCULATE FRAME RATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Questa funzione calcola il frame rate e l'intervallo tra i frames
	/////
	///////////////////////////////// CALCULATE FRAME RATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void CalculateFrameRate()
	{
		// Get the current time in seconds.  Notice that we use timeGetTime() instead
		// of GetTickCount().  This is because on some computers, depending on their
		// CPU, GetTickCount() does update as fast or correctly as clock().  You need
		// to include <mmsystem.h> to use this function.  You will also need to include 
		// winmm.lib and mmsystem.h in your code.
		long currentTime = System.currentTimeMillis();				
		// Here we store the elapsed time between the current and last frame,
		// then keep the current frame in our static variable for the next frame.
		FrameInterval = ((float)(currentTime - frameTime))/1000;
		frameTime = currentTime;

		// Increase the frame counter
		++framesPerSecond;

		// Now we want to subtract the current time by the last time that was stored
		// to see if the time elapsed has been over a second, which means we found our FPS.
		if( currentTime - lastTime > 1000 )
		{
			// Here we set the lastTime to the currentTime
			lastTime = currentTime;
			
			FrameRate = framesPerSecond;

			// Reset the frames per second
			framesPerSecond = 0;
		}
	}
       
	// Key Listener events
	public void keyTyped(KeyEvent e)
	{
	}
   
	public void keyPressed(KeyEvent e)
	{
		// only interested in first 250 key codes
		if(e.getKeyCode()<250)
			keys[e.getKeyCode()]=true;
	}
   
	public void keyReleased(KeyEvent e)
	{
		// only interested in first 250 key codes
		if(e.getKeyCode()<250)  
			keys[e.getKeyCode()]=false;
	}
   
	// mouse listener events 
	public void mouseMoved(MouseEvent evt)
	{
	}

	public void mouseDragged(MouseEvent evt)
	{
	}

	// These Methods Override the default implementation of MouseListener in GLAnimCanvas
	public void mouseEntered( MouseEvent evt )
	{
		Component comp = evt.getComponent();
		if( comp.equals(this ) )
		{
			requestFocus();
		}
	}

	public void mouseClicked( MouseEvent evt )
	{ 
		Component comp = evt.getComponent();
		if( comp.equals(this ) )
		{
			requestFocus();
		}
	}
	
	public void doCleanup()
	{
	}
   
}
