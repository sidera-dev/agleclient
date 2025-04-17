/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE agle.lib

                            FILE CFACE
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
package agle.lib.loaders;

import java.util.ArrayList;
import java.io.*;
import java.lang.*;
import java.util.*;
// This is our face structure.  This is is used for indexing into the vertex 
// and texture coordinate arrays.  From this information we know which vertices
// from our vertex array go to which face, along with the correct texture coordinates.
public class CFace implements Serializable
{
	public int[] vertIndex	= new int[3];			// indicies for the verts that make up this triangle
	public int[] coordIndex = new int[3];			// indicies for the tex coords to texture this face
	
};
