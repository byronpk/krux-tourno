  /**
*  Krux 3 Class
* ===================================================
*  Oberon_AIDrivenEnemy
*  OBERON AI Bot Control Class, Version 13.11
*  Copyright(c) Microtech Technologies 2010
*  Copyright(c) Micron Information Systems 2017
*
* @see      Object
* @see      kruxloader
* @version  13.11.1921 May 14, 2017
* @author   Byron Kleingeld
*/

/* Updates made to this version before release:
* =========================================================================================================
*	-Update:		Ghost Potion now supported
*
*  From 13.11.1921 --> 56 Rebuilds
*/

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

public class Oberon_AIDrivenEnemy {
   public static String CLASSVERSION = "13.11.1921";
 
   protected String enemyTarget = "player";
   protected int locationEnemyX = 0; 		            // X-Location variable
   protected int locationEnemyY = 0; 		            // Y-Location variable
   protected int locationEnemyZ = 21; 						// Horizontal Size variable
   protected int locationEnemyR = 21; 						// Vertical Size variable
   public int enemyLevel = 1;
   protected int enemyWeapon = -1;
   protected int eWeapUses = 1;
   protected int eWeapLeft = 0;
   protected int eyesT = 0;
   public boolean enemyHater = false;
   
   public int life = 15;
   public int maximumE = 15;
   
   public static final int UP = 0;
   public static final int DOWN = 1;
   public static final int LEFT = 2;
   public static final int RIGHT = 3;
   
   public static final int ITEMGET = 500;
   public static final int ONEUPGET = 12500;
   public static final int WEAPONGET = 1250;
   public static final int ENEMY_DEFEAT = 120;
   public static final int HEALTHGET = 350;
   public static final int THIRD_DEFEAT = 60;
   public static final int DEFEAT = 50;
   public static final int HIT = 15;
   public static final int GETFLAG = 1000;
   
   protected int     spriteKey   = 0;
   public boolean    expDrawn    = false;
   protected int     expTime     = 0;
   public boolean    hpDrawn     = false;
   protected int     hpTime      = 0;
   
   public int        expForNext_E;
   public int        expLast_E   = 0;          // Last Requirement
   public int        exp_E       = 0;              // Current Experience

   protected int[]   movesBias   = new int[12];        // Moves that are made in advance
   protected int     moves       = 0;         // A Move counter
   protected int     maxMoves    = 4;      // The Move limit for the OBERON AI
   protected int     movetemp    = 0;      // a Temp field
   protected int     recalls     = 0;       // Debugging Field
   protected int     difficulty  = 2;    // Game difficulty
   
   protected int     strengthB    = ((int)(Math.random() * 5) * 10) + 60;
   protected int     enduranceB   = ((int)(Math.random() * 5) * 10) + 60;
   
   protected int     strengthIV    = (int)(Math.random() * 16);
   protected int     enduranceIV   = (int)(Math.random() * 16);
   protected int		lifeIV 		  = (int)(Math.random() * 16);
   
   protected int     strengthEV    = 0;
   protected int     enduranceEV   = 0;
   protected int		lifeEV 		  = 0;
   
   protected int     strength    = 0;
   protected int     endurance   = 0;
  
   protected boolean restrictE   = false;
   protected int     strategyHP  = 10;
   
   public kruxloader source      = null;
   
   protected boolean ghost    	= false;
   protected int     ghost_time 	= 0;
	
   protected boolean u_no_see    = false;
   protected int     unosee_time = 0;
   
   protected boolean painKiller  = false; // Reference for painkillers (OBJ_PKPILLSM)
   protected int		painKillRem = 0;
   protected int     pkcount		= 0;
   
   protected boolean hasMegaHP   = false;
   protected boolean hasMegaEXP  = false;
   protected int     MHPRemain   = 0;
   protected int     MEXPRemain  = 0;
   
   protected int     armor       = 0;
   protected int     armorHunger = 0;
   
   public boolean    isPoisoned  = false;
   public int        poisonLevel = 0;
   public boolean    isZapped    = false;
   public int        zaptimer    = 0;
   
   public boolean    isPredef    = false;
   public boolean    isCustom    = false;
   public boolean		hasCustomEyes = false;
   public String     predefName  = "notpredef";
   public String		myBotsName	= "notpredef";
   public String		customName  = "notcustom";
	
   protected String botnames[] = new String[] {
      	"Tumor",
      	"Malcolm",
      	"Bedwetter",
      	"James",
      	"Nutter",
      	"Pigwidgin",
      	"Giddy",
      	"Jack-O-Balls",
      	"Krimp",
      	"Jo-Jo",
      	"F(r)anny",
      	"Ellen-N-Anne",
      	"Bugbait",
      	"Flanagan",
      	"Golly-Wacker",
      	"Tally-The-Wacker",
      	"Beesting",
      	"Yum-Yum",
      	"Mr.Roboto",
      	"Fly",
      	"Ruby",
      	"Jam",
      	"Eggs-N-Ham",
      	"Lil' Ms. Sunshine",
      	"Johnny Riptide",
      	"Oo-M-Oo",
      };
      
   protected String deathStrings[] = new String[] {
      	" got oblitirated!",
      	" got his head removed!",
      	" bit the dust!",
      	" cried uncle!",
      	" went to Heaven!",
      	" got his butt wipped!",
      	" got creamed!",
      	" has lost his foothold!",
      	" is no more!",
      	" got scrambled!",
      	" got 'em self mooshed!",
      	" got a free autopsy!",
      	" was turned inside out!",
      	" had desconstructive surgery!",
      	" was removed, like a stain!",
      	" took a trip downstairs!",
      	" got his cord cut!",
      	"'s cake flopped!",
      	"'s head exploded!",
      	" failed his bloodtest!",
      	"'s light went out!",
      	"'s junk went to his trunk!",
      	" had two left feet!",
      	"'s sleeping with the fishies!",
      	" became Falcom Poop!",
      	" went GRENOW!"
      };
   
	/**
	*	AI Engine Constructor
	* =========================================================================
	*	* First Constructor *
	* 	
	*	Initializes a new instance of the AI Engine using the provided (kruxloader src) instance of the
	*	kruxloader.
	*/
   public Oberon_AIDrivenEnemy(kruxloader src, int level, int x, int y) {
      locationEnemyX = x;
      locationEnemyY = y;
   	
      spriteKey = (int) (Math.random() * 17);
      
      myBotsName = botnames[(int)(Math.random() * botnames.length)];
      
      source = src;
      
      if(spriteKey == 16)
         strategyHP += (Math.random() * 25);
      else if(spriteKey < 16)
         strategyHP += (Math.random() * 50);
      else if(predefName.equals("riptide"))
         strategyHP += (Math.random() * 20);
      
      if(predefName.equals("riptide"))
         armorHunger = (int)(Math.random() * 3) * 20;
      else
         armorHunger = (int)(Math.random() * 3) * 50;
      
      expDrawn = true;
      expTime = 15;
   	
      if(myBotsName.equals("Johnny Riptide")) {
         isPredef = true;
         predefName = "riptide";
      }
      else {
         if(spriteKey == 17)
            spriteKey = 16;
      
         if(spriteKey == 16)
            exp_E = source.CalculateEXP(enemyLevel);
         else
            exp_E = source.CalculateEXP(enemyLevel);
      }
   
      if(source.DEATHMATCHMODE) {
         if(source.lastWasRed) {
            enemyHater = true;
            locationEnemyX = source.spawnPoint1.x;
            locationEnemyY = source.spawnPoint1.y;
            source.lastWasRed = false;
            // System.out.println("JOINS BLUE TEAM");
         }
         else {
            enemyHater = false;
            source.lastWasRed = true;
            // System.out.println("JOINS RED TEAM");
         }
      }
      else {
         int chancer = (int) (Math.random() * 100);
         if (chancer <= 20) {
            enemyHater = true;
         }
         // System.out.println("JOINS RANDOM TEAM");
      }
      
      expForNext_E = source.CalculateEXP(enemyLevel + 1);
     
      enemyLevel = level;
         
      if(predefName.equals("riptide")) {
         strengthB += 10;
         enduranceB += 10;
      }
      	
      strength    = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
      endurance   = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
         
      expLast_E = exp_E;
         
      maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
      life = maximumE;
   	
      // printDebugMessage("Constructor Call: " + src + ", " + level + ", " + x + ", " + y);
   }
   
	/**
	* Custom Placement Constructor
	* ==================
	*
	*/
   public Oberon_AIDrivenEnemy(kruxloader src, int level, int x, int y, boolean third) {
      locationEnemyX = x;
      locationEnemyY = y;
      if(!third) {
         spriteKey = (int) (Math.random() * 17);
      }
      else
         spriteKey = 16;
      
      if(spriteKey == 17)
         spriteKey = 16;
      
      if(spriteKey == 16)
         exp_E = source.CalculateEXP(enemyLevel);
      else
         exp_E = source.CalculateEXP(enemyLevel);
      
      source = src;
      
      if(spriteKey == 16)
         strategyHP += (Math.random() * 25);
      else
         strategyHP += (Math.random() * 50);
         
      armorHunger = (int)(Math.random() * 3) * 50;
      
      expDrawn = true;
      expTime = 15;
   
      int chancer = (int) (Math.random() * 100);
      if (chancer <= 20) {
         enemyHater = true;
      }
      
      expForNext_E = source.CalculateEXP(enemyLevel + 1);
     
      enemyLevel = level;
         
      if(predefName.equals("riptide")) {
         strengthB += 10;
         enduranceB += 10;
      }
      	
      strength    = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
      endurance   = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
         
      expLast_E = exp_E;
         
      maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
      life = maximumE;
      
      myBotsName = botnames[(int)(Math.random() * botnames.length)];
   }
   
