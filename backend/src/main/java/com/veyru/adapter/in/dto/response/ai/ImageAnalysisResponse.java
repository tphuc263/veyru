package com.veyru.adapter.in.dto.response.ai;

import com.veyru.application.result.ai.ImageAnalysisResult;
import java.util.List;

public record ImageAnalysisResponse(
    String imageDescription,
    String sceneType,
    String mood,
    List<String> colors,
    List<String> objects,
    List<String> suggestedTags,
    List<String> captionSuggestions) {
  public static ImageAnalysisResponse from(ImageAnalysisResult value) {
    return new ImageAnalysisResponse(
        value.getImageDescription(),
        value.getSceneType(),
        value.getMood(),
        value.getColors(),
        value.getObjects(),
        value.getSuggestedTags(),
        value.getCaptionSuggestions());
  }
}
