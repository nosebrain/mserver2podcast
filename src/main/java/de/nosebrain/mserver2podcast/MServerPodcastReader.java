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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import de.nosebrain.mserver2podcast.model.Entry;
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

	public List<Video> get(final String topic) throws IOException, URISyntaxException {
		final List<Video> videos = new LinkedList<>();

		final File fileToUse = this.getLatestFile();

		final Iterator<Entry> entryIterator = new MserverIterator(fileToUse);

		while (entryIterator.hasNext()) {
      final Entry entry = entryIterator.next();
      if (entry.getTopic().equals(topic)) {
        videos.add(entry.getVideo());
      }
    }

		return videos;
	}

	private class MserverIterator implements Iterator<Entry> {
	  private static final String BEGINNING = "\"X\":[";

	  private int currentIndex = 0;
    private Entry lastEntry = new Entry();
	  private final List<String> lines = new LinkedList<>();

	  public MserverIterator(final File file) throws IOException {
      try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
        String line;
        while ((line = reader.readLine()) != null) {
          this.lines.add(line);
        }
      }

      if (this.lines.size() == 1) {
        // get the only line and split it in entries
        final String allLines = this.lines.get(0);
        this.lines.remove(0);

        final String[] split = allLines.split(Pattern.quote(BEGINNING));

        for (String line : split) {
          this.lines.add(line);
        }
      }

      this.lines.remove(0);
    }

    @Override
    public boolean hasNext() {
      return this.currentIndex < this.lines.size();
    }

    @Override
    public Entry next() {
      final String line = BEGINNING + this.lines.get(this.currentIndex++);
      final Entry entry = new Entry();

      final JSONTokener tokener = new JSONTokener("{" + line + "}");
      final JSONObject root = new JSONObject(tokener);
      final JSONArray infoArray = root.getJSONArray("X");

      final Video video = new Video();
      final String name = infoArray.getString(2);
      video.setName(name);

      final String dateString = infoArray.getString(3);
      final String timeString = infoArray.getString(4);

      if (!dateString.isEmpty() && !timeString.isEmpty()) {
        final String dateTimeString = dateString + " " + timeString;
        final LocalDateTime published = LocalDateTime.parse(dateTimeString, VIDEO_FORMATTER);
        video.setDateTime(published);
      }

      final String description = infoArray.getString(7);
      video.setDescription(description);

      final String url = infoArray.getString(8);
      try {
        final URI uri = new URI(url);
        video.setUrl(uri);
      } catch (final URISyntaxException e) {
        // ignore
      }

      final String channel = getInfo(infoArray,0, this.lastEntry.getChannel());
      final String topic = getInfo(infoArray,1, this.lastEntry.getTopic());
      entry.setChannel(channel);
      entry.setTopic(topic);
      entry.setVideo(video);

      this.lastEntry = entry;

      return entry;
    }

    private String getInfo(JSONArray infoArray, int index, String defaultValue) {
      final String info = infoArray.getString(index).trim();
      if (info.isEmpty()) {
        return defaultValue;
      }

      return info;
    }
  }

	private File getLatestFile() {
		final File rootFolder = new File(this.basePath);
		final File[] files = rootFolder.listFiles(((dir, name) -> name.endsWith(FILE_SUFFIX)));

		// get the latest file
		final SortedSet<LocalDateTime> timestamps = new TreeSet<>();
		for (final File videoFile : files) {
			final String name = videoFile.getName().replaceFirst(FILE_SUFFIX, "");

			final LocalDateTime dateTime = LocalDateTime.parse(name, FORMATTER);
			timestamps.add(dateTime);
		}

		return new File(rootFolder, FORMATTER.format(timestamps.last()) + FILE_SUFFIX);
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
}
