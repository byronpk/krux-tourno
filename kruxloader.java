/**
 *	Krux(c) Tourno Series 3 Release X2
 *	Copyright(c) 2010 MicroTech Technologies Inc. All Rights Reserved.
 *	Copyright(c) 2010 Radioactive Reds Animation
 *    Copyright(c) 2017 Micron Information Services
 * 
 *    EULAID: DEC91C.KRX_REM_SYS.140810-KRUX3RTSX
 *    ============================================================================
 *    Version Identification
 *    ----------------------------------------------------------------------------
 *    Krux Tourno
 *    Version:             3.12.xxxx
 *    Support Version:     3.12.2225 BETA
 *    Project "DIAMOND DOG"
 *    Build Number 2225
 */
  
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
 //   import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import javax.sound.sampled.*;
import javax.imageio.*;

/* MicroTech Propietary Class(es) */
import xwin.kernel.directsound.*;

/* Bugs found
* =========================================================================================================
*     -Infinite Invisibility/Ghost Bug
*/

/* Intended upgrade to this version (Krux Series 3 Revision X2)
* =========================================================================================================
*	-Automated Installer (*)
*	-Custom Controls on the LoadGame screen (*)
*	-Color Choosing
* 	-New Soundset (*)
*	-Multiple Same Type Items
*     -Add painkiller item
*     -Allow for remapping of keys
*     -Weapon reloading
*     -Add additional map-types
*     -EXPERIMENTAL: Add joystick support (ticked off, not supported in Java)
*     -Restructure in-game calculations for better performance
*     -Transparent NullTile (Completed!) 
*     -Custom Theme Support
*/

public class kruxloader implements Runnable {
 // == Variables ==

 /**
 * Runnable interface for Krux(c) used for implementation of secondary redrawing
 * Note all SHARED variables are marked as public
 */
 
//    MICRON SYSTEM TEAM CONSTRANTS
      protected static final int MAIN_PLAYER = 1;
      protected static final int MAIN_ENEMY = 2;

      protected static final int HEALTH = 1;
      protected static final int EXPERIENCE = 2;
      protected static final int ONE_UP = 3;
      protected static final int WEAPON = 4;
      protected static final int MEGA_HEALTH = 5;
      protected static final int MEGA_EXPERIENCE = 6;
      protected static final int GHOST_POTION = 7;
      protected static final int LAMP = 8;
      protected static final int ARMOR = 9;
      protected static final int UNOSEE_POTION = 10;
      protected static final int PAINKILLER = 11;

//    Krux 4.0 ALPHA Constants
      protected static final int BOUND_SOLID    = 0;
      protected static final int BOUND_SHIFT    = 1;
      protected static final int BOUND_TRACK    = 2;

//    System Constants
      protected static final String PRODUCT     = "Krux Tourno";
      protected static final String VERSION     = "3.12";
      protected static final String BUILD       = "2225";
      protected static final String RELEASE     = "3.12." + BUILD + "(BETA)";
      protected static final String REVISION 	= "KMF Series 3 (1.3.0.048)";
      protected static final String DATE        = new java.util.Date().toString();
      protected static final String buildstring = "Build " + BUILD;
      protected static final String REVIEW      = "";
	
//    Scoreboard
      protected Scoreboard scrboard;
      protected boolean showNominal = false;
   
	// Team Deathmatch Variable
   protected boolean DEATHMATCHMODE = false;		// Will revert to false after BETA
   protected boolean FRIENDLYFIRE   = true;		// Will be added to New Game section
   protected boolean lastWasRed	= false;		// Manages the last player to be generated
	
	// Registration Variables
   protected final regBase4 rB =				// This supplies Krux's unique product ID
   			new regBase4(111821); 			// This key is locked and unique
   protected String prodcode = "";				// The product key buffer
   protected boolean isRegist = false;			// Defines registration
   
	// Adventure Maze Mode
   private int mapnumber = 0;
	
	// Screenshot Buffer
   private BufferedImage outputImage = new BufferedImage(630, 447, BufferedImage.TYPE_INT_ARGB);

   // System Variables
   protected PrintWriter printErr;                    // The error output stream
   protected PrintWriter printDebug;                  // The debug output stream
   protected final JFrame notneeded = new JFrame();   // A simingly vestigial JFrame
   protected JFrame mainwin = null;                   // The main krux window
   protected JFrame nominal = null;				// The nominal rates screen
   protected Thread controlThread = null;             // The main thread for krux
   protected JDialog startdiag = new JDialog();	      // The Game Generator Dialog
   protected File tempo;                              // The temporary file link needed for sound
   protected byte[] soundBuffer = new byte[24576];    // 24 kB Sound Buffer
   protected int revive = 0; 					// Amount of times player has revived
   protected final int levelMax = 100;                // Game level maximum
   
   // Map Constraints
   protected Point mapsize = new Point(12,12); 		// The mapsize variable
   protected String mapdata[][] =                     // The buffer representing the layout of the map
      new String[mapsize.x][mapsize.y];
   protected int maxbounds = 32;                      // The boundary limit variable
   protected Point spawnPoint1 = new Point(0,0);      // Spawnpoint for Player
   protected Point spawnPoint2 =                      // Spawnpoint for Enemy
      new Point(mapsize.x - 1, mapsize.y - 1);
   protected Point kohlocat =                         // Location of the K.O.T.H flag
      new Point(-1, -1);
   protected String mapName = "unknown map";          // Map Name
   protected boolean loadedmap = false;               // True if external map is loaded
   protected boolean kohmode = false;                 // True if King of the Hill mode is on
   protected boolean isGameOver = false;              // True if Game Over
   protected int flagowner = 0;                       // The player that owns the kohflag
   protected Point[] trackingbounds =                 // The tracking boundary buffer
      new Point[maxbounds / 2];
   protected int countTrackers = 0;                   // Tracking Boundary Counter
   protected int trackingBoundsMax = 5;               // Trackbounds
	
	// Level Style Themes
   boolean genCave = false;
   protected String[] themeNames = new String[] {
      "General",
      "Stone",
      "Ice",
      "Grass",
      "Metal"
      };
  
  /**
   *  Notes on the "Line-of-Sight" engine
   *  ===================================
   *  In order to improve the realism of the game a primitive line-of-sight engine was added to
   *  Krux 3 RTS X. The engine imposes a realistic limitation on the maximum distance a enemy must
   *  be from something in order to "see" it.
   *
   *  The standard distance for this is 7 tiles around the player which decreases to 4 tiles around
   *  when Night Mode(tm)is activated (Since with NightMode(tm) the blue player cannot see further
   *  than 4 tiles around either)
   *
   *  This engine also includes a "search" mode. With this search mode all non-blue players will
   *  actively scout the map in order to find items or targets.
   */
   protected int enemyLineOfSight =  7;               // Determines the enemies line-of-sight
        
   /**
   *  Vector variable "aiEnemies" contains all instances of the OBERON_AIEnemy Class used to implement
   *  intelligent "bots". 
   */
   protected final Vector<Oberon_AIDrivenEnemy> aiEnemies = new Vector<Oberon_AIDrivenEnemy>();
   
   // Game Variables
   protected boolean soundOn = true;                  // Specifies the default sound status
   protected boolean isPause = false;                 // Pause or no pause
   protected boolean debugMode = false;               // Debugging and Developer mode switch
   protected boolean nightMode = false;               // Specifies Night Mode
   protected Point scrollOffset                       // The scrollOffset variable is what allows the background to scroll as required
      = new Point (254, 164);
   protected int LampRemain = 0;                      // Lamp counter
   protected boolean hasLamp         = false;         // Lamp conditional
   protected boolean extremeRules    = false;         // Determines if EXTREME RULES COUNT
   protected boolean botLimitImposed = false;         // Determines if the in-game bot limit is set
   protected int 		botLimit      	 = 20;            // In-game bot limit (this variable is currently final)
            
   // Highscore Listing Variables
   protected String[] highScoreList = new String[5];  // The current list of HighScores
   protected boolean showHighScores = false;          // Display the window or not?
   protected int showHSTime = 0;                      // As of yet unused
   protected int scorePosi = 0;                       // Your position in the HighScore list
   
   /* 
   *  Floortile Variables
   * ==========================================================================
   *  These variables (floorsx) contain the fixed points of the custom floor
   *  designs used in KMF2 and KMF3 map schemes
   */
   protected Point[] floorsV = new Point[mapsize.x * mapsize.y];
   protected Point[] floors1 = new Point[mapsize.x * mapsize.y];
   protected Point[] floors2 = new Point[mapsize.x * mapsize.y];
   protected Point[] floors3 = new Point[mapsize.x * mapsize.y];
   protected Point[] floors4 = new Point[mapsize.x * mapsize.y];
   protected Point[] floors5 = new Point[mapsize.x * mapsize.y];
   
   // Floortile Counters (acts as a primitive array size counter backup method)
   protected int floorsVcnt = 0;
   protected int floors1cnt = 0;
   protected int floors2cnt = 0;
   protected int floors3cnt = 0;
   protected int floors4cnt = 0;
   protected int floors5cnt = 0;
   
   // Gem Hunter Mode Variables
   protected boolean gemHunterMode = false;        // Reminder: Change to FALSE after BETA TEST
   protected Point gemLocat = new Point( -1, -1 ); // Default Location buffer for the gem 
   protected short gemType = -1;                     // Defines the type of gem on-screen
   
   /**
   * Possible values for "gemType"
   * ==================================
   * (All values are 32-bit integers)
   *  0  - Blue Gem (1 gem value)
   *  1  - Red Gem (2 gem value)
   *  2  - Green Gem (5 gem value)
   *  3  - Cyan Gem (10 gem value)
   *  4  - Yellow Gem (20 gem value)
   */
   
   // Message and Damage Painting Defaults
   protected int damageE = 0;                      // The damage value to paint
   protected int damageP = 0;                      // see above value
   protected int redrawsP = 0;                     // Redraw timer
   protected int redrawsE = 0;                     // Redraw timer
   protected boolean damagePlayerPaint = false;    // Sets if the damage should be painted
   protected boolean damageEnemyPaint = false;     // Sets if the damage should be painted
   protected int redrawAUX = 0;                    // Redraw timer
   protected boolean hasAUX = false;               // Sets if the message should be painted
   protected String AUX = "";                      // Message string
         
   // Full-Screen Exclusize Mode Variables
   protected GraphicsDevice device =					// The primary Graphics Device
   	GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
   protected DisplayMode modalForm =					// The define the current screen resolution
   	device.getDisplayMode();
   protected GraphicsEnvironment h =               // The main Graphics Environment
    	 GraphicsEnvironment.getLocalGraphicsEnvironment();
   
   // AI Bias and Difficulty variables
   protected int[] movesBias = new int[12];        // Moves that are made in advance
   protected int moves = 0;                        // A Move counter
   protected int maxMoves = 4;                     // The Move limit for the OBERON AI
   protected int movetemp = 0;                     // a Temp field
   protected int recalls = 0;                      // Debugging Field
   protected int difficulty = 2;                   // Game difficulty
   
   // Chatbox Variables
   protected boolean enteringHS = false;           // Boolean for HighScore Initials
   protected String scoreInitial = "";             // Buffer used for entering HighScore names
   protected boolean enteringPredef = false;       // Boolean for Predef
   protected String predefKey = "";                // Buffer used for entering Predef Enemy Keys
   protected boolean enteringCheat = false;        // Chat Windows conditional
   protected String CHEAT = "";                    // The current contents of the chatwindow
   protected boolean cheatsOn = false;             // Cheat conditional
   protected boolean cheatsUsed = false;             // Cheat conditional
         
   // Status Variables
   /**
   *  Status variables for the BLUE PLAYER (PLAYER 1)
   * ===========================================================================================
   *  Variables relating to the status of players
   * (Note: Variables used may differ for non-red enemies)
   * ===========================================================================================
   *  Name              Type           Use
   * -------------------------------------------------------------------------------------------
   *  isGhostx          boolean        Determines if the player can pass through barriers
   *  u_no_seex         boolean        Determines if the player is visible
   *  hasMegaHP(_E)     boolean        Determines if the player has an active Elixir of Life
   *  MegaHPRemain(_E)  signed int     Determines the remaining life in the active Elixir of Life
   *  hasMegaExp(_E)    boolean        Determines if the player has an active Vial of Wisdom
   *  MegaExpRemain(_E) signed int     Determines the remaining exp in the active Vial of Wisdom
   *  isPoisonedx       boolean        Determines if the target player is poisoned
   *  poisonLevelx      signed int     Determines the level of poison the player has
   *  isZappedx         boolean        Determines if the target player is crippled
   *  zaptimex          signed int     Defines the remaining time for which the player is crippled
   *  expForNext(_E)    signed int     Defines the amount exp needed to reach the next level
   *  expLast(_E)       signed int     Defines the amount exp needed for the last level
   *  exp(_E)           signed int     Defines the current total exp
   *  strengthPx        signed int     Defines to strength of the target player
   *  endurancePx       signed int     Defines to endurance of the targer player
	*  painKillerPx		boolean			Defines if the painkiller item is active
	*	painKillerRemPx	signed int		Defines if the remainder of the painkiller item
	*	strengthIVPx		signed int		Helps determine the strength rating
	*	enduranceIVPx		signed int		Helps determine the endurance rating
	*	lifeIVPx				signed int		Helps determine the life rating
	*	strengthEVPx		signed int		Helps determine the strength rating
	*	enduranceEVPx		signed int		Helps determine the endurance rating
	*	lifeEVPx				signed int		Helps determine the life rating
   *  gemsPx            signed int     Defines the amount of gem value the player has accumalated
   *  armorPx           signed int     Defines the armor applied to this player
   *  maximum(P/E)      signed int     Defines the maximum Life Value for this player
   *  lifexxx           signed int   	A generic life holder variable
   *  levelxxx          signed int   	A generic level holder variable
   *  eyes(P/E)         signed int     Defines the facing direction of the player's eyes
   *  locationxxxX      signed int     Contains the player's current location X variable
   *  locationxxxY      signed int     Contains the player's current location Y variable
   *  xxxLevel          signed int     Keeps track of this player's level
   *  xxxWeapon         signed int     Defines the current weapon used by this player
   *  (p/e)WeapUses     signed int     Defines the maximum ammunition for this weapon
   *  (p/e)WeapLeft     signed int     Defines the remaining ammunition for this weapon
   *  restrict(E)       boolean        Defines if the player may move or not
   */
   protected boolean isGhost1 = false;
   protected boolean u_no_see1 = false;           
   protected boolean hasMegaHP = false;               
   protected int MegaHPRemain = 0;                    
   protected boolean hasMegaExp = false;              
   protected int MegaExpRemain = 0;                   
   protected boolean isPoisoned1 = false;             
   protected int poisonLevel1 = 0;                   
   protected boolean isZapped1 = false;               
   protected int zaptimer1 = 0;                     
   protected int expForNext = 60 + (int) (Math.random() * 120);
   protected int expLast = 0;                         
   protected int exp = 0;  
   protected int playerLevel = 2;               
	            
   protected int strengthIVP1 = (int)(Math.random() * 16);
   protected int enduranceIVP1 = (int)(Math.random() * 16);
   protected int lifeIVP1 = (int)(Math.random() * 16);
	
   protected int strengthEVP1 = 0;
   protected int enduranceEVP1 = 0;
   protected int lifeEVP1 = 0;
   
   protected int strengthBP1 = ((int)(Math.random() * 5) * 10) + 60;
   protected int enduranceBP1 = ((int)(Math.random() * 5) * 10) + 60;
   
   protected int strengthP1 = CalculateStat(strengthIVP1, strengthBP1, strengthEVP1, playerLevel);
   protected int enduranceP1 = CalculateStat(enduranceIVP1, enduranceBP1, enduranceEVP1, playerLevel);
	
   protected boolean painKillerP1 = false;
   protected int painKillerRemP1 = 0;
   protected int pkcount1 = 0;

   protected int gemsP1 = 0;
   protected int armorP1 = 0;
   protected int maxHealthP = CalculateHP(lifeIVP1, lifeEVP1, playerLevel); 
   protected int curHealthP = maxHealthP;
   protected int levelPlayer = 0;
   protected int eyesP = 0; 
   protected int locationPlayerX = spawnPoint1.x;
   protected int locationPlayerY = spawnPoint1.y;
   protected int playerWeapon = -1;
   protected int pWeapUses = 1;
   protected int pWeapLeft = 0;
   protected boolean restrict = false;


   /**
   *  Blue Player specific variables
   * ===========================================================================================
   *  Name              Type           Use
   * -------------------------------------------------------------------------------------------
   *  senseDist         signed int     Defines the maximum distance for the Radar Sense(tm) tool
   *  sense             boolean        Defines if the Radar Sense tool is in use
   *  statusBoxDrawn    boolean        Defines if the Player Status box is drawn
   *  score             signed int     Defines the Blue Player's current score
   */
   protected int senseDist = 5;
   protected boolean sense = false;
   protected boolean statusBoxDrawn = false;
   protected int score = 0;
   
   /**
   *  Status variables for the RED PLAYER (PLAYER 2)
   */
   protected boolean isGhost2 = false;                // Is the red player a ghost?
   protected boolean u_no_see2 = false;
   protected boolean hasMegaHP_E = false;             // Do I have an active elixir?
   protected int MegaHPRemain_E = 0;                  // How much is left?
   protected boolean hasMegaExp_E = false;            // Do I have an active vial?
   protected int MegaExpRemain_E = 0;                 // How much is left?
   protected boolean isPoisoned2 = false;
   protected int poisonLevel2 = 0;
   protected boolean isZapped2 = false;
   protected int zaptimer2 = 0;
   protected int expForNext_E =
      60 + (int) (Math.random() * 120);
   protected int expLast_E = 0;                       // Last Requirement
   protected int exp_E = 0;                           // Current Experience
   protected boolean expDrawn = false;
   protected int expTime = 0;
   protected boolean hpDrawn = false;
   protected int hpTime = 0;
   protected int enemyLevel = 2; 

   protected int strengthIVP2 = (int)(Math.random() * 16);
   protected int enduranceIVP2 = (int)(Math.random() * 16);
   protected int lifeIVP2 = (int)(Math.random() * 16);
	
   protected int strengthEVP2 = 0;
   protected int enduranceEVP2 = 0;
   protected int lifeEVP2 = 0;
   
   protected int strengthBP2 = ((int)(Math.random() * 5) * 10) + 60;
   protected int enduranceBP2 = ((int)(Math.random() * 5) * 10) + 60;
   
   protected int strengthP2 = CalculateStat(strengthIVP2, strengthBP2, strengthEVP2, enemyLevel);
   protected int enduranceP2 = CalculateStat(enduranceIVP2, enduranceBP2, enduranceEVP2, enemyLevel);
	
   protected boolean painKillerP2 = false;
   protected int painKillerRemP2 = 0;
   protected int pkcount2 = 0;
	
   protected Point searchLocation = new Point( -1, -1 );
   protected int gemsP2 = 0;
   protected int armorP2 = 0;
   protected int maxHealthE = CalculateHP(lifeIVP2, lifeEVP2, enemyLevel);
   protected int levelEnemy = 0; 
   protected int curHealthE = maxHealthE; 
   protected int eyesE = 0;
   protected int locationEnemyX = spawnPoint2.x;
   protected int locationEnemyY = spawnPoint2.y;
   protected int enemyWeapon = -1;
   protected int eWeapUses = 1;
   protected int eWeapLeft = 0;
   protected boolean restrictE = false;

   /**
   *  Non-Blue Player specific variables
   * ===========================================================================================
   *  Name              Type           Use
   * -------------------------------------------------------------------------------------------
   *  enemyTarget       String         Defines the current AI target for the OBERON AI Engine
	*	u_no_see_timer		int				Defines the time for the invisibility potion
	*	ghost_timer			int				Defines the time for the ghosting potion
   */
   protected String enemyTarget = "player";
   protected int u_no_see_timer = 0;
   protected int ghost_timer = 0;
   
	// Map Boundary Defaults
   protected int makebou = 10; 								// Amount of static boundaries (10 default)
   protected int makembou = 5; 								// Amount of shifting boundaries (5 default)
   protected int maketbou = 0; 								// Amount of shifting boundaries (5 default)
   
	// Map Drawing Range
   int lxlimit = Math.max(locationPlayerX - 11, 0);
   int lylimit = Math.max(locationPlayerY - 7, 0);
   int uxlimit = Math.min(locationPlayerX + 12, mapsize.x);
   int uylimit = Math.min(locationPlayerY + 8, mapsize.y);
   
   // Box Constaints
   protected int hpboxtype = 0;                       // 0 defines regular box, 1 defines a crate
   protected int painKillType = 0;							// 0 defines a small painkiller, 1 defines a large one
   
	// Health Ratio
   protected int healthRatio = 100; 						// Persentage of maximum health each player has
   
	// Score Management
   protected int hiscore = 0; 								// The game's current hiscore
   protected String hiscorename = "Last High-Score"; 	// The game's current hiscore name
   
	// Player reviving variables
   protected boolean revivesUnlimited = true; 		   // If true, revives are unlimited
   protected byte revivelimit = 0; 						   // Limit to amount of revives allowed
   protected boolean isDead = false;                  // Defines if the you are dead
   
	// The main play area grid
   protected JPanel grid;
   
	// Menu Bar Text Fields
   protected JTextField kohtimer = new JTextField("0");  // The K.O.T.H Timer field
   
	// Locator Arrays for boundaries
   protected Point[] bounds;  								// Holds static boundary positions
   protected Point[] mobileBounds; 							// Holds shifting boundary positions
   protected char[]  mBoundClass;
    
	// Location Points for boxes
   /**
   *  Default itembox and object drawing points
   * =======================================================================
   *  Name                 Object
   * -----------------------------------------------------------------------
   *  healthLocat          Locator for Health Box and Health Crate items
   *  levelboxLocat        Locator for Level Box items
   *  extraLifeLocat       Locator for Additional Lives for Blue Player items
   *  weaponboxLocat       Locator for Weapon Crates
   *  megahealthLocat      Locator for Elixir of Life items
   *  megaexpLocat         Locator for Vial of Wisdom items
   *  ghostpotionLocat     Locator for Ghosting Potion items
   *  lampLocat            Locator for Lamp items
   *  armorLocat           Locator for Armor items
   *  unoseeLocat          Locator for "U-No-See" Potion
	*	painkLocat				Locator for "Painkiller"
   */
   protected Point healthLocat = new Point(mapsize.x * 2, mapsize.y * 2);
   protected Point levelboxLocat = new Point(mapsize.x * 2, mapsize.y * 2);
   protected Point extraLifeLocat = new Point(mapsize.x * 2, mapsize.y * 2);
   protected Point weaponboxLocat = new Point(mapsize.x * 2, mapsize.y * 2);
   protected Point megahealthLocat = new Point(mapsize.x * 2, mapsize.y * 2);
   protected Point megaexpLocat = new Point(mapsize.x * 2, mapsize.y * 2);
   protected Point ghostpotionLocat = new Point(mapsize.x * 2, mapsize.y * 2);
   protected Point lampLocat = new Point (mapsize.x * 2, mapsize.y * 2);
   protected Point armorLocat = new Point (mapsize.x * 2, mapsize.y * 2);
   protected Point unoseeLocat = new Point (mapsize.x * 2, mapsize.y * 2);
   protected Point painkLocat = new Point (mapsize.x * 2, mapsize.y * 2);
	
	// Default off-screen point
   public 	 Point OFFSCREEN = new Point (mapsize.x * 2, mapsize.y * 2); 
	
	// Other variables and constants
   protected long gametimer = 0;						// The amount of ticks the game was online
   protected long lastkill = 0;						// The time you last killed someone
   protected byte frenzytime = 30;					// The time between kills to get the frenzy bonus
   protected byte frenzymult = 1;					// The frenzy multiplier
   protected int kills = 0;							// The amount of kills
   protected int items = 0;							// The amount of items collected
   protected int lastkills = 0;						// The last amount of kills
	
   protected int count = 0; 							// static boundaries buffer 
   protected int gamespeed = 150; 					// Game speed (in milliseconds per refresh)
   protected int countM = 0; 							// shifting boundaries buffer
   protected int healthlust = 25; 					// How desperately will the enemy be searching for health crates?
   protected boolean allowThinking = true; 		// If true enemies will think thier moves through
   protected boolean violence = false; 			// Sets weather the Third Guy is violent or not
   protected JPanel lifePanelPlayer;				// Player Life Panel
   protected JLabel label1;							// A Label
   protected JLabel label2;							// A Label
   protected JLabel label4;							// A Label
   protected boolean hasGenerated = false;	   // Keeps track of weather or not the user has generated a map or not
   protected boolean fullScreen = false;        // Defines if the game is played in full-screen mode
   
   /*
   *  CGString Variables
   * ========================================================================
   *  This variables serve as containers for the native fontset strings used
   *  by the Krux 3 game.
   *
   *  Name              Contains
   * ------------------------------------------------------------------------
   *  hiScoreStr        Current High Score
   *  scoreStr          The Blue Player's current score
   *  timerStr          The Generic Timer values for Gem Hunter and C.T.F games
   *  cheatStr          The Generic Message area strings
   *  spawnStr          Warns the player to "Press R to Respawn"
   *  goStr             Displays "Game Over"
   *  gems(1/2)         Gems collected by players
   *  hs(0-5)           The current highscore values
   */
   protected CGString hiScoreStr    = new CGString("0", 12);
   protected CGString scoreStr	= new CGString("0", 8, CGString.ALIGN_RIGHT, CGString.DIGITAL);
   protected CGString timerStr	= new CGString("0", 4, CGString.ALIGN_RIGHT, CGString.DIGITAL);
   protected CGString cheatStr 	= new CGString("0", 64);
   protected CGString spawnStr 	= new CGString("PRESS R TO RESPAWN", 18);
   protected CGString goStr 		= new CGString("GAME OVER", 9);
   
   protected CGString gems1		= new CGString("0", 3, CGString.ALIGN_RIGHT, CGString.DIGITAL);
   protected CGString gems2		= new CGString("0", 3, CGString.ALIGN_RIGHT, CGString.DIGITAL);
   
   protected CGString hs0 = new CGString("HIGH SCORES", 11);
   protected CGString hs1 = new CGString("KRX 10000", 20);
   protected CGString hs2 = new CGString("KRX 10000", 20);
   protected CGString hs3 = new CGString("KRX 10000", 20);
   protected CGString hs4 = new CGString("KRX 10000", 20);
   protected CGString hs5 = new CGString("KRX 10000", 20);

	/* 
   *  Constants
   * ==========================================================================
   */
	// 1) Movement Constants
   protected static final int UP = 0;
   protected static final int DOWN = 1;
   protected static final int LEFT = 2;
   protected static final int RIGHT = 3;
   /*
   *  2) Scoring Constants
   * ==========================================================================
   *  These variables define the score values for certain conditionals within
   *  game environment.
   *
   *  Name              Use
   * --------------------------------------------------------------------------
   *  ITEMGET           Score for picking up any item box other than extra life
   *  ONEUPGET          Score for picking up an extra life box
   *  WEAPONGET         Score for picking up a weapon
   *  ENEMY_DEFEAT      Score per level for defeating an non-blue player
   *  HEALTHGET         Score for picking up a health box\crate
   *  DEFEAT            Score lost per level for being defeated
   *  HIT               Score gained for hitting an enemy
   *  GETFLAG           Score for capturing the C.T.F flag
   *  GEMGET            Score for getting a gem
   */
   protected static final int ITEMGET = 500;
   protected static final int ONEUPGET = 12500;
   protected static final int WEAPONGET = 1250;
   protected static final int ENEMY_DEFEAT = 3500;
   protected static final int HEALTHGET = 850;
   protected static final int DEFEAT = 500;
   protected static final int HIT = 35;
   protected static final int GETFLAG = 10000;
   protected static final int GEMGET = 5000;
   // 3) Gem Hunter Constants
   protected static final int NULL_GEM = -1;
   protected static final int BLUE_GEM = 0;
   protected static final int RED_GEM = 1;
   protected static final int GREEN_GEM = 2;
   protected static final int CYAN_GEM = 3;
   protected static final int YELLOW_GEM = 4;
   // 4) Layout Constants
   // * Vertical
   protected static final Dimension VGAP5 = new Dimension(0,5);
   protected static final Dimension VGAP10 = new Dimension(0,10);
   protected static final Dimension VGAP15 = new Dimension(0,15);
   protected static final Dimension VGAP20 = new Dimension(0,20);
   protected static final Dimension VGAP25 = new Dimension(0,25);
   // * Horizontal
   protected static final Dimension HGAP5 = new Dimension(5,0);
   protected static final Dimension HGAP10 = new Dimension(10,0);
   protected static final Dimension HGAP15 = new Dimension(15,0);
   protected static final Dimension HGAP20 = new Dimension(20,0);
   protected static final Dimension HGAP25 = new Dimension(25,0);
   // 5) Names
   /**
   *  This array contains the registered names of the game weapons
   */
   protected static final String[] weaponNames = {
      "Buzzsaw",
      "Iron Curtain",
      "Poison Ring",
      "Lightning Shield",
      "Leech Ring",
      "Toxi Cannon",
      "Pulse Blaster",
      "Flare Ring",
      "Blue Bottle"
      };
   /**
   *  Image Constants\ Variables
   * =================================================================================
   *  Contains the instances of the default images used to draw all onscreen data
	*
	*	Most of these variables are marked as final and cannot be changed, I do however
	*	hope in the future to implement a sort of rough theme support engine.
   */
   protected final Image GUI_FLAG1          = new ImageIcon(getClass().getResource("/rtsx/flag1.PNG")).getImage();
   protected final Image GUI_FLAG2          = new ImageIcon(getClass().getResource("/rtsx/flag2.PNG")).getImage();
   protected final Image GUI_NOMINAL        = new ImageIcon(getClass().getResource("/rtsx2/N_SCORE_BACK.png")).getImage();
   protected final Image ITEM_LAMP          = new ImageIcon(getClass().getResource("/krux3/lamp.png")).getImage();
   protected final Image ITEM_HEALTH        = new ImageIcon(getClass().getResource("/krux2/hcrate.png")).getImage();
   protected final Image ITEM_WEAPON        = new ImageIcon(getClass().getResource("/krux2/wcrate.png")).getImage();
   protected final Image ITEM_LEVEL         = new ImageIcon(getClass().getResource("/krux2/lcrate.png")).getImage();
   protected final Image ITEM_MEGAHP        = new ImageIcon(getClass().getResource("/krux3/megahealth.png")).getImage();
   protected final Image ITEM_MEGAEXP       = new ImageIcon(getClass().getResource("/krux3/megaexp.png")).getImage();
   protected final Image ITEM_GHOSTPOTION   = new ImageIcon(getClass().getResource("/krux3/ghostp.png")).getImage();
   protected final Image ITEM_LIFE          = new ImageIcon(getClass().getResource("/krux2/oneup.png")).getImage();
   protected final Image ITEM_LIFEBOX       = new ImageIcon(getClass().getResource("/rts7/hpbox.png")).getImage();
   protected final Image ITEM_LIFESUPER     = new ImageIcon(getClass().getResource("/rtsx/superlife.png")).getImage();
   protected final Image ITEM_ARMOR         = new ImageIcon(getClass().getResource("/rts7/armor.png")).getImage();
   protected final Image ITEM_UNOSEE        = new ImageIcon(getClass().getResource("/rtsx/u_no_see.png")).getImage();
   protected final Image ITEM_PAINKILLSM    = new ImageIcon(getClass().getResource("/extras/painkill.png")).getImage(); 
   protected final Image ITEM_PAINKILLLG    = new ImageIcon(getClass().getResource("/extras/painkil2.png")).getImage(); 
   protected final Image SPRITE_GHOSTBLUE   = new ImageIcon(getClass().getResource("/krux3/ghost1.png")).getImage();
   protected final Image SPRITE_GHOSTRED    = new ImageIcon(getClass().getResource("/krux3/ghost2.png")).getImage();
   protected final Image SPRITE_EYESUP      = new ImageIcon(getClass().getResource("/krux3/eye0.png")).getImage();
   protected final Image SPRITE_EYESDOWN    = new ImageIcon(getClass().getResource("/krux3/eye1.png")).getImage();
   protected final Image SPRITE_EYESLEFT    = new ImageIcon(getClass().getResource("/krux3/eye2.png")).getImage();
   protected final Image SPRITE_EYESRIGHT   = new ImageIcon(getClass().getResource("/krux3/eye3.png")).getImage();
   protected       Image SPRITE_FLOOR       = new ImageIcon(getClass().getResource("/krux2/floor.GIF")).getImage();
   protected       Image SPRITE_STATICB     = new ImageIcon(getClass().getResource("/krux2/K_Border1.GIF")).getImage();
   protected       Image SPRITE_MOBILEB     = new ImageIcon(getClass().getResource("/krux2/K_Border.GIF")).getImage();
   protected       Image SPRITE_V_BOUND     = new ImageIcon(getClass().getResource("/mazemode/objects/shift_v.PNG")).getImage();
   protected       Image SPRITE_H_BOUND     = new ImageIcon(getClass().getResource("/mazemode/objects/shift_h.PNG")).getImage();
   protected       Image SPRITE_BLUEPLAY    = new ImageIcon(getClass().getResource("/krux2/player.png")).getImage();
   protected       Image SPRITE_REDPLAY     = new ImageIcon(getClass().getResource("/krux2/enemy.png")).getImage();
   protected final Image SPRITE_RADAR       = new ImageIcon(getClass().getResource("/krux3/sense.gif")).getImage();
   protected       Image OBJECT_HBORDER     = new ImageIcon(getClass().getResource("/krux3/homingborder.png")).getImage();   
   protected final Image OBJECT_BLUEGEM     = new ImageIcon(getClass().getResource("/rts7/bluegem.png")).getImage();
   protected final Image OBJECT_CYANGEM     = new ImageIcon(getClass().getResource("/rts7/cyangem.png")).getImage();
   protected final Image OBJECT_GREENGEM    = new ImageIcon(getClass().getResource("/rts7/greengem.png")).getImage();
   protected final Image OBJECT_REDGEM      = new ImageIcon(getClass().getResource("/rts7/redgem.png")).getImage();
   protected final Image OBJECT_YELLOWGEM   = new ImageIcon(getClass().getResource("/rts7/yellowgem.png")).getImage();
   protected final Image OBJECT_NULLFLAG    = new ImageIcon(getClass().getResource("/krux2/grayflag.PNG")).getImage();
   protected final Image OBJECT_BLUEFLAG    = new ImageIcon(getClass().getResource("/krux2/blueflag.PNG")).getImage();
   protected final Image OBJECT_REDFLAG     = new ImageIcon(getClass().getResource("/krux2/redflag.PNG")).getImage();
   protected final Image OVERLAY_FLAGHUNT   = new ImageIcon(getClass().getResource("/rtsx/flaghuntdisp.PNG")).getImage();
   protected final Image OVERLAY_GEMHUNT    = new ImageIcon(getClass().getResource("/rtsx/gemhuntdisp.PNG")).getImage();
   protected final Image OVERLAY_HPBAR      = new ImageIcon(getClass().getResource("/krux3/hpbar.PNG")).getImage();
   protected final Image OVERLAY_HEALING    = new ImageIcon(getClass().getResource("/krux3/healing.gif")).getImage();
   protected final Image OVERLAY_NIGHT      = new ImageIcon(getClass().getResource("/krux3/night.png")).getImage();  
   protected final Image OVERLAY_NIGHTLAMP  = new ImageIcon(getClass().getResource("/rtsxi/nightmode_lamp.png")).getImage();  
   protected final Image OVERLAY_POISON     = new ImageIcon(getClass().getResource("/krux3/poisoned.png")).getImage(); 
   protected final Image OVERLAY_SHOCK      = new ImageIcon(getClass().getResource("/krux3/shocked.png")).getImage();
   protected final Image OVERLAY_STATUS     = new ImageIcon(getClass().getResource("/krux3/statusbox.png")).getImage();
   protected final Image OVERLAY_SCORES     = new ImageIcon(getClass().getResource("/krux3/hs_overlay.png")).getImage();
   protected final Image OVERLAY_TIMER      = new ImageIcon(getClass().getResource("/rtsx/timerdisp.PNG")).getImage();
   protected final Image OVERLAY_LIFEBAR    = new ImageIcon(getClass().getResource("/rtsxi/lifedraw.png")).getImage();
   protected final Image OVERLAY_EXPBAR     = new ImageIcon(getClass().getResource("/rtsxi/exprdraw.png")).getImage();
   protected final Image OVERLAY_ARMORBAR   = new ImageIcon(getClass().getResource("/rtsxi/armrdraw.png")).getImage();
   protected final Image OVERLAY_TEAMBLUE   = new ImageIcon(getClass().getResource("/rtsxi/blueteam.png")).getImage();
   protected final Image OVERLAY_TEAMRED    = new ImageIcon(getClass().getResource("/rtsxi/redteam.png")).getImage();
   protected final Image OVERLAY_STATBALLOON= new ImageIcon(getClass().getResource("/rtsx2/statusdiag.png")).getImage();
   protected final Image OVERLAY_HPBALLOON  = new ImageIcon(getClass().getResource("/rtsx2/hponlydiag.png")).getImage();
   protected final Image OVERLAY_EXPBALLOON = new ImageIcon(getClass().getResource("/rtsx2/exponlydiag.png")).getImage();
   protected final Image OVERLAY_LIFEBARM   = new ImageIcon(getClass().getResource("/rtsx2/lifedraw_med.png")).getImage();
   protected final Image OVERLAY_LIFEBARH   = new ImageIcon(getClass().getResource("/rtsx2/lifedraw_high.png")).getImage();
	
	// Generic Null Tile
   protected final Image NULL_TILE   = new ImageIcon(getClass().getResource("/tileset/predef/nulltile.png")).getImage();   
			
   // Floortile design variables
   protected       Image floortile1         = NULL_TILE;
   protected       Image floortile2         = NULL_TILE;
   protected       Image floortile3         = NULL_TILE;
   protected       Image floortile4         = NULL_TILE;
   protected       Image floortile5         = NULL_TILE;
   
	/*
	*	Resource Methods
	* ==================================================================================
	*	Controls the gathering of external resources
	*/
   protected Image getImageResource(String resourceKey) {
      return new ImageIcon(getClass().getResource("/rtsxi/armrdraw.png")).getImage();
   }
	
	/*
   *  Initialization Methods
   * ==================================================================================
	*  Controls the startup and loading of Krux
	*/
	
   /**
   *  Krux Method
   * ==================================================================================
   *  initialize
   *  Processes the startup for the Krux App, REMLOCKE startup method
   */	
   public void initialize() {
    	// Set the default L&F to decorate the windows and dialogs...cute...
      printDebugMessage("DEBUGGER - Session Started");
      printDebugMessage("DEBUGGER - Process:\t" + this.toString());
      printDebugMessage("DEBUGGER - Class:\tkruxloader");
      printDebugMessage("DEBUGGER - Start Time: " + java.time.LocalDateTime.now().getHour() + "-" + java.time.LocalDateTime.now().getMinute() + "-" + java.time.LocalDateTime.now().getSecond() + "\n");
   	
      printDebugMessage("Generic - Process Started");
   	
      try {
         // Attempt the apply the look and feel
         UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
      	// Disable to default bold text of the Java Metal Theme        
         UIManager.put("swing.boldMetal", Boolean.FALSE);
      	
      	// Search for an existing scoreboard file
         if(!new File("scoreboard.adf").exists()) {
            scrboard = new Scoreboard(); // ...and created a new one if it doesn't exist...
         	
            try {
               FileOutputStream fstrm = new FileOutputStream(new File("scoreboard.adf"));
               ObjectOutput ostrm = new ObjectOutputStream(fstrm);
               ostrm.writeObject(scrboard);
               ostrm.flush();
               ostrm.close();
               fstrm.close();
            }
            catch (Exception e) {
               e.printStackTrace();
               printDebugMessage("INIT - EXCEPTION: " + e.getMessage() + ", Line: 1098");
            }
         	
            System.out.println("New Scoreboard Created!");
            scrboard.printRates();
         }
         else {
            FileInputStream fin = new FileInputStream(new File("scoreboard.adf")); // ...or load the existing one if it does.
            ObjectInputStream istrm = new ObjectInputStream(fin);
            scrboard = (Scoreboard) istrm.readObject();
            istrm.close();
         	
            System.out.println("Saved Scoreboard Loaded!");
            //scrboard.printRates(); <-- removed for faster start-up times ^^
         }
      }
      catch (Exception e) {
         e.printStackTrace();
         printDebugMessage("INIT - EXCEPTION: " + e.getMessage() + ", Line: 1116");
      }
      JFrame.setDefaultLookAndFeelDecorated(true);
      JDialog.setDefaultLookAndFeelDecorated(true);
      kohtimer.setEditable(false);
    
//    Consider this game "cracked"
      prodcode = rB.runProdCodeCheck();
      isRegist = rB.runRegCheck();
     
      if(!isRegist) {
         printDebugMessage("Regbase4 - Product Not Registered");
         JOptionPane.showMessageDialog(
                        startdiag,
            				"<html><b>This version of " + PRODUCT + " has not been activated.</b><br><br>" + 
            				"This application must be activated before it can be used. The activation dialog<br>" +
            				"will now be displayed.</html>",
                        "Activation required!",
                        JOptionPane.WARNING_MESSAGE);
         rB.enterCode();
      }
      else {
         printDebugMessage("Regbase4 - Product in Registered to: " + rB.REGNAME);
      // Inits some values
         try {
            printDebugMessage("ErrHandle - ErrorHandler is now loaded");
            printErr = new PrintWriter(new FileOutputStream(new File("errdesc.txt")));
            printDebugMessage("DSound - Sound Buffer has been initialized");
            tempo = File.createTempFile("tmpo1", null);
            tempo.deleteOnExit();
         }
         catch (Throwable e) {
            printErrMeth(e, "INIT", false); 
            printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage() + ", Line: 1148");
         }
         maxbounds = (mapsize.x * mapsize.y) / 2; 
         label1 = new JLabel("Standard Boundries (0 - " + maxbounds + ")");
         label2 = new JLabel("Flexible Boundries (0 - " + maxbounds + ")");
         label4 = new JLabel("Tracking Boundries (0 - " + maxbounds / mapsize.x + ")");
      
      /*
      * This method displays the new game dialog 
      * The dialog is used to specify the start-up game play variables
      */
         printDebugMessage("GDI - Frame Buffer Loaded");
         notneeded.setSize(800,600);
         curHealthP = maxHealthP;
         curHealthE = maxHealthE;
      
      // The required panels...
         JPanel creator = new JPanel();
         JPanel newGame = new JPanel();
         JPanel mapConstraints = new JPanel();
         JPanel boundConstraints = new JPanel();
         JPanel AIConstraints = new JPanel();
         JPanel gameConstraints = new JPanel();
         JPanel miscConstraints = new JPanel();
         
      // ...and their layouts
         creator.setLayout(new BoxLayout(creator, BoxLayout.Y_AXIS));
         newGame.setLayout(new BoxLayout(newGame, BoxLayout.X_AXIS));
         
         mapConstraints.setLayout(new BoxLayout(mapConstraints, BoxLayout.Y_AXIS));
         mapConstraints.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Map Size"));
         
         boundConstraints.setLayout(new BoxLayout(boundConstraints, BoxLayout.Y_AXIS));
         boundConstraints.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Boundaries"));
         
         miscConstraints.setLayout(new BoxLayout(miscConstraints, BoxLayout.Y_AXIS));
         miscConstraints.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Miscellaneous"));
      	
         AIConstraints.setLayout(new BoxLayout(AIConstraints, BoxLayout.Y_AXIS));
         AIConstraints.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "AI"));
         
         gameConstraints.setLayout(new BoxLayout(gameConstraints, BoxLayout.Y_AXIS));
         gameConstraints.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Gameplay"));
      
         final JLabel label3 = new JLabel("Radar Sense Distance");
      
      // Panel Items
         final JSlider setStandardBounds = new JSlider(JSlider.HORIZONTAL, 0, maxbounds, 10);
         setStandardBounds.setSnapToTicks(true);
         setStandardBounds.setToolTipText("Sets the amount of stationary boundries on the field");
      
         final JSlider setMobileBounds = new JSlider(JSlider.HORIZONTAL, 0, maxbounds, 5);
         setMobileBounds.setSnapToTicks(true);
         setMobileBounds.setToolTipText("Sets the amount of flexible boundries on the field");
      
         final JSlider setTrackingBounds = new JSlider(JSlider.HORIZONTAL, 0, trackingBoundsMax, 0);
         setTrackingBounds.setSnapToTicks(true);
         setTrackingBounds.setToolTipText("Sets the amount of tracking boundries on the field");
      
         final JSlider setXSize = new JSlider(JSlider.HORIZONTAL, 8, 128, 12);
         setXSize.setSnapToTicks(true);
         setXSize.setToolTipText("Sets the horizontal size (width) of the playing field");
      
         String[] gametypes = { "Standard Game", "Control the Flag Mode", "Gem Hunter Mode", "Team Deatchmatch" };
      
         final JComboBox<String> gameSet = new JComboBox<String>(gametypes);
         gameSet.setToolTipText("Sets the Krux gameplay mode");
      
         final JCheckBox nightSet = new JCheckBox("Enable Night Mode");
         nightSet.setToolTipText("Enabled or disables Night Mode. In nightmode your field of vision is greatly reduced");
      
         final JCheckBox fullSet = new JCheckBox("Windowed Mode");
         fullSet.setToolTipText("Set weather or not the game should be playing in full screen mode");
         fullSet.setSelected(true);
      
         final JCheckBox limitSet = new JCheckBox("Limit Enemies");
         limitSet.setToolTipText("Limits the maximum amount of enemies on the enemies on the map to improve performance");
      
         final JSlider setSense = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
         setSense.setSnapToTicks(true);
         setSense.setToolTipText("Sets the Maximum Radar Sense distance");
      	
      	// The change listener for the setSense JSlider
         setSense.addChangeListener(
                  new ChangeListener() {
                     public void stateChanged(ChangeEvent e) {
                        JSlider source = (JSlider)e.getSource();
                        if (!source.getValueIsAdjusting()) {
                           senseDist = (int) source.getValue();
                        }
                     } 
                  });
            
         final JCheckBox thirdSet = new JCheckBox("Enable Extreme Rules");
         thirdSet.setToolTipText("Enabled or disables Extreme Rules. Extreme Rules make the game a lot more challanging but make you gain more score");
         thirdSet.setEnabled(false);
      
         final JSlider setDifficulty = new JSlider(JSlider.HORIZONTAL, 1, 3, 2);
         setDifficulty.setSnapToTicks(true);
         setDifficulty.setToolTipText("Sets the Maximum Difficulty of the AI");
      
         setDifficulty.addChangeListener(
                  new ChangeListener() {
                     public void stateChanged(ChangeEvent e) {
                        JSlider source = (JSlider)e.getSource();
                        if (!source.getValueIsAdjusting()) {
                           difficulty = (int)source.getValue();
                           if (difficulty == 1) {
                              nightSet.setSelected(false);
                              nightSet.setEnabled(false);
                              gameSet.setSelectedIndex(0);
                              gameSet.setEnabled(false);
                           }
                           else {
                              nightSet.setEnabled(true);
                              gameSet.setEnabled(true);
                           }
                        
                           if (difficulty == 4) {
                              setSense.setEnabled(false);
                              setSense.setMaximum(1);
                           }
                           else if (difficulty > 2) {
                              setSense.setMaximum(5);
                              thirdSet.setEnabled(true);
                           }
                           else {
                              setSense.setMaximum(10);
                              thirdSet.setEnabled(false);
                              thirdSet.setSelected(false);
                           }
                        }
                     } 
                  });
      
         setXSize.addChangeListener(
                  new ChangeListener() {
                     public void stateChanged(ChangeEvent e) {
                        JSlider source = (JSlider)e.getSource();
                        if (!source.getValueIsAdjusting()) {
                           mapsize.x = (int)source.getValue();
                           maxbounds = (mapsize.x * mapsize.y) / 2;
                           OFFSCREEN = new Point (mapsize.x * 2, mapsize.y * 2);
                           setStandardBounds.setMaximum(maxbounds);
                           setMobileBounds.setMaximum(maxbounds);
                           trackingBoundsMax = maxbounds / mapsize.x;
                           setTrackingBounds.setMaximum(maxbounds / mapsize.x);
                           label1.setText("Standard Boundries (0 - " + maxbounds + ")");
                           label2.setText("Flexible Boundries (0 - " + maxbounds + ")");
                           label4.setText("Tracking Boundries (0 - " + maxbounds / mapsize.x + ")");
                        
                           if((mapsize.x * mapsize.y) >= 4096) {
                              limitSet.setSelected(true);
                              limitSet.setEnabled(false);
                              if((mapsize.x * mapsize.y) >= 9216)
                                 botLimit = 12;
                              else if((mapsize.x * mapsize.y) >= 16384)
                                 botLimit = 8;
                              else
                                 botLimit = 18;
                           }
                           else {
                              if(!DEATHMATCHMODE)
                                 limitSet.setEnabled(true);
                           }
                        }
                     } 
                  });
      
         final JSlider setYSize = new JSlider(JSlider.HORIZONTAL, 8, 128, 12);
         setYSize.setSnapToTicks(true);
         setYSize.setToolTipText("Sets the vertical size (height) of the playing field");
      
         setYSize.addChangeListener(
                  new ChangeListener() {
                     public void stateChanged(ChangeEvent e) {
                        JSlider source = (JSlider)e.getSource();
                        if (!source.getValueIsAdjusting()) {
                           mapsize.y = (int)source.getValue();
                           OFFSCREEN = new Point (mapsize.x * 2, mapsize.y * 2);
                           maxbounds = (mapsize.x * mapsize.y) / 2;
                           setStandardBounds.setMaximum(maxbounds);
                           setMobileBounds.setMaximum(maxbounds);
                           label1.setText("Standard Boundries (0 - " + maxbounds + ")");
                           label2.setText("Flexible Boundries (0 - " + maxbounds + ")");
                           label4.setText("Tracking Boundries (0 - " + maxbounds / mapsize.x + ")");
                        
                           if((mapsize.x * mapsize.y) >= 4096) {
                              limitSet.setSelected(true);
                              limitSet.setEnabled(false);
                              if((mapsize.x * mapsize.y) >= 9216)
                                 botLimit = 12;
                              else if((mapsize.x * mapsize.y) >= 16384)
                                 botLimit = 8;
                              else
                                 botLimit = 18;
                           }
                           else {
                              if(!DEATHMATCHMODE)
                                 limitSet.setEnabled(true);
                           }
                        }
                     } 
                  });
      
         final JSlider setGameSpeed = new JSlider(JSlider.HORIZONTAL, 850, 1000, 850);
         setGameSpeed.setToolTipText("Sets the game speed in terms of refresh rate (Keep this low on older systems)");
      
         final JSlider setHealthRatio = new JSlider(JSlider.HORIZONTAL, 25, 100, 100);
         setHealthRatio.setSnapToTicks(true);
         setHealthRatio.setToolTipText("Sets the game's health handicap");
      
         final JSlider setHealthLust = new JSlider(JSlider.HORIZONTAL, 10, 50, 25);
         setHealthLust.setSnapToTicks(true);
         setHealthLust.setToolTipText("Sets the life level at which the Enemy starts looking for a Health Crate (Requires \"Smart Enemies\")");
      
         final JSlider setReviveLimit = new JSlider(JSlider.HORIZONTAL, 0, 10, 5);
         setReviveLimit.setSnapToTicks(true);
         setReviveLimit.setToolTipText("Sets the amount starting Lives. If set to 0, you have unlimited lives, however you cannot create a High Score in this case.");
      
         final JCheckBox violentSet = new JCheckBox("Enable Sound");
         violentSet.setToolTipText("Toggles the in-game sound effects and music");
         violentSet.setSelected(true);
      
         final JCheckBox senseSet = new JCheckBox("Radar Sense Mode");
         senseSet.setToolTipText("Enables or disables the Radar Sense in night mode");
         senseSet.setEnabled(false);
      
         final JButton LoadMap = new JButton("Load a KMF Map...");
         LoadMap.setToolTipText("Loads a precreated custom map");
      
         final JButton terminate = new JButton("Generate a Custom Map");
         terminate.setToolTipText("Generates a Random Map");
      
         final JButton kruxabout = new JButton("About Krux Tourno");
      
         final JPanel AboutPanel = 
               new JPanel() {
                  public void paintComponent(Graphics g) {
                     String regSt = "";
                     String regSt2 = "";
                     if (isRegist) {
                        regSt = rB.REGNAME;
                        regSt2 = rB.REGCUMP;
                     }
                     else 
                        regSt = "Unactivated";
                  
                  // Heading
                     g.setFont(new Font("Tahoma", Font.PLAIN, 11));
                     g.drawImage(new ImageIcon(getClass().getResource("/images/about_img.PNG")).getImage(),0,0,this);
                     g.drawString(PRODUCT + " (" + VERSION + "." + BUILD + "), " + REVISION, 78, 17);
                     g.drawString(REVIEW, 78, 34);
                     g.drawString("2008-2010 Microtech Technologies, 2017 Micron Information Systems", 91, 50);
                  
                  // Copyright Notice
                     g.setFont(new Font("Tahoma", Font.PLAIN, 9));
                     g.drawString("Parts of this application were created by external authors and are copyrights of their respective owners and", 78, 75);
                     g.drawString("are not maintained by Micron Information Services. The KMF file format is a trademark of Radioactive Reds", 78, 87);
                     g.drawString("Animation Studios. Krux Tourno is a shared trademark of Radioactive Reds Animation, Nature's", 78, 99);
                     g.drawString("Little Helpers and Micron Information Systems.", 78, 111);
                     g.drawString("", 78, 123);
                     g.drawString("", 78, 135);
                  
                  // Registration Section
                     g.setFont(new Font("Tahoma", Font.PLAIN, 11));
                     g.drawString(regSt, 86, 246);
                     g.drawString(regSt2, 86, 262);
                  
                  // Product Key
                     g.drawString(prodcode, 150, 278);
                  
                  // Warning
                     g.drawString("This computer is protected by copyright law and international treaties. Unauthorized reproduction", 53, 346);
                     g.drawString("or distribution of this program, or any portion of it, may result in severe civil and criminal penalties, and will", 6, 360);
                     g.drawString("be prosecuted to the maximum extent possible under the law", 6, 374);
                     g.drawString("", 6, 388);
                     g.drawString("", 6, 402);
                     g.drawString("", 6, 416);
                  }
               };
      
         printDebugMessage("KRUXTOURN - About Panel Rendered");
         AboutPanel.setPreferredSize(new Dimension(529, 395));
      
         kruxabout.addActionListener(
                  new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        Object[] mess = { AboutPanel };
                        String[] ops = { "        OK        ", " File Versions " };               
                        int dodo = JOptionPane.showOptionDialog(
                                    null,
                                    mess,
                                    "About " + PRODUCT,
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.PLAIN_MESSAGE,
                                    null,
                                    ops,
                                    ops[0]);
                        if(dodo == 1) {
                           JPanel VersPanel = 
                              new JPanel() {
                                 public void paintComponent(Graphics g) {
                                    g.setFont(new Font("Tahoma", Font.PLAIN, 11));
                                    g.drawImage(new ImageIcon(getClass().getResource("/images/filever_img.PNG")).getImage(),0,0,this);
                                    try {
                                       g.drawString(Oberon_AIDrivenEnemy.CLASSVERSION, 350, 36);
                                       g.drawString(regBase4.CLASSVERSION, 350, 49);
                                       g.drawString(fileCompressionUtil.CLASSVERSION, 350, 62);
                                       g.drawString(CGString.CLASSVERSION, 350, 75);
                                    }
                                    catch (Exception e) {
                                       e.printStackTrace();
                                       printDebugMessage("ABOUT - EXCEPTION: " + e.getMessage() + ", Line: 1461");
                                    };
                                    g.setFont(new Font("Tahoma", Font.BOLD, 11));
                                    g.drawString("(" + regBase4.CLASSTYPE + ")", 247, 49);
                                 }
                              };
                        	
                           VersPanel.setPreferredSize(new Dimension(529, 100));
                        
                           Object[] msg = { VersPanel }; 
                           String[] opz = { "        OK        " };               
                           JOptionPane.showOptionDialog(
                                    null,
                                    msg,
                                    PRODUCT + " system file versions",
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.PLAIN_MESSAGE,
                                    null,
                                    opz,
                                    opz[0]);
                        }
                     }   
                  }
            );
      
         LoadMap.addActionListener(
                  new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        printDebugMessage("KRUXTOURN - Calling MapLoader...");
                        JFileChooser fc = new JFileChooser(".");
                        fc.setFileFilter(
                              new javax.swing.filechooser.FileFilter() {
                                 FileFilter mxtFilter;
                              
                                 public boolean accept(File f) {
                                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".kmf");
                                 }
                              
                                 public String getDescription() {
                                    return "Krux Map Format (*.KMF)";
                                 }
                              }
                           );
                        int coco = fc.showOpenDialog(null);
                     
                        if(coco == JFileChooser.APPROVE_OPTION) {
                           gamespeed = (1001 - setGameSpeed.getValue());
                           if(setReviveLimit.getValue() <= 0) {
                              revivesUnlimited = true;
                           }
                           else {
                              revivesUnlimited = false;
                              revivelimit = (byte)setReviveLimit.getValue();
                           }
                           healthRatio = setHealthRatio.getValue();
                           healthlust = setHealthLust.getValue();
                        
                           loadedmap = true;
                        
                           loadCustomMap(fc.getSelectedFile());
                        
                           healthLocat = new Point(mapsize.x * 2, mapsize.y * 2); 		// Health Box location (default is off-screen)
                           levelboxLocat = new Point(mapsize.x * 2, mapsize.y * 2); 	// Level-Up Box location (default is off-screen)
                           extraLifeLocat = new Point(mapsize.x * 2, mapsize.y * 2); 	// Extra Life Box location (default is off-screen)
                           weaponboxLocat = new Point(mapsize.x * 2, mapsize.y * 2);
                           megahealthLocat = new Point(mapsize.x * 2, mapsize.y * 2);
                           megaexpLocat = new Point(mapsize.x * 2, mapsize.y * 2);
                           lampLocat = new Point (mapsize.x * 2, mapsize.y * 2);
                           JOptionPane.showMessageDialog(
                              null,
                              "Completed Loading of \"" + fc.getSelectedFile().getName() + "\nWill now resume loading Krux 3...",
                              "Map Loading Complete",
                              JOptionPane.INFORMATION_MESSAGE);
                           startdiag.dispose();
                           loadGame();
                           resetMap();
                        }
                        else
                           printDebugMessage("KRUXTOURN - MapLoader Call Failed or Cancelled");
                     }
                  }
            );
      
      // Item Listener for Third Guy Violence (I recycled this object, why havn't I updated the
      // comment yet?)
         violentSet.addItemListener(
                  new ItemListener() {
                     public void itemStateChanged(ItemEvent e) {
                        if (soundOn) {
                           soundOn = false;
                        }
                        else {
                           soundOn = true;
                        }
                     }});
               
         limitSet.addItemListener(
                  new ItemListener() {
                     public void itemStateChanged(ItemEvent e) {
                        botLimitImposed = limitSet.isSelected();
                     }});
               
         nightSet.addItemListener(
                  new ItemListener() {
                     public void itemStateChanged(ItemEvent e) {
                        nightMode = nightSet.isSelected();
                        if (nightMode) {
                           senseSet.setEnabled(true);
                           senseSet.setSelected(true);
                           enemyLineOfSight = 4;
                        }
                        else {
                           senseSet.setSelected(false);
                           senseSet.setEnabled(false);
                           setSense.setEnabled(false);
                           label3.setEnabled(false);
                           enemyLineOfSight = 8;
                        }
                     }});
               
         fullSet.addItemListener(
                  new ItemListener() {
                     public void itemStateChanged(ItemEvent e) {
                        fullScreen = !fullSet.isSelected();
                     }});
      
         senseSet.addItemListener(
                  new ItemListener() {
                     public void itemStateChanged(ItemEvent e) {
                        sense = senseSet.isSelected();
                        if (sense) {
                           setSense.setEnabled(true);
                           label3.setEnabled(true);
                        }
                        else {
                           setSense.setEnabled(false);
                           label3.setEnabled(false);
                        }
                     }});
               
         gameSet.addActionListener(
                  new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        JComboBox cb = (JComboBox)e.getSource();
                        int ca = cb.getSelectedIndex();
                     
                        if(ca == 0) {
                           kohmode = false;
                           gemHunterMode = false;
                           DEATHMATCHMODE = false;
                           FRIENDLYFIRE   = true;
                           limitSet.setEnabled(true);
                        }
                        else if (ca == 1) {
                           kohmode = true;
                           gemHunterMode = false;
                           DEATHMATCHMODE = false;
                           FRIENDLYFIRE   = true;
                           limitSet.setEnabled(true);
                        }
                        else if (ca == 2) {
                           kohmode = false;
                           gemHunterMode = true;
                           DEATHMATCHMODE = false;
                           FRIENDLYFIRE   = true;
                           limitSet.setEnabled(true);
                        }
                        else {
                           kohmode = false;
                           gemHunterMode = false;
                           DEATHMATCHMODE = true;
                           FRIENDLYFIRE   = false;
                        // Limit must be set in team deathmatch mode
                           limitSet.setSelected(true);
                           limitSet.setEnabled(false);
                        }
                     
                        if(difficulty == 1) {
                           cb.setSelectedIndex(0);
                        }
                     
                        if((mapsize.x * mapsize.y) >= 4096) {
                           limitSet.setSelected(true);
                           limitSet.setEnabled(false);
                           if((mapsize.x * mapsize.y) >= 9216)
                              botLimit = 12;
                           else if((mapsize.x * mapsize.y) >= 16384)
                              botLimit = 8;
                           else
                              botLimit = 18;
                        }
                     }
                  });
      			
         thirdSet.addItemListener(
                  new ItemListener() {
                     public void itemStateChanged(ItemEvent e) {
                        if(extremeRules)
                           extremeRules = false;
                        else
                           extremeRules = true;
                     }});
               
      
      // Terminate Button's Action Listener
         terminate.addActionListener(
                  new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        printDebugMessage("KRUXTOURNO - No Map Specified, Generating Random Map...");
                     
                        bounds = new Point[maxbounds];
                        mobileBounds = new Point[maxbounds];
                        mBoundClass = new char[maxbounds];
                        trackingbounds = new Point[trackingBoundsMax]; 
                     
                        for(int h = 0; h < maxbounds; h ++) {
                           bounds[h] = new Point(0,0);
                        }
                        for(int h = 0; h < maxbounds; h ++) {
                           mobileBounds[h] = new Point(0,0);
                        }
                        for(int h = 0; h < trackingbounds.length ; h ++) {
                           trackingbounds[h] = new Point(0,0);
                        }
                     
                        floorsV = new Point[mapsize.x * mapsize.y];
                        floors1 = new Point[mapsize.x * mapsize.y];
                        floors2 = new Point[mapsize.x * mapsize.y];
                        floors3 = new Point[mapsize.x * mapsize.y];
                        floors4 = new Point[mapsize.x * mapsize.y];
                        floors5 = new Point[mapsize.x * mapsize.y];
                     
                     // Test if the game should use a level theme or not
                        int genTheme = (int) (Math.random() * themeNames.length);
                        genCave  = (Math.random() * 10) > 4.5 ? true : false;
                        if(genTheme != 0) {
                           printDebugMessage("KRUXTOURNO - Theme " + themeNames[genTheme] + " Loaded, Loading Theme Data...");
                        // Load all the data for the theme
                           String theme = themeNames[genTheme].toLowerCase();
                           SPRITE_FLOOR       = new ImageIcon(getClass().getResource("/tileset/predef/" + theme + "_floor.png")).getImage();
                           SPRITE_STATICB     = new ImageIcon(getClass().getResource("/tileset/predef/" + theme + "_static.png")).getImage();
                           SPRITE_MOBILEB     = new ImageIcon(getClass().getResource("/tileset/predef/" + theme + "_mobile.png")).getImage();
                           OBJECT_HBORDER     = new ImageIcon(getClass().getResource("/tileset/predef/" + theme + "_tracker.png")).getImage();
                        
                           try {
                              floortile1			 = new ImageIcon(getClass().getResource("/tileset/predef/" + theme + "_fprop1.png")).getImage();
                              floortile2			 = new ImageIcon(getClass().getResource("/tileset/predef/" + theme + "_fprop2.png")).getImage();
                              floortile3			 = new ImageIcon(getClass().getResource("/tileset/predef/" + theme + "_fprop3.png")).getImage();
                           }
                           catch (NullPointerException npe) {
                              printDebugMessage("A required theme element was not found...it's not a train smash though, so let's continue shall we?");
                           }   
                        // Generate Random Floors
                           int totalratio = (int) ((mapsize.x * mapsize.y) * 0.75);
                           int fl1ratio = (int) (Math.random() * totalratio);
                           int fl2ratio = (int) ((totalratio - fl1ratio) * Math.random());
                           int fl3ratio = (int) ((totalratio - fl1ratio - fl2ratio) * Math.random());
                        
                           for(int x = 0; x < fl1ratio; x++) {
                              Point h = findFreeBlock();
                              addFloorTileDesign(h.x, h.y, 1);
                           }
                           for(int x = 0; x < fl2ratio; x++) {
                              Point h = findFreeBlock();
                              addFloorTileDesign(h.x, h.y, 2);
                           }
                           for(int x = 0; x < fl3ratio; x++) {
                              Point h = findFreeBlock();
                              addFloorTileDesign(h.x, h.y, 3);
                           }
                        
                           if(genCave) {
                              for(int x = 1; x < (mapsize.x + 1); x++) {
                                 addBounds(x, 1, BOUND_SOLID);
                                 addBounds(x, mapsize.y, BOUND_SOLID);
                              }
                           
                              for(int y = 2; y < (mapsize.y + 1); y++) {
                                 addBounds(1, y, BOUND_SOLID);
                                 addBounds(mapsize.x, y, BOUND_SOLID);
                              }
                           }
                        }
                     
                        makebou = (setStandardBounds.getValue() + count) < maxbounds ? setStandardBounds.getValue() : maxbounds - setStandardBounds.getValue();
                        makembou = setMobileBounds.getValue();
                        maketbou = setTrackingBounds.getValue();
                     
                        gamespeed = (1001 - setGameSpeed.getValue());
                        if(setReviveLimit.getValue() <= 0) {
                           revivesUnlimited = true;
                        }
                        else {
                           revivesUnlimited = false;
                           revivelimit = (byte)setReviveLimit.getValue();
                        }
                        healthRatio = setHealthRatio.getValue();
                        healthlust = setHealthLust.getValue();
                     
                        mapdata = new String[mapsize.x][mapsize.y];
                     
                        maxMoves = (difficulty * 4);
                     
                        healthLocat = new Point(mapsize.x * 2, mapsize.y * 2); 		// Health Box location (default is off-screen)
                        levelboxLocat = new Point(mapsize.x * 2, mapsize.y * 2); 	// Level-Up Box location (default is off-screen)
                        extraLifeLocat = new Point(mapsize.x * 2, mapsize.y * 2); 	// Extra Life Box location (default is off-screen)
                        weaponboxLocat = new Point(mapsize.x * 2, mapsize.y * 2);	// Weapon Box Location
                        megahealthLocat = new Point(mapsize.x * 2, mapsize.y * 2);
                        megaexpLocat = new Point(mapsize.x * 2, mapsize.y * 2);
                        lampLocat = new Point (mapsize.x * 2, mapsize.y * 2);
                                      
                        if(genCave) {
                           spawnPoint1.x = 1;
                           spawnPoint1.y = 1;
                           locationPlayerX = spawnPoint1.x;
                           locationPlayerY = spawnPoint1.y;
                        
                           spawnPoint2.x = mapsize.x - 2;
                           spawnPoint2.y = mapsize.y - 2;
                           locationEnemyX = spawnPoint2.x;
                           locationEnemyY = spawnPoint2.y;
                        }
                        else 
                           spawnPoint2 = new Point(mapsize.x - 1, mapsize.y - 1);
                     
                        if(kohmode) {
                           kohlocat = new Point(mapsize.x / 2, mapsize.y / 2);
                        }
                     
                     // initializing the boundries
                        JOptionPane.showMessageDialog(
                           null,
                           "Completed Generation of \"RMGMAP.KMF\"\nWill now resume loading Krux 3...",
                           "Generation Complete",
                           JOptionPane.INFORMATION_MESSAGE);
                        startdiag.dispose();
                        printDebugMessage("MapLoader - Random Map \"RMGMAP.KMF\" Generated");
                        loadGame();
                        printDebugMessage("KRUXTOURN - Now starting the game...");
                     }
                  });
      
         setSense.setEnabled(false);
         label3.setEnabled(false);
      
      // Putting it all together		
         mapConstraints.add(new JLabel("Horizontal Map Size (" + setXSize.getMinimum() + " - " + setXSize.getMaximum() + ")"));
         mapConstraints.add(setXSize);
         mapConstraints.add(Box.createRigidArea(VGAP10));
         mapConstraints.add(new JLabel("Vertical Map Size (" + setXSize.getMinimum() + " - " + setYSize.getMaximum() + ")"));
         mapConstraints.add(setYSize);
         mapConstraints.add(Box.createRigidArea(new Dimension(0, 87)));  
         
         boundConstraints.add(label1);
         boundConstraints.add(setStandardBounds);
         boundConstraints.add(Box.createRigidArea(VGAP15));
         boundConstraints.add(label2);
         boundConstraints.add(setMobileBounds);
         boundConstraints.add(Box.createRigidArea(VGAP15));
         boundConstraints.add(label4);
         boundConstraints.add(setTrackingBounds);
         boundConstraints.add(Box.createRigidArea(VGAP15)); 
         boundConstraints.add(Box.createRigidArea(new Dimension(0, 21)));                 
      
         gameConstraints.add(fullSet);
         gameConstraints.add(Box.createRigidArea(VGAP10));
         gameConstraints.add(violentSet);
         gameConstraints.add(Box.createRigidArea(VGAP10));
         gameConstraints.add(new JLabel("Game Speed (" + setGameSpeed.getMinimum() + " - " + + setGameSpeed.getMaximum() +")"));
         gameConstraints.add(setGameSpeed);
         gameConstraints.add(Box.createRigidArea(VGAP10));
         gameConstraints.add(new JLabel("Starting Lives (0 - 10)"));
         gameConstraints.add(setReviveLimit);
         gameConstraints.add(Box.createRigidArea(VGAP10));
         
         miscConstraints.add(limitSet);
         miscConstraints.add(Box.createRigidArea(VGAP10));
         miscConstraints.add(thirdSet);
         miscConstraints.add(Box.createRigidArea(VGAP10));
         miscConstraints.add(nightSet);
         miscConstraints.add(Box.createRigidArea(VGAP10));
         miscConstraints.add(senseSet);
         miscConstraints.add(Box.createRigidArea(VGAP10));
         miscConstraints.add(label3);
         miscConstraints.add(setSense);
         miscConstraints.add(Box.createRigidArea(new Dimension(0, 3)));
         
         AIConstraints.add(new JLabel("Difficulty"));
         AIConstraints.add(setDifficulty);
         AIConstraints.add(Box.createRigidArea(VGAP10));
         AIConstraints.add(new JLabel("Handicap (25 - 100)"));
         AIConstraints.add(setHealthRatio);
         AIConstraints.add(Box.createRigidArea(VGAP10));
         AIConstraints.add(new JLabel("Retreat HP (10 - 50)"));
         AIConstraints.add(setHealthLust);
         AIConstraints.add(Box.createRigidArea(new Dimension(0, 235)));
      
      	/*The upper-most panel in the set */
         JPanel newgame1 = new JPanel();
         newgame1.setLayout(new BoxLayout(newgame1, BoxLayout.Y_AXIS));
         
         JPanel newgame2 = new JPanel();
         newgame2.setLayout(new BoxLayout(newgame2, BoxLayout.X_AXIS));
               
         newgame2.add(Box.createRigidArea(HGAP10));
         newgame2.add(gameSet);
         newgame2.add(Box.createRigidArea(HGAP10));
         newgame1.add(newgame2);
         newgame1.add(Box.createRigidArea(VGAP10));
      
      	/*The left-most panel in the set */
         JPanel consts = new JPanel();
         consts.setLayout(new BoxLayout(consts, BoxLayout.Y_AXIS));
         consts.add(mapConstraints);
         consts.add(boundConstraints);
         
      	/*The center panel in the set */
         JPanel centr = new JPanel();
         centr.setLayout(new BoxLayout(centr , BoxLayout.Y_AXIS));
         centr.add(gameConstraints);
         centr.add(miscConstraints);
      
         newGame.add(consts);
         newGame.add(centr);
         newGame.add(AIConstraints);
         newgame1.add(newGame);
      
         JPanel header = new JPanel();
         header.add(new JLabel(new ImageIcon(getClass().getResource("/kdeskicons/kruxicon.PNG"))));
         JPanel termbutt = new JPanel();
         termbutt.add(kruxabout);
         termbutt.add(terminate);
         termbutt.add(LoadMap);
      
         creator.add(header);
         creator.add(Box.createRigidArea(VGAP10));
         creator.add(newgame1);
         creator.add(Box.createRigidArea(VGAP10));
         creator.add(termbutt);
         creator.add(Box.createRigidArea(VGAP10));
      
      // The XWindows Dialog implementation method call
         startdiag = implementDialog("Krux New Game", creator, 570, 674, false);
         startdiag.addWindowListener(
                  new WindowAdapter() {
                     public void windowClosing(WindowEvent e) {
                        printDebugMessage("KRUXTOURN - Dialog was closed, exiting");
                        System.exit(0);
                     };
                  });
      }
   }
   
   /**
   *  Krux 3 Method
   * ==================================================================================
   *  getPoint
   *  Returns a random point
   *
   *  @return  A random Point Variable
   */	
   protected Point getPoint() {
      return new Point ((int) (Math.random() * mapsize.x) + 1, (int) (Math.random() * mapsize.y) + 1);
   }
   
	/**
   *  Krux 4 BETA Method
   * ==================================================================================
   *  botCheck
	*  Checks the amount of bots on the map and adds up to the minimum limit
   *
   *  @return  A random Point Variable
   */	
   protected void botCheck() {
      int botMinimum = (mapsize.x + mapsize.y) / 2;
      
      if (botMinimum > Math.min(50,botLimit)) {
         botMinimum = 50;
      }
   	
      int botCount = aiEnemies.size();
      int botsToAdd = 0;
      if (botCount < botMinimum) {
         botsToAdd = botMinimum - botCount;
         for (int i = botCount; i < botMinimum; i++) {
            addAIEnemy(findFreeBlock());
         }
      }
   }
   
   /**
   *  Krux 3 Method
   * ==================================================================================
   *  findFreeBlock
   *  Finds a unused block on the map and returns its location
   *
   *  @return  A Point pointing to a unused block
   */	
   protected Point findFreeBlock() {
      Point tempo = getPoint();
      boolean checkForOpening = true;
      
      while (checkForOpening) {
         if(tempo.x > (mapsize.x - 1) || tempo.y > (mapsize.y - 1)) {
            checkForOpening = true;
         }
         else {
            checkForOpening = testBoundsAtLocat(new Point(tempo.x, tempo.y - 1), DOWN); 
         }
         if(checkForOpening)
            tempo = getPoint();
         else
            break; 
      }
      
      return tempo;
   }
   
	/*
	*	Krux 3 RTS X2 Method
	* ================================================================================
	*	CalculateEXP
	*	Calculates the amount EXP needed to level up
	*
	*/
   public int CalculateEXP(int lv) {
      float out = 0;
      out = (float)(((6.0f / 5.0f) * Math.pow((double)lv, 3.0)) - (15.0f * Math.pow((double)lv, 2.0)) + (100 * lv) - 140);
      return (int)out;
   }
	
   public int CalculateHP(int iv, int ev, int lv) {
   	//											replace with Base
      float out = (float)((((float)iv + (float)100.0f + ((float)Math.sqrt((double)ev) / 8.0f) + 50) * (float)lv) / 50.0f) + 10.0f;
      return (int) out;
   }
   
   public int CalculateStat(int iv, int base, int ev, int lv) {
      float out = (float)((((float)iv + (float)base + ((float)Math.sqrt((double)ev)) / 8.0f) * (float)lv) / 50.0f) + 5.0f;
      return (int) out;
   }
   
   public int CalculateDamage(int Level, int strength, int base, int enm_end) {
      return (int)(((((((((Level * 2) / 5) + 2) * (float)base * (float)strength / 50) / (float)enm_end) * 1) + 2) *  GetCriticalHit() * GetRandom() / 100) * 1);
   }
	
   public float GetRandom() {
      float one = ((217.0f + ((float)Math.random() * 39.0f)) * 100.0f) / 255.0f;
      return one;
   }
	
   public float GetCriticalHit() {
      float chit = 0.0f;
      float rand = (float)Math.random();
   	
      if(rand > 0.9f)
         chit = 1.5f;
      else if(rand > 0.95f) 
         chit = 2.0f;
      else 
         chit = 1.0f;
         
      return chit;
   }
   
   public int EXPGained(int levelatkr, int levelme) {
   	// Calculate to amount of EXP the opponent gets from kicking my ass
      float out = (((1 * 1 * (float)210 * levelatkr) / 5) * ((levelatkr + 2)/(levelme + 2))) + 1;
   		
      return (int)out;
   }
	
	/**
	*  Hydra-Bounce Method
	* ==================================================================================
	*  loadMapSequence
	*  
	*
	*/
   protected void loadMapSequence() {
      printDebugMessage("KRUXTOURN - Calling MapLoader...");
                  
      gamespeed = (1001 - 850);
   
      revivesUnlimited = false;
      revivelimit = 5;
   		
      healthRatio = 100;
      healthlust = 35;
                     
      loadedmap = true;
      
   	// parseHydraBounceMap(new File("\\mazemode\\mapdata\\hb_map_" + mapnumber + ".hbm"));          
      loadCustomMap(new File("\\mazemode\\mapdata\\hb_map_" + mapnumber + ".kmz"));
                     
      healthLocat = new Point(mapsize.x * 2, mapsize.y * 2); 		// Health Box location (default is off-screen)
      levelboxLocat = new Point(mapsize.x * 2, mapsize.y * 2); 	// Level-Up Box location (default is off-screen)
      extraLifeLocat = new Point(mapsize.x * 2, mapsize.y * 2); 	// Extra Life Box location (default is off-screen)
      weaponboxLocat = new Point(mapsize.x * 2, mapsize.y * 2);
      megahealthLocat = new Point(mapsize.x * 2, mapsize.y * 2);
      megaexpLocat = new Point(mapsize.x * 2, mapsize.y * 2);
      lampLocat = new Point (mapsize.x * 2, mapsize.y * 2);
   
      startdiag.dispose();
      loadGame();
      resetMap();
   }
	
   /**
   *  Krux 3 Method
   * ==================================================================================
   *  doCheat
   *  Processes cheat calls and executes their reactions
   *
   *  @param   cheat    The code to process
   */	
   protected void doCheat(String cheat) {
      String tokens[] = tokenizer(cheat, " ");
      System.out.println("Received: " + cheat);
      
      if(cheat.equals("sv_cheats 1")) {
         System.out.println("Processed as valid by Activation: " + cheat);
         cheatsOn = true;
         cheatsUsed = true;
         hasAUX = true;
         AUX = "Cheat Mode Enabled"; 
         printDebugMessage("CheatLoader - Cheat Mode was Enabled");
         cheat = "";
      }
      else if(cheat.equals("snapscreen")) {
         System.out.println("Processed as valid by Screencap: " + cheat);
         try {
            Graphics2D gd = outputImage.createGraphics();
            grid.print(gd);
            boolean done = ImageIO.write(outputImage, "png",new File("screenshot.png"));
               
            hasAUX = true;
            AUX = "SCREENSHOT SAVED AS screenshot.PNG";
            printDebugMessage("KRUXTOURN - Screenshot Saved");
         }
         catch (Exception e) {
            e.printStackTrace();
            printDebugMessage("CHEATLOADER - EXCEPTION: " + e.getMessage() + ", Line: 2103");
         }
      }
      else if(tokens[0].equals("snapscreen")) {
         System.out.println("Processed as valid by Screencap: " + cheat);
         try {
            Graphics2D gd = outputImage.createGraphics();
            grid.print(gd);
            boolean done = ImageIO.write(outputImage, "png",new File(tokens[1] + ".png"));
               
            hasAUX = true;
            AUX = "SCREENSHOT SAVED AS " + tokens[1] + ".PNG";
            printDebugMessage("KRUXTOURN - Screenshot Saved");
         }
         catch (Exception e) {
            e.printStackTrace();
            printDebugMessage("SCREENSNAP - EXCEPTION: " + e.getMessage() + ", Line: 2119");
         }
      }
      else {
         if(cheatsOn) {}
         else {
            System.out.println("Processed as invalid: " + cheat);
            hasAUX = true;
            AUX = "THOU ART NOT WORTHY";
         }
      }
      
      // sv_cheats setting check
      if(cheatsOn) {
         if(cheat.equals("gunslinger")) {
            playerWeapon = (int) (Math.random() * 8);
            hasAUX = true;
            AUX = "Weapon$ " + playerWeapon + " found";
            if(playerWeapon >= 5 && playerWeapon != 7) {
               pWeapUses = 1;
               pWeapLeft = pWeapUses;
            }
            else if(playerWeapon == 2) {
               pWeapUses = 5;
               pWeapLeft = pWeapUses;
            }
            else {
               pWeapUses = (int) ((9 - playerWeapon) * (Math.random() * 10));
               pWeapLeft = pWeapUses; 
            }   
            
            hasAUX = true;
            AUX = "May the force be with you";
         }
         else if(cheat.equals("pharmo friendly")) {
            getItem(MAIN_PLAYER, HEALTH);
            hasAUX = true;
            AUX = "Drug Addict";
         }
         else if(cheat.equals("senzu bean")) {
            MegaHPRemain = maxHealthP - curHealthP;
            hasMegaHP = true;
            hasAUX = true;
            AUX = "I feel my strength returning";
         }
         else if(cheat.equals("hyperbolic trainer")) {
            MegaExpRemain = (int) ((double) expForNext + ((1 + 0.25) * (1 + 0.25) * (1 + 0.25) * (1 + 0.25) * (1 + 0.25)));
            hasMegaExp = true;
            hasAUX = true;
            AUX = "I feel like a god!";
         }
         else if(cheat.equals("ectoplasmer")) {
            getItem(MAIN_PLAYER, GHOST_POTION);
         }
         else if(cheat.equals("nybble")) {
            getItem(MAIN_PLAYER, MEGA_HEALTH);
            hasAUX = true;
            AUX = "How Refreshing";
         }
         else if(cheat.equals("speak of alecia to me")) {
            Object[] mess = {new JLabel(new ImageIcon(getClass().getResource("/krux3/internal/avatar.png"))), "Alecia Says: \n\"The cheat code that you've entered wasn't really indended for use with this perticular game!\""};
            String[] ops = { "Whoops, my bad!" };               
            JOptionPane.showOptionDialog(
                                    null,
                                    mess,
                                    "A Dialog Copyrighted to Microtech Technologies Incorporated",
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.ERROR_MESSAGE,
                                    null,
                                    ops,
                                    ops[0]);
         }
         else if(cheat.equals("tools for wimps")) {
            getItem(MAIN_PLAYER, EXPERIENCE);
            hasAUX = true;
            AUX = "WIMP";
         }
         else if(cheat.equals("i will never die")) {
            getItem(MAIN_PLAYER, ONE_UP);
            hasAUX = true;
            AUX = "ONE UP";
         }
         else if(cheat.equals("mesa winner")) {
            getVictory(1);
            hasAUX = true;
            AUX = "Such a Loser";
         }
         else if(cheat.equals("cold hearted")) {
            curHealthE = (1);
            hasAUX = true;
            AUX = "Mwahahahaha";
         }
         else if(cheat.equals("that are my flag")) {
            getFlagP();
            hasAUX = true;
            AUX = "CTF";
         }
         else if(cheat.equals("i am weak")) {
            playerLevel = 200;
            levelPlayer = (200);
            hasAUX = true;
            AUX = "The first step is acceptance";
         }
         else if(cheat.equals("i can see clearly now")) {
            getItem(MAIN_PLAYER, LAMP);
         }
         else if(cheat.equals("darkness shall reign")) {
            if(nightMode) {
               nightMode = false;
               hasAUX = true;
               AUX = "The sun rises once more";
            }
            else {
               nightMode = true;
               hasAUX = true;
               AUX = "Night falls";
            }
         }
         else if(cheat.equals("spidersense")) {
            if(sense) {
               sense = false;
               hasAUX = true;
               AUX = "I was wrong";
            }
            else {
               sense = true;
               hasAUX = true;
               AUX = "My Spider Sense is tingling";
            }
         }
         else if(cheat.equals("depth finder")) {
            if(sense) {
               senseDist = 10;
               hasAUX = true;
               AUX = "My perception is heightend";
            }
            else {
               hasAUX = true;
               AUX = "A good dish needs a good radar";
            }
         }
         else if(cheat.equals("fuller up")) {
            curHealthP = maxHealthP;
            hasAUX = true;
            AUX = "Fountain of Youth";
         }
         else if(cheat.equals("you cant touch me")) {
            maxHealthP = 25000;
            curHealthP = 25000;
            hasAUX = true;
            AUX = "25000 Point comin up";
         }
         else if(cheat.equals("what is atonement")) {
            hasAUX = true;
            AUX = "A type of musical lozenge";
         }
         else if(cheat.equals("sv_cheats 0")) {
            cheatsOn = false;
            hasAUX = true;
            AUX = "Cheat Mode Disabled";
         }
         else if(cheat.equals("i could swear you were red")) {
            new ImageIcon(getClass().getResource("/krux2/enemy.png")).getImage();
            SPRITE_REDPLAY = new ImageIcon(getClass().getResource("/krux2/player.png")).getImage();
            hasAUX = true;
            AUX = "What just happened";
         }
         else if(cheat.equals("bot frenzy")) {
            for (int i = 0; i < maxbounds; i++) {
               addAIEnemy((int) (Math.random() * mapsize.x), (int) (Math.random() * mapsize.y));
            }
         }
         else if(cheat.equals("milhouse")) {
            hasAUX = true;
            AUX = "MILHOUSE IS NOT A CHEAT!";
         }
         else if(cheat.equals("pinkietard")) {
            hasAUX = true;
            AUX = "WAT ._.";
         }
         else if(cheat.equals("copyright info")) {
            hasAUX = true;
            AUX = "What...copyright...heavens no...";
         }
         else if(cheat.equals("")) {
         }
         else {
            hasAUX = true;
            AUX = "THOU ART NOT WORTHY";
         }
      }
      
      // dev_mode setting check
      if(debugMode) {
         if(cheat.equals("set flagowner 0")) {
            flagowner = 0;
            hasAUX = true;
            AUX = "Flag Owner set to 0";
         }
         else if(cheat.equals("set flagowner 1")) {
            flagowner = 1;
            hasAUX = true;
            AUX = "Flag Owner set to 1";
         }
         else if(cheat.equals("set flagowner 2")) {
            flagowner = 2;
            hasAUX = true;
            AUX = "Flag Owner set to 2";
         }
         else if(cheat.equals("set nightmode 0")) {
            nightMode = false;
            hasAUX = true;
            AUX = "The sun rises once more";
         }
         else if(cheat.equals("set nightmode 1")) {
            nightMode = true;
            hasAUX = true;
            AUX = "Night falls";
         }
         else if(cheat.equals("set revivesunlimited 0")) {
            revivesUnlimited = false;
            hasAUX = true;
            AUX = "Revivesunlimited set to false";
         }
         else if(cheat.equals("set revivesunlimited 1")) {
            revivesUnlimited = true;
            hasAUX = true;
            AUX = "Revivesunlimited set to true";
         }
         else if(cheat.equals("set enemythinking 0")) {
            allowThinking = false;
            hasAUX = true;
            AUX = "allowThinking set to false";
         }
         else if(cheat.equals("set enemythinking 1")) {
            allowThinking = true;
            hasAUX = true;
            AUX = "allowThinking set to true";
         }
         else if(cheat.equals("set gameover 0")) {
            isGameOver = false;
            hasAUX = true;
            AUX = "isGameOver set to false";
         }
         else if(cheat.equals("set gameover 1")) {
            isGameOver = true;
            hasAUX = true;
            AUX = "isGameOver set to true";
         }
         else if(cheat.equals("set kohmode 0")) {
            kohmode = false;
            hasAUX = true;
            AUX = "kohmode set to false";
         }
         else if(cheat.equals("set kohmode 1")) {
            kohmode = true;
            hasAUX = true;
            AUX = "kohmode set to true";
         }
         else if(cheat.equals("set sound 0")) {
            soundOn = false;
            hasAUX = true;
            AUX = "soundOn set to false";
         }
         else if(cheat.equals("set sound 1")) {
            soundOn = true;
            hasAUX = true;
            AUX = "soundOn set to true";
         }
         else if(cheat.equals("set nostalgiatheme 0")) {
            if(loadedmap) {
               hasAUX = true;
               AUX = "access denied";
            }
            else {
               SPRITE_FLOOR = new ImageIcon(getClass().getResource("/krux2/floor.GIF")).getImage();
               SPRITE_STATICB = new ImageIcon(getClass().getResource("/krux2/K_Border1.GIF")).getImage();
               SPRITE_MOBILEB = new ImageIcon(getClass().getResource("/krux2/K_Border.GIF")).getImage();
               SPRITE_BLUEPLAY = new ImageIcon(getClass().getResource("/krux2/player.png")).getImage();
               SPRITE_REDPLAY = new ImageIcon(getClass().getResource("/krux2/enemy.png")).getImage();
               hasAUX = true;
               AUX = "nostalgiatheme set to false";
            }
         }
         else if(cheat.equals("set nostalgiatheme 1")) {
            if(loadedmap) {
               hasAUX = true;
               AUX = "access denied";
            }
            else {
               SPRITE_FLOOR = new ImageIcon(getClass().getResource("/krux3/internal/krux1_floor.PNG")).getImage();
               SPRITE_STATICB = new ImageIcon(getClass().getResource("/krux3/internal/krux1_static.GIF")).getImage();
               SPRITE_MOBILEB = new ImageIcon(getClass().getResource("/krux3/internal/krux1_mobile.GIF")).getImage();
               SPRITE_BLUEPLAY = new ImageIcon(getClass().getResource("/krux3/internal/sprite1.PNG")).getImage();
               SPRITE_REDPLAY = new ImageIcon(getClass().getResource("/krux3/internal/sprite2.PNG")).getImage();
               hasAUX = true;
               AUX = "nostalgiatheme set to true";
            }
         }
         else {
            hasAUX = true;
            AUX = "THOU ART NOT WORTHY";
         }
      }
      
      if(cheat.equals("bot frenzy")) {
         for (int i = 0; i < maxbounds; i++) {
            addAIEnemy((int) (Math.random() * mapsize.x), (int) (Math.random() * mapsize.y));
         }
      }
      else if(cheat.equals("ver")) {
         hasAUX = true;
         AUX = "Krux " + VERSION + " (C) Microtech 2010";
      }
      else if(cheat.equals("kmfver")) {
         hasAUX = true;
         AUX = REVISION;
      }
      else if(cheat.equals("info")) {
         hasAUX = true;
         AUX = "Created by VoaxmasterSpydre 2009 - 2010";
      }
      else if(cheat.equals("build")) {
         hasAUX = true;
         AUX = "BUILD " + BUILD;
      }
      
      printDebugMessage("ChatScreen - Input: " + cheat);
   }
   			
   /**
   *  Krux 2 Method
   * ==================================================================================
   *  loadgame
   *  Loads the main game window and map
   *  Within this method all calls relating to keyboard listener, windows and code
   *  watchers are made
   */		
   public void loadGame() {
      try {
         printDebugMessage("KRUXTOURN - Now Reading HighScore List...");
         BufferedReader in = new BufferedReader(new FileReader("kruxdata.adf"));
         int asdf = 0;
         String gogo = "";
         while ((gogo = in.readLine()) != null) {
            highScoreList[asdf] = gogo;
            asdf++;
         }
         
         String[] hms = tokenizer(highScoreList[0], " ");
         
         hiscorename = hms[0];
         hiscore = Integer.parseInt(hms[1]);
         hiScoreStr.setText("HI$" + hiscore);
      }
      catch (IOException esadfasdf) {
         printErrMeth(esadfasdf, "LOADGAME", false);
         printDebugMessage("Exception: " + esadfasdf.toString() + ": " + esadfasdf.getMessage() + ", Line: 2476");
      }
    
      printDebugMessage("KRUXTOURN - HighScore List Loaded");
    	// Pre-game initializations now taking place
      scoreStr.setText("" + scrboard.score); // Putting score in TextField
      scoreStr.repaint();
    	
   	/* initializing the JProgressBars
   	
   	 * Frankly ever since KRUX 1.2.7 and up the JProgressBar are not drawn and are thus a little vestigial
   	 * but for the complexity of the code and all, I still use them to store the integer values representing
   	 * life and level. Why? 'Coz replacing them with REAL integers would take a lot of reprogramming. Yes, it
   	 * would make the code leaner and more memory efficient, but right now...I JUST DON'T FEEL LIKE RECODING
   	 * THE WHOLE DAMN APPLICATION!
   	 
   	 * Doesn't "int something = curHealthP" work the same as "int something = curHealthP" ?
   	 * It does, but the latter being a whole 11 characters shorter cuts 11 bytes off the overall filesize, 11
   	 * bytes that one can surely tolerate, no?
   	 
   	 * [New Note: 21/10/2010]
   	 * The JProgressBars were finally removed and replaced with actual integers and the performance improvement
   	 * is quite noticable. (PS: I kinda lied about it being a lot of effort, I just used the "Find\Replace" tool
   	 * }=D
      
   	/* Now creating the control panel */ 
      JPanel main = new JPanel();
      main.setLayout(new BoxLayout(main, BoxLayout.X_AXIS)); 
   	
      printDebugMessage("KRUXTOURN - Main Canvas Initialized");
      grid = 
            new JPanel() {
            /* The big cahoona. This is the class that dreams are
            	made of. This baby draws the play area for Krux(c) */
               public void paintComponent(Graphics f) {
                  try {
                     Graphics2D g = (Graphics2D) f;
                  
                     if(showNominal) {
                        g.drawImage(GUI_NOMINAL, 0, 0, this);
                        scrboard.updateCurrent();
                      
                        CGString nom1 = new CGString("" + scrboard.SCORE_RATE, 4);
                        CGString nom2 = new CGString("" + scrboard.KILLS_RATE, 4);
                        CGString nom3 = new CGString("" + scrboard.ITEMS_RATE, 4);
                        CGString nom4 = new CGString("" + scrboard.GEMSH_RATE, 4);
                        CGString nom5 = new CGString("" + scrboard.OVRAL_RATE, 4);
                      
                        g.drawImage(nom1.getDrawnString(), 536, 88, this);
                        g.drawImage(nom2.getDrawnString(), 536, 146, this);
                        g.drawImage(nom3.getDrawnString(), 536, 204, this);
                        g.drawImage(nom4.getDrawnString(), 536, 261, this);
                        g.drawImage(nom5.getDrawnString(), 536, 343, this);
                      
                        AlphaComposite ac = AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER, 0.25f);
                        g.setComposite(ac);
                        g.setColor(Color.GRAY);
                        g.fillRect(16, 120, (int)(603.0f * (scrboard.LSCORE_RATE / 100)), 17);
                        g.fillRect(16, 178, (int)(603.0f * (scrboard.LKILLS_RATE / 100)), 17);
                        g.fillRect(16, 236, (int)(603.0f * (scrboard.LITEMS_RATE / 100)), 17);
                        g.fillRect(16, 294, (int)(603.0f * (scrboard.LGEMSH_RATE / 100)), 17);
                        g.fillRect(16, 375, (int)(603.0f * (scrboard.LOVRAL_RATE / 100)), 17);
                      
                        ac = AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER, 0.5f);
                        g.setComposite(ac);
                        g.setColor(Color.BLACK);
                        g.fillRect(16, 120, (int)(603.0f * (scrboard.SCORE_RATE / 100)), 17);
                        g.fillRect(16, 178, (int)(603.0f * (scrboard.KILLS_RATE / 100)), 17);
                        g.fillRect(16, 236, (int)(603.0f * (scrboard.ITEMS_RATE / 100)), 17);
                        g.fillRect(16, 294, (int)(603.0f * (scrboard.GEMSH_RATE / 100)), 17);
                        g.fillRect(16, 375, (int)(603.0f * (scrboard.OVRAL_RATE / 100)), 17);
                     
                        ac = AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER, 1.0f);
                        g.setComposite(ac);
                        g.setColor(Color.BLUE);
                        g.fillRect(13, 117, (int)(603.0f * (scrboard.SCORE_RATE / 100)), 17);
                        g.fillRect(13, 175, (int)(603.0f * (scrboard.KILLS_RATE / 100)), 17);
                        g.fillRect(13, 233, (int)(603.0f * (scrboard.ITEMS_RATE / 100)), 17);
                        g.fillRect(13, 291, (int)(603.0f * (scrboard.GEMSH_RATE / 100)), 17);
                        g.fillRect(13, 372, (int)(603.0f * (scrboard.OVRAL_RATE / 100)), 17);
                     
                        ac = AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER, 0.5f);
                        g.setComposite(ac);
                        g.setColor(Color.GREEN);
                        g.fillRect(13, 117, (int)(603.0f * (scrboard.LSCORE_RATE / 100)), 17);
                        g.fillRect(13, 175, (int)(603.0f * (scrboard.LKILLS_RATE / 100)), 17);
                        g.fillRect(13, 233, (int)(603.0f * (scrboard.LITEMS_RATE / 100)), 17);
                        g.fillRect(13, 291, (int)(603.0f * (scrboard.LGEMSH_RATE / 100)), 17);
                        g.fillRect(13, 372, (int)(603.0f * (scrboard.LOVRAL_RATE / 100)), 17);
                     
                        ac = AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER, 1.0f);
                        g.setComposite(ac);
                     }
                     else {
                     // Paint Rendering Settings
                     // RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                     // g.setRenderingHints(rh);
                     
                     // draws the grid
                        g.setColor(Color.BLACK);
                        g.fillRect(0,0, 640, 480); // erases the previous frame
                     
                     // Draws the floor
                        for (int x = lxlimit; x < (uxlimit - 1) ; x ++) {
                           for (int y = lylimit; y < (uylimit - 1); y ++) {
                              g.drawImage(SPRITE_FLOOR,x * 30 + 40 + scrollOffset.x, y * 30 + 40 + scrollOffset.y,this);
                           }
                        }
                     
                        for (int i = 0; i < floorsVcnt; i++) {
                           g.setColor(Color.BLACK);
                           if(floorsV[i].x >= lxlimit && floorsV[i].y >= lylimit && floorsV[i].x < uxlimit && floorsV[i].y < uylimit)
                              g.fillRect((floorsV[i].x) * 30 + 40 + scrollOffset.x, (floorsV[i].y) * 30 + 40 + scrollOffset.y, 30, 30);
                        }
                        for (int i = 0; i < floors1cnt; i++) {
                           if(floors1[i].x > lxlimit && floors1[i].y > lylimit && floors1[i].x < uxlimit && floors1[i].y < uylimit)
                              g.drawImage(floortile1, (floors1[i].x) * 30 + 40 + scrollOffset.x, (floors1[i].y) * 30 + 40 + scrollOffset.y, this);
                        }
                        for (int i = 0; i < floors2cnt; i++) {
                           if(floors2[i].x > lxlimit && floors2[i].y > lylimit && floors2[i].x < uxlimit && floors2[i].y < uylimit)
                              g.drawImage(floortile2, (floors2[i].x) * 30 + 40 + scrollOffset.x, (floors2[i].y) * 30 + 40 + scrollOffset.y, this);
                        }
                        for (int i = 0; i < floors3cnt; i++) {
                           if(floors3[i].x > lxlimit && floors3[i].y > lylimit && floors3[i].x < uxlimit && floors3[i].y < uylimit)
                              g.drawImage(floortile3, (floors3[i].x) * 30 + 40 + scrollOffset.x, (floors3[i].y) * 30 + 40 + scrollOffset.y, this);
                        }
                        for (int i = 0; i < floors4cnt; i++) {
                           if(floors4[i].x > lxlimit && floors4[i].y > lylimit && floors4[i].x < uxlimit && floors4[i].y < uylimit)
                              g.drawImage(floortile4, (floors4[i].x) * 30 + 40 + scrollOffset.x, (floors4[i].y) * 30 + 40 + scrollOffset.y, this);
                        }
                        for (int i = 0; i < floors5cnt; i++) {
                           if(floors5[i].x > lxlimit && floors5[i].y > lylimit && floors5[i].x < uxlimit && floors5[i].y < uylimit)
                              g.drawImage(floortile5, (floors5[i].x) * 30 + 40 + scrollOffset.x, (floors5[i].y) * 30 + 40 + scrollOffset.y, this);
                        }
                     
                     // draws the character   
                        if (!isDead && !isGameOver && !u_no_see1) {
                           if(isGhost1) {
                              AlphaComposite ac = AlphaComposite.getInstance(
                                 AlphaComposite.SRC_OVER, 0.66f);
                              g.setComposite(ac);
                           }
                           g.drawImage(SPRITE_BLUEPLAY, 299, 209,this);
                           AlphaComposite ac = AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER, 1.0f);
                           g.setComposite(ac);
                        
                           if (DEATHMATCHMODE) {
                              g.drawImage(OVERLAY_TEAMBLUE, 299, 209,this);
                           }
                        }
                     
                     // draws the player's choice of weapon...
                        if(playerWeapon != -1 && !isDead && !isGameOver && !u_no_see1) {
                           g.drawImage(new ImageIcon(getClass().getResource("/krux2/weapon" + (playerWeapon + 1) + ".png")).getImage(), 294, 204,this);
                        }
                     
                     // draws the enemy
                        if(!u_no_see2) {
                           if(isGhost2) {
                              AlphaComposite ac = AlphaComposite.getInstance(
                                 AlphaComposite.SRC_OVER, 0.66f);
                              g.setComposite(ac);
                           }
                           g.drawImage(SPRITE_REDPLAY,(locationEnemyX * 30) + 5 + 40 + scrollOffset.x,(locationEnemyY * 30) + 5 + 40 + scrollOffset.y,this);
                        
                           AlphaComposite ac = AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER, 1.0f);
                           g.setComposite(ac);
                        
                           if (DEATHMATCHMODE) {
                              g.drawImage(OVERLAY_TEAMRED,(locationEnemyX * 30) + 5 + 40 + scrollOffset.x,(locationEnemyY * 30) + 5 + 40 + scrollOffset.y,this);
                           }
                        }
                     	
                     // draws the enemy's weapon  
                        if(enemyWeapon != -1 && !u_no_see2) {
                           g.drawImage(new ImageIcon(getClass().getResource("/krux2/weapon" + (enemyWeapon + 1) + ".png")).getImage(),(locationEnemyX * 30) + 40 + scrollOffset.x,(locationEnemyY * 30) + 40 + scrollOffset.y,this);
                        }
                     
                     // draw the enemy's poison and zap overlays
                        if (isPoisoned2 && !u_no_see2) {
                           g.drawImage(OVERLAY_POISON,(locationEnemyX * 30) + 40 + scrollOffset.x,(locationEnemyY * 30) + 40 + scrollOffset.y,this);
                        }
                        if (isZapped2 && !u_no_see2) {
                           g.drawImage(OVERLAY_SHOCK,(locationEnemyX * 30) + 40 + scrollOffset.x,(locationEnemyY * 30) + 40 + scrollOffset.y,this);
                        }
                     
                     // draws physical boundaries
                        for(int x = 0; x < maxbounds; x++) {
                           if(bounds[x].x > lxlimit && bounds[x].y > lylimit && bounds[x].x < uxlimit && bounds[x].y < uylimit) {
                              g.drawImage(SPRITE_STATICB,(bounds[x].x - 1) * 30 + 40 + scrollOffset.x,(bounds[x].y - 1) * 30 + 40 + scrollOffset.y,this);
                           }
                        }
                     
                     
                     // draws mobile boundaries
                        for(int x = 0; x < maxbounds; x++) {
                           if(mobileBounds[x].x > lxlimit && mobileBounds[x].y > lylimit && mobileBounds[x].x < uxlimit && mobileBounds[x].y < uylimit) {
                              if(mBoundClass[x] == 's')
                                 g.drawImage(SPRITE_MOBILEB,(mobileBounds[x].x - 1) * 30 + 40 + scrollOffset.x,(mobileBounds[x].y - 1) * 30 + 40 + scrollOffset.y,this);
                              else if (mBoundClass[x] == 'h')
                                 g.drawImage(SPRITE_H_BOUND,(mobileBounds[x].x - 1) * 30 + 40 + scrollOffset.x,(mobileBounds[x].y - 1) * 30 + 40 + scrollOffset.y,this);
                              else
                                 g.drawImage(SPRITE_V_BOUND,(mobileBounds[x].x - 1) * 30 + 40 + scrollOffset.x,(mobileBounds[x].y - 1) * 30 + 40 + scrollOffset.y,this);
                           }
                        }
                     
                     // draws tracking boundaries               
                        for(int x = 0; x < trackingbounds.length; x++) {
                           if(trackingbounds[x].x > lxlimit && trackingbounds[x].y > lylimit && trackingbounds[x].x < uxlimit && trackingbounds[x].y < uylimit) {
                              g.drawImage(OBJECT_HBORDER,(trackingbounds[x].x - 1) * 30 + 40 + scrollOffset.x,(trackingbounds[x].y - 1) * 30 + 40 + scrollOffset.y,this);
                           }
                        }
                     
                     // draws the gems in gemhunter mode
                        if(gemHunterMode) {
                           if(gemType == BLUE_GEM)
                              g.drawImage(OBJECT_BLUEGEM,((gemLocat.x - 1) * 30)+ 40 + scrollOffset.x,((gemLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                           else if(gemType == RED_GEM)
                              g.drawImage(OBJECT_REDGEM,((gemLocat.x - 1) * 30) + 40 + scrollOffset.x,((gemLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                           else if(gemType == GREEN_GEM)
                              g.drawImage(OBJECT_GREENGEM,((gemLocat.x - 1) * 30) + 40 + scrollOffset.x,((gemLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                           else if(gemType == CYAN_GEM)
                              g.drawImage(OBJECT_CYANGEM,((gemLocat.x - 1) * 30) + 40 + scrollOffset.x,((gemLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                           else if(gemType == YELLOW_GEM)
                              g.drawImage(OBJECT_YELLOWGEM,((gemLocat.x - 1) * 30) + 40 + scrollOffset.x,((gemLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                           else
                              ;
                        } 
                     
                     // draw map objects and items
                        if(hpboxtype == 0)                 
                           g.drawImage(ITEM_HEALTH,((healthLocat.x - 1) * 30) + 40 + scrollOffset.x,((healthLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                        else if(hpboxtype == 1)
                           g.drawImage(ITEM_LIFEBOX,((healthLocat.x - 1) * 30) + 40 + scrollOffset.x,((healthLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                        else if(hpboxtype == 2)
                           g.drawImage(ITEM_LIFESUPER,((healthLocat.x - 1) * 30) + 40 + scrollOffset.x,((healthLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                        g.drawImage(ITEM_WEAPON,((weaponboxLocat.x - 1) * 30) + 40 + scrollOffset.x,((weaponboxLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                        g.drawImage(ITEM_MEGAEXP,((megaexpLocat.x - 1) * 30) + 40 + scrollOffset.x,((megaexpLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                        g.drawImage(ITEM_MEGAHP,((megahealthLocat.x - 1) * 30) + 40 + scrollOffset.x,((megahealthLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                        g.drawImage(ITEM_GHOSTPOTION,((ghostpotionLocat.x - 1) * 30) + 40 + scrollOffset.x,((ghostpotionLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                        g.drawImage(ITEM_LIFE,((extraLifeLocat.x - 1) * 30) + 40 + scrollOffset.x,((extraLifeLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                        g.drawImage(ITEM_LEVEL,((levelboxLocat.x - 1) * 30) + 40 + scrollOffset.x,((levelboxLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                        g.drawImage(ITEM_ARMOR,((armorLocat.x - 1) * 30) + 40 + scrollOffset.x,((armorLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                        g.drawImage(ITEM_LAMP,(lampLocat.x - 1) * 30 + 40 + scrollOffset.x,(lampLocat.y - 1) * 30 + 40 + scrollOffset.y,this);
                        g.drawImage(ITEM_UNOSEE,(unoseeLocat.x - 1) * 30 + 40 + scrollOffset.x,(unoseeLocat.y - 1) * 30 + 40 + scrollOffset.y,this);
                        if(painKillType == 0)                 
                           g.drawImage(ITEM_PAINKILLSM,((painkLocat.x - 1) * 30) + 40 + scrollOffset.x,((painkLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                        else
                           g.drawImage(ITEM_PAINKILLLG,((painkLocat.x - 1) * 30) + 40 + scrollOffset.x,((painkLocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                     
                     // draws the C.T.F mode flag
                        if(kohmode) {
                           if(flagowner == 0) {
                              g.drawImage(OBJECT_NULLFLAG,((kohlocat.x - 1) * 30) + 40 + scrollOffset.x,((kohlocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                           }
                           else if (flagowner == 1) {
                              g.drawImage(OBJECT_BLUEFLAG,((kohlocat.x - 1) * 30) + 40 + scrollOffset.x,((kohlocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                           }
                           else if (flagowner == 2) {
                              g.drawImage(OBJECT_REDFLAG,((kohlocat.x - 1) * 30) + 40 + scrollOffset.x,((kohlocat.y - 1) * 30) + 40 + scrollOffset.y,this);
                           }
                        }
                     
                     // draw AIDrivenEnemies
                        for (int i = 0; i < aiEnemies.size(); i++) {
                           Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
                           Point t = aie.getLocat();
                        // draw the non-red enemy
                           if(!aie.u_no_see) {
                              if(aie.ghost) {
                                 AlphaComposite ac = AlphaComposite.getInstance(
                                    AlphaComposite.SRC_OVER, 0.66f);
                                 g.setComposite(ac);
                              }
                              g.drawImage(aie.getSprite(), (t.x * 30) + 45 + scrollOffset.x, (t.y * 30) + 45 + scrollOffset.y,this);
                           
                              AlphaComposite ac = AlphaComposite.getInstance(
                                 AlphaComposite.SRC_OVER, 1.0f);
                              g.setComposite(ac);                  
                           
                           // draw his weapon...
                              if(aie.getWeaponType() != -1) {
                                 g.drawImage(new ImageIcon(getClass().getResource("/krux2/weapon" + (aie.getWeaponType() + 1) + ".png")).getImage(),(t.x * 30) + 40 + scrollOffset.x,(t.y * 30) + 40 + scrollOffset.y,this);
                              }
                           
                              if (DEATHMATCHMODE) {
                                 if(aie.enemyHater)
                                    g.drawImage(OVERLAY_TEAMBLUE,(t.x * 30) + 40 + scrollOffset.x,(t.y * 30) + 40 + scrollOffset.y,this);
                                 else
                                    g.drawImage(OVERLAY_TEAMRED,(t.x * 30) + 40 + scrollOffset.x,(t.y * 30) + 40 + scrollOffset.y,this);
                              }
                           
                           //...and overlays...
                              if (aie.isPoisoned) {
                                 g.drawImage(OVERLAY_POISON,(t.x * 30) + 40 + scrollOffset.x,(t.y * 30) + 40 + scrollOffset.y,this);
                              }
                              if (aie.isZapped) {
                                 g.drawImage(OVERLAY_SHOCK,(t.x * 30) + 40 + scrollOffset.x,(t.y * 30) + 40 + scrollOffset.y,this);
                              }
                           
                           // draw his eyes, cute!
                              int eyesT = aie.getEyes();
                              Image img = aie.getCustomEyes(eyesT);
                           
                              if(img != null) {
                                 g.drawImage(img, (t.x * 30) + 40 + scrollOffset.x, (t.y * 30) + 40 + scrollOffset.y,this);
                              }
                              else {
                                 if (eyesT == 0) {
                                    g.drawImage(SPRITE_EYESUP, (t.x * 30) + 40 + scrollOffset.x, (t.y * 30) + 40 + scrollOffset.y,this);
                                 }
                                 else if (eyesT == 1) {
                                    g.drawImage(SPRITE_EYESDOWN, (t.x * 30) + 40 + scrollOffset.x, (t.y * 30) + 40 + scrollOffset.y,this);
                                 } 
                                 else if (eyesT == 2) {
                                    g.drawImage(SPRITE_EYESLEFT, (t.x * 30) + 40 + scrollOffset.x, (t.y * 30) + 40 + scrollOffset.y,this);
                                 }   
                                 else if (eyesT == 3) {
                                    g.drawImage(SPRITE_EYESRIGHT, (t.x * 30) + 40 + scrollOffset.x, (t.y * 30) + 40 + scrollOffset.y,this);
                                 }
                              }
                           }
                           
                        // variable controls, added to prevent bad looking variable over-runs
                           float hpRatio = (float)aie.life / maxHealthE; 
                           float armRatio = (float)aie.armor / 200;
                           float expRatio = (float)(aie.exp_E - aie.expLast_E) / (aie.expForNext_E  - aie.expLast_E);
                           
                           if(hpRatio > 1)
                              hpRatio = 1.0f;
                           if(armRatio > 1)
                              armRatio = 1.0f;
                           if(expRatio > 1)
                              expRatio = 1.0f;
                        
                        // ...and status information.                        
                           if(aie.expDrawn && aie.hpDrawn) {
                              g.drawImage(OVERLAY_STATBALLOON, (t.x * 30) + 40 + scrollOffset.x,(t.y * 30) + 10 + scrollOffset.y, this);
                           
                              g.setColor(Color.BLACK);
                              g.drawString("" + aie.enemyLevel, (t.x * 30) + 42 + scrollOffset.x,(t.y * 30) + 23 + scrollOffset.y);
                           
                              Image LifeLevel = null;
                              if(((float)aie.life / aie.maxHealthE) > 0.5f)
                                 LifeLevel = OVERLAY_LIFEBARH;
                              else if(((float)aie.life / aie.maxHealthE) > 0.66f)
                                 LifeLevel = OVERLAY_LIFEBARM;
                              else
                                 LifeLevel = OVERLAY_LIFEBAR;
                           
                           // ...the rest of the drawing code
                              g.drawImage(LifeLevel, (t.x * 30) + 47 + scrollOffset.x,(t.y * 30) + 24 + scrollOffset.y, (int)((float) 21 * (hpRatio)), 3, this);
                              g.drawImage(OVERLAY_ARMORBAR, (t.x * 30) + 47 + scrollOffset.x,(t.y * 30) + 25 + scrollOffset.y, (int)((float) 21 * (armRatio)), 2, this);
                              g.drawImage(OVERLAY_EXPBAR, (t.x * 30) + 47 + scrollOffset.x,(t.y * 30) + 30 + scrollOffset.y, (int)((float) 21 * (expRatio)), 3, this);
                           }
                        
                           if(aie.expDrawn && !aie.hpDrawn) {
                              g.drawImage(OVERLAY_EXPBALLOON, (t.x * 30) + 40 + scrollOffset.x,(t.y * 30) + 10 + scrollOffset.y, this);
                           
                              g.setColor(Color.BLACK);
                              g.drawString("" + enemyLevel, (t.x * 30) + 42 + scrollOffset.x,(t.y * 30) + 28 + scrollOffset.y);
                           
                              g.drawImage(OVERLAY_EXPBAR, (t.x * 30) + 47 + scrollOffset.x,(t.y * 30) + 30 + scrollOffset.y, (int)(expRatio), 3, this);
                           }
                        
                        
                           if(aie.hpDrawn && !aie.expDrawn) {
                              g.drawImage(OVERLAY_HPBALLOON, (t.x * 30) + 40 + scrollOffset.x,(t.y * 30) + 10 + scrollOffset.y, this);
                           
                              if(aie.hasMegaHP) {
                                 g.drawImage(OVERLAY_HEALING, (t.x * 30) + 5 + 40 + scrollOffset.x,(t.y * 30) + 5 + 40 + scrollOffset.y,this);
                              }
                           
                              Image LifeLevel = null;
                              if(((float)aie.life / aie.maxHealthE) > 0.66f)
                                 LifeLevel = OVERLAY_LIFEBARH;
                              else if(((float)aie.life / aie.maxHealthE) > 0.33f)
                                 LifeLevel = OVERLAY_LIFEBARM;
                              else
                                 LifeLevel = OVERLAY_LIFEBAR;
                           
                              g.drawImage(LifeLevel, (t.x * 30) + 47 + scrollOffset.x,(t.y * 30) + 30 + scrollOffset.y, (int)((float) 21 * hpRatio), 3, this);
                              g.drawImage(OVERLAY_ARMORBAR, (t.x * 30) + 47 + scrollOffset.x,(t.y * 30) + 31 + scrollOffset.y, (int)((float) 21 * armRatio), 2, this);
                           }
                        }
                     
                     /*
                     *   Krux 3 additional drawing calls
                     */
                     
                        if(hasMegaExp) {
                           g.drawImage(new ImageIcon(getClass().getResource("/krux3/empty_e.png")).getImage(), 5, 50,this);
                        }
                     
                        if (!isDead && !isGameOver) {
                           if (eyesP == 0) {
                              g.drawImage(SPRITE_EYESUP, 295, 204,this);
                           }
                           else if (eyesP == 1) {
                              g.drawImage(SPRITE_EYESDOWN, 295, 204,this);
                           } 
                           else if (eyesP == 2) {
                              g.drawImage(SPRITE_EYESLEFT, 295, 204,this);
                           }   
                           else if (eyesP == 3) {
                              g.drawImage(SPRITE_EYESRIGHT, 295, 204,this);
                           }
                        }
                     
                        if (!u_no_see2) {
                           if (eyesE == 0) {
                              g.drawImage(SPRITE_EYESUP,(locationEnemyX * 30) + 40 + scrollOffset.x,(locationEnemyY * 30) + 40 + scrollOffset.y,this);
                           }
                           else if (eyesE == 1) {
                              g.drawImage(SPRITE_EYESDOWN,(locationEnemyX * 30) + 40 + scrollOffset.x,(locationEnemyY * 30) + 40 + scrollOffset.y,this);
                           } 
                           else if (eyesE == 2) {
                              g.drawImage(SPRITE_EYESLEFT,(locationEnemyX * 30) + 40 + scrollOffset.x,(locationEnemyY * 30) + 40 + scrollOffset.y,this);
                           }   
                           else if (eyesE == 3) {
                              g.drawImage(SPRITE_EYESRIGHT,(locationEnemyX * 30) + 40 + scrollOffset.x,(locationEnemyY * 30) + 40 + scrollOffset.y,this);
                           }
                        }
                     
                        g.setColor(Color.BLACK);
                        g.fillRect(0, 0, 640, 24);
                        g.fillRect(0, 450 - 24, 640, 24);
                              
                        g.drawImage(OVERLAY_HPBAR,0,0,this);
                     
                        if (isPoisoned1 && !isGameOver) {
                           g.drawImage(OVERLAY_POISON, 294, 204,this);
                        }
                     
                        if (isZapped1 && !isGameOver) {
                           g.drawImage(OVERLAY_SHOCK, 294, 204,this);
                        }
                     
                        if(hasMegaHP) {
                           if (!isDead && !isGameOver) {
                              g.drawImage(OVERLAY_HEALING, 299, 209,this);
                           }
                        }
                        else
                        
                        // Life and armour bars
                           g.setColor(Color.BLACK);
                        int goose = (int)((float) 222 * ((float)curHealthP / maxHealthP));
                        g.fillRect(20 + (222 - goose), 3, goose, 7);
                        g.setColor(Color.BLACK);
                        g.fillRect(388, 3, (int)((float) 222 * ((float)armorP1 / 200)), 7);
                     
                     // Level Bar
                     // This is no longer drawn as of Krux 3 RTS X in favor of a numeric representation           
                     // g.fillRect(37, 11, (int)((float) 70 * ((float)levelPlayer / levelMax)), 7);
                     
                     // Experience Bar
                        int goose2 = (int)((float) 222 * ((float) (exp - expLast) / (expForNext  - expLast)));
                        g.fillRect(20 + (222 - goose2), 13, goose2, 2);
                     
                     // Weapon Bar
                        if (playerWeapon != -1) {
                           g.fillRect(388, 13, (int)((float) 222 * ((float)pWeapLeft) / pWeapUses), 2);
                        }
                     
                        if(!revivesUnlimited) {
                           g.drawImage(new CGString("" + (revivelimit - revive), 3, CGString.ALIGN_RIGHT, CGString.DIGITAL).getDrawnString(), 352, 6, this);
                        }
                     
                        g.drawImage(new CGString(String.valueOf(levelPlayer), 3, CGString.ALIGN_RIGHT, CGString.DIGITAL).getDrawnString(), 259, 6, this);
                     
                        g.drawImage(scoreStr.getDrawnString(), 288, 6, this);
                        g.drawImage(hiScoreStr.getDrawnString(), 10, 450 - 22, this);
                     
                        if (nightMode && !hasLamp) {
                           g.drawImage(OVERLAY_NIGHT, 0, 24, this);
                        }
                        else if(nightMode && hasLamp) {
                           g.drawImage(OVERLAY_NIGHTLAMP, 0, 24, this);
                        }
                     
                        if(kohmode || gemHunterMode || u_no_see1 || isGhost1) {
                           timerStr = new CGString(kohtimer.getText(), 4, CGString.ALIGN_RIGHT, CGString.DIGITAL);
                           g.drawImage(OVERLAY_TIMER, 293, 21, this);
                           g.drawImage(timerStr.getDrawnString(), 302, 28, this);
                        }
                     
                        if(isDead) {
                           spawnStr.setText("Press R to respawn");
                           g.drawImage(spawnStr.getDrawnString(), 630 / 2 - 108, 450 / 2 - 6, this);
                        }
                     
                        if(isGameOver) {
                           g.drawImage(goStr.getDrawnString(), 630 / 2 - 54, 450 / 2 - 6, this);
                        }
                     
                        if(enteringCheat) {
                           g.setColor(Color.BLACK);
                           g.fillRect(0, 450 - 39, 640, 17);
                           cheatStr.setText("Code$ " + CHEAT + "_");
                           g.drawImage(cheatStr.getDrawnString(), 10, 450 - 39, this);
                        }
                     
                        if(enteringHS) {
                           g.setColor(Color.BLACK);
                           g.fillRect(0, 450 - 39, 640, 17);
                           cheatStr.setText("Initials$ " + scoreInitial + "_");
                           g.drawImage(cheatStr.getDrawnString(), 10, 450 - 39, this);
                        }
                     
                        if(enteringPredef) {
                           g.setColor(Color.BLACK);
                           g.fillRect(0, 450 - 39, 640, 17);
                           cheatStr.setText("PredefKey$ " + predefKey + "_");
                           g.drawImage(cheatStr.getDrawnString(), 10, 450 - 39, this);
                        }
                     
                        if((mapsize.x * mapsize.y) > 144 && nightMode && sense) {                
                           doRadarSense(g);
                        }
                     
                        if(hasAUX && !enteringCheat) {
                           g.setColor(Color.BLACK);
                           g.fillRect(0, 450 - 39, 640, 17);
                           cheatStr.setText(AUX);
                           g.drawImage(cheatStr.getDrawnString(), 10, 450 - 39, this);
                           redrawAUX++;
                           if (redrawAUX == 25) {
                              redrawAUX = 0;
                              hasAUX = false;
                           }
                        }
                     
                        if(damagePlayerPaint && !isDead) {
                           g.drawImage(OVERLAY_HPBALLOON, (locationPlayerX * 30) + 40 + scrollOffset.x,(locationPlayerY * 30) + 10 + scrollOffset.y, this);
                        
                           g.setColor(Color.RED);
                           g.drawString("-" + damageP, (locationPlayerX * 30) + 60 + scrollOffset.x,(locationPlayerY * 30) + 19 + scrollOffset.y);
                           g.drawString("-" + damageP, (locationPlayerX * 30) + 61 + scrollOffset.x,(locationPlayerY * 30) + 19 + scrollOffset.y);
                           redrawsP++;
                        // New code added
                        
                           Image LifeLevel = null;
                           if(((float)curHealthP / maxHealthP) > 0.66f)
                              LifeLevel = OVERLAY_LIFEBARH;
                           else if(((float)curHealthP / maxHealthP) > 0.33f)
                              LifeLevel = OVERLAY_LIFEBARM;
                           else
                              LifeLevel = OVERLAY_LIFEBAR;
                        
                           g.drawImage(LifeLevel, (locationPlayerX * 30) + 47 + scrollOffset.x,(locationPlayerY * 30) + 30 + scrollOffset.y, (int)((float) 21 * ((float)curHealthP / maxHealthP)), 3, this);
                           g.drawImage(OVERLAY_ARMORBAR, (locationPlayerX * 30) + 47 + scrollOffset.x,(locationPlayerY * 30) + 31 + scrollOffset.y, (int)((float) 21 * ((float)armorP1 / 200)), 2, this);
                        
                           if (redrawsP == 15) {
                              redrawsP = 0;
                              damagePlayerPaint = false;
                           }
                        }
                     
                        if(frenzymult > 1) {
                           g.setColor(Color.YELLOW);
                           g.drawString("x" + frenzymult, (locationPlayerX * 30) + 5 + 50 + scrollOffset.x,(locationPlayerY * 30) + 35 + scrollOffset.y);
                           g.drawString("x" + frenzymult, (locationPlayerX * 30) + 5 + 51 + scrollOffset.x,(locationPlayerY * 30) + 35 + scrollOffset.y);
                        }
                     
                        if(expDrawn && hpDrawn) {
                           g.drawImage(OVERLAY_STATBALLOON, (locationEnemyX * 30) + 40 + scrollOffset.x,(locationEnemyY * 30) + 10 + scrollOffset.y, this);
                        
                           g.setColor(Color.BLACK);
                           g.drawString("" + enemyLevel, (locationEnemyX * 30) + 42 + scrollOffset.x,(locationEnemyY * 30) + 23 + scrollOffset.y);
                        
                           Image LifeLevel = null;
                           if(((float)curHealthE / maxHealthE) > 0.5f)
                              LifeLevel = OVERLAY_LIFEBARH;
                           else if(((float)curHealthE / maxHealthE) > 0.66f)
                              LifeLevel = OVERLAY_LIFEBARM;
                           else
                              LifeLevel = OVERLAY_LIFEBAR;
                        
                           g.drawImage(LifeLevel, (locationEnemyX * 30) + 47 + scrollOffset.x,(locationEnemyY * 30) + 24 + scrollOffset.y, (int)((float) 21 * ((float)curHealthE / maxHealthE)), 3, this);
                           g.drawImage(OVERLAY_ARMORBAR, (locationEnemyX * 30) + 47 + scrollOffset.x,(locationEnemyY * 30) + 25 + scrollOffset.y, (int)((float) 21 * ((float)armorP2 / 200)), 2, this);
                           g.drawImage(OVERLAY_EXPBAR, (locationEnemyX * 30) + 47 + scrollOffset.x,(locationEnemyY * 30) + 30 + scrollOffset.y, (int)((float) 21 * ((float) (exp_E - expLast_E) / (expForNext_E  - expLast_E))), 3, this);
                        }
                     
                        if(expDrawn && !hpDrawn) {
                           g.drawImage(OVERLAY_EXPBALLOON, (locationEnemyX * 30) + 40 + scrollOffset.x,(locationEnemyY * 30) + 10 + scrollOffset.y, this);
                        
                           g.setColor(Color.BLACK);
                           g.drawString("" + enemyLevel, (locationEnemyX * 30) + 42 + scrollOffset.x,(locationEnemyY * 30) + 28 + scrollOffset.y);
                        
                           g.drawImage(OVERLAY_EXPBAR, (locationEnemyX * 30) + 47 + scrollOffset.x,(locationEnemyY * 30) + 30 + scrollOffset.y, (int)((float) 21 * ((float) (exp_E - expLast_E) / (expForNext_E  - expLast_E))), 3, this);
                        }
                     
                     
                        if(hpDrawn && !expDrawn) {
                           g.drawImage(OVERLAY_HPBALLOON, (locationEnemyX * 30) + 40 + scrollOffset.x,(locationEnemyY * 30) + 10 + scrollOffset.y, this);
                        
                           if(hasMegaHP_E) {
                              g.drawImage(OVERLAY_HEALING, (locationEnemyX * 30) + 5 + 40 + scrollOffset.x,(locationEnemyY * 30) + 5 + 40 + scrollOffset.y,this);
                           }
                        
                           Image LifeLevel = null;
                           if(((float)curHealthE / maxHealthE) > 0.66f)
                              LifeLevel = OVERLAY_LIFEBARH;
                           else if(((float)curHealthE / maxHealthE) > 0.33f)
                              LifeLevel = OVERLAY_LIFEBARM;
                           else
                              LifeLevel = OVERLAY_LIFEBAR;
                        
                           g.drawImage(LifeLevel, (locationEnemyX * 30) + 47 + scrollOffset.x,(locationEnemyY * 30) + 30 + scrollOffset.y, (int)((float) 21 * ((float)curHealthE / maxHealthE)), 3, this);
                           g.drawImage(OVERLAY_ARMORBAR, (locationEnemyX * 30) + 47 + scrollOffset.x,(locationEnemyY * 30) + 31 + scrollOffset.y, (int)((float) 21 * ((float)armorP2 / 200)), 2, this);
                        }
                     
                        if(damageEnemyPaint) {
                           g.setColor(Color.RED);
                           g.drawString("-" + enemyLevel, (locationEnemyX * 30) + 60 + scrollOffset.x,(locationEnemyY * 30) + 19 + scrollOffset.y);
                        
                           redrawsE++;
                           if (redrawsE == 10) {
                              redrawsE = 0;
                              damageEnemyPaint = false;
                           }
                        }
                     
                        if(gemHunterMode) {
                           g.drawImage(OVERLAY_GEMHUNT, 253, 21, this);
                           g.drawImage(gems1.getDrawnString(), 276, 28, this);
                           g.drawImage(gems2.getDrawnString(), 335, 28, this);
                        }
                     
                     // Code for drawing the status overlay
                        if(statusBoxDrawn) {
                           g.drawImage(OVERLAY_STATUS, 462, 20,this);
                           g.setColor(Color.BLACK);
                           g.fillRect(501, 67, (int)((float) 75 * ((float) pWeapLeft / pWeapUses)), 7);
                           g.fillRect(501, 54, (int)((float) 75 * ((float)levelPlayer / levelMax)), 7);
                           g.fillRect(501, 41, (int)((float) 75 * ((float)curHealthP / maxHealthP)), 7);
                           g.fillRect(501, 114, (int)((float) 75 * ((float) (exp - expLast) / (expForNext  - expLast))), 7);
                           g.fillRect(501, 85, (int)((float) 75 * ((float) strengthP1 / 100)), 7);
                           g.fillRect(501, 98, (int)((float) 75 * ((float) enduranceP1 / 100)), 7);
                        
                           g.drawImage(scoreStr.getDrawnString(), 567, 26, this);
                           g.drawImage(new CGString("" + curHealthP, 6, CGString.ALIGN_RIGHT, CGString.DIGITAL).getDrawnString(), 581, 39, this);
                           g.drawImage(new CGString("" + levelPlayer, 6, CGString.ALIGN_RIGHT, CGString.DIGITAL).getDrawnString(), 581, 52, this);
                           g.drawImage(new CGString("" + pWeapLeft, 6, CGString.ALIGN_RIGHT, CGString.DIGITAL).getDrawnString(), 581, 65, this);
                        
                           g.drawImage(new CGString("" + strengthP1, 4, CGString.ALIGN_RIGHT, CGString.DIGITAL).getDrawnString(), 595, 83, this);
                           g.drawImage(new CGString("" + enduranceP1, 4, CGString.ALIGN_RIGHT, CGString.DIGITAL).getDrawnString(), 595, 96, this);
                        
                           g.drawImage(new CGString("" + (expForNext - exp), 6, CGString.ALIGN_RIGHT, CGString.DIGITAL).getDrawnString(), 581, 112, this);
                        }
                     
                        if(kohmode) {
                           g.drawImage(OVERLAY_FLAGHUNT, 280, 21, this);
                           if (flagowner == 1) {
                              g.drawImage(GUI_FLAG1, 287, 27, this);
                           }
                           else if (flagowner == 2) {
                              g.drawImage(GUI_FLAG2, 333, 27, this);
                           }
                        }
                     
                        if(debugMode) {
                           g.setColor(Color.BLACK);
                           g.drawString("== Developer Mode ==", 11, 36);
                           g.drawString("Krux Series 3, Version " + VERSION + " Build " + BUILD, 11, 51);
                           g.drawString("== Key Guide ==" , 11, 81);
                           g.drawString("[1]   View Player Life" , 11, 96);
                           g.drawString("[2]   View Player Level" , 11, 111);
                           g.drawString("[3]   View Player Weapon" , 11, 126);
                           g.drawString("[4]   View Enemy Life" , 11, 141);
                           g.drawString("[5]   View Enemy Level" , 11, 156);
                           g.drawString("[6]   View Enemy Weapon" , 11, 171);
                           g.drawString("[7]   View Player Positional" , 11, 186);
                           g.drawString("[8]   View Enemy Positional" , 11, 201);
                           g.drawString("[9]   View C.T.F Flag Positional" , 11, 216);
                           g.drawString("[0]   View OBERON AI Target" , 11, 231);
                           g.drawString("== Player Monitor ==" , 11, 260);
                           g.drawString("Blue Player Positional: (" + locationPlayerX + " , " + locationPlayerY + ")" , 11, 276);
                           g.drawString("Blue Player Life Value: (" + curHealthP + " /" + maxHealthP + ") (" + armorP1 + ")" , 11, 291);
                           g.drawString("Red Player Positional:  (" + locationEnemyX + " , " + locationEnemyY + ")" , 11, 306);
                           g.drawString("Red Player Life Value:  (" + curHealthE + " /" + maxHealthE + ") (" + armorP2 + ")" , 11, 321);
                           g.setColor(Color.YELLOW);
                           g.drawString("== Developer Mode ==", 10, 35);
                           g.drawString("Krux Series 3, Version " + VERSION + " Build " + BUILD, 10, 50);
                           g.drawString("== Key Guide ==" , 10, 80);
                           g.drawString("[1]   View Player Life" , 10, 95);
                           g.drawString("[2]   View Player Level" , 10, 110);
                           g.drawString("[3]   View Player Weapon" , 10, 125);
                           g.drawString("[4]   View Enemy Life" , 10, 140);
                           g.drawString("[5]   View Enemy Level" , 10, 155);
                           g.drawString("[6]   View Enemy Weapon" , 10, 170);
                           g.drawString("[7]   View Player Positional" , 10, 185);
                           g.drawString("[8]   View Enemy Positional" , 10, 200);
                           g.drawString("[9]   View C.T.F Flag Positional" , 10, 215);
                           g.drawString("[0]   View OBERON AI Target" , 10, 230);
                           g.drawString("== Player Monitor ==" , 10, 260);
                           g.drawString("Blue Player Positional: (" + locationPlayerX + " , " + locationPlayerY + ")" , 10, 275);
                           g.drawString("Blue Player Life Value: (" + curHealthP + " /" + maxHealthP + ") (" + armorP1 + ")" , 10, 290);
                           g.drawString("Red Player Positional:  (" + locationEnemyX + " , " + locationEnemyY + ")" , 10, 305);
                           g.drawString("Red Player Life Value:  (" + curHealthE + " /" + maxHealthE + ") (" + armorP2 + ")" , 10, 320);
                        }
                     
                        if(showHighScores) {
                           g.setColor(Color.BLACK);
                           g.drawImage(OVERLAY_SCORES,156, 180, this);
                           g.drawImage(hs0.getDrawnString(), 232, 184, this);
                           g.drawImage(hs1.getDrawnString(), 160, 216, this);
                           g.drawImage(hs2.getDrawnString(), 160, 232, this);
                           g.drawImage(hs3.getDrawnString(), 160, 248, this);
                           g.drawImage(hs4.getDrawnString(), 160, 264, this);
                           g.drawImage(hs5.getDrawnString(), 160, 280, this);
                        }
                     }
                  }
                  catch (Exception e) {
                     e.printStackTrace();
                     printDebugMessage("DEBUGMODE - EXCEPTION: " + e.getMessage() + ", Line: 3186");
                  }
               }
            };
         
      grid.setPreferredSize(new Dimension((mapsize.x + 1) * 30 + 40,(mapsize.y + 1) * 30 + 40)); // Sets the recommended size for the Play area
      grid.setBackground(Color.BLACK);
   
      controlThread 		= new Thread(this); // This game's extra Thread
   	
   	// Here we go boys! The main frame is about to be made
      mainwin = implementWindow("Krux Tourno", grid, new ImageIcon(getClass().getResource("/krux/icosml.gif")), 640, 480, false);
      
      if(fullScreen) {
         printDebugMessage("KRUXTOURN - Now Rendering in FSE Mode");
         device.setFullScreenWindow(mainwin);
         device.setDisplayMode(new DisplayMode(640, 480, 16, 70));
      }
      
      /*
      * This is the intricate key-listening interface for the Krux 3 UI.
      * It uses a rather large amount of conditionals to make sure that it's doing the right thing at the right time.
      */
      printDebugMessage("KRUXTOURN - Initializing Keyboard Listeners");
      mainwin.addKeyListener(
               new KeyAdapter() {
               // Using KeyAdapter instead of KeyListener as we are not using all the possible method overrides
                  public void keyPressed(KeyEvent e) {
                     int codes = e.getKeyCode();
                  
                     if(enteringCheat) { // Check if the text I am entering should be used as a cheat code or otherwise
                        if (codes == KeyEvent.VK_ENTER) {
                           System.out.println("Entered: " + CHEAT);
                           doCheat(CHEAT.toLowerCase());            // Perform the coded action
                           CHEAT = "";                              // Reset to variable
                           enteringCheat = false;                   // Terminate to code entering interface
                        }
                        else if (codes == KeyEvent.VK_BACK_SPACE) { // I need to add a special handle for backspace
                           try {                                    // or it gets parsed as a non-existing character
                              char[] temph = CHEAT.toCharArray();
                              int cnth = temph.length - 1;          // reduce the lenght of the character string by one
                              char[] tempg = new char[cnth];
                           
                              for (int i = 0; i < cnth; i++) {
                                 tempg[i] = temph[i];                // reallocate the text to the array
                              }
                           
                              CHEAT = new String(tempg);             // reassign it to the String
                                                   // repaint the main window
                           }
                           catch (Throwable efc) {
                              printDebugMessage("Exception: " + efc.toString() + ": " + efc.getMessage() + ", Line: 3237");
                           }
                        }
                        else if (codes == KeyEvent.VK_SHIFT) {       // Pressing SHIFT is totally ambigious...
                        }                                            // ...the UI always displays all-caps
                        else {
                           CHEAT += e.getKeyChar();                  // Append the entered character to the Code String
                                                   // The ever present repaint
                        }
                     }
                     else if(enteringHS) { // Check if the text I am entering should be used as a highscore or otherwise
                        if (codes == KeyEvent.VK_ENTER) {
                           updateHighScore(scorePosi);
                           scoreInitial = "";                       // Reset to variable
                           enteringHS = false;                      // Terminate to code entering interface
                        }
                        else if (codes == KeyEvent.VK_BACK_SPACE) { // I need to add a special handle for backspace
                           try {                                    // or it gets parsed as a non-existing character
                              char[] temph =  scoreInitial.toCharArray();
                              int cnth = temph.length - 1;          // reduce the lenght of the character string by one
                              char[] tempg = new char[cnth];
                           
                              for (int i = 0; i < cnth; i++) {
                                 tempg[i] = temph[i];                // reallocate the text to the array
                              }
                           
                              scoreInitial = new String(tempg);             // reassign it to the String
                                                   // repaint the main window
                           }
                           catch (Throwable efc) {
                              printDebugMessage("Exception: " + efc.toString() + ": " + efc.getMessage() + ", Line: 3267");
                           }
                        }
                        else if (codes == KeyEvent.VK_SHIFT) {       // Pressing SHIFT is totally ambigious...
                        }                                            // ...the UI always displays all-caps
                        else if (scoreInitial.length() == 3) {
                        }                                          
                        else {
                           scoreInitial += e.getKeyChar();                  // Append the entered character to the Code String
                                                   // The ever present repaint
                        }
                     }
                     else if(enteringPredef) { // Check if the text I am entering should be used as a predefined enemy code
                        if (codes == KeyEvent.VK_ENTER) {
                           addPredef(predefKey);
                           enteringPredef = false;                   // Terminate to code entering interface
                        }
                        else if (codes == KeyEvent.VK_BACK_SPACE) { // I need to add a special handle for backspace
                           try {                                    // or it gets parsed as a non-existing character
                              char[] temph =  predefKey.toCharArray();
                              int cnth = temph.length - 1;          // reduce the lenght of the character string by one
                              char[] tempg = new char[cnth];
                           
                              for (int i = 0; i < cnth; i++) {
                                 tempg[i] = temph[i];                // reallocate the text to the array
                              }
                           
                              predefKey = new String(tempg);             // reassign it to the String
                                                   // repaint the main window
                           }
                           catch (Throwable efc) {
                              printDebugMessage("Exception: " + efc.toString() + ": " + efc.getMessage() + ", Line: 3298");
                           }
                        }
                        else if (codes == KeyEvent.VK_SHIFT) {       // Pressing SHIFT is totally ambigious...
                        }                                            // ...the UI always displays all-caps
                        else if (predefKey.length() == 8) {
                        }                                          
                        else {
                           predefKey += e.getKeyChar();                  // Append the entered character to the Code String
                                                   // The ever present repaint
                        }
                     }
                     else {
                        if(!isPause && !isDead && !isGameOver && !isZapped1) {                    // Make sure that the game is not paused...
                           switch (codes) {                          // ...or that I'm not dead!
                              case KeyEvent.VK_UP: 
                                 {
                                    eyesP = 0;
                                    restrict = false;                      // Default the restriction variable
                                    if (locationPlayerY == 0) {            // If I am against the edge of the map, I can't move
                                    }
                                    else if ((locationPlayerY - 1) == locationEnemyY && locationPlayerX == locationEnemyX) {
                                    }                                      // If I am against the enemy or the third guy, I can't move
                                    else {
                                       if (!testBounds(0)) {              // Call the bound-testing method to see if my move is valid
                                          locationPlayerY--;              // If I am NOT restricted, I can move
                                       
                                       }
                                    }
                                    break;
                                 }
                              case KeyEvent.VK_DOWN: 
                                 {
                                    eyesP = 1;
                                    restrict = false;
                                    if (locationPlayerY == (mapsize.y - 1)) {
                                    }
                                    else if ((locationPlayerY + 1) == locationEnemyY && locationPlayerX == locationEnemyX) {
                                    }
                                    else {
                                       if (!testBounds(1)) {
                                          locationPlayerY++;
                                       
                                       }
                                    }
                                    break;
                                 }
                              case KeyEvent.VK_LEFT: 
                                 {
                                    eyesP = 2;
                                    restrict = false;
                                    if (locationPlayerX == 0) {
                                    }
                                    else if ((locationPlayerX - 1) == locationEnemyX && locationPlayerY == locationEnemyY) {
                                    }
                                    else {
                                       if (!testBounds(2)) {
                                          locationPlayerX--;
                                       
                                       }
                                    }
                                    break;
                                 }
                              case KeyEvent.VK_RIGHT: 
                                 {
                                    eyesP = 3;
                                    restrict = false;
                                    if (locationPlayerX == (mapsize.x - 1)) {
                                    }
                                    else if ((locationPlayerX + 1) == locationEnemyX && locationPlayerY == locationEnemyY) {
                                    }
                                    else {
                                       if (!testBounds(3)) {
                                          locationPlayerX++;
                                       
                                       }
                                    }
                                    break;
                                 }
                              case KeyEvent.VK_SPACE: 
                                 {
                                    if ((locationPlayerX + 1) == locationEnemyX && locationPlayerY == locationEnemyY) {
                                       doDamagePE();
                                    }
                                    else if ((locationPlayerX - 1) == locationEnemyX && locationPlayerY == locationEnemyY) {
                                       doDamagePE();
                                    }
                                    else if ((locationPlayerY + 1) == locationEnemyY && locationPlayerX == locationEnemyX) {
                                       doDamagePE();
                                    }
                                    else if ((locationPlayerY - 1) == locationEnemyY && locationPlayerX == locationEnemyX) {
                                       doDamagePE();
                                    }
                                    else {
                                       for (int i = 0; i < aiEnemies.size(); i++) {
                                          Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
                                          Point typer = aie.getLocat();
                                       
                                          if ((locationPlayerX + 1) == typer.x && locationPlayerY == typer.y) {
                                             if(aie.getHit()) {
                                                try {
                                                   exp += EXPGained(((Oberon_AIDrivenEnemy) aiEnemies.elementAt(i)).enemyLevel, playerLevel);
                                                   if(extremeRules)
                                                      scrboard.score += (ENEMY_DEFEAT * aie.enemyLevel * 2 * (int) frenzymult);
                                                   else
                                                      scrboard.score += (ENEMY_DEFEAT * aie.enemyLevel * (int) frenzymult);
                                                
                                                   scoreStr.setText("" + scrboard.score);
                                                   aiEnemies.removeElementAt(i);printDebugMessage("OBERONAI - DESTRUCTOR: " + "Vector Size: " + aiEnemies.size()); 
                                                   kills++;
                                                   lastkill = gametimer;
                                                }
                                                catch (Exception f) {
                                                   hasAUX = true;
                                                   AUX = "Something sinistar happened!";
                                                   printDebugMessage("Exception: " + f.toString() + ": " + f.getMessage() + ", Line: 3414");
                                                }
                                             }
                                             else {
                                                if(extremeRules)
                                                   scrboard.score += HIT * 2;
                                                else
                                                   scrboard.score += HIT;
                                             
                                                scoreStr.setText("" + scrboard.score);
                                             }
                                          }
                                          else if ((locationPlayerX - 1) == typer.x && locationPlayerY == typer.y) {
                                             if(aie.getHit()) {
                                                try {
                                                   exp += EXPGained(((Oberon_AIDrivenEnemy) aiEnemies.elementAt(i)).enemyLevel, playerLevel);
                                                   if(extremeRules)
                                                      scrboard.score += (ENEMY_DEFEAT * aie.enemyLevel * 2);
                                                   else
                                                      scrboard.score += (ENEMY_DEFEAT * aie.enemyLevel * (int) frenzymult);
                                                
                                                   scoreStr.setText("" + scrboard.score);
                                                   aiEnemies.removeElementAt(i);printDebugMessage("OBERONAI - DESTRUCTOR: " + "Vector Size: " + aiEnemies.size()); 
                                                   kills++;
                                                   lastkill = gametimer;
                                                }
                                                catch (Exception F) {
                                                   hasAUX = true;
                                                   AUX = "Something sinistar happened!";
                                                   printDebugMessage("Exception: " + F.toString() + ": " + F.getMessage() + ", Line: 3445");
                                                }
                                             }
                                             else {
                                                if(extremeRules)
                                                   scrboard.score += HIT * 2;
                                                else
                                                   scrboard.score += HIT;
                                             
                                                scoreStr.setText("" + scrboard.score);
                                             }
                                          }
                                          else if ((locationPlayerY + 1) == typer.y && locationPlayerX == typer.x) {
                                             if(aie.getHit()) {
                                                try {
                                                   exp += EXPGained(((Oberon_AIDrivenEnemy) aiEnemies.elementAt(i)).enemyLevel, playerLevel);
                                                   if(extremeRules)
                                                      scrboard.score += (ENEMY_DEFEAT * aie.enemyLevel * 2);
                                                   else
                                                      scrboard.score += (ENEMY_DEFEAT * aie.enemyLevel * (int) frenzymult);
                                                
                                                   scoreStr.setText("" + scrboard.score);
                                                   aiEnemies.removeElementAt(i);printDebugMessage("OBERONAI - DESTRUCTOR: " + "Vector Size: " + aiEnemies.size()); 
                                                   kills++;
                                                   lastkill = gametimer;
                                                }
                                                catch (Exception de) {
                                                   hasAUX = true;
                                                   AUX = "Something sinistar happened!";
                                                   printDebugMessage("Exception: " + de.toString() + ": " + de.getMessage() + ", Line: 3476");
                                                }
                                             }
                                             else {
                                                if(extremeRules)
                                                   scrboard.score += HIT * 2;
                                                else
                                                   scrboard.score += HIT;
                                             
                                                scoreStr.setText("" + scrboard.score);
                                             }
                                          }
                                          else if ((locationPlayerY - 1) == typer.y && locationPlayerX == typer.x) {
                                             if(aie.getHit()) {
                                                try {
                                                   exp += EXPGained(((Oberon_AIDrivenEnemy) aiEnemies.elementAt(i)).enemyLevel, playerLevel);
                                                   if(extremeRules)
                                                      scrboard.score += (ENEMY_DEFEAT * aie.enemyLevel * 2);
                                                   else
                                                      scrboard.score += (ENEMY_DEFEAT * aie.enemyLevel * (int) frenzymult);
                                                
                                                   scoreStr.setText("" + scrboard.score);
                                                   aiEnemies.removeElementAt(i);printDebugMessage("OBERONAI - DESTRUCTOR: " + "Vector Size: " + aiEnemies.size()); 
                                                   kills++;
                                                   lastkill = gametimer;
                                                }
                                                catch (Exception dd) {
                                                   hasAUX = true;
                                                   AUX = "Something sinistar happened!";
                                                   printDebugMessage("Exception: " + dd.toString() + ": " + dd.getMessage() + ", Line: 3506");
                                                }
                                             }
                                             else {
                                                if(extremeRules)
                                                   scrboard.score += HIT * 2;
                                                else
                                                   scrboard.score += HIT;
                                             
                                                scoreStr.setText("" + scrboard.score);
                                             }
                                          }
                                       }
                                    }
                                    break;
                                 }
                           }
                        }
                        
                        switch (codes) {           // These key events are always fired
                           case KeyEvent.VK_R: 
                              {
                                 if (isDead && !isGameOver) {
                                    rebornP();
                                    scrboard.updateRates();
                                 
                                    try {
                                       FileOutputStream fstrm = new FileOutputStream(new File("scoreboard.adf"));
                                       ObjectOutput ostrm = new ObjectOutputStream(fstrm);
                                       ostrm.writeObject(scrboard);
                                       ostrm.flush();
                                       ostrm.close();
                                       fstrm.close();
                                    }
                                    catch (Exception f) {
                                       f.printStackTrace();
                                       printDebugMessage("SWITCH - EXCEPTION: " + f.getMessage() + ", Line: 3543");
                                    }
                                 
                                    printDebugMessage("KRUXTOURN - Player Revives");
                                 }
                                 break;
                              }
                           case KeyEvent.VK_N: 
                              {
                                 if(!showNominal) {
                                    showNominal = true;
                                    isPause = true;
                                 }
                                 else {
                                    showNominal = false;
                                    isPause = false;
                                 }
                                 printDebugMessage("KRUXTOURN - Nominal Score Shown");
                                 break;
                              }
                           case KeyEvent.VK_S:
                              {
                                 if (statusBoxDrawn)
                                    statusBoxDrawn = false;
                                 else
                                    statusBoxDrawn = true;
                                 
                                 printDebugMessage("KRUXTOURN - StatusBox toggled");
                                 break;
                              }
                           case KeyEvent.VK_F4: 
                              {
                              // Display the cheat entering window
                                 enteringCheat = true;
                                 printDebugMessage("KRUXTOURN - Chat Window Displayed");
                                 break;
                              }
                           case KeyEvent.VK_F5: 
                              {
                              // Add a random "bot" enemy
                              // Function Disabled as for Rel 14.
                                 hasAUX = true;
                                 AUX = "Function Disabled"; 
                              // addAIEnemy(spawnPoint2.x, spawnPoint2.y);
                                 break;
                              }
                           case KeyEvent.VK_F6: 
                              {
                              // Unassigned Key
                                 break;
                              }
                           case KeyEvent.VK_F7:
                              {
                              // Unassigned Key
                                 break;
                              }
                           case KeyEvent.VK_F8:
                              {
                                 int countr = 0;
                                 try {
                                    Graphics2D gd = outputImage.createGraphics();
                                    grid.print(gd);
                                 
                                    File scrfile = new File ("shot" + countr + ".png");
                                    while(scrfile.exists()) {
                                       countr++;
                                       scrfile = new File ("shot" + countr + ".png");
                                    }
                                 
                                    boolean done = ImageIO.write(outputImage, "png", scrfile);
                                 
                                    hasAUX = true;
                                    AUX = "SCREENSHOT SAVED AS " + scrfile.getName();
                                    printDebugMessage("KRUXTOURN - Screenshot Saved");
                                 }
                                 catch (Exception exception) {
                                    printDebugMessage("VKEYLISTENER - EXCEPTION: " + exception.getMessage() + ", Line: 3619");
                                 }
                                 break;
                              }
                           case KeyEvent.VK_F12: 
                              {
                                 if(DEATHMATCHMODE) {
                                    AUX = "CANNOT ADD TO DEATHMATCH MODE!";
                                    hasAUX = true;
                                 }
                                 else {
                                    predefKey = "";
                                    enteringPredef = true;
                                 }
                                 break;
                              }
                        }
                     
                        if(debugMode) {                      // These key events are only fired in developer (debug) mode
                           switch (codes) {
                              case KeyEvent.VK_1: 
                                 {
                                    hasAUX = true;
                                    AUX = "Player Life " + curHealthP + " of " + maxHealthP;
                                    break;
                                 }
                              case KeyEvent.VK_2: 
                                 {
                                    hasAUX = true;
                                    AUX = "Player Level " + levelPlayer + " of " + levelMax;
                                    break;
                                 }
                              case KeyEvent.VK_3: 
                                 {
                                    hasAUX = true;
                                    AUX = "PlayerWeapon (" + playerWeapon + ") " + pWeapLeft + " of " + pWeapUses;
                                    break;
                                 }
                              case KeyEvent.VK_4: 
                                 {
                                    hasAUX = true;
                                    AUX = "Enemy Life " + curHealthE + " of " + maxHealthE;
                                    break;
                                 }
                              case KeyEvent.VK_5: 
                                 {
                                    hasAUX = true;
                                    AUX = "Enemy Level " + levelEnemy + " of " + levelMax;
                                    break;
                                 }
                              case KeyEvent.VK_6: 
                                 {
                                    hasAUX = true;
                                    AUX = "Enemy Weapon (" + enemyWeapon + ") " + eWeapLeft + " of " + eWeapUses;
                                    break;
                                 }
                              case KeyEvent.VK_7: 
                                 {
                                    hasAUX = true;
                                    AUX = "Player Positional (" + locationPlayerX + "," + locationPlayerY + ")";
                                    break;
                                 }
                              case KeyEvent.VK_8: 
                                 {
                                    hasAUX = true;
                                    AUX = "Enemy Positional (" + locationEnemyX + "," + locationEnemyY + ")";
                                    break;
                                 }
                              case KeyEvent.VK_9: 
                                 {
                                    if(kohmode) {
                                       hasAUX = true;
                                       AUX = "KOH Flag Positional (" + kohlocat.x + "," + kohlocat.y + ")";
                                    }
                                    else {
                                       hasAUX = true;
                                       AUX = "KOH Flag Positional is absent";
                                    }
                                    break;
                                 }
                              case KeyEvent.VK_0: 
                                 {
                                    hasAUX = true;
                                    AUX = "OBERON AI TARGET$ " + enemyTarget;
                                    break;
                                 }
                           }
                        }
                     
                        switch (codes) {
                           case KeyEvent.VK_F1: 
                              {
                                 final JPanel AboutPanel = 
                                    new JPanel() {
                                       public void paintComponent(Graphics g) {
                                          String regSt = "";
                                          String regSt2 = "";
                                          if (isRegist) {
                                             regSt = rB.REGNAME;
                                             regSt2 = rB.REGCUMP;
                                          }
                                          else 
                                             regSt = "Unactivated";
                                       
                                       // Heading
                                          g.setFont(new Font("Tahoma", Font.PLAIN, 11));
                                          g.drawImage(new ImageIcon(getClass().getResource("/images/about_img.PNG")).getImage(),0,0,this);
                                          g.drawString(PRODUCT + " (" + VERSION + "." + BUILD + ") " + REVISION, 78, 17);
                                          g.drawString(REVIEW, 78, 34);
                                          g.drawString("2010 Microtech Technologies", 91, 50);
                                       
                                       // Copyright Notice
                                          g.setFont(new Font("Tahoma", Font.PLAIN, 9));
                                          g.drawString("Parts of this application were created by external authors and are copyrights of their respective owners and", 78, 75);
                                          g.drawString("are not maintained by Microtech Technologies. The KMF file format is a trademark of Radioactive Reds", 78, 87);
                                          g.drawString("Animation Studios. Krux Tourno is a shared trademark of Radioactive Reds Animation, Nature's", 78, 99);
                                          g.drawString("Little Helpers and Microtech Technologies.", 78, 111);
                                          g.drawString("", 78, 123);
                                          g.drawString("", 78, 135);
                                       
                                       // Registration Section
                                          g.setFont(new Font("Tahoma", Font.PLAIN, 11));
                                          g.drawString(regSt, 86, 246);
                                          g.drawString(regSt2, 86, 262);
                                       
                                       // Product Key
                                          g.drawString(prodcode, 150, 278);
                                       
                                       // Warning
                                          g.drawString("This computer is protected by copyright law and international treaties. Unauthorized reproduction", 53, 346);
                                          g.drawString("or distribution of this program, or any portion of it, may result in severe civil and criminal penalties, and will", 6, 360);
                                          g.drawString("be prosecuted to the maximum extent possible under the law", 6, 374);
                                          g.drawString("", 6, 388);
                                          g.drawString("", 6, 402);
                                          g.drawString("", 6, 416);
                                       }
                                    };
                              
                                 AboutPanel.setPreferredSize(new Dimension(529, 395));	
                              	
                                 Object[] mess = { AboutPanel };
                                 String[] ops = { "        OK        " };               
                                 JOptionPane.showOptionDialog(
                                             null,
                                             mess,
                                             "About " + PRODUCT,
                                             JOptionPane.DEFAULT_OPTION,
                                             JOptionPane.PLAIN_MESSAGE,
                                             null,
                                             ops,
                                             ops[0]);
                              }
                           case KeyEvent.VK_F2: 
                              {
                                 hasAUX = true;
                                 if(soundOn) {
                                    AUX = "Sound Disabled";
                                    soundOn = false;
                                 }
                                 else {
                                    AUX = "Sound Enabled";
                                    soundOn = true;
                                 }
                              
                                 printDebugMessage("KRUXTOURN - Sound set to " + soundOn);
                                 break;
                              }
                           case KeyEvent.VK_F3: 
                              {
                                 if(!showNominal) {
                                    hasAUX = true;
                                    if(isPause) {
                                       AUX = "Game Resumed";
                                       isPause = false;
                                    
                                    }
                                    else {
                                       AUX = "Game Paused";
                                       isPause = true;
                                    
                                    }
                                    printDebugMessage("KRUXTOURN - GameState set to " + isPause);
                                 }
                                 break;
                              }
                            case KeyEvent.VK_F9:
                            {
                                displayAuxMessage("New Game Not Allowed!");
                            }
                        }
                     }
                  };
               });
      
      if(!loadedmap) {
        addBounds(makebou, BOUND_SOLID); // Static boundaries are being made
        addBounds(makembou, BOUND_SHIFT); // Shifting boundaries are being made
        addBounds(maketbou, BOUND_TRACK);
      }
      
      if(gemHunterMode) {
         gemLocat = findFreeBlock();
         gemType = (short) (((double) Math.random() * 4) + 1);
         kohtimer.setText("" + 1000);
      }
      
      printDebugMessage("KRUXTOURN - Now starting the main thread...");
      controlThread.start(); // Let the games begin! (Starts the Thread)r
      printDebugMessage("KRUXTOURN - Main Thread started.");
   }

   /**
   *  Krux 3 Method
   * ==================================================================================
   *  defaultAll
   *  Nullifies all game variables in order to clear the playing field for the next game
   */
   protected void defaultAll() {
      printDebugMessage("KRUXTOURN - All variables returned to default");
      // Firstly stop the current thread and nullify it
      controlThread = null;
      // Now take out all the trash
      System.gc();
    
      aiEnemies.removeAllElements();
      
      GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
      DisplayMode modalForm = device.getDisplayMode();
   
      mainwin = null;
      startdiag = new JDialog();			// The Game Generator Dialog
      mapsize = new Point(12,12); 		// The mapsize variable
      OFFSCREEN = new Point (mapsize.x * 2, mapsize.y * 2);
      countTrackers = 0;
      botLimitImposed = false;
      
      DEATHMATCHMODE = false;
      FRIENDLYFIRE   = false;
      lastWasRed	   = false;
   	
      gametimer = 0;					
      lastkill = 0;					
      frenzytime = 30;			
      frenzymult = 1;				
      kills = 0;			
      lastkills = 0;
      
      items = 0;			
      
      senseDist = 5;
      
      soundOn = true;
      isPause = false;
      nightMode = false;
      showHighScores = false;
      isDead = false;
      
      gemHunterMode = false;
      gemsP1 = 0;
      gemsP2 = 0;
      gems1 = new CGString("" + 0, 3, CGString.ALIGN_RIGHT, CGString.DIGITAL);
      gems2 = new CGString("" + 0, 3, CGString.ALIGN_RIGHT, CGString.DIGITAL);
      
      scrollOffset = new Point (254, 164);
      
      exp = 0;
      expLast = 0;
      expForNext = CalculateEXP(playerLevel + 1);
      
      exp_E = 0;
      expLast_E = 0;
      expForNext_E = CalculateEXP(enemyLevel + 1);
      
      strengthP1 = (int) (Math.random() * 10);
      enduranceP1 = (int) (Math.random() * 10);
      strengthP2 = (int) (Math.random() * 10);
      enduranceP2 = (int) (Math.random() * 10);
   
   // Image defaults
      SPRITE_FLOOR =                       // The default floor tile design
         new ImageIcon(getClass().getResource("/krux2/floor.GIF")).getImage();
      SPRITE_STATICB =                         // The default boundary design
         new ImageIcon(getClass().getResource("/krux2/K_Border1.GIF")).getImage();
      SPRITE_MOBILEB =                        // The default mobile boundary design
         new ImageIcon(getClass().getResource("/krux2/K_Border.GIF")).getImage();
      SPRITE_BLUEPLAY =                          // The default player design
         new ImageIcon(getClass().getResource("/krux2/player.png")).getImage();
      SPRITE_REDPLAY =                          // The default enemy design
         new ImageIcon(getClass().getResource("/krux2/enemy.png")).getImage();
         
      lampLocat = new Point (mapsize.x * 2, mapsize.y * 2);
   
   // Floortile Designs
      floorsV = new Point[mapsize.x * mapsize.y];
      floors1 = new Point[mapsize.x * mapsize.y];
      floors2 = new Point[mapsize.x * mapsize.y];
      floors3 = new Point[mapsize.x * mapsize.y];
      floors4 = new Point[mapsize.x * mapsize.y];
      floors5 = new Point[mapsize.x * mapsize.y];
      floortile1 = NULL_TILE;
      floortile2 = NULL_TILE;
      floortile3 = NULL_TILE;
      floortile4 = NULL_TILE;
      floortile5 = NULL_TILE;
      floorsVcnt = 0;
      floors1cnt = 0;
      floors2cnt = 0;
      floors3cnt = 0;
      floors4cnt = 0;
      floors5cnt = 0; 
           
   // Map defaults
      mapdata = new String[mapsize.x][mapsize.y];
      maxbounds = 32;                   // The boundary limit variable
      spawnPoint1 = new Point(0,0);   // Spawnpoint for Player
      spawnPoint2 =                   // Spawnpoint for Enemy
         new Point(mapsize.x - 1, mapsize.y - 1);
      kohlocat =                      // Location of the K.O.T.H flag
         new Point(-1, -1);
      mapName = "unknown map";       // Map Name
      loadedmap = false;            // True if external map is loaded
      kohmode = false;              // True if King of the Hill mode is on
      isGameOver = false;           // True if Game Over
      flagowner = 0;                    // The player that owns the kohflag
   
   // Message and Damage Painting Defaults
      damageE = 0;                      // The damage value to paint
      damageP = 0;                      // see above value
      redrawsP = 0;                     // Redraw timer
      redrawsE = 0;                     // Redraw timer
      damagePlayerPaint = false;    // Sets if the damage should be painted
      damageEnemyPaint = false;     // Sets if the damage should be painted
      redrawAUX = 0;                    // Redraw timer
      hasAUX = false;               // Sets if the message should be painted
      AUX = "";                      // Message string
   
   // Cheat Subsystem Defaults
      enteringCheat = false;
      CHEAT = "";
      cheatsOn = false;
         
   // Unused Full-Screen Loader
      h = GraphicsEnvironment.getLocalGraphicsEnvironment();
   
   // AI Bias and Difficulty variables
      movesBias = new int[12];        // Moves that are made in advance
      moves = 0;                        // A Move counter
      maxMoves = 4;                     // The Move limit for the OBERON AI
      movetemp = 0;                     // a Temp field
      recalls = 0;                      // Debugging Field
      difficulty = 2;                   // Game difficulty
       	 
   // Player location variables
      locationPlayerX = spawnPoint1.x; 		// X-Location variable
      locationPlayerY = spawnPoint1.y; 		// Y-Location variable
   
   // Map Constraints
      makebou = 10; 								// Amount of static boundaries (10 default)
      makembou = 5; 								// Amount of shifting boundaries (5 default)
      revive = 0; 									// Amount of times player has revived
   
   // Enemy location variables
      enemyTarget = "player";
      locationEnemyX = spawnPoint2.x; 		// X-Location variable
      locationEnemyY = spawnPoint2.y; 		// Y-Location variable
   
   // Health Ratio
      healthRatio = 100; 						// Persentage of maximum health each player has
   
   // Score Management
   // score = 0; 									// The player's current score (starts at 0)
      hiscore = 0; 								// The game's current hiscore
      hiscorename = "Last High-Score"; 	// The game's current hiscore name
   
   // Player reviving variables
      revivesUnlimited = true; 		// If true, revives are unlimited
      revivelimit = 0; 						// Limit to amount of revives allowed
      isDead = false;               // Defines if the you are dead
   
   // Movement restriction variables
      restrict = false; 					// If true, player cannot move
      restrictE = false; 					// If true, enemy cannot move
   
   // Menu Bar Text Fields
      kohtimer = new JTextField("0");  // The K.O.T.H Timer field
   
   
   // Current levels
      enemyLevel = 2; 							// Enemy's level 
      playerLevel = 2; 							// Player's level
      
       
     // Current Weapons
      playerWeapon = -1;                  // The current weapon
      pWeapUses = 1;                      // The maximum amount of shots
      pWeapLeft = 0;                      // Shots left
      enemyWeapon = -1;
      eWeapUses = 1;
      eWeapLeft = 0;
    
   // Location Points for boxes
      healthLocat = new Point(mapsize.x * 2, mapsize.y * 2); 		// Health Box location (default is off-screen)
      levelboxLocat = new Point(mapsize.x * 2, mapsize.y * 2); 	// Level-Up Box location (default is off-screen)
      extraLifeLocat = new Point(mapsize.x * 2, mapsize.y * 2); 	// Extra Life Box location (default is off-screen)
      weaponboxLocat = new Point(mapsize.x * 2, mapsize.y * 2); 	// Extra Life Box location (default is off-screen)
   
   // Maximum Hit Points calculations
      maxHealthP = CalculateHP(lifeIVP1, lifeEVP1, playerLevel); // Player's max HP
      maxHealthE = CalculateHP(lifeIVP2, lifeEVP2, enemyLevel); // Enemy's max HP
   
   // Other variables and constants
      count = 0; 							// static boundaries buffer 
      gamespeed = 250; 					// Game speed (in milliseconds per refresh)
      countM = 0; 							// shifting boundaries buffer
      healthlust = 25; 					// How desperately will the enemy be searching for health crates?
      allowThinking = true; 		// If true enemies will think thier moves through
      violence = false; 			// Sets weather the Third Guy is violent or not
      hasGenerated = false;	// Keeps track of weather or not the user has generated a map or not
   
   // Life/Level bars of players
      curHealthP = 0; 		// Player Life
      curHealthE = 0; 		// Enemy Life
   
      levelPlayer = 0; 		// Player Level
      levelEnemy = 0; 		// Enemy Level
   
   // CGString variables
      hiScoreStr = new CGString("0", 12);
      scoreStr = new CGString("0", 8, CGString.ALIGN_RIGHT, CGString.DIGITAL);
      timerStr = new CGString("0", 4);
      cheatStr = new CGString("0", 64);
      spawnStr = new CGString("PRESS R TO RESPAWN", 18);
      goStr = new CGString("GAME OVER", 9);
   }
   
	/**
   *  Krux 2 Method
   * ==================================================================================
   *  implementDialog
   *  Microtech Remlocke OS propietry method for generating generic dialogs
   *
   *  @param   title    The window title
   *  @param   content  The content pane for this window
   *  @param   hsize    The width of the window
   *  @param   vsize    The height of the window
   *  @param   pack     Pack the content or not
   *  @return  The JDialog with the required set-up
   */
   public JDialog implementDialog(String title, Container content, int hsize, int vsize, boolean pack) {
      JDialog newdiag = new JDialog(notneeded, title, false); // <-- Remember JFrame notneeded...that's what it is for!
      if (pack) {
         newdiag.pack();
      }
      else {
         newdiag.setSize(hsize,vsize);
      }
      newdiag.setContentPane(content);
      newdiag.setLocation((modalForm.getWidth() / 2) - (hsize / 2), (modalForm.getHeight() / 2) - (vsize / 2));
      newdiag.setVisible(true);
      return newdiag;
   }

   /**
   *  Krux 2 Method
   * ==================================================================================
   *  implementWindow
   *  Microtech Remlocke OS propietry method for generating generic windows
   *
   *  @param   title    The window title
   *  @param   content  The content pane for this window
   *  @param   hg       The frame icon
   *  @param   hsize    The width of the window
   *  @param   vsize    The height of the window
   *  @param   pack     Pack the content or not
   *  @return  The JFrame with the required set-up
   */
   public JFrame implementWindow(String title, Container content, ImageIcon hg, int hsize, int vsize, boolean pack) {
      JFrame newframe = new JFrame(title);
      newframe.setContentPane(content);
      if (pack) {
         newframe.pack();
         newframe.setLocation((modalForm.getWidth() / 2) - (newframe.getWidth() / 2), (modalForm.getHeight() / 2) - (newframe.getHeight() / 2));
      }
      else {
         newframe.setSize(hsize,vsize);
         newframe.setLocation((modalForm.getWidth() / 2) - (hsize / 2), (modalForm.getHeight() / 2) - (vsize / 2));
      }
      newframe.setResizable(false);
      newframe.setIconImage(hg.getImage());
      newframe.setVisible(true);
      newframe.addWindowListener(
               new WindowAdapter() {
                  public void windowClosing(WindowEvent e) {
                     if(debugMode)
                        printDebug.close();
                     System.exit(1);
                  }
               });  
   	
      return newframe;
   }
   
   /**
   *  Krux RTS 7 Method
   * ==================================================================================
   *  addPredef
   *  Adds one of the predefined enemies to the map
   *
   *  @param   key      The key identifying the enemy
   */
   protected void addPredef(String key) {
      Oberon_AIDrivenEnemy aie = new Oberon_AIDrivenEnemy(this, findFreeBlock(), key);
      try {
         if(botLimitImposed && aiEnemies.size() == botLimit) {
            hasAUX = true;
            AUX = "Cannot add, Limit Reached";
         }
         else
            aiEnemies.addElement(aie);
      }
      catch (Exception efc) {
         printDebugMessage("Exception: " + efc.toString() + ": " + efc.getMessage() + ", Line: 4169");
      }
   }
   
   /**
   *  Krux Method
   * ==================================================================================
   *  run
   *  Main Thread control methods, executes unit movement and box generation
   *
   *  @see java.lang.Runnable
   */
   public void run() {
      while (true) {
         /*
         * This is the active randomization engine behind Krux.
         * The assures that everything that needs to be random, is random.
         */
         if(!isPause) {
            gametimer++;
            grid.repaint();
            
            performItemChecks();
          
         // Perform Bot-Related Tasks	       
            botCheck();
            
         // Update the scoreboard
            scrboard.timer = (int)gametimer;
         // scrboard.score = score;
            scrboard.kills = kills;
            scrboard.items = items;
            scrboard.gemsh = gemsP1;
            scoreStr.repaint();
         	
            if(((lastkill - gametimer) % 30) == 0) {
               frenzymult = 1;
               lastkills = kills;
            }
            else {
               frenzymult = (byte) (kills - lastkills);
            }
         	
            handleAIMoves();
            handleKOHGems();
            handleItems();
            handleExperience();
         }
         
         if(isGameOver)
            isDead = false;
      
         handleDeath();	
         
         try { 
            Thread.sleep(gamespeed); } // Implements the game speed setting
         catch (InterruptedException e) {
            e.printStackTrace();
            printErrMeth(e, "MAINTHREAD", true);
            printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage() + ", Line: 4659");
         }
         
      }
   }
   
   public void handleAIMoves() {
      if(moves != 0) {  // Executes the moves returned by the OBERON AI Engine
         getEnemyMove(movesBias[movetemp]);
         movetemp++;
         if(movetemp == moves) {
            moves = 0;
            movetemp = 0;
         }
      }
      else {
         EnemyMove(); // Sends a call to the OBERON AI to generate a movement path
      }
         
      for(int i = 0; i < aiEnemies.size(); i++) {
         Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
         aie.getNextMove();
      }
            
      for (int i = 0; i < aiEnemies.size(); i++) {
         Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
      // printDebugMessage("OBERONAI - TARGET: " + aie.getTarget());
         if(aie.isPoisoned) {
            aie.life -= 2;
            if(aie.life <= 0) {
               try {
                  aiEnemies.removeElementAt(i);
                  printDebugMessage("OBERONAI - DESTRUCTOR: " + "Vector Size: " + aiEnemies.size()); 
               }
               catch (Exception e) {
                  e.printStackTrace();
                  hasAUX = true;
                  AUX = "Something sinistar happened!";
                  printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage() + ", Line: 4319");
               }
            }
         }
      }
   }
   
   public void handleKOHGems() {
      if(kohmode && flagowner != 0 && !isGameOver) { // Tests for a game over situation in KOTH mode
         kohtimer.setText("" + (Integer.parseInt(kohtimer.getText()) - 1));
         if(flagowner == 1) {
            scrboard.score += 25;
            scoreStr.setText("" + scrboard.score);
         }
         else if(flagowner == 2) {
            scoreStr.setText("" + scrboard.score);
            scrboard.score -= 10;
         }
         if(Integer.parseInt(kohtimer.getText()) <= 0) {
            scrboard.score = scrboard.score * (levelPlayer + (revivelimit - revive));
            getVictory(flagowner);
         }
      }
            
      if(gemHunterMode && !isGameOver) {
         if(Integer.parseInt(kohtimer.getText()) <= 0) {
            if(gemsP1 > gemsP2) {
               scrboard.score += gemsP1 * 7500;
                     
               scoreStr.setText("" + scrboard.score);
               getVictory(1);
            }
            else if(gemsP2 > gemsP1) {
               getVictory(2);
            }
            else {
               kohtimer.setText("" + 100);
            }
         }
         else {
            kohtimer.setText("" + (Integer.parseInt(kohtimer.getText()) - 1));
            isGameOver = false;
         }
      }
   }
   
   public void handleDeath() {
      if (!isDead && (locationPlayerX != (mapsize.x * 2))) {
         lxlimit = Math.max(locationPlayerX - 11, 0);
         lylimit = Math.max(locationPlayerY - 7, 0);
         uxlimit = Math.min(locationPlayerX + 12, mapsize.x + 1);
         uylimit = Math.min(locationPlayerY + 8, mapsize.y + 1);
         scrollOffset.x = (254 - locationPlayerX * 30);
         scrollOffset.y = (164 - locationPlayerY * 30);        
      }
      else {
         lxlimit = Math.max(locationEnemyX - 11, 0);
         lylimit = Math.max(locationEnemyY - 7, 0);
         uxlimit = Math.min(locationEnemyX + 12, mapsize.x + 1);
         uylimit = Math.min(locationEnemyY + 8, mapsize.y + 1);
         scrollOffset.x = (254 - locationEnemyX * 30);
         scrollOffset.y = (164 - locationEnemyY * 30);   
      }  
      
      if(curHealthE <= 0) {
         printDebugMessage("KRUXTOURN - Player 2 was killed");
         playSound("krux/explode.wav");
         if(extremeRules)
            scrboard.score += (ENEMY_DEFEAT * enemyLevel * 2);
         else
            scrboard.score += (ENEMY_DEFEAT * enemyLevel);
            
         scoreStr.setText("" + scrboard.score);
         locationEnemyX = (mapsize.x * 2);
         locationEnemyY = (mapsize.y * 2);
            
         exp += EXPGained(enemyLevel, playerLevel);
         rebornE();
      }
    
      if(curHealthP <= 0 && !isDead) {
         printDebugMessage("KRUXTOURN - Player 1 was killed");
         playSound("krux/explode.wav");
         locationPlayerX = (mapsize.x * 2);
         locationPlayerY = (mapsize.y * 2);
         isDead = true;
      }
         
      if(curHealthE > maxHealthE)
         curHealthE = maxHealthE;
      		
      if(curHealthP > maxHealthP)
         curHealthP = maxHealthP;
   }
   
   public void handleExperience() {
      if(exp >= expForNext) {
         int last = (strengthP1 + enduranceP1) * (levelPlayer + 1);
            
         expLast = expForNext;
         expForNext = CalculateEXP(playerLevel + 1);
            	
         playerLevel++;
         levelPlayer = (playerLevel);
               
         strengthP1 = CalculateStat(strengthIVP1, strengthBP1, strengthEVP1, playerLevel);
         enduranceP1 = CalculateStat(enduranceIVP1, enduranceBP1, enduranceEVP1, playerLevel);
               
         int lastLife = maxHealthP;
         maxHealthP = CalculateHP(lifeIVP1, lifeEVP1, playerLevel);
               
         curHealthP = (int) (((float)curHealthP / (float)lastLife) * (float)maxHealthP);
               
         hasAUX = true;
         AUX = "Level: " + playerLevel + ", STR: " + strengthP1 + ", END: " + enduranceP1 + ", HP: " + maxHealthP;
      }
            
      if(exp_E >= expForNext_E) {
         int last = (strengthP2 + enduranceP2 * (levelEnemy) + 1); 
            	
         expLast_E = expForNext_E;
         expForNext_E = CalculateEXP(enemyLevel + 1);
            	
         enemyLevel++;
         levelEnemy = (enemyLevel);
               
         strengthP2 = CalculateStat(strengthIVP2, strengthBP2, strengthEVP2, enemyLevel);
         enduranceP2 = CalculateStat(enduranceIVP2, enduranceBP2, enduranceEVP2, enemyLevel);
            
         int lastLife = maxHealthE;
         maxHealthE = CalculateHP(lifeIVP2, lifeEVP2, enemyLevel);
               
         curHealthE = (int) (((float)curHealthE / (float)lastLife) * (float)maxHealthE);
               
         hpDrawn = true;
         hpTime = 25;
      }
   }
   public void handleItems() {
      /**
         	* Code for handling the U-No-See Potion's effects
         	*/
      if(u_no_see1) {
         if(Integer.parseInt(kohtimer.getText()) <= 0)
            u_no_see1 = false;
         else {
            kohtimer.setText("" + (Integer.parseInt(kohtimer.getText()) - 1));
            isGameOver = false;
         }
      }
            
      if(u_no_see2) {
         if(u_no_see_timer <= 0)
            u_no_see2 = false;
         else
            u_no_see_timer -= 1;
      }
                
           /**
         	* Code for handling the Elixir of Life's effects
         	*/           
      if(hasMegaHP) {
         if(MegaHPRemain > (1 * playerLevel)) {
            curHealthP = (curHealthP + (1 * playerLevel));
            MegaHPRemain -= (1 * playerLevel);
         }
         else {
            curHealthP = (curHealthP + MegaHPRemain);
            MegaHPRemain = 0;
            hasMegaHP = false;
         }
      }
            
      if(hasMegaHP_E) {
         if(MegaHPRemain_E > (1 * enemyLevel)) {
            curHealthE = (curHealthE + (1 * enemyLevel));
            MegaHPRemain_E -= (1 * enemyLevel);
         }
         else {
            curHealthE = (curHealthE + MegaHPRemain_E);
            MegaHPRemain_E = 0;
            hasMegaHP_E = false;
            hpDrawn = false;
         }
      }
            
         	/**
         	* Code for handling the painkiller item's effects
         	*/
      if(painKillerP1) {
         if(pkcount1 == 3) {
            if(painKillerRemP1 > 1) {
               curHealthP -= 1;
               painKillerRemP1 -= 1;
            }
            else {
               curHealthP -= painKillerRemP1;
               painKillerRemP1 = 0;
               painKillerP1 = false;
            }
            pkcount1 = 0;
         }
         else
            pkcount1++;
      }
            
      if(painKillerP2) {
         if(pkcount2 == 3) {
            if(painKillerRemP2 > 1) {
               curHealthE -= 1;
               painKillerRemP2 -= 1;
            }
            else {
               curHealthE -= painKillerRemP2;
               painKillerRemP2 = 0;
               painKillerP2 = false;
            }
            pkcount2 = 0;
         }
         else
            pkcount2++;
      }
            
    // The state of being zapped
      if(isZapped1) {
         zaptimer1 -= 1;
         if(zaptimer1 == 0) {
            isZapped1 = false;
         }
      }
            
      if(isZapped2) {
         zaptimer2 -= 1;
         if(zaptimer2 == 0) {
            isZapped2 = false;
         }
      }
            
    // The state of Poisoned
      if(isPoisoned1) {
         if(extremeRules)
            curHealthP = (curHealthP - (poisonLevel1 * 2));
         else
            curHealthP = (curHealthP - poisonLevel1);
                  
         if(curHealthP <= 0) {
            isDead = true;
            poisonLevel1 = 0;
            isPoisoned1 = false;
         }
      }
            
      if(isPoisoned2) {
         curHealthE = (curHealthE - poisonLevel2);
         if(curHealthE <= 0) {
            playSound("krux/explode.wav");
            if(extremeRules)
               scrboard.score += (ENEMY_DEFEAT * enemyLevel * 2);
            else
               scrboard.score += (ENEMY_DEFEAT * enemyLevel);
            locationEnemyX = (mapsize.x * 2);
            locationEnemyY = (mapsize.y * 2);
                  
            rebornE();
         }
      }
            
      if(hasLamp) {
         if(LampRemain > 1)
            LampRemain--;
         else {
            LampRemain = 0;
            hasLamp = false;
         }
      }
            
      if(hasMegaExp) {
         if(MegaExpRemain > (2 * playerLevel)) {
            exp += 2;
            MegaExpRemain -= 2;
         }
         else {
            exp += MegaExpRemain;
            MegaExpRemain = 0;
            hasMegaExp = false;
         }
      }
            
      if(hasMegaExp_E) {
         if(MegaExpRemain_E > 2) {
            exp_E += 2;
            MegaExpRemain_E -= 2;
         }
         else {
            exp_E += MegaExpRemain_E;
            MegaExpRemain_E = 0;
            hasMegaExp_E = false;
            expDrawn = false;
         }
      }
            
      if(expDrawn && expTime != -1) {
         expTime -= 1;
         if(expTime == 0)
            expDrawn = false;
      }
            
      if(hpDrawn && hpTime != -1) {
         hpTime -= 1;
         if(hpTime == 0)
            hpDrawn = false;
      }
            
      if(isGhost1) {
         if(Integer.parseInt(kohtimer.getText()) <= 0)
            isGhost1 = false;
         else {
            kohtimer.setText("" + (Integer.parseInt(kohtimer.getText()) - 1));
            isGameOver = false;
         }
      }
            
      if(isGhost2) {
         if(ghost_timer <= 0)
            isGhost2 = false;
         else
            ghost_timer -= 1;
      }
   }
   
   public void performItemChecks() {
      int hDraw = (int) Math.round(Math.random() * 500);
      if(hDraw < 1 || hDraw > 499) { // Creates an extra life box
         extraLifeLocat = findFreeBlock();
      }
      else if(hDraw < 2 || hDraw > 498) {
         if(!kohmode && !gemHunterMode)
            unoseeLocat = findFreeBlock();
      }
      else if(hDraw < 3 || hDraw > 497) { // Create a level box
         levelboxLocat = findFreeBlock();
      }
      else if(hDraw < 5 || hDraw > 495) {
         if(!kohmode && !gemHunterMode)
            ghostpotionLocat = findFreeBlock();
      }
      else if(hDraw < 6 || hDraw > 494) { // Create a weapon box
         weaponboxLocat = findFreeBlock();
      }
      else if(hDraw < 7 || hDraw > 493) {
         megahealthLocat = findFreeBlock();
      }
      else if(hDraw < 8 || hDraw > 492) {
         megaexpLocat = findFreeBlock();
      }
      else if(hDraw < 9 || hDraw > 491) {
         if (nightMode)
            lampLocat = findFreeBlock();
      }
      else if(hDraw < 10 || hDraw > 490) {
         if(gemHunterMode) {
            gemLocat = findFreeBlock();
            gemType = (short) (((double) Math.random() * 4) + 1);
         }
      }
      else if(hDraw < 11 || hDraw > 489) {
         armorLocat = findFreeBlock();
         searchLocation = findFreeBlock();
      }
      else if(hDraw < 12 || hDraw > 488) {
         painkLocat = findFreeBlock();
         int g2e = (int) ((double)Math.random() * 8);
               
         if (g2e > 6)
            painKillType = 1;
         else
            painKillType = 0;
      }
      else if(hDraw < 15) {
         healthLocat = findFreeBlock();
         int g2e = (int) ((double)Math.random() * 8);
               
         if (g2e > 7)
            hpboxtype = 2;
         else if (g2e > 6)
            hpboxtype = 1;
         else
            hpboxtype = 0;
      }
         
      if(hDraw < 50) { // Shifts the shifting boundaries
         updateBoundaries(BOUND_SHIFT);
      }
            
      if(hDraw < 75) {
         updateBoundaries(BOUND_TRACK);
      }
         
      if(hDraw < 4 || hDraw > 496) {
         if(extremeRules) {
            for(int i = 0; i < 5; i++) {
               addAIEnemy(findFreeBlock());
            }
         }
         else if(difficulty == 3) {
            addAIEnemy(findFreeBlock());
            addAIEnemy(findFreeBlock());
         }
         else {
            addAIEnemy(findFreeBlock());
         }
         System.gc();
      }
   }
   
   /**
   *  Krux 3 RTS X2 Method
   * ==================================================================================
   *  doRadarSense
   *  Calculates the variables relating to RaderSense(tm)
   *  Draws the Krux NightMode(tm) and RadarSense(tm) overlays
   *
   *  @param      g     The Graphics instance to draw with
   *  @version    1.2  Aug 13, 2010
   *  @author     Byron Kleingeld
   */
   protected void doRadarSense(Graphics g) {
      if(!isDead && !isGameOver) {
         // Rotational search variables
         int hpr = (int) Math.pow((Math.pow((double) (locationPlayerY - healthLocat.y), 2) + Math.pow((double) (locationPlayerX - healthLocat.x), 2)), 0.5);
         int lvr = (int) Math.pow((Math.pow((double) (locationPlayerY - levelboxLocat.y), 2) + Math.pow((double) (locationPlayerX - levelboxLocat.x), 2)), 0.5);
         int wbr = (int) Math.pow((Math.pow((double) (locationPlayerY - weaponboxLocat.y), 2) + Math.pow((double) (locationPlayerX - weaponboxLocat.x), 2)), 0.5);
         int lpr = (int) Math.pow((Math.pow((double) (locationPlayerY - lampLocat.y), 2) + Math.pow((double) (locationPlayerX - lampLocat.x), 2)), 0.5);
         int mhr = (int) Math.pow((Math.pow((double) (locationPlayerY - megahealthLocat.y), 2) + Math.pow((double) (locationPlayerX - megahealthLocat.x), 2)), 0.5);
         int mer = (int) Math.pow((Math.pow((double) (locationPlayerY - megaexpLocat.y), 2) + Math.pow((double) (locationPlayerX - megaexpLocat.x), 2)), 0.5);
         int enm = (int) Math.pow((Math.pow((double) (locationPlayerY - locationEnemyY), 2) + Math.pow((double) (locationPlayerX - locationEnemyX), 2)), 0.5);
         int arm = (int) Math.pow((Math.pow((double) (locationPlayerY - armorLocat.y), 2) + Math.pow((double) (locationPlayerX - armorLocat.x), 2)), 0.5);
         
         String drawer = "";
                  
         int enemyCount = 0;
         
         // Search for non-red enemies       
         for (int i = 0; i < aiEnemies.size(); i++) {
            Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
            Point t = aie.getLocat();
            int bot = (int) Math.pow((Math.pow((double) (locationPlayerY - t.y), 2) + Math.pow((double) (locationPlayerX - t.x), 2)), 0.5);
            if (bot <= senseDist) {
               if (!extremeRules)
                  g.drawImage(SPRITE_RADAR,((t.x) * 30) + 40 + scrollOffset.x,((t.y) * 30) + 40 + scrollOffset.y,grid);
               enemyCount++;
            }
         }
         
         // This section constructs the string that informs you of nearby objects         
         if (hpr <= senseDist) {
            drawer += "Health";
         }
         if (lvr <= senseDist) {
            if(!drawer.equals(""))
               drawer += ", ";
            drawer += "Level-Up";
         }
         if (wbr <= senseDist) {
            if(!drawer.equals(""))
               drawer += ", ";
            drawer += "Weapon";
         }
         if (lpr <= senseDist) {
            if(!drawer.equals(""))
               drawer += ", ";
            drawer += "Lamp";
         }
         if (mhr <= senseDist) {
            if(!drawer.equals(""))
               drawer += ", ";
            drawer += "Elixir";
         }
         if (mer <= senseDist) {
            if(!drawer.equals(""))
               drawer += ", ";
            drawer += "Vial";
         }
         if (arm <= senseDist) {
            if(!drawer.equals(""))
               drawer += ", ";
            drawer += "Armor";
         }
         if (enm <= senseDist) {
            enemyCount++;
         }
                  
         if (enemyCount == 1) {
            if(!drawer.equals(""))
               drawer += " and ";
            drawer += "1 enemy";
         }
         else if (enemyCount > 1) {
            if(!drawer.equals(""))
               drawer += " and ";
            drawer += enemyCount + " enemies";
         }
      
         if(!drawer.equals(""))
            drawer += " nearby";
         
         // <<RTS X2 Note>>
         // Code modifaction: Exact locations are no longer shown in Extreme Rules(tm) mode
         if(!extremeRules) {                  
            if (hpr <= senseDist) {
               g.drawImage(SPRITE_RADAR,((healthLocat.x - 1) * 30) + 40 + scrollOffset.x,((healthLocat.y - 1) * 30) + 40 + scrollOffset.y,grid);
            }
            if (lvr <= senseDist) {
               g.drawImage(SPRITE_RADAR,((levelboxLocat.x - 1) * 30) + 40 + scrollOffset.x,((levelboxLocat.y - 1) * 30) + 40 + scrollOffset.y,grid);
            }
            if (wbr <= senseDist) {
               g.drawImage(SPRITE_RADAR,((weaponboxLocat.x - 1) * 30) + 40 + scrollOffset.x,((weaponboxLocat.y - 1) * 30) + 40 + scrollOffset.y,grid);
            }
            if (lpr <= senseDist) {
               g.drawImage(SPRITE_RADAR,((lampLocat.x - 1) * 30) + 40 + scrollOffset.x,((lampLocat.y - 1) * 30) + 40 + scrollOffset.y,grid);
            }
            if (mhr <= senseDist) {
               g.drawImage(SPRITE_RADAR,((megahealthLocat.x - 1) * 30) + 40 + scrollOffset.x,((megahealthLocat.y - 1) * 30) + 40 + scrollOffset.y,grid);
            }
            if (mer <= senseDist) {
               g.drawImage(SPRITE_RADAR,((megaexpLocat.x - 1) * 30) + 40 + scrollOffset.x,((megaexpLocat.y - 1) * 30) + 40 + scrollOffset.y,grid);
            }
            if (enm <= senseDist) {
               g.drawImage(SPRITE_RADAR,((locationEnemyX) * 30) + 40 + scrollOffset.x,((locationEnemyY) * 30) + 40 + scrollOffset.y,grid);
            }
            if (arm <= senseDist) {
               g.drawImage(SPRITE_RADAR,((armorLocat.x - 1) * 30) + 40 + scrollOffset.x,((armorLocat.y - 1) * 30) + 40 + scrollOffset.y,grid);
            }
         }
                  
         if (!drawer.equals("")) {
            g.setColor(Color.YELLOW);
            g.drawString(drawer, 299 - (drawer.length() * 2), 142);
            g.drawString(drawer, 298 - (drawer.length() * 2), 142);
            g.drawImage(SPRITE_RADAR, 294, 204,grid);
         }
      }
   }
   
	/*
   *  AI Methods
	* ==================================================================================================================
	*  Controls the AI of the Enemy and Third Guy
	*/

   /**
   *  Krux 2.4.113 Method
   * ==================================================================================================================
   *  EnemyMove
   *  This has to be my most favourite (albeit most irritating) section of code. To think that this game started at
   *  version 1.0.0 with no AI at all, just a random number generator to get the moves. Now I can bearly defeat my
   *  own creation. The STELLER AI was kinda smart when I first made it, but it was still really quite random. The
   *  OBERON AI on the other hand actually THINKS! It guesses your possible moves and attack accordingly, it even
   *  searches for you if it can't find you, awesomeness :-)!
   *
   *  The OBERON AI uses a fixed set of "target variables" to identify what it is gunning for, for example: "player",
   *  "health", "levels", etc. (See "OBERON AI Engine Targeting Scheme"). The OBERON Engine also as a greatly improved
   *  ability to navigate through maps and a MUCH LOWER chance of getting stuck in a stupid place. I must admit though,
   *  that the AI can still be outsmarted, if you can think fast enough (The AI may take some time to navigate complex
   *  maps). Just if information sake, the OBERON Engine was actually a lot faster in the past and its thinking speed
   *  was drastically cut to make to game playable, otherwise the AI could find, attack and destroy you in 2 seconds
   *  flat. Now it can't move any faster than you can. Hehehehehehe!
   *
   *  @version    1.0       Feb 01, 2010 (STELLAR AI Engine)
   *  @version    1.0       Mar 18, 2010 (OBERON AI Engine)
   *  @version    3.4.1772  Aug 12, 2010 (Method Generel)
   *  @author     Radioactive Reds Gaming Studios (C) 2010
   *  @author     Byron Kleingeld, Microtech Technologies Inc(c) 2010
   */
   public void EnemyMove() {
      int bias = 0;
      int dir = 0;
      int think = 0;
      int gembias = -1;
      
      /**
      *  Line-Of-Sight calculations and variables
      * ==========================================================================================================
      *  The variables are used to calculate to distance to an object using a modification to the standard
      *  Phythagorain equation, that is:
      *
      *                                             (X^2 + Y^2) = Z^2
      *
      *  Our version on the other hand reads...
      *
      *                       ((locationY - objectY)^2 + (locationX - objectX)^2) = distance^2
      *
      *  ...then...
      *
      *                                      distance = (distance^2)^0.5
      *
      *  Which equates to the distance from the object rounded down to the nearest full tile.
      *
      *  The variables used are:
      *  ---------------------------------------------------------------------------------------------------------
      *  Name           Use
      *  ---------------------------------------------------------------------------------------------------------
      *  hpr            Distance from a Health Crate\Box
      *  lvr            Distance from a Level Crate
      *  wbr            Distance from a Weapon Crate
      *  mhr            Distance from an Elixir of Life
      *  mer            Distance from a Vial of Wisdom
      *  enm            Distance from the BLUE PLAYER
      *  arm            Distance from an Armor Item
      *  uns            Distance from the U-No-See Potion
      *  gns            Distance from the Ghosting Potion
      *  gembias        Distance from a Gem
      */
      int hpr = (int) Math.pow((Math.pow((double) (locationEnemyY - healthLocat.y), 2) + Math.pow((double) (locationEnemyX - healthLocat.x), 2)), 0.5);
      int lvr = (int) Math.pow((Math.pow((double) (locationEnemyY - levelboxLocat.y), 2) + Math.pow((double) (locationEnemyX - levelboxLocat.x), 2)), 0.5);
      int wbr = (int) Math.pow((Math.pow((double) (locationEnemyY - weaponboxLocat.y), 2) + Math.pow((double) (locationEnemyX - weaponboxLocat.x), 2)), 0.5);
      int mhr = (int) Math.pow((Math.pow((double) (locationEnemyY - megahealthLocat.y), 2) + Math.pow((double) (locationEnemyX - megahealthLocat.x), 2)), 0.5);
      int mer = (int) Math.pow((Math.pow((double) (locationEnemyY - megaexpLocat.y), 2) + Math.pow((double) (locationEnemyX - megaexpLocat.x), 2)), 0.5);
      int enm = (int) Math.pow((Math.pow((double) (locationEnemyY - locationPlayerY), 2) + Math.pow((double) (locationEnemyX - locationPlayerX), 2)), 0.5);
      int arm = (int) Math.pow((Math.pow((double) (locationEnemyY - armorLocat.y), 2) + Math.pow((double) (locationEnemyX - armorLocat.x), 2)), 0.5);
      int uns = (int) Math.pow((Math.pow((double) (locationEnemyY - unoseeLocat.y), 2) + Math.pow((double) (locationEnemyX - unoseeLocat.x), 2)), 0.5);
      int gns = (int) Math.pow((Math.pow((double) (locationEnemyY - ghostpotionLocat.y), 2) + Math.pow((double) (locationEnemyX - ghostpotionLocat.x), 2)), 0.5);
      
      if (gemHunterMode) {
         gembias = (int) Math.pow((Math.pow((double) (locationEnemyY - gemLocat.y), 2) + Math.pow((double) (locationEnemyX - gemLocat.x), 2)), 0.5);
      }
      
      /**
      *  OBERON AI Engine Targeting Scheme
      * ============================================================================================================
      *  The OBERON AI determines what it must target in terms of a set of "Target Strings". These target strings
      *  are:
      *
      *  String         Target
      * ------------------------------------------------------------------------------------------------------------
      *  health         Health Crate\Box
      *  megahealth     Elixir of Life
      *  levels         Level Crate
      *  megalevels     Vial of Wisdom
      *  gems           Gems
      *  weapons        Weapon Crate
      *  armor          Armor Item
      *  kohflag        C.T.F Flag
      *  haters         Non-Red Enemies that are in the BLUE PLAYER's team
      *  bots           Non-Red Enemies that are in the RED PLAYER's team
      *  player         The BLUE PLAYER
      *  random         Exploring the Map, no fixed location
      *  unosee         Invisibility Potion
      */
      boolean haters = false;
      Point local = null;
      if(!isZapped2) {
         /*
         *  This section contains the new OBERON AI Engine, this AI is deadlier than the STELLAR AI Engine
         */
         if ((unoseeLocat.x < mapsize.x) && uns <= enemyLineOfSight) {
            searchLocation = findFreeBlock();
            enemyTarget = "unosee";
         }
         else if(((((float) curHealthE / maxHealthE) * 100) <= healthlust) && (hpr <= 5 || mhr <= enemyLineOfSight)) {
            if(Math.min(Math.min(hpr,mhr), enemyLineOfSight) < enemyLineOfSight) {
               if(hpr < mhr) {
                  searchLocation = findFreeBlock();
                  enemyTarget = "health";
               }
               else if(hpr > mhr) {
                  searchLocation = findFreeBlock();
                  enemyTarget = "megahealth";
               }
               else {
                  enemyTarget = "health";
                  searchLocation = findFreeBlock();
               }
            }
         }
         else {
            if((!gemHunterMode && levelEnemy < (levelPlayer * 0.75f)) && (levelboxLocat.x < mapsize.x) && (lvr <= 5 || mer <= enemyLineOfSight)) {
               if(Math.min(Math.min(lvr,mer), 5) < 5) {
                  if(lvr < mer) {
                     searchLocation = findFreeBlock();
                     enemyTarget = "levels";
                  }
                  else if(lvr > mer)   {
                     searchLocation = findFreeBlock();
                     enemyTarget = "megalevels";
                  }
                  else {
                     searchLocation = findFreeBlock();
                     enemyTarget = "levels";
                  }
               }
            }
            else {
               if(gemHunterMode && (gembias <= enemyLineOfSight) && ((gemsP2 < gemsP1) || (gemsP2 < 20))) {
                  enemyTarget = "gems";
                  searchLocation = findFreeBlock();
               }
               else if((enemyWeapon == -1 && weaponboxLocat.x < mapsize.x) && wbr <= enemyLineOfSight) {
                  enemyTarget = "weapons";
                  searchLocation = findFreeBlock();
               } 
               else if(armorP2 < 25 && armorLocat.x < mapsize.x && arm <= enemyLineOfSight) {
                  enemyTarget = "armor";
                  searchLocation = findFreeBlock();
               }
               else {
                  if(flagowner != 2 && kohmode) {
                     enemyTarget = "kohflag";
                     searchLocation = findFreeBlock();
                  }
                  else if ((ghostpotionLocat.x < mapsize.x) && gns <= enemyLineOfSight) {
                     enemyTarget = "ghost_p";
                     searchLocation = findFreeBlock();
                  }
                  else {
                     for (int i = 0; i < aiEnemies.size(); i++) {
                        Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
                        local = aie.getLocat();
                        int enx = (int) Math.pow((Math.pow((double) (locationEnemyY - local.y), 2) + Math.pow((double) (locationEnemyX - local.x), 2)), 0.5);
                        haters = aie.enemyHater;   
                          
                        if (haters) {
                           if(enx <= enemyLineOfSight) {
                              if(!aie.u_no_see) {  
                                 enemyTarget = "haters";
                              }
                              searchLocation = findFreeBlock();
                              break;
                           }
                           else {
                              enemyTarget = "random";
                           }
                        }
                        else {
                           if(enx <= enemyLineOfSight) {
                              if(!aie.u_no_see && !DEATHMATCHMODE) {  
                                 enemyTarget = "bots";
                              }
                              searchLocation = findFreeBlock();
                              break;
                           }
                           else {
                              enemyTarget = "random";
                           }
                        }
                     }
                     if (!haters) {
                        if((!u_no_see1 && locationPlayerX < mapsize.x) && (!isGameOver && enm <= enemyLineOfSight)) {
                           enemyTarget = "player";
                           searchLocation = findFreeBlock();
                        }
                        else {
                           if(aiEnemies.size() < 1 && (isGameOver || isDead) && (enemyTarget.equals("random")))
                              enemyTarget = "random";
                        }
                     }
                  }
               }
            }
         }  
         
         if (enemyTarget.equals("player")) {
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = new Point(locationPlayerX, locationPlayerY);
            
            if (me.y > him.y || me.y < him.y) {
               bias = 0;
            }
            else if (me.x > him.x || me.x < him.x) {
               bias = 1;
            }
            
            if(recalls >= 6) {
               recalls = 0;
               bias = changeDirection(bias);
            }
            Oberon_SeekerBot (me, bias, him);
         }
         else if (enemyTarget.equals("haters")) {
            try {
               Point me = new Point(locationEnemyX, locationEnemyY);
               Point him = local;
            
               if (me.y > him.y || me.y < him.y) {
                  bias = 0;
               }
               else if (me.x > him.x || me.x < him.x) {
                  bias = 1;
               }
            
               if(recalls >= 6) {
                  recalls = 0;
                  bias = changeDirection(bias);
               }
               Oberon_SeekerBot (me, bias, him);
            }
            catch (Exception e) {
               e.printStackTrace();
               printDebugMessage("OBERONSEEKERBOT - EXCEPTION: " + e.getMessage() + ", Line: 5048");
            }
         }
         else if (enemyTarget.equals("unosee")) {
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = new Point (unoseeLocat.x - 1, unoseeLocat.y - 1);
            
            if (me.y > him.y || me.y < him.y) {
               bias = 0;
            }
            else if (me.x > him.x || me.x < him.x) {
               bias = 1;
            }
            
            if(recalls >= 6) {
               recalls = 0;
               bias = changeDirection(bias);
            }
            Oberon_SeekerBot (me, bias, him);
         }
         else if (enemyTarget.equals("ghost_p")) {
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = new Point (ghostpotionLocat.x - 1, ghostpotionLocat.y - 1);
            
            if (me.y > him.y || me.y < him.y) {
               bias = 0;
            }
            else if (me.x > him.x || me.x < him.x) {
               bias = 1;
            }
            
            if(recalls >= 6) {
               recalls = 0;
               bias = changeDirection(bias);
            }
            Oberon_SeekerBot (me, bias, him);
         }
         else if (enemyTarget.equals("bots")) {
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = local;
            
            try {
               if (me.y > him.y || me.y < him.y) {
                  bias = 0;
               }
               else if (me.x > him.x || me.x < him.x) {
                  bias = 1;
               }
               
               if(recalls >= 6) {
                  recalls = 0;
                  bias = changeDirection(bias);
               }
               Oberon_SeekerBot (me, bias, him);
            }
            catch(NullPointerException npe) {
               printDebugMessage("OBERONSEEKERBOT - EXCEPTION: " + npe.getMessage() + ", Line: 5104");
            }
         }
         else if (enemyTarget.equals("gems")) {
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = new Point (gemLocat.x - 1, gemLocat.y - 1);
            ;
            
            if (me.y > him.y || me.y < him.y) {
               bias = 0;
            }
            else if (me.x > him.x || me.x < him.x) {
               bias = 1;
            }
            
            if(recalls >= 6) {
               recalls = 0;
               bias = changeDirection(bias);
            }
            Oberon_SeekerBot (me, bias, him);
         }
         else if (enemyTarget.equals("kohflag")) {
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = new Point (kohlocat.x - 1, kohlocat.y - 1);
            
            if (me.y > him.y || me.y < him.y) {
               bias = 0;
            }
            else if (me.x > him.x || me.x < him.x) {
               bias = 1;
            }
            
            if(recalls >= 6) {
               recalls = 0;
               bias = changeDirection(bias);
            }
            Oberon_SeekerBot (me, bias, him);
         }
         else if (enemyTarget.equals("weapons")) {
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = new Point (weaponboxLocat.x - 1, weaponboxLocat.y - 1);
            
            if (me.y > him.y || me.y < him.y) {
               bias = 0;
            }
            else if (me.x > him.x || me.x < him.x) {
               bias = 1;
            }
            
            if(recalls >= 6) {
               recalls = 0;
               bias = changeDirection(bias);
            }
            Oberon_SeekerBot (me, bias, him);
         }
         else if (enemyTarget.equals("health")) {
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = new Point (healthLocat.x - 1, healthLocat.y - 1);
            
            if (me.y > him.y || me.y < him.y) {
               bias = 0;
            }
            else if (me.x > him.x || me.x < him.x) {
               bias = 1;
            }
            
            if(recalls >= 6) {
               recalls = 0;
               bias = changeDirection(bias);
            }
            Oberon_SeekerBot (me, bias, him);
         }
         else if (enemyTarget.equals("armor")) {
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = new Point (armorLocat.x - 1, armorLocat.y - 1);
            
            if (me.y > him.y || me.y < him.y) {
               bias = 0;
            }
            else if (me.x > him.x || me.x < him.x) {
               bias = 1;
            }
            
            if(recalls >= 6) {
               recalls = 0;
               bias = changeDirection(bias);
            }
            Oberon_SeekerBot (me, bias, him);
         }
         else if (enemyTarget.equals("megahealth")) {
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = new Point (megahealthLocat.x - 1, megahealthLocat.y - 1);
            
            if (me.y > him.y || me.y < him.y) {
               bias = 0;
            }
            else if (me.x > him.x || me.x < him.x) {
               bias = 1;
            }
            
            if(recalls >= 6) {
               recalls = 0;
               bias = changeDirection(bias);
            }
            Oberon_SeekerBot (me, bias, him);
         }
         else if (enemyTarget.equals("levels")) {
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = new Point (levelboxLocat.x - 1, levelboxLocat.y - 1);
            
            if (me.y > him.y || me.y < him.y) {
               bias = 0;
            }
            else if (me.x > him.x || me.x < him.x) {
               bias = 1;
            }
            
            if(recalls >= 6) {
               recalls = 0;
               bias = changeDirection(bias);
            }
            Oberon_SeekerBot (me, bias, him);
         }
         else if (enemyTarget.equals("megalevels")) {
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = new Point (megaexpLocat.x - 1, megaexpLocat.y - 1);
            
            if (me.y > him.y || me.y < him.y) {
               bias = 0;
            }
            else if (me.x > him.x || me.x < him.x) {
               bias = 1;
            }
            
            if(recalls >= 6) {
               recalls = 0;
               bias = changeDirection(bias);
            }
            Oberon_SeekerBot (me, bias, him);
         }
         else if (enemyTarget.equals("random")) {
            /**
            *  Find a random location to move to...
            */
            if(locationEnemyX == searchLocation.x && locationEnemyY == searchLocation.y)
               searchLocation = findFreeBlock();
            else if(searchLocation.x == -1 || searchLocation.y == -1)
               searchLocation = findFreeBlock();
            else if(searchLocation.x > (mapsize.x - 1) || searchLocation.y > (mapsize.y - 1))
               searchLocation = findFreeBlock();
                  
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = new Point (searchLocation.x, searchLocation.y);
            
            if (me.y > him.y || me.y < him.y) {
               bias = 0;
            }
            else if (me.x > him.x || me.x < him.x) {
               bias = 1;
            }
            
            if(recalls >= 6) {
               recalls = 0;
               bias = changeDirection(bias);
            }
            Oberon_SeekerBot (me, bias, him);
         }
      }
   }
   
   /**
   *  Krux 2.4.113 Method
   * ==================================================================================
   *  modif
   *  OBERON AI Method for incrementing the input Point by 1 within the x and y variables
   *  in a specified direction
   *
   *  @param      start       The input Point
   *  @param      direction   The direction of movement
   *  @return     The displaced Point variable
   *  @version    1.0  Mar 18, 2010
   *  @author     Radioactive Reds Gaming Studios (C) 2010
   */
   protected Point modif(Point start, int direction) {
      Point out = null;
      if (direction == 0) {
         out = new Point(start.x, start.y - 1);
      }
      else if (direction == 1) {
         out = new Point(start.x, start.y + 1);
      }
      else if (direction == 2) {
         out = new Point(start.x - 1, start.y);
      }
      else {
         out = new Point(start.x + 1, start.y);
      }
      return out;
   }
   
   /**
   *  Krux 2.4.113 Method
   * ==================================================================================
   *  changeDirection
   *  OBERON AI Method for mirroring input directions
   *
   *  @param      direction   The direction of movement
   *  @return     The direction code representing the opposite direction
   *  @version    1.0  Mar 18, 2010
   *  @author     Radioactive Reds Gaming Studios (C) 2010
   */
   protected int changeDirection(int direction) {
      int out = 0;
      if (direction == 0) {
         out = 1;
      }
      else if (direction == 1) {
         out = 0;
      }
      else if (direction == 2) {
         out = 3;
      }
      else {
         out = 2;
      }
      return out;
   }
   
   /**
   *  Krux 2.4.113 Method
   * ==================================================================================
   *  Oberon_SeekerBot
   *  OBERON AI Method for generating moves
   *
   *  @param      direction   The direction of movement
   *  @version    1.0  Mar 18, 2010
   *  @author     Radioactive Reds Gaming Studios (C) 2010
   */
   protected void Oberon_SeekerBot (Point seekPoint, int directionalBias, Point target) {
      int DirectionalBias = directionalBias;
      int SearchingIn = 0;
      int MovesAhead = 0;
      boolean tester = true;
      boolean overflow = false;
      
      while (tester) {
         if (DirectionalBias == 0) {
            // // printDebugMessage("Bias0");
            if (seekPoint.y > target.y) {
               SearchingIn = 0;
            }
            else if (seekPoint.y < target.y) {
               SearchingIn = 1;
            }
         }
      
         if (DirectionalBias == 1) {
            // // printDebugMessage("Bias1");
            if (seekPoint.x > target.x) {
               SearchingIn = 2;
            }
            else if (seekPoint.x < target.x) {
               SearchingIn = 3;
            }
         }
      
         tester = testBoundsAt(seekPoint, SearchingIn);
         
         if(MovesAhead == 0 && tester) {
            DirectionalBias = changeDirection(DirectionalBias);
            SearchingIn = changeDirection(SearchingIn);
            // // printDebugMessage("DirChange");
         }
         if(tester) {
            int memdir = 4;
            if(DirectionalBias == 0) {
               if (seekPoint.y >= target.y) {
                  if(testBoundsAt(seekPoint, 0)) {
                     Oberon_NestThisMove(1);
                     memdir = 1;
                  }
                  else {
                     Oberon_NestThisMove(0);
                     memdir = 0;
                  }
               }
               else if (seekPoint.y <= target.y) {
                  if(testBoundsAt(seekPoint, 1)) {
                     Oberon_NestThisMove(0);
                     memdir = 0;
                  }
                  else {
                     Oberon_NestThisMove(1);
                     memdir = 1;
                  }
               }
               if (seekPoint.x >= target.x) {
                  if(testBoundsAt(modif(seekPoint, memdir), 2)) {
                     Oberon_NestThisMove(3);
                     memdir = 3;
                  }
                  else {
                     Oberon_NestThisMove(2);
                     memdir = 2;
                  }
               }
               else if (seekPoint.x <= target.x) {
                  if(testBoundsAt(modif(seekPoint, memdir), 3)) {
                     Oberon_NestThisMove(2);
                     memdir = 2;
                  }
                  else {
                     Oberon_NestThisMove(3);
                     memdir = 3;
                  }
               }
            }
            else {
               if (seekPoint.x >= target.x) {
                  if(testBoundsAt(seekPoint, 2)) {
                     Oberon_NestThisMove(3);
                     memdir = 3;
                  }
                  else {
                     Oberon_NestThisMove(2);
                     memdir = 2;
                  }
               }
               else if (seekPoint.x <= target.x) {
                  if(testBoundsAt(seekPoint, 3)) {
                     Oberon_NestThisMove(2);
                     memdir = 2;
                  }
                  else {
                     Oberon_NestThisMove(3);
                     memdir = 3;
                  }
               }
               if (seekPoint.y >= target.y) {
                  if(testBoundsAt(modif(seekPoint, memdir), 0)) {
                     Oberon_NestThisMove(1);
                     memdir = 1;
                  }
                  else {
                     Oberon_NestThisMove(0);
                     memdir = 0;
                  }
               }
               else if (seekPoint.y <= target.y) {
                  if(testBoundsAt(modif(seekPoint, memdir), 1)) {
                     Oberon_NestThisMove(0);
                     memdir = 0;
                  }
                  else {
                     Oberon_NestThisMove(1);
                     memdir = 1;
                  }
               }
            }
            // // printDebugMessage("GenMove");
            break;
         }
         if(MovesAhead >= maxMoves / 2) {
            SearchingIn = changeDirection(SearchingIn);
         }
         if(MovesAhead == maxMoves) {
            tester = false;
            // // printDebugMessage("MoveOverflow");
            seekPoint = modif(seekPoint, SearchingIn);
            Oberon_NavigatorBot(seekPoint, changeDirection(DirectionalBias), target);
            recalls++;
            overflow = true;
         }
         MovesAhead++;
      }
      
      if(!tester && !overflow) {
         for(int i = 0; i < MovesAhead; i++) {
            // // printDebugMessage("Nesting");
            Oberon_NestThisMove(SearchingIn);
         }
      }
   }
   
   /**
   *  Krux 2.4.113 Method
   * ==================================================================================
   *  Oberon_NavigatorBot
   *  OBERON AI Method for generating moves
   *
   *  @param      direction   The direction of movement
   *  @version    1.0  Mar 18, 2010
   *  @author     Radioactive Reds Gaming Studios (C) 2010
   */
   protected void Oberon_NavigatorBot (Point seekPoint, int directionalBias, Point target) {
      int DirectionalBias = directionalBias;
      int SearchingIn = 0;
      int MovesAhead = 0;
      boolean tester = true;
      boolean overflow = false;
      
      while (tester) {
         if (DirectionalBias == 0) {
            // // printDebugMessage("Bias0");
            if (seekPoint.y > target.y) {
               SearchingIn = 0;
            }
            else if (seekPoint.y < target.y) {
               SearchingIn = 1;
            }
         }
      
         if (DirectionalBias == 1) {
            // // printDebugMessage("Bias1");
            if (seekPoint.x > target.x) {
               SearchingIn = 2;
            }
            else if (seekPoint.x < target.x) {
               SearchingIn = 3;
            }
         }
      
         tester = testBoundsAt(seekPoint, SearchingIn);
         
         if(MovesAhead == 0 && tester) {
            DirectionalBias = changeDirection(DirectionalBias);
            SearchingIn = changeDirection(SearchingIn);
            // // printDebugMessage("DirChange");
         }
         if(tester) {
            int memdir = 4;
            if(DirectionalBias == 0) {
               if (seekPoint.y >= target.y) {
                  if(testBoundsAt(seekPoint, 0)) {
                     Oberon_NestThisMove(1);
                     memdir = 1;
                  }
                  else {
                     Oberon_NestThisMove(0);
                     memdir = 0;
                  }
               }
               else if (seekPoint.y <= target.y) {
                  if(testBoundsAt(seekPoint, 1)) {
                     Oberon_NestThisMove(0);
                     memdir = 0;
                  }
                  else {
                     Oberon_NestThisMove(1);
                     memdir = 1;
                  }
               }
               if (seekPoint.x >= target.x) {
                  if(testBoundsAt(modif(seekPoint, memdir), 2)) {
                     Oberon_NestThisMove(3);
                     memdir = 3;
                  }
                  else {
                     Oberon_NestThisMove(2);
                     memdir = 2;
                  }
               }
               else if (seekPoint.x <= target.x) {
                  if(testBoundsAt(modif(seekPoint, memdir), 3)) {
                     Oberon_NestThisMove(2);
                     memdir = 2;
                  }
                  else {
                     Oberon_NestThisMove(3);
                     memdir = 3;
                  }
               }
            }
            else {
               if (seekPoint.x >= target.x) {
                  if(testBoundsAt(seekPoint, 2)) {
                     Oberon_NestThisMove(3);
                     memdir = 3;
                  }
                  else {
                     Oberon_NestThisMove(2);
                     memdir = 2;
                  }
               }
               else if (seekPoint.x <= target.x) {
                  if(testBoundsAt(seekPoint, 3)) {
                     Oberon_NestThisMove(2);
                     memdir = 2;
                  }
                  else {
                     Oberon_NestThisMove(3);
                     memdir = 3;
                  }
               }
               if (seekPoint.y >= target.y) {
                  if(testBoundsAt(modif(seekPoint, memdir), 0)) {
                     Oberon_NestThisMove(1);
                     memdir = 1;
                  }
                  else {
                     Oberon_NestThisMove(0);
                     memdir = 0;
                  }
               }
               else if (seekPoint.y <= target.y) {
                  if(testBoundsAt(modif(seekPoint, memdir), 1)) {
                     Oberon_NestThisMove(0);
                     memdir = 0;
                  }
                  else {
                     Oberon_NestThisMove(1);
                     memdir = 1;
                  }
               }
            }
            // // printDebugMessage("GenMove");
            break;
         }
         if(MovesAhead >= maxMoves / 2) {
            SearchingIn = changeDirection(SearchingIn);
         }
         if(MovesAhead == maxMoves) {
            tester = false;
            // // printDebugMessage("MoveOverflow");
            seekPoint = modif(seekPoint, SearchingIn);
            Oberon_NavigatorBot(seekPoint, changeDirection(DirectionalBias), target);
            recalls++;
            overflow = true;
         }
         MovesAhead++;
      }
      
      if(!tester && !overflow) {
         for(int i = 0; i < MovesAhead; i++) {
            Oberon_NestThisMove(SearchingIn);
         }
      }
   }

   /**
   *  Krux 2.4.113 Method
   * ==================================================================================
   *  Oberon_NestThisMove
   *  OBERON AI Method for nesting moves
   *
   *  @param      direction   The direction of movement
   *  @version    1.0  Mar 18, 2010
   *  @author     Radioactive Reds Gaming Studios (C) 2010
   */
   protected void Oberon_NestThisMove (int direction) {
      if(moves < maxMoves) {
         // // printDebugMessage("Nesting");
         movesBias[moves] = direction;
         moves++;
      }
   }
   		
	/*
   *  Damage Methods
	* ==================================================================================
	*	Controls the damage taken and done the you and other players
	*/
	
   /**
   *  Krux 2 Method
   * ==================================================================================
   *  doDamagePE
   *  Calculates and processes damage done by the player to the enemy
   */
   public void doDamagePE() {
      int index = 0;
      if (enduranceP2 >= strengthP1) {
         index = 1;
      }
      else  {
         index = strengthP1 - enduranceP2;
      }
    
      hpDrawn = true;
      hpTime = 25;
      if (playerWeapon == 6) {
         curHealthE = (0);
      }
      else if (playerWeapon == 2) {
         isPoisoned2 = true;
         poisonLevel2 += 2;
      }
      else if (playerWeapon == 3) {
         damageE = CalculateDamage(playerLevel, strengthP1, 20, enduranceP2);
         
         if(armorP2 > 0 && armorP2 > (damageE / 2)) {
            armorP2 -= (damageE / 2);
            damageE = (damageE / 2);
         }
         else if (armorP2 < (damageE / 2)) {
            damageE -= armorP2;
            armorP2 = 0;
         }
         
         curHealthE = (curHealthE - damageE);
         damageEnemyPaint = true;
         isZapped2 = true;
         zaptimer2 = (int) Math.round(Math.random() * 20);
      
      }
      else if (playerWeapon == 4) {
         damageE = CalculateDamage(playerLevel, strengthP1, 15, enduranceP2);
         
         if(armorP2 > 0 && armorP2 > (damageE / 2)) {
            armorP2 -= (damageE / 2);
            damageE = (damageE / 2);
         }
         else if (armorP2 < (damageE / 2)) {
            damageE -= armorP2;
            armorP2 = 0;
         }
         
         curHealthE = (curHealthE - damageE);
         damageEnemyPaint = true;
         
         int lifeget = damageE / 2;
      
         if((curHealthP + lifeget) > maxHealthP) {
            int temp = maxHealthP;
         
            maxHealthP = curHealthP + lifeget;
            curHealthP = (maxHealthP);
         
            hasAUX = true;
            AUX = "HP Maxed out from " + temp + " to " + maxHealthP;
         }
         else
            curHealthP = (curHealthP + lifeget);
      }
      else if (playerWeapon == 5) {
         damageE = CalculateDamage(playerLevel, strengthP1, 120, enduranceP2);
         
         if(armorP2 > 0 && armorP2 > (damageE / 2)) {
            armorP2 -= (damageE / 2);
            damageE = (damageE / 2);
         }
         else if (armorP2 < (damageE / 2)) {
            damageE -= armorP2;
            armorP2 = 0;
         }
         
         curHealthE = (curHealthE - damageE);
         damageEnemyPaint = true;
         isZapped2 = true;
         zaptimer2 = (int) Math.round(Math.random() * 50);
         isPoisoned2 = true;
         poisonLevel2 += 10;
      }
      else if (playerWeapon == 7) {
         damageE = CalculateDamage(playerLevel, strengthP1, 80, enduranceP2);
         damageP = damageE / 4; // Calculate Recoil Damage        
      	
         if(curHealthP < damageP) {
            playerWeapon = -1;
         }
         else {
            curHealthP = (curHealthP - damageP);
            damagePlayerPaint = true;
         
         /* (This weapon pierces armor)
         if(armorP2 > 0 && armorP2 > (damageE / 2)) {
            armorP2 -= (damageE / 2);
            damageE = (damageE / 2);
         }
         else if (armorP2 < (damageE / 2)) {
            damageE -= armorP2;
            armorP2 = 0;
         }
         */
         
         // Test for the radius damage
            for (int i = 0; i < aiEnemies.size(); i++) {
               Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
               Point typer = aie.getLocat();
            
               int bnp = (int) Math.pow((Math.pow((double) (typer.y - locationPlayerY), 2) + Math.pow((double) (typer.x - locationPlayerX), 2)), 0.5);
               int dam = CalculateDamage(playerLevel, strengthP1, 35, aie.endurance);
            
               if(bnp <= 3) {
                  if(aie.getHitWeapon7(dam)) {
                     try {
                        aiEnemies.removeElementAt(i);printDebugMessage("OBERONAI - DESTRUCTOR: " + "Vector Size: " + aiEnemies.size()); 
                     }
                     catch (Exception e) {
                        e.printStackTrace();
                        hasAUX = true;
                        AUX = "Something sinistar happened!";
                        printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage() + ", Line: 5959");
                     }
                  }
                  else {
                     aie.isPoisoned = true;
                     aie.poisonLevel += 1;
                  }
               }
            }
         
            curHealthE = (curHealthE - damageE);
            damageEnemyPaint = true;
            isPoisoned2 = true;
            poisonLevel2 += 1;
         }
      }
      else if (playerWeapon == 8) {
         doDamageWeapon7(locationPlayerX, locationPlayerY);
      }
      else {
         if(playerWeapon == 0)
            damageE = CalculateDamage(playerLevel, strengthP1, 35, enduranceP2);
         else if(playerWeapon == 1)
            damageE = CalculateDamage(playerLevel, strengthP1, 45, enduranceP2);
         else
            damageE = CalculateDamage(playerLevel, strengthP1, 18, enduranceP2);
         
         if(armorP2 > 0 && armorP2 > (damageE / 2)) {
            armorP2 -= (damageE / 2);
            damageE = (damageE / 2);
         }
         else if (armorP2 < (damageE / 2)) {
            damageE -= armorP2;
            armorP2 = 0;
         }
         
         curHealthE = (curHealthE - damageE);
         damageEnemyPaint = true;
      }
      if(playerWeapon != -1) { // If I have a weapon substract one AMMO from it
         if(pWeapLeft > 1) {
            pWeapLeft--; 
         }
         else {
            playerWeapon = -1;
         }
      }
      
      if(extremeRules)
         scrboard.score += HIT * 2;
      else
         scrboard.score += HIT;
         
      if(gemHunterMode) {
         gemsP2 -= (int) ((float) damageE / 10);
         
         if(gemsP2 <= 0) {
            gemsP2 = 0;
         }
         gems2 = new CGString(String.valueOf(gemsP2), 3, CGString.ALIGN_RIGHT, CGString.DIGITAL);
      }
      
      scoreStr.setText("" + scrboard.score);
      if(curHealthE <= 0) {
         playSound("krux/explode.wav");
         if(extremeRules)
            scrboard.score += (ENEMY_DEFEAT * enemyLevel * 2);
         else
            scrboard.score += (ENEMY_DEFEAT * enemyLevel);
         
         scoreStr.setText("" + scrboard.score);
         locationEnemyX = (mapsize.x * 2);
         locationEnemyY = (mapsize.y * 2);
         
         exp += EXPGained(enemyLevel, playerLevel);
         kills++;
         lastkill = gametimer;
         rebornE();
      }
   }
   
   /**
   *  Krux 3 RTS 7 Method
   * ==================================================================================
   *  doDamageWeapon7
   *  Applies the damage done by the "bluebottle" weapon
   *
   *  @param   x     The x locator of the origination point
   *  @param   y     The y locator of the origination point
   */
   protected void doDamageWeapon7(int x, int y) {
      Point from = new Point(x, y);
      int enp = (int) Math.pow((Math.pow((double) (locationEnemyY - from.y), 2) + Math.pow((double) (locationEnemyX - from.x), 2)), 0.5);
      int pnp = (int) Math.pow((Math.pow((double) (locationPlayerY - from.y), 2) + Math.pow((double) (locationPlayerX - from.x), 2)), 0.5);
      int damage = CalculateDamage(100, 200, 300, 50);
      
      {
         hpDrawn = true;
         hpTime = 25;
         
         curHealthE = (curHealthE - ((int) (damage / (enp + 0.0001f))));
         damageEnemyPaint = true;
         
         isZapped2 = true;
         zaptimer2 = (int) Math.round(Math.random() * ((mapsize.x - enp) * 200));
         isPoisoned2 = true;
         poisonLevel2 += (int) (200 / (enp + 0.0001f));
         
         if(curHealthE <= 0) {
            playSound("krux/explode.wav");
            if(extremeRules)
               scrboard.score += (ENEMY_DEFEAT * enemyLevel * 2);
            else
               scrboard.score += (ENEMY_DEFEAT * enemyLevel);
            
            scoreStr.setText("" + scrboard.score);
            locationEnemyX = (mapsize.x * 2);
            locationEnemyY = (mapsize.y * 2);
            
            exp += EXPGained(enemyLevel, playerLevel);
            rebornE();
         }
      }
      
      {
         curHealthP = (curHealthP - ((int) (damage / (pnp + 0.0001f))));
         damagePlayerPaint = true;
         isZapped1 = true;
         zaptimer1 = (int) Math.round(Math.random() * ((mapsize.x - pnp) * 200));
         isPoisoned1 = true;
         poisonLevel1 += (int) (200 / (pnp + 0.0001f));
         
         hasAUX = true;
         AUX = "You were hit by an atomic bomb!";
         
         if(curHealthP <= 0) {
            playSound("krux/explode.wav");
            locationPlayerX = (mapsize.x * 2);
            locationPlayerY = (mapsize.y * 2);
            isDead = true;
         }
      }
      
      for (int i = 0; i < aiEnemies.size(); i++) {
         Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
         Point typer = aie.getLocat();
            
         int bnp = (int) Math.pow((Math.pow((double) (typer.y - from.y), 2) + Math.pow((double) (typer.x - from.x), 2)), 0.5);
            
         if(aie.getHitWeapon7((int) (damage / (bnp + 0.0001f)))) {
            try {
               aiEnemies.removeElementAt(i);printDebugMessage("OBERONAI - DESTRUCTOR: " + "Vector Size: " + aiEnemies.size()); 
            }
            catch (Exception e) {
               e.printStackTrace();
               hasAUX = true;
               AUX = "Something sinistar happened!";
               printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage() + ", Line: 6118");
            }
         }
         else {
            aie.isZapped = true;
            aie.zaptimer = (int) Math.round(Math.random() * ((mapsize.x - bnp) * 200));
            aie.isPoisoned = true;
            aie.poisonLevel += (int) (200 / (bnp + 0.0001f));
         }
      }
   }
   
   /**
   *  Krux 3 Method
   * ==================================================================================
   *  addThirdGuy
   *  Add a "Third Guy"-based enemy
   *
   *  @param   x     X-Locator for the new enemy
   *  @param   y     Y-Locator for the new enemy
   */
   protected void addAIEnemy(int x, int y) {
      Oberon_AIDrivenEnemy aie = new Oberon_AIDrivenEnemy(this, (int) (Math.random() * (((levelPlayer + levelEnemy) / 2 + 2))), x, y);
      try {
         if(botLimitImposed && aiEnemies.size() == botLimit) {
            hasAUX = true;
            AUX = "Cannot add, Limit Reached";
         }
         else
            aiEnemies.addElement(aie);
      }
      catch (Exception e) {
         e.printStackTrace();
         printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage() + ", Line: 6151");
      }
      ;
      // printDebugMessage("Call Received");
   }
   
   protected void addAIEnemy(Point h) {
      Oberon_AIDrivenEnemy aie = new Oberon_AIDrivenEnemy(this, (int) (Math.random() * (((levelPlayer + levelEnemy) / 2 + 2))), h.x, h.y);
      try {
         if(botLimitImposed && aiEnemies.size() == botLimit) {
            hasAUX = true;
            AUX = "Cannot add, Limit Reached";
         }
         else
            aiEnemies.addElement(aie);
      }
      catch (Exception e) {
         e.printStackTrace();
         printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage() + ", Line: 6169");
      }
      ;
      // printDebugMessage("Call Received");
   }
   
   /**
   *  Krux 3 RTS 7 Method
   * ==================================================================================
   *  addThirdGuy
   *  Add a "Third Guy"-based enemy
   *
   *  @param   x     X-Locator for the new enemy
   *  @param   y     Y-Locator for the new enemy
   */
   protected void addThirdGuy(int x, int y) {
      Oberon_AIDrivenEnemy aie = new Oberon_AIDrivenEnemy(this, (int) (Math.random() * (((levelPlayer + levelEnemy) / 2 + 2))), x, y, true);
      try {
         if(botLimitImposed && aiEnemies.size() == botLimit) {
            hasAUX = true;
            AUX = "Cannot add, Limit Reached";
         }
         else
            aiEnemies.addElement(aie);
      }
      catch (Exception e) {
         e.printStackTrace();
         printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage() + ", Line: 6196");
      }
   }
   
   /**
   *  Krux 2 Method
   * ==================================================================================
   *  doDamageEP
   *  Calculates and processes damage done by the enemy to the player
   */
   public void doDamageEP() {
      if (enemyWeapon == 6) {
         curHealthP = (0);
      }
      else if (enemyWeapon == 8) {
         doDamageWeapon7(locationEnemyX, locationEnemyY);
      }
      else if (enemyWeapon == 2) {
         isPoisoned1 = true;
         poisonLevel1 += 2;
         hasAUX = true;
         AUX = "You have been poisoned";
      }
      else if (enemyWeapon == 3) {
         damageP = CalculateDamage(enemyLevel, strengthP2, 20, enduranceP1);
         
         if(armorP1 > 0 && armorP1 > (damageP / 2)) {
            armorP1 -= (damageP / 2);
            damageP = (damageP / 2);
         }
         else if (armorP1 < (damageE / 2)) {
            damageP -= armorP1;
            armorP1 = 0;
         }
         
         curHealthP = (curHealthP - damageP);
         damagePlayerPaint = true;
         isZapped1 = true;
         zaptimer1 = (int) Math.round(Math.random() * 20);
         hasAUX = true;
         AUX = "You have been zapped";
      }
      else if (enemyWeapon == 4) {
         damageP = CalculateDamage(enemyLevel, strengthP2, 15, enduranceP1);
         
         if(armorP1 > 0 && armorP1 > (damageP / 2)) {
            armorP1 -= (damageP / 2);
            damageP = (damageP / 2);
         }
         else if (armorP1 < (damageE / 2)) {
            damageP -= armorP1;
            armorP1 = 0;
         }
         
         curHealthP = (curHealthP - damageP);
         damagePlayerPaint = true;
         
         int lifeget = damageP / 2;
      
         if((curHealthE + lifeget) > maxHealthE) {
            int temp = maxHealthE;
         
            maxHealthE = curHealthE + lifeget;
            
            curHealthE = (maxHealthE);
         
            hasAUX = true;
            AUX = "HP Maxed out from " + temp + " to " + maxHealthE;
         }
         else
            curHealthE = (curHealthE + lifeget);
      }
      else if (enemyWeapon == 5) {
         damageP = CalculateDamage(enemyLevel, strengthP2, 120, enduranceP1);
         
         if(armorP1 > 0 && armorP1 > (damageP / 2)) {
            armorP1 -= (damageP / 2);
            damageP = (damageP / 2);
         }
         else if (armorP1 < (damageE / 2)) {
            damageP -= armorP1;
            armorP1 = 0;
         }
         
         curHealthP = (curHealthP - damageP);
         damagePlayerPaint = true;
         isZapped1 = true;
         zaptimer1 = (int) Math.round(Math.random() * 50);
         isPoisoned1 = true;
         poisonLevel1 += 10;
         hasAUX = true;
         AUX = "You have been intoxicated";
      }
      else if (enemyWeapon == 7) {
         damageE = CalculateDamage(enemyLevel, strengthP2, 80, enduranceP1);
         damageP = damageE / 4; // Calculate Recoil Damage        
      	
         if(curHealthE < damageP) {
            enemyWeapon = -1;
         }
         else {
            curHealthE = (curHealthE - damageP);
            damageEnemyPaint = true;
         
         // Test for the radius damage
            for (int i = 0; i < aiEnemies.size(); i++) {
               Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
               Point typer = aie.getLocat();
            
               int bnp = (int) Math.pow((Math.pow((double) (typer.y - locationEnemyY), 2) + Math.pow((double) (typer.x - locationEnemyX), 2)), 0.5);
               int dam = CalculateDamage(enemyLevel, strengthP2, 120, aie.endurance);
            
               if(bnp <= 3) {
                  if(aie.getHitWeapon7(dam)) {
                     try {
                        aiEnemies.removeElementAt(i);printDebugMessage("OBERONAI - DESTRUCTOR: " + "Vector Size: " + aiEnemies.size()); 
                     }
                     catch (Exception e) {
                        e.printStackTrace();
                        hasAUX = true;
                        AUX = "Something sinistar happened!";
                        printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage() + ", Line: 6318");
                     }
                  }
                  else {
                     aie.isPoisoned = true;
                     aie.poisonLevel += 1;
                  }
               }
            }
         
            curHealthP = (curHealthE - damageE);
            damagePlayerPaint = true;
            isPoisoned1 = true;
            poisonLevel1 += 1;
         }
      }
      else {
         if(enemyWeapon == 0)
            damageP = CalculateDamage(enemyLevel, strengthP2, 35, enduranceP1);
         else if (enemyWeapon == 1)
            damageP = CalculateDamage(enemyLevel, strengthP2, 45, enduranceP1);
         else
            damageP = CalculateDamage(enemyLevel, strengthP2, 18, enduranceP1);
         
         if(armorP1 > 0 && armorP1 > (damageP / 2)) {
            armorP1 -= (damageP / 2);
            damageP = (damageP / 2);
         }
         else if (armorP1 < (damageE / 2)) {
            damageP -= armorP1;
            armorP1 = 0;
         }
         
         curHealthP = (curHealthP - damageP);
         damagePlayerPaint = true;
      }
      if(enemyWeapon != -1) {
         if(eWeapLeft > 1) {
            eWeapLeft--; 
         }
         else {
            enemyWeapon = -1;
         }
      }
      scoreStr.setText("" + scrboard.score);
      
      if(gemHunterMode) {
         gemsP1 -= (int) ((double) damageP / 10);
         
         if(gemsP1 < 0) {
            gemsP1 = 0;
         }
         gems1 = new CGString(String.valueOf(gemsP1), 3, CGString.ALIGN_RIGHT, CGString.DIGITAL);
      }
      
      if(curHealthP <= 0) {
         playSound("krux/explode.wav");
         scoreStr.setText("" + scrboard.score);
         locationPlayerX = (mapsize.x * 2);
         locationPlayerY = (mapsize.y * 2);
         isDead = true;
         
      }
   }
   
/*
*  Recovery Methods
* ==================================================================================
*  Controls the recovery from Health Crates
*/

   /**
   *  Krux 2 Method
   * ==================================================================================
   *  getFlagP
   *  Calculates and processes the BLUE PLAYERS's ability to get the flag
   */
   public void getFlagP() {
      if(kohmode) {
         if(flagowner != 1) {
            scrboard.score += GETFLAG;
            
            scoreStr.setText("" + scrboard.score);
            kohtimer.setText("" + 500);
            flagowner = 1;
         }
      }
   }

   /**
   *  Krux 2 Method
   * ==================================================================================
   *  getFlagE
   *  Calculates and processes the RED PLAYERS's ability to get the flag
   */
   public void getFlagE() {
      if(kohmode) {
         if(flagowner != 2) {
            scoreStr.setText("" + scrboard.score);
            kohtimer.setText("" + 500);
            flagowner = 2;
         }
      }
   }
   
   /**
   *  Krux 3 RTS 8 Method
   * ==================================================================================
   *  getGemP
   *  Calculates and processes the BLUE PLAYERS's ability to get Gems Crates
   */
   public void getGemP() {
      scrboard.score += GEMGET * (gemType + 1);
      
      scoreStr.setText("" + scrboard.score);
      
      if(gemType == BLUE_GEM)
         gemsP1 += 1;
      else if (gemType == RED_GEM)
         gemsP1 += 2;
      else if (gemType == GREEN_GEM)
         gemsP1 += 5;
      else if (gemType == CYAN_GEM)
         gemsP1 += 10;
      else if (gemType == YELLOW_GEM)
         gemsP1 += 20;
      
      gems1 = new CGString(String.valueOf(gemsP1), 3, CGString.ALIGN_RIGHT, CGString.DIGITAL);
      
      gemLocat = findFreeBlock();
      gemType = (short) ((Math.random() * 4) + 1);
      
   }
   
   /**
   *  Krux 3 RTS 8 Method
   * ==================================================================================
   *  getGemE
   *  Calculates and processes the RED PLAYERS's ability to get Gems Crates
   */
   public void getGemE() {      
      if(gemType == BLUE_GEM)
         gemsP2 += 1;
      else if (gemType == RED_GEM)
         gemsP2 += 2;
      else if (gemType == GREEN_GEM)
         gemsP2 += 5;
      else if (gemType == CYAN_GEM)
         gemsP2 += 10;
      else if (gemType == YELLOW_GEM)
         gemsP2 += 20;
         
      gems2 = new CGString(String.valueOf(gemsP2), 3, CGString.ALIGN_RIGHT, CGString.DIGITAL);
      
      gemLocat = findFreeBlock();
      gemType = (short) ((Math.random() * 4) + 1);
      
   }

   /**
   *  Krux 2 Method
   * ==================================================================================
   *  rebornP
   *  Controls BLUE PLAYER respawning
   */
   public void rebornP() { // Player is revived
      try {
         maxHealthP = CalculateHP(lifeIVP1, lifeEVP1, playerLevel);
         revive++;
         if (!revivesUnlimited) {
            if (revive >= revivelimit) {
               if (cheatsUsed) {
               // insert the advanced cheat screen here
                  hasAUX = true;
                  AUX = "Cheater! You are not worthy!";
                  revive = revivelimit;
                  isDead = false;
                  isGameOver = true;
               }
               else {
                  isDead = false;
                  isGameOver = true;
                  scrboard.updateRates();
               
                  try {
                     FileOutputStream fstrm = new FileOutputStream(new File("scoreboard.adf"));
                     ObjectOutput ostrm = new ObjectOutputStream(fstrm);
                     ostrm.writeObject(scrboard);
                     ostrm.flush();
                     ostrm.close();
                     fstrm.close();
                  }
                  catch (Exception e) {
                     e.printStackTrace();
                     printDebugMessage("REVIVER - EXCEPTION: " + e.getMessage() + ", Line: 7037");
                  }
               
                  scorePosi = getHighScore(scrboard.score);
                  if (scorePosi == 5)
                     writeHighScore();
                  else
                     enteringHS = true;
               }
            }
            else {
               scrboard.score -= (DEFEAT * playerLevel);
            
               scoreStr.setText("" + scrboard.score);
               levelPlayer = (playerLevel);
            
               curHealthP = (maxHealthP);
               locationPlayerX = spawnPoint1.x;
               locationPlayerY = spawnPoint1.y;
               isDead = false;
               playerWeapon = -1;
               isPoisoned1 = false;
               poisonLevel1 = 0;
               isZapped1 = false;
               u_no_see1 = false;
            }
         }
         else {
            scrboard.score -= (DEFEAT * playerLevel);
            scoreStr.setText("" + scrboard.score);
            levelPlayer = (playerLevel);
            curHealthP = (maxHealthP);
            locationPlayerX = spawnPoint1.x;
            locationPlayerY = spawnPoint1.y;
            isDead = false;
            playerWeapon = -1;
            isPoisoned1 = false;
            poisonLevel1 = 0;
            isZapped1 = false;
            u_no_see1 = false;
         }
      }
      catch (Exception e) {
         e.printStackTrace();
         printDebugMessage("REVIVER - EXCEPTION: " + e.getMessage() + ", Line: 7081");
      }
   }

   /**
   *  Krux 2 Method
   * ==================================================================================
   *  rebornE
   *  Controls RED PLAYER respawning
   */
   public void rebornE() { // Enemy is reborn
      maxHealthE = CalculateHP(lifeIVP2, lifeEVP2, enemyLevel);
      levelEnemy = (enemyLevel);
      
      curHealthE = (maxHealthE);
      locationEnemyX = spawnPoint2.x;
      locationEnemyY = spawnPoint2.y;
      enemyWeapon = -1;
      isZapped2 = false;
      isPoisoned2 = false;
      u_no_see2 = false;
      poisonLevel2 = 0;
      
   }
   
   /*
   *  Scoring, Victory, Sound and Map Management Methods
   * ==================================================================================
   */
   
   /**
   *  Krux 3 Method
   * ==================================================================================
   *  getVictory
   *  Generates a victory condition for the specified player
   *
   *  @param   winner      The player to generate victory for
   */
   public void getVictory(int winner) {
      isGameOver = true;
      scrboard.updateRates();
   	
      try {
         FileOutputStream fstrm = new FileOutputStream(new File("scoreboard.adf"));
         ObjectOutput ostrm = new ObjectOutputStream(fstrm);
         ostrm.writeObject(scrboard);
         ostrm.flush();
         ostrm.close();
         fstrm.close();
      }
      catch (Exception e) {
         e.printStackTrace();
         printDebugMessage("GETVICTORY - EXCEPTION: " + e.getMessage() + ", Line: 7133");
      }
      
      isDead = true;
      locationPlayerX = mapsize.x * 2;
      locationPlayerY = mapsize.y * 2;
      
      if (cheatsOn) {
         hasAUX = true;
         AUX = "Cheater! You are not worthy!";
         revive = revivelimit;
         isDead = false;
      }
      else {
         if (winner == 1) {
            scorePosi = getHighScore(scrboard.score);
            if (scorePosi == 5)
               writeHighScore();
            else
               enteringHS = true;
         }
      }
   }

   /**
   *  Krux 3 RTS 7 Method
   * ==================================================================================
   *  getHighScore
   *  Calculates the position of the Player's score on the scoreboard
   *
   *  @param   scores      The score achieved by the player
   *  @return  The position of the score on the scoreboard
   */
   protected int getHighScore(int scores) {
      int scorePos = -1;
      int lastScore = 0;
      int loadScore = 0;
      for (scorePos = 0; scorePos < 5; scorePos++) {
         String[] tempo = tokenizer(highScoreList[scorePos], " ");
         loadScore = Integer.parseInt(tempo[1]);
         lastScore = Math.max(loadScore, scores);
         if(lastScore == scores)
            break;
      }
      return scorePos;
   }

   /**
   *  Krux 3 RTS 7 Method
   * ==================================================================================
   *  updateHighScore
   *  Sorts the High Score List
   *
   *  @param   pos      The position to add the new highscore to
   */
   protected void updateHighScore(int pos) {
      String[] temp = new String[5];
      for (int i = (pos - 1); i > -1; i--) {
         temp[i] = highScoreList[i];
      }
      temp[pos] = scoreInitial.toUpperCase() + " " + score;
      for (int i = (pos + 1); i < 5; i++) {
         temp[i] = highScoreList[i - 1];
      }
      
      for (int i = 0; i < 5; i++) {
         highScoreList[i] = temp[i];
      }
      writeHighScore();
   }
   
   /**
   *  Krux 3 RTS 7 Method
   * ==================================================================================
   *  writeHighScore
   *  Writes the kruxdata.adf file
   */
   protected void writeHighScore() {
      try {
         FileWriter out = new FileWriter("kruxdata.adf");
         for (int x = 0; x < 5; x++) {
            out.write(highScoreList[x] + "\r\n");
         }
         out.close();
      }
      catch (IOException e) {
         e.printStackTrace();
         printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage() + ", Line: 7220");
      }
         
      hs1 = new CGString(highScoreList[0], 20);
      hs2 = new CGString(highScoreList[1], 20);
      hs3 = new CGString(highScoreList[2], 20);
      hs4 = new CGString(highScoreList[3], 20);
      hs5 = new CGString(highScoreList[4], 20);
   
      showHighScores = true;
      showHSTime = 50;
   }
   
   /**
   *  Krux 2 Method
   * ==================================================================================
   *  playSound
   *  Plays a sound from a file
   *
   *  @param   resource    Specifies the sound resource locator
   */
   protected void playSound(String resource) {
   /*
   *  The most obscure method to date!
   *  Firstly I grab the file form inside the JAR package and write it to a temp file
   *  next I play to temp file. Easy!
   */
      if (soundOn) {
        try {
            // grab..
            InputStream fin  = getClass().getResourceAsStream(resource);
            int count = fin.read(soundBuffer, 0, soundBuffer.length);
            // ...write...
            FileOutputStream fout = new FileOutputStream(tempo);
            fout.write(soundBuffer, 0, count);
            fout.close();
            // ...play!
            dsound snd = new dsound(tempo);
            snd.playDSound();
        }
        catch (FileNotFoundException e) {
            printErrMeth(e, "PLAYSOUND", true); 
            printDebugMessage("PLAYSOUND - FileNotFoundException: " + e.toString() + ": " + e.getMessage() + ", Line: 7262");
        }
        catch (IOException e) {
            printErrMeth(e, "PLAYSOUND", true); 
            printDebugMessage("PLAYSOUND - IOException: " + e.toString() + ": " + e.getMessage() + ", Line: 7262");
        }
        catch (UnsupportedAudioFileException e) {
            printErrMeth(e, "PLAYSOUND", true); 
            printDebugMessage("PLAYSOUND - UnsupportedAudioFileException: " + e.toString() + ": " + e.getMessage() + ", Line: 7262");
        }
        catch (LineUnavailableException e) {
            printErrMeth(e, "PLAYSOUND", true); 
            printDebugMessage("PLAYSOUND - LineUnavailableException: " + e.toString() + ": " + e.getMessage() + ", Line: 7262");
        }
    }
}
   
   /*
   *  Utility Methods
   * ==================================================================================
   */
   
   /**
   *  Krux 3 Method
   * ==================================================================================
   *  tokenizer
   *  Breaks an input string up into tokens and returns an array of tokens
   *
   *  @param   input       The String to tokenize
   *  @param   delim       The delimiter to use in the tokenization
   *  @return  An array of tokens from the input string
   */
   protected String[] tokenizer(String input, String delim) {
      StringTokenizer st = new StringTokenizer(input, delim);
      String[] temp1 = new String[128];
      int counter = 0;
   	
      while(st.hasMoreTokens()) {
         temp1[counter] = st.nextToken();
         counter++;
      }
   	
      String[] temp2 = new String[counter];
   	
      for (int i = 0; i < counter; i++) {
         temp2[i] = temp1[i];
      }
   	
      return temp2;
   }
         
   /*
   *  Map Management Methods
   *  =================================================================================
   */
   
   /**
   *  Krux 3 Method
   * ==================================================================================
   *  resetMap
   *  Resets the map back to it default state
   */
   protected void resetMap() {
      maxHealthE = (strengthP2 + enduranceP2);
      levelEnemy = (enemyLevel);
      
      curHealthE = (maxHealthE);
      checkSpawnPoint("e");
      locationEnemyX = spawnPoint2.x;
      locationEnemyY = spawnPoint2.y;
      enemyWeapon = -1;
      levelPlayer = (playerLevel);
      
      curHealthP = (maxHealthP);
      checkSpawnPoint("p");
      locationPlayerX = spawnPoint1.x;
      locationPlayerY = spawnPoint1.y;
      isDead = false;
      playerWeapon = -1;
      
   }
   
   protected void checkSpawnPoint(String player) {
      boolean canSpawn = false;
      
      if(player == "e") {
         while (!canSpawn) {
            canSpawn = testBoundsAtLocat(spawnPoint2, 0);
            if(!canSpawn) {
               spawnPoint2 = findFreeBlock();
            }
         }
      } else if (player == "p") {
         while (!canSpawn) {
            canSpawn = testBoundsAtLocat(spawnPoint1, 0);
            if(!canSpawn) {
               spawnPoint1 = findFreeBlock();
            }
         }
      }
   }
	
   /**
   *  Krux 2 Method
   * ==================================================================================
   *  loadCustomMap
   *  Loads and interprets KMF maps from disc
   *
   *  @param      filename        The file from which to load the map
   */
   protected void loadCustomMap(File filename) {
      try {
         BufferedReader input = new BufferedReader(new FileReader(filename));
         String[] mapbuffer = new String[64];
         String temp = null;
         int counter = 0;
      	
         while((temp = input.readLine()) != null) {
            mapbuffer[counter] = temp;
            counter++;
         }
      	
         if(mapbuffer[0].equals("<KMF2MAP>")) {
            printDebugMessage("Map Loader: Redirected to Version 2");
            // This map is not KMF, but KMF2 for move to loadCustomMap3
            loadCustomMap3(filename);
         }
         else if(mapbuffer[0].equals("<KMF3MAP>")) {
            printDebugMessage("Map Loader: Redirected to Version 3");
            // This map is not KMF, but KMF3 for move to loadCustomMap3X
            loadCustomMap3X(filename);
         }
         else {
         
            mapName = mapbuffer[1];
            if(!mapbuffer[2].equals("default")) {
               printDebugMessage("Map Loader: Default Floor Type Set");
               SPRITE_FLOOR = new ImageIcon(mapbuffer[2]).getImage();
            }
            if(!mapbuffer[3].equals("default")) {
               printDebugMessage("Map Loader: Default Static Boundary Type Set");
               SPRITE_STATICB = new ImageIcon(mapbuffer[3]).getImage();
            }
            if(!mapbuffer[4].equals("default")) {
               printDebugMessage("Map Loader: Default Mobile Boundary Type Set");
               SPRITE_MOBILEB = new ImageIcon(mapbuffer[4]).getImage();
            }
         
            printDebugMessage("Map Loader: Map Loading Begins...");
            String[] tempo = tokenizer(mapbuffer[5], ",");
            mapsize = new Point(Integer.parseInt(tempo[0]),Integer.parseInt(tempo[1]));
            OFFSCREEN = new Point (mapsize.x * 2, mapsize.y * 2);
         
            maxbounds = (mapsize.x * mapsize.y); 
         
            bounds = new Point[maxbounds];
            mobileBounds = new Point[maxbounds];
            mBoundClass = new char[maxbounds];
            trackingbounds = new Point[trackingBoundsMax];
                  
            for(int h = 0; h < maxbounds; h ++) {
               bounds[h] = new Point(0,0);
            }
            for(int h = 0; h < maxbounds; h ++) {
               mobileBounds[h] = new Point(0,0);
            }
            for(int h = 0; h < trackingbounds.length; h ++) {
               trackingbounds[h] = new Point(0,0);
            }
         
            mapdata = new String[mapsize.x][mapsize.y];
         
            for (int y = 0; y < mapsize.y; y++) {
               String tempd[] = tokenizer(mapbuffer[y + 7], ",");
               for (int x = 0; x < mapsize.x; x++) {
                  mapdata[x][y] = tempd[x];
               }
            }
            printDebugMessage("Map Loader: Map Loading Complete");
            printDebugMessage("Map Loader: Map Interpretation Begins...");
            for (int x = 0; x < mapsize.x; x++) {
               for (int y = 0; y < mapsize.y; y++) {
                  if(mapdata[x][y].toLowerCase().equals("s")) {
                     addBounds(x + 1, y + 1, BOUND_SOLID);
                     printDebugMessage("Map Loader: Static Boundary Added (" + x + ", " + y + ")");
                  }
                  else if(mapdata[x][y].toLowerCase().equals("m")) {
                     addBounds(x + 1, y + 1, BOUND_SHIFT);
                     printDebugMessage("Map Loader: Mobile Boundary Added (" + x + ", " + y + ")");
                  }
                  else if(mapdata[x][y].toLowerCase().equals("p")) {
                     spawnPoint1 = new Point(x,y);
                     printDebugMessage("Map Loader: Player Spawn Set (" + x + ", " + y + ")");
                  }
                  else if(mapdata[x][y].toLowerCase().equals("e")) {
                     spawnPoint2 = new Point(x,y);
                     printDebugMessage("Map Loader: Enemy Spawn Set (" + x + ", " + y + ")");
                  }
                  else if(mapdata[x][y].toLowerCase().equals("k")) {
                     if(kohmode) {
                        kohlocat = new Point(x + 1,y + 1);
                     }
                     printDebugMessage("Map Loader: Flag Location Set (" + x + 1 + ", " + y + 1 + ")");
                  }
                  else {
                  }
               } 
            }
            printDebugMessage("Map Loader: Map Interpretation Complete");
         }
      }
      catch (Exception f) {
         printErrMeth(f, "LOADCUSTOMMAP", false);
         printDebugMessage("Exception: " + f.toString() + ": " + f.getMessage() + ", Line: 7441");
      }
   }
   
   /**
   *  Krux 3 Method
   * ==================================================================================
   *  loadCustomMap3
   *  Loads and interprets KMF2 maps from disc
   *
   *  @param      filename        The file from which to load the map
   */
   protected void loadCustomMap3(File filename) {
      try {
         BufferedReader input = new BufferedReader(new FileReader(filename));
         String[] mapbuffer = new String[64];
         String temp = null;
         int counter = 0;
      	
         while((temp = input.readLine()) != null) {
            mapbuffer[counter] = temp;
            counter++;
         }
      	
         mapName = mapbuffer[1];
         if(!mapbuffer[2].equals("default")) {
            SPRITE_FLOOR = new ImageIcon(mapbuffer[2]).getImage();
            printDebugMessage("Map Loader: Default Floor Type Set");
         }
         if(!mapbuffer[3].equals("default")) {
            SPRITE_STATICB = new ImageIcon(mapbuffer[3]).getImage();
            printDebugMessage("Map Loader: Default Static Boundary Type Set");
         }
         if(!mapbuffer[4].equals("default")) {
            SPRITE_MOBILEB = new ImageIcon(mapbuffer[4]).getImage();
            printDebugMessage("Map Loader: Default Mobile Boundary Type Set");
         }
         if(!mapbuffer[5].equals("default")) {
            floortile1 = new ImageIcon(mapbuffer[5]).getImage();
            printDebugMessage("Map Loader: Floor Type 1 Set");
         }
         if(!mapbuffer[6].equals("default")) {
            floortile2 = new ImageIcon(mapbuffer[6]).getImage();
            printDebugMessage("Map Loader: Floor Type 2 Set");
         }
         if(!mapbuffer[7].equals("default")) {
            floortile3 = new ImageIcon(mapbuffer[7]).getImage();
            printDebugMessage("Map Loader: Floor Type 3 Set");
         }
         
         printDebugMessage("Map Loader: Map Loading Begins...");
         String[] tempo = tokenizer(mapbuffer[8], ",");
         mapsize = new Point(Integer.parseInt(tempo[0]),Integer.parseInt(tempo[1]));
         OFFSCREEN = new Point (mapsize.x * 2, mapsize.y * 2);
         
         maxbounds = (mapsize.x * mapsize.y); 
         
         bounds = new Point[maxbounds];
         mobileBounds = new Point[maxbounds];
         mBoundClass = new char[maxbounds];
         trackingbounds = new Point[trackingBoundsMax]; 
                  
         for(int h = 0; h < maxbounds; h ++) {
            bounds[h] = new Point(0,0);
         }
         for(int h = 0; h < maxbounds; h ++) {
            mobileBounds[h] = new Point(0,0);
         }
         for(int h = 0; h < trackingbounds.length; h ++) {
            trackingbounds[h] = new Point(0,0);
         }
      	
         mapdata = new String[mapsize.x][mapsize.y];
      	
         floorsV = new Point[mapsize.x * mapsize.y];
         floors1 = new Point[mapsize.x * mapsize.y];
         floors2 = new Point[mapsize.x * mapsize.y];
         floors3 = new Point[mapsize.x * mapsize.y];
      	
         for (int y = 0; y < mapsize.y; y++) {
            String tempd[] = tokenizer(mapbuffer[y + 10], ",");
            for (int x = 0; x < mapsize.x; x++) {
               mapdata[x][y] = tempd[x];
            }
         }
         
         printDebugMessage("Map Loader: Map Loading Complete");
         printDebugMessage("Map Loader: Map Interpretation Begins...");
      	
         for (int x = 0; x < mapsize.x; x++) {
            for (int y = 0; y < mapsize.y; y++) {
               if(mapdata[x][y].toLowerCase().equals("s")) {
                  addBounds(x + 1, y + 1, BOUND_SOLID);
                  printDebugMessage("Map Loader: Static Boundary Added (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("1")) {
                  addFloorTileDesign(x, y, 1);
                  printDebugMessage("Map Loader: Floor Type 1 Added (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("2")) {
                  addFloorTileDesign(x, y, 2);
                  printDebugMessage("Map Loader: Floor Type 2 Added (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("3")) {
                  addFloorTileDesign(x, y, 3);
                  printDebugMessage("Map Loader: Floor Type 3 Added (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("m")) {
                  addBounds(x + 1, y + 1, BOUND_SHIFT);
                  printDebugMessage("Map Loader: Mobile Boundary Added (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("p")) {
                  spawnPoint1 = new Point(x,y);
                  printDebugMessage("Map Loader: Player Spawn Set (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("e")) {
                  spawnPoint2 = new Point(x,y);
                  printDebugMessage("Map Loader: Enemy Spawn Set (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("k")) {
                  if(kohmode) {
                     kohlocat = new Point(x + 1,y + 1);
                     printDebugMessage("Map Loader: Flag Location Set (" + x + 1 + ", " + y + 1 + ")");
                  }
               }
               else {
               }
               printDebugMessage("Map Loader: Map Interpretation Complete");
            }
         }
      }
      catch (Exception f) {
         printErrMeth(f, "LOADCUSTOMMAP", false);
         printDebugMessage("Exception: " + f.toString() + ": " + f.getMessage() + ", Line: 7574");
      }
   }

   /**
   *  Krux 3 RTS 7 Method
   * ==================================================================================
   *  loadCustomMap3X
   *  Loads and interprets KMF3 maps from disc
   *
   *  @param      filename        The file from which to load the map
   */
   protected void loadCustomMap3X(File filename) {
      try {
         BufferedReader input = new BufferedReader(new FileReader(filename));
         String[] mapbuffer = new String[64];
         String temp = null;
         int counter = 0;
      	
         while((temp = input.readLine()) != null) {
            mapbuffer[counter] = temp;
            counter++;
         }
      	
         mapName = mapbuffer[1];
         if(!mapbuffer[2].equals("default")) {
            SPRITE_FLOOR = new ImageIcon(mapbuffer[2]).getImage();
            printDebugMessage("Map Loader: Default Floor Type Set");
         }
         if(!mapbuffer[3].equals("default")) {
            SPRITE_STATICB = new ImageIcon(mapbuffer[3]).getImage();
            printDebugMessage("Map Loader: Default Static Boundary Type Set");
         }
         if(!mapbuffer[4].equals("default")) {
            SPRITE_MOBILEB = new ImageIcon(mapbuffer[4]).getImage();
            printDebugMessage("Map Loader: Default Mobile Boundary Type Set");
         }
         if(!mapbuffer[5].equals("default")) {
            OBJECT_HBORDER = new ImageIcon(mapbuffer[5]).getImage();
            printDebugMessage("Map Loader: Default Tracking Boundary Type Set");
         }
         if(!mapbuffer[6].equals("default")) {
            floortile1 = new ImageIcon(mapbuffer[6]).getImage();
            printDebugMessage("Map Loader: Floor Type 1 Set");
         }
         if(!mapbuffer[7].equals("default")) {
            floortile2 = new ImageIcon(mapbuffer[7]).getImage();
            printDebugMessage("Map Loader: Floor Type 2 Set");
         }
         if(!mapbuffer[8].equals("default")) {
            floortile3 = new ImageIcon(mapbuffer[8]).getImage();
            printDebugMessage("Map Loader: Floor Type 3 Set");
         }
         if(!mapbuffer[9].equals("default")) {
            floortile4 = new ImageIcon(mapbuffer[9]).getImage();
            printDebugMessage("Map Loader: Floor Type 4 Set");
         }
         if(!mapbuffer[10].equals("default")) {
            floortile5 = new ImageIcon(mapbuffer[10]).getImage();
            printDebugMessage("Map Loader: Floor Type 5 Set");
         }
         
         printDebugMessage("Map Loader: Map Loading Begins...");
         String[] tempo = tokenizer(mapbuffer[11], ",");
         mapsize = new Point(Integer.parseInt(tempo[0]),Integer.parseInt(tempo[1]));
         OFFSCREEN = new Point (mapsize.x * 2, mapsize.y * 2);
         
         maxbounds = (mapsize.x * mapsize.y); 
         trackingBoundsMax = maxbounds / mapsize.x; 
         
         bounds = new Point[maxbounds];
         mobileBounds = new Point[maxbounds];
         mBoundClass = new char[maxbounds];
         trackingbounds = new Point[trackingBoundsMax]; 
      	
         floorsV = new Point[mapsize.x * mapsize.y];
         floors1 = new Point[mapsize.x * mapsize.y];
         floors2 = new Point[mapsize.x * mapsize.y];
         floors3 = new Point[mapsize.x * mapsize.y];
         floors4 = new Point[mapsize.x * mapsize.y];
         floors5 = new Point[mapsize.x * mapsize.y];
                  
         for(int h = 0; h < maxbounds; h ++) {
            bounds[h] = new Point(0,0);
         }
         for(int h = 0; h < maxbounds; h ++) {
            mobileBounds[h] = new Point(0,0);
         }
         for(int h = 0; h < trackingbounds.length; h ++) {
            trackingbounds[h] = new Point(0,0);
         }
      	
         mapdata = new String[mapsize.x][mapsize.y];
      	
         for (int y = 0; y < mapsize.y; y++) {
            String tempd[] = tokenizer(mapbuffer[y + 13], ",");
            for (int x = 0; x < mapsize.x; x++) {
               mapdata[x][y] = tempd[x];
            }
         }
         
         printDebugMessage("Map Loader: Map Loading Complete");
         printDebugMessage("Map Loader: Map Interpretation Begins...");
         
         for (int x = 0; x < mapsize.x; x++) {
            for (int y = 0; y < mapsize.y; y++) {
               if(mapdata[x][y].toLowerCase().equals("s")) {
                  addBounds(x + 1, y + 1, BOUND_SOLID);
                  printDebugMessage("Map Loader: Static Boundary Added (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("1")) {
                  addFloorTileDesign(x, y, 1);
                  printDebugMessage("Map Loader: Floor Type 1 Added (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("2")) {
                  addFloorTileDesign(x, y, 2);
                  printDebugMessage("Map Loader: Floor Type 2 Added (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("3")) {
                  addFloorTileDesign(x, y, 3);
                  printDebugMessage("Map Loader: Floor Type 3 Added (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("4")) {
                  addFloorTileDesign(x, y, 4);
                  printDebugMessage("Map Loader: Floor Type 4 Added (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("5")) {
                  addFloorTileDesign(x, y, 5);
                  printDebugMessage("Map Loader: Floor Type 5 Added (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("v")) {
                  addFloorTileDesign(x, y, 99);
                  printDebugMessage("Map Loader: Floor Type \"Void\" Added (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("m")) {
                  addBounds(x + 1, y + 1, BOUND_SHIFT);
                  printDebugMessage("Map Loader: Mobile Boundary Added (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("t")) {
                  addBounds(x + 1, y + 1, BOUND_TRACK);
                  printDebugMessage("Map Loader: Tracking Boundary Added (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("p")) {
                  spawnPoint1 = new Point(x,y);
                  printDebugMessage("Map Loader: Player Spawn Set (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("e")) {
                  spawnPoint2 = new Point(x,y);
                  printDebugMessage("Map Loader: Enemy Spawn Set (" + x + ", " + y + ")");
               }
               else if(mapdata[x][y].toLowerCase().equals("k")) {
                  if(kohmode) {
                     kohlocat = new Point(x + 1,y + 1);
                     printDebugMessage("Map Loader: Flag Location Set (" + x + 1 + ", " + y + 1 + ")");
                  }
               }
               else {
               }
            }
         }
         printDebugMessage("Map Loader: Map Interpretation Complete");
      }
      catch (Exception f) {
         printErrMeth(f, "LOADCUSTOMMAP", false);
         printDebugMessage("Exception: " + f.toString() + ": " + f.getMessage() + ", Line: 7738");
      }
   }
   
   /**
   *  Krux 3 Method
   * ==================================================================================
   *  addFloorTileDesign
   *  Specifies a custom design for a floortile
   *
   *  @param      x              The floortile locator X variable
   *  @param      y              The floortile locator Y variable
   *  @param      design         The number of the design to use
   */
   
   protected void addFloorTileDesign(int x,int y, int design) {
      // Note: Input range for the design variable is 1 - 5 else
      // this method doesn't do anything
      if (design == 1) {
         floors1[floors1cnt] = new Point(x, y);
         floors1cnt++;
      }
      else if (design == 2) {
         floors2[floors2cnt] = new Point(x, y);
         floors2cnt++;
      }
      else if (design == 3) {
         floors3[floors3cnt] = new Point(x, y);
         floors3cnt++;
      }
      else if (design == 4) {
         floors4[floors4cnt] = new Point(x, y);
         floors4cnt++;
      }
      else if (design == 5) {
         floors5[floors5cnt] = new Point(x, y);
         floors5cnt++;
      }
      else if (design == 99) {
         floorsV[floorsVcnt] = new Point(x, y);
         floorsVcnt++;
      }
      else {
      }
   }
   
	/*
   *  Movement Generating Methods
	* ==================================================================================
	*  Controls the movements and AI of Enemies
	*/
	 
	/**
   *  Krux 2 Method
   * ==================================================================================
   *  getEnemyMove
   *  Gets a move for the RED PLAYER
   *
   *  This is the heart of the Krux(c) Enemy AI
	*	All it needs is an Integer and it makes movement for you
   *
   *  Spoiler Note: This "AI" does not "think", it just tasks the enemy in a random
   *                direction If you want smarter AI, write it yourself there is simply
   *                not enough time to create complex AI for this application!
   *
   *  @param      direction     The direction of movement
   */
						  
   public void getEnemyMove(int direction) { // The enemy's movement method
      if(direction == UP) { // enemy moves up
         eyesE = 0;
         restrictE = false;
         if (locationEnemyY == 0) { // If I am against the side of the grid, I cannot move...
         }
         else if ((locationEnemyY - 1) == locationPlayerY && locationEnemyX == locationPlayerX) {
            doDamageEP(); // If I am about to collide with Player, I must damage him...
         }
         else {
            restrictE = testEnemyBounds(0);
            if (restrictE) { // If I am about to collide with a boundary, I must not move...
            }
            else {
               locationEnemyY--; // If all goes good move me up!
               
            }
         }
         for (int i = 0; i < aiEnemies.size(); i++) { // check if I can damage a bot
            Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
            Point typer = aie.getLocat();
            if ((locationEnemyY - 1) == typer.y && locationEnemyX == typer.x) {
               if(aie.getHitE()) {
                  try {
                     exp_E += EXPGained(aie.enemyLevel, enemyLevel);
                     expDrawn = true;
                     expTime = 25;
                     aiEnemies.removeElementAt(i);printDebugMessage("OBERONAI - DESTRUCTOR: " + "Vector Size: " + aiEnemies.size()); 
                  }
                  catch (Exception e) {
                     e.printStackTrace();
                     hasAUX = true;
                     AUX = "Something sinistar happened!";
                     aiEnemies.removeElementAt(i);printDebugMessage("OBERONAI - DESTRUCTOR: " + "Vector Size: " + aiEnemies.size()); 
                     printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage() + ", Line: 7839");
                  }
                  ;
               }
            }
         }
      }
      /* I did not comment on the other directions because they're all essentially to same */
      if(direction == DOWN) { // enemy moves down
         eyesE = 1;
         restrictE = false;
         if (locationEnemyY == (mapsize.y - 1)) {
         }
         else if ((locationEnemyY + 1) == locationPlayerY && locationEnemyX == locationPlayerX) {
            doDamageEP();
         }
         else {
            restrictE = testEnemyBounds(1);
            if (restrictE) {
            }
            else {
               locationEnemyY++;
               
            }
         }
         for (int i = 0; i < aiEnemies.size(); i++) {
            Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
            Point typer = aie.getLocat();
            if ((locationEnemyY + 1) == typer.y && locationEnemyX == typer.x) {
               if(aie.getHitE()) {
                  try {
                     exp_E += EXPGained(aie.enemyLevel, enemyLevel);
                     expDrawn = true;
                     expTime = 25;
                     aiEnemies.removeElementAt(i);
                     printDebugMessage("OBERONAI - DESTRUCTOR: " + "Vector Size: " + aiEnemies.size()); 
                  }
                  catch (Exception e) {
                     e.printStackTrace();
                     hasAUX = true;
                     AUX = "Something sinistar happened!";
                     printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage() + ", Line: 7879");
                  }
                  ;
               }
            }
         }
      }	
      if(direction == LEFT) { // enemy moves left
         eyesE = 2;
         restrictE = false;
         if (locationEnemyX == 0) {
         }
         else if ((locationEnemyX - 1) == locationPlayerX && locationEnemyY == locationPlayerY) {
            doDamageEP();
         }
         else {
            restrictE = testEnemyBounds(2);
            if (restrictE) {
            }
            else {
               locationEnemyX--;
               
            }
         }
         for (int i = 0; i < aiEnemies.size(); i++) {
            Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
            Point typer = aie.getLocat();
            if ((locationEnemyX - 1) == typer.x && locationEnemyY == typer.y) {
               if(aie.getHitE()) {
                  try {
                     exp_E += EXPGained(aie.enemyLevel, enemyLevel);
                     expDrawn = true;
                     expTime = 25;
                     aiEnemies.removeElementAt(i);printDebugMessage("OBERONAI - DESTRUCTOR: " + "Vector Size: " + aiEnemies.size()); 
                  }
                  catch (Exception e) {
                     e.printStackTrace();
                     hasAUX = true;
                     AUX = "Something sinistar happened!";
                     printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage() + ", Line: 7918");
                  }
                  ;
               }
            }
         }
      }	 
      if(direction == RIGHT) { // enemy moves right
         eyesE = 3;
         restrictE = false;
         if (locationEnemyX == (mapsize.x - 1)) {
         }
         else if ((locationEnemyX + 1) == locationPlayerX && locationEnemyY == locationPlayerY) {
            
            doDamageEP();
         }
         else {
            restrictE = testEnemyBounds(3);
            if (restrictE) {
            }
            else {
               locationEnemyX++;
               
            }
         }
         for (int i = 0; i < aiEnemies.size(); i++) {
            Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
            Point typer = aie.getLocat();
            if ((locationEnemyX + 1) == typer.x && locationEnemyY == typer.y) {
               if(aie.getHitE()) {
                  try {
                     exp_E += EXPGained(aie.enemyLevel, enemyLevel);
                     expDrawn = true;
                     expTime = 25;
                     aiEnemies.removeElementAt(i);printDebugMessage("OBERONAI - DESTRUCTOR: " + "Vector Size: " + aiEnemies.size()); 
                  }
                  catch (Exception e) {
                     e.printStackTrace();
                     hasAUX = true;
                     AUX = "Something sinistar happened!";
                     printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage() + ", Line: 7958");
                  }
                  ;
               }
            }
         }
      }	 	 		 	
   }
   
   /*
   *  Boundary Testing Methods 
   * ==================================================================================
	*  Tests and controls the movements of players
	*/
        
	/**
   *  Krux 2 Method
   * ==================================================================================
   *  testEnemyBounds
   *  Tests the possibility of movement for the RED PLAYER
   *
   *  @param      direction     The direction of movement
   *  @return     a boolean representing for Object can move or not
   */
   public boolean testEnemyBounds(int direction) { // Test if the enemy can move
      boolean out = false;
      for(int x = 0; x < maxbounds; x++) {
         if(direction == UP){ //up
            if ((locationEnemyY - 1) == (bounds[x].y - 1) && locationEnemyX == (bounds[x].x - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if ((locationEnemyY - 1) == (mobileBounds[x].y - 1) && locationEnemyX == (mobileBounds[x].x - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if ((locationEnemyY - 1) == (healthLocat.y - 1) && locationEnemyX == (healthLocat.x - 1)) {
               getItem(MAIN_ENEMY, HEALTH);
            }
            else if ((locationEnemyY - 1) == (armorLocat.y - 1) && locationEnemyX == (armorLocat.x - 1)) {
               getItem(MAIN_ENEMY, ARMOR);
            }
            else if ((locationEnemyY - 1) == (unoseeLocat.y - 1) && locationEnemyX == (unoseeLocat.x - 1)) {
               getItem(MAIN_ENEMY, UNOSEE_POTION);
            }
            else if ((locationEnemyY - 1) == (gemLocat.y - 1) && locationEnemyX == (gemLocat.x - 1)) {
               getGemE();
            }
            else if ((locationEnemyY - 1) == (megahealthLocat.y - 1) && locationEnemyX == (megahealthLocat.x - 1)) {
               getItem(MAIN_ENEMY, MEGA_HEALTH);
            }
            else if ((locationEnemyY - 1) == (megaexpLocat.y - 1) && locationEnemyX == (megaexpLocat.x - 1)) {
               getItem(MAIN_ENEMY, MEGA_EXPERIENCE);
            }
            else if ((locationEnemyY - 1) == (ghostpotionLocat.y - 1) && locationEnemyX == (ghostpotionLocat.x - 1)) {
               getItem(MAIN_ENEMY, GHOST_POTION);
            }
            else if ((locationEnemyY - 1) == (painkLocat.y - 1) && locationEnemyX == (painkLocat.x - 1)) {
               getItem(MAIN_ENEMY, PAINKILLER);
            }
            else if (kohmode && (locationEnemyY - 1) == (kohlocat.y - 1) && locationEnemyX == (kohlocat.x - 1)) {
               getFlagE();
            }
            else if ((locationEnemyY - 1) == (weaponboxLocat.y - 1) && locationEnemyX == (weaponboxLocat.x - 1)) {
               if (enemyWeapon == -1) {
                  getItem(MAIN_ENEMY, WEAPON);
               }
               else {
                  out = true;
                  break;
               }
            }
            else if ((locationEnemyY - 1) == (levelboxLocat.y - 1) && locationEnemyX == (levelboxLocat.x - 1)) {
               if (enemyLevel >= (levelMax)) {
                  out = true;
                  break;
               }
               else {
                  getItem(MAIN_ENEMY, EXPERIENCE);
               }
            }
            else if (x < floorsVcnt && (locationEnemyY - 1) == (floorsV[x].y) && locationEnemyX == (floorsV[x].x)) {
               out = true;
               break;
            }
            else {
               out = false;
            }
            for (int i = 0; i < aiEnemies.size(); i++) {
               Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
               Point typer = aie.getLocat();
               if ((locationEnemyY - 1) == typer.y && locationEnemyX == typer.x) {
                  out = true;
                  break;
               }
            }
         }
         if(direction == DOWN){ //down
            if ((locationEnemyY + 1) == (bounds[x].y - 1) && locationEnemyX == (bounds[x].x - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if ((locationEnemyY + 1) == (mobileBounds[x].y - 1) && locationEnemyX == (mobileBounds[x].x - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if ((locationEnemyY + 1) == (healthLocat.y - 1) && locationEnemyX == (healthLocat.x - 1)) {
               getItem(MAIN_ENEMY, HEALTH);
            }
            else if ((locationEnemyY + 1) == (armorLocat.y - 1) && locationEnemyX == (armorLocat.x - 1)) {
               getItem(MAIN_ENEMY, ARMOR);
            }
            else if ((locationEnemyY + 1) == (unoseeLocat.y - 1) && locationEnemyX == (unoseeLocat.x - 1)) {
               getItem(MAIN_ENEMY, UNOSEE_POTION);
            }
            else if ((locationEnemyY + 1) == (gemLocat.y - 1) && locationEnemyX == (gemLocat.x - 1)) {
               getGemE();
            }
            else if ((locationEnemyY + 1) == (megahealthLocat.y - 1) && locationEnemyX == (megahealthLocat.x - 1)) {
               getItem(MAIN_ENEMY, MEGA_HEALTH);
            }
            else if ((locationEnemyY + 1) == (megaexpLocat.y - 1) && locationEnemyX == (megaexpLocat.x - 1)) {
               getItem(MAIN_ENEMY, MEGA_EXPERIENCE);
            }
            else if ((locationEnemyY + 1) == (ghostpotionLocat.y - 1) && locationEnemyX == (ghostpotionLocat.x - 1)) {
               getItem(MAIN_ENEMY, GHOST_POTION);
            }
            else if ((locationEnemyY + 1) == (painkLocat.y - 1) && locationEnemyX == (painkLocat.x - 1)) {
               getItem(MAIN_ENEMY, PAINKILLER);
            }
            else if (kohmode && (locationEnemyY + 1) == (kohlocat.y - 1) && locationEnemyX == (kohlocat.x - 1)) {
               getFlagE();
            }
            else if ((locationEnemyY + 1) == (weaponboxLocat.y - 1) && locationEnemyX == (weaponboxLocat.x - 1)) {
               if (enemyWeapon == -1) {
                  getItem(MAIN_ENEMY, WEAPON);
               }
               else {
                  out = true;
                  break;
               }
            }
            else if ((locationEnemyY + 1) == (levelboxLocat.y - 1) && locationEnemyX == (levelboxLocat.x - 1)) {
               if (enemyLevel >= (levelMax)) {
                  out = true;
                  break;
               }
               else {
                  getItem(MAIN_ENEMY, EXPERIENCE);
               }
            }
            else if (x < floorsVcnt && (locationEnemyY + 1) == (floorsV[x].y) && locationEnemyX == (floorsV[x].x)) {
               out = true;
               break;
            }
            else {
               out = false;
            }
            for (int i = 0; i < aiEnemies.size(); i++) {
               Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
               Point typer = aie.getLocat();
               if ((locationEnemyY + 1) == typer.y && locationEnemyX == typer.x) {
                  out = true;
                  break;
               }
            }
         }
         if(direction == LEFT){ //left
            if ((locationEnemyX - 1) == (bounds[x].x - 1) && locationEnemyY == (bounds[x].y - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if ((locationEnemyX - 1) == (mobileBounds[x].x - 1) && locationEnemyY == (mobileBounds[x].y - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if ((locationEnemyX - 1) == (ghostpotionLocat.x - 1) && locationEnemyY == (ghostpotionLocat.y - 1)) {
               getItem(MAIN_ENEMY, GHOST_POTION);
            }
            else if ((locationEnemyX - 1) == (healthLocat.x - 1) && locationEnemyY == (healthLocat.y - 1)) {
               getItem(MAIN_ENEMY, HEALTH);
            }
            else if ((locationEnemyX - 1) == (armorLocat.x - 1) && locationEnemyY == (armorLocat.y - 1)) {
               getItem(MAIN_ENEMY, ARMOR);
            }
            else if ((locationEnemyX - 1) == (unoseeLocat.x - 1) && locationEnemyY == (unoseeLocat.y - 1)) {
               getItem(MAIN_ENEMY, UNOSEE_POTION);
            }
            else if ((locationEnemyX - 1) == (gemLocat.x - 1) && locationEnemyY == (gemLocat.y - 1)) {
               getGemE();
            }
            else if ((locationEnemyX - 1) == (megahealthLocat.x - 1) && locationEnemyY == (megahealthLocat.y - 1)) {
               getItem(MAIN_ENEMY, MEGA_HEALTH);
            }
            else if ((locationEnemyX - 1) == (megaexpLocat.x - 1) && locationEnemyY == (megaexpLocat.y - 1)) {
               getItem(MAIN_ENEMY, MEGA_EXPERIENCE);
            }
            else if ((locationEnemyX - 1) == (painkLocat.x - 1) && locationEnemyY == (painkLocat.y - 1)) {
               getItem(MAIN_ENEMY, PAINKILLER);
            }
            else if (kohmode && (locationEnemyX - 1) == (kohlocat.x - 1) && locationEnemyY == (kohlocat.y - 1)) {
               getFlagE();
            }
            else if ((locationEnemyX - 1) == (weaponboxLocat.x - 1) && locationEnemyY == (weaponboxLocat.y - 1)) {
               if (enemyWeapon == -1) {
                  getItem(MAIN_ENEMY, WEAPON);
               }
               else {
                  out = true;
                  break;
               }
            }
            else if ((locationEnemyX - 1) == (levelboxLocat.x - 1) && locationEnemyY == (levelboxLocat.y - 1)) {
               if (enemyLevel >= (levelMax)) {
                  out = true;
                  break;
               }
               else {
                  getItem(MAIN_ENEMY, EXPERIENCE);
               }
            }
            else if (x < floorsVcnt && (locationEnemyX - 1) == (floorsV[x].x) && locationEnemyY == (floorsV[x].y)) {
               out = true;
               break;
            }
            else {
               out = false;
            }
            for (int i = 0; i < aiEnemies.size(); i++) {
               Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
               Point typer = aie.getLocat();
               if ((locationEnemyX - 1) == typer.x && locationEnemyY == typer.y) {
                  out = true;
                  break;
               }
            }
         }
         if(direction == RIGHT){ //right
            if ((locationEnemyX + 1) == (bounds[x].x - 1) && locationEnemyY == (bounds[x].y - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if ((locationEnemyX + 1) == (mobileBounds[x].x - 1) && locationEnemyY == (mobileBounds[x].y - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if ((locationEnemyX + 1) == (healthLocat.x - 1) && locationEnemyY == (healthLocat.y - 1)) {
               getItem(MAIN_ENEMY, HEALTH);
            }
            else if ((locationEnemyX + 1) == (armorLocat.x - 1) && locationEnemyY == (armorLocat.y - 1)) {
               getItem(MAIN_ENEMY, ARMOR);
            }
            else if ((locationEnemyX + 1) == (unoseeLocat.x - 1) && locationEnemyY == (unoseeLocat.y - 1)) {
               getItem(MAIN_ENEMY, UNOSEE_POTION);
            }
            else if ((locationEnemyX + 1) == (gemLocat.x - 1) && locationEnemyY == (gemLocat.y - 1)) {
               getGemE();
            }
            else if ((locationEnemyX + 1) == (megahealthLocat.x - 1) && locationEnemyY == (megahealthLocat.y - 1)) {
               getItem(MAIN_ENEMY, MEGA_HEALTH);
            }
            else if ((locationEnemyX + 1) == (megaexpLocat.x - 1) && locationEnemyY == (megaexpLocat.y - 1)) {
               getItem(MAIN_ENEMY, MEGA_EXPERIENCE);
            }
            else if ((locationEnemyX + 1) == (ghostpotionLocat.x - 1) && locationEnemyY == (ghostpotionLocat.y - 1)) {
               getItem(MAIN_ENEMY, GHOST_POTION);
            }
            else if ((locationEnemyX + 1) == (painkLocat.x - 1) && locationEnemyY == (painkLocat.y - 1)) {
               getItem(MAIN_ENEMY, PAINKILLER);
            }
            else if (kohmode && (locationEnemyX + 1) == (kohlocat.x - 1) && locationEnemyY == (kohlocat.y - 1)) {
               getFlagE();
            }
            else if ((locationEnemyX + 1) == (weaponboxLocat.x - 1) && locationEnemyY == (weaponboxLocat.y - 1)) {
               if (enemyWeapon == -1) {
                  getItem(MAIN_ENEMY, WEAPON);
               }
               else {
                  out = true;
                  break;
               }
            }
            else if ((locationEnemyX + 1) == (levelboxLocat.x - 1) && locationEnemyY == (levelboxLocat.y - 1)) {
               if (enemyLevel >= (levelMax)) {
                  out = true;
                  break;
               }
               else {
                  getItem(MAIN_ENEMY, EXPERIENCE);
               }
            }
            else if (x < floorsVcnt && (locationEnemyX + 1) == (floorsV[x].x) && locationEnemyY == (floorsV[x].y)) {
               out = true;
               break;
            }
            else {
               out = false;
            }
            for (int i = 0; i < aiEnemies.size(); i++) {
               Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
               Point typer = aie.getLocat();
               if ((locationEnemyX + 1) == typer.x && locationEnemyY == typer.y) {
                  out = true;
                  break;
               }
            }
         }
      }
      return out;
   }

  /**
   *  Krux 3 Method
   * ==================================================================================
   *  testBoundsAtLocat
   *  Tests the possibility of movement at a certain location in a certain direction
   *
   *  @param      atpoint       The Point to test the movement from
   *  @param      direction     The direction of movement
   *  @return     a boolean representing for Object can move or not
   */
   protected boolean testBoundsAtLocat(Point atpoint, int direction) { // Test if the enemy can move
      boolean out = true;
      for(int x = 0; x < (mapsize.x * mapsize.y); x++) {
         if(direction == UP){ //up
            if (x < maxbounds && (atpoint.y - 1) == (bounds[x].y - 1) && atpoint.x == (bounds[x].x - 1)) {
               out = true;
               break;
            }
            else if (x < maxbounds && (atpoint.y - 1) == (mobileBounds[x].y - 1) && atpoint.x == (mobileBounds[x].x - 1)) {
               out = true;
               break;
            }
            else if (x < trackingBoundsMax && (atpoint.y - 1) == (trackingbounds[x].y - 1) && atpoint.x == (trackingbounds[x].x - 1)) {
               out = true;
               break;
            }
            else if ((atpoint.y - 1) == (healthLocat.y - 1) && atpoint.x == (healthLocat.x - 1)) {
               out = true;
               break;
            }
            else if (kohmode && (atpoint.y - 1) == (kohlocat.y - 1) && atpoint.x == (kohlocat.x - 1)) {
               out = true;
               break;
            }
            else if ((atpoint.y - 1) == (weaponboxLocat.y - 1) && atpoint.x == (weaponboxLocat.x - 1)) {
               out = true;
               break;
            }
            else if ((atpoint.y - 1) == (levelboxLocat.y - 1) && atpoint.x == (levelboxLocat.x - 1)) {
               out = true;
               break;
            }
            else if (x < floorsVcnt && (atpoint.y - 1) == (floorsV[x].y) && atpoint.x == (floorsV[x].x)) {
               out = true;
               break;
            }
            else {
               out = false;
            }
         }
         if(direction == DOWN){ //down
            if (x < maxbounds && (atpoint.y + 1) == (bounds[x].y) && atpoint.x == bounds[x].x) {
               out = true;
               break;
            }
            else if (x < maxbounds && (atpoint.y + 1) == (mobileBounds[x].y) && atpoint.x == mobileBounds[x].x) {
               out = true;
               break;
            }
            else if (x < trackingBoundsMax && (atpoint.y + 1) == (trackingbounds[x].y) && atpoint.x == trackingbounds[x].x) {
               out = true;
               break;
            }
            else if ((atpoint.y + 1) == (healthLocat.y - 1) && atpoint.x == (healthLocat.x - 1)) {
               out = true;
               break;
            }
            else if (kohmode && (atpoint.y + 1) == (kohlocat.y - 1) && atpoint.x == (kohlocat.x - 1)) {
               out = true;
               break;
            }
            else if ((atpoint.y + 1) == (weaponboxLocat.y - 1) && atpoint.x == (weaponboxLocat.x - 1)) {
               out = true;
               break;
            }
            else if ((atpoint.y + 1) == (levelboxLocat.y - 1) && atpoint.x == (levelboxLocat.x - 1)) {
               out = true;
               break;
            }
            else if (x < floorsVcnt && (atpoint.y + 1) == (floorsV[x].y + 1) && atpoint.x == (floorsV[x].x)) {
               out = true;
               break;
            }
            else {
               out = false;
            }
         }
         if(direction == LEFT){ //left
            if (x < maxbounds && (atpoint.x - 1) == (bounds[x].x - 1) && atpoint.y == (bounds[x].y - 1)) {
               out = true;
               break;
            }
            else if (x < maxbounds && (atpoint.x - 1) == (mobileBounds[x].x - 1) && atpoint.y == (mobileBounds[x].y - 1)) {
               out = true;
               break;
            }
            else if (x < trackingBoundsMax && (atpoint.x - 1) == (trackingbounds[x].x - 1) && atpoint.y == (trackingbounds[x].y - 1)) {
               out = true;
               break;
            }
            else if ((atpoint.x - 1) == (healthLocat.x - 1) && atpoint.y == (healthLocat.y - 1)) {
               out = true;
               break;
            }
            else if (kohmode && (atpoint.x - 1) == (kohlocat.x - 1) && atpoint.y == (kohlocat.y - 1)) {
               out = true;
               break;
            }
            else if ((atpoint.x - 1) == (weaponboxLocat.x - 1) && atpoint.y == (weaponboxLocat.y - 1)) {
               out = true;
               break;
            }
            else if ((atpoint.x - 1) == (levelboxLocat.x - 1) && atpoint.y == (levelboxLocat.y - 1)) {
               out = true;
               break;
            }
            else if (x < floorsVcnt && (atpoint.x - 1) == (floorsV[x].x) && atpoint.y == (floorsV[x].y)) {
               out = true;
               break;
            }
            else {
               out = false;
            }
         }
         if(direction == RIGHT){ //right
            if (x < maxbounds && (atpoint.x + 1) == (bounds[x].x - 1) && atpoint.y == (bounds[x].y - 1)) {
               out = true;
               break;
            }
            else if (x < maxbounds && (atpoint.x + 1) == (mobileBounds[x].x - 1) && atpoint.y == (mobileBounds[x].y - 1)) {
               out = true;
               break;
            }
            else if (x < trackingBoundsMax && (atpoint.x + 1) == (trackingbounds[x].x - 1) && atpoint.y == (trackingbounds[x].y - 1)) {
               out = true;
               break;
            }
            else if ((atpoint.x + 1) == (healthLocat.x - 1) && atpoint.y == (healthLocat.y - 1)) {
               out = true;
               break;
            }
            else if (kohmode && (atpoint.x + 1) == (kohlocat.x - 1) && atpoint.y == (kohlocat.y - 1)) {
               out = true;
               break;
            }
            else if ((atpoint.x + 1) == (weaponboxLocat.x - 1) && atpoint.y == (weaponboxLocat.y - 1)) {
               out = true;
               break;
            }
            else if ((atpoint.x + 1) == (levelboxLocat.x - 1) && atpoint.y == (levelboxLocat.y - 1)) {
               out = true;
               break;
            }
            else if (x < floorsVcnt && (atpoint.x + 1) == (floorsV[x].x) && atpoint.y == (floorsV[x].y)) {
               out = true;
               break;
            }
            else {
               out = false;
            }
         }
      }
      return out;
   }

   /**
   *  Krux 3 Method
   * ==================================================================================
   *  testBoundsAt
   *  Tests the possibility of movement at a certain location in a certain direction
   *  Note: This Method is affected by the "isGhost2" variable while "testBoundsAtLocat"
   *        is not.
   *
   *  @param      atpoint       The Point to test the movement from
   *  @param      direction     The direction of movement
   *  @return     a boolean representing for Object can move or not
   */
   protected boolean testBoundsAt(Point atpoint, int direction) { // Test if the enemy can move
      boolean out = false;
      for(int x = 0; x < (mapsize.x * mapsize.y); x++) {
         if(direction == UP){ //up
            if (x < maxbounds && (atpoint.y - 1) == (bounds[x].y - 1) && atpoint.x == (bounds[x].x - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if (x < maxbounds && (atpoint.y - 1) == (mobileBounds[x].y - 1) && atpoint.x == (mobileBounds[x].x - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if (x < trackingBoundsMax && (atpoint.y - 1) == (trackingbounds[x].y - 1) && atpoint.x == (trackingbounds[x].x - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if (x < maxbounds && (atpoint.y - 1) == (healthLocat.y - 1) && atpoint.x == (healthLocat.x - 1)) {
               out = false;
               break;
            }
            else if (kohmode && (atpoint.y - 1) == (kohlocat.y - 1) && atpoint.x == (kohlocat.x - 1)) {
               out = false;
               break;
            }
            else if ((atpoint.y - 1) == (weaponboxLocat.y - 1) && atpoint.x == (weaponboxLocat.x - 1)) {
               if (enemyWeapon == -1) {
                  out = false;
                  break;
               }
               else {
                  break;
               }
            }
            else if ((atpoint.y - 1) == (levelboxLocat.y - 1) && atpoint.x == (levelboxLocat.x - 1)) {
               if (enemyLevel >= (levelMax)) {
                  out = true;
                  break;
               }
               else {
                  out = false;
                  break;
               }
            }
            else if (x < floorsVcnt && (atpoint.y - 1) == (floorsV[x].y) && atpoint.x == (floorsV[x].x)) {
               out = true;
               break;
            }
            else {
               out = false;
            }
         }
         if(direction == DOWN){ //down
            if (x < maxbounds && (atpoint.y + 1) == (bounds[x].y - 1) && atpoint.x == (bounds[x].x - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if (x < maxbounds && (atpoint.y + 1) == (mobileBounds[x].y - 1) && atpoint.x == (mobileBounds[x].x - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if (x < trackingBoundsMax && (atpoint.y + 1) == (trackingbounds[x].y - 1) && atpoint.x == (trackingbounds[x].x - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if ((atpoint.y + 1) == (healthLocat.y - 1) && atpoint.x == (healthLocat.x - 1)) {
               out = false;
               break;
            }
            else if (kohmode && (atpoint.y + 1) == (kohlocat.y - 1) && atpoint.x == (kohlocat.x - 1)) {
               out = false;
               break;
            }
            else if ((atpoint.y + 1) == (weaponboxLocat.y - 1) && atpoint.x == (weaponboxLocat.x - 1)) {
               if (enemyWeapon == -1) {
                  out = false;
                  break;
               }
               else {
                  break;
               }
            }
            else if (x < floorsVcnt && (atpoint.y + 1) == (floorsV[x].y) && atpoint.x == (floorsV[x].x)) {
               out = true;
               break;
            }
            else if ((atpoint.y + 1) == (levelboxLocat.y - 1) && atpoint.x == (levelboxLocat.x - 1)) {
               if (enemyLevel >= (levelMax)) {
                  out = true;
                  break;
               }
               else {
                  out = false;
                  break;
               }
            }
            else {
               out = false;
            }
         }
         if(direction == LEFT){ //left
            if (x < maxbounds && (atpoint.x - 1) == (bounds[x].x - 1) && atpoint.y == (bounds[x].y - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if (x < maxbounds && (atpoint.x - 1) == (mobileBounds[x].x - 1) && atpoint.y == (mobileBounds[x].y - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if (x < trackingBoundsMax && (atpoint.x - 1) == (trackingbounds[x].x - 1) && atpoint.y == (trackingbounds[x].y - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if ((atpoint.x - 1) == (healthLocat.x - 1) && atpoint.y == (healthLocat.y - 1)) {
               out = false;
               break;
            }
            else if (kohmode && (atpoint.x - 1) == (kohlocat.x - 1) && atpoint.y == (kohlocat.y - 1)) {
               out = false;
               break;
            }
            else if ((atpoint.x - 1) == (weaponboxLocat.x - 1) && atpoint.y == (weaponboxLocat.y - 1)) {
               if (enemyWeapon == -1) {
                  out = false;
                  break;
               }
               else {
                  break;
               }
            }
            else if ((atpoint.x - 1) == (levelboxLocat.x - 1) && atpoint.y == (levelboxLocat.y - 1)) {
               if (enemyLevel >= (levelMax)) {
                  out = true;
                  break;
               }
               else {
                  out = false;
                  break;
               }
            }
            else if (x < floorsVcnt && (atpoint.x - 1) == (floorsV[x].x) && atpoint.y == (floorsV[x].y)) {
               out = true;
               break;
            }
            else {
               out = false;
            }
         }
         if(direction == RIGHT){ //right
            if (x < maxbounds && (atpoint.x + 1) == (bounds[x].x - 1) && atpoint.y == (bounds[x].y - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if (x < maxbounds && (atpoint.x + 1) == (mobileBounds[x].x - 1) && atpoint.y == (mobileBounds[x].y - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if (x < trackingBoundsMax && (atpoint.x + 1) == (trackingbounds[x].x - 1) && atpoint.y == (trackingbounds[x].y - 1)) {
               if(isGhost2) {
                  out = false;
                  break;
               }
               else {
                  out = true;
                  break;
               }
            }
            else if ((atpoint.x + 1) == (healthLocat.x - 1) && atpoint.y == (healthLocat.y - 1)) {
               out = false;
               break;
            }
            else if (kohmode && (atpoint.x + 1) == (kohlocat.x - 1) && atpoint.y == (kohlocat.y - 1)) {
               out = false;
               break;
            }
            else if ((atpoint.x + 1) == (weaponboxLocat.x - 1) && atpoint.y == (weaponboxLocat.y - 1)) {
               if (enemyWeapon == -1) {
                  out = false;
                  break;
               }
               else {
                  break;
               }
            }
            else if ((atpoint.x + 1) == (levelboxLocat.x - 1) && atpoint.y == (levelboxLocat.y - 1)) {
               if (enemyLevel >= (levelMax)) {
                  out = true;
                  break;
               }
               else {
                  out = false;
                  break;
               }
            }
            else if (x < floorsVcnt && (atpoint.x + 1) == (floorsV[x].x) && atpoint.y == (floorsV[x].y)) {
               out = true;
               break;
            }
            else {
               out = false;
            }
         }
      }
      return out;
   }

   /**
   *  Krux 2 Method
   * ==================================================================================
   *  testBounds
   *  Tests the BLUE PLAYER's movement
   *
   *  @param      direction     The direction of movement
   *  @return     a boolean representing for the Blue Player can move or not
   */
      public boolean testBounds(int direction) {
            boolean out = false;
            for(int x = 0; x < maxbounds; x++) {
                  if(direction == UP) { //up
                        if(!isGhost1) {
                              if ((locationPlayerY - 1) == (bounds[x].y - 1) && locationPlayerX == (bounds[x].x - 1)) {
                                    out = true;
                                    break;
                              }
                              else if ((locationPlayerY - 1) == (mobileBounds[x].y - 1) && locationPlayerX == (mobileBounds[x].x - 1)) {
                                    out = true;
                                    break;
                              }
                              else if (x < trackingBoundsMax && (locationPlayerY - 1) == (trackingbounds[x].y - 1) && locationPlayerX == (trackingbounds[x].x - 1)) {
                                    out = true;
                                    break;
                              }
                        } else {
                              out = false;
                              break;
                        }
                        
                        if (kohmode && (locationPlayerY - 1) == (kohlocat.y - 1) && locationPlayerX == (kohlocat.x - 1)) {
                              getFlagP();
                        }
                        else if ((locationPlayerY - 1) == (lampLocat.y - 1) && locationPlayerX == (lampLocat.x - 1)) {
                              getItem(MAIN_PLAYER, LAMP);
                        }
                        else if ((locationPlayerY - 1) == (healthLocat.y - 1) && locationPlayerX == (healthLocat.x - 1)) {
                              getItem(MAIN_PLAYER, HEALTH);
                        }
                        else if ((locationPlayerY - 1) == (armorLocat.y - 1) && locationPlayerX == (armorLocat.x - 1)) {
                              getItem(MAIN_PLAYER, ARMOR);
                        }
                        else if ((locationPlayerY - 1) == (unoseeLocat.y - 1) && locationPlayerX == (unoseeLocat.x - 1)) {
                              getItem(MAIN_PLAYER, UNOSEE_POTION);
                        }
                        else if ((locationPlayerY - 1) == (gemLocat.y - 1) && locationPlayerX == (gemLocat.x - 1)) {
                              getGemP();
                        }
                        else if ((locationPlayerY - 1) == (megahealthLocat.y - 1) && locationPlayerX == (megahealthLocat.x - 1)) {
                              getItem(MAIN_PLAYER, MEGA_HEALTH);
                        }
                        else if ((locationPlayerY - 1) == (megaexpLocat.y - 1) && locationPlayerX == (megaexpLocat.x - 1)) {
                              getItem(MAIN_PLAYER, MEGA_EXPERIENCE);
                        }
                        else if ((locationPlayerY - 1) == (ghostpotionLocat.y - 1) && locationPlayerX == (ghostpotionLocat.x - 1)) {
                              getItem(MAIN_PLAYER, GHOST_POTION);
                        }
                        else if ((locationPlayerY - 1) == (extraLifeLocat.y - 1) && locationPlayerX == (extraLifeLocat.x - 1)) {
                              getItem(MAIN_PLAYER, ONE_UP);
                        }
                        else if ((locationPlayerY - 1) == (painkLocat.y - 1) && locationPlayerX == (painkLocat.x - 1)) {
                              getItem(MAIN_PLAYER, PAINKILLER);
                        }
                        else if ((locationPlayerY - 1) == (weaponboxLocat.y - 1) && locationPlayerX == (weaponboxLocat.x - 1) && playerWeapon == -1) {
                              getItem(MAIN_PLAYER, WEAPON);
                        }
                        else if ((locationPlayerY - 1) == (levelboxLocat.y - 1) && locationPlayerX == (levelboxLocat.x - 1) && playerLevel < (levelMax)) {
                              getItem(MAIN_PLAYER, EXPERIENCE);
                        }
                        else if (x < floorsVcnt && (locationPlayerY - 1) == (floorsV[x].y) && locationPlayerX == (floorsV[x].x)) {
                              out = true;
                              break;
                        }
                        else {
                              out = false;
                        }
                        for (int i = 0; i < aiEnemies.size(); i++) {
                              Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
                              Point typer = aie.getLocat();
                              if ((locationPlayerY - 1) == typer.y && locationPlayerX == typer.x) {
                                    out = true;
                                    break;
                              }
                        }
                  }
                  if(direction == DOWN) {  //down
                        if (!isGhost1) {
                              if ((locationPlayerY + 1) == (bounds[x].y - 1) && locationPlayerX == (bounds[x].x - 1)) {
                                    out = true;
                                    break;
                              }
                              else if ((locationPlayerY + 1) == (mobileBounds[x].y - 1) && locationPlayerX == (mobileBounds[x].x - 1)) {
                                    out = true;
                                    break;
                              }
                              else if (x < trackingBoundsMax && (locationPlayerY + 1) == (trackingbounds[x].y - 1) && locationPlayerX == (trackingbounds[x].x - 1)) {
                                    out = true;
                                    break;
                              }
                        } else {
                              out = false;
                              break;
                        }
                        
                        if ((locationPlayerY + 1) == (healthLocat.y - 1) && locationPlayerX == (healthLocat.x - 1)) {
                              getItem(MAIN_PLAYER, HEALTH);
                        }
                        else if ((locationPlayerY + 1) == (armorLocat.y - 1) && locationPlayerX == (armorLocat.x - 1)) {
                              getItem(MAIN_PLAYER, ARMOR);
                        }
                        else if ((locationPlayerY + 1) == (unoseeLocat.y - 1) && locationPlayerX == (unoseeLocat.x - 1)) {
                              getItem(MAIN_PLAYER, UNOSEE_POTION);
                        }
                        else if ((locationPlayerY + 1) == (lampLocat.y - 1) && locationPlayerX == (lampLocat.x - 1)) {
                              getItem(MAIN_PLAYER, LAMP);
                        }
                        else if ((locationPlayerY + 1) == (gemLocat.y - 1) && locationPlayerX == (gemLocat.x - 1)) {
                              getGemP();
                        }
                        else if ((locationPlayerY + 1) == (megahealthLocat.y - 1) && locationPlayerX == (megahealthLocat.x - 1)) {
                              getItem(MAIN_PLAYER, MEGA_HEALTH);
                        }
                        else if ((locationPlayerY + 1) == (megaexpLocat.y - 1) && locationPlayerX == (megaexpLocat.x - 1)) {
                              getItem(MAIN_PLAYER, MEGA_EXPERIENCE);
                        }
                        else if ((locationPlayerY + 1) == (ghostpotionLocat.y - 1) && locationPlayerX == (ghostpotionLocat.x - 1)) {
                              getItem(MAIN_PLAYER, GHOST_POTION);
                        }
                        else if (kohmode && (locationPlayerY + 1) == (kohlocat.y - 1) && locationPlayerX == (kohlocat.x - 1)) {
                              getFlagP();
                        }
                        else if ((locationPlayerY + 1) == (extraLifeLocat.y - 1) && locationPlayerX == (extraLifeLocat.x - 1)) {
                              getItem(MAIN_PLAYER, ONE_UP);
                        }
                        else if ((locationPlayerY + 1) == (painkLocat.y - 1) && locationPlayerX == (painkLocat.x - 1)) {
                              getItem(MAIN_PLAYER, PAINKILLER);
                        }
                        else if ((locationPlayerY + 1) == (weaponboxLocat.y - 1) && locationPlayerX == (weaponboxLocat.x - 1) && playerWeapon == -1) {
                              getItem(MAIN_PLAYER, WEAPON);
                        }
                        else if ((locationPlayerY + 1) == (levelboxLocat.y - 1) && locationPlayerX == (levelboxLocat.x - 1) && playerLevel < (levelMax)) {
                              getItem(MAIN_PLAYER, EXPERIENCE);
                        }
                        else if (x < floorsVcnt && (locationPlayerY + 1) == (floorsV[x].y) && locationPlayerX == (floorsV[x].x)) {
                              out = true;
                              break;
                        } else {
                              out = false;
                        }
                        for (int i = 0; i < aiEnemies.size(); i++) {
                              Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
                              Point typer = aie.getLocat();
                              if ((locationPlayerY + 1) == typer.y && locationPlayerX == typer.x) {
                                    out = true;
                                    break;
                              }
                        }
                  }
                  if(direction == LEFT){ //left
                        if(!isGhost1) {
                              if ((locationPlayerX - 1) == (bounds[x].x - 1) && locationPlayerY == (bounds[x].y - 1)) {
                                    out = true;
                                    break;
                              }
                              else if ((locationPlayerX - 1) == (mobileBounds[x].x - 1) && locationPlayerY == (mobileBounds[x].y - 1)) {
                                    out = true;
                                    break;
                              }
                              else if (x < trackingBoundsMax && (locationPlayerX - 1) == (trackingbounds[x].x - 1) && locationPlayerY == (trackingbounds[x].y - 1)) {
                                    out = true;
                                    break;
                              } 
                        } else {
                              out = false;
                              break;
                        }
                        
                        if ((locationPlayerX - 1) == (healthLocat.x - 1) && locationPlayerY == (healthLocat.y - 1)) {
                              getItem(MAIN_PLAYER, HEALTH);
                        }
                        else if ((locationPlayerX - 1) == (armorLocat.x - 1) && locationPlayerY == (armorLocat.y - 1)) {
                              getItem(MAIN_PLAYER, ARMOR);
                        }
                        else if ((locationPlayerX - 1) == (unoseeLocat.x - 1) && locationPlayerY == (unoseeLocat.y - 1)) {
                              getItem(MAIN_PLAYER, UNOSEE_POTION);
                        }
                        else if ((locationPlayerX - 1) == (lampLocat.x - 1) && locationPlayerY == (lampLocat.y - 1)) {
                              getItem(MAIN_PLAYER, LAMP);
                        }
                        else if ((locationPlayerX - 1) == (gemLocat.x - 1) && locationPlayerY == (gemLocat.y - 1)) {
                              getGemP();
                        }
                        else if ((locationPlayerX - 1) == (megahealthLocat.x - 1) && locationPlayerY == (megahealthLocat.y - 1)) {
                              getItem(MAIN_PLAYER, MEGA_HEALTH);
                        }
                        else if ((locationPlayerX - 1) == (megaexpLocat.x - 1) && locationPlayerY == (megaexpLocat.y - 1)) {
                              getItem(MAIN_PLAYER, MEGA_EXPERIENCE);
                        }
                        else if ((locationPlayerX - 1) == (ghostpotionLocat.x - 1) && locationPlayerY == (ghostpotionLocat.y - 1)) {
                              getItem(MAIN_PLAYER, GHOST_POTION);
                        }
                        else if (kohmode && (locationPlayerX - 1) == (kohlocat.x - 1) && locationPlayerY == (kohlocat.y - 1)) {
                              getFlagP();
                        }
                        else if ((locationPlayerX - 1) == (extraLifeLocat.x - 1) && locationPlayerY == (extraLifeLocat.y - 1)) {
                              getItem(MAIN_PLAYER, ONE_UP);
                        }
                        else if ((locationPlayerX - 1) == (painkLocat.x - 1) && locationPlayerY == (painkLocat.y - 1)) {
                              getItem(MAIN_PLAYER, PAINKILLER);
                        }
                        else if ((locationPlayerX - 1) == (weaponboxLocat.x - 1) && locationPlayerY == (weaponboxLocat.y - 1) && playerWeapon == -1) {
                              getItem(MAIN_PLAYER, WEAPON);
                        }
                        else if ((locationPlayerX - 1) == (levelboxLocat.x - 1) && locationPlayerY == (levelboxLocat.y - 1) && playerLevel < (levelMax)) {
                              getItem(MAIN_PLAYER, EXPERIENCE);
                        }
                        else if (x < floorsVcnt && (locationPlayerX - 1) == (floorsV[x].x) && locationPlayerY == (floorsV[x].y)) {
                              out = true;
                              break;
                        }
                        else {
                              out = false;
                        }
                        for (int i = 0; i < aiEnemies.size(); i++) {
                              Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
                              Point typer = aie.getLocat();
                                          
                              if ((locationPlayerX - 1) == typer.x && locationPlayerY == typer.y) {
                                    out = true;
                                    break;
                              }
                        }
                  }
                  if(direction == RIGHT) { //right
                        if(!isGhost1) {
                              if ((locationPlayerX + 1) == (bounds[x].x - 1) && locationPlayerY == (bounds[x].y - 1)) {
                                    out = true;
                                    break;
                              }
                              else if ((locationPlayerX + 1) == (mobileBounds[x].x - 1) && locationPlayerY == (mobileBounds[x].y - 1)) {
                                    out = true;
                                    break;
                              }
                              else if (x < trackingBoundsMax && (locationPlayerX + 1) == (trackingbounds[x].x - 1) && locationPlayerY == (trackingbounds[x].y - 1)) {
                                    out = true;
                                    break;
                              }
                        } else {
                              out = false;
                              break;
                        }
                        
                        if ((locationPlayerX + 1) == (healthLocat.x - 1) && locationPlayerY == (healthLocat.y - 1)) {
                              getItem(MAIN_PLAYER, HEALTH);
                        }
                        else if ((locationPlayerX + 1) == (armorLocat.x - 1) && locationPlayerY == (armorLocat.y - 1)) {
                              getItem(MAIN_PLAYER, ARMOR);
                        }
                        else if ((locationPlayerX + 1) == (unoseeLocat.x - 1) && locationPlayerY == (unoseeLocat.y - 1)) {
                              getItem(MAIN_PLAYER, UNOSEE_POTION);
                        }
                        else if ((locationPlayerX + 1) == (lampLocat.x - 1) && locationPlayerY == (lampLocat.y - 1)) {
                              getItem(MAIN_PLAYER, LAMP);
                        }
                        else if ((locationPlayerX + 1) == (gemLocat.x - 1) && locationPlayerY == (gemLocat.y - 1)) {
                              getGemP();
                        }
                        else if ((locationPlayerX + 1) == (megahealthLocat.x - 1) && locationPlayerY == (megahealthLocat.y - 1)) {
                              getItem(MAIN_PLAYER, MEGA_HEALTH);
                        }
                        else if ((locationPlayerX + 1) == (megaexpLocat.x - 1) && locationPlayerY == (megaexpLocat.y - 1)) {
                              getItem(MAIN_PLAYER, MEGA_EXPERIENCE);
                        }
                        else if ((locationPlayerX + 1) == (ghostpotionLocat.x - 1) && locationPlayerY == (ghostpotionLocat.y - 1)) {
                              getItem(MAIN_PLAYER, GHOST_POTION);
                        }
                        else if (kohmode && (locationPlayerX + 1) == (kohlocat.x - 1) && locationPlayerY == (kohlocat.y - 1)) {
                              getFlagP();
                        }
                        else if ((locationPlayerX + 1) == (extraLifeLocat.x - 1) && locationPlayerY == (extraLifeLocat.y - 1)) {
                              getItem(MAIN_PLAYER, ONE_UP);
                        }
                        else if ((locationPlayerX + 1) == (painkLocat.x - 1) && locationPlayerY == (painkLocat.y - 1)) {
                              getItem(MAIN_PLAYER, PAINKILLER);
                        }
                        else if ((locationPlayerX + 1) == (weaponboxLocat.x - 1) && locationPlayerY == (weaponboxLocat.y - 1) && playerWeapon == -1) {
                              getItem(MAIN_PLAYER, WEAPON);
                        }
                        else if ((locationPlayerX + 1) == (levelboxLocat.x - 1) && locationPlayerY == (levelboxLocat.y - 1) && playerLevel < (levelMax)) {
                              getItem(MAIN_PLAYER, EXPERIENCE);
                        }
                        else if (x < floorsVcnt && (locationPlayerX + 1) == (floorsV[x].x) && locationPlayerY == (floorsV[x].y)) {
                              out = true;
                              break;
                        }
                        else {
                              out = false;
                        }

                        for (int i = 0; i < aiEnemies.size(); i++) {
                              Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) aiEnemies.elementAt(i);
                              Point typer = aie.getLocat();       
                              if ((locationPlayerX + 1) == typer.x && locationPlayerY == typer.y) {
                                    out = true;
                                    break;
                              }
                        }
                  }
            }
            return out;
      }
   
	/*
      *  Other Methods
	* ==================================================================================
	*  Methods that control other important things
	*/
   
	/**
	* Generic Method (Krux 4.0 ALPHA)
	* ==================================================================================
	* updateBoundaries
	*
	* updates the positions of mobile boundaries
	*/
      protected void updateBoundaries(int type) {
      //    Shared direction variable
            int dir = 0;
            if (type == BOUND_SHIFT) {
            //    Controls the positions of mobile/shifting boundaries
                  for(int i = 0; i < countM; i++) {
                        dir = (int)(Math.random() * 4);
                        if(mobileBounds[i].x > lxlimit && mobileBounds[i].y > lylimit && mobileBounds[i].x < uxlimit && mobileBounds[i].y < uylimit) {
                              switch (dir) {
                                    case 0:
                                          if(!testBoundsAtLocat(mobileBounds[i], UP) && mobileBounds[i].y != 1 && mBoundClass[i] != 'h') {
                                                mobileBounds[i].y -= (int)(Math.random() * 2);
                                          }
                                          break;
                                    case 1:
                                          if(!testBoundsAtLocat(mobileBounds[i], DOWN) && mobileBounds[i].y != (mapsize.y - 1) && mBoundClass[i] != 'h') {
                                                mobileBounds[i].y += (int)(Math.random() * 2);
                                          }
                                          break;
                                    case 2:
                                          if(!testBoundsAtLocat(mobileBounds[i], LEFT) && mobileBounds[i].x != 1 && mBoundClass[i] != 'v') {
                                                mobileBounds[i].x -= (int)(Math.random() * 2);
                                          }
                                          break;
                                    case 3:
                                          if(!testBoundsAtLocat(mobileBounds[i], RIGHT) && mobileBounds[i].x != (mapsize.x - 1) && mBoundClass[i] != 'v') {
                                                mobileBounds[i].x += (int)(Math.random() * 2);
                                          }
                                          break;
                                    default:
                                          continue;
                              }
                        }
                  } 
            } else if (type == BOUND_TRACK) {
            //    Controls the positions of tracking boundaries
                  for(int i = 0; i < countTrackers; i++) {
                        if(trackingbounds[i].x > lxlimit && trackingbounds[i].y > lylimit && trackingbounds[i].x < uxlimit && trackingbounds[i].y < uylimit) {
                              if (locationPlayerX > trackingbounds[i].x) {
                                    dir = 3;
                              } else if (locationPlayerX < trackingbounds[i].x) {
                                    dir = 2;
                              } else if (locationPlayerY < trackingbounds[i].y) {
                                    dir = 0;
                              } else if (locationPlayerY > trackingbounds[i].y) {
                                    dir = 1;
                              } else {
                                    dir = (int) Math.round(Math.random() * 4);
                              }
                              
                              switch (dir) {
                                    case 0:
                                          if(!testBoundsAtLocat(trackingbounds[i], UP) && trackingbounds[i].y != 1) {
                                                trackingbounds[i].y -= (int) (Math.random() * 2);
                                          }
                                          break;
                                    case 1:
                                          if(!testBoundsAtLocat(trackingbounds[i], DOWN) && trackingbounds[i].y != (mapsize.y - 1)) {
                                                trackingbounds[i].y += (int) (Math.random() * 2);
                                          }
                                          break;
                                    case 2:
                                          if(!testBoundsAtLocat(trackingbounds[i], LEFT) && trackingbounds[i].x != 1) {
                                                trackingbounds[i].x -= (int) (Math.random() * 2);
                                          }
                                          break;
                                    case 3:
                                          if(!testBoundsAtLocat(trackingbounds[i], RIGHT) && trackingbounds[i].x != (mapsize.x - 1)) {
                                                trackingbounds[i].x += (int) (Math.random() * 2);
                                          }
                                          break;
                              }
                        }
                  } 
            }
      }
   
/**
*	Generic Method (Krux 4.0 ALPHA)
* ==================================================================================
*	addBounds
*	adds boundaries to the map matrix
*
*	@param		x			The X locator for the boundary
*	@param		y			The Y locator for the boundary
*	@param		type		The boundary type identifier
*/
   protected void addBounds(int x, int y, int type) {
      if(type == BOUND_SOLID) {
         if(!loadedmap) {
            if((x < spawnPoint1.x + 3 && x > spawnPoint1.x - 3) && (y < spawnPoint1.y + 3 && y > spawnPoint1.y - 3) && !genCave)
               ;
            else if((x < spawnPoint2.x + 3 && x > spawnPoint2.x - 3) && (y < spawnPoint2.y + 3 && y > spawnPoint2.y - 3) && !genCave)
               ;
            else
               bounds[count] = new Point(x, y);
         }
         else
            bounds[count] = new Point(x, y);
         count++;
      }
      else if (type == BOUND_SHIFT) {
         if(!loadedmap) {
            if((x < spawnPoint1.x + 3 && x > spawnPoint1.x - 3) && (y < spawnPoint1.y + 3 && y > spawnPoint1.y - 3))
               ;
            else if((x < spawnPoint2.x + 3 && x > spawnPoint2.x - 3) && (y < spawnPoint2.y + 3 && y > spawnPoint2.y - 3))
               ;
            else
               mobileBounds[countM] = new Point(x, y);
            mBoundClass[countM] = 's';
         }
         else
            mobileBounds[countM] = new Point(x, y);
         mBoundClass[countM] = 's';
         countM++;
      }
      else if (type == BOUND_TRACK) {
         if(!loadedmap) {
            if((x < spawnPoint1.x + 3 && x > spawnPoint1.x - 3) && (y < spawnPoint1.y + 3 && y > spawnPoint1.y - 3))
               ;
            else if((x < spawnPoint2.x + 3 && x > spawnPoint2.x - 3) && (y < spawnPoint2.y + 3 && y > spawnPoint2.y - 3))
               ;
            else
               trackingbounds[countTrackers] = new Point(x, y);
         }
         else
            trackingbounds[countTrackers] = new Point(x, y);
         countTrackers++;
      }
      else {
      	// null
      }
   }
	
/**
*	Generic Method (Krux 4.0 ALPHA)
*     ==================================================================================
*	addBounds
*	adds boundaries to the map matrix at a random location
*
*	@param		type		The boundary type identifier
*/
    protected void addBounds(int type) {
        if(type == BOUND_TRACK) {
            if (countTrackers < trackingbounds.length) {
                trackingbounds[countTrackers] = findFreeBlock();
                countTrackers++;
            }
        }
    }
   
/**
*	Generic Method (Krux 4.0 ALPHA)
*     ==================================================================================
*	addBounds
*	adds boundaries to the map matrix at a random location
*
*	@param		amount	Boundary count
*	@param		type		The boundary type identifier		
*/
    protected void addBounds(int amount, int type) {
        if (type == BOUND_SOLID) {
            for(int i = 0; i < amount; i++) {
                Point adder = new Point((int) (Math.random() * (mapsize.x - 1)) + 1,(int) (Math.random() * (mapsize.y - 1)) + 1);
                addBounds(adder.x, adder.y, BOUND_SOLID);
                mapdata[adder.x][adder.y] = "S";
            }
        } else if (type == BOUND_SHIFT) {
            for(int i = 0; i < amount; i++) {
                Point adder = new Point((int) (Math.random() * (mapsize.x - 1)) + 1,(int) (Math.random() * (mapsize.y - 1)) + 1);
                addBounds(adder.x, adder.y, BOUND_SHIFT);
                mapdata[adder.x][adder.y] = "M";
            }
        } else if (type == BOUND_TRACK && countTrackers < trackingbounds.length) {
            for(int i = 0; i < amount; i++) {
                Point adder = new Point((int) (Math.random() * (mapsize.x - 1)) + 1,(int) (Math.random() * (mapsize.y - 1)) + 1);
                addBounds(adder.x, adder.y, BOUND_TRACK);
            }
        }
    }

// MICRON UPDATED FUNCTIONS
/**
*	Item Control Method (Micro Development Team)
*   ==================================================================================
*	getItem
*	Processes the collection of items in-game
*
*   Quite a few bugs were fixed when creating this new method
*
*	@param	player	integer representing the input player
*	@param	item	the type of item collection to process	
*/
protected void getItem (int player, int item) {
//    Used for time-based items
      int potionTime = (50 + (50 * (int)(Math.round(Math.random() * 4) + 1)));
      int twentySidedDice = (int)((Math.random() * 20) + 1);
      int eightSidedDice = (int)(Math.random() * 8);

//    Filter Item operations based on the specific item collected
      switch (item) {
            case HEALTH:
                int lifeget = 0;
                if (player == MAIN_PLAYER) {
                    switch (hpboxtype) {
                        case 1:
                            lifeget = (maxHealthP * 2) - curHealthP;
                            break;
                        case 2:
                            lifeget = maxHealthP - curHealthP;
                            break;
                        default:
                            lifeget = extremeRules ? (int) Math.round(Math.random() * (18 * playerLevel)) : (int) Math.round(Math.random() * (24 * playerLevel));
                            break;
                    }

                    if ((curHealthP + lifeget) > maxHealthP) {
                        maxHealthP = curHealthP + lifeget;
                        curHealthP = maxHealthP;
                        displayAuxMessage("Life Maxed Out!");
                    } else {
                        curHealthP += lifeget;
                        displayAuxMessage("Health Crate!");
                    }
                } else if (player == MAIN_ENEMY) {
                //  The enemy player has variables to make his HP box appear when he collects HP
                    hpDrawn = true;
                    hpTime = 25;
                
                    switch (hpboxtype) {
                        case 1:
                            lifeget = (maxHealthE * 2) - curHealthE;
                            break;
                        case 2:
                            lifeget = maxHealthE - curHealthE;
                            break;
                        default:
                        //  Enemy players are not affected by the penalties of "Extreme Rules"
                            lifeget = (int) Math.round(Math.random() * (32 * enemyLevel));
                            break;
                    }
                    
                //  There was a bug in this section of code, displaying messages for the enemy collecting health...
                //  ...well as giving the player score for the ENEMY collecting health WTH?!
                    if ((curHealthE + lifeget) > maxHealthE) {
                        maxHealthE = curHealthE + lifeget;
                        curHealthE = maxHealthE;
                    } else {
                        curHealthE += lifeget;
                    }
                }

                healthLocat = OFFSCREEN;
            //  Determine what the next health crate item type will be
                switch (eightSidedDice + 1) {
                    case 8:
                        hpboxtype = 2;
                        break;
                    case 7:
                        hpboxtype = 1;
                        break;
                    default:
                        hpboxtype = 0;
                        break;
                }
                break;
            case EXPERIENCE:
                if (player == MAIN_PLAYER) {
                    exp += 200;
                    displayAuxMessage("Level Crate!"); 
                } else if (player == MAIN_ENEMY) {
                //  Display the enemy's experience bar
                    expDrawn = true;
                    expTime = -1;
                    exp_E += 200;
                }
                levelboxLocat = OFFSCREEN;
                break;
            case ONE_UP:
                if (player == MAIN_PLAYER) {
                    revivelimit++;
                    displayAuxMessage("ONE-UP!");
                }
                extraLifeLocat = OFFSCREEN;
                break;
            case WEAPON:
                if (player == MAIN_PLAYER) {
                    if(twentySidedDice == 20) {
                        playerWeapon = 8;
                    } else {
                        playerWeapon = eightSidedDice;
                    }
                    displayAuxMessage(weaponNames[playerWeapon] + " found");

                    switch(playerWeapon) {
                        case 2:
                            pWeapUses = 5;
                            break;
                        case 5:
                        case 6:
                        case 8:
                            pWeapUses = 1;
                            break;
                        default:
                            pWeapUses = ((9 - playerWeapon) * 10);
                            break;
                    }

                    pWeapLeft = pWeapUses;
                } else if (player == MAIN_ENEMY) {
                    if(twentySidedDice == 20) {
                        enemyWeapon = 8;
                    } else {
                        enemyWeapon = eightSidedDice;
                    }

                    switch(enemyWeapon) {
                        case 2:
                            eWeapUses = 5;
                            break;
                        case 5:
                        case 6:
                        case 8:
                            eWeapUses = 1;
                            break;
                        default:
                            eWeapUses = ((9 - enemyWeapon) * 10);
                            break;
                    }

                    eWeapLeft = eWeapUses;
                }
                weaponboxLocat = OFFSCREEN;
                break;
            case MEGA_HEALTH:
                if (player == MAIN_PLAYER) {
                    MegaHPRemain = (extremeRules) ? (int) Math.round(Math.random() * (24 * playerLevel)) : (int) Math.round(Math.random() * (32 * playerLevel));
                    hasMegaHP = true;
                    displayAuxMessage("Elixir of Life");
                } else if (player == MAIN_ENEMY) {
                //  The enemy player has variables to make his HP box appear when he collects HP
                    hpDrawn = true;
                    hpTime = -1;
                //  Enemy players are not affected by the penalties of "Extreme Rules"
                    MegaHPRemain_E = (int) Math.round(Math.random() * (32 * playerLevel));
                    hasMegaHP_E = true;
                }
                megahealthLocat = OFFSCREEN;
                break;
            case MEGA_EXPERIENCE:
                if (player == MAIN_PLAYER) {
                    MegaExpRemain = 300;
                    hasMegaExp = true;
                    displayAuxMessage("Vial of Wisdom");
                } else if (player == MAIN_ENEMY) {
                //  Display the enemy's experience bar
                    expDrawn = true;
                    expTime = -1;
                    MegaExpRemain_E = 300;
                    hasMegaExp_E = true;
                }
                megaexpLocat = OFFSCREEN;
                break;
            case GHOST_POTION:
                if (player == MAIN_PLAYER) {
                    isGhost1 = true;
                    u_no_see1 = false;

                    displayAuxMessage("He goes unseen...");
                    kohtimer.setText("" + potionTime);
                } else if (player == MAIN_ENEMY) {
                    isGhost2 = true;
                    u_no_see2 = false;
                    ghost_timer = potionTime;
                }
                ghostpotionLocat = OFFSCREEN;
                break;
            case LAMP:
                if (player == MAIN_PLAYER) {
                    LampRemain = 200;
                    hasLamp = true;

                //  Display witty status message
                    displayAuxMessage("Let there be light!");
                }
                lampLocat = OFFSCREEN;
                break;
            case ARMOR:
                if (player == MAIN_PLAYER) {
                    if (armorP1 < 200) {
                        armorP1 += 50;
                    } else {
                        armorP1 = 200;
                    }
                } else if (player == MAIN_ENEMY) {
                    if (armorP2 < 200) {
                        armorP2 += 50;
                    } else {
                        armorP2 = 200;
                    }
                }
                armorLocat = OFFSCREEN;
                break;
            case UNOSEE_POTION:
                if (player == MAIN_PLAYER) {
                    u_no_see1 = true;
                    isGhost1 = false;

                    kohtimer.setText("" + potionTime);
                } else if(player == MAIN_ENEMY) {
                    u_no_see2 = true;
                    isGhost2 = false;

                    u_no_see_timer = potionTime;
                }
                unoseeLocat = OFFSCREEN;
                break;
            case PAINKILLER:
                if(player == MAIN_PLAYER) {
                    painKillerP1 = true;
                    painKillerRemP1 = (painKillType == 0) ? 50 : 200;

                    if ((maxHealthP - curHealthP) < painKillerRemP1) {
                        painKillerRemP1 = (maxHealthP - curHealthP);
                        curHealthP = maxHealthP;
                    } else {
                        curHealthP += painKillerRemP1;
                    }
                } else if(player == MAIN_ENEMY) {
                    painKillerP2 = true;
                    painKillerRemP2 = (painKillType == 0) ? 50 : 200;

                    if ((maxHealthE - curHealthE) < painKillerRemP2) {
                        painKillerRemP2 = (maxHealthE - curHealthE);
                        curHealthE = maxHealthE;
                    } else {
                        curHealthE += painKillerRemP2;
                    }
                }
                painkLocat = OFFSCREEN;
                break;
            default:
                break;
        }

    //  Update the scoreboard
        if(player == MAIN_PLAYER) {
            scrboard.score += ITEMGET;
            scoreStr.setText("" + scrboard.score);
            playSound("krux/itemget.wav");
            items++;
        }
    }

    protected void displayAuxMessage(String message) {
        hasAUX = true;
        AUX = message;
        printDebugMessage("AUX MSG - " + message);
    }
	
// ============================ System Methods ============================
   
  /**
   *  Krux 2 Method
   * ==================================================================================
   *  printErrMeth
   *  Used by Microtech Applications for advanced error handling
   *
   *  @param      thr      The Throwable assosiated with the error  
   *  @param      meth     The name of the method that generated the error
   *  @param      serious  Defines if the error is serious or not
   */
   public void printErrMeth(final Throwable thr, final String meth, boolean serious) {
      JTextArea blueText = new JTextArea("===STOP ERROR: EXCEPTION IN KRUXLOADER===\n" + 
         					"An Exception has occured in method '" + meth + "' of KRUXLOADER\n\n" +
               			thr.toString() + "\n\n" +
               			"System operation has been halted to prevent futher errors.\n\n" +
         					"If this is the first time you are seeing this screen, it may be due to recent file or hardware changes.\n" +
               			"Remove or disable any new hardware or software and attempt to reboot your system. If this screen reoccurs, \n" +
         					"it is recommended that you contact your software provider or MicroTech Software Incorporated.\n" +
               			"An error descriptor 'errdesc.txt' has been created in the your root folder of Device 1 for reference");
         					
      if(!serious) {
         JOptionPane.showMessageDialog(
                        null,
                        "<html><b>An Exception has occured in '" + meth + "'</b><br>" +
               			"Exception Type:" + "\t\t" + "<i>" + thr.toString() + "</i><br>" +
               			"Operation has been halted to prevent futher errors.<br><br>" +
               			"<i>Please let MicroTech know of this error, an error descriptor has been created</i><br>" +
               			"<br><font color=blue>Descriptor:\t errdesc.txt.</font></html>",
                        "Error: " + thr.getMessage(),
                        JOptionPane.ERROR_MESSAGE);
         printErr.println("==START EXCEPTION==");
         printErr.println("Application Version:\t" + VERSION + ", Build " + BUILD);
         printErr.println("Release:\t\t" + RELEASE + ", " + DATE);
         printErr.println("Exception in File:\tkernel64");
         printErr.println("Exception Type:\t\t" + thr.toString());
         printErr.println("Exception Details:\t" + thr.getMessage());
         printErr.println("Exception Location:\t'" + meth + "'");
         printErr.println("==Exception Stack Trace==");
         thr.printStackTrace();
         thr.printStackTrace(printErr);
         printErr.println("==END OF EXCEPTION==");
         printErr.flush();
      }
      else {
      	// The Error was serious display a Red Screen of Death!
         GraphicsEnvironment h = GraphicsEnvironment.getLocalGraphicsEnvironment();
         GraphicsDevice g = h.getDefaultScreenDevice();
      
         printDebug.close();
      
         JWindow w = new JWindow();
         w.setContentPane(blueText);
         blueText.setBackground(Color.RED);
         blueText.setEditable(false);
         blueText.setForeground(Color.WHITE);
         blueText.setFont(new Font("Lucida Sans", Font.PLAIN, 12));
         blueText.addMouseListener(
                  new MouseAdapter() {
                     public void mousePressed(MouseEvent e) {
                        printErr.println("==START EXCEPTION==");
                        printErr.println("Application Version:\t" + VERSION + ", Build " + BUILD);
                        printErr.println("Release:\t\t" + RELEASE + ", " + DATE);
                        printErr.println("Exception in File:\tKRUXLOADER");
                        printErr.println("Exception Type:\t\t" + thr.toString());
                        printErr.println("Exception Details:\t" + thr.getMessage());
                        printErr.println("Exception Location:\t'" + meth + "'");
                        printErr.println("==Exception Stack Trace==");
                        thr.printStackTrace();
                        thr.printStackTrace(printErr);
                        printErr.println("==END OF EXCEPTION==");
                        printErr.flush();
                        System.exit(2);
                     }
                  });
         g.setFullScreenWindow(w);
         g.setDisplayMode(new DisplayMode(640, 480, 8, 70));
      }
   }
   
   protected void printDebugMessage(String message) {
      if(debugMode) {
         printDebug.println("[" + java.time.LocalDateTime.now().getHour() + "-" + java.time.LocalDateTime.now().getMinute() + "-" + java.time.LocalDateTime.now().getSecond() + "] KRUXTOURN (" + this.toString() + ") - " + message);
         System.out.println("[" + java.time.LocalDateTime.now().getHour() + "-" + java.time.LocalDateTime.now().getMinute() + "-" + java.time.LocalDateTime.now().getSecond() + "] KRUXTOURN (" + this.toString() + ") - " + message);
      }
   }
	
   public static void main(String[] args) {
      kruxloader w = new kruxloader();
      try {
        if(args.length > 0 && args[0].equals("-devmode")) {
         w.debugMode = true;
         System.out.println("Starting Krux Tourno...");
         System.out.println("== Developer Mode ==");
         System.out.println("Krux Series 3, Version " + VERSION + " Build " + BUILD);
         System.out.println("== Key Guide ==");
         System.out.println("[1]   View Player Life");
         System.out.println("[2]   View Player Level");
         System.out.println("[3]   View Player Weapon");
         System.out.println("[4]   View Enemy Life");
         System.out.println("[5]   View Enemy Level");
         System.out.println("[6]   View Enemy Weapon");
         System.out.println("[7]   View Player Positional");
         System.out.println("[8]   View Enemy Positional");
         System.out.println("[9]   View C.T.F Flag Positional");
         System.out.println("[0]   View OBERON AI Target\n");
            
         w.printDebug = new PrintWriter(new FileOutputStream(new File("debug_" + java.time.LocalDateTime.now().getDayOfMonth() + java.time.LocalDateTime.now().getMonth() + java.time.LocalDateTime.now().getYear() + "_"  + java.time.LocalDateTime.now().getHour() + java.time.LocalDateTime.now().getMinute() + ".txt")));
         }
      }
      catch(Exception e) {
         e.printStackTrace();
         w.printDebugMessage("INIT - EXCEPTION: " + e.getMessage() + ", Line: 9479");
      }
      w.initialize();
   }
}

// End of kruxWalker.java
// Thanks for playing! :-D

/**
*	Scoreboard Class
*/

class Scoreboard implements Serializable {
// Nominal Constants
   public static final int MAX_SCORE = 3500;
   public static final int MAX_KILLS = 12;
   public static final int MAX_ITEMS = 6;
   public static final int MAX_GEMSH = 50;
	
   public float SCORE_RATE = 0.0f;
   public float KILLS_RATE = 0.0f;
   public float ITEMS_RATE = 0.0f;
   public float GEMSH_RATE = 0.0f;
   public float OVRAL_RATE = 0.0f;
   
   public float LSCORE_RATE = 0.0f;
   public float LKILLS_RATE = 0.0f;
   public float LITEMS_RATE = 0.0f;
   public float LGEMSH_RATE = 0.0f;
   public float LOVRAL_RATE = 0.0f;
	
   public static int timer = 1;
	
   protected int score = 1;
   protected int kills = 1;
   protected int items = 1;
   protected int gemsh = 1;
	
   protected void updateRates() {
      if(timer < 100)
         timer = 100;
   
      SCORE_RATE = Math.max(SCORE_RATE, (float)((float)score / (float)timer) / MAX_SCORE * 100);
      KILLS_RATE = Math.max(KILLS_RATE, (float)((float)kills / ((float)timer / 100)) / MAX_KILLS * 100);
      ITEMS_RATE = Math.max(ITEMS_RATE, (float)((float)items / ((float)timer / 100)) / MAX_ITEMS * 100);
      GEMSH_RATE = Math.max(GEMSH_RATE, (float)((float)gemsh / ((float)timer / 100)) / MAX_GEMSH * 100);
      OVRAL_RATE = ((float)(SCORE_RATE + KILLS_RATE + ITEMS_RATE + GEMSH_RATE) / 4.0f);
   	
      if (SCORE_RATE > 100)
         SCORE_RATE = 100;
      if (KILLS_RATE > 100)
         KILLS_RATE = 100;
      if (ITEMS_RATE > 100)
         ITEMS_RATE = 100;
      if (GEMSH_RATE > 100)
         GEMSH_RATE = 100;
      
      LSCORE_RATE = (float)((float)score / (float)timer) / MAX_SCORE * 100;
      LKILLS_RATE = (float)((float)kills / ((float)timer / 100)) / MAX_KILLS * 100;
      LITEMS_RATE = (float)((float)items / ((float)timer / 100)) / MAX_ITEMS * 100;
      LGEMSH_RATE = (float)((float)gemsh / ((float)timer / 100)) / MAX_GEMSH * 100;
      LOVRAL_RATE = ((float)(SCORE_RATE + KILLS_RATE + ITEMS_RATE +  GEMSH_RATE) / 4.0f);
   }
   
   protected void updateCurrent() {
      LSCORE_RATE = (float)((float)score / (float)timer) / MAX_SCORE * 100;
      LKILLS_RATE = (float)((float)kills / ((float)timer / 100)) / MAX_KILLS * 100;
      LITEMS_RATE = (float)((float)items / ((float)timer / 100)) / MAX_ITEMS * 100;
      LGEMSH_RATE = (float)((float)gemsh / ((float)timer / 100)) / MAX_GEMSH * 100;
      LOVRAL_RATE = ((float)(LSCORE_RATE + LKILLS_RATE + LGEMSH_RATE + LOVRAL_RATE) / 5.0f);
   }
	
   protected void printRates() {
      System.out.println("== Nominal Rates ==");
      System.out.println("= Maximum =");
      System.out.println("Score: " + (int)SCORE_RATE + "%");
      System.out.println("Kills: " + (int)KILLS_RATE + "%");
      System.out.println("Items: " + (int)ITEMS_RATE + "%");
      System.out.println("Gems: " + (int)GEMSH_RATE + "%");
      System.out.println("===================");
      System.out.println("Overall: " + (int)OVRAL_RATE + "%\n");
      
      System.out.println("= Last =");
      System.out.println("Score: " + (int)LSCORE_RATE + "%");
      System.out.println("Kills: " + (int)LKILLS_RATE + "%");
      System.out.println("Items: " + (int)LITEMS_RATE + "%");
      System.out.println("Gems: " + (int)LGEMSH_RATE + "%");
      System.out.println("===================");
      System.out.println("Overall: " + (int)LOVRAL_RATE + "%\n");
   }
   
   public Scoreboard() {
      score = 0;
      kills = 0;
      items = 0;
      gemsh = 0;
   	
      updateRates();
   }
}