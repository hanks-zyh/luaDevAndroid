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