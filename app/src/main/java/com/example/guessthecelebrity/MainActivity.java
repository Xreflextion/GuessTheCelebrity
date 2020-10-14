package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    HashMap<String, String> celebs = new HashMap<>();
    ArrayList<String> names = new ArrayList<>();
    ArrayList<String> cur_names = new ArrayList<>();
    ConstraintLayout game_board;
    ConstraintLayout end_game;
    LinearLayout vertical_layout;
    ImageView img_view;
    TextView score_text_view;
    Bitmap img;
    String answer = "";
    Random rand = new Random();

    private int cur;
    private int score;
    final int total = 20;

    public class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url;
            HttpURLConnection url_connection = null;
            Bitmap img = null;
            try {
                url = new URL(strings[0]);
                url_connection = (HttpURLConnection) url.openConnection();
                InputStream input_stream = url_connection.getInputStream();
                img = BitmapFactory.decodeStream(input_stream);
                return img;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public class GetString extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            URL url;
            HttpURLConnection url_connection = null;

            ArrayList<String> img_links;
            ArrayList<String> names = new ArrayList<>();
            String html = "";
            try {
                url = new URL(strings[0]);
                url_connection
                        = (HttpURLConnection) url.openConnection();
                InputStream input_stream = url_connection
                        .getInputStream();
                InputStreamReader reader = new InputStreamReader(input_stream);
                BufferedReader br = new BufferedReader(reader);
                String data = br.readLine();
                while (data != null) {
                    String line = data;
                    html += line;
                    html += "\n";
                    data = br.readLine();
                }
                String[] split_html = html.split("lister-list");
                html = split_html[1];
                split_html = html.split("footer filmosearch");
                html = split_html[0];
                img_links = find_all(html, "src=\"", "\"");
                names = find_all(html, "alt=\"", "\"");
                for (int i = 0;i <names.size();i++) {
                    celebs.put(names.get(i), img_links.get(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return names;
        }
    }

    public ArrayList<String> find_all(String str, String start, String end) {
        ArrayList<String> array = new ArrayList<>();
        Pattern p = Pattern.compile(start + "(.*?)" + end);
        Matcher m = p.matcher(str);
        while (m.find()) {
            array.add(m.group(1));
        }
        return array;
    }

    public void set_up() {
        cur_names = new ArrayList<String>();
        for (int i=0; i<total; i++) {
            String name = names.get(rand.nextInt(total));
            while (cur_names.contains(name)) {
                name = names.get(rand.nextInt(total));
            }
            cur_names.add(name);
        }
        score = 0;
        cur = 1;
        score_text_view.setText(""+cur+"/"+total);
    }

    public void start_game(View view) {
        set_up();
        Button button = (Button) view;
        button.setText("Play again?");
        end_game.setVisibility(View.INVISIBLE);
        game_board = (ConstraintLayout) findViewById(R.id.gameBoardLayout);
        game_board.setVisibility(View.VISIBLE);

        new_question();
    }

    public void new_question() {
        if (cur <= 20) {
            ArrayList<String> options = new ArrayList<>();
            DownloadImage get_img = new DownloadImage();

            for (int i = 0; i < 4; i++) {
                String name = names.get(rand.nextInt(celebs.size()));
                while (options.contains(name)) {
                    name = names.get(rand.nextInt(celebs.size()));
                }
                options.add(name);
                Button button = (Button) vertical_layout.getChildAt(i);
                button.setText(options.get(i));
            }
            answer = options.get(rand.nextInt(4));
            score_text_view.setText("" + cur + "/" + total);
            cur += 1;

            try {
                img = get_img.execute(celebs.get(answer)).get();
                img_view.setImageBitmap(img);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            game_board.setVisibility(View.INVISIBLE);
            end_game.setVisibility(View.VISIBLE);
            TextView final_score = (TextView) findViewById(R.id.totalScoreTextView);
            final_score.setVisibility(View.VISIBLE);
            final_score.setText("Score: "+score+"/"+total);
        }
    }

    public void check_answer(View view) {
        Button choice = (Button) view;
        if (choice.getText().equals(answer)) {
            score += 1;
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Incorrect... The answer was "+answer, Toast.LENGTH_LONG).show();
        }
        new_question();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        end_game = (ConstraintLayout) findViewById(R.id.gameOverLayout);
        vertical_layout = (LinearLayout) findViewById(R.id.linearLayout);
        score_text_view = (TextView) findViewById(R.id.scoreTextView);
        img_view = (ImageView) findViewById(R.id.celebImageView);
        GetString get_str = new GetString();

        try {
            names = get_str.execute("https://www.imdb.com/list/ls052283250/").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}