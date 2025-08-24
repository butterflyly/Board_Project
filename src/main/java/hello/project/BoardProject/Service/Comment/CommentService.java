package hello.project.BoardProject.Service.Comment;


import hello.project.BoardProject.DTO.Board.Response.BoardResponseDTO;
import hello.project.BoardProject.DTO.ChartData;
import hello.project.BoardProject.DTO.Comment.CommentResponseDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Comment.Comment;
import hello.project.BoardProject.Entity.Comment.CommentImage;
import hello.project.BoardProject.Entity.Comment.CommentNotRecommend;
import hello.project.BoardProject.Entity.Comment.CommentRecommend;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Error.DataNotFoundException;
import hello.project.BoardProject.Repository.Board.BoardRepository;
import hello.project.BoardProject.Repository.Comment.CommentImageRepository;
import hello.project.BoardProject.Repository.Comment.CommentNotRecommendRepository;
import hello.project.BoardProject.Repository.Comment.CommentRecommendRepository;
import hello.project.BoardProject.Repository.Comment.CommentRepository;
import hello.project.BoardProject.Repository.Users.DeleteUserRepository;
import hello.project.BoardProject.Repository.Users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRecommendRepository commentRecommendRepository;
    private final CommentNotRecommendRepository commentNotRecommendRepository;
    private final CommentImageRepository commentImageRepository;
    private final DeleteUserRepository deleteUserRepository;

    @Value("${file.commentImagePath}")
    private String uploadFolder;




    // 댓글 작성
    @Transactional
    public Long writeReply(UserResponseDTO userResponseDTO, Long boardId,
                           String content, boolean secret,MultipartFile image) {
        Users users = userRepository.findByusername(userResponseDTO.getUsername()).orElseThrow(()
                -> new UsernameNotFoundException("해당 유저는 존재하지 않습니다."));
        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        Comment newComment = Comment.builder()
                .content(content)
                .users(users)
                .board(board)
                .secret(secret)
                .createDate(LocalDateTime.now())
                .build();
        // 댓글 생성
        commentRepository.save(newComment);

        // 이 부분 추가
        if (image != null) {

            UUID uuid = UUID.randomUUID();  //  범용 고유 식별자(UUID)를 생성하는 코드
            String imageFileName = uuid + "_" + image.getOriginalFilename(); //  UUID와 파일의 원본 파일명을 연결하여 이미지 파일명을 생성하는 코드

            File destinationFile = new File(uploadFolder + imageFileName); // 파일의 경로, 파일 이름

            try {
                //   MultipartFile에서 제공하는 메서드 중에 하나로,
                // 업로드된 파일을 지정된 경로에 저장하는 역할을 함
                image.transferTo(destinationFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            CommentImage commentImage = CommentImage.builder()
                    .url("/commentImage/" + imageFileName)
                    .comment(newComment)
                    .build();

            commentImageRepository.save(commentImage);
        }


        return newComment.getId();
    }

    // 대댓글 작성
    @Transactional
    public CommentResponseDTO createReplyComment(String content,
                                                 Boolean secret, UserResponseDTO userResponseDTO,
                                                 BoardResponseDTO boardResponseDTO,
                                                 CommentResponseDTO commentResponseDTO,MultipartFile image) {
        {
            Board board1 = boardRepository.findById(boardResponseDTO.getId()).orElseThrow(null);
            Users users = userRepository.findByusername(userResponseDTO.getUsername()).orElseThrow(null);
            Comment parent = getComment(commentResponseDTO.getId());

            Comment newComment = Comment.builder()
                    .content(content)
                    .users(users)
                    .board(board1)
                    .secret(secret)
                    .createDate(LocalDateTime.now())
                    .parent(parent)
                    .build();
            // 대댓글 저장
            commentRepository.save(newComment);

            // 이 부분 추가
            if (image != null) {

                UUID uuid = UUID.randomUUID();  //  범용 고유 식별자(UUID)를 생성하는 코드
                String imageFileName = uuid + "_" + image.getOriginalFilename(); //  UUID와 파일의 원본 파일명을 연결하여 이미지 파일명을 생성하는 코드

                File destinationFile = new File(uploadFolder + imageFileName); // 파일의 경로, 파일 이름

                try {
                    //   MultipartFile에서 제공하는 메서드 중에 하나로,
                    // 업로드된 파일을 지정된 경로에 저장하는 역할을 함
                    log.info("파일 저장됨 ?");
                    image.transferTo(destinationFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                CommentImage commentImage = CommentImage.builder()
                        .url("/commentImage/" + imageFileName)
                        .comment(newComment)
                        .build();

                commentImageRepository.save(commentImage);
            }

            CommentResponseDTO commentResponseDTO1 = getCommentDTO(newComment.getId());

            return commentResponseDTO1;
        }
    }


    // 게시글 내의 댓글 페이징 데이터
    public Page<CommentResponseDTO> findAll(int page, BoardResponseDTO boardResponseDto, String sort) {

        Board board =
                boardRepository.findById(boardResponseDto.getId()).
                        orElseThrow(() -> new IllegalArgumentException("해당 게시글은 존재하지 않습니다."));


        Pageable pageable;
        Page<Comment> commentList;

        // sort 가 생성시간 기준인 경우
        if(sort.equals("createDate"))
        {
            pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC,  "createDate"));
            commentList = commentRepository.findAllByBoard(pageable, board);
        }
        // sort가 추천 기준인 경우
        else {
            pageable = PageRequest.of(page, 10);
            commentList = commentRepository.findAllByBoardOrderByRecommends(board.getId(),pageable);
        }

        Page<CommentResponseDTO> commentResponseDTOS = commentList.map(b -> new CommentResponseDTO(b));

        return commentResponseDTOS;
    }

    // 댓글 데이터 가져오기
    public CommentResponseDTO getCommentDTO(Long id) {
        Optional<Comment> comment = this.commentRepository.findById(id);
        if (comment.isPresent()) {
            if(comment.get().getDeleteboardid() != null)
            {
                throw new DataNotFoundException("댓글이 없습니다.");
            }
            CommentResponseDTO commentResponseDTO = new CommentResponseDTO(comment.get());
            return commentResponseDTO;
        } else {
            throw new DataNotFoundException("댓글이 없습니다.");
        }
    }

    // 엔티티 댓글 데이터 가져오기
    public Comment getComment(Long id) {
        Optional<Comment> comment = this.commentRepository.findById(id);
        if (comment.isPresent()) {
            if(comment.get().getDeleteboardid() != null)
            {
                throw new DataNotFoundException("댓글이 없습니다.");
            }
            return comment.get();
        } else {
            throw new DataNotFoundException("댓글이 없습니다.");
        }
    }

    // 댓글 수정
    @Transactional
    public void modify(CommentResponseDTO commentResponseDTO, String content, boolean secret, MultipartFile image) {

        Comment comment = getComment(commentResponseDTO.getId());
        log.info("Comment ID :" + comment.getId());

        Comment mComment = comment.toBuilder()
                .content(content)
                .secret(secret)
                .modifyDate(LocalDateTime.now())
                .build();

        commentRepository.save(mComment);

        // 수정이미지가 있는 경우
        if (image != null) {
            // 수정 이미지가 있는데 기존 이미지가 있는 경우
            if(comment.getImage() != null)
            {
                ImageDelete(comment.getId());
            }


            UUID uuid = UUID.randomUUID();  //  범용 고유 식별자(UUID)를 생성하는 코드
            String imageFileName = uuid + "_" + image.getOriginalFilename(); //  UUID와 파일의 원본 파일명을 연결하여 이미지 파일명을 생성하는 코드

            File destinationFile = new File(uploadFolder + imageFileName); // 파일의 경로, 파일 이름

            CommentImage imageCheck = commentImageRepository.findByComment_Id(comment.getId());


            try {
                //   MultipartFile에서 제공하는 메서드 중에 하나로,
                // 업로드된 파일을 지정된 경로에 저장하는 역할을 함
                image.transferTo(destinationFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            CommentImage Image = CommentImage.builder()
                    .url("/commentImage/" + imageFileName)
                    .comment(mComment)
                    .build();

            commentImageRepository.save(Image);
        }
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = getComment(commentId);
        commentRepository.delete(comment);
    }

    // 유저가 작성한 최근 5개 댓글 가져오기
    public List<CommentResponseDTO> getCommentTop5LatestByUser(UserResponseDTO userResponseDTO) {
        Users users = userRepository.findByusername(userResponseDTO.
                getUsername()).orElseThrow(() -> new IllegalArgumentException("코멘트를 단 유저가 없음"));

        // 유저가 작성한 게시글이 삭제되지 않은 최근 5개 댓글 가져오기
        List<Comment> commentList = commentRepository.findTop5ByUsersAndBoardIsNotNullOrderByCreateDateDesc(users);

        List<CommentResponseDTO> commentResponseDTOList =
                commentList.stream().map(b -> new CommentResponseDTO(b)).
                        collect(Collectors.toList());

        return commentResponseDTOList;
    }


    // 유저가 작성한 댓글 개수 가져오기(Board가 NOT NULL인 경우만)
    public int getCommentCount(UserResponseDTO userResponseDTO) {
        Users users = userRepository.findByusername(userResponseDTO.
                getUsername()).orElseThrow(() -> new IllegalArgumentException("코멘트를 단 유저가 없음"));

        // 게시글이 NOTNULL 인 유저가 작성한 댓글 수
        return commentRepository.countByUsersAndBoardIsNotNull(users);
    }


    // 변수 데이터의 소프트삭제된 유저가 최근에 작성한 댓글 5개
    public List<CommentResponseDTO> getDeleteUserCommentTop5LatestByUser(UserResponseDTO userResponseDTO) {
        Users users = deleteUserRepository.findByusername(userResponseDTO.
                getUsername()).get();


        // 유저가 작성한 게시글이 삭제되지 않은 최근 5개 댓글 가져오기
        List<Comment> commentList =
                commentRepository.findTop5ByDelete_User_IdAndBoardIsNotNullOrderByCreateDateDesc(users.getId());

        List<CommentResponseDTO> commentResponseDTOList =
                commentList.stream().map(b -> new CommentResponseDTO(b)).
                        collect(Collectors.toList());

        return commentResponseDTOList;
    }

    // 소프트 삭제된 유저가 작성한 댓글 개수
    public Long getDeleteUserCommentCount(UserResponseDTO userResponseDTO) {
        Users users = deleteUserRepository.findByusername(userResponseDTO.
                getUsername()).get();

        // 게시글이 NOTNULL 인 유저가 작성한 댓글 수
        return commentRepository.countByDelete_User_IdAndBoardIsNotNull(users.getId());

    }

    // 유저가 작성한 댓글 페이징 정보 가져오기
    public Page<CommentResponseDTO> getPersonalCommentList(int page, String username) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts)); //페이지 번호, 개수

        Users users = userRepository.findByusername(username).orElseThrow(() -> new IllegalArgumentException("해당 유저없음"));
        // 게시글이 삭제된 댓글은 안가져오게끔
        Page<Comment> commentPage = commentRepository.findAllByUsersAndBoardIsNotNull(users, pageable);


        Page<CommentResponseDTO> commentResponseDTOPage =
                commentPage.map(b -> new CommentResponseDTO(b));
        return commentResponseDTOPage;

    }


    // 댓글 삭제
    @Transactional
    public void delete(Long commentId) {

        Comment comment = getComment(commentId);

        // 댓글이 없을 경우
        if (comment == null) {
            throw new IllegalArgumentException("Comment cannot be null");
        }

        // 댓글에 자식이 있는 경우
        if (comment.getChildren().size() != 0) {
            recommendDelete(comment);
            not_recommendDelete(comment);
            // 자식이 있으면 삭제 상태만 변경
            comment.deleteParent();
        } else if (comment.getParent() != null) {

            Comment parentComment = comment.getParent();
            // 자식 댓글 삭제
            parentComment.getChildren().remove(comment);
            commentRepository.delete(comment);

            if (parentComment.getChildren().size() == 0 && parentComment.isDeleted()) {
                commentRepository.delete(parentComment);
            }
        } else {
            commentRepository.delete(comment);
        }
    }


    @Transactional
    public Comment getDeletableAncestorComment(Comment comment) {
        Comment parent = comment.getParent(); // 현재 댓글의 부모를 구함
        if (parent != null && parent.getChildren().size() == 1 && parent.isDeleted() == true) {
            // 부모가 있고, 부모의 자식이 1개(지금 삭제하는 댓글)이고, 부모의 삭제 상태가 TRUE인 댓글이라면 재귀

            // 삭제가능 댓글 -> 만일 댓글의 조상(대댓글의 입장에서 할아버지 댓글)도 해당 댓글 삭제 시 삭제 가능한지 확인
            // 삭제 -> Cascade 옵션으로 가장 부모만 삭제 해도 자식들도 다 삭제 가능

            // Ajax로 비동기로 리스트 가져오기에, 대댓글 1개인거 삭제할 때 연관관계 삭제하고 부모 댓글 삭제하기 필요
            // 컨트롤러가 아닌 서비스의 삭제에서 처리해주는 이유는 연관관계를 삭제해주면 parent를 구할 수 없기에 여기서 끊어줘야 함
            // 연관관계만 끊어주면 orphanRemoval 옵션으로 자식 객체는 삭제되니 부모를 삭제 대상으로 넘기면 됨
            parent.getChildren().remove(comment);
            return getDeletableAncestorComment(parent);
        }


        return comment;
    }

    // 추천
    @Transactional
    public void recommend(Long comment_id, String userId) {
        Users users = userRepository.findByusername(userId).orElseThrow(() -> new IllegalArgumentException("유저가 없음"));
        commentRecommendRepository.recommend(comment_id, users.getId());
    }

    // 추천 취소
    @Transactional
    public void cancelRecommend(Long comment_id, String userId) {

        Users users = userRepository.findByusername(userId).orElseThrow(() -> new IllegalArgumentException("유저가 없음"));
        commentRecommendRepository.cancelRecommend(comment_id, users.getId());
    }

    // 유저가 추천을 했는가
    public boolean voteUsers(CommentResponseDTO commentResponseDTO, UserResponseDTO userResponseDTO) {
        Users users = userRepository.findByusername(userResponseDTO.getUsername()).orElseThrow(() -> new IllegalArgumentException("유저가 없음"));
        Comment comment = getComment(commentResponseDTO.getId());
        CommentRecommend commentRecommend = commentRecommendRepository.findByCommentAndUser(comment, users);

        if (commentRecommend == null) {
            return false;
        } else {
            return true;
        }

    }

    // 비추천
    @Transactional
    public void not_recommend(Long comment_id, String userId) {

        Users users = userRepository.findByusername(userId).orElseThrow(() -> new IllegalArgumentException("유저가 없음"));
        commentNotRecommendRepository.Notrecommend(comment_id, users.getId());
    }

    // 비추천 취소
    @Transactional
    public void not_cancelRecommend(Long comment_id, String userId) {

        Users users = userRepository.findByusername(userId).orElseThrow(() -> new IllegalArgumentException("유저가 없음"));
        commentNotRecommendRepository.cancelNotRecommend(comment_id, users.getId());
    }


    @Transactional
    public void recommendDelete(Comment comment) {
        Set<CommentRecommend> commentRecommend = commentRecommendRepository.findByComment(comment);

        for (CommentRecommend recommend : commentRecommend) {
            Long commentRecommendId = recommend.getId();
            commentRecommendRepository.deleteById(commentRecommendId);
        }
    }


    @Transactional
    public void not_recommendDelete(Comment comment) {
        Set<CommentNotRecommend> commentNotRecommendSet = commentNotRecommendRepository.findByComment(comment);
        for (CommentNotRecommend commentNotRecommend : commentNotRecommendSet) {
            Long commentNotRecommendId = commentNotRecommend.getId();
            commentNotRecommendRepository.deleteById(commentNotRecommendId);
        }
    }


    // 유저가 비추천을 했는가
    public boolean not_voteUsers(CommentResponseDTO commentResponseDTO, UserResponseDTO userResponseDTO) {
        Users users = userRepository.findByusername(userResponseDTO.getUsername()).
                orElseThrow(() -> new IllegalArgumentException("유저가 없음"));
        Comment comment = getComment(commentResponseDTO.getId());
        CommentNotRecommend commentRecommend = commentNotRecommendRepository.findByCommentAndUser(comment, users);

        if (commentRecommend == null) {
            return false;
        } else {
            return true;
        }

    }

    // 특정 순서의 데이터 제거 후 새로운 Page 객체 생성
    private static Page<Comment> removeDataByIndex(Page<Comment> originalPage, int indexToRemove) {
        List<Comment> originalData = originalPage.getContent();
        List<Comment> newData = new ArrayList<>();

        // indexToRemove를 제외한 데이터 복사
        for (int i = 0; i < originalData.size(); i++) {
            if (i != indexToRemove) {
                newData.add(originalData.get(i));
            }
        }

        // 새로운 Page 객체 생성
        return new PageImpl<>(newData, originalPage.getPageable(), newData.size());
    }


    public List<CommentResponseDTO> CommentTop3(BoardResponseDTO boardResponseDTO)
    {
        Board board = boardRepository.findById(boardResponseDTO.getId()).orElseThrow(() -> new IllegalArgumentException("게시글 없엉"));

        List<Comment> comments = commentRepository.findByOrderByRecommendCountMinusNotRecommendCountDesc(board.getId());


        List<CommentResponseDTO> commentResponseDTOList = comments.
                stream().map(b -> new CommentResponseDTO(b)).
                        collect(Collectors.toList());


        return commentResponseDTOList;
    }



    public boolean CommentVoterBoolean(BoardResponseDTO boardResponseDTO)
    {
        List<CommentResponseDTO> commentResponseDTOPage = CommentTop3(boardResponseDTO);

        int i= 0;
        for(CommentResponseDTO commentResponseDTO : commentResponseDTOPage)
        {
            // 추천댓글 조건이 맞으므로 i 값을 늘림
            if(commentResponseDTO.getRecommends().size() >0 &&
                    (commentResponseDTO.getRecommends().size() -
                            commentResponseDTO.getNotRecommends().size()) >=0 )
            {
                i += 1;
            }
            // 아닌경우(즉 추천댓글의 조건이랑 맞지가 않음)
        }

        // 추천댓글 조건이 아얘 안맞았다는 뜻이므로 false 출력
        if(i==0){
            return false;
        }
        else {
            return true;
        }
    }


    public int childListCheck(CommentResponseDTO commentResponseDTOParent) {

        int i=0;

        for(Comment comment : commentResponseDTOParent.getChildren())
        {
            if(!comment.getSecret()) // 대댓글이 비밀댓글이 아니다
            {
                i +=1; // i의 값을 늘린다
            }
        }
        Comment comment = getComment(commentResponseDTOParent.getId());
        comment.SecretNumber(i);
        return i; // 만약 i가 0이면 모두 비밀댓글이란뜻 0이 아니면 비밀댓글이 아닌 대댓글이 있다는 뜻이다.
    }


    @Transactional
    public void ImageDelete(Long commentId) {
        //첨부파일 조회
        Comment comment = getComment(commentId);
        CommentImage commentImage = commentImageRepository.findById(comment.getImage().getId()).orElseThrow(()->new RuntimeException());

        String URL = commentImage.getUrl();
        String URL_Replace = URL.replace("commentImage","commentImageUpload");

        //첨부파일 삭제
        File file = new File("C:/Temp" +URL_Replace);
        log.info("URL 링크 :" + URL_Replace);
        comment.setImage(null);
        file.delete();

        if(comment.getImage() != null)
        {
            log.info(" 왜 삭제안됨 ?");
        }
        else {
            log.info("삭제 됨");
        }

    }

    // 코멘트 차트

    public ChartData commentChart(List<BoardResponseDTO> boardResponseDTOList) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthMinus = now.minusMonths(1);

        List<Board> boardList = new ArrayList<>();
        List<Comment> commentList = new ArrayList<>();


        for(BoardResponseDTO boardResponseDTO : boardResponseDTOList)
        {
            Optional<Board> board = boardRepository.findById(boardResponseDTO.getId());

            if(!board.isEmpty())
            {
                boardList.add(board.get());
            }
        }

        for(Board board : boardList)
        {
            List<Comment> comments = commentRepository.
                    findAllByBoardAndCreateDateBetweenOrderByCreateDateAsc(board,oneMonthMinus,now);

            for(Comment comment : comments)
            {
                commentList.add(comment);
            }
        }

        Collections.sort(commentList, Comparator.comparing(Comment::getCreateDate));



        List<CommentResponseDTO> commentResponseDTOList = commentList.stream().map(c -> new CommentResponseDTO(c)).
                collect(Collectors.toList());

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        Map<String, Integer> BoardCounts = new LinkedHashMap<>();

        for (CommentResponseDTO commentResponseDTO : commentResponseDTOList) {
            String date = commentResponseDTO.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            BoardCounts.put(date, BoardCounts.getOrDefault(date, 0) + 1);
        }

        for (String date : BoardCounts.keySet()) {
            labels.add(date);
            values.add(BoardCounts.get(date));
        }

        ChartData chartData = new ChartData(labels,values);

        return chartData;
    }


}


