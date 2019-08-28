package com.plume.monitor.service;


/**
 * @author AC
 */
public interface IMonitorService {
	

	/**
	 * HK dvr back play video download by time video back play
	 */
	public String fetchHKVideo(String ip, int port, String userName, String password, long starTime, long endTime,
			int routes, short channel);


	/**
	 * HK dvr catch picture
	 */
	public String fetchHKCatchPicture(String ip, int port, int routes, int channel, String userName, String password);

	/**
	 * DH dvr back play video download by time video back play
	 */
	public String fetchDHVideo(String ip, int port, String userName, String password, long starTime, long endTime,
			int routes, short channel);
	
	/**
	 * DH dvr catch picture
	 */
	public String fetchDHCatchPicture(String ip, int port, String userName, String password, int channel)
			throws InterruptedException;


}
