/*
  ele: id
  size: 16、22 默认22
  slabel: checkbox前描述信息
  elabel: checkbox后描述信息
  labelWidth: 文字区域宽度
  onChange: '触发checkbox回调函数'
*/
define(function () {
    function checkBox(config) {
        this.ele = config.ele;
        this.size = config.size || 16;
        this.slabel = config.slabel;
        this.elabel = config.elabel;
        this.labelWidth = config.labelWidth || 'auto';
        this.ele_cb = config.ele + '-cb';
        this.onChange = config.onChange;
        this.onInit();
    }
    checkBox.prototype.onInit = function () {
        var html = '<div class="fa-cb-box">';
        if (this.slabel) {
            html += '<div class="fa-cb-label rj-fl" style="width:' + this.labelWidth + '"><label for="' + this.ele_cb + '">' + this.slabel + '</label></div>'
        }
        html += '<label class="ck-box rj-fl ck-' + this.size + '">' +
            '<input id="' + this.ele_cb + '" type="checkbox">' +
            '<span class="ck-bg"></span>' +
            '</label>'
        if (this.elabel) {
            html += '<div class="fa-cb-label rj-fl" style="margin-left:5px;"><label for="' + this.ele_cb + '">' + this.elabel + '</label></div>'
        }
        html += '</div>'
        $('#' + this.ele).addClass('fa-cb-container').html(html)
        if (this.onChange && typeof this.onChange == 'function') {
            var _self = this;
            $('#' + this.ele_cb).change(function () {
                _self.onChange()
            })
        }
    };
    checkBox.prototype.getValue = function () {
        return $('#' + this.ele_cb)[0].checked
    };
    checkBox.prototype.setValue = function (val) {
        $('#' + this.ele_cb).prop('checked', val);
    };

    return checkBox
})