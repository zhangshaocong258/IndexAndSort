import mybatis.Forward;
import mybatis.QuestionForwardDao;
import mybatis.Reverse;
import mybatis.TopicForwardDao;
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
    private static Map<String, TreeSet<Forward>> questionsMap;

    private static List<String> qList;
    private static List<File> fileList = new ArrayList<File>();

    public static void main(String args[]) throws IOException {
        genToBeSorted();
        genSorted();
    }

    private static void genReverseIndex() {

    }

    //将每个文件进行内部排序
    private static void genSorted() throws IOException {

        genFileList(toBeSorted);
        for (int i = 0; i < fileList.size(); i++) {
            qList = new ArrayList<String>();
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileList.get(i))));
            String string;
            while ((string = br.readLine()) != null) {
                qList.add(string);
            }
            Collections.sort(qList);
            File output = new File(sorted + File.separator + i + ".txt");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
            for (String str : qList) {
                bw.write(str);
                bw.newLine();
            }
            bw.close();
            br.close();
        }

    }

    //得到未排序的倒排文件
    private static void genToBeSorted() throws IOException {
        int count = getCount();
        System.out.println(count);
        for (int i = 0; i < 10; i++) {
            fQuestions = selectQuestion(2000 * i, 2000);
            questionsMap = new HashMap<String, TreeSet<Forward>>();
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
//                String IDF = String.format("%.2f", Math.log((double) (size / entry.getValue().size())) / Math.log(2));
                StringBuilder stringBuilder = new StringBuilder();
                StringBuilder stringBuilder2 = new StringBuilder();
                for (Forward forward : entry.getValue()) {
                    stringBuilder.append(forward.getId()).append(Config.DELIMITER);
                    stringBuilder2.append(forward.getQuality()).append(",").append(forward.getId()).append(Config.DELIMITER);
                }
                String pageID = stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(Config.DELIMITER));
                String qualityAndPID = stringBuilder2.toString().substring(0, stringBuilder2.toString().lastIndexOf(Config.DELIMITER));
                StringBuilder sb = new StringBuilder();
                sb.append(keyWords).append(DELIMITER).append(pageID).append(DELIMITER).append(qualityAndPID);
                bw.write(sb.toString());
                bw.newLine();
            }
            bw.close();
        }


    }

    private static void genFileList(String path) {
        File folder = new File(path);
        if (folder.isFile()) {
            fileList.add(folder);
        } else if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    genFileList(f.getAbsolutePath());
                } else {
                    fileList.add(f);
                }
            }
        }
    }

    private static int getCount() {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSessionFactory().openSession();
            QuestionForwardDao forwardDao = sqlSession.getMapper(QuestionForwardDao.class);
            return forwardDao.getCount();
        } finally {
            sqlSession.close();
        }
    }

    private static List<Forward> selectQuestion(int offset, int limit) {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSessionFactory().openSession();
            QuestionForwardDao forwardDao = sqlSession.getMapper(QuestionForwardDao.class);
            return forwardDao.selectByLimit(offset, limit);
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
