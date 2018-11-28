 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,
										   itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
        var id = $location.search()["id"];
        if(id != null) {
            goodsService.findOne(id).success(
                function (response) {
                    $scope.entity = response;
                    //读取商品描述
                    editor.html(response.goodsDesc.introduction);
                    //读取商品图片列表
                    $scope.entity.goodsDesc.itemImages = JSON.parse(response.goodsDesc.itemImages);

                    //读取扩展属性列表
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.goodsDesc.customAttributeItems);

                    //读取已勾选的规格列表
                    $scope.entity.goodsDesc.specificationItems = JSON.parse(response.goodsDesc.specificationItems);

                    //读取sku列表的规格信息
					for(var i = 0; i < $scope.entity.itemList.length; i++){
                        $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
					}
                }
            );
        }
	}
	
	//保存 
	$scope.save=function(){
		//先获取富文本的内容
        $scope.entity.goodsDesc.introduction=editor.html();

		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
                alert(response.message);
				if(response.success){
					//清空商品信息
					$scope.entity={};
					//清空富文本
                    editor.html("");
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

    $scope.image_entity = {url: ""};
    /**
	 * 图片上传逻辑实现
     */
	$scope.uploadFile=function () {
		uploadService.uploadFile().success(function (response) {
			//上传成功
			if(response.success){
				$scope.image_entity.url=response.message;
			}else{
				alert(response.message);
			}
        })
    }

    //定义页面实体结构{goods:{商品基本信息},
	// goodsDesc:{itemImages:图片列表,specificationItems：规格列表}}
    $scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};

	//商品图片添加
	$scope.add_image_entity=function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }
	//商品图片删除
    $scope.remove_image_entity=function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }

    //查询商品一级分类
	$scope.selectItemCat1List=function () {
		itemCatService.findByParentId(0).success(function (response) {
            $scope.itemCat1List = response;
        })
    }
    
    //加载二级商品分类，监听一级商品分类的值，如果值变化执行以下逻辑
	//参数一：监听的变量名
	//参数二：fun(新的值，原来的值)
	$scope.$watch("entity.goods.category1Id",function (newValue,oldValue) {
        //alert(oldValue + "-->" + newValue);
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCat2List = response;
        })
    });

    //加载三级商品分类，监听一级商品分类的值，如果值变化执行以下逻辑
    //参数一：监听的变量名
    //参数二：fun(新的值，原来的值)
    $scope.$watch("entity.goods.category2Id",function (newValue,oldValue) {
        //alert(oldValue + "-->" + newValue);
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCat3List = response;
        })
    });

    //加载模板id，监听一级商品分类的值，如果值变化执行以下逻辑
    //参数一：监听的变量名
    //参数二：fun(新的值，原来的值)
    $scope.$watch("entity.goods.category3Id",function (newValue,oldValue) {
        itemCatService.findOne(newValue).success(function (response) {
			$scope.entity.goods.typeTemplateId=response.typeId;
        })
    });

    //加载品牌列表、扩展属性列表，监听一级商品分类的值，如果值变化执行以下逻辑
    $scope.$watch("entity.goods.typeTemplateId",function (newValue,oldValue) {
        typeTemplateService.findOne(newValue).success(function (response) {
			$scope.typeTemplate=response;
			//把数据库查询到的品牌字符串转换成品牌数组
            $scope.typeTemplate.brandIds = JSON.parse(response.brandIds);
            //把数据库查询到的扩展属性字符串转换成扩展属性数组
			var id = $location.search()["id"];
			if(id == null) {
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
            }
            //查询规格列表
			typeTemplateService.findSpecList(newValue).success(function (response) {
                $scope.specList = response;
            })
        })
    });

    /**
	 * 规格checkbox点击事件
     * @param $event 当前的checkbox
     * @param specName 规格名称
     * @param optionName 规格选项列表
     */
    $scope.updateSpecAttribute=function ($event,specName,optionName) {
    	//先查找规格名称有没有存储过
		var obj = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,"attributeName",specName);
		//如果没有勾选过当前规格
		if(obj == null){
            $scope.entity.goodsDesc.specificationItems.push({
                "attributeName":specName,
                "attributeValue":[
                    optionName
                ]
            });
		}else {
			//如果是选中状态
            if ($event.target.checked) {
				//追加选项值
				obj.attributeValue.push(optionName);
        	}else{
				//取消勾选
                var optionIndex = obj.attributeValue.indexOf(optionName);
                obj.attributeValue.splice(optionIndex, 1);
                //如果取消节点后，当前规格已经没有选项，删除整个规格
				if(obj.attributeValue.length < 1){
                    var specIndex = $scope.entity.goodsDesc.specificationItems.indexOf(obj);
                    $scope.entity.goodsDesc.specificationItems.splice(specIndex, 1);
				}
			}
		}
		//更新sku列表
        $scope.createItemList();
    }

    // 1. 	创建$scope.createItemList方法，同时创建一条有基本数据，不带规格的初始数据
    $scope.createItemList=function () {
        // 基础行数据，参考: $scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' }]
        $scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' }];

        // 2. 	查找遍历所有已选择的规格列表，后续会重复使用它，所以我们可以抽取出个变量items
		var items = $scope.entity.goodsDesc.specificationItems;
		for(var i = 0; i < items.length; i++){
            // 9. 	回到createItemList方法中，在循环中调用addColumn方法，并让itemList重新指向返回结果;
            $scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}

    }
    // 3. 	抽取addColumn(当前的表格，列名称，列的值列表)方法，用于每次循环时追加列
	addColumn=function (list,specName,optionName) {
        // 4. 	编写addColumn逻辑，当前方法要返回添加所有列后的表格，定义新表格变量newList
		var newList =[];
        // 5. 	在addColumn添加两重嵌套循环，一重遍历之前表格的列表，二重遍历新列值列表
		for(var i = 0; i < list.length; i++){
			for(var j = 0; j < optionName.length; j++){
                // 6. 	在第二重循环中，使用深克隆技巧，把之前表格的一行记录copy所有属性，
				// 用到var newRow = JSON.parse(JSON.stringify(之前表格的一行记录));
				var newRow = JSON.parse(JSON.stringify(list[i]));
                // 7. 	接着第6步，向newRow里追加一列
                newRow.spec[specName]=optionName[j];
                // 8. 	把新生成的行记录，push到newList中
                newList.push(newRow);
			}
		}
		return newList;
    }

    $scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态

    $scope.itemCatList=[];//商品分类列表
    /**
	 * 查询并组装所有商品分类列表
     */
	$scope.findItemCatList=function () {
		itemCatService.findAll().success(function (response) {
			for(var i = 0; i < response.length; i++){
                $scope.itemCatList[response[i].id]=response[i].name;
			}
        })
    }

    /**
	 * 验证规格checkbox是否要勾中
     * @param specName 规格名称
     * @param optionName 规格选项名称
	 * @return {查找的结果}
     */
    $scope.checkAttributeValue=function (specName,optionName) {
        var obj = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,"attributeName",specName);
		if(obj != null){
			/*for(var i = 0; i < obj.attributeValue.length; i++){
				if(obj.attributeValue[i] == optionName){
					return true;
				}
			}*/

			if(obj.attributeValue.indexOf(optionName) >= 0){
				return true;
			}
		}
        return false;
    }
});
