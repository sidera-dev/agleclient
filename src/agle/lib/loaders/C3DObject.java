/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE agle.lib

                            FILE C3DOBJECT
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

// This holds all the information for our model/scene. 
// You should eventually turn into a robust class that 
// has loading/drawing/querying functions like:
// LoadModel(...); DrawObject(...); DrawModel(...); DestroyModel(...);
package agle.lib.loaders;

import java.io.*;
import java.lang.*;
import java.util.*;

import agle.lib.math.*;

public class C3DObject implements Serializable
{
	public int			numOfVerts;			// The number of verts in the model
	public int			numOfFaces;			// The number of faces in the model
	public int			numTexVertex;		// The number of texture coordinates
	public int			materialID;			// The texture ID to use, which is the index into our texture array
	public boolean		bHasTexture;		// This is TRUE if there is a texture map for this object
	public String		strName;			// The name of the object
	public CVector3[]	pVerts;				// The object's vertices
	public CVector3[]	pNormals;			// The object's normals
	public CVector2[]	pTexVerts;			// The texture's UV coordinates
	public CFace[]		pFaces;				// The faces information of the object
	public int[]		pIndices;			// The straight face indices in a row, used for vertex arrays
};
