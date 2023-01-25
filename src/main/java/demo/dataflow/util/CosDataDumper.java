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

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class writes data to COS bucket.
 */
public class CosDataDumper extends CosHelper implements IDataDumper {

    private static final Logger logger = LoggerFactory.getLogger(CosDataDumper.class);

    public CosDataDumper(String endpointUrl, String serviceInstanceId, String apikey, String location) {
        super(endpointUrl, serviceInstanceId, apikey, location);
    }

    @Override
    public void dumpData(String targetName, ArrayList<String> dataRecords) {
        logger.debug("dump ArrayList to target " + targetName);
        String delimiter = "\n";
        String content = String.join(delimiter, dataRecords);
        dumpData(targetName, content);
    }

    @Override
    public void dumpData(String targetName, String content) {
        logger.debug("dump string to target " + targetName);
        // targetName is of format bucketName:keyName
        String[] dest = targetName.split(":");
        // dest should have 2 elements
        if (dest.length != 2) return;
        String bucketName = dest[0];
        String keyName = dest[1];
        putObject(bucketName, keyName, content);
    }


    
}
