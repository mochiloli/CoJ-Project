package com.mfec.speech.ServiceImp;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mfec.speech.Model.SoundChunks;
import com.mfec.speech.Service.SoundChunksService;

@Service
public class SoundChunksServiceImp implements SoundChunksService {
	
	private List<SoundChunks> SoundChunks;
	
	public SoundChunksServiceImp() {
		
	}

	@Override
	public SoundChunks findById(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SoundChunks> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

}
