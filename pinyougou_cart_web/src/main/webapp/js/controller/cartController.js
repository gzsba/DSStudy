app.controller("cartController",function ($scope,cartService,addressService) {
    $scope.findCartList=function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;
            //计算总金额与数量
            sum(response);
        })
    }

    $scope.addGoodsToCartList=function (itemId,num) {
        cartService.addGoodsToCartList(itemId,num).success(function (response) {
            if(response.success){
                $scope.findCartList();
            }else{
                alert(response.message);
            }
        })
    }

    /**
     * 计算总金额与数量
     * @param cartList
     */
    sum=function (cartList) {
        $scope.totalValue={totalNum:0, totalMoney:0.00 };//合计实体

        for(var i = 0; i < cartList.length; i++){
            var itemList = cartList[i].orderItemList;
            for(var j = 0; j < itemList.length; j++){
                $scope.totalValue.totalNum += itemList[j].num;
                $scope.totalValue.totalMoney += itemList[j].totalFee;
            }
        }
    }

    //查询用户的收货人列表
    $scope.findAddressList=function () {
        addressService.findAddressList().success(function (response) {
            $scope.addressList = response;
            //检测默认地址
            for(var i = 0; i < response.length; i++){
                if(response[i].isDefault == "1"){
                    $scope.address = response[i];
                    return;
                }
            }
        })
    }

    //记录用户勾选的地址
    $scope.address={};

    //修改收货地址
    $scope.selectAddress=function (address) {
        $scope.address = address;
    }
    //检查收件人是否要勾选
    $scope.isSelectedAddress=function (address) {
        if(address == $scope.address){
            return true;
        }else{
            return false;
        }
    }
    //订单默认支付方式
    $scope.order={paymentType:"1"};
    //修改支付方式
    $scope.selectPayType=function (type) {
        $scope.entity.paymentType = type;
    }

    //保存订单
    $scope.submitOrder=function () {
        //设置收货人信息
        $scope.order.receiverAreaName = $scope.address.address;//地址
        $scope.order.receiverMobile = $scope.address.mobile;//手机
        $scope.order.receiver = $scope.address.contact;//联系人
        cartService.submitOrder($scope.order).success(function (response) {
            alert(response.message);
            if(response.success){
                if($scope.order.paymentType == "1"){
                    window.location.href = "pay.html";
                }else{
                    window.location.href = "paysuccess.html";
                }
            }
        })
    }

})