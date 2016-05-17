/*
	Class to model atom from .sd file (mdf format)
	(More parameters may be added to DB table from other sources)
	DPL 09.09.14
*/

public class Compound
{

	private String compoundID;			// compound identifier (ZINC00...) 
	private int numAtoms;				// number of atom in compound (From line 4 first item)
	private int numBonds;				// number of bonds in compound (From line 4 second item)
	private String batchID;
	private String code;
	
	public Compound(String compoundID, int numAtoms, int numBonds, String id, String c)
	{
		this.compoundID = compoundID;
		this.numAtoms = numAtoms;
		this.numBonds = numBonds;
		this.batchID = id;
		this.code = c;
	}
	public void setcompoundID(String cid){compoundID = cid; return;}
	
	//----------------------- accessor methods -----------------------//
	
	public String getCompoundID()
	{
		return compoundID;
	}
	
	public int getNumAtoms()
	{
		return numAtoms;
	}
	
	public int getNumBonds()
	{
		return numBonds;
	}
	
	// For writing out line into SQL file for upload into DB
	public String getFormatted()
	{
		return (compoundID + "\t"  + numAtoms + "\t" + numBonds +"\t" + code);
	}	
}
