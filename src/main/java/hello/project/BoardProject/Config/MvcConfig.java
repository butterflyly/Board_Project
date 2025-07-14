package hello.project.BoardProject.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 리소스 등록 및 핸들러를 관리하는 객체인 ResourceHandlerRegistry를 통해 리소스의 위치와 리소스와 매칭 될 url을 설정
        registry.addResourceHandler("/profileImages/**") // 사용자의 요청을 Resource로 맵핑할 경로를 적어주면 됨
                .addResourceLocations("file:/C:/Temp/profileImageUpload/"); // 실제 리소스가 존재하는 외부 경로를 지정

        registry.addResourceHandler("/commentImage/**")
                        .addResourceLocations("file:/C:/Temp/commentImageUpload/");

        registry.addResourceHandler("/boardImages/**")
                .addResourceLocations("file:/C:/Temp/boardImageUpload/");

        registry.addResourceHandler("/deleteboardImages/**")
                .addResourceLocations("file:/C:/Temp/deleteboardImageUpload/");

    }
}