define(['utils', 'api'], function (utils, api) {

    var modVars = {
      "chart": null
    };

    function init() {
        //setUserName();
        initCharts(); // 初始化图表实例
    }

    function setUserName() {
        var option = {
            type: 'post',
            url: api.user.getSystemUser,
            success: function (ret) {
                if (ret.code === 200) {
                    console.log(ret.obj);
                }else {
                    layer.msg('code：' + ret.code + ' 信息: ' + ret.msg, {icon: 2});
                }
            }
        };
        utils.ajax(option);
    }

    /**
     * 初始化图表实例
     * @author [刘耀填]
     * @date 2018/10/08
     */
    function initCharts() {
        modVars.chart = {
            "barChart": echarts.init($('#barChart')[0], 'shine'),
            "stackedLineChart": echarts.init($('#lineChart')[0], 'shine'),
            "pieChart": echarts.init($('#pieChart')[0], 'shine')
        };

        // ECharts图表在div尺寸变化时的自适应
        window.onresize = function(){
            modVars.chart.barChart.resize();
            modVars.chart.stackedLineChart.resize();
            modVars.chart.pieChart.resize();
        };
        getChartsData(); // 获取所有图表的数据
    }

    /**
     * 获取所有图表的数据
     * @author [刘耀填]
     * @date 2018/10/08
     */
    function getChartsData() {
        function successFun(ret) {
            if (ret.code === 200) {
                var obj = ret.obj;
                renderCharts(obj);
            } else {
                layer.msg("首页数据获取失败！");
            }
        }
        /*var option = {
            url: api.home.getHomePageStatistics,
            success: successFun
        };
        utils.ajax(option);*/

        $.getJSON("../../js/model/home/content.json", "", successFun);
    }

    /**
     * 渲染图表
     * @author [刘耀填]
     * @date 2018/10/08
     */
    function renderCharts(data) {

        $('#userCount').html(data.userCount); // 总用户数
        $('#monthloginCount').html(data.monthloginCount); // 本月登录用户数
        $('#dayloginCount').html(data.dayloginCount); // 今日登录用户数
        $('#loginTotalCount').html(data.loginTotalCount); // 总登录次数

        // 数字从低到高动态累加
        $('.counter').counterUp({
            delay: 100,
            time: 1200
        });

        $('#appTotalCount').html(data.appTotalCount); // 总应用数量


        renderBar(data.activiUserRanking); // 渲染柱状图(活跃用户排行榜)
        renderStackedLine(data.appRateRanking); // 渲染堆叠折线图(系统使用频率分析)
        renderPie(data.appRateStatistic); // 渲染圆饼图(每个应用登录次数)

    }

    /**
     * 渲染柱状图(活跃用户排行榜)
     * @author [刘耀填]
     * @date 2018/10/08
     */
    function renderBar(barData) {
        var one = barData[0].loginCount,
            two = barData[1].userCount,
            ranking = barData[2].ranking; // 排行榜数据

        var xAxisData = [], // x轴上的类别
            oneData = [],
            twoData = [];

        // 获取状元，榜眼，探花每个月的登录次数
        for (monthName in one) {
            if (one.hasOwnProperty(monthName)) {
                xAxisData.push(monthName);

                var oneItem = (one[monthName] === 0) ? 0 : one[monthName];
                oneData.push(oneItem);

                var twoItem = (two[monthName] === 0) ? 0 : two[monthName];
                twoData.push(twoItem);
            }
        }


        // 获取当前月的状元，榜眼，探花 (短路&&)
        ranking.one && $('#first').html(ranking.one.username);
        ranking.two && $('#second').html(ranking.two.username);
        ranking.three && $('#third').html(ranking.three.username);

        // 配置项：用于控制条形柱顶部显示数量，或文字等
        var itemStyle = {
            normal: {
                label: {
                    show: false, // 不显示（暂时）
                    position: 'top', // 在上方显示
                    textStyle: { // 数值样式
                        color: 'black',
                        fontSize: 14
                    },
                    formatter: function (param) {
                        if (param.value === 0) {
                            return '';
                        } else {
                            return param.data.username;
                        }
                    }

                }
            }
        };

        var option = {
            tooltip: {
                trigger: 'axis',
                axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                    type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                }
            },
            // 自定义tooltip显示
            // formatter: function (params) {
            //     var res = params[0].name;
            //     res += '<style>td{padding:3px;padding-top: 1px;}</style><table>';
            //     if (params[0].data) {
            //         res += '<tr><td>' + params[0].marker + '状元:</td><td>' + params[0].data.username + '</td></tr>';
            //     }
            //     if (params[1].data) {
            //         res += '<tr><td>' + params[1].marker + '榜眼:</td><td>' + params[1].data.username + '</td></tr>';
            //     }
            //     if (params[2].data) {
            //         res += '<tr><td>' + params[2].marker + '探花:</td><td>' + params[2].data.username + '</td></tr>';
            //     }
            //     res += '</table>';
            //     return res;
            // },
            xAxis: {
                type: 'category',
                data: xAxisData
            },
            yAxis: {
                type: 'value',
                min: 0,
                minInterval: 1,
                name: '',
                axisLabel: {
                    formatter: ' {value}'
                },
                "axisLine": {       //y轴
                    "show": false
                },
                "axisTick": {       //y轴刻度线
                    "show": false
                },
                "splitLine": {     //网格线
                    "show": true
                },
            },
            series: [{
                name: '使用次数',
                type: 'bar',
                data: oneData,
                // 暂时不用，需要时，将对象里的show变为true
                itemStyle: itemStyle
            }, {
                name: '用户数',
                type: 'bar',
                data: twoData,
                // 暂时不用，需要时，将对象里的show变为true
                itemStyle: itemStyle
            }]
        };

        // 使用刚指定的配置项和数据显示图表。
        modVars.chart.barChart.setOption(option);

    }

    /**
     * 渲染堆叠折线图(系统使用频率分析)
     * @author [刘耀填]
     * @date 2018/10/08
     */
    function renderStackedLine(lineData) {

        var xAxisData = [], // x轴上的类别
            legendArr = [], // 图例
            seriesData = [];

        for (var i = 0; i <= lineData.length; i++) {
            var appObj = {}; // 保存每个应用的数据
            var appItem = lineData[i]; // 每个应用对象，只有一个属性
            for (appName in appItem) {
                if (appItem.hasOwnProperty(appName)) {

                    var appItemMonthData = appItem[appName]; // 每个应用对应的月份数据
                    appObj = {
                        "name": appName, // 应用名
                        "type": "line",
                        "stack": appName,
                        "data": [] // 该应用每个月的使用频率
                    };
                    for (monthName in appItemMonthData) {
                        if (appItemMonthData.hasOwnProperty(monthName)) {
                            if (i === 0) {
                                xAxisData.push(monthName);  // 获取x轴上的类别
                            }
                            appObj.data.push(appItemMonthData[monthName]);
                        }

                    }
                    legendArr.push(appName);
                    seriesData.push(appObj);
                }
            }
        }

        var option = {
            title: {
                // text: '折线图堆叠'
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                    type: 'line'        // 默认为直线，可选为：'line' | 'shadow'
                }
            },
            legend: {
                data: legendArr
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: xAxisData
            },
            yAxis: {
                // minInterval: 10,
                min: 0,
                minInterval: 1,
                type: 'value',
                axisLabel: {
                    formatter: ' {value}'
                },
                "axisLine": {       //y轴
                    "show": false

                },
                "axisTick": {       //y轴刻度线
                    "show": false
                },
                "splitLine": {     //网格线
                    "show": true
                }
            },
            series: seriesData
        };

        // 使用刚指定的配置项和数据显示图表。
        modVars.chart.stackedLineChart.setOption(option);
    }

    /**
     * 渲染圆饼图(每个应用登录次数)
     * @author [刘耀填]
     * @date 2018/10/08
     */
    function renderPie(pieData) {
        var option = {
            tooltip: {
                trigger: 'item',
                formatter: "{a} <br/>{b} : {c} ({d}%)"
            },
            color: ['rgb(59,192,195)', 'rgb(241,60,110)', 'rgb(249,205,173)', 'rgb(26,41,66)', 'rgb(131,175,155)'],
            series: [
                {
                    name: '使用次数',
                    type: 'pie',
                    radius: '55%',
                    center: ['50%', '60%'],
                    data: pieData,
                    itemStyle: {
                        emphasis: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        },
                        normal: {
                            label: {
                                show: false   //隐藏标示文字
                            },
                            labelLine: {
                                show: false   //隐藏标示线
                            }
                        }
                    }
                }
            ]
        };
        // 使用刚指定的配置项和数据显示图表。
        modVars.chart.pieChart.setOption(option);
    }

    return {
        'init': init
    }
});
