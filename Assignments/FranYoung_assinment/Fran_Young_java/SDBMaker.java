/*
	Simple GUI application to parse SDF files and generate files of formatted 
	data to load into sql tables. FileParser, parses the input file and PropertyParser
	uses Chemaxon to generate attributes , eg Mass,Formula ,LogP, etc
	Adapted from code produced by David P Leader 10.09.14
*/

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.*;

import java.util.Scanner;
import java.sql.*;
import chemaxon.*;

public class SDBMaker extends JFrame implements ActionListener
{
	private JMenuItem openFile, openBatch, saveAtom, saveBond, saveCompound,saveProperty, quit; 		
	private JTextArea inputArea;
	private JScrollPane inputPane;		// so text area will scroll if required		
	
	private int width = 500;					// default app width
	private int height = 300;					// default app height
	
	AtomList aList;
	BondList bList;
	CompoundList cList;
	PropertyList pList;

	public SDBMaker()
	{ 
		setSize(width, height);
		setTitle("StructureDB Maker");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setup();
	}
	
	public static void main (String args[])
	{
		Frame f = new SDBMaker();
		f.setBackground(Color.lightGray);
		f.setVisible(true);
	}
	
	public void setup()
	{		
		// layout
		this.setLayout(new BorderLayout());
		
		//------------------- Menus ---------------------//
		JMenuBar menuBar = new JMenuBar();

		/* --- File menu --- */
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);	
		
