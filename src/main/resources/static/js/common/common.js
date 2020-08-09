/**
 * 公共函数
 * @author [MC]
 * @date 2018-05-11
 */
define(['constant'], function(constant) {

    //跨域调用父级方法
    function execParentFun(funName, paraJson) {
        var para = "t=" + Math.random() + "&funName=" + funName;
        $.each(paraJson, function(name, value) {
            para += (para == "" ? "" : "&");
            para += name + "=" + escape(value);
        });
        if (typeof(exec_obj) == 'undefined') {
            exec_obj = document.createElement('iframe');
            exec_obj.name = 'tmp_frame';
            exec_obj.src = constant.URL.WEB_URL + 'cn/components/execJs.html?' + para;
            exec_obj.style.display = 'none';
            document.body.appendChild(exec_obj);
        } else {
            exec_obj.src = constant.URL.WEB_URL + 'cn/components/execJs.html?' + para;
        }
    }

    function G(id) {
        return document.getElementById(id);
    }

    var UI = new Object();
    //事件注册
    UI.register = function(id, even, fun, arr) {
        if (G(id)) {
            G(id)["on" + even] = function() {
                fun(arr);
            };
        }
    }

    /**
     * @desc 页面路径跳转
     * @param [url] 跳转路径
     * @param [parameterList] 参数对象集合 {name value}
     * @author [张君培]
     * @date 2018/08/13
     */
    function goUrlModule(url, parameterList) {
        var parameter = '';
        for (var i = 0; i < parameterList.length; i++) {
            var symbol = i === 0 ? '?' : '&';
            var o = parameterList[i];
            parameter += symbol + o.name + '=' + o.value;
        }
        window.location.href = url + parameter;
    }

    return {
        'UI': UI,
        'goUrlModule': goUrlModule,
        'execParentFun': execParentFun
    }

});

function open_initTab() {
    // 关闭 （off防止多次绑定，重复执行。最好是使用事件委托）
    $(".icon-close").off('click').click(function() {
        var v = $(this).parent().attr("for");
        $("#" + v).remove();
        var fordiv = $(this).parent().attr("fordiv");
        $("#" + fordiv).remove();
        $(this).parent().remove();

        $("input[name='grp']").eq($("input[name='grp']").length - 1).prop("checked", "checked");
    });

    // 弹出右键菜单 （off防止多次绑定，重复执行。最好是使用事件委托）
    $('.ifr').find('label').off('contextmenu').contextmenu(function(e) {
        e.preventDefault();
        e.stopPropagation();
        $('.dropdown').removeClass('open');

        window.currentRightClickLabel = this;

        // 获取窗口尺寸
        var winWidth = $(document).width();

        // ul标签的宽高
        var menuWidth = $("#contextmenu").width();

        // 最小边缘margin(具体窗口边缘最小的距离)
        var minEdgeMargin = 10;

        var mouseX = e.clientX,
            mouseY = e.clientY;

        var menuLeft;
        var menuTop;

        if (mouseX + menuWidth + minEdgeMargin >= winWidth) {
            menuLeft = mouseX - menuWidth - minEdgeMargin + "px";
            menuTop = mouseY + minEdgeMargin + "px";
        } else {
            menuLeft = mouseX + minEdgeMargin + "px";
            menuTop = mouseY + minEdgeMargin + "px";
        }

        var oMenu = $('#contextmenu')[0];

        oMenu.style.display = "block";
        oMenu.style.left = menuLeft;
        oMenu.style.top = menuTop;

    })

}

//添加选项卡
function open_addTab(url, title, tabid, refresh) {
    var titleName = activeMenu(url);
    if (title == null || title == "null" || title == undefined) {
        title = titleName;
    }

    var id = "tab-" + tabid;

    //判断是否已存在
    var isHas = false;
    if (sessionStorage.pageMenus) {
        var pageMenus = JSON.parse(sessionStorage.pageMenus) || [];
        for (var i = 0; i < pageMenus.length; i++) {
            var urlList = url.split('/');
            var lastUrl =  urlList[urlList.length - 1].split('?')[0];
            var urlText = urlList[urlList.length - 2] + '/' + lastUrl;
            var menuList = pageMenus[i].menuUrl.split('/');
            var menuText = menuList[menuList.length - 2] + '/' + menuList[menuList.length - 1];
            if (urlText == menuText) {
                id = "tab-" + 'menu-' + pageMenus[i].id;
            }
        }
    }

    $('input[name="grp"]').each(function(index, obj) {
        if ($(obj).attr("id") == id && $(obj).css("display") == "none") {
            $("#" + id).prop("checked", "checked");
            $("#" + id).next().css("display", "");
            $("#" + id).next().next().css("display", "");
            isHas = true;
            return;
        }
    });

    if (isHas) {
        if (refresh == 'new') { //新的数据，参数切换
            $('#' + id).attr('h', url);
            var iframe = document.getElementById("iframe-" + id);
            $('#iframe-' + id).attr('src', url);
            return;
        }
        if (refresh) {
            var iframe = document.getElementById("iframe-" + id);
            iframe.contentWindow.location.reload(true);
            iframe.src = url;
        }
        return;
    }

    $("#operateTab").before("<input id=\"" + id + "\" type=\"radio\" name=\"grp\" h=\"" + url + "\" checked=\"\">" +
        "<label for=\"" + id + "\" fordiv=\"tabdiv-" + id + "\">" +
        "<div class=\"ifr-tab-title\">" + title + "</div>" +
        "<div class=\"icon-list ionicon-list icon-close\"><div class=\"col-md-3 col-sm-4\"><i class=\"ion-ios7-close-empty\"></i></div></div>" +
        "</label> " +
        "<div class=\"tab-item\" id=\"tabdiv-" + id + "\">" +
        "<iframe src=\"" + url + "\"  id=\"iframe-" + id + "\" frameborder=\"0\" class=\"fw-admin-iframe\" allowfullscreen=\"true\"  allow=\"autoplay; fullscreen\"></iframe>" +
        "</div>");

    $("#" + id).attr('checked', 'true');

    open_initTab();
}

