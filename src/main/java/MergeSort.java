import mybatis.Forward;
import mybatis.QuestionForwardDao;
import mybatis.Reverse;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.*;
import java.util.*;

/**
 * Created by zsc on 2017/7/2.
 * 归并法构建索引
 */
public class MergeSort {

    private static String toBeSorted = "E:\\sort\\toBeSorted";
    private static String sorted = "E:\\sort\\sorted";
    private static String finalPath = "E:\\sort\\finalPath";
    private static String DELIMITER = "\t";

    private static List<Forward> fQuestions;
    private static Map<String, TreeSet<Forward>> questionsMap = new HashMap<String, TreeSet<Forward>>();

    public static void main(String args[])  throws IOException{
        genToBeSorted();
    }



    private static void genToBeSorted() throws IOException{
        for (int i = 0; i < 4; i++) {
            fQuestions = selectQuestion(5000 * i, 5000);
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
            File file = new File(toBeSorted + File.separator + i + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            int size = questionsMap.size();
            for (Map.Entry<String, TreeSet<Forward>> entry : questionsMap.entrySet()) {
                String keyWords = entry.getKey();
                String IDF = String.format("%.2f", Math.log((double) (size / entry.getValue().size())) / Math.log(2));
                StringBuilder stringBuilder = new StringBuilder();
                StringBuilder stringBuilder2 = new StringBuilder();
                for (Forward forward : entry.getValue()) {
                    stringBuilder.append(forward.getId()).append(Config.DELIMITER);
                    stringBuilder2.append(forward.getQuality()).append(",").append(forward.getId()).append(Config.DELIMITER);
                }
                String pageID = stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(Config.DELIMITER));
                String qualityAndPID = stringBuilder2.toString().substring(0, stringBuilder2.toString().lastIndexOf(Config.DELIMITER));
                StringBuilder sb = new StringBuilder();
                sb.append(keyWords).append(DELIMITER).append(IDF).append(DELIMITER).append(pageID).append(DELIMITER).append(qualityAndPID);
                bw.write(sb.toString());
                bw.newLine();
            }
            bw.close();
        }


    }

    private static List<Forward> selectQuestion(int offset, int limit) {
        SqlSession sqlSession = getSessionFactory().openSession();
        QuestionForwardDao forwardDao = sqlSession.getMapper(QuestionForwardDao.class);
        return forwardDao.selectByLimit(offset, limit);
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
