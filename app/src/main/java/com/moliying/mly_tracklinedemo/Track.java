package com.moliying.mly_tracklinedemo;

import java.util.ArrayList;

/**
 * 线路跟踪对象
 * @author Administrator
 *
 */
public class Track {
	private int id; //ID
	private String track_name;  //路线名称
	private String create_date;  //创建时间
	private String start_loc; //开始位置地址
	private String end_loc; //结束位置地址
	private ArrayList<TrackDetail> trackDetails;

	public ArrayList<TrackDetail> getTrackDetails() {
		return trackDetails;
	}
	public void setTrackDetails(ArrayList<TrackDetail> trackDetails) {
		this.trackDetails = trackDetails;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTrack_name() {
		return track_name;
	}
	public void setTrack_name(String track_name) {
		this.track_name = track_name;
	}
	public String getCreate_date() {
		return create_date;
	}
	public void setCreate_date(String create_date) {
		this.create_date = create_date;
	}
	public String getStart_loc() {
		return start_loc;
	}
	public void setStart_loc(String start_loc) {
		this.start_loc = start_loc;
	}
	public String getEnd_loc() {
		return end_loc;
	}
	public void setEnd_loc(String end_loc) {
		this.end_loc = end_loc;
	}
	public Track(int id, String track_name, String create_date,
				 String start_loc, String end_loc) {
		super();
		this.id = id;
		this.track_name = track_name;
		this.create_date = create_date;
		this.start_loc = start_loc;
		this.end_loc = end_loc;
	}
	public Track(String track_name, String create_date, String start_loc,
				 String end_loc) {
		super();
		this.track_name = track_name;
		this.create_date = create_date;
		this.start_loc = start_loc;
		this.end_loc = end_loc;
	}
	public Track() {
		super();
	}

}
