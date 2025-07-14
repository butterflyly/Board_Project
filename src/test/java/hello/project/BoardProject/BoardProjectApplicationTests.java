package hello.project.BoardProject;

import hello.project.BoardProject.DTO.Board.BoardImageUploadDTO;
import hello.project.BoardProject.DTO.Board.Request.BoardRequestDTO;
import hello.project.BoardProject.DTO.Board.Response.BoardResponseDTO;
import hello.project.BoardProject.DTO.Users.UserResponseDTO;
import hello.project.BoardProject.Entity.Board.Board;
import hello.project.BoardProject.Entity.Board.BoardNotVoter;
import hello.project.BoardProject.Entity.Board.BoardVoter;
import hello.project.BoardProject.Entity.Board.Board_Views;
import hello.project.BoardProject.Entity.Comment.Comment;
import hello.project.BoardProject.Entity.Users.Message;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Repository.Board.BoardNotVoterRepository;
import hello.project.BoardProject.Repository.Board.BoardRepository;
import hello.project.BoardProject.Repository.Board.BoardVoterRepository;
import hello.project.BoardProject.Repository.Board.Board_ViewsRepository;
import hello.project.BoardProject.Repository.Comment.CommentRepository;
import hello.project.BoardProject.Repository.Users.DeleteUserRepository;
import hello.project.BoardProject.Repository.Users.MessageRepository;
import hello.project.BoardProject.Repository.Users.UserRepository;
import hello.project.BoardProject.Service.BoardService;
import hello.project.BoardProject.Service.CommentService;
import hello.project.BoardProject.Service.MessageService;
import hello.project.BoardProject.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@SpringBootTest

class BoardProjectApplicationTests {

	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private BoardService boardService;
	@Autowired
	private UserService userService;

	@Autowired
	private CommentService commentService;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private MessageService messageService;

	@Autowired
	private Board_ViewsRepository boardViewsRepository;

	@Autowired
	private UserRepository userRepository;


	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private DeleteUserRepository deleteUserRepository;

	@Autowired
	private BoardVoterRepository boardVoterRepository;

	@Autowired
	private BoardNotVoterRepository boardNotVoterRepository;

	@Test
	void 게시글생성() {
		Random random = new Random();
		BoardRequestDTO boardRequestDTO = new BoardRequestDTO();

		for (int i = 0; i < 1800; i++){
			int j = 0;
			for(j =0; j < 31; j++)
			{
				int randomCreate = random.nextInt(60);
				for(int k=0; k <= randomCreate; k++)
				{

					int UserRandom = random.nextInt(112);

					Optional<Users> users = userRepository.findById((long) UserRandom);
					Users testUser;

					if(!users.isEmpty())
					{
						testUser = users.get();
					}
					else {
						continue;
					}

					int category = random.nextInt(3);
					// 게시글 저장
					if(category ==0)
					{
						boardRequestDTO.setTitle("테스트 질문 게시글"+ k+j);

					} else if (category ==1) {
						boardRequestDTO.setTitle("테스트 자유 게시글"+ k+j);
					}
					else {
						boardRequestDTO.setTitle("테스트 버그 게시글"+ k+j);
					}
					boardRequestDTO.setContent("테스트 내용" + k+j +i);
					boardRequestDTO.setUsers(testUser);
					boardRequestDTO.setCateogory(category);
					boardRequestDTO.setFix(false);
					boardRequestDTO.setCreateDate(LocalDateTime.now().minusDays(j));
					Board board = boardRequestDTO.toEntity();
					boardRepository.save(board);
				}
			}
			if(j == 31)
			{
				break;
			}
		}
	}

	@Test
	void 유저생성()
	{
		Random random = new Random();
		int randomCreate = random.nextInt(500);
		for (int i = 0; i < randomCreate; i++){
			int randomLocalDateTime = random.nextInt(31);
			userService.UserCreate("test"+i,"1111", String.valueOf(i+1),
					"테스트유저"+i,LocalDateTime.now().minusDays(randomLocalDateTime));
		}
	}

