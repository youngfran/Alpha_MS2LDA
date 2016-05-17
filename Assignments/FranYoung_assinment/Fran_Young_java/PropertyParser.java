
import java.io.*;

	import chemaxon.calculations.*;
	import chemaxon.formats.*;
	import chemaxon.marvin.calculations.*;
	import chemaxon.marvin.plugin.PluginException;
	import chemaxon.struc.Molecule;
public class PropertyParser 
{
	private PropertyList pList;
	private String compoundID;			// compound identifier (ZINC00...) 
	private String formula;	
	private String smiles;			// number of atom in compound (From line 4 first item)
	private double mass;
	private int rotB ;
	private int hbAcceptor;
	private int hbDonar;
	private int ringCount;
	private double logP;
	private double area;
	private boolean Lipinski;
	private boolean BioAv;
	private boolean LeadLike;
	//formula,smiles,mass,numRotB, hbAcceptor,hbDonar,lopgP
	
		public PropertyParser(String fName, PropertyList pList, CompoundList cList)
		{
			final int  NOCOMPOUNDS = 30;
			
			Molecule[] mols = new Molecule[30];
			byte[] d3 = new byte[100000];

			// read input molecule
			try 
			{
			MolImporter mi = new MolImporter(fName);
			
			

				for(int i = 0; i < NOCOMPOUNDS; i++)
				{


					Molecule mol = mi.read();
					mols[i] = mol;
					

					System.out.println (" MOLECULE " + (1+i));



					// mass
					mass = getMass(mols[i]);
					System.out.println("mass" + mass); 
					// formula
					formula = getFormula(mols[i]);

					logP = getLogP (mols[i]);
					hbAcceptor = getHBAcceptor( mols[i]);
					hbDonar = getHBDonar( mols[i]);
					
					area =  getSurfaceArea(mols[i]);
					rotB = getRotatableBoundCount( mol);

					ringCount = getNoRings(mols[i]);
					System.out.println(" ring count 1  " + ringCount);
					//int rc = getRingCount(mols[i]);
					//System.out.println(" ring count 1  " + rc);
					
					smiles = MolExporter.exportToFormat(mols[i], "smiles");

					System.out.println(smiles);
					//d3 = (byte[])MolExporter.exportToObject(mols[i], "png:w100,h100,b32,#00ffff00");
					
					Lipinski = isLipinski();
					BioAv = isBioAv();
					LeadLike = isLeadLike();
					
					compoundID = cList.getCompoundID(i);
					int noBonds = cList.getNumBonds(i);
					int noAtoms = cList.getNumAtoms(i);
			
					//String compoundID, String f,String s, double m,double lP,int hbA, int hbD, int numRotBonds )
					Property p = new Property(compoundID,formula,mass,Lipinski,BioAv, LeadLike,logP, hbAcceptor,
								hbDonar,area,rotB,ringCount ,noAtoms,noBonds,smiles);
					pList.addProperty(p);
				}
				mi.close();
				
			}
			
			
			catch(IOException e)
			{ 
				System.out.println("exception " + e); 
			}




		}

			private static double getLogP (Molecule mol)
			// method to get logP for molecule
			{


				// create plugin
				logPPlugin plugin = new logPPlugin();



				// set parameters
				plugin.setCloridIonConcentration(0.2);
				plugin.setNaKIonConcentration(0.2);
				try
				{
					// set result types
					plugin.setUserTypes("logPTrue,logPMicro");
					// set the input molecule
					plugin.setMolecule(mol);
					// run the calculation
					plugin.run();
				}
				catch(PluginException e)
				{ 
					System.out.println("exception " + e); 
				}

				// get the overall logP value
				double logp = plugin.getlogPTrue(); 

				return logp;
			}

			private static  int getHBAcceptor(Molecule mol)
			{
				// create plugin
				HBDAPlugin plugin = new HBDAPlugin();

				plugin.setpH(7.4);
				int molecularAcceptorAtomCount =0;

				try 
				{
					// set target molecule
					plugin.setMolecule(mol);
					// run the calculation
					plugin.run();

				}
				catch(PluginException e)
				{ 
					System.out.println("exception " + e); 
				}
				// without multiplicity
				molecularAcceptorAtomCount = plugin.getAcceptorAtomCount();


				return  molecularAcceptorAtomCount;
			}


