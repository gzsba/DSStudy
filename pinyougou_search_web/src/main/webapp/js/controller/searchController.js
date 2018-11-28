app.controller("searchController",function ($scope,$location,searchService) {

    /**
     * 搜索对象
     * @type {{keywords: 关键字, category: 商品分类, brand: 品牌, spec: {'网络'：'移动4G','机身内存':'64G'},
     *          price:价格区间(0-500,500-1000,3000-*),'pageNo':当前页,'pageSize':查询的记录数,
     *          'sortField':排序的业务域,'sort':排序的方式ASC|DESC}}
     */
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'',
                        'pageNo':1,'pageSize':40,'sortField':'','sort':''};

    $scope.loadkeywords=function () {
        var keywords = $location.search()["keywords"];
        if(keywords != null && keywords != ''){
            $scope.searchMap.keywords = keywords;

            $scope.search();
        }
    }

    /**
     * 搜索方法
     */
    $scope.search=function () {
        searchService.search($scope.searchMap).success(function (response) {
            //alert(JSON.stringify(response));
            $scope.resultMap = response;
            //计算分页的数据
            buildPageLabel();
        })
    }

    /**
     * 识别关键字是否包含品牌
     */
    $scope.keywordsIsBrand=function () {
        var brandIds = $scope.resultMap.brandIds;
        for(var i = 0; i < brandIds.length; i++){
            if($scope.searchMap.keywords.indexOf(brandIds[i].text) > -1){
                return true;
            }
        }
        return false;
    }

    /**
     * 排序查询
     * @param sortField 排序的域
     * @param sort 排序的方式AEC|DESC
     */
    $scope.sortSearch=function (sortField,sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        //刷新查询数据
        $scope.search();
    }

    /**
     * 分页页码点击事件
     * @param pageNo 当前页
     */
    $scope.queryByPage=function (pageNo) {
        pageNo = parseInt(pageNo);
        if(pageNo < 1 || pageNo > $scope.resultMap.totalPages){
            alert("请输入正确的页码！");
            return;
        }
        //修改当前页
        $scope.searchMap.pageNo = pageNo;
        //刷新数据
        $scope.search();
    }

    /**
     * 计算分页逻辑
     */
    buildPageLabel=function () {
        $scope.pageLabel=[];  //记录总共的页数

        var firstPage = 1; //开始页码
        var lastPage = $scope.resultMap.totalPages;  //截止页码

        $scope.firstDot=true;//前面有点
        $scope.lastDot=true;//后边有点

        //如果总页数 > 5
        if($scope.resultMap.totalPages > 5){
            //处理当前页为前3页特殊场景
            if($scope.searchMap.pageNo <= 3){
                lastPage = 5;
                $scope.firstDot = false;
            //处理当前页最后3页的特殊场景
            }else if($scope.searchMap.pageNo >= ($scope.resultMap.totalPages -2)){
                firstPage = $scope.resultMap.totalPages - 4;

                $scope.lastDot=false;
            }else{
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
        }else{
            $scope.firstDot=false;//前面没点
            $scope.lastDot=false;//后边没点
        }
        //组装页码
        for(var i = firstPage; i <= lastPage; i ++){
            $scope.pageLabel.push(i);
        }
    }

    /**
     * 添加搜索项-分类、品牌、规格项的点击事件
     * @param key 搜索参数key名称
     * @param value 搜索参数value的值
     */
    $scope.addSearchItem=function (key,value) {
        if(key == 'category' || key == 'brand' || key == 'price'){
            $scope.searchMap[key] = value;
        }else{
            $scope.searchMap.spec[key] = value;
        }
        //刷新页面
        $scope.search();
    }

    /**
     * 删除搜索项-分类、品牌、规格项取消的点击事件
     * @param key 搜索参数key名称
     */
    $scope.removeSearchItem=function (key) {
        if(key == 'category' || key == 'brand' || key == 'price'){
            $scope.searchMap[key] = '';
        }else{
            delete $scope.searchMap.spec[key];
        }
        //刷新页面
        $scope.search();
    }
})