	@Test
	void 조회수주작은머야()
	{
		List<Board> boardList = boardRepository.findAll();
		Random random = new Random();

		for(Board board : boardList)
		{
			int randomView = random.nextInt(55);
			for(int i=0; i<randomView; i++)
			{
				boardRepository.updateView(board.getId());
				int randomLocalDateTime = random.nextInt(31);

				Board_Views boardViews = Board_Views.builder().
						boardId(board.getId()).category(board.getCategory()).
						viewsTime(LocalDateTime.now().minusDays(randomLocalDateTime)).
						build();

				boardViewsRepository.save(boardViews);
			}
		}
	}

	@Test
	void 댓글통계테스트()
	{
		List<Board> boardList = boardRepository.findAll();
		Random random = new Random();
		Users users = userService.getUser("admin");

		for(Board board : boardList)
		{
			int randomView = random.nextInt(45);
			for(int i=0; i<randomView; i++)
			{
				int randomLocalDateTime = random.nextInt(31);

				String content = "테스트 댓글입니당" + randomView+1 + "번째 댓글이에용";

				Comment newComment = Comment.builder()
						.content(content)
						.users(users)
						.board(board)
						.secret(false)
						.createDate(LocalDateTime.now().minusDays(randomLocalDateTime))
						.build();
				// 댓글 생성
				commentRepository.save(newComment);
			}
		}
	}

	@Test
	void 게시글_랜덤_삭제()
	{
		List<Board> boardList = boardRepository.findAll();
		Random random = new Random();

		int deleteBoardCount = 300;
		int bool = 0;

		// 당연히 살아있는 게시글 리스트 중에서 반복문을 돌림
		for(Board board : boardList)
		{
			bool +=1;
			if(bool >= deleteBoardCount)
			{
				break;
			}

			int randomLocalDateTime = random.nextInt(31);
			LocalDateTime localDateTime = LocalDateTime.now().minusDays(randomLocalDateTime);

			// 게시글의 삭제날짜가 생성일보다 미래인 경우
			if(board.getCreateDate().isBefore(localDateTime))
			{
				boardService.delete(board.getId(),localDateTime);
			}
			else {
				continue;
			}
		}
	}

	@Test
	void 랜덤유저의_댓글생성()
	{
		List<Board> boardList = boardRepository.findAll();
		Random random = new Random();

		for(Board board : boardList)
		{
			int randomView = random.nextInt(45);
			for(int i=0; i<randomView; i++)
			{
				int UserRandom = random.nextInt(32);

				Optional<Users> users = userRepository.findById((long) UserRandom);
				Users testUser;

				if(!users.isEmpty())
				{
					testUser = users.get();
				}
				else {
					continue;
				}

				int randomLocalDateTime = random.nextInt(31);

				String content = "테스트 댓글입니당" + randomView+1150 + "번째 댓글이에용";

				Comment newComment = Comment.builder()
						.content(content)
						.users(testUser)
						.board(board)
						.secret(false)
						.createDate(LocalDateTime.now().minusDays(randomLocalDateTime))
						.build();
				// 댓글 생성
				commentRepository.save(newComment);
			}
		}
	}

	@Test
	void 유저삭제시간지정()
	{
		List<Users> usersList = deleteUserRepository.findAll();
		Random random = new Random();

		for(Users users : usersList)
		{
			int randomTime = random.nextInt(31);
			deleteUserRepository.save(users.getId(),LocalDateTime.now().minusDays(randomTime));
		}
	}

	@Test
	void 유저하나생성()
	{
		userService.UserCreate("admin","1111", "wkfdlek1@naver.com",
				"관리자",LocalDateTime.now());
	}

	@Test
	void 유저랜덤삭제()
	{
		Random random = new Random();

		int delete = random.nextInt(60);

		for(int i=0; i<delete; i++)
		{
			int randomUser = random.nextInt(3,110);

			Optional<Users> users = userRepository.findById(Long.valueOf(randomUser));

			if(users.isEmpty())
			{
				continue;
			}
			else {
				Users deleteUser = users.get();

				UserResponseDTO userResponseDTO = userService.getUserDTO(deleteUser.getUsername());
				userService.UserDelete(userResponseDTO.getUsername());
			}
		}
	}

