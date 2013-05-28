/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.framework.check.transformer;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;

/**
 * The class Transformer02.
 * 
 * @author Prasser, Kohlmayer
 */
public class Transformer02 extends AbstractTransformer {

    /**
     * Instantiates a new transformer.
     * 
     * @param data
     *            the data
     * @param hierarchies
     *            the hierarchies
     */
    public Transformer02(final int[][] data,
                         final GeneralizationHierarchy[] hierarchies,
                         final int[] sensitiveValues,
                         final IntArrayDictionary dictionarySensValue,
                         final IntArrayDictionary dictionarySensFreq,
                         final ARXConfiguration config) {
        super(data, hierarchies, sensitiveValues, dictionarySensValue, dictionarySensFreq, config);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.framework.check.transformer.AbstractTransformer
     * #walkAll()
     */
    @Override
    protected void processAll() {
        for (int i = startIndex; i < stopIndex; i++) {
            intuple = data[i];
            outtuple = buffer[i];
            outtuple[outindex0] = idindex0[intuple[index0]][stateindex0];
            outtuple[outindex1] = idindex1[intuple[index1]][stateindex1];

            // TODO: Maybe move to a separate method shared between all
            // transformers
            switch (requirements) {
            case ARXConfiguration.REQUIREMENT_COUNTER:
                groupify.addAll(outtuple, i, 1, -1, -1);
                break;
            case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER:
                groupify.addAll(outtuple, i, 1, -1, 1);
                break;
            case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER | ARXConfiguration.REQUIREMENT_DISTRIBUTION:
                groupify.addAll(outtuple, i, 1, sensitiveValues[i], 1);
                break;
            // TODO: If we only need a distribution, we should get rid of the primary counter
            case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_DISTRIBUTION:
            case ARXConfiguration.REQUIREMENT_DISTRIBUTION:
                groupify.addAll(outtuple, i, 1, sensitiveValues[i], -1);
                break;
            default:
                throw new RuntimeException("Invalid requirements: " + requirements);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.framework.check.transformer.AbstractTransformer
     * #walkGroupify ()
     */
    @Override
    protected void processGroupify() {

        int processed = 0;
        while (element != null) {

            intuple = data[element.representant];
            outtuple = buffer[element.representant];
            outtuple[outindex0] = idindex0[intuple[index0]][stateindex0];
            outtuple[outindex1] = idindex1[intuple[index1]][stateindex1];

            // TODO: Maybe move to a separate method shared between all
            // transformers
            switch (requirements) {
            case ARXConfiguration.REQUIREMENT_COUNTER:
                groupify.addGroupify(outtuple, element.representant, element.count, null, -1);
                break;
            case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER:
                groupify.addGroupify(outtuple, element.representant, element.count, null, element.pcount);
                break;
            case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER | ARXConfiguration.REQUIREMENT_DISTRIBUTION:
                groupify.addGroupify(outtuple, element.representant, element.count, element.distribution, element.pcount);
                break;
            // TODO: If we only need a distribution, we should get rid of the primary counter
            case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_DISTRIBUTION:
            case ARXConfiguration.REQUIREMENT_DISTRIBUTION:
                groupify.addGroupify(outtuple, element.representant, element.count, element.distribution, -1);
                break;
            default:
                throw new RuntimeException("Invalid requirements: " + requirements);
            }

            // Next element
            processed++;
            if (processed == numElements) { return; }
            element = element.nextOrdered;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.framework.check.transformer.AbstractTransformer
     * #walkSnapshot ()
     */
    @Override
    protected void processSnapshot() {

        startIndex *= ssStepWidth;
        stopIndex *= ssStepWidth;

        for (int i = startIndex; i < stopIndex; i += ssStepWidth) {
            intuple = data[snapshot[i]];
            outtuple = buffer[snapshot[i]];
            outtuple[outindex0] = idindex0[intuple[index0]][stateindex0];
            outtuple[outindex1] = idindex1[intuple[index1]][stateindex1];

            // TODO: Maybe move to a separate method shared between all
            // transformers
            switch (requirements) {
            case ARXConfiguration.REQUIREMENT_COUNTER:
                groupify.addSnapshot(outtuple, snapshot[i], snapshot[i + 1], null, null, -1);
                break;
            case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER:
                groupify.addSnapshot(outtuple, snapshot[i], snapshot[i + 1], null, null, snapshot[i + 2]);
                break;
            case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER | ARXConfiguration.REQUIREMENT_DISTRIBUTION:
                groupify.addSnapshot(outtuple, snapshot[i], snapshot[i + 1], dictionarySensValue.get(snapshot[i + 3]), dictionarySensFreq.get(snapshot[i + 4]), snapshot[i + 2]);
                break;
            // TODO: If we only need a distribution, we should get rid of the primary counter
            case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_DISTRIBUTION:
            case ARXConfiguration.REQUIREMENT_DISTRIBUTION:
                groupify.addSnapshot(outtuple, snapshot[i], snapshot[i + 1], dictionarySensValue.get(snapshot[i + 2]), dictionarySensFreq.get(snapshot[i + 3]), -1);
                break;
            default:
                throw new RuntimeException("Invalid requirements: " + requirements);
            }
        }
    }
}
