define(['utils', 'api', 'constant', 'common', 'jquery_app'], function(utils, api, constant, common, jquery_app) {

    function init() {
        bindEvents(); // 绑定事件
        getUserMenu(); // 获取用户权限下菜单列表  生成侧边栏菜单
    }

    // 判断是否要打开新的标签页
    function openNewTab() {
        var params = utils.getUrlParams(window.location.href); // 获取页面 跳转路径,打开一个新的tab页
        if (params.redirect_url !== undefined) {
            open_addTab(params.redirect_url);
        }
    }

    function bindEvents() {
        $('#logout').on('click', function(e) {
            logout();
        });

        $('#ownerHtml').on('click', 'a', function(e) {
            var ownerId = $(this).attr("id");
            changeOwner(ownerId);
        });

        // 点击之后，右键菜单隐藏
        $(document).click(function() {
            $("#contextmenu").hide();
        });

    }

    
    /**
     * 获取用户权限下菜单列表
     */
    function getUserMenu() {
        function requestFunction(ret) {
            if (ret.code === 200) {
                var returnData = buildMenuHtml(ret.obj);
                $('#asideMenu').html(returnData.menusHtml);
                jquery_app.init();

                var defaultLoadMenu = returnData.defaultLoadMenu; // 默认加载菜单
                // var token = localStorage.getItem('token'); // 登录时存的token

                var url = defaultLoadMenu.menuUrl;
                open_addTab(url, defaultLoadMenu.menuName, 'menu-' + defaultLoadMenu.id, false);
                openNewTab(); // 判断是否有新的标签页需要打开
                $('#headBar').on('click', function() {

                    if ($('#iframe_body').find('iframe').length) {
                        $('#iframe_body').show();
                        $('#content_body').hide();
                    } else {
                        var url = constant.ONLY.STYSTEM_URL + 'cn/visualise/visualise.html' + '?token=' + token;
                        var html = '<iframe src="' + url + '" frameborder="0" class="visualise-iframe w100 h100" id="visualise_iframe" allowfullscreen="true"  allow="autoplay; fullscreen"></iframe>';
                        $('#iframe_body').append(html);
                        $('#iframe_body').show();
                        $('#content_body').hide();
                    }
                });
            } else {
                layer.msg('code：' + ret.code + ' 信息: ' + ret.msg, {
                    icon: 2
                });
            }
        }

        var option = {
            type: 'get',
            url: api.authority.queryMenuByUser,
            data: {
                //"level": "1,2,3",
                "platform": 0
            },
            success: requestFunction
        };
        utils.ajax(option); // 发送请求
    }

    /**
     * 构建菜单html，并获取登录进来时默认要打开的第一个菜单
     * @author [刘耀填]
     * @date 2018-09-30
     */
    function buildMenuHtml(menusData) {

        var defaultLoadMenu = null; // 默认要打开的菜单（第一个菜单下menuUrl不为空的菜单（即页面））
        var defaultLoadMenuParentId = ''; // 登录进来默认打开的菜单的父菜单id
        var menusHtml = '';
        var level1Menus = menusData.first,
            level2Menus = menusData.second,
            level3Menus = menusData.third;

        var pageAuthorityCodes = []; // 页面权限编码
        var pageMenus = [];
        for (var i = 0; i < level1Menus.length; i++) { // 一级
            var level1 = level1Menus[i];

            // ------判断默认加载菜单（1）-------
            if (i === 0) {
                if (level1.menuUrl !== '') { // 是模块
                    // 只有一级菜单
                    defaultLoadMenu = level1;
                } else { // 是页面
                    defaultLoadMenuParentId = level1.id;
                }
            }
            // ------判断默认加载菜单-------


            if (level1.menuUrl !== '') { // 只有一级 (是页面)
                pageAuthorityCodes.push(level1.permission); // (页面权限1)
                var name = (/^fa/).test(level1.menuIcon) ? (level1.menuIcon + ' fa') : level1.menuIcon
                if (i === 0) {
                    menusHtml += '<li class="has-submenu active">\n' +
                        '                <a href="javascript:void(0)" id="menu-' + level1.id + '" h="' + level1.menuUrl + '"><i class="' + name + '"></i>\n' +
                        '                    <span class="nav-label">' + level1.menuName + '</span>\n' +
                        '                </a>\n' +
                        '            </li>';
                } else {
                    menusHtml += '<li class="has-submenu">\n' +
                        '                <a href="javascript:void(0)" id="menu-' + level1.id + '" h="' + level1.menuUrl + '"><i class="' + name + '"></i>\n' +
                        '                    <span class="nav-label">' + level1.menuName + '</span>\n' +
                        '                </a>\n' +
                        '            </li>';
                }

            } else { // 包含二级菜单

                var secondMenuHtml = ''; // 二级菜单html

                level2Menus.forEach(function(level2) {

                    // ------判断默认加载菜单（2）-------
                    if (defaultLoadMenu === null && level2.parentId === defaultLoadMenuParentId) {
                        if (level2.menuUrl !== '') {
                            // 只有两级菜单
                            defaultLoadMenu = level2;
                        } else {
                            defaultLoadMenuParentId = level2.id;
                        }
                    }
                    // ------判断默认加载菜单-------


                    if (level2.parentId === level1.id) {

                        if (level2.menuUrl !== '') { // 只有二级
                            pageAuthorityCodes.push(level2.permission); // (页面权限2)

                            secondMenuHtml += '<li><a href="javascript:void(0)" id=' + '"menu-' + level2.id + '" h="' + level2.menuUrl + '">' + level2.menuName + '</a></li>\n';

                        } else { // 包含三级菜单

                            var thirdMenuHtml = '';

                            level3Menus.forEach(function(level3) {

                                // ------判断默认加载菜单（3）-------
                                if (defaultLoadMenu === null && level3.parentId === defaultLoadMenuParentId) {
                                    // 三级菜单
                                    defaultLoadMenu = level3;
                                }
                                // ------判断默认加载菜单-------


                                if (level3.parentId === level2.id) {
                                    pageAuthorityCodes.push(level3.permission); // (页面权限3)
                                    thirdMenuHtml += '<li class="third-menu"><a href="javascript:void(0)" id=' + '"menu-' + level3.id + '" h="' + level3.menuUrl + '">' + level3.menuName + '</a></li>\n';
                                }

                            });

                            // 将二，三级拼接
                            secondMenuHtml += '<li class="has-submenu second-menu"><a href="#">' + level2.menuName + '</a>\n' +
                                '                <ul class="list-unstyled">\n' +
                                thirdMenuHtml +
                                '                </ul>\n' +
                                '            </li>'

                        }

                    }

                });
                var name = (/^fa/).test(level1.menuIcon) ? (level1.menuIcon + ' fa') : level1.menuIcon
                // 将模块的一，二级拼接
                menusHtml += '<li class="has-submenu"><a href="#"><i class="' + name + '"></i> <span class="nav-label">' + level1.menuName + '</span></a>\n' +
                    '                <ul class="list-unstyled">\n' +
                    secondMenuHtml +
                    '                </ul>\n' +
                    '            </li>'
            }
        }
        if (level1Menus) {
            for (var i = 0; i < level1Menus.length; i++) {
                pageMenus.push({
                    id:level1Menus[i].id,
                    menuUrl:level1Menus[i].menuUrl
                });
            }
        }
        if (level2Menus) {
            for (var i = 0; i < level2Menus.length; i++) {
                pageMenus.push({
                    id:level2Menus[i].id,
                    menuUrl:level2Menus[i].menuUrl
                });
            }
        }

        if (level3Menus) {
            for (var i = 0; i < level3Menus.length; i++) {
                pageMenus.push({
                    id:level3Menus[i].id,
                    menuUrl:level3Menus[i].menuUrl
                })
            }
        }
        // console.log(pageAuthorityCodes);
        sessionStorage.setItem('pageMenus', JSON.stringify(pageMenus));
        sessionStorage.setItem('pageAuthorityCodes', JSON.stringify(pageAuthorityCodes));

        return {
            menusHtml: menusHtml,
            defaultLoadMenu: defaultLoadMenu
        };

    }


    return {
        'init': init
    }
});