package net.airvantage.model;

public class Alert {

	public String target;
	public long date;
	public String eventType;
	public String uid;
	public long acknowledgedAt;
	public String acknowledgedBy;
	public Rule rule;
	public int nbOccurrence;
	public long lastDate;

	public static class Rule {
		public String name;
		public String message;
		public String uid;
	}
}
