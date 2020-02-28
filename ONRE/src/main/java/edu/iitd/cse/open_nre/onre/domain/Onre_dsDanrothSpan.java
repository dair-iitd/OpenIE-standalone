package edu.iitd.cse.open_nre.onre.domain;


public class Onre_dsDanrothSpan {
	public Double value;
	public String phrase;
	public String bound;
	public String unit;
	
	public int start;
	public int end;
	
	@Override
	public String toString() {
		return this.bound + this.value + this.unit + "[" + this.phrase + "]";
	}
}
