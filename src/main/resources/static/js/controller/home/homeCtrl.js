/**
 * 
 * @returns
 */
require(['../../js/common/common.config.js'], function () {

    require(['baseMod', 'homeMod', 'common'], function (baseMod, homeMod, common) {

        baseMod.init();
        homeMod.init();
    });

});