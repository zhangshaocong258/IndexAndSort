import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zsc on 2016/12/21.
 * 添加至数据库之前，首先检查是否报错，对于报错的页面，删除
 */
public class UrlFormatTest {

    public static void main(String args[]) throws IOException {
        UrlFormatTest urlFormatTest = new UrlFormatTest();
        urlFormatTest.checkFormat(Config.path);
    }

    private void checkFormat(String path) throws IOException{
        File folder = new File(path);
        if (folder.isFile()) {
            operateDoc(folder);
        } else if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    checkFormat(f.getAbsolutePath());
                } else {
                    operateDoc(f);
                }
            }
        }
    }

    private void operateDoc(File file) throws IOException{
        String fileName = file.getName();
        Pattern question = Pattern.compile("^(www.zhihu.com_question_(.*))");
        Pattern collection = Pattern.compile("^(www.zhihu.com_collection_(.*))");
        Pattern topic = Pattern.compile("^(www.zhihu.com_topic_(.*))");
        Pattern people = Pattern.compile("^(www.zhihu.com_people_(.*))");
        Pattern roundTable = Pattern.compile("^(www.zhihu.com_roundtable_(.*))");
        Pattern org = Pattern.compile("^(www.zhihu.com_org_(.*))");
        Pattern publications = Pattern.compile("^(www.zhihu.com_publications_(.*))");

        Matcher questionMatcher = question.matcher(fileName);
        Matcher collectionMatcher = collection.matcher(fileName);
        Matcher topicMatcher = topic.matcher(fileName);
        Matcher peopleMatcher = people.matcher(fileName);
        Matcher roundTableMatcher = roundTable.matcher(fileName);
        Matcher orgMatcher = org.matcher(fileName);
        Matcher publicationsMatcher = publications.matcher(fileName);

        Document doc = Jsoup.parse(file, "UTF-8");
        int quality = -1;
        if (questionMatcher.matches()) {
            String str = doc.select("div.zh-question-followers-sidebar").select("strong").text();
            if (str.trim().equals("")) {
                System.out.println("question " + fileName);
            }
        } else if (collectionMatcher.matches()) {
            String str = doc.select("a[data-za-l=collection_followers_count]").text();//收藏关注数
            if (str.trim().equals("")) {
                System.out.println("collection " + fileName);
            }
        } else if (topicMatcher.matches()) {
            String str = doc.select("div.zm-topic-side-followers-info").select("strong").text();//话题关注者
            if (str.trim().equals("")) {
                System.out.println("topic " + fileName);
            }
        } else if (peopleMatcher.matches()) {
            String str = doc.select("a[href~=(.*)followers]").select("div.Profile-followStatusValue").text();//关注者
            if (str.trim().equals("")) {
                System.out.println("people " + fileName);
            }
        } else if (roundTableMatcher.matches()) {
            String str = doc.select("a.followers").select("span.count").text();//关注者
            if (str.trim().equals("")) {
                System.out.println("roundTable " + fileName);
            }
        }
        if (quality == -1) {
            throw new NullPointerException("获取特征失败 " + fileName);
        }
    }

}
