    // MicroTech(tm) File Compression Utility Class
    // Created By: Byron Kleingeld
	 // for use with MicroTech(tm) Remlocke(c) OS
	 
	 // Implements:
	 // java.util.zip.Inflater
	 // java.util.zip.Deflater
	 // java.util.zip.DataFormatException
	 // java.io.UnsupportedEncodingException

    public class fileCompressionUtil {
	 	public static String CLASSVERSION = "1.0.002";
      private int dataLength;
      
   	 // public byte[] inflateFromFile(java.io.File file) throws java.io.IOException, java.io.FileNotFoundException,	java.io.UnsupportedEncodingException, java.util.zip.DataFormatException
       
   	 // Inflates a stored file and returns the deflated contents as a byte array
   	 
   	 // -- Variables --
   	 // file - the deflated file to inflate
   	 
   	 // -- Returns --
   	 // the deflated contents of the input file as a byte array
   	
       public byte[] inflateFromFile(java.io.File file) throws java.io.IOException,
       																	  		java.io.FileNotFoundException,
       																	  		java.io.UnsupportedEncodingException,
       																	  		java.util.zip.DataFormatException
      {
         java.io.FileInputStream reader = new java.io.FileInputStream(file);
         byte[] in = new byte[(int) file.length()];
         int length = 0;
         
         length = reader.read(in, 0, in.length);
         
         byte[] output = new byte[1048576];
      	
         java.util.zip.Inflater decompresser = new java.util.zip.Inflater();
         decompresser.setInput(in, 0, in.length);
         dataLength = decompresser.inflate(output);
         decompresser.end();
         
         reader.close();
         return output;
      }
      
   	// public byte[] inflateFile(java.io.File file) throws java.io.IOException, java.io.FileNotFoundException,	java.io.UnsupportedEncodingException, java.util.zip.DataFormatException
       
   	 // Inflates a stored file
   	 
   	 // -- Variables --
   	 // file - the file to inflate
      
       public void inflateFile(java.io.File file) throws java.io.IOException,
       																	java.io.FileNotFoundException,
       																	java.io.UnsupportedEncodingException,
       																	java.util.zip.DataFormatException
      {
      	// read to contents of the file...
         java.io.FileInputStream reader = new java.io.FileInputStream(file);
         byte[] in = new byte[(int) file.length()];
         int length = 0;
         
         length = reader.read(in, 0, in.length);
      	
      	// The 32 MB temporary data buffer...
         byte[] output = new byte[33554432];
      	
      	// Inflates the data...
         java.util.zip.Inflater decompresser = new java.util.zip.Inflater();
         decompresser.setInput(in, 0, length);
         dataLength = decompresser.inflate(output);
         decompresser.end();
         
      	// Writes the data back to the file...
         java.io.FileOutputStream writer = new java.io.FileOutputStream(file);
         writer.write(output, 0, dataLength);
         reader.close();
         writer.close();
      }
   	
   	 // public byte[] deflateFile(java.io.File file) throws java.io.IOException, java.io.FileNotFoundException,	java.io.UnsupportedEncodingException, java.util.zip.DataFormatException
       
   	 // Deflates a stored file
   	 
   	 // -- Variables --
   	 // file - the file to deflate
      
       public void deflateFile(java.io.File file) throws java.io.IOException,
       																	java.io.FileNotFoundException,
       																	java.io.UnsupportedEncodingException,
       																	java.util.zip.DataFormatException
      {
      	// read to contents of the file...
         java.io.FileInputStream reader = new java.io.FileInputStream(file);
         byte[] in = new byte[(int) file.length()];
         int length = 0;
         
         length = reader.read(in, 0, in.length);
      	
      	// The 32 MB temporary data buffer...
         byte[] output = new byte[33554432];
      	
      	// Deflates the data...
         java.util.zip.Deflater compresser = new java.util.zip.Deflater();
         compresser.setInput(in);
         compresser.finish();
         dataLength = compresser.deflate(output);
         
      	// Writes the data to the file...
         java.io.FileOutputStream writer = new java.io.FileOutputStream(file);
         writer.write(output, 0, dataLength);
         reader.close();
         writer.close();
      }
   	
   	// public byte[] deflateToFile(byte[] in, java.io.File file) throws java.io.IOException, java.io.FileNotFoundException,	java.io.UnsupportedEncodingException, java.util.zip.DataFormatException
       
   	 // Deflates the input byte array and stores the deflated data to the specified file
   	 
   	 // -- Variables --
   	 // in - the input byte array
   	 // file - the output file for the deflated data
   	
       public void deflateToFile(byte[] in, java.io.File file) throws java.io.IOException,
       																	  				java.io.FileNotFoundException,
       																	  				java.io.UnsupportedEncodingException,
       																	  				java.util.zip.DataFormatException
      {
         byte[] output = new byte[1048576];
      	
         java.util.zip.Deflater compresser = new java.util.zip.Deflater();
         compresser.setInput(in);
         compresser.finish();
         dataLength = compresser.deflate(output);
         
         java.io.FileOutputStream writer = new java.io.FileOutputStream(file);
       	
         writer.write(output, 0, dataLength);
         writer.close();
      }
    
       public byte[] deflateBytes(byte[] in) throws java.io.UnsupportedEncodingException, java.util.zip.DataFormatException {
         byte[] output = new byte[1048576];
      	
         java.util.zip.Deflater compresser = new java.util.zip.Deflater();
         compresser.setInput(in);
         compresser.finish();
         dataLength = compresser.deflate(output);
         
         return output;
      }
      
       public byte[] inflateBytes(byte[] in, int bytecount) throws java.io.UnsupportedEncodingException, java.util.zip.DataFormatException {
         byte[] output = new byte[1048576];
         
         java.util.zip.Inflater decompresser = new java.util.zip.Inflater();
         decompresser.setInput(in, 0, bytecount);
         dataLength = decompresser.inflate(output);
         decompresser.end();
         
         return output;
      }
      
       public byte[] inflateBytes(byte[] in) throws java.io.UnsupportedEncodingException, java.util.zip.DataFormatException {
         byte[] output = new byte[1048576];
         
         java.util.zip.Inflater decompresser = new java.util.zip.Inflater();
         decompresser.setInput(in, 0, in.length);
         dataLength = decompresser.inflate(output);
         decompresser.end();
         
         return output;
      }
      
       public int getLength() {
         return dataLength;
      }
   }