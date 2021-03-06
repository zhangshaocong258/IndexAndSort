package mybatis;

import java.util.List;

/**
 * Created by zsc on 2016/12/22.
 */
public interface PeopleReverseDao {
    Reverse selectById (int Id);

    List<Reverse> selectAll();

    void insert(Reverse reverse);

    void insertAll(List<Reverse> reverses);

    void update(Reverse reverse);

    void delete(int Id);
}
