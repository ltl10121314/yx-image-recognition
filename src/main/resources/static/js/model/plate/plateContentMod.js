define(['api', 'utils'], function(api, utils){

    function init(){
        // 初始化树结构
        initTree();

        // 加载其他html文件
        loadHtml();

        // 按钮绑定事件 -- 加载完成后可见按钮
        bindBtnEvent();

        // 监听按钮事件
        /*$('body').keydown(function (e) {
            if (event.keyCode==116){ //回车键 //F5按键
                e.preventDefault();
                setTimeout(function () {
                    $("#refreshPlate").trigger('click');
                }, 200);
            }
        });*/

    }

    var treeId = "#treeId";
    var plateTreeNode = null;
    var dirTreeNode = null;

    function loadHtml(){
        // $("#choseDirDiv").load('./choseDir.html', function () {
        //     $("#newRootDirBtn").on("click", function () {
        //         addRootDir();
        //     });
        // });

        getProcessStep();
    }

    function getProcessStep() {
        function successFun(ret) {
            if (ret.code === 200) {
                $("#processStepDiv").html("").html('<br/>');
                $.each(ret.obj, function (index, item){
                    var $div = $('<div class="process" align="left" style="padding-top:10px;width: 100%;"></div>');
                    $div.attr("id", index);
                    $span = $('<span style="float: left;font-weight: bolder;font-size: 20px;"></span>');
                    $span.text(index + "：");

                    $div.append($span);
                    $div.append($('<br/><br/>'));

                    $("#processStepDiv").append($div);
                });
            } else {
                layer.msg('失败 ！', {icon: 2});
            }
        }
        var option = {
            type: 'get',
            url: api.plate.getProcessStep,
            data: {},
            success: successFun
        };
        utils.ajax(option);
    }

    function bindBtnEvent(){
        $("#recognise").on("click", function () {
            recognise(plateTreeNode.filePath, true);
        });
    }

    function recognise(filePath, reRecognise){
        function successFun(ret) {
            if (ret.code === 200) {
                $("#span_plate").text(ret.obj.plate);
                $("#span_color").text(ret.obj.plateColor);

                $("#processStepDiv").find(".process-div").remove();
                $("#processStepDiv").find(".process-img").remove();

                $.each(ret.obj.debug, function (index, item){
                    var $div = $('<div class="process-div"></div>');
                    var $img = $('<img class="process-img">');
                    $img.attr("src", encodeURI(api.file.readFile + "?filePath="+ item.filePath));
                    $div.append($img);

                    if(item.debugType.indexOf("result") == 0){
                        $div.append($('<br/><span>' + item.recoPlate  + '</span>'));
                        $div.append($('<span>&nbsp;' + item.plateColor  + '</span>'));
                    }

                    $("#"+ item.debugType).append($div);
                });
            } else {
                layer.msg('失败 ！', {icon: 2});
            }
        }
        var option = {
            type: 'get',
            url: api.plate.recognise,
            data: {"filePath": filePath, "reRecognise": reRecognise},
            success: successFun
        };
        utils.ajax(option);
    }

    function initTree() {
        isFirst = false; //加载树的时候默认咱开第一层级
        $.fn.zTree.destroy(treeId);
        $.fn.zTree.init($(treeId), setting);
    }

    // 树结构配置
    var setting = {
        edit: {
            enable: true,
            editNameSelectAll: true,
            showRemoveBtn: true,
            showRenameBtn: true
        },
        view: {
            addHoverDom: addHoverDom,
            removeHoverDom: removeHoverDom
        },
        check: {
            enable: false
        },
        callback: {
            onClick: treeClick,
            onAsyncSuccess:onAsyncSuccess,
            beforeRemove: beforeRemove,
            beforeRename: beforeRename,
        },
        async: {
            enable: true,
            url: api.file.getFileTreeByDir,
            type: 'get',
            dataType: "json",
            autoParam: ["filePath=dir"],
            otherParam: {"typeFilter":"png,jpg,jpeg"},
            dataFilter: ajaxDataFilter
        },
        data: {
            simpleData: {
                enable: true
            }
        }
    };

    // 添加刷新按钮
    function addHoverDom(treeId, treeNode) {
        var aObj = $("#" + treeNode.tId + "_a");
        if(!treeNode.isParent){
            return;
        }
        if ($("#" + treeNode.tId + "_refresh").length > 0){
            return;
        }
        var refreshStr = $('<button type="button" class="icon-refresh" id="'+treeNode.tId+'_refresh" title="refresh" treenode_refresh=""></button >');
        aObj.append(refreshStr);
        refreshStr.bind("click", function(){
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            treeObj.reAsyncChildNodes(treeNode, "refresh");
        });
    };
    // 移除刷新按钮
    function removeHoverDom(treeId, treeNode) {
        $("#" + treeNode.tId + "_refresh").unbind().remove();
    };

    function beforeRemove(treeId, treeNode) {
        layer.confirm("是否删除？", function(index){
            function successFun(ret) {
                if (ret.code === 200) {
                    layer.msg("删除成功", {icon: 1});
                    var treeObj = $.fn.zTree.getZTreeObj(treeId);
                    treeObj.reAsyncChildNodes(treeNode.getParentNode(), "refresh");
                } else {
                    layer.msg(ret.msg, {icon: 2});
                }
            }
            var option = {
                type: 'get',
                url: api.plate.removeDirOrFile,
                success: successFun,
                data: {"fileName": treeNode.filePath}
            };
            utils.ajax(option);
            layer.close(index);
        });
        return false;
    }


    function beforeRename(treeId, treeNode, newName, isCancel) {
        function successFun(ret) {
            if (ret.code === 200) {
                var treeObj = $.fn.zTree.getZTreeObj(treeId);
                treeObj.reAsyncChildNodes(treeNode.getParentNode(), "refresh");
            } else {
                layer.msg(ret.msg, {icon: 2});
            }
        }
        var option = {
            type: 'get',
            url: api.plate.renameDirOrFile,
            success: successFun,
            data: {"fileName": treeNode.filePath, "newName": newName}
        };
        utils.ajax(option);

        var treeObj = $.fn.zTree.getZTreeObj(treeId);
        treeObj.refresh(treeNode);
        return false;
    }


    var isFirst = false;
    function onAsyncSuccess(event, treeId) {
        if (isFirst) {
            //获得树形图对象
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            var nodes = treeObj.getNodes();
            if (nodes.length>0) {
                for(var i=0;i<nodes.length;i++){
                    if(nodes[i].isParent){
                        treeObj.expandNode(nodes[i], true, false, false); // 展开第一层级
                    }
                }
            }
            isFirst= false;
        }
    }

    // 异步加载树结构数据
    function ajaxDataFilter(treeId, parentNode, ret) {
        var treeNode = [];
        if (ret.code === 200) {
            $.each(ret.obj, function (index, item){
                var node = {};
                node.id = item.id;
                node.name = item.fileName;
                node.isParent = item.isDir;
                node.filePath = encodeURI(item.filePath);   // 路径编码，防止出现特殊字符影响
                treeNode.push(node);
            });
        }
        return treeNode;
    };

    // 树节点点击事件
    function treeClick(event, treeId, node) {
        var treeObj = $.fn.zTree.getZTreeObj(treeId);

        if(node.name.indexOf(".png") > 1 || node.name.indexOf(".jpg") > 1){
            $('#baseImage').attr("src", encodeURI(api.file.readFile + "?filePath=" + node.filePath));
            recognise(node.filePath, false);
        }

        if(node.isParent){
            $("#parentDir").val(node.filePath);
            dirTreeNode = node;
        } else {
            plateTreeNode = node;
        }
    }




    return {
        "init": init
    }
});