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

import java.util.TimerTask;

/**
 * Use the provided data manager to write data to destination at fixed interval based on a timer.
 */

public class ConsumerDataDumperTask extends TimerTask {

    private ConsumerDataManager dataManager;

    public ConsumerDataDumperTask(ConsumerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void run() {
        // dump content to cos bucket if needed
        dataManager.dumpData();

    }
}
