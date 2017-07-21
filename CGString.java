  /**
*  Krux 3 Class
* ===================================================
*  CGString
*  Krux 3 Character Set Drawing System, Version 2
*  Copyright(c) Microtech Technologies 2009
*
* @see      Object
* @see      kruxloader.CGString
* @version  2.1  May 14,2017
* @author   Byron Kleingeld
*/

import java.awt.image.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;

public class CGString {
   public static String CLASSVERSION = "2.1.025";
 
   public static final int ALIGN_LEFT = 0;
   public static final int ALIGN_RIGHT = 1;
   
   public static final int STANDARD = 0;
   public static final int DIGITAL = 1;
   public static final int SM_DIGITAL = 2;
       
   protected String cgstring;
   protected char[] datas = new char[255];
   protected Image[] chars;
   protected BufferedImage thisString;
   protected Graphics g;
   protected int sze;
   protected int align = 0;
   protected int strtype = STANDARD;
 
   public CGString(String data) {
      cgstring = data;
      datas = cgstring.toCharArray();
      sze = 12;
      
      thisString = new BufferedImage(145, 16, BufferedImage.TYPE_INT_ARGB);
      g = thisString.createGraphics();
      
      drawString();
   }
   
   public CGString(String data, int spaces) {
      cgstring = data;
      datas = cgstring.toCharArray();
      sze = spaces;
      
      thisString = new BufferedImage(12 * spaces + 1, 16, BufferedImage.TYPE_INT_ARGB);
      g = thisString.createGraphics();
      
      drawString();
   }
   
   public CGString(String data, int spaces, int alignm) {
      cgstring = data;
      datas = cgstring.toCharArray();
      sze = spaces;
      align = alignm;
      
      thisString = new BufferedImage(12 * spaces + 1, 16, BufferedImage.TYPE_INT_ARGB);
      g = thisString.createGraphics();
      
      drawString();
   }
   
   public CGString(String data, int spaces, int alignm, int type) {
      cgstring = data;
      datas = cgstring.toCharArray();
      sze = spaces;
      align = alignm;
      strtype = type;
      
      if (strtype == DIGITAL) {
         thisString = new BufferedImage(7 * spaces + 1, 11, BufferedImage.TYPE_INT_ARGB);
      }
      else {
         thisString = new BufferedImage(12 * spaces + 1, 16, BufferedImage.TYPE_INT_ARGB);
      }
      g = thisString.createGraphics();
      
      drawString();
   }
   
   public CGString(char[] data) {
      cgstring = new String(data);
      datas = data;
      sze = 12;
      
      thisString = new BufferedImage(144, 16, BufferedImage.TYPE_INT_ARGB);
      g = thisString.createGraphics();
      
      drawString();
   }
   
   protected void drawString() {
   // ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
      String temp = "";
      if(strtype == STANDARD) {
         // This section of code handles the drawing of the standard KRUX 3 fontface
         for (int i = 0; i < datas.length; i++) {
            if (align == 0) {
               if(datas[i] == ' ') {
               }
               else if (datas[i] == '.') {
                  try {
                     temp = String.valueOf(datas[i]);
                     temp = temp.toUpperCase();
                     g.drawImage(new ImageIcon(getClass().getResource("/charset/$dot.PNG")).getImage(), (i * 12), 0, null);
                  
                  }
                  catch (Exception e) {
                  }
               }
               else {
                  try {
                     temp = String.valueOf(datas[i]);
                     temp = temp.toUpperCase();
                     g.drawImage(new ImageIcon(getClass().getResource("/charset/" + temp + ".PNG")).getImage(), (i * 12), 0, null);
                  
                  }
                  catch (Exception e) {
                  }
               }
            }
            else {
               if(datas[(datas.length - 1) - i] == ' ') {
               }
               else if(datas[(datas.length - 1) - i] == '.')  {
                  try {
                     temp = String.valueOf(datas[(datas.length - 1) - i]);
                     temp = temp.toUpperCase();
                     g.drawImage(new ImageIcon(getClass().getResource("/charset/$dot.PNG")).getImage(), thisString.getWidth() - ((i + 1) * 12), 0, null);
                  }
                  catch (Exception e) {
                     System.out.println(e.getMessage());
                     System.out.println("Unknown character was drawn!");
                  }
               }
               else {
                  try {
                     temp = String.valueOf(datas[(datas.length - 1) - i]);
                     temp = temp.toUpperCase();
                     g.drawImage(new ImageIcon(getClass().getResource("/charset/" + temp + ".PNG")).getImage(), thisString.getWidth() - ((i + 1) * 12), 0, null);
                  }
                  catch (Exception e) {
                     System.out.println(e.getMessage());
                     System.out.println("Unknown character was drawn!");
                  }
               }
            }
         }
      }
      else {
         // This section of code handles the drawing of the new "Digital" RTS 10 fontface
         for (int i = 0; i < datas.length; i++) {
            if (align == 0) {
               if(datas[i] == ' ') {
               }
               else {
                  try {
                     temp = String.valueOf(datas[i]);
                     temp = temp.toUpperCase();
                     g.drawImage(new ImageIcon(getClass().getResource("/charset/" + temp + "dig.PNG")).getImage(), (i * 7), 0, null);
                  
                  }
                  catch (Exception e) {
                     e.printStackTrace();
                  }
               }
            }
            else {
               if(datas[(datas.length - 1) - i] == ' ') {
               }
               else {
                  try {
                     temp = String.valueOf(datas[(datas.length - 1) - i]);
                     temp = temp.toUpperCase();
                     g.drawImage(new ImageIcon(getClass().getResource("/charset/" + temp + "dig.PNG")).getImage(), thisString.getWidth() - ((i + 1) * 7), 0, null);
                  
                  }
                  catch (Exception e) {
                     System.out.println(e.getMessage());
                     System.out.println("Unknown character was drawn!");
                  }
               }
            }
         }
      }
   }
   
   
   public void repaint() {
   // Activates the AlphaComposite function to allow the graphics to be properly cleared
      ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
      g.setColor(new Color(255, 255, 255, 0));
      g.fillRect(0, 0, thisString.getWidth(), thisString.getHeight());
      drawString();
   }
   
   public void setText(String data) {
      cgstring = data;
      datas = cgstring.toCharArray();
      repaint();
   }
   
   public String getText() {
      return cgstring;
   }
   
   public BufferedImage getDrawnString() {
      return thisString;
   }
}