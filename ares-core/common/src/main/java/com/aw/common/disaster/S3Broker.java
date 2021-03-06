package com.aw.common.disaster;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.aw.common.rest.security.TenantAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by scott on 15/09/16.
 */
public class S3Broker implements TenantAware {

	public static final Logger logger = LoggerFactory.getLogger(S3Broker.class);
	private AmazonS3 s3Client;


	public S3Broker() {


		AWSCredentialsProvider creds = new AWSCredentialsProviderChain(
			new EnvironmentVariableCredentialsProvider(),
			new SystemPropertiesCredentialsProvider()
		);


		this.s3Client  = AmazonS3ClientBuilder.standard()
			.withCredentials(creds)
			.withRegion(Regions.valueOf("EU_CENTRAL_1")) //TODO: env or platform?
			.build();
	}


	public void ensureNamespace (String fullNamespace) throws Exception {

		try {
			if(!(s3Client.doesBucketExist(fullNamespace)))
			{
				System.out.println(" bucket " + fullNamespace + " not found...creating  " );
				// Note that CreateBucketRequest does not specify region. So bucket is
				// created in the region specified in the client.
				s3Client.createBucket(new CreateBucketRequest(
					fullNamespace));

				System.out.println(" bucket " + fullNamespace + " CREATED  ");
			}


			// Get location.
			String bucketLocation = s3Client.getBucketLocation(new GetBucketLocationRequest(fullNamespace));
			System.out.println("bucket location = " + bucketLocation);

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which " +
				"means your request made it " +
				"to Amazon S3, but was rejected with an error response" +
				" for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which " +
				"means the client encountered " +
				"an internal error while trying to " +
				"communicate with S3, " +
				"such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}



	public void writeStream(String bucketName, String key, InputStream inputStream) throws Exception {


		try {

			ObjectMetadata meta = new ObjectMetadata();
			meta.setLastModified(Date.from(Instant.now()));
			PutObjectRequest req = new PutObjectRequest(bucketName, key, inputStream, meta);


			PutObjectResult por = s3Client.putObject(req);
			logger.warn(" stream written: " + bucketName + File.separator + key);


		}
		catch (Exception e) {
			throw  e;
		}
	}


	public  List<String> listKeys(String bucketPrefix) {
		return listKeys(bucketPrefix, null);
	}

	public  List<String> listKeys(String bucketPrefix, String prefix) {

		String bucketName = bucketPrefix + "-" + getTenantID();

		List<String> ret = new ArrayList<>();

		try {
			System.out.println("Listing objects");
			ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(2);

			if (prefix != null) {
				req = req.withPrefix(prefix);
			}


			ListObjectsV2Result result;
			do {
				result = s3Client.listObjectsV2(req);

				for (S3ObjectSummary objectSummary :
					result.getObjectSummaries()) {
					System.out.println(" - " + objectSummary.getKey() + "  " +
						"(size = " + objectSummary.getSize() +
						")");


					ret.add(objectSummary.getKey());

				}



				System.out.println("Next Continuation Token : " + result.getNextContinuationToken());
				req.setContinuationToken(result.getNextContinuationToken());
			} while(result.isTruncated() == true );




		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, " +
				"which means your request made it " +
				"to Amazon S3, but was rejected with an error response " +
				"for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, " +
				"which means the client encountered " +
				"an internal error while trying to communicate" +
				" with S3, " +
				"such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}

		return ret;
	}


	public void emptyBucket(String bucketName) {
		logger.warn("Emptying S3 bucket: " + bucketName);
		ObjectListing objectListing = s3Client.listObjects(bucketName);

		while (true) {
			for ( Iterator<?> iterator = objectListing.getObjectSummaries().iterator(); iterator.hasNext(); ) {
				S3ObjectSummary objectSummary = (S3ObjectSummary) iterator.next();
				s3Client.deleteObject(bucketName, objectSummary.getKey());
			}

			if (objectListing.isTruncated()) {
				objectListing = s3Client.listNextBatchOfObjects(objectListing);
			} else {
				break;
			}
		};
		VersionListing list = s3Client.listVersions(new ListVersionsRequest().withBucketName(bucketName));
		for ( Iterator<?> iterator = list.getVersionSummaries().iterator(); iterator.hasNext(); ) {
			S3VersionSummary s = (S3VersionSummary)iterator.next();
			s3Client.deleteVersion(bucketName, s.getKey(), s.getVersionId());
		}
	}

	public void deleteBucket(String bucketName) {
		logger.warn(" deleting S3 bucket: " + bucketName);
		emptyBucket(bucketName);
	}

	public List<String> listBuckets() {

		List<String> ret = new ArrayList<>();

		for (Bucket bucket: s3Client.listBuckets()) {
			ret.add(bucket.getName());
		}

		return ret;
	}

}
