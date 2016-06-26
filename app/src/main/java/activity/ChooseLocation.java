package activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import db.WeatherDButils;
import model.City;
import model.County;
import model.Province;
import taojiang.coolweather.R;
import utils.HttpListener;
import utils.HttpUtils;
import utils.Utility;

/**
 * Created by taojiang on 6/10/2016.
 */
public class ChooseLocation extends Activity{
    private TextView title;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private WeatherDButils weatherDButils;
    private static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private List<String> datalist = new ArrayList<String>();
    private ProgressDialog progressDialog;
    private boolean from_weatherInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        from_weatherInfo = getIntent().getBooleanExtra("FromWeatherInfo",false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("city_selected", false)&&!from_weatherInfo) {
            Intent intent = new Intent(this, WeatherInfo.class);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.choose_location);
        title = (TextView)findViewById(R.id.title_text);
        listView=(ListView)findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,datalist);
        listView.setAdapter(adapter);
        weatherDButils = WeatherDButils.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY) {
                    String countyCode = countyList.get(position).getCountyCode();
                    Intent intent = new Intent(ChooseLocation.this,
                            WeatherInfo.class);
                    intent.putExtra("county_code", countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();
    }
    private void queryProvinces(){
        provinceList = weatherDButils.loadProvince();
        if(provinceList.size()>0){
            datalist.clear();
            for (Province p : provinceList){
                datalist.add(p.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            title.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }else {
            queryFromServer(null, "province");
        }
    }
    private void queryCities() {
        cityList = weatherDButils.loadCities(selectedProvince.getId());
        if (cityList.size() > 0) {
            datalist.clear();
            for (City city : cityList) {
                datalist.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            title.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }
    private void queryCounties() {
        countyList = weatherDButils.loadCounties(selectedCity.getId());
        if (countyList.size() > 0) {
            datalist.clear();
            for (County county : countyList) {
                datalist.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            title.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }
    private void queryFromServer(final String  Code, final String Type){
        String address;
        if(!TextUtils.isEmpty(Code)){
            address = "http://www.weather.com.cn/data/list3/city" + Code +
                    ".xml";
        }else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtils.sendHttpRequest(address, new HttpListener() {
            @Override
            public void onResponse(String response) {
                boolean result = false;
                if("province".equals(Type)){
                    result = Utility.handleProvinceResponse(response,weatherDButils);
                }else if("city".equals(Type)){
                    result = Utility.handleCitiesResponse(response,weatherDButils,selectedProvince.getId());
                }else if("county".equals(Type)){
                    result = Utility.handleCountiesResponse(response,weatherDButils,selectedCity.getId());
                }
                if(result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(Type)) {
                                queryProvinces();
                            } else if ("city".equals(Type)) {
                                queryCities();
                            } else if ("county".equals(Type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseLocation.this,
                                "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(ChooseLocation.this);
            progressDialog.setMessage("正在加载。。。");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        } else if(from_weatherInfo){
            Intent intent = new Intent(this,WeatherInfo.class);
            startActivity(intent);
            finish();
        }
    }
}
