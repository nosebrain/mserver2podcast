package de.nosebrain.mserver2podcast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;
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

	private static final DateTimeFormatter VIDEO_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

  @Value("${mserver.filePath}")
	private String filePath;

	public List<Video> get(final String topic, final String filter, final String channel, final LocalTime startTime, final Integer minLength) throws IOException {
		final File fileToUse = new File(this.filePath);
		final MServerParser parser = new MServerParser(fileToUse);

		final List<Entry> entries = parser.parse(topic, filter, channel, startTime, minLength);

    final List<Video> videos = new LinkedList<>();
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

    public List<Entry> parse(final String topic, final String filter, final String channel, final LocalTime startTime, final Integer minLength) throws IOException {
	    final List<Entry> entries = new LinkedList<>();
	    final List<String> lines = new LinkedList<>();
      try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
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

          if (channel != null && !currentChannel.equals(channel)) {
            continue;
          }

          values[INDEX_TOPIC] = currentTopic;
          values[0] = currentChannel;

          final String startTimeStr = values[4];
          if (!startTimeStr.isEmpty()) {
            try {
              final LocalTime indexStartTime = LocalTime.parse(startTimeStr);

              if (startTime != null && !startTime.equals(indexStartTime)) {
                continue;
              }
            } catch (final DateTimeParseException e) {
              // ignore
            }
          }

          final String lengthStr = values[5];
          if (minLength != null && !lengthStr.isEmpty()) {
            final String[] lengthValues = lengthStr.split(":");
            final int hours = Integer.parseInt(lengthValues[0]);
            final int minutes = Integer.parseInt(lengthValues[1]);

            final int runtimeInMinutes = 60 * hours + minutes;
            if (runtimeInMinutes < minLength) {
              continue;
            }
          }

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

  /**
   * @param filePath the filePath to set
   */
  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }
}
