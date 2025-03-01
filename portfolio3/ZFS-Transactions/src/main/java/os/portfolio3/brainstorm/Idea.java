package os.portfolio3.brainstorm;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;

public class Idea {
    private String idea;
    private List<String> comments;

    public Idea(String idea) {
        this.idea = idea;
        this.comments = new ArrayList<>();
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static Idea fromJson(String json) {
        return new Gson().fromJson(json, Idea.class);
    }

    public String getIdea() {
        return idea;
    }

    public void setIdea(String idea) {
        this.idea = idea;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }
}
