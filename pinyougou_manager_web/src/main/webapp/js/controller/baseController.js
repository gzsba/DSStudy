//抽取共用的controller
app.controller("baseController", function ($scope) {
    $scope.paginationConf = {
        //当前页
        currentPage: 1,
        //总记录数
        totalItems: 10,
        //每页查询的记录数
        itemsPerPage: 10,
        //分页选项，用于选择每页显示多少条记录
        perPageOptions: [10, 20, 30, 40, 50],
        //当页码变更后触发的函数
        onChange: function () {
            $scope.reloadList();//重新加载
        }
    };

    //刷新页面
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    //记录要删除的id列表
    $scope.selectIds=[];
    /**
     * 跟据id批量删除数据
     * @param event
     * @param id
     */
    $scope.updateSelection=function ($event,id) {
        //识别有没有勾选中checkbox
        if($event.target.checked){
            $scope.selectIds.push(id);
        }else{
            //获取传入id的下标
            var index = $scope.selectIds.indexOf(id);
            //删除数据数据，参数一：删除的下标，参数二：删除的个数
            $scope.selectIds.splice(index,1);
        }
    }

    //跟据需求输出json串
    //jsonString要转换的json串,key要读取的值
    $scope.jsonToString=function (jsonString,key) {
        var json = JSON.parse(jsonString);
        var result = "";
        for(var i = 0;i < json.length; i++){
            if(i > 0){
                result += ",";
            }
            result += json[i][key];
        }
        return result;
    }

});