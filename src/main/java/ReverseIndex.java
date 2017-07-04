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

    private Reverse rQuestion = new Reverse();
    private Reverse rPeople = new Reverse();
    private Reverse rTopic = new Reverse();

    private List<Reverse> rQuestionList = new ArrayList<Reverse>();
    private List<Reverse> rPeopleList = new ArrayList<Reverse>();
    private List<Reverse> rTopicList = new ArrayList<Reverse>();


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
        int size = fQuestions.size();
        for (Map.Entry<String, TreeSet<Forward>> entry : questionsMap.entrySet()) {
//            rQuestion.setKeyWords(entry.getKey());
//            rQuestion.setIDF(String.format("%.2f", Math.log((double) (size / entry.getValue().size())) / Math.log(2)));
            String keyWords = entry.getKey();
            String IDF = String.format("%.2f", Math.log((double) (size / entry.getValue().size())) / Math.log(2));
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringBuilder2 = new StringBuilder();
            //urls表示为89，tf，idf DELIMITER 88，tf，idf
            for (Forward forward : entry.getValue()) {
                stringBuilder.append(forward.getId()).append(Config.DELIMITER);
                stringBuilder2.append(forward.getQuality()).append(",").append(forward.getId()).append(Config.DELIMITER);
            }
//            rQuestion.setPageID(stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(Config.DELIMITER)));
            String pageID = stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(Config.DELIMITER));
            String qualityAndPID = stringBuilder2.toString().substring(0, stringBuilder2.toString().lastIndexOf(Config.DELIMITER));
            rQuestionList.add(new Reverse(keyWords, IDF, pageID, qualityAndPID));
//            insertQuestion(rQuestion);
        }
        insertListQuestion(rQuestionList);

    }

    private void genRPeoples() {
        selectAllPeoples();//得到people的正排索引
        for (Forward people : fPeoples) {
            List<String> keyWords = Arrays.asList(people.getKeyWords().split(", "));//间隔是逗号加空格！！！
            for (String keyWord : keyWords) {
                if (peoplesMap.containsKey(keyWord)) {
                    peoplesMap.get(keyWord).add(people);
                } else {
                    peoplesMap.put(keyWord, new TreeSet<Forward>());
                    peoplesMap.get(keyWord).add(people);
                }
            }
        }
        int size = fPeoples.size();
        for (Map.Entry<String, TreeSet<Forward>> entry : peoplesMap.entrySet()) {
//            rPeople.setKeyWords(entry.getKey());
//            rPeople.setIDF(String.format("%.2f", Math.log((double) (size / entry.getValue().size())) / Math.log(2)));
            String keyWords = entry.getKey();
            String IDF = String.format("%.2f", Math.log((double) (size / entry.getValue().size())) / Math.log(2));
//            System.out.println(IDF);
            StringBuilder stringBuilder = new StringBuilder();
            for (Forward forward : entry.getValue()) {
                stringBuilder.append(forward.getId()).append(Config.DELIMITER);
            }
//            rPeople.setPageID(stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(Config.DELIMITER)));
            String pageID = stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(Config.DELIMITER));

            rPeopleList.add(new Reverse(keyWords, IDF, pageID));
//            insertPeople(rPeople);
        }
        insertListPeople(rPeopleList);
    }

    private void genRTopics() {
        selectAllTopics();//得到people的正排索引
        for (Forward topic : fTopics) {
            List<String> keyWords = Arrays.asList(topic.getKeyWords().split(", "));//间隔是逗号加空格！！！
            for (String keyWord : keyWords) {
                if (topicsMap.containsKey(keyWord)) {
                    topicsMap.get(keyWord).add(topic);
                } else {
                    topicsMap.put(keyWord, new TreeSet<Forward>());
                    topicsMap.get(keyWord).add(topic);
                }
            }
        }
        int size = fTopics.size();
        for (Map.Entry<String, TreeSet<Forward>> entry : topicsMap.entrySet()) {
//            rTopic.setKeyWords(entry.getKey());
//            rTopic.setIDF(String.format("%.2f", Math.log((double) (size / entry.getValue().size())) / Math.log(2)));
            String keyWords = entry.getKey();
            String IDF = String.format("%.2f", Math.log((double) (size / entry.getValue().size())) / Math.log(2));
            StringBuilder stringBuilder = new StringBuilder();
            for (Forward forward : entry.getValue()) {
                stringBuilder.append(forward.getId()).append(Config.DELIMITER);
            }
//            rTopic.setPageID(stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(Config.DELIMITER)));
            String pageID = stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(Config.DELIMITER));
            rTopicList.add(new Reverse(keyWords, IDF, pageID));
//            insertTopic(rTopic);
        }
        insertListTopic(rTopicList);
    }


    private void insertListQuestion(List<Reverse> content) {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSessionFactory().openSession();
            QuestionReverseDao reverseDao = sqlSession.getMapper(QuestionReverseDao.class);
            reverseDao.insertAll(content);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }

    private void insertListPeople(List<Reverse> content) {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSessionFactory().openSession();
            PeopleReverseDao reverseDao = sqlSession.getMapper(PeopleReverseDao.class);
            reverseDao.insertAll(content);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }

    private void insertListTopic(List<Reverse> content) {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSessionFactory().openSession();
            TopicReverseDao reverseDao = sqlSession.getMapper(TopicReverseDao.class);
            reverseDao.insertAll(content);
            sqlSession.commit();
        } finally {
            sqlSession.close();
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
        SqlSession sqlSession = null;
        try {
            sqlSession = getSessionFactory().openSession();
            QuestionForwardDao forwardDao = sqlSession.getMapper(QuestionForwardDao.class);
            fQuestions = forwardDao.selectAll();
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }

    private void selectAllPeoples() {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSessionFactory().openSession();
            PeopleForwardDao forwardDao = sqlSession.getMapper(PeopleForwardDao.class);
            fPeoples = forwardDao.selectAll();
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }

    private void selectAllTopics() {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSessionFactory().openSession();
            TopicForwardDao forwardDao = sqlSession.getMapper(TopicForwardDao.class);
            fTopics = forwardDao.selectAll();
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
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
