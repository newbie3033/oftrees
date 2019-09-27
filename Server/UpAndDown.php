<?php

include_once './Server.php';

class UpAndDown extends Server {

    public function __construct() {
        parent::__construct();
    }

    public function __destruct() {
        parent::__destruct();
    }

    public function run() {
        parent::run();

        switch ($this->_request->type) {
            case "DATA_UPLOADFILE":
                $this->Data_UploadFile();
                break;
            case "DATA_DOWNLOADFILE":
                $this->Data_DownloadFile();
                break;
            case "DATA_GETINFO":
                $this->Data_GetInfo();
                break;
            case "DATA_INPUTINFO":
                $this->Data_InputInfo();
                break;
            case "DATA_UPDATE":
                $this->Data_Update();
                break;
            case "DATA_GET_TREES":
                $this->Data_GetTrees();
                break;
            case"DATA_CHECK":
                $this->Data_Check();
                break;
            case"DATA_DELETE":
                $this->Data_Delete();
                break;
            default :
                $this->makeResponse(false, "Does not contain the type in Data");
                break;
        }
    }

    protected function Data_Delete() {
        $picID = $this->_request->params->t_id;
        // 获取文件夹
        $dir = "./Uploads/" . $picID;
        if (file_exists($dir)) {
            //先删除目录下的文件：
            $dh = opendir($dir);
            while ($file = readdir($dh)) {
                if ($file != "." && $file != "..") {
                    $fullpath = $dir . "/" . $file;
                    unlink($fullpath);
                }
            }
            closedir($dh);
            //删除当前文件夹：
            if (rmdir($dir)) {
                $this->makeResponse(true, "delete secceed");
            } else {
                $this->makeResponse(false, "delete failed");
            }
        }
        $this->makeResponse(true, "no filepath");
    }

    protected function Data_Check() {
        $sql1 = "select * from tree_info where t_positionlat=$1 and t_positionlng=$2 or t_id=$3";
        $result1 = pg_query_params($this->_connection, $sql1,
                array($this->_request->params->positionLat,
                    $this->_request->params->positionLng,
                    $this->_request->params->id));
        $rows = pg_fetch_all($result1);
        if (pg_num_rows($result1) > 0) {
            $this->makeResponse(true, "Duplicate tree position！");
            pg_free_result($result1);
        } else {
            $this->makeResponse(false, "no duplicate");
            pg_free_result($result1);
        }
    }

    protected function Data_GetTrees() {
        $sql = "select t_positionlat,t_positionlng  from  tree_info";
        $result = pg_query_params($this->_connection, $sql, array());

        if (pg_num_rows($result) > 0) {
            $rows = pg_fetch_all($result);
            $this->makeResponse(true, "you got it", $rows);
        } else {
            $this->makeResponse(false, "no tree");
        }

        pg_free_result($result);
    }

    protected function Data_UploadFile() {
        
    }

    protected function Data_DownloadFile() {
        $picID = $this->_request->params->id;
        // 获取当前文件的上级目录
        $con = "./Uploads/" . $picID . "/";
        if (!file_exists($con)) {
            $this->makeResponse(false, "there is no pic");
            exit(1);
        }
        // 扫描$con目录下的所有文件
        $filename = scandir($con);
        // 定义一个数组接收文件名
        $_target = array();
        for ($k = 0; $k < count($filename); $k++) {
            // 跳过两个特殊目录   continue跳出循环
            if ($v == "." || $v == "..") {
                continue;
            }
            $tempID = substr($filename[$k], 0, strpos($filename[$k], "_"));
            if ($tempID == $picID) {
                $_target[] = substr($filename[$k], 0, strpos($filename[$k], "."));
            }
        }
        if (count($_target) < 1) {
            $this->makeResponse(false, "there is no pic", $_target);
        } else {
            $this->makeResponse(true, "you get pics path", $_target);
        }
    }

