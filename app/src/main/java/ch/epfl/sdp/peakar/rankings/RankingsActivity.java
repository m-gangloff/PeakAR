package ch.epfl.sdp.peakar.rankings;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.utils.ToolbarHandler;

public class RankingsActivity extends AppCompatActivity {

    private final static List<RankingItem> rankingItems = Collections.synchronizedList(new ArrayList<>());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rankings);

        ToolbarHandler.SetupToolbar(this, getString(R.string.toolbar_rankings));

        fillRankingsList();
    }

    /**
     * Fetch ListView and setup it upp with a ranking item list adapter.
     */
    private void fillRankingsList(){
        ListView rankingsListView = findViewById(R.id.rankings_list_view);

        RankingListAdapter listAdapter = new RankingListAdapter(this,
                R.layout.ranking_item,
                rankingItems);

        listAdapter.setNotifyOnChange(true);

        // Start rankings synchronization
        RemoteRankingsList.synchronizeRankings(rankingItems, listAdapter);

        rankingsListView.setAdapter(listAdapter);
    }
}