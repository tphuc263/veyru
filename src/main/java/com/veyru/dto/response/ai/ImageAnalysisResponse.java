package com.veyru.dto.response.ai;

import java.util.List;

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

  /** AI's description of the image */
  public String getImageDescription() {
    return this.imageDescription;
  }

  /** Detected scene type */
  public String getSceneType() {
    return this.sceneType;
  }

  /** Detected mood/atmosphere */
  public String getMood() {
    return this.mood;
  }

  /** Dominant colors (if detected) */
  public List<String> getColors() {
    return this.colors;
  }

  /** Objects/elements detected in image */
  public List<String> getObjects() {
    return this.objects;
  }

  /** Suggested tags based on image analysis */
  public List<String> getSuggestedTags() {
    return this.suggestedTags;
  }

  /** Initial caption suggestions based on the image */
  public List<String> getCaptionSuggestions() {
    return this.captionSuggestions;
  }

  /** AI's description of the image */
  public void setImageDescription(final String imageDescription) {
    this.imageDescription = imageDescription;
  }

  /** Detected scene type */
  public void setSceneType(final String sceneType) {
    this.sceneType = sceneType;
  }

  /** Detected mood/atmosphere */
  public void setMood(final String mood) {
    this.mood = mood;
  }

  /** Dominant colors (if detected) */
  public void setColors(final List<String> colors) {
    this.colors = colors;
  }

  /** Objects/elements detected in image */
  public void setObjects(final List<String> objects) {
    this.objects = objects;
  }

  /** Suggested tags based on image analysis */
  public void setSuggestedTags(final List<String> suggestedTags) {
    this.suggestedTags = suggestedTags;
  }

  /** Initial caption suggestions based on the image */
  public void setCaptionSuggestions(final List<String> captionSuggestions) {
    this.captionSuggestions = captionSuggestions;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof ImageAnalysisResponse)) return false;
    final ImageAnalysisResponse other = (ImageAnalysisResponse) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    final java.lang.Object this$imageDescription = this.getImageDescription();
    final java.lang.Object other$imageDescription = other.getImageDescription();
    if (this$imageDescription == null
        ? other$imageDescription != null
        : !this$imageDescription.equals(other$imageDescription)) return false;
    final java.lang.Object this$sceneType = this.getSceneType();
    final java.lang.Object other$sceneType = other.getSceneType();
    if (this$sceneType == null ? other$sceneType != null : !this$sceneType.equals(other$sceneType))
      return false;
    final java.lang.Object this$mood = this.getMood();
    final java.lang.Object other$mood = other.getMood();
    if (this$mood == null ? other$mood != null : !this$mood.equals(other$mood)) return false;
    final java.lang.Object this$colors = this.getColors();
    final java.lang.Object other$colors = other.getColors();
    if (this$colors == null ? other$colors != null : !this$colors.equals(other$colors))
      return false;
    final java.lang.Object this$objects = this.getObjects();
    final java.lang.Object other$objects = other.getObjects();
    if (this$objects == null ? other$objects != null : !this$objects.equals(other$objects))
      return false;
    final java.lang.Object this$suggestedTags = this.getSuggestedTags();
    final java.lang.Object other$suggestedTags = other.getSuggestedTags();
    if (this$suggestedTags == null
        ? other$suggestedTags != null
        : !this$suggestedTags.equals(other$suggestedTags)) return false;
    final java.lang.Object this$captionSuggestions = this.getCaptionSuggestions();
    final java.lang.Object other$captionSuggestions = other.getCaptionSuggestions();
    if (this$captionSuggestions == null
        ? other$captionSuggestions != null
        : !this$captionSuggestions.equals(other$captionSuggestions)) return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof ImageAnalysisResponse;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $imageDescription = this.getImageDescription();
    result = result * PRIME + ($imageDescription == null ? 43 : $imageDescription.hashCode());
    final java.lang.Object $sceneType = this.getSceneType();
    result = result * PRIME + ($sceneType == null ? 43 : $sceneType.hashCode());
    final java.lang.Object $mood = this.getMood();
    result = result * PRIME + ($mood == null ? 43 : $mood.hashCode());
    final java.lang.Object $colors = this.getColors();
    result = result * PRIME + ($colors == null ? 43 : $colors.hashCode());
    final java.lang.Object $objects = this.getObjects();
    result = result * PRIME + ($objects == null ? 43 : $objects.hashCode());
    final java.lang.Object $suggestedTags = this.getSuggestedTags();
    result = result * PRIME + ($suggestedTags == null ? 43 : $suggestedTags.hashCode());
    final java.lang.Object $captionSuggestions = this.getCaptionSuggestions();
    result = result * PRIME + ($captionSuggestions == null ? 43 : $captionSuggestions.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "ImageAnalysisResponse(imageDescription="
        + this.getImageDescription()
        + ", sceneType="
        + this.getSceneType()
        + ", mood="
        + this.getMood()
        + ", colors="
        + this.getColors()
        + ", objects="
        + this.getObjects()
        + ", suggestedTags="
        + this.getSuggestedTags()
        + ", captionSuggestions="
        + this.getCaptionSuggestions()
        + ")";
  }

  public ImageAnalysisResponse() {}

  /**
   * Creates a new {@code ImageAnalysisResponse} instance.
   *
   * @param imageDescription AI's description of the image
   * @param sceneType Detected scene type
   * @param mood Detected mood/atmosphere
   * @param colors Dominant colors (if detected)
   * @param objects Objects/elements detected in image
   * @param suggestedTags Suggested tags based on image analysis
   * @param captionSuggestions Initial caption suggestions based on the image
   */
  public ImageAnalysisResponse(
      final String imageDescription,
      final String sceneType,
      final String mood,
      final List<String> colors,
      final List<String> objects,
      final List<String> suggestedTags,
      final List<String> captionSuggestions) {
    this.imageDescription = imageDescription;
    this.sceneType = sceneType;
    this.mood = mood;
    this.colors = colors;
    this.objects = objects;
    this.suggestedTags = suggestedTags;
    this.captionSuggestions = captionSuggestions;
  }
}
