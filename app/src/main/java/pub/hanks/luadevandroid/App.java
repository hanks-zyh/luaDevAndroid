package pub.hanks.luadevandroid;

import android.app.Application;

import androlua.LuaManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LuaManager.getInstance().setDebugable(false).init(this);
    }
}
