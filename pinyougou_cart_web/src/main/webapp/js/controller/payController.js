app.controller("payController",function ($scope,$location, payService) {
    //生成二维码
    $scope.createNative=function () {
        payService.createNative().success(function (response) {
            //订单号
            $scope.out_trade_no = response.out_trade_no;
            //支付金额，1.0987898.toFixed(2)=1.09
            $scope.total_fee = (response.total_fee / 100).toFixed(2);

            //二维码
            var qr = new QRious({
                element:document.getElementById('qrious'),
                size:260,
                level:'Q',
                value:response.code_url
            });

            //查询订单状态
            payService.queryPayStatus(response.out_trade_no).success(function (response) {
                alert(response.message);
                if(response.success){
                    window.location.href = "paysuccess.html#?money="+$scope.total_fee;
                }else{
                    if(response.message == "支付已超时"){
                        window.location.href = "paytimeout.html";
                    }else{
                        window.location.href = "payfail.html";
                    }
                }
            });

        })
    };

    $scope.getMoney=function () {
        return $location.search()["money"];
    }
});