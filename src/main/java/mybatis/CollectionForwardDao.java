package mybatis;

import java.util.List;

/**
 * Created by zsc on 2016/12/22.
 */
public interface CollectionForwardDao {
    Forward selectById (int Id);

    List<Forward> selectAll();

    void insert(Forward forward);

    void update(Forward forward);

    void delete(int Id);
}
