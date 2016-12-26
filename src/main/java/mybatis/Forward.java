package mybatis;

/**
 * Created by zsc on 2016/12/20.
 * 仅用权重作比较
 */
public class Forward implements Comparable<Forward>{
    private int id;
    private String title;//标题
    private String url;//网页地址
    private String description;//摘要
    private int quality;//权重
    private String keyWords;//关键词

    public Forward() {}

    public Forward(String title, String url, String description, int quality, String keyWords) {
        this.title = title;
        this.url = url;
        this.description = description;
        this.quality = quality;
        this.keyWords = keyWords;
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
}
