/**
 * 基础函数
 * @author [chenjy]
 * @date 2018-05-11
 */
define(['common', 'utils', 'jquery_app', 'template', 'api', 'constant'], function(common, utils, jquery_app, template, api, constant) {

	function init() {
		open_initTab();
        initMenu();
        initSize();
        bindEvent();
    }

    function initMenu() {
        // 点击侧边栏菜单 或 修改密码
        $(".navigation, #updatePassword").on('click', 'a', function(e) {

            var href = $(this).attr("href");

            // 折叠后的侧边栏，一级菜单为#
            if (href === '#') {
                return;
            }
            
            var url = $(this).attr("h");
            var txt = $(this).text();
            var menuId = $(this).attr("id");

            open_addTab(url, txt, menuId);
            return false;
        });

    }

    function bindEvent() {
        $('#closeOtherTab').click(function(ev) {
            close_other_tab2();
        });
        $('#closeThisTab').click(function(ev) {
            close_current_tab();
        });
        $('#closeAllTab').click(function(ev) {
            close_all_tab();
        });
        // 下拉菜单展开时，关闭选项卡右键菜单
        $('.dropdown').on('show.bs.dropdown', function() {
            $("#contextmenu").hide();
        })
    }

    function initSize() {
        $(".ifr").height($(window).height() - 54);
        $(window).resize(function() {
            $(".ifr").height($(window).height() - 104);
        });
    }

    return {
        "init": init
    }
});