//高亮菜单
function activeMenu(url) {
    var aList = $(".list-unstyled").find("a");
    $(".active").removeClass("active");
    for (var i = 0; i < aList.length; i++) {
        if (url.split('?')[0] === $(aList[i]).attr("h")) {
            $(aList[i]).parent().addClass("active");
            $(aList[i]).parent().parent().parent().addClass("active");
            $(aList[i]).parent().parent().parent().parent().parent().addClass("active");
            return $(aList[i]).html();
        }
    }
}

// 关闭选项卡
function close_tab(closeTabId, openTabId) {
    var $tab = $("#tab-" + closeTabId); // 必须先删除子元素
    $tab.next().remove();
    $tab.remove();
    $('#tabdiv-tab-' + closeTabId).remove();

    if (openTabId) {
        $('#tab-' + openTabId).prop("checked", true);
    } else {
        $("input[name='grp']").eq($("input[name='grp']").length - 1).prop("checked", "checked");
    }
}

// 关闭未选中的选项卡
function close_other_tab() {
    var $tabs = $('.ifr').find('input[name="grp"]:not(:checked)');
    $tabs.each(function() {
        $(this).next().next().remove();
        $(this).next().remove();
        $(this).remove();
    })
}

// 右键关闭其他选项卡
function close_other_tab2() {

    var currentRightClickLabel = window.currentRightClickLabel; // 右键点击的label
    var input = $(currentRightClickLabel).prev();

    var $tabs = $('.ifr').find('input[name="grp"]');
    $tabs.each(function() {
        if ($(input)[0].id !== this.id) {
            $(this).next().next().remove();
            $(this).next().remove();
            $(this).remove();
        }
    })
}

// 右键关闭当前选项卡
function close_current_tab() {

    var currentRightClickLabel = window.currentRightClickLabel; // 右键点击的label
    var input = $(currentRightClickLabel).prev();
    var currTabId = $(input)[0].id;

    var $tab = $('#' + currTabId);
    $tab.next().remove();
    $tab.remove();
    $('#tabdiv-' + currTabId).remove();

}

// 隐藏当前选项卡，并打开新的选项卡
function open_close_tab(url, title, openTabId) {
    var currTabId = "";
    $('input[name="grp"]').each(function(index, obj) {
        if ($(obj).prop("checked")) {
            currTabId = $(obj).attr("id").substring(4);
            return;
        }
    });

    var $tab = $("#tab-" + currTabId);
    $tab.next().hide();
    $tab.hide();
    $('#tabdiv-tab-' + currTabId).hide();
    open_addTab(url, title, openTabId, true);
}

// 关闭当前选项卡，打开新的选项卡
function close_single_tab(openTabId, url, title, refresh) {
    var currTabId = "";
    $('input[name="grp"]').each(function(index, obj) {
        if ($(obj).prop("checked")) {
            currTabId = $(obj).attr("id").substring(4);
            return;
        }
    });

    refresh = (refresh === "true") ? true : false;
    open_addTab(url, title, openTabId, refresh);
    var $tab2 = $("#tab-" + openTabId);
    $tab2.next().css("display", "");
    // $tab2.show();
    $('#tabdiv-tab-' + openTabId).css("display", "");

    $tab2.removeAttr("style");
    $('#tabdiv-tab-' + openTabId).removeAttr("style");

    var $tab = $("#tab-" + currTabId);
    $tab.next().remove();
    $tab.remove();
    $('#tabdiv-tab-' + currTabId).remove();
}

// 关闭所有选项卡
function close_all_tab() {
    var $tabs = $('.ifr').find('input[name="grp"]');
    $tabs.each(function() {
        $(this).next().next().remove();
        $(this).next().remove();
        $(this).remove();
    })
}

//用于设备系统隐藏抬头iframe
function close_frame(func, url, title, tabid, refresh) {
    $('#iframe_body').hide();
    $('#content_body').show();
    if (func == "addTab") {
        open_addTab(url, title, tabid, refresh);       
    }

}