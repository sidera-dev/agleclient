/*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                      AGLE LIB

                            FILE COCTREE
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package agle.lib.utils;

// Java classes
import gl4java.*;
import java.util.ArrayList;

// classes
import agle.lib.*;
import agle.lib.math.*;
import agle.lib.loaders.*;
import agle.lib.utils.*;

///////////////////////////////// COCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/////
/////	Questa classe serve a creare un octree, ovvero un albero che suddivide
/////	lo spazio in cubi, o meglio in 8 sottocubi ricorsivamente, usato per
/////	rilevare di collisioni, ma soprattutto per sapere quali sottocubi 
/////	sono nella nostra frustum (campo visivo della telecamera) e di conseguenza
/////	quali poligoni o parti di essi vediamo e sono da renderizzare
/////
///////////////////////////////// COCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
public class COctree
{
	static final int TOP_LEFT_FRONT		= 0;
	static final int TOP_LEFT_BACK		= 1;
	static final int TOP_RIGHT_BACK		= 2;
	static final int TOP_RIGHT_FRONT	= 3;
	static final int BOTTOM_LEFT_FRONT	= 4;
	static final int BOTTOM_LEFT_BACK	= 5;
	static final int BOTTOM_RIGHT_BACK	= 6;
	static final int BOTTOM_RIGHT_FRONT	= 7;

	// The current amount of subdivisions we are currently at.
	// This is used to make sure we don't go over the max amount
	public static int CurrentSubdivision = 0;

	// The maximum amount of triangles per node
	public static int MaxTriangles;

	// The maximum amount of subdivisions allow (Levels of subdivision)
	public static int MaxSubdivisions;

	// The amount of nodes created in the octree
	public static int EndNodeCount;
	
	// Extern our debug object because we use it in the octree code
	public boolean bDebug = false;
	public static CDebug Debug;

	// Extern our global frustum object so we can check if our nodes are 
	// inside the frustum before we draw them.
	public static CFrustum Frustum;

	// This stores the amount of nodes that are in the frustum
	public int TotalNodesDrawn = 0;

	// This tells us if we have divided this node into more sub nodes
	public boolean m_bSubDivided;

	// This is the size of the cube for this current node
	float m_Width;

	// This holds the amount of triangles stored in this node
	public int TriangleCount;

	// This is the center (X, Y, Z) point in this node
	CVector3 m_vCenter;
	
	// Initialize some temporary variables to hold the max dimensions found
	public float maxWidth, maxHeight, maxDepth;
		
	// This stores the triangles that should be drawn with this node
	CVector3[] m_pVertices;

	// These are the eight nodes branching down from this current node
	COctree[] m_pOctreeNodes;	

	// This returns the center of this node
	public CVector3 GetCenter() {	 return m_vCenter;	}

	// This returns the triangle count stored in this node
	public int GetTriangleCount()  {   return TriangleCount;	}

	// This returns the widht of this node (since it's a cube the height and depth are the same)
	public float GetWidth() {	 return m_Width;	}

		// This returns if this node is subdivided or not
	public boolean IsSubDivided()  {   return m_bSubDivided;	}

	// This returns this nodes display list ID
	public int GetDisplayListID()		{   return m_DisplayListID;		}

	// This sets the nodes display list ID
	public void SetDisplayListID(int displayListID)	{	m_DisplayListID = displayListID;  }

	// This is used for our pLists in CreateNode() to help partition the world into
	// different nodes.

	class FaceList
	{
		// This is a vector of booleans to store if the face index is in the nodes 3D Space
		ArrayList pFaceList;	

		// This stores the total face count that is in the nodes 3D space (how many "true"'s)
		int totalFaceCount;
	};


	// This holds all the scene information (verts, normals, texture info, etc..) for this node
	C3DModel m_pWorld;

	// This stores the indices into the original model's object list
	ArrayList m_pObjectList = new ArrayList();

	// This holds the display list ID for the current node, which increases the rendering speed
	int m_DisplayListID;
	
	public int[] Texture;

	///////////////////////////////// OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	The COctree contstructor which calls our init function
	/////
	///////////////////////////////// OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public COctree()
	{	
		// Set the subdivided flag to false
		m_bSubDivided = false;

		// Set the dimensions of the box to false
		m_Width = 0; 

		// Initialize the triangle count
		TriangleCount = 0;

		// Initialize the center of the box to the 0
		m_vCenter = new CVector3(0, 0, 0);
		
		// Notice that we got rid of our InitOctree() function and just stuck the
		// initialization code in our constructor.  This is because we no longer need
		// to create the octree in real-time.

		// Initialize our world data to NULL.  This stores all the object's
		// face indices that need to be drawn for this node.  
		//m_pWorld = null;
		// Set the sub nodes to NULL
		m_pOctreeNodes = new COctree[8];
		
		Texture = new int[100];
	}

	///////////////////////////////// INIT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	The Init function
	/////
	///////////////////////////////// INIT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void Init(GLFunc gl,GLUFunc glu,C3DModel Landscape, String texturePath, int maxtriangles, int maxsubdivision)
	{
		if( bDebug = true)
			Debug = new CDebug();
			this.Debug = Debug;
		
		MaxTriangles = maxtriangles;
		MaxSubdivisions = maxsubdivision;
			// Go through all the materials
		for(int i = 0; i < Landscape.numOfMaterials; i++)
		{
			// Check to see if there is a file name to load in this material
			if( ((CMaterialInfo)Landscape.pMaterials.get(i)).strFile != null)
			{
				// Use the name of the texture file to load the bitmap, with a texture ID (i).
				// We pass in our global texture array, the name of the texture, and an ID to reference it.	
				Texture[i] = CTextureloader.LoadJpg(gl,glu,texturePath + ((CMaterialInfo)Landscape.pMaterials.get(i)).strFile);
			}
			// Set the texture ID for this material
			((CMaterialInfo)Landscape.pMaterials.get(i)).texureId = i;
		}
		GetSceneDimensions(Landscape);
		int TotalTriangleCount = GetSceneTriangleCount(Landscape);
		CreateNode(Landscape, TotalTriangleCount, GetCenter(), GetWidth());
		SetDisplayListID(gl.glGenLists(EndNodeCount));
		CreateDisplayList(gl,glu,this, Landscape, GetDisplayListID());
	}

	//////////////////////////// GET SCENE TRIANGLE COUNT \\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns the total number of polygons in our scene
	/////
	//////////////////////////// GET SCENE TRIANGLE COUNT \\\\\\\\\\\\\\\\\\\\\\\\\\*
	public int GetSceneTriangleCount(C3DModel pWorld)
	{
		// This function is only called once, right before we create our first root node.
		// Basically, we just go through all of the objects in our scene and add up their triangles.

		// Initialize a variable to hold the total amount of polygons in the scene
		int numberOfTriangles = 0;

		// Go through all the objects and add up their polygon count
		for(int i = 0; i < pWorld.numOfObjects; i++)
		{
			// Increase the total polygon count
			numberOfTriangles += ((C3DObject)pWorld.pObject.get(i)).numOfFaces;
		}

		// Return the number of polygons in the scene
		return numberOfTriangles;
	}

	///////////////////////////////// OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This sets our initial width of the scene, as well as our center point
	/////
	///////////////////////////////// OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void GetSceneDimensions(C3DModel pWorld)
	{
		// Initialize some temporary variables to hold the max dimensions found
		maxWidth = 0; maxHeight = 0; maxDepth = 0;
		// Return from this function if we passed in bad data.  This used to be a check
		// to see if the vertices passed in were allocated, now it's a check for world data.
		if(pWorld == null) return;

		// Initialize a variable to hold the total amount of vertices in the scene
		int numberOfVerts = 0;

		// This code is still doing the same things as in the previous tutorials,
		// except that we have to go through every object in the scene to find the
		// center point.

		// Go through all of the object's vertices and add them up to eventually find the center
		for(int i = 0; i < pWorld.numOfObjects; i++)
		{
			// Increase the total vertice count
			numberOfVerts += ((C3DObject)pWorld.pObject.get(i)).numOfVerts;

			// Add the current object's vertices up
			for(int n = 0; n < ((C3DObject)pWorld.pObject.get(i)).numOfVerts; n++)
			{
				// Add the current vertex to the center variable (Using operator overloading)
				m_vCenter = CMath3d.Add(m_vCenter , ((C3DObject)pWorld.pObject.get(i)).pVerts[n]);
			}
		}
		// Divide the total by the number of vertices to get the center point.
		// We could have overloaded the / symbol but I chose not to because we rarely use it.
		m_vCenter.x /= numberOfVerts;
		m_vCenter.y /= numberOfVerts;	
		m_vCenter.z /= numberOfVerts;

		// Now that we have the center point, we want to find the farthest distance from
		// our center point.  That will tell us how big the width of the first node is.
		// Once we get the farthest height, width and depth, we then check them against each
		// other.  Which ever one is higher, we then use that value for the cube width.

		int currentWidth = 0, currentHeight = 0, currentDepth = 0;

		// This code still does the same thing as in the previous octree tutorials,
		// except we need to go through each object in the scene to find the max dimensions.

		// Go through all of the scene's objects
		for(int i = 0; i < pWorld.numOfObjects; i++)
		{
			// Go through all of the current objects vertices
			for(int j = 0; j < ((C3DObject)pWorld.pObject.get(i)).numOfVerts; j++)
			{
				// Get the distance in width, height and depth this vertex is from the center.
				currentWidth  = (int)Math.abs(((C3DObject)pWorld.pObject.get(i)).pVerts[j].x - m_vCenter.x);	
				currentHeight = (int)Math.abs(((C3DObject)pWorld.pObject.get(i)).pVerts[j].y - m_vCenter.y);		
				currentDepth  = (int)Math.abs(((C3DObject)pWorld.pObject.get(i)).pVerts[j].z - m_vCenter.z);

				// Check if the current width value is greater than the max width stored.
				if(currentWidth  > maxWidth)	maxWidth  = currentWidth;

				// Check if the current height value is greater than the max height stored.
				if(currentHeight > maxHeight)	maxHeight = currentHeight;

				// Check if the current depth value is greater than the max depth stored.
				if(currentDepth > maxDepth)		maxDepth  = currentDepth;
			}
		}
		// Set the member variable dimensions to the max ones found.
		// We multiply the max dimensions by 2 because this will give us the
		// full width, height and depth.  Otherwise, we just have half the size
		// because we are calculating from the center of the scene.
		maxWidth *= 2;		maxHeight *= 2;		maxDepth *= 2;

		// Check if the width is the highest value and assign that for the cube dimension
		if(maxWidth > maxHeight && maxWidth > maxDepth)
			m_Width = maxWidth;

		// Check if the height is the heighest value and assign that for the cube dimension
		else if(maxHeight > maxWidth && maxHeight > maxDepth)
			m_Width = maxHeight;

		// Else it must be the depth or it's the same value as some of the other ones
		else
			m_Width = maxDepth;
	}


	///////////////////////////////// GET NEW NODE CENTER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This returns the center point of the new subdivided node, depending on the ID
	/////
	///////////////////////////////// GET NEW NODE CENTER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	CVector3 GetNewNodeCenter(CVector3 vCenter, float width, int nodeID)
	{
		// I created this function which takes an enum ID to see which node's center
		// we need to calculate.  Once we find that we need to subdivide a node we find
		// the centers of each of the 8 new nodes.  This is what that function does.
		// We just tell it which node we want.

		// Initialize the new node center
		CVector3 vNodeCenter = new CVector3(0, 0, 0);

		// Create a dummy variable to cut down the code size
		CVector3 vCtr = vCenter;

		// Switch on the ID to see which subdivided node we are finding the center
		switch(nodeID)							
		{
			case TOP_LEFT_FRONT:
				// Calculate the center of this new node
				vNodeCenter = new CVector3(vCtr.x - width/4, vCtr.y + width/4, vCtr.z + width/4);
				break;

			case TOP_LEFT_BACK:
				// Calculate the center of this new node
				vNodeCenter = new CVector3(vCtr.x - width/4, vCtr.y + width/4, vCtr.z - width/4);
				break;

			case TOP_RIGHT_BACK:
				// Calculate the center of this new node
				vNodeCenter = new CVector3(vCtr.x + width/4, vCtr.y + width/4, vCtr.z - width/4);
				break;

			case TOP_RIGHT_FRONT:
				// Calculate the center of this new node
				vNodeCenter = new CVector3(vCtr.x + width/4, vCtr.y + width/4, vCtr.z + width/4);
				break;

			case BOTTOM_LEFT_FRONT:
				// Calculate the center of this new node
				vNodeCenter = new CVector3(vCtr.x - width/4, vCtr.y - width/4, vCtr.z + width/4);
				break;

			case BOTTOM_LEFT_BACK:
				// Calculate the center of this new node
				vNodeCenter = new CVector3(vCtr.x - width/4, vCtr.y - width/4, vCtr.z - width/4);
				break;

			case BOTTOM_RIGHT_BACK:
				// Calculate the center of this new node
				vNodeCenter = new CVector3(vCtr.x + width/4, vCtr.y - width/4, vCtr.z - width/4);
				break;

			case BOTTOM_RIGHT_FRONT:
				// Calculate the center of this new node
				vNodeCenter = new CVector3(vCtr.x + width/4, vCtr.y - width/4, vCtr.z + width/4);
				break;
		}

		// Return the new node center
		return vNodeCenter;
	}

	///////////////////////////////// CREATE NEW NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This figures out the new node information and then passes it into CreateNode()
	/////
	///////////////////////////////// CREATE NEW NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void CreateNewNode(C3DModel pWorld, ArrayList pList, int triangleCount,
								CVector3 vCenter, float width, int nodeID)
	{
		// This function is used as our helper function to partition the world data
		// to pass into the subdivided nodes.  The same things go on as in the previous
		// tutorials, but it's dealing with more than just vertices.  We are given
		// the world data that needs to be partitioned, the list of faces that are in
		// the new node about to be created, the triangle count, the parent node's center
		// and width, along with the enum ID that tells us which new node is being created.
		//
		// The FaceList structure stores a vector of booleans, which tell us if that face
		// index is in our end node (true) or not (false).  It also contains a integer
		// to tell us how many of those faces (triangles) are "true", or in other words, 
		// are in our node that is being created.  

		// Check if the first node found some triangles in it, if not we don't continue
		if(triangleCount == 0) return;
		
		// Here we create the temporary partitioned data model, which will contain
		// all the objects and triangles in this end node.
		C3DModel pTempWorld = new C3DModel();

		// Intialize the temp model data and assign the object count to it
		pTempWorld.numOfObjects = pWorld.numOfObjects;
		
		// Go through all of the objects in the current partition passed in
		for(int i = 0; i < pWorld.numOfObjects; i++)
		{
			// Get a pointer to the current object to avoid ugly code
			C3DObject pObject = (C3DObject)pWorld.pObject.get(i);

			// Create a new object, initialize it, then add it to our temp partition
			C3DObject newObject = new C3DObject();
			pTempWorld.pObject.add(newObject);

			// Assign the new node's face count, material ID, texture boolean and 
			// vertices to the new object.  Notice that it's not that pObject's face
			// count, but the pList's.  Also, we are just assigning the pointer to the
			// vertices, not copying them.
			((C3DObject)pTempWorld.pObject.get(i)).numOfFaces  = ((FaceList)pList.get(i)).totalFaceCount;
			((C3DObject)pTempWorld.pObject.get(i)).materialID  = pObject.materialID;
			((C3DObject)pTempWorld.pObject.get(i)).bHasTexture = pObject.bHasTexture;
			((C3DObject)pTempWorld.pObject.get(i)).pVerts      = pObject.pVerts;

			// Allocate memory for the new face list
			((C3DObject)pTempWorld.pObject.get(i)).pFaces = new CFace [((C3DObject)pTempWorld.pObject.get(i)).numOfFaces];

			// Create a counter to count the current index of the new node vertices
			int index = 0;

			// Go through all of the current object's faces and only take the ones in this new node
			for(int j = 0; j < pObject.numOfFaces; j++)
			{
				// If this current triangle is in the node, assign it's index to our new face list
				if( ((Boolean)((FaceList)pList.get(i)).pFaceList.get(j)).booleanValue() )	
				{
					((C3DObject)pTempWorld.pObject.get(i)).pFaces[index] = new CFace();
					((C3DObject)pTempWorld.pObject.get(i)).pFaces[index] = pObject.pFaces[j];
					index++;
				}
			}
		}
		// Now comes the initialization of the node.  First we allocate memory for
		// our node and then get it's center point.  Depending on the nodeID, 
		// GetNewNodeCenter() knows which center point to pass back (TOP_LEFT_FRONT, etc..)

		// Allocate a new node for this octree
		m_pOctreeNodes[nodeID] = new COctree();

		// Get the new node's center point depending on the nodexIndex (which of the 8 subdivided cubes).
		CVector3 vNodeCenter = GetNewNodeCenter(vCenter, width, nodeID);
			
		// Below, before and after we recurse further down into the tree, we keep track
		// of the level of subdivision that we are in.  This way we can restrict it.

		// Increase the current level of subdivision
		CurrentSubdivision++;

		// This chance is just that we pass in the temp partitioned world for this node,
		// instead of passing in just straight vertices.

		// Recurse through this node and subdivide it if necessary
		m_pOctreeNodes[nodeID].CreateNode(pTempWorld, triangleCount, vNodeCenter, width / 2);

		// Decrease the current level of subdivision
		CurrentSubdivision--;

		// To free the temporary partition, we just go through all of it's objects and
		// free the faces.  The rest of the dynamic data was just being pointed too and
		// does not to be deleted.  Finally, we delete the allocated pTempWorld.

		// Go through all of the objects in our temporary partition
		for(int i = 0; i < pWorld.numOfObjects; i++)
		{
			// If there are faces allocated for this object, delete them
			((C3DObject)pTempWorld.pObject.get(i)).pFaces = null;
		}

		// Delete the allocated partition
		pTempWorld = null;
	}


	///////////////////////////////// CREATE NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This is our recursive function that goes through and subdivides our nodes
	/////
	///////////////////////////////// CREATE NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void CreateNode(C3DModel pWorld, int numberOfTriangles, CVector3 vCenter, float width)
	{
		// Initialize this node's center point.  Now we know the center of this node.
		m_vCenter = vCenter;

		// Initialize this nodes cube width.  Now we know the width of this current node.
		m_Width = width;
		
		// Add the current node to our debug rectangle list so we can visualize it.
		// We can now see this node visually as a cube when we render the rectangles.
		// Since it's a cube we pass in the width for width, height and depth.
		Debug.AddDebugRectangle(vCenter, width, width, width);

		// Check if we have too many triangles in this node and we haven't subdivided
		// above our max subdivisions.  If so, then we need to break this node into
		// 8 more nodes (hence the word OCTree).  Both must be true to divide this node.
		if( (numberOfTriangles > MaxTriangles) && (CurrentSubdivision < MaxSubdivisions) )
		{
			// Since we need to subdivide more we set the divided flag to true.
			// This let's us know that this node does NOT have any vertices assigned to it,
			// but nodes that perhaps have vertices stored in them (Or their nodes, etc....)
			// We will querey this variable when we are drawing the octree.
			m_bSubDivided = true;
		
			// This function pretty much stays the same, except a small twist because
			// we are dealing with multiple objects for the scene, not just an array of vertices.
			// In the previous tutorials, we used a vector<> of booleans, but now we use our
			// FaceList to store a vector of booleans for each object.

			// Create the list of FaceLists for each child node
			ArrayList pList1 = new ArrayList(pWorld.numOfObjects);		// TOP_LEFT_FRONT node list
			ArrayList pList2 = new ArrayList(pWorld.numOfObjects);		// TOP_LEFT_BACK node list
			ArrayList pList3 = new ArrayList(pWorld.numOfObjects);		// TOP_RIGHT_BACK node list
			ArrayList pList4 = new ArrayList(pWorld.numOfObjects);		// TOP_RIGHT_FRONT node list
			ArrayList pList5 = new ArrayList(pWorld.numOfObjects);		// BOTTOM_LEFT_FRONT node list
			ArrayList pList6 = new ArrayList(pWorld.numOfObjects);		// BOTTOM_LEFT_BACK node list
			ArrayList pList7 = new ArrayList(pWorld.numOfObjects);		// BOTTOM_RIGHT_BACK node list
			ArrayList pList8 = new ArrayList(pWorld.numOfObjects);		// BOTTOM_RIGHT_FRONT node list

			// Create this variable to cut down the thickness of the code below (easier to read)
			CVector3 vCtr = vCenter;

			// Go through every object in the current partition of the world
			for(int i = 0; i < pWorld.numOfObjects; i++)
			{
				// Store a point to the current object
				C3DObject pObject = (C3DObject)pWorld.pObject.get(i);

				// Now, we have a face list for each object, for every child node.
				// We need to then check every triangle in this current object
				// to see if it's in any of the child nodes dimensions.  We store a "true" in
				// the face list index to tell us if that's the case.  This is then used
				// in CreateNewNode() to create a new partition of the world for that child node.

				pList1.add(new FaceList());
				pList2.add(new FaceList());
				pList3.add(new FaceList());
				pList4.add(new FaceList());
				pList5.add(new FaceList());
				pList6.add(new FaceList());
				pList7.add(new FaceList());
				pList8.add(new FaceList());

				// Resize the current face list to be the size of this object's face count
				((FaceList)pList1.get(i)).pFaceList = new ArrayList(pObject.numOfFaces);
				((FaceList)pList2.get(i)).pFaceList = new ArrayList(pObject.numOfFaces);
				((FaceList)pList3.get(i)).pFaceList = new ArrayList(pObject.numOfFaces);
				((FaceList)pList4.get(i)).pFaceList = new ArrayList(pObject.numOfFaces);
				((FaceList)pList5.get(i)).pFaceList = new ArrayList(pObject.numOfFaces);
				((FaceList)pList6.get(i)).pFaceList = new ArrayList(pObject.numOfFaces);
				((FaceList)pList7.get(i)).pFaceList = new ArrayList(pObject.numOfFaces);
				((FaceList)pList8.get(i)).pFaceList = new ArrayList(pObject.numOfFaces);

				// Go through all the triangles for this object
				for(int j = 0; j < pObject.numOfFaces; j++)
				{
					// Initialize to false first...
					((FaceList)pList1.get(i)).pFaceList.add(new Boolean(false));
					((FaceList)pList2.get(i)).pFaceList.add(new Boolean(false));
					((FaceList)pList3.get(i)).pFaceList.add(new Boolean(false));
					((FaceList)pList4.get(i)).pFaceList.add(new Boolean(false));
					((FaceList)pList5.get(i)).pFaceList.add(new Boolean(false));
					((FaceList)pList6.get(i)).pFaceList.add(new Boolean(false));
					((FaceList)pList7.get(i)).pFaceList.add(new Boolean(false));
					((FaceList)pList8.get(i)).pFaceList.add(new Boolean(false));

					// Check every vertice in the current triangle to see if it's inside a child node
					for(int whichVertex = 0; whichVertex < 3; whichVertex++)
					{
						// Store the current vertex to be checked against all the child nodes
						CVector3 vPoint = pObject.pVerts[pObject.pFaces[j].vertIndex[whichVertex]];

						// Check if the point lies within the TOP LEFT FRONT node
						if( (vPoint.x <= vCtr.x) && (vPoint.y >= vCtr.y) && (vPoint.z >= vCtr.z) ) 
							((FaceList)pList1.get(i)).pFaceList.set(j, new Boolean(true));

						// Check if the point lies within the TOP LEFT BACK node
						if( (vPoint.x <= vCtr.x) && (vPoint.y >= vCtr.y) && (vPoint.z <= vCtr.z) ) 
							((FaceList)pList2.get(i)).pFaceList.set(j, new Boolean(true));

						// Check if the point lies within the TOP RIGHT BACK node
						if( (vPoint.x >= vCtr.x) && (vPoint.y >= vCtr.y) && (vPoint.z <= vCtr.z) ) 
							((FaceList)pList3.get(i)).pFaceList.set(j, new Boolean(true));

						// Check if the point lies within the TOP RIGHT FRONT node
						if( (vPoint.x >= vCtr.x) && (vPoint.y >= vCtr.y) && (vPoint.z >= vCtr.z) ) 
							((FaceList)pList4.get(i)).pFaceList.set(j, new Boolean(true));

						// Check if the point lies within the BOTTOM LEFT FRONT node
						if( (vPoint.x <= vCtr.x) && (vPoint.y <= vCtr.y) && (vPoint.z >= vCtr.z) ) 
							((FaceList)pList5.get(i)).pFaceList.set(j, new Boolean(true));

						// Check if the point lies within the BOTTOM LEFT BACK node
						if( (vPoint.x <= vCtr.x) && (vPoint.y <= vCtr.y) && (vPoint.z <= vCtr.z) ) 
							((FaceList)pList6.get(i)).pFaceList.set(j, new Boolean(true));

						// Check if the point lies within the BOTTOM RIGHT BACK node
						if( (vPoint.x >= vCtr.x) && (vPoint.y <= vCtr.y) && (vPoint.z <= vCtr.z) ) 
							((FaceList)pList7.get(i)).pFaceList.set(j, new Boolean(true));

						// Check if the point lines within the BOTTOM RIGHT FRONT node
						if( (vPoint.x >= vCtr.x) && (vPoint.y <= vCtr.y) && (vPoint.z >= vCtr.z) ) 
							((FaceList)pList8.get(i)).pFaceList.set(j, new Boolean(true));
					}
				}	

				// Here we initialize the face count for each list that holds how many triangles
				// were found for each of the 8 subdivided nodes.
				((FaceList)pList1.get(i)).totalFaceCount = 0;		((FaceList)pList2.get(i)).totalFaceCount = 0;
				((FaceList)pList3.get(i)).totalFaceCount = 0;		((FaceList)pList4.get(i)).totalFaceCount = 0;
				((FaceList)pList5.get(i)).totalFaceCount = 0;		((FaceList)pList6.get(i)).totalFaceCount = 0;
				((FaceList)pList7.get(i)).totalFaceCount = 0;		((FaceList)pList8.get(i)).totalFaceCount = 0;
			}

			// Here we create a variable for each list that holds how many triangles
			// were found for each of the 8 subdivided nodes.
			int triCount1 = 0;	int triCount2 = 0;	int triCount3 = 0;	int triCount4 = 0;
			int triCount5 = 0;	int triCount6 = 0;	int triCount7 = 0;	int triCount8 = 0;
				
			// Go through all of the objects of this current partition
			for(int i = 0; i < pWorld.numOfObjects; i++)
			{
				// Go through all of the current objects triangles
				for(int j = 0; j < ((C3DObject)pWorld.pObject.get(i)).numOfFaces; j++)
				{
					// Increase the triangle count for each node that has a "true" for the index i.
					// In other words, if the current triangle is in a child node, add 1 to the count.
					// We need to store the total triangle count for each object, but also
					// the total for the whole child node.  That is why we increase 2 variables.
					if( ((Boolean)((FaceList)pList1.get(i)).pFaceList.get(j)).booleanValue() )	{ ((FaceList)pList1.get(i)).totalFaceCount++; triCount1++; }
					if( ((Boolean)((FaceList)pList2.get(i)).pFaceList.get(j)).booleanValue() )	{ ((FaceList)pList2.get(i)).totalFaceCount++; triCount2++; }
					if( ((Boolean)((FaceList)pList3.get(i)).pFaceList.get(j)).booleanValue() )	{ ((FaceList)pList3.get(i)).totalFaceCount++; triCount3++; }
					if( ((Boolean)((FaceList)pList4.get(i)).pFaceList.get(j)).booleanValue() )	{ ((FaceList)pList4.get(i)).totalFaceCount++; triCount4++; }
					if( ((Boolean)((FaceList)pList5.get(i)).pFaceList.get(j)).booleanValue() )	{ ((FaceList)pList5.get(i)).totalFaceCount++; triCount5++; }
					if( ((Boolean)((FaceList)pList6.get(i)).pFaceList.get(j)).booleanValue() )	{ ((FaceList)pList6.get(i)).totalFaceCount++; triCount6++; }
					if( ((Boolean)((FaceList)pList7.get(i)).pFaceList.get(j)).booleanValue() )	{ ((FaceList)pList7.get(i)).totalFaceCount++; triCount7++; }
					if( ((Boolean)((FaceList)pList8.get(i)).pFaceList.get(j)).booleanValue() )	{ ((FaceList)pList8.get(i)).totalFaceCount++; triCount8++; }
				}
			}

			// Next we do the dirty work.  We need to set up the new nodes with the triangles
			// that are assigned to each node, along with the new center point of the node.
			// Through recursion we subdivide this node into 8 more potential nodes.

			// Create the subdivided nodes if necessary and then recurse through them.
			// The information passed into CreateNewNode() are essential for creating the
			// new nodes.  We pass the 8 ID's in so it knows how to calculate it's new center.
			CreateNewNode(pWorld, pList1, triCount1, vCenter, width, TOP_LEFT_FRONT);
			CreateNewNode(pWorld, pList2, triCount2, vCenter, width, TOP_LEFT_BACK);
			CreateNewNode(pWorld, pList3, triCount3, vCenter, width, TOP_RIGHT_BACK);
			CreateNewNode(pWorld, pList4, triCount4, vCenter, width, TOP_RIGHT_FRONT);
			CreateNewNode(pWorld, pList5, triCount5, vCenter, width, BOTTOM_LEFT_FRONT);
			CreateNewNode(pWorld, pList6, triCount6, vCenter, width, BOTTOM_LEFT_BACK);
			CreateNewNode(pWorld, pList7, triCount7, vCenter, width, BOTTOM_RIGHT_BACK);
			CreateNewNode(pWorld, pList8, triCount8, vCenter, width, BOTTOM_RIGHT_FRONT);
		}
		else
		{
			// If we get here we must either be subdivided past our max level, or our triangle
			// count went below the minimum amount of triangles so we need to store them.
			
			// We pass in the current partition of world data to be assigned to this end node
			AssignTrianglesToNode(pWorld, numberOfTriangles);
		}
	}

	//////////////////////////// ADD OBJECT INDEX TO LIST \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This adds the index into the model's object list to our object index list
	/////
	//////////////////////////// ADD OBJECT INDEX TO LIST \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	void AddObjectIndexToList(int index)
	{
		// To eliminate the need to loop through all of the objects in the original
		// model, when drawing the end nodes, we create an instance of our C3DModel
		// structure to hold only the objects that lie in the child node's 3D space.

		// Go through all of the objects in our face index list
		for(int i = 0; i < m_pObjectList.size(); i++)
		{
			// If we already have this index stored in our object index list, don't add it.
			if( ((Integer)m_pObjectList.get(i)).intValue() == index)
				return;
		}

		// Add this index to our object index list, which indexes into the root world object list
		m_pObjectList.add(new Integer(index));
	}

	//////////////////////////// ASSIGN TRIANGLES TO NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This allocates memory for the face indices to assign to the current end node
	/////
	//////////////////////////// ASSIGN TRIANGLES TO NODE \\\\\\\\\\\\\\\\\\\\\\\\\\\*
	void AssignTrianglesToNode(C3DModel pWorld, int numberOfTriangles)
	{
		// We take our pWorld partition and then copy it into our member variable
		// face list, m_pWorld.  This holds the face indices that need to be rendered.
		// Since we are using vertex arrays, we can't use the CFace structure for the
		// indices, so we need to create an array that has all the face indices in a row.
		// This will be stored in our pIndices array, which is of type unsigned int.
		// Remember, it must be unsigned int for vertex arrays to register it.

		// Since we did not subdivide this node we want to set our flag to false
		m_bSubDivided = false;

		// Initialize the triangle count of this end node 
		TriangleCount = numberOfTriangles;

		// Create and init an instance of our model structure to store the face index information
		m_pWorld = new C3DModel();

		// Assign the number of objects to our face index list
		m_pWorld.numOfObjects = pWorld.numOfObjects;

		// Go through all of the objects in the partition that was passed in
		for(int i = 0; i < m_pWorld.numOfObjects; i++)
		{
			// Create a pointer to the current object
			C3DObject pObject = (C3DObject)pWorld.pObject.get(i);

			// Create and init a new object to hold the face index information
			C3DObject newObject = new C3DObject();

			// If this object has face information, add it's index to our object index list
			if(pObject.numOfFaces != 0)
				AddObjectIndexToList(i);

			// Add our new object to our face index list
			m_pWorld.pObject.add(newObject);

			// Store the number of faces in a local variable
			int numOfFaces = pObject.numOfFaces;

			// Assign the number of faces to this current face list
			((C3DObject)m_pWorld.pObject.get(i)).numOfFaces = numOfFaces;

			// Allocate memory for the face indices.  Remember, we also have faces indices
			// in a row, pIndices, which can be used to pass in for vertex arrays.  
			((C3DObject)m_pWorld.pObject.get(i)).pFaces = new CFace [numOfFaces];
			((C3DObject)m_pWorld.pObject.get(i)).pIndices = new int [numOfFaces * 3];

			// Copy the faces from the partition passed in to our end nodes face index list
			for (int j=0; j < numOfFaces; j++ )
			{
				((C3DObject)m_pWorld.pObject.get(i)).pFaces[j] = new CFace();
				((C3DObject)m_pWorld.pObject.get(i)).pFaces[j] = pObject.pFaces[j];
			}

			// Since we are using vertex arrays, we want to create a array with all of the
			// faces in a row.  That way we can pass it into glDrawElements().  We do this below.

			// Go through all the faces and assign them in a row to our pIndices array
			for(int j = 0; j < numOfFaces * 3; j += 3)
			{
				((C3DObject)m_pWorld.pObject.get(i)).pIndices[j]     = ((C3DObject)m_pWorld.pObject.get(i)).pFaces[j / 3].vertIndex[0];
				((C3DObject)m_pWorld.pObject.get(i)).pIndices[j + 1] = ((C3DObject)m_pWorld.pObject.get(i)).pFaces[j / 3].vertIndex[1];
				((C3DObject)m_pWorld.pObject.get(i)).pIndices[j + 2] = ((C3DObject)m_pWorld.pObject.get(i)).pFaces[j / 3].vertIndex[2];
			}

			// We can now free the pFaces list if we want since it isn't going to be used from here
			// on out.  If you do NOT want to use vertex arrays, don't free the pFaces, and get
			// rid of the loop up above to store the pIndices.

			//((C3DObject)m_pWorld.pObject.get(i)).pFaces = null;
		}

		// Assign the current display list ID to be the current end node count
		m_DisplayListID = EndNodeCount;

		// Increase the amount of end nodes created (Nodes with vertices stored)
		EndNodeCount++;
	}

	//////////////////////////////// DRAW OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function recurses through all the nodes and draws the end node's vertices
	/////
	//////////////////////////////// DRAW OCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void DrawOctree(GLFunc gl,GLUFunc glu, COctree pNode, C3DModel pRootWorld)
	{

		// To draw our octree, all that needs to be done is call our display list ID.
		// First we want to check if the current node is even in our frustum.  If it is,
		// we make sure that the node isn't subdivided.  We only can draw the end nodes.

		// Make sure a valid node was passed in, otherwise go back to the last node
		if(pNode == null) return;

		// Check if the current node is in our frustum
		if(!Frustum.CubeInFrustum(pNode.m_vCenter.x, pNode.m_vCenter.y, 
									pNode.m_vCenter.z, pNode.m_Width / 2) )
		{
			return;
		}

		// Check if this node is subdivided. If so, then we need to recurse and draw it's nodes
		if(pNode.IsSubDivided())
		{
			// Recurse to the bottom of these nodes and draw the end node's vertices
			// Like creating the octree, we need to recurse through each of the 8 nodes.
			DrawOctree(gl,glu,pNode.m_pOctreeNodes[TOP_LEFT_FRONT],		pRootWorld);
			DrawOctree(gl,glu,pNode.m_pOctreeNodes[TOP_LEFT_BACK],			pRootWorld);
			DrawOctree(gl,glu,pNode.m_pOctreeNodes[TOP_RIGHT_BACK],		pRootWorld);
			DrawOctree(gl,glu,pNode.m_pOctreeNodes[TOP_RIGHT_FRONT],		pRootWorld);
			DrawOctree(gl,glu,pNode.m_pOctreeNodes[BOTTOM_LEFT_FRONT],		pRootWorld);
			DrawOctree(gl,glu,pNode.m_pOctreeNodes[BOTTOM_LEFT_BACK],		pRootWorld);
			DrawOctree(gl,glu,pNode.m_pOctreeNodes[BOTTOM_RIGHT_BACK],		pRootWorld);
			DrawOctree(gl,glu,pNode.m_pOctreeNodes[BOTTOM_RIGHT_FRONT],	pRootWorld);
		}
		else
		{
			// Increase the amount of nodes in our viewing frustum (camera's view)
			TotalNodesDrawn++;


			// Make sure we have valid data assigned to this node
			if(pNode.m_pWorld == null) return;
				
			// Call the list with our end node's display list ID
			gl.glCallList(pNode.m_DisplayListID);

		}
		if( bDebug == true)
			Debug.RenderDebugLines(gl,glu);
	}

	//////////////////////////////// CREATE DISPLAY LIST \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This function recurses through all the nodes and creates a display list for them
	/////
	//////////////////////////////// CREATE DISPLAY LIST \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void CreateDisplayList(GLFunc gl,GLUFunc glu, COctree pNode, C3DModel pRootWorld, int displayListOffset)
	{
		// This function handles our rendering code in the beginning and assigns it all
		// to a display list.  This increases our rendering speed, as long as we don't flood
		// the pipeline with a TON of data.  Display lists can actually be to bloated or too small.
		// Like our DrawOctree() function, we need to find the end nodes by recursing down to them.
		// We only create a display list for the end nodes and ignore the rest.  The 
		// displayListOffset is used to add to the end nodes current display list ID, in case
		// we created some display lists before creating the octree.  Usually it is just 1 otherwise.

		// Make sure a valid node was passed in, otherwise go back to the last node
		if(pNode == null) return;

		// Check if this node is subdivided. If so, then we need to recurse down to it's nodes
		if(pNode.IsSubDivided())
		{
			// Recurse down to each one of the children until we reach the end nodes
			CreateDisplayList(gl,glu,pNode.m_pOctreeNodes[TOP_LEFT_FRONT],		pRootWorld, displayListOffset);
			CreateDisplayList(gl,glu,pNode.m_pOctreeNodes[TOP_LEFT_BACK],		pRootWorld, displayListOffset);
			CreateDisplayList(gl,glu,pNode.m_pOctreeNodes[TOP_RIGHT_BACK],		pRootWorld, displayListOffset);
			CreateDisplayList(gl,glu,pNode.m_pOctreeNodes[TOP_RIGHT_FRONT],	pRootWorld, displayListOffset);
			CreateDisplayList(gl,glu,pNode.m_pOctreeNodes[BOTTOM_LEFT_FRONT],	pRootWorld, displayListOffset);
			CreateDisplayList(gl,glu,pNode.m_pOctreeNodes[BOTTOM_LEFT_BACK],	pRootWorld, displayListOffset);
			CreateDisplayList(gl,glu,pNode.m_pOctreeNodes[BOTTOM_RIGHT_BACK],	pRootWorld, displayListOffset);
			CreateDisplayList(gl,glu,pNode.m_pOctreeNodes[BOTTOM_RIGHT_FRONT],	pRootWorld, displayListOffset);
		}
		else 
		{
			// Make sure we have valid data assigned to this node
			if(pNode.m_pWorld == null) return;

			// Add our display list offset to our current display list ID
			pNode.m_DisplayListID += displayListOffset;

			// Start the display list and assign it to the end nodes ID
			gl.glNewList(pNode.m_DisplayListID, gl.GL_COMPILE);

			// Create a temp counter for our while loop below to store the objects drawn
			int counter = 0;
			
			// Store the object count and material count in some local variables for optimization
			int objectCount = pNode.m_pObjectList.size();
			int materialCount = pRootWorld.pMaterials.size();

			// Go through all of the objects that are in our end node
			while(counter < objectCount)
			{
				// Get the first object index into our root world
				int i = ((Integer)pNode.m_pObjectList.get(counter)).intValue();

				// Store pointers to the current face list and the root object 
				// that holds all the data (verts, texture coordinates, normals, etc..)
				C3DObject pObject     = (C3DObject)pNode.m_pWorld.pObject.get(i);
				C3DObject pRootObject = (C3DObject)pRootWorld.pObject.get(i);

				// Check to see if this object has a texture map, if so, bind the texture to it.
				if(pRootObject.bHasTexture) 
				{
					// Turn on texture mapping and turn off color
					gl.glEnable(gl.GL_TEXTURE_2D);

					// Reset the color to normal again
					gl.glColor3f(1f, 1f, 1f);

					// Bind the texture map to the object by it's materialID
					gl.glBindTexture(gl.GL_TEXTURE_2D, Texture[pRootObject.materialID]);
				} 
				else 
				{
					// Turn off texture mapping and turn on color
					gl.glDisable(gl.GL_TEXTURE_2D);

					// Reset the color to normal again
					gl.glColor3f(1f, 1f, 1f);
				}

				// Check to see if there is a valid material assigned to this object
				if(materialCount > 0 && pRootObject.materialID >= 0) 
				{
					// Get and set the color that the object is, since it must not have a texture
					byte[] pColor = ((CMaterialInfo)pRootWorld.pMaterials.get(pRootObject.materialID)).color;

					// Assign the current color to this model
					gl.glColor3ubv(pColor);
				}

				// Now we get to the more unknown stuff, vertex arrays.  If you haven't
				// dealt with vertex arrays yet, let me give you a brief run down on them.
				// Instead of doing loops to go through and pass in each of the vertices
				// of a model, we can just pass in the array vertices, then an array of
				// indices that MUST be an unsigned int, which gives the indices into
				// the vertex array.  That means that we can send the vertices to the video
				// card with one call to glDrawElements().  There are a bunch of other
				// functions for vertex arrays that do different things, but I am just going
				// to mention this one.  Since texture coordinates, normals and colors are also
				// associated with vertices, we are able to point OpenGL to these arrays before
				// we draw the geometry.  It uses the same indices that we pass to glDrawElements()
				// for each of these arrays.  Below, we point OpenGL to our texture coordinates,
				// vertex and normal arrays.  This is done with calls to glTexCoordPointer(), 
				// glVertexPointer() and glNormalPointer().
				//
				// Before using any of these functions, we need to enable their states.  This is
				// done with glEnableClientState().  You just pass in the ID of the type of array 
				// you are wanting OpenGL to look for.  If you don't have data in those arrays,
				// the program will most likely crash.
				//
				// If you don't want to use vertex arrays, you can just render the world like normal.
				// That is why I saved the pFace information, as well as the pIndices info.  This
				// way you can use what ever method you are comfortable with.  I tried both, and
				// by FAR the vertex arrays are incredibly faster.  You decide :)

				// Make sure we have texture coordinates to render
				if(pRootObject.pTexVerts != null) 
				{
					// Turn on the texture coordinate state
					gl.glEnableClientState(gl.GL_TEXTURE_COORD_ARRAY);

					// Point OpenGL to our texture coordinate array.
					// We have them in a pair of 2, of type float and 0 bytes of stride between them.
					gl.glTexCoordPointer(2, gl.GL_FLOAT, 0, toFloatArray(pRootObject.pTexVerts));
				}

				// Make sure we have vertices to render
				if(pRootObject.pVerts != null)
				{
					// Turn on the vertex array state
					gl.glEnableClientState(gl.GL_VERTEX_ARRAY);

					// Point OpenGL to our vertex array.  We have our vertices stored in
					// 3 floats, with 0 stride between them in bytes.
					gl.glVertexPointer(3, gl.GL_FLOAT, 0, toFloatArray(pRootObject.pVerts));
				}

				// Make sure we have normals to render
				if(pRootObject.pNormals != null)
				{
					// Turn on the normals state
					gl.glEnableClientState(gl.GL_NORMAL_ARRAY);

					// Point OpenGL to our normals array.  We have our normals
					// stored as floats, with a stride of 0 between.
					gl.glNormalPointer(gl.GL_FLOAT, 0, toFloatArray(pRootObject.pNormals));
				}

				// Here we pass in the indices that need to be rendered.  We want to
				// render them in triangles, with numOfFaces * 3 for indice count,
				// and the indices are of type UINT (important).
				gl.glDrawElements(gl.GL_TRIANGLES,    pObject.numOfFaces * 3, 
							   gl.GL_UNSIGNED_INT, pObject.pIndices);

				// Increase the current object count rendered
				counter++;
			}

			// End the display list for this ID
			gl.glEndList();
		}
	}
	
	//////////////////////////////// INTERSECTSPHEREWITHOCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	///// Check to see if a sphere intersects with a polygon in the Octree.
	/////
	/////	[in]	pNode			The Octree Node to check.
	/////	[in]	spehere			The Line to check intersection for.
	/////	[in]	vIntersectionPt	The Point at which the line intersected.
	/////	[return]	Wheter there was an intersection or not.
	/////
	//////////////////////////////// INTERSECTSPHEREWITHOCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public boolean IntersectSphereWithOctree( COctree pNode, C3DModel pWorld, CBoundingSphere Sphere )
	{
		// If the passed in node is invalid, leave.
		if ( pNode == null ) return false;
	
		// If this node is subdivided, traverse to it's children.
		if ( pNode.IsSubDivided() )
		{
			// Lots of Logic Tests, but with a purpose. If ANY node comes back saying there was a collision in it or one
			// of it's sub-nodes, return immediately without checking anymore nodes. This echos back recursivly to the root.
			if ( IntersectSphereWithOctree( pNode.m_pOctreeNodes[TOP_LEFT_FRONT],    pWorld, Sphere )) return true; 
			if ( IntersectSphereWithOctree( pNode.m_pOctreeNodes[TOP_LEFT_BACK],     pWorld, Sphere )) return true; 
			if ( IntersectSphereWithOctree( pNode.m_pOctreeNodes[TOP_RIGHT_BACK],    pWorld, Sphere )) return true; 
			if ( IntersectSphereWithOctree( pNode.m_pOctreeNodes[TOP_RIGHT_FRONT],   pWorld, Sphere )) return true; 
			if ( IntersectSphereWithOctree( pNode.m_pOctreeNodes[BOTTOM_LEFT_FRONT], pWorld, Sphere )) return true; 
			if ( IntersectSphereWithOctree( pNode.m_pOctreeNodes[BOTTOM_LEFT_BACK],  pWorld, Sphere )) return true; 
			if ( IntersectSphereWithOctree( pNode.m_pOctreeNodes[BOTTOM_RIGHT_BACK], pWorld, Sphere )) return true;
			if ( IntersectSphereWithOctree( pNode.m_pOctreeNodes[BOTTOM_RIGHT_FRONT],pWorld, Sphere )) return true; 
		}
		else
		{
			// Make sure there is a world to test.
			if ( pNode.m_pWorld == null) return false;
	
			CVector3 vTempFace[] = new CVector3[3];
	
			// Check all of this Nodes World Objects.
			for ( int i = 0; i < pNode.m_pWorld.numOfObjects; i++ )
			{
				C3DObject pObject = (C3DObject)pNode.m_pWorld.pObject.get(i);
	
				// Check all of the Worlds Faces.
				for ( int j = 0; j < pObject.numOfFaces; j++ )
				{
					// Look at the 3 Vertices of this Face.
					for ( int k = 0; k < 3; k++ )
					{
						// Get the Vertex Index;
						int iIndex = pObject.pFaces[j].vertIndex[k];
	
						// Now look in the Root World and just get the Vertices we need.
						vTempFace[k] = ((C3DObject)pWorld.pObject.get(i)).pVerts[iIndex];
					}
					if (Sphere.CheckSphereCollision( vTempFace, 3 )) return true;
				}
			}
		}
	// Se siamo arrivati fin qui niente collisione
	return false;
	}
	
	//////////////////////////////// INTERSECTLINEWITHOCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	///// Check to see if a line intersects with a polygon in the Octree.
	/////
	/////	[in]	pNode			The Octree Node to check.
	/////	[in]	vLine			The Line to check intersection for.
	/////	[in]	vIntersectionPt	The Point at which the line intersected.
	/////	[return]		Wheter there was an intersection or not.
	/////
	//////////////////////////////// INTERSECTLINEWITHOCTREE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public boolean IntersectLineWithOctree( COctree pNode, C3DModel pWorld, CClampingLine vLine )
	{
		// If the passed in node is invalid, leave.
		if ( pNode == null ) return false;
	
		// If this node is subdivided, traverse to it's children.
		if ( pNode.IsSubDivided() )
		{
			// Lots of Logic Tests, but with a purpose. If ANY node comes back saying there was a collision in it or one
			// of it's sub-nodes, return immediately without checking anymore nodes. This echos back recursivly to the root.
			if ( IntersectLineWithOctree( pNode.m_pOctreeNodes[TOP_LEFT_FRONT],    pWorld, vLine )) return true; 
			if ( IntersectLineWithOctree( pNode.m_pOctreeNodes[TOP_LEFT_BACK],     pWorld, vLine )) return true; 
			if ( IntersectLineWithOctree( pNode.m_pOctreeNodes[TOP_RIGHT_BACK],    pWorld, vLine )) return true; 
			if ( IntersectLineWithOctree( pNode.m_pOctreeNodes[TOP_RIGHT_FRONT],   pWorld, vLine )) return true; 
			if ( IntersectLineWithOctree( pNode.m_pOctreeNodes[BOTTOM_LEFT_FRONT], pWorld, vLine )) return true; 
			if ( IntersectLineWithOctree( pNode.m_pOctreeNodes[BOTTOM_LEFT_BACK],  pWorld, vLine )) return true; 
			if ( IntersectLineWithOctree( pNode.m_pOctreeNodes[BOTTOM_RIGHT_BACK], pWorld, vLine )) return true; 
			if ( IntersectLineWithOctree( pNode.m_pOctreeNodes[BOTTOM_RIGHT_FRONT],pWorld, vLine )) return true; 
		}
		else
		{
			// Make sure there is a world to test.
			if ( pNode.m_pWorld == null) return false;
	
			CVector3 vTempFace[] = new CVector3[3];
	
			// Check all of this Nodes World Objects.
			for ( int i = 0; i < pNode.m_pWorld.numOfObjects; i++ )
			{
				C3DObject pObject = (C3DObject)pNode.m_pWorld.pObject.get(i);
	
				// Check all of the Worlds Faces.
				for ( int j = 0; j < pObject.numOfFaces; j++ )
				{
					// Look at the 3 Vertices of this Face.
					for ( int k = 0; k < 3; k++ )
					{
						// Get the Vertex Index;
						int iIndex = pObject.pFaces[j].vertIndex[k];
	
						// Now look in the Root World and just get the Vertices we need.
						vTempFace[k] = ((C3DObject)pWorld.pObject.get(i)).pVerts[iIndex];
					}
					if ( vLine.CheckLineCollision( vTempFace, 3 )) return true;
				}
			}
		}
	// Se siamo arrivati fin qui niente collisione
	return false;
	}
	
	//////////////////////////////// DOCLEANUP \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzioni per pulier opengl
	/////
	//////////////////////////////// DOCLEANUP \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	public void doCleanup(GLFunc gl,GLUFunc glu)
	{
		gl.glDeleteTextures(Texture.length,Texture);
		// le lists di opengl bisogna cancellarle manualmente
		gl.glDeleteLists(GetDisplayListID(), EndNodeCount);
	}

	//////////////////////////////// TOFLOATARRAY \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	Funzioni per convertire dei float in un array
	/////
	//////////////////////////////// TOFLOATARRAY \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	float[] toFloatArray(CVector2[] v)
	{
		float[] result = new float[v.length * 2];
		for (int i=0; i < v.length ;i++ )
		{
			result[i*2 + 0] = v[i].x;
			result[i*2 + 1] = v[i].y;
		}
		return result;
	}

	float[] toFloatArray(CVector3[] v)
	{
		float[] result = new float[v.length * 3];
		for (int i=0; i < v.length ;i++ )
		{
			result[i*3 + 0] = v[i].x;
			result[i*3 + 1] = v[i].y;
			result[i*3 + 2] = v[i].z;
		}
		return result;
	}
}
