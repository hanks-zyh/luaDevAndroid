package pub.hanks.luadevandroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.luajava.LuaException;
import com.luajava.LuaState;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import androlua.LuaContext;
import androlua.LuaDexLoader;
import androlua.LuaManager;

public class MainActivity extends AppCompatActivity implements LuaContext {

    private ViewGroup luaContent;
    private EditText etUrl;
    private LuaState L;
    private LuaManager luaManager;
    private LuaDexLoader luaDexLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isCheckPermission()) {
            requestPermission();
        }

        luaContent = findViewById(R.id.lua_content);
        etUrl = findViewById(R.id.et_url);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    private boolean isCheckPermission() {
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, 0x233);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0x233: {
                if (!isCheckPermission()) {
                    requestPermission();
                } else {
                    initLuaEnv();
                }
            }
        }
    }


    private void initLuaEnv() {
        if (luaManager == null) {
            luaManager = LuaManager.getInstance();
            luaManager.init(this);
        }

        if (L == null) {
            L = luaManager.initLua(this);
        }


        if (luaDexLoader == null) {
            luaDexLoader = new LuaDexLoader();
            try {
                luaDexLoader.loadLibs();
            } catch (LuaException e) {
                e.printStackTrace();
            }
        }

        // copy assets files to sdcard

        try {
            String outputDir = luaManager.getLuaDir();
            String[] files = getAssets().list("lua");
            if (files == null) {
                return;
            }
            for (String file : files) {
                copyFile(getAssets().open("lua/" + file), outputDir + "/" + file);
            }
            luaManager.appendLuaDir(L, outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<ClassLoader> getClassLoaders() {
        return luaDexLoader.getClassLoaders();
    }

    public HashMap<String, String> getLibrarys() {
        return luaDexLoader.getLibrarys();
    }

    public static void copyFile(InputStream inStream, String newPath) throws IOException {
        int len;
        FileOutputStream fs = new FileOutputStream(newPath);
        byte[] buffer = new byte[4096];
        while ((len = inStream.read(buffer)) != -1) {
            fs.write(buffer, 0, len);
        }
        fs.flush();
        fs.close();
        inStream.close();
    }

    private void loadLuaContent() {
        luaContent.removeAllViews();
        String url = etUrl.getText().toString();
        Object o = null;
        try {
            if (L == null) {
                initLuaEnv();
            }
            luaManager.doFile(L, url);
            o = luaManager.runFunc(L, "getView");
        } catch (LuaException e) {
            e.printStackTrace();
        }
        if (o instanceof View) {
            luaContent.addView((View) o);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (L != null) {
            L.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem send = menu.add("send");
        send.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        send.setIcon(android.R.drawable.ic_menu_send);
        menu.add("refresh").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String title = item.getTitle().toString();
        if ("refresh".equals(title)) {
            loadLuaContent();
        } else if ("send".equals(title)) {
            loadLuaContent();
        }
        return super.onOptionsItemSelected(item);
    }
}
