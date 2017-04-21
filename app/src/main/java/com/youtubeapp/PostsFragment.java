package com.youtubeapp;

/**
 * Created by amitozdeol on 2/18/17.
 */

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * While this looks like a lot of code, all this class
 * actually does is load the posts in to the listview.
 *
 * @author Hathy
 */
public class PostsFragment extends Fragment{

    ListView postsList;
    WebView webView;
    ArrayAdapter<Post> adapter;
    Handler handler;
    View spinner;
    SwipeRefreshLayout swipeView;

    String subreddit;
    List<Post> posts;
    PostsHolder postsHolder;

    public PostsFragment(){
        handler=new Handler();
        posts=new ArrayList<Post>();
    }

    public static Fragment newInstance(String subreddit){
        PostsFragment pf=new PostsFragment();
        pf.subreddit=subreddit;
        pf.postsHolder=new PostsHolder(pf.subreddit);
        return pf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.posts
                , container
                , false);
        swipeView = (SwipeRefreshLayout) v.findViewById(R.id.swipe);

        swipeView.setEnabled(false);
        postsList=(ListView)v.findViewById(R.id.posts_list);
        webView = (WebView) v.findViewById(R.id.webview);
        webView.setWebViewClient(new MyBrowser());

        spinner = (View)v.findViewById(R.id.loadingPanel);
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initialize();
    }

    private void initialize(){
        // This should run only once for the fragment as the
        // setRetainInstance(true) method has been called on
        // this fragment

        if(posts.size()==0){

            // Must execute network tasks outside the UI
            // thread. So create a new thread.

            new Thread(){
                public void run(){
                    posts.addAll(postsHolder.fetchPosts());

                    // UI elements should be accessed only in
                    // the primary thread, so we must use the
                    // handler here.

                    handler.post(new Runnable(){
                        public void run(){
                            createAdapter();
                        }
                    });
                }
            }.start();
        }else{
            createAdapter();
        }
    }

    /**
     * This method creates the adapter from the list of posts
     * , and assigns it to the list.
     */
    private void createAdapter(){

        // Make sure this fragment is still a part of the activity.
        if(getActivity()==null) return;
        adapter=new ArrayAdapter<Post>(getActivity(),R.layout.post_item, posts){
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {

                if(convertView==null){
                    convertView=getActivity().getLayoutInflater().inflate(R.layout.post_item, null);
                }

                convertView.setSelected(true);
                final TextView postTitle;
                postTitle=(TextView)convertView.findViewById(R.id.post_title);

                TextView postDetails;
                postDetails=(TextView)convertView.findViewById(R.id.post_details);

                TextView postScore;
                postScore=(TextView)convertView.findViewById(R.id.post_score);

                postTitle.setText(posts.get(position).title);
                postDetails.setText(posts.get(position).getDetails());
                postScore.setText(posts.get(position).getScore());
                postScore.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);  //measure the actual width and height of score

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) postTitle.getLayoutParams(); //add margin between title and score
                params.rightMargin = postScore.getMeasuredWidth()+5;
                postTitle.setLayoutParams(params);

                //everytime a list item is clicked - load into webview - change textcolor
                convertView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String url = posts.get(position).getUrl();
                        url = url+"?wmode=opaque&autohide=1&autoplay=1&vq=small";   //for autoplay video
                        webView.getSettings().setLoadsImagesAutomatically(true);
                        webView.getSettings().setJavaScriptEnabled(true);
                        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
                        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                        webView.loadUrl(url);
                        postTitle.setTextColor(Color.rgb(88,17,252));
                    }
                });
                return convertView;
            }
        };
        spinner.setVisibility(View.GONE);
        postsList.setAdapter(adapter);
        //pull to refresh
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);
                ( new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeView.setRefreshing(false);
                        PostsFragment.newInstance("listentothis");
                    }
                }, 3000);
            }
        });
        postsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0)
                    swipeView.setEnabled(true);
                else
                    swipeView.setEnabled(false);
            }
        });

    }




    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

}
