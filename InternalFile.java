import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Base64;

public class InternalFile {
   public String name;
   public boolean isDir;
   public ArrayList<String> data;
   public boolean isEncoded;

   /**
    * Add an internal file to the system using an external file; used when copying from external files
    * @param extFile a File object of the external file
    * @param intFileName the name you would like to associate with the internal file name
    */
   public InternalFile(File extFile, String intFileName) {
      try {
         this.data = new ArrayList<>();
         FileInputStream fis = new FileInputStream(extFile);
         byte[] fileBytes = new byte[(int) extFile.length()];
         fis.read(fileBytes);
         fis.close();
         // if non-ascii character is detected within external file - encode data
         this.isEncoded = !new String(fileBytes).matches(Symbol.ASCII_CHECK_REGEX);
         encodeData(fileBytes);

         // truncate file name if it exceeds 254 characters (not 255 to allow for \n)
         if (intFileName.length() > Symbol.MAX_CHARS - 1) {
            intFileName = intFileName.substring(0, Symbol.MAX_CHARS-1);
         }
         this.name = intFileName;
         this.isDir = extFile.isDirectory();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Add an internal file to the system using the name and associated data; used for adding files
    * @param name
    * @param data
    */
   public InternalFile(String name, ArrayList<String> data, boolean isEncoded) {
      this.name = name;
      this.isDir = false;
      this.data = data;
      // first line of data within file has non-ascii characters means file should be encoded
      this.isEncoded = isEncoded;
   }


   /**
    * Add an internal file to the system using only a name; used for adding directories
    * @param dirName the name of the directory to add to the internal file system
    */
   public InternalFile(String dirName) {
      // truncate directory name if it exceeds 254 characters (not 255 to allow for \n)
      if (dirName.length() > Symbol.MAX_CHARS - 1) {
         dirName = dirName.substring(0, Symbol.MAX_CHARS -1);
      }
      this.name = dirName;
      this.isDir = true;
      this.data = null;
      this.isEncoded = false;
   }

   /**
    * Encode (or keep as is) data of a given file
    * @param fileBytes array of all bytes of a data file
    */
   public void encodeData(byte[] fileBytes) {
      String currLine = "";
      if (this.isEncoded) {
         // encode data
         String encodedData = Base64.getEncoder().encodeToString(fileBytes);
         int startIndex = 0;
         // wrap data around every 254 characters
         while (encodedData.substring(startIndex).length() > Symbol.MAX_CHARS-1) {
            this.data.add(encodedData.substring(startIndex, startIndex + Symbol.MAX_CHARS-1));
            startIndex += Symbol.MAX_CHARS-1;
         }
         this.data.add(encodedData.substring(startIndex, encodedData.length()));
      } else {
         // add each byte to current line until a newline is reached
         for (byte b : fileBytes) {
            currLine += (char) b;
            if (((char) b) == '\n') {
               this.data.add(currLine);
               currLine = "";
            }
         }
      }
   }

   /**
    * Add the data of a given internal file to the file system notes file
    */
   public void addToFileSystem() {
      Util.recursiveCheckDirs(this.name, 0);
      // print initial prefix for file ("=" for directory, "@" for file)
      if (this.isDir) {
         Util.writeToFile(Symbol.DIR);
      } else {
         Util.writeToFile(Symbol.FILE);
      }

      // print the name of the file
      Util.writeLineToFile(this.name);

      if (this.isEncoded) {
         Util.writeLineToFile(Symbol.ENCODED_SHEBANG);
      }

      // print the data of the file (if applicable: a directory will not contain any data)
      for (String s : data) {
         if (this.isEncoded) {
            Util.writeLineToFile(Symbol.DATA + s);
         } else {
            Util.writeToFile(Symbol.DATA + s);
         }
      }

   }
}
