package net.airvantage.model;

import java.util.Collection;

public class System {
	public String uid;
	public String name;
	public String commStatus;
	public Long lastCommDate;
	public String type;
	public String state;
	public String activityState;
	public Long lastSyncDate;
	public String syncStatus;
	public Gateway gateway;
	public Data data;
	public Collection<Application> applications;

	public static class Data {
		public Double rssi;
		public String rssiLevel;
		public String networkServiceType;
		public Double latitude;
		public Double longitude;
	}
	
	public static class Gateway {
		public String uid;
		public String imei;
		public String macAddress;
		public String serialNumber;
		public String type;
	}
	
	public static class Application {
		public String uid;
		public String name;
		public String revision;
		public String type;
		public String category;
	}
}
