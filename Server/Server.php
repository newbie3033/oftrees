<?php

include_once './DBConfig.php';

class Server {

    protected $_request = NULL;
    protected $_response = NULL;
    protected $_connection = false;

    public function __construct() {
        $this->makeResponse(false, "Unknow Error");
    }

    public function __destruct() {
        
    }

    public function run() {
        
    }

    public function setRequest($request) {
        $this->_request = $request;
    }

    public function makeResponse($isok, $message, $data = array()) {
        $this->_response = array(
            "success" => $isok,
            "message" => $message,
            "data" => $data
        );
    }

    public function getResponse() {
        return $this->_response;
    }

    public function openConnection() {
        $connstr = "host=" . HOST . " port=" . PORT
                . " dbname=" . DBNAME . " user=" . USER . " password=" . PASSWORD;

        $conn = @pg_connect($connstr);

        if (!$conn) {
            $this->makeResponse(false, "Can not connect to db server");
            return false;
        }
        $this->_connection = $conn;
        return true;
    }
    
    public function closeConnection(){
        if($this->_connection){
            pg_close($this->_connection);
            $this->_connection = false;
        }
    }

}
