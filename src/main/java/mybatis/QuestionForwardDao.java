package mybatis;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by zsc on 2016/12/21.
 */
public interface QuestionForwardDao {

    Forward selectById (int Id);

    List<Forward> selectIn(List<Integer> Ids);

    List<Forward> selectByLimit(@Param("offset") int offset, @Param("limit") int limit);

    List<Forward> selectAll();

    void insert(Forward forward);

    void insertAll(List<Forward> forwardList);

    void update(Forward forward);

    void delete(int Id);

    int getCount();
}
