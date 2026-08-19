package com.veyru.dto.response.ai;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageAnalysisResponse {
  /** AI's description of the image */
  private String imageDescription;

  /** Detected scene type */
  private String sceneType;

  /** Detected mood/atmosphere */
  private String mood;

  /** Dominant colors (if detected) */
  private List<String> colors;

  /** Objects/elements detected in image */
  private List<String> objects;

  /** Suggested tags based on image analysis */
  private List<String> suggestedTags;

  /** Initial caption suggestions based on the image */
  private List<String> captionSuggestions;
}
