/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE CLIENT

                            FILE CMAIN
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.client;

// Java classes
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.*;
import java.util.*;

// GL4Java classes
import gl4java.GLContext;
import gl4java.awt.GLCanvas;

public class CMain extends JFrame
{
	static Component	canvas;
	static JPanel 		contents;
   
	public static void main(String[] args) 
	{
		CMain start = new CMain();
		start.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		WindowListener wl = new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				System.exit(0); 
			}        
		};        
		start.addWindowListener(wl);
		start.pack();
		start.setSize(800,600);
		start.show();
	}
	
	public CMain()
	{	
		this.setTitle("AGLE Client ver. 0.2");
		contents = new JPanel(new BorderLayout());
		canvas = new JLabel();
		canvas.setSize(800,600);
		contents.add(canvas,BorderLayout.CENTER);
		this.getContentPane().add(contents);
		// Initalize canvas and pass h,w value
		ChangeCanvas(new CCanvas(800,600));
	}
	
	static public void ChangeCanvas(Component newCanvas)
	{
		try {
			RemoveCanvas();
			contents.remove(canvas);
			canvas = newCanvas;
			contents.add(canvas,BorderLayout.CENTER);
			contents.revalidate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public void RemoveCanvas()
	{
		if (GLCanvas.class.isInstance(canvas))
		{
			((GLCanvas) canvas).doCleanup();
			((GLCanvas) canvas).cvsDispose();
		}
	}

	protected void finalize()		//Release OpenGL resources
	{
		RemoveCanvas();
	}
	
}