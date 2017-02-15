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
 * 关键词A,B,C按IDF大小排序
 * 按由多到少得到A,B,C的全组合
 * 对每一个组合进行求交，得到网页编号，并添加至LinkedHashSet
 */
public class QuestionSE {
    private List<Forward> fQuestions;
    private List<Reverse> rQuestions;
    private List<Forward> fFinalQuestions;
    private Map<String, Reverse> rQuestionsMap = new HashMap<String, Reverse>();
    private Map<String, Forward> fQuestionsMap = new HashMap<String, Forward>();


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
            fQuestionsMap.put(String.valueOf(question.getId()), question);
        }

    }

    private void genUrls(String str) {
        //得到按序排列的关键字集合
        List<Term> terms = Filter.accept(StandardTokenizer.segment(str));
        List<Reverse> keyWords = new ArrayList<Reverse>();
        for (Term term : terms) {
            if (rQuestionsMap.containsKey(term.word)) {
                keyWords.add(rQuestionsMap.get(term.word));
            }
        }
        Collections.sort(keyWords);//按IDF大小排序

        //得到按序排列的url集合，只要string
        List<String> sortedUrls = new ArrayList<String>();//1DELIMITER26DELIMITER48,84DELIMITER85
        for (Reverse reverse : keyWords) {
            sortedUrls.add(reverse.getUrls());
        }

        //得到最终排序
        Set<String> urls = new LinkedHashSet<String>();//保证按顺序且不重复
        System.out.println("urls " + sortedUrls);
        List<String> temp = new ArrayList<String>();
        int len = sortedUrls.size() + 1;
        for (int i = len - 1; i != 0; i--) {
            genInSequence(urls, sortedUrls, 0, i, temp);
        }

        //显示结果
        System.out.println("最终url: " + urls);

        //从数据库读数据
        List<Integer> fUrls = new ArrayList<Integer>();
        for (String url : urls) {
            fUrls.add(Integer.parseInt(url));
        }
        selectInQuestion(fUrls);
        for (int i = 0; i < fFinalQuestions.size(); i++) {
            System.out.println("读取结果 " + fFinalQuestions.get(i).getId());
        }
    }

    private void genInSequence(Set<String> urls, List<String> sortedUrls, int start, int len, List<String> temp) {//len为组合的长度
        if (len == 0) {
            List<String> result = new ArrayList<String>();
            result.addAll(Arrays.asList(temp.get(0).split(Config.DELIMITER)));
            for (int i = 1; i < temp.size(); i++) {
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

    private void selectInQuestion(List<Integer> fUrls) {
        SqlSession sqlSession = getSessionFactory().openSession();
        QuestionForwardDao forwardDao = sqlSession.getMapper(QuestionForwardDao.class);
        fFinalQuestions = forwardDao.selectIn(fUrls);
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

}
