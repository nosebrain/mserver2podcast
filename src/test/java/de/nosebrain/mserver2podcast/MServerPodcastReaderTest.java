package de.nosebrain.mserver2podcast;

import de.nosebrain.mserver2podcast.model.Video;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class MServerPodcastReaderTest {

  @Test
  @Ignore
  public void testReader() throws IOException {
    final MServerPodcastReader reader = new MServerPodcastReader();
    reader.setFilePath("/Users/nosebrain/Desktop/filmliste-act");

    final List<Video> videos = reader.get("frontal-21", null, null, null, null);

    videos.get(0);
  }
}