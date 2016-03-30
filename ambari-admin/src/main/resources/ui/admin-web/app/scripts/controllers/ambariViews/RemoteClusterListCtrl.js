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
.controller('RemoteClusterListCtrl',['$scope', 'RemoteCluster', '$modal', 'Alert', 'ConfirmationModal', '$location', function($scope, RemoteCluster, $modal, Alert, ConfirmationModal, $location) {
  var deferredList = [];
  $scope.$on('$locationChangeStart', function() {
    deferredList.forEach(function(def) {
      def.reject();
    })
  });

  function loadClusters(){
    RemoteCluster.all().then(function(clusters) {
      $scope.clusters = clusters;
//      $scope.getFilteredViews();
//      angular.forEach(views, function(view) {
//        angular.forEach(view.versions, function(versionObj, versionNumber) {
//          if (versionObj.status !== 'DEPLOYED' || versionObj.status !== 'ERROR'){
//            checkViewVersionStatus(view, versionObj, versionNumber);
//          }
//        });
//      })
    }).catch(function(data) {
      Alert.error('Cannot load views', data.data.message);
    });
  }

  loadClusters();

  $scope.create = function() {
   console.log("creating");
   $location.path('/remoteclusters/create')
  };

//  $scope.viewsFilter = '';
//  $scope.filteredViews = [];
//  $scope.getFilteredViews = function(views) {
//    var result = [];
//    var filter = $scope.viewsFilter.toLowerCase();
//    if(!filter){  // if no filter return all views
//      result = $scope.views.map(function(view) {
//        view.isOpened = false;
//        return view;
//      });
//    } else {
//      result = $scope.views.map(function(view) {
//        view.isOpened = true;
//        if(view.view_name.toLowerCase().indexOf(filter) >= 0){
//          return view; // if filter matched with view name -- return whole view
//        } else {
//          var instances = [];
//          angular.forEach(view.instances, function(instance) {
//            if(instance.ViewInstanceInfo.label.toLowerCase().indexOf(filter) >= 0){
//              instances.push(instance);
//            }
//          });
//          if( instances.length ){ // If inside view exists instances with matched filter - show only this instances
//            var v = angular.copy(view);
//            v.instances = instances;
//            return v;
//          }
//        }
//      }).filter(function(view) {
//        return !!view; // Remove 'undefined'
//      });
//    }
//    $scope.filteredViews = result;
//  };

//  $scope.gotoCreate = function(viewName, isAllowed) {
//    if(isAllowed){
//      $location.path('/views/'+viewName+'/new');
//    }
//  };
}]);