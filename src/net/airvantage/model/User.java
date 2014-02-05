package net.airvantage.model;

public class User {

	public String uid;
	public String email;
	public Pictures picture;
	public String name;

	public static class Pictures {
		public String normal;
		public String small;
		public String icon;
	}
}