   public Oberon_AIDrivenEnemy(kruxloader src, Point pnt, String predef) {
      locationEnemyX = pnt.x;
      locationEnemyY = pnt.y;
      
      enemyLevel = (int) (Math.random() * 101);
      
      source = src;
   
      expDrawn = true;
      expTime = 15;
      
      armorHunger = (int)(Math.random() * 3) * 50;
      
      exp_E = source.CalculateEXP(enemyLevel);
   
      if(predef.equals("axion")) {
         isPredef = true;
         predefName = predef;
         
         strengthB	= 60;
         enduranceB	= 150;
      	
         strength    = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
         endurance   = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
         
         maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
         life = maximumE;
      
         expForNext_E = source.CalculateEXP(enemyLevel + 1);
      
         source.hasAUX = true;
         source.AUX = "AXION JOINS";
      }
      else if(predef.equals("cuma")) {
         isPredef = true;
         predefName = predef;
         
         strengthB	= 75;
         enduranceB	= 90;
      	
         strength    = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
         endurance   = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
         
         maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
         life = maximumE;
      
         expForNext_E = source.CalculateEXP(enemyLevel + 1);
      
         source.hasAUX = true;
         source.AUX = "CUMA JOINS";
      }
      else if(predef.equals("paxus")) {
         isPredef = true;
         predefName = predef;
         
         strengthB	= 80;
         enduranceB	= 80;
      	
         strength    = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
         endurance   = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
         
         maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
         life = maximumE;
      
         expForNext_E = source.CalculateEXP(enemyLevel + 1);
      
         source.hasAUX = true;
         source.AUX = "PAXUS JOINS";
      }
      else if(predef.equals("dunhill")) {
         isPredef = true;
         predefName = predef;
         
         strengthB	= 120;
         enduranceB	= 90;
      	
         strength    = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
         endurance   = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
         
         maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
         life = maximumE;
      
         expForNext_E = source.CalculateEXP(enemyLevel + 1);
      
         source.hasAUX = true;
         source.AUX = "DUNHILL JOINS";
      }
      else if(predef.equals("ranebou")) {
         isPredef = true;
         predefName = predef;
         
         strengthB	= 50;
         enduranceB	= 85;
      	
         strength    = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
         endurance   = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
         
         maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
         life = maximumE;
      
         expForNext_E = source.CalculateEXP(enemyLevel + 1);
      
         source.hasAUX = true;
         source.AUX = "RANEBOU JOINS";
      }
      else if(predef.equals("planeto")) {
         isPredef = true;
         predefName = predef;
         
         strengthB	= 95;
         enduranceB	= 160;
      	
         strength    = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
         endurance   = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
         
         maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
         life = maximumE;
      
         expForNext_E = source.CalculateEXP(enemyLevel + 1);
      
         source.hasAUX = true;
         source.AUX = "PLANETO JOINS";
      }
      else if(predef.equals("marbill")) {
         isPredef = true;
         predefName = predef;
         
         strengthB	= 45;
         enduranceB	= 125;
      	
         strength    = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
         endurance   = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
         
         maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
         life = maximumE;
      
         expForNext_E = source.CalculateEXP(enemyLevel + 1);
      
         source.hasAUX = true;
         source.AUX = "MARBILL JOINS";
      }
      else if(predef.equals("mudvayne")) {
         isPredef = true;
         predefName = predef;
         
         strengthB	= 110;
         enduranceB	= 105;
      	
         strength    = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
         endurance   = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
         
         maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
         life = maximumE;
      
         expForNext_E = source.CalculateEXP(enemyLevel + 1);
      
         source.hasAUX = true;
         source.AUX = "MUDVAYNE JOINS";
      }
      else if(predef.equals("grasso")) {
         isPredef = true;
         predefName = predef;
         
         strengthB	= 55;
         enduranceB	= 60;
      	
         strength    = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
         endurance   = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
         
         maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
         life = maximumE;
      
         expForNext_E = source.CalculateEXP(enemyLevel + 1);
      
         source.hasAUX = true;
         source.AUX = "GRASSO JOINS";
      }
      else if(predef.equals("dimpel")) {
         isPredef = true;
         predefName = predef;
         
         strengthB	= 95;
         enduranceB	= 100;
      	
         strength    = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
         endurance   = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
         
         maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
         life = maximumE;
      
         expForNext_E = source.CalculateEXP(enemyLevel + 1);
      
         source.hasAUX = true;
         source.AUX = "DIMPEL JOINS";
      }
      else if(predef.equals("blakforn")) {
         isPredef = true;
         predefName = predef;
         
         strengthB	= 125;
         enduranceB	= 135;
      	
         strength    = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
         endurance   = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
         
         maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
         life = maximumE;
      
         expForNext_E = source.CalculateEXP(enemyLevel + 1);
      
         source.hasAUX = true;
         source.AUX = "BLACKTHORN JOINS";
      }
      else if(predef.equals("corral")) {
         isPredef = true;
         predefName = predef;
         
         strengthB	= 90;
         enduranceB	= 105;
      	
         strength    = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
         endurance   = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
         
         maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
         life = maximumE;
      
         expForNext_E = source.CalculateEXP(enemyLevel + 1);
      
         source.hasAUX = true;
         source.AUX = predefName.toUpperCase() + " JOINS";
      }
      else {
      	// Interface for the support of custom characters
      	
         java.io.File custom = new java.io.File("custom/" + predef + ".png"); // Custom Character file
         java.io.File customEyes = new java.io.File("custom/customeyes/" + predef + "_eyes0.png"); // Custom Character Eyes
         if (custom.exists()) {
            isCustom = true;	// This is a custom sprite
            isPredef = true;	// This is a predefined enemy (in addition to being custom)
         
            if(customEyes.exists())
               hasCustomEyes = true; // This enemy has a custom eye-set
         
         	// Character Statusses
            int strup = 0;
            int endup = 0;
            float expmult = 0;
         
            customName = predef;
         	
         	// Attempt to load the Character File
            custom = new java.io.File("custom/" + predef + ".kcf");
            if (custom.exists()) {
               try {
                  String[] kcfload = new String[32];
                  String temp = "";
                  int count = 0;
               
                  java.io.BufferedReader input = new java.io.BufferedReader(new java.io.FileReader(custom));
               
                  while((temp = input.readLine()) != null) {
                     kcfload[count] = temp;
                     count++;
                  }
               
               /*	Offsets for Important Data in KCF File
               *	---------------------------------------------
               *	Name				Offset
               *	---------------------------------------------
               *	Name				1
               *	Strength			7
               *	Endurance		8
               *	StrIncrement	9
               *	EndIncrement	10
               *	ExpFactor		11
               */
               
                  predefName = kcfload[1];
               
                  strength    = Integer.parseInt(kcfload[7]);
                  endurance   = Integer.parseInt(kcfload[8]);
               
                  strup = Integer.parseInt(kcfload[9]);
                  endup = Integer.parseInt(kcfload[10]);
                  expmult = Float.parseFloat(kcfload[11]);
               
                  input.close();
               }
               catch(java.io.IOException ioe) {
                  ioe.printStackTrace();
                  // Loading has failed, make all statusses random to prevent game failure
                  predefName = predef;
                    
                  strengthB   = (int) (Math.random() * 11.0);
                  enduranceB   = (int) (Math.random() * 11.0);
                     
                  strength    = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
                  endurance   = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
               }
            }
            else {
            // There was no character file, the statusses must be random
               predefName = predef;
            
               strength    = (int) (Math.random() * 11.0);
               endurance   = (int) (Math.random() * 11.0);
               
               strength    = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
               endurance   = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
            }
         
            maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
            life = maximumE;
         
            exp_E = source.CalculateEXP(enemyLevel);	
            expForNext_E = source.CalculateEXP(enemyLevel + 1);
         
            source.hasAUX = true;
            source.AUX = predefName.toUpperCase() + " JOINS";	
         }
         else {
         	// The key was not valid
            source.hasAUX = true;
            source.AUX = "Invalid Key!";
         
            int level = (int) (source.exp * Math.random());
         
            locationEnemyX = pnt.x;
            locationEnemyY = pnt.y;
            spriteKey = (int) (Math.random() * 17);
         
            if(spriteKey == 17)
               spriteKey = 16;
         
            exp_E  = source.CalculateEXP(enemyLevel);
         
            source = src;
         
            if(spriteKey == 16)
               strategyHP += (Math.random() * 25);
            else
               strategyHP += (Math.random() * 50);
         
            armorHunger = (int)(Math.random() * 3) * 50;
         
            expDrawn = true;
            expTime = 15;
         
            int chancer = (int) (Math.random() * 100);
            if (chancer <= 20) {
               enemyHater = true;
            }
         
            expForNext_E  = source.CalculateEXP(enemyLevel + 1);
         
            strength = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
            endurance = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
            
            expLast_E = exp_E;
            
            maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
            life = maximumE;
         }
      }
   }
   
   public Point getLocat() {
      // // printDebugMessage("Location Call: " + locationEnemyX + ", " + locationEnemyY);
      return new Point(locationEnemyX, locationEnemyY);
   }
   
   public int getEyes() {
      return eyesT;
   }
   
   protected void getMLifeE() {
      hpDrawn = true;
      hpTime = -1;
      MHPRemain = (int) Math.round(Math.random() * (32 * enemyLevel));
      hasMegaHP = true;         
      source.megahealthLocat.x = (source.mapsize.x * 2);
      source.megahealthLocat.y = (source.mapsize.y * 2);
      source.grid.repaint();
      // printDebugMessage("Monitor: Elixir Item Found [" + MHPRemain + "]");
   }
   
   protected void getMExpE() {
      MEXPRemain = (int) Math.round(Math.random() * (64 * enemyLevel)) * 4;
      hasMegaEXP = true;
      expDrawn = true;
      expTime = -1;
      source.megaexpLocat.x = (source.mapsize.x * 2);
      source.megaexpLocat.y = (source.mapsize.y * 2);
      source.grid.repaint();
      // printDebugMessage("Monitor: Vial Item Found [" + MEXPRemain + "]");
   }
         
   protected void getNextMove() {
   // Check if the enemy isn't trapped inside a block
      if(source.testBoundsAt(this.getLocat(), 0) && source.testBoundsAt(this.getLocat(), 1) && source.testBoundsAt(this.getLocat(), 2) && source.testBoundsAt(this.getLocat(), 3))  {
         life = 0;

         locationEnemyX = (source.mapsize.x * 2);
         locationEnemyY = (source.mapsize.y * 2);
         source.grid.repaint();

         source.hasAUX = true;
         source.AUX = myBotsName + " was trapped inside a block and died!"; 
      }
      if(moves != 0) {
         getEnemyMove(movesBias[movetemp]);
         movetemp++;
         if(movetemp == moves) {
            moves = 0;
            movetemp = 0;
         }
      }
      else {
         getMove();
      }
   }
   
