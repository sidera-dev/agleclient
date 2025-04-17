############################### AGLE  ##################################
This is a very old project, uses the old Gl4Java library

Agle means ANOTHER GAME LIBRARY and ENVIRONMENT
I've developed it for learning purpose, I wanted an
easy to use and comprensible game library with also online futures.
Agle has a client and a server, you don't need to run the server to run
the current client example only install gl4java library
This library is under GPL license and feel free to use it just
remeber me in your credits.

########################### AGLE CLIENT ###############################

it's in Java and implements the basics to run a game:
-Load 3ds models for level
-Use octree and frustum culling for rendering and collisions
-Load quake 3 model fol player, and npc
-Can add a Skybox to our level
-Has font rendering 
-Manage basic collision level-player level-npc level-mob player-npc player-obj player-exit npc-obj
-Has particle support
-Connection support (very basic implementation, tcp only)
-Cureent agle client example it's so structured
 CMain --> CCanvas (extends CTimedCanvas) --> CZone +-> CPlayer (Extends CLivingform wich extends CForm wich extends CMass) 
 							 						+-> CMob 	 (Extends CLivingform)
 													+-> CObj  	 (Extends CUnlivingform)
 							 						+-> CExit 
 													+-> etc.							 
-The client current example load a level from world/enter/portal/Portal.txt as specificated in CCanvas.java 
-Portal.txt is a text file in wich you can add and edit all the things of a Zone, npc,objects,graviti,lights,particles etc.
 CZone.java contains it's definition.

How RUN agle client :
1> Use the script in gl4java directory or the bin package and install
	gl4java libray on your pc
2> Double click or command line start.bat (start.sh in linux) in agle/ directory

How COMPILE agle client:
1> Install JDK 
2> Set or create JAVA_HOME to the JDK path into build.bat under build/
3> Download and install ANT 
4> Set or create ANT_HOME to the ant path in build.bat (build.sh in linux) under build/
5> Set PATH to the  Ant\bin path in  build.bat (build.sh in linux) under build/
6> Set or create the path Ant\lib in build.bat (build.sh in linux) under build/
7> Use the java script in gl4java directory or download it yourself to install
	gl4java libray on your pc	
8> Than double click or command line build.bat (build.sh in linux) in agle/build directory