			private static  int getNoRings(Molecule mol)
			{

				Ring r = new Ring();
				// set target molecule
				r.setMolecule(mol);
				// run the calculation
				int count = r.fusedAromaticRingCount();		
				return count;
			}
			
			private static  int getHBDonar(Molecule mol)
			{
				// create plugin
				HBDAPlugin plugin = new HBDAPlugin();

				plugin.setpH(7.4);

				try 
				{
					// set target molecule
					plugin.setMolecule(mol);

					// run the calculation
					plugin.run();
				}
				catch(PluginException e)
				{ 
					System.out.println("exception " + e); 
				}

				// without multiplicity
				int molecularDonorAtomCount = plugin.getDonorAtomCount();
				return  molecularDonorAtomCount;
			}

			private static  double getSurfaceArea(Molecule mol)
			{
				// create plugin
				TPSAPlugin plugin = new TPSAPlugin();

				// optional: take major microspecies at pH=7.4
				plugin.setpH(7.4);
				try
				{
					// set target molecule
					plugin.setMolecule(mol);

					// run the calculation
					plugin.run();
				}
				catch(PluginException e)
				{ 
					System.out.println("exception " + e); 
				}
				// get result
				double area = plugin.getSurfaceArea();

				
				return  area;
			}

			private static double getMass(Molecule mol)
			{
				// create plugin
				ElementalAnalyserPlugin plugin = new ElementalAnalyserPlugin();

				try
				{
					// set target molecule
					plugin.setMolecule(mol);

					// run the calculation
					plugin.run();
				}
				catch(PluginException e)
				{ 
					System.out.println("exception " + e); 
				}

				// get results
		//		double exactMass = plugin.getExactMass();
				double mass = plugin.getMass();
				return mass;
			}

			private static String getFormula(Molecule mol)
			{
				// create plugin
				ElementalAnalyserPlugin plugin = new ElementalAnalyserPlugin();

				try
				{
					// set target molecule
					plugin.setMolecule(mol);

					// run the calculation
					plugin.run();
				}
				catch(PluginException e)
				{ 
					System.out.println("exception " + e); 
				}

				// get results
				String formula = plugin.getFormula();
				return formula;
			}


			private static int  getRingCount(Molecule mol)
			{
				// create plugin
				TopologyAnalyserPlugin plugin = new TopologyAnalyserPlugin();
				try
				{
					// set target molecule
					plugin.setMolecule(mol);

					// run the calculation
					plugin.run();
				}
				catch(PluginException e)
				{ 
					System.out.println("exception " + e); 
				}

				// get molecular results
				int ringCount = plugin.getRingCount();
				System.out.println( " ring count" + ringCount);
				int rotatableBondCount = plugin.getRotatableBondCount();
				int smallestRingSize = plugin.getSmallestRingSize();
				return ringCount;
			}
			
			private static int  getRotatableBoundCount(Molecule mol)
			{
				// create plugin
				TopologyAnalyserPlugin plugin = new TopologyAnalyserPlugin();
				try
				{
					// set target molecule
					plugin.setMolecule(mol);

					// run the calculation
					plugin.run();
				}
				catch(PluginException e)
				{ 
					System.out.println("exception " + e); 
				}

				// get molecular results
				
				int rotatableBondCount = plugin.getRotatableBondCount();
				//int smallestRingSize = plugin.getSmallestRingSize();
				return rotatableBondCount;
			}
			
			private  boolean  isLipinski()
			{
				boolean l = false ;
				if( (mass < 500) && ( logP < 5) && ( hbAcceptor < 10) && (hbDonar < 5))
					l = true;
						
				return l;
			}		
			
			private  boolean  isBioAv()
			{
				boolean b = false ;
				int count = 0;
				if(mass < 500)
					count ++;
				if (logP < 5) 
					count ++;
				if ( hbAcceptor <= 10)
					count ++;
				if(hbDonar < 5);
					count ++;
				if (rotB < 10)
					count ++;
				if (area < 200)
					count++;
				if (ringCount < 5)
				
				if 	(count >= 6)
					b = true;
				
					
				return b;
			}	
			
			private  boolean  isLeadLike()
			{
				boolean ll = false ;
				if( (mass < 300) && ( logP < 3) && ( hbAcceptor <= 3) && (hbDonar < 3) &&  (rotB < 3))
					ll = true;
						
				return ll;
			}	
		
}





