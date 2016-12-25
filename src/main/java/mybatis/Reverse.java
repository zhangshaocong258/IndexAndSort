package mybatis;

/**
 * Created by zsc on 2016/12/22.
 * urls添加所有包含此关键词的序号
 */
public class Reverse {
    private int id;
    private String keyWords;
    private String IDF;
    private String urls;

    public Reverse() {}

//    public Reverse(String keyWords, String urls) {
//        this.keyWords = keyWords;
//        this.urls = urls;
//    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }

    public String getIDF() {
        return IDF;
    }

    public void setIDF(String IDF) {
        this.IDF = IDF;
    }

    public String getUrls() {
        return urls;
    }

    public void setUrls(String urls) {
        this.urls = urls;
    }
}
