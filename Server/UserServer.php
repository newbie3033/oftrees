<?php

include_once './Server.php';


class UserServer extends Server {

    public function __construct() {
        parent::__construct();
    }

    public function __destruct() {
        parent::__destruct();
    }

    public function run() {
        parent::run();

        switch ($this->_request->type) {
            case "USER_HAS_LOGIN":
                $this->checkHasLogin();
                break;
            case "USER_LOGIN_OFF":
                $this->login_off();
                break;
            case "USER_LOGIN":
                $this->login();
                break;
            case "USER_REGISTER":
                $this->register();
                break;
            case "USER_GET_FRIEND_LIST":
                $this->getFriendList();
                break;
            case "USER_CHAT_SEND":
                $this->sendMessages();
                break;
            case "USER_CHAT_GET":
                $this->getMessage();
                break;
            default :
                $this->makeResponse(false, "Does not contain the type in USER");
                break;
        }
    }

    protected function login() {
        
        $sql = "select id,username from user1 where username=$1 and password=md5($2) limit 1";
        $result = pg_query_params($this->_connection, $sql, array(
            $this->_request->params->username,
            $this->_request->params->password
        ));
        if (pg_num_rows($result) === 1) {
            $this->makeResponse(true, "Login success");
            $row = pg_fetch_row($result);
            $_SESSION["USERID"] = $row[0];
            $_SESSION["USERNAME"] = $row[1];
            $_SESSION["LASTTIME"] = time();
        } else {
            $this->makeResponse(false, "Login failed");
        }

        pg_free_result($result);
    }

    protected function register() {
        $sql = "insert  into user1(username,password,email,phnumber) values($1,md5($2),$3,$4)";


        $sql2 = "select * from user1 where username=$1";
        $result2 = pg_query_params($this->_connection, $sql2, array($this->_request->params->username));
        if (pg_fetch_all($result2)) {
            $this->makeResponse(false, "Duplicate user name！");
            pg_free_result($result2);
        } else {

            $result = pg_query_params($this->_connection, $sql, array(
                $this->_request->params->username,
                $this->_request->params->password,
                $this->_request->params->email,
                $this->_request->params->mobile
            ));

            if ($result) {
                $this->makeResponse(true, "Register Success");
            } else {
                $this->makeResponse(false, "Register Failed");
            }
            pg_free_result($result);
        }
    }

    protected function checkHasLogin() {
        if (isset($_SESSION["USERID"])) {
            $this->makeResponse(true, "online");
        } else {
            $this->makeResponse(false, "offline");
        }
    }

    protected function login_off() {
        unset($_SESSION["USERID"]);
        unset($_SESSION["USERNAME"]);
        if (isset($_SESSION["USERID"]))
            $this->makeResponse(false, "log out failed");
        else
            $this->makeResponse(true, "log out success");
    }

    protected function getFriendList() {
        $sql = "
            select 
                A.id,A.user1,A.user2,C.username 
            from 
                qq_friendship A, 
                qq_user B, 
                qq_user C  
            where 
                A.user1 = B.id 
                and A.user1 = $1
                and C.id=A.user2 ";
        $result = pg_query_params($this->_connection, $sql, array(
            $_SESSION["USERID"]
        ));
        $i = 0;
        $users = array();
        while ($row = pg_fetch_row($result)) {
            $user = array(
                "id" => $row[0],
                "friendid" => $row[2],
                "friendname" => $row[3]
            );
            $users[$i++] = $user;
        }
        $_SESSION["LASTTIME"] = time();

        $this->makeResponse(true, "friend fresh successful", $users);
    }

    protected function sendMessages() {
        $sql = "insert into qq_message(from_id,to_id,message) values($1,$2,$3);";
        $result = pg_query_params($this->_connection, $sql, array(
            $_SESSION["USERID"],
            $this->_request->params->toUser,
            $this->_request->params->message
        ));
        if ($result) {
           $_SESSION["LASTTIME"] = time();
            $this->makeResponse(true, 'send message successful');
        }
    }

    protected function getMessage() {
        $sql = "select message,extract(epoch from qq_message.sendtime) from qq_message where from_id=$1 and to_id=$2";
        $result = pg_query_params($this->_connection, $sql, array(
            $this->_request->params->toUser,
            $_SESSION["USERID"]
        ));
        $i = 0;
        $messages = array();
        while ($row = pg_fetch_row($result)) {
            $t= explode(".", $row[1]);
            if ($t > $_SESSION["LASTTIME"]) {
                $date= date('Y-m-d H:i:s',$row[1]);
                $message = array(
                    "message" => $row[0],
                    "time" => $date
                );
                $messages[$i++] = $message;
            }
        }
        if (isset($messages))
            $this->makeResponse(true, "get message successful", $messages);
        else
            $this->makeResponse(false, "get message failed", $messages);
    }

}
