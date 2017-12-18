package com.example.capston.pcjari.fragment;

/**
 * Created by KangSeungho on 2017-10-27.
 */

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.capston.pcjari.DetailedInformationActivity;
import com.example.capston.pcjari.GPSTracker;
import com.example.capston.pcjari.MainActivity;
import com.example.capston.pcjari.PC.PCListAdapter;
import com.example.capston.pcjari.PC.PCListItem;
import com.example.capston.pcjari.R;
import com.example.capston.pcjari.sqlite.DataBaseTables;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.example.capston.pcjari.MainActivity.db;

public class SearchByMeFragment extends Fragment {
    private ListView pcListView;
    private PCListAdapter pcListAdapter;
    private ArrayList<PCListItem> pcItem = new ArrayList<PCListItem>();
    private String url;
    private GPSTracker gps;
    private String dist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle("내 주변");

        View view = inflater.inflate(R.layout.fragment_searchbyme, container, false);
        pcListView = (ListView)view.findViewById(R.id.listview2);

        gps = new GPSTracker(getContext());
        dist = String.valueOf(((double)MainActivity.dist/10));
        dataSetting();

        pcListView.setOnItemClickListener(ListshortListener);
        pcListView.setOnItemLongClickListener(ListlongListener);

        return view;
    }

    //리스트 아이템 클릭했을 때 나오는 이벤트
    AdapterView.OnItemClickListener ListshortListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getActivity(), DetailedInformationActivity.class);
            intent.putExtra(DetailedInformationActivity.POSITION, position);
            MainActivity.pc = pcItem.get(position);
            startActivity(intent);

            /*
            Bundle args = new Bundle();
            args.putSerializable("PCItem", pc);
            Fragment detailedInformationFragment = new DetailedInformationFragment();
            detailedInformationFragment.setArguments(args);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, detailedInformationFragment, "PCItemTag")
                    .addToBackStack("PCItemTag").commit();
            */
        }
    };

    // 리스트 아이템 꾹 눌렀을 때 나오는 이벤트
    AdapterView.OnItemLongClickListener ListlongListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            PCListItem pc = pcListAdapter.getItem(position);
            int pcId = pc.getPcID();

            if(!MainActivity.favorite.contains(pcId)) {
                try {
                    MainActivity.favorite.add(pcId);
                    String sql = "INSERT INTO " + DataBaseTables.CreateDB_favorite._TABLENAME + " VALUES("
                            + pc.getPcID() + ");";
                    db.execSQL(sql);
                    Toast.makeText(getContext(), "즐겨찾기에 추가 되었습니다.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "즐겨찾기를 추가하던 도중 에러가 났습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                try {
                    int index = MainActivity.favorite.indexOf(pcId);
                    MainActivity.favorite.remove(index);
                    String sql = "DELETE FROM " + DataBaseTables.CreateDB_favorite._TABLENAME + " WHERE _ID="
                            + pc.getPcID() + ";";
                    db.execSQL(sql);
                    Toast.makeText(getContext(), "즐겨찾기에서 삭제 되었습니다.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "즐겨찾기를 하던 도중 에러가 났습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            pcListAdapter.notifyDataSetChanged();

            return true;
        }
    };

    private void dataSetting(){
        pcListAdapter = new PCListAdapter();

        url = MainActivity.server + "pclist_search.php?";
        url+= "code=1&lat=" + gps.getLatitude() + "&lng=" + gps.getLongitude() + "&dist=" + dist;

        GettingPHP gPHP = new GettingPHP();
        gPHP.execute(url);
    }

    class GettingPHP extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            StringBuilder jsonHtml = new StringBuilder();
            try {
                URL phpUrl = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) phpUrl.openConnection();

                if (conn != null) {
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                        while (true) {
                            String line = br.readLine();
                            if (line == null)
                                break;
                            jsonHtml.append(line + "\n");
                        }
                        br.close();
                    }
                    conn.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonHtml.toString();
        }

        protected void onPostExecute(String str) {
            try {
                JSONObject jObject = new JSONObject(str);
                JSONArray results = jObject.getJSONArray("results");

                if (jObject.get("status").equals("OK")) {
                    pcItem.clear();
                    pcItem = new ArrayList<PCListItem>();

                    if(results.length() == 0) {
                        Toast.makeText(getContext(), "주변에 PC방이 없습니다.", Toast.LENGTH_SHORT).show();
                        pcListAdapter.setItem(pcItem);
                        pcListView.setAdapter(pcListAdapter);
                    }
                    else {
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject temp = results.getJSONObject(i);

                            PCListItem pc = new PCListItem();
                            pc.setPcID(temp.getInt("id"));
                            pc.setTitle(temp.getString("name"));
                            pc.setIcon(temp.getString("url"));
                            pc.setSi(temp.getString("si"));
                            pc.setGu(temp.getString("gu"));
                            pc.setDong(temp.getString("dong"));
                            pc.setPrice(temp.getInt("price"));
                            pc.setTotalSeat(temp.getInt("total"));
                            pc.setSpaceSeat(temp.getInt("space"));
                            pc.setUsingSeat(temp.getInt("using"));
                            pc.setLocation_x(temp.getDouble("x"));
                            pc.setLocation_y(temp.getDouble("y"));

                            pc.setEtc_juso(temp.getString("etc_juso"));
                            pc.setNotice(temp.getString("notice"));
                            pc.setTel(temp.getString("tel"));
                            pc.setCpu(temp.getString("cpu"));
                            pc.setRam(temp.getString("ram"));
                            pc.setVga(temp.getString("vga"));
                            pc.setPeripheral(temp.getString("peripheral"));
                            pc.setSeatLength(temp.getInt("seatlength"));
                            pc.setDist(temp.getDouble("distance"));

                            if(temp.getInt("card") == 0) {
                                pc.setCard(false);
                            } else {
                                pc.setCard(true);
                            }

                            pcItem.add(pc);
                        }

                        pcListAdapter = new PCListAdapter();
                        pcListAdapter.setItem(pcItem);
                        pcListView.setAdapter(pcListAdapter);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}