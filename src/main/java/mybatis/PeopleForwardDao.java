package mybatis;

import java.util.List;

/**
 * Created by zsc on 2016/12/22.
 */
public interface PeopleForwardDao {
    Forward selectById (int Id);

    List<Forward> selectAll();

    void insert(Forward forward);

    void insertAll(List<Forward> forwardList);

    void update(Forward forward);

    void delete(int Id);
}
