app.controller("contentController",function ($scope,contentService) {
    //所有广告的数组
    $scope.contentList=[];
    //跟据类型查询广告列表
    $scope.findByCategoryId=function (categoryId) {
        contentService.findByCategoryId(categoryId).success(function (response) {
            $scope.contentList[categoryId] = response;
        })
    }
    $scope.keywords = '';

    $scope.search=function () {
        if($scope.keywords != ''){
            window.location.href = "http://localhost:8084/search.html#?keywords="+$scope.keywords;
        }else {
            alert("请输入查询条件!");
        }
    }
});