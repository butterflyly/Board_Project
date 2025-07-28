package hello.project.BoardProject.Service;


import hello.project.BoardProject.DTO.ImageUploadDTO;
import hello.project.BoardProject.DTO.Users.ImageResponseDTO;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Entity.Users.UsersImage;
import hello.project.BoardProject.Repository.Users.ImageRepository;
import hello.project.BoardProject.Repository.Users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @Value("${file.profileImagePath}")
    private String uploadFolder;

    // 이미지 업로드
    public void upload(ImageUploadDTO imageUploadDTO, String email) {
        Users users = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("이메일이 존재하지 않습니다."));
        UsersImage image = imageRepository.findByUsers(users);

        // 업로드 이미지가 안비어있는 경우
        if(!imageUploadDTO.getFile().isEmpty() && imageUploadDTO.getFile() !=null)
        {
            MultipartFile file = imageUploadDTO.getFile();
            UUID uuid = UUID.randomUUID();
            String imageFileName = uuid + "_" + file.getOriginalFilename();

            File destinationFile = new File(uploadFolder + imageFileName);

            try {
                file.transferTo(destinationFile);

                if (image != null) {
                    // 이미지가 이미 존재하면 url 업데이트
                    image.updateUrl("/profileImages/" + imageFileName);
                } else {
                    // 이미지가 없으면 객체 생성 후 저장
                    image = UsersImage.builder()
                            .users(users)
                            .url("/profileImages/" + imageFileName)
                            .build();
                }
                imageRepository.save(image);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // 업로드 이미지가 비어있는 경우
        else
        {
            if(image == null)
            {
                UsersImage image2 = UsersImage.builder()
                        .url("/profileImages/anonymous.png")
                        .users(users)
                        .build();
                imageRepository.save(image2);
            }
        }
    }


    // 이미지 복원
    public void ImageReStore(String email)
    {
        Users users = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("이메일이 존재하지 않습니다."));
        UsersImage image = imageRepository.findByUsers(users);

        UsersImage image2 = UsersImage.builder()
                .url("/profileImages/anonymous.png")
                .users(users)
                .build();
        imageRepository.save(image2);
    }

    // 이미지 찾기
    public ImageResponseDTO findImage(String email) {
        Users users = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("이메일이 존재하지 않습니다."));
        UsersImage image = imageRepository.findByUsers(users);

        String defaultImageUrl = "/profileImages/anonymous.png";

        if (image == null) {
            UsersImage image2 = UsersImage.builder()
                    .url(defaultImageUrl)
                    .users(users)
                    .build();
            imageRepository.save(image2);

            return ImageResponseDTO.builder()
                    .url(defaultImageUrl)
                    .build();
        } else {
            return ImageResponseDTO.builder()
                    .url(image.getUrl())
                    .build();
        }
    }
}


