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
.controller('RemoteClusterInstanceCtrl',['$scope', 'RemoteCluster', 'Alert', 'Cluster', '$routeParams', '$location', 'UnsavedDialog', function($scope, RemoteCluster, Alert, Cluster, $routeParams, $location, UnsavedDialog) {
  $scope.form = {};
  var targetUrl = '';

  function loadConfigurations(){
    RemoteCluster.getConfigurations().then(function(services){
      console.log(services);
      $scope.instance= {
        name : '',
        services : services
      };
    }).catch(function(data) {
      Alert.error('Cannot load view cluster configurations', data.data.message);
    });
  };

  loadConfigurations();


  $scope.nameValidationPattern = /^\s*\w*\s*$/;

  $scope.cancel = function(){
    $location.path('remoteclusters');
  };

  $scope.save = function(){
    if (!$scope.form.remoteclusterform.isSaving) {
      $scope.form.remoteclusterform.submitted = true;
       if($scope.form.remoteclusterform.$valid){
         $scope.form.remoteclusterform.isSaving = true;
         RemoteCluster.createInstance($scope.instance,false)
          .then(function(data) {
            Alert.success('Created View Instance ' + $scope.instance.name);
              $scope.form.remoteclusterform.$setPristine();
              if( targetUrl ){
                $location.path(targetUrl);
              } else {
                $location.path('/remoteclusters/' + $scope.instance.name + '/edit');
              }
                $scope.form.remoteclusterform.isSaving = false;
                $scope.$root.$emit('remoteclusterUpdate');
              })
          .catch(function (data) {
              var errorMessage = data.message;

              if (data.status >= 400) {
                try {
                    $scope.form.instanceCreateForm.generalValidationError = errorMessage;
                } catch (e) {
                  console.error('Unable to parse error message:', data.message);
                }
              }
              Alert.error('Cannot create cluster instance', errorMessage);
              $scope.form.remoteclusterform.isSaving = false;
          });
       }
    }
  };

  $scope.$on('$locationChangeStart', function(event, __targetUrl) {
    if( $scope.form.remoteclusterform.$dirty ){
      UnsavedDialog().then(function(action) {
        targetUrl = __targetUrl.split('#').pop();
        switch(action){
          case 'save':
            $scope.save();
            break;
          case 'discard':
            $scope.form.remoteclusterform.$setPristine();
            $location.path(targetUrl);
            break;
          case 'cancel':
            targetUrl = '';
            break;
        }
      });
      event.preventDefault();
    }
  });

}]);

