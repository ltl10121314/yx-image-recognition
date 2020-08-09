define(['constant'], function (constant) {
    return {
        // 代码生成
        "generator": {
            // 数据库配置模块
            'dbConfig': constant.URL.MAIN_URL + '/generatorDb/',
            'queryDbList': constant.URL.MAIN_URL + '/generatorDb/queryByCondition',
            'testDbConnect': constant.URL.MAIN_URL + '/generatorDb/testDbConnect',
            'queryTableList': constant.URL.MAIN_URL + '/generatorDb/queryTableList',
            'queryColumnList': constant.URL.MAIN_URL + '/generatorDb/queryColumnList',

            // 代码生成配置模块
            "generCongfig": constant.URL.MAIN_URL + '/generatorConfig/',
            'queryGenerConfigList': constant.URL.MAIN_URL + '/generatorConfig/queryByCondition',
            'openDir': constant.URL.MAIN_URL + '/generatorConfig/openDir',

            // 代码生成模块
            'doGenerator': constant.URL.MAIN_URL + '/codeGenerator/doGeneratior',
        },

        // 口令管理
        "pass": {
            'passMain':constant.URL.MAIN_URL + '/passMain/',
            'queryByPage':constant.URL.MAIN_URL + '/passMain/queryByPage',
        },

        // 业主管理
        "owner": {
            'systemOwner':constant.URL.MAIN_URL + '/systemOwner/',
            'queryByPage':constant.URL.MAIN_URL + '/systemOwner/queryByPage',
        },

        // 环境管理
        "evn": {
            'evnMain':constant.URL.MAIN_URL + '/evnMain/',
            'queryByPage':constant.URL.MAIN_URL + '/evnMain/queryByPage',
            'doExport':constant.URL.MAIN_URL + '/evnMain/doExport',
        },

        // 环境管理
        "md": {
            'getFileTreeByDir':constant.URL.MAIN_URL + '/mdMain/getFileTreeByDir',
            'readMd':constant.URL.MAIN_URL + '/mdMain/readMd',
            'getRootDir':constant.URL.MAIN_URL + '/mdMain/getRootDir',
            'setDefaultRootDir':constant.URL.MAIN_URL + '/mdMain/setDefaultRootDir',
            'addRootDir':constant.URL.MAIN_URL + '/mdMain/addRootDir',
            'delRootDir':constant.URL.MAIN_URL + '/mdMain/delRootDir',
            'createDirOrFile':constant.URL.MAIN_URL + '/mdMain/createDirOrFile',
            'removeDirOrFile':constant.URL.MAIN_URL + '/mdMain/removeDirOrFile',
            'renameDirOrFile':constant.URL.MAIN_URL + '/mdMain/renameDirOrFile',
            'saveMd':constant.URL.MAIN_URL + '/mdMain/saveMd',
            'mdImgUpload':constant.URL.MAIN_URL + '/mdMain/mdImgUpload',
            'readFile':constant.URL.MAIN_URL + '/mdMain/readFile',
            'searchMd':constant.URL.MAIN_URL + '/mdMain/searchMd',
        },


        // 用户模块
        "user": {
            'login': constant.URL.MAIN_URL + '/doLogin ',
            'logout': constant.URL.MAIN_URL + '/logout',
            'getSystemUser': constant.URL.MAIN_URL + '/getSystemUser',
            'systemUser':constant.URL.MAIN_URL + '/systemUser/',
            'queryByPage':constant.URL.MAIN_URL + '/systemUser/queryByPage',
            'updatePasswd': constant.URL.MAIN_URL + 'systemUser/updatePasswd',  // 修改用户密码
            'resetPasswd': constant.URL.MAIN_URL + 'systemUser/resetPasswd',  // 重置用户密码
            'queryByCondition': constant.URL.MAIN_URL + 'systemUser/queryByCondition'   // 查询用户列表信息
        },

        "dept": {
            'systemDept':constant.URL.MAIN_URL + '/systemDept/',
            'queryByPage':constant.URL.MAIN_URL + 'systemDept/queryByPage',
            'queryByCondition': constant.URL.MAIN_URL + 'systemDept/queryByCondition'   // 查询用户列表信息
        },

        "menu": {
            'systemMenu':constant.URL.MAIN_URL + '/systemMenu/',
            'queryByPage':constant.URL.MAIN_URL + 'systemMenu/queryByPage',
            'queryByCondition': constant.URL.MAIN_URL + 'systemMenu/queryByCondition'   // 查询用户列表信息
        },

        "res": {
            'systemRes': constant.URL.MAIN_URL + 'systemRes/',
            'queryByPage': constant.URL.MAIN_URL + 'systemRes/queryByPage'
        },

        // 部门组织模块
        'organize': {
            'org': constant.URL.MAIN_URL + 'org',  //  组织增，删，改
            'queryOrgForTree': constant.URL.MAIN_URL + 'org/queryOrgForTree', // 查询组织架构树
            'queryUserList': constant.URL.MAIN_URL + 'org/queryUserList',  // 查询部门已选、待选用户,
            'associatedUser': constant.URL.MAIN_URL + 'org/associatedUser',  // 关联用户
        },

        // 权限管理
        'authority': {
            'roleQueryByCondition': constant.URL.MAIN_URL + 'role/queryByCondition',  //  角色管理分页信息
            'role': constant.URL.MAIN_URL + 'role',  //  API增，删，改
            'dataGroupQueryByCondition': constant.URL.MAIN_URL + 'dataGroup/queryByCondition',  //  数据权限分组分页信息
            'queryDataAuthByRole': constant.URL.MAIN_URL + 'dataGroup/queryDataAuthByRole', // 查询数据权限
            'savaDataAuthByRole': constant.URL.MAIN_URL + 'dataGroup/savaDataAuthByRole', // 保存数据权限
            'dataGroup': constant.URL.MAIN_URL + 'dataGroup',  //  API增，删，改
            'queryDataGroupforTree': constant.URL.MAIN_URL + 'dataGroup/queryDataGroupforTree',  //  数据分组树形结构
            'queryRoleUserByOrg': constant.URL.MAIN_URL + 'role/queryRoleUserByOrg', // 查询角色用户列表
            'addRoleUser': constant.URL.MAIN_URL + 'role/addRoleUser', // 角色分配用户
            'queryMenuByRole': constant.URL.MAIN_URL + 'role/queryMenuByRole', // 查询角色对应菜单
            'addRoleMenu': constant.URL.MAIN_URL + 'role/addRoleMenu', // 角色分配菜单
            'queryMenuByUser': constant.URL.MAIN_URL + 'systemMenu/getUserMenu', // 查询用户权限下的菜单
        },
        
        // 车牌识别
        "plate": {
            'getProcessStep':constant.URL.MAIN_URL + '/plate/getProcessStep',
            'recognise':constant.URL.MAIN_URL + '/plate/recognise',
        },

        "file": {
            'getFileTreeByDir':constant.URL.MAIN_URL + '/file/getFileTreeByDir',
            'readFile':constant.URL.MAIN_URL + '/file/readFile',
        }
        

        
        
    };
});