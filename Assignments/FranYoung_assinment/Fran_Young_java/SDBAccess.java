/*
	Simple GUI-based app to illustrate direct JDBC connection using StructureDB
	David P Leader 
	last update: 24.10.2014
 */	
import java.awt.*;
import java.sql.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.xml.*;

import com.mysql.jdbc.*;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetMetaData;

import net.proteanit.sql.DbUtils;


public class SDBAccess extends JFrame implements ActionListener, ItemListener
{	
	// GUI components that need to be instance variables 
	private JTable table;
	private JTextArea feedback;	
	private JButton bSubmit, bClear;
	private JPanel pCentre;

	private ButtonGroup Compoundgroup ;
	private JRadioButton allButton ;
	private JRadioButton lipinskiButton ;
	private JRadioButton bioButton ;
	private JRadioButton leadLikeButton;


	private ButtonGroup propertyGroup ;
	private JRadioButton allPButton ;
	private JRadioButton lpnskPButton ;
	private JRadioButton bioPButton ;
	private JRadioButton corePButton;
	private JRadioButton llButton;


	

	private String compoundSel = "All";
	private String propertySel = "Core properties";
	//private String rankSel;
	private Connection conn;
	private ResultSet rs;
	private String	query;
	private int noCompounds = 30; // should set this by querying db

	//	private JComboBox orderCombo;
	//	private final int ORDER_MAX = 3;		// Limit of orders for populating JComboBox	
	//	private int orderChoice;				// Value of bond order (for SQL query)

	//Listener listener = new Listener();		// For JComboBox

	public SDBAccess()
	{		
		this.setLayout(new BorderLayout(10,10));
		setTitle("ChemDat Database");
		setSize(800,200);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setup();
	}

	// main method is placed within the GUI class to avoid surplus starter class
	public static void main(String args[])
	{
		JFrame jf = new SDBAccess();
		jf.setVisible(true);
	}

	// set up GUI and initialize
	void setup()
	{
		/*--- Main panel for Radio Buttons ---*/		
		JPanel pNorth = new JPanel();	
		add("North", pNorth);
		JPanel compoundChoice = new JPanel();
		compoundChoice = createCompoundChoices(); 
		pNorth.add(compoundChoice);
		JPanel propertyChoice = new JPanel();
		propertyChoice = createPropertyChoices();
		pNorth.add(propertyChoice);
		
		bSubmit = new JButton("Submit");
		bSubmit.addActionListener(this);
		pNorth.add(bSubmit);		

	}	

	// event-handling method for button-press
	public void actionPerformed(ActionEvent event)
	{
		// send call on submit
		if (event.getSource() ==bSubmit)
		{					
			processSubmit();
		}	
		
	}
	// dealing with the radio buttons
	public void itemStateChanged(ItemEvent e)
	{
		
		compoundSel = getSelectedButtonText( Compoundgroup);    
		propertySel = getSelectedButtonText( propertyGroup);
		
	}

