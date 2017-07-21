   package xwin.kernel.directsound;

   import java.io.*;
   import javax.swing.*;
   import java.awt.event.*;
   import javax.sound.sampled.*;
   import javax.sound.midi.*;

    public class dsound implements Runnable {
      boolean midi;            // Are we playing a midi file or a sampled one?
      Sequence sequence;       // The contents of a MIDI file
      Sequencer sequencer;     // We play MIDI Sequences with a Sequencer
      Clip clip;               // Contents of a sampled audio file
      boolean playing = false; // whether the sound is currently playing
   
    // Length and position of the sound are measured in milliseconds for 
    // sampled sounds and MIDI "ticks" for MIDI sounds
      int audioLength;         // Length of the sound.  
      int audioPosition = 0;   // Current position within the sound
   
    // The following fields are for the GUI
    // Displays audioPosition as a number 
      Timer timer;
      int time = 0;
      File f;
    
       protected void initialize() throws IOException,
               UnsupportedAudioFileException,
               LineUnavailableException {
         f = new File("Untitled.au");
         midi = false;
      
         AudioInputStream ain = AudioSystem.getAudioInputStream(f);
         try {
            DataLine.Info info =
                    new DataLine.Info(Clip.class,ain.getFormat( ));
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(ain);
         }
         finally { // We're done with the input stream.
            ain.close( );
         }
            // Get the clip length in microseconds and convert to milliseconds
         audioLength = (int)(clip.getMicrosecondLength( )/ 10000);
         new Thread(this).start();
      }
      
       public dsound(File h) throws IOException,
               UnsupportedAudioFileException,
               LineUnavailableException {
         f = h;
         midi = false;
      
         AudioInputStream ain = AudioSystem.getAudioInputStream(f);
         try {
            DataLine.Info info =
                    new DataLine.Info(Clip.class,ain.getFormat( ));
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(ain);
         }
         finally { // We're done with the input stream.
            ain.close( );
         }
            // Get the clip length in microseconds and convert to milliseconds
         audioLength = (int)(clip.getMicrosecondLength( )/ 10000);
      }
      
       public void playDSound() {
         new Thread(this).start();
      }
      
       public void stopPlayback() {
         stop();
      }
      
       public void run() {
         timer = new Timer(1, 
                new ActionListener( ) {
                   public void actionPerformed(ActionEvent e) {
                     time++;
                  }
               });
         if(!playing) {
            playthis();
         }
         else {
            stop();
         }
         while (true) {
            if(time >= audioLength) {
               stop();
               break;
            }
            try {
               Thread.sleep(1000);
            }
                catch (InterruptedException e) {
               }
         }
      }
   	
       private void playthis() {
         timer.start();
         clip.start( );
         playing = true;
      }
   	
       private void stop( ) {
         timer.stop();
         clip.stop( );
         playing = false;
      }
   }