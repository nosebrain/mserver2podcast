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
import java.util.regex.Pattern;

import de.nosebrain.mserver2podcast.model.Entry;
import de.nosebrain.mserver2podcast.model.Video;
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

	public List<Video> get(final String topic, final String filter) throws IOException {
		final List<Video> videos = new LinkedList<>();

		final File fileToUse = this.getLatestFile();
		final MServerParser parser = new MServerParser(fileToUse);

		final List<Entry> entries = parser.parse(topic, filter);

		for (final Entry entry : entries) {
		  videos.add(entry.getVideo());
    }

		return videos;
	}

  private class MServerParser {
	  private static final String BEGINNING = "\"X\":[";
	  private static final int INDEX_TOPIC = 1;
    private static final int INDEX_NAME = 2;

	  private final File file;

	  public MServerParser(final File file) {
	    this.file = file;
    }

    public List<Entry> parse(final String topic, final String filter) throws IOException {
	    final List<Entry> entries = new LinkedList<>();
	    final List<String> lines = new LinkedList<>();
      try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
        String line;
        while ((line = reader.readLine()) != null) {
          lines.add(line);
        }
      }

      if (lines.size() == 1) {
        // get the single line and split it in entries
        final String[] split = lines.get(0).split(Pattern.quote(BEGINNING));

        String currentTopic = "";
        String currentChannel = "";

        for (final String line : split) {
          final String infoStr = line.substring(1, line.length() - 2); // remove '],'
          final String[] values = infoStr.split("\",\"");
          final String lineTopic = values[INDEX_TOPIC];
          final String lineChannel = values[0];
          if (!lineTopic.isEmpty()) {
            currentTopic = lineTopic;
          }

          if (!lineChannel.isEmpty()) {
            currentChannel = lineChannel;
          }

          values[INDEX_TOPIC] = currentTopic;
          values[0] = currentChannel;

          if (matches(values, topic, filter)) {
            final String name = values[INDEX_NAME];

            final Entry entry = new Entry();

            final Video video = new Video();
            video.setName(name);

            final String dateString = values[3];
            final String timeString = values[4];

            if (!dateString.isEmpty() && !timeString.isEmpty()) {
              final String dateTimeString = dateString + " " + timeString;
              final LocalDateTime published = LocalDateTime.parse(dateTimeString, VIDEO_FORMATTER);
              video.setDateTime(published);
            }

            final String description = values[7];
            video.setDescription(description);

            final String url = values[8];
            try {
              final URI uri = new URI(url);
              video.setUrl(uri);
            } catch (final URISyntaxException e) {
              // ignore
            }

            entry.setChannel(currentChannel);
            entry.setTopic(currentTopic);
            entry.setVideo(video);
            entries.add(entry);
          }
        }
      }

      return entries;
    }

    private boolean matches(String[] values, String topic, String filter) {
	    if (!topic.equals(values[INDEX_TOPIC])) {
	      return false;
      }

      if (filter != null) {
	      if (!values[INDEX_NAME].contains(filter)) {
	        return false;
        }
      }

      return true;
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
