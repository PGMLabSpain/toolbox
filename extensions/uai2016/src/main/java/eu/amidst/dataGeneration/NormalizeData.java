/*
 *
 *
 *    Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 *    See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0 (the "License"); you may not use
 *    this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software distributed under the License is
 *    distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and limitations under the License.
 *
 *
 */

package eu.amidst.dataGeneration;

import eu.amidst.core.datastream.Attribute;
import eu.amidst.core.datastream.DataInstance;
import eu.amidst.core.variables.stateSpaceTypes.RealStateSpace;
import eu.amidst.flinklink.core.data.DataFlink;
import eu.amidst.flinklink.core.io.DataFlinkLoader;
import eu.amidst.flinklink.core.io.DataFlinkWriter;
import org.apache.flink.api.common.functions.RichMapPartitionFunction;
import org.apache.flink.api.common.functions.RichReduceFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * Created by ana@cs.aau.dk on 08/02/16.
 */
public class NormalizeData {

    public static class MinMaxValues {
        double[] max;
        double[] min;

        public MinMaxValues(double[] min_, double[] max_){
            max = max_;
            min = min_;
        }
    }

    public static void assignRanges(DataFlink<DataInstance> dataFlink, String fileName, ExecutionEnvironment env) throws Exception {

        /*
         * Calculate max and min ranges for all attributes
         */
        System.out.println("--------------- Calculate max and min ranges for all attributes ---------------");

        MinMaxValues minMaxValues = dataFlink.getDataSet()
                .mapPartition(new GetRange(dataFlink.getAttributes().getNumberOfAttributes()))
                .reduce(new ReduceMinMax())
                .collect().get(0);

        /*
         * Write header with ranges
         */
        System.out.println("--------------- Write header with ranges ---------------");

        dataFlink = DataFlinkLoader.loadDataFromFolder(env,fileName, false);

        for (Attribute att : dataFlink.getAttributes()) {
            if (!att.getName().equalsIgnoreCase("DEFAULT")) {
                ((RealStateSpace) att.getStateSpaceType()).setMaxInterval(minMaxValues.max[att.getIndex()]);
                ((RealStateSpace) att.getStateSpaceType()).setMinInterval(minMaxValues.min[att.getIndex()]);
            }
        }

        DataFlinkWriter.writeHeader(env, dataFlink, fileName, true);

        //TODO: Strange behaviour, the header is only written if loadDataFromFolder is invoked afterwards, OMG!!
        DataFlinkLoader.loadDataFromFolder(env,fileName, false);

    }

    static class ReduceMinMax extends RichReduceFunction<MinMaxValues> {

        @Override
        public MinMaxValues reduce(MinMaxValues set1, MinMaxValues set2) throws Exception {

            double[] min = new double[set1.max.length];
            double[] max = new double[set1.max.length];
            for (int i = 0; i < min.length; i++) {
                if(set1.max[i]>set2.max[i])
                    max[i] = set1.max[i];
                else
                    max[i] = set2.max[i];
                if(set1.min[i]<set2.min[i])
                    min[i] = set1.min[i];
                else
                    min[i] = set2.min[i];

            }
            return new MinMaxValues(min, max);
        }
    }

    static class GetRange extends RichMapPartitionFunction<DataInstance, MinMaxValues> {

        int numAtts;

        public GetRange(int numAtts_){
            numAtts = numAtts_;
        }
        @Override
        public void mapPartition(Iterable<DataInstance> values, Collector<MinMaxValues> out){

            double[] min = new double[numAtts];
            double[] max = new double[numAtts];

            for (int i = 0; i < numAtts; i++) {
                min[i] = Double.MAX_VALUE;
                max[i] = Double.MIN_VALUE;
            }

            for(DataInstance instance: values){
                instance.getAttributes().forEach(att -> {
                    if(instance.getValue(att)>max[att.getIndex()])
                        max[att.getIndex()] = instance.getValue(att);
                    if(instance.getValue(att)<min[att.getIndex()])
                        min[att.getIndex()] = instance.getValue(att);
                });
            }

            out.collect(new MinMaxValues(min,max));

        }
    }

    public static void normalizeWithFlinkML(DataFlink<DataInstance> dataFlink, String fileName) throws Exception {

    }


    public static void main(String[] args) throws Exception{
        //String fileName = "./datasets/dataStream/uai1K.arff";


        String fileName = args[0];

        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        DataFlink<DataInstance> dataFlink = DataFlinkLoader.loadDataFromFolder(env,fileName, false);

        assignRanges(dataFlink, fileName, env);

        //normalizeWithFlinkML(dataStream, fileName);

    }
}