package hello.project.BoardProject.Service;

import hello.project.BoardProject.DTO.ChartData;
import hello.project.BoardProject.Entity.Board.Board_Views;
import hello.project.BoardProject.Entity.Users.LoginLog;
import hello.project.BoardProject.Entity.Users.Users;
import hello.project.BoardProject.Repository.Users.LoginLogRepository;
import hello.project.BoardProject.Repository.Users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Service
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;
    private final UserRepository userRepository;


    public void loginLogSave(String username)
    {
        Users users = userRepository.findByusername(username).orElseThrow();
        LoginLog loginLog = LoginLog.builder().
                loginlogTime(LocalDateTime.now()).user_id(users.getId()).
                build();

        loginLogRepository.save(loginLog);
    }

    public ChartData LoginLogChartData() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        Map<String, Integer> LoginLogCounts = new LinkedHashMap<>();

        List<LoginLog> loginLogList =
                loginLogRepository.findByLoginlogTimeBetweenOrderByLoginlogTime(oneMonthAgo,now);

        Collections.sort(loginLogList, Comparator.comparing(LoginLog::getLoginlogTime));


        for(LoginLog loginLog : loginLogList)
        {
            String date = loginLog.getLoginlogTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LoginLogCounts.put(date, LoginLogCounts.getOrDefault(date, 0) + 1);
        }

        for (String date : LoginLogCounts.keySet()) {
            labels.add(date);
            values.add(LoginLogCounts.get(date));
        }

        ChartData chartData = new ChartData(labels,values);

        return chartData;

    }

}