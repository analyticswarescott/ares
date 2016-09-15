package com.aw.common.disaster;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.aw.common.rest.security.SecurityUtil;

/**
 * Created by scott on 14/09/16.
 */
public class Test {



	public static void main(String[] args) throws Exception {

		SecurityUtil.setThreadSystemAccess();

		DefaultColdStorageProvider csp = new DefaultColdStorageProvider();
		csp.init("shill-buckets"); //set namespace=S3 bucket

		File f = new File("/Users/scott/dev/src/ares/cluster/.editorconfig");
		csp.storeStream("prefix1-file2", new FileInputStream(f));

		for (String s : csp.getKeyList()) {
			System.out.println(" key in bucket: " + s);
		}

		System.out.println(" ====== now with prefix ");
		for (String s : csp.getKeyList("prefix2")) {
			System.out.println(" key in bucket: " + s);
		}

	}


	//TODO: implement multi-part if needed

/*
		public static void main(String[] args) throws IOException {


			String existingBucketName  = "shill-1234";
			String keyName             = "myfile1";
			String filePath            = "/Users/scott/dev/src/ares/cluster/.editorconfig";

		*/
/*	ProfileCredentialsProvider pcp = new ProfileCredentialsProvider();
			pcp.
			pcp.getCredentials();
			AmazonS3 s3Client = new AmazonS3Client(pcp);*//*



			AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
				.withCredentials(new EnvironmentVariableCredentialsProvider())
				.withRegion(Regions.EU_CENTRAL_1)
				.build();


// Create a list of UploadPartResponse objects. You get one of these for
// each part upload.
			List<PartETag> partETags = new ArrayList<PartETag>();

// Step 1: Initialize.
			InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(
				existingBucketName, keyName);
			InitiateMultipartUploadResult initResponse =
				s3Client.initiateMultipartUpload(initRequest);

			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);


			long contentLength = file.length();
			long partSize = 5 * 1024 * 1024; // Set part size to 5 MB.

			try {
				// Step 2: Upload parts.
				long filePosition = 0;
				for (int i = 1; filePosition < contentLength; i++) {
					// Last part can be less than 5 MB. Adjust part size.
					partSize = Math.min(partSize, (contentLength - filePosition));

					// Create request to upload a part.
					UploadPartRequest uploadRequest = new UploadPartRequest()
						.withBucketName(existingBucketName).withKey(keyName)
						.withUploadId(initResponse.getUploadId()).withPartNumber(i)
						.withFileOffset(filePosition)
						//.withFile(file)
						.withInputStream(fis)
						.withPartSize(partSize);

					// Upload part and add response to our list.
					partETags.add(s3Client.uploadPart(uploadRequest).getPartETag());

					filePosition += partSize;
				}

				// Step 3: Complete.
				CompleteMultipartUploadRequest compRequest = new
					CompleteMultipartUploadRequest(existingBucketName,
					keyName,
					initResponse.getUploadId(),
					partETags);

				s3Client.completeMultipartUpload(compRequest);

				System.out.println("upload id was: " + compRequest.getUploadId());


				listKeys(s3Client, "shill-1234");

			} catch (Exception e) {
				s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(
					existingBucketName, keyName, initResponse.getUploadId()));
				throw  e;
			}



		}
*/




}





