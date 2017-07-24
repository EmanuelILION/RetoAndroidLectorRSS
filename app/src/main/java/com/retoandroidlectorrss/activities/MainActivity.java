package com.retoandroidlectorrss.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.retoandroidlectorrss.R;
import com.retoandroidlectorrss.databases.SQLDataBase;
import com.retoandroidlectorrss.models.ReadRss;
import com.retoandroidlectorrss.utils.Constantes;
import com.retoandroidlectorrss.utils.OperationsBBDD;
import com.retoandroidlectorrss.utils.RssParserSax;
import com.rey.material.widget.Spinner;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    //Components
    private ListView listViewNews;
    List<ReadRss> rssList;
    private RelativeLayout splashMain;
    private Spinner selectNode;
    private SwipeRefreshLayout refreshNews;

    //Database
    private SQLDataBase BBDD;
    private static SQLiteDatabase sqlDatabase;

    //SharedPreferences
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private boolean firstTime;

    private static String URL_RSS_XML_SELECTED;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewNews = (ListView) findViewById(R.id.listViewMain);
        splashMain = (RelativeLayout) findViewById(R.id.main_splash);

        //intancia de la base de datos y la funcionalidad de SQLite
        BBDD = new SQLDataBase(getApplicationContext());
        sqlDatabase = BBDD.getWritableDatabase();

        //inicializacion de la lista
        rssList = new ArrayList<>();

        //almacena si la aplicación se conectó almenos una vez a internet
        sharedPreferences = getSharedPreferences(Constantes.SHARED_PREFERENCES_ID, MODE_PRIVATE);
        firstTime = sharedPreferences.getBoolean(Constantes.SHARED_PREFERNCES_FIRST_TIME, false);

        //spinner de donde elegiremos las fuentes de datos
        URL_RSS_XML_SELECTED = Constantes.URL_RSS_XML_INTERNATIONAL;
        selectNode = (com.rey.material.widget.Spinner) findViewById(R.id.select_node);
        //Creamos el adaptador
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.selectNodes, android.R.layout.simple_spinner_item);
        //Añadimos el layout para el menú
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Le indicamos al spinner el adaptador a usar
        selectNode.setAdapter(adapter);
        ((TextView) selectNode.getChildAt(0)).setTextColor(Color.BLACK);


        selectNode.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Spinner parent, View view, int position, long id) {
                //captura valor del item seleccionado
                if (parent.getAdapter().getItem(position).equals("Internacional")) {
                    URL_RSS_XML_SELECTED = Constantes.URL_RSS_XML_INTERNATIONAL;
                } else if (parent.getAdapter().getItem(position).equals("España")) {
                    URL_RSS_XML_SELECTED = Constantes.URL_RSS_XML_ESPANIA_;
                } else if (parent.getAdapter().getItem(position).equals("Economia")) {
                    URL_RSS_XML_SELECTED = Constantes.URL_RSS_XML_ECONOMIA;
                }

                //comprobacion que almenos hay 1 registro con la fuente seleccionada
                if (!OperationsBBDD.chargedNode(sqlDatabase, URL_RSS_XML_SELECTED)) {
                    Toast.makeText(getApplicationContext(), "Tiene que almenos una vez haber cargado esta fuente conectado a internet",
                            Toast.LENGTH_SHORT).show();
                } else {
                    //Carga del XML mediante la tarea asíncrona
                    CargarXmlTask tarea = new CargarXmlTask();
                    //llamamos al AsyncTask pasándole la URL
                    tarea.execute(URL_RSS_XML_SELECTED);

                    Toast.makeText(getApplicationContext(), "Cambiando fuente de datos a: " + parent.getAdapter().getItem(position).toString(),
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

        //servirá para realizar un refresh de la visualización de los datos del listView
        refreshNews = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refreshNews.setOnRefreshListener(this);

        //evento para realizar el trabajo correspondiente a cada item seleccionado
        listViewNews.setOnItemClickListener(new AdapterView.OnItemClickListener()

        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i,
                                    long l) {

                //instanciar dialogo
                final Dialog showInformation = new Dialog(MainActivity.this);
                showInformation.setContentView(R.layout.custom_dialog);

                ImageView infoImg = (ImageView) showInformation.findViewById(R.id.dialog_image);
                TextView infoTitle = (TextView) showInformation.findViewById(R.id.dialog_title);
                TextView infoDescription = (TextView) showInformation.findViewById(R.id.dialog_detail);

                ImageView infoClose = (ImageView) showInformation.findViewById(R.id.dialog_close);
                Button infoLaunchLink = (Button) showInformation.findViewById(R.id.dialog_button_link);

                //cargar los datos en los componentes del dialogo y mostramos el dialogo
                Picasso.with(getApplicationContext()).load(rssList.get(i).getImage()).into(infoImg);
                infoTitle.setText(rssList.get(i).getTitle());
                infoDescription.setText(rssList.get(i).getDescription());
                showInformation.setTitle(rssList.get(i).getDate());
                showInformation.show();

                //cerrar el dialogo
                infoClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showInformation.dismiss();
                    }
                });

                //abrimos la noticia en el navegador del dispositivo
                infoLaunchLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!isNetworkAvailable()) {
                            Toast.makeText(getApplicationContext(), Constantes.CONNECTION_NOT_AVIABLE,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            //Lanza el navegador del dispositivo por defecto y carga el link de la noticia
                            Intent launch = new Intent(Intent.ACTION_VIEW);
                            launch.setData(Uri.parse(rssList.get(i).getLink()));
                            startActivity(launch);
                        }
                    }
                });

                Toast.makeText(getApplicationContext(), rssList.get(i).getDate(), Toast.LENGTH_SHORT).show();

            }
        });
        //Carga del XML mediante la tarea asíncrona
        CargarXmlTask tarea = new CargarXmlTask();

        //llamamos al AsyncTask pasándole la URL
        tarea.execute(URL_RSS_XML_SELECTED);


    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onRefresh() {
        if (!isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(), Constantes.CONNECTION_NOT_AVIABLE,
                    Toast.LENGTH_LONG).show();
        } else {
            CargarXmlTask tarea = new CargarXmlTask();
            tarea.execute(URL_RSS_XML_SELECTED);
        }
    }

    //Tarea Asíncrona para cargar un XML en segundo plano -
    //Necesario en versiones superiores a Android 3.0
    private class CargarXmlTask extends AsyncTask<String, Integer, Boolean> {
        boolean emptyDatabase = false;

        protected Boolean doInBackground(String... params) {
            //comprobacion de la conexion a internet
            if (!isNetworkAvailable()) {
                //comprobación si almenos una vez se conectó a internet el dispositivo
                if (!firstTime) {
                    emptyDatabase = true;
                } else {
                    rssList = OperationsBBDD.allNews(sqlDatabase, rssList, URL_RSS_XML_SELECTED);
                }
            } else {
                //añade clave para indicar que almenos hubo una conexion a la red
                editor = sharedPreferences.edit();
                editor.putBoolean(Constantes.SHARED_PREFERNCES_FIRST_TIME, true);
                editor.apply();

                //Parsea el xml mediante el link pasado por parametro
                RssParserSax saxparser = new RssParserSax(params[0]);

                //pasa datos parseados a la lista
                rssList = saxparser.parse();

                //registramos esta lista de objetos en la base de datos del dispositivo
                OperationsBBDD.insertNewItems(sqlDatabase, rssList, URL_RSS_XML_SELECTED);
                rssList.clear();

                //obtenemos desde la BBDD una lista con los datos guardados
                //esto se debe a que la aplicación cargará datos desde la base de datos
                rssList = OperationsBBDD.allNews(sqlDatabase, rssList, URL_RSS_XML_SELECTED);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (!emptyDatabase) {
                AdaptadorTitulares adaptador = new AdaptadorTitulares(getApplicationContext());
                listViewNews.setAdapter(adaptador);
                refreshNews.setRefreshing(false);
                ocultarSplashConAnimation();
            } else {
                Toast.makeText(getApplicationContext(), Constantes.DATABASE_IS_EMPTY,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void ocultarSplashConAnimation() {
        splashMain.animate()
                .translationY(splashMain.getHeight())
                .alpha(0.0f)
                .setDuration(3000)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        splashMain.setVisibility(View.GONE);
                    }
                });
    }

    class AdaptadorTitulares extends ArrayAdapter<ReadRss> {

        public AdaptadorTitulares(Context context) {
            super(context, R.layout.item_noticia, rssList);
        }


        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inﬂater = LayoutInflater.from(getContext());
            View item = inﬂater.inflate(R.layout.item_noticia, null);
            ImageView image = (ImageView) item.findViewById(R.id.news_image);

            //libreria Picasso: como vamos a usar una funcionalidad offline, cargaremos las imágnes desde la caché
            //por lo que no hará falta realizar un hilo Async para descargar la imagen
            Picasso.with(getApplicationContext()).load(rssList.get(position).getImage()).into(image);

            TextView lblTitulo = (TextView) item.findViewById(R.id.news_title);
            lblTitulo.setText(rssList.get(position).getTitle());
            TextView lblSubtitulo = (TextView) item.findViewById(R.id.news_detail);
            lblSubtitulo.setText(rssList.get(position).getDescription());

            return (item);
        }
    }
}

