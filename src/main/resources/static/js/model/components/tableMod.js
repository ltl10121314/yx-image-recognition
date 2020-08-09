/**
 * DataTable二次封装
 * @author [刘耀填]
 * @date 2018-10-08
 */
define(['constant', 'utils'], function(constant, utils){

    var table = {
        "instance": null,
        "api": '',
        "queryParams": {}
    };

    // 初始化Datatables实例 并 绑定行点击事件
    function init(option) {

        var tableId = option.id;
        var $table = $('#' + tableId);
        var ajaxFun = option.ajax;

        if (typeof ajaxFun !== "function") {
            var ajaxSetting = option.ajaxSetting;
            if (typeof ajaxSetting.api === 'string') {
                table.queryParams = ajaxSetting.queryParamsDefault || {};
                table.api = ajaxSetting.api;
                ajaxFun = createAjaxFun;
            } else {
                alert('ajaxSetting配置错误！');
            }
        }

        table.instance = $table.DataTable({
            "select": false,
            "scrollX": option.scrollX,
            "scrollY": option.scrollY,
            "language": constant.DATATABLE_LANGUAGE,
            "processing": true, // 加载提示
            "serverSide": true, // 启用服务器端分页
            "searching": option.searching, // 禁用原生搜索
            "orderMulti": false, // 启用多列排序
            "ordering": false, // 排序查询
            /*"autoWidth": false,*/
            "lengthMenu": [15, 30, 50], // 每页显示条数，默认10
            "bLengthChange": true,
            "pagingType": "full_numbers",  // 分页样式：simple,simple_numbers,full,full_numbers
            "ajax": ajaxFun,
            "rowId": option.rowId ? option.rowId : "id",
            "columns": option.columns
        });

        // 行点击事件
        $table.find('tbody').on('click', 'tr', function () {
            $(this)
                .addClass('selected')
                .siblings('.selected').removeClass('selected'); // 高亮显示
        });

        return table.instance;

    }

    function createAjaxFun(data, callback, settings) {

        var ajaxOption = {
            type: 'post',
            contentType: 'application/json;charset=UTF-8',
            url: table.api + "?rows=" + data.length + "&page=" + (data.start / data.length + 1),
            data: JSON.stringify(table.queryParams),
            success: function (ret) {
                if (ret.code === 200) {
                    var data = ret.obj;
                    var tableData = data.items || []; // 无记录返回为null，需要处理为[]
                    var callbackJson = {
                        'recordsTotal': data.totalNum, // 总记录数，分页显示用
                        'recordsFiltered': data.totalNum, // 过滤后的总记录数
                        'data': tableData // 表格填充用的数据
                    };
                    callback(callbackJson);
                } else {
                    layer.msg('获取记录失败！(' + ret['msg'] + ')', {icon: 2});
                }
            }
        };

        utils.ajax(ajaxOption);

    }

    // 重新加载表格数据
    function reload(params) {

        table.queryParams = params || table.queryParams; // 使用createAjaxFun时才需要外部传参
        table.instance.ajax.reload(); // 重新加载表格数据

    }

    // 刷新当前页数据
    function refresh() {
        table.instance.draw(false);
    }

    return {
        'init':init,
        'reload': reload,
        'refresh': refresh
    }

});