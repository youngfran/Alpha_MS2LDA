import FormatIOX.*;

public class ExceptionTest2 {

   public static void main (String [] args) {

      Console con = new Console();
      con.print("Enter name of input file: ");
      String fileName = con.readWord();

      FileIn fin = new FileIn(fileName);

      try {
         int num = fin.readInt();
	 con.println(num);
         String line = fin.readLine();
	 con.println(line);
      }
      catch (EofX e) {
	  System.err.println("Unexpected end of file");
      }
      catch (NFX e) {
	  System.err.println("Number format error");
      }

      con.println("Done");
      fin.close();

   }
}
