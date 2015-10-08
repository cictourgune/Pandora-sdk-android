package org.tourgune.pandora.sdk.utils.bus;

public class AndroidEventMessage {
	
	private Integer id;
	private Object content;
	 
	
	public AndroidEventMessage(Integer id, Object content) {
		super();
		this.id = id;
		this.content = content;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Object getContent() {
		return content;
	}
	public void setContent(Object content) {
		this.content = content;
	}
	
	

}
