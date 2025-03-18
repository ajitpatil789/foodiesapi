package in.ajit.foodiesapi.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import in.ajit.foodiesapi.entity.FoodEntity;
import in.ajit.foodiesapi.io.FoodRequest;
import in.ajit.foodiesapi.io.FoodResponse;
import in.ajit.foodiesapi.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FoodServiceImpl implements FoodService {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private FoodRepository foodRepository;

    // Upload folder name from application.properties
    @Value("${cloudinary.upload-folder}")
    private String uploadFolder;

    /**
     * Uploads a file to Cloudinary and returns the secure URL.
     */
    @Override
    public String uploadFile(MultipartFile file) {
        String filenameExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        String uniqueFilename = UUID.randomUUID().toString();

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", uploadFolder + "/" + uniqueFilename,
                    "resource_type", "auto"
            ));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error uploading file", e);
        }
    }

    /**
     * Adds a new food item with an uploaded image.
     */
    @Override
    public FoodResponse addFood(FoodRequest request, MultipartFile file) {
        FoodEntity newFoodEntity = convertToEntity(request);
        String imageUrl = uploadFile(file);
        newFoodEntity.setImageUrl(imageUrl);
        newFoodEntity = foodRepository.save(newFoodEntity);
        return convertToResponse(newFoodEntity);
    }

    /**
     * Retrieves all food items from the database.
     */
    @Override
    public List<FoodResponse> readFoods() {
        return foodRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Without Stream
//    public List<FoodResponse> readFoods() {
//        List<FoodEntity> foodEntities = foodRepository.findAll();
//        List<FoodResponse> foodResponses = new ArrayList<>();
//
//        for (FoodEntity foodEntity : foodEntities) {
//            foodResponses.add(convertToResponse(foodEntity));
//        }
//
//        return foodResponses;
//    }


    /**
     * Retrieves a single food item by its ID.
     */
    @Override
    public FoodResponse readFood(String id) {
        FoodEntity existingFood = foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food not found for the id " + id));
        return convertToResponse(existingFood);
    }

    /**
     * Deletes a file from Cloudinary using the extracted public ID.
     */
    @Override
    public boolean deleteFile(String imageUrl) {
        try {
            // Step 1: Extract the path after 'upload/' (removing domain and version)
            String afterUpload = imageUrl.substring(imageUrl.indexOf("upload/") + 7); // foodies-foods/14713155-f833-4209-ba36-74232db71ad0.jpg.jpg

            // Step 2: Remove the version number (v1741935833/)
            int firstSlash = afterUpload.indexOf("/");
            String withoutVersion = afterUpload.substring(firstSlash + 1); // foodies-foods/14713155-f833-4209-ba36-74232db71ad0.jpg.jpg

            // Step 3: Remove file extension (.jpg, .png, etc.)
            String publicId = withoutVersion.substring(0, withoutVersion.lastIndexOf(".")); // foodies-foods/14713155-f833-4209-ba36-74232db71ad0

            // Step 4: Delete file from Cloudinary
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return "ok".equals(result.get("result"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a food item, including its image from Cloudinary and record from MongoDB.
     */
    @Override
    public void deleteFood(String id) {
        FoodResponse response = readFood(id);
        String imageUrl = response.getImageUrl();

        boolean isFileDeleted = imageUrl == null || imageUrl.isEmpty() || deleteFile(imageUrl);

        if (isFileDeleted) {
            foodRepository.deleteById(response.getId());
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete image, skipping database deletion.");
        }
    }

    /**
     * Converts FoodRequest DTO to FoodEntity.
     */
    private FoodEntity convertToEntity(FoodRequest request) {
        return FoodEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .build();
    }


    /**
     * Instead of using FoodEntity.builder(), we manually create an instance of FoodEntity.
     * We use setter methods to set the properties.
     * Finally, we return the populated FoodEntity object.
     */

//    private FoodEntity convertToEntity(FoodRequest request) {
//        FoodEntity foodEntity = new FoodEntity();
//        foodEntity.setName(request.getName());
//        foodEntity.setDescription(request.getDescription());
//        foodEntity.setCategory(request.getCategory());
//        foodEntity.setPrice(request.getPrice());
//        return foodEntity;
//    }


    /**
     * Converts FoodEntity to FoodResponse DTO.
     */
    private FoodResponse convertToResponse(FoodEntity entity) {
        return FoodResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl())
                .build();
    }
}