	/**
	* Processes combat variable from another bot character
	*
	*/
   protected boolean getHitS(Oberon_AIDrivenEnemy oaie) {
      int index = 0;
      if (endurance >= oaie.strength)
         index = 1;
      else 
         index = oaie.strength - endurance;
    
      boolean killed = false;
      if(source.DEATHMATCHMODE ? (source.FRIENDLYFIRE ?  true : !(enemyHater == oaie.enemyHater)) : true) {
         hpDrawn = true;
         hpTime = 25;
         if (oaie.enemyWeapon == 6) {
            life = 0;
         }
         else if (oaie.enemyWeapon == 2) {
            isPoisoned = true;
            poisonLevel += 2;
         }
         else if (oaie.enemyWeapon == 3) {
            int damageE = source.CalculateDamage(oaie.enemyLevel, oaie.strength, 20, endurance);
         
            if(armor > 0 && armor > (damageE / 2)) {
               armor -= (damageE / 2);
               damageE = (damageE / 2);
            }
            else if (armor < (damageE / 2)) {
               damageE -= armor;
               armor = 0;
            }
         
            life -= damageE;
            isZapped = true;
            zaptimer = (int) Math.round(Math.random() * 20);
         }
         else if (oaie.enemyWeapon == 4) {
            int damageE = source.CalculateDamage(oaie.enemyLevel, oaie.strength, 15, endurance);
         
            if(armor > 0 && armor > (damageE / 2)) {
               armor -= (damageE / 2);
               damageE = (damageE / 2);
            }
            else if (armor < (damageE / 2)) {
               damageE -= armor;
               armor = 0;
            }
         
            life -= damageE;
         
            int lifeget = damageE / 2;
         
            if((oaie.life + lifeget) > oaie.maximumE) {
               int temp = oaie.maximumE;
            
               oaie.maximumE = source.lifeEnemy + lifeget;
               oaie.life = oaie.maximumE;
            
               source.hasAUX = true;
               source.AUX = "HP Maxed out from " + temp + " to " + source.maximumE;
            }
            else
               oaie.life += lifeget;
         }
         else if (oaie.enemyWeapon == 5) {
            int damageE = source.CalculateDamage(oaie.enemyLevel, oaie.strength, 120, endurance);
         
            if(armor > 0 && armor > (damageE / 2)) {
               armor -= (damageE / 2);
               damageE = (damageE / 2);
            }
            else if (armor < (damageE / 2)) {
               damageE -= armor;
               armor = 0;
            }
         
            life -= damageE;
            isZapped = true;
            zaptimer = (int) Math.round(Math.random() * 50);
            isPoisoned = true;
            poisonLevel += 10;
         }
         else if (oaie.enemyWeapon == 7) {
            int damageE = source.CalculateDamage(oaie.enemyLevel, oaie.strength, 80, endurance);
            int damageP = damageE / 4; // Calculate Recoil Damage        
         
            Point myLocat = getLocat();
            Point hisLocat = oaie.getLocat();
         
            if(oaie.life < damageP) {
               oaie.enemyWeapon = -1;
            }
            else {
               oaie.life -= damageP;
            
            // Test for the radius damage
               int bnp = (int) Math.pow((Math.pow((double) (source.locationPlayerY - hisLocat.y), 2) + Math.pow((double) (source.locationPlayerX - hisLocat.x), 2)), 0.5);
               int dam = source.CalculateDamage(oaie.enemyLevel, oaie.strength, 40, source.enduranceP1);
            
               if(bnp <= 3) {
                  {
                     source.lifePlayer = (source.lifePlayer - dam);
                     source.damagePlayerPaint = true;
                     source.isPoisoned1 = true;
                     source.poisonLevel1 += 1;
                  
                     source.hasAUX = true;
                     source.AUX = "You were burned!";
                  
                     if(source.lifePlayer == 0) {
                        source.playSound("krux/explode.wav");
                        source.locationPlayerX = (source.mapsize.x * 2);
                        source.locationPlayerY = (source.mapsize.y * 2);
                        source.isDead = true;
                        source.grid.repaint();
                     }
                  }
               }
            
               bnp = (int) Math.pow((Math.pow((double) (source.locationEnemyY - hisLocat.y), 2) + Math.pow((double) (source.locationEnemyX - hisLocat.x), 2)), 0.5);
               dam = source.CalculateDamage(oaie.enemyLevel, oaie.strength, 40, source.enduranceP2);
            
               if(bnp <= 3) {
                  {
                     source.hpDrawn = true;
                     source.hpTime = 25;
                  
                     source.lifeEnemy = (source.lifeEnemy - dam);
                     source.damageEnemyPaint = true;
                  
                     source.isPoisoned2 = true;
                     source.poisonLevel2 += 1;
                  
                     if(source.lifeEnemy == 0) {
                        source.locationEnemyX = (source.mapsize.x * 2);
                        source.locationEnemyY = (source.mapsize.y * 2);
                        source.grid.repaint();
                        source.exp += maximumE;
                        source.rebornE();
                     }
                  }
               }
            
               for (int i = 0; i < source.aiEnemies.size(); i++) {
                  Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) source.aiEnemies.elementAt(i);
                  Point typer = aie.getLocat();      
               
                  if(typer.x == myLocat.x && typer.y == myLocat.y) {
                  }
                  else if(typer.x == hisLocat.x && typer.y == hisLocat.y) {
                  }
                  else {
                     bnp = (int) Math.pow((Math.pow((double) (typer.y - hisLocat.y), 2) + Math.pow((double) (typer.x - hisLocat.x), 2)), 0.5);
                     dam = source.CalculateDamage(oaie.enemyLevel, oaie.strength, 40, aie.endurance);
                  
                     if(bnp <= 3) {
                        if(aie.getHitWeapon7(dam)) {
                           try {
                              if(source.DEATHMATCHMODE) {
                                 aie.rebornE();
                              }
                              else {
                                 source.aiEnemies.removeElementAt(i);
                              }
                           }
                           catch (Exception e) {
                              source.hasAUX = true;
                              source.AUX = "Something sinistar happened!";
                                 // printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage());
                           }
                        }
                        else {
                           aie.isPoisoned = true;
                           aie.poisonLevel += 1;
                        }
                     }
                  }
               }
            
               life -= damageE;
               isPoisoned = true;
               poisonLevel += 1;
            }
         }
         else if (oaie.enemyWeapon == 8) {
            source.doDamageWeapon7(locationEnemyX, locationEnemyY);
         }
         else {
            int damageE = 0;
            if(oaie.enemyWeapon == 0)
               damageE = source.CalculateDamage(oaie.enemyLevel, oaie.strength, 35, endurance);
            else if(oaie.enemyWeapon == 1)
               damageE = source.CalculateDamage(oaie.enemyLevel, oaie.strength, 40, endurance);
            else
               damageE = source.CalculateDamage(oaie.enemyLevel, oaie.strength, 18, endurance);
         
            if(armor > 0 && armor > (damageE / 2)) {
               armor -= (damageE / 2);
               damageE = (damageE / 2);
            }
            else if (armor < (damageE / 2)) {
               damageE -= armor;
               armor = 0;
            }
         
            life -= damageE;
         }
         if(oaie.enemyWeapon != -1) { // If I have a weapon substract one AMMO from it
            if(oaie.eWeapLeft > 1) {
               oaie.eWeapLeft--; 
            }
            else {
               oaie.enemyWeapon = -1;
            }
         }
      
         source.score += HIT;
      
         if(life <= 0) {
            locationEnemyX = (source.mapsize.x * 2);
            locationEnemyY = (source.mapsize.y * 2);
            source.grid.repaint();
            killed = true;
         }
      	
         if(killed) {
            source.hasAUX = true;
            source.AUX = myBotsName + deathStrings[(int)(Math.random() * deathStrings.length)];
         // printDebugMessage("Destructor Called for " + myBotsName + "_" + this.toString());   
         }	                 
      	
         if(source.DEATHMATCHMODE && killed) {
            killed = false;
            rebornE();
         }
      }
      return killed;
   }
   
   protected boolean getHitE() {
      int index = 0;
      if (endurance >= source.strengthP2)
         index = 1;
      else 
         index = source.strengthP2 - endurance;
    
      boolean killed = false;
   	
      if(source.DEATHMATCHMODE ? (source.FRIENDLYFIRE ?  true : enemyHater) : true) {
         hpDrawn = true;
         hpTime = 25;
         if (source.enemyWeapon == 6) {
            life = 0;
         }
         else if (source.enemyWeapon == 2) {
            isPoisoned = true;
            poisonLevel += 2;
         }
         else if (source.enemyWeapon == 3) {
            int damageE = source.CalculateDamage(source.enemyLevel, source.strengthP2, 20, endurance);
         
            if(armor > 0 && armor > (damageE / 2)) {
               armor -= (damageE / 2);
               damageE = (damageE / 2);
            }
            else if (armor < (damageE / 2)) {
               damageE -= armor;
               armor = 0;
            }
         
            life -= damageE;
            isZapped = true;
            zaptimer = (int) Math.round(Math.random() * 20);
         }
         else if (source.enemyWeapon == 4) {
            int damageE = source.CalculateDamage(source.enemyLevel, source.strengthP2, 15, endurance);
         
            if(armor > 0 && armor > (damageE / 2)) {
               armor -= (damageE / 2);
               damageE = (damageE / 2);
            }
            else if (armor < (damageE / 2)) {
               damageE -= armor;
               armor = 0;
            }
         
            life -= damageE;
         
            int lifeget = damageE / 2;
         
            if((source.lifeEnemy + lifeget) > source.maximumE) {
               int temp = source.maximumE;
            
               source.maximumE = source.lifeEnemy + lifeget;
            
               source.lifeEnemy = (source.maximumE);
            
               source.hasAUX = true;
               source.AUX = "HP Maxed out from " + temp + " to " + source.maximumE;
            }
            else
               source.lifeEnemy = (source.lifeEnemy + lifeget);
         }
         else if (source.enemyWeapon == 5) {
            int damageE = source.CalculateDamage(source.enemyLevel, source.strengthP2, 120, endurance);
         
            if(armor > 0 && armor > (damageE / 2)) {
               armor -= (damageE / 2);
               damageE = (damageE / 2);
            }
            else if (armor < (damageE / 2)) {
               damageE -= armor;
               armor = 0;
            }
         
            life -= damageE;
            isZapped = true;
            zaptimer = (int) Math.round(Math.random() * 50);
            isPoisoned = true;
            poisonLevel += 10;
         }
         else if (source.enemyWeapon == 7) {
            int damageE = source.CalculateDamage(source.enemyLevel, source.strengthP2, 80, endurance);
            int damageP = damageE / 4; // Calculate Recoil Damage        
         
            if(source.lifeEnemy < damageP) {
               source.enemyWeapon = -1;
            }
            else {
               source.lifeEnemy = (source.lifeEnemy - damageP);
               source.damageEnemyPaint = true;
            
            // Test for the radius damage
               int bnp = (int) Math.pow((Math.pow((double) (source.locationPlayerY - source.locationEnemyY), 2) + Math.pow((double) (source.locationPlayerX - source.locationEnemyX), 2)), 0.5);
               int dam = source.CalculateDamage(source.enemyLevel, source.strengthP2, 40, source.enduranceP1);
            
               if(bnp <= 3) {
                  {
                     source.lifePlayer = (source.lifePlayer - dam);
                     source.damagePlayerPaint = true;
                     source.isPoisoned1 = true;
                     source.poisonLevel1 += 1;
                  
                     source.hasAUX = true;
                     source.AUX = "You were burned!";
                  
                     if(source.lifePlayer == 0) {
                        source.playSound("krux/explode.wav");
                        source.locationPlayerX = (source.mapsize.x * 2);
                        source.locationPlayerY = (source.mapsize.y * 2);
                        source.isDead = true;
                        source.grid.repaint();
                     }
                  }
               }
            
               for (int i = 0; i < source.aiEnemies.size(); i++) {
                  Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) source.aiEnemies.elementAt(i);
                  Point typer = aie.getLocat();
                  Point me = getLocat();          
               
                  if(typer.x == me.x && typer.y == me.y) {
                  }
                  else {
                     bnp = (int) Math.pow((Math.pow((double) (typer.y - source.locationEnemyY), 2) + Math.pow((double) (typer.x - source.locationEnemyX), 2)), 0.5);
                     dam = source.CalculateDamage(source.enemyLevel, source.strengthP2, 40, aie.endurance);
                  
                     if(bnp <= 3) {
                        if(aie.getHitWeapon7(dam)) {
                           try {
                              if(source.DEATHMATCHMODE) {
                                 aie.rebornE();
                              }
                              else {
                                 source.aiEnemies.removeElementAt(i);
                              }
                           }
                           catch (Exception e) {
                              source.hasAUX = true;
                              source.AUX = "Something sinistar happened!";
                                 // printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage());
                           }
                        }
                        else {
                           aie.isPoisoned = true;
                           aie.poisonLevel += 1;
                        }
                     }
                  }
               }
            
               life -= damageE;
               isPoisoned = true;
               poisonLevel += 1;
            }
         }
         else if (source.enemyWeapon == 8) {
            source.doDamageWeapon7(locationEnemyX, locationEnemyY);
         }
         else {
            int damageE = 0;
          
            if(source.enemyWeapon == 0) 
               damageE = source.CalculateDamage(source.enemyLevel, source.strengthP2, 35, endurance);
            else if(source.enemyWeapon == 0)  
               damageE = source.CalculateDamage(source.enemyLevel, source.strengthP2, 40, endurance);
            else
               damageE = source.CalculateDamage(source.enemyLevel, source.strengthP2, 18, endurance);
         
            if(armor > 0 && armor > (damageE / 2)) {
               armor -= (damageE / 2);
               damageE = (damageE / 2);
            }
            else if (armor < (damageE / 2)) {
               damageE -= armor;
               armor = 0;
            }
         
            life -= damageE;
         }
         if(source.enemyWeapon != -1) { // If I have a weapon substract one AMMO from it
            if(source.eWeapLeft > 1) {
               source.eWeapLeft--; 
            }
            else {
               source.enemyWeapon = -1;
            }
         }
      
         if(life <= 0) {
            locationEnemyX = (source.mapsize.x * 2);
            locationEnemyY = (source.mapsize.y * 2);
            source.grid.repaint();
            killed = true;
         }
      
         if(killed) {
            source.hasAUX = true;
            source.AUX = myBotsName + deathStrings[(int)(Math.random() * deathStrings.length)];
            // printDebugMessage("Destructor Called, Enemy Killed!");   
         }	
      
         if(source.DEATHMATCHMODE && killed) {
            killed = false;
            rebornE();
         }
      }
      return killed;
   }
   
	/*
	* Atom Bomb Method :)
	*/
   protected boolean getHitWeapon7(int damage) {
      boolean killed = false;
      hpDrawn = true;
      hpTime = 25;
      
      life -= damage;
      
      if(life <= 0) {
         source.playSound("krux/explode.wav");
         source.score += (source.ENEMY_DEFEAT * enemyLevel);
         
         locationEnemyX = (source.mapsize.x * 2);
         locationEnemyY = (source.mapsize.y * 2);
         source.grid.repaint();
         killed = true;
      }
      
      if(killed) {
         source.hasAUX = true;
         source.AUX = myBotsName + deathStrings[(int)(Math.random() * deathStrings.length)];
         // printDebugMessage("Destructor Called, Enemy Killed!");   
      }	
   	
      if(source.DEATHMATCHMODE) {
         killed = false;
         rebornE();
      }
      return killed;
   }
   
