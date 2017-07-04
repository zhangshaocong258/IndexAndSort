import mybatis.*;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by zsc on 2017/7/2.
 * 归并法构建索引
 */
public class MergeSort {

    private static String toBeSorted = "E:\\SearchEngine\\toBeSorted";
    private static String sorted = "E:\\SearchEngine\\sorted";
    private static String finalPath = "E:\\SearchEngine\\finalPath\\final.txt";
    private static String DELIMITER = "\t";

    private static List<Forward> fQuestions;
    private static Map<String, TreeSet<Forward>> questionsMap;

    private static List<String> qList;
    private static List<File> fileList = new ArrayList<File>();
    private static int count;

    private static String minKey = "!";
    private static String maxKey = "龻";//unicode中找一个靠后的
    private static List<Reverse> rQuestionList = new ArrayList<Reverse>();


    public static void main(String args[]) throws IOException {
//        genToBeSorted();
//        genSorted();
//        genReverseIndex();
//        insertReverseIndex();
    }

    private static void insertReverseIndex() throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(finalPath))));
        String str;
        while ((str = br.readLine()) != null) {
            rQuestionList.add(new Reverse(str.split(Pattern.quote(DELIMITER))[0], str.split(Pattern.quote(DELIMITER))[3],
                    str.split(Pattern.quote(DELIMITER))[1], str.split(Pattern.quote(DELIMITER))[2]));
        }
        br.close();
        insertListQuestion(rQuestionList);
    }

    private static void genReverseIndex() throws IOException {
        fileList.clear();
        count = getCount();
        System.out.println(count);
        genFileList(sorted);
        multiWayMergeSort(fileList, finalPath);
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
            Collections.sort(qList, new Comparator<String>() {
                @Override
                public int compare(String str1, String str2) {
                    if (str1.split(Pattern.quote(DELIMITER))[0].compareTo(str2.split(Pattern.quote(DELIMITER))[0]) < 0) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });
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
            for (Map.Entry<String, TreeSet<Forward>> entry : questionsMap.entrySet()) {
                String keyWords = entry.getKey();
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

    private static String[] merge(String[] a, String[] b) {
        int left = a.length;
        int right = b.length;
        int i = 0, j = 0;
        String[] str = new String[2];
        StringBuilder qualityAndPID = new StringBuilder();
        StringBuilder PID = new StringBuilder();
        while (i < left && j < right) {
            //从大到小排序
            if (a[i].compareTo(b[j]) >= 1) {
                qualityAndPID.append(a[i]).append(Config.DELIMITER);
                PID.append(a[i].split(",")[1]).append(Config.DELIMITER);
                i++;
            } else {
                qualityAndPID.append(b[j]).append(Config.DELIMITER);
                PID.append(b[j].split(",")[1]).append(Config.DELIMITER);
                j++;
            }
        }
        while (i < left) {
            qualityAndPID.append(a[i]).append(Config.DELIMITER);
            PID.append(a[i].split(",")[1]).append(Config.DELIMITER);
            i++;

        }
        while (j < right) {
            qualityAndPID.append(b[j]).append(Config.DELIMITER);
            PID.append(b[j].split(",")[1]).append(Config.DELIMITER);
            j++;
        }
        str[0] = PID.toString().substring(0, PID.toString().length() - 1);
        str[1] = qualityAndPID.toString().substring(0, qualityAndPID.toString().length() - 1);

        return str;
    }

    private static void multiWayMergeSort(List<File> files, String output) throws IOException {
        int n = files.size();
        String[] b = new String[n + 1];

        List<BufferedReader> rList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(files.get(i))));
            rList.add(br);
        }

        for (int i = 0; i < n; i++) {
            String str;
            if ((str = rList.get(i).readLine()) != null) {
                b[i] = str;
            } else {
                b[i] = maxKey;
            }
        }

        int[] ls = new int[n];

        createLoserTree(ls, b, n);
//        for (int i = 0; i < b.length; i++) {
//            System.out.println(b[i]);
//        }
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(output))));
        String temp = null;
        while (b[ls[0]] != maxKey) {
            if (temp == null) {//判断第一次
                temp = b[ls[0]];
                String str;
                if ((str = rList.get(ls[0]).readLine()) != null) {
                    b[ls[0]] = str;
                } else {
                    b[ls[0]] = maxKey;
                }
                adjust(ls, b, n, ls[0]);
                continue;
            } else {
                String[] tempArray = temp.split(Pattern.quote(DELIMITER));
                String[] bArray = b[ls[0]].split(Pattern.quote(DELIMITER));
                if (tempArray[0].equals(bArray[0])) {
                    //两路归并
                    String[] col = merge(tempArray[2].split(Pattern.quote(Config.DELIMITER)), bArray[2].split(Pattern.quote(Config.DELIMITER)));
                    temp = new StringBuilder().append(tempArray[0]).append(DELIMITER).append(col[0]).append(DELIMITER).
                            append(col[1]).toString();
                    String str;
                    if ((str = rList.get(ls[0]).readLine()) != null) {
                        b[ls[0]] = str;
                    } else {
                        b[ls[0]] = maxKey;
                    }
                } else {
                    temp = new StringBuilder().append(temp).append(DELIMITER).
                            append(String.format("%.2f", Math.log((double) (count / (temp.split(Pattern.quote(DELIMITER))[1].split(Pattern.quote(Config.DELIMITER))).length)) / Math.log(2))).toString();
                    bw.write(temp);
                    bw.newLine();
                    bw.flush();
                    temp = b[ls[0]];//位置和下面颠倒了
                    String str;
                    if ((str = rList.get(ls[0]).readLine()) != null) {
                        b[ls[0]] = str;
                    } else {
                        b[ls[0]] = maxKey;
                    }
                }
            }
            adjust(ls, b, n, ls[0]);
        }
        //最后一个
        temp = new StringBuilder().append(temp).append(DELIMITER).
                append(String.format("%.2f", Math.log((double) (count / (temp.split(Pattern.quote(DELIMITER))[1].split(Pattern.quote(Config.DELIMITER))).length)) / Math.log(2))).toString();
        bw.write(temp);
        bw.flush();
        bw.close();
        for (BufferedReader br : rList) {
            br.close();
        }

    }

    private static void createLoserTree(int[] ls, String[] b, int n) {
        b[n] = minKey;
        for (int i = 0; i < n; i++) {
            ls[i] = n;
        }
        for (int i = n - 1; i >= 0; i--) {
            adjust(ls, b, n, i);
        }
    }

    private static void adjust(int[] ls, String[] b, int n, int s) {
        for (int t = (s + n) / 2; t > 0; t = t / 2) {//t=(s+k)，得到与之相连ls数组的索引
            if (b[s].split(Pattern.quote(DELIMITER))[0].compareTo(b[ls[t]].split(Pattern.quote(DELIMITER))[0]) >= 1) {//父亲节点
                int temp = s; //s永远是指向这一轮比赛最小节点
                s = ls[t];
                ls[t] = temp;
            }
        }
        ls[0] = s;//将最小节点的索引存储在ls[0]
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

    private static void insertListQuestion(List<Reverse> content) {
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
