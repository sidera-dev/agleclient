/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE agle.lib

                            FILE C3DMODEL
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
package agle.lib.loaders;

// Java classes
import java.util.ArrayList;
import java.io.*;
import java.lang.*;
import java.util.*;

import agle.lib.math.*;

public class C3DModel implements Serializable
{
	public int			numOfObjects;					// The number of objects in the model
	public int			numOfMaterials;					// The number of materials for the model
	public ArrayList	pMaterials = new ArrayList();	// The list of material information (Textures and colors)
	public ArrayList	pObject = new ArrayList();		// The object list for our model

	public int			numOfAnimations;				// The number of animations in this model 
	public int			currentAnim;					// The current index into pAnimations list 
	public int			currentFrame;					// The current frame of the current animation 
	public int			nextFrame;						// The next frame of animation to interpolate too
	public float		t;								// The ratio of 0.0f to 1.0f between each key frame
	public long			lastTime;						// This stores the last time that was stored
	public ArrayList	pAnimations = new ArrayList();	// The list of animations

	public int			numOfTags;						// This stores the number of tags in the model
	public C3DModel[]	pLinks;							// This stores a list of pointers that are linked to this model
	public C3DModelLoader2.tMd3Tag[]	pTags;			// This stores all the tags for the model animations

	public float		width;
	public float		height;
	public float		depth;
	public CVector3		vCenter = new CVector3(0, 0, 0);
}