app.service("loginService",function ($http) {
    //发起请求，获取登录名
    this.loginName=function () {
        return $http.get("../login/name.do");
    }
})