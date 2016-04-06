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
.controller('RemoteClusterInstanceCtrl',['$scope','$route', 'RemoteCluster', 'Alert', 'Cluster', '$routeParams', '$location', 'UnsavedDialog', function($scope, $route, RemoteCluster, Alert, Cluster, $routeParams, $location, UnsavedDialog) {
  $scope.form = {};
  var targetUrl = '';

  function loadConfigurations(){
    RemoteCluster.getConfigurations().then(function(services){
      $scope.services = {};
      services.forEach(function(service){
        $scope.services[service.name] = service;
      });
      loadViews();
    }).catch(function(data) {
      Alert.error('Cannot load view cluster configurations', data.data.message);
    });
  };

  function loadViews(){
    RemoteCluster.getViewServices().then(function(views){
      $scope.views = [];
      views.forEach(function(view){
        view.isChecked = checkIfViewSelected(view);
        $scope.views.push(view);
      });
      refreshInstanceData(true);
    }).catch(function(data) {
      Alert.error('Cannot load views configurations', data.data.message);
    });
  };

  function checkIfViewSelected(view){
    if($scope.selectedServices && $scope.selectedServices.length > 0){
      var checked = true;
      view.services.forEach(function(serviceName){
        if($scope.selectedServices.indexOf(serviceName) == -1){
          checked = false;
          return;
        }
      });
      return checked;
    }
    return false;
  };

  $scope.isEditPage = $route.current.$$route.isEditPage;
  $scope.enableInputs = true;
  if($scope.isEditPage) $scope.enableInputs = false;

  $scope.instance= {
    name : '',
    services : {}
  };

  if($scope.isEditPage){
    $scope.title = $routeParams.clusterName;
    RemoteCluster.getCluster($routeParams.clusterName).then(function(data){
      var clusterData = data.data.ViewClusterInstanceInfo;
      $scope.clusterData = {
        'name' : clusterData.name,
        'services':{}
      };

      $scope.instance.name = $scope.clusterData.name;

      $scope.selectedServices = [];
      clusterData.services.forEach(function(service){
        $scope.selectedServices.push(service.name);
        $scope.clusterData.services[service.name]= service;
      });

      loadConfigurations();
    });
  }else{
    $scope.title = "Create Cluster"
    loadConfigurations();
  }

  $scope.nameValidationPattern = /^\s*\w*\s*$/;

  $scope.cancel = function(){
    if($scope.isEditPage){
      $scope.enableInputs = false;
    }else{
      $location.path('remoteclusters');
    }
  };

  $scope.save = function(){
    if (!$scope.form.remoteclusterform.isSaving) {
      $scope.form.remoteclusterform.submitted = true;
       if($scope.form.remoteclusterform.$valid){
         $scope.form.remoteclusterform.isSaving = true;
         RemoteCluster.createInstance(getClusterInstance(),$scope.isEditPage)
          .then(function(data) {
            if($scope.isEditPage){
              Alert.success('Updated Cluster Instance ' + $scope.instance.name);
            }else{
              Alert.success('Created Cluster Instance ' + $scope.instance.name);
            }
              $scope.enableInputs = false;
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

  function getClusterInstance(){
    var instance = {};
    instance.name = $scope.instance.name;
    instance.services = [];

    $scope.selectedServices.forEach(function(serviceName){
      var service = $scope.services[serviceName];
      if(service){
        var instanceService = {};
        instanceService.name = service.name;
        var properties = {};
        service.parameters.forEach(function(parameter){
        var value = $scope.instance.services[service.commonName].parameters[parameter.name].value;
         if(value) properties[parameter.name] = value;
        });
        instanceService.properties = properties ;
        instance.services.push(instanceService);
      }
    });
    return instance;
  };

  function refreshInstanceData(fillFromDataCluster){
    $scope.instance.services = {};
      $scope.selectedServices = [];
      $scope.views.forEach(function(view){
        if(view.isChecked){
          view.services.forEach(function(serviceName){
            if($scope.selectedServices.indexOf(serviceName) == -1){
              $scope.selectedServices.push(serviceName);
            }
            var service = $scope.services[serviceName];
            if(service) {
            //merging parameters for same service name (different versions)
              console.log(service)
              var instanceService = $scope.instance.services[service.commonName];
              if(!instanceService){
                instanceService = {
                  'name' : service.commonName,
                  'parameters' : {},
                  'parameterOrder' : []
                };
              }
              service.parameters.forEach(function(parameter){
                if(!instanceService.parameters[parameter.name]){
                  instanceService.parameters[parameter.name] = parameter;
                  if(fillFromDataCluster && $scope.isEditPage && $scope.clusterData){
                    if($scope.clusterData.services[serviceName]
                    && $scope.clusterData.services[serviceName].properties[parameter.name]){
                      parameter.value = $scope.clusterData.services[serviceName].properties[parameter.name];
                    }
                  }
                  instanceService.parameterOrder.push(parameter.name);
                }
              })
              $scope.instance.services[service.commonName] = instanceService;
            }
          });
        }
     });
  };

  $scope.onViewSelectionChange = function(){
    refreshInstanceData(false);
  };

  $scope.toggleEdit = function(){
    $scope.enableInputs = !$scope.enableInputs;
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

