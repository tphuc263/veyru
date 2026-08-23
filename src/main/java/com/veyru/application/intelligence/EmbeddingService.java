package com.veyru.application.intelligence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local embedding service using hash-based approach. Generates fixed-dimension vectors from text
 * for similarity matching.
 */
public class EmbeddingService {
  private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);
  public static final int EMBEDDING_DIMENSION = 128;

  public float[] generateEmbedding(String text) {
    if (text == null || text.isBlank()) {
      log.warn("Empty text provided for embedding generation");
      return null;
    }
    String truncated = text.length() > 2000 ? text.substring(0, 2000) : text;

    float[] embedding = new float[EMBEDDING_DIMENSION];
    String[] tokens = truncated.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").split("\\s+");
    for (String token : tokens) {
      if (token.isBlank()) continue;
      for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
        int combinedHash = hashString(token + "_" + i);
        float value = ((combinedHash & 4294967295L) / (float) 4294967295L) - 0.5F;
        embedding[i] += value;
      }
    }
    return normalize(embedding);
  }

  public float[] generateEmbeddingFromTexts(List<String> texts) {
    if (texts == null || texts.isEmpty()) {
      return new float[EMBEDDING_DIMENSION];
    }
    String combined = String.join(" ", texts);
    float[] embedding = generateEmbedding(combined);
    return embedding != null ? embedding : new float[EMBEDDING_DIMENSION];
  }

  public String buildPhotoText(String caption, List<String> tags) {
    StringBuilder sb = new StringBuilder();
    if (caption != null && !caption.isBlank()) {
      sb.append(caption).append(" ");
    }
    if (tags != null && !tags.isEmpty()) {
      sb.append(String.join(" ", tags));
    }
    return sb.toString().trim();
  }

  public String buildUserProfileText(
      String bio, List<String> topTags, List<String> recentCaptions) {
    StringBuilder sb = new StringBuilder();
    if (bio != null && !bio.isBlank()) {
      sb.append(bio).append(" ");
    }
    if (topTags != null && !topTags.isEmpty()) {
      sb.append(String.join(" ", topTags)).append(" ");
    }
    if (recentCaptions != null && !recentCaptions.isEmpty()) {
      recentCaptions.stream().limit(5).forEach(c -> sb.append(c).append(" "));
    }
    return sb.toString().trim();
  }

  public float cosineSimilarity(float[] a, float[] b) {
    if (a == null || b == null || a.length != b.length) {
      return 0.0F;
    }
    float dotProduct = 0.0F;
    float normA = 0.0F;
    float normB = 0.0F;
    for (int i = 0; i < a.length; i++) {
      dotProduct += a[i] * b[i];
      normA += a[i] * a[i];
      normB += b[i] * b[i];
    }
    float denominator = (float) Math.sqrt(normA) * (float) Math.sqrt(normB);
    if (denominator == 0) {
      return 0.0F;
    }
    return dotProduct / denominator;
  }

  public static byte[] floatArrayToBytes(float[] floats) {
    byte[] bytes = new byte[floats.length * 4];
    for (int i = 0; i < floats.length; i++) {
      int bits = Float.floatToIntBits(floats[i]);
      bytes[i * 4] = (byte) (bits & 255);
      bytes[i * 4 + 1] = (byte) ((bits >> 8) & 255);
      bytes[i * 4 + 2] = (byte) ((bits >> 16) & 255);
      bytes[i * 4 + 3] = (byte) ((bits >> 24) & 255);
    }
    return bytes;
  }

  public static float[] bytesToFloatArray(byte[] bytes) {
    float[] floats = new float[bytes.length / 4];
    for (int i = 0; i < floats.length; i++) {
      int bits =
          (bytes[i * 4] & 255)
              | ((bytes[i * 4 + 1] & 255) << 8)
              | ((bytes[i * 4 + 2] & 255) << 16)
              | ((bytes[i * 4 + 3] & 255) << 24);
      floats[i] = Float.intBitsToFloat(bits);
    }
    return floats;
  }

  private int hashString(String input) {
    byte[] hash;
    try {
      hash = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is required by the Java platform", e);
    }
    return ((hash[0] & 255) << 24)
        | ((hash[1] & 255) << 16)
        | ((hash[2] & 255) << 8)
        | (hash[3] & 255);
  }

  private float[] normalize(float[] vector) {
    float sum = 0.0F;
    for (float v : vector) {
      sum += v * v;
    }
    float magnitude = (float) Math.sqrt(sum);
    if (magnitude == 0) {
      return vector;
    }
    float[] normalized = new float[vector.length];
    for (int i = 0; i < vector.length; i++) {
      normalized[i] = vector[i] / magnitude;
    }
    return normalized;
  }
}
