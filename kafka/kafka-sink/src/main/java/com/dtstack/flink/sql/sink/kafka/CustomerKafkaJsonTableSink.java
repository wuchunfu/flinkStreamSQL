/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtstack.flink.sql.sink.kafka;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.kafka.KafkaTableSink;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.utils.TableConnectorUtils;
import org.apache.flink.types.Row;

import java.util.Optional;
import java.util.Properties;

/**
 * @author: chuixue
 * @create: 2019-11-05 11:54
 * @description:
 **/
public class CustomerKafkaJsonTableSink extends KafkaTableSink {

    protected SerializationSchema schema;


    public CustomerKafkaJsonTableSink(TableSchema schema,
                                      String topic,
                                      Properties properties,
                                      Optional<FlinkKafkaPartitioner<Row>> partitioner,
                                      SerializationSchema<Row> serializationSchema) {

        super(schema, topic, properties, partitioner, serializationSchema);
        this.schema = serializationSchema;
    }

    @Override
    protected SinkFunction<Row> createKafkaProducer(String topic, Properties properties, SerializationSchema<Row> serializationSchema, Optional<FlinkKafkaPartitioner<Row>> optional) {
        return new CustomerFlinkKafkaProducer<Row>(topic, serializationSchema, properties);
    }

    @Override
    public void emitDataStream(DataStream<Row> dataStream) {
        SinkFunction<Row> kafkaProducer = createKafkaProducer(topic, properties, schema, partitioner);
        // always enable flush on checkpoint to achieve at-least-once if query runs with checkpointing enabled.
        //kafkaProducer.setFlushOnCheckpoint(true);
        dataStream.addSink(kafkaProducer).name(TableConnectorUtils.generateRuntimeName(this.getClass(), getFieldNames()));
    }
}