	/* method to make	Panel for the radio button group that lets you
	 *  choose which filter is applied */
	public JPanel createCompoundChoices() 
	{ 

		System.out.println("in create compound radio buttons");

		// create radio buttons
		allButton = new JRadioButton("All");
		lipinskiButton = new JRadioButton("Lipinski");
		bioButton = new JRadioButton("Bioavailable");
		leadLikeButton = new JRadioButton("LeadLike");

		// Add radio buttons into a ButtonGroup so that
		// only one button in group is on at any time
		Compoundgroup = new ButtonGroup();
		Compoundgroup.add(allButton);
		Compoundgroup.add(lipinskiButton);
		Compoundgroup.add(bioButton);
		Compoundgroup.add(leadLikeButton);
		allButton.setSelected(true); 

		allButton.addItemListener(this); 
		lipinskiButton.addItemListener(this); 
		bioButton.addItemListener(this); 
		leadLikeButton.addItemListener(this);

		JPanel panel = new JPanel(new GridLayout(4,1));



		panel.add(allButton);	    
		panel.add(lipinskiButton);
		panel.add(bioButton);
		panel.add(leadLikeButton);
		panel.setBorder 
		(new TitledBorder(new EtchedBorder(), "Compounds")); 
		return panel; 
	} 

/* method to make	Panel for the radio button grup that lets you
 *  choose which properties are displayed
 */
	public JPanel createPropertyChoices() 
	{ 

		System.out.println("in create property radio buttons");

		// create radio buttons
		allPButton = new JRadioButton("All properties");
		corePButton = new JRadioButton("Core properties");
		lpnskPButton = new JRadioButton("Lipinski properties");
		bioPButton = new JRadioButton("Bioavailable properties");
		llButton = new JRadioButton ("LeadLike properties");


		// Add radio buttons into a ButtonGroup so that
		// only one button in group is on at any time
		propertyGroup = new ButtonGroup();
		propertyGroup.add(corePButton);
		propertyGroup.add(allPButton);
		propertyGroup.add(lpnskPButton);
		propertyGroup.add(bioPButton);
		propertyGroup.add(llButton);
		corePButton.setSelected(true); 


		allPButton.addItemListener(this); 
		lpnskPButton.addItemListener(this); 
		bioPButton.addItemListener(this); 
		corePButton.addItemListener(this);
		llButton.addItemListener(this);
		//


		JPanel panel = new JPanel(new GridLayout(5,1));


		panel.add(corePButton);
		panel.add(allPButton);	    
		panel.add(lpnskPButton);
		panel.add(bioPButton);
		panel.add(llButton);

		panel.setBorder 
		(new TitledBorder(new EtchedBorder(), "Properties "
				+ "to display")); 
		return panel; 
	} 

	private static String getSelectedButtonText(ButtonGroup buttonGroup)
	/*
	 * method to find which button in a group was selected.
	 * Adapted from code on StackOverflow
	 */
	{	
		String txt  = " ";
		for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();)
		{
			AbstractButton button = buttons.nextElement();

			if (button.isSelected())
				txt= button.getText();
		}



		return txt;
	}

	/* Method to process the submit button  */
	public  boolean processSubmit()
	{
		Boolean allOK = true;
		Statement st = null;
		ResultSet rs = null;

		try
		{
			// create our mysql database connection

			Connect con = new Connect();
			Connection conn = (Connection) con.getConnection();
			QueryDict qd = new QueryDict ();
			//query = " SELECT * FROM compound ";
			
			// get the query from the query dictionary
			query = qd.getQuery(compoundSel, propertySel);
			

			st = conn.createStatement();
			st = conn.prepareStatement(query);


			// execute the query, and get a java resultset
			rs = st.executeQuery(query);	

			// Display the results
			DisplayTable(rs);
			st.close();
			rs.close(); 
			con.close();

		}
		catch (Exception e)
		{
			System.err.println("Got an exception querying database! ");
			System.err.println(e.getMessage());
			allOK = false;
		}
		return allOK;

	}
/* Method to display a resultset from a query in a jTable */
	public void DisplayTable(ResultSet rs)
	{

		Vector columnNames = new Vector();
		Vector data = new Vector();

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		f.setSize(1000,500);
		f.setLocationRelativeTo(null);
		f.setVisible(true);

		try 
		{



			rs.beforeFirst();
			java.sql.ResultSetMetaData md = rs.getMetaData();
			int columns = md.getColumnCount();
			System.out.println("count" + columns);


			//  Get column names
			for (int i = 1; i <= columns; i++) {
				columnNames.addElement(md.getColumnName(i));
			}

			//  Get row data
			while (rs.next()) {
				Vector row = new Vector(columns);

				for (int i = 1; i <= columns; i++) {
					row.addElement(rs.getObject(i));
					Object x =  (rs.getObject(i));
				}

				data.addElement(row);
			}

			rs.close();

		}
		catch (Exception e)
		{
			System.out.println(e);
		}

		//  Create table with database data
		table = new JTable(data, columnNames);
		table.setAutoCreateRowSorter (true );
		
		int n = table.getRowCount();
		String header = String.format("ChemDat  %s  Filter:  %d  "
				+ "compounds selected from %d",compoundSel, n ,noCompounds);


		f.setTitle(header);
		JScrollPane dataPane = new JScrollPane(table);
		f.add(dataPane);



		return;
	}
}


