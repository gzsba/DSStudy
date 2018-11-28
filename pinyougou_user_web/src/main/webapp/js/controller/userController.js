 //控制层 
app.controller('userController' ,function($scope,userService){

    $scope.entity={'phone':''};
    $scope.code='';

	//用户注册
	$scope.reg=function () {
		//注册前端逻辑
		if($scope.entity.password != $scope.password){
            alert("您输入的两次密码不一致，请重新输入！");
            return;
		}
		if($scope.code == ''){
            alert("请输入验证码！");
            return;
        }

		userService.add($scope.entity,$scope.code).success(function (response) {
            alert(response.message);
            if(response.success){

			}
        })
    }

    //发送验证码
    $scope.sendCode=function(){
        if($scope.entity.phone==null){
            alert("请输入手机号！");
            return ;
        }
        userService.sendCode($scope.entity.phone).success(
            function(response){
                alert(response.message);
            }
        );
    };

});	
