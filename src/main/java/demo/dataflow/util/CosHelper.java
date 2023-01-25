/* © Copyright IBM Corporation 2023
*
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package demo.dataflow.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import com.ibm.cloud.objectstorage.ClientConfiguration;
import com.ibm.cloud.objectstorage.auth.AWSCredentials;
import com.ibm.cloud.objectstorage.auth.AWSStaticCredentialsProvider;
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder;
import com.ibm.cloud.objectstorage.services.s3.model.Bucket;
import com.ibm.cloud.objectstorage.services.s3.model.GetObjectRequest;
import com.ibm.cloud.objectstorage.services.s3.model.ListObjectsRequest;
import com.ibm.cloud.objectstorage.services.s3.model.ObjectListing;
import com.ibm.cloud.objectstorage.services.s3.model.S3Object;
import com.ibm.cloud.objectstorage.services.s3.model.S3ObjectInputStream;
import com.ibm.cloud.objectstorage.services.s3.model.S3ObjectSummary;
import com.ibm.cloud.objectstorage.oauth.BasicIBMOAuthCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the helper class for COS bucket operations.
 */

public class CosHelper {

    private static final Logger logger = LoggerFactory.getLogger(CosHelper.class);

    private final String endpointUrl;
    private final String serviceInstanceId;
    private final String apikey;
    private final String location;

    private AmazonS3 cosClient;


    public CosHelper(String endpointUrl, String serviceInstanceId, String apikey, String location) {
        this.endpointUrl = endpointUrl;
        this.serviceInstanceId = serviceInstanceId;
        this.apikey = apikey;
        this.location = location;

        cosClient = getClient();
    }

    public AmazonS3 getClient() {
        if (cosClient != null) return cosClient;

        AWSCredentials credentials = new BasicIBMOAuthCredentials(apikey, serviceInstanceId);
        ClientConfiguration clientConfig = new ClientConfiguration()
                .withRequestTimeout(5000)
                .withTcpKeepAlive(true);

        cosClient = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new EndpointConfiguration(endpointUrl, location))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfig)
                .build();
        return cosClient;
    }

    public String listObjects(String bucketName) {
        logger.debug("Listing objects in bucket " + bucketName);
        StringBuffer sb = new StringBuffer();
        ObjectListing objectListing = getClient().listObjects(new ListObjectsRequest().withBucketName(bucketName));
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            sb.append(" - " + objectSummary.getKey() + "  " + "(size = " + objectSummary.getSize() + ")");
        }
        return sb.toString();
    }

    public void createBucket(String bucketName, String storageClass) {
        logger.debug("createBucket " + bucketName);
        getClient().createBucket(bucketName, storageClass);
    }

    public String listBuckets() {
        logger.debug("Listing buckets ");
        StringBuffer sb = new StringBuffer();
        final List<Bucket> bucketList = getClient().listBuckets();
        for (final Bucket bucket : bucketList) {
            sb.append(bucket.getName() + " ");
        }
        return sb.toString();
    }

    /**
    * Returns the string that represents the content of the object. 
    *
    * @param  bucketName  The bucket name of the cloud object storage
    * @param  key the object to process in the bucket
    * @param  fileNamePattern only process object whose name matches the pattern
    * @param  maxRow the max number of rows to process for the object
    * @return      the content of the object
    */
    public String getObject(String bucketName, String key, String fileWhitePattern, int maxRow) {
        // check the name pattern, process it only when it matches the pattern
        // if fileWhitePattern is null, allow all the access for now 
         if (fileWhitePattern != null && !isNameMatched(fileWhitePattern, key)) {
            // if object name is not in the white list, do nothing and return
            return null;
        }

        // if max_row == -1, process all the rows, otherwise, only process up to max_row
        logger.debug("Downloading object " + key + " from " + bucketName);
        GetObjectRequest objReq = new GetObjectRequest(bucketName, key);

        S3Object obj = getClient().getObject(objReq);
        S3ObjectInputStream inputStream = obj.getObjectContent();
        StringBuffer sb = new StringBuffer();
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            int counter = 0;
            
            while(reader.ready()) {
                String line = reader.readLine();
                counter++;
                sb.append(line + '\n');
                if (maxRow != -1 && counter >= maxRow) break;
           }

        } catch (Exception ex) {
            logger.error("Caught exception: " + ex);
        }
        return sb.toString();
    }

    public boolean isNameMatched(String patternString, String name) {
        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);
        boolean matchFound = matcher.find();
        if(matchFound) {
          return true;
        } else {
          return false;
        }
    
    }

    public InputStreamReader getObjectInputStream(String bucketName, String key, String fileWhitePattern, int maxRow) {
        logger.debug("getObjectInputStream: reading " + key + " from " + bucketName);
        // check the name pattern, process it only when it matches the pattern
        // if fileWhitePattern is null, allow all the access for now 
        if (fileWhitePattern != null && !isNameMatched(fileWhitePattern, key)) {
            // if object name is not in the white list, do nothing and return
            return null;
        }

        GetObjectRequest objReq = new GetObjectRequest(bucketName, key);

        S3Object obj = getClient().getObject(objReq);
        S3ObjectInputStream inputStream = obj.getObjectContent();
        return new InputStreamReader(inputStream);
    }

    public void putObject(String bucketName, String key, String content) {
        logger.debug("Entering putObject: bucketName:" + bucketName + ", key:" + key);
        getClient().putObject(
            bucketName, 
            key, 
            content);
    }
}

