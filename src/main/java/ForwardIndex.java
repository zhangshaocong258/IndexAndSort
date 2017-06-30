import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import mybatis.*;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zsc on 2016/12/17.
 * 建立正向排序表，分词前要将专用名词存入字典，人名、组织名、收藏夹、书、话题，分词只针对问题和圆桌
 */
public class ForwardIndex {
    private List<Forward> question;
    private List<Forward> people;
    private List<Forward> topic;
//    private List<Forward> collection;

    private Set<Forward> questionSet = new HashSet<Forward>();
    private Set<Forward> peopleSet = new HashSet<Forward>();
    private Set<Forward> topicSet = new HashSet<Forward>();
    private Set<Forward> collectionSet = new HashSet<Forward>();

    private List<File> fileList = new ArrayList<File>();
    private int count = 0;


    public static void main(String args[]) {
        ForwardIndex forwardIndex = new ForwardIndex();
        forwardIndex.genFileList(Config.path);
        forwardIndex.genForwardIndex();
        forwardIndex.insertListToDB();
//        HanLP.Config.ShowTermNature = true;
//        System.out.println(HanLP.segment(""));
    }

    private static List<Term> filter(List<Term> termList) {
        List<Term> temp = new ArrayList<Term>();
        for (int i = 0; i < termList.size(); i++) {
            Term term = termList.get(i);
            String nature = term.nature != null ? term.nature.toString() : "空";
            char firstChar = nature.charAt(0);
            switch (firstChar) {
                case '1': //自定义
                    break;
                case 'b': //区别词 正 副
                case 'z': //状态词

                case 'm':
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
                case 'r': //代词 怎样 如何
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
                    if (term.word.length() == 1) {//长度为1，删除，可以理解为没有分出来词，因此删除，最后查询时分出的词，也可以删除停用词
                        temp.add(term);
                    }
            }
        }
        termList.removeAll(temp);
        return termList;
    }

