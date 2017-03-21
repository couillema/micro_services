/**
 * Copyright 2016 Dhatim
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dhatim.io.dropwizard.metrics.elasticsearch;

import io.dropwizard.metrics.BaseReporterFactory;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.metrics.ElasticsearchReporter;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("elasticsearch")
public class ElasticSearchReporterFactory extends BaseReporterFactory {
    private static class Server {
        @NotEmpty
        private String host = "localhost";

        @JsonProperty
        @Range(min = 0, max = 49151)
        private int port = 9002;

        @JsonProperty
        public String getHost() {
            return this.host;
        }

        @JsonProperty
        public void setHost(String host) {
            this.host = host;
        }

        @JsonProperty
        public int getPort() {
            return this.port;
        }

        @JsonProperty
        public void setPort(int port) {
            this.port = port;
        }
    }

    @NotEmpty
    private Server[] servers;

    private String prefix = "";

    private String index = "metrics";

    private String indexDateFormat = "yyyy.MM.dd";

    private Map<String, Object> additionalFields;

    @JsonProperty
    public Server[] getServers() {
        return this.servers;
    }

    @JsonProperty
    public void setServers(Server[] servers) {
        this.servers = servers;
    }

    @JsonProperty
    public String getPrefix() {
        return this.prefix;
    }

    @JsonProperty
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @JsonProperty
    public String getindex() {
        return this.index;
    }

    @JsonProperty
    public void setindex(String index) {
        this.index = index;
    }

    @JsonProperty
    public String getIndexDateFormat() {
        return this.indexDateFormat;
    }

    @JsonProperty
    public void setIndexDateFormat(String indexDateFormat) {
        this.indexDateFormat = indexDateFormat;
    }

    @JsonProperty
    public Map<String, Object> getAdditionalFields() {
        return this.additionalFields;
    }

    @JsonProperty
    public void setAdditionalFields(Map<String, Object> additionalFields) {
        this.additionalFields = additionalFields;
    }

    @Override
    public ScheduledReporter build(MetricRegistry registry) {
        try {
            String[] hosts = new String[servers.length];
            for (int i=0; i<servers.length; i++) {
                hosts[i] = new StringBuilder()
                    .append(servers[i].getHost())
                    .append(":")
                    .append(servers[i].getPort())
                    .toString();
            }

            return ElasticsearchReporter.forRegistry(registry)
                    .hosts(hosts)
                    .prefixedWith(prefix)
                    .index(index)
                    .indexDateFormat(indexDateFormat)
                    .additionalFields(additionalFields)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("can't build elasticsearch reporter", e);
        }
    }
}