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
'use strict';

angular.module('ambariAdminConsole')
.factory('RemoteCluster', ['$http', '$q', 'Settings', function($http, $q, Settings) {

  function RemoteClusterServiceConfigurations(item){
     var self = this;
     self.name = item.serviceInfo.name;
     self.parameters = item.serviceInfo.parameters;
  }

  function RemoteCluster(item){
      var self = this;
      self.name = item.ViewClusterInstanceInfo.name;
      self.services = item.ViewClusterInstanceInfo.services;
    }

  RemoteCluster.all = function(){
    var deferred = $q.defer();
    var fields = [
          'ViewClusterInstanceInfo/services',
        ];

      $http({
        method: 'GET',
        url: Settings.baseUrl + '/viewclusters',
        params:{
                'fields': fields.join(',')
              }
      }).success(function(data) {
        var clusters = [];
        angular.forEach(data.items, function(item) {
          clusters.push(new RemoteCluster(item));
        });
        deferred.resolve(clusters);
      })
      .error(function(data) {
        deferred.reject(data);
      });

      return deferred.promise;
  };

  RemoteCluster.getConfigurations = function(){
      var deferred = $q.defer();
      var fields = [
            'serviceInfo/parameters',
          ];

        $http({
          method: 'GET',
          url: Settings.baseUrl + '/viewservice',
          params:{
                  'fields': fields.join(',')
                }
        }).success(function(data) {
          var services = [];
          angular.forEach(data.items, function(item) {
            services.push(new RemoteClusterServiceConfigurations(item));
          });
          deferred.resolve(services);
        })
        .error(function(data) {
          deferred.reject(data);
        });

        return deferred.promise;
  };

  RemoteCluster.getCluster = function(clusterName){
    return $http({
      method: 'GET',
      url: Settings.baseUrl + '/viewclusters/'+clusterName
    });
  };


  RemoteCluster.createInstance = function(instanceInfo,isUpdate) {
    var deferred = $q.defer();

    var services = [];

    angular.forEach(instanceInfo.services, function(item) {
      var properties = {};
      angular.forEach(item.parameters, function(property) {
          console.log(property);
          properties[property.name] = property.value;
      });

      var service = {
        'name' : item.name,
        'properties': properties
      };

      services.push(service);
    });

    var data = {
      'name' : instanceInfo.name,
      'services': services
    };

    var method = isUpdate ? 'PUT' : 'POST'

    $http({
      method: method,
      url: Settings.baseUrl + '/viewclusters',
      data:{
        'ViewClusterInstanceInfo' : data
      }
    })
    .success(function(data) {
      deferred.resolve(data);
    })
    .error(function(data) {
      deferred.reject(data);
    });

    return deferred.promise;
  };

  return RemoteCluster;
}]);