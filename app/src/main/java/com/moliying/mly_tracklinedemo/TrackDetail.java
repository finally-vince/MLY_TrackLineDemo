package com.moliying.mly_tracklinedemo;

/**
 * 每条线路的明细(详情)
 */
public class TrackDetail {

	private int id;  //id
	private double lat; //纬度
	private double lng;  //经度
	private Track track;  //当前坐标点所属的线程
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public Track getTrack() {
		return track;
	}
	public void setTrack(Track track) {
		this.track = track;
	}
	public TrackDetail(int id, double lat, double lng) {
		super();
		this.id = id;
		this.lat = lat;
		this.lng = lng;
	}
	public TrackDetail(double lat, double lng) {
		super();
		this.lat = lat;
		this.lng = lng;
	}
	public TrackDetail() {
		super();
	}

}
