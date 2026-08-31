package com.veyru.config;

import com.cloudinary.Cloudinary;
import com.veyru.adapter.out.cloudinary.CloudinaryImageStorage;
import com.veyru.application.port.out.ImageStorage;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CloudinaryConfig {
  @Bean
  public Cloudinary cloudinary(CloudinaryProperties properties) {
    Map<String, Object> config = new HashMap<>();
    config.put("cloud_name", properties.cloudName());
    config.put("api_key", properties.apiKey());
    config.put("api_secret", properties.apiSecret());
    config.put("secure", properties.secure());
    return new Cloudinary(config);
  }

  @Bean
  public ImageStorage cloudinaryImageStorage(Cloudinary cloudinary) {
    return new CloudinaryImageStorage(cloudinary);
  }
}
