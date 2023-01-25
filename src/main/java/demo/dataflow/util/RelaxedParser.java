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
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * This class extends the default parser and allows extra parameters being passed in. 
 * When program is invoked via code engine or cloud function, it may contain extra parameters,
 * which program will simply ignore, rather than being crashed.
 */

public class RelaxedParser extends DefaultParser {

    /**
     * Parses the arguments according to options
     * @param options   the options allowed by the program
     * @param arguments the command line arguments passed in
     */
    @Override
    public CommandLine parse(final Options options, final String[] arguments) throws ParseException {
        // list to hold valid arguments
        final List<String> validArgs = new ArrayList<>();

        for (int i = 0; i < arguments.length; i++) {
            // check if the argument is valid, if so, collect it and pass it to parent parser
            if (options.hasOption(arguments[i])) {
                validArgs.add(arguments[i]);

                // collect the value of the option, if needed
                if (options.getOption(arguments[i]).hasArg() && arguments.length > (i + 1) ) {
                    validArgs.add(arguments[i + 1]);
                }
            }
        }
        return super.parse(options, validArgs.toArray(new String[0]));
    }
}
