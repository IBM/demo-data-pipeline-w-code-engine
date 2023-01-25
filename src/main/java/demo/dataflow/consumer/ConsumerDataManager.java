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

package demo.dataflow.consumer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import demo.dataflow.util.IDataDumper;

/**
 * Class to manage data records.
 * Data can be accumulated to a list. Data can be written to destination when it reaches 
 * certain number of records or when told to do so (eg. via a scheduled job).
 */
public class ConsumerDataManager {

    private IDataDumper dataDumper;
    private String bucketName;
    private String keyAppendix;
    private int batchSize;
    private ArrayList<String> dataRecords;


    private String DATE_TIME_PATTERN = "yyyy-MM-dd_HH-mm-ss";
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);  
 
    public ConsumerDataManager(IDataDumper dataDumper, String bucketName, String keyAppendix, int batchSize) {
        this.dataDumper = dataDumper;
        this.bucketName = bucketName;
        this.keyAppendix = keyAppendix;
        this.batchSize = batchSize;

        dataRecords = new ArrayList<String>();
    }

    public synchronized void addRecord(String record) {
        dataRecords.add(record);

        // check the size of the list, if it reached certain size, dump the data to COS bucket
        if (dataRecords.size() >= batchSize) dumpData();
    }

    public synchronized String getRecords() {
        // if no records, return null
        if (dataRecords.size() == 0) return null;
        String ret = String.join("\n", dataRecords);
        // clear the list
        dataRecords.clear();
        return ret;
    }

    public void dumpData() {
            // dump content to cos bucket if needed
            String content = getRecords();
            if (content != null ) {
                // generate key name
                String key = getDateTime() + "_" + keyAppendix;
                dataDumper.dumpData(bucketName+":"+key, content);
            }
    }

    public String getDateTime() {
        LocalDateTime now = LocalDateTime.now();  
        return dtf.format(now);
    }
}
