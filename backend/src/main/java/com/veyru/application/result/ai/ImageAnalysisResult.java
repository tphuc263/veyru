package com.veyru.application.result.ai;

import java.util.List;

public record ImageAnalysisResult(
    String imageDescription,
    String sceneType,
    String mood,
    List<String> colors,
    List<String> objects,
    List<String> suggestedTags,
    List<String> captionSuggestions) {
  public ImageAnalysisResult {
    colors = colors == null ? null : List.copyOf(colors);
    objects = objects == null ? null : List.copyOf(objects);
    suggestedTags = suggestedTags == null ? null : List.copyOf(suggestedTags);
    captionSuggestions = captionSuggestions == null ? null : List.copyOf(captionSuggestions);
  }

  public String getImageDescription() {
    return imageDescription;
  }

  public String getSceneType() {
    return sceneType;
  }

  public String getMood() {
    return mood;
  }

  public List<String> getColors() {
    return colors;
  }

  public List<String> getObjects() {
    return objects;
  }

  public List<String> getSuggestedTags() {
    return suggestedTags;
  }

  public List<String> getCaptionSuggestions() {
    return captionSuggestions;
  }
}