	@Test
	void 메세지송신()
	{
		Random random = new Random();
		for(int i=0;i<1000; i++)
		{
			int randomUser = random.nextInt(1,34);
			int randomUser2 = random.nextInt(1,34);

			Optional<Users> sender = userRepository.findById(Long.valueOf(randomUser));
			Optional<Users> receiver = userRepository.findById((long) randomUser2);

			Users send;
			Users receive;

			if(!sender.isEmpty())
			{
				send = sender.get();
			}
			else {
				continue;
			}

			if(!receiver.isEmpty())
			{
				receive = receiver.get();
			}
			else {
				continue;
			}

			if(send.getId().equals(receive.getId()))
			{
				continue;
			}

			Message message = Message.builder().
					sender(send).receiver(receive).
					title("테스트로 송신합니다" + i).content("").sendTime(LocalDateTime.now()).
					build();

			messageRepository.save(message);
		}
	}

	@Test
	@Transactional
	void 게시글추천()
	{
		Random random = new Random();
		List<Board> boardList = boardRepository.findAll();

		for(Board board : boardList)
		{
			int randomloop = random.nextInt(15);
			for(int i =0; i<=randomloop; i++)
			{
				int randomUser = random.nextInt(3,30);
				Optional<Users> users = userRepository.findById(Long.valueOf(randomUser));

				// 유저가 게시글 내에서 추천을 했는지 찾기
				BoardVoter boardVoter = boardVoterRepository.findByBoardAndVoter(board,users.get());
				// 유저가 게시글 내에서 비추천을 하였는지 찾기
				BoardNotVoter boardNotVoter = boardNotVoterRepository.findByBoardAndNotVoter(board,users.get());


				// 추천을 한 경우
				if(boardVoter != null)
				{
					// 추천을 한 유저가 비추천을 안한 경우(사실 이게 일반적이긴함)
					if(boardNotVoter == null)
					{
						// 추천 클릭 이벤트므로 추천취소로 해석
						board.VoteMinus(boardVoter);
						this.boardRepository.save(board); // orphanRemoval = true 하였으므로 고아객체가 되서 추천 엔티티가 자동삭제됨
					}
				}

				// 추천을 안한 경우
				else
				{
					// 비추천도 안한 경우 => 추천을 누르면 추천이 + 됨
					if(boardNotVoter ==null)
					{
						BoardVoter newBoardVoter = BoardVoter.builder().
								voter(users.get())
								.board(board).createDate(LocalDateTime.now()).
								build();

						boardVoterRepository.save(newBoardVoter);
					}
				}
			}
		}

	}

	@Test
	@Transactional
	void 게시글비추천()
	{
		Random random = new Random();
		List<Board> boardList = boardRepository.findAll();

		for(Board board : boardList) {
			int randomloop = random.nextInt(15);

			for (int i = 0; i <= randomloop; i++) {

				int randomUser = random.nextInt(3, 30);
				Optional<Users> users = userRepository.findById(Long.valueOf(randomUser));

				BoardVoter boardVoter = boardVoterRepository.findByBoardAndVoter(board, users.get());
				BoardNotVoter boardNotVoter = boardNotVoterRepository.findByBoardAndNotVoter(board, users.get());

				// 비추천이 클릭되어 있는 경우
				if (boardNotVoter != null) {
					// 추천을 안한 경우 => 비추천을 취소함
					if (boardVoter == null) {
						board.NotVoterMinus(boardNotVoter);
						this.boardRepository.save(board);
					}
				}

				// 비추천을 안 누른 경우
				else {
					// 추천을 안누름 => 비추천을 함
					if (boardVoter == null) {
						BoardNotVoter newBoardNotVoter = BoardNotVoter.builder().
								notVoter(users.get())
								.board(board).createDate(LocalDateTime.now()).
								build();

						boardNotVoterRepository.save(newBoardNotVoter);
					}
				}
			}
		}
	}

	@Test
	void 댓글추천()
	{

	}

	@Test
	void 댓글비추천()
	{

	}

}
