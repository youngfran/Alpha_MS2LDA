import chemaxon.calculations.Ring;
import chemaxon.marvin.calculations.ElementalAnalyserPlugin;
import chemaxon.marvin.calculations.HBDAPlugin;
import chemaxon.marvin.calculations.TPSAPlugin;
import chemaxon.marvin.calculations.TopologyAnalyserPlugin;
import chemaxon.marvin.calculations.logPPlugin;
import chemaxon.marvin.plugin.PluginException;
import chemaxon.struc.Molecule;

public class Property {
	/*
	Class to model properties gathered from CHEMAXON
	(More parameters may be added to DB table from other sources)
    Adapted from David leaders code fo compound class
*/
	


	private String compoundID;			// compound identifier (ZINC00...) 
	private String formula;	
	private String smiles;			// number of atom in compound (From line 4 first item)
	private double mass;
	private int numRotableB ;
	private int hbAcceptor;
	private int hbDonar;
	private double logP;
	private double area;
	private int ringCount;
	private int noAtoms;
	private int noBonds;
	private boolean Lipinski;
	private boolean BioAv;
	private boolean LeadLike;
	
	
	
	public Property(String compoundID, String f, double m,boolean Lip, boolean bAv, Boolean LL, 
										double lP,int hbA, int hbD,double a, int numRotBonds,int rc ,int noA, int noB, String s)
	{
		this.compoundID = compoundID;
		this.formula = f;
		this.smiles = s;
		this.mass = m;
		this.hbAcceptor = hbA;
		this.hbDonar = hbD;
		this.numRotableB = numRotBonds;
		this.logP = lP;
		this.area = a;
		this.ringCount = rc;
		this.Lipinski =Lip;
		this.BioAv = bAv ;
		this.LeadLike = LL;
		this.noAtoms = noA;
		this.noBonds = noB;
		
		
	}
	public void setcompoundID(String cid){compoundID = cid; return;}
	
	//----------------------- accessor methods -----------------------//
	
	
	
	// For writing out line into SQL file for upload into DB
	public String getFormatted()
	{
		
		return (String.format( "%s\t%s\t%8.2f\t%s\t%s\t%s\t%d\t%d\t%8.2f\t%8.2f\t%d\t%d\t%d\t%d\t%s",
					compoundID,formula,mass,Lipinski,BioAv, LeadLike,hbAcceptor,hbDonar,logP,area ,
										numRotableB,ringCount,noAtoms,noBonds,smiles ));
	}	
	
	
	
}





