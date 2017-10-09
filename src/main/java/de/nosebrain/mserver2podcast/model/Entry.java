package de.nosebrain.mserver2podcast.model;

public class Entry {
  private Video video;
  private String channel;
  private String topic;

  /**
   * @return the video
   */
  public Video getVideo() {
    return video;
  }

  /**
   * @param video the video to set
   */
  public void setVideo(Video video) {
    this.video = video;
  }

  /**
   * @return the channel
   */
  public String getChannel() {
    return channel;
  }

  /**
   * @param channel the channel to set
   */
  public void setChannel(String channel) {
    this.channel = channel;
  }

  /**
   * @return the topic
   */
  public String getTopic() {
    return topic;
  }

  /**
   * @param topic the topic to set
   */
  public void setTopic(String topic) {
    this.topic = topic;
  }
}
