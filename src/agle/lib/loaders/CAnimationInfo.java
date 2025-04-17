/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE agle.lib

                            FILE CANIMATIONINFO
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
package agle.lib.loaders;

// Java classes
import java.util.ArrayList;
import java.io.*;
import java.lang.*;
import java.util.*;

// This holds our information for each animation of the Quake model.
// A STL vector list of this structure is created in our C3DModel structure below.
public class CAnimationInfo
{
	public String strName;				// This stores the name of the animation (I.E. "TORSO_STAND")
	public int startFrame;				// This stores the first frame number for this animation
	public int endFrame;				// This stores the last frame number for this animation
	public int loopingFrames;			// This stores the looping frames for this animation (not used)
	public int framesPerSecond;		// This stores the frames per second that this animation runs
}