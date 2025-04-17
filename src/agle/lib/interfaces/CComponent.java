/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE agle.lib

                            FILE CInterfaces
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.lib.interfaces;

// Java  classes
import java.io.*;

public abstract class CComponent {
	public float   X;
	public float   Y;
	public float   Width;
	public float   Height;    
	public static  float   zOrder = 0.0f;
	public float   Alpha;
	public boolean Visible;
}  