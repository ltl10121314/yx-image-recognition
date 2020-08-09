/**
 * @author M.C
 * @date 2018/6/1
 * @Description 设施列表
 */
define(['api','utils','template'], function (api,utils,template) {
    
    function init(){

        utils.ajaxReq({
            url: api.map.queryGistTypeByPid,
            data: {"level":"1,2"},
            success: function (res) {
                
                var html = template('temTypeList',res);  
     
                $("#selTypeList").html(html);
                $('#selTypeList').click(function () {
                    getNextGistType($("#selTypeList").val()[0]);
                })

                
            }
        });
        
    }

    // 获取具体设施列表
    function getNextGistType(pid) {
        var option = {
            url: api.map.queryGistTypeByPid,
            data: {"level":0, "pid": pid},
            success: function (res) {
                var html = template('temTypeList',res);  
     
                $("#selFacilityList").html(html);

                $('#selFacilityList').dblclick(function () {
                    console.log($("#selFacilityList").find("option:selected"));
                    $("#selFacilityListRes").append($("#selFacilityList").find("option:selected"));


                    $('#selFacilityListRes').dblclick(function () {
                        $("#selFacilityListRes").find("option:selected").remove();
                        //$('#selFacilityListRes')[].remove();
                    })
       
                })


            }
        };
        utils.ajaxReq(option);
    }

    return {
        'init': init
    }
});