    private void genFileList(String path) {
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

    private void genForwardIndex() {
        System.out.println(fileList.size());
        for (File file : fileList) {
            operateDoc(file);
            count++;
            if (count % 10000 == 0) {
                System.out.println("count " + count);
            }
        }
        this.question = new ArrayList<Forward>(questionSet);
        this.people = new ArrayList<Forward>(peopleSet);
        this.topic = new ArrayList<Forward>(topicSet);
    }

    private void operateDoc(File file) {
        try {
            String fileName = file.getName();
//            System.out.println(file.getName());
            Pattern question = Pattern.compile("^(www.zhihu.com_question_(.*))");
            Pattern collection = Pattern.compile("^(www.zhihu.com_collection_(.*))");
            Pattern topic = Pattern.compile("^(www.zhihu.com_topic_(.*))");
            Pattern people = Pattern.compile("^(www.zhihu.com_people_(.*))");
//            Pattern org = Pattern.compile("^(www.zhihu.com_org_(.*))");
//            Pattern roundTable = Pattern.compile("^(www.zhihu.com_roundtable_(.*))");
//            Pattern publications = Pattern.compile("^(www.zhihu.com_publications_(.*))");

            Matcher questionMatcher = question.matcher(fileName);
            Matcher collectionMatcher = collection.matcher(fileName);
            Matcher topicMatcher = topic.matcher(fileName);
            Matcher peopleMatcher = people.matcher(fileName);
//            Matcher orgMatcher = org.matcher(fileName);
//            Matcher roundTableMatcher = roundTable.matcher(fileName);
//            Matcher publicationsMatcher = publications.matcher(fileName);

            Document doc = Jsoup.parse(file, "UTF-8");
            String title = doc.title().trim().toLowerCase();
            if (title.trim().equals("")) {
                System.out.println("标题为空");
                return;//标题不能为空
            }
            String url = doc.select("url").first().text();
            String description;
            int quality = 1;
            int quality2 = 1;
            String keyWords;
            String TF;
            String quality_str;

            //格式不匹配的直接丢弃
            if (questionMatcher.matches()) {
                /*if (!(quality_str = doc.select("div.NumberBoard-value").text()).equals("")) {
                    quality1 += Integer.valueOf(quality_str.split(" ")[0]);//问题关注数
                    quality2 += Integer.valueOf(quality_str.split(" ")[1]);//问题被浏览
                }
                quality1 += Integer.valueOf(doc.select("div.QuestionHeader").first().select("meta[itemprop=followerCount]").attr("content"));//问题关注数
                quality2 += Integer.valueOf(doc.select("div.QuestionHeader").first().select("meta[itemprop=visitsCount]").attr("content"));//问题被浏览
                */

                try {
                    //老页面
                    if (!(quality_str = doc.select("div.zh-question-followers-sidebar").select("strong").text()).equals("")) {
                        quality = Integer.valueOf(quality_str);//问题关注数（Jsoup）
                    }

                } catch (NullPointerException e) {
                    //新页面
                    if (!(quality_str = doc.select("div.QuestionHeader").first().select("meta[itemprop=followerCount]").attr("content")).equals("")) {
                        quality += Integer.valueOf(quality_str);//问题关注数
                    }
//                    e.printStackTrace();
                }
                //关键词和搜索分词算法统一
                title = title.substring(0, title.length() - 5);
                keyWords = filter(HanLP.segment(title.trim())).toString();
                keyWords = keyWords.substring(1, keyWords.length() - 1);
                if (keyWords.trim().equals("")) {
                    System.out.println("question " + title);
                    return;
                }

                try {
                    //老问题描述 点赞+用户名+描述
                    description = doc.select("div.zm-item-vote-info").first().attr("data-votecount") + Config.DELIMITER +
                            doc.select("div.zm-item-answer-author-info").first().select("a.author-link").text() + Config.DELIMITER +
                            doc.select("div.zm-editable-content").select("div.clearfix").text().substring(0, doc.select("div.zm-editable-content").select("div.clearfix").text().length()
                                    > 300 ? 300 : doc.select("div.zm-editable-content").select("div.clearfix").first().text().length()) + "...";
                } catch (NullPointerException e) {
                    //新问题描述 点赞+用户名+描述
                    description = doc.select("meta[itemprop=upvoteCount]").first().attr("content") + Config.DELIMITER +
                            doc.select("div.AuthorInfo-content").first().select("a.UserLink-link").text() + Config.DELIMITER +
                            doc.select("div.RichContent-inner").first().text().substring(0, doc.select("div.RichContent-inner").first().text().length()
                                    > 300 ? 300 : doc.select("div.RichContent-inner").first().text().length()) + "...";
                }

                TF = String.format("%.2f", (double) 1 / keyWords.split(",").length);
                this.questionSet.add(new Forward(title, url, description, quality, keyWords, TF));
            } else if (topicMatcher.matches()) {
                if (!(quality_str = doc.select("div.zm-topic-side-followers-info").select("strong").text()).equals("")) {
                    quality += Integer.valueOf(quality_str);//话题关注者
                } else {
                    return;
                }
                title = title.substring(0, title.length() - 12);
                keyWords = filter(HanLP.segment(title.trim())).toString();//关键词没有收录，分词结果为空
                keyWords = keyWords.substring(1, keyWords.length() - 1);
                if (keyWords.trim().equals("")) {
                    System.out.println("topic " + title);
                    return;
                }
                if (!(description = doc.select("div.zm-editable-content").text()).equals("")) {
                    if (doc.select("div.zm-editable-content").select("a[href=javascript:;]").text().equals("修改")) {
                        description = description.substring(0, description.length() - 2);
                    }
                } else {
                    description = "";
                }
                this.topicSet.add(new Forward(title, url, description, quality, keyWords));
//                System.out.println("topic " + quality);
            } else if (peopleMatcher.matches()) {
                if (!(quality_str = doc.select("div.ProfileHeader").first().select("meta[itemprop=followerCount]").attr("content")).equals("")) {
                    quality += Integer.valueOf(quality_str);
                } else {
                    return;
                }
                title = title.substring(0, title.length() - 5);
                description = doc.select("div.ProfileHeader-contentHead").select("span").get(1).text();
                keyWords = filter(HanLP.segment(doc.select("div.ProfileHeader-contentHead").text())).toString();
                keyWords = keyWords.substring(1, keyWords.length() - 1);
                if (keyWords.trim().equals("")) {
                    System.out.println("people " + title);
                    return;
                }
                TF = String.format("%.2f", (double) 1 / keyWords.split(",").length);
                this.peopleSet.add(new Forward(title, url, description, quality, keyWords, TF));
//                System.out.println("people " + quality);
            }
            /*else if (orgMatcher.matches()) {
                quality = Integer.valueOf(doc.select("a[href~=(.*)followers]").select("div.Profile-followStatusValue").text().equals("") ?
                        doc.select("a[href~=(.*)followers]").select("div.NumberBoard-value").text() :
                        doc.select("a[href~=(.*)followers]").select("div.Profile-followStatusValue").text());//关注者
                keyWords = title;
                this.people.add(new Forward(title, url, description, quality, keyWords));
                System.out.println("org " + quality);
            } else if (collectionMatcher.matches()) {
                if (!(quality_str = doc.select("a[data-za-l=collection_followers_count]").text()).equals("")) {
                    quality1 = Integer.valueOf(quality1);//收藏关注数
                }
                keyWords = description = null;
                this.collectionSet.add(new Forward(title, url, description, quality1, quality2, keyWords));
                System.out.println("collection " + quality);
            }
            if (quality1 == -1) {
                throw new NullPointerException("获取特征失败 " + fileName);
            }*/
        } catch (NullPointerException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String ListToString(List<String> list) {
        return list.toString().substring(1, list.toString().length() - 1).toLowerCase();
    }

    public void insertListToDB() {
        if (question.size() > 0) {
            insertAllQuestion(question);
        }

        if (people.size() > 0) {
            insertAllPeople(people);
        }

        if (topic.size() > 0) {
            insertAllTopic(topic);

        }
//        if (collection.size() > 0) {
//            insertAllCollection(collection);
//        }

    }

    public void insertToDB() {
        if (question.size() > 0) {
            for (Forward content : question) {
                insertQuestion(content);
            }
        }

        if (people.size() > 0) {
            for (Forward content : people) {
                insertPeople(content);
            }
        }

        if (topic.size() > 0) {
            for (Forward content : topic) {
                insertTopic(content);
            }
        }
//        if (collection.size() > 0) {
//            for (Forward content : collection) {
//                insertCollection(content);
//            }
//        }

    }

    private void insertAllQuestion(List<Forward> content) {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSessionFactory().openSession();
            QuestionForwardDao forwardDao = sqlSession.getMapper(QuestionForwardDao.class);
            forwardDao.insertAll(content);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }

    private void insertAllPeople(List<Forward> content) {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSessionFactory().openSession();
            PeopleForwardDao forwardDao = sqlSession.getMapper(PeopleForwardDao.class);
            forwardDao.insertAll(content);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }

    private void insertAllTopic(List<Forward> content) {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSessionFactory().openSession();
            TopicForwardDao forwardDao = sqlSession.getMapper(TopicForwardDao.class);
            forwardDao.insertAll(content);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }

    private void insertAllCollection(List<Forward> content) {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSessionFactory().openSession();
            CollectionForwardDao forwardDao = sqlSession.getMapper(CollectionForwardDao.class);
            forwardDao.insertAll(content);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }

    private void insertQuestion(Forward content) {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSessionFactory().openSession();
            QuestionForwardDao forwardDao = sqlSession.getMapper(QuestionForwardDao.class);
            forwardDao.insert(content);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }

    private void insertPeople(Forward content) {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSessionFactory().openSession();
            PeopleForwardDao forwardDao = sqlSession.getMapper(PeopleForwardDao.class);
            forwardDao.insert(content);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }

    private void insertTopic(Forward content) {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSessionFactory().openSession();
            TopicForwardDao forwardDao = sqlSession.getMapper(TopicForwardDao.class);
            forwardDao.insert(content);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }

    private void insertCollection(Forward content) {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSessionFactory().openSession();
            CollectionForwardDao forwardDao = sqlSession.getMapper(CollectionForwardDao.class);
            forwardDao.insert(content);
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
