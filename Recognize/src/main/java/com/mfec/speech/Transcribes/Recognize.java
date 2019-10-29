package com.mfec.speech.Transcribes;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1p1beta1.SpeechContext;
import com.google.cloud.speech.v1p1beta1.LongRunningRecognizeMetadata;
import com.google.cloud.speech.v1p1beta1.LongRunningRecognizeResponse;
import com.google.cloud.speech.v1p1beta1.RecognitionAudio;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1p1beta1.RecognitionMetadata;
import com.google.cloud.speech.v1p1beta1.RecognitionMetadata.InteractionType;
import com.google.cloud.speech.v1p1beta1.RecognitionMetadata.MicrophoneDistance;
import com.google.cloud.speech.v1p1beta1.RecognitionMetadata.RecordingDeviceType;
import com.google.cloud.speech.v1p1beta1.RecognizeResponse;
import com.google.cloud.speech.v1p1beta1.SpeakerDiarizationConfig;
import com.google.cloud.speech.v1p1beta1.SpeechClient;

import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionResult;
import com.google.cloud.speech.v1p1beta1.WordInfo;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

@Component
public class Recognize implements Runnable {
	private String gcsUri;
	private String filename;
	Thread thread;
	ExecutorService executor = Executors.newFixedThreadPool(3);

	public Recognize() {

	}

	public Recognize(String gcsUri, String name) {
		this.gcsUri = gcsUri;
		this.filename = name;
	}

	class Node {
		String guri;
		boolean state;
		int display; // 0 = yet, 1 = already, 2 = show
		Node prev;
		Node next;

		Node(String uri, Boolean stat, int dis) {
			guri = uri;
			state = stat;
			display = dis;
		}
	}

	Node head;

	private static Recognize rc = new Recognize();

	// Add a node at the end of the list
	public void append(String new_uri, Boolean new_state, int new_display) {
		Node new_node = new Node(new_uri, new_state, new_display);

		Node last = head;

		new_node.next = null;

		if (head == null) {
			new_node.prev = null;
			head = new_node;
			return;
		}

		while (last.next != null)
			last = last.next;

		last.next = new_node;

		new_node.prev = last;
	}

	// This function prints contents of linked list starting from the given node
	public static void printList(Node node) {
		Node last = null;
		System.out.println("Traversal in forward Direction");
		while (node != null) {
			System.out.println(node.guri + " ");
			System.out.println(node.state + " ");
			System.out.println(node.display + " ");
			last = node;
			node = node.next;
		}
		System.out.println();
		/*
		 * System.out.println("Traversal in reverse direction"); while (last != null) {
		 * System.out.println(last.guri + " "); System.out.println(last.state + " ");
		 * System.out.println(last.display + " "); last = last.prev; }
		 */
	}

	public void changeTranStateToDone(String guri) {
		Node currNode = rc.head;

		if (currNode != null && currNode.guri.equals(guri)) {
			System.out.println(guri + " : found and update state (head)");
			currNode.state = true;
			currNode.display = 1;
			return;
		}

		while (currNode != null && !currNode.guri.equals(guri)) {
			currNode = currNode.next;
		}

		if (currNode != null) {
			currNode.state = true;
			currNode.display = 1;
			System.out.println(guri + " : found update state");
		} else {
			System.out.println(guri + " not found");
		}
	}

	public void CheckFromHeadForShow() {
		Node currNode = rc.head;

		while (currNode != null) {
			if (currNode.display == 1) {
				System.out.println(currNode.guri + " : show and update display state to : 2");
				currNode.display = 2;
				currNode = currNode.next;
			} else if (currNode.display == 2) {
				System.out.println(currNode.guri + " : showed");
				currNode = currNode.next;
			} else {
				System.out.println("Nothing show next");
				return;
			}
		}
		
		if(currNode == null) {
			deleteList();
		}

	}
	
	public void deleteList() { 
        rc.head = null; 
    } 

	@Override
	public void run() {
		try (SpeechClient speechClient = SpeechClient.create()) {

			ArrayList<String> languageList = new ArrayList<>();
			languageList.add("es-ES");
			languageList.add("en-US");
			languageList.add("th-TH");

			// Configure request to enable multiple languages
			RecognitionConfig config = RecognitionConfig.newBuilder().setEncoding(AudioEncoding.LINEAR16)
					// .setSampleRateHertz(44100)
					.setLanguageCode("th-TH")
					// .setAudioChannelCount(2)
					// .setEnableSeparateRecognitionPerChannel(true)
					.addAllAlternativeLanguageCodes(languageList).setEnableWordTimeOffsets(true).build();

			// Set the remote path for the audio file
			RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(gcsUri).build();

			PrintWriter writer = new PrintWriter(filename+".txt", "UTF-8");

			// Use non-blocking call for getting file transcription
			OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response = speechClient
					.longRunningRecognizeAsync(config, audio);

			System.out.println("Waiting transcribe..." + gcsUri);
			while (!response.isDone()) {
				System.out.println("Waiting for response..." + gcsUri);
				Thread.sleep(10000);
			}
			response.get().getResultsList().stream().forEach(s -> writer.println(s.getAlternatives(0).getTranscript()));
			for (SpeechRecognitionResult result : response.get().getResultsList()) {

				// There can be several alternative transcripts for a given chunk of speech.
				// Just use the
				// first (most likely) one here.
				SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);

				// Print out the result
				writer.println("Transcript : " + alternative.getTranscript() + "\n");
				//System.out.printf("Transcript : %s\n\n", alternative.getTranscript());
				// Word and each sec
				/*for (WordInfo wordInfo : alternative.getWordsList()) {
					writer.println(wordInfo.getWord());
					writer.println("\t" + wordInfo.getStartTime().getSeconds() + "."
							+ wordInfo.getStartTime().getNanos() / 100000000 + "sec - "
							+ wordInfo.getEndTime().getSeconds() + "." + wordInfo.getEndTime().getNanos() / 100000000
							+ " sec\n");
				}*/
				writer.println("JSON : \n" + result.getAlternatives(0).toString());
				//writer.println("=========================================================");
			}
			writer.close();
			rc.changeTranStateToDone(gcsUri);
			System.out.println("End..." + gcsUri);
			CheckFromHeadForShow();

		} // end try
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createThread(Recognize rec) {
		thread = new Thread(rec);
		rc.append(rec.gcsUri,false,0);
	}
	
	public void executeThread() {
		//rc.append(this.gcsUri,false,0);
		executor.execute(thread);
	}

	public String getGcsUri() {
		return gcsUri;
	}

	public void setGcsUri(String gcsUri) {
		this.gcsUri = gcsUri;
	}
}
