import com.hankcs.hanlp.seg.common.Term;

import java.util.List;

/**
 * Created by zsc on 2016/12/17.
 */
public interface Filter {
    List<Term> accept(List<Term> termList);//不用public
}
