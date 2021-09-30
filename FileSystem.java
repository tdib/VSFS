import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileSystem {
   public static File fs;
   private static PrintWriter out = null;
   private static Scanner sc = null;
   public static ArrayList<InternalFile> allFiles;

   /**
    * Initialises file system by loading given .notes file from file name
    *
    * @param fileName name of file in which to load the file system from
    */
   public static void initialiseFS(String fileName) {
      allFiles = new ArrayList<>();

      try {
         // creates file for filesystem whether you type the extension or not
         File fileSystem = null;
         if (fileName.endsWith(".notes")) {
            fileSystem = new File(fileName);
         } else {
            fileSystem = new File(fileName + ".notes");
         }

         // set up scanner for given file system
         sc = new Scanner(fileSystem);
         // ensure first line is the correct format (otherwise terminate)
         if (!sc.nextLine().equals("NOTES V1.0")) {
            throw new FileSystemException("File system format is incorrect. It should begin with \"NOTES V1.0\". Terminating program.");
         }

         // iterate the file system and convert all data to java objects for easy access
         String nextLine = sc.hasNextLine() ? sc.nextLine() : "";
         do {
            // read a single file
            if (nextLine.startsWith(Symbol.FILE)) {
               // title of the file
               String currFileName = nextLine.substring(1);
               ArrayList<String> currFileData = new ArrayList<>();

               // iterate through data of current file
               while (sc.hasNextLine()) {
                  nextLine = sc.nextLine();
                  // ensure all lines are data
                  if (nextLine.startsWith(Symbol.DATA)) {
                     currFileData.add(nextLine);
                  // once a line other than data is reached, exit the loop
                  } else {
                     break;
                  }
               }
               // add the file to allFiles to keep track of it
               allFiles.add(new InternalFile(currFileName, currFileData));
            // read a directory
            } else if (nextLine.startsWith(Symbol.DIR)) {
               // ensure the directory ends with "/"
               if (nextLine.endsWith("/")) {
                  String currDirName = nextLine.substring(1);
                  allFiles.add(new InternalFile(currDirName));
               // if the directory does not end with a "/", consider it incorrectly formatted
               } else {
                  System.out.println("This directory is not correctly formatted. (must end with a \"/\")");
               }
               nextLine = sc.nextLine();
            // handle comments/ignored lines (beginning with "#")
            } else if (nextLine.startsWith(Symbol.IGNORE)) {
               System.out.println("A line was ignored by the compiler");
               nextLine = sc.nextLine();
            // handle extraneous values
            } else {
               System.out.println("An unknown file type was found by the compiler.");
               nextLine = sc.nextLine();
            }
         } while (sc.hasNextLine());

//         System.out.println("OUTSIDE WHILE LOOP");
//         allFiles.forEach((file) -> {
//            System.out.println("PRINTING NEW FILE ---------------");
//            System.out.println(file.name);
//            System.out.println(file.isDir);
//            System.out.println(file.data);
//            System.out.println("---------------------------------");
//         });

         // prepare file system for being written/appended to
         out = new PrintWriter(new BufferedWriter(new FileWriter(fileSystem.getName(), true)));
         fs = fileSystem;
      } catch (FileSystemException e) {
         e.printStackTrace();
         System.err.println(e.getMessage());
      } catch (Exception e) {
         e.printStackTrace();
         System.err.println(e.getMessage());
      }
   }

   /**
    * Check the existence of a given internal file based on the name of it
    * @param fileName name of the file to check
    * @return true if the file exists within the internal file system, false otherwise
    */
   public static boolean fileExists(String fileName) {
      for (InternalFile file : allFiles) {
         if (file.name.equals(fileName)) {
            return true;
         }
      }
      return false;
   }

   /**
    * Clean up the file system variables by closing the PrintWriter and Scanner
    */
   public static void closeFS() {
      try {
         out.close();
         sc.close();
      } catch (Exception e) {

      }
   }

   /**
    * Append to the file system notes file with no newline
    * @param text text to be appended to the file system
    */
   public static void writeToFile(String text) {
      out.print(text);
   }

   /**
    * Append to the file system notes file with a newline
    * @param text text to be appended to the file system
    */
   public static void writeLineToFile(String text) {
      out.println(text);
   }

   public static void delFromFile() {

   }

}