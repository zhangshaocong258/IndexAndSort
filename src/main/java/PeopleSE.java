import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import mybatis.*;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.util.*;

/**
 * Created by zsc on 2016/12/26.
 */
public class PeopleSE {
    private List<Forward> fPeoples;
    private List<Reverse> rPeoples;
    private Map<String, Reverse> rPeoplesMap = new HashMap<String, Reverse>();
    private Map<String, Forward> fPeoplesForward = new HashMap<String, Forward>();

    public static void main(String args[]) {
        PeopleSE peopleSE = new PeopleSE();
        peopleSE.readAllMaps();
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入字符串：");
        peopleSE.genUrls(scanner.nextLine());

    }
    //将正排和倒排数据读到内存中，保存在map，初始化数据
    private void readAllMaps() {
        selectAllPeople();//将数据库的问题正排和倒排读进来
        //将2个表读到Map里
        for (Reverse people : rPeoples) {
            rPeoplesMap.put(people.getKeyWords(), people);
        }

        for (Forward people : fPeoples) {
            fPeoplesForward.put(String.valueOf(people.getId()), people);
        }

    }

    private void genUrls(String str) {
        //得到按序排列的关键字集合
        List<Term> terms = Filter.accept(StandardTokenizer.segment(str));
        List<Reverse> keyWords = new ArrayList<Reverse>();
        for (Term term : terms) {
            for (Map.Entry<String, Reverse> entry : rPeoplesMap.entrySet()) {
                if (entry.getKey().contains(term.word)) {
                    keyWords.add(entry.getValue());
                }
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

    private void selectAllPeople() {
        SqlSession sqlSession = getSessionFactory().openSession();
        PeopleReverseDao reverseDao = sqlSession.getMapper(PeopleReverseDao.class);
        PeopleForwardDao forwardDao = sqlSession.getMapper(PeopleForwardDao.class);
        rPeoples = reverseDao.selectAll();
        fPeoples = forwardDao.selectAll();
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
