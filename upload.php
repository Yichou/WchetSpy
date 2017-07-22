<?php

function upload() {
//	var_dump($_FILES);
	
        $fname = $_FILES['file']["name"];
        $fpath = $_FILES['file']["tmp_name"];
        
        
        if(empty($fname) || empty($fpath)) {
            throw new Exception('Input file error');
        }
        
		$b = $_POST['bucket'];
		$bp = $_POST['path'];
		
		$path = $_SERVER['DOCUMENT_ROOT'] . '/upload/';
		if(!empty($b))
			$path .= $b . '/';
		if(!empty($bp))
			$path .= $bp . '/';
		
		if(!is_dir($path))
			mkdir($path, 0755, TRUE);
		
		$path .= $fname;
		
		if(is_file($path))
			unlink($path);
		
//		var_dump($path);
		
		if(move_uploaded_file($fpath, $path)) {
//			echo 'ok|' . $path;
			die(json_encode(array('code'=>200, 'path'=>$path)));
		} else {
//			echo 'error';
			die(json_encode(array('code'=>201, 'path'=>$path)));
		}
		
//		var_dump($fname);
//		var_dump($path);
}

upload();

?>