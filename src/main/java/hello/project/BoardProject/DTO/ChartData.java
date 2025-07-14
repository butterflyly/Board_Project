package hello.project.BoardProject.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ChartData {

    private List<String> labels;
    private List<Integer> data;

}
