package de.nosebrain.mserver2podcast.server.controller;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEnclosureImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;
import de.nosebrain.mserver2podcast.MServerPodcastReader;
import de.nosebrain.mserver2podcast.model.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author dzo
 */
@Controller
public class BaseController {

	@Autowired
	private MServerPodcastReader podcastReader;

	@Value("${general.home}")
	private String projectHome;

	@RequestMapping("/{topic}")
	public void topicPodcast(final @PathVariable("topic") String topic, final HttpServletResponse response) throws IOException, URISyntaxException, FeedException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/xml");

		final List<Video> videos = this.podcastReader.get(topic);

		final SyndFeed feed = new SyndFeedImpl();
		feed.setTitle(topic);
		feed.setFeedType("rss_2.0");
		final List<SyndEntry> entries = new LinkedList<>();
		feed.setEntries(entries);
		feed.setDescription("Podcast for " + topic);
		feed.setLink(this.projectHome + topic);

		for (final Video video : videos) {
			final SyndEntry entry = new SyndEntryImpl();
			entry.setTitle(video.getName());
			final String link = video.getUrl().toString();
			entry.setLink(link);
			final SyndContent description = new SyndContentImpl();
			description.setType("text/plain");
			description.setValue(video.getDescription());
			entry.setDescription(description);
			entry.setPublishedDate(Date.from(video.getDateTime().toInstant(ZoneOffset.UTC)));
			final List<SyndEnclosure> enclosures = new LinkedList<>();

			final SyndEnclosure enclosure = new SyndEnclosureImpl();
			enclosure.setUrl(link);
			enclosure.setType("video/mp4");

			enclosures.add(enclosure);

			entry.setEnclosures(enclosures);

			entries.add(entry);

		}

		final SyndFeedOutput output = new SyndFeedOutput();
		output.output(feed, response.getWriter());
	}
}
