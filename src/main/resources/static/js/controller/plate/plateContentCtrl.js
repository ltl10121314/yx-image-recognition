require(['../../js/common/common.config.js'], function() {
    require(["plateContentMod"], function(plateContentMod) {
        function setDiv() {
            var height = $(window).height() - 135;
            $("#bodyDiv").height(height);
            $("#bodyDiv").css('max-height', height + "px");

            $("#custom-toc-container").height(height);
            $("#custom-toc-container").css('max-height', height + "px");

            $("#treeDiv").height(height);
            $("#treeDiv").css('max-height', height + "px");

        }
        plateContentMod.init();
        setDiv();

        $(window).resize(function () {
            setDiv();
        });
    });
});