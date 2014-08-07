/**
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

module.exports = {
  "items" : [
    {
      "StackServices" : {
        "comments" : "Apache Hadoop Distributed Processing Framework",
        "service_name" : "MAPREDUCE",
        "service_version" : "1.2.0.1.3.3.0",
        "stack_name" : "HDP",
        "stack_version" : "1.3.2"
      },
      "serviceComponents" : [
        {
          "StackServiceComponents" : {
            "component_category" : "MASTER",
            "component_name" : "HISTORYSERVER",
            "is_client" : false,
            "is_master" : true,
            "service_name" : "MAPREDUCE",
            "stack_name" : "HDP",
            "stack_version" : "1.3.2"
          },
          "dependencies": []
        },
        {
          "StackServiceComponents" : {
            "component_category" : "MASTER",
            "component_name" : "JOBTRACKER",
            "is_client" : false,
            "is_master" : true,
            "service_name" : "MAPREDUCE",
            "stack_name" : "HDP",
            "stack_version" : "1.3.2"
          },
          "dependencies": []
        },
        {
          "StackServiceComponents" : {
            "component_category" : "CLIENT",
            "component_name" : "MAPREDUCE_CLIENT",
            "is_client" : true,
            "is_master" : false,
            "service_name" : "MAPREDUCE",
            "stack_name" : "HDP",
            "stack_version" : "1.3.2"
          },
          "dependencies": []
        },
        {
          "StackServiceComponents" : {
            "component_category" : "SLAVE",
            "component_name" : "TASKTRACKER",
            "is_client" : false,
            "is_master" : false,
            "service_name" : "MAPREDUCE",
            "stack_name" : "HDP",
            "stack_version" : "1.3.2"
          },
          "dependencies": []
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/FALCON",
      "StackServices" : {
        "comments" : "Data management and processing platform",
        "custom_commands" : [ ],
        "service_check_supported" : true,
        "service_name" : "FALCON",
        "service_version" : "0.5.0.2.1",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "falcon-env" : {
            "supports" : {
              "final" : "false"
            }
          },
          "falcon-runtime.properties" : {
            "supports" : {
              "final" : "false"
            }
          },
          "falcon-startup.properties" : {
            "supports" : {
              "final" : "false"
            }
          },
          "oozie-site" : {
            "supports" : {
              "final" : "true"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/FALCON/serviceComponents/FALCON_CLIENT",
          "StackServiceComponents" : {
            "cardinality" : "1+",
            "component_category" : "CLIENT",
            "component_name" : "FALCON_CLIENT",
            "custom_commands" : [ ],
            "is_client" : true,
            "is_master" : false,
            "service_name" : "FALCON",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/FALCON/serviceComponents/FALCON_SERVER",
          "StackServiceComponents" : {
            "cardinality" : "1",
            "component_category" : "MASTER",
            "component_name" : "FALCON_SERVER",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "FALCON",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/FALCON/serviceComponents/FALCON_SERVER/dependencies/OOZIE_CLIENT",
              "Dependencies" : {
                "component_name" : "OOZIE_CLIENT",
                "dependent_component_name" : "FALCON_SERVER",
                "dependent_service_name" : "FALCON",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/FALCON/serviceComponents/FALCON_SERVER/dependencies/OOZIE_SERVER",
              "Dependencies" : {
                "component_name" : "OOZIE_SERVER",
                "dependent_component_name" : "FALCON_SERVER",
                "dependent_service_name" : "FALCON",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            }
          ]
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/FLUME",
      "StackServices" : {
        "comments" : "Data management and processing platform",
        "custom_commands" : [ ],
        "service_check_supported" : true,
        "service_name" : "FLUME",
        "service_version" : "1.4.0.2.1.1.0",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "flume-conf" : {
            "supports" : {
              "final" : "false"
            }
          },
          "flume-env" : {
            "supports" : {
              "final" : "false"
            }
          },
          "flume-log4j" : {
            "supports" : {
              "final" : "false"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/FLUME/serviceComponents/FLUME_HANDLER",
          "StackServiceComponents" : {
            "cardinality" : "0+",
            "component_category" : "SLAVE",
            "component_name" : "FLUME_HANDLER",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : false,
            "service_name" : "FLUME",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/GANGLIA",
      "StackServices" : {
        "comments" : "Ganglia Metrics Collection system (<a href=\"http://oss.oetiker.ch/rrdtool/\" target=\"_blank\">RRDTool</a> will be installed too)",
        "custom_commands" : [ ],
        "service_check_supported" : false,
        "service_name" : "GANGLIA",
        "service_version" : "3.5.0",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "ganglia-env" : {
            "supports" : {
              "final" : "false"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/GANGLIA/serviceComponents/GANGLIA_MONITOR",
          "StackServiceComponents" : {
            "cardinality" : "ALL",
            "component_category" : "SLAVE",
            "component_name" : "GANGLIA_MONITOR",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : false,
            "service_name" : "GANGLIA",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "auto_deploy" : {
            "enabled" : true
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/GANGLIA/serviceComponents/GANGLIA_SERVER",
          "StackServiceComponents" : {
            "cardinality" : "1",
            "component_category" : "MASTER",
            "component_name" : "GANGLIA_SERVER",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "GANGLIA",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HBASE",
      "StackServices" : {
        "comments" : "Non-relational distributed database and centralized service for configuration management &\n        synchronization\n      ",
        "custom_commands" : [ ],
        "service_check_supported" : true,
        "service_name" : "HBASE",
        "service_version" : "0.98.0.2.1",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "hbase-env" : {
            "supports" : {
              "final" : "false"
            }
          },
          "hbase-log4j" : {
            "supports" : {
              "final" : "false"
            }
          },
          "hbase-policy" : {
            "supports" : {
              "final" : "true"
            }
          },
          "hbase-site" : {
            "supports" : {
              "final" : "true"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HBASE/serviceComponents/HBASE_CLIENT",
          "StackServiceComponents" : {
            "cardinality" : "1+",
            "component_category" : "CLIENT",
            "component_name" : "HBASE_CLIENT",
            "custom_commands" : [ ],
            "is_client" : true,
            "is_master" : false,
            "service_name" : "HBASE",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HBASE/serviceComponents/HBASE_MASTER",
          "StackServiceComponents" : {
            "cardinality" : "1+",
            "component_category" : "MASTER",
            "component_name" : "HBASE_MASTER",
            "custom_commands" : [
              "DECOMMISSION"
            ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "HBASE",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HBASE/serviceComponents/HBASE_MASTER/dependencies/HDFS_CLIENT",
              "Dependencies" : {
                "component_name" : "HDFS_CLIENT",
                "dependent_component_name" : "HBASE_MASTER",
                "dependent_service_name" : "HBASE",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HBASE/serviceComponents/HBASE_MASTER/dependencies/ZOOKEEPER_SERVER",
              "Dependencies" : {
                "component_name" : "ZOOKEEPER_SERVER",
                "dependent_component_name" : "HBASE_MASTER",
                "dependent_service_name" : "HBASE",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            }
          ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HBASE/serviceComponents/HBASE_REGIONSERVER",
          "StackServiceComponents" : {
            "cardinality" : "1+",
            "component_category" : "SLAVE",
            "component_name" : "HBASE_REGIONSERVER",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : false,
            "service_name" : "HBASE",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HCATALOG",
      "StackServices" : {
        "comments" : "This is comment for HCATALOG service",
        "custom_commands" : [ ],
        "service_check_supported" : true,
        "service_name" : "HCATALOG",
        "service_version" : "0.12.0.2.1",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "hive-env" : {
            "supports" : {
              "final" : "false"
            }
          },
          "hive-site" : {
            "supports" : {
              "final" : "true"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HCATALOG/serviceComponents/HCAT",
          "StackServiceComponents" : {
            "cardinality" : null,
            "component_category" : "CLIENT",
            "component_name" : "HCAT",
            "custom_commands" : [ ],
            "is_client" : true,
            "is_master" : false,
            "service_name" : "HCATALOG",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HDFS",
      "StackServices" : {
        "comments" : "Apache Hadoop Distributed File System",
        "custom_commands" : [ ],
        "service_check_supported" : true,
        "service_name" : "HDFS",
        "service_version" : "2.4.0.2.1",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "core-site" : {
            "supports" : {
              "final" : "true"
            }
          },
          "hadoop-env" : {
            "supports" : {
              "final" : "false"
            }
          },
          "hadoop-policy" : {
            "supports" : {
              "final" : "true"
            }
          },
          "hdfs-log4j" : {
            "supports" : {
              "final" : "false"
            }
          },
          "hdfs-site" : {
            "supports" : {
              "final" : "true"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HDFS/serviceComponents/DATANODE",
          "StackServiceComponents" : {
            "cardinality" : "1+",
            "component_category" : "SLAVE",
            "component_name" : "DATANODE",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : false,
            "service_name" : "HDFS",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HDFS/serviceComponents/HDFS_CLIENT",
          "StackServiceComponents" : {
            "cardinality" : "1+",
            "component_category" : "CLIENT",
            "component_name" : "HDFS_CLIENT",
            "custom_commands" : [ ],
            "is_client" : true,
            "is_master" : false,
            "service_name" : "HDFS",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HDFS/serviceComponents/JOURNALNODE",
          "StackServiceComponents" : {
            "cardinality" : "0+",
            "component_category" : "SLAVE",
            "component_name" : "JOURNALNODE",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : false,
            "service_name" : "HDFS",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HDFS/serviceComponents/NAMENODE",
          "StackServiceComponents" : {
            "cardinality" : "1-2",
            "component_category" : "MASTER",
            "component_name" : "NAMENODE",
            "custom_commands" : [
              "DECOMMISSION"
            ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "HDFS",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HDFS/serviceComponents/SECONDARY_NAMENODE",
          "StackServiceComponents" : {
            "cardinality" : "1",
            "component_category" : "MASTER",
            "component_name" : "SECONDARY_NAMENODE",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "HDFS",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HDFS/serviceComponents/ZKFC",
          "StackServiceComponents" : {
            "cardinality" : "0+",
            "component_category" : "SLAVE",
            "component_name" : "ZKFC",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : false,
            "service_name" : "HDFS",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HIVE",
      "StackServices" : {
        "comments" : "Data warehouse system for ad-hoc queries & analysis of large datasets and table & storage management service",
        "custom_commands" : [ ],
        "service_check_supported" : true,
        "service_name" : "HIVE",
        "service_version" : "0.13.0.2.1",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "hive-env" : {
            "supports" : {
              "final" : "false"
            }
          },
          "hive-exec-log4j" : {
            "supports" : {
              "final" : "false"
            }
          },
          "hive-log4j" : {
            "supports" : {
              "final" : "false"
            }
          },
          "hive-site" : {
            "supports" : {
              "final" : "true"
            }
          },
          "tez-site" : {
            "supports" : {
              "final" : "false"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HIVE/serviceComponents/HIVE_CLIENT",
          "StackServiceComponents" : {
            "cardinality" : "1+",
            "component_category" : "CLIENT",
            "component_name" : "HIVE_CLIENT",
            "custom_commands" : [ ],
            "is_client" : true,
            "is_master" : false,
            "service_name" : "HIVE",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HIVE/serviceComponents/HIVE_METASTORE",
          "StackServiceComponents" : {
            "cardinality" : "1",
            "component_category" : "MASTER",
            "component_name" : "HIVE_METASTORE",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "HIVE",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "auto_deploy" : {
            "enabled" : true,
            "location" : "HIVE/HIVE_SERVER"
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HIVE/serviceComponents/HIVE_SERVER",
          "StackServiceComponents" : {
            "cardinality" : "1",
            "component_category" : "MASTER",
            "component_name" : "HIVE_SERVER",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "HIVE",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HIVE/serviceComponents/HIVE_SERVER/dependencies/MAPREDUCE2_CLIENT",
              "Dependencies" : {
                "component_name" : "MAPREDUCE2_CLIENT",
                "dependent_component_name" : "HIVE_SERVER",
                "dependent_service_name" : "HIVE",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HIVE/serviceComponents/HIVE_SERVER/dependencies/TEZ_CLIENT",
              "Dependencies" : {
                "component_name" : "TEZ_CLIENT",
                "dependent_component_name" : "HIVE_SERVER",
                "dependent_service_name" : "HIVE",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HIVE/serviceComponents/HIVE_SERVER/dependencies/YARN_CLIENT",
              "Dependencies" : {
                "component_name" : "YARN_CLIENT",
                "dependent_component_name" : "HIVE_SERVER",
                "dependent_service_name" : "HIVE",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HIVE/serviceComponents/HIVE_SERVER/dependencies/ZOOKEEPER_SERVER",
              "Dependencies" : {
                "component_name" : "ZOOKEEPER_SERVER",
                "dependent_component_name" : "HIVE_SERVER",
                "dependent_service_name" : "HIVE",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            }
          ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/HIVE/serviceComponents/MYSQL_SERVER",
          "StackServiceComponents" : {
            "cardinality" : "0-1",
            "component_category" : "MASTER",
            "component_name" : "MYSQL_SERVER",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "HIVE",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/MAPREDUCE2",
      "StackServices" : {
        "comments" : "Apache Hadoop NextGen MapReduce (YARN)",
        "custom_commands" : [ ],
        "service_check_supported" : true,
        "service_name" : "MAPREDUCE2",
        "service_version" : "2.1.0.2.0.6.0",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "core-site" : {
            "supports" : {
              "final" : "true"
            }
          },
          "mapred-env" : {
            "supports" : {
              "final" : "false"
            }
          },
          "mapred-queue-acls" : {
            "supports" : {
              "final" : "true"
            }
          },
          "mapred-site" : {
            "supports" : {
              "final" : "true"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/MAPREDUCE2/serviceComponents/HISTORYSERVER",
          "StackServiceComponents" : {
            "cardinality" : "1",
            "component_category" : "MASTER",
            "component_name" : "HISTORYSERVER",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "MAPREDUCE2",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "auto_deploy" : {
            "enabled" : true,
            "location" : "YARN/RESOURCEMANAGER"
          },
          "dependencies" : [
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/MAPREDUCE2/serviceComponents/HISTORYSERVER/dependencies/HDFS_CLIENT",
              "Dependencies" : {
                "component_name" : "HDFS_CLIENT",
                "dependent_component_name" : "HISTORYSERVER",
                "dependent_service_name" : "MAPREDUCE2",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            }
          ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/MAPREDUCE2/serviceComponents/MAPREDUCE2_CLIENT",
          "StackServiceComponents" : {
            "cardinality" : "0+",
            "component_category" : "CLIENT",
            "component_name" : "MAPREDUCE2_CLIENT",
            "custom_commands" : [ ],
            "is_client" : true,
            "is_master" : false,
            "service_name" : "MAPREDUCE2",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/NAGIOS",
      "StackServices" : {
        "comments" : "Nagios Monitoring and Alerting system",
        "custom_commands" : [ ],
        "service_check_supported" : false,
        "service_name" : "NAGIOS",
        "service_version" : "3.5.0",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "nagios-env" : {
            "supports" : {
              "final" : "false"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/NAGIOS/serviceComponents/NAGIOS_SERVER",
          "StackServiceComponents" : {
            "cardinality" : "1",
            "component_category" : "MASTER",
            "component_name" : "NAGIOS_SERVER",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "NAGIOS",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/NAGIOS/serviceComponents/NAGIOS_SERVER/dependencies/HCAT",
              "Dependencies" : {
                "component_name" : "HCAT",
                "dependent_component_name" : "NAGIOS_SERVER",
                "dependent_service_name" : "NAGIOS",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/NAGIOS/serviceComponents/NAGIOS_SERVER/dependencies/HDFS_CLIENT",
              "Dependencies" : {
                "component_name" : "HDFS_CLIENT",
                "dependent_component_name" : "NAGIOS_SERVER",
                "dependent_service_name" : "NAGIOS",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/NAGIOS/serviceComponents/NAGIOS_SERVER/dependencies/MAPREDUCE2_CLIENT",
              "Dependencies" : {
                "component_name" : "MAPREDUCE2_CLIENT",
                "dependent_component_name" : "NAGIOS_SERVER",
                "dependent_service_name" : "NAGIOS",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/NAGIOS/serviceComponents/NAGIOS_SERVER/dependencies/OOZIE_CLIENT",
              "Dependencies" : {
                "component_name" : "OOZIE_CLIENT",
                "dependent_component_name" : "NAGIOS_SERVER",
                "dependent_service_name" : "NAGIOS",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/NAGIOS/serviceComponents/NAGIOS_SERVER/dependencies/TEZ_CLIENT",
              "Dependencies" : {
                "component_name" : "TEZ_CLIENT",
                "dependent_component_name" : "NAGIOS_SERVER",
                "dependent_service_name" : "NAGIOS",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/NAGIOS/serviceComponents/NAGIOS_SERVER/dependencies/YARN_CLIENT",
              "Dependencies" : {
                "component_name" : "YARN_CLIENT",
                "dependent_component_name" : "NAGIOS_SERVER",
                "dependent_service_name" : "NAGIOS",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            }
          ]
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/OOZIE",
      "StackServices" : {
        "comments" : "System for workflow coordination and execution of Apache Hadoop jobs.  This also includes the installation of the optional Oozie Web Console which relies on and will install the <a target=\"_blank\" href=\"http://www.sencha.com/legal/open-source-faq/\">ExtJS</a> Library.\n      ",
        "custom_commands" : [ ],
        "service_check_supported" : true,
        "service_name" : "OOZIE",
        "service_version" : "4.0.0.2.1",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "oozie-env" : {
            "supports" : {
              "final" : "false"
            }
          },
          "oozie-log4j" : {
            "supports" : {
              "final" : "false"
            }
          },
          "oozie-site" : {
            "supports" : {
              "final" : "true"
            }
          },
          "yarn-site" : {
            "supports" : {
              "final" : "false"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/OOZIE/serviceComponents/OOZIE_CLIENT",
          "StackServiceComponents" : {
            "cardinality" : "1+",
            "component_category" : "CLIENT",
            "component_name" : "OOZIE_CLIENT",
            "custom_commands" : [ ],
            "is_client" : true,
            "is_master" : false,
            "service_name" : "OOZIE",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/OOZIE/serviceComponents/OOZIE_CLIENT/dependencies/HDFS_CLIENT",
              "Dependencies" : {
                "component_name" : "HDFS_CLIENT",
                "dependent_component_name" : "OOZIE_CLIENT",
                "dependent_service_name" : "OOZIE",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/OOZIE/serviceComponents/OOZIE_CLIENT/dependencies/MAPREDUCE2_CLIENT",
              "Dependencies" : {
                "component_name" : "MAPREDUCE2_CLIENT",
                "dependent_component_name" : "OOZIE_CLIENT",
                "dependent_service_name" : "OOZIE",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            }
          ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/OOZIE/serviceComponents/OOZIE_SERVER",
          "StackServiceComponents" : {
            "cardinality" : "1",
            "component_category" : "MASTER",
            "component_name" : "OOZIE_SERVER",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "OOZIE",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/OOZIE/serviceComponents/OOZIE_SERVER/dependencies/HDFS_CLIENT",
              "Dependencies" : {
                "component_name" : "HDFS_CLIENT",
                "dependent_component_name" : "OOZIE_SERVER",
                "dependent_service_name" : "OOZIE",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/OOZIE/serviceComponents/OOZIE_SERVER/dependencies/MAPREDUCE2_CLIENT",
              "Dependencies" : {
                "component_name" : "MAPREDUCE2_CLIENT",
                "dependent_component_name" : "OOZIE_SERVER",
                "dependent_service_name" : "OOZIE",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/OOZIE/serviceComponents/OOZIE_SERVER/dependencies/YARN_CLIENT",
              "Dependencies" : {
                "component_name" : "YARN_CLIENT",
                "dependent_component_name" : "OOZIE_SERVER",
                "dependent_service_name" : "OOZIE",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            }
          ]
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/PIG",
      "StackServices" : {
        "comments" : "Scripting platform for analyzing large datasets",
        "custom_commands" : [ ],
        "service_check_supported" : true,
        "service_name" : "PIG",
        "service_version" : "0.12.1.2.1",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "pig-env" : {
            "supports" : {
              "final" : "false"
            }
          },
          "pig-log4j" : {
            "supports" : {
              "final" : "false"
            }
          },
          "pig-properties" : {
            "supports" : {
              "final" : "false"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/PIG/serviceComponents/PIG",
          "StackServiceComponents" : {
            "cardinality" : "0+",
            "component_category" : "CLIENT",
            "component_name" : "PIG",
            "custom_commands" : [ ],
            "is_client" : true,
            "is_master" : false,
            "service_name" : "PIG",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/SQOOP",
      "StackServices" : {
        "comments" : "Tool for transferring bulk data between Apache Hadoop and\n        structured data stores such as relational databases\n      ",
        "custom_commands" : [ ],
        "service_check_supported" : true,
        "service_name" : "SQOOP",
        "service_version" : "1.4.4.2.1",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "sqoop-env" : {
            "supports" : {
              "final" : "false"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/SQOOP/serviceComponents/SQOOP",
          "StackServiceComponents" : {
            "cardinality" : "1+",
            "component_category" : "CLIENT",
            "component_name" : "SQOOP",
            "custom_commands" : [ ],
            "is_client" : true,
            "is_master" : false,
            "service_name" : "SQOOP",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/SQOOP/serviceComponents/SQOOP/dependencies/HDFS_CLIENT",
              "Dependencies" : {
                "component_name" : "HDFS_CLIENT",
                "dependent_component_name" : "SQOOP",
                "dependent_service_name" : "SQOOP",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/SQOOP/serviceComponents/SQOOP/dependencies/MAPREDUCE2_CLIENT",
              "Dependencies" : {
                "component_name" : "MAPREDUCE2_CLIENT",
                "dependent_component_name" : "SQOOP",
                "dependent_service_name" : "SQOOP",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            }
          ]
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/STORM",
      "StackServices" : {
        "comments" : "Apache Hadoop Stream processing framework",
        "custom_commands" : [ ],
        "service_check_supported" : true,
        "service_name" : "STORM",
        "service_version" : "0.9.1.2.1",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "storm-env" : {
            "supports" : {
              "final" : "false"
            }
          },
          "storm-site" : {
            "supports" : {
              "final" : "true"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/STORM/serviceComponents/DRPC_SERVER",
          "StackServiceComponents" : {
            "cardinality" : "1",
            "component_category" : "MASTER",
            "component_name" : "DRPC_SERVER",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "STORM",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/STORM/serviceComponents/NIMBUS",
          "StackServiceComponents" : {
            "cardinality" : "1",
            "component_category" : "MASTER",
            "component_name" : "NIMBUS",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "STORM",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/STORM/serviceComponents/NIMBUS/dependencies/ZOOKEEPER_SERVER",
              "Dependencies" : {
                "component_name" : "ZOOKEEPER_SERVER",
                "dependent_component_name" : "NIMBUS",
                "dependent_service_name" : "STORM",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            }
          ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/STORM/serviceComponents/STORM_REST_API",
          "StackServiceComponents" : {
            "cardinality" : "1",
            "component_category" : "MASTER",
            "component_name" : "STORM_REST_API",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "STORM",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/STORM/serviceComponents/STORM_UI_SERVER",
          "StackServiceComponents" : {
            "cardinality" : "1",
            "component_category" : "MASTER",
            "component_name" : "STORM_UI_SERVER",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "STORM",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/STORM/serviceComponents/SUPERVISOR",
          "StackServiceComponents" : {
            "cardinality" : "1+",
            "component_category" : "SLAVE",
            "component_name" : "SUPERVISOR",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : false,
            "service_name" : "STORM",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/TEZ",
      "StackServices" : {
        "comments" : "Tez is the next generation Hadoop Query Processing framework written on top of YARN.",
        "custom_commands" : [ ],
        "service_check_supported" : false,
        "service_name" : "TEZ",
        "service_version" : "0.4.0.2.1",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "tez-env" : {
            "supports" : {
              "final" : "false"
            }
          },
          "tez-site" : {
            "supports" : {
              "final" : "true"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/TEZ/serviceComponents/TEZ_CLIENT",
          "StackServiceComponents" : {
            "cardinality" : "1+",
            "component_category" : "CLIENT",
            "component_name" : "TEZ_CLIENT",
            "custom_commands" : [ ],
            "is_client" : true,
            "is_master" : false,
            "service_name" : "TEZ",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/WEBHCAT",
      "StackServices" : {
        "comments" : "This is comment for WEBHCAT service",
        "custom_commands" : [ ],
        "service_check_supported" : true,
        "service_name" : "WEBHCAT",
        "service_version" : "0.13.0.2.1",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "webhcat-env" : {
            "supports" : {
              "final" : "false"
            }
          },
          "webhcat-site" : {
            "supports" : {
              "final" : "true"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/WEBHCAT/serviceComponents/WEBHCAT_SERVER",
          "StackServiceComponents" : {
            "cardinality" : "1",
            "component_category" : "MASTER",
            "component_name" : "WEBHCAT_SERVER",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "WEBHCAT",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/WEBHCAT/serviceComponents/WEBHCAT_SERVER/dependencies/HDFS_CLIENT",
              "Dependencies" : {
                "component_name" : "HDFS_CLIENT",
                "dependent_component_name" : "WEBHCAT_SERVER",
                "dependent_service_name" : "WEBHCAT",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/WEBHCAT/serviceComponents/WEBHCAT_SERVER/dependencies/MAPREDUCE2_CLIENT",
              "Dependencies" : {
                "component_name" : "MAPREDUCE2_CLIENT",
                "dependent_component_name" : "WEBHCAT_SERVER",
                "dependent_service_name" : "WEBHCAT",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/WEBHCAT/serviceComponents/WEBHCAT_SERVER/dependencies/YARN_CLIENT",
              "Dependencies" : {
                "component_name" : "YARN_CLIENT",
                "dependent_component_name" : "WEBHCAT_SERVER",
                "dependent_service_name" : "WEBHCAT",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/WEBHCAT/serviceComponents/WEBHCAT_SERVER/dependencies/ZOOKEEPER_CLIENT",
              "Dependencies" : {
                "component_name" : "ZOOKEEPER_CLIENT",
                "dependent_component_name" : "WEBHCAT_SERVER",
                "dependent_service_name" : "WEBHCAT",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            },
            {
              "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/WEBHCAT/serviceComponents/WEBHCAT_SERVER/dependencies/ZOOKEEPER_SERVER",
              "Dependencies" : {
                "component_name" : "ZOOKEEPER_SERVER",
                "dependent_component_name" : "WEBHCAT_SERVER",
                "dependent_service_name" : "WEBHCAT",
                "stack_name" : "HDP",
                "stack_version" : "2.1"
              }
            }
          ]
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/YARN",
      "StackServices" : {
        "comments" : "Apache Hadoop NextGen MapReduce (YARN)",
        "custom_commands" : [ ],
        "service_check_supported" : true,
        "service_name" : "YARN",
        "service_version" : "2.4.0.2.1",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "capacity-scheduler" : {
            "supports" : {
              "final" : "true"
            }
          },
          "core-site" : {
            "supports" : {
              "final" : "true"
            }
          },
          "yarn-env" : {
            "supports" : {
              "final" : "false"
            }
          },
          "yarn-log4j" : {
            "supports" : {
              "final" : "false"
            }
          },
          "yarn-site" : {
            "supports" : {
              "final" : "true"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/YARN/serviceComponents/APP_TIMELINE_SERVER",
          "StackServiceComponents" : {
            "cardinality" : "0-1",
            "component_category" : "MASTER",
            "component_name" : "APP_TIMELINE_SERVER",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "YARN",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/YARN/serviceComponents/NODEMANAGER",
          "StackServiceComponents" : {
            "cardinality" : "1+",
            "component_category" : "SLAVE",
            "component_name" : "NODEMANAGER",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : false,
            "service_name" : "YARN",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/YARN/serviceComponents/RESOURCEMANAGER",
          "StackServiceComponents" : {
            "cardinality" : "1-2",
            "component_category" : "MASTER",
            "component_name" : "RESOURCEMANAGER",
            "custom_commands" : [
              "DECOMMISSION",
              "REFRESHQUEUES"
            ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "YARN",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/YARN/serviceComponents/YARN_CLIENT",
          "StackServiceComponents" : {
            "cardinality" : "1+",
            "component_category" : "CLIENT",
            "component_name" : "YARN_CLIENT",
            "custom_commands" : [ ],
            "is_client" : true,
            "is_master" : false,
            "service_name" : "YARN",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        }
      ]
    },
    {
      "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/ZOOKEEPER",
      "StackServices" : {
        "comments" : "Centralized service which provides highly reliable distributed\n        coordination.",
        "custom_commands" : [ ],
        "service_check_supported" : true,
        "service_name" : "ZOOKEEPER",
        "service_version" : "3.4.5.2.1",
        "stack_name" : "HDP",
        "stack_version" : "2.1",
        "user_name" : null,
        "config_types" : {
          "zoo.cfg" : {
            "supports" : {
              "final" : "false"
            }
          },
          "zookeeper-env" : {
            "supports" : {
              "final" : "false"
            }
          },
          "zookeeper-log4j" : {
            "supports" : {
              "final" : "false"
            }
          }
        }
      },
      "serviceComponents" : [
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/ZOOKEEPER/serviceComponents/ZOOKEEPER_CLIENT",
          "StackServiceComponents" : {
            "cardinality" : "1+",
            "component_category" : "CLIENT",
            "component_name" : "ZOOKEEPER_CLIENT",
            "custom_commands" : [ ],
            "is_client" : true,
            "is_master" : false,
            "service_name" : "ZOOKEEPER",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        },
        {
          "href" : "http://c6401.ambari.apache.org:8080/api/v1/stacks2/HDP/versions/2.1/stackServices/ZOOKEEPER/serviceComponents/ZOOKEEPER_SERVER",
          "StackServiceComponents" : {
            "cardinality" : "1+",
            "component_category" : "MASTER",
            "component_name" : "ZOOKEEPER_SERVER",
            "custom_commands" : [ ],
            "is_client" : false,
            "is_master" : true,
            "service_name" : "ZOOKEEPER",
            "stack_name" : "HDP",
            "stack_version" : "2.1"
          },
          "dependencies" : [ ]
        }
      ]
    }
  ]
};
