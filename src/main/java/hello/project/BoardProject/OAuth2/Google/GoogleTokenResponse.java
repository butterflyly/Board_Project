package hello.project.BoardProject.OAuth2.Google;

// GoogleTokenResponse 클래스 (응답 JSON 파싱)
public class GoogleTokenResponse {
    private String access_token;
    private String token_type;
    private int expires_in;
    private String refresh_token; // Refresh token이 재발급될 수도 있습니다.

    // getter methods
    public String getAccessToken() { return access_token; }
    public String getTokenType() { return token_type; }
    public int getExpiresIn() { return expires_in; }
    public String getRefreshToken() { return refresh_token; }
}