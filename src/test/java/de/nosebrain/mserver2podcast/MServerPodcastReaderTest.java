package de.nosebrain.mserver2podcast;

import de.nosebrain.mserver2podcast.model.Video;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class MServerPodcastReaderTest {

  @Test
  public void testReader() throws IOException {
    final MServerPodcastReader reader = new MServerPodcastReader();

    reader.setBasePath("/Users/nosebrain/Desktop");

    final List<Video> videos = reader.get("frontal-21", null);

  }
}