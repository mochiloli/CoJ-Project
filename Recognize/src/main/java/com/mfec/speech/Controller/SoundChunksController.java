package com.mfec.speech.Controller;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mfec.speech.Model.SoundChunks;
import com.mfec.speech.Transcribes.Recognize;

@RestController
@RequestMapping("/api")
public class SoundChunksController {
	
	@Autowired
	private Recognize rec;

	@GetMapping("/test")
	public String test() {
        return "Hello!!!!!!2";
    }
	
	@PostMapping(
			path = "/receiver", 
			consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	public String getGsLink(@RequestBody SoundChunks path) { //TO-DO Change to return JSONObject
		
		if(path != null) {
			rec = new Recognize(path.getUri(), path.getName());
			//rec.setGcsUri(path.getUri());
			rec.createThread(rec);
			rec.executeThread();
			return "ID: "+ path.getIndex() + "\nfilename: " + path.getName() + "\nguri: " + path.getUri() + "\nrange: " + path.getRange();
		}
		
        return null;
    }//End getGsLink
	
	@PostMapping(
			path = "/receiver2", 
			consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	public ResponseEntity<Object> receiveUri(@RequestBody SoundChunks path) {
		
		if(path != null) {
			rec = new Recognize(path.getUri(), path.getName());
			//rec.setGcsUri(path.getUri());
			rec.createThread(rec);
			rec.executeThread();
			ResponseEntity<Object> resp = new ResponseEntity<Object>(path, HttpStatus.OK);
			return resp;
		}else {
			ResponseEntity<Object> resp = new ResponseEntity<Object>( HttpStatus.NOT_FOUND);
	        return resp;
		}
		
	}//End receiveUri
	
}
