做移动端开发，做蛋疼的就是不能**动态发版**，不能像 web 那样发版立即全部用户生效，然而 [lua](http://www.runoob.com/manual/lua53doc/)语言 为其提供了可能性。使用 lua 来构建跨平台原生应用有许多好处，比如 lua 语言简洁高效，可移植性好， Lua虚拟机极为轻量，仅占用200到300k的内存空间，且速度极快。

## 演示

写一个简单的代码演示一下。新建一个 lua 文件，叫做 `view.lua`, 放在手机的 sdcard 上，文件目录为 /sdcard/view.lua

```lua
require "import"
import "android.widget.*"
import "android.content.*"

function getView()
    local layout = {
        LinearLayout,
        orientation = "vertical",
        layout_width = "fill",
        layout_height = "fill",
        {
            Button,
            id = "btn",
            layout_marginTop="8dp",
            layout_width = "fill",
            layout_height = "50dp",
            text = "click"
        },
    }
    local view = loadlayout(layout)
    return view
end

```
运行一下，

![](https://user-gold-cdn.xitu.io/2018/11/18/167261767ddce265?w=424&h=746&f=gif&s=33320)

屏幕中上半部分是 Android 的 xml 布局中写好的代码，当点击运行按钮时，加载 lua 脚本，返回一个 View 对象，然后添加到布局中。一个简单的 lua 脚本编写的视图就写好了。
接下来修改一下，设置个点击事件。

```lua
require "import"
import "android.widget.*"
import "android.content.*"

function getView()
    local layout = {
        LinearLayout,
        orientation = "vertical",
        layout_width = "fill",
        layout_height = "fill",
        {
            Button,
            id = "btn",
            layout_marginTop="8dp",
            layout_width = "fill",
            layout_height = "50dp",
            text = "click"
        },
    }
    local ids = {} -- store ids to find view
    local view = loadlayout(layout, ids)
    ids.btn.onClick = function()
        Toast.makeText(activity,"2333",0).show()
    end
    return view
end
```
运行效果

![](https://user-gold-cdn.xitu.io/2018/11/18/1672617fce0cf29f?w=424&h=746&f=gif&s=112338)

再来个稍微复杂点的例子，写个列表，新建 `list.lua` 文件，放在手机的 sdcard/list.lua

```lua
require "import"
import "android.widget.*"
import "android.content.*"
import "android.view.View"
import "androlua.LuaHttp"
import "androlua.LuaAdapter"
import "androlua.LuaImageLoader"

local JSON = require("cjson")
local uihelper = require('uihelper')

-- create view table
local layout = {
    LinearLayout,
    orientation = "vertical",
    layout_width = "fill",
    layout_height = "fill",
    {
        ListView,
        id = "listview",
        dividerHeight = 0,
        layout_width = "fill",
        layout_height = "fill",
    },
}

local item_view = {
    FrameLayout,
    layout_width = "fill",
    layout_height = "240dp",
    {
        ImageView,
        id = "iv_image",
        layout_width = "fill",
        layout_height = "fill",
        scaleType = "centerCrop",
    },
    {
        TextView,
        id = "tv_title",
        background = "#66000000",
        layout_width = "fill",
        layout_height = "fill",
        padding = "32dp",
        gravity = "center",
        maxLines = "5",
        lineSpacingMultiplier = '1.2',
        textSize = "14sp",
        textColor = "#CCFFFFFF",
    },
}


local data = {
    dailyList = {}
}
local adapter

local function getData()
    -- http://baobab.kaiyanapp.com/api/v1/feed
    local url = data.nextPageUrl
    if url == nil then url = 'http://baobab.kaiyanapp.com/api/v1/feed?udid=3e7ee30c6fc0004a773dc33b0597b5732b145c04' end
    if url:find('udid=') == nil then url = url .. '&udid=3e7ee30c6fc0004a773dc33b0597b5732b145c04' end
    print(url)
    LuaHttp.request({ url = url }, function(error, code, body)
        if error or code ~= 200 then
            print('fetch data error')
            return
        end
        local str = JSON.decode(body)
        uihelper.runOnUiThread(activity, function()
            data.nextPageUrl = str.nextPageUrl
            local list = str.dailyList[1].videoList
            for i = 1, #list do
                data.dailyList[#data.dailyList + 1] = list[i]
            end
            adapter.notifyDataSetChanged()
        end)
    end)
end

local function launchDetail(item)
    Toast.makeText(activity, item.title, 0).show()
end

function getView()
    local view = loadlayout(layout)
    adapter = LuaAdapter(luajava.createProxy("androlua.LuaAdapter$AdapterCreator", {
        getCount = function() return #data.dailyList end,
        getItem = function(position) return nil end,
        getItemId = function(position) return position end,
        getView = function(position, convertView, parent)
            position = position + 1 -- lua 索引从 1开始
            if position == #data.dailyList then
                getData()
            end
            if convertView == nil then
                local views = {} -- store views
                convertView = loadlayout(item_view, views, ListView)
                if parent then
                    local params = convertView.getLayoutParams()
                    params.width = parent.getWidth()
                end
                convertView.setTag(views)
            end
            local views = convertView.getTag()
            local item = data.dailyList[position]
            if item then
                LuaImageLoader.load(views.iv_image, item.coverForFeed)
                views.tv_title.setText(item.title)
            end
            return convertView
        end
    }))
    listview.setAdapter(adapter)
    listview.setOnItemClickListener(luajava.createProxy("android.widget.AdapterView$OnItemClickListener", {
        onItemClick = function(adapter, view, position, id)
            launchDetail(data.dailyList[position + 1])
        end,
    }))
    getData()
    return view
end

```

创建 listView ， 设置 adapter ，网络请求，刷新列表。看下效果吧。


![](https://user-gold-cdn.xitu.io/2018/11/18/16726332b91dafd7?w=424&h=746&f=gif&s=5205132)


代码放到了 github 👉 [源码](https://github.com/hanks-zyh/luaDevAndroid)

## 原理图

![](https://user-gold-cdn.xitu.io/2018/11/18/16726167db0f906f?w=960&h=720&f=jpeg&s=81945)

写了几篇文章比较详细的介绍了原理，想了解的可以看一下
- [Android 与 Lua](https://juejin.im/post/5beeeb7cf265da616c6530f8)
- [探究 lua 在 Android 中的应用](https://juejin.im/post/5beeeaa651882546150a954f)
- [Lua 嵌入 Android 原理](https://hanks.pub/2017/09/28/lua-into-android/)

## 支持 iOS 吗？
Lua 是用 c 语言开发的，可移植性比较好，想支持 iOS 的话，原理时一样的，不过参考目前已有的跨平台技术。关于跨平台方面的一些个人见解，目前已有的跨平台技术每当涉及到不同平台的特性时，事情就比较蛋疼了，需要单独去适配，还有建立一堆连接库，比如选取本地图片，不同平台的数据库，平台特有 api，真是**一份代码到处运行终是梦，一份儿代码到处采坑才是真**。

## Android 开发能支持到什么程度？

看到了上面的原理图就可以知道，支持 Android SDK 几乎所有的 API。


## 联系我
- github:[hanks-zyh](https://github.com/hanks-zyh)
- weibo: [@D蓝小鱼](http://weibo.com/hanksZyh)
- twitter: [hanks](https://twitter.com/zhangyuhan3030)
- email: [zhangyuhan2014@gmail.com](mailto:zhangyuhan2014@gmail.com)


