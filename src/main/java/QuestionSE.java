import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import mybatis.Forward;
import mybatis.QuestionForwardDao;
import mybatis.QuestionReverseDao;
import mybatis.Reverse;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.util.*;

/**
 * Created by zsc on 2016/12/24.
 * 务必确保初始化
 */
public class QuestionSE {
    private List<Forward> fQuestions;
    private List<Reverse> rQuestions;
    private Map<String, Reverse> rQuestionsMap = new HashMap<String, Reverse>();
    private Map<String, Forward> fQuestionsForward = new HashMap<String, Forward>();


    public static void main(String args[]) {
        QuestionSE questionSE = new QuestionSE();
        questionSE.readAllMaps();
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入字符串：");
        questionSE.genUrls(scanner.nextLine());

    }

    //将正排和倒排数据读到内存中，保存在map，初始化数据
    private void readAllMaps() {
        selectAllQuestion();//将数据库的问题正排和倒排读进来
        //将2个表读到Map里
        for (Reverse question : rQuestions) {
            rQuestionsMap.put(question.getKeyWords(), question);
        }

        for (Forward question : fQuestions) {
            fQuestionsForward.put(String.valueOf(question.getId()), question);
        }

    }

    private void genUrls(String str) {
        //得到按序排列的关键字集合
        List<Term> terms = filter.accept(StandardTokenizer.segment(str));
        List<Reverse> keyWords = new ArrayList<Reverse>();
        for (Term term : terms) {
            if (rQuestionsMap.containsKey(term.word)) {
                keyWords.add(rQuestionsMap.get(term.word));
            }
        }
        Collections.sort(keyWords);

        //得到按序排列的url集合
        List<String> sortedUrls = new ArrayList<String>();
        for (Reverse reverse : keyWords) {
            sortedUrls.add(reverse.getUrls());
        }

        //得到最终排序
        Set<String> urls = new LinkedHashSet<>();
        List<String> temp = new ArrayList<String>();
        int len = sortedUrls.size() + 1;
        for (int i = len - 1; i != 0; i--) {
            genInSequence(urls, sortedUrls, 0, i, temp);
        }

        //显示结果
        System.out.println("最终url: " + urls);
    }

    private void genInSequence(Set<String> urls, List<String> sortedUrls, int start, int len, List<String> temp) {//len为组合的长度
        if (len == 0) {
            List<String> result = new ArrayList<>();
            result.addAll(Arrays.asList(temp.get(0).split(Config.DELIMITER)));
            for (int i = 0; i < temp.size(); i++) {
                result.retainAll(Arrays.asList(temp.get(i).split(Config.DELIMITER)));
            }
            urls.addAll(result);
            return;
        }
        if (start == sortedUrls.size()) {
            return;
        }
        temp.add(sortedUrls.get(start));
        genInSequence(urls, sortedUrls, start + 1, len - 1, temp);
        temp.remove(temp.size() - 1);
        genInSequence(urls, sortedUrls, start + 1, len, temp);
    }

    private void selectAllQuestion() {
        SqlSession sqlSession = getSessionFactory().openSession();
        QuestionReverseDao reverseDao = sqlSession.getMapper(QuestionReverseDao.class);
        QuestionForwardDao forwardDao = sqlSession.getMapper(QuestionForwardDao.class);
        rQuestions = reverseDao.selectAll();
        fQuestions = forwardDao.selectAll();
    }


    //Mybatis 通过SqlSessionFactory获取SqlSession, 然后才能通过SqlSession与数据库进行交互
    private static SqlSessionFactory getSessionFactory() {
        SqlSessionFactory sessionFactory = null;
        String resource = "configuration.xml";
        try {
            sessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader(resource));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sessionFactory;
    }




    Filter filter = new Filter() {
        @Override
        public List<Term> accept(List<Term> termList) {
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
    };
}