    protected function Data_GetInfo() {
        $sql = "select t_location,t_location_detail,t_id,t_family,
                       t_category,t_chsname,t_alias,t_latinname,
                       t_trait,t_type,t_age,t_rounds,
                       t_crown_ave,t_crown_ew,t_crown_sn,t_elevation,
                       t_aspect,t_gradient,t_slope,t_soil_name,
                       t_soil_density,t_growth_potential,t_growth_environment,t_history_detail
                from tree_info
                where t_positionlat=$1 and t_positionlng=$2
                ";

        $result = pg_query_params($this->_connection, $sql, array(
            $this->_request->params->positionLat,
            $this->_request->params->positionLng
        ));
        if (pg_num_rows($result) === 1) {
            $row = pg_fetch_row($result);
        } else {
            $this->makeResponse(false, "query information failed:row!=1");
            pg_free_result($result);
            exit(1);
        }
        $infomations = array(
            "t_location" => $row[0],
            "t_location_detail" => $row[1],
            "t_id" => $row[2],
            "t_family" => $row[3],
            "t_category" => $row[4],
            "t_chsname" => $row[5],
            "t_alias" => $row[6],
            "t_latinname" => $row[7],
            "t_trait" => $row[8],
            "t_type" => $row[9],
            "t_age" => $row[10],
            "t_rounds" => $row[11],
            "t_crown_ave" => $row[12],
            "t_crown_ew" => $row[13],
            "t_crown_sn" => $row[14],
            "t_elevation" => $row[15],
            "t_aspect" => $row[16],
            "t_gradient" => $row[17],
            "t_slope" => $row[18],
            "t_soil_name" => $row[19],
            "t_soil_density" => $row[20],
            "t_growth_potential" => $row[21],
            "t_growth_environment" => $row[22],
            "t_history_detail" => $row[23],
        );

        $this->makeResponse(true, "query information succeed", $infomations);

        pg_free_result($result);
    }

    protected function Data_InputInfo() {
        $sql = "insert into tree_info(t_positionlat,t_positionlng,t_location,t_location_detail,
                                        t_id,t_family,t_category,t_chsname,t_alias,t_latinname,
                                        t_trait,t_type,t_age,t_rounds,t_crown_ave,t_crown_ew,t_crown_sn,
                                        t_elevation,t_aspect,t_gradient,t_slope,t_soil_name,t_soil_density,
                                        t_growth_potential,t_growth_environment,t_history_detail)
                                    values($1,$2,$3,$4,
                                           $5,$6,$7,$8,
                                           $9,$10,$11,$12,
                                           $13,$14,$15,$16,
                                           $17,$18,$19,$20,
                                           $21,$22,$23,$24,
                                           $25,$26)
                ";


        $sql2 = "delete from tree_info where t_positionlat=$1 and t_positionlng=$2 or t_id=$3";
        $result2 = pg_query_params($this->_connection, $sql2,
                array($this->_request->params->t_positionlat,
                    $this->_request->params->t_positionlng,
                    $this->_request->params->t_id));
        pg_free_result($result2);

        $result = pg_query_params($this->_connection, $sql, array(
            $this->_request->params->t_positionlat,
            $this->_request->params->t_positionlng,
            $this->_request->params->t_location,
            $this->_request->params->t_location_detail,
            $this->_request->params->t_id,
            $this->_request->params->t_family,
            $this->_request->params->t_category,
            $this->_request->params->t_chsname,
            $this->_request->params->t_alias,
            $this->_request->params->t_latinname,
            $this->_request->params->t_trait,
            $this->_request->params->t_type,
            $this->_request->params->t_age,
            $this->_request->params->t_rounds,
            $this->_request->params->t_crown_ave,
            $this->_request->params->t_crown_ew,
            $this->_request->params->t_crown_sn,
            $this->_request->params->t_elevation,
            $this->_request->params->t_aspect,
            $this->_request->params->t_gradient,
            $this->_request->params->t_slope,
            $this->_request->params->t_soil_name,
            $this->_request->params->t_soil_density,
            $this->_request->params->t_growth_potential,
            $this->_request->params->t_growth_environment,
            $this->_request->params->t_history_detail
        ));

        if ($result) {
            $this->makeResponse(true, "Input Information Succeed");
        } else {
            $this->makeResponse(false, "Input Information Failed");
        }
        pg_free_result($result);
    }

    protected function Data_Update() {
        
    }

}
