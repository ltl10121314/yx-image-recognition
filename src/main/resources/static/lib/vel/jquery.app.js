/**
 * Velonic Admin theme 
 * @author [chenjy]
 * @date 2018-05-11
 */

define(function(){
    function init(){

        ! function($) {
            "use strict";

            /**
            Sidebar Module
            */
            var SideBar = function() {
                this.$body = $("body"),
                this.$sideBar = $('aside.left-panel'),
                this.$navbarToggle = $(".navbar-toggle"),
                this.$navbarItem = $("aside.left-panel nav.navigation > ul > li > a"),
                this.$secondMenu = $("aside.left-panel nav.navigation > ul li.second-menu > a")
            };

            //initilizing 
            SideBar.prototype.init = function() {
                //on toggle side menu bar
                var $this = this;
                $(document).on('click', '.navbar-toggle', function () {
                    if(!$this.$sideBar.hasClass('collapsed')) {
                        $this.$sideBar.find('a').next('ul').hide();
                    } else {
                        $this.$sideBar.find('li.active').children('ul').show();
                    }

                    $this.$sideBar.toggleClass('collapsed');
                    $('header.top-head.container-fluid').toggleClass('collapsed');
                });

                //on menu item clicking (1) 菜单点击 展开/收缩
                this.$navbarItem.click(function () {
                    if ($this.$sideBar.hasClass('collapsed') == false || $(window).width() < 768) {
                        $("aside.left-panel nav.navigation > ul > li > ul").slideUp(300);
                        $("aside.left-panel nav.navigation > ul > li").removeClass('active');
                        if (!$(this).next().is(":visible")) {
                            $(this).next().slideToggle(300, function () {
                                $("aside.left-panel:not(.collapsed)").getNiceScroll().resize();
                            });
                            $(this).closest('li').addClass('active');
                        }
                        // return false; 注释掉，允许冒泡到自定义的父级事件
                    }
                });

                // (2) 二级菜单点击 展开/收缩
                this.$secondMenu.click(function () {
                    $("aside.left-panel nav.navigation > ul li.second-menu > ul").slideUp(200);
                    $("aside.left-panel nav.navigation > ul li.second-menu").removeClass('active');
                    if (!$(this).next().is(":visible")) {
                        $(this).next().slideToggle(300, function () {
                            $("aside.left-panel:not(.collapsed)").getNiceScroll().resize();
                        });
                        $(this).closest('li').addClass('active');
                    }
                    return false;
                });

                // 鼠标移动
                $('aside.left-panel').on("mouseenter", 'nav.navigation > ul > li:has(ul) > a', function (ev) {
                    ev.preventDefault();
                    var $aside = $(ev.delegateTarget);
                    if ($aside.hasClass('collapsed')) {
                        $(this).next().find('.active > ul').show(); // 三级菜单所在ul
                    }

                });


                //adding nicescroll to sidebar
                if ($.isFunction($.fn.niceScroll)) {
                    $("aside.left-panel:not(.collapsed)").niceScroll({
                        cursorcolor: '#8e909a',
                        cursorborder: '0px solid #fff',
                        cursoropacitymax: '0.5',
                        cursorborderradius: '0px'
                    });
                }
            },

            //exposing the sidebar module
            $.SideBar = new SideBar, $.SideBar.Constructor = SideBar
            
        }(window.jQuery),


        //portlets
        function($) {
            "use strict";

            /**
            Portlet Widget
            */
            var Portlet = function() {
                this.$body = $("body"),
                this.$portletIdentifier = ".portlet",
                this.$portletCloser = '.portlet a[data-toggle="remove"]',
                this.$portletRefresher = '.portlet a[data-toggle="reload"]'
            };

            //on init
            Portlet.prototype.init = function() {
                // Panel closest
                var $this = this;
                $(document).on("click",this.$portletCloser, function (ev) {
                    ev.preventDefault();
                    var $portlet = $(this).closest($this.$portletIdentifier);
                        var $portlet_parent = $portlet.parent();
                    $portlet.remove();
                    if ($portlet_parent.children().length == 0) {
                        $portlet_parent.remove();
                    }
                });

                // Panel Reload
                $(document).on("click",this.$portletRefresher, function (ev) {
                    ev.preventDefault();
                    var $portlet = $(this).closest($this.$portletIdentifier);
                    // This is just a simulation, nothing is going to be reloaded
                    $portlet.append('<div class="panel-disabled"><div class="loader-1"></div></div>');
                    var $pd = $portlet.find('.panel-disabled');
                    setTimeout(function () {
                        $pd.fadeOut('fast', function () {
                            $pd.remove();
                        });
                    }, 500 + 300 * (Math.random() * 5));
                });
            },
            //
            $.Portlet = new Portlet, $.Portlet.Constructor = Portlet
            
        }(window.jQuery),
        

        //main app module
        function($) {
            "use strict";
            
            var VelonicApp = function() {
                this.VERSION = "1.0.0", 
                this.AUTHOR = "Coderthemes", 
                this.SUPPORT = "coderthemes@gmail.com", 
                this.pageScrollElement = "html, body", 
                this.$body = $("body")
            };

            //initializing tooltip
            VelonicApp.prototype.initTooltipPlugin = function() {
                $.fn.tooltip && $('[data-toggle="tooltip"]').tooltip()
            }, 

            //initializing popover
            VelonicApp.prototype.initPopoverPlugin = function() {
                $.fn.popover && $('[data-toggle="popover"]').popover()
            }, 

            //initializing nicescroll
            VelonicApp.prototype.initNiceScrollPlugin = function() {
                //You can change the color of scroll bar here
                $.fn.niceScroll &&  $(".nicescroll").niceScroll({ cursorcolor: '#9d9ea5', cursorborderradius: '0px'});
            },

            //initializing knob
            VelonicApp.prototype.initKnob = function() {
                if ($(".knob").length > 0) {
                    $(".knob").knob();
                }
            },
            
            //initilizing 
            VelonicApp.prototype.init = function() {
                this.initTooltipPlugin(),
                this.initPopoverPlugin(),
                this.initNiceScrollPlugin(),
                this.initKnob(),
                //creating side bar
                $.SideBar.init(),
                //creating portles
                $.Portlet.init();
            },

            $.VelonicApp = new VelonicApp, $.VelonicApp.Constructor = VelonicApp

        }(window.jQuery),

        //initializing main application module
        function($) {
            "use strict";
            $.VelonicApp.init()
        }(window.jQuery);


        /* ==============================================
        7.WOW plugin triggers animate.css on scroll
        =============================================== */
        var wow = new WOW(
            {
                boxClass: 'wow', // animated element css class (default is wow)
                animateClass: 'animated', // animation css class (default is animated)
                offset: 50, // distance to the element when triggering the animation (default is 0)
                mobile: false        // trigger animations on mobile devices (true is default)
            }
        );
        wow.init();

    }
    return {
        "init": init
    }
});