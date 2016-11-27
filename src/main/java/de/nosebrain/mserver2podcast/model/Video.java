package de.nosebrain.mserver2podcast.model;

import java.net.URI;
import java.time.LocalDateTime;

/**
 * @author dzo
 */
public class Video {

	private String name;

	private String description;

	private URI url;

	private LocalDateTime dateTime;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public URI getUrl() {
		return url;
	}

	public void setUrl(URI url) {
		this.url = url;
	}
}
