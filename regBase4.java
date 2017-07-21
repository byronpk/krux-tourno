  /*  Registration Code Database
	*	Version:					4.0.000
	*	Support Version: 		4.0.000
   *	Project "BRONZE ANACONDA"
	*
// [Copyrights and Notices]

// RCD, MTText, MTSheet, MTWord the MXS, MXD, KMF and MXT file formats, and the MXT file format are all Copyrights of
// MicroTech(C) Technologies.
// Java(tm) is a trademark of Sun(R) Microsystems
// Windows(tm) is a trademark of the Microsoft(R) Corporation
// Macintosh and Mac are trademark of Apple(R) Computers

// [About RCD4]

// This is a RCD4 Database. With that said it is important to know that
// RCD1, RCD2 and RCD3 registration codes are not compatible with this RCD version and
// that the prodwordpuct codes are read entirely differently than in previous versions.

// [About this '.java' file]

// This registration codebase is the centre of the MicroTech Registration
// System. It allows for the registration and activation of MicroTech prodwordpuct
// This file remains the same for all MicroTech Products with the exception of
// different regcodes.

// Take Note: This '.java' is compatible with only JRE 6 and cannot be run and compiled
//				  by earlier versions of JDK or JRE.
*/

   import java.io.*;
   import java.awt.*;
   import javax.swing.*;
   import java.awt.event.*;

    public class regBase4 {
      public static String CLASSVERSION = "4.1.035";
      public static String CLASSTYPE = "RCD4";
   	
      protected JFrame nano = new JFrame();
      protected static JTextField blockA = new JTextField();
      protected static JTextField blockB = new JTextField();
      protected static JTextField blockC = new JTextField();
      protected static JTextField blockD = new JTextField();
      protected static JTextField blockE = new JTextField();
      protected static JTextField blockF = new JTextField();
      protected static JButton comfirm = new JButton("Activate Product");
      protected static JButton decline = new JButton("Activate Later");
      
      protected String codestring = "";
      protected int[] code = new int[9];
      protected int[] testcode = new int[6];
      protected int[] inputcode = new int[6];
      protected String[] blocks = { "A", "B", "C", "D", "E", "F" };
      
      protected JDialog f = new JDialog(nano, "Product Activation", false);
    
      protected String name; 								
      protected String cump;
      protected String currLine; 							
      protected int i = 0; 
      protected int counter = 0;
      protected String[] regInfo = new String[6];
   													
      public String REGNAME;
      public String REGCUMP;
   	
      protected int programID;
      
      private fileCompressionUtil n = new fileCompressionUtil();
    
       public regBase4 (int proID) {
         programID = proID;
      }
    
       protected void enterCode() {
         try {
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
         	
            for (int i = 0; i < 9; i++) {
               if(i == 0) {
                  code[i] = programID;
               }
               else {
                  code[i] = (int) (100000 + (Math.random() * 899999.99));
               }
               codestring += String.valueOf(code[i]);
               if(i != 8) {
                  codestring += "-";
               }
            }
         
         // Generate the test code
            testcode[0] = (code[0] + code[8]) / 2;
            testcode[1] = (code[1] + code[7]) / 2;
            testcode[2] = (code[2] + code[6]) / 2;
            testcode[3] = (code[3] + code[5]) / 2;
            testcode[4] = (testcode[0] + testcode[1] + testcode[2] + testcode[3] + code[4]) / 5;
            testcode[5] = (code[0] + code[1] + code[2] + code[3] + code[4] + code[5] + code[6] + code[7] + code[8]) / 9;    
            
            JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
         	
            JPanel cde1 = new JPanel();
            cde1.setLayout(new BoxLayout(cde1, BoxLayout.Y_AXIS));
            cde1.add(blockA);
            JPanel cde2 = new JPanel();
            cde2.setLayout(new BoxLayout(cde2, BoxLayout.Y_AXIS));
            cde2.add(blockB);
            JPanel cde3 = new JPanel();
            cde3.setLayout(new BoxLayout(cde3, BoxLayout.Y_AXIS));
            cde3.add(blockC);
            JPanel cde4 = new JPanel();
            cde4.setLayout(new BoxLayout(cde4, BoxLayout.Y_AXIS));
            cde4.add(blockD);
            JPanel cde5 = new JPanel();
            cde5.setLayout(new BoxLayout(cde5, BoxLayout.Y_AXIS));
            cde5.add(blockE);
            JPanel cde6 = new JPanel();
            cde6.setLayout(new BoxLayout(cde6, BoxLayout.Y_AXIS));
            cde6.add(blockF);
         	
            JPanel cde7 = new JPanel();
            cde7.setLayout(new BoxLayout(cde7, BoxLayout.X_AXIS));
            cde7.add(Box.createRigidArea(new Dimension(20,0)));
            cde7.add(cde1);
            cde7.add(Box.createRigidArea(new Dimension(5,0)));
            cde7.add(cde2);
            cde7.add(Box.createRigidArea(new Dimension(5,0)));
            cde7.add(cde3);
            cde7.add(Box.createRigidArea(new Dimension(5,0)));
            cde7.add(cde4);
            cde7.add(Box.createRigidArea(new Dimension(5,0)));
            cde7.add(cde5);
            cde7.add(Box.createRigidArea(new Dimension(5,0)));
            cde7.add(cde6);
            cde7.add(Box.createRigidArea(new Dimension(20,0)));
         	
            p.add(cde7, "Center");
         	
            JPanel upper1 = 
                new JPanel() {
                   public void paintComponent(Graphics g) {
                     g.drawImage(new ImageIcon(getClass().getResource("/images/key.png")).getImage(), 0, 0, this);
                     g.setFont(new Font("Tahoma", Font.BOLD, 11));
                     g.drawString(codestring, 138, 103);
                  } 
               };
            upper1.setPreferredSize(new Dimension(631,146));
            
            JPanel coco = new JPanel();
            coco.setLayout(new BoxLayout(coco, BoxLayout.X_AXIS));
            coco.add(Box.createRigidArea(new Dimension(10,0)));
            coco.add(comfirm);
            coco.add(Box.createRigidArea(new Dimension(25,0)));
            coco.add(decline);
            coco.add(Box.createRigidArea(new Dimension(10,0)));
         	
            JPanel conf = new JPanel();
            conf.setLayout(new BoxLayout(conf, BoxLayout.Y_AXIS));
            conf.add(Box.createRigidArea(new Dimension(0,10)));
            conf.add(coco);
            conf.add(Box.createRigidArea(new Dimension(0,10)));
         	
            JPanel conf1 = new JPanel();
            conf1.setLayout(new BoxLayout(conf1, BoxLayout.X_AXIS));
            conf1.add(Box.createRigidArea(new Dimension(172,0)));
            conf1.add(conf);
            // nconf1.add(Box.createRigidArea(new Dimension(300,0)));
         	
            p.add(upper1, "North");
            p.add(conf1, "South");
         
            p.add(Box.createRigidArea(new Dimension(25,0)), "East");				
            f.setContentPane(p);
            f.setSize(681, 246);
            f.show();
         	
            f.addWindowListener(
                   new WindowAdapter() {
                      public void windowClosing(WindowEvent e) {
                        System.exit(0);
                     }
                  });
         	
            comfirm.addActionListener(
                   new ActionListener() {
                      public void actionPerformed(ActionEvent e) {
                        testReg();
                     }
                  });
         	
            decline.addActionListener(
                   new ActionListener() {
                      public void actionPerformed(ActionEvent e) {
							 	JOptionPane.showMessageDialog(
                           null,
               				"<html>This application <b>must</b> be activated before it can be used.<br>" +
									"The application will now close.</html>",
                           "Activation required!",
                           JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                     }
                  });
         			
         }
             catch (Throwable e) {
               System.err.println(e);
               e.printStackTrace();
            }
      }
      		
       protected void testReg() {
         try {
            boolean success = false;
            f.dispose();
            f = null;
            inputcode[0] = Integer.parseInt(blockA.getText());
            inputcode[1] = Integer.parseInt(blockB.getText());
            inputcode[2] = Integer.parseInt(blockC.getText());
            inputcode[3] = Integer.parseInt(blockD.getText());
            inputcode[4] = Integer.parseInt(blockE.getText());
            inputcode[5] = Integer.parseInt(blockF.getText());
                     
            for (int i = 0; i < 6; i++) {
               if(testcode[i] == inputcode[i]) {
                  success = true;
               }
               else {
                  JOptionPane.showMessageDialog(
                     null,
                     "Product Activation Failed due to the code error!",
                     "Registration Failed",
                     JOptionPane.ERROR_MESSAGE);
                  System.exit(1);
               }
            }
         	
            if (success) {
               FileWriter out1 = new FileWriter(String.valueOf(programID) + "prod.adf");
               out1.write(codestring);
               out1.close();
            
               FileWriter out = new FileWriter(String.valueOf(programID) + "reg.adf");
               name = JOptionPane.showInputDialog(
                  null,
                  "Enter Your Name",
                  "RCD4 Registraion Wizard",
                  JOptionPane.QUESTION_MESSAGE);;
               cump = JOptionPane.showInputDialog(
                  null,
                  "Enter Your Company",
                  "RCD4 Registraion Wizard",
                  JOptionPane.QUESTION_MESSAGE);
               if (code[0] != 0) {
                  JOptionPane.showMessageDialog(
                     null,
                     "Your MicroTech Product has been successfully registered to:" + '\n' +
                     name + '\n' +
                     cump + '\n' + '\n' +
                     "Product ID: " + codestring,
                     "RCD4 Registration Successful",
                     JOptionPane.INFORMATION_MESSAGE);
                  out.write("1" + "\r\n");
                  out.write(name + "\r\n");
                  out.write(cump + "\r\n");
                  out.write(codestring + "\r\n");
                  out.write(testcode[0] + "-" + testcode[1] + "-" + testcode[2] + "-" + testcode[3] + "-" + testcode[4] + "-" + testcode[5] + "\r\n");
                  out.write("RCD4");
                  out.close();
                  n.deflateFile(new File(String.valueOf(programID) + "reg.adf"));
               }
               else {
               }
               JOptionPane.showMessageDialog(
                           null,
                           "Registration Successful",
                           "Refresh Required",
                           JOptionPane.INFORMATION_MESSAGE);
               System.exit(0);
            }
         }
             catch (Throwable ef) {
               JOptionPane.showMessageDialog(
                     null,
                     "Product Activation Failed due to unexpected error!",
                     "Registration Failed",
                     JOptionPane.ERROR_MESSAGE);
               ef.printStackTrace();
               System.exit(1);
            }
      }
      
       public boolean runRegCheck() {
         boolean putty = false;
         try {
            String prodwordp = "";
            try {
               BufferedReader fileIn = new BufferedReader(new FileReader(String.valueOf(programID) + "prod.adf"));
            rest:
               while ((currLine = fileIn.readLine()) != null) {
                  prodwordp = currLine;
               }
               fileIn.close();
            }
                catch (Throwable e) {};
         	
            java.util.StringTokenizer st = new java.util.StringTokenizer(prodwordp, "-");
            String progID = st.nextToken();
            int proID = Integer.parseInt(progID);
         	
            if(proID == programID) {      
               counter = 0;
               n.inflateFile(new File(String.valueOf(programID) + "reg.adf"));
               BufferedReader fileIn = new BufferedReader(new FileReader(String.valueOf(programID) + "reg.adf"));
            rest:
               while ((currLine = fileIn.readLine()) != null) {
                  regInfo[counter] = currLine;
                  counter++;
               }
               fileIn.close();
               n.deflateFile(new File(String.valueOf(programID) + "reg.adf"));
            
               if (regInfo[0].equals("0")) {
                  putty = false;
               }
               else if (!regInfo[3].equals(runProdCodeCheck())) {
                  putty = false;
               }
               else {
                  putty = true;
                  REGNAME = regInfo[1];
                  REGCUMP = regInfo[2];
               }
            }
            else {
               putty = false;
            }
         }
             catch (Throwable e) {
               putty = false;
            }
         return putty;
      }
      
       public String runProdCodeCheck() {
         String prodwordp = "";
         try {
            BufferedReader fileIn = new BufferedReader(new FileReader(String.valueOf(programID) + "prod.adf"));
         rest:
            while ((currLine = fileIn.readLine()) != null) {
               prodwordp = currLine;
            }
            fileIn.close();
         }
             catch (Throwable e) {};
         return prodwordp;
      }
            
       public static void main(String args[]) {
         regBase4 t = new regBase4(100000);
         t.enterCode();
      }
   }
   
	// 242774	540513	346583	579267	376147	398875






