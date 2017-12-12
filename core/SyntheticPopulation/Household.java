package core.SyntheticPopulation;

import java.util.ArrayList;

import core.HardcodedData.AggreHholdTypes;
import core.HardcodedData.HholdRelSP;
import core.HardcodedData.HholdTypes;

public class Household {
	private int id;
	private HholdTypes hhType;
	private AggreHholdTypes aggreHhType;
	private ArrayList<Integer> residentsID;
	private String zoneName;
	private String zoneDescription;
	private int nBedroomsNeeded;
	
	public Household(int newId, HholdTypes newHhType, ArrayList<Integer> newResidents, String newZone, String newZoneDescription) {
		this.setId(newId);
		this.setHhType(newHhType);
		this.setResidentsID(newResidents);
		this.setZoneName(newZone);
		this.setZoneDescription(newZoneDescription);
		this.assignAggreHholdType();
	}
	
	public Household(int newId, HholdTypes newHhType, ArrayList<Integer> newResidents, String newZone, String newZoneDescription, AggreHholdTypes newAggreHhType) {
		this.setId(newId);
		this.setHhType(newHhType);
		this.setResidentsID(newResidents);
		this.setZoneName(newZone);
		this.setZoneDescription(newZoneDescription);
		this.setAggreHhType(newAggreHhType);
	}
	
	
	/**
	 * assigns initial AggreHholdType to households output from SP construction.
	 * The method assumes that the composition of relationships in a household is correct (e.g. a household should not have a Married individual and a LoneParent individual).
	 * It therefore doesn't check and correct this composition.
	 */
	public void assignAggreHholdType() {
		if (this.getResidentsID()==null || this.getResidentsID().size()==0) return;
		
		ArrayList<Integer> marriedID = this.getIndivIDOfHHRel(HholdRelSP.Married);
		ArrayList<Integer> loneParentID = this.getIndivIDOfHHRel(HholdRelSP.LoneParent);
		ArrayList<Integer> u15ChildID = this.getIndivIDOfHHRel(HholdRelSP.U15Child);
		ArrayList<Integer> o15ChildStudentID = this.getIndivIDOfHHRel(HholdRelSP.Student);
		o15ChildStudentID = this.getIndivIDOfHHRel(HholdRelSP.O15Child, o15ChildStudentID);
		
		if (marriedID.size()==2) {
			if (u15ChildID.size()>0) {
				if (o15ChildStudentID.size()>0) {
					this.setAggreHhType(AggreHholdTypes.coupleU15O15);
				} else {
					this.setAggreHhType(AggreHholdTypes.coupleU15);
				}
			} else {
				if (o15ChildStudentID.size()>0) {
					this.setAggreHhType(AggreHholdTypes.coupleO15);
				} else {
					this.setAggreHhType(AggreHholdTypes.couple);
				}
			}
		} else {
			if (loneParentID.size()==1) {
				if (u15ChildID.size()>0) {
					if (o15ChildStudentID.size()>0) {
						this.setAggreHhType(AggreHholdTypes.loneParentU15O15);
					} else {
						this.setAggreHhType(AggreHholdTypes.loneParentU15);
					}
				} else {
					if (o15ChildStudentID.size()>0) {
						this.setAggreHhType(AggreHholdTypes.loneParentO15);
					} else {
						this.setAggreHhType(AggreHholdTypes.Other);
					}
				}
			} else {
				this.setAggreHhType(AggreHholdTypes.Other);
			}
		}
	}
	
	
	/**
	 * 
	 */
	public void correctRelationshipInHhold() {
		if (this.getResidentsID()==null || this.getResidentsID().size()==0) return;
		
		if (this.getResidentsID().size()==1) {
			Population.getIndivPool().get(this.getResidentsID().get(0)).setHhRel(HholdRelSP.LonePerson);
			return;
		}
		
		ArrayList<Integer> marriedID = this.getIndivIDOfHHRel(HholdRelSP.Married);
		ArrayList<Integer> loneParentID = this.getIndivIDOfHHRel(HholdRelSP.LoneParent);
		ArrayList<Integer> u15ChildID = this.getIndivIDOfHHRel(HholdRelSP.U15Child);
		ArrayList<Integer> o15ChildStudentID = this.getIndivIDOfHHRel(HholdRelSP.Student);
		o15ChildStudentID = this.getIndivIDOfHHRel(HholdRelSP.O15Child, o15ChildStudentID);
		
		ArrayList<Integer> relativeID = this.getIndivIDOfHHRel(HholdRelSP.Relative);
		
		ArrayList<Integer> nfPeople = this.getIndivIDOfHHRel(HholdRelSP.LonePerson);
		nfPeople = this.getIndivIDOfHHRel(HholdRelSP.GroupHhold,nfPeople);
		
		
		if (marriedID.size()>=2) {
			// randomly changes (married.size()-2) individuals to Relative;
			for (int i=2; i<=marriedID.size()-1; i++) {
				Population.getIndivPool().get(marriedID.get(i)).setHhRel(HholdRelSP.Relative);
			}
			// change any individual in loneParentID to Relative;
			for (Integer id : loneParentID) {
				Population.getIndivPool().get(id).setHhRel(HholdRelSP.Relative);
			}
			// change any individual in nfPeople to Relative;
			for (Integer id : nfPeople) {
				Population.getIndivPool().get(id).setHhRel(HholdRelSP.Relative);
			}
		} else if (marriedID.size()==1) {
			if (loneParentID.size()>=1) {// if there is at least 1 LoneParent
				// changes the married individual to Relative
				Population.getIndivPool().get(marriedID.get(0)).setHhRel(HholdRelSP.Relative);
				// changes all loneparent (except the 1st one) to Relative
				for (int i=1; i<=loneParentID.size()-1; i++) {
					Population.getIndivPool().get(loneParentID.get(i)).setHhRel(HholdRelSP.Relative);
				}
				// changes all nfPeople (if any) to Relative
				for (Integer id : nfPeople) {
					Population.getIndivPool().get(id).setHhRel(HholdRelSP.Relative);
				}
				if (u15ChildID.size()==0 && o15ChildStudentID.size()==0) { // there is no children in this household
					// changes the remaining LoneParent to Relative;
					Population.getIndivPool().get(loneParentID.get(0)).setHhRel(HholdRelSP.Relative);
				}
			} else { // there is no lone parent
				if (u15ChildID.size()>0 || o15ChildStudentID.size()>0) { // if there's at least 1 child
					// changes married individual to LoneParent
					Population.getIndivPool().get(marriedID.get(0)).setHhRel(HholdRelSP.LoneParent);
					// changes all nfPeople (if any) to Relative
					for (Integer id : nfPeople) {
						Population.getIndivPool().get(id).setHhRel(HholdRelSP.Relative);
					}
				} else { // no children and there are more than 1 person in the household
					// converts all of them to Relative
					for (Integer residentID : this.getResidentsID()) {
						Population.getIndivPool().get(residentID).setHhRel(HholdRelSP.Relative);
					}
				}
			}
		} else { // no married individuals in this household
			if (loneParentID.size()>=1) { // if there is at least 1 LoneParent
				if (u15ChildID.size()>0 || o15ChildStudentID.size()>0) { // if there's at least 1 child
					// changes all loneparent (except the 1st one) to Relative
					for (int i=1; i<=loneParentID.size()-1; i++) {
						Population.getIndivPool().get(loneParentID.get(i)).setHhRel(HholdRelSP.Relative);
					}
					// changes all nfPeople (if any) to Relative
					for (Integer id : nfPeople) {
						Population.getIndivPool().get(id).setHhRel(HholdRelSP.Relative);
					}
				} else { //there are no children in this household and there are more than 1 person in this household
					// converts all of them to Relative
					for (Integer residentID : this.getResidentsID()) {
						Population.getIndivPool().get(residentID).setHhRel(HholdRelSP.Relative);
					}
				}
			} else { // no LoneParent in this household
				if (u15ChildID.size()>0 || o15ChildStudentID.size()>0 || relativeID.size()>0) { // if there's at least 1 child or 1 relative
					// converts all residents to Relative, including any non-family members
					for (Integer residentID : this.getResidentsID()) {
						Population.getIndivPool().get(residentID).setHhRel(HholdRelSP.Relative);
					}
				} else { // there are no family members in this household, only group household member
					// assigns GrHholdMember to residents 
					for (Integer residentID : this.getResidentsID()) {
						Population.getIndivPool().get(residentID).setHhRel(HholdRelSP.GroupHhold);
					}
				}
			}
		}
	}
	
	
	public ArrayList<Integer> getIndivIDOfHHRel(HholdRelSP hhRel) {
		ArrayList<Integer> indivIDList = new ArrayList<Integer>();
		for (Integer indivID : this.getResidentsID()) {
			if (Population.getIndivPool().get(indivID).getHhRel().equals(hhRel)) {
				indivIDList.add(indivID);
			}
		}
		return indivIDList;
	}
	
	
	public ArrayList<Integer> getIndivIDOfHHRel(HholdRelSP hhRel, ArrayList<Integer> existingList) {
		if (existingList==null) {
			existingList = new ArrayList<Integer>();
		}
		for (Integer indivID : this.getResidentsID()) {
			if (Population.getIndivPool().get(indivID).getHhRel().equals(hhRel)) {
				existingList.add(indivID);
			}
		}
		return existingList;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public HholdTypes compute17HholdTypesy() {

		HholdTypes resultingHholdType = null;
		
		/* Initial household category */
		int nParents = this.getIndivIDOfHHRel(HholdRelSP.Married,null).size() + this.getIndivIDOfHHRel(HholdRelSP.LoneParent,null).size();
		int nChildU15 = this.getIndivIDOfHHRel(HholdRelSP.U15Child,null).size();
		int nStudents = this.getIndivIDOfHHRel(HholdRelSP.Student,null).size();
		int nChildO15 = this.getIndivIDOfHHRel(HholdRelSP.O15Child,null).size();
		int nRelative = this.getIndivIDOfHHRel(HholdRelSP.Relative,null).size();
		int nTotChildren = nChildU15 + nStudents + nChildO15;// getNTotChildren();
		int nNonFamilyMem = this.getIndivIDOfHHRel(HholdRelSP.LonePerson,null).size() + this.getIndivIDOfHHRel(HholdRelSP.GroupHhold,null).size();
		int nResidents = nParents + nTotChildren + nRelative + nNonFamilyMem;

		if (nResidents == 0) {
			return null;
        }

		// if (nParents>2) { // this should not happen but if it does, keep 2
		// parents only and change other parents into Relatives.
		// int tmpnParents = 0;
		// for (Individual indiv : this.getResidents())
		// if (indiv.getHouseholdRelationship()==HouseholdRelationship.Married
		// ||
		// indiv.getHouseholdRelationship()==HouseholdRelationship.DeFacto) {
		// if (tmpnParents<2) tmpnParents += 1;
		// else indiv.setHouseholdRelationship(HouseholdRelationship.Relative);
		// }
		// tfHdlr.writeToText("testCalHholdCat.csv",
		// "Hhold has more than 2 parents!!!", true);
		// // recalculates the number of parents in this household
		// nParents = getNMarried() + getNDefacto();
		// }

		if (nResidents == 1) {
			resultingHholdType = HholdTypes.NF;
			Population.getIndivPool().get(this.getResidentsID().get(0)).setHhRel(HholdRelSP.LonePerson);
		} else {
			switch (nParents) {
			case 2:
				if (nTotChildren == 0) {
					// sets category to HF1
					resultingHholdType = HholdTypes.HF1;
				} else {
					if (nChildU15 > 0 && nStudents == 0 && nChildO15 == 0) {
						// sets category to HF5
						resultingHholdType = HholdTypes.HF5;
					}
					if (nChildU15 > 0 && nStudents > 0 && nChildO15 == 0) {
						// sets category to HF3
						resultingHholdType = HholdTypes.HF3;
					}
					if (nChildU15 > 0 && nStudents == 0 && nChildO15 > 0) {
						// sets category to HF4
						resultingHholdType = HholdTypes.HF4;
					}
					if (nChildU15 > 0 && nStudents > 0 && nChildO15 > 0) {
						// sets category to HF2
						resultingHholdType = HholdTypes.HF2;
					}
					if (nChildU15 == 0 && nStudents > 0 && nChildO15 == 0) {
						// sets category to HF7
						resultingHholdType = HholdTypes.HF7;
					}
					if (nChildU15 == 0 && nStudents == 0 && nChildO15 > 0) {
						// sets category to HF8
						resultingHholdType = HholdTypes.HF8;
					}
					if (nChildU15 == 0 && nStudents > 0 && nChildO15 > 0) {
						// sets category to HF6
						resultingHholdType = HholdTypes.HF6;
					}
				}
				break;
			case 1:
				if (nTotChildren == 0) {
					// assigns HF16 as category of this household
					resultingHholdType = HholdTypes.HF16;
					// // assigns Relatives as household relationship to all residents in this household
					for (Integer indivID : this.getResidentsID()) {
						Individual indiv = Population.getIndivPool().get(indivID);
						indiv.setHhRel(HholdRelSP.Relative);
                    }
				} else {
					if (nChildU15 > 0 && nStudents == 0 && nChildO15 == 0) {
						// sets category to HF12
						resultingHholdType = HholdTypes.HF12;
					}
					if (nChildU15 > 0 && nStudents > 0 && nChildO15 == 0) {
						// sets category to HF10
						resultingHholdType = HholdTypes.HF10;
					}
					if (nChildU15 > 0 && nStudents == 0 && nChildO15 > 0) {
						// sets category to HF11
						resultingHholdType = HholdTypes.HF11;
					}
					if (nChildU15 > 0 && nStudents > 0 && nChildO15 > 0) {
						// sets category to HF9
						resultingHholdType = HholdTypes.HF9;
					}
					if (nChildU15 == 0 && nStudents > 0 && nChildO15 == 0) {
						// sets category to HF14
						resultingHholdType = HholdTypes.HF14;
					}
					if (nChildU15 == 0 && nStudents == 0 && nChildO15 > 0) {
						// sets category to HF15
						resultingHholdType = HholdTypes.HF15;
					}
					if (nChildU15 == 0 && nStudents > 0 && nChildO15 > 0) {
						// sets category to HF13
						resultingHholdType = HholdTypes.HF13;
					}
				}
				break;
			case 0:
				if (nRelative == 0 && nTotChildren == 0) {
					// assigns cateogry to NF
					resultingHholdType = HholdTypes.NF;
					// // assigns groupHhold as household relationship to all residents in this household
					for (Integer indivID : this.getResidentsID()) {
						Individual indiv = Population.getIndivPool().get(indivID);
						indiv.setHhRel(HholdRelSP.GroupHhold);
                    }
				} else {
					// if (nRelative>0 || nTotChildren>0) {
					// assigns HF16 as category of this household
					resultingHholdType = HholdTypes.HF16;
					// // assigns Relatives as household relationship to all residents in this household
					for (Integer indivID : this.getResidentsID()) {
						Individual indiv = Population.getIndivPool().get(indivID);
						indiv.setHhRel(HholdRelSP.Relative);
                    }
				}
				break;
			}
		}

		this.setHhType(resultingHholdType);

		return resultingHholdType;
	}
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public HholdTypes getHhType() {
		return hhType;
	}

	public void setHhType(HholdTypes hhType) {
		this.hhType = hhType;
	}

	public ArrayList<Integer> getResidentsID() {
		return residentsID;
	}

	public void setResidentsID(ArrayList<Integer> residentsID) {
		this.residentsID = residentsID;
	}

	public String getZoneName() {
		return zoneName;
	}

	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}
	
	public String getZoneDescription() {
		return zoneDescription;
	}

	public void setZoneDescription(String zoneDescription) {
		this.zoneDescription = zoneDescription;
	}

	public AggreHholdTypes getAggreHhType() {
		return aggreHhType;
	}

	public void setAggreHhType(AggreHholdTypes aggreHhType) {
		this.aggreHhType = aggreHhType;
	}
	
	public int getnBedroomsNeeded() {
		return nBedroomsNeeded;
	}

	public void setnBedroomsNeeded(int nBedroomsNeeded) {
		this.nBedroomsNeeded = nBedroomsNeeded;
	}
}
