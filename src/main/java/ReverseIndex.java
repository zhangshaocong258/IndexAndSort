import mybatis.*;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.util.*;

/**
 * Created by zsc on 2016/12/17.
 * 建立倒排索引
 */
public class ReverseIndex {
    private List<Forward> fQuestions;
    private List<Forward> fPeoples;
    private List<Forward> fTopics;

    private Map<String, TreeSet<Forward>> questionsMap = new HashMap<String, TreeSet<Forward>>();
    private Map<String, TreeSet<Forward>> peoplesMap = new HashMap<String, TreeSet<Forward>>();
    private Map<String, TreeSet<Forward>> topicsMap = new HashMap<String, TreeSet<Forward>>();

    private Reverse rQuestions = new Reverse();
    private Reverse rPeoples = new Reverse();
    private Reverse rTopics = new Reverse();


    public static void main(String args[]) {
        ReverseIndex reverseIndex = new ReverseIndex();
        reverseIndex.genRQuestions();
        reverseIndex.genRPeoples();
        reverseIndex.genRTopics();

    }

    private void genRQuestions() {
        selectAllQuestion();
        for (Forward question : fQuestions) {
            List<String> keyWords = Arrays.asList(question.getKeyWords().split(", "));//间隔是逗号加空格！！！
            for (String keyWord : keyWords) {
                if (questionsMap.containsKey(keyWord)) {
                    questionsMap.get(keyWord).add(question);
                } else {
                    questionsMap.put(keyWord, new TreeSet<Forward>());
                    questionsMap.get(keyWord).add(question);

                }
            }
        }
        int size = questionsMap.size();
        for (Map.Entry<String, TreeSet<Forward>> entry : questionsMap.entrySet()) {
            rQuestions.setKeyWords(entry.getKey());
            rQuestions.setIDF(String.format("%.2f", Math.log((double) (size / entry.getValue().size())) / Math.log(2)));
            StringBuilder stringBuilder = new StringBuilder();
            //urls表示为89，阿里巴巴，0.33DELIMITER88，阿里巴巴，0.25
            for (Forward forward : entry.getValue()) {
                stringBuilder.append(forward.getId()).append(",").append(entry.getKey()).append(",").
                        append(forward.getTF()).append(",").append(rQuestions.getIDF()).append(Config.DELIMITER);
            }
            rQuestions.setUrls(stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(Config.DELIMITER)));
            insertQuestion(rQuestions);
        }


    }

    private void genRPeoples() {
        selectAllPeoples();//得到people的正排索引
        for (Forward people : fPeoples) {
            if (peoplesMap.containsKey(people.getKeyWords())) {
                peoplesMap.get(people.getKeyWords()).add(people);
            } else {
                peoplesMap.put(people.getKeyWords(), new TreeSet<Forward>());
                peoplesMap.get(people.getKeyWords()).add(people);
            }
        }
        int size = peoplesMap.size();
        for (Map.Entry<String, TreeSet<Forward>> entry : peoplesMap.entrySet()) {
            rPeoples.setKeyWords(entry.getKey());
            rPeoples.setIDF(String.format("%.2f", Math.log((double) (size / entry.getValue().size())) / Math.log(2)));
            StringBuilder stringBuilder = new StringBuilder();
            for (Forward forward : entry.getValue()) {
                stringBuilder.append(forward.getId()).append(Config.DELIMITER);
            }
            rPeoples.setUrls(stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(Config.DELIMITER)));
            insertPeople(rPeoples);
        }
    }

    private void genRTopics() {
        selectAllTopics();//得到people的正排索引
        for (Forward topic : fTopics) {
            if (topicsMap.containsKey(topic.getTitle())) {
                topicsMap.get(topic.getTitle()).add(topic);
            } else {
                topicsMap.put(topic.getTitle(), new TreeSet<Forward>());
                topicsMap.get(topic.getTitle()).add(topic);
            }
        }
        int size = topicsMap.size();
        for (Map.Entry<String, TreeSet<Forward>> entry : topicsMap.entrySet()) {
            rTopics.setKeyWords(entry.getKey());
            rTopics.setIDF(String.format("%.2f", Math.log((double) (size / entry.getValue().size())) / Math.log(2)));
            StringBuilder stringBuilder = new StringBuilder();
            for (Forward forward : entry.getValue()) {
                stringBuilder.append(forward.getId()).append(Config.DELIMITER);
            }
            rTopics.setUrls(stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(Config.DELIMITER)));
            insertTopic(rTopics);
        }
    }


    private void insertQuestion(Reverse content) {
        SqlSession sqlSession = getSessionFactory().openSession();
        QuestionReverseDao reverseDao = sqlSession.getMapper(QuestionReverseDao.class);
        reverseDao.insert(content);
        sqlSession.commit();
    }

    private void insertPeople(Reverse content) {
        SqlSession sqlSession = getSessionFactory().openSession();
        PeopleReverseDao reverseDao = sqlSession.getMapper(PeopleReverseDao.class);
        reverseDao.insert(content);
        sqlSession.commit();
    }

    private void insertTopic(Reverse content) {
        SqlSession sqlSession = getSessionFactory().openSession();
        TopicReverseDao reverseDao = sqlSession.getMapper(TopicReverseDao.class);
        reverseDao.insert(content);
        sqlSession.commit();
    }

//    private void insertCollection(Reverse content) {
//        SqlSession sqlSession = getSessionFactory().openSession();
//        CollectionReverseDao reverseDao = sqlSession.getMapper(CollectionReverseDao.class);
//        reverseDao.insert(content);
//        sqlSession.commit();
//    }



    private void selectAllQuestion() {
        SqlSession sqlSession = getSessionFactory().openSession();
        QuestionForwardDao forwardDao = sqlSession.getMapper(QuestionForwardDao.class);
        fQuestions = forwardDao.selectAll();
    }

    private void selectAllPeoples() {
        SqlSession sqlSession = getSessionFactory().openSession();
        PeopleForwardDao forwardDao = sqlSession.getMapper(PeopleForwardDao.class);
        fPeoples = forwardDao.selectAll();
    }

    private void selectAllTopics() {
        SqlSession sqlSession = getSessionFactory().openSession();
        TopicForwardDao forwardDao = sqlSession.getMapper(TopicForwardDao.class);
        fTopics = forwardDao.selectAll();
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
