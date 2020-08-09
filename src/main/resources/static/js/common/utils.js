/**
 * 工具函数
 * @author [chenjy]
 * @date 2018-05-11
 */
define(['constant'], function (constant) {

    /**
     * 解析url返回对应参数
     * @author [刘耀填]
     * @date 2018/09/11
     * @param url 页面url
     * @returns {Object} url后参数
     */
    function getUrlParams(url) {
        var params = {};
        if (url.indexOf("?") !== -1) {
            var str = url.substr(url.indexOf("?") + 1);
            strs = str.split("&");
            for (var i = 0; i < strs.length; i++) {
                params[strs[i].split("=")[0]] = decodeURI(strs[i].split("=")[1]);
            }
        }
        return params;
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
        mui.openWindow({
            id: 'newW',
            url: url + parameter
       });
    }
    /**
     * @author 徐尚华
     * @date 2018/5/14
     * @Description: ajax请求 支持get和post,异步和同步
     */
    function ajax(option) {
        var type = option.type || 'get';
        var url = option.url;
        var contentType = option.contentType || 'application/x-www-form-urlencoded;charset=UTF-8';
        var data = option.data || '';
        var dataType = option.dataType || 'json';
        // 默认异步
        var async = true;
        if (option.async === false) {
            async = false;
        }
        var cache = false;
        if (option.cache === true) {
            cache = true;
        }
        var timeout = option.timeout || 20000;
        jQuery.support.cors = true; // 允许跨域请求
        $.ajax({
            type: type,
            url: url,
            contentType: contentType,
            data: data,
            dataType: dataType,
            async: async,
            cache: cache,
            timeout: timeout,
            xhrFields: {withCredentials: true},
            success: function (res) {
                switch (res.code) {
                    case 401:  // 未登录
                        if (window != top) {
                            top.location.href = location.href;
                        } else {
                            window.location.href = constant.URL.PATH_LOGIN;
                        }
                        break;
                    case 7001:  // 权限不足
                        layer.msg(res.msg, {icon: 2});
                        break;

                    default:
                        if (typeof option.success == 'function') {
                            option.success(res);
                        }
                }
            },
            error: function (jqXHR) {
                // console.log(jqXHR);
                if (jqXHR.status === 401 ) {
                    layer.msg(jqXHR.responseJSON.msg, {icon: 2});
                    return;
                }

                if (typeof option.error == 'function') {
                    option.error(jqXHR);
                }
            }
        })
    }

    function addToken(targetUrl, token) {

        var successUrl = '';
        if (targetUrl.indexOf('?') !== -1) {
            // 原路径已经存在查询参数
            if (targetUrl.indexOf('token') !== -1) {
                // 且存在旧token,要先移除
                successUrl = targetUrl.substring(0, targetUrl.indexOf('token')) + 'token=' + token;
            } else {
                successUrl = targetUrl + '&token=' + token;
            }
        } else {
            // 原路径不存在查询参数
            successUrl = targetUrl + '?token=' + token;
        }
        return successUrl;
    }

    String.format = function() {
        if (arguments.length == 0)
            return null;
        var str = arguments[0];
        for ( var i = 1; i < arguments.length; i++) {
            var re = new RegExp('\\{' + (i - 1) + '\\}', 'gm');
            str = str.replace(re, arguments[i]);
        }
        return str;
        /*
        调用方式：
            var info = "我喜欢吃{0}，也喜欢吃{1}，但是最喜欢的还是{0},偶尔再买点{2}。";
            var msg=String.format(info , "苹果","香蕉","香梨")
            alert(msg);
            输出:我喜欢吃苹果，也喜欢吃香蕉，但是最喜欢的还是苹果,偶尔再买点香梨。
        */
    };

    // 下横线 转 驼峰
    /*console.log(toCamel('test_to_camel')); //testToCamel*/
    function toCamel(str) {
        str = str.replace(/([^_])(?:_+([^_]))/g, function ($0, $1, $2) {
            return $1 + $2.toUpperCase();
        });
        // 首字母大写
        return str.slice(0,1).toUpperCase() + str.slice(1);
    }

    // 驼峰 转 下横线
    /*console.log(toLowerLine("TestToLowerLine"));  //test_to_lower_line*/
    function toLowerLine(str) {
        var temp = str.replace(/[A-Z]/g, function (match) {
            return "_" + match.toLowerCase();
        });
        if(temp.slice(0,1) === '_'){ //如果首字母是大写，执行replace时会多一个_，这里需要去掉
            temp = temp.slice(1);
        }
        return temp;
    };


    // 初始化表单值
    function  initFormData(formId, data, notReset) {
        if(!notReset){
            $(formId)[0].reset();
        }
        $.each(data, function (key, value) {
            $(formId).find("[name='" + key +"']").val(value);
        })
    }

    // 获取form表单数据
    function getFormData($form) {
        var arrayData = $form.serializeArray();
        var formData = {};
        // 去除两边空格
        $.map(arrayData, function (item, i) {
            if(item['name'].indexOf("Check") > 0 && item['value'] =="on"){
                formData[item['name']] = true;
            } else {
                formData[item['name']] = $.trim(item['value']);
            }
        });
        return formData;
    }

    function checkFormData($form){
        var bl = true;
        $form.find(".require").each(function (index, item) {
            if($.trim($(item).val()) === ''){
                layer.msg("请填写必填项", {icon: 2});
                $(item).focus();
                bl = false;
                return false;
            }
        });
        return bl;
    }

    /**
     * 导出Excel
     * @param api 请求接口
     * @param params 请求参数
     */
    function exportData(api, params) {
        var form = $('<form>'); //定义一个form表单
        form.attr("style", "display:none");
        form.attr("target", "_blank");
        form.attr("method", "post"); //请求类型
        form.attr("action", api); //请求地址
        $("body").append(form); //将表单放置在web中
        var input1 = $("<input>");
        input1.attr("type", "hidden");
        input1.attr("name", "jsonString");
        input1.attr("value", JSON.stringify(params));
        form.append(input1);
        form.submit(); // 发送请求
    }

    return {
        'ajax': ajax,
        'getUrlParams': getUrlParams,
        'goUrlModule':goUrlModule,
        'addToken': addToken,
        'toCamel': toCamel,
        'toLowerLine': toLowerLine,
        'getFormData': getFormData,
        'initFormData': initFormData,
        'checkFormData': checkFormData,
        'exportData': exportData,
    }
});
