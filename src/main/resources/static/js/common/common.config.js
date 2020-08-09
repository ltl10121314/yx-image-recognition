/**
 * common
 * js 资源配置
 *
 */
require.config({
    baseUrl: "../../",
    //urlArgs:"v=1.0.0" ,

    paths: {
        // 公共资源
        "common": "js/common/common",
        "api": "js/common/api",// 所有请求API地址
        "constant": "js/common/constant",// 常量
        "utils": "js/common/utils",
        "jquery": "lib/jquery/jquery",

        // 
        "baseMod": "js/model/baseMod",
        "template": "lib/template/template-web",
        "jquery_app": "lib/vel/jquery.app",

        // 组件
        "tableMod": "js/model/components/tableMod", // DataTable二次封装
        "imgUploadMod": "js/model/components/imgUploadMod",


        // 主页模块
        "homeMod": "js/model/home/homeMod",
        "homeContentMod": "js/model/home/homeContentMod",

        // 车牌识别模块
        "plateContentMod": "js/model/plate/plateContentMod"
    }
});
    
    