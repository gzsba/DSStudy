app.controller("seckillGoodsController", function ($scope,$location,$interval,seckillGoodsService) {
    $scope.findList=function () {
        seckillGoodsService.findList().success(function (response) {
            $scope.list = response;
        })
    }

    //查询秒杀商品详情
    $scope.findOneFromRedis=function () {
        var id = $location.search()["id"];
        if (id != null) {
            seckillGoodsService.findOneFromRedis(id).success(function (response) {
                $scope.goods = response;

                var second = Math.floor((new Date(response.endTime).getTime() - new Date().getTime()) / 1000);
                //$interval(执行的函数,间隔的毫秒数,运行次数);
                timer = $interval(function () {
                    second--;
                    if(second < 1){
                        $interval.cancel(timer);
                    }
                    $scope.secondStr = convertTimeString(second);
                },1000);
            })
        }
    }

    //把秒转换为 天小时分钟秒格式  XXX天 10:22:33
    convertTimeString=function(allsecond){
        var days= Math.floor( allsecond/(60*60*24));//天数
        var hours= Math.floor( (allsecond-days*60*60*24)/(60*60) );//小时数
        var minutes= Math.floor(  (allsecond -days*60*60*24 - hours*60*60)/60    );//分钟数
        var seconds= allsecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
        var timeString="";
        if(days>0){
            timeString=days+"天 ";
        }
        return timeString+hours+":"+minutes+":"+seconds;
    }

    //提交订单
    $scope.submitOrder=function () {
        seckillGoodsService.submitOrder($scope.goods.id).success(function (response) {
            alert(response.message);
            if(response.success){
                window.location.href = "pay.html";
            }

        })
    }

});