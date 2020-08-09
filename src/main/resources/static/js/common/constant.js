
define({
    'URL':{
        'MAIN_URL': 'http://localhost:16666/', // 接口地址
        'WEB_URL': 'http://localhost:16666/', // 前端地址
        'PATH_LOGIN': '/login'
    },
    'SYSTEM': {
        // key代表城市 value代表该城市公众号对应的appId
        "china": 'wx95112c5c2ed8bca0', // 默认城市交通大脑
        "xuancheng": 'wxd599e99b0ac87622', // 宣城
        "test": 'wx4062a9ae141c5268' // 演示环境公众号
    },
    'PLACE':{
        'CITY':'XUANCHENG'
    },
    "ONLY":{
        "STYSTEM":"SHESHI", // 只用于设施系统
    },
    'DATATABLE_LANGUAGE':{
        'sProcessing': '处理中...',
        'sLengthMenu': '显示 _MENU_ 条',
        'sZeroRecords': '没有匹配结果',
        'sInfo': '共 _TOTAL_ 项',
        'sInfoEmpty': '共 0 项',
        'sInfoFiltered': '',
        'sInfoPostFix': '',
        'sSearch': '',
        'sUrl': '',
        'sEmptyTable': '表中数据为空',
        'sLoadingRecords': '载入中...',
        'sInfoThousands': ',',
        'oPaginate': {
            'sFirst': '首页',
            'sPrevious': '上页',
            'sNext': '下页',
            'sLast': '末页'
        },
        'oAria': {
            'sSortAscending': ': 以升序排列此列',
            'sSortDescending': ': 以降序排列此列'
        }
    }

});