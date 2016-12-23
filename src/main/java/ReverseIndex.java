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
    private String DELIMITER = "\r\n";

    private Map<String, StringBuilder> questionsMap = new HashMap<String, StringBuilder>();
    private Map<String, StringBuilder> peoplesMap = new HashMap<String, StringBuilder>();
    private Map<String, StringBuilder> topicsMap = new HashMap<String, StringBuilder>();

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
            List<String> keyWords = Arrays.asList(question.getKeyWords().split(", "));//间隔是都和加空格！！！
            for (String keyWord : keyWords) {
                if (questionsMap.containsKey(keyWord)) {
                    questionsMap.get(keyWord).append(DELIMITER).append(question.getId());
                } else {
                    questionsMap.put(keyWord, new StringBuilder().append(question.getId()));
                }
            }
        }
        for (Map.Entry<String, StringBuilder> entry : questionsMap.entrySet()) {
            rQuestions.setKeyWords(entry.getKey());
            rQuestions.setUrls(entry.getValue().toString());
            insertQuestion(rQuestions);
            System.out.println(entry.getKey() + "   " + entry.getValue().toString());
        }


    }

    private void genRPeoples() {
        selectAllPeoples();//得到people的正排索引
        for (Forward people : fPeoples) {
            if (peoplesMap.containsKey(people.getKeyWords())) {
                peoplesMap.get(people.getKeyWords()).append(DELIMITER).append(people.getId());
            } else {
                peoplesMap.put(people.getKeyWords(), new StringBuilder().append(people.getId()));
            }
        }

        for (Map.Entry<String, StringBuilder> entry : peoplesMap.entrySet()) {
            rPeoples.setKeyWords(entry.getKey());
            rPeoples.setUrls(entry.getValue().toString());
            insertPeople(rPeoples);
            System.out.println(entry.getKey() + "   " + entry.getValue().toString());
        }
    }

    private void genRTopics() {
        selectAllTopics();//得到people的正排索引
        for (Forward topic : fTopics) {
            if (topicsMap.containsKey(topic.getTitle())) {
                topicsMap.get(topic.getTitle()).append(DELIMITER).append(topic.getId());
            } else {
                topicsMap.put(topic.getTitle(), new StringBuilder().append(topic.getId()));
            }
        }

        for (Map.Entry<String, StringBuilder> entry : topicsMap.entrySet()) {
            rTopics.setKeyWords(entry.getKey());
            rTopics.setUrls(entry.getValue().toString());
            insertTopic(rTopics);
            System.out.println(entry.getKey() + "   " + entry.getValue().toString());
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
        QuestionForwardDao forwardMapper = sqlSession.getMapper(QuestionForwardDao.class);
        fQuestions = forwardMapper.selectAll();
    }

    private void selectAllPeoples() {
        SqlSession sqlSession = getSessionFactory().openSession();
        PeopleForwardDao forwardMapper = sqlSession.getMapper(PeopleForwardDao.class);
        fPeoples = forwardMapper.selectAll();
    }

    private void selectAllTopics() {
        SqlSession sqlSession = getSessionFactory().openSession();
        TopicForwardDao forwardMapper = sqlSession.getMapper(TopicForwardDao.class);
        fTopics = forwardMapper.selectAll();
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