//    Function to find out where the AI is currently heading
   public String getTarget() {
      return enemyTarget;
   }
   
   protected boolean getHit() {
      if(enemyHater) {
         source.hasAUX = true;
         if(source.DEATHMATCHMODE)
            source.AUX = "I'm going to$ " + enemyTarget;
         else
            source.AUX = "Take it easy, I'm friendly!";
      }
    
      int index = 0;
      if (endurance >= source.strengthP1)
         index = 1;
      else 
         index = source.strengthP1 - endurance;
    
      boolean killed = false;
   	
      if(source.DEATHMATCHMODE ? (source.FRIENDLYFIRE ?  true : !enemyHater) : true) {
         hpDrawn = true;
         hpTime = 25;
         if (source.playerWeapon == 6) {
            life = 0;
         }
         else if (source.playerWeapon == 2) {
            isPoisoned = true;
            poisonLevel += 2;
         }
         else if (source.playerWeapon == 3) {
            int damageE = source.CalculateDamage(source.playerLevel, source.strengthP1, 20, endurance);
         
            if(armor > 0 && armor > (damageE / 2)) {
               armor -= (damageE / 2);
               damageE = (damageE / 2);
            }
            else if (armor < (damageE / 2)) {
               damageE -= armor;
               armor = 0;
            }
         
            life -= damageE;
            isZapped = true;
            zaptimer = (int) Math.round(Math.random() * 20);
         }
         else if (source.playerWeapon == 4) {
            int damageE = source.CalculateDamage(source.playerLevel, source.strengthP1, 15, endurance);
         
            if(armor > 0 && armor > (damageE / 2)) {
               armor -= (damageE / 2);
               damageE = (damageE / 2);
            }
            else if (armor < (damageE / 2)) {
               damageE -= armor;
               armor = 0;
            }
         
            life -= damageE;
         
            int lifeget = damageE / 2;
         
            if((source.lifePlayer + lifeget) > source.maximumP) {
               int temp = source.maximumP;
            
               source.maximumP = source.lifePlayer + lifeget;
            
               source.lifePlayer = (source.maximumP);
            
               source.hasAUX = true;
               source.AUX = "HP Maxed out from " + temp + " to " + source.maximumP;
            }
            else
               source.lifePlayer = (source.lifePlayer + lifeget);
         }
         else if (source.playerWeapon == 5) {
            int damageE = source.CalculateDamage(source.playerLevel, source.strengthP1, 120, endurance);
         
            if(armor > 0 && armor > (damageE / 2)) {
               armor -= (damageE / 2);
               damageE = (damageE / 2);
            }
            else if (armor < (damageE / 2)) {
               damageE -= armor;
               armor = 0;
            }
         
            life -= damageE;
            isZapped = true;
            zaptimer = (int) Math.round(Math.random() * 50);
            isPoisoned = true;
            poisonLevel += 10;
         }
         else if (source.playerWeapon == 7) {
            int damageE = source.CalculateDamage(source.playerLevel, source.strengthP1, 80, endurance);
            int damageP = damageE / 4; // Calculate Recoil Damage        
         
            if(source.lifePlayer < damageP) {
               source.playerWeapon = -1;
            }
            else {
               source.lifePlayer = (source.lifePlayer - damageP);
               source.damagePlayerPaint = true;
            
            // Test for the radius damage
               int bnp = (int) Math.pow((Math.pow((double) (source.locationEnemyY - source.locationPlayerY), 2) + Math.pow((double) (source.locationEnemyX - source.locationPlayerX), 2)), 0.5);
               int dam = source.CalculateDamage(source.playerLevel, source.strengthP1, 40, source.enduranceP2);
            
               if(bnp <= 3) {
                  {
                     source.hpDrawn = true;
                     source.hpTime = 25;
                  
                     source.lifeEnemy = (source.lifeEnemy - dam);
                     source.damageEnemyPaint = true;
                  
                     source.isPoisoned2 = true;
                     source.poisonLevel2 += 1;
                  
                     if(source.lifeEnemy == 0) {
                        source.playSound("krux/explode.wav");
                        if(source.extremeRules)
                           source.score += (ENEMY_DEFEAT * enemyLevel * 2);
                        else
                           source.score += (ENEMY_DEFEAT * enemyLevel);
                     
                        source.scoreStr = new CGString("" + source.score, 8, CGString.ALIGN_RIGHT, CGString.DIGITAL);
                        source.locationEnemyX = (source.mapsize.x * 2);
                        source.locationEnemyY = (source.mapsize.y * 2);
                        source.grid.repaint();
                        source.exp += maximumE;
                        source.rebornE();
                     }
                  }
               }
            
               for (int i = 0; i < source.aiEnemies.size(); i++) {
                  Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) source.aiEnemies.elementAt(i);
                  Point typer = aie.getLocat();
                  Point me = getLocat();          
               
                  if(typer.x == me.x && typer.y == me.y) {
                  }
                  else {
                     bnp = (int) Math.pow((Math.pow((double) (typer.y - source.locationPlayerY), 2) + Math.pow((double) (typer.x - source.locationPlayerX), 2)), 0.5);
                     dam = source.CalculateDamage(source.playerLevel, source.strengthP1, 40, aie.endurance);
                  
                     if(bnp <= 3) {
                        if(aie.getHitWeapon7(dam)) {
                           try {
                              if(source.DEATHMATCHMODE) {
                                 aie.rebornE();
                              }
                              else {
                                 source.aiEnemies.removeElementAt(i);
                              }
                           }
                           catch (Exception e) {
                              source.hasAUX = true;
                              source.AUX = "Something sinistar happened!";
                                 // printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage());
                           }
                        }
                        else {
                           aie.isPoisoned = true;
                           aie.poisonLevel += 1;
                        }
                     }
                  }
               }
            
               life -= damageE;
               isPoisoned = true;
               poisonLevel += 1;
            }
         }
         else if (source.playerWeapon == 8) {
            source.doDamageWeapon7(locationEnemyX, locationEnemyY);
         }
         else {
            int damageE = 0;
         	
            if (source.playerWeapon == 0)
               damageE = source.CalculateDamage(source.playerLevel, source.strengthP1, 35, endurance);
            else if (source.playerWeapon == 0)
               damageE = source.CalculateDamage(source.playerLevel, source.strengthP1, 40, endurance);
            else
               damageE = source.CalculateDamage(source.playerLevel, source.strengthP1, 18, endurance);
         
            if(armor > 0 && armor > (damageE / 2)) {
               armor -= (damageE / 2);
               damageE = (damageE / 2);
            }
            else if (armor < (damageE / 2)) {
               damageE -= armor;
               armor = 0;
            }
         
            life -= damageE;
         }
         if(source.playerWeapon != -1) { // If I have a weapon substract one AMMO from it
            if(source.pWeapLeft > 1) {
               source.pWeapLeft--; 
            }
            else {
               source.playerWeapon = -1;
            }
         }
      
         if(life <= 0) {
            source.playSound("krux/explode.wav");
            source.score += (source.ENEMY_DEFEAT * enemyLevel);
         
            locationEnemyX = (source.mapsize.x * 2);
            locationEnemyY = (source.mapsize.y * 2);
            source.grid.repaint();
            killed = true;
         }
      
         if(killed) {
            source.hasAUX = true;
            source.AUX = myBotsName + deathStrings[(int)(Math.random() * deathStrings.length)];
            // printDebugMessage("Destructor Called for " + myBotsName + "_" + this.toString());   
         }	
      
         if(source.DEATHMATCHMODE && killed) {
            killed = false;
            source.kills++;
            rebornE();
         }
      }
      return killed;
   }
	
	/**
	*	Team Deathmatch Method
	* ============================================================
	*	Allows non-red enemies to respawn
	*/
   public void rebornE() {
      maximumE = (strength + endurance) * enemyLevel;
      
      life = (maximumE);
   	
      if(enemyHater) {
         locationEnemyX = source.spawnPoint1.x;
         locationEnemyY = source.spawnPoint1.y;
      }
      else {
         locationEnemyX = source.spawnPoint2.x;
         locationEnemyY = source.spawnPoint2.y;
      }
      enemyWeapon = -1;
      isZapped = false;
      isPoisoned = false;
      u_no_see = false;
      poisonLevel = 0;
      source.grid.repaint();
   }
   
   public Image getSprite() {
      Image ret = null;
      
      if(!isPredef) {
         ret = new ImageIcon(getClass().getResource("/krux3/aisprite/sprite" + spriteKey + ".png")).getImage();
      }
      else if (isCustom) {
         ret = new ImageIcon("custom/" + customName + ".png").getImage();
      }
      else {
         ret = new ImageIcon(getClass().getResource("/krux3/aisprite/" + predefName + ".png")).getImage();
      }
      
      return ret;
   }
   
   public Image getCustomEyes(int dir) {
      Image ret = null;
   	
      if(isCustom && hasCustomEyes) {
         ret = new ImageIcon("custom/customeyes/" + customName + "_eyes" + dir + ".png").getImage();
      }
   	
      return ret;
   }
   
  //  Check if an object is actually onscreen
   protected boolean isOnScreen(Point location) {
      boolean onScreen = false;
      
      if(location.x < source.mapsize.x && location.x > 1 && location.y < source.mapsize.y && location.y > 1) {
         onScreen = true;
      }
      
      return onScreen;
   }
    
   protected void getMove() {
      String lastTarg = "";
      Point local = new Point(0, 0);
      if(isPredef) {
         myBotsName	= predefName;
      }
      if(!isZapped) {
         lastTarg = enemyTarget;
      
         int bias = 0;
         int dir = 0;
         int think = 0;
         
         Point searchLocation = new Point(0,0);
         
         if(maximumE == 0) {
            maximumE += 1;
         }
         
         int hpr = (int) Math.pow((Math.pow((double) (locationEnemyY - source.healthLocat.y), 2) + Math.pow((double) (locationEnemyX - source.healthLocat.x), 2)), 0.5);
         int lvr = (int) Math.pow((Math.pow((double) (locationEnemyY - source.levelboxLocat.y), 2) + Math.pow((double) (locationEnemyX - source.levelboxLocat.x), 2)), 0.5);
         int wbr = (int) Math.pow((Math.pow((double) (locationEnemyY - source.weaponboxLocat.y), 2) + Math.pow((double) (locationEnemyX - source.weaponboxLocat.x), 2)), 0.5);
         int mhr = (int) Math.pow((Math.pow((double) (locationEnemyY - source.megahealthLocat.y), 2) + Math.pow((double) (locationEnemyX - source.megahealthLocat.x), 2)), 0.5);
         int mer = (int) Math.pow((Math.pow((double) (locationEnemyY - source.megaexpLocat.y), 2) + Math.pow((double) (locationEnemyX - source.megaexpLocat.x), 2)), 0.5);
         int enm = (int) Math.pow((Math.pow((double) (locationEnemyY - source.locationPlayerY), 2) + Math.pow((double) (locationEnemyX - source.locationPlayerX), 2)), 0.5);
         int enn = (int) Math.pow((Math.pow((double) (locationEnemyY - source.locationEnemyY), 2) + Math.pow((double) (locationEnemyX - source.locationEnemyX), 2)), 0.5);
         int arm = (int) Math.pow((Math.pow((double) (locationEnemyY - source.armorLocat.y), 2) + Math.pow((double) (locationEnemyX - source.armorLocat.x), 2)), 0.5);
         int uns = (int) Math.pow((Math.pow((double) (locationEnemyY - source.unoseeLocat.y), 2) + Math.pow((double) (locationEnemyX - source.unoseeLocat.x), 2)), 0.5);
         int gns = (int) Math.pow((Math.pow((double) (locationEnemyY - source.ghostpotionLocat.y), 2) + Math.pow((double) (locationEnemyX - source.ghostpotionLocat.x), 2)), 0.5);
      
         if (isOnScreen(source.unoseeLocat) && uns <= source.enemyLineOfSight) {
            enemyTarget = "unosee";
         }
         else if((((life / maximumE) * 100) <= strategyHP)) {
            if(Math.min(Math.min(hpr,mhr), 5) < 5) {
               if(hpr < mhr && isOnScreen(source.healthLocat)) {
                  searchLocation = source.findFreeBlock();
                  enemyTarget = "health";
               }
               else if(hpr > mhr && isOnScreen(source.megahealthLocat)) {
                  searchLocation = source.findFreeBlock();
                  enemyTarget = "megahealth";
               }
               else  {
                  searchLocation = source.findFreeBlock();
                  enemyTarget = "random";
               }
            }
            else  {
               searchLocation = source.findFreeBlock();
               enemyTarget = "random";
            }
         }
         else {
            if(enemyLevel < (source.levelPlayer * 0.75f) && isOnScreen(source.levelboxLocat)) {
               if(Math.min(Math.min(lvr,mer), source.enemyLineOfSight) < source.enemyLineOfSight) {
                  if(lvr < mer && isOnScreen(source.levelboxLocat)) {  
                     searchLocation = source.findFreeBlock();
                     enemyTarget = "levels";
                  }
                  else if(lvr > mer && isOnScreen(source.megaexpLocat)) {
                     searchLocation = source.findFreeBlock();
                     enemyTarget = "megalevels";
                  }
                  else {
                     searchLocation = source.findFreeBlock();
                     enemyTarget = "random";
                  }
               }
               else  {
                  searchLocation = source.findFreeBlock();
                  enemyTarget = "random";
               }
            }
            else if(enemyWeapon == -1 && isOnScreen(source.weaponboxLocat) && source.difficulty > 2 && wbr <= source.enemyLineOfSight) {
               searchLocation = source.findFreeBlock();
               enemyTarget = "weapons";
            }
            else if(armor < armorHunger && isOnScreen(source.armorLocat) && arm <= source.enemyLineOfSight) {
               searchLocation = source.findFreeBlock();
               enemyTarget = "armor";
            }
            else {
               for (int i = 0; i < source.aiEnemies.size(); i++) {
                  Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) source.aiEnemies.elementAt(i);
                  local = aie.getLocat();
                  int enx = (int) Math.pow((Math.pow((double) (locationEnemyY - local.y), 2) + Math.pow((double) (locationEnemyX - local.x), 2)), 0.5);
                  boolean haters = aie.enemyHater;
               	
                  if(!(enemyHater == haters) && enx <= source.enemyLineOfSight && source.DEATHMATCHMODE) {
                     enemyTarget = "team";
                     break;
                  }
                  else if (isOnScreen(source.ghostpotionLocat) && gns <= source.enemyLineOfSight) {
                     enemyTarget = "ghost_p";
                     searchLocation = source.findFreeBlock();
                  }
                  else {
                     if(enemyHater) {
                        if(!source.u_no_see2 && isOnScreen(new Point(source.locationEnemyX, source.locationEnemyY)) && enn <= source.enemyLineOfSight) {
                           searchLocation = source.findFreeBlock();
                           enemyTarget = "enemy";
                        }
                        else if (enm <= source.enemyLineOfSight && source.DEATHMATCHMODE) {
                           searchLocation = source.findFreeBlock();
                           enemyTarget = "player";
                        }
                        else {
                           enemyTarget = "random";
                        }
                     }
                     else {
                        if(!source.u_no_see1 && isOnScreen(new Point(source.locationPlayerX, source.locationPlayerY)) && enm <= source.enemyLineOfSight) {
                           searchLocation = source.findFreeBlock();
                           enemyTarget = "player";
                        }
                        else if (enn <= source.enemyLineOfSight && source.DEATHMATCHMODE) {
                           searchLocation = source.findFreeBlock();
                           enemyTarget = "enemy";
                        }
                        else {
                           enemyTarget = "random";
                        }
                     }
                  }
               }
            }
         } 
         
         if (enemyTarget.equals("player")) {
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = new Point(source.locationPlayerX, source.locationPlayerY);
         
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
         else if (enemyTarget.equals("enemy")) {
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = new Point(source.locationEnemyX, source.locationEnemyY);
         
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
            Point him = new Point (source.ghostpotionLocat.x - 1, source.ghostpotionLocat.y - 1);
            
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
         else if (enemyTarget.equals("team")) {
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = new Point(local.x, local.y);
         
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
            Point him = new Point (source.armorLocat.x - 1, source.armorLocat.y - 1);
         
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
         else if (enemyTarget.equals("unosee")) {
            Point me = new Point(locationEnemyX, locationEnemyY);
            Point him = new Point (source.unoseeLocat.x - 1, source.unoseeLocat.y - 1);
         
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
            Point him = new Point(source.weaponboxLocat.x - 1, source.weaponboxLocat.y - 1);
         
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
            Point him = new Point (source.healthLocat.x - 1, source.healthLocat.y - 1);
         
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
            Point him = new Point (source.megahealthLocat.x - 1, source.megahealthLocat.y - 1);
         
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
            Point him = new Point (source.megaexpLocat.x - 1, source.megaexpLocat.y - 1);
         
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
            Point him = new Point (source.levelboxLocat.x - 1, source.levelboxLocat.y - 1);
         
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
            if(!isOnScreen(searchLocation))
               searchLocation = source.findFreeBlock();
            while(searchLocation.y < (source.mapsize.y / 2)) {
               searchLocation = source.findFreeBlock();
            }
         
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
         
         if(hasMegaEXP) {
            if(MEXPRemain > 2) {
               exp_E += 2;
               MEXPRemain -= 2;
            }
            else {
               exp_E += MEXPRemain;
               MEXPRemain = 0;
               hasMegaEXP = false;
               expDrawn = false;
            }
         }
      	
         if(ghost) {
            if(ghost_time != 0) {
               ghost_time -= 1;
            }
            else {
               ghost = false;
            }
         }
         
         if(u_no_see) {
            if(unosee_time != 0) {
               unosee_time -= 1;
            }
            else {
               u_no_see = false;
            }
         }
      
         if(hasMegaHP) {
            if(MHPRemain > 2) {
               life += 2;
               MHPRemain -= 2;
            }
            else {
               life += MHPRemain;
               MHPRemain = 0;
               hasMegaHP = false;
               hpDrawn = false;
            }
         }
         
         if(hpDrawn) {
            hpTime -= 1;
            if(hpTime == 0)
               hpDrawn = false;
         }
         
         if(expDrawn) {
            expTime -= 1;
            if(expTime == 0)
               expDrawn = false;
         }
         
         if(painKiller) {
            if(pkcount == 3) {
               if(painKillRem > 1) {
                  life -= 1;
                  painKillRem -= 1;
               }
               else {
                  life -= painKillRem;
                  painKillRem = 0;
                  painKiller = false;
               }
               pkcount = 0;
            }
            else {
               pkcount++;
            }
         }
         
         if(exp_E >= expForNext_E) {
            int last = ((strength + endurance) * enemyLevel);
            enemyLevel++;
         
            strength = source.CalculateStat(strengthIV, strengthB, strengthEV, enemyLevel);
            endurance = source.CalculateStat(enduranceIV, enduranceB, enduranceEV, enemyLevel);
         
            expLast_E = expForNext_E;
         
            maximumE = source.CalculateHP(lifeIV, lifeEV, enemyLevel);
            life = maximumE;
         
            expForNext_E = source.CalculateEXP(enemyLevel + 1);
         }
      }
      else {
         zaptimer -= 1;
         if(zaptimer == 0) {
            isZapped = false;
         }
      }
      
      //if(!enemyTarget.equals(lastTarg))
         // printDebugMessage("Seeker: Target Changed from " + lastTarg + " to " + enemyTarget + ".");   
   }
   
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

   protected void Oberon_SeekerBot (Point seekPoint, int directionalBias, Point target) {
      int DirectionalBias = directionalBias;
      int SearchingIn = 0;
      int MovesAhead = 0;
      boolean tester = true;
      boolean overflow = false;
      
      while (tester) {
         if (DirectionalBias == 0) {
            if(DirectionalBias != directionalBias)
               // printDebugMessage("Seeker: Bias switched to 0");
            
               if (seekPoint.y > target.y) {
                  SearchingIn = 0;
               }
               else if (seekPoint.y < target.y) {
                  SearchingIn = 1;
               }
         }
      
         if (DirectionalBias == 1) {
            if(DirectionalBias != directionalBias)
               // printDebugMessage("Seeker: Bias switched to 1");
            
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
            // printDebugMessage("Seeker: Direction Flipped");
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
            // printDebugMessage("Seeker: Move made using Move Buffer");
            break;
         }
         if(MovesAhead >= maxMoves / 2) {
            SearchingIn = changeDirection(SearchingIn);
         }
         if(MovesAhead == maxMoves) {
            tester = false;
            // printDebugMessage("Seeker: Move Buffer Overflow");
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
   
   protected void Oberon_NavigatorBot (Point seekPoint, int directionalBias, Point target) {
      int DirectionalBias = directionalBias;
      int SearchingIn = 0;
      int MovesAhead = 0;
      boolean tester = true;
      boolean overflow = false;
      
      while (tester) {
         if (DirectionalBias == 0) {
         // // // System.out.println("Bias0");
            if (seekPoint.y > target.y) {
               SearchingIn = 0;
            }
            else if (seekPoint.y < target.y) {
               SearchingIn = 1;
            }
         }
      
         if (DirectionalBias == 1) {
         // // // System.out.println("Bias1");
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
         // // // System.out.println("DirChange");
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
         // // // System.out.println("GenMove");
            break;
         }
         if(MovesAhead >= maxMoves / 2) {
            SearchingIn = changeDirection(SearchingIn);
         }
         if(MovesAhead == maxMoves) {
            tester = false;
         // // // System.out.println("MoveOverflow");
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
   
   protected void getEnemyMove(int direction) { // The enemy's movement method
      if(direction == UP) { // enemy moves up
         eyesT = 0;
         restrictE = false;
         if (locationEnemyY == 0) { // If I am against the side of the grid, I cannot move...
         }
         else if ((locationEnemyY - 1) == source.locationPlayerY && locationEnemyX == source.locationPlayerX) {
            doDamageEP(); // If I am about to collide with Player, I must damage him...
         }
         else if ((locationEnemyY - 1) == source.locationEnemyY && locationEnemyX == source.locationEnemyX) {
            doDamageEE(); // If I am about to collide with Enemy, I must damage him...
         }
         else {
            restrictE = testBoundsAt(new Point(locationEnemyX, locationEnemyY), 0);
            if (restrictE) { // If I am about to collide with a boundary, I must not move...
            }
            else {
               locationEnemyY--;
               source.grid.repaint();
            }
         }
         for (int i = 0; i < source.aiEnemies.size(); i++) {
            Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) source.aiEnemies.elementAt(i);
            Point typer = aie.getLocat();
            if ((locationEnemyY - 1) == typer.y && locationEnemyX == typer.x && (typer.y != locationEnemyY)) {
               if(aie.getHitS(this)) {
                  try {
                     exp_E += source.EXPGained(aie.enemyLevel, enemyLevel);
                     if(source.DEATHMATCHMODE) {
                        aie.rebornE();
                     }
                     else {
                        source.aiEnemies.removeElementAt(i);
                     }
                  }
                  catch (Exception e) {
                     source.hasAUX = true;
                     source.AUX = "Something sinistar happened!";
                        // printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage());
                  }
                  ;
               }
            }
         }
      }
    	/* I did not comment on the other directions because they're all essentially to same */
      if(direction == DOWN) { // enemy moves down
         eyesT = 1;
         restrictE = false;
         if (locationEnemyY == (source.mapsize.y - 1)) {
         }
         else if ((locationEnemyY + 1) == source.locationPlayerY && locationEnemyX == source.locationPlayerX) {
            doDamageEP();
         }
         else if ((locationEnemyY + 1) == source.locationEnemyY && locationEnemyX == source.locationEnemyX) {
            doDamageEE(); // If I am about to collide with Enemy, I must damage him...
         }
         else {
            restrictE = testBoundsAt(new Point(locationEnemyX, locationEnemyY), 1);
            if (restrictE) {
            }
            else {
               locationEnemyY++;
               source.grid.repaint();
            }
         }
         for (int i = 0; i < source.aiEnemies.size(); i++) {
            Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) source.aiEnemies.elementAt(i);
            Point typer = aie.getLocat();
            if ((locationEnemyY + 1) == typer.y && locationEnemyX == typer.x && (typer.y != locationEnemyY)) {
               if(aie.getHitS(this)) {
                  try {
                     exp_E += source.EXPGained(aie.enemyLevel, enemyLevel);
                     if(source.DEATHMATCHMODE) {
                        aie.rebornE();
                     }
                     else {
                        source.aiEnemies.removeElementAt(i);
                     }
                  }
                  catch (Exception e) {
                     source.hasAUX = true;
                     source.AUX = "Something sinistar happened!";
                        // printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage());
                  }
                  ;
               }
            }
         }
      }	
      if(direction == LEFT) { // enemy moves left
         eyesT = 2;
         restrictE = false;
         if (locationEnemyX == 0) {
         }
         else if ((locationEnemyX - 1) == source.locationPlayerX && locationEnemyY == source.locationPlayerY) {
            doDamageEP();
         }
         else if ((locationEnemyX - 1) == source.locationEnemyX && locationEnemyY == source.locationEnemyY) {
            doDamageEE(); // If I am about to collide with Enemy, I must damage him...
         }
         else {
            restrictE = testBoundsAt(new Point(locationEnemyX, locationEnemyY), 2);
            if (restrictE) {
            }
            else {
               locationEnemyX--;
               source.grid.repaint();
            }
         }
         for (int i = 0; i < source.aiEnemies.size(); i++) {
            Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) source.aiEnemies.elementAt(i);
            Point typer = aie.getLocat();
            if ((locationEnemyX - 1) == typer.x && locationEnemyY == typer.y && (typer.x != locationEnemyX)) {
               if(aie.getHitS(this)) {
                  try {
                     exp_E += source.EXPGained(aie.enemyLevel, enemyLevel);
                     if(source.DEATHMATCHMODE) {
                        aie.rebornE();
                     }
                     else {
                        source.aiEnemies.removeElementAt(i);
                     }
                  }
                  catch (Exception e) {
                     source.hasAUX = true;
                     source.AUX = "Something sinistar happened!";
                        // printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage());
                  }
                  ;
               }
            }
         }
      }	 
      if(direction == RIGHT) { // enemy moves right
         eyesT = 3;
         restrictE = false;
         if (locationEnemyX == (source.mapsize.x - 1)) {
         }
         else if ((locationEnemyX + 1) == source.locationPlayerX && locationEnemyY == source.locationPlayerY) {
         
            doDamageEP();
         }
         else if ((locationEnemyX + 1) == source.locationEnemyX && locationEnemyY == source.locationEnemyY) {
            doDamageEE(); // If I am about to collide with Enemy, I must damage him...
         }
         else {
            restrictE = testBoundsAt(new Point(locationEnemyX, locationEnemyY), 3);
            if (restrictE) {
            }
            else {
               locationEnemyX++;
               source.grid.repaint();
            }
         }
         for (int i = 0; i < source.aiEnemies.size(); i++) {
            Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) source.aiEnemies.elementAt(i);
            Point typer = aie.getLocat();
            if ((locationEnemyX + 1) == typer.x && locationEnemyY == typer.y && (typer.x != locationEnemyX)) {
               if(aie.getHitS(this)) {
                  try {
                     exp_E += source.EXPGained(aie.enemyLevel, enemyLevel);
                     if(source.DEATHMATCHMODE) {
                        aie.rebornE();
                     }
                     else {
                        source.aiEnemies.removeElementAt(i);
                     }
                  }
                  catch (Exception e) {
                     source.hasAUX = true;
                     source.AUX = "Something sinistar happened!";
                        // printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage());
                  }
                  ;
               }
            }
         }
      }	 		 		 	
   }
   
   protected void Oberon_NestThisMove (int direction) {
      if(moves < maxMoves) {
      // System.out.println("Nesting");
         movesBias[moves] = direction;
         moves++;
      }
   }
   
   protected void getUNoSeeE() {
      u_no_see = true;
      unosee_time = (50 + (50 * (int) (Math.round(Math.random() * 4) + 1)));
      
      source.unoseeLocat = new Point(source.mapsize.x * 2, source.mapsize.y * 2);
      // printDebugMessage("Monitor: U-No-See Found [" + unosee_time + "]");
   }
   
   protected void getGhostP() {
      ghost = true;
      ghost_time = (50 + (50 * (int) (Math.round(Math.random() * 4) + 1)));
      
      source.ghostpotionLocat = new Point(source.mapsize.x * 2, source.mapsize.y * 2);
      // printDebugMessage("Monitor: Ghost Potion Found [" + unosee_time + "]");
   }
   
   public void doDamageEE() {
      if(source.DEATHMATCHMODE ? (source.FRIENDLYFIRE ? true : enemyHater) : true) {
         int index = 0;
         if (source.enduranceP2 >= strength)
            index = 1;
         else 
            index = strength - source.enduranceP2;
      
         source.hpDrawn = true;
         source.hpTime = 25;
         if (enemyWeapon == 6) {
            source.lifeEnemy = (0);
         }
         else if (enemyWeapon == 2) {
            source.isPoisoned2 = true;
            source.poisonLevel2 += 2;
         }
         else if (enemyWeapon == 3) {
            source.damageE = source.CalculateDamage(enemyLevel, strength, 20, source.enduranceP2);
         
            if(source.armorP2 > 0 && source.armorP2 > (source.damageE / 2)) {
               source.armorP2 -= (source.damageE / 2);
               source.damageE = (source.damageE / 2);
            }
            else if (source.armorP2 < (source.damageE / 2)) {
               source.damageE -= source.armorP2;
               source.armorP2 = 0;
            }
         
            source.lifeEnemy = (source.lifeEnemy - source.damageE);
            source.damageEnemyPaint = true;
            source.isZapped2 = true;
            source.zaptimer2 = (int) Math.round(Math.random() * 20);
         }
         else if (enemyWeapon == 4) {
            source.damageE = source.CalculateDamage(enemyLevel, strength, 15, source.enduranceP2);
         
            if(source.armorP2 > 0 && source.armorP2 > (source.damageE / 2)) {
               source.armorP2 -= (source.damageE / 2);
               source.damageE = (source.damageE / 2);
            }
            else if (source.armorP2 < (source.damageE / 2)) {
               source.damageE -= source.armorP2;
               source.armorP2 = 0;
            }
         
            source.lifeEnemy = (source.lifeEnemy - source.damageE);
            source.damageEnemyPaint = true;
         
            int lifeget = source.damageE / 2;
            if((life + lifeget) > maximumE) {
               maximumE = life + lifeget;
               life = maximumE;
            }
            else {
               life += lifeget;
            }
            source.hasAUX = true;
            source.AUX = "HP Remain: " + life + " of " + maximumE;
            source.grid.repaint();
         }
         else if (enemyWeapon == 5) {
            source.damageE = source.CalculateDamage(enemyLevel, strength, 120, source.enduranceP2);
         
            if(source.armorP2 > 0 && source.armorP2 > (source.damageE / 2)) {
               source.armorP2 -= (source.damageE / 2);
               source.damageE = (source.damageE / 2);
            }
            else if (source.armorP2 < (source.damageE / 2)) {
               source.damageE -= source.armorP2;
               source.armorP2 = 0;
            }
         
            source.lifeEnemy = (source.lifeEnemy - source.damageE);
            source.damageEnemyPaint = true;
            source.isZapped2 = true;
            source.zaptimer2 = (int) Math.round(Math.random() * 50);
            source.isPoisoned2 = true;
            source.poisonLevel2 += 10;
         }
         else if (enemyWeapon == 8) {
            source.doDamageWeapon7(locationEnemyX, locationEnemyY);
         }
         else {
            if(enemyWeapon == 0)
               source.damageE = source.CalculateDamage(enemyLevel, strength, 35, source.enduranceP2);
            else if(enemyWeapon == 1)
               source.damageE = source.CalculateDamage(enemyLevel, strength, 40, source.enduranceP2);
            else
               source.damageE = source.CalculateDamage(enemyLevel, strength, 18, source.enduranceP2);
         
            if(source.armorP2 > 0 && source.armorP2 > (source.damageE / 2)) {
               source.armorP2 -= (source.damageE / 2);
               source.damageE = (source.damageE / 2);
            }
            else if (source.armorP2 < (source.damageE / 2)) {
               source.damageE -= source.armorP2;
               source.armorP2 = 0;
            }
         
            source.lifeEnemy = (source.lifeEnemy - source.damageE);
            source.damageEnemyPaint = true;
         }
         if(enemyWeapon != -1) { // If I have a weapon substract one AMMO from it
            if(eWeapLeft > 1) {
               eWeapLeft--; 
            }
            else {
               enemyWeapon = -1;
            }
         }
      
         if(source.lifeEnemy == 0) {
            exp_E += source.EXPGained(source.enemyLevel, enemyLevel);
            expDrawn = true;
            expTime = 25;
            source.playSound("krux/explode.wav");
            source.locationEnemyX = (source.mapsize.x * 2);
            source.locationEnemyY = (source.mapsize.y * 2);
            source.grid.repaint();
            source.rebornE();
         }
      }
   }
   
   public void doDamageEP() {
      if(source.DEATHMATCHMODE ? (source.FRIENDLYFIRE ? true : !enemyHater) : true) {
         int index = 0;
         if (source.enduranceP1 >= strength)
            index = 1;
         else 
            index = strength - source.enduranceP1;
      
         if (enemyWeapon == 6) {
            source.lifePlayer = (0);
         }
         else if (enemyWeapon == 8) {
            source.doDamageWeapon7(locationEnemyX, locationEnemyY);
         }
         else if (enemyWeapon == 2) {
            source.isPoisoned1 = true;
            source.poisonLevel1 += 2;
            source.hasAUX = true;
            source.AUX = "You have been poisoned";
         }
         else if (enemyWeapon == 3) {
            source.damageP = source.CalculateDamage(enemyLevel, strength, 20, source.enduranceP1);
         
            if(source.armorP1 > 0 && source.armorP1 > (source.damageP / 2)) {
               source.armorP1 -= (source.damageP / 2);
               source.damageP = (source.damageP / 2);
            }
            else if (source.armorP1 < (source.damageE / 2)) {
               source.damageP -= source.armorP1;
               source.armorP1 = 0;
            }
         
            source.lifePlayer = (source.lifePlayer - source.damageP);
            source.damagePlayerPaint = true;
            source.isZapped1 = true;
            source.zaptimer1 = (int) Math.round(Math.random() * 20);
            source.hasAUX = true;
            source.AUX = "You have been zapped";
         }
         else if (enemyWeapon == 4) {
            source.damageP = source.CalculateDamage(enemyLevel, strength, 15, source.enduranceP1);
         
            if(source.armorP1 > 0 && source.armorP1 > (source.damageP / 2)) {
               source.armorP1 -= (source.damageP / 2);
               source.damageP = (source.damageP / 2);
            }
            else if (source.armorP1 < (source.damageE / 2)) {
               source.damageP -= source.armorP1;
               source.armorP1 = 0;
            }
         
            source.lifePlayer = (source.lifePlayer - source.damageP);
            source.damagePlayerPaint = true;
         
            int lifeget = source.damageP / 2;
            if((life + lifeget) > maximumE) {
               maximumE = life + lifeget;
               life = maximumE;
            }
            else {
               life += lifeget;
            }
            source.hasAUX = true;
            source.AUX = "HP Remain: " + life + " of " + maximumE;
            source.grid.repaint();
         }
         else if (enemyWeapon == 5) {
            source.damageP = source.CalculateDamage(enemyLevel, strength, 120, source.enduranceP1);
         
            if(source.armorP1 > 0 && source.armorP1 > (source.damageP / 2)) {
               source.armorP1 -= (source.damageP / 2);
               source.damageP = (source.damageP / 2);
            }
            else if (source.armorP1 < (source.damageE / 2)) {
               source.damageP -= source.armorP1;
               source.armorP1 = 0;
            }
         
            source.lifePlayer = (source.lifePlayer - source.damageP);
            source.damagePlayerPaint = true;
            source.isZapped1 = true;
            source.zaptimer1 = (int) Math.round(Math.random() * 50);
            source.isPoisoned1 = true;
            source.poisonLevel1 += 10;
            source.hasAUX = true;
            source.AUX = "You have been intoxicated";
         }
         else {
            if(enemyWeapon == 0)
               source.damageP = source.CalculateDamage(enemyLevel, strength, 35, source.enduranceP1);
            else if(enemyWeapon == 1)
               source.damageP = source.CalculateDamage(enemyLevel, strength, 40, source.enduranceP1);
            else
               source.damageP = source.CalculateDamage(enemyLevel, strength, 18, source.enduranceP1);
         
            if(source.armorP1 > 0 && source.armorP1 > (source.damageP / 2)) {
               source.armorP1 -= (source.damageP / 2);
               source.damageP = (source.damageP / 2);
            }
            else if (source.armorP1 < (source.damageE / 2)) {
               source.damageP -= source.armorP1;
               source.armorP1 = 0;
            }
         
            source.lifePlayer = (source.lifePlayer - source.damageP);
            source.damagePlayerPaint = true;
         }
         if(enemyWeapon != -1) {
            if(eWeapLeft > 1) {
               eWeapLeft--; 
            }
            else {
               enemyWeapon = -1;
            }
         }
      
         if(source.lifePlayer == 0) {
            exp_E += source.EXPGained(source.playerLevel, enemyLevel);
            expDrawn = true;
            expTime = 25;
            source.playSound("krux/explode.wav");
            source.locationPlayerX = (source.mapsize.x * 2);
            source.locationPlayerY = (source.mapsize.y * 2);
            source.isDead = true;
            source.grid.repaint();
         }
      }
   }
   
   public boolean testBoundsAt(Point atpoint, int direction) { // Test if the enemy can move
      boolean out = false;
      try {
         for(int x = 0; x < source.maxbounds; x++) {
            if(direction == UP){ //up
               if ((atpoint.y - 1) == (source.bounds[x].y - 1) && atpoint.x == (source.bounds[x].x - 1)) {
                  if(!ghost)
                     out = true;
                  break;
               }
               else if ((atpoint.y - 1) == (source.mobileBounds[x].y - 1) && atpoint.x == (source.mobileBounds[x].x - 1)) {
                  if(!ghost)
                     out = true;
                  break;
               }
               else if (x < source.trackingbounds.length && (atpoint.y - 1) == (source.trackingbounds[x].y - 1) && atpoint.x == (source.trackingbounds[x].x - 1)) {
                  if(!ghost)
                     out = true;
                  break;
               }
               else if ((atpoint.y - 1) == (source.healthLocat.y - 1) && atpoint.x == (source.healthLocat.x - 1)) {
                  getLifeE();
                  break;
               }
               else if ((atpoint.y - 1) == (source.armorLocat.y - 1) && atpoint.x == (source.armorLocat.x - 1)) {
                  getArmorE();
                  break;
               }
               else if ((atpoint.y - 1) == (source.unoseeLocat.y - 1) && atpoint.x == (source.unoseeLocat.x - 1)) {
                  getUNoSeeE();
                  break;
               }
               else if ((atpoint.y - 1) == (source.ghostpotionLocat.y - 1) && atpoint.x == (source.ghostpotionLocat.x - 1)) {
                  getGhostP();
                  break;
               }
               else if ((atpoint.y - 1) == (source.painkLocat.y - 1) && atpoint.x == (source.painkLocat.x - 1)) {
                  getPainKiller();
                  break;
               }
               else if (source.kohmode && (atpoint.y - 1) == (source.kohlocat.y - 1) && atpoint.x == (source.kohlocat.x - 1)) {
                  out = false;
                  break;
               }
               else if ((atpoint.y - 1) == (source.weaponboxLocat.y - 1) && atpoint.x == (source.weaponboxLocat.x - 1)) {
                  if (enemyWeapon == -1) {
                     getWeaponE();
                     break;
                  }
                  else {
                     out = true;
                     break;
                  }
               }
               else if ((atpoint.y - 1) == (source.levelboxLocat.y - 1) && atpoint.x == (source.levelboxLocat.x - 1)) {
                  getLevelE();
               }
               else if ((atpoint.y - 1) == (source.megahealthLocat.y - 1) && atpoint.x == (source.megahealthLocat.x - 1)) {
                  getMLifeE();
               }
               else if ((atpoint.y - 1) == (source.megaexpLocat.y - 1) && atpoint.x == (source.megaexpLocat.x - 1)) {
                  getMExpE();
               }
               else if (x < source.floorsVcnt && (atpoint.y - 1) == (source.floorsV[x].y) && atpoint.x == (source.floorsV[x].x)) {
                  out = true;
                  break;
               }
               else {
                  out = false;
               }
               for (int i = 0; i < source.aiEnemies.size(); i++) {
                  Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) source.aiEnemies.elementAt(i);
                  Point typer = aie.getLocat();
                  if ((atpoint.y - 1) == typer.y && atpoint.x == typer.x && !(typer == atpoint)) {
                     out = true;
                     break;
                  }
               }
            }
            if(direction == DOWN){ //down
               if ((atpoint.y + 1) == (source.bounds[x].y - 1) && atpoint.x == (source.bounds[x].x - 1)) {
                  if(!ghost)
                     out = true;
                  break;
               }
               else if ((atpoint.y + 1) == (source.mobileBounds[x].y - 1) && atpoint.x == (source.mobileBounds[x].x - 1)) {
                  if(!ghost)
                     out = true;
                  break;
               }
               else if (x < source.trackingbounds.length && (atpoint.y + 1) == (source.trackingbounds[x].y - 1) && atpoint.x == (source.trackingbounds[x].x - 1)) {
                  if(!ghost)
                     out = true;
                  break;
               }
               else if ((atpoint.y + 1) == (source.healthLocat.y - 1) && atpoint.x == (source.healthLocat.x - 1)) {
                  getLifeE();
                  break;
               }
               else if ((atpoint.y + 1) == (source.armorLocat.y - 1) && atpoint.x == (source.armorLocat.x - 1)) {
                  getArmorE();
                  break;
               }
               else if ((atpoint.y + 1) == (source.unoseeLocat.y - 1) && atpoint.x == (source.unoseeLocat.x - 1)) {
                  getUNoSeeE();
                  break;
               }
               else if ((atpoint.y + 1) == (source.ghostpotionLocat.y - 1) && atpoint.x == (source.ghostpotionLocat.x - 1)) {
                  getGhostP();
                  break;
               }
               else if ((atpoint.y + 1) == (source.painkLocat.y - 1) && atpoint.x == (source.painkLocat.x - 1)) {
                  getPainKiller();
                  break;
               }
               else if (source.kohmode && (atpoint.y + 1) == (source.kohlocat.y - 1) && atpoint.x == (source.kohlocat.x - 1)) {
                  out = false;
                  break;
               }
               else if ((atpoint.y + 1) == (source.weaponboxLocat.y - 1) && atpoint.x == (source.weaponboxLocat.x - 1)) {
                  if (enemyWeapon == -1) {
                     getWeaponE();
                     break;
                  }
                  else {
                     out = true;
                     break;
                  }
               }
               else if ((atpoint.y + 1) == (source.levelboxLocat.y - 1) && atpoint.x == (source.levelboxLocat.x - 1)) {
                  getLevelE();
               }
               else if ((atpoint.y + 1) == (source.megahealthLocat.y - 1) && atpoint.x == (source.megahealthLocat.x - 1)) {
                  getMLifeE();
               }
               else if ((atpoint.y + 1) == (source.megaexpLocat.y - 1) && atpoint.x == (source.megaexpLocat.x - 1)) {
                  getMExpE();
               }
               else if (x < source.floorsVcnt && (atpoint.y + 1) == (source.floorsV[x].y) && atpoint.x == (source.floorsV[x].x)) {
                  out = true;
                  break;
               }
               else {
                  out = false;
               }
               for (int i = 0; i < source.aiEnemies.size(); i++) {
                  Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) source.aiEnemies.elementAt(i);
                  Point typer = aie.getLocat();
                  if ((atpoint.y + 1) == typer.y && atpoint.x == typer.x && !(typer == atpoint)) {
                     out = true;
                     break;
                  }
               }
            }
            if(direction == LEFT){ //left
               if ((atpoint.x - 1) == (source.bounds[x].x - 1) && atpoint.y == (source.bounds[x].y - 1)) {
                  if(!ghost)
                     out = true;
                  break;
               }
               else if ((atpoint.x - 1) == (source.mobileBounds[x].x - 1) && atpoint.y == (source.mobileBounds[x].y - 1)) {
                  if(!ghost)
                     out = true;
                  break;
               }
               else if (x < source.trackingbounds.length && (atpoint.x - 1) == (source.trackingbounds[x].x - 1) && atpoint.x == (source.trackingbounds[x].y - 1)) {
                  if(!ghost)
                     out = true;
                  break;
               }
               else if ((atpoint.x - 1) == (source.healthLocat.x - 1) && atpoint.y == (source.healthLocat.y - 1)) {
                  getLifeE();
                  break;
               }
               else if ((atpoint.x - 1) == (source.armorLocat.x - 1) && atpoint.y == (source.armorLocat.y - 1)) {
                  getArmorE();
                  break;
               }
               else if ((atpoint.x - 1) == (source.unoseeLocat.x - 1) && atpoint.y == (source.unoseeLocat.y - 1)) {
                  getUNoSeeE();
                  break;
               }
               else if ((atpoint.x - 1) == (source.ghostpotionLocat.x - 1) && atpoint.y == (source.ghostpotionLocat.y - 1)) {
                  getGhostP();
                  break;
               }
               else if ((atpoint.x - 1) == (source.painkLocat.x - 1) && atpoint.y == (source.painkLocat.y - 1)) {
                  getPainKiller();
                  break;
               }
               else if (source.kohmode && (atpoint.x - 1) == (source.kohlocat.x - 1) && atpoint.y == (source.kohlocat.y - 1)) {
                  out = false;
                  break;
               }
               else if ((atpoint.x - 1) == (source.weaponboxLocat.x - 1) && atpoint.y == (source.weaponboxLocat.y - 1)) {
                  if (enemyWeapon == -1) {
                     getWeaponE();
                     break;
                  }
                  else {
                     out = true;
                     break;
                  }
               }
               else if ((atpoint.x - 1) == (source.levelboxLocat.x - 1) && atpoint.y == (source.levelboxLocat.y - 1)) {
                  getLevelE();
               }
               else if ((atpoint.x - 1) == (source.megahealthLocat.x - 1) && atpoint.y == (source.megahealthLocat.y - 1)) {
                  getMLifeE();
               }
               else if ((atpoint.x - 1) == (source.megaexpLocat.x - 1) && atpoint.y == (source.megaexpLocat.y - 1)) {
                  getMExpE();
               }
               else if (x < source.floorsVcnt && (atpoint.x - 1) == (source.floorsV[x].x) && atpoint.y == (source.floorsV[x].y)) {
                  out = true;
                  break;
               }
               else {
                  out = false;
               }
               for (int i = 0; i < source.aiEnemies.size(); i++) {
                  Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) source.aiEnemies.elementAt(i);
                  Point typer = aie.getLocat();
                  if ((atpoint.x - 1) == typer.x && atpoint.y == typer.y && !(typer == atpoint)) {
                     out = true;
                     break;
                  }
               }
            }
            if(direction == RIGHT){ //right
               if ((atpoint.x + 1) == (source.bounds[x].x - 1) && atpoint.y == (source.bounds[x].y - 1)) {
                  if(!ghost)
                     out = true;
                  break;
               }
               else if ((atpoint.x + 1) == (source.mobileBounds[x].x - 1) && atpoint.y == (source.mobileBounds[x].y - 1)) {
                  if(!ghost)
                     out = true;
                  break;
               }
               else if (x < source.trackingbounds.length && (atpoint.x + 1) == (source.trackingbounds[x].x - 1) && atpoint.x == (source.trackingbounds[x].y - 1)) {
                  if(!ghost)
                     out = true;
                  break;
               }
               else if ((atpoint.x + 1) == (source.healthLocat.x - 1) && atpoint.y == (source.healthLocat.y - 1)) {
                  getLifeE();
                  break;
               }
               else if ((atpoint.x + 1) == (source.armorLocat.x - 1) && atpoint.y == (source.armorLocat.y - 1)) {
                  getArmorE();
                  break;
               }
               else if ((atpoint.x + 1) == (source.unoseeLocat.x - 1) && atpoint.y == (source.unoseeLocat.y - 1)) {
                  getUNoSeeE();
                  break;
               }
               else if ((atpoint.x + 1) == (source.ghostpotionLocat.x - 1) && atpoint.y == (source.ghostpotionLocat.y - 1)) {
                  getGhostP();
                  break;
               }
               else if ((atpoint.x + 1) == (source.painkLocat.x - 1) && atpoint.y == (source.painkLocat.y - 1)) {
                  getPainKiller();
                  break;
               }
               else if (source.kohmode && (atpoint.x + 1) == (source.kohlocat.x - 1) && atpoint.y == (source.kohlocat.y - 1)) {
                  out = false;
                  break;
               }
               else if ((atpoint.x + 1) == (source.weaponboxLocat.x - 1) && atpoint.y == (source.weaponboxLocat.y - 1)) {
                  if (enemyWeapon == -1) {
                     getWeaponE();
                     break;
                  }
                  else {
                     out = true;
                     break;
                  }
               }
               else if ((atpoint.x + 1) == (source.levelboxLocat.x - 1) && atpoint.y == (source.levelboxLocat.y - 1)) {
                  getLevelE();
               }
               else if ((atpoint.x + 1) == (source.megahealthLocat.x - 1) && atpoint.y == (source.megahealthLocat.y - 1)) {
                  getMLifeE();
               }
               else if ((atpoint.x + 1) == (source.megaexpLocat.x - 1) && atpoint.y == (source.megaexpLocat.y - 1)) {
                  getMExpE();
               }
               else if (x < source.floorsVcnt && (atpoint.x + 1) == (source.floorsV[x].x) && atpoint.y == (source.floorsV[x].y)) {
                  out = true;
                  break;
               }
               else {
                  out = false;
               }
               for (int i = 0; i < source.aiEnemies.size(); i++) {
                  Oberon_AIDrivenEnemy aie = (Oberon_AIDrivenEnemy) source.aiEnemies.elementAt(i);
                  Point typer = aie.getLocat();
                  if ((atpoint.x + 1) == typer.x && atpoint.y == typer.y && !(typer == atpoint)) {
                     out = true;
                     break;
                  }
               }
            }
         }
      }
      catch (Exception e) {
         source.hasAUX = true;
         source.AUX = "Stack breach at 0xBAADF00D";
            // printDebugMessage("Exception: " + e.toString() + ": " + e.getMessage());
      }
      return out;
   }
   
   public int getWeaponType() {
      return enemyWeapon;
   }
   
   protected void getArmorE() {
      if(armor < 200) {
         armor += 50;
      }
      else {
         armor = 200;
      }
      
      source.armorLocat = source.findFreeBlock();
      // printDebugMessage("Monitor: Armor Found");
   }
   
   protected void getLifeE() { // Enemy picks up a health box
      hpDrawn = true;
      hpTime = 25;
      int lifeget = 0;
      
      if(source.hpboxtype == 2) {
         lifeget = maximumE * 2 - life;
      }
      else if(source.hpboxtype == 1) {
         lifeget = maximumE - life;
      }
      else {
         lifeget = (int) Math.round(Math.random() * (32 * enemyLevel));
      }
      if((life + lifeget) > maximumE) {
         maximumE = life + lifeget;
         life = maximumE;
      }
      else {
         life += lifeget;
      }
      source.hasAUX = true;
      source.AUX = "HP Remain: " + life + " of " + maximumE;
      source.healthLocat = source.findFreeBlock();
      int g2e = (int) ((double) Math.random() * 9);
   
      if (g2e > 7)
         source.hpboxtype = 2;
      else if (g2e > 6)
         source.hpboxtype = 1;
      else
         source.hpboxtype = 0;
      source.grid.repaint();
      
      // printDebugMessage("Monitor: Health Item Found [" + lifeget + "]");
   }
   
   protected void getWeaponE() { // Enemy picks up a weapon box
      int weaponcounter = (int) ((Math.random() * 20) + 1);
      if(weaponcounter == 20) {
         enemyWeapon = 8;
      }
      else {
         enemyWeapon = (int) (Math.random() * 8);
      }
      
      source.playSound("krux/itemget.wav");
      source.weaponboxLocat = source.findFreeBlock();
      if(enemyWeapon >= 5 && enemyWeapon != 7) {
         eWeapUses = 1;
         eWeapLeft = eWeapUses; 
      }
      else if(enemyWeapon == 2) {
         eWeapUses = 5;
         eWeapLeft = eWeapUses;
      }
      else {
         eWeapUses = (int) ((9 - enemyWeapon) * 10);
         eWeapLeft = eWeapUses; 
      }
      source.grid.repaint();
      // printDebugMessage("Monitor: Weapon Found [" + enemyWeapon + "]");
   }
   
   protected void printDebugMessage(String message) {
      if(source.debugMode) {
         source.printDebug.println("[" + new java.util.Date().getHours() + ":" + new java.util.Date().getMinutes() + ":" + new java.util.Date().getSeconds() + "] OBERON_AI (" + myBotsName + "_" + this.toString() + ") - " + message);
         // System.out.println("[" + new java.util.Date().getHours() + ":" + new java.util.Date().getMinutes() + ":" + new java.util.Date().getSeconds() + "] OBERON_AI (" + myBotsName + "_" + this.toString() + ") - " + message);
      }
   }
   
	/**
	* 	Radioactive Reds(r) OBERON AI Engine
	* ==============================================
	*	Method:	getPainKiller
	*	Access:	protected
	*	Use:
	*	Called when the enemy aquires a painkiller
	*/
   protected void getPainKiller() {
      painKiller = true;
   	
      if(source.painKillType == 0)
         painKillRem = 50;
      else 
         painKillRem = 200;
      
      if((maximumE - life) < painKillRem) {
         painKillRem = (maximumE - life);
         life = maximumE;
      }
      else
         life += painKillRem;
   	
      source.painkLocat = source.OFFSCREEN;
   }
	   	
	/**
	* 	Radioactive Reds(r) OBERON AI Engine
	* ==============================================
	*	Method:	getLevelE
	*	Access:	protected
	*	Use:
	*	Called when the enemy aquires a level crate
	*/
   protected void getLevelE() {
      exp_E += 200;
      expDrawn = true;
      expTime = 25;
      source.levelboxLocat = source.findFreeBlock();
      source.grid.repaint();
      // printDebugMessage("Monitor: Experience Item Found [" + exp_E + "]");
   }
}