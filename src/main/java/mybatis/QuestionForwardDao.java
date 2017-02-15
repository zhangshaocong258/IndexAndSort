package mybatis;

import java.util.List;

/**
 * Created by zsc on 2016/12/21.
 */
public interface QuestionForwardDao {

    Forward selectById (int Id);

    List<Forward> selectIn(List<Integer> Ids);

    List<Forward> selectAll();

    void insert(Forward forward);

    void update(Forward forward);

    void delete(int Id);
}
