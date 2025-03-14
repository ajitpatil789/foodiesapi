package in.ajit.foodiesapi.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

public class CloudinaryBuilder {
    private String cloudName;
    private String apiKey;
    private String apiSecret;

    public static CloudinaryBuilder builder() {
        return new CloudinaryBuilder();
    }

    public CloudinaryBuilder cloudName(String cloudName) {
        this.cloudName = cloudName;
        return this;
    }

    public CloudinaryBuilder apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public CloudinaryBuilder apiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
        return this;
    }

    public Cloudinary build() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }
}