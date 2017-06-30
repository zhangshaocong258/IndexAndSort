package mybatis;

/**
 * Created by zsc on 2016/12/20.
 * 仅用权重作比较
 */
public class Forward implements Comparable<Forward> {
    private int id;
    private String title;//标题
    private String url;//网页地址
    private String description;//摘要
    private int quality;//关注者，关注者，关注者
//    private int quality2;//被浏览，回答，问题
    private String keyWords;//关键词
    private String TF;

    public Forward() {
    }

    public Forward(String title, String url, String description, int quality, String keyWords) {
        this.title = title;
        this.url = url;
        this.description = description;
        this.quality = quality;
        this.keyWords = keyWords;
    }

    //question专用
    public Forward(String title, String url, String description, int quality, String keyWords, String TF) {
        this.title = title;
        this.url = url;
        this.description = description;
        this.quality = quality;
        this.keyWords = keyWords;
        this.TF = TF;
    }

    @Override
    public int compareTo(Forward forward) {
        if (forward.quality == quality) {
            return 0;
        } else {
            return forward.quality > quality ? 1 : -1;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

//    public int getQuality2() {
//        return quality2;
//    }
//
//    public void setQuality2(int quality2) {
//        this.quality2 = quality2;
//    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTF() {
        return TF;
    }

    public void setTF(String TF) {
        this.TF = TF;
    }
}
