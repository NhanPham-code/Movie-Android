package com.example.ojtbadaassignment14;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.ojtbadaassignment14.adapters.NavReminderAdapter;
import com.example.ojtbadaassignment14.adapters.ViewPagerAdapter;
import com.example.ojtbadaassignment14.db.DatabaseHelper;
import com.example.ojtbadaassignment14.fragments.AboutFragment;
import com.example.ojtbadaassignment14.fragments.CommonFragment;
import com.example.ojtbadaassignment14.fragments.FavoriteListFragment;
import com.example.ojtbadaassignment14.fragments.MovieDetailFragment;
import com.example.ojtbadaassignment14.fragments.MovieListFragment;
import com.example.ojtbadaassignment14.fragments.SettingFragment;
import com.example.ojtbadaassignment14.fragments.SettingsFragment;
import com.example.ojtbadaassignment14.models.Movie;
import com.example.ojtbadaassignment14.models.Reminder;
import com.example.ojtbadaassignment14.services.Base64Helper;
import com.example.ojtbadaassignment14.services.CallbackService;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements CallbackService {


    private ArrayList<String> tabNameList;
    private ArrayList<Integer> tabIconList;


    // view pager, tab layout
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private ViewPagerAdapter viewPagerAdapter;

    // toolbar, drawer layout, navigation view,...
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;
    ImageButton btnChangeLayout;
    private boolean isGridLayout = false;
    EditText edtSearch;
    ImageButton btnSearch;

    // Fragments
    MovieListFragment movieListFragment;
    MovieDetailFragment movieDetailFragment;
    CommonFragment commonFragment;
    FavoriteListFragment favoriteListFragment;
    SettingFragment settingFragment;
    AboutFragment aboutFragment;
    List<Fragment> fragmentList;

    // Preferences Setting fragment
    SettingsFragment settingsFragment;

    // Base64Helper
    Base64Helper base64Helper = new Base64Helper();

    // DatabaseHelper
    DatabaseHelper databaseHelper = new DatabaseHelper(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init view
        init();

        // set up toolbar
        setUpToolbar();

        // create fragments
        createFragments();

        // set up button change layout of movie list
        setBtnChangeLayout();

        // Set up tab change listener to change toolbar title...
        setUpTabChangeListener();

        // Set up search button of favorite list
        setUpSearchButton();

        // click edit profile button in navigation view
        onClickEditProfileButton();


        // Register the receiver to update reminder list in navigation view after deleting a reminder from alarmReceiver
        IntentFilter filter = new IntentFilter("com.example.ojtbadaassignment14.UPDATE_REMINDER_LIST_MAIN");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(updateReminderListReceiver, filter, Context.RECEIVER_EXPORTED);
        }

        // click show all reminder button in navigation view to show all reminder
        onClickShowAllReminderButton();

    }


    /**
     * Get user profile data from SharedPreferences when activity start and fill in the header of navigation view
     * Update reminder list in navigation view when activity start if reminder is added or deleted in ReminderActivity
     * Update badge tag for favorite list tab when activity start (Get favorite list size from SQLite)
     */
    @Override
    protected void onStart() {
        super.onStart();

        // update user information in header of navigation view from shared preferences
        updateUserProfile();

        // update reminder list in navigation view when reload activity
        updateReminderList();

        // update badge tag for favorite list tab wait for 1 second to tab layout create completely before update badge
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateBadgeTag();
            }
        }, 1000);

        // get intent from all reminder adapter to show movie detail
        getIntentFromAllReminderAdapterToShowMovieDetail();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the receiver
        unregisterReceiver(updateReminderListReceiver);
    }

    private void init() {
        viewPager2 = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        btnChangeLayout = findViewById(R.id.change_layout_button);
        edtSearch = findViewById(R.id.search_edit_text);
        btnSearch = findViewById(R.id.search_button);
    }

    /**
     * Set up toolbar
     */
    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    /**
     * Set up button change layout of movie list
     */
    private void setBtnChangeLayout() {
        btnChangeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGridLayout = !isGridLayout;
                if (isGridLayout) {
                    btnChangeLayout.setImageResource(R.drawable.ic_list);
                    movieListFragment.changeLayout(isGridLayout);
                } else {
                    btnChangeLayout.setImageResource(R.drawable.ic_grid);
                    movieListFragment.changeLayout(isGridLayout);
                }
            }
        });
    }


    /**
     * Get intent from all reminder adapter to show movie detail after clicking on reminder
     */
    private void getIntentFromAllReminderAdapterToShowMovieDetail() {
        Intent intent = getIntent();
        if (intent != null) {
            Movie movie = intent.getParcelableExtra("movie");
            if (movie != null) {

                // check if movie is favorite from SQLite
                List<Movie> favoriteMovies = databaseHelper.getAllFavoriteMovies();
                for(Movie m : favoriteMovies) {
                    if (m.getId() == movie.getId()) {
                        movie.setIsFavorite(1);
                        break;
                    }
                }

                // Show movie detail fragment
                // Delay 500ms to wait for common fragment attached to activity before show movie detail fragment
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onShowMovieDetail(movie);
                    }
                }, 500);
            }
        }
    }


    /**
     * Broadcast receiver to update reminder list in navigation view after notify from alarmReceiver in Main Activity
     */
    private final BroadcastReceiver updateReminderListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // update reminder list in navigation view
            updateReminderList();
        }

    };

    /**
     * Update reminder list in navigation view after add or delete a reminder
     */
    @Override
    public void updateReminderList() {
        // get reminder list from SQLite
        List<Reminder> reminders = databaseHelper.getAllReminders();

        // sort reminders by time and get the two nearest reminders
        reminders.sort(Comparator.comparingLong(Reminder::getTime));
        List<Reminder> nearestReminders = reminders.stream().limit(2).collect(Collectors.toList());

        // set up RecyclerView with ReminderAdapter
        View headerOfNavigationView = navigationView.getHeaderView(0);
        RecyclerView rvReminder = headerOfNavigationView.findViewById(R.id.rvReminder);
        NavReminderAdapter reminderAdapter = new NavReminderAdapter(nearestReminders);
        rvReminder.setLayoutManager(new LinearLayoutManager(this));
        rvReminder.setAdapter(reminderAdapter);
    }


    /**
     * Click show all reminder button in navigation view
     */
    private void onClickShowAllReminderButton() {
        View headerOfNavigationView = navigationView.getHeaderView(0);
        Button btnShowAllReminder = headerOfNavigationView.findViewById(R.id.btnShowAll);
        btnShowAllReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // move to reminder activity
                Intent intent = new Intent(MainActivity.this, AllReminderActivity.class);
                startActivity(intent);
                // close drawer
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
    }


    /**
     * Click edit profile button in navigation view
     */
    private void onClickEditProfileButton() {
        View headerOfNavigationView = navigationView.getHeaderView(0);
        Button btnEdit = headerOfNavigationView.findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Đọc lại dữ liệu từ SharedPreferences và cập nhật giao diện
                SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
                String fullName = sharedPreferences.getString("fullName", "");
                String email = sharedPreferences.getString("email", "");
                String birthday = sharedPreferences.getString("birthday", "");
                String gender = sharedPreferences.getString("gender", "");
                String avatarBase64 = sharedPreferences.getString("avatar", "");
                Bundle bundle = new Bundle();
                bundle.putString("fullName", fullName);
                bundle.putString("email", email);
                bundle.putString("birthday", birthday);
                bundle.putString("gender", gender);
                bundle.putString("avatar", avatarBase64);

                // move to edit profile activity
                Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);

                // close drawer
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * Update header of navigation view from shared preferences
     */
    private void updateUserProfile() {
        // Đọc lại dữ liệu từ SharedPreferences và cập nhật giao diện
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String fullName = sharedPreferences.getString("fullName", "");
        String email = sharedPreferences.getString("email", "");
        String birthday = sharedPreferences.getString("birthday", "");
        String gender = sharedPreferences.getString("gender", "");
        String avatarBase64 = sharedPreferences.getString("avatar", "");

        // Cập nhật thông tin trong header
        View headerOfNavigationView = navigationView.getHeaderView(0);
        TextView tvFullName = headerOfNavigationView.findViewById(R.id.fullName);
        tvFullName.setText(fullName);
        TextView tvEmail = headerOfNavigationView.findViewById(R.id.email);
        tvEmail.setText(email);
        TextView tvBirthDay = headerOfNavigationView.findViewById(R.id.birthday);
        tvBirthDay.setText(birthday);
        TextView tvGender = headerOfNavigationView.findViewById(R.id.gender);
        tvGender.setText(gender);

        // Cập nhật avatar
        ImageView avatarImg = headerOfNavigationView.findViewById(R.id.avatarImage);
        if (!avatarBase64.isEmpty()) {
            avatarImg.setImageBitmap(base64Helper.convertBase64ToBitmap(avatarBase64));
        }
    }

    /**
     * Set up tab change listener to change toolbar title...
     */
    private void setUpTabChangeListener() {
        // Set up tab change listener
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0: // Movie List
                        toolbar.setTitle("Moives");
                        btnChangeLayout.setVisibility(View.VISIBLE);
                        edtSearch.setVisibility(View.GONE);
                        btnSearch.setVisibility(View.GONE);
                        break;
                    case 1: // Favorite List
                        toolbar.setTitle("");
                        btnChangeLayout.setVisibility(View.GONE);
                        edtSearch.setVisibility(View.VISIBLE);
                        edtSearch.setText("Favourite");
                        btnSearch.setVisibility(View.VISIBLE);
                        break;
                    case 2: // Setting
                        toolbar.setTitle("Setting");
                        btnChangeLayout.setVisibility(View.GONE);
                        edtSearch.setVisibility(View.GONE);
                        btnSearch.setVisibility(View.GONE);
                        break;
                    case 3: // About
                        toolbar.setTitle("About");
                        btnChangeLayout.setVisibility(View.GONE);
                        edtSearch.setVisibility(View.GONE);
                        btnSearch.setVisibility(View.GONE);
                        break;
                }
            }
        });
    }

    /**
     * Set up listener search button
     */
    private void setUpSearchButton() {
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFavoriteMovie();
            }
        });
    }

    /**
     * Search favorite movie by name keyword
     */
    private void searchFavoriteMovie() {
        String search = edtSearch.getText().toString();
        if (search.isEmpty()) {
            // show all favorite movies
            favoriteListFragment.searchFavoriteMovie(search);
            Toast.makeText(this, "Please enter search keyword", Toast.LENGTH_SHORT).show();
        } else {
            // search favorite movies by keyword
            favoriteListFragment.searchFavoriteMovie(search);
        }
    }


    /**
     * Create fragments and set up view pager with tab layout
     */
    private void createFragments() {
        // create fragments
        movieListFragment = MovieListFragment.newInstance();
        movieListFragment.setCallbackService(MainActivity.this);
        commonFragment = new CommonFragment();// use common fragment class to handle fragment transaction
        commonFragment.setCallbackService(MainActivity.this);
        commonFragment.setMovieListFragment(movieListFragment);

        favoriteListFragment = FavoriteListFragment.newInstance();
        favoriteListFragment.setCallbackService(MainActivity.this);

        //settingFragment = SettingFragment.newInstance();
        // Preferences Setting fragment
        settingsFragment = new SettingsFragment();

        aboutFragment = AboutFragment.newInstance();


        fragmentList = new ArrayList<>();
        fragmentList.add(commonFragment); // add common fragment to handle fragment transaction
        fragmentList.add(favoriteListFragment);
        //fragmentList.add(settingFragment);
        // use Preferences Setting fragment
        fragmentList.add(settingsFragment);

        fragmentList.add(aboutFragment);


        viewPagerAdapter = new ViewPagerAdapter(this, fragmentList);
        viewPager2.setAdapter(viewPagerAdapter);
        viewPager2.setOffscreenPageLimit(4); // create two 4 tabs to avoid null

        // set tab layout with view pager
        createTabName();
        createTabIcon();
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            tab.setText(tabNameList.get(position));
            tab.setIcon(tabIconList.get(position));
        }).attach();
    }

    /**
     * Create tab name list
     */
    private void createTabName() {
        tabNameList = new ArrayList<>();
        tabNameList.add("Movie List");
        tabNameList.add("Favorite List");
        tabNameList.add("Setting");
        tabNameList.add("About");
    }

    /**
     * Create tab icon list
     */
    private void createTabIcon() {
        tabIconList = new ArrayList<>();
        tabIconList.add(R.drawable.ic_home);
        tabIconList.add(R.drawable.ic_favorite);
        tabIconList.add(R.drawable.ic_setting);
        tabIconList.add(R.drawable.ic_about);
    }


    /**
     * Create option menu
     *
     * @param menu: menu object
     * @return true if menu is created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    /**
     * Handle option menu item click to load movie list with different category
     *
     * @param item: menu item
     * @return true if item is selected
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_movie_popular) {
            movieListFragment.loadMovieListByCategory("popular");
        } else if (itemId == R.id.menu_movie_top_rated) {
            movieListFragment.loadMovieListByCategory("top_rated");
        } else if (itemId == R.id.menu_movie_upcoming) {
            movieListFragment.loadMovieListByCategory("upcoming");
        } else if (itemId == R.id.menu_movie_now_playing) {
            movieListFragment.loadMovieListByCategory("now_playing");
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Show movie detail
     *
     * @param movie: movie object to show detail
     */
    @Override
    public void onShowMovieDetail(Movie movie) {
        Log.d("check", "onShowMovieDetail called with movie: " + movie.getTitle());

        // Check if the current tab is the favorite tab
        if (tabLayout.getSelectedTabPosition() == 1) {
            // Switch to the movie tab
            viewPager2.setCurrentItem(0);
        }

        // set up toolbar title
        toolbar.setTitle(movie.getTitle());
        btnChangeLayout.setVisibility(View.GONE);

        // Create MovieDetailFragment
        movieDetailFragment = MovieDetailFragment.newInstance(movie);
        movieDetailFragment.setCallbackService(MainActivity.this);

        // Add detail fragment to CommonFragment if it's attached
        if (commonFragment.isAdded()) {
            commonFragment.setMovieDetailFragment(movieDetailFragment);
            commonFragment.showDetailFragment();
        } else {
            Log.d("MainActivity", "CommonFragment is not attached yet.");
        }
    }

    /**
     * Back to movie list
     */
    @Override
    public void backToMovieList() {
        toolbar.setTitle("Movies");
        btnChangeLayout.setVisibility(View.VISIBLE);
        commonFragment.showMovieListFragment();
    }

    /**
     * Callback from movie list fragment to favorite or unfavorite movie
     *
     * @param movie: movie object to favorite or unfavorite
     */
    @Override
    public void onFavoriteMovie(Movie movie) {
        // update movie favorite status
        movie.setIsFavorite(movie.getIsFavorite() == 0 ? 1 : 0);

        // update movie list fragment and favorite list fragment
        movieListFragment.updateMovieListShow(movie);
        favoriteListFragment.updateFavoriteList(movie);

        // update movie detail fragment if it's visible
        if (movieDetailFragment != null && movieDetailFragment.isVisible()) {
            movieDetailFragment.updateMovieDetail(movie);
        }

        // update badge tag after favorite or unfavorite movie
        updateBadgeTag();

    }

    /**
     * Update badge tag for favorite list tab when Preference Setting change
     */
    @Override
    public void updateBadgeCount() {
        updateBadgeTag();
    }

    /**
     * Update badge tag for favorite list tab when run app
     */
    private void updateBadgeTag() {
        int count = favoriteListFragment.getFavoriteListSize();
        setBadge(count);
    }

    /**
     * Set badge for favorite list tab to show number of favorite movies after favorite or unfavorite movie
     *
     * @param count: number of favorite movies
     */
    private void setBadge(int count) {
        TabLayout.Tab tab = tabLayout.getTabAt(1);
        if (tab != null) {
            if (count > 0) {
                Log.d("checked", "setBadge: count = " + count);
                BadgeDrawable badge = tab.getOrCreateBadge();
                badge.setVisible(true);
                badge.setNumber(count);
            } else {
                Log.d("checked", "setBadge: count = 0");
                tab.removeBadge();
            }
        }
    }

}