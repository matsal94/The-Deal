package co.sepin.thedeal.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import co.sepin.thedeal.R;
import co.sepin.thedeal.adapter.NewsItemAdapter;
import co.sepin.thedeal.api.NewsApi;
import co.sepin.thedeal.api.RetrofitUtils3;
import co.sepin.thedeal.application.ModeClass;
import co.sepin.thedeal.model.NewsResult;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


public class NewsCategoryFragment extends Fragment {

    private static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
    private CompositeDisposable compositeDisposable;
    private ProgressBar categoryLoadingPB;
    private ConstraintLayout categoryLayout;
    private ConstraintLayout categoryNoInternetConnectionLayout;
    private RecyclerView categoryRecyclerView;
    private NewsApi service;
    private String category;


    public NewsCategoryFragment() {
    }


    public static final NewsCategoryFragment newInstance(String message) {

        NewsCategoryFragment newsCategoryFragment = new NewsCategoryFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString(EXTRA_MESSAGE, message);
        newsCategoryFragment.setArguments(bundle);

        return newsCategoryFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View itemView = inflater.inflate(R.layout.fragment_news_category, container, false);

        category = getArguments().getString(EXTRA_MESSAGE);

        categoryRecyclerView = (RecyclerView) itemView.findViewById(R.id.news_categoryRV);
        categoryLayout = (ConstraintLayout) itemView.findViewById(R.id.news_categoryCL1);
        categoryNoInternetConnectionLayout = (ConstraintLayout) itemView.findViewById(R.id.news_categoryCL0);
        categoryLoadingPB = (ProgressBar) itemView.findViewById(R.id.news_categoryPB);

        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitUtils3.getInstance();
        service = retrofit.create(NewsApi.class);

        initRecyclerView();

        return itemView;
    }


    @Override
    public void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }


    private void initRecyclerView() {

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        boolean isBigTablet = getResources().getBoolean(R.bool.isBigTablet);

        if (isTablet || isBigTablet)
            categoryRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)); // tablet
        else
            categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false)); // telefon

        categoryRecyclerView.setHasFixedSize(false);
    }


    public void getNewsInformation() {

        categoryLoadingPB.setVisibility(View.VISIBLE);
        categoryNoInternetConnectionLayout.setVisibility(View.GONE);

        compositeDisposable.add(service.getNewsCategory("pl", category, ModeClass.NEWS_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.functions.Consumer<NewsResult>() {

                    @Override
                    public void accept(NewsResult newsResult) throws Exception {

                        categoryLayout.setVisibility(View.VISIBLE);
                        displayNews(newsResult);
                    }
                }, new io.reactivex.functions.Consumer<Throwable>() {

                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        categoryLoadingPB.setVisibility(View.GONE);
                    }
                })
        );
    }


    public void noInternetConnection() {

        categoryLoadingPB.setVisibility(View.GONE);

        if (categoryLayout.getVisibility() == View.GONE) {
            categoryNoInternetConnectionLayout.setVisibility(View.VISIBLE);
        }
    }


    private void displayNews(NewsResult newsResult) {

        NewsItemAdapter adapter = new NewsItemAdapter(getActivity(), newsResult, categoryRecyclerView);
        categoryRecyclerView.setAdapter(adapter);

        categoryLoadingPB.setVisibility(View.GONE);
    }
}