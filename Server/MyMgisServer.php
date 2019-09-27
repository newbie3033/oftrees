<?php

include_once './Server.php';
include_once './UserServer.php';
include_once './UpAndDown.php';

session_start();

$json = file_get_contents('php://input');


//$file = request()->file('file');
//$info = $file->move(ROOT_PATH . '/uploads/'); //图片保存路径
//$filesaveName = '/uploads/' . $info->getSaveName(); //储存到数据库
//if (!$info) {
//    $this->error('图片上传失败');
//}

$img = $_FILES['img']; //获取到表单过来的文件变量，uploadImg为表单id
//检测变量是否获取到
if (isset($img)) {
//上传成功$img中的属性error为0，当error>0时则上传失败有一下几种情况
    if ($img['error'] > 0) {
        $error = '上传失败:';
        switch ($img['error']) {
            case 1:
                $error .= '大小超过了服务器设置的限制！';
                break;
            case 2:
                $error .= '文件大小超过了表单设置的限制！';
                break;
            case 3:
                $error .= '文件只有部分被上传';
                break;
            case 4:
                $error .= '没有文件被上传';
                break;
            case 6:
                $error .= '上传文件的临时目录不存在！';
                break;
            case 7:
                $error .= '写入失败';
                break;
            default:
                $error .= '未知错误';
                break;
        }
        exit($error); //在php页面输出错误
    } else {
        $type = strrchr($img['name'], '.'); //截取文件后缀名
        $name = explode('_', $img['name']);
        $_path="./Uploads/" . $name[0];
        //新建文件夹
        $dir = iconv("UTF-8", "GBK", $_path);
        if (!file_exists($dir)) {
            mkdir($dir,0777, true);
        }
        $path = $_path . "/" . $img['name']; //设置路径：当前目录下的uploads文件夹并且图片名称为$img['name'];
        if (strtolower($type) == '.png' || strtolower($type) == '.jpg' || strtolower($type) == '.bmp' || strtolower($type) == '.gif') {//判断上传的文件是否为图片格式
            move_uploaded_file($img['tmp_name'], $path); //将图片文件移到该目录下
        }
    }
    echo'picture upload succeed!';
    exit(1);
}

//
$requestobj = json_decode($json);


$sv = new Server();

$type = $requestobj->type;

$typearr = explode("_", $type);

switch ($typearr[0]) {
    case"USER":
        $sv = new UserServer();
        break;
    case"DATA":
        $sv = new UpAndDown();
        break;
    default :
        $sv->makeResponse(false, "Does not contain the type,check your json");
        break;
}


$sv->setRequest($requestobj);
if (!$sv->openConnection()) {
    echo json_encode($sv->getResponse());
    exit(1);
}
$sv->run();
$sv->closeConnection();


echo json_encode($sv->getResponse());

