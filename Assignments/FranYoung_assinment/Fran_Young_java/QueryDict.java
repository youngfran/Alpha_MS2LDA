import java.util.HashMap;
import java.util.Map;

import javax.swing.JRadioButton;

public class QueryDict {
	
	final static Map<String, String> QueryDict = new HashMap<String, String>();
	

	public QueryDict ()
	{
		// Dictionary to store all the strings of column for selected buttons

	// Property Choice
		
	QueryDict.put("All properties", " * ");
	QueryDict.put("Core properties", "CompoundID,Formula,Mass, Lipinski,Bioavailable,LeadLike,SMILES ");
	QueryDict.put("Lipinski properties", "CompoundID, Formula, Mass,LogP,HbAcceptor, HbDonor ");
	QueryDict.put("Bioavailable properties", "CompoundID, Formula, Mass,LogP,HbAcceptor, HbDonor,PolarSA ,RotBondsCount,RingCount ");
	QueryDict.put("LeadLike properties", "CompoundID, Formula, Mass,LogP,HbAcceptor, HbDonor, RotBondsCount ");
	
	
	//  Compound choice
	
	QueryDict.put("All", "" );
	QueryDict.put("Lipinski", "WHERE Lipinski = 'TRUE' ");
	QueryDict.put("Bioavailable", "WHERE Bioavailable = 'TRUE' ");
	QueryDict.put("LeadLike", "WHERE LeadLike = 'TRUE' ");
	

	}
	/* Method to return a query string, from the radio button choices*/
	public String getQuery (String colChoice,String rowChoice )
	{
		String q = String.format("SELECT %s FROM compound  %s ", QueryDict.get(rowChoice),
														QueryDict.get(colChoice));
		//System.out.println (" in get query " +q + colChoice + rowChoice);
		return q;
	}

}
