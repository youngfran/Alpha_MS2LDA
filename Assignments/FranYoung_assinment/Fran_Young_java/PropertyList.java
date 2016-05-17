
public class PropertyList {
	/*
	Class with array to hold a list all Properties  objects
	Adapted from DL code
	*/



	private Property  pList[];					// array of Compound objects
	private int listLen = 1000;				// array size
	private int listSize = 0;				// array occupancy
	
	public PropertyList()
	{
		pList = new Property[listLen];
	}
			
	// Method to add new Compound object to next position in list
	public void addProperty(Property p)
	{
		// expand array if necessary
		if(listSize >= listLen)
		{
			listLen = 2 * listLen;
			Property[] newList = new Property[listLen];
			System.arraycopy (pList, 0, newList, 0, pList.length);
			pList = newList;
		}
		// add new Compound object in next position
		pList[listSize] = p;
		listSize++;
	}
	
	// returns number of Compounds in list
	public int getSize()
	{
		return listSize;
	}
	
	// returns Compound at particular position in list numbered from 0
	public Property getProperty(int pos)
	{
		return pList[pos];
	}



}
