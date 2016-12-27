import com.hankcs.hanlp.seg.common.Term;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsc on 2016/12/17.
 */
public class Filter {
    public static List<Term> accept(List<Term> termList){
        List<Term> temp = new ArrayList<Term>();
        for (int i = 0; i < termList.size(); i++) {
            Term term = termList.get(i);
            String nature = term.nature != null ? term.nature.toString() : "空";
            char firstChar = nature.charAt(0);
            switch(firstChar) {
                case 'b': //区别词 正 副
                case 'z': //状态词
                case 'r': //代词 怎样 如何
                case 'm':
                    break;
                case 'c':
                case 'e':
                case 'o':
                case 'p':
                case 'q':
                case 'u':
                case 'w':
                case 'y':
                    temp.add(term);
                    break;
                case 'd':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'n':
                case 's':
                case 't':
                case 'v':
                case 'x':
                default:
                    if(term.word.length() == 1) {//长度为1，删除，可以理解为没有分出来词，因此删除，最后查询时分出的词，也可以删除停用词
                        temp.add(term);
                    }
            }
        }
        termList.removeAll(temp);
        return termList;
    }
}
