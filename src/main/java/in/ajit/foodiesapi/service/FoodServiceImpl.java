package in.ajit.foodiesapi.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import in.ajit.foodiesapi.entity.FoodEntity;
import in.ajit.foodiesapi.io.FoodRequest;
import in.ajit.foodiesapi.io.FoodResponse;
import in.ajit.foodiesapi.repository.FoodRepository;
import in.ajit.foodiesapi.service.FoodService;
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



    @Value("${cloudinary.upload-folder}") // Optional, if you want to organize images
    private String uploadFolder;

    @Override
    public String uploadFile(MultipartFile file) {
        // Generate a unique filename
        String filenameExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        String uniqueFilename = UUID.randomUUID().toString() + "." + filenameExtension;

        try {
            // Upload the file to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", uploadFolder + "/" + uniqueFilename,  // Store inside an upload folder (optional)
                    "resource_type", "auto"       // Auto-detect file type
            ));

            // Get the secure URL of the uploaded image
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while uploading the file");
        }
    }

    @Override
    public FoodResponse addFood(FoodRequest request, MultipartFile file) {
        FoodEntity newFoodEntity = convertToEntity(request);
        String imageUrl = uploadFile(file);
        newFoodEntity.setImageUrl(imageUrl);
        newFoodEntity = foodRepository.save(newFoodEntity);
        return convertToResponse(newFoodEntity);
    }

    @Override
    public List<FoodResponse> readFoods() {
        List<FoodEntity> databaseEntries = foodRepository.findAll();
        return databaseEntries.stream().map(object ->convertToResponse(object)).collect(Collectors.toList());
    }

    private FoodEntity convertToEntity(FoodRequest request){
        return FoodEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .build();
    }
    private FoodResponse convertToResponse(FoodEntity entity){
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
