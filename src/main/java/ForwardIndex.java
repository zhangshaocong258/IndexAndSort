import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import mybatis.*;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zsc on 2016/12/17.
 * 建立正向排序表，分词前要将专用名词存入字典，人名、组织名、收藏夹、书、话题，分词只针对问题和圆桌
 */
public class ForwardIndex {
    private List<Forward> question = new ArrayList<Forward>();
    private List<Forward> people = new ArrayList<Forward>();
    private List<Forward> topic = new ArrayList<Forward>();
    private List<Forward> collection = new ArrayList<Forward>();

    public static void main(String args[]) throws IOException{
        ForwardIndex forwardIndex = new ForwardIndex();
        forwardIndex.genForwardIndex(Config.path);
        forwardIndex.insertToDB();
    }

    private void genForwardIndex(String path) throws IOException{
        File folder = new File(path);
        if (folder.isFile()) {
            operateDoc(folder);
        } else if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    genForwardIndex(f.getAbsolutePath());
                } else {
                    operateDoc(f);
                }
            }
        }
    }

    private void operateDoc(File file) throws IOException{
        String fileName = file.getName();
        System.out.println(file.getName());
        Pattern question = Pattern.compile("^(www.zhihu.com_question_(.*))");
        Pattern collection = Pattern.compile("^(www.zhihu.com_collection_(.*))");
        Pattern topic = Pattern.compile("^(www.zhihu.com_topic_(.*))");
        Pattern people = Pattern.compile("^(www.zhihu.com_people_(.*))");
        Pattern org = Pattern.compile("^(www.zhihu.com_org_(.*))");
//        Pattern roundTable = Pattern.compile("^(www.zhihu.com_roundtable_(.*))");
//        Pattern publications = Pattern.compile("^(www.zhihu.com_publications_(.*))");

        Matcher questionMatcher = question.matcher(fileName);
        Matcher collectionMatcher = collection.matcher(fileName);
        Matcher topicMatcher = topic.matcher(fileName);
        Matcher peopleMatcher = people.matcher(fileName);
        Matcher orgMatcher = org.matcher(fileName);
//        Matcher roundTableMatcher = roundTable.matcher(fileName);
//        Matcher publicationsMatcher = publications.matcher(fileName);
        
        Document doc = Jsoup.parse(file, "UTF-8");
        String title = doc.title().split("-")[0].trim();
        if (title.equals("")) {
            return;//标题不能为空
        }
        System.out.println(title);
        String url = doc.select("url").first().text();
        String description = title;
        int quality = -1;
        List<String> keyWordsTerm = null;
        String keyWords = null;
        if (questionMatcher.matches()) {
            quality = Integer.valueOf(doc.select("div.zh-question-followers-sidebar").select("strong").text());//问题关注数（Jsoup）
            keyWordsTerm = HanLP.extractKeyword(title.split("-")[0].trim(), 10);
            keyWords = ListToString(keyWordsTerm);
            this.question.add(new Forward(title, url, description, quality, keyWords));
            System.out.println("question " + quality);
        } else if (collectionMatcher.matches()) {
            quality = Integer.valueOf(doc.select("a[data-za-l=collection_followers_count]").text());//收藏关注数
            keyWords = title;
            this.collection.add(new Forward(title, url, description, quality, keyWords));
            System.out.println("collection " + quality);
        } else if (topicMatcher.matches()) {
            quality = Integer.valueOf(doc.select("div.zm-topic-side-followers-info").select("strong").text());//话题关注者
            keyWords = title;
            this.topic.add(new Forward(title, url, description, quality, keyWords));
            System.out.println("topic " + quality);
        } else if (peopleMatcher.matches()) {
            quality = Integer.valueOf(doc.select("a[href~=(.*)followers]").select("div.Profile-followStatusValue").text());//关注者
            if (quality < 100000) {
                quality = 100000;
            }
            keyWords = title;
            this.people.add(new Forward(title, url, description, quality, keyWords));
            System.out.println("people " + quality);
        } else if (orgMatcher.matches()) {
            quality = 100000;//关注者
            keyWords = title;
            this.people.add(new Forward(title, url, description, quality, keyWords));
            System.out.println("org " + quality);
        }
        if (quality == -1 || keyWords == null) {
            throw new NullPointerException("获取特征失败 " + fileName);
        }

    }

    private String  ListToString(List<String> list) {
        return list.toString().substring(1, list.toString().length() - 1);
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
        if (collection.size() > 0) {
            for (Forward content : collection) {
                insertCollection(content);
            }
        }

    }

    private void insertQuestion(Forward content) {
        SqlSession sqlSession = getSessionFactory().openSession();
        QuestionForwardDao forwardMapper = sqlSession.getMapper(QuestionForwardDao.class);
        forwardMapper.insert(content);
        sqlSession.commit();
    }

    private void insertPeople(Forward content) {
        SqlSession sqlSession = getSessionFactory().openSession();
        PeopleForwardDao forwardMapper = sqlSession.getMapper(PeopleForwardDao.class);
        forwardMapper.insert(content);
        sqlSession.commit();
    }

    private void insertTopic(Forward content) {
        SqlSession sqlSession = getSessionFactory().openSession();
        TopicForwardDao forwardMapper = sqlSession.getMapper(TopicForwardDao.class);
        forwardMapper.insert(content);
        sqlSession.commit();
    }

    private void insertCollection(Forward content) {
        SqlSession sqlSession = getSessionFactory().openSession();
        CollectionForwardDao forwardMapper = sqlSession.getMapper(CollectionForwardDao.class);
        forwardMapper.insert(content);
        sqlSession.commit();
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

    Filter filter = new Filter() {
        @Override
        public List<Term> accept(List<Term> termList) {
            List<Term> temp = new ArrayList<Term>();
            for (int i = 0; i < termList.size(); i++) {
                Term term = termList.get(i);
                String nature = term.nature != null ? term.nature.toString() : "空";
                char firstChar = nature.charAt(0);
                switch(firstChar) {
                    case 'b': //区别词 正 副
                    case 'z': //状态词
                    case 'r': //代词 怎样 如何
                    case 'm':
                        break;
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
                        if(term.word.length() == 1) {//长度为1，删除，可以理解为没有分出来词，因此删除，最后查询时分出的词，也可以删除停用词
                            temp.add(term);
                        }
                }
            }
            termList.removeAll(temp);
            return termList;
        }
    };
}
