package com.mfec.speech.Service;

import java.util.List;

import com.mfec.speech.Model.SoundChunks;

public interface SoundChunksService {

	public SoundChunks findById(int id) ;
	
	public List<SoundChunks> findAll();
}
