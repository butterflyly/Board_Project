# # Board_Project

## 1. 프로젝트 소개

간단한 커뮤니티 형식으로 유저간 커뮤니케이션 웹 페이지를 만들어 보았다.

자세한 기능은 후술

***

## 2. 개발환경

- Version : Java 17
- IDE : IntelliJ
- Framework : SpringBoot 3.4.2
- ORM : JPA

***

## 3. 주요 기능
- 게시글 작성 CRUD
- 유저 CRUD
- 댓글 및 대댓글 CRUD 및 부가기능
- 관리자 페이지
    - 소프트 삭제 게시글 관리 페이지
    - 소프트 삭제 유저 관리 페이지
    - 각종 차트 관리 페이지
- 회원간 메세지 기능
- 소셜 로그인 기능(네이버, 구글)
- 정확한 로직은 컨트롤러 및 서비스 코드에 주석으로 달아놓을 예정(진행중)

***

## 4. 현재 아쉬운 기능(수정중)

- 소셜 회원 연결 해제 시 기존 회원탈퇴와 다른 로직으로 실행되고 있음
- 소셜 로그인 시 엑세스 토큰 데이터베이스에 저장하는 형식인데 Redis로 수정하기


***

## 5. 참고한 웹 페이지 및 블로그
- https://wikidocs.net/book/7601
- https://velog.io/@puar12/series/%EC%A0%90%ED%94%84%ED%88%AC%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8
- https://getbootstrap.com/
- https://velog.io/@location/series/SpringBoot-%EA%B2%8C%EC%8B%9C%ED%8C%90-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8 (개인 블로그)
- 등 기타 웹사이트 및 블로그