		openFile = new JMenuItem("Open .sd File");
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));	
		openFile.addActionListener(this);
		openFile.setEnabled(true); 
		fileMenu.add(openFile);
		
		openBatch = new JMenuItem("Open Directory of .sd Files");
		openBatch.addActionListener(this);
		openBatch.setEnabled(true); 
		fileMenu.add(openBatch);
		
		fileMenu.addSeparator();

		saveAtom =  new JMenuItem("Save Atom.sql file"); 
		saveAtom.addActionListener(this);
		saveAtom.setEnabled(false);
		fileMenu.add(saveAtom);
		
		saveBond =  new JMenuItem("Save Bond.sql file"); 
		saveBond.addActionListener(this);
		saveBond.setEnabled(false);
		fileMenu.add(saveBond);
		
		saveCompound =  new JMenuItem("Save Compound.sql file"); 
		saveCompound.addActionListener(this);
		saveCompound.setEnabled(false);
		fileMenu.add(saveCompound);
		
		saveProperty =  new JMenuItem("Save Property.sql file"); 
		saveProperty.addActionListener(this);
		saveProperty.setEnabled(false);
		fileMenu.add(saveProperty);
		fileMenu.addSeparator();
		
		// Quit
		quit = new JMenuItem("Quit");
		quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileMenu.add(quit);
		quit.addActionListener(this);
		
		setJMenuBar(menuBar);
			
		/* --- Border Layout --- */
		
		// Center
		inputArea = new JTextArea(6,50);
		inputArea.setMargin(new Insets(10,10,10,10));	// White standoff for text
		inputArea.setFont(new Font("Monospaced",Font.PLAIN,12));
		inputArea.setBackground(Color.white);
		inputArea.setEditable(false);
		inputPane = new JScrollPane(inputArea);	
		this.add(inputPane,"Center");	
		
		centreInScreen();
	}
	
	// process menu items
	public void actionPerformed(ActionEvent event)
	{			
		// Open single .sd file		
		if (event.getSource() == openFile)
		{
			aList = new AtomList();
			bList = new BondList();
			cList = new CompoundList();
			pList = new PropertyList();
			LoadFile ldfi = new LoadFile(this);
			Scanner scanner = ldfi.getScanner();
			if(scanner != null)
			{
				// get original directory stuff
				String fileName = ldfi.getFileName();
				
				// send for parsing
				inputArea.append("parsing...\n");
				FileParser parser = new FileParser(scanner, aList, bList, cList);
				PropertyParser props = new PropertyParser(fileName,pList,cList);
				if(parser.isParsed())
				{
					saveAtom.setEnabled(true);
					saveBond.setEnabled(true);
					saveCompound.setEnabled(true);
					saveProperty.setEnabled(true);
					inputArea.setText("\nFile: '" + fileName + "' parsed.\n");
				}
				ldfi.closeReader();		
			}	
			else
			{
				ldfi.closeReader();
			}
		}
		
		else if (event.getSource() == openBatch)
		{
			// Get list of Files in batch folder
			JFileChooser chooser = new JFileChooser(); 
			chooser.setCurrentDirectory(new java.io.File("."));
			chooser.setDialogTitle("Open Folder");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);		// Only allow choosing directories
			chooser.setAcceptAllFileFilterUsed(false); 							// Disable the "All files" filter option  (cosmetic)
			
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) 	// If user has selected and hit 'Choose', 'OK' (etc)
			{ 
				File batchFolder = chooser.getSelectedFile(); 	// This is actually the chosen directory
				File[] fileList = batchFolder.listFiles();		// Array of File objects
				// create list objects to hold tables for DB entities
				aList = new AtomList();
				bList = new BondList();
				cList = new CompoundList();
				pList = new PropertyList();
		
				// Go through list of Files and read into Scanner and parse
				for (int i = 0; i < fileList.length; i++) 
				{
					if (fileList[i].isFile())		// ignore directories
					{
						FileParser parser = null;
						try
						{
							FileReader in = new FileReader(fileList[i]);
							Scanner scanner = new Scanner(in);
							String fileName = fileList[i].getName();
							parser = new FileParser(scanner, aList, bList, cList);
							PropertyParser props = new PropertyParser(fileName,pList,cList);
						}
						catch (IOException e)
						{
							JOptionPane.showMessageDialog(this, "Error loading file, " + fileList[i].getName() +" !", "Error", JOptionPane.ERROR_MESSAGE);
						}
						
						if(parser != null && parser.isParsed())
						{
							inputArea.append(fileList[i].getName() + " parsed.\n");
						}
						else
						{
							inputArea.append(fileList[i].getName() + " NOT parsed.\n");						
						}
					} 
				}
				inputArea.append("\n  Finished parsing.\n");
				saveAtom.setEnabled(true);
				saveBond.setEnabled(true);
				saveCompound.setEnabled(true);
				saveProperty.setEnabled(true);
			}

		}
				
		else if (event.getSource() == saveAtom)
		{
			StringBuilder builder = new StringBuilder();
			
			for (int i=0; i<aList.getSize(); i++)
			{
				if(aList.getAtom(i).getFormatted() != null)
				{
					builder.append(aList.getAtom(i).getFormatted());
					builder.append("\n");
				}
			}			
			@SuppressWarnings("unused")
			SaveFile sf = new SaveFile(this, builder.toString());
			inputArea.append("\nAtom file saved.\n");
		}

		else if (event.getSource() == saveBond)
		{
			StringBuilder builder = new StringBuilder();
			
			for (int i=0; i<bList.getSize(); i++)
			{
				if(bList.getBond(i).getFormatted() != null)
				{
					builder.append(bList.getBond(i).getFormatted());
					builder.append("\n");
				}
			}			
			@SuppressWarnings("unused")
			SaveFile sf = new SaveFile(this, builder.toString());
			inputArea.append("\nBond file saved.\n");
		}
		
		else if (event.getSource() == saveCompound)
		{
			StringBuilder builder = new StringBuilder();
			
			for (int i=0; i<cList.getSize(); i++)
			{
				if(cList.getCompound(i).getFormatted() != null)
				{
					builder.append(cList.getCompound(i).getFormatted());
					builder.append("\n");
				}
			}			
			@SuppressWarnings("unused")
			SaveFile sf = new SaveFile(this, builder.toString());
			inputArea.append("\nCompound file saved.\n");
		}
		else if (event.getSource() == saveProperty)
		{
			StringBuilder builder = new StringBuilder();
			
			for (int i=0; i<pList.getSize(); i++)
			{
				if(pList.getProperty(i).getFormatted() != null)
				{
					builder.append(pList.getProperty(i).getFormatted());
					builder.append("\n");
				}
			}			
			@SuppressWarnings("unused")
			SaveFile sf = new SaveFile(this, builder.toString());
			inputArea.append("\nProperty file saved.\n");
		}
		
		else if (event.getSource() == quit)
		{
			shutDown();
		}			 		
	}	
	
	// shuts down the app
	public void shutDown()
	{
		setVisible(false);
		dispose();
		System.exit(0);
	}
	
	// utility alert method	
	public void showAlert(String titleString, String alertString)
	{				
		JOptionPane.showMessageDialog(this, titleString, 
			alertString, JOptionPane.ERROR_MESSAGE);
	}
	
	// centres app in user's screen
	void centreInScreen()
	{
		Dimension appDim = this.getSize();
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screenDim.width - appDim.width)/2;
		int y = (screenDim.height - appDim.height)/2;
		this.setLocation(x, y);
	}
}