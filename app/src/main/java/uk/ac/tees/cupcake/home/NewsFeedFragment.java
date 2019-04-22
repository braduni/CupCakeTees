package uk.ac.tees.cupcake.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import uk.ac.tees.cupcake.R;
import uk.ac.tees.cupcake.feed.FeedAdapter;
import uk.ac.tees.cupcake.feed.Post;

public class NewsFeedFragment extends Fragment {

    private FeedAdapter mFeedAdapter;
    private CollectionReference usersCollectionReference;
    private ArrayList<Post> mPosts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_newsfeed, container, false);

        // Initialise
        RecyclerView recyclerView = view.findViewById(R.id.feed_recycler_view);
        usersCollectionReference = FirebaseFirestore.getInstance().collection("Users");

        //Layout
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

        //Setup
        recyclerView.setLayoutManager(layoutManager);

        mFeedAdapter = new FeedAdapter(mPosts);
        recyclerView.setAdapter(mFeedAdapter);

        //Get all user posts.
        getAllPostsQuery();

        //getAllFollowingPostsQuery();
        return view;
    }

    /**
     * Gets all posts from each user, orders then by date of post.
     */
    private void getAllPostsQuery(){
        TreeMap<Date, Post> allPosts = new TreeMap<>();

        usersCollectionReference.get()
                                .addOnSuccessListener(documentSnapshots -> {
                                    // Iterates "Users" collection
                                    for(DocumentSnapshot documentSnapshot : documentSnapshots){
                                        //Users to "User Posts"
                                        documentSnapshot.getReference()
                                                        .collection("User Posts")
                                                        .get()
                                                        .addOnSuccessListener(documentSnapshots1 -> {
                                                            // Iterates all "Users Posts" and adds each one to allPosts
                                                            // TreeMap with TimeStamp as Key and Post as value.
                                                            for(DocumentSnapshot documentSnapshot1 : documentSnapshots1){
                                                                Post currentDoc = documentSnapshot1.toObject(Post.class);
                                                                allPosts.put(currentDoc.getTimeStamp(), currentDoc);
                                                            }

                                                            // Removes all current posts in mPost array, adds all values in allPost treeMap in descending order.
                                                            mPosts.clear();
                                                            mPosts.addAll(allPosts.descendingMap().values());

                                                            mFeedAdapter.notifyDataSetChanged();
                                                        });
                                    }
                                });
    }

    private void getAllFollowingPostsQuery(){

        TreeMap<Date, Post> allFollowersPosts = new TreeMap<>();
        String currentUserUid = FirebaseAuth.getInstance()
                                            .getCurrentUser()
                                            .getUid();

        usersCollectionReference.document(currentUserUid)
                                .collection("Following")
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot documentSnapshots) {
                                        // Iterates through each active users followers
                                        for(DocumentSnapshot documentSnapshot : documentSnapshots){

                                            usersCollectionReference.document(documentSnapshot.getReference()
                                                                    .getId())
                                                                    .collection("User Posts")
                                                                    .get()
                                                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(QuerySnapshot documentSnapshots) {

                                                                            for(DocumentSnapshot documentSnapshot1 : documentSnapshots){
                                                                                Post currentDoc = documentSnapshot1.toObject(Post.class);
                                                                                allFollowersPosts.put(currentDoc.getTimeStamp(), currentDoc);
                                                                            }

                                                                            mPosts.clear();
                                                                            mPosts.addAll(allFollowersPosts.descendingMap().values());

                                                                            mFeedAdapter.notifyDataSetChanged();
                                                                        }
                                                                    });
                                        }
                                    }
                                });
    }

}

//        TreeMap<Date, Post> allPosts = new TreeMap<>();
//
//        collectionReference.get()
//                .addOnSuccessListener(documentSnapshots -> {
//                    // Iterates "Users" collection
//                    for(DocumentSnapshot documentSnapshot : documentSnapshots){
//                        //Users to "User Posts"
//                        listener = documentSnapshot.getReference()
//                                .collection("User Posts")
//                                // Add snapshot listener to each user posts document, listener detaches when user leaves activity.
//                                .addSnapshotListener((documentSnapshots1, e) -> {
//                                    if(e != null){
//                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
//                                        return;
//                                    }
//                                    // Iterates through only the changed documents.
//                                    for(DocumentChange documentChange : documentSnapshots1.getDocumentChanges()){
//
//                                        DocumentSnapshot currentDoc = documentChange.getDocument();
//                                        Post post = currentDoc.toObject(Post.class);
//
//                                        // Switch to determine the change to the document
//                                        switch(documentChange.getType()){
//                                            case ADDED:
//                                                Toast.makeText(getContext(), "Post add", Toast.LENGTH_SHORT).show();
//                                                allPosts.put(post.getTimeStamp(), post);
//                                                break;
//                                            case MODIFIED:
//                                                Toast.makeText(getContext(), "Post modified", Toast.LENGTH_SHORT).show();
//                                                allPosts.put(post.getTimeStamp(), post);
//                                                break;
//                                            case REMOVED:
//                                                Toast.makeText(getContext(), "Post deleted", Toast.LENGTH_SHORT).show();
//                                                allPosts.remove(post.getTimeStamp());
//                                                break;
//                                        }
//
//                                        mPosts.addAll(allPosts.descendingMap().values());
//                                        mFeedAdapter.notifyDataSetChanged();
//
//                                    }
//
//                                    //mPosts.clear();
//                                    //mPosts.addAll(allPosts.descendingMap().values());
//                                    //mFeedAdapter.notifyDataSetChanged();

//                                });
//
//                    }
//
//                });
