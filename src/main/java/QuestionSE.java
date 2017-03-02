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
        System.out.println("urls " + sortedUrls + "     " + sortedUrls.size());

        //递归排序
        List<String> temp = new ArrayList<String>();
        int len = sortedUrls.size() + 1;
        for (int i = len - 1; i != 0; i--) {
            genInSequence(urls, sortedUrls, 0, i, temp);
        }
        //显示结果
        System.out.println("最终url: " + urls);


        //非递归排序


        //从数据库读数据
//        List<Integer> fUrls = new ArrayList<Integer>();
//        for (String url : urls) {
//            fUrls.add(Integer.parseInt(url));
//        }
//        selectInQuestion(fUrls);
//        for (int i = 0; i < fFinalQuestions.size(); i++) {
//            System.out.println("读取结果 " + fFinalQuestions.get(i).getId());
//        }
    }

    private void genInSequence(Set<String> urls, List<String> sortedUrls, int start, int len, List<String> temp) {//len为组合的长度
        if (len == 0) {
            List<TF_IDF> result = new ArrayList<TF_IDF>();
            for (int i = 0; i < Arrays.asList(temp.get(0).split(Config.DELIMITER)).size(); i++) {
                TF_IDF tf_idf = new TF_IDF(Arrays.asList(temp.get(0).split(Config.DELIMITER)).get(i));
                result.add(tf_idf);
            }
//            result.addAll(Arrays.asList(temp.get(0).split(Config.DELIMITER)));
            for (int i = 1; i < temp.size(); i++) {
                List<TF_IDF> result2 = new ArrayList<TF_IDF>();//后面的存到一个数组里，与result求交
                for (int j = 0; j < Arrays.asList(temp.get(i).split(Config.DELIMITER)).size(); j++) {
                    TF_IDF tf_idf = new TF_IDF(Arrays.asList(temp.get(i).split(Config.DELIMITER)).get(j));
                    result2.add(tf_idf);
                }
                result.retainAll(result2);
            }
            Collections.sort(result);
            System.out.println(result.toString());
            for (int i = 0; i < result.size(); i++) {
                urls.add(result.get(i).getUrl());
            }
//            urls.addAll(result);
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

    class TF_IDF implements Comparable<TF_IDF>{
        private double TF;
        private double IDF;
        private String url;

        public String getUrl() {
            return url;
        }

        TF_IDF(String str) {
            this.url = str.split(",")[0];
            this.TF = Double.parseDouble(str.split(",")[2]);
            this.IDF = Double.parseDouble(str.split(",")[3]);
        }

        @Override
        public String toString() {
            return url.toString();
        }

        @Override
        public boolean equals(Object obj) {
            return url.equals(((TF_IDF) obj).getUrl());
        }

        @Override
        public int hashCode() {
            return url.hashCode();
        }

        //从小到大？？？？？
        @Override
        public int compareTo(TF_IDF object) {
            String str1 = String.valueOf(TF * IDF);
            String str2 = String.valueOf(object.TF * object.IDF);
            return -str1.compareTo(str2);
        }
    }

}
