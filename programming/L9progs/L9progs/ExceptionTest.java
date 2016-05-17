import FormatIOX.*;

public class ExceptionTest {

   public static void main (String [] args) {

      FileIn fin = new FileIn("input.txt");
      Console con = new Console();

      try {
         String line = fin.readLine();
	 con.println(line);
         line = fin.readLine();
	 con.println(line);
      }
      catch (EofX e) {
	  System.err.println("Unexpected end of file");
      }

      con.println("Done");
      fin.close();

   }
}
