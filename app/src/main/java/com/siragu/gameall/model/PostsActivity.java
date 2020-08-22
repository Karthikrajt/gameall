
package com.siragu.gameall.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostsActivity {

    @SerializedName("posted_by")
    @Expose
    private PostedBy postedBy;
    @SerializedName("post")
    @Expose
    private Post post;
    @SerializedName("type")
    @Expose
    private Integer type;

    public PostsActivity(Post post) {
        this.post = post;
    }

    public PostsActivity(PostedBy postedBy, Post post, Integer type) {
        this.postedBy = postedBy;
        this.post = post;
        this.type = type;
    }

    /**
     * 
     * @return
     *     The postedBy
     */
    public PostedBy getPostedBy() {
        return postedBy;
    }

    /**
     * 
     * @param postedBy
     *     The posted_by
     */
    public void setPostedBy(PostedBy postedBy) {
        this.postedBy = postedBy;
    }

    /**
     * 
     * @return
     *     The post
     */
    public Post getPost() {
        return post;
    }

    /**
     * 
     * @param post
     *     The post
     */
    public void setPost(Post post) {
        this.post = post;
    }

    /**
     * 
     * @return
     *     The type
     */
    public Integer getType() {
        return type;
    }

    /**
     * 
     * @param type
     *     The type
     */
    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        return !((obj == null) || (getClass() != obj.getClass())) && post.equals(((PostsActivity) obj).getPost());
    }
}
