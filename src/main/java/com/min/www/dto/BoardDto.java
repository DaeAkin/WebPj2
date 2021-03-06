package com.min.www.dto;

import java.util.Set;

public class BoardDto {
	private int id;
	private String title;
	private String writer;
	private String writetime;
	private String content;
	private int hit;
	private String IMAGEURL;

	
	public BoardDto() {
		// TODO Auto-generated constructor stub
	}
	
	public BoardDto(int id,String title, String writer,
			String writetime,String content,int hit, 
			String IMAGEURL) {
		// TODO Auto-generated constructor stub
		this.id = id ;
		this.title = title ;
		this.writer = writer ;
		this.writetime = writetime ;
		this.hit = hit ;
		this.content = content ;
		this.IMAGEURL = IMAGEURL;
	}
	
	
	
	public String getIMAGEURL() {
		return IMAGEURL;
	}
	public void setIMAGEURL(String iMAGEURL) {
		IMAGEURL = iMAGEURL;
	}
	public String getWritetime() {
		return writetime;
	}

	public void setWritetime(String writetime) {
		this.writetime = writetime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getWriter() {
		return writer;
	}
	public void setWriter(String writer) {
		this.writer = writer;
	}
	public String getWritertime() {
		return writetime;
	}
	public void setWritertime(String writertime) {
		this.writetime = writertime;
	}
	public int getHit() {
		return hit;
	}
	public void setHit(int hit) {
		this.hit = hit;
	}


	
}
