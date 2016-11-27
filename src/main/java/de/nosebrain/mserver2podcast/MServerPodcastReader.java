package de.nosebrain.mserver2podcast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import de.nosebrain.mserver2podcast.model.Video;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author dzo
 */
@Component
public class MServerPodcastReader {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd__HH.mm.ss");
	private static final DateTimeFormatter VIDEO_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

	private static final String FILE_SUFFIX = "__filme.json";

	@Value("${mserver.basePath}")
	private String basePath;

	public static void main(String[] args) throws Exception {
		MServerPodcastReader re = new MServerPodcastReader();
		re.setBasePath("/Users/nosebrain/.mserver/filmlisten");
		final List<Video> videos = re.get("Frontal 21");

		for (final Video video : videos) {
			System.out.print(video.getName() + " => " + video.getUrl());
		}
	}

	public List<Video> get(final String topic) throws IOException, URISyntaxException {
		final List<Video> videos = new LinkedList<>();

		final File rootFolder = new File(this.basePath);

		final File[] files = rootFolder.listFiles(((dir, name) -> name.endsWith(FILE_SUFFIX)));

		final SortedSet<LocalDateTime> timestamps = new TreeSet<>();

		for (final File videoFile : files) {
			final String name = videoFile.getName().replaceFirst(FILE_SUFFIX, "");

			final LocalDateTime dateTime = LocalDateTime.parse(name, FORMATTER);
			timestamps.add(dateTime);
		}

		final File fileToUse = new File(rootFolder, FORMATTER.format(timestamps.last()) + FILE_SUFFIX);


		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileToUse), "UTF-8"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains(topic)) {
					final JSONTokener tokener = new JSONTokener("{" + line + "}");
					final JSONObject root = new JSONObject(tokener);
					final JSONArray infoArray = root.getJSONArray("X");

					final String jsonTopic = infoArray.getString(1).trim();
					if (!jsonTopic.equals(topic.trim())) {
						continue;
					}

					final Video video = new Video();
					final String name = infoArray.getString(2);
					video.setName(name);

					final String dateTimeString = infoArray.getString(3) + " " + infoArray.getString(4);
					final LocalDateTime published = LocalDateTime.parse(dateTimeString, VIDEO_FORMATTER);
					video.setDateTime(published);

					final String description = infoArray.getString(7);
					video.setDescription(description);

					final String url = infoArray.getString(8);
					final URI uri = new URI(url);
					video.setUrl(uri);
					videos.add(video);
				}

			}
		}


		return videos;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
}
