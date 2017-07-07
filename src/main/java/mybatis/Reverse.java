package mybatis;

/**
 * Created by zsc on 2016/12/22.
 * urls添加所有包含此关键词的序号
 */
public class Reverse implements Comparable<Reverse> {
    private int id;
    private String keyWords;
    private String IDF;
    private String pageID;//多个，中间用DELIMITER隔开
    private String TFIDF;//多个，中间用DELIMITER隔开
    private String qualityAndPID;//<关注数，编号>多个，中间用DELIMITER隔开

    public Reverse() {
    }

    public Reverse(String keyWords, String IDF, String pageID) {
        this.keyWords = keyWords;
        this.IDF = IDF;
        this.pageID = pageID;
    }

    //question
    public Reverse(String keyWords, String IDF, String pageID, String TFIDF, String qualityAndPID) {
        this.keyWords = keyWords;
        this.IDF = IDF;
        this.pageID = pageID;
        this.TFIDF = TFIDF;
        this.qualityAndPID = qualityAndPID;
    }


//    public Reverse(String keyWords, String pageID) {
//        this.keyWords = keyWords;
//        this.pageID = pageID;
//    }

    @Override
    public int compareTo(Reverse reverse) {
        if (Double.valueOf(IDF) < Double.valueOf(reverse.IDF)) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return keyWords.equals(((Reverse) obj).getKeyWords());
    }

    @Override
    public int hashCode() {
        return keyWords.hashCode();
    }

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

    public String getPageID() {
        return pageID;
    }

    public void setPageID(String pageID) {
        this.pageID = pageID;
    }

    public String getTFIDF() {
        return TFIDF;
    }

    public void setTFIDF(String TFIDF) {
        this.TFIDF = TFIDF;
    }

    public String getQualityAndPID() {
        return qualityAndPID;
    }

    public void setQualityAndPID(String qualityAndPID) {
        this.qualityAndPID = qualityAndPID;
    }